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

public class RandomMsg extends ClientNetMsg {

	private int max;
	private int roll;
	private int sourceType;
	private int sourceID;

	/**
	 * This is the general purpose constructor.
	 */

	public RandomMsg() {
		super(Protocol.RANDOM);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public RandomMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.RANDOM, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.max);
		writer.putInt(this.roll);
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.max = reader.getInt();
		this.roll = reader.getInt();
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
	}

	public int getMax() {
		return this.max;
	}

	public int getRoll() {
		return this.roll;
	}

	public int getSourceType() {
		return this.sourceType;
	}

	public int getSourceID() {
		return this.sourceID;
	}

	public void setMax(int value) {
		this.max = value;
	}

	public void setRoll(int value) {
		this.roll = value;
	}

	public void setSourceType(int value) {
		this.sourceType = value;
	}

	public void setSourceID(int value) {
		this.sourceID = value;
	}
}
