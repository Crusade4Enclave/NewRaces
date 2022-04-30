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

public class ModifyStatMsg extends ClientNetMsg {

	private int amount;
	private int type;
	private int unknown01;

	/**
	 * This is the general purpose constructor.
	 */
	public ModifyStatMsg() {
		super(Protocol.RAISEATTR);
	}

	public ModifyStatMsg(int amount, int type, int unknown01) {
		super(Protocol.RAISEATTR);
		this.amount = amount;
		this.type = type;
		this.unknown01 = unknown01;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ModifyStatMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.RAISEATTR, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.amount);
		writer.putInt(this.type);
		writer.putInt(this.unknown01);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.amount = reader.getInt();
		this.type = reader.getInt();
		this.unknown01 = reader.getInt();
	}

	public int getAmount() {
		return this.amount;
	}

	public int getType() {
		return this.type;
	}

	public int getUnknown01() {
		return this.unknown01;
	}

	public void setAmount(int value) {
		this.amount = value;
	}

	public void setType(int value) {
		this.type = value;
	}

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}
}
