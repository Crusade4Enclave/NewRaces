// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.AbstractCharacter;
import engine.objects.Building;

public class MoveToPointMsg extends ClientNetMsg {

	private int sourceType;
	private int sourceID;
	private float startLat;
	private float startLon;
	private float startAlt;
	private float endLat;
	private float endLon;
	private float endAlt;
	private int targetType;
	private int targetID;
	private int inBuilding; // 0=true, -1=false 0/1/2 = floor you are on
	private int unknown01;
	private byte unknown02;
	private byte unknown03;

	/**
	 * This is the general purpose constructor.
	 */
	public MoveToPointMsg() {
		super(Protocol.MOVETOPOINT);
	}



	public MoveToPointMsg(MoveToPointMsg msg) {
		super(Protocol.MOVETOPOINT);
		this.sourceType = msg.sourceType;
		this.sourceID = msg.sourceID;
		this.startLat = msg.startLat;
		this.startLon = msg.startLon;
		this.startAlt = msg.startAlt;
		this.endLat = msg.endLat;
		this.endLon = msg.endLon;
		this.endAlt = msg.endAlt;
		this.targetType = msg.targetType;
		this.targetID = msg.targetID;
		this.inBuilding = msg.inBuilding;
		this.unknown01 = msg.unknown01;
		this.unknown02 = msg.unknown02;
		this.unknown03 = msg.unknown03;
	}
	//Moving Furniture out of building to unload properly. //for outside regions only
	public MoveToPointMsg(Building building) {
		super(Protocol.MOVETOPOINT);
		this.sourceType = building.getObjectType().ordinal();
		this.sourceID = building.getObjectUUID();
		this.startLat = building.getLoc().x;
		this.startLon = building.getLoc().z;
		this.startAlt = building.getLoc().y;
		this.endLat = building.getLoc().x;
		this.endLon = building.getLoc().z;
		this.endAlt = building.getLoc().y;
		this.targetType = 0;
		this.targetID = 0;
		this.inBuilding = -1;
		this.unknown01 = -1;
		this.unknown02 = 0;
		this.unknown03 = 0;
	}



	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public MoveToPointMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.MOVETOPOINT, origin, reader);
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

		writer.putInt(this.targetType);
		writer.putInt(this.targetID);

		writer.putInt(this.inBuilding);
		writer.putInt(this.unknown01);

		writer.put((byte)0);
		writer.put((byte)0);

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

		this.targetType = reader.getInt();
		this.targetID = reader.getInt();

		this.inBuilding = reader.getInt();
		this.unknown01 = reader.getInt();

		this.unknown02 = reader.get();
		this.unknown03 = reader.get();
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

	public int getTargetType() {
		return this.targetType;
	}

	public int getTargetID() {
		return this.targetID;
	}

	public int getInBuilding() {
		return this.inBuilding;
	}

	public int getUnknown01() {
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

	public void setTargetType(int value) {
		this.targetType = value;
	}

	public void setTargetID(int value) {
		this.targetID = value;
	}

	public void clearTarget() {
		this.targetType = 0;
		this.targetID = 0;
	}

	public void setInBuilding(int value) {
		this.inBuilding = value;
	}

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}

	public void setPlayer(AbstractCharacter ac) {
		this.sourceType = ac.getObjectType().ordinal();
		this.sourceID = ac.getObjectUUID();
		this.setStartCoord(ac.getLoc());
		this.setEndCoord(ac.getEndLoc());
		this.targetType = 0;
		this.targetID = 0;
		this.inBuilding = ac.getInBuilding();
		this.unknown01 = ac.getInFloorID();

	}
	
	public void setTarget(AbstractCharacter ac, Building target){
		if (target == null){
			this.setStartCoord(ac.getLoc());
			this.setEndCoord(ac.getEndLoc());
			this.targetType = 0;
			this.targetID = 0;
			this.inBuilding = -1;
			this.unknown01 = -1;
		}else{
			Vector3fImmutable convertLocStart = ZoneManager.convertWorldToLocal(target, ac.getLoc());
			Vector3fImmutable convertLocEnd = convertLocStart;
			if (ac.isMoving())
				convertLocEnd = ZoneManager.convertWorldToLocal(target, ac.getEndLoc());
			
			this.setStartCoord(convertLocStart);
			this.setEndCoord(convertLocEnd);
			this.targetType = GameObjectType.Building.ordinal();
			this.targetID = target.getObjectUUID();
			this.inBuilding = ac.getInBuilding();
			this.unknown01 = ac.getInFloorID();
		}
		
	}

	public int getUnknown03() {
		return unknown03;
	}

	public int getUnknown02() {
		return unknown02;
	}
}
