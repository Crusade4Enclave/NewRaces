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

public class SpecialLoot extends AbstractGameObject {

	private int itemID;
	private int dropChance;
	private boolean dropOnDeath;
	private boolean noSteal;
	private int lootSetID;

	public static HashMap<Integer,ArrayList<SpecialLoot>> LootMap = new HashMap<>();
	/**
	 * ResultSet Constructor
	 */
	public SpecialLoot(ResultSet rs) throws SQLException {
		super(rs);
		this.itemID = rs.getInt("itemID");
		this.dropChance = rs.getInt("dropChance");
		this.dropOnDeath = rs.getBoolean("dropOnDeath");
		this.noSteal = rs.getBoolean("noSteal");
	}

	public SpecialLoot(ResultSet rs,boolean specialLoot) throws SQLException {
		super(rs);

		this.lootSetID = rs.getInt("lootSet");
		this.itemID = rs.getInt("itemID");
		this.dropChance = rs.getInt("dropChance");
		this.dropOnDeath = false;
		this.noSteal = true;
	}

	/*
	 * Getters
	 */

	public int getItemID() {
		return this.itemID;
	}

	public int getDropChance() {
		return this.dropChance;
	}

	public boolean dropOnDeath() {
		return this.dropOnDeath;
	}

	public boolean noSteal() {
		return this.noSteal;
	}

	public static ArrayList<SpecialLoot> getSpecialLoot(int mobbaseID) {
		return DbManager.SpecialLootQueries.GET_SPECIALLOOT(mobbaseID);
	}

	@Override
	public void updateDatabase() {

	}
}