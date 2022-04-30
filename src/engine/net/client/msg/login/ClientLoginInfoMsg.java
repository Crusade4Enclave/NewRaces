// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.login;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;

public class ClientLoginInfoMsg extends ClientNetMsg {

	private String uname;
	private String pword;

	private int unknown01;
	private int unknown02;

	private String os;

	private int unknown03;
	private int unknown04;
	private int unknown05;
	private int unknown06;
	private int unknown07;
	private int unknown08;
	private short unknown09;

	/**
	 * This is the general purpose constructor.
	 */
	public ClientLoginInfoMsg(String uName, String pWord, String os) {
		super(Protocol.LOGIN);
		this.uname = uName;
		this.pword = pWord;
		this.os = os;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ClientLoginInfoMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.LOGIN, origin, reader);
	}

	/**
	 * This is the Copy constructor.
	 */
	public ClientLoginInfoMsg(ClientLoginInfoMsg msg) {
		super(Protocol.LOGIN, msg);
        this.uname = msg.uname;
        this.pword = msg.pword;
        this.os = msg.os;
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putString(this.uname);
		writer.putString(this.pword);
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		writer.putString(this.os);
		writer.putInt(this.unknown03);
		writer.putInt(this.unknown04);
		writer.putInt(this.unknown05);
		writer.putInt(this.unknown06);
		writer.putInt(this.unknown07);
		writer.putInt(this.unknown08);
		writer.putShort(this.unknown09);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.uname = reader.getString();
		this.pword = reader.getString();

		this.unknown01 = reader.monitorInt(0, "ClientLoginInfoMsg 01");
		this.unknown02 = reader.monitorInt(0, "ClientLoginInfoMsg 02");

		this.os = reader.getString();

		this.unknown03 = reader.monitorInt(0, "ClientLoginInfoMsg 03");
		this.unknown04 = reader.monitorInt(0, "ClientLoginInfoMsg 04");
		this.unknown05 = reader.monitorInt(0, "ClientLoginInfoMsg 05");
		this.unknown06 = reader.monitorInt(0, "ClientLoginInfoMsg 06");
		this.unknown07 = reader.monitorInt(0, "ClientLoginInfoMsg 07");
		this.unknown08 = reader.monitorInt(0, "ClientLoginInfoMsg 08");
		this.unknown09 = reader
				.monitorShort((short) 0, "ClientLoginInfoMsg 09");

	}

	/**
	 * @return the uname
	 */
	public String getUname() {
		return uname;
	}

	/**
	 * @return the pword
	 */
	public String getPword() {
		return pword;
	}

	/**
	 * @return the os
	 */
	public String getOs() {
		return os;
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
	 * @return the unknown05
	 */
	public int getUnknown05() {
		return unknown05;
	}

	/**
	 * @param unknown05
	 *            the unknown05 to set
	 */
	public void setUnknown05(int unknown05) {
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
	public int getUnknown08() {
		return unknown08;
	}

	/**
	 * @param unknown08
	 *            the unknown08 to set
	 */
	public void setUnknown08(int unknown08) {
		this.unknown08 = unknown08;
	}

	/**
	 * @return the unknown09
	 */
	public short getUnknown09() {
		return unknown09;
	}

	/**
	 * @param unknown09
	 *            the unknown09 to set
	 */
	public void setUnknown09(short unknown09) {
		this.unknown09 = unknown09;
	}

}
