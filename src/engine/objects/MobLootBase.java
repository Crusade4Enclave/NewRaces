// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class MobLootBase  {

	private int mobBaseID;
	private int lootTableID;
	private float chance;

	public static HashMap<Integer, ArrayList<MobLootBase>> MobLootSet = new HashMap<>();


	/**
	 * ResultSet Constructor
	 */

	public MobLootBase(ResultSet rs) throws SQLException {
		this.mobBaseID = rs.getInt("mobBaseID");
        this.lootTableID = rs.getInt("lootTable");
        this.chance = rs.getFloat("chance");
	}

	public MobLootBase(int mobBaseID, int lootTableID, int chance) {
		super();
		this.mobBaseID = mobBaseID;
        this.lootTableID = lootTableID;
        this.chance = chance;

	}

	public int getMobBaseID() {
		return mobBaseID;
	}
	public float getChance() {
		return chance;
	}

	public int getLootTableID() {
		return lootTableID;
	}

	public void setLootTableID(int lootTableID) {
		this.lootTableID = lootTableID;
	}

}
