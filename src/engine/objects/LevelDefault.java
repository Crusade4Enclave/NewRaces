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
import java.util.concurrent.ConcurrentHashMap;

public class LevelDefault {

	public final int level;
	public final float health;
	public final float mana;
	public final float stamina;
	public final float atr;
	public final float def;
	public final float minDamage;
	public final float maxDamage;
	public final int goldMin;
	public final int goldMax;

	public static ConcurrentHashMap<Byte, LevelDefault> defaults = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

	/**
	 * ResultSet Constructor
	 */
	public LevelDefault(ResultSet rs) throws SQLException {
		super();
		this.level = rs.getInt("level");
		this.health = rs.getFloat("health");
		this.mana = (float)rs.getInt("mana");
		this.stamina = (float)rs.getInt("stamina");
		this.atr = (float)rs.getInt("atr");
		this.def = (float)rs.getInt("def");
		this.minDamage = (float)rs.getInt("minDamage");
		this.maxDamage = (float)rs.getInt("maxDamage");
		this.goldMin = rs.getInt("goldMin");
		this.goldMax = rs.getInt("goldMax");
	}

	public static LevelDefault getLevelDefault(byte level) {
		LevelDefault ret = null;
		if (LevelDefault.defaults.containsKey(level))
			return LevelDefault.defaults.get(level);

		PreparedStatementShared ps = null;
		try {
			ps = new PreparedStatementShared("SELECT * FROM `static_npc_level_defaults` WHERE level = ?;");
			ps.setInt(1, (int)level);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ret = new LevelDefault(rs);
				LevelDefault.defaults.put(level, ret);
			}
		} catch (SQLException e) {
			Logger.error("SQL Error number: " + e.getErrorCode() + ' ' + e.getMessage());
		} finally {
			ps.release();
		}
		return ret;
	}
}