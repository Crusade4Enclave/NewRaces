// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.ai.MobileFSM.STATE;
import engine.math.Vector3fImmutable;
import engine.objects.Mob;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;
import engine.server.MBServerStatics;
import engine.server.world.WorldServer;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class dbMobHandler extends dbHandlerBase {

	public dbMobHandler() {
		this.localClass = Mob.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public Mob ADD_MOB(Mob toAdd, boolean isMob)
			 {
		prepareCallable("CALL `mob_CREATE`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		setLong(1, toAdd.getParentZoneID());
		setInt(2, toAdd.getMobBaseID());
		setInt(3, toAdd.getGuildUUID());
		setFloat(4, toAdd.getSpawnX());
		setFloat(5, toAdd.getSpawnY());
		setFloat(6, toAdd.getSpawnZ());
		setInt(7, 0);
		setFloat(8, toAdd.getSpawnRadius());
		setInt(9, toAdd.getTrueSpawnTime());
		if (toAdd.getContract() != null)
			setInt(10, toAdd.getContract().getContractID());
		else
			setInt(10, 0);
		setInt(11, toAdd.getBuildingID());
		setInt(12, toAdd.getLevel());
		int objectUUID = (int) getUUID();
		if (objectUUID > 0)
			return GET_MOB(objectUUID);
		return null;
	}

	public Mob ADD_SIEGE_MOB(Mob toAdd, boolean isMob)
			 {
		prepareCallable("CALL `mob_SIEGECREATE`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		setLong(1, toAdd.getParentZoneID());
		setInt(2, toAdd.getMobBaseID());
		setInt(3, toAdd.getGuildUUID());
		setFloat(4, toAdd.getSpawnX());
		setFloat(5, toAdd.getSpawnY());
		setFloat(6, toAdd.getSpawnZ());
		setInt(7,0);
		setFloat(8, toAdd.getSpawnRadius());
		setInt(9, toAdd.getTrueSpawnTime());
		setInt(10, toAdd.getBuildingID());

		int objectUUID = (int) getUUID();
		if (objectUUID > 0)
			return GET_MOB(objectUUID);
		return null;
	}

	public boolean updateUpgradeTime(Mob mob, DateTime upgradeDateTime) {



		try {

			prepareCallable("UPDATE obj_mob SET upgradeDate=? "
					+ "WHERE UID = ?");

			if (upgradeDateTime == null)
				setNULL(1, java.sql.Types.DATE);
			else
				setTimeStamp(1, upgradeDateTime.getMillis());

			setInt(2, mob.getObjectUUID());
			executeUpdate();
		} catch (Exception e) {
			Logger.error("Mob.updateUpgradeTime", "UUID: " + mob.getObjectUUID());
			return false;
		}
		return true;
	}

	public int DELETE_MOB(final Mob mob) {
		prepareCallable("DELETE FROM `object` WHERE `UID` = ?");
		setLong(1, mob.getDBID());
		return executeUpdate();
	}

	public void LOAD_PATROL_POINTS(Mob captain) {



		prepareCallable("SELECT * FROM `dyn_guards` WHERE `captainUID` = ?");
		setInt(1,captain.getObjectUUID());

		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {
				int mobBaseID = rs.getInt("mobBaseID");
				String name = rs.getString("name");
				Mob toCreate = captain.createGuardMob(mobBaseID, captain.getGuild(), captain.getParentZone(), captain.getBuilding().getLoc(), captain.getLevel(),name);
				if (toCreate == null)
					return;

				//   toCreate.despawn();
				if (toCreate != null) {
					
					toCreate.setTimeToSpawnSiege(System.currentTimeMillis() + MBServerStatics.FIFTEEN_MINUTES);
					toCreate.setDeathTime(System.currentTimeMillis());
					toCreate.setState(STATE.Respawn);

				}
			}


		} catch (SQLException e) {
			Logger.error( e.toString());
		} finally {
			closeCallable();
		}



	}

	public boolean ADD_TO_GUARDS(final long captainUID, final int mobBaseID, final String name, final int slot) {
		prepareCallable("INSERT INTO `dyn_guards` (`captainUID`, `mobBaseID`,`name`, `slot`) VALUES (?,?,?,?)");
		setLong(1, captainUID);
		setInt(2, mobBaseID);
		setString(3, name);
		setInt(4, slot);
		return (executeUpdate() > 0);
	}

	public boolean REMOVE_FROM_GUARDS(final long captainUID, final int mobBaseID, final int slot) {
		prepareCallable("DELETE FROM `dyn_guards` WHERE `captainUID`=? AND `mobBaseID`=? AND `slot` =?");
		setLong(1, captainUID);
		setInt(2, mobBaseID);
		setInt(3,slot);
		return (executeUpdate() > 0);
	}


	public ArrayList<Mob> GET_ALL_MOBS_FOR_ZONE(Zone zone) {
		prepareCallable("SELECT `obj_mob`.*, `object`.`parent` FROM `object` INNER JOIN `obj_mob` ON `obj_mob`.`UID` = `object`.`UID` WHERE `object`.`parent` = ?;");
		setLong(1, zone.getObjectUUID());
		return getLargeObjectList();
	}

	public ArrayList<Mob> GET_ALL_MOBS_FOR_BUILDING(int buildingID) {
		prepareCallable("SELECT * FROM `obj_mob` WHERE `mob_buildingID` = ?");
		setInt(1, buildingID);
		return getObjectList();
	}

	public ArrayList<Mob> GET_ALL_MOBS() {
		prepareCallable("SELECT `obj_mob`.*, `object`.`parent` FROM `object` INNER JOIN `obj_mob` ON `obj_mob`.`UID` = `object`.`UID`;");
		return getObjectList();
	}

	public Mob GET_MOB(final int objectUUID) {
		prepareCallable("SELECT `obj_mob`.*, `object`.`parent` FROM `object` INNER JOIN `obj_mob` ON `obj_mob`.`UID` = `object`.`UID` WHERE `object`.`UID` = ?;");
		setLong(1, objectUUID);
		return (Mob) getObjectSingle(objectUUID);
	}

	public int MOVE_MOB(long mobID, long parentID, float locX, float locY, float locZ) {
		prepareCallable("UPDATE `object` INNER JOIN `obj_mob` On `object`.`UID` = `obj_mob`.`UID` SET `object`.`parent`=?, `obj_mob`.`mob_spawnX`=?, `obj_mob`.`mob_spawnY`=?, `obj_mob`.`mob_spawnZ`=? WHERE `obj_mob`.`UID`=?;");
		setLong(1, parentID);
		setFloat(2, locX);
		setFloat(3, locY);
		setFloat(4, locZ);
		setLong(5, mobID);
		return executeUpdate();
	}

	public boolean UPDATE_MOB_BUILDING(int buildingID, int mobID) {
		prepareCallable("UPDATE `object` INNER JOIN `obj_mob` On `object`.`UID` = `obj_mob`.`UID` SET  `obj_mob`.`mob_buildingID`=? WHERE `obj_mob`.`UID`=?;");
		setInt(1, buildingID);
		setInt(2, mobID);
		return (executeUpdate() > 0);
	}

	public String SET_PROPERTY(final Mob m, String name, Object new_value) {
		prepareCallable("CALL mob_SETPROP(?,?,?)");
		setLong(1, m.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		return getResult();
	}

	public String SET_PROPERTY(final Mob m, String name, Object new_value, Object old_value) {
		prepareCallable("CALL mob_GETSETPROP(?,?,?,?)");
		setLong(1, m.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		setString(4, String.valueOf(old_value));
		return getResult();
	}


	public static boolean COPY_ZONE_MOBILES(PlayerCharacter pc, Zone sourceZone, Zone targetZone) {

		ArrayList<Mob> sourceMobList;
		Vector3fImmutable worldDelta;
		Mob newMobile;

		// Sanity check.  Can't copy a non existent zone

		if ((sourceZone == null) || (targetZone == null))
			return false;

		// Generate collections for all buildings in each zone


		for (Mob mobile : sourceZone.zoneMobSet) {

			// Calculate world coordinate offset between zones

			worldDelta = new Vector3fImmutable(targetZone.getAbsX(), targetZone.getAbsY(), targetZone.getAbsZ());
			worldDelta = worldDelta.subtract(new Vector3fImmutable(sourceZone.getAbsX(), sourceZone.getAbsY(), sourceZone.getAbsZ()));

			newMobile = Mob.createMob(mobile.getLoadID(),
					mobile.getLoc().add(worldDelta), null, true, targetZone, mobile.getBuilding(), 0);

			if (newMobile != null) {
				newMobile.updateDatabase();
			}

		}

		return true;
	}


	public void LOAD_RUNES_FOR_FIDELITY_MOBS() {





		prepareCallable("SELECT static_zone_npc.npcID,static_zone_npc.loadNum, static_zone_npc.classID, static_zone_npc.professionID, static_zone_npc.extraRune, static_zone_npc.extraRune2 FROM static_zone_npc ; ");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {


				int loadNum = rs.getInt("loadNum");
				int fidelityID = rs.getInt("npcID");
				int classID = rs.getInt("classID");
				int professionID = rs.getInt("professionID");
				int extraRune = rs.getInt("extraRune");
				int extraRune2 = rs.getInt("extraRune2");

				if (WorldServer.ZoneFidelityMobRunes.get(loadNum) == null)
					WorldServer.ZoneFidelityMobRunes.put(loadNum, new HashMap<>());
				ArrayList<Integer> runeList;
				if (WorldServer.ZoneFidelityMobRunes.get(loadNum).get(fidelityID) == null){
					runeList = new ArrayList<>(4);
				}else
					runeList = WorldServer.ZoneFidelityMobRunes.get(loadNum).get(fidelityID);



				if (classID != 0)
					runeList.add(classID);
				if (professionID != 0)
					runeList.add(professionID);
				if(extraRune != 0)
					runeList.add(extraRune);

				if (extraRune2 != 0)
					runeList.add(extraRune2);

				WorldServer.ZoneFidelityMobRunes.get(loadNum).put(fidelityID, runeList);


			}

			rs.close();



		} catch (SQLException e) {
			Logger.error( e.toString());
		} finally {
			closeCallable();

		}

	}


}
