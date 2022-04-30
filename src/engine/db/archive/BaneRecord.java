// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.archive;

import engine.Enum;
import engine.objects.Bane;
import engine.objects.City;
import engine.workthreads.WarehousePushThread;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;

import static engine.Enum.RecordEventType;

public class BaneRecord extends DataRecord {

	private static final LinkedBlockingQueue<BaneRecord> recordPool = new LinkedBlockingQueue<>();
	private RecordEventType eventType;
	private String cityHash;
	private String cityName;
	private String cityGuildHash;
	private String cityNationHash;
	private String baneDropperHash;
	private String baneGuildHash;
	private String baneNationHash;
	private DateTime baneLiveTime;
	private DateTime baneDropTime;

	private BaneRecord(Bane bane) {
		this.recordType = Enum.DataRecordType.BANE;
		this.eventType = RecordEventType.PENDING;
	}

	public static BaneRecord borrow(Bane bane, RecordEventType eventType) {
		BaneRecord baneRecord;

		baneRecord = recordPool.poll();

		if (baneRecord == null) {
			baneRecord = new BaneRecord(bane);
			baneRecord.eventType = eventType;
		}
		else {
			baneRecord.recordType = Enum.DataRecordType.BANE;
			baneRecord.eventType = eventType;

		}

		baneRecord.cityHash = bane.getCity().getHash();
		baneRecord.cityName = bane.getCity().getCityName();
		baneRecord.cityGuildHash = bane.getCity().getGuild().getHash();
		baneRecord.cityNationHash = bane.getCity().getGuild().getNation().getHash();


		if (bane.getOwner() == null) {
			baneRecord.baneDropperHash = "ERRANT";
			baneRecord.baneGuildHash = "ERRANT";
			baneRecord.baneNationHash = "ERRANT";
		}
		else {
			baneRecord.baneDropperHash = DataWarehouse.hasher.encrypt(bane.getOwner().getObjectUUID());  // getPlayerCharacter didn't check hash first?  OMFG


			baneRecord.baneGuildHash = bane.getOwner().getGuild().getHash();
			baneRecord.baneNationHash = bane.getOwner().getGuild().getNation().getHash();


			baneRecord.baneLiveTime = bane.getLiveDate();
			baneRecord.baneDropTime = bane.getPlacementDate();
		}


		return baneRecord;
	}

	public static PreparedStatement buildBanePushStatement(Connection connection, ResultSet rs) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "INSERT INTO `warehouse_banehistory` (`event_number`, `city_id`, `city_name`, `char_id`, `offGuild_id`, `offNat_id`, `defGuild_id`, `defNat_id`, `dropDatetime`, `liveDateTime`, `resolution`) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		java.util.Date sqlDateTime;

		outStatement = connection.prepareStatement(queryString);

		// Bind record data

		outStatement.setInt(1, rs.getInt("event_number"));
		outStatement.setString(2, rs.getString("city_id"));
		outStatement.setString(3, rs.getString("city_name"));
		outStatement.setString(4, rs.getString("char_id"));
		outStatement.setString(5, rs.getString("offGuild_id"));
		outStatement.setString(6, rs.getString("offNat_id"));
		outStatement.setString(7, rs.getString("defGuild_id"));
		outStatement.setString(8, rs.getString("defNat_id"));

		sqlDateTime = rs.getTimestamp("dropDatetime");

		if (sqlDateTime == null)
			outStatement.setNull(9, Types.DATE);
		else
			outStatement.setTimestamp(9, rs.getTimestamp("dropDatetime"));

		sqlDateTime = rs.getTimestamp("dropDatetime");

		if (sqlDateTime == null)
			outStatement.setNull(10, Types.DATE);
		else
			outStatement.setTimestamp(10, rs.getTimestamp("liveDateTime"));

		outStatement.setString(11, rs.getString("resolution"));

		return outStatement;
	}

	public static PreparedStatement buildBaneQueryStatement(Connection connection) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "SELECT * FROM `warehouse_banehistory` WHERE `event_number` > ?";
		outStatement = connection.prepareStatement(queryString);
		outStatement.setInt(1, WarehousePushThread.baneIndex);
		return outStatement;
	}

	public static DateTime getLastBaneDateTime(City city) {

		DateTime outDateTime = null;

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = buildDateTimeQueryStatement(connection, city);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {

				outDateTime = new DateTime(rs.getTimestamp("endDatetime"));

			}

		} catch (SQLException e) {
			Logger.error( e.toString());
		}

		return outDateTime;
	}


	private static PreparedStatement buildDateTimeQueryStatement (Connection connection, City city) throws SQLException {
		PreparedStatement outStatement;
		String queryString = "SELECT `endDatetime` FROM `warehouse_banehistory` WHERE `city_id` = ? ORDER BY `endDatetime` DESC LIMIT 1";
		outStatement = connection.prepareStatement(queryString);
		outStatement.setString(1, city.getHash());
		return outStatement;

	}

	public static void updateLiveDate(Bane bane, DateTime dateTime) {

		if (bane == null)
			return;

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = buildUpdateLiveDateStatement(connection, bane, dateTime)) {

			statement.execute();

		} catch (SQLException e) {
			Logger.error( e.toString());
		}
	}

	private static PreparedStatement buildUpdateLiveDateStatement(Connection connection, Bane bane, DateTime dateTime) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "UPDATE `warehouse_banehistory` SET `liveDatetime` = ?, `dirty` = 1 WHERE `city_id` = ? AND `resolution` = 'PENDING'";

		outStatement = connection.prepareStatement(queryString);
		outStatement.setTimestamp(1, new java.sql.Timestamp(dateTime.getMillis()));
		outStatement.setString(2, bane.getCity().getHash());

		return outStatement;
	}

	private static PreparedStatement buildUpdateResolutionStatement(Connection connection, Bane bane, RecordEventType eventType) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "UPDATE `warehouse_banehistory` SET `endDatetime` = ?, `resolution` = ?, `dirty` = 1 WHERE `city_id` = ? AND `resolution` = 'PENDING'";

		outStatement = connection.prepareStatement(queryString);
		outStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
		outStatement.setString(2, eventType.name());
		outStatement.setString(3, bane.getCity().getHash());

		return outStatement;
	}

	public static void updateResolution(Bane bane, RecordEventType eventType) {

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = buildUpdateResolutionStatement(connection, bane, eventType)) {

			statement.execute();

		} catch (SQLException e) {
			Logger.error(e.toString());
		}
	}

	public static String getBaneHistoryString() {

		String outString;
		String queryString;
		String dividerString;
		String newLine = System.getProperty("line.separator");
		outString = "[LUA_BANES() DATA WAREHOUSE]" + newLine;
		dividerString = "--------------------------------" + newLine;
		queryString = "CALL `baneHistory`()";

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = connection.prepareCall(queryString);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {

				outString += "Magicbane unresolved banes: " + rs.getInt("PENDING") + '/' + rs.getInt("TOTAL") + newLine;
				outString += dividerString;
				outString += "Bane Resolution History" + newLine;
				outString += dividerString;

				outString += "Destruction: " + rs.getInt("DESTROY") + newLine;
				outString += "Capture: " + rs.getInt("CAPTURE") + newLine;
				outString += "Defended: " + rs.getInt("DEFEND") + newLine;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return outString;
	}

	public static void updateDirtyRecords() {

		String queryString = "SELECT * FROM `warehouse_banehistory` where `dirty` = 1";

		// Reset character delta

		WarehousePushThread.baneDelta = 0;

		try (Connection localConnection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = localConnection.prepareStatement(queryString, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); // Make this an updatable result set as we'll reset the dirty flag as we go along
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {

				// Only update the index and dirty flag
				// if the remote database update succeeded

				if (updateDirtyRecord(rs) == true)
					WarehousePushThread.baneDelta++;
				else
					continue;

				// Reset the dirty flag in the local database

				rs.updateInt("dirty", 0);
				rs.updateRow();
			}

		} catch (SQLException e) {
			Logger.error( e.toString());
		}
	}

	private static boolean updateDirtyRecord(ResultSet rs) {

		try (Connection remoteConnection = DataWarehouse.remoteConnectionPool.getConnection();
				PreparedStatement statement = buildUpdateDirtyStatement(remoteConnection, rs)) {

			statement.execute();
			return true;
		} catch (SQLException e) {
			Logger.error( e.toString());
			return false;
		}
	}

	private static PreparedStatement buildUpdateDirtyStatement(Connection connection, ResultSet rs) throws SQLException {

		PreparedStatement outStatement;
		String queryString = "UPDATE `warehouse_banehistory` SET `liveDateTime` = ?, `endDateTime` = ?, `resolution` = ? WHERE `event_number` = ?";
		java.util.Date sqlDateTime;

		outStatement = connection.prepareStatement(queryString);

		// Bind record data

		sqlDateTime = rs.getTimestamp("liveDateTime");

		if (sqlDateTime == null)
			outStatement.setNull(1, Types.DATE);
		else
			outStatement.setTimestamp(1, rs.getTimestamp("liveDateTime"));

		sqlDateTime = rs.getTimestamp("endDateTime");

		if (sqlDateTime == null)
			outStatement.setNull(2, Types.DATE);
		else
			outStatement.setTimestamp(2, rs.getTimestamp("endDateTime"));

		outStatement.setString(3, rs.getString("resolution"));
		outStatement.setInt(4, rs.getInt("event_number"));

		return outStatement;
	}

	void reset() {
		this.cityHash = null;
		this.cityGuildHash = null;
		this.cityNationHash = null;
		this.baneDropperHash = null;
		this.baneGuildHash = null;
		this.baneNationHash = null;
		this.baneLiveTime = null;
	}

	public void release() {
		this.reset();
		recordPool.add(this);
	}

	public void write() {

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = buildBaneInsertStatement(connection)) {

			statement.execute();

		} catch (SQLException e) {
			Logger.error( e.toString());
		}

	}

	private PreparedStatement buildBaneInsertStatement(Connection connection) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "INSERT INTO `warehouse_banehistory` (`city_id`, `city_name`, `char_id`, `offGuild_id`, `offNat_id`, `defGuild_id`, `defNat_id`, `dropDatetime`, `liveDateTime`, `resolution`) VALUES(?,?,?,?,?,?,?,?,?,?)";

		outStatement = connection.prepareStatement(queryString);

		outStatement.setString(1, this.cityHash);
		outStatement.setString(2, this.cityName);
		outStatement.setString(3, this.baneDropperHash);
		outStatement.setString(4, this.baneGuildHash);
		outStatement.setString(5, this.baneNationHash);
		outStatement.setString(6, this.cityGuildHash);
		outStatement.setString(7, this.cityNationHash);

		if (this.baneDropTime == null)
			outStatement.setNull(8, java.sql.Types.DATE);
		else
			outStatement.setTimestamp(8, new java.sql.Timestamp(this.baneDropTime.getMillis()));

		if (this.baneLiveTime == null)
			outStatement.setNull(9, java.sql.Types.DATE);
		else
			outStatement.setTimestamp(9, new java.sql.Timestamp(this.baneLiveTime.getMillis()));

		outStatement.setString(10, this.eventType.name());


		return outStatement;
	}
} // END CLASS

