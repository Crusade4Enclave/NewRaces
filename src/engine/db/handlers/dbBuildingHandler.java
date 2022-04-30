// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum;
import engine.Enum.DbObjectType;
import engine.Enum.ProtectionState;
import engine.Enum.TaxType;
import engine.gameManager.DbManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.awt.geom.Line2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class dbBuildingHandler extends dbHandlerBase {

	public dbBuildingHandler() {
		this.localClass = Building.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public Building CREATE_BUILDING(Building b) {
		try {
			b = this.addBuilding(b);
			b.setObjectTypeMask(MBServerStatics.MASK_BUILDING);
		} catch (Exception e) {
			Logger.error(e);
			b = null;
		}
		return b;
	}

	/*
	 *
	 * @param worldServerID
	 * @param OwnerUUID
	 * @param name
	 * @param meshUUID
	 * @param meshScale
	 * @param currentHP
	 * @param protectionState
	 * @param currentGold
	 * @param rank
	 * @param upgradeDate
	 * @param blueprintUUID
	 * @param w
	 * @param rotY
	 * @return
	 */
	public Building CREATE_BUILDING(int parentZoneID, int OwnerUUID, String name, int meshUUID,
									Vector3fImmutable location, float meshScale, int currentHP,
									ProtectionState protectionState, int currentGold, int rank,
									DateTime upgradeDate, int blueprintUUID, float w, float rotY) {

		Building toCreate = null;
		try {

			prepareCallable("CALL `building_CREATE`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? ,? ,?, ?);");

			setInt(1, parentZoneID);
			setInt(2, OwnerUUID);
			setString(3, name);
			setInt(4, meshUUID);
			setFloat(5, location.x);
			setFloat(6, location.y);
			setFloat(7, location.z);
			setFloat(8, meshScale);
			setInt(9, currentHP);
			setString(10, protectionState.name());
			setInt(11, currentGold);
			setInt(12, rank);

			if (upgradeDate != null)
				setTimeStamp(13, upgradeDate.getMillis());
			else
				setNULL(13, java.sql.Types.DATE);

			setInt(14, blueprintUUID);
			setFloat(15, w);
			setFloat(16, rotY);

			int objectUUID = (int) getUUID();

			if (objectUUID > 0)
				toCreate = GET_BUILDINGBYUUID(objectUUID);

		} catch (Exception e) {
			Logger.error("CREATE_BUILDING", e.getMessage());
			return null;
		}
		return toCreate;

	}

	public boolean DELETE_FROM_DATABASE(final Building b) {

		return removeFromBuildings(b);
	}

	public ArrayList<Building> GET_ALL_BUILDINGS_FOR_ZONE(Zone zone) {
		prepareCallable("SELECT `obj_building`.*, `object`.`parent` FROM `object` INNER JOIN `obj_building` ON `obj_building`.`UID` = `object`.`UID` WHERE `object`.`parent` = ?;");
		setLong(1, zone.getObjectUUID());
		return getLargeObjectList();
	}

	public ArrayList<Building> GET_ALL_BUILDINGS() {
		prepareCallable("SELECT `obj_building`.*, `object`.`parent` FROM `object` INNER JOIN `obj_building` ON `obj_building`.`UID` = `object`.`UID`;");
		return getLargeObjectList();
	}

	public Building GET_BUILDINGBYUUID(int uuid) {
		if (uuid == 0)
			return null;

		Building building = (Building) DbManager.getFromCache(Enum.GameObjectType.Building, uuid);

		if (building != null)
			return building;

		prepareCallable("SELECT `obj_building`.*, `object`.`parent` FROM `object` INNER JOIN `obj_building` ON `obj_building`.`UID` = `object`.`UID` WHERE `object`.`UID` = ?;");

		setLong(1, (long) uuid);
		return (Building) getObjectSingle(uuid);

	}

	public Building GET_BUILDING_BY_MESH(final int meshID) {
		Building toReturn = null;
		prepareCallable("CALL building_GETBYMESH(?)");
		setInt(1, meshID);
		try {
			ResultSet rs = executeQuery();
			if (rs.next())
				toReturn = new Building(rs);
			rs.close();
		} catch (SQLException e) {
			Logger.error("Building", e);
			return null;
		} finally {
			closeCallable();
		}
		return toReturn;
	}

	public String SET_PROPERTY(final Building b, String name, Object new_value) {
		prepareCallable("CALL building_SETPROP(?,?,?)");
		setLong(1, b.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		return getResult();
	}

	public String SET_PROPERTY(final Building b, String name, Object new_value, Object old_value) {
		prepareCallable("CALL building_GETSETPROP(?,?,?,?)");
		setLong(1, b.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		setString(4, String.valueOf(old_value));
		return getResult();
	}

	public int MOVE_BUILDING(long buildingID, long parentID, float locX, float locY, float locZ) {
		prepareCallable("UPDATE `object` INNER JOIN `obj_building` On `object`.`UID` = `obj_building`.`UID` SET `object`.`parent`=?, `obj_building`.`locationX`=?, `obj_building`.`locationY`=?, `obj_building`.`locationZ`=? WHERE `obj_building`.`UID`=?;");
		setLong(1, parentID);
		setFloat(2, locX);
		setFloat(3, locY);
		setFloat(4, locZ);
		setLong(5, buildingID);
		return executeUpdate();
	}

	private Building addBuilding(Building toAdd) {
		prepareCallable("CALL `building_CREATE`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		setLong(1, toAdd.getParentZoneID());
		setLong(2, toAdd.getOwnerUUID());
		setString(3, toAdd.getName());
		setInt(4, toAdd.getMeshUUID());
		setFloat(5, toAdd.getStatLat());
		setFloat(6, toAdd.getStatAlt());
		setFloat(7, toAdd.getStatLon());
		setFloat(8, toAdd.getMeshScale().x);
		setInt(9, (int) toAdd.getHealth());
		setString(10, toAdd.getProtectionState().name());
		setInt(11, toAdd.getStrongboxValue());
		setInt(12, toAdd.getRank());

		// Write Joda DateTime to database
		// *** Refactor : Wrap this logic in our
		// own override setDateTime() ?
		if (toAdd.getUpgradeDateTime() != null)
			setLocalDateTime(13, toAdd.getUpgradeDateTime());
		else
			setNULL(13, java.sql.Types.DATE);

		setInt(14, toAdd.getBlueprintUUID());
		setFloat(15, toAdd.getw());
		setFloat(16, toAdd.getRot().y);

		int objectUUID = (int) getUUID();

		if (objectUUID > 0)
			return GET_BUILDINGBYUUID(objectUUID);
		return null;

	}

	private boolean removeFromBuildings(final Building b) {
		prepareCallable("DELETE FROM `object` WHERE `UID` = ?");
		setLong(1, b.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean ClaimAsset(final long SetBuildingID, int newowner, int OldOwner) {

		prepareCallable("UPDATE `obj_building` SET `ownerUUID`=? WHERE `UID`=? and `ownerUUID`= ?");
		setInt(1, newowner);
		setLong(2, SetBuildingID);
		setLong(3, OldOwner);
		return (executeUpdate() > 0);
	}

	public boolean CHANGE_NAME(Building b, String newName) {
		prepareCallable("UPDATE `obj_building` SET `name`=? WHERE `UID`=?");
		setString(1, newName);
		setLong(2, b.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean SET_RESERVE(Building b, int reserveAmount) {
		prepareCallable("UPDATE `obj_building` SET `reserve`=? WHERE `UID`=?");
		setInt(1, reserveAmount);
		setLong(2, b.getObjectUUID());
		return (executeUpdate() > 0);
	}

	//CAS update to rank
	public boolean CHANGE_RANK(final long buildingID, int newRank) {
		prepareCallable("UPDATE `obj_building` SET `rank`=? WHERE `UID`=?");
		setInt(1, newRank);
		setLong(2, buildingID);
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_BUILDING_HEALTH(final long buildingID, int NewHealth) {
		prepareCallable("UPDATE `obj_building` SET `currentHP`=? WHERE `UID`=?");
		setInt(1, NewHealth);
		setLong(2, buildingID);
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_BUILDING_ALTITUDE(final long buildingID, float newAltitude) {
		prepareCallable("UPDATE `obj_building` SET `locationY`=? WHERE `UID`=?");
		setFloat(1, newAltitude);
		setLong(2, buildingID);
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_PROTECTIONSTATE(final long buildingUUID, ProtectionState protectionState) {

		try {
			prepareCallable("UPDATE `obj_building` SET `protectionState`=? WHERE `UID`=?");
			setString(1, protectionState.name());
			setLong(2, buildingUUID);
			return (executeUpdate() > 0);
		} catch (Exception e) {
			Logger.error(e.toString());
			return false;
		}
	}

	public boolean UPDATE_DOOR_LOCK(final int buildingUUID, int doorFlags) {

		try {
			prepareCallable("UPDATE obj_building SET doorState = ? WHERE UID = ?");

			setInt(1, doorFlags);
			setInt(2, buildingUUID);

			executeUpdate();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean ADD_TO_FRIENDS_LIST(final long buildingID, final long friendID, final long guildID, final int friendType) {
		prepareCallable("INSERT INTO `dyn_building_friends` (`buildingUID`, `playerUID`,`guildUID`, `friendType`) VALUES (?,?,?,?)");
		setLong(1, buildingID);
		setLong(2, friendID);
		setLong(3, guildID);
		setInt(4, friendType);
		return (executeUpdate() > 0);
	}

	public boolean REMOVE_FROM_FRIENDS_LIST(final long buildingID, long friendID, long guildID, int friendType) {
		prepareCallable("DELETE FROM `dyn_building_friends` WHERE `buildingUID`=? AND `playerUID`=? AND `guildUID` =? AND `friendType` = ?");
		setLong(1, buildingID);
		setLong(2, friendID);
		setLong(3,guildID);
		setInt(4, friendType);
		return (executeUpdate() > 0);
	}

	public boolean REMOVE_FROM_CONDEMNED_LIST(final long buildingID, long friendID, long guildID, int friendType) {
		prepareCallable("DELETE FROM `dyn_building_condemned` WHERE `buildingUID`=? AND `playerUID`=? AND `guildUID` =? AND `friendType` = ?");
		setLong(1, buildingID);
		setLong(2, friendID);
		setLong(3,guildID);
		setInt(4, friendType);
		return (executeUpdate() > 0);
	}

	public void CLEAR_FRIENDS_LIST(final long buildingID) {
		prepareCallable("DELETE FROM `dyn_building_friends` WHERE `buildingUID`=?");
		setLong(1, buildingID);
		executeUpdate();
	}

	public void CLEAR_CONDEMNED_LIST(final long buildingID) {
		prepareCallable("DELETE FROM `dyn_building_condemned` WHERE `buildingUID`=?");
		setLong(1, buildingID);
		executeUpdate();
	}

	public boolean CLEAR_PATROL(final long buildingID) {
		prepareCallable("DELETE FROM `dyn_building_patrol_points` WHERE `buildingUID`=?");
		setLong(1, buildingID);
		return (executeUpdate() > 0);
	}

	public void LOAD_ALL_FRIENDS_FOR_BUILDING(Building building) {

		if (building == null)
			return;

		prepareCallable("SELECT * FROM `dyn_building_friends` WHERE `buildingUID` = ?");
		setInt(1,building.getObjectUUID());

		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {
				BuildingFriends friend = new BuildingFriends(rs);
				switch(friend.getFriendType()){
				case 7:
					building.getFriends().put(friend.getPlayerUID(), friend);
					break;
				case 8:
				case 9:
					building.getFriends().put(friend.getGuildUID(), friend);
					break;
				}
			}

		} catch (SQLException e) {
			Logger.error("LOAD friends for building: " + e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}

	}

	public void LOAD_ALL_CONDEMNED_FOR_BUILDING(Building building) {

		if (building == null)
			return;

		prepareCallable("SELECT * FROM `dyn_building_condemned` WHERE `buildingUID` = ?");
		setInt(1,building.getObjectUUID());

		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {
				Condemned condemn = new Condemned(rs);
				switch(condemn.getFriendType()){
				case 2:
					building.getCondemned().put(condemn.getPlayerUID(), condemn);
					break;
				case 4:
				case 5:
					building.getCondemned().put(condemn.getGuildUID(), condemn);
					break;
				}
			}

		} catch (SQLException e) {
			Logger.error("LOAD Condemned for building: " + e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}

	}

	public ArrayList<Vector3fImmutable> LOAD_PATROL_POINTS(Building building) {
		if (building == null)
			return null;
		ArrayList<Vector3fImmutable> patrolPoints = new ArrayList<>();


		prepareCallable("SELECT * FROM `dyn_building_patrol_points` WHERE `buildingUID` = ?");
		setInt(1,building.getObjectUUID());

		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {
				float x1 = rs.getFloat("patrolX");
				float y1 = rs.getFloat("patrolY");
				float z1 = rs.getFloat("patrolZ");
				Vector3fImmutable patrolPoint = new Vector3fImmutable(x1,y1,z1);
				patrolPoints.add(patrolPoint);
			}

		} catch (SQLException e) {
			Logger.error("LOAD Patrol Points for building: " + e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}

		return patrolPoints;

	}

	public boolean ADD_TO_CONDEMNLIST(final long parentUID, final long playerUID, final long guildID, final int friendType) {
		prepareCallable("INSERT INTO `dyn_building_condemned` (`buildingUID`, `playerUID`,`guildUID`, `friendType`) VALUES (?,?,?,?)");
		setLong(1, parentUID);
		setLong(2, playerUID);
		setLong(3, guildID);
		setInt(4, friendType);
		return (executeUpdate() > 0);
	}

	public boolean ADD_TO_PATROL(final long parentUID, final Vector3fImmutable patrolPoint) {
		prepareCallable("INSERT INTO `dyn_building_patrol_points` (`buildingUID`, `patrolX`,`patrolY`, `patrolZ`) VALUES (?,?,?,?)");
		setLong(1, parentUID);
		setFloat(2, (int)patrolPoint.x);
		setFloat(3, (int)patrolPoint.y);
		setFloat(4, (int)patrolPoint.z);
		return (executeUpdate() > 0);
	}

	public boolean ADD_TO_COLLIDE(final long parentUID, final float startX, final float startY, final float endX, final float endY) {
		prepareCallable("INSERT INTO `static_building_colliders` (`MeshID`, `startX`,`startY`, `endX`, `endY`) VALUES (?,?,?,?,?)");
		setLong(1, parentUID);
		setFloat(2, startX);
		setFloat(3, startY);
		setFloat(4, endX);
		setFloat(5, endY);
		return (executeUpdate() > 0);
	}

	public ArrayList<Line2D.Float> GET_COLLIDERS(final long meshID) {
		ArrayList<Line2D.Float> colliders = new ArrayList<>();
		prepareCallable("SELECT * FROM `static_building_colliders` WHERE `MeshID`=? AND `doorID` =0");
		setLong(1, meshID);
		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {
				int startX = rs.getInt("startX");
				int startY = rs.getInt("startY");
				int endX = rs.getInt("endX");
				int endY = rs.getInt("endY");
				colliders.add(new Line2D.Float(startX,startY,endX,endY));
			}

			rs.close();
		} catch (SQLException e) {
			Logger.error("dbBuildingHandler.GET_COLLIDERS", e);
		} finally {
			closeCallable();
		}
		return colliders;
	}

	public ArrayList<Line2D.Float> GET_DOOR_COLLIDERS(final long meshID) {
		ArrayList<Line2D.Float> colliders = new ArrayList<>();
		prepareCallable("SELECT * FROM `static_building_colliders` WHERE `MeshID`=? AND `doorID` <> 0");
		setLong(1, meshID);
		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {
				int startX = rs.getInt("startX");
				int startY = rs.getInt("startY");
				int endX = rs.getInt("endX");
				int endY = rs.getInt("endY");
				colliders.add(new Line2D.Float(startX,startY,endX,endY));
			}

			rs.close();
		} catch (SQLException e) {
			Logger.error("dbBuildingHandler.GET_COLLIDERS", e);
		} finally {
			closeCallable();
		}
		return colliders;
	}
	public HashMap<Integer, ArrayList<BuildingRegions>> LOAD_BUILDING_REGIONS() {

		HashMap<Integer, ArrayList<BuildingRegions>> regions;
		BuildingRegions thisRegions;


		regions = new HashMap<>();
		int recordsRead = 0;

		prepareCallable("SELECT * FROM static_building_regions");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;
				thisRegions = new BuildingRegions(rs);
				if (regions.get(thisRegions.getBuildingID()) == null){
					ArrayList<BuildingRegions> regionsList = new ArrayList<>();
					regionsList.add(thisRegions);
					regions.put(thisRegions.getBuildingID(), regionsList);
				}
				else{
					ArrayList<BuildingRegions>regionsList = regions.get(thisRegions.getBuildingID());
					regionsList.add(thisRegions);
					regions.put(thisRegions.getBuildingID(), regionsList);
				}
			}

			Logger.info( "read: " + recordsRead + " cached: " + regions.size());

		} catch (SQLException e) {
			Logger.error(": " + e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		return regions;
	}
	
	public HashMap<Integer, MeshBounds> LOAD_MESH_BOUNDS() {

		HashMap<Integer, MeshBounds> meshBoundsMap;
		MeshBounds meshBounds;

		meshBoundsMap = new HashMap<>();
		int recordsRead = 0;

		prepareCallable("SELECT * FROM static_mesh_bounds");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;
				meshBounds = new MeshBounds(rs);
				
				meshBoundsMap.put(meshBounds.meshID, meshBounds);
				
			}

			Logger.info( "read: " + recordsRead + " cached: " + meshBoundsMap.size());

		} catch (SQLException e) {
			Logger.error("LoadMeshBounds: " + e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		return meshBoundsMap;
	}

	public HashMap<Integer, ArrayList<StaticColliders>> LOAD_ALL_STATIC_COLLIDERS() {

		HashMap<Integer, ArrayList<StaticColliders>> colliders;
		StaticColliders thisColliders;


		colliders = new HashMap<>();
		int recordsRead = 0;

		prepareCallable("SELECT * FROM static_building_colliders");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;
				thisColliders = new StaticColliders(rs);
				if (colliders.get(thisColliders.getMeshID()) == null){
					ArrayList<StaticColliders> colliderList = new ArrayList<>();
					colliderList.add(thisColliders);
					colliders.put(thisColliders.getMeshID(), colliderList);
				}
				else{
					ArrayList<StaticColliders>colliderList = colliders.get(thisColliders.getMeshID());
					colliderList.add(thisColliders);
					colliders.put(thisColliders.getMeshID(), colliderList);
				}


			}

			Logger.info( "read: " + recordsRead + " cached: " + colliders.size());

		} catch (SQLException e) {
			Logger.error("LoadAllBlueprints: " + e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		return colliders;
	}

	// This public class inserted here as it's a generic utility function
	// with no other good place for it.  If you find a good home for it
	// feel free to move it.  -

	public final DbObjectType GET_UID_ENUM(long object_UID) {

		DbObjectType storedEnum = DbObjectType.INVALID;
		String typeString;

		if (object_UID == 0)
			return storedEnum;

		// Set up call to stored procedure
		prepareCallable("CALL object_UID_ENUM(?)");
		setLong(1, object_UID);

		try {

			// Evaluate database ordinal and return enum
			storedEnum = DbObjectType.valueOf(getString("type").toUpperCase());

		} catch (Exception e) {
			storedEnum = DbObjectType.INVALID;
			Logger.error("UID_ENUM ", "Orphaned Object? Lookup failed for UID: " + object_UID);
		} finally {
			closeCallable();
		}
		return storedEnum;
	}

	public ConcurrentHashMap<Integer, Integer> GET_FRIENDS(final long buildingID) {
		ConcurrentHashMap<Integer, Integer> friendsList = new ConcurrentHashMap<>();
		prepareCallable("SELECT * FROM `dyn_building_friends` WHERE `buildingUID`=?");
		setLong(1, buildingID);
		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {
				int friendType = rs.getInt("friendType");
				switch (friendType) {
				case 7:
					friendsList.put(rs.getInt("playerUID"), 7);
					break;
				case 8:
					friendsList.put(rs.getInt("guildUID"), 8);
					break;
				case 9:
					friendsList.put(rs.getInt("guildUID"), 9);
				}
			}

			rs.close();
		} catch (SQLException e) {
			Logger.error("dbBuildingHandler.GET_FRIENDS_GUILD_IC", e);
		} finally {
			closeCallable();
		}
		return friendsList;
	}

	public boolean updateBuildingRank(final Building b, int Rank) {

		prepareCallable("UPDATE `obj_building` SET `rank`=?,"
				+ "`upgradeDate`=?, `meshUUID`=?, `currentHP`=? "
				+ "WHERE `UID` = ?");

		setInt(1, Rank);
		setNULL(2, java.sql.Types.DATE);
		setInt(3, b.getBlueprint().getMeshForRank(Rank));
		setInt(4, b.getBlueprint().getMaxHealth(Rank));
		setInt(5, b.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean updateReverseKOS(final Building b, boolean reverse) {

		prepareCallable("UPDATE `obj_building` SET `reverseKOS`=? "
				+ "WHERE `UID` = ?");
		setBoolean(1, reverse);
		setInt(2, b.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean updateActiveCondemn(final Condemned condemn, boolean active) {

		prepareCallable("UPDATE `dyn_building_condemned` SET `active`=? "
				+ "WHERE`buildingUID` = ? AND `playerUID` = ? AND `guildUID` = ? AND `friendType` = ?");
		setBoolean(1, active);
		setInt(2, condemn.getParent());
		setInt(3, condemn.getPlayerUID());
		setInt(4, condemn.getGuildUID());
		setInt(5, condemn.getFriendType());
		return (executeUpdate() > 0);
	}

	public boolean updateBuildingOwner(final Building building, int ownerUUID) {

		prepareCallable("UPDATE `obj_building` SET `ownerUUID`=? "
				+ " WHERE `UID` = ?");

		setInt(1, ownerUUID);
		setInt(2, building.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean updateBuildingUpgradeTime(LocalDateTime upgradeDateTime, Building toUpgrade, int costToUpgrade) {

		prepareCallable("UPDATE obj_building SET upgradeDate=?, currentGold=? "
				+ "WHERE UID = ?");

		if (upgradeDateTime == null)
			setNULL(1, java.sql.Types.DATE);
		else
			setTimeStamp(1, upgradeDateTime.atZone(ZoneId.systemDefault())
					.toInstant().toEpochMilli());

		setInt(2, toUpgrade.getStrongboxValue() - costToUpgrade);
		setInt(3, toUpgrade.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean updateMaintDate(Building building) {

		prepareCallable("UPDATE obj_building SET maintDate=? "
				+ "WHERE UID = ?");

		if (building.maintDateTime == null)
			setNULL(1, java.sql.Types.DATE);
		else
			setLocalDateTime(1, building.maintDateTime);

		setInt(2, building.getObjectUUID());

		return (executeUpdate() > 0);
	}

	public boolean addTaxes(Building building, TaxType taxType, int amount, boolean enforceKOS){
		prepareCallable("UPDATE obj_building SET taxType=?, taxAmount = ?, enforceKOS = ? "
				+ "WHERE UID = ?");

		setString(1, taxType.name());
		setInt(2, amount);
		setBoolean(3, enforceKOS);
		setInt(4, building.getObjectUUID());

		return (executeUpdate() > 0);

	}

	public boolean removeTaxes(Building building){
		prepareCallable("UPDATE obj_building SET taxType=?, taxAmount = ?, enforceKOS = ?, taxDate = ? "
				+ "WHERE UID = ?");

		setString(1, TaxType.NONE.name());
		setInt(2, 0);
		setBoolean(3, false);
		setNULL(4, java.sql.Types.DATE);
		setInt(5, building.getObjectUUID());



		return (executeUpdate() > 0);

	}

	public boolean acceptTaxes(Building building) {

		prepareCallable("UPDATE obj_building SET taxDate=? "
				+ "WHERE UID = ?");

		setTimeStamp(1, DateTime.now().plusDays(7).getMillis());
		setInt(2, building.getObjectUUID());

		return (executeUpdate() > 0);
	}



}
