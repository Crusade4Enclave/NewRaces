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

public class ScaleObjectMsg extends ClientNetMsg {

	private long compID;
	private float scaleX;
	private float scaleY;
	private float scaleZ;

	/**
	 * This is the general purpose constructor.
	 */
	public ScaleObjectMsg(long compID, float scaleX, float scaleY, float scaleZ) {
		super(Protocol.SCALEOBJECT);
		this.compID = compID;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ScaleObjectMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.SCALEOBJECT, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putLong(this.compID);
		writer.putFloat(this.scaleX);
		writer.putFloat(this.scaleY);
		writer.putFloat(this.scaleZ);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.compID = reader.getLong();
		this.scaleX = reader.getFloat();
		this.scaleY = reader.getFloat();
		this.scaleZ = reader.getFloat();
	}

	public long getCompID() {
		return this.compID;
	}

	public float getScaleX() {
		return this.scaleX;
	}

	public float getScaleY() {
		return this.scaleY;
	}

	public float getScaleZ() {
		return this.scaleZ;
	}

	public void setCompID(long value) {
		this.compID = value;
	}

	public void setScaleX(float value) {
		this.scaleX = value;
	}

	public void setScaleY(float value) {
		this.scaleY = value;
	}

	public void setScaleZ(float value) {
		this.scaleZ = value;
	}
}
