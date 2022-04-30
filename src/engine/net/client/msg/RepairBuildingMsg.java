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
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;


public class RepairBuildingMsg extends ClientNetMsg {

	private int type;
	private int buildingID;
	private int maxHP;
	private int missingHealth;
	private int strongBox;
	private int repairCost;



	public RepairBuildingMsg(int buildingID, int maxHP, int missingHealth,int repairCost,int strongBox) {
		super(Protocol.REPAIRBUILDING);
		this.buildingID = buildingID;
		this.maxHP = maxHP;
		this.missingHealth = missingHealth;
		this.repairCost = repairCost;
		this.strongBox = strongBox;
	}

	public RepairBuildingMsg() {
		super(Protocol.REPAIRBUILDING);
	}


	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public RepairBuildingMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.REPAIRBUILDING, origin, reader);
	}
	//CALL THIS AFTER SANITY CHECKS AND BEFORE UPDATING HEALTH/GOLD.


	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.type = reader.getInt();
		reader.getInt(); //Building Type
		this.buildingID = reader.getInt();
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
		writer.putInt(this.maxHP);
		writer.putInt(this.strongBox);
		writer.putInt(0); //?
		writer.putInt(this.repairCost);
		writer.putInt(this.missingHealth);

	}



	public int getBuildingID() {
		return buildingID;
	}

	public void setBuildingID(int buildingID) {
		this.buildingID = buildingID;
	}
	public int getType() {
		return type;
	}
}
