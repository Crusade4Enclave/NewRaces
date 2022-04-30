// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.server.MBServerStatics;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class BuildingModelBase extends AbstractGameObject {

	private ArrayList<BuildingLocation> locations = new ArrayList<>();
	private static ConcurrentHashMap<Integer, BuildingModelBase> modelBases = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private final int buildingBaseID;

	private ArrayList<BuildingLocation> slotLocations = new ArrayList<>();

	public BuildingModelBase(int buildingBaseID) {
		super();
		this.buildingBaseID = buildingBaseID;
	}

	public void addLocation(BuildingLocation bl) {
		this.locations.add(bl);
	}

	public void addSlotLocation(BuildingLocation bl) {
		this.slotLocations.add(bl);
	}

	public ArrayList<BuildingLocation> getLocations() {
		return this.locations;
	}

	public BuildingLocation getNPCLocation(int slot) {
		for (BuildingLocation bl : this.locations) {
			if (bl.getType() == 6 && bl.getSlot() == slot)
				return bl;
		}
		return null; //not found
	}

	public BuildingLocation getStuckLocation() {

		for (BuildingLocation bl : this.locations) {
			if (bl.getType() == 8)
				return bl;
		}
		return null; //not found
	}

	public BuildingLocation getSlotLocation(int slot) {

		try{
			return this.slotLocations.get(slot - 1);
		}catch(Exception e){
			return null;
		}
	}


	@Override
	public void updateDatabase() {
	}

	public int getBuildingBaseID() {
		return this.buildingBaseID;
	}

	public static ConcurrentHashMap<Integer, BuildingModelBase> getModelBases() {
		return BuildingModelBase.modelBases;
	}

	public static BuildingModelBase getModelBase(int ID) {
		if (!BuildingModelBase.modelBases.containsKey(ID))
			BuildingModelBase.modelBases.put(ID, new BuildingModelBase(ID));
		return BuildingModelBase.modelBases.get(ID);
	}

}
