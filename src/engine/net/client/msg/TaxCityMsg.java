// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.exception.SerializationException;
import engine.gameManager.BuildingManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Building;
import engine.objects.GuildTag;


public class TaxCityMsg extends ClientNetMsg {


	private int buildingID;
	private int msgType;


	public TaxCityMsg(Building building, int msgType) {
		super(Protocol.TAXCITY);
		this.buildingID = building.getObjectUUID();
		this.msgType = msgType;

	}

	public TaxCityMsg() {
		super(Protocol.TAXCITY);
	}


	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TaxCityMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.TAXCITY, origin, reader);
	}
	//CALL THIS AFTER SANITY CHECKS AND BEFORE UPDATING HEALTH/GOLD.


	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.msgType = reader.getInt();
		reader.getInt(); //object Type.. always building
		this.buildingID = reader.getInt();
		reader.getInt();
		reader.getInt();

		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();

		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();

	}


	// Precache and configure this message before we serialize it


	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		writer.putInt(0);
		writer.putInt(GameObjectType.Building.ordinal());
		writer.putInt(this.buildingID);
		Building building = BuildingManager.getBuildingFromCache(this.buildingID);

		writer.putInt(0);
		writer.putFloat(.2f);
		GuildTag._serializeForDisplay(building.getGuild().getGuildTag(),writer);
		GuildTag._serializeForDisplay(building.getGuild().getGuildTag(),writer);



	}

	public int getGuildID() {
		return buildingID;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

}
