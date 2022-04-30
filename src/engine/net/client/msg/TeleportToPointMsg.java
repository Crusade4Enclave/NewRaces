// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.AbstractWorldObject;

public class TeleportToPointMsg extends ClientNetMsg {

	private int sourceType;
	private int sourceUUID;
	private float endLat;
	private float endLon;
	private float endAlt;
	private int targetType;
	private int targetUUID;
	private int unknown01;
	private int unknown02;

	/**
	 * This is the general purpose constructor.
	 */
	public TeleportToPointMsg(AbstractWorldObject ago, float endLat, float endAlt, float endLon, long targetID, int unknown01, int unknown02) {
		super(Protocol.TELEPORT);

		this.sourceType = ago.getObjectType().ordinal();
		this.sourceUUID = ago.getObjectUUID();
		this.endLat = endLat;
		this.endAlt = endAlt;
		this.endLon = endLon;
		if (targetID != 0){
			this.targetType = GameObjectType.Building.ordinal();
			this.targetUUID = (int) targetID;
		}else{
			this.targetType = 0;
			this.targetUUID = 0;
		}
		
		

		this.unknown01 = unknown01;
		this.unknown02 = unknown02;
		
		if (ago.getRegion() != null){
			this.targetType = GameObjectType.Building.ordinal();
			this.targetUUID = ago.getRegion().parentBuildingID;
			this.unknown01 = ago.getRegion().level;
			this.unknown02 = ago.getRegion().room;
		}
	}

	/**
	 * This is the general purpose constructor.
	 */
	public TeleportToPointMsg() {
		super(Protocol.TELEPORT);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TeleportToPointMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.TELEPORT, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceUUID);
		writer.putFloat(this.endLat);
		writer.putFloat(this.endAlt);
		writer.putFloat(this.endLon);
		writer.putInt(this.targetType);
		writer.putInt(this.targetUUID);
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.sourceType = reader.getInt();
		this.sourceUUID = reader.getInt();
		this.endLat = reader.getInt();
		this.endAlt = reader.getInt();
		this.endLon = reader.getInt();
		this.targetType = reader.getInt();
		this.targetUUID = reader.getInt();
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
	}


	/**
	 * @return the endLat
	 */
	public float getEndLat() {
		return endLat;
	}

	/**
	 * @param endLat
	 *            the endLat to set
	 */
	public void setEndLat(float endLat) {
		this.endLat = endLat;
	}

	/**
	 * @return the endLon
	 */
	public float getEndLon() {
		return endLon;
	}

	/**
	 * @param endLon
	 *            the endLon to set
	 */
	public void setEndLon(float endLon) {
		this.endLon = endLon;
	}

	/**
	 * @return the endAlt
	 */
	public float getEndAlt() {
		return endAlt;
	}

	/**
	 * @param endAlt
	 *            the endAlt to set
	 */
	public void setEndAlt(float endAlt) {
		this.endAlt = endAlt;
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

	public int getSourceType() {
		return sourceType;
	}

	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	public int getSourceUUID() {
		return sourceUUID;
	}

	public void setSourceUUID(int sourceUUID) {
		this.sourceUUID = sourceUUID;
	}

	public int getTargetType() {
		return targetType;
	}

	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	public int getTargetUUID() {
		return targetUUID;
	}

	public void setTargetUUID(int targetUUID) {
		this.targetUUID = targetUUID;
	}

}
