// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.server.world;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.Enum.DispatchChannel;
import engine.Enum.MinionType;
import engine.Enum.SupportMsgType;
import engine.InterestManagement.HeightMap;
import engine.InterestManagement.RealmMap;
import engine.InterestManagement.WorldGrid;
import engine.ai.MobileFSMManager;
import engine.db.archive.DataWarehouse;
import engine.exception.MsgSendException;
import engine.gameManager.*;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.LogoutCharacterJob;
import engine.loot.LootManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.ItemProductionManager;
import engine.net.Network;
import engine.net.client.ClientConnection;
import engine.net.client.ClientConnectionManager;
import engine.net.client.ClientMessagePump;
import engine.net.client.Protocol;
import engine.net.client.msg.RefinerScreenMsg;
import engine.net.client.msg.TrainerInfoMsg;
import engine.net.client.msg.UpdateStateMsg;
import engine.net.client.msg.chat.ChatSystemMsg;
import engine.objects.*;
import engine.server.MBServerStatics;
import engine.util.ThreadUtils;
import engine.workthreads.DisconnectTrashTask;
import engine.workthreads.HourlyJobThread;
import engine.workthreads.PurgeOprhans;
import engine.workthreads.WarehousePushThread;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.StartupPolicy;
import org.pmw.tinylog.writers.RollingFileWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import static engine.gameManager.SimulationManager.SERVERHEARTBEAT;
import static java.lang.System.exit;

public class WorldServer {

	private static LocalDateTime bootTime = LocalDateTime.now();
	private static long lastHZChange = System.currentTimeMillis();
	public boolean isRunning = false;

	// Member variable declaration

	public static HashMap<Integer,HashMap<Integer,ArrayList<Integer>>> ZoneFidelityMobRunes = new HashMap<>();

	public WorldServer() {
		super();
	}

	public static void main(String[] args) {

		WorldServer worldServer;

		// Configure TinyLogger
		Configurator.defaultConfig()
				.addWriter(new RollingFileWriter("logs/world/world.txt", 30, new TimestampLabeler(), new StartupPolicy()))
				.level(Level.DEBUG)
				.formatPattern("{level} {date:yyyy-MM-dd HH:mm:ss.SSS} [{thread}] {class}.{method}({line}) : {message}")
				.writingThread("main", 2)
				.activate();

		if (ConfigManager.init() == false) {
			Logger.error("ABORT! Missing config entry!");
			return;
		}

		try {
			
			worldServer = new WorldServer();

			ConfigManager.serverType = Enum.ServerType.WORLDSERVER;
			ConfigManager.worldServer = worldServer;
			ConfigManager.handler = new ClientMessagePump(worldServer);

			worldServer.init();
			
			int retVal = worldServer.exec();

			if (retVal != 0)
				Logger.error(
						".exec() returned value: '" + retVal);
			exit(retVal);

		} catch (Exception e) {
			Logger.error(e.getMessage());
			exit(1);
		}
	}

	public static long getLastHZChange() {
		return lastHZChange;
	}

	public static void setLastHZChange(long lastChange) {
		lastHZChange = lastChange;
	}

	public static void trainerInfo(TrainerInfoMsg msg, ClientConnection origin) {

		NPC npc = NPC.getFromCache(msg.getObjectID());
		float sellPercent = 1;

		if (npc != null){
			
			if (origin.getPlayerCharacter() != null)
				sellPercent = npc.getSellPercent(origin.getPlayerCharacter());
			else
				sellPercent = npc.getSellPercent();
			
			msg.setTrainPercent(sellPercent); //TrainMsg.getTrainPercent(npc));
		}

		Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

	}

	public static void refinerScreen(RefinerScreenMsg msg, ClientConnection origin)
			throws MsgSendException {

		NPC npc = NPC.getFromCache(msg.getNpcID());

		if (npc != null)
			msg.setUnknown02(0); //cost to refine?

		Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	}

	public static void shutdown() {
		exit(1);
	}

	public static String getUptimeString() {
		String outString = null;
		java.time.Duration uptimeDuration;
		String newLine = System.getProperty("line.separator");

		try {
			outString = "[LUA_UPTIME()]" + newLine;
			uptimeDuration = java.time.Duration.between(LocalDateTime.now(), WorldServer.bootTime);
			long uptimeSeconds = Math.abs(uptimeDuration.getSeconds());
			String uptime =   String.format("%d hours %02d minutes %02d seconds", uptimeSeconds / 3600, (uptimeSeconds % 3600) / 60, (uptimeSeconds % 60));
			outString += "uptime: " + uptime;
			outString += " pop: " + SessionManager.getActivePlayerCharacterCount() + " max pop: " + SessionManager._maxPopulation;
		} catch (Exception e) {
			Logger.error("Failed to build string");
		}
		return outString;
	}

	private int exec() {

		LocalDateTime nextHeartbeatTime = LocalDateTime.now();
		LocalDateTime nextPopulationFileTime = LocalDateTime.now();
		LocalDateTime nextFlashTrashCheckTime = LocalDateTime.now();
		LocalDateTime nextHourlyJobTime = LocalDateTime.now().withMinute(0).withSecond(0).plusHours(1);
		LocalDateTime nextWareHousePushTime = LocalDateTime.now();;

		// Begin execution of main game loop

		this.isRunning = true;

		while (true) {

			if (LocalDateTime.now().isAfter(nextHeartbeatTime)) {
				SERVERHEARTBEAT.tick();
				nextHeartbeatTime = LocalDateTime.now().plusNanos(50000000);
			}

			if (LocalDateTime.now().isAfter(nextPopulationFileTime)) {
				writePopulationFile();
				nextPopulationFileTime = LocalDateTime.now().plusMinutes(1);
			}

			if (LocalDateTime.now().isAfter(nextFlashTrashCheckTime)) {
				processFlashFile();
				processTrashFile();
				nextFlashTrashCheckTime = LocalDateTime.now().plusSeconds(15);
			}

			if (LocalDateTime.now().isAfter(nextHourlyJobTime)) {
				Thread hourlyJobThread = new Thread(new HourlyJobThread());
				hourlyJobThread.setName("hourlyJob");
				hourlyJobThread.start();
				nextHourlyJobTime = LocalDateTime.now().withMinute(0).withSecond(0).plusHours(1);
			}

			if (LocalDateTime.now().isAfter(nextWareHousePushTime)) {
				Thread warehousePushThread = new Thread(new WarehousePushThread());
				warehousePushThread.setName("warehousePush");
				warehousePushThread.start();
				nextWareHousePushTime = LocalDateTime.now().plusMinutes(15);
			}

			ThreadUtils.sleep(50);
		}
	}

	private void initClientConnectionManager() {

		try {

			String name = ConfigManager.MB_WORLD_NAME.getValue();

			Logger.info("Magicbane network config: " + ConfigManager.MB_BIND_ADDR.getValue() + ":" + ConfigManager.MB_WORLD_PORT.getValue());

			InetAddress addy = InetAddress.getByName(ConfigManager.MB_BIND_ADDR.getValue());
			int port = Integer.parseInt(ConfigManager.MB_WORLD_PORT.getValue());

			ClientConnectionManager connectionManager =  new ClientConnectionManager(name + ".ClientConnMan", addy,
					port);
			connectionManager.startup();

		} catch (IOException e) {
			Logger.error("Exception while creating a ClientConnectionManager.");
		}
	}

	private boolean init() {

		Logger.info("MAGICBANE SERVER GREETING:");
		Logger.info(ConfigManager.MB_WORLD_GREETING.getValue());

		Logger.info("Initialize network protocol");
		Protocol.initProtocolLookup();

		Logger.info("Initialize database layer");
		initDatabaselayer();

		Logger.info("Setting cross server session behavior");
		SessionManager.setCrossServerBehavior(1); // Sets cross server behavior

		Logger.info("Starting Item Production thread");
		ItemProductionManager.ITEMPRODUCTIONMANAGER.startMessagePump();

		Logger.info("Initializing Errant Guild");
		Guild.CreateErrantGuild();

		Logger.info("Initializing PowersManager.");
		// activate powers manager
		PowersManager.initPowersManager(true);
		
		Logger.info("Initializing granted Skills for Runes");
		DbManager.SkillsBaseQueries.LOAD_ALL_RUNE_SKILLS();
		
		Logger.info("Initializing Player Friends");
		DbManager.PlayerCharacterQueries.LOAD_PLAYER_FRIENDS();
		
		Logger.info("Initializing NPC Profits");
		DbManager.NPCQueries.LOAD_NPC_PROFITS();
		
		Logger.info("Initializing MeshBounds");
		MeshBounds.InitializeBuildingBounds();

		// Load ItemBases
		Logger.info("Loading ItemBases");
		ItemBase.loadAllItemBases();
		
		Logger.info("Loading PromotionClasses");
		DbManager.PromotionQueries.GET_ALL_PROMOTIONS();

		Logger.info("Loading NPC and Mob Equipment Sets");
		EquipmentSetEntry.LoadAllEquipmentSets();

		Logger.info("Loading Gold Loot for Mobbases");
		MobbaseGoldEntry.LoadMobbaseGold();

		Logger.info("Loading fidelity mob runes.");
		DbManager.MobQueries.LOAD_RUNES_FOR_FIDELITY_MOBS();

		//load lootTable
		Logger.info("Loading Loot Tables");
		LootTable.populateLootTables();

		// Load new loot system
		Logger.info("Loading SuperLoot Tables");
		LootManager.loadLootData();
		RuneBaseAttribute.LoadAllAttributes();
		RuneBase.LoadAllRuneBases();
		BaseClass.LoadAllBaseClasses();
		Race.loadAllRaces();
		RuneBaseEffect.LoadRuneBaseEffects();

		Logger.info("Loading MobBases.");
		DbManager.MobBaseQueries.GET_ALL_MOBBASES();

		//load item enchantment values
		DbManager.LootQueries.LOAD_ENCHANT_VALUES();

		//initialize realms
		Logger.info("Loading Realms");
		Realm.loadAllRealms();

		Logger.info("Loading Kits");
		DbManager.KitQueries.GET_ALL_KITS();
		
		Logger.info("Loading World Grid");
		WorldGrid.InitializeGridObjects();
		
		Logger.info("Starting InterestManager.");
		WorldGrid.startLoadJob();
		
		
		Logger.info("Loading Spaital Hash");
		RealmMap.loadRealmImageMap();

		DbManager.MobBaseQueries.SET_AI_DEFAULTS();

		Logger.info("Loading blueprint data.");
		StaticColliders.loadAllStaticColliders();
		BuildingRegions.loadAllStaticColliders();
		Blueprint.loadAllDoorNumbers();
		Blueprint.loadAllBlueprints();

		Logger.info("Loading Special Loot For Mobs");
		DbManager.SpecialLootQueries.GenerateSpecialLoot();

		Logger.info("Initializing Heightmap data");
		HeightMap.loadAlHeightMaps();

		Logger.info("Loading Race data");
		Enum.RaceType.initRaceTypeTables();
		Race.loadAllRaces();

		Logger.info("Loading building mountpoint data.");
		BuildingLocation.loadAllLocations();

		// Starting before loading of structures/guilds/characters
		// so the database connections are available to write
		// historical data.

		Logger.info("Starting Data Warehouse");
		DataWarehouse.bootStrap();

		Logger.info("Loading Minion Bases.");
		MinionType.InitializeMinions();

		Logger.info("Loading Support Types");
		SupportMsgType.InitializeSupportMsgType();

		//Load Buildings, Mobs and NPCs for server

		getWorldBuildingsMobsNPCs();

		// Configure realms for serialization
		// Doing this after the world is loaded

		Logger.info("Configuring realm serialization data");

		try{
			Realm.configureAllRealms();
		}catch(Exception e){
			Logger.error( e.getMessage());
		}

		Logger.info("Loading Mine data.");
		//DbManager.MineQueries.syncMineWindowsWithToday();
		Mine.loadAllMines();

		Logger.info("Loading Shrine data.");
		DbManager.ShrineQueries.LOAD_ALL_SHRINES();

		Logger.info("Initialize Resource type lookup");
		Enum.ResourceType.InitializeResourceTypes();

		Logger.info("Loading Warehouse data.");
		DbManager.WarehouseQueries.LOAD_ALL_WAREHOUSES();

		Logger.info("Loading Runegate data.");
		Runegate.loadAllRunegates();

		Logger.info("Loading Pirate Names.");
		NPC.loadAllPirateNames();

		Logger.info("Loading Max Skills for Trainers");
		DbManager.SkillsBaseQueries.LOAD_ALL_MAX_SKILLS_FOR_CONTRACT();

		//pick a startup Hotzone
		ZoneManager.generateAndSetRandomHotzone();
		
		Logger.info("Loading All Players from database to Server Cache");
		long start = System.currentTimeMillis();
		try{
			DbManager.PlayerCharacterQueries.GET_ALL_CHARACTERS();
		}catch(Exception e){
			e.printStackTrace();
		}
	
		long end = System.currentTimeMillis();
		Logger.info("Loading All Players took "  + (end - start) + " ms.");
		
		ItemProductionManager.ITEMPRODUCTIONMANAGER.initialize();

		Logger.info("Loading Player Heraldries");
		DbManager.PlayerCharacterQueries.LOAD_HERALDY();
		
		Logger.info("Running Heraldry Audit for Deleted Players");
		Heraldry.AuditHeraldry();

		if (ZoneManager.getHotZone() != null)
			WorldServer.setLastHZChange(System.currentTimeMillis());

		Logger.info("Starting Mobile AI FSM");
		MobileFSMManager.getInstance();


		for (Zone zone : ZoneManager.getAllZones()) {
			if (zone.getHeightMap() != null) {
				if (zone.getHeightMap().getBucketWidthX() == 0) {
					System.out.println("Zone load num: " + zone.getLoadNum() + " has no bucket width");
				}
			}
		}

		Logger.info("World data loaded.");

		//set default accesslevel for server  *** Refactor who two separate variables?
		MBServerStatics.accessLevel = MBServerStatics.worldAccessLevel;
		Logger.info("Default access level set to " + MBServerStatics.accessLevel);

		Logger.info("Initializing Network");
		Network.init();

		Logger.info("Initializing Client Connection Manager");
		initClientConnectionManager();

		Logger.info("Starting message pumps");
		DispatchMessage.startMessagePump();

		// Run maintenance
		MaintenanceManager.dailyMaintenance();

		// Disabled but kept in case of emergency
		Logger.info("Starting Orphan Item Purge");
		PurgeOprhans.startPurgeThread();

		// Calculate bootstrap time and rest boot time to current time.
		java.time.Duration bootDuration = java.time.Duration.between(LocalDateTime.now(), bootTime);
		long bootSeconds = Math.abs(bootDuration.getSeconds());
		String boottime = String.format("%d hours %02d minutes %02d seconds", bootSeconds / 3600, (bootSeconds % 3600) / 60, (bootSeconds % 60));
		Logger.info("Bootstrap time was " + boottime);

		bootTime = LocalDateTime.now();
		LootTable.initialized = true;

		Logger.info("Running garbage collection...");
		System.gc();
		return true;
	}

	protected boolean initDatabaselayer() {

		// Try starting a GOM <-> DB connection.
		try {

			Logger.info("Configuring GameObjectManager to use Database: '"
					+  ConfigManager.MB_DATABASE_NAME.getValue() + "' on "
					+  ConfigManager.MB_DATABASE_ADDRESS.getValue() + ':'
					+  ConfigManager.MB_DATABASE_PORT.getValue());

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


	private void getWorldBuildingsMobsNPCs() {

		ArrayList<Zone> rootParent;

		rootParent = DbManager.ZoneQueries.GET_MAP_NODES(MBServerStatics.worldUUID);

		if (rootParent.isEmpty()) {
			Logger.error("populateWorldBuildings: No entries found in worldMap for parent " + MBServerStatics.worldUUID);
			return;
		}

		//Set sea floor object for server
		Zone seaFloor = rootParent.get(0);
		seaFloor.setParent(null);
		ZoneManager.setSeaFloor(seaFloor);

		//  zoneManager.addZone(seaFloor.getLoadNum(), seaFloor); <- DIE IN A FUCKING CAR FIRE BONUS CODE LIKE THIS SUCKS FUCKING DICK

		rootParent.addAll(DbManager.ZoneQueries.GET_ALL_NODES(seaFloor));

		long start = System.currentTimeMillis();

		for (Zone zone : rootParent) {

			try {
				ZoneManager.addZone(zone.getLoadNum(), zone);

				try{
					zone.generateWorldAltitude();
				}catch(Exception e){
					Logger.error( e.getMessage());
					e.printStackTrace();
				}

				//Handle Buildings

				ArrayList<Building> bList;
				bList = DbManager.BuildingQueries.GET_ALL_BUILDINGS_FOR_ZONE(zone);

				for (Building b : bList) {

					try {
						b.setObjectTypeMask(MBServerStatics.MASK_BUILDING);
						b.setLoc(b.getLoc());
					} catch (Exception e) {
						Logger.error( b.getObjectUUID() + " returned an Error Message :" + e.getMessage());
					}
				}

				//Handle Mobs
				ArrayList<Mob> mobs;
				mobs = DbManager.MobQueries.GET_ALL_MOBS_FOR_ZONE(zone);

				for (Mob m : mobs) {
					m.setObjectTypeMask(MBServerStatics.MASK_MOB | m.getTypeMasks());
					m.setLoc(m.getLoc());
					m.setParentZone(zone);

				//ADD GUARDS HERE.
				if (m.getBuilding() != null && m.getBuilding().getBlueprint() != null && m.getBuilding().getBlueprint().getBuildingGroup() == BuildingGroup.BARRACK)
					DbManager.MobQueries.LOAD_PATROL_POINTS(m);
				}

				//Handle npc's
				ArrayList<NPC> npcs;

				// Ignore npc's on the seafloor (npc guild leaders, etc)

				if (zone.equals(seaFloor))
					continue;

				npcs = DbManager.NPCQueries.GET_ALL_NPCS_FOR_ZONE(zone);

				for (NPC n : npcs) {

					try {
						n.setObjectTypeMask(MBServerStatics.MASK_NPC);
						n.setLoc(n.getLoc());
						n.setParentZone(zone);
					} catch (Exception e) {
						Logger.error( n.getObjectUUID() + " returned an Error Message :" + e.getMessage());
					}
				}

				//Handle cities

				City.loadCities(zone);
				ZoneManager.populateWorldZones(zone);

			} catch (Exception e) {
				Logger.info(e.getMessage() + zone.getName() + ' ' + zone.getObjectUUID());
			}
		}

		Logger.info("time to load: " + (System.currentTimeMillis() - start) + " ms");
	}

	/**
	 * Called to remove a client on "leave world", "quit game", killed client
	 * process, etc.
	 */

	public void removeClient(ClientConnection origin) {

		if (origin == null) {
			Logger.info(
					"ClientConnection null in removeClient.");
			return;
		}

		PlayerCharacter pc = SessionManager.getPlayerCharacter(
				origin);

		if (pc == null)
			// TODO log this
			return;

		//cancel any trade
		if (pc.getCharItemManager() != null)
			pc.getCharItemManager().endTrade(true);

		// logout
		long delta = MBServerStatics.LOGOUT_TIMER_MS;

		if (System.currentTimeMillis() - pc.getTimeStamp("LastCombatPlayer") < 60000) {
			delta = 60000;

		}
		pc.stopMovement(pc.getLoc());
		UpdateStateMsg updateStateMsg = new UpdateStateMsg();
		updateStateMsg.setPlayer(pc);
	
		updateStateMsg.setActivity(5);
		DispatchMessage.dispatchMsgToInterestArea(pc, updateStateMsg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
		
		if (pc.getRegion() != null)
			if (PlayerCharacter.CanBindToBuilding(pc, pc.getRegion().parentBuildingID))
				pc.bindBuilding = pc.getRegion().parentBuildingID;
			else
				pc.bindBuilding = 0;
		
		pc.getLoadedObjects().clear();
		pc.getLoadedStaticObjects().clear();

		LogoutCharacterJob logoutJob = new LogoutCharacterJob(pc, this);
		JobContainer jc = JobScheduler.getInstance().scheduleJob(logoutJob,
				System.currentTimeMillis() + delta);
		pc.getTimers().put("Logout", jc);
		pc.getTimestamps().put("logout", System.currentTimeMillis());
		
		//send update to friends that you are logged off.

		PlayerFriends.SendFriendsStatus(pc,false);

	}

	public void logoutCharacter(PlayerCharacter player) {

		if (player == null) {
			Logger.error("Unable to find PlayerCharacter to logout");
			return;
		}

		player.getTimestamps().put("logout", System.currentTimeMillis());
		player.setEnteredWorld(false);

		// remove from simulation and zero current loc

		WorldGrid.RemoveWorldObject(player);

		// clear Logout Timer

		if (player.getTimers() != null)
			player.getTimers().remove("Logout");

		if (player.getPet() != null)
			player.getPet().dismiss();
		
		player.dismissNecroPets();

		// Set player inactive so they quit loading for other players

		player.setActive(false);

		// Remove from group

		Group group = GroupManager.getGroup(player);

		try {
			if (group != null)
				GroupManager.LeaveGroup(player);
		} catch (MsgSendException e) {
			Logger.error( e.toString());
		}
		
		player.respawnLock.writeLock().lock();
		try{
			if (!player.isAlive())
				player.respawn(false, false, true);
		}catch(Exception e){
			Logger.error(e);
		}finally{
			player.respawnLock.writeLock().unlock();
		}
	}


	public static void writePopulationFile() {

		int population = SessionManager.getActivePlayerCharacterCount();
try {
	

		File populationFile = new File(MBServerStatics.DEFAULT_DATA_DIR + ConfigManager.MB_WORLD_NAME.getValue().replaceAll("'","") + ".pop");
		FileWriter fileWriter;

		try {
			fileWriter = new FileWriter(populationFile, false);
			fileWriter.write(Integer.toString(population));
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
}catch(Exception e){
	Logger.error(e);
}
	}

	private void processTrashFile() {

		ArrayList<String> machineList;
		ArrayList<PlayerCharacter> trashList = new ArrayList<>();
		ArrayList<Integer> accountList = new ArrayList<>();

		File trashFile = new File("trash");

		if (trashFile.exists() == false)
			return;

		// Build list of machineID's in the trash file

		machineList = DbManager.AccountQueries.GET_TRASH_LIST();

		// Build list of trash characters associated with that machineID

		for (String machineID:machineList) {
			trashList = DbManager.AccountQueries.GET_ALL_CHARS_FOR_MACHINE(machineID);


			// Deactivate these players and add them to loginCache table

			for (PlayerCharacter trashPlayer : trashList) {

				if (trashPlayer == null)
					continue;

				// Need to collate accounts.

				if (!accountList.contains(trashPlayer.getAccount().getObjectUUID()))
					accountList.add(trashPlayer.getAccount().getObjectUUID());

				DbManager.PlayerCharacterQueries.SET_ACTIVE(trashPlayer, false);
				DbManager.AccountQueries.INVALIDATE_LOGIN_CACHE(trashPlayer.getObjectUUID(), "character");
			}
		}

		//  delete vault of associated accounts and then invalidate them
		//  in the login cache.

		for (Integer accountID : accountList) {
			DbManager.AccountQueries.DELETE_VAULT_FOR_ACCOUNT(accountID);
			DbManager.AccountQueries.INVALIDATE_LOGIN_CACHE(accountID, "account");
		}

		// Trigger the Login Server to invalidate these accounts in the cache..

		try {
			Files.write(Paths.get("cacheInvalid"), "".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// If any of these players are active disconnect them.
		// The account and player should be removed from the login
		// server cache file by now.

		Timer timer = new Timer("Disconnect Trash");
		timer.schedule(new DisconnectTrashTask( trashList ), 3000L);

		// Clean up after ourselves

		try {
			Files.deleteIfExists(Paths.get("trash"));
			DbManager.AccountQueries.CLEAR_TRASH_TABLE();
		} catch (IOException e) {
			e.printStackTrace();
		}

		}

	private void processFlashFile() {

		File flashFile = new File("flash");
		String flashString;
		List<String> fileContents;

		if (flashFile.exists() == false)
			return;

		try {
			fileContents = Files.readAllLines(Paths.get("flash"));
		} catch (IOException e) {
			return;
		}

		// Flash file detected: read contents
		// and send as a flash.

		flashString = fileContents.toString();

		if (flashString == null)
			return;

		if (flashString == "")
			flashString = "Rebooting for to fix bug.";

		Logger.info( "Sending flash from external interface");
		Logger.info( "Msg: " + flashString);

		ChatSystemMsg msg = new ChatSystemMsg(null, flashString);
		msg.setChannel(engine.Enum.ChatChannelType.FLASH.getChannelID());
		msg.setMessageType(Enum.ChatMessageType.INFO.ordinal());
		DispatchMessage.dispatchMsgToAll(msg);

		// Delete file

		try {
			Files.deleteIfExists(Paths.get("flash"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}