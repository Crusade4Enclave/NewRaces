// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum.ProfitType;
import engine.objects.*;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class dbNPCHandler extends dbHandlerBase {

	public dbNPCHandler() {
		this.localClass = NPC.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public NPC ADD_NPC(NPC toAdd, boolean isMob) {
		prepareCallable("CALL `npc_CREATE`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		setLong(1, toAdd.getParentZoneID());
		setString(2, toAdd.getName());
		setInt(3, toAdd.getContractID());
		setInt(4, toAdd.getGuildUUID());
		setFloat(5, toAdd.getSpawnX());
		setFloat(6, toAdd.getSpawnY());
		setFloat(7, toAdd.getSpawnZ());
		setInt(8, toAdd.getLevel());
		setFloat(9, toAdd.getBuyPercent());
		setFloat(10, toAdd.getSellPercent());
		if (toAdd.getBuilding() != null) {
			setInt(11, toAdd.getBuilding().getObjectUUID());
		} else {
			setInt(11, 0);
		}

		int objectUUID = (int) getUUID();
		if (objectUUID > 0) {
			return GET_NPC(objectUUID);
		}
		return null;
	}

	public int DELETE_NPC(final NPC npc) {
		if (npc.isStatic()) {
			return DELETE_STATIC_NPC(npc);
		}

		npc.removeFromZone();
		prepareCallable("DELETE FROM `object` WHERE `UID` = ?");
		setLong(1, (long) npc.getDBID());
		return executeUpdate();
	}

	private int DELETE_STATIC_NPC(final NPC npc) {
		npc.removeFromZone();
		prepareCallable("DELETE FROM `_init_npc` WHERE `ID` = ?");
		setInt(1, npc.getDBID());
		return executeUpdate();
	}

	public ArrayList<NPC> GET_ALL_NPCS_FOR_ZONE(Zone zone) {
		prepareCallable("SELECT `obj_npc`.*, `object`.`parent` FROM `object` INNER JOIN `obj_npc` ON `obj_npc`.`UID` = `object`.`UID` WHERE `object`.`parent` = ?;");
		setLong(1, (long) zone.getObjectUUID());
		return getLargeObjectList();
	}

	public ArrayList<NPC> GET_ALL_NPCS() {
		prepareCallable("SELECT `obj_npc`.*, `object`.`parent` FROM `object` INNER JOIN `obj_npc` ON `obj_npc`.`UID` = `object`.`UID`;");
		
		return getObjectList();
	}

	public ArrayList<NPC> GET_NPCS_BY_BUILDING(final int buildingID) {
		prepareCallable("SELECT `obj_npc`.*, `object`.`parent` FROM `obj_npc` INNER JOIN `object` ON `obj_npc`.`UID` = `object`.`UID` WHERE `npc_buildingID` = ? LIMIT 3");
		setInt(1, buildingID);
		return getObjectList();
	}

	public NPC GET_NPC(final int objectUUID) {
		prepareCallable("SELECT `obj_npc`.*, `object`.`parent` FROM `object` INNER JOIN `obj_npc` ON `obj_npc`.`UID` = `object`.`UID` WHERE `object`.`UID` = ?;");
		setLong(1, (long) objectUUID);
		return (NPC) getObjectSingle(objectUUID);
	}

	public int MOVE_NPC(long npcID, long parentID, float locX, float locY, float locZ) {
		prepareCallable("UPDATE `object` INNER JOIN `obj_npc` On `object`.`UID` = `obj_npc`.`UID` SET `object`.`parent`=?, `obj_npc`.`npc_spawnX`=?, `obj_npc`.`npc_spawnY`=?, `obj_npc`.`npc_spawnZ`=? WHERE `obj_npc`.`UID`=?;");
		setLong(1, parentID);
		setFloat(2, locX);
		setFloat(3, locY);
		setFloat(4, locZ);
		setLong(5, npcID);
		return executeUpdate();
	}


	public String SET_PROPERTY(final NPC n, String name, Object new_value) {
		prepareCallable("CALL npc_SETPROP(?,?,?)");
		setLong(1, (long) n.getDBID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		return getResult();
	}

	public String SET_PROPERTY(final NPC n, String name, Object new_value, Object old_value) {
		prepareCallable("CALL npc_GETSETPROP(?,?,?,?)");
		setLong(1, (long) n.getDBID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		setString(4, String.valueOf(old_value));
		return getResult();
	}

	public void updateDatabase(final NPC npc) {
		prepareCallable("UPDATE obj_npc SET npc_name=?, npc_contractID=?, npc_typeID=?, npc_guildID=?,"
				+ " npc_spawnX=?, npc_spawnY=?, npc_spawnZ=?, npc_level=? ,"
				+ " npc_buyPercent=?, npc_sellPercent=?, npc_buildingID=? WHERE UID = ?");
		setString(1, npc.getName());
		setInt(2, (npc.getContract() != null) ? npc.getContract().getObjectUUID() : 0);
		setInt(3, 0);
		setInt(4, (npc.getGuild() != null) ? npc.getGuild().getObjectUUID() : 0);
		setFloat(5, npc.getBindLoc().x);
		setFloat(6, npc.getBindLoc().y);
		setFloat(7, npc.getBindLoc().z);
		setShort(8, npc.getLevel());
		setFloat(9, npc.getBuyPercent());
		setFloat(10, npc.getSellPercent());
		setInt(11, (npc.getBuilding() != null) ? npc.getBuilding().getObjectUUID() : 0);
		setInt(12, npc.getDBID());
		executeUpdate();
	}

	public boolean updateUpgradeTime(NPC npc, DateTime upgradeDateTime) {



		try {

			prepareCallable("UPDATE obj_npc SET upgradeDate=? "
					+ "WHERE UID = ?");

			if (upgradeDateTime == null)
				setNULL(1, java.sql.Types.DATE);
			else
				setTimeStamp(1, upgradeDateTime.getMillis());

			setInt(2, npc.getObjectUUID());
			executeUpdate();
		} catch (Exception e) {
			Logger.error("UUID: " + npc.getObjectUUID());
			return false;
		}
		return true;
	}

	public boolean UPDATE_BUY_PROFIT(NPC npc,float percent) {
		prepareCallable("UPDATE `obj_npc` SET `npc_buyPercent`=? WHERE `UID`=?");
		setFloat(1, percent);
		setLong(2, npc.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_SELL_PROFIT(NPC npc,float percent) {
		prepareCallable("UPDATE `obj_npc` SET `npc_sellPercent`=? WHERE `UID`=?");
		setFloat(1, percent);
		setLong(2, npc.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_SLOT(NPC npc,int slot) {
		prepareCallable("UPDATE `obj_npc` SET `npc_slot`=? WHERE `UID`=?");
		setFloat(1, slot);
		setLong(2, npc.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_MOBBASE(NPC npc, int mobBaseID) {
		prepareCallable("UPDATE `obj_npc` SET `npc_raceID`=? WHERE `UID`=?");
		setLong(1, mobBaseID);
		setLong(2, npc.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_EQUIPSET(NPC npc, int equipSetID) {
		prepareCallable("UPDATE `obj_npc` SET `equipsetID`=? WHERE `UID`=?");
		setInt(1, equipSetID);
		setLong(2, npc.getObjectUUID());
		return (executeUpdate() > 0);
	}
	
	public boolean UPDATE_NAME(NPC npc,String name) {
		prepareCallable("UPDATE `obj_npc` SET `npc_name`=? WHERE `UID`=?");
		setString(1, name);
		setLong(2, npc.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public void LOAD_PIRATE_NAMES() {

		String pirateName;
		int mobBase;
		int recordsRead = 0;

		prepareCallable("SELECT * FROM static_piratenames");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;
				mobBase = rs.getInt("mobbase");
				pirateName = rs.getString("first_name");

				// Handle new mobbbase entries

				if (NPC._pirateNames.get(mobBase) == null) {
					NPC._pirateNames.putIfAbsent(mobBase, new ArrayList<>());
					}

				// Insert name into proper arraylist

				NPC._pirateNames.get(mobBase).add(pirateName);

			}

			Logger.info("names read: " + recordsRead + " for "
			             + NPC._pirateNames.size() + " mobBases");

		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
	}
	
	public void LOAD_RUNES_FOR_FIDELITY_NPC(NPC npc) {





		prepareCallable("SELECT static_zone_npc.npcID,static_zone_npc.loadNum, static_zone_npc.classID, static_zone_npc.professionID, static_zone_npc.extraRune, static_zone_npc.extraRune2 FROM static_zone_npc WHERE static_zone_npc.loadNum = ? AND static_zone_npc.npcID = ?");
		setInt(1,npc.getParentZoneID());
		setInt(2, npc.getFidalityID());
		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {


				
				int classID = rs.getInt("classID");
				int professionID = rs.getInt("professionID");
				int extraRune = rs.getInt("extraRune");
				int extraRune2 = rs.getInt("extraRune2");
				
				npc.classID = classID;
				npc.professionID = professionID;
				npc.extraRune = extraRune;
				npc.extraRune2 = extraRune2;

			

			}

			rs.close();



		} catch (SQLException e) {
			Logger.error( e.toString());
		} finally {
			closeCallable();

		}
	}

	public boolean ADD_TO_PRODUCTION_LIST(final long ID,final long npcUID, final long itemBaseID, DateTime dateTime, String prefix, String suffix, String name, boolean isRandom, int playerID) {
		prepareCallable("INSERT INTO `dyn_npc_production` (`ID`,`npcUID`, `itemBaseID`,`dateToUpgrade`, `isRandom`, `prefix`, `suffix`, `name`,`playerID`) VALUES (?,?,?,?,?,?,?,?,?)");
		setLong(1,ID);
		setLong(2, npcUID);
		setLong(3, itemBaseID);
		setTimeStamp(4, dateTime.getMillis());
		setBoolean(5, isRandom);
		setString(6, prefix);
		setString(7, suffix);
		setString(8, name);
		setInt(9,playerID);
		return (executeUpdate() > 0);
	}

	public boolean REMOVE_FROM_PRODUCTION_LIST(final long ID,final long npcUID) {
		prepareCallable("DELETE FROM `dyn_npc_production` WHERE `ID`=? AND `npcUID`=?;");
		setLong(1,ID);
		setLong(2, npcUID);
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_ITEM_TO_INVENTORY(final long ID,final long npcUID) {
		prepareCallable("UPDATE `dyn_npc_production` SET `inForge`=? WHERE `ID`=? AND `npcUID`=?;");
		setByte(1, (byte)0);
		setLong(2, ID);
		setLong(3, npcUID);
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_ITEM_PRICE(final long ID,final long npcUID, int value) {
		prepareCallable("UPDATE `dyn_npc_production` SET `value`=? WHERE `ID`=? AND `npcUID`=?;");
		setInt(1, value);
		setLong(2, ID);
		setLong(3, npcUID);

		return (executeUpdate() > 0);
	}

	public boolean UPDATE_ITEM_ID(final long ID,final long npcUID,final long value) {
		prepareCallable("UPDATE `dyn_npc_production` SET `ID`=? WHERE `ID`=? AND `npcUID`=? LIMIT 1;");
		setLong(1, value);
		setLong(2, ID);
		setLong(3, npcUID);

		return (executeUpdate() > 0);
	}

	public void LOAD_ALL_ITEMS_TO_PRODUCE(NPC npc) {

		if (npc == null)
			return;

		prepareCallable("SELECT * FROM `dyn_npc_production` WHERE `npcUID` = ?");
		setInt(1,npc.getObjectUUID());

		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {
				ProducedItem producedItem = new ProducedItem(rs);
				npc.forgedItems.add(producedItem);
			}

		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
	}
	
	public boolean UPDATE_PROFITS(NPC npc,ProfitType profitType, float value){
		prepareCallable("UPDATE `dyn_npc_profits` SET `" + profitType.dbField + "` = ? WHERE `npcUID`=?");
		setFloat(1, value);
		setInt(2, npc.getObjectUUID());
		return (executeUpdate() > 0);
	}
	
	public void LOAD_NPC_PROFITS() {

		HashMap<Integer, ArrayList<BuildingRegions>> regions;
		NPCProfits npcProfit;


		prepareCallable("SELECT * FROM dyn_npc_profits");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				
				npcProfit = new NPCProfits(rs);
				NPCProfits.ProfitCache.put(npcProfit.npcUID, npcProfit);
			}

		} catch (SQLException e) {
			Logger.error(": " + e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
	}
	
	public boolean CREATE_PROFITS(NPC npc){
			prepareCallable("INSERT INTO `dyn_npc_profits` (`npcUID`) VALUES (?)");
			setLong(1,npc.getObjectUUID());
			return (executeUpdate() > 0);
	}
}
