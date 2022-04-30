// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.guild;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;

public class GuildControlMsg extends ClientNetMsg {

	private int unknown01;
	private int unknown02;
	private int unknown03;
	private int unknown04;
	private String message;
	private byte unknown05;
	private int unknown06;
	private int unknown07;
	private byte unknown08;

	private byte isGM;

	/**
	 * This is the general purpose constructor.
	 */
	public GuildControlMsg() {
		super(Protocol.REQUESTMEMBERLIST);
		this.unknown01 = 2;
		this.unknown02 = 0;
		this.unknown03 = 0;
		this.unknown04 = 0;
		this.message = "No error message";
		this.unknown05 = 0;
		this.unknown06 = 0;
		this.unknown07 = 257;
		this.unknown08 = (byte) 1;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public GuildControlMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.REQUESTMEMBERLIST, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		writer.putInt(this.unknown03);
		writer.putInt(this.unknown04);
		writer.putString(this.message);

		writer.put((byte) 1);	//Always 1

		writer.put(isGM);	//Can be Tax Collector
		writer.put(isGM);	//Can be Recruiter
		writer.put(isGM);	//Can be IC
		writer.put(isGM);

		writer.put(isGM);	//Can be GM
		writer.put((byte) 1);
		writer.put((byte) 1);
		writer.put((byte) 1);
		writer.put((byte) 1);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
		this.unknown04 = reader.getInt();
		this.message = reader.getString();
		this.unknown05 = reader.get();
		if (this.unknown05 == (byte) 1) {
			this.unknown06 = reader.getInt();
			this.unknown07 = reader.getInt();
			this.unknown08 = reader.get();
		}
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
	public int getUnknown04() {
		return unknown04;
	}

	/**
	 * @param unknown04
	 *            the unknown04 to set
	 */
	public void setUnknown04(int unknown04) {
		this.unknown04 = unknown04;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
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

	/**
	 * @return the unknown06
	 */
	public int getUnknown06() {
		return unknown06;
	}

	/**
	 * @param unknown06
	 *            the unknown06 to set
	 */
	public void setUnknown06(int unknown06) {
		this.unknown06 = unknown06;
	}

	/**
	 * @return the unknown07
	 */
	public int getUnknown07() {
		return unknown07;
	}

	/**
	 * @param unknown07
	 *            the unknown07 to set
	 */
	public void setUnknown07(int unknown07) {
		this.unknown07 = unknown07;
	}

	/**
	 * @return the unknown08
	 */
	public byte getUnknown08() {
		return unknown08;
	}

	public void setGM(byte b) {
		this.isGM = b;
	}

	/**
	 * @param unknown08
	 *            the unknown08 to set
	 */
	public void setUnknown08(byte unknown08) {
		this.unknown08 = unknown08;
	}

}
