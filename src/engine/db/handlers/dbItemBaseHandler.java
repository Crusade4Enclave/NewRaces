// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.EquipmentSetEntry;
import engine.objects.ItemBase;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class dbItemBaseHandler extends dbHandlerBase {

	public dbItemBaseHandler() {

	}

	public void LOAD_BAKEDINSTATS(ItemBase itemBase) {

		try {
			prepareCallable("SELECT * FROM `static_item_bakedinstat` WHERE `itemID` = ?");
			setInt(1, itemBase.getUUID());

			ResultSet rs = executeQuery();

			while (rs.next()) {

				if (rs.getBoolean("fromUse"))
					itemBase.getUsedStats().put(rs.getInt("token"), rs.getInt("numTrains"));
				else
					itemBase.getBakedInStats().put(rs.getInt("token"), rs.getInt("numTrains"));
			}
		} catch (SQLException e) {
			Logger.error( e.toString());
		} finally {
			closeCallable();
		}
	}

	public void LOAD_ANIMATIONS(ItemBase itemBase) {

		ArrayList<Integer> tempList = new ArrayList<>();
		ArrayList<Integer> tempListOff = new ArrayList<>();
		try {
			prepareCallable("SELECT * FROM `static_itembase_animations` WHERE `itemBaseUUID` = ?");
			setInt(1, itemBase.getUUID());

			ResultSet rs = executeQuery();

			while (rs.next()) {
				int animation = rs.getInt("animation");

				boolean rightHand = rs.getBoolean("rightHand");

				if (rightHand)
					tempList.add(animation);
				else
					tempListOff.add(animation);

			}
		} catch (SQLException e) {
			Logger.error( e.toString());
		} finally {
			closeCallable();
		}

		itemBase.setAnimations(tempList);
		itemBase.setOffHandAnimations(tempListOff);
	}

	public void LOAD_ALL_ITEMBASES() {

		ItemBase itemBase;

		int recordsRead = 0;

		prepareCallable("SELECT * FROM static_itembase");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;
				itemBase = new ItemBase(rs);

				// Add ItemBase to internal cache

				ItemBase.addToCache(itemBase);
			}

			Logger.info( "read: " + recordsRead + "cached: " + ItemBase.getUUIDCache().size());

		} catch (SQLException e) {
			Logger.error( e.toString());
		} finally {
			closeCallable();
		}
	}

	public HashMap<Integer, ArrayList<EquipmentSetEntry>> LOAD_EQUIPMENT_FOR_NPC_AND_MOBS() {

		HashMap<Integer, ArrayList<EquipmentSetEntry>> equipmentSets;
		EquipmentSetEntry equipmentSetEntry;
		int	equipSetID;

		equipmentSets = new HashMap<>();
		int recordsRead = 0;

		prepareCallable("SELECT * FROM static_npc_equipmentset");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;

				equipSetID = rs.getInt("equipmentSet");
				equipmentSetEntry = new EquipmentSetEntry(rs);

				if (equipmentSets.get(equipSetID) == null){
					ArrayList<EquipmentSetEntry> equipList = new ArrayList<>();
					equipList.add(equipmentSetEntry);
					equipmentSets.put(equipSetID, equipList);
				}
				else{
					ArrayList<EquipmentSetEntry>equipList = equipmentSets.get(equipSetID);
					equipList.add(equipmentSetEntry);
					equipmentSets.put(equipSetID, equipList);
				}
			}

			Logger.info("read: " + recordsRead + " cached: " + equipmentSets.size());

		} catch (SQLException e) {
			Logger.error( e.toString());
		} finally {
			closeCallable();
		}
		return equipmentSets;
	}
}
