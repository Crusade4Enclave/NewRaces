// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.DbManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class EquipmentSetEntry {

	private float dropChance;
	private int itemID;

	static HashMap<Integer, ArrayList<EquipmentSetEntry>> EquipmentSetMap = new HashMap<>();

	/**
	 * ResultSet Constructor
	 */

	public EquipmentSetEntry(ResultSet rs) throws SQLException {
		this.dropChance = (rs.getFloat("dropChance"));
		this.itemID = (rs.getInt("itemID"));
	}

	public static void LoadAllEquipmentSets() {
		EquipmentSetMap = DbManager.ItemBaseQueries.LOAD_EQUIPMENT_FOR_NPC_AND_MOBS();
	}

	float getDropChance() {
		return dropChance;
	}

	public int getItemID() {
		return itemID;
	}

}
