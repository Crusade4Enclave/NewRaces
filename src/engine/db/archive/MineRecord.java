// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.archive;

import engine.Enum;
import engine.objects.AbstractCharacter;
import engine.objects.Mine;
import engine.objects.PlayerCharacter;
import engine.workthreads.WarehousePushThread;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;

public class MineRecord extends DataRecord {

    private static final LinkedBlockingQueue<MineRecord> recordPool = new LinkedBlockingQueue<>();
    private Enum.RecordEventType eventType;
    private String zoneHash;
    private String charHash;
    private String mineGuildHash;
    private String mineNationHash;
    private String mineType;
    private float locX;
    private float locY;

    private MineRecord() {
        this.recordType = Enum.DataRecordType.MINE;
        this.eventType = Enum.RecordEventType.CAPTURE;

    }

    public static MineRecord borrow(Mine mine, AbstractCharacter character, Enum.RecordEventType eventType) {

        MineRecord mineRecord;
        mineRecord = recordPool.poll();
        PlayerCharacter player;

        if (mineRecord == null) {
            mineRecord = new MineRecord();
            mineRecord.eventType = eventType;
        }
        else {
            mineRecord.recordType = Enum.DataRecordType.MINE;
            mineRecord.eventType = eventType;
        }

        mineRecord.zoneHash = mine.getParentZone().getHash();

        if (character.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) {
            player = (PlayerCharacter) character;
            mineRecord.charHash = player.getHash();
        }
        else
            mineRecord.charHash = character.getName();

        DataWarehouse.hasher.encrypt(0);

        if (mine.getOwningGuild() == null)
            mineRecord.mineGuildHash = "ERRANT";
        else
            mineRecord.mineGuildHash = mine.getOwningGuild().getHash();

        if (mine.getOwningGuild() == null)
            mineRecord.mineNationHash = "ERRANT";
        else
            mineRecord.mineNationHash = mine.getOwningGuild().getNation().getHash();

        mineRecord.locX = mine.getParentZone().getLoc().x;
        mineRecord.locY = -mine.getParentZone().getLoc().z;

        mineRecord.mineType = mine.getMineType().name;

        return mineRecord;
    }

    public static PreparedStatement buildMinePushStatement(Connection connection, ResultSet rs) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "INSERT INTO `warehouse_minehistory` (`event_number`, `zone_id`, `mine_type`, `char_id`, `mine_guildID`, `mine_nationID`, `loc_x`, `loc_y`, `eventType`, `datetime`) VALUES(?,?,?,?,?,?,?,?,?,?)";
        outStatement = connection.prepareStatement(queryString);

        // Bind record data

        outStatement.setInt(1, rs.getInt("event_number"));
        outStatement.setString(2, rs.getString("zone_id"));
        outStatement.setString(3, rs.getString("char_id"));
        outStatement.setString(4, rs.getString("mine_type"));
        outStatement.setString(5, rs.getString("mine_guildID"));
        outStatement.setString(6, rs.getString("mine_nationID"));

        outStatement.setFloat(7, rs.getFloat("loc_x"));
        outStatement.setFloat(8, rs.getFloat("loc_y"));
        outStatement.setString(9, rs.getString("eventType"));
        outStatement.setTimestamp(10, rs.getTimestamp("datetime"));

        return outStatement;
    }

    public static PreparedStatement buildMineQueryStatement(Connection connection) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "SELECT * FROM `warehouse_minehistory` WHERE `event_number` > ?";
        outStatement = connection.prepareStatement(queryString);
        outStatement.setInt(1, WarehousePushThread.mineIndex);
        return outStatement;
    }

    void reset() {
        this.zoneHash = null;
        this.charHash = null;
        this.mineGuildHash = null;
        this.mineNationHash = null;
        this.mineType = null;
        this.locX = 0.0f;
        this.locY = 0.0f;

    }

    public void release() {
        this.reset();
        recordPool.add(this);
    }

    public void write() {

        try (Connection connection = DataWarehouse.connectionPool.getConnection();
             PreparedStatement statement = this.buildMineInsertStatement(connection)) {

            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement buildMineInsertStatement(Connection connection) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "INSERT INTO `warehouse_minehistory` (`zone_id`, `mine_type`, `char_id`, `mine_guildID`, `mine_nationID`, `loc_x`, `loc_y`, `eventType`, `datetime`) VALUES(?,?,?,?,?,?,?,?,?)";

        outStatement = connection.prepareStatement(queryString);

        // Bind character data

        outStatement.setString(1, this.zoneHash);
        outStatement.setString(2, this.mineType);
        outStatement.setString(3, this.charHash);
        outStatement.setString(4, this.mineGuildHash);
        outStatement.setString(5, this.mineNationHash);

        outStatement.setFloat(6, this.locX);
        outStatement.setFloat(7, this.locY);
        outStatement.setString(8, this.eventType.name());
        outStatement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

        return outStatement;
    }
}
