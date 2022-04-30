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
import engine.objects.Building;

import java.util.HashMap;


public class TaxResourcesMsg extends ClientNetMsg {


	private int buildingID;
	private int msgType;
	private HashMap<Integer,Integer> resources;
	private float taxPercent;


	public TaxResourcesMsg(Building building, int msgType) {
		super(Protocol.TAXRESOURCES);
		this.buildingID = building.getObjectUUID();
		this.msgType = msgType;

	}

	public TaxResourcesMsg() {
		super(Protocol.TAXRESOURCES);
	}


	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TaxResourcesMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.TAXRESOURCES, origin, reader);
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
		HashMap<Integer,Integer> resourcesTemp = new HashMap<>();
		int size = reader.getInt();
		for (int i=0;i<size;i++){
			int resourceHash = reader.getInt();
			resourcesTemp.put(resourceHash,0);
		}
		this.resources = resourcesTemp;
		taxPercent = reader.getFloat();
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
		writer.putInt(this.resources.size());
		for (int resource:resources.keySet())
			writer.putInt(resource);
		writer.putFloat(this.taxPercent);
		writer.putInt(this.resources.size());
		for (int resource:resources.keySet()){
			writer.putInt(resource);
			writer.putInt(0);
			writer.putInt(resources.get(resource));
		}
			
	}

	public int getBuildingID() {
		return buildingID;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public HashMap<Integer,Integer> getResources() {
		return resources;
	}

	public float getTaxPercent() {
		return taxPercent;
	}

}
