// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.net.client;

import engine.Enum;
import engine.gameManager.ConfigManager;
import engine.gameManager.SessionManager;
import engine.job.JobScheduler;
import engine.jobs.DisconnectJob;
import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.Network;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.login.LoginErrorMsg;
import engine.objects.Account;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import engine.session.SessionID;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

public class ClientConnection extends AbstractConnection {

	// Enumeration of a message's origin for logging purposes
	private enum MessageSource {

		SOURCE_CLIENT,
		SOURCE_SERVER
	}

	private byte cryptoInitTries = 0;
	protected SessionID sessionID = null;
	private final ClientAuthenticator crypto;
	private final String clientIpAddress;
	public String machineID;
	public long guildtreespam = 0;
	public long ordernpcspam = 0;
	public ReentrantLock trainLock = new ReentrantLock();
	public ReentrantLock sellLock = new ReentrantLock();
	public ReentrantLock buyLock = new ReentrantLock();

	public boolean desyncDebug = false;
	
	public byte[] lastByteBuffer;

	public ClientConnection(ClientConnectionManager connMan,
			SocketChannel sockChan) {
		super(connMan, sockChan, true);
		this.crypto = new ClientAuthenticator(this);

		this.clientIpAddress = sockChan.socket().getRemoteSocketAddress()
				.toString().replace("/", "").split(":")[0];
	}

	@Override
	protected boolean _sendMsg(AbstractNetMsg msg) {
		try {
			msg.setOrigin(this);
			ByteBuffer bb = msg.serialize();

			// Application protocol logging toggled via
			// DevCmd: netdebug on|off

			if (MBServerStatics.DEBUG_PROTOCOL)
				applicationProtocolLogger(msg, MessageSource.SOURCE_SERVER);

			boolean retval = this.sendBB(bb);
			Network.byteBufferPool.putBuffer(bb);//return here.

			return retval;

		} catch (Exception e) { // Catch-all
			Logger.error(e);
			return false;
		}
	}

	/**
	 * Sending a NetMsg to the client involves NOT including a dataLen parameter
	 * and also involves encrypting the data.
	 *
	 */
	@Override
	protected boolean _sendBB(ByteBuffer bb) {
		boolean useCrypto = this.crypto.initialized();
		boolean retVal;

		// Logger.debug("useCrypto: " + useCrypto + ". bb.cap(): " +
		// bb.capacity());
		if (useCrypto == false)
			retVal = super._sendBB(bb);
		else {
			if (bb == null)
				Logger.error("Incoming bb is null");
			ByteBuffer encrypted = Network.byteBufferPool.getBufferToFit(bb.capacity());
			if (encrypted == null)
				Logger.error("Encrypted bb is null");
			this.crypto.encrypt(bb, encrypted);
			retVal = super._sendBB(encrypted);
		}

		return retVal;
	}

	/**
	 * Receiving data from a client involves the initial Crypto Key Exchange,
	 * waiting for a complete NetMsg to arrive using an accumulation factory and
	 * decrypting the data.
	 */
	//FIXME the int return value on this means nothing!  Clean it up!
	@Override
	protected int read() {

		if (readLock.tryLock())
			try {

				// First and foremost, check to see if we the Crypto is initted yet
				if (!this.crypto.initialized())
					this.crypto.initialize(this);

				if (!this.crypto.initialized()) {
					++this.cryptoInitTries;
					if (this.cryptoInitTries >= MBServerStatics.MAX_CRYPTO_INIT_TRIES) {
						Logger.info("Failed to initialize after "
								+ MBServerStatics.MAX_CRYPTO_INIT_TRIES
								+ " tries. Disconnecting.");
						this.disconnect();
					}
					return 0;
				}

				// check to see if SessionID == null;
				if (this.sessionID == null)
					this.sessionID = new SessionID(this.crypto.getSecretKeyBytes());

				// Get ByteBuffers out of pool.
				ByteBuffer bb = Network.byteBufferPool.getBuffer(16);
				ByteBuffer decrypted = Network.byteBufferPool.getBuffer(16);
				// ByteBuffer bb = ByteBuffer.allocate(1024 * 4);

				int totalBytesRead = 0;
				int lastRead = 0;
				do {
					try {
						bb.clear();
						decrypted.clear();
							lastRead = this.sockChan.read(bb);
						// On EOF on the SocketChannel, disconnect.
						if (lastRead <= -1) {
							this.disconnect();
							break;
						}

						if (lastRead == 0)
							continue;

						// ByteBuffer decrypted = ByteBuffer.allocate(lastRead);
						this.crypto.decrypt(bb, decrypted);
						this.factory.addData(decrypted);
						
						
						this.checkInternalFactory();

						totalBytesRead += lastRead;
					

					} catch (NotYetConnectedException e) {
						Logger.error(e.getLocalizedMessage());
						totalBytesRead = -1; // Set error retVal
						break;

					} catch (ClosedChannelException e) {
						// TODO Should a closed channel be logged or just cleaned up?
						// this.logEXCEPTION(e);
						this.disconnect();
						totalBytesRead = -1; // Set error retVal
						break;

					} catch (IOException e) {
						if ( e.getLocalizedMessage() != null && (!e.getLocalizedMessage().equals(MBServerStatics.EXISTING_CONNECTION_CLOSED) && !e.getLocalizedMessage().equals(MBServerStatics.RESET_BY_PEER))){
							Logger.info("Error Reading message opcode " + this.lastOpcode);
							Logger.error(e);
						}
						this.disconnect();
						totalBytesRead = -1; // Set error retVal
						break;

					} catch (Exception e){
						Logger.info("Error Reading message opcode " + this.lastOpcode);
						Logger.error(e);
						totalBytesRead = -1; // Set error retVal
						this.disconnect();
						break;
					}
				}

				while (lastRead > 0);

				Network.byteBufferPool.putBuffer(bb);
				Network.byteBufferPool.putBuffer(decrypted);

				return totalBytesRead;

			} finally {
				readLock.unlock();
			}
		else {
			Logger.debug("Another thread already has a read lock! Skipping.");
			return 0;
		}
	}

	@Override
	public void disconnect() {
		super.disconnect();
		try {

			if (ConfigManager.serverType.equals(Enum.ServerType.WORLDSERVER))
				ConfigManager.worldServer.removeClient(this);
			else
				ConfigManager.loginServer.removeClient(this);

			// TODO There has to be a more direct way to do this...
			SessionManager.remSession(
					SessionManager.getSession(sessionID));
		} catch (NullPointerException e) {
			Logger
			.error(
					"Tried to remove improperly initialized session. Skipping." +
					e);
		}
	}
	
	public void forceDisconnect() {
		super.disconnect();
	}

	/*
	 * Getters n setters
	 */

	public SessionID getSessionID() {
		return sessionID;
	}

	public byte[] getSecretKeyBytes() {
		return this.crypto.getSecretKeyBytes();
	}

	/*
	 * Convenience getters for SessionManager
	 */
	public Account getAccount() {
		return SessionManager.getAccount(this);
	}

	public PlayerCharacter getPlayerCharacter() {
		return SessionManager.getPlayerCharacter(this);
	}

	@Override
	public boolean handleClientMsg(ClientNetMsg msg) {

		Protocol protocolMsg = msg.getProtocolMsg();

		switch (protocolMsg) {
			case KEEPALIVESERVERCLIENT:
			this.setLastKeepAliveTime();
			break;
			//           case ClientOpcodes.OpenVault:
				//           case ClientOpcodes.Random:
			//           case ClientOpcodes.DoorTryOpen:
			//           case ClientOpcodes.SetSelectedObect:
			//           case ClientOpcodes.MoveObjectToContainer:
			//            case ClientOpcodes.ToggleSitStand:
			//            case ClientOpcodes.SocialChannel:
			//            case ClientOpcodes.OpenFriendsCondemnList:
			case SELLOBJECT:
			this.setLastMsgTime();
			break;
			case MOVETOPOINT:
			case ARCCOMBATMODEATTACKING:
			this.setLastMsgTime();
			break;
		default:
			this.setLastMsgTime();
			break;
		}

		// Application protocol logging toggled via
		// DevCmd: netdebug on|off

		if (MBServerStatics.DEBUG_PROTOCOL)
			applicationProtocolLogger(msg, MessageSource.SOURCE_CLIENT);

		return ConfigManager.handler.handleClientMsg(msg); // *** Refactor : Null check then call
	}
	// Method logs detailed information about application
	// protocol traffic.  Toggled at runtime via the
	// DevCmd netdebug on|off

	private void applicationProtocolLogger(AbstractNetMsg msg, MessageSource origin) {

		String outString = "";
		PlayerCharacter tempPlayer = null;

		// Log the protocolMsg
		if (origin == MessageSource.SOURCE_CLIENT)
			outString = " Incoming protocolMsg: ";
		else
			outString = " Outgoing protocolMsg: ";

		Logger.info(outString
				+ Integer.toHexString(msg.getProtocolMsg().opcode)
				+ '/' + msg.getProtocolMsg());

		// Dump message contents using reflection
		tempPlayer = this.getPlayerCharacter();
		outString = "";
		outString += (tempPlayer == null) ? "PlayerUnknown" : tempPlayer.getFirstName() + ' '
				+ msg.toString();
		Logger.info(outString);
	}

	public void kickToLogin(int errCode, String message) {

		LoginErrorMsg lom = new LoginErrorMsg(errCode, message);

		if (!sendMsg(lom))
			Logger.error("Failed to send  message"); // TODO Do we just accept this failure to send Msg?

		DisconnectJob dj = new DisconnectJob(this);
		JobScheduler.getInstance().scheduleJob(dj, 250);

	}

	public final String getClientIpAddress() {
		return this.clientIpAddress;
	}
}
