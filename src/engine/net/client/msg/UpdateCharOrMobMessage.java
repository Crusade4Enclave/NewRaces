// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.AbstractCharacter;

public class UpdateCharOrMobMessage extends ClientNetMsg {

	private int type;
	private int npcType;
	private int npcID;

	private int playerType;
	private int playerID;
	private int size;
	private int subRace;

	private int pad = 0;
	private int objectType;
	private int objectUUID;

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UpdateCharOrMobMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.UPDATECHARORMOB, origin, reader);
	}
	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {

	}

	public UpdateCharOrMobMessage(AbstractCharacter tar, int type, int subRaceID) {
		super(Protocol.UPDATECHARORMOB);
		this.playerType = tar.getObjectType().ordinal();
		this.playerID = tar.getObjectUUID();
		this.type = type;
		this.subRace = subRaceID;
	}
	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {
		writer.putInt(this.type);
		if (this.type == 2){
			writer.putInt(this.playerType);
			writer.putInt(this.playerID);
			writer.putInt(this.subRace);
			writer.putInt(-600065291);
			return;
		}
		writer.putInt(this.playerType);
		writer.putInt(this.playerID);
		writer.put((byte)1);
		writer.putInt(0);
		writer.putInt(this.subRace);
		writer.putInt(this.playerType);
		writer.putInt(this.playerID);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putFloat(1);
		writer.putFloat(1);
		writer.putFloat(1);
		writer.putFloat(1);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putLong(-1);
		writer.putInt(0);
		writer.put((byte)1);
		writer.putInt(0);
		writer.put((byte)1);
		writer.putInt(0);
		writer.putInt(0);
		writer.put((byte)1);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(4);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(1);
		writer.putShort((short)0);
		writer.put((byte)0);


	}

	public int getObjectType() {
		return objectType;
	}

	public void setObjectType(int value) {
		this.objectType = value;
	}

	public void setPad(int value) {
		this.pad = value;
	}

	public int getUUID() {
		return objectUUID;

	}

	public int getPad() {
		return pad;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}

	public int getSubRace() {
		return subRace;
	}
	public void setSubRace(int subRace) {
		this.subRace = subRace;
	}
	public int getNpcType() {
		return npcType;
	}
	public void setNpcType(int npcType) {
		this.npcType = npcType;
	}
	public int getNpcID() {
		return npcID;
	}
	public void setNpcID(int npcID) {
		this.npcID = npcID;
	}
	public int getPlayerType() {
		return playerType;
	}
	public void setPlayerType(int playerType) {
		this.playerType = playerType;
	}
	public int getPlayerID() {
		return playerID;
	}
	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}
}
