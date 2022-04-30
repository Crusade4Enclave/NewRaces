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

public class RecvSummonsRequestMsg extends ClientNetMsg {

	private int sourceType;
	private int sourceID;
	private String sourceName;
	private String locationName; //where being summoned to
	private boolean accepted;

	/**
	 * This is the general purpose constructor.
	 */
	public RecvSummonsRequestMsg(int sourceType, int sourceID, String sourceName, String locationName, boolean accepted) {
		super(Protocol.ARCSUMMON);
		this.sourceType = sourceType;
		this.sourceID = sourceID;
		this.sourceName = sourceName;
		this.locationName = locationName;
		this.accepted = accepted;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public RecvSummonsRequestMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ARCSUMMON, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);
		writer.putString(this.sourceName);
		writer.putString(this.locationName);
		writer.put(this.accepted ? (byte)1 : (byte)0);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
		this.sourceName = reader.getString();
		this.locationName = reader.getString();
		this.accepted = (reader.get() == 1) ? true : false;
	}

	public int getSourceType() {
		return this.sourceType;
	}

	public int getSourceID() {
		return this.sourceID;
	}

	public String getSourceName() {
		return this.sourceName;
	}

	public String getLocationName() {
		return this.locationName;
	}

	public boolean accepted() {
		return this.accepted;
	}

	public void setSourceType(int value) {
		this.sourceType = value;
	}

	public void setSourceID(int value) {
		this.sourceID = value;
	}

	public void setSourceName(String value) {
		this.sourceName = value;
	}

	public void setLocationName(String value) {
		this.locationName = value;
	}

	public void setAccepted(boolean value) {
		this.accepted = value;
	}
}
