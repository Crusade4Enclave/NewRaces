// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.archive;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import engine.gameManager.ConfigManager;
import engine.util.Hasher;
import org.pmw.tinylog.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import static engine.Enum.DataRecordType;

public class DataWarehouse implements Runnable {

    public static final Hasher hasher = new Hasher("Cthulhu Owns Joo");
    private static final LinkedBlockingQueue<DataRecord> recordQueue = new LinkedBlockingQueue<>();
    public static HikariDataSource connectionPool = null;
    public static HikariDataSource remoteConnectionPool = null;

    public DataWarehouse() {

        Logger.info("Configuring local Database Connection Pool...");

        configureConnectionPool();

        // If WarehousePush is disabled
        // then early exit

        if ( ConfigManager.MB_WORLD_WAREHOUSE_PUSH.getValue().equals("false")) {
            Logger.info("Warehouse Remote Connection disabled along with push");
            return;
        }

        Logger.info( "Configuring remote Database Connection Pool...");
        configureRemoteConnectionPool();

    }

    public static void bootStrap() {
        Thread warehousingThread;
        warehousingThread = new Thread(new DataWarehouse());

        warehousingThread.setName("DataWarehouse");
        warehousingThread.setPriority(Thread.NORM_PRIORITY - 1);
        warehousingThread.start();
    }

    public static void pushToWarehouse(DataRecord dataRecord) {

        DataWarehouse.recordQueue.add(dataRecord);
    }

    public static void writeHash(DataRecordType recordType, int uuid) {

        // Member variable declaration

        Connection connection = null;
        PreparedStatement statement = null;
        String queryString;
        String hashString;

        try {
            connection = DataWarehouse.connectionPool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (connection == null) {
            Logger.error("Null connection when writing zone hash.");
            return;
        }

        // Build query string

        switch (recordType) {
            case CHARACTER:
                queryString = "UPDATE `obj_character` SET hash = ? WHERE `UID` = ?";
                break;
            case GUILD:
                queryString = "UPDATE `obj_guild` SET hash = ? WHERE `UID` = ?";
                break;
            case ZONE:
                queryString = "UPDATE `obj_zone` SET hash = ? WHERE `UID` = ?";
                break;
            case CITY:
                queryString = "UPDATE `obj_city` SET hash = ? WHERE `UID` = ?";
                break;
            case REALM:
                queryString = "UPDATE `obj_realm` SET hash = ? WHERE `realmID` = ?";
                break;
            default:
                queryString = null;
                break;
        }

        hashString = hasher.encrypt(uuid);

        // Write this record to the warehouse

        try {

            statement = connection.prepareStatement(queryString);

            statement.setString(1, hashString);
            statement.setLong(2, uuid);
            statement.execute();
        } catch (SQLException e) {
            Logger.error("Error writing hash for uuid" + uuid + " of type " + recordType.name() + ' ' + e.toString());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean recordExists(DataRecordType recordType, int uuid) {

        // Member variable declaration

        Connection connection = null;
        PreparedStatement statement = null;
        String queryString;
        ResultSet resultSet;

        try {
            connection = DataWarehouse.connectionPool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (connection == null) {
            Logger.error("Null connection during char record lookup");
            return true;  // False positive here, so as not to try and write the record twice.
            // will refactor out once we write hashes to object tables
        }

        // Build query string

        switch (recordType) {
            case CHARACTER:
                queryString = "SELECT COUNT(*) from warehouse_characterhistory where char_id = ?";
                break;
            case GUILD:
                queryString = "SELECT COUNT(*) from warehouse_guildhistory where guild_id = ?";
                break;
            case CITY:
                queryString = "SELECT COUNT(*) from warehouse_cityhistory where city_id = ?";
                break;
            case REALM:
                queryString = "SELECT COUNT(*) from warehouse_realmhistory where realm_id = ?";
                break;
            case BANE:
                queryString = "SELECT COUNT(*) from warehouse_banehistory where city_id = ? AND `resolution` = 'PENDING'";
                break;
            case ZONE: // Does not really exist but enum acts as a proxy for hash lookup
            case MINE: // Does not really exist but enum acts as a proxy for hash lookup
            default:
                queryString = null;
                break;
        }

        try {
            statement = connection.prepareStatement(queryString);
            statement.setString(1, DataWarehouse.hasher.encrypt(uuid));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getInt("COUNT(*)") > 0;
            }

        } catch (SQLException e) {
            Logger.error("Error in record lookup for " + recordType.name() + " of uuid:" + uuid + e.toString());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void run() {

        // Working variable set

        DataRecord dataRecord;
        PvpRecord pvpRecord;
        GuildRecord guildRecord;
        CharacterRecord characterRecord;
        CityRecord cityRecord;
        BaneRecord baneRecord;
        RealmRecord realmRecord;
        MineRecord mineRecord;

        Logger.info( "DataWarehouse is running.");

        while (true) {

            dataRecord = null;
            pvpRecord = null;
            guildRecord = null;
            characterRecord = null;
            cityRecord = null;
            baneRecord = null;
            realmRecord = null;
            mineRecord = null;

            try {
                dataRecord = recordQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Write record to appropriate warehousing table

            if (dataRecord != null) {

                switch (dataRecord.recordType) {
                    case PVP:
                        pvpRecord = (PvpRecord) dataRecord;
                        pvpRecord.write();
                        pvpRecord.release();
                        break;
                    case CHARACTER:
                        characterRecord = (CharacterRecord) dataRecord;
                        characterRecord.write();
                        characterRecord.release();
                        break;
                    case GUILD:
                        guildRecord = (GuildRecord) dataRecord;
                        guildRecord.write();
                        guildRecord.release();
                        break;
                    case CITY:
                        cityRecord = (CityRecord) dataRecord;
                        cityRecord.write();
                        cityRecord.release();
                        break;
                    case BANE:
                        baneRecord = (BaneRecord) dataRecord;
                        baneRecord.write();
                        baneRecord.release();
                        break;
                    case REALM:
                        realmRecord = (RealmRecord) dataRecord;
                        realmRecord.write();
                        realmRecord.release();
                        break;
                    case MINE:
                        mineRecord = (MineRecord) dataRecord;
                        mineRecord.write();
                        mineRecord.release();
                        break;
                    default:
                        Logger.error( "Unhandled record type");
                        break;

                } // end switch
            }
        }
    }

    private static void configureConnectionPool() {

        HikariConfig config = new HikariConfig();

        config.setMaximumPoolSize(10);

        config.setJdbcUrl("jdbc:mysql://" +  ConfigManager.MB_DATABASE_ADDRESS.getValue() +
                ":" +  ConfigManager.MB_DATABASE_PORT.getValue() + "/" +
                ConfigManager.MB_DATABASE_NAME.getValue());
        config.setUsername(ConfigManager.MB_DATABASE_USER.getValue());
        config.setPassword( ConfigManager.MB_DATABASE_PASS.getValue());
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        connectionPool = new HikariDataSource(config); // setup the connection pool

        Logger.info("Local warehouse database connection configured");
    }

    private static void configureRemoteConnectionPool() {

        HikariConfig config = new HikariConfig();

        config.setMaximumPoolSize(1); // Only the server talks to remote, so yeah.
        config.setJdbcUrl(ConfigManager.MB_WAREHOUSE_ADDR.getValue());
        config.setUsername(ConfigManager.MB_WAREHOUSE_USER.getValue());
        config.setPassword(ConfigManager.MB_WAREHOUSE_PASS.getValue());
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        remoteConnectionPool = new HikariDataSource(config); // setup the connection pool

        Logger.info("remote warehouse connection configured");
    }

}
