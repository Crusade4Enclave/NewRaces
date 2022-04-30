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
import engine.objects.AbstractCharacter;

public class MoveCorrectionMsg extends ClientNetMsg {

	private int sourceType;
	private int sourceID;
	private float startLat;
	private float startAlt;
	private float startLon;
	private float endLat;
	private float endAlt;
	private float endLon;
	private int unknown01 = 2;
	private int unknown02 = 0;
	private int unknown03 = 0;

	/**
	 * This is the general purpose constructor.
	 */
	public MoveCorrectionMsg(AbstractCharacter ac, boolean teleport) {
		super(Protocol.MOVECORRECTION);
		this.sourceType = ac.getObjectType().ordinal();
		this.sourceID = ac.getObjectUUID();
		this.startLat = ac.getLoc().x;
		this.startAlt = ac.getLoc().y;
		this.startLon = ac.getLoc().z;
		if (teleport){
			this.endLat = ac.getLoc().x;
			this.endAlt = ac.getLoc().y;
			this.endLon = ac.getLoc().z;
		}else{
			if (ac.isMoving()){
				this.endLat = ac.getEndLoc().x;
				this.endAlt = ac.getEndLoc().y;
				this.endLon = ac.getEndLoc().z;
			}else{
				this.endLat = ac.getLoc().x;
				this.endAlt = ac.getLoc().y;
				this.endLon = ac.getLoc().z;
			}
		}

		this.unknown01 = Float.floatToIntBits(ac.getAltitude());
		this.unknown02 =Float.floatToIntBits(ac.getAltitude());
		this.unknown03 = Float.floatToIntBits(ac.getAltitude());

	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public MoveCorrectionMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.MOVECORRECTION, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);

		writer.putFloat(this.startLat);
		writer.putFloat(this.startAlt);
		writer.putFloat(this.startLon);

		writer.putFloat(this.endLat);
		writer.putFloat(this.endAlt);
		writer.putFloat(this.endLon);
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		writer.putInt(this.unknown03);


	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();

		this.startLat = reader.getFloat();
		this.startAlt = reader.getFloat();
		this.startLon = reader.getFloat();

		this.endLat = reader.getFloat();
		this.endAlt = reader.getFloat();
		this.endLon = reader.getFloat();

		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();

	}

	public int getSourceType() {
		return this.sourceType;
	}

	public int getSourceID() {
		return this.sourceID;
	}

	public float getStartLat() {
		return this.startLat;
	}

	public float getStartLon() {
		return this.startLon;
	}

	public float getStartAlt() {
		return this.startAlt;
	}

	public float getEndLat() {
		return this.endLat;
	}

	public float getEndLon() {
		return this.endLon;
	}

	public float getEndAlt() {
		return this.endAlt;
	}

	public int getUnknown01() {
		return this.unknown01;
	}

	public int getUnknown02() {
		return this.unknown01;
	}

	public int getUnknown03() {
		return this.unknown01;
	}

	public void setSourceType(int value) {
		this.sourceType = value;
	}

	public void setSourceID(int value) {
		this.sourceID = value;
	}

	public void setStartLat(float value) {
		this.startLat = value;
	}

	public void setStartLon(float value) {
		this.startLon = value;
	}

	public void setStartAlt(float value) {
		this.startAlt = value;
	}

	public void setStartCoord(Vector3fImmutable value) {
		this.startLat = value.x;
		this.startAlt = value.y;
		this.startLon = value.z;
	}

	public void setEndLat(float value) {
		this.endLat = value;
	}

	public void setEndLon(float value) {
		this.endLon = value;
	}

	public void setEndAlt(float value) {
		this.endAlt = value;
	}

	public void setEndCoord(Vector3fImmutable value) {
		this.endLat = value.x;
		this.endAlt = value.y;
		this.endLon = value.z;
	}

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}

	public void setUnknown02(int value) {
		this.unknown02 = value;
	}

	public void setUnknown03(int value) {
		this.unknown03 = value;
	}

	public void setPlayer(AbstractCharacter ac) {
		this.sourceType = 85;
		this.sourceID = ac.getObjectUUID();
		this.setStartCoord(ac.getLoc());
		this.setEndCoord(ac.getEndLoc());
	}
}
