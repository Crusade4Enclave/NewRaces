// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;

public class RespawnMsg extends ClientNetMsg {

	protected int objectType;
	protected int objectID;
	protected float playerHealth;
	protected int playerExp;

	/**
	 * This is the general purpose constructor.
	 */
	public RespawnMsg() {
		super(Protocol.RESETAFTERDEATH);
		this.objectType = 0;
		this.objectID = 0;
		this.playerHealth = 0;
		this.playerExp = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public RespawnMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.RESETAFTERDEATH, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.objectType);
		writer.putInt(this.objectID);
		writer.putFloat(this.playerHealth);
		writer.putInt(this.playerExp);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {

		this.objectType = reader.getInt();
		this.objectID = reader.getInt();
		this.playerHealth = reader.getFloat();
		this.playerExp = reader.getInt();
	}

	public int getObjectType() {
		return this.objectType;
	}

	public int getObjectID() {
		return this.objectID;
	}

	public float getPlayerHealth() {
		return this.playerHealth;
	}

	public int getPlayerExp() {
		return this.playerExp;
	}

	public void setObjectType(int value) {
		this.objectType = value;
	}

	public void setObjectID(int value) {
		this.objectID = value;
	}

	public void setPlayerHealth(float value) {
		this.playerHealth = value;
	}

	public void setPlayerExp(int value) {
		this.playerExp = value;
	}
}
