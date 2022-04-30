// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;

public class PerformActionMsg extends ClientNetMsg {

	protected int powerUsedID;
	protected int numTrains;
	protected int sourceType;
	protected int sourceID;
	protected int targetType;
	protected int targetID;

	protected float targetX;
	protected float targetY;
	protected float targetZ;
	protected int unknown04; //1, 2, 6
	protected int unknown05;

	protected int realTrains; //not serialized. Used for mob AI tracking.

	/**
	 * This is the general purpose constructor.
	 */
	public PerformActionMsg() {
		super(Protocol.POWER);
	}

	public PerformActionMsg(int powerUsedID, int numTrains, int sourceType, int sourceID, int targetType, int targetID, float targetX, float targetY, float targetZ, int unknown04, int unknown05) {
		super(Protocol.POWER);
		this.powerUsedID = powerUsedID;
		this.numTrains = numTrains;
		this.sourceType = sourceType;
		this.sourceID = sourceID;
		this.targetType = targetType;
		this.targetID = targetID;
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetZ = targetZ;
		this.unknown04 = unknown04;
		this.unknown05 = unknown05;

		this.realTrains = this.numTrains;
	}
	
	

	public PerformActionMsg(PerformActionMsg msg) {
		super(Protocol.POWER);
		this.powerUsedID = msg.powerUsedID;
		this.numTrains = msg.numTrains;
		this.sourceType = msg.sourceType;
		this.sourceID = msg.sourceID;
		this.targetType = msg.targetType;
		this.targetID = msg.targetID;
		this.targetX = msg.targetX;
		this.targetY = msg.targetY;
		this.targetZ = msg.targetZ;
		this.unknown04 = msg.unknown04;
		this.unknown05 = msg.unknown05;
		this.realTrains = msg.realTrains;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public PerformActionMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.POWER, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.powerUsedID);
		writer.putInt(this.numTrains);

		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);
		writer.putInt(this.targetType);
		writer.putInt(this.targetID);

		writer.putFloat(this.targetX);
		writer.putFloat(this.targetY);
		writer.putFloat(this.targetZ);
		writer.putInt(this.unknown04);
		writer.putInt(this.unknown05);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.powerUsedID = reader.getInt();
		this.numTrains = reader.getInt();
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
		this.targetType = reader.getInt();
		this.targetID = reader.getInt();

		this.targetX = reader.getFloat();
		this.targetY = reader.getFloat();
		this.targetZ = reader.getFloat();
		this.unknown04 = reader.getInt();
		this.unknown05 = reader.getInt(); //2=FailToCast, 3=Miss, 4=Dodge, 5=Immune, 6=resisted, 7=targetDead, 8=PowerInterupted, 9=NoValidTargets, 10=NotBeenGrantedPower
		this.realTrains = this.numTrains;
	}

	/**
	 * @return the powerUsedID
	 */
	public int getPowerUsedID() {
		return powerUsedID;
	}

	/**
	 * @param powerUsedID
	 *            the powerUsedID to set
	 */
	public void setPowerUsedID(int powerUsedID) {
		this.powerUsedID = powerUsedID;
	}

	/**
	 * @return the numTrains
	 */
	public int getNumTrains() {
		return numTrains;
	}

	/**
	 * @param numTrains
	 *            the numTrains to set
	 */
	public void setNumTrains(int numTrains) {
		this.numTrains = numTrains;
	}

	/**
	 * @return the sourceType
	 */
	public int getSourceType() {
		return sourceType;
	}

	/**
	 * @param sourceType
	 *            the sourceType to set
	 */
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * @return the sourceID
	 */
	public int getSourceID() {
		return sourceID;
	}

	/**
	 * @param sourceID
	 *            the sourceID to set
	 */
	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}

	/**
	 * @return the targetType
	 */
	public int getTargetType() {
		return targetType;
	}

	/**
	 * @param targetType
	 *            the targetType to set
	 */
	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	/**
	 * @return the targetID
	 */
	public int getTargetID() {
		return targetID;
	}

	/**
	 * @param targetID
	 *            the targetID to set
	 */
	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

	/**
	 * @return the unknown01
	 */
	public float getTargetX() {
		return targetX;
	}

	/**
     */
	public void setTargetX(float targetX) {
		this.targetX = targetX;
	}

	/**
	 * @return the unknown02
	 */
	public float getTargetY() {
		return targetY;
	}

	public void setTargetY(float targetY) {
		this.targetY = targetY;
	}

	/**
	 * @return the unknown03
	 */
	public float getTargetZ() {
		return targetZ;
	}


	public void setTargetZ(float targetZ) {
		this.targetZ = targetZ;
	}

	public void setTargetLoc(Vector3f targetLoc) {
		this.targetX = targetLoc.x;
		this.targetY = targetLoc.y;
		this.targetZ = targetLoc.z;
	}

	public Vector3fImmutable getTargetLoc() {
		return new Vector3fImmutable(this.targetX, this.targetY, this.targetZ);
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

	public int getRealTrains() {
		return this.realTrains;
	}
}
