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


public class LockUnlockDoorMsg extends ClientNetMsg {

	private int doorID;
	private long targetID;
	private int unk1;
	private int unk2;

	/**
	 * This is the general purpose constructor.
	 */
	public LockUnlockDoorMsg() {
		super(Protocol.LOCKUNLOCKDOOR);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public LockUnlockDoorMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.LOCKUNLOCKDOOR, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(0);
		writer.putInt(doorID);
		writer.putLong(targetID);
		writer.putInt(unk1);
		writer.putInt(unk2);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.getInt();
		this.doorID = reader.getInt();
		this.targetID = reader.getLong();
		this.unk1 = reader.getInt();
		this.unk2 = reader.getInt();
	}

	/**
	 * @return the unknown1
	 */
	public int getDoorID() {
		return doorID;
	}

	/**
	 * @param unknown1
	 *            the unknown1 to set
	 */
	public void setDoorID(int doorID) {
		this.doorID = doorID;
	}

	/**
	 * @return the targetID
	 */
	public long getTargetID() {
		return targetID;
	}

	/**
	 * @param targetID
	 *            the targetID to set
	 */
	public void setTargetID(long targetID) {
		this.targetID = targetID;
	}

	public int getUnk1() {
		return this.unk1;
	}

	public void setUnk1(int value) {
		this.unk1 = value;
	}

	public int getUnk2() {
		return this.unk2;
	}

	public void setUnk2(int value) {
		this.unk2 = value;
	}

}
