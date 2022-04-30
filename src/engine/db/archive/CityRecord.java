// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.archive;

import engine.Enum;
import engine.objects.City;
import engine.workthreads.WarehousePushThread;

import java.sql.*;
import java.util.concurrent.LinkedBlockingQueue;

public class CityRecord extends DataRecord {

	private static final LinkedBlockingQueue<CityRecord> recordPool = new LinkedBlockingQueue<>();
	private Enum.RecordEventType eventType;
	private City city;
	private String cityHash;
	private String cityGuildHash;
	private String cityName;
	private String cityMotto;
	private float locX;
	private float locY;
	private String zoneHash;
	private java.time.LocalDateTime establishedDatetime;

	private CityRecord(City city) {
		this.recordType = Enum.DataRecordType.CITY;
		this.city = city;
		this.eventType = Enum.RecordEventType.CREATE;

	}

	public static CityRecord borrow(City city, Enum.RecordEventType eventType) {
		CityRecord cityRecord;

		cityRecord = recordPool.poll();

		if (cityRecord == null) {
			cityRecord = new CityRecord(city);
			cityRecord.eventType = eventType;
		}
		else {
			cityRecord.recordType = Enum.DataRecordType.CITY;
			cityRecord.eventType = eventType;
			cityRecord.city = city;

		}

		if (cityRecord.city.getHash() == null)
			cityRecord.city.setHash(DataWarehouse.hasher.encrypt(cityRecord.city.getObjectUUID()));

		cityRecord.cityHash = cityRecord.city.getHash();


		cityRecord.cityName = cityRecord.city.getCityName();
		cityRecord.cityMotto = cityRecord.city.getMotto();

		cityRecord.cityGuildHash = cityRecord.city.getGuild().getHash();

		cityRecord.locX = cityRecord.city.getTOL().getLoc().x;
		cityRecord.locY = -cityRecord.city.getTOL().getLoc().z; // flip sign on 'y' coordinate

		cityRecord.zoneHash = cityRecord.city.getParent().getHash();

		if (cityRecord.eventType.equals(Enum.RecordEventType.CREATE))
            cityRecord.establishedDatetime =  cityRecord.city.established;
		else
			cityRecord.establishedDatetime = java.time.LocalDateTime.now();

		return cityRecord;
	}

	public static PreparedStatement buildCityPushStatement(Connection connection, ResultSet rs) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "INSERT INTO `warehouse_cityhistory` (`event_number`, `city_id`, `city_name`, `city_motto`, `guild_id`, `loc_x`, `loc_y`, `zone_id`, `eventType`, `datetime`) VALUES(?,?,?,?,?,?,?,?,?,?)";
		outStatement = connection.prepareStatement(queryString);

		// Bind record data

		outStatement.setInt(1, rs.getInt("event_number"));
		outStatement.setString(2, rs.getString("city_id"));
		outStatement.setString(3, rs.getString("city_name"));
		outStatement.setString(4, rs.getString("city_motto"));
		outStatement.setString(5, rs.getString("guild_id"));

		outStatement.setFloat(6, rs.getFloat("loc_x"));
		outStatement.setFloat(7, rs.getFloat("loc_y"));
		outStatement.setString(8, rs.getString("zone_id"));
		outStatement.setString(9, rs.getString("eventType"));
		outStatement.setTimestamp(10, rs.getTimestamp("datetime"));

		return outStatement;
	}

	public static PreparedStatement buildCityQueryStatement(Connection connection) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "SELECT * FROM `warehouse_cityhistory` WHERE `event_number` > ?";
		outStatement = connection.prepareStatement(queryString);
		outStatement.setInt(1, WarehousePushThread.cityIndex);
		return outStatement;
	}

	void reset() {
		this.city = null;
		this.cityHash = null;
		this.cityGuildHash = null;
		this.cityMotto = null;
		this.zoneHash = null;
		this.establishedDatetime = null;

	}

	public void release() {
		this.reset();
		recordPool.add(this);
	}

	public void write() {

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = this.buildCityInsertStatement(connection)) {

			statement.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private PreparedStatement buildCityInsertStatement(Connection connection) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "INSERT INTO `warehouse_cityhistory` (`city_id`, `city_name`, `city_motto`, `guild_id`, `loc_x`, `loc_y`, `zone_id`, `eventType`, `datetime`) VALUES(?,?,?,?,?,?,?,?,?)";

		outStatement = connection.prepareStatement(queryString);

		// Bind character data

		outStatement.setString(1, this.cityHash);
		outStatement.setString(2, this.cityName);
		outStatement.setString(3, this.cityMotto);
		outStatement.setString(4, this.cityGuildHash);

		outStatement.setFloat(5, this.locX);
		outStatement.setFloat(6, this.locY);
		outStatement.setString(7, this.zoneHash);
		outStatement.setString(8, this.eventType.name());
		outStatement.setTimestamp(9,  Timestamp.valueOf(this.establishedDatetime));

		return outStatement;
	}
}
