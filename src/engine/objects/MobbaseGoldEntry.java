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
import java.util.HashMap;

public class MobbaseGoldEntry {

	private float chance;
	private int min;
	private int max;

	public static HashMap<Integer, MobbaseGoldEntry> MobbaseGoldMap = new HashMap<>();

	/**
	 * ResultSet Constructor
	 */

	public MobbaseGoldEntry(ResultSet rs) throws SQLException {

		this.chance = rs.getFloat("chance");
		this.min = rs.getInt("min");
		this.max = (rs.getInt("max"));
	}

	public static void LoadMobbaseGold() {
		MobbaseGoldMap = DbManager.MobBaseQueries.LOAD_GOLD_FOR_MOBBASE();
	}

	public float getChance() {
		return chance;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}





}
