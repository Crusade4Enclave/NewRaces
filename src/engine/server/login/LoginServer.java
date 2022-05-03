// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.server.login;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import engine.Enum;
import engine.gameManager.*;
import engine.job.JobScheduler;
import engine.jobs.CSessionCleanupJob;
import engine.net.Network;
import engine.net.client.ClientConnection;
import engine.net.client.ClientConnectionManager;
import engine.net.client.Protocol;
import engine.net.client.msg.login.ServerStatusMsg;
import engine.net.client.msg.login.VersionInfoMsg;
import engine.objects.*;
import engine.server.MBServerStatics;
import engine.util.ByteUtils;
import engine.util.ThreadUtils;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.StartupPolicy;
import org.pmw.tinylog.writers.RollingFileWriter;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Iterator;

import static java.lang.System.exit;

public class LoginServer {


    // Instance variables

    private VersionInfoMsg versionInfoMessage;
    public static HikariDataSource connectionPool = null;
    public static int population = 0;
    public static boolean worldServerRunning = false;
    public static boolean loginServerRunning = false;

    public static ServerStatusMsg serverStatusMsg = new ServerStatusMsg(0, (byte) 1);

    // This is the entrypoint for the MagicBane Login Server when
    // it is executed by the command line scripts.  The fun begins here!

    public static void main(String[] args) {

        LoginServer loginServer;

        // Initialize TinyLog logger with our own format

        Configurator.defaultConfig()
                .addWriter(new RollingFileWriter("logs/login/login.txt", 30, new TimestampLabeler(), new StartupPolicy()))
                .level(Level.DEBUG)
                .formatPattern("{level} {date:yyyy-MM-dd HH:mm:ss.SSS} [{thread}] {class}.{method}({line}) : {message}")
                .activate();

        try {

            // Configure the the Login Server

            loginServer = new LoginServer();
            ConfigManager.loginServer = loginServer;
            ConfigManager.handler = new LoginServerMsgHandler(loginServer);

            ConfigManager.serverType = Enum.ServerType.LOGINSERVER;

            if (ConfigManager.init() == false) {
              Logger.error("ABORT! Missing config entry!");
              return;
            }

            // Start the Login Server

            loginServer.init();
            loginServer.exec();

            exit(0);

        } catch (Exception e) {
            Logger.error(e);
            e.printStackTrace();
            exit(1);
        }
    }

    // Mainline execution loop for the login server.

    private void exec() {


        LocalDateTime nextCacheTime = LocalDateTime.now();
        LocalDateTime nextServerTime = LocalDateTime.now();
        LocalDateTime nextDatabaseTime = LocalDateTime.now();

        loginServerRunning = true;

        while (true) {

            // Invalidate cache for players driven by forum
            // and stored procedure forum_link_pass()

            try {

                // Run cache routine right away if requested.

                File cacheFile = new File("cacheInvalid");


                if (cacheFile.exists() == true) {
                    nextCacheTime = LocalDateTime.now();
                    Files.deleteIfExists(Paths.get("cacheInvalid"));
                }

                if (LocalDateTime.now().isAfter(nextCacheTime)) {
                    invalidateCacheList();
                    nextCacheTime = LocalDateTime.now().plusSeconds(30);
                }

                if (LocalDateTime.now().isAfter(nextServerTime)) {
                    checkServerHealth();
                    nextServerTime = LocalDateTime.now().plusSeconds(1);
                }

                if (LocalDateTime.now().isAfter(nextDatabaseTime)) {
                    String pop = SimulationManager.getPopulationString();
                    Logger.info("Keepalive: " + pop);
                    nextDatabaseTime = LocalDateTime.now().plusMinutes(30);
                }

                ThreadUtils.sleep(100);
            } catch (Exception e) {
                Logger.error(e);
                e.printStackTrace();
            }
        }
    }

    // Constructor

    public LoginServer() {

    }

    private boolean init() {

        // Initialize Application Protocol

        Protocol.initProtocolLookup();

        // Configure the VersionInfoMsgs:

        this.versionInfoMessage = new VersionInfoMsg(MBServerStatics.PCMajorVer,
                MBServerStatics.PCMinorVer);

        Logger.info("Initializing Database Pool");
        initDatabasePool();

        Logger.info("Initializing Database layer");
        initDatabaseLayer();

        Logger.info("Initializing Network");
        Network.init();

        Logger.info("Initializing Client Connection Manager");
        initClientConnectionManager();

        // instantiate AccountManager
        Logger.info("Initializing SessionManager.");

        // Sets cross server behavior
        SessionManager.setCrossServerBehavior(0);

        // activate powers manager
        Logger.info("Initializing PowersManager.");
        PowersManager.initPowersManager(false);

        RuneBaseAttribute.LoadAllAttributes();
        RuneBase.LoadAllRuneBases();
        BaseClass.LoadAllBaseClasses();
        Race.loadAllRaces();
        RuneBaseEffect.LoadRuneBaseEffects();

        Logger.info("Initializing Blueprint data.");
        Blueprint.loadAllBlueprints();

        Logger.info("Loading Kits");
        DbManager.KitQueries.GET_ALL_KITS();

        Logger.info("Initializing ItemBase data.");
        ItemBase.loadAllItemBases();

        Logger.info("Initializing Race data");
        Enum.RaceType.initRaceTypeTables();
        Race.loadAllRaces();

        Logger.info("Initializing Errant Guild");
        Guild.CreateErrantGuild();

        Logger.info("Loading All Guilds");
        DbManager.GuildQueries.GET_ALL_GUILDS();


        Logger.info("***Boot Successful***");
        return true;
    }

    private boolean initDatabaseLayer() {

        // Try starting a GOM <-> DB connection.
        try {

            Logger.info("Configuring GameObjectManager to use Database: '"
                    + ConfigManager.MB_DATABASE_NAME.getValue() + "' on "
                    + ConfigManager.MB_DATABASE_ADDRESS.getValue() + ':'
                    + ConfigManager.MB_DATABASE_PORT.getValue());

            DbManager.configureDatabaseLayer();

        } catch (Exception e) {
            Logger.error(e.getMessage());
            return false;
        }

        PreparedStatementShared.submitPreparedStatementsCleaningJob();

        if (MBServerStatics.DB_DEBUGGING_ON_BY_DEFAULT) {
            PreparedStatementShared.enableDebugging();
        }

        return true;
    }

    public void removeClient(ClientConnection conn) {
        if (conn == null) {
            Logger.info(
                    "ClientConnection null in removeClient.");
            return;
        }
        String key = ByteUtils.byteArrayToSafeStringHex(conn
                .getSecretKeyBytes());

        CSessionCleanupJob cscj = new CSessionCleanupJob(key);

        JobScheduler.getInstance().scheduleJob(cscj,
                MBServerStatics.SESSION_CLEANUP_TIMER_MS);
    }

    private void initClientConnectionManager() {

        try {

            String name = ConfigManager.MB_WORLD_NAME.getValue();


            if (ConfigManager.MB_PUBLIC_ADDR.getValue().equals("0.0.0.0")) {

                // Autoconfigure IP address for use in worldserver response
                // message.

                URL whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
                ConfigManager.MB_PUBLIC_ADDR.setValue(in.readLine());
            }

            Logger.info("Public address: " + ConfigManager.MB_PUBLIC_ADDR.getValue());
            Logger.info("Magicbane bind config: " + ConfigManager.MB_BIND_ADDR.getValue() + ":" + ConfigManager.MB_LOGIN_PORT.getValue());

            InetAddress addy = InetAddress.getByName(ConfigManager.MB_BIND_ADDR.getValue());
            int port = Integer.parseInt(ConfigManager.MB_LOGIN_PORT.getValue());

            ClientConnectionManager connectionManager = new ClientConnectionManager(name + ".ClientConnMan", addy,
                    port);
            connectionManager.startup();

        } catch (IOException e) {
            Logger.error(e.toString());
        }
    }
    /*
     * message handlers (relay)
     */

    // ==============================
    // Support Functions
    // ==============================

    public VersionInfoMsg getDefaultVersionInfo() {
        return versionInfoMessage;
    }

    //this updates a server being up or down without resending the entire char select screen.
    public void updateServersForAll(boolean isRunning) {

        try {

            Iterator<ClientConnection> i = SessionManager.getAllActiveClientConnections().iterator();

            while (i.hasNext()) {

                ClientConnection clientConnection = i.next();

                if (clientConnection == null)
                    continue;

                Account ac = clientConnection.getAccount();

                if (ac == null)
                    continue;

                boolean isUp = isRunning;


                if (MBServerStatics.worldAccessLevel.ordinal() > ac.status.ordinal())
                    isUp = false;

                LoginServer.serverStatusMsg.setServerID(MBServerStatics.worldMapID);
                LoginServer.serverStatusMsg.setIsUp(isUp ? (byte) 1 : (byte) 0);
                clientConnection.sendMsg(LoginServer.serverStatusMsg);
            }
        } catch (Exception e) {
            Logger.error(e);
            e.printStackTrace();
        }
    }

    public void checkServerHealth() {

        // Check if worldserver is running

        if (!isPortInUse(Integer.parseInt(ConfigManager.MB_WORLD_PORT.getValue()))) {
            worldServerRunning = false;
            population = 0;
            updateServersForAll(worldServerRunning);
            return;
        }

        // Worldserver is running and writes a polling file.
        // Read the current population count from the server and
        // update player displays accordingly.

        worldServerRunning = true;
        population = readPopulationFile();
        updateServersForAll(worldServerRunning);

    }

    private void initDatabasePool() {

        HikariConfig config = new HikariConfig();

        config.setMaximumPoolSize(33); // (16 cores 1 spindle)

        config.setJdbcUrl("jdbc:mysql://" + ConfigManager.MB_DATABASE_ADDRESS.getValue() +
                ":" + ConfigManager.MB_DATABASE_PORT.getValue() + "/" +
                      ConfigManager.MB_DATABASE_NAME.getValue());
        config.setUsername(ConfigManager.MB_DATABASE_USER.getValue());
        config.setPassword(ConfigManager.MB_DATABASE_PASS.getValue());
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        connectionPool = new HikariDataSource(config); // setup the connection pool

        Logger.info("local database connection configured");
    }

    public void invalidateCacheList() {

        int objectUUID;
        String objectType;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `login_cachelist`");
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                objectUUID = rs.getInt("UID");
                objectType = rs.getString("type");

                Logger.info("INVALIDATED : " +  objectType + " UUID: " + objectUUID);

                switch (objectType) {

                    case "account":
                        DbManager.removeFromCache(Enum.GameObjectType.Account, objectUUID);
                        break;
                    case "character":
                        DbManager.removeFromCache(Enum.GameObjectType.PlayerCharacter, objectUUID);
                        PlayerCharacter player = (PlayerCharacter) DbManager.getObject(Enum.GameObjectType.PlayerCharacter, objectUUID);
                        PlayerCharacter.initializePlayer(player);
                        player.getAccount().characterMap.replace(player.getObjectUUID(), player);
                        Logger.info("Player active state is : " + player.isActive());
                        break;
                }

            }

        } catch (SQLException e) {
            Logger.info(e.toString());
        }

        // clear the db table

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM `login_cachelist`")) {

            statement.execute();

        } catch (SQLException e) {
            Logger.info(e.toString());
        }


    }

    public static boolean getActiveBaneQuery(PlayerCharacter playerCharacter) {

        boolean outStatus = false;

        // char has never logged on so cannot have dropped a bane

        if (playerCharacter.getHash() == null)
            return outStatus;

        // query data warehouse for unresolved bane with this character

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = buildQueryActiveBaneStatement(connection, playerCharacter);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                outStatus = true;
            }

        } catch (SQLException e) {
            Logger.error(e.toString());
        }

        return outStatus;
    }

    private static PreparedStatement buildQueryActiveBaneStatement(Connection connection, PlayerCharacter playerCharacter) throws SQLException {
        PreparedStatement outStatement;
        String queryString = "SELECT `city_id` FROM `warehouse_banehistory` WHERE `char_id` = ? AND `RESOLUTION` = 'PENDING'";
        outStatement = connection.prepareStatement(queryString);
        outStatement.setString(1, playerCharacter.getHash());
        return outStatement;

    }

    public static boolean isPortInUse(int port) {

        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "lsof -i tcp:" + port + " | tail -n +2 | awk '{print $2}'");
        builder.redirectErrorStream(true);
        Process process = null;
        String line = null;
        boolean portInUse = false;

        try {
            process = builder.start();

            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                portInUse = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return portInUse;
    }

    private int readPopulationFile() {

        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "cat " + MBServerStatics.DEFAULT_DATA_DIR + ConfigManager.MB_WORLD_NAME.getValue().replaceAll("'","") + ".pop");
        builder.redirectErrorStream(true);
        Process process = null;
        String line = null;
        int population = 0;

        try {

            process = builder.start();

            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                population = Integer.parseInt(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        return population;
    }

}
