// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.archive;

import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.objects.Guild;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;
import engine.workthreads.WarehousePushThread;
import org.pmw.tinylog.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import static engine.Enum.DataRecordType;
import static engine.Enum.PvpHistoryType;

public class PvpRecord extends DataRecord {

	private static final LinkedBlockingQueue<PvpRecord> recordPool = new LinkedBlockingQueue<>();

	private PlayerCharacter player;
	private PlayerCharacter victim;
	private Vector3fImmutable location;
	private boolean pvpExp;

	private PvpRecord(PlayerCharacter player, PlayerCharacter victim, Vector3fImmutable location, boolean pvpExp) {
		this.recordType = DataRecordType.PVP;
		this.player = player;
		this.victim = victim;
		this.location = new Vector3fImmutable(location);
		this.pvpExp = pvpExp;
	}

	public static PvpRecord borrow(PlayerCharacter player, PlayerCharacter victim, Vector3fImmutable location, boolean pvpExp) {

		PvpRecord pvpRecord;

		pvpRecord = recordPool.poll();

		if (pvpRecord == null) {
			pvpRecord = new PvpRecord(player, victim, location, pvpExp);
		}
		else {
			pvpRecord.recordType = DataRecordType.PVP;
			pvpRecord.player = player;
			pvpRecord.victim = victim;
			pvpRecord.location = new Vector3fImmutable(location);
			pvpRecord.pvpExp = pvpExp;
		}

		return pvpRecord;
	}

	private static PreparedStatement buildHistoryStatement(Connection connection, int charUUID, PvpHistoryType historyType) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "";

		switch (historyType) {
		case KILLS:
			queryString = "SELECT DISTINCT `victim_id`, `datetime` FROM warehouse_pvphistory where char_id = ? " +
					"ORDER BY `datetime` DESC LIMIT 10";
			break;
		case DEATHS:
			queryString = "SELECT DISTINCT `char_id`,`datetime` FROM warehouse_pvphistory where `victim_id` = ? " +
					"ORDER BY `datetime` DESC LIMIT 10";
			break;
		}

		outStatement = connection.prepareStatement(queryString);
		outStatement.setString(1, DataWarehouse.hasher.encrypt(charUUID));

		return outStatement;
	}

	public static LinkedList<Integer> getCharacterPvPHistory(int charUUID, PvpHistoryType historyType) {

		// Member variable declaration

		LinkedList<Integer> outList = new LinkedList<>();

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = buildHistoryStatement(connection, charUUID, historyType);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {

				switch (historyType) {
				case KILLS:
					outList.add((int) DataWarehouse.hasher.decrypt(rs.getString("victim_id"))[0]);
					break;
				case DEATHS:
					outList.add((int) DataWarehouse.hasher.decrypt(rs.getString("char_id"))[0]);
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return outList;
	}

	private static PreparedStatement buildLuaHistoryQueryStatement(Connection connection, int charUUID) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "CALL `pvpHistory`(?)";

		outStatement = connection.prepareStatement(queryString);
		outStatement.setString(1, DataWarehouse.hasher.encrypt(charUUID));

		return outStatement;
	}

	public static String getPvpHistoryString(int charUUID) {

		String outString;
		String dividerString;

		String newLine = System.getProperty("line.separator");

		outString = "[LUA_PVP() DATA WAREHOUSE]" + newLine;
		dividerString = "--------------------------------" + newLine;

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = buildLuaHistoryQueryStatement(connection, charUUID);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {

				int killCount;
				int deathCount;
				float killRatio;

				outString += "Total Magicbane murdered souls: " + rs.getInt("TOTALDEATHS") + newLine;
				outString += dividerString;
				outString += String.format("%-8s %-8s %-8s %-8s %n", "Period", "Kills", "Deaths", "K/D");
				outString += dividerString;

				killCount = rs.getInt("KILLCOUNT");
				deathCount = rs.getInt("DEATHCOUNT");

				if (deathCount == 0)
					killRatio = (float) killCount;
				else
					killRatio = (float) killCount / deathCount;

				try {
					outString += String.format("%-8s %-8d %-8d %.2f %n", "Total", killCount, deathCount, killRatio);

					killCount = rs.getInt("DAILYKILLS");
					deathCount = rs.getInt("DAILYDEATHS");

					if (deathCount == 0)
						killRatio = (float) killCount;
					else
						killRatio = (float) killCount / deathCount;

					outString += String.format("%-8s %-8d %-8d %.2f %n", "24hrs", killCount, deathCount, killRatio);

					killCount = rs.getInt("HOURLYKILLS");
					deathCount = rs.getInt("HOURLYDEATHS");

					if (deathCount == 0)
						killRatio = (float) killCount;
					else
						killRatio = (float) killCount / deathCount;

					outString += String.format("%-8s %-8d %-8d %.2f %n", "1hr", killCount, deathCount, killRatio);
				} catch (Exception e) {
					Logger.error(e.toString());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return outString;
	}

	public static PreparedStatement buildPvpPushStatement(Connection connection, ResultSet rs) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "INSERT INTO `warehouse_pvphistory` (`event_number`, `char_id`, `char_guild_id`, `char_nation_id`, `char_level`," +
				" `victim_id`, `victim_guild_id`, `victim_nation_id`, `victim_level`," +
				" `zone_id`, `zone_name`, `loc_x`, `loc_y`, `gave_exp`, `datetime`) " +
				" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		outStatement = connection.prepareStatement(queryString);

		// Bind record data

		outStatement.setInt(1, rs.getInt("event_number"));
		outStatement.setString(2, rs.getString("char_id"));
		outStatement.setString(3, rs.getString("char_guild_id"));
		outStatement.setString(4, rs.getString("char_nation_id"));
		outStatement.setInt(5, rs.getInt("char_level"));

		// Bind victim data

		outStatement.setString(6, rs.getString("victim_id"));
		outStatement.setString(7, rs.getString("victim_guild_id"));
		outStatement.setString(8, rs.getString("victim_nation_id"));
		outStatement.setInt(9, rs.getInt("victim_level"));

		outStatement.setString(10, rs.getString("zone_id"));
		outStatement.setString(11, rs.getString("zone_name"));
		outStatement.setFloat(12, rs.getFloat("loc_x"));
		outStatement.setFloat(13, rs.getFloat("loc_y"));
		outStatement.setBoolean(14, rs.getBoolean("gave_exp"));
		outStatement.setTimestamp(15, rs.getTimestamp("datetime"));

		return outStatement;
	}

	public static PreparedStatement buildPvpQueryStatement(Connection connection) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "SELECT * FROM `warehouse_pvphistory` WHERE `event_number` > ?";
		outStatement = connection.prepareStatement(queryString);
		outStatement.setInt(1, WarehousePushThread.pvpIndex);
		return outStatement;
	}

	void reset() {
		this.player = null;
		this.victim = null;
		this.location = Vector3fImmutable.ZERO;
		pvpExp = false;
	}

	public void release() {
		this.reset();
		recordPool.add(this);
	}

	private PreparedStatement buildPvPInsertStatement(Connection connection) throws SQLException {

		Guild charGuild;
		Guild victimGuild;
		Zone zone;
		PreparedStatement outStatement = null;

		String queryString = "INSERT INTO `warehouse_pvphistory` (`char_id`, `char_guild_id`, `char_nation_id`, `char_level`," +
				" `victim_id`, `victim_guild_id`, `victim_nation_id`, `victim_level`," +
				" `zone_id`, `zone_name`, `loc_x`, `loc_y`, `gave_exp`, `datetime`) " +
				" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		outStatement = connection.prepareStatement(queryString);

		charGuild = this.player.getGuild();
		victimGuild = this.victim.getGuild();

		// Use a proxy in the situation where a char guild is null (errant)

		
		// Retrieve the zone name where the PvP event occurred

		zone = ZoneManager.findSmallestZone(this.location);

		outStatement.setString(1, DataWarehouse.hasher.encrypt(this.player.getObjectUUID()));
		outStatement.setString(2, DataWarehouse.hasher.encrypt(charGuild.getObjectUUID()));
		outStatement.setString(3, DataWarehouse.hasher.encrypt(charGuild.getNation().getObjectUUID()));
		outStatement.setInt(4, this.player.getLevel());

		// Bind victim data

		outStatement.setString(5, DataWarehouse.hasher.encrypt(this.victim.getObjectUUID()));
		outStatement.setString(6, DataWarehouse.hasher.encrypt(victimGuild.getObjectUUID()));
		outStatement.setString(7, DataWarehouse.hasher.encrypt(victimGuild.getNation().getObjectUUID()));
		outStatement.setInt(8, this.victim.getLevel());

		outStatement.setString(9, DataWarehouse.hasher.encrypt(zone.getObjectUUID()));
		outStatement.setString(10, zone.getName());
		outStatement.setFloat(11, this.location.getX());
		outStatement.setFloat(12, -this.location.getZ()); // flip sign on 'y' coordinate
		outStatement.setBoolean(13, this.pvpExp);
		outStatement.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));

		return outStatement;
	}


	public void write() {

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = buildPvPInsertStatement(connection)) {

			statement.execute();

		} catch (SQLException e) {
			Logger.error( e.toString());
		}

		// Warehouse record for this pvp event written if code path reaches here.
		// Time to update the respective kill counters.

		CharacterRecord.advanceKillCounter(this.player);
		CharacterRecord.advanceDeathCounter(this.victim);

	}
}
