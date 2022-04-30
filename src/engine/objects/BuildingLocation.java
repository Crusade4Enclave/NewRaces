// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.DbManager;
import engine.math.Vector3fImmutable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class BuildingLocation extends AbstractGameObject {

	private final int buildingUUID;
	private final int type;
	private final int slot;
	private final int unknown;
	private final Vector3fImmutable loc;
	private final Vector3fImmutable rot;
	private final float w;


	/**
	 * ResultSet Constructor
	 */
	public BuildingLocation(ResultSet rs) throws SQLException {
		super(rs);
		this.buildingUUID = rs.getInt("BuildingID");
		this.type = rs.getInt("type");
		this.slot = rs.getInt("slot");
		this.unknown = rs.getInt("unknown");
		this.loc = new Vector3fImmutable(rs.getFloat("locX"), rs.getFloat("locY"), rs.getFloat("locZ"));
		this.rot = new Vector3fImmutable(rs.getFloat("rotX"), rs.getFloat("rotY"), rs.getFloat("rotZ"));
		this.w = rs.getFloat("w");
	}

	/*
	 * Getters
	 */

	public int getBuildingUUID() {
		return this.buildingUUID;
	}

	public Vector3fImmutable rotatedLoc() {
		Vector3fImmutable convertLoc = null;


		double rotY = 2.0 * Math.asin(this.rot.y);


		// handle building rotation

		convertLoc = new Vector3fImmutable(
				(float) ((loc.z * Math.sin(rotY)) + (loc.x * Math.cos(rotY))),
				loc.y,
				(float) ((loc.z * Math.cos(rotY)) - (loc.x * Math.sin(rotY))));

		return convertLoc;

	}

	public int getType() {
		return this.type;
	}

	public int getSlot() {
		return this.slot;
	}

	public int getUnknown() {
		return this.unknown;
	}

	public float getLocX() {
		return this.loc.x;
	}

	public float getLocY() {
		return this.loc.y;
	}

	public float getLocZ() {
		return this.loc.z;
	}

	public float getRotX() {
		return this.rot.x;
	}

	public float getRotY() {
		return this.rot.y;
	}

	public float getRotZ() {
		return this.rot.z;
	}

	public float getW() {
		return this.w;
	}

	public Vector3fImmutable getLoc() {
		return this.loc;
	}

	public Vector3fImmutable getRot() {
		return this.rot;
	}

	
	@Override
	public void updateDatabase() {
	}


	public static void loadAllLocations() {
		ArrayList<BuildingLocation> bls = DbManager.BuildingLocationQueries.LOAD_ALL_BUILDING_LOCATIONS();
		ConcurrentHashMap<Integer, BuildingModelBase> mbs = BuildingModelBase.getModelBases();
		for (BuildingLocation bl : bls) {
			int modelID = bl.buildingUUID;
			BuildingModelBase mb = null;
			if (!mbs.containsKey(modelID)) {
				mb = new BuildingModelBase(modelID);
				mbs.put(modelID, mb);
			} else
				mb = mbs.get(modelID);
			mb.addLocation(bl);

			if (bl.type == 6)
				mb.addSlotLocation(bl);
		}
	}
}
