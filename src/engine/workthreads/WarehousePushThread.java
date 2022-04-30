// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.workthreads;

/*
 * This thread pushes cumulative warehouse data to
 * a remote database.
 *
 */

import engine.Enum;
import engine.db.archive.*;
import engine.gameManager.ConfigManager;
import org.pmw.tinylog.Logger;

import java.sql.*;

public class WarehousePushThread implements Runnable {

	// Used to track last push.  These are read
	// at thread startup and written back out
	// when we're done

	public static int charIndex, charDelta;
	public static int cityIndex, cityDelta;
	public static int guildIndex, guildDelta;
	public static int realmIndex, realmDelta;
	public static int baneIndex, baneDelta;
	public static int pvpIndex, pvpDelta;
	public static int mineIndex, mineDelta;

	public WarehousePushThread() {

	}

	public void run() {

		int recordCount = 0;
		boolean writeSuccess = true;

        if ( ConfigManager.MB_WORLD_WAREHOUSE_PUSH.getValue().equals("false")) {
            Logger.info("WAREHOUSEPUSH DISABLED: EARLY EXIT");
            return;
        }

		// Cache where we left off from the last push
		// for each of the warehouse tables

		if (readWarehouseIndex() == false)
			return;

		// Log run to console

		Logger.info( "Pushing records to remote...");

		// Push records to remote database

		for (Enum.DataRecordType recordType : Enum.DataRecordType.values()) {

			switch (recordType) {
			case PVP:
				if (pushPvpRecords() == true) {
					recordCount = Math.max(0, pvpDelta - pvpIndex);
					pvpIndex += recordCount;
				} else
					writeSuccess = false;
				break;
			case CHARACTER:
				if (pushCharacterRecords() == true) {
					recordCount = Math.max(0, charDelta - charIndex);
					charIndex += recordCount;
				} else
					writeSuccess = false;
				break;
			case REALM:
				if (pushRealmRecords() == true) {
					recordCount = Math.max(0, realmDelta - realmIndex);
					realmIndex += recordCount;
				}
				else
					writeSuccess = false;
				break;
			case GUILD:
				if (pushGuildRecords() == true) {
					recordCount = Math.max(0, guildDelta - guildIndex);
					guildIndex += recordCount;
				}
				else
					writeSuccess = false;
				break;
			case BANE:
				if (pushBaneRecords() == true) {
					recordCount = Math.max(0, baneDelta - baneIndex);
					baneIndex += recordCount;
				}
				else
					writeSuccess = false;
				break;
			case CITY:
				if (pushCityRecords() == true) {
					recordCount = Math.max(0, cityDelta - cityIndex);
					cityIndex += recordCount;
				} else
					writeSuccess = false;
				break;
			case MINE:
				if (pushMineRecords() == true) {
					recordCount = Math.max(0, mineDelta - mineIndex);
					mineIndex += recordCount;
				} else
					writeSuccess = false;
				break;
			default:
				recordCount = 0;
				writeSuccess = false;
				break; // unhandled type
			}

			if (writeSuccess == true)
				Logger.info( recordCount + " " + recordType.name() + " records sent to remote");
			else
				Logger.info( recordCount + " returning failed success");

		}  // Iterate switch

		// Update indices

		updateWarehouseIndex();

		// Update dirty records

		Logger.info( "Pushing updates of dirty warehouse records");
		CharacterRecord.updateDirtyRecords();

		if (charDelta > 0)
			Logger.info( charDelta + " dirty character records were sent");
		;
		BaneRecord.updateDirtyRecords();

		if (baneDelta > 0)
			Logger.info( baneDelta + " dirty bane records were sent");

		Logger.info( "Process has completed");

	}

	public static boolean pushMineRecords() {

		try (Connection localConnection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = MineRecord.buildMineQueryStatement(localConnection);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {
				pushMineRecord(rs);
				mineDelta = rs.getInt("event_number");
			}

			return true;
		} catch (SQLException e) {
			Logger.error( "Error with local DB connection: " + e.toString());
			e.printStackTrace();
			return false;
		}
	}

	public static boolean pushCharacterRecords() {

		try (Connection localConnection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = CharacterRecord.buildCharacterQueryStatement(localConnection);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {
				pushCharacterRecord(rs);
				charDelta = rs.getInt("event_number");
			}

			return true;
		} catch (SQLException e) {
			Logger.error( "Error with local DB connection: " + e.toString());
			e.printStackTrace();
			return false;
		}
	}

	private static boolean pushGuildRecords() {

		try (Connection localConnection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = GuildRecord.buildGuildQueryStatement(localConnection);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {
				pushGuildRecord(rs);
				guildDelta = rs.getInt("event_number");
			}

			return true;
		} catch (SQLException e) {
			Logger.error("Error with local DB connection: " + e.toString());
			e.printStackTrace();
			return false;
		}
	}

	private static boolean pushMineRecord(ResultSet rs) {

		try (Connection remoteConnection = DataWarehouse.remoteConnectionPool.getConnection();
				PreparedStatement statement = MineRecord.buildMinePushStatement(remoteConnection, rs)) {

			statement.execute();
			return true;

		} catch (SQLException e) {
			Logger.error( e.toString());
			return false;
		}
	}

	private static boolean pushGuildRecord(ResultSet rs) {

		try (Connection remoteConnection = DataWarehouse.remoteConnectionPool.getConnection();
				PreparedStatement statement = GuildRecord.buildGuildPushStatement(remoteConnection, rs)) {

			statement.execute();
			return true;

		} catch (SQLException e) {
			Logger.error(e.toString());
			return false;
		}
	}

	private static boolean pushBaneRecords() {

		try (Connection localConnection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = BaneRecord.buildBaneQueryStatement(localConnection);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {
				pushBaneRecord(rs);
				baneDelta = rs.getInt("event_number");
			}

			return true;
		} catch (SQLException e) {
			Logger.error("Error with local DB connection: " + e.toString());
			e.printStackTrace();
			return false;
		}
	}

	private static boolean pushBaneRecord(ResultSet rs) {

		try (Connection remoteConnection = DataWarehouse.remoteConnectionPool.getConnection();
				PreparedStatement statement = BaneRecord.buildBanePushStatement(remoteConnection, rs)) {

			statement.execute();
			return true;

		} catch (SQLException e) {
			Logger.error(e.toString());
			return false;
		}
	}

	private static boolean pushCityRecords() {

		try (Connection localConnection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = CityRecord.buildCityQueryStatement(localConnection);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {
				pushCityRecord(rs);
				cityDelta = rs.getInt("event_number");
			}

			return true;
		} catch (SQLException e) {
			Logger.error( "Error with local DB connection: " + e.toString());
			e.printStackTrace();
			return false;
		}
	}

	private static boolean pushPvpRecords() {

		try (Connection localConnection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = PvpRecord.buildPvpQueryStatement(localConnection);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {

				if (pushPvpRecord(rs) == true)
					pvpDelta = rs.getInt("event_number");
			}

			return true;
		} catch (SQLException e) {
			Logger.error("Error with local DB connection: " + e.toString());
			return false;
		}
	}

	private static boolean pushPvpRecord(ResultSet rs) {

		try (Connection remoteConnection = DataWarehouse.remoteConnectionPool.getConnection();
				PreparedStatement statement = PvpRecord.buildPvpPushStatement(remoteConnection, rs)) {

			statement.execute();
			return true;
		} catch (SQLException e) {
			Logger.error(e.toString());
			return false;
		}

	}

	private static boolean pushRealmRecords() {

		try (Connection localConnection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = RealmRecord.buildRealmQueryStatement(localConnection);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {

				if (pushRealmRecord(rs) == true)
					realmDelta = rs.getInt("event_number");
			}

			return true;
		} catch (SQLException e) {
			Logger.error( "Error with local DB connection: " + e.toString());
			e.printStackTrace();
			return false;
		}
	}

	private static boolean pushRealmRecord(ResultSet rs) {

		try (Connection remoteConnection = DataWarehouse.remoteConnectionPool.getConnection();
				PreparedStatement statement = RealmRecord.buildRealmPushStatement(remoteConnection, rs)) {
			statement.execute();
			return true;

		} catch (SQLException e) {
			Logger.error( e.toString());
			return false;
		}

	}

	private static boolean pushCharacterRecord(ResultSet rs) {

		try (Connection remoteConnection = DataWarehouse.remoteConnectionPool.getConnection();
				PreparedStatement statement = CharacterRecord.buildCharacterPushStatement(remoteConnection, rs)) {

			statement.execute();
			return true;

		} catch (SQLException e) {
			Logger.error(e.toString());
			return false;
		}

	}

	private static boolean pushCityRecord(ResultSet rs) {

		try (Connection remoteConnection = DataWarehouse.remoteConnectionPool.getConnection();
				PreparedStatement statement = CityRecord.buildCityPushStatement(remoteConnection, rs)) {

			statement.execute();
			return true;

		} catch (SQLException e) {
			Logger.error( e.toString());
			return false;
		}
	}

	private static boolean readWarehouseIndex() {

		// Member variable declaration

		String queryString;

		queryString = "SELECT * FROM `warehouse_index`";

		try (Connection localConnection = DataWarehouse.connectionPool.getConnection();
				CallableStatement statement = localConnection.prepareCall(queryString);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {
				charIndex = rs.getInt("charIndex");
				cityIndex = rs.getInt("cityIndex");
				guildIndex = rs.getInt("guildIndex");
				realmIndex = rs.getInt("realmIndex");
				baneIndex = rs.getInt("baneIndex");
				pvpIndex = rs.getInt("pvpIndex");
				mineIndex = rs.getInt("mineIndex");
			}

			return true;

		} catch (SQLException e) {
			Logger.error( "Error reading warehouse index" + e.toString());
			e.printStackTrace();
			return false;
		}
	}

	private static boolean updateWarehouseIndex() {

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = WarehousePushThread.buildIndexUpdateStatement(connection)) {

			statement.execute();
			return true;

		} catch (SQLException e) {
			Logger.error( e.toString());
			return false;
		}
	}

	private static PreparedStatement buildIndexUpdateStatement(Connection connection) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "UPDATE `warehouse_index` SET `charIndex` = ?, `cityIndex` = ?, `guildIndex` = ?, `realmIndex` = ?, `baneIndex` = ?, `pvpIndex` = ?, `mineIndex` = ?";
		outStatement = connection.prepareStatement(queryString);

		// Bind record data

		outStatement.setInt(1, charIndex);
		outStatement.setInt(2, cityIndex);
		outStatement.setInt(3, guildIndex);
		outStatement.setInt(4, realmIndex);
		outStatement.setInt(5, baneIndex);
		outStatement.setInt(6, pvpIndex);
		outStatement.setInt(7, mineIndex);
		return outStatement;
	}
}

