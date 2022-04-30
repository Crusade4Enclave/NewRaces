// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net;

import engine.exception.FactoryBuildException;
import engine.gameManager.ChatManager;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class NetMsgFactory {

	// NetMsg Opcode to Constructor List
	private static final HashMap<Integer, Constructor> netMsgDefinitions = new HashMap<>();

	// Standardize the error strings
	private static String ALL_GOOD_JUST_NOT_ENOUGH_BYTES = "Not enough Bytes";
	private static String DESERIALIZATION_FAILURE = "Deserialization Failure";
	private static String UNIMPLEMENTED_OPCODE = "Unimplemented Opcode";
	private static String UNKNOWN_OPCODE = "Unknown Opcode";

	protected ByteBuffer internalBuffer;
	private final ArrayList<AbstractNetMsg> msgOutbox;

	private boolean enableFloodControl;
	private boolean bypassFloodControl; // temp bypass
	private boolean floodControlTripped;

	private static final int FLOOD_CONTROL_TRIP_SETPOINT = 1000;
	private int badOpcodeCount;
	private final AbstractConnection owner;
	private int lastMsgPosition = 0;

	public NetMsgFactory(AbstractConnection origin, boolean enableFloodControl) {
		this.internalBuffer = Network.byteBufferPool.getBuffer(18); //256k buffer
		
		this.bypassFloodControl = false;
		this.msgOutbox = new ArrayList<>();
		this.enableFloodControl = enableFloodControl;
		this.floodControlTripped = false;
		this.owner = origin;
	}

	public NetMsgFactory(AbstractConnection origin) {
		this(origin, true);
	}

	public final void addData(byte[] ba) {
		// Dont use prefab BB's here, since sizeof(ba) is unknown.
		ByteBuffer bb = ByteBuffer.wrap(ba);
		bb.position(bb.capacity());
		this.addData(bb);
	}

	public final void addData(ByteBuffer newData) {
		synchronized (this.internalBuffer) {

			int newCapacity = this.internalBuffer.position() + newData.position();

			if (newCapacity >= this.internalBuffer.capacity()) {
				//Resize!!!!
				Logger.warn(
						"Bytebuffer is being be Resized.");

				//Get a newer, bigger BB
				ByteBuffer newBB = Network.byteBufferPool.getBufferToFit((int) (newCapacity * 1.5));

				//Copy old data in
				this.internalBuffer.flip();
				newBB.put(this.internalBuffer);

				//Get a handle on old BB
				ByteBuffer oldBB = this.internalBuffer;
				
				//install new BB
				this.internalBuffer = newBB;

				//Return old BB
				Network.byteBufferPool.putBuffer(oldBB);
			}

			synchronized (newData) {
				// Copy over the data.
				newData.flip();

				try {
					this.internalBuffer.put(newData);
				} catch (Exception e) {
					Logger.error( e.toString());
					// TODO figure out how to handle this error.
				}
			}
		}
	}

	public void parseBuffer() {
		// Check flood control first
		if (this.floodControlTripped)
			// this.conn.disconnect();
			return;

		// MBServer.jobMan.submitJob(new ParseBufferJob(this));
		this._parseBuffer();
	}

	/**
	 * This function makes a copy of the current internal byte buffer and loads
	 * the copy into a ByteBufferReader. It is copied so that the Factory can
	 * continue to accumulate data on the internal buffer from the
	 * socketChannels. The ByteBufferReader is then used in an attempt to build
	 * an AbstractNetMsg subclass based on protocolMsg. If a message is successfully
	 * built, the bytes used are removed from the Factory's internal byte
	 * buffer.
	 *
	 * @return
	 * @throws Exception
	 */
	protected void _parseBuffer() {
		synchronized (this.internalBuffer) {
			while (this.internalBuffer.position() > 0) {
				// Check flood control first
				if (this.floodControlTripped)
					break;

				ByteBufferReader reader = null;

				// Check to see if the minimum amount of data is here:
				if (this.internalBuffer.position() < 4)
					// nothing wrong, just not enough info yet.
					break;

				// copy internal buffer into a reader
				reader = new ByteBufferReader(this.internalBuffer, false);

				// Reset the limit to the capacity
				this.internalBuffer.limit(this.internalBuffer.capacity());

				try {
					AbstractNetMsg msg = this.tryBuild(owner, reader);

					// error, null messages are being returned on unhandled
					// opcodes
					// for some reason
					if (msg == null)
						throw new FactoryBuildException(UNIMPLEMENTED_OPCODE);
						
					
					
					if (owner.getClass().equals(ClientConnection.class)){
						ClientConnection client = (ClientConnection)owner;
						client.setLastOpcode(msg.getProtocolMsg().opcode);
					}
					
					
					

					//					Logger.debug("Adding a " + msg.getSimpleClassName()
					//							+ " to the outbox.");
					this.addMsgToOutBox(msg);

					this.dropLeadingBytesFromBuffer(reader.position());
					this.bypassFloodControl = false;

				} catch (FactoryBuildException e) {
					String error = e.getMessage();
					int readerPos = reader.position();

					if (error.equals(ALL_GOOD_JUST_NOT_ENOUGH_BYTES)){
						break;
					}
						// no worries, just break.
						
					else if (error.equals(DESERIALIZATION_FAILURE)) {
						// Lop readerPos bytes off the buffer.
						this.dropLeadingBytesFromBuffer(readerPos);

						// Lets bypass flood control for now.
						this.bypassFloodControl = true;
						continue;

					} else if (error.equals(UNIMPLEMENTED_OPCODE)) {
						
						if (owner.lastProtocol != null && owner.lastProtocol.constructor == null){
							this.dropLeadingBytesFromBuffer(readerPos);
							this.bypassFloodControl = true;
							continue;
						}
							
						// Lop readerPos bytes off the buffer.
						if (reader.position() >= 4)
						reader.position(reader.position() - 4);
						int newPosition = Protocol.FindNextValidOpcode(reader);
						this.dropLeadingBytesFromBuffer(newPosition);
						// Lets bypass flood control for now.
						this.bypassFloodControl = true;

						continue;

					} else if (error.equals(UNKNOWN_OPCODE)) {
						
						if (owner.lastProtocol != null && owner.lastProtocol.constructor == null){
							this.dropLeadingBytesFromBuffer(readerPos);
							this.bypassFloodControl = true;
							continue;
						}
						// We don't know what this is or how long, so dump the
						// first
						// byte and try again
						if (reader.position() >= 4)
						reader.position(reader.position() - 4);
						int newPosition = Protocol.FindNextValidOpcode(reader);
						this.dropLeadingBytesFromBuffer(newPosition);
						// Lets bypass flood control for now.
						this.bypassFloodControl = true;

						continue;
					}
				} catch (Exception e) {
					// TODO FIX THIS!!!!
//					Logger.error( e);

				}// end catch

			} // end while loop
		}
	}// end fn

	public AbstractNetMsg tryBuild(AbstractConnection origin,
			ByteBufferReader reader) throws FactoryBuildException {
		try {

			// Get the protocolMsg
			int opcode = reader.getInt();
			// String ocHex = StringUtils.toHexString(protocolMsg);

			if (MBServerStatics.PRINT_INCOMING_OPCODES)
				try {
					Logger.info( "Incoming protocolMsg: "
							+ Protocol.getByOpcode(opcode).name() + " " + Integer.toHexString(opcode) + ", size: " + reader.getBb().limit() + "; " + getByteArray(reader));
				} catch (Exception e) {
					Logger.error( e);
				}

			return NetMsgFactory.getNewInstanceOf(opcode, origin, reader);

		} catch (BufferUnderflowException e) {
			// This is okay. it indicates that we recognized the protocolMsg, but
			// there isn't enough information in
			// the reader to complete the NetMsg deserialization
			throw new FactoryBuildException(ALL_GOOD_JUST_NOT_ENOUGH_BYTES);

		}
	}

	public static String getByteArray(ByteBufferReader reader) {
		String ret = "";
		if (reader == null)
			return ret;

		ByteBuffer bb = reader.getBb();
		if (bb == null)
			return ret;

		int length = bb.limit(); // - bb.position();
		ByteBuffer temp = bb.duplicate();
		temp.position(bb.limit());
		temp.flip();
		for (int i = 0; i < length; i++) {
			ret += Integer.toString((temp.get() & 0xff) + 0x100, 16).substring(1).toUpperCase();
		}
		return ret;
	}

	private void incrBadOpcodeCount() {
		// keeping this a nested if for Troubleshooting/clarity
		if (this.enableFloodControl == true)
			if (this.bypassFloodControl == false) {
				++this.badOpcodeCount;



				if (this.badOpcodeCount >= FLOOD_CONTROL_TRIP_SETPOINT){
					if (this.owner != null){
						if (this.owner instanceof ClientConnection){
							ClientConnection client = (ClientConnection) this.owner;
							if (client.getPlayerCharacter() != null){
								ChatManager.chatSystemError(client.getPlayerCharacter(),"TRIPPED Flood Control! PLEASE RELOG!");
								Logger.info( client.getPlayerCharacter().getName() + " Tripped Flood Control!" + this.badOpcodeCount);
							}

						}
					}
					this.floodControlTripped = true;
				}else{
					if (this.owner != null){
						if (this.owner instanceof ClientConnection){
							ClientConnection client = (ClientConnection) this.owner;
							if (client.getPlayerCharacter() != null){
								ChatManager.chatSystemError(client.getPlayerCharacter(),"Client sending bad messages. bad message Count " + this.badOpcodeCount);
								Logger.info( client.getPlayerCharacter().getName() + " has been caught sending bad opcodes. Bad Opcode Count " + this.badOpcodeCount);
							}

						}


					}
				}

			}
	}

	protected final void dropLeadingBytesFromBuffer(int numberOfBytes) {
		this.internalBuffer.limit(this.internalBuffer.position());
		this.internalBuffer.position(numberOfBytes);
		this.internalBuffer.compact(); // Compact
	}

	protected boolean addMsgToOutBox(AbstractNetMsg msg) {
		synchronized (this.msgOutbox) {
			return msgOutbox.add(msg);
		}
	}

	public AbstractNetMsg getMsg() {
		synchronized (this.msgOutbox) {
			if (this.msgOutbox.isEmpty())
				return null;
			return msgOutbox.remove(0);
		}
	}

	public boolean hasMsg() {
		synchronized (this.msgOutbox) {
			return !msgOutbox.isEmpty();
		}
	}

	public boolean hasData() {
		synchronized (this.internalBuffer) {
			return (this.internalBuffer.position() != 0);
		}
	}

	@SuppressWarnings("unchecked")
	private static AbstractNetMsg getNewInstanceOf(int opcode,
			AbstractConnection origin, ByteBufferReader reader) {
		try {

			Protocol protocolMsg = Protocol.getByOpcode(opcode);

			if (protocolMsg == Protocol.NONE){
				
				String errorString = DateTime.now().toString() + origin.lastProtocol.name();
				
				int errorCode = errorString.hashCode();
				
				
				if (origin instanceof ClientConnection){
					PlayerCharacter player = ((ClientConnection)origin).getPlayerCharacter();
					if (player != null){
//						if (MBServerStatics.worldServerName.equals("Grief"))
						Logger.error("Invalid protocol msg for player " + player.getFirstName() + " : " + opcode + " lastopcode: " + origin.lastProtocol.name() + " Error Code : " + errorCode);
					}else
						Logger.error("Invalid protocol msg  : " + opcode + " lastopcode: " + origin.lastProtocol.name() + " Error Code : " + errorCode);

				}
					
				return null;
			}
			origin.lastProtocol = protocolMsg;

			if (protocolMsg.constructor == null){
				return null;
			}
			
			

			Constructor<AbstractNetMsg> constructor = protocolMsg.constructor;

			if (constructor == null)
				return null;

			Object[] myArgs = new Object[2];
			myArgs[0] = origin;
			myArgs[1] = reader;

			Object object = constructor.newInstance(myArgs);

			if (object instanceof engine.net.AbstractNetMsg)
				return (AbstractNetMsg) object;

		} catch (IllegalAccessException | IllegalArgumentException | InstantiationException | ExceptionInInitializerError e) {
			Logger.error( e);

		} catch (InvocationTargetException e) {
			if (e.getCause() != null
					&& e.getCause().getClass() == BufferUnderflowException.class)
				throw new BufferUnderflowException();
			Logger.error(e);
		}
		return null;
	}

	public ByteBuffer getInternalBuffer() {
		return internalBuffer;
	}

}
