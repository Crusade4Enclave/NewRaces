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

public class UnknownMsg extends ClientNetMsg {

	private int unknown01;
	private int unknown02;
	private int unknown03;
	private short unknown04;
	private byte unknown05;

	/**
	 * This is the general purpose constructor.
	 */
	public UnknownMsg() {
		super(Protocol.UNKNOWN);
		this.unknown01 = 0x40A5BDB0;
		this.unknown02 = 0x342AA9F0;
		this.unknown03 = 0;
		this.unknown04 = (short) 0;
		this.unknown05 = (byte) 0;

	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UnknownMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.UNKNOWN, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		writer.putInt(this.unknown03);
		writer.putShort(this.unknown04);
		writer.put(this.unknown05);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.getFloat();
		reader.getString();
		reader.getString();
		reader.getInt();
		reader.get();
		reader.getInt();
		reader.getInt();
		int unknownSize =reader.getInt();
		for (int i = 0; i < unknownSize;i++)
			reader.getInt();
		reader.getInt();
		reader.getString();
	}

	/**
	 * @return the unknown01
	 */
	public int getUnknown01() {
		return unknown01;
	}

	/**
	 * @param unknown01
	 *            the unknown01 to set
	 */
	public void setUnknown01(int unknown01) {
		this.unknown01 = unknown01;
	}

	/**
	 * @return the unknown02
	 */
	public int getUnknown02() {
		return unknown02;
	}

	/**
	 * @param unknown02
	 *            the unknown02 to set
	 */
	public void setUnknown02(int unknown02) {
		this.unknown02 = unknown02;
	}

	/**
	 * @return the unknown03
	 */
	public int getUnknown03() {
		return unknown03;
	}

	/**
	 * @param unknown03
	 *            the unknown03 to set
	 */
	public void setUnknown03(int unknown03) {
		this.unknown03 = unknown03;
	}

	/**
	 * @return the unknown04
	 */
	public short getUnknown04() {
		return unknown04;
	}

	/**
	 * @param unknown04
	 *            the unknown04 to set
	 */
	public void setUnknown04(short unknown04) {
		this.unknown04 = unknown04;
	}

	/**
	 * @return the unknown05
	 */
	public byte getUnknown05() {
		return unknown05;
	}

	/**
	 * @param unknown05
	 *            the unknown05 to set
	 */
	public void setUnknown05(byte unknown05) {
		this.unknown05 = unknown05;
	}

}
