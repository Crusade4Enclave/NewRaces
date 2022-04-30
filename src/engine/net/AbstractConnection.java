// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net;

import engine.job.JobManager;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractConnection implements
NetMsgHandler {

	private static final String error01 = "A byte buffer is being free()ed that is not of the size stored in ByteBufferPool. Size: %1%";
	private static final String error02 = "(IP=%1%): Socket has reached the maximum outbound buffer length of %2%. Moving on while we wait for data to be sent.";

	protected final SocketChannel sockChan;
	protected final AbstractConnectionManager connMan;
	protected final NetMsgFactory factory;

	protected long lastMsgTime = System.currentTimeMillis();
	protected long lastKeepAliveTime = System.currentTimeMillis();
	protected long lastOpcode = -1;

	protected ConcurrentLinkedQueue<ByteBuffer> outbox = new ConcurrentLinkedQueue<>();
	protected ByteBuffer outBuf = null;

	protected final AtomicBoolean execTask = new AtomicBoolean(false);

	protected ConcurrentHashMap<Long, Byte> cacheList;

	protected final ReentrantLock writeLock = new ReentrantLock();
	protected final ReentrantLock readLock = new ReentrantLock();
	protected long nextWriteTime = 0L;
	protected Protocol lastProtocol = Protocol.NONE;
	protected static final long SOCKET_FULL_WRITE_DELAY = 50L; //wait one second to write on full socket

	//Opcode tracking

	private static final int OPCODEHASH = 1016;	//Opcodes are unique modulo this number as of 11/21/10

	public AbstractConnection(AbstractConnectionManager connMan,
			SocketChannel sockChan,  boolean clientMode) {
		this.connMan = connMan;
		this.sockChan = sockChan;
		this.factory = new NetMsgFactory(this);

	}

	public void disconnect() {
		try {
			this.sockChan.close();
		} catch (IOException e) {
			Logger.error(e.toString());
		}
	}

	/**
	 * Serializes AbstractNetMsg <i>msg</i> into a ByteBuffer, then queues that
	 * ByteBuffer for sending on this AbstractConnection's SocketChannel.
	 *
	 * @param msg
	 * - AbstractNetMsg to be sent.
	 * @return boolean status if queue is successful or not. On false return,
	 * the field <i>lastError</i> will be set.
	 */
	public final boolean sendMsg(AbstractNetMsg msg) {
		//		Logger.info("Send: " + msg.getSimpleClassName());
		try {
			return this._sendMsg(msg);

		} catch (Exception e) { // Catch-all
			Logger.error(e);
			return false;
		}
	}

	/**
	 * Serializes AbstractNetMsg <i>msg</i> into a ByteBuffer, then queues that
	 * ByteBuffer for sending on this AbstractConnection's SocketChannel. This
	 * internal function is <b>required</b> to be overridden by subclasses.
	 *
	 * @param msg
	 * - AbstractNetMsg to be sent.
	 * @return boolean status if queue is successful or not. On false return,
	 * the field <i>lastError</i> will be set.
	 */
	protected abstract boolean _sendMsg(AbstractNetMsg msg);

	/**
	 * Queues ByteBuffer <i>bb</i> for sending on this AbstractConnection's
	 * SocketChannel.
	 *
	 * @param bb
	 * - ByteBuffer to be sent.
	 * @return boolean status if queue is successful or not. On false return,
	 * the field <i>lastError</i> will be set.
	 */
	public final boolean sendBB(ByteBuffer bb) {
		
		if (bb == null)
			return false;
		try {
			return this._sendBB(bb);
		} catch (Exception e) { // Catch-all
			Logger.error(e);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Queues ByteBuffer <i>bb</i> for sending on this AbstractConnection's
	 * SocketChannel. This internal function is designed to be overrideable by
	 * subclasses if needed.
	 *
	 * @param bb
	 * - ByteBuffer to be sent.
	 * @return boolean status if queue is successful or not. On false return,
	 * the field <i>lastError</i> will be set.
	 */
	protected boolean _sendBB(ByteBuffer bb) {
		this.outbox.add(bb);
		this.connMan.sendStart(this.sockChan);
		return true;
	}

	/**
	 * Move data off the socketChannel's buffer and into the Connection's
	 * internal NetMsgFactory.
	 */
	// FIXME the int return value on this means nothing! Clean it up!
	protected int read() {

		if (readLock.tryLock())
			try {

				if (this.sockChan.isOpen() == false) {
					   Logger.info("Sock channel closed. Disconnecting " + this.getRemoteAddressAndPortAsString());
					this.disconnect();
					return 0;
				}

				// get socket buffer sized buffer from multipool
				ByteBuffer bb = Network.byteBufferPool.getBuffer(16);

				int totalBytesRead = 0;
				int lastRead;
				do {
					try {
						bb.clear();
						lastRead = this.sockChan.read(bb);

						// On EOF on the SocketChannel, disconnect.
						if (lastRead <= -1) {
							   Logger.info(" EOF on Socket Channel " + this.getRemoteAddressAndPortAsString());
							this.disconnect();
							break;
						}

						if (lastRead == 0)
							continue;

						synchronized (this.factory) {
							this.factory.addData(bb);
						}
						this.checkInternalFactory();

						totalBytesRead += lastRead;

					} catch (NotYetConnectedException e) {
						Logger.error(e.toString());
						break;

					} catch (ClosedChannelException e) {
						Logger.error(e.toString());
						this.disconnect();
						break;

					} catch (IOException e) {
						if (!e.getMessage().startsWith(
								"An existing connection was forcibly closed"))
							Logger.error(e.toString());
						this.disconnect();
						break;

					} catch (Exception e) {
						Logger.error(e.toString());
						break;
					}
				} while (lastRead > 0);

				// put buffer back into multipool
				Network.byteBufferPool.putBuffer(bb);

				this.checkInternalFactory();
				return totalBytesRead;

			} finally {
				readLock.unlock();
			}
		else {
			Logger.debug("Another thread already has a read lock! Skipping.");
			return 0;
		}
	}

	/**
	 * Move data off the Connection's buffer and into the SocketChannel's
	 * Buffer.
	 */
	protected boolean writeAll() {

		//intentional delay if socket is full. Give the socket a chance to clear out.
		if (System.currentTimeMillis() < this.nextWriteTime)
			return false;

		if (writeLock.tryLock())
			try {
				String s = "";
				int written = 0;

				boolean allSentOK = true, bufferDoubled = false;

				while (this.outbox.peek() != null) {
					ByteBuffer bb = this.outbox.peek();

					int toSend = bb.position();

					try {
						bb.flip();
						written = this.sockChan.write(bb);

						// Logger.debug("Using a BB with cap of: " + bb.capacity()
						// + ". toSend: " + toSend + ", written: " + written);
						if (written != toSend)
							bb.compact(); // Clean up in case not all gets sent

						if (toSend == written) {
							// Actually remove it from the queue
							this.outbox.poll();

							// Pool it
							Network.byteBufferPool.putBuffer(bb);
							continue;

						} else
							if (written == 0) {
								//Socket full, let's delay the next write on socket.
								this.nextWriteTime = System.currentTimeMillis() + AbstractConnection.SOCKET_FULL_WRITE_DELAY;

								int currentBufferSize = this.sockChan.socket()
										.getSendBufferSize();

								if (!bufferDoubled && currentBufferSize <= Network.INITIAL_SOCKET_BUFFER_SIZE) {
									this.doubleSocketSendBufferSize();
									bufferDoubled = true;
								} else {

									//                                    s = error02;
									//                                    s = s.replaceAll("%1%", this
									//                                            .getRemoteAddressAndPortAsString() + ":" +
									//                                            this.printLastOpcodes());
									//                                    s = s.replaceAll("%2%", currentBufferSize + "");
									//                                    this.warn(s);

									allSentOK = false;
									break;
								}
							}

					} catch (ClosedChannelException e) {
						// Catches AsynchronousCloseException,
						// and ClosedByInterruptException
						Logger.error(e);
						break;

					} catch (Exception e) {
						// Catches NotYetConnectedException
						// and IOException
						Logger.error(e);
						this.disconnect();
						break;
					}

				}
				return allSentOK;
			} finally {
				writeLock.unlock();
			}
		else {
			Logger.debug("Another thread already has a write lock! Skipping.");
			return false;
		}
	}

	private boolean doubleSocketSendBufferSize() {
		String s;
		try {
			int currentSize = this.sockChan.socket().getSendBufferSize();
			int newSize = currentSize << 1;

			this.sockChan.socket().setSendBufferSize(newSize);

			//            s = "(IP=" + this.getRemoteAddressAndPortAsString() + "): ";
			//            s += this.printLastOpcodes();
			//            s += "Socket has reached the maximum outbound buffer length of ";
			//            s += currentSize + ". Attempting to double the outbound buffer size.";
			//
			//            this.warn(s);
			return true;
		} catch (SocketException e) {
			Logger.error( e.toString());
			return false;
		}
	}

	public boolean isConnected() {
		return this.sockChan.isConnected();
	}

	public boolean isOpen() {
		return this.sockChan.isOpen();
	}

	/*
	 * Getters n Setters
	 */
	public SocketChannel getSocketChannel() {
		return this.sockChan;
	}

	public final void checkInternalFactory() {
		CheckNetMsgFactoryJob j = new CheckNetMsgFactoryJob(this);
		JobManager.getInstance().submitJob(j);
	}

	public NetMsgFactory getFactory() {
		return factory;
	}

	public String getRemoteAddressAndPortAsString() {
		String out = "";

		if (this.sockChan == null)
			out += "NotConnected";
		else if (this.sockChan.socket() == null)
			out += "NotConnected";
		else if (this.sockChan.socket().getRemoteSocketAddress() == null)
			out += "NotConnected";
		else
			out += this.sockChan.socket().getRemoteSocketAddress().toString();

		return out;
	}

	public String getLocalAddressAndPortAsString() {
		String out = "";

		if (this.sockChan == null)
			out += "NotConnected";
		else if (this.sockChan.socket() == null)
			out += "NotConnected";
		else if (this.sockChan.socket().getRemoteSocketAddress() == null)
			out += "NotConnected";
		else {
			out += this.sockChan.socket().getLocalSocketAddress().toString();
			out += ":";
			out += this.sockChan.socket().getLocalPort();
		}

		return out;
	}

	/**
	 * Gives the Connection a chance to act on a msg prior to sending it to the
	 * provided NetMsgHandler
	 *
	 */
	@Override
	public abstract boolean handleClientMsg(ClientNetMsg msg);

	protected long getLastMsgTime() {
		return this.lastMsgTime;
	}

	protected void setLastMsgTime() {
		// TODO Consider making this a static to latest system time
		this.lastMsgTime = System.currentTimeMillis();
	}

	protected long getLastKeepAliveTime() {
		return this.lastKeepAliveTime;
	}

	protected void setLastKeepAliveTime() {
		this.lastKeepAliveTime = System.currentTimeMillis();
	}

	public long getLastOpcode() {
		return lastOpcode;
	}

	public void setLastOpcode(long lastOpcode) {
		this.lastOpcode = lastOpcode;
	}

}
