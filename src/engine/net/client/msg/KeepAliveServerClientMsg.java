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


public class KeepAliveServerClientMsg extends ClientNetMsg {

	private int firstData; // First 4 bytes
	private int secondData; // Second 4 bytes
	
	private double timeLoggedIn;
	private byte endData; // Last byte

	/**
	 * This is the general purpose constructor.
	 */
	public KeepAliveServerClientMsg(int firstData, int secondData, byte endData) {
		super(Protocol.KEEPALIVESERVERCLIENT);
		this.firstData = firstData;
		this.secondData = secondData;
		this.endData = endData;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public KeepAliveServerClientMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.KEEPALIVESERVERCLIENT, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putDouble(this.timeLoggedIn);
		writer.put((byte)0);
		//writer.putDouble(1000);
		
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
	this.timeLoggedIn = reader.getDouble();
		this.endData = reader.get();
	}

	public int getFirstData() {
		return firstData;
	}

	public int getSecondData() {
		return secondData;
	}

	public byte getEndData() {
		return endData;
	}

	public double getTimeLoggedIn() {
		return timeLoggedIn;
	}

	public void setTimeLoggedIn(double timeLoggedIn) {
		this.timeLoggedIn = timeLoggedIn;
	}
}
