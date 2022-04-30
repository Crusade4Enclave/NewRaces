// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.PlayerCharacter;



public class AllianceChangeMsg extends ClientNetMsg {
	public final static int MAKE_ENEMY = 4;
	public final static int MAKE_ALLY = 6;
	public final static int REMOVE = 7;
	public final static byte INFO_SUCCESS = 0;
	public final static byte ERROR_NOT_RECOMMENDED = 1;
	public final static byte ERROR_NOT_SAME_GUILD = 2;
	public final static byte ERROR_NOT_AUTHORIZED = 4;
	public final static byte ERROR_NOT_SAME_FACTION = 7;
	public final static byte ERROR_TOO_MANY = 13;
	public final static byte ERROR_TO0_EARLY = 14;
	private byte msgType;
	private int sourceGuildID;
	private int targetGuildID;
	private int secondsToWait;
	private boolean ally;


	public AllianceChangeMsg(PlayerCharacter player, int sourceGuildID,int targetGuildID, byte msgType, int secondsToWait) {
		super(Protocol.ALLIANCECHANGE);
		this.sourceGuildID = sourceGuildID;
		this.targetGuildID = targetGuildID;
		this.msgType = msgType;
		this.secondsToWait = secondsToWait;

	}

	public AllianceChangeMsg() {
		super(Protocol.ALLIANCECHANGE);
	}


	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public AllianceChangeMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ALLIANCECHANGE, origin, reader);
	}
	//CALL THIS AFTER SANITY CHECKS AND BEFORE UPDATING HEALTH/GOLD.


	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.msgType = reader.get();

		switch (this.msgType){
		case 1:
		case 2:
		case 3:
		case MAKE_ENEMY:
		case MAKE_ALLY:
		case REMOVE:
		case 5:
			reader.getInt(); //source guild type;
			this.sourceGuildID = reader.getInt();
			reader.getInt();
			this.targetGuildID = reader.getInt();
			break;


		}




	}


	// Precache and configure this message before we serialize it


	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {
		writer.put(this.msgType);
		if (this.msgType == ERROR_TO0_EARLY)
			writer.putInt(this.secondsToWait);
		writer.putInt(GameObjectType.Guild.ordinal());
		writer.putInt(this.sourceGuildID);
		writer.putInt(GameObjectType.Guild.ordinal());
		writer.putInt(this.targetGuildID);



	}


	public int getSecondsToWait() {
		return secondsToWait;
	}

	public void setSecondsToWait(int secondsToWait) {
		this.secondsToWait = secondsToWait;
	}

	public int getMsgType() {
		return msgType;
	}

	public int getSourceGuildID() {
		return sourceGuildID;
	}

	public int getTargetGuildID() {
		return targetGuildID;
	}

	public boolean isAlly() {
		return ally;
	}

	public void setMsgType(byte msgType) {
		this.msgType = msgType;
	}
}
