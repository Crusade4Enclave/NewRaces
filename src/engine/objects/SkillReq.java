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


public class SkillReq extends AbstractGameObject {


	private int skillID;
	private short level;
	private ArrayList<Byte> skillReqs;

	/* This shouldn't be used
	public SkillReq(SkillsBase skillsBase, short level, ArrayList<Byte>skillReqs) {

		super();
		this.skillsBase = skillsBase;
		this.level = level;
		this.skillReqs = skillReqs;
	}
	*/

	/* This shouldn't be used
	public SkillReq(SkillsBase skillsBase, short level, ArrayList<Byte>skillReqs, int newUUID) {

		super(newUUID);
		this.skillsBase = skillsBase;
		this.level = level;
		this.skillReqs = skillReqs;
	}
	*/

	/* This shouldn't be used
	public SkillReq(SkillReq a, int newUUID) {
		super(a, newUUID);
		this.skillsBase = a.skillsBase;
		this.level = a.level;
		this.skillReqs = a.skillReqs;
	}
	*/

	/**
	 * ResultSet Constructor
	 */
	public SkillReq(ResultSet rs) throws SQLException {
		super(rs, 0);
		this.skillID = rs.getInt("skillID");
		this.level = rs.getShort("level");
		skillReqs = new ArrayList<>(0);

		int skillReq;
		skillReq = rs.getInt("skillreq1");
		if (skillReq > 0) skillReqs.add((byte)skillReq);
		skillReq = rs.getInt("skillreq2");
		if (skillReq > 0) skillReqs.add((byte)skillReq);
		skillReq = rs.getInt("skillreq3");
		if (skillReq > 0) skillReqs.add((byte)skillReq);
	}

	/*
	 * Getters
	 */
	public SkillsBase getSkillsBase() {
		return DbManager.SkillsBaseQueries.GET_BASE(this.skillID);
	}

	public int getSkillID() {
		return this.skillID;
	}

	public short getLevel() {
		return this.level;
	}

	public ArrayList<Byte> getSkillReqs() {
		return this.skillReqs;
	}


	@Override
	public void updateDatabase() {

	}
}