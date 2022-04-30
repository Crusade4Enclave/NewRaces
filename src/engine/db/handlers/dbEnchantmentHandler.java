// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class dbEnchantmentHandler extends dbHandlerBase {

	public ConcurrentHashMap<String, Integer> GET_ENCHANTMENTS_FOR_ITEM(final int id) {
		ConcurrentHashMap<String, Integer> enchants = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		prepareCallable("SELECT * FROM `dyn_item_enchantment` WHERE `ItemID`=?;");
		setLong(1, (long)id);
		try {
			ResultSet resultSet = executeQuery();
			while (resultSet.next())
				enchants.put(resultSet.getString("powerAction"), resultSet.getInt("rank"));
		} catch (SQLException e) {
			Logger.error("SQL Error number: " + e.getErrorCode());
		} finally {
			closeCallable();
		}
		return enchants;
	}

	public boolean CREATE_ENCHANTMENT_FOR_ITEM(long itemID, String powerAction, int rank) {
		prepareCallable("INSERT INTO `dyn_item_enchantment` (`itemID`, `powerAction`, `rank`) VALUES (?, ?, ?);");
		setLong(1, itemID);
		setString(2, powerAction);
		setInt(3, rank);
		return (executeUpdate() != 0);
	}

	public boolean CLEAR_ENCHANTMENTS(long itemID) {
		prepareCallable("DELETE FROM `dyn_item_enchantment` WHERE `itemID`=?;");
		setLong(1, itemID);
		return (executeUpdate() != 0);
	}

}
