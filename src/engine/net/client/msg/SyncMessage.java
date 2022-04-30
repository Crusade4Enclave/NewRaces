// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.exception.SerializationException;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Building;
import engine.objects.Zone;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;

public class SyncMessage extends ClientNetMsg {
	
	private int type;
	private int size;
	private int pad = 0;
	private int objectType;
	private int objectUUID;

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public SyncMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CITYASSET, origin, reader);
	}
	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
	//none yet
	}

	public SyncMessage() {
		super(Protocol.CITYASSET);
	}
	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {
		//lets do returns before writing so we don't send improper structures to the client
		
		Building tol = BuildingManager.getBuilding(this.objectUUID);
                
		if (tol == null){
			Logger.debug("TOL is null");
			return;
		}
		Zone zone = ZoneManager.findSmallestZone(tol.getLoc());
		if (zone == null){
			Logger.debug( "Zone is null");
			return;
		}
		ArrayList<Building> allCityAssets = DbManager.BuildingQueries.GET_ALL_BUILDINGS_FOR_ZONE(zone);
		
                // *** Refactor: collection created but never used?
                        
                ArrayList<Building> canProtectAssets = new ArrayList<>();

                for (Building b: allCityAssets){
                        if (b.getBlueprintUUID() != 0)
				canProtectAssets.add(b);
		}
                
                // *** Refactor : Not sure what this synch message does
                // Get the feeling it should be looping over upgradable
                // assets.
		writer.putInt(0);
		writer.putInt(0);
                writer.putInt(this.objectType);
                writer.putInt(this.objectUUID);
		writer.putInt(allCityAssets.size());
		for (Building b: allCityAssets){
			String name = b.getName();
		//	if (name.equals(""))
		//		name = b.getBuildingSet().getName();
                        writer.putInt(b.getObjectType().ordinal());
                        writer.putInt(b.getObjectUUID());
			
			writer.putString(b.getName()); // Blueprint name?
			writer.putString(b.getGuild().getName());
			writer.putInt(20);//  \/ Temp \/
			writer.putInt(b.getRank());
			writer.putInt(1);  // symbol
			writer.putInt(7);  //TODO identify these Guild tags??
			writer.putInt(17);
			writer.putInt(14);
			writer.putInt(14);
			writer.putInt(98);// /\ Temp /\
			
		}		
	}

	public int getObjectType() {
		return objectType;
	}

	public void setObjectType(int value) {
		this.objectType = value;
	}

	public void setPad(int value) {
		this.pad = value;
	}

	public int getUUID() {
		return objectUUID;

	}

	public int getPad() {
		return pad;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
}
