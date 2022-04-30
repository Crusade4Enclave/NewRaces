// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.archive;

import engine.Enum;
import engine.objects.Realm;
import engine.workthreads.WarehousePushThread;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;

public class RealmRecord extends DataRecord {

    private static final LinkedBlockingQueue<RealmRecord> recordPool = new LinkedBlockingQueue<>();

    private Realm realm;
    private Enum.RecordEventType eventType;
    private String cityHash;
    private String guildHash;
    private String charterType;
    private LocalDateTime eventDateTime;

    private RealmRecord(Realm realm) {
        this.recordType = Enum.DataRecordType.REALM;
        this.realm = realm;
        this.eventType = Enum.RecordEventType.CAPTURE;

    }

    public static RealmRecord borrow(Realm realm, Enum.RecordEventType eventType) {
        RealmRecord realmRecord;

        realmRecord = recordPool.poll();

        if (realmRecord == null) {
            realmRecord = new RealmRecord(realm);
            realmRecord.eventType = eventType;
        }
        else {
            realmRecord.recordType = Enum.DataRecordType.REALM;
            realmRecord.eventType = eventType;
            realmRecord.realm = realm;

        }

        realmRecord.cityHash = realm.getRulingCity().getHash();
        realmRecord.guildHash = realm.getRulingCity().getGuild().getHash();
        realmRecord.charterType = Enum.CharterType.getCharterTypeByID(realmRecord.realm.getCharterType()).name();

        if (realmRecord.eventType.equals(Enum.RecordEventType.CAPTURE))
            realmRecord.eventDateTime =  realm.ruledSince;
        else
            realmRecord.eventDateTime = LocalDateTime.now();

        return realmRecord;
    }

    public static PreparedStatement buildRealmPushStatement(Connection connection, ResultSet rs) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "INSERT INTO `warehouse_realmhistory` (`event_number`, `realm_id`, `realm_name`, `charter`, `city_id`, `guild_id`, `eventType`, `datetime`) VALUES(?,?,?,?,?,?,?,?)";

        outStatement = connection.prepareStatement(queryString);

        // Bind record data

        outStatement.setInt(1, rs.getInt("event_number"));
        outStatement.setString(2, rs.getString("realm_id"));
        outStatement.setString(3, rs.getString("realm_name"));
        outStatement.setString(4, rs.getString("charter"));
        outStatement.setString(5, rs.getString("city_id"));
        outStatement.setString(6, rs.getString("guild_id"));
        outStatement.setString(7, rs.getString("eventType"));
        outStatement.setTimestamp(8, rs.getTimestamp("datetime"));

        return outStatement;
    }

    public static PreparedStatement buildRealmQueryStatement(Connection connection) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "SELECT * FROM `warehouse_realmhistory` WHERE `event_number` > ?";
        outStatement = connection.prepareStatement(queryString);
        outStatement.setInt(1, WarehousePushThread.realmIndex);
        return outStatement;
    }

    void reset() {

        this.realm = null;
        this.cityHash = null;
        this.guildHash = null;
        this.eventDateTime = null;
        this.charterType = null;
    }

    public void release() {
        this.reset();
        recordPool.add(this);
    }

    private PreparedStatement buildRealmInsertStatement(Connection connection) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "INSERT INTO `warehouse_realmhistory` (`realm_id`, `realm_name`, `charter`, `city_id`, `guild_id`, `eventType`, `datetime`) VALUES(?,?,?,?,?,?,?)";
        outStatement = connection.prepareStatement(queryString);

        // Bind Record Data

        outStatement.setString(1, realm.getHash());
        outStatement.setString(2, realm.getRealmName());
        outStatement.setString(3, charterType);
        outStatement.setString(4, cityHash);
        outStatement.setString(5, guildHash);
        outStatement.setString(6, eventType.name());
        outStatement.setTimestamp(7,  Timestamp.valueOf(this.eventDateTime));

        return outStatement;
    }

    public void write() {

        try (Connection connection = DataWarehouse.connectionPool.getConnection();
             PreparedStatement statement = this.buildRealmInsertStatement(connection)) {

            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
