// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.math.Vector3fImmutable;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;

public class ShowMsg extends ClientNetMsg {

	private int targetType;
	private int targetID;
	private Vector3fImmutable unknown01;
	private Vector3fImmutable unknown02;
	private float range01;
	private Vector3fImmutable unknown03;
	private Vector3fImmutable unknown04;
	private float range02;

	/**
	 * This is the general purpose constructor.
	 */
	public ShowMsg() {
		super(Protocol.SHOWCOMBATINFO);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ShowMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.SHOWCOMBATINFO, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.targetType);
		writer.putInt(this.targetID);
		writer.putVector3f(this.unknown01);
		writer.putVector3f(this.unknown02);
		writer.putFloat(this.range01);
		writer.putVector3f(this.unknown03);
		writer.putVector3f(this.unknown04);
		writer.putFloat(this.range02);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.targetType = reader.getInt();
		this.targetID = reader.getInt();
		this.unknown01 = reader.getVector3fImmutable();
		this.unknown02 = reader.getVector3fImmutable();
		this.range01 = reader.getFloat();
		this.unknown03 = reader.getVector3fImmutable();
		this.unknown04 = reader.getVector3fImmutable();
		this.range02 = reader.getFloat();
	}

	public int getTargetType() {
		return this.targetType;
	}

	public int getTargetID() {
		return this.targetID;
	}

	public Vector3fImmutable getUnknown01() {
		return this.unknown01;
	}

	public Vector3fImmutable getUnknown02() {
		return this.unknown02;
	}

	public Vector3fImmutable getUnknown03() {
		return this.unknown03;
	}

	public Vector3fImmutable getUnknown04() {
		return this.unknown04;
	}

	public float getRange01() {
		return this.range01;
	}

	public float getRange02() {
		return this.range02;
	}

	public void setTargetType(int value) {
		this.targetType = value;
	}

	public void setTargetID(int value) {
		this.targetID = value;
	}

	public void setUnknown01(Vector3fImmutable value) {
		this.unknown01 = value;
	}

	public void setUnknown02(Vector3fImmutable value) {
		this.unknown02 = value;
	}

	public void setUnknown03(Vector3fImmutable value) {
		this.unknown03 = value;
	}

	public void setUnknown04(Vector3fImmutable value) {
		this.unknown04 = value;
	}

	public void setRange01(float value) {
		this.range01 = value;
	}

	public void setRange02(float value) {
		this.range02 = value;
	}
}
