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



public class MobBaseStats  {

	private final int baseStr;
	private final int baseInt;
	private final int baseCon;
	private final int baseSpi;
	private final int baseDex;
	private final long skillSet;
	private final int skillValue;
	public static MobBaseStats mbs = null;


	/**
	 * ResultSet Constructor
	 */
	public MobBaseStats(ResultSet rs) throws SQLException {
		this.baseStr = rs.getInt("Strength");
		this.baseInt = rs.getInt("Intelligence");
		this.baseCon = rs.getInt("Constitution");
		this.baseSpi = rs.getInt("Spirit");
		this.baseDex = rs.getInt("Dexterity");
		this.skillSet = rs.getLong("baseSkills");
		this.skillValue = rs.getInt("skillAmount");
	}

	/**
	 * Generic Constructor
	 */

	public MobBaseStats()  {
		this.baseStr = 0;
		this.baseInt = 0;
		this.baseCon = 0;
		this.baseSpi = 0;
		this.baseDex = 0;
		this.skillSet = 0;
		this.skillValue = 0;
	}
	public int getBaseStr() {
		return baseStr;
	}


	public int getBaseInt() {
		return baseInt;
	}


	public int getBaseCon() {
		return baseCon;
	}


	public int getBaseSpi() {
		return baseSpi;
	}


	public int getBaseDex() {
		return baseDex;
	}

	public long getSkillSet() {
		return skillSet;
	}

	public int getSkillValue() {
		return skillValue;
	}
	
	public static MobBaseStats GetGenericStats(){
		if (mbs != null)
			return mbs;
        mbs = new MobBaseStats();
		return mbs;
	}


}
