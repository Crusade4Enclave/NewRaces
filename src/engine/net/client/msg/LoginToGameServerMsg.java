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

public class LoginToGameServerMsg extends ClientNetMsg {

	private String secKey;
	private int Unknown01;
	private int Unknown02;

	/**
	 * This is the general purpose constructor.
	 */
	public LoginToGameServerMsg() {
		super(Protocol.LOGINTOGAMESERVER);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public LoginToGameServerMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.LOGINTOGAMESERVER, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putHexString(this.secKey);
		writer.putInt(this.Unknown01);
		writer.putInt(this.Unknown02);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.secKey = reader.getHexString();

		this.Unknown01 = reader.monitorInt(1065353216, "ValidateGameServerMsg 01");
		this.Unknown02 = reader.monitorInt(1065353216, "ValidateGameServerMsg 02");
	}

	/**
	 * @return the secKey
	 */
	public String getSecKey() {
		return secKey;
	}

	/**
	 * @param secKey
	 *            the secKey to set
	 */
	public void setSecKey(String secKey) {
		this.secKey = secKey;
	}

	/**
	 * @return the unknown01
	 */
	public int getUnknown01() {
		return Unknown01;
	}

	/**
	 * @param unknown01
	 *            the unknown01 to set
	 */
	public void setUnknown01(int unknown01) {
		Unknown01 = unknown01;
	}

	/**
	 * @return the unknown02
	 */
	public int getUnknown02() {
		return Unknown02;
	}

	/**
	 * @param unknown02
	 *            the unknown02 to set
	 */
	public void setUnknown02(int unknown02) {
		Unknown02 = unknown02;
	}
}
