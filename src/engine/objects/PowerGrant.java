// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;




public class PowerGrant extends AbstractGameObject {

	private int token;
	private ConcurrentHashMap<Integer, Short> runeGrants = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private static ConcurrentHashMap<Integer, PowerGrant> grantedPowers = null;

	/**
	 * ResultSet Constructor
	 */
	public PowerGrant(ResultSet rs) throws SQLException {
		super();
		this.token = rs.getInt("powerToken");
		runeGrants.put(rs.getInt("runeID"), rs.getShort("grantAmount"));
	}

	/*
	 * Getters
	 */

	public ConcurrentHashMap<Integer, Short> getRuneGrants() {
		return this.runeGrants;
	}

	public int getToken() {
		return this.token;
	}

	private void addRuneGrant(int runeID, short amount) {
		this.runeGrants.put(runeID, amount);
	}


	/*
	 * Database
	 */

	public static Short getGrantedTrains(int token, PlayerCharacter pc) {
		if (pc == null)
			return (short) 0;

		if (PowerGrant.grantedPowers == null)
			fillGrantedPowers();

		if (PowerGrant.grantedPowers.containsKey(token)) {
			PowerGrant pg = PowerGrant.grantedPowers.get(token);
            ConcurrentHashMap<Integer, Short> runeGrants = pg.runeGrants;
			ArrayList<Integer> toks = new ArrayList<>();

			//get race ID
			Race race = pc.getRace();
			if (race != null)
				toks.add(race.getRaceRuneID());

			//get baseClass ID
			BaseClass bc = pc.getBaseClass();
			if (bc != null)
				toks.add(bc.getObjectUUID());

			//get promoClass ID
			PromotionClass pcc = pc.getPromotionClass();
			if (pcc != null)
				toks.add(pcc.getObjectUUID());

			//get promotion and base class combined ID
			if (bc != null && pcc != null)
				toks.add( ((pcc.getObjectUUID() * 10) + bc.getObjectUUID()) );

			//get any other rune IDs
			ArrayList<CharacterRune> runes = pc.getRunes();
			for (CharacterRune rune : runes)
				toks.add(rune.getRuneBaseID());

			//Add any power bonuses granted from runes up
			short amount = (short) 0;
			for (Integer tok : toks) {
				if (runeGrants.containsKey(tok))
					amount += runeGrants.get(tok);
			}

			return amount;
		} else
			return (short) 0;
	}

	public static void fillGrantedPowers() {
		PowerGrant.grantedPowers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		PreparedStatementShared ps = null;
		try {
			ps = prepareStatement("SELECT * FROM static_power_powergrant");
			ResultSet rs = ps.executeQuery();
			if (PowerGrant.grantedPowers.size() > 0) {
				rs.close();
				return;
			}
			while (rs.next()) {
				int token = rs.getInt("powerToken");
				PowerGrant pg = null;
				if (PowerGrant.grantedPowers.containsKey(token)) {
					pg = PowerGrant.grantedPowers.get(token);
					pg.addRuneGrant(rs.getInt("runeID"), rs.getShort("grantAmount"));
				} else {
					pg = new PowerGrant(rs);
					PowerGrant.grantedPowers.put(token, pg);
				}
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error( "SQL Error number: " + e.getErrorCode(), e);
		} finally {
			ps.release();
		}
	}

	@Override
	public void updateDatabase() {}
}
