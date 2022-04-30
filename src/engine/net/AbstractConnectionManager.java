// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net;

import engine.core.ControlledRunnable;
import engine.job.AbstractJob;
import engine.job.JobManager;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractConnectionManager extends ControlledRunnable {

	private final Selector selector;
	private final ServerSocketChannel listenChannel;
	private final ConcurrentLinkedQueue<ChangeRequest> chngReqs = new ConcurrentLinkedQueue<>();

	/*
	 *
	 */
	public AbstractConnectionManager(String nodeName, InetAddress hostAddress,
			int port) throws IOException {
		super(nodeName);

		this.selector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking Server channel
		this.listenChannel = ServerSocketChannel.open();
		this.listenChannel.configureBlocking(false);

		// Bind
		InetSocketAddress isa = new InetSocketAddress(hostAddress, port);
		this.listenChannel.socket().bind(isa);

		Logger.info(this.getLocalNodeName() + " Configured to listen: "
				+ isa.getAddress().toString() + ':' + port);

		// register an interest in Accepting new connections.
		SelectionKey sk = this.listenChannel.register(this.selector, SelectionKey.OP_ACCEPT);
		sk.attach(this);
	}

	/*
	 * ControlledRunnable implementations
	 */
	@Override
	protected void _startup() {
	}

	@Override
	protected void _shutdown() {
		this.selector.wakeup();
		this.disconnectAll();
		this.selector.wakeup();
	}

	@Override
	protected boolean _preRun() {
		this.runStatus = true;
		return true;
	}

	@Override
	protected boolean _Run() {
		while (this.runCmd) {
			try {
				this.runLoopHook();

				this.processChangeRequests();
				this.auditSocketChannelToConnectionMap();
				this.selector.select(250L);
				this.processNewEvents();

			} catch (Exception e) {
				Logger.error(e.toString());
			}
		}
		return true;
	}

	@Override
	protected boolean _postRun() {

		this.runStatus = false;

		this.disconnectAll();

		try {
			this.selector.close();
		} catch (IOException e) {
			Logger.error( e.toString());
		}

		return true;
	}

	/**
	 * Hook for subclasses to use.
	 *
	 */
	protected void runLoopHook() {
	}

	/*
	 * Accept / New Connection FNs
	 */
	private AbstractConnection acceptNewConnection(final SelectionKey key)
			throws IOException {

		this.preAcceptNewConnectionHook(key);

		// Cancel incoming connections if server isn't set to listen
		if (this.listenChannel == null || this.listenChannel.isOpen() == false) {
			key.cancel();
			return null;
		}

		// For an accept to be pending, the key contains a reference to the
		// ServerSocketChannel
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();

		// Get SocketChannel
		SocketChannel sockChan = ssc.accept();
		sockChan.configureBlocking(false);

		//Configure the Socket
		Socket socket = sockChan.socket();
		socket.setSendBufferSize(Network.INITIAL_SOCKET_BUFFER_SIZE);
		socket.setReceiveBufferSize(Network.INITIAL_SOCKET_BUFFER_SIZE);
		socket.setTcpNoDelay(MBServerStatics.TCP_NO_DELAY_DEFAULT);

		//Register with the selector
		SelectionKey sk = sockChan.register(this.selector, SelectionKey.OP_READ);
		if (sk == null) {
			Logger.error("FIX ME! NULL SELECTION KEY!");
			return null;
		}

		//Initialize Connection
		AbstractConnection ac = this.getNewIncomingConnection(sockChan);
		sk.attach(ac);

		this.postAcceptNewConnectionHook(ac);
		return ac;
	}

	/**
	 * Hook for subclasses to use.
	 *
	 * @param key
	 */
	protected void preAcceptNewConnectionHook(SelectionKey key) {
	}

	/**
	 * Hook for subclasses to use.
	 *
	 * @param ac
	 */
	protected void postAcceptNewConnectionHook(AbstractConnection ac) {
	}

	protected abstract AbstractConnection getNewIncomingConnection(SocketChannel sockChan);

	protected abstract AbstractConnection getNewOutgoingConnection(SocketChannel sockChan);

	/*
	 * Disconnect / Destroy Connection FNs
	 */
	protected boolean disconnect(final SelectionKey key) {

		this.disconnect((AbstractConnection) key.attachment());

		key.attach(null);
		key.cancel();
		return key.isValid();
	}

	protected boolean disconnect(final AbstractConnection c) {
		if (c == null)
			return false;

		c.disconnect();

		return c.getSocketChannel().isConnected();
	}

	protected void disconnectAll() {
		synchronized (this.selector.keys()) {
			for (SelectionKey sk : this.selector.keys()) {
				if (sk.channel() instanceof SocketChannel)
					disconnect(sk);
			}
		}
	}

	/*
	 * Data IO
	 */

	/*
	 * WRITE SEQUENCE
	 */
	/**
	 * Submits a request to set this Connection to WRITE mode.
	 *
	 */
	protected void sendStart(final SocketChannel sockChan) {
		synchronized (this.chngReqs) {
			// Indicate we want the interest ops set changed
			this.chngReqs.add(new ChangeRequest(sockChan, ChangeType.CHANGEOPS, SelectionKey.OP_WRITE));
		}

		this.selector.wakeup();
	}

	/**
	 *
	 * @param key
	 * @return Boolean indication emit success.
	 */
	protected boolean sendFinish(final SelectionKey key) {
		SocketChannel sockChan = (SocketChannel) key.channel();

		// Check to see if the SocketChannel the selector offered up
		// is null.
		if (sockChan == null) {
			Logger.error(": null sockChannel.");
			this.disconnect(key);
			return false;
		}

		AbstractConnection c = (AbstractConnection) key.attachment();

		if (c == null) {
			Logger.error(": null Connection.");
			this.disconnect(key);
			return false;
		}

		//		long startTime = System.currentTimeMillis();
		boolean allSent = c.writeAll();

		//		if ((System.currentTimeMillis() - startTime) > 20)
		//			this.logDirectWARNING(c.getRemoteAddressAndPortAsString() + " took " + (System.currentTimeMillis() - startTime) + "ms to handle!");

		// If all was written, switch back to Read Mode.
		if (allSent || !c.isConnected()) {

			// Indicate we want the interest ops set changed
			ChangeRequest chReq = new ChangeRequest(c.getSocketChannel(), ChangeType.CHANGEOPS, SelectionKey.OP_READ);
			synchronized (this.chngReqs) {
				this.chngReqs.add(chReq);
			}

			this.selector.wakeup();
		}
		return true;
	}

	/*
	 * READ SEQUENCE
	 */
	/**
	 *
	 * @param key
	 * @return Boolean indication of success.
	 */
	private boolean receive(final SelectionKey key) {
		SocketChannel sockChan = (SocketChannel) key.channel();

		// Check to see if the SocketChannel the selector offered up
		// is null.
		if (sockChan == null) {
			Logger.error("null sockChannel.");
			this.disconnect(key);
			return false;
		}

		AbstractConnection c = (AbstractConnection) key.attachment();

		if (c == null) {
			Logger.error("null Connection.");
			this.disconnect(key);
			return false;
		}

		c.read();

		return true;
	}

	/*
	 * Main Loop And Loop Controls
	 */
	private void processChangeRequests() {
		SelectionKey selKey = null;
		ChangeRequest sccr = null;
		ChangeType change = null;
		SocketChannel sockChan = null;

		synchronized (this.chngReqs) {
			Iterator<ChangeRequest> it = this.chngReqs.iterator();
			while (it.hasNext()) {
				sccr = it.next();

				if (sccr == null) {
					it.remove();
					continue;
				}

				change = sccr.getChangeType();
				sockChan = sccr.getSocketChannel();

				switch (change) {
				case CHANGEOPS:
					selKey = sockChan.keyFor(this.selector);

					if (selKey == null || selKey.isValid() == false)
						continue;

					selKey.interestOps(sccr.getOps());
					break;

				case REGISTER:
					try {
						sockChan.register(this.selector, sccr.getOps());

					} catch (ClosedChannelException e) {
						// TODO Should a closed channel be logged or just
						// cleaned up?
						// Logger.error(this.getLocalNodeName(), e);
					}
					break;
				}
			}
			this.chngReqs.clear();
		}
	}

	private void processNewEvents() {
		SelectionKey thisKey = null;
		Iterator<SelectionKey> selectedKeys = null;
		JobManager jm = JobManager.getInstance();

		selectedKeys = this.selector.selectedKeys().iterator();

		if (selectedKeys.hasNext() == false)
			return;

		while (selectedKeys.hasNext()) {
			thisKey = selectedKeys.next();

			//To shake out any issues
			if (thisKey.attachment() == null)
				Logger.error("Found null attachment! PANIC!");

			if (thisKey.attachment() instanceof AbstractConnection)
				if (((AbstractConnection) thisKey.attachment()).execTask.get() == true)
					continue;

			selectedKeys.remove();

			try {
				if (thisKey.isValid() == false)
					break;  // Changed from continue
				else if (thisKey.isAcceptable())
					this.acceptNewConnection(thisKey);
				else if (thisKey.isReadable())
					jm.submitJob(new ReadOperationHander(thisKey));
				else if (thisKey.isWritable())
					jm.submitJob(new WriteOperationHander(thisKey));
				else if (thisKey.isConnectable())
					this.finishConnectingTo(thisKey);
				else
					Logger.error("Unhandled keystate: " + thisKey.toString());
			} catch (CancelledKeyException cke) {
				Logger.error(this.getLocalNodeName(), cke);
				this.disconnect(thisKey);

			} catch (IOException e) {
				Logger.error(this.getLocalNodeName(), e);
			}
		}
	}

	protected void connectTo(String host, int port) {
		try {
			this.connectTo(InetAddress.getByName(host), port);
		} catch (UnknownHostException e) {
			Logger.error(this.getLocalNodeName(), e);
		}
	}

	protected void connectTo(InetAddress host, int port) {
		try {
			this.startConnectingTo(host, port);
			this.selector.wakeup();
		} catch (IOException e) {
			Logger.error(this.getLocalNodeName(), e);
		}
	}

	protected final void startConnectingTo(InetAddress host, int port)
			throws IOException {
		// Create a non-blocking socket channel
		SocketChannel sockChan = SocketChannel.open();
		sockChan.configureBlocking(false);
		sockChan.socket().setSendBufferSize(Network.INITIAL_SOCKET_BUFFER_SIZE);
		sockChan.socket().setReceiveBufferSize(Network.INITIAL_SOCKET_BUFFER_SIZE);

		// Make a new Connection object
		this.getNewOutgoingConnection(sockChan);

		// Kick off connection establishment
		sockChan.connect(new InetSocketAddress(host, port));

		synchronized (this.chngReqs) {
			this.chngReqs.add(new ChangeRequest(sockChan, ChangeType.REGISTER, SelectionKey.OP_CONNECT));
		}

		this.selector.wakeup();
	}

	private void finishConnectingTo(SelectionKey key) throws IOException {
		this.preFinishConnectingToHook(key);

		// Get sockChan
		SocketChannel sockChan = (SocketChannel) key.channel();

		// Get AbstractConnection
		AbstractConnection ac = (AbstractConnection) key.attachment();

		if (sockChan == null) {
			Logger.error(this.getLocalNodeName(), "null socketChannel");
			this.disconnect(key);
			return;
		}

		if (ac == null) {
			Logger.error(this.getLocalNodeName(), "null AbstractConnection");
			this.disconnect(key);
			return;
		}

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			sockChan.finishConnect();
		} catch (IOException e) {
			if (e.getMessage().startsWith("Connection refused:")
					|| e.getMessage().startsWith(
							"An existing connection was forcibly closed")) {
				// eat this type of IOException
			} else
				Logger.error(this.getLocalNodeName(), e);

			// Cancel the channel's registration with our selector
			key.cancel();
			return;
		}

		Socket socket = sockChan.socket();
		Logger.debug("Connected to: "
				+ socket.getInetAddress() + ':'
				+ socket.getPort());

		sockChan.configureBlocking(false);
		sockChan.register(this.selector, SelectionKey.OP_READ);

		this.postFinishConnectingToHook(ac);
	}

	/**
	 * Hook for subclasses to use.
	 *
	 * @param key
	 */
	protected void preFinishConnectingToHook(SelectionKey key) {
	}

	/**
	 * Hook for subclasses to use.
	 *
	 * @param ac
	 */
	protected void postFinishConnectingToHook(AbstractConnection ac) {
	}

	public final String getLocalNodeName() {
		return this.getThreadName();
	}

	/**
	 * Removes the mapping that contains the key 'sockChan'
	 *
	 * @param sockChan
	 */
	private long lastAuditTime = 0;

	protected int auditSocketChannelToConnectionMap() {
		long startTime = System.currentTimeMillis();
		int numberOfItemsToProcess = 0;

		if (lastAuditTime + MBServerStatics.TIMEOUT_CHECKS_TIMER_MS > startTime)
			return -1;

		synchronized (this.selector.keys()) {
			for (SelectionKey sk : this.selector.keys()) {
				if (!(sk.channel() instanceof SocketChannel))
					continue;

				SocketChannel sockChan = (SocketChannel) sk.channel();
				AbstractConnection conn = (AbstractConnection) sk.attachment();

				if (sockChan == null)
					continue;

				if (!sockChan.isOpen()) {
					numberOfItemsToProcess++;
				     Logger.info("sockChan closed. Disconnecting..");
					disconnect(sk);
					continue;
				}

				if (conn == null) {
					numberOfItemsToProcess++;
					Logger.info("Connection is null, Disconnecting.");
					disconnect(sk);
					continue;
				}

				//removed keep alive timeout. Believe failmu used this for disconnecting players on force quit, but a closed socket will already disconnect.
//                if (conn.getLastKeepAliveTime() + MBServerStatics.KEEPALIVE_TIMEOUT_MS < startTime) {
//                    numberOfItemsToProcess++;
//                    Logger.info("Keep alive Disconnecting " + conn.getRemoteAddressAndPortAsString());
//                    conn.disconnect();
//                    continue;
//                }

				if (conn.getLastMsgTime() + MBServerStatics.AFK_TIMEOUT_MS < startTime) {
					numberOfItemsToProcess++;
					   Logger.info("AFK TIMEOUT Disconnecting " + conn.getRemoteAddressAndPortAsString());
					conn.disconnect();
				}
			}
		}

		if (numberOfItemsToProcess != 0)
			Logger.info( "Cleaned "
					+ numberOfItemsToProcess
					+ " dead connections in "
					+ (System.currentTimeMillis() - startTime)
					+ " millis.");

		lastAuditTime = System.currentTimeMillis();
		return numberOfItemsToProcess;
	}

	/*
	 *
	 */
	protected static enum ChangeType {

		REGISTER, CHANGEOPS
	}

	private class ChangeRequest {

		private final SocketChannel sockChan;
		private final ChangeType changeType;
		private final Integer ops;

		public ChangeRequest(SocketChannel sockChan, ChangeType changeType,
				int ops) {
			this.sockChan = sockChan;
			this.changeType = changeType;
			this.ops = ops;
		}

		public SocketChannel getSocketChannel() {
			synchronized (this.sockChan) {
				return this.sockChan;
			}
		}

		public ChangeType getChangeType() {
			synchronized (this.changeType) {
				return this.changeType;
			}
		}

		public int getOps() {
			synchronized (this.ops) {
				return this.ops;
			}
		}
	}

	public int getConnectionSize() {
		if (this.selector == null)
			return -1;
		if (this.selector.keys() == null)
			return -1;
		return this.selector.keys().size();
	}

	/**
	 * Returns the port on which this socket is listening.
	 *
	 * @return the port number to which this socket is listening or -1 if the
	 * socket is not bound yet.
	 *
	 */
	public int getListeningPort() {
		if (this.listenChannel == null)
			return -1;
		if (this.listenChannel.socket() == null)
			return -1;
		return this.listenChannel.socket().getLocalPort();
	}

	/**
	 * Returns the address of the endpoint this socket is bound to, or null if
	 * it is not bound yet.
	 *
	 * @return a SocketAddress representing the local endpoint of this socket,
	 * or null if it is not bound yet.
	 */
	public SocketAddress getListeningAddress() {
		if (this.listenChannel == null)
			return null;
		if (this.listenChannel.socket() == null)
			return null;
		return this.listenChannel.socket().getLocalSocketAddress();
	}

	private class ReadOperationHander extends AbstractJob {

		private final SelectionKey sk;
		private final AbstractConnection ac;
		private final boolean runStatus;

		public ReadOperationHander(final SelectionKey sk) {
			this.sk = sk;

			if (sk.attachment() instanceof AbstractConnection) {
				this.ac = (AbstractConnection) sk.attachment();
				this.runStatus = this.ac.execTask.compareAndSet(false, true);
			} else {
				this.ac = null;
				this.runStatus = false;
				Logger.error("Passed selection key did not have a corresponding Connection!(Read)");
			}
		}

		@Override
		protected void doJob() {
			if (runStatus) {
				this.ac.connMan.receive(sk);
				this.ac.execTask.compareAndSet(true, false);
			}
		}
	}

	private class WriteOperationHander extends AbstractJob {

		private final SelectionKey sk;
		private final AbstractConnection ac;
		private final boolean runStatus;

		public WriteOperationHander(final SelectionKey sk) {
			this.sk = sk;

			if (sk.attachment() instanceof AbstractConnection) {
				this.ac = (AbstractConnection) sk.attachment();
				this.runStatus = this.ac.execTask.compareAndSet(false, true);
			} else {
				this.runStatus = false;
				this.ac = null;
				Logger.error("Passed selection key did not have a corresponding Connection!(Write)");
			}

		}

		@Override
		protected void doJob() {
			if (runStatus) {
				this.ac.connMan.sendFinish(sk);
				this.ac.execTask.compareAndSet(true, false);
			}
		}
	}

}
