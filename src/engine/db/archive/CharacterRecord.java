// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.archive;

import engine.Enum;
import engine.objects.Guild;
import engine.objects.PlayerCharacter;
import engine.workthreads.WarehousePushThread;
import org.pmw.tinylog.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * This class warehouses character creation events.  It also tracks
 * updates to summary kills/death data and their promotion class.
 */
public class CharacterRecord extends DataRecord {

    // Local object pool for class

    private static final LinkedBlockingQueue<CharacterRecord> recordPool = new LinkedBlockingQueue<>();

    private PlayerCharacter player;

    private CharacterRecord(PlayerCharacter player) {
        this.recordType = Enum.DataRecordType.CHARACTER;
        this.player = player;
    }

    public static CharacterRecord borrow(PlayerCharacter player) {
        CharacterRecord characterRecord;

        characterRecord = recordPool.poll();

        if (characterRecord == null) {
            characterRecord = new CharacterRecord(player);
        }
        else {
            characterRecord.recordType = Enum.DataRecordType.CHARACTER;
            characterRecord.player = player;

        }

        return characterRecord;
    }

    private static PreparedStatement buildCharacterInsertStatement(Connection connection, PlayerCharacter player) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "INSERT INTO `warehouse_characterhistory` (`char_id`, `char_fname`, `char_lname`, `baseClass`, `race`, `promoteClass`, `startingGuild`, `datetime`) VALUES(?,?,?,?,?,?,?,?)";
        Guild charGuild;

        outStatement = connection.prepareStatement(queryString);

        charGuild = player.getGuild();

        // Bind character data

        outStatement.setString(1, DataWarehouse.hasher.encrypt(player.getObjectUUID()));
        outStatement.setString(2, player.getFirstName());
        outStatement.setString(3, player.getLastName());
        outStatement.setInt(4, player.getBaseClassID());
        outStatement.setInt(5, player.getRaceID());
        outStatement.setInt(6, player.getPromotionClassID());
        outStatement.setString(7, DataWarehouse.hasher.encrypt(charGuild.getObjectUUID()));
        outStatement.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

        return outStatement;
    }

    public static PreparedStatement buildCharacterPushStatement(Connection connection, ResultSet rs) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "INSERT INTO `warehouse_characterhistory` (`event_number`, `char_id`, `char_fname`, `char_lname`, `baseClass`, `race`, `promoteClass`, `startingGuild`, `datetime`) VALUES(?,?,?,?,?,?,?,?,?)";

        outStatement = connection.prepareStatement(queryString);

        // Bind record data

        outStatement.setInt(1, rs.getInt("event_number"));
        outStatement.setString(2, rs.getString("char_id"));
        outStatement.setString(3, rs.getString("char_fname"));
        outStatement.setString(4, rs.getString("char_lname"));
        outStatement.setInt(5, rs.getInt("baseClass"));
        outStatement.setInt(6, rs.getInt("race"));
        outStatement.setInt(7, rs.getInt("promoteClass"));
        outStatement.setString(8, rs.getString("startingGuild"));
        outStatement.setTimestamp(9, rs.getTimestamp("datetime"));
        return outStatement;
    }

    public static PreparedStatement buildCharacterQueryStatement(Connection connection) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "SELECT * FROM `warehouse_characterhistory` WHERE `event_number` > ?";
        outStatement = connection.prepareStatement(queryString);
        outStatement.setInt(1, WarehousePushThread.charIndex);
        return outStatement;
    }

    public static void advanceKillCounter(PlayerCharacter player) {

        try (Connection connection = DataWarehouse.connectionPool.getConnection();
             PreparedStatement statement = buildKillCounterStatement(connection, player)) {

            statement.execute();

        } catch (SQLException e) {
            Logger.error( e.toString());
        }

    }

    private static PreparedStatement buildKillCounterStatement(Connection connection, PlayerCharacter player) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "UPDATE `warehouse_characterhistory` SET `kills` = `kills` +1, `dirty` = 1 WHERE `char_id` = ?";

        if (player == null)
            return outStatement;

        outStatement = connection.prepareStatement(queryString);
        outStatement.setString(1, player.getHash());

        return outStatement;
    }

    public static void advanceDeathCounter(PlayerCharacter player) {

        try (Connection connection = DataWarehouse.connectionPool.getConnection();
             PreparedStatement statement = buildDeathCounterStatement(connection, player)) {

            statement.execute();

        } catch (SQLException e) {
            Logger.error( e.toString());
        }

    }

    private static PreparedStatement buildDeathCounterStatement(Connection connection, PlayerCharacter player) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "UPDATE `warehouse_characterhistory` SET `deaths` = `deaths` +1, `dirty` = 1 WHERE `char_id` = ?";

        if (player == null)
            return outStatement;

        outStatement = connection.prepareStatement(queryString);
        outStatement.setString(1, player.getHash());

        return outStatement;
    }

    public static void updatePromotionClass(PlayerCharacter player) {

        try (Connection connection = DataWarehouse.connectionPool.getConnection();
             PreparedStatement statement = buildUpdatePromotionStatement(connection, player)) {

            statement.execute();

        } catch (SQLException e) {
            Logger.error( e.toString());
        }

    }

    private static PreparedStatement buildUpdatePromotionStatement(Connection connection, PlayerCharacter player) throws SQLException {

        PreparedStatement outStatement = null;
        String queryString = "UPDATE `warehouse_characterhistory` SET `promoteClass` = ?, `dirty` = 1 WHERE `char_id` = ?";

        if (player == null)
            return outStatement;

        outStatement = connection.prepareStatement(queryString, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        outStatement.setInt(1, player.getPromotionClassID());
        outStatement.setString(2, player.getHash());

        return outStatement;
    }

    public static void updateDirtyRecords() {

        String queryString = "SELECT * FROM `warehouse_characterhistory` where `dirty` = 1";

        // Reset character delta

        WarehousePushThread.charDelta = 0;

        try (Connection localConnection = DataWarehouse.connectionPool.getConnection();
             PreparedStatement statement = localConnection.prepareStatement(queryString, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); // Make this an updatable result set as we'll reset the dirty flag as we go along
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                // Only update the index and dirty flag
                // if the remote database update succeeded

                if (updateDirtyRecord(rs) == true)
                    WarehousePushThread.charDelta++;
                else
                    continue;

                // Reset the dirty flag in the local database

                rs.updateInt("dirty", 0);
                rs.updateRow();
            }

        } catch (SQLException e) {
            Logger.error(e.toString());
        }
    }

    private static boolean updateDirtyRecord(ResultSet rs) {

        try (Connection remoteConnection = DataWarehouse.remoteConnectionPool.getConnection();
             PreparedStatement statement = buildUpdateDirtyStatement(remoteConnection, rs)) {

            statement.execute();
            return true;
        } catch (SQLException e) {
            Logger.error(e.toString());
            return false;
        }
    }

    private static PreparedStatement buildUpdateDirtyStatement(Connection connection, ResultSet rs) throws SQLException {

        PreparedStatement outStatement;
        String queryString = "UPDATE `warehouse_characterhistory` SET `promoteClass` = ?, `kills` = ?, `deaths` = ? WHERE `char_id` = ?";

        outStatement = connection.prepareStatement(queryString);

        // Bind record data

        outStatement.setInt(1, rs.getInt("promoteClass"));
        outStatement.setInt(2, rs.getInt("kills"));
        outStatement.setInt(3, rs.getInt("deaths"));
        outStatement.setString(4, rs.getString("char_id"));

        return outStatement;
    }

    void reset() {
        this.player = null;
    }

    public void release() {
        this.reset();
        recordPool.add(this);
    }

    public void write() {

        try (Connection connection = DataWarehouse.connectionPool.getConnection();
             PreparedStatement statement = buildCharacterInsertStatement(connection, this.player)) {

            statement.execute();

        } catch (SQLException e) {
            Logger.error( "Error writing character record " + e.toString());

        }
    }

}







