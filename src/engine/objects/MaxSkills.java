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

public class MaxSkills  {

	private int runeID;
	private int skillToken;
	private int skillLevel;
	private int maxSkillPercent;



	public static HashMap<Integer, ArrayList<MaxSkills>> MaxSkillsSet = new HashMap<>();


	/**
	 * ResultSet Constructor
	 */

	public MaxSkills(ResultSet rs) throws SQLException {
		this.runeID = rs.getInt("runeID");
		this.skillToken =rs.getInt("skillToken");
		this.skillLevel = rs.getInt("skillLevel");
        this.maxSkillPercent = rs.getInt("maxSkillPercent");
    }

	public MaxSkills(int runeID, int skillToken, int skillLevel, int maxSkillPercent) {
		super();
		this.runeID = runeID;
		this.skillToken = skillToken;
		this.skillLevel = skillLevel;
        this.maxSkillPercent = maxSkillPercent;
    }

	public int getRuneID() {
		return runeID;
	}

	public void setRuneID(int runeID) {
		this.runeID = runeID;
	}

	public int getSkillLevel() {
		return skillLevel;
	}

	public void setSkillLevel(int skillLevel) {
		this.skillLevel = skillLevel;
	}

	public int getSkillToken() {
		return skillToken;
	}

	public void setSkillToken(int skillToken) {
		this.skillToken = skillToken;
	}

	public int getMaxSkillPercent() {
		return maxSkillPercent;
	}

	public void setMaxSkillPercent(int maxSkillPercent) {
		this.maxSkillPercent = maxSkillPercent;
	}
}
