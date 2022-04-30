// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum.GameObjectType;
import engine.gameManager.DbManager;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class dbMobBaseHandler extends dbHandlerBase {

	public dbMobBaseHandler() {
		this.localClass = MobBase.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

    public MobBase GET_MOBBASE(int id, boolean forceDB) {


		if (id == 0)
			return null;

		MobBase mobBase = (MobBase) DbManager.getFromCache(GameObjectType.MobBase, id);

		if ( mobBase != null)
			return mobBase;

		prepareCallable("SELECT * FROM `static_npc_mobbase` WHERE `ID`=?");
		setInt(1, id);
		return (MobBase) getObjectSingle(id, forceDB, true);
	}

	public ArrayList<MobBase> GET_ALL_MOBBASES() {
		prepareCallable("SELECT * FROM `static_npc_mobbase`;");
		return  getObjectList();
	}

	public void SET_AI_DEFAULTS() {
		prepareCallable("SELECT * FROM `static_ai_defaults`");
		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {
				MBServerStatics.AI_BASE_AGGRO_RANGE = rs.getInt("aggro_range");
				MBServerStatics.AI_PATROL_DIVISOR = rs.getInt("patrol_chance");
				MBServerStatics.AI_DROP_AGGRO_RANGE = rs.getInt("drop_aggro_range");
				MBServerStatics.AI_POWER_DIVISOR = rs.getInt("cast_chance");
				MBServerStatics.AI_RECALL_RANGE = rs.getInt("recall_range");
				MBServerStatics.AI_PET_HEEL_DISTANCE = rs.getInt("pet_heel_distance");
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error( e.getMessage());
		} finally {
			closeCallable();
		}

	}

	public boolean UPDATE_AI_DEFAULTS() {
		prepareCallable("UPDATE `static_ai_defaults` SET `aggro_range` = ?,`patrol_chance`= ?,`drop_aggro_range`= ?,`cast_chance`= ?,`recall_range`= ? WHERE `ID` = 1");
		setInt(1, MBServerStatics.AI_BASE_AGGRO_RANGE);
		setInt(2, MBServerStatics.AI_PATROL_DIVISOR);
		setInt(3, MBServerStatics.AI_DROP_AGGRO_RANGE);
		setInt(4, MBServerStatics.AI_POWER_DIVISOR);
		setInt(5, MBServerStatics.AI_RECALL_RANGE);
		return (executeUpdate() > 0);

	}

	public boolean UPDATE_FLAGS(int mobBaseID, long flags) {
		prepareCallable("UPDATE `static_npc_mobbase` SET `flags` = ? WHERE `ID` = ?");
		setLong(1, flags);
		setInt(2, mobBaseID);
		return (executeUpdate() > 0);

	}

	public HashMap<Integer, Integer> LOAD_STATIC_POWERS(int mobBaseUUID) {
		HashMap<Integer, Integer> powersList = new HashMap<>();
		prepareCallable("SELECT * FROM `static_npc_mobbase_powers` WHERE `mobbaseUUID`=?");
		setInt(1, mobBaseUUID);
		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {

				powersList.put(rs.getInt("token"), rs.getInt("rank"));
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error( e.getMessage());
		} finally {
			closeCallable();
		}
		return powersList;

	}

	public ArrayList<MobBaseEffects> LOAD_STATIC_EFFECTS(int mobBaseUUID) {
		ArrayList<MobBaseEffects> effectsList = new ArrayList<>();

		prepareCallable("SELECT * FROM `static_npc_mobbase_effects` WHERE `mobbaseUUID` = ?");
		setInt(1, mobBaseUUID);

		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {
				MobBaseEffects mbs = new MobBaseEffects(rs);
				effectsList.add(mbs);
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error( e.getMessage());
		} finally {
			closeCallable();
		}
		return effectsList;

	}

	public ArrayList<MobBaseEffects> GET_RUNEBASE_EFFECTS(int runeID) {
		ArrayList<MobBaseEffects> effectsList = new ArrayList<>();
		prepareCallable("SELECT * FROM `static_npc_mobbase_effects` WHERE `mobbaseUUID` = ?");
		setInt(1, runeID);

		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {

				MobBaseEffects mbs = new MobBaseEffects(rs);
				effectsList.add(mbs);
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error (e.getMessage());
		} finally {
			closeCallable();
		}

		return effectsList;

	}

	public MobBaseStats LOAD_STATS(int mobBaseUUID) {
		MobBaseStats mbs = MobBaseStats.GetGenericStats();

		prepareCallable("SELECT * FROM `static_npc_mobbase_stats` WHERE `mobbaseUUID` = ?");
		setInt(1, mobBaseUUID);
		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {

				mbs = new MobBaseStats(rs);
			}

		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		return mbs;

	}

	public ArrayList<RuneBase> LOAD_RUNES_FOR_MOBBASE(int mobBaseUUID) {

		ArrayList<RuneBase> runes = new ArrayList<>();
		prepareCallable("SELECT * FROM `static_npc_mobbase_runes` WHERE `mobbaseUUID` = ?");
		setInt(1, mobBaseUUID);
		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {
				int runeID = rs.getInt("runeID");
				RuneBase rune = RuneBase.getRuneBase(runeID);
				runes.add(rune);
			}

		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		return runes;

	}

	public boolean ADD_MOBBASE_EFFECT(int mobBaseUUID, int token, int rank, int reqLvl) {
		prepareCallable("INSERT INTO `static_npc_mobbase_effects` (`mobbaseUUID`, `token`, `rank`, `reqLvl`) VALUES (?, ?, ?, ?);");
		setInt(1, mobBaseUUID);
		setInt(2, token);
		setInt(3, rank);
		setInt(4, reqLvl);
		return (executeUpdate() > 0);
	}

	public boolean ADD_MOBBASE_POWER(int mobBaseUUID, int token, int rank) {
		prepareCallable("INSERT INTO `static_npc_mobbase_powers` (`mobbaseUUID`, `token`, `rank`) VALUES (?, ?, ?);");
		setInt(1, mobBaseUUID);
		setInt(2, token);
		setInt(3, rank);
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_SKILLS(int ID, int skillsID) {
		prepareCallable("UPDATE `static_npc_mobbase` SET `baseSkills`=? WHERE `ID`=?;");
		setInt(1, skillsID);
		setInt(2, ID);
		return (executeUpdate() > 0);
	}

	public boolean ADD_MOBBASE_RUNE(int mobBaseUUID, int runeID) {
		prepareCallable("INSERT INTO `static_npc_mobbase_runes` (`mobbaseUUID`, `runeID`) VALUES (?, ?);");
		setInt(1, mobBaseUUID);
		setInt(2, runeID);
		return (executeUpdate() > 0);
	}

	public MobBase COPY_MOBBASE(MobBase toAdd, String name) {
		prepareCallable("INSERT INTO `static_npc_mobbase` (`loadID`, `lootTableID`, `name`, `level`, `health`, `atr`, `defense`, `minDmg`,`maxDmg`, `goldMod`, `seeInvis`, `flags`, `noaggro`, `spawntime`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		setInt(1, toAdd.getLoadID());
		setInt(2, toAdd.getLootTable());
		setString(3, (name.length() > 0) ? name : toAdd.getFirstName());
		setInt(4, toAdd.getLevel());
		setFloat(5, toAdd.getHealthMax());
		setInt(5, toAdd.getAtr());
		setInt(6, toAdd.getDefense());
		setFloat(7, toAdd.getMinDmg());
		setFloat(8, toAdd.getMaxDmg());
		setInt(9, toAdd.getGoldMod());
		setInt(10, toAdd.getSeeInvis());
		setLong(11, toAdd.getFlags().toLong());
		setLong(12, toAdd.getNoAggro().toLong());
		setInt(13, toAdd.getSpawnTime());
		int objectUUID = insertGetUUID();
		if (objectUUID > 0)
			return GET_MOBBASE(objectUUID, true);
		return null;
	}

	public boolean RENAME_MOBBASE(int ID, String newName) {
		prepareCallable("UPDATE `static_npc_mobbase` SET `name`=? WHERE `ID`=?;");
		setString(1, newName);
		setInt(2, ID);
		return (executeUpdate() > 0);
	}


	public void LOAD_ALL_MOBBASE_LOOT(int mobBaseID) {

		if (mobBaseID == 0)
			return;
		ArrayList<MobLootBase> mobLootList = new ArrayList<>();
		prepareCallable("SELECT * FROM `static_mob_loottable` WHERE `mobBaseID` = ?");
		setInt(1,mobBaseID);

		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {

				MobLootBase mobLootBase = new MobLootBase(rs);
				mobLootList.add(mobLootBase);

			}

			MobLootBase.MobLootSet.put(mobBaseID, mobLootList);

		} catch (SQLException e) {
			Logger.error( e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}

	}

	public void LOAD_ALL_MOBBASE_SPEEDS(MobBase mobBase) {

		if (mobBase.getLoadID() == 0)
			return;
		ArrayList<MobLootBase> mobLootList = new ArrayList<>();
		prepareCallable("SELECT * FROM `static_npc_mobbase_race` WHERE `mobbaseID` = ?");
		setInt(1,mobBase.getLoadID());

		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {
				float walk = rs.getFloat("walkStandard");
				float walkCombat = rs.getFloat("walkCombat");
				float run = rs.getFloat("runStandard");
				float runCombat = rs.getFloat("runCombat");
				mobBase.updateSpeeds(walk, walkCombat, run, runCombat);
			}


		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}

	}

	public HashMap<Integer, MobbaseGoldEntry> LOAD_GOLD_FOR_MOBBASE() {

		HashMap<Integer, MobbaseGoldEntry> goldSets;
		MobbaseGoldEntry goldSetEntry;
		int	mobbaseID;

		goldSets = new HashMap<>();
		int recordsRead = 0;

		prepareCallable("SELECT * FROM static_npc_mobbase_gold");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;

				mobbaseID = rs.getInt("mobbaseID");
				goldSetEntry = new MobbaseGoldEntry(rs);
				goldSets.put(mobbaseID, goldSetEntry);

			}

			Logger.info("read: " + recordsRead + " cached: " + goldSets.size());

		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		return goldSets;
	}
}
