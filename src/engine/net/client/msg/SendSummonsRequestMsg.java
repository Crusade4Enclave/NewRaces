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

public class SendSummonsRequestMsg extends ClientNetMsg {

	private int powerToken;
	private int sourceType;
	private int sourceID;
	private String targetName;
	private int trains;

	/**
	 * This is the general purpose constructor.
	 */
	public SendSummonsRequestMsg() {
		super(Protocol.POWERTARGNAME);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public SendSummonsRequestMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.POWERTARGNAME, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.powerToken);
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);
		writer.putString(this.targetName);
		writer.putInt(this.trains);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.powerToken = reader.getInt();
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
		this.targetName = reader.getString();
		this.trains = reader.getInt();
	}

	public int getPowerToken() {
		return this.powerToken;
	}

	public int getSourceType() {
		return this.sourceType;
	}

	public int getSourceID() {
		return this.sourceID;
	}

	public String getTargetName() {
		return this.targetName;
	}

	public int getTrains() {
		return this.trains;
	}

	public void setPowerToken(int value) {
		this.powerToken = value;
	}

	public void setSourceType(int value) {
		this.sourceType = value;
	}

	public void setSourceID(int value) {
		this.sourceID = value;
	}

	public void setTargetName(String value) {
		this.targetName = value;
	}

	public void setTrains(int value) {
		this.trains = value;
	}
}
