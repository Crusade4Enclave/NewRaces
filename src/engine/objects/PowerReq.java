// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.PowersManager;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;


public class PowerReq extends AbstractGameObject implements Comparable<PowerReq> {

	private PowersBase powersBase;
	private int token;
	private short level;
	private ConcurrentHashMap<Integer, Byte> powerReqs;
	private ConcurrentHashMap<Integer, Byte> skillReqs;

	private static ConcurrentHashMap<Integer, ArrayList<PowerReq>> runePowers = fillRunePowers();
	private static ArrayList<PowerReq> powersForAll = new ArrayList<>();

	/**
	 * No Table ID Constructor
	 */
	public PowerReq(PowersBase powersBase, short level, ConcurrentHashMap<Integer, Byte> powerReqs, ConcurrentHashMap<Integer, Byte> skillReqs) {
		super();
		this.powersBase = powersBase;
		this.level = level;
		this.powerReqs = powerReqs;
		this.skillReqs = skillReqs;
		if (this.powersBase != null)
			this.token = this.powersBase.getToken();
		else
			this.token = 0;
	}

	/**
	 * Normal Constructor
	 */
	public PowerReq(PowersBase powersBase, short level, ConcurrentHashMap<Integer, Byte> powerReqs, ConcurrentHashMap<Integer, Byte> skillReqs, int newUUID) {
		super(newUUID);
		this.powersBase = powersBase;
		this.level = level;
		this.powerReqs = powerReqs;
		this.skillReqs = skillReqs;
		if (this.powersBase != null)
			this.token = this.powersBase.getToken();
		else
			this.token = 0;
	}

	/**
	 * ResultSet Constructor
	 */
	public PowerReq(ResultSet rs) throws SQLException {
		super(rs);
		this.token = rs.getInt("powerToken");
		this.powersBase = PowersManager.getPowerByToken(this.token);
		this.level = rs.getShort("level");
		int type = rs.getInt("type");
		this.powerReqs = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		this.skillReqs = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		if (type == 1)
			this.skillReqs.put(rs.getInt("requiredToken"), rs.getByte("requiredAmount"));
		else if (type == 2)
			this.powerReqs.put(rs.getInt("requiredToken"), rs.getByte("requiredAmount"));
	}

	/*
	 * Getters
	 */
	public PowersBase getPowersBase() {
		if (this.powersBase == null) {
			this.powersBase = PowersManager.getPowerByToken(this.token);
		}
		return this.powersBase;
	}

	public short getLevel() {
		return this.level;
	}

	public ConcurrentHashMap<Integer, Byte> getPowerReqs() {
		return this.powerReqs;
	}

	public ConcurrentHashMap<Integer, Byte> getSkillReqs() {
		return this.skillReqs;
	}

	public int getToken() {
		return this.token;
	}

	private void addPower(int token, byte amount) {
		this.powerReqs.put(token, amount);
	}

	private void addSkill(int token, byte amount) {
		this.skillReqs.put(token, amount);
	}


	@Override
	public int compareTo(PowerReq n) throws ClassCastException {
		if (n.level == this.level)
			return 0;
		else if (this.level > n.level)
			return 1;
		else
			return -1;
	}


	/*
	 * Database
	 */

	public static ArrayList<PowerReq> getPowerReqsForRune(int id) {
//		if (PowerReq.runePowers == null)
//			fillRunePowers();
		if (PowerReq.runePowers.containsKey(id))
			return PowerReq.runePowers.get(id);
		return new ArrayList<>();
	}

	public static ConcurrentHashMap<Integer, ArrayList<PowerReq>> fillRunePowers() {
		PowerReq.runePowers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		PreparedStatementShared ps = null;
		try {
			ps = prepareStatement("SELECT * FROM static_power_powerrequirement");
			ResultSet rs = ps.executeQuery();
			if (PowerReq.runePowers.size() > 0) {
				rs.close();
				return PowerReq.runePowers;
			}
			while (rs.next()) {
				ArrayList<PowerReq> runePR = null;
				int runeID = rs.getInt("runeID");
				int token = rs.getInt("powerToken");
				if (PowerReq.runePowers.containsKey(runeID))
					runePR = PowerReq.runePowers.get(runeID);
				else {
					runePR = new ArrayList<>();
					PowerReq.runePowers.put(runeID, runePR);
				}
				boolean found = false;
				for (PowerReq pr : runePR) {
                    if (pr.token == token) {
						int type = rs.getInt("type");
						if (type == 1)
							pr.addSkill(rs.getInt("requiredToken"), rs.getByte("requiredAmount"));
						else
							pr.addPower(rs.getInt("requiredToken"), rs.getByte("requiredAmount"));
						found = true;
					}
				}
				if (!found) {
					PowerReq pr = new PowerReq(rs);
					runePR.add(pr);
				}
			}
			rs.close();

			//order the lists by level so prerequisites are met
			for (ArrayList<PowerReq> runePR : PowerReq.runePowers.values()) {
				Collections.sort(runePR);
			}
		} catch (SQLException e) {
			Logger.error( "SQL Error number: " + e.getErrorCode(), e);
		} finally {
			ps.release();
		}
		return PowerReq.runePowers;
	}
	
	@Override
	public void updateDatabase() {

	}
}