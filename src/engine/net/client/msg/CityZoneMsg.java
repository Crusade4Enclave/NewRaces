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
import engine.objects.Zone;

public class CityZoneMsg extends ClientNetMsg {

	private int messageType; //1 or 2
	private int zoneType;
	private int zoneID;
	private float locX;
	private float locY;
	private float locZ;
	private String name;
	private float radiusX;
	private float radiusZ;
	private int unknown01 = 0;

	/**
	 * This is the general purpose constructor.
	 */

	public CityZoneMsg(int messageType, float locX, float locY, float locZ, String name, Zone zone, float radiusX, float radiusZ) {
		super(Protocol.CITYZONE);
		this.messageType = messageType; //only 1 or 2 used on message type
		this.zoneType = GameObjectType.Zone.ordinal();
		this.zoneID = zone.getObjectUUID();
		this.locX = locX;
		this.locY = locY;
		this.locZ = locZ;
		this.name = name;
		this.radiusX = radiusX;
		this.radiusZ = radiusZ;
		this.unknown01 = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public CityZoneMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.CITYZONE, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {


		writer.putInt(this.messageType);


		//MSGTYPE 3 SERIALIZATION
		if (this.messageType == 3){
			writer.putInt(1);
			writer.putInt(this.zoneType);
			writer.putInt(this.zoneID);
			writer.putString("RuinedCity");
			writer.putString("Ruined");
			writer.putFloat(this.locX);
			writer.putFloat(this.locY);
			writer.putFloat(this.locZ);
			writer.putInt(0);
			return;
		}



		//END SERIALIZIONTYPE 3

		//	writer.putInt(this.messageType);
		if (this.messageType == 1){
			writer.putInt(this.zoneType);
			writer.putInt(this.zoneID);
		}

		writer.putFloat(this.locX);
		writer.putFloat(this.locY);
		writer.putFloat(this.locZ);
		writer.putString(this.name);
		if (this.messageType == 1) {
			writer.putFloat(this.radiusX);
			writer.putFloat(this.radiusZ);
		}
		writer.putInt(this.unknown01);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		//		this.locX = reader.getFloat();
		//		this.locY = reader.getFloat();
		//		this.locZ = reader.getFloat();
		//		this.name = reader.getString();
		//		this.unknown01 = reader.getInt();
	}

	public float getLocX() {
		return this.locX;
	}

	public float getLocY() {
		return this.locY;
	}

	public float getLocZ() {
		return this.locZ;
	}

	public String getName() {
		return this.name;
	}

	public int getUnknown01() {
		return this.unknown01;
	}

	public void setLocX(float value) {
		this.locX = value;
	}

	public void setLocY(float value) {
		this.locY = value;
	}

	public void setLocZ(float value) {
		this.locZ = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}


}
