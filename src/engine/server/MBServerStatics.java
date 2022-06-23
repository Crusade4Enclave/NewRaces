// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.server;

import engine.Enum;
import engine.gameManager.ConfigManager;
import engine.math.Vector3fImmutable;

public class MBServerStatics {

	public static final int revisionNumber = 1;

    public static String getEmulatorVersion() {
		return Integer.toString(revisionNumber);
	}

	public static final String CMDLINE_ARGS_EXE_NAME_DELIMITER = "-name";
	public static final String CMDLINE_ARGS_CONFIG_FILE_PATH_DELIMITER = "-config";
	public static final String CMDLINE_ARGS_CALLER_DELIMITER = "-caller";
	public static final String CMDLINE_ARGS_REASON_DELIMITER = "-reason";
	public static final String EXISTING_CONNECTION_CLOSED = "An existing connection was forcibly closed by the remote host";
	public static final String RESET_BY_PEER = "Connection reset by peer";
	/*
	 * ####Debugging Flags####
	 */
	public static final boolean POWERS_DEBUG = false;
	public static final boolean MOVEMENT_SYNC_DEBUG = false;
	public static final boolean BONUS_TRAINS_ENABLED = false;
	public static final boolean REGENS_DEBUG = false;
	public static final boolean SHOW_SAFE_MODE_CHANGE = false;
	public static final boolean COMBAT_TARGET_HITBOX_DEBUG = false; // output
	// hit box
	// calcs
	public static final boolean PRINT_INCOMING_OPCODES = false; // print
	// incoming
	// opcodes to
	// console

	public static final int BANK_GOLD_LIMIT = 25000000;
	public static final int PLAYER_GOLD_LIMIT = 10000000;
	public static final int BUILDING_GOLD_LIMIT = 15000000;

	public static final String VENDOR_FULL = "This vendor has no more gold to give.";
	public static final boolean HEIGHTMAP_DEBUG = false;
	public static final boolean FAST_LOAD = false; // skip loading mobs,
	// buildings, npcs
	public static final boolean FAST_LOAD_INIT = false; // skip loading mobs,
	// buildings, npcs
	/*
	 * Login cache flags
	 */
	public static final boolean SKIP_CACHE_LOGIN = false; // skip caching														// login server
	public static final boolean SKIP_CACHE_LOGIN_PLAYER = false; // skip caching															// on login
	public static final boolean SKIP_CACHE_LOGIN_ITEM = false; // skip caching

	/*
	 * Logger
	 */
	public static final int bannerWidth = 80;
	public static final int typeWidth = 10;
	public static final int originWidth = 25;
	public static final int logWidth = 80;

	/*
	 * ConfigSystem related
	 */
	public static final String DEFAULT_CONFIG_DIR = "mb.conf/";
	public static final String DEFAULT_DATA_DIR = "mb.data/";
	/*
	 * ChatManager related
	 */
	public static final int SHOUT_PERCEPTION_RADIUS_MOD = 2;
	/*
	 * DevCmd related
	 */
	public static final String DEV_CMD_PREFIX = "./";

	/*
	 * JobManager related
	 */

	// The number of elements in INITIAL_WORKERS defines the initial number of
	// job pools
	public static final int[] INITIAL_JOBPOOL_WORKERS = { 4, 2, 1 };
	public static final int DEFAULT_JOBPOOL_WORKERS = 1;
	public static final int DEFAULT_LOGIN_JOBPOOL_WORKERS = 5;

	public static final int JOBWORKER_IDLE_TIMEOUT_MS = 750;
	public static final int JOBMANAGER_INTERNAL_MONITORING_INTERVAL_MS = 1000;
	public static final int JOB_STALL_THRESHOLD_MS = 120 * 1000;
	public static final int MAX_JOB_HISTORY_OBJECTS = 1000; // max number of
	// historic jobs to
	// store on queue
	// after execution
	public static final int JOBSTATISTICS_WAKE_INTERVAL_MS = 500; // wake up and
	// gather
	// job stats
	// every X
	// ms,
	// decrease
	// this is
	// we blow
	// the job
	// history
	// queue

	public static final int SCHEDULER_INITIAL_CAPACITY = 1000;
	public static final int SCHEDULER_EXECUTION_TIME_COMPENSATION = 16;

	/*
	 * Concurrent Hash Map - Defaults
	 */

	public static final int CHM_INIT_CAP = 10;
	public static final float CHM_LOAD = 0.75f;
	public static final int CHM_THREAD_HIGH = 4;
	public static final int CHM_THREAD_MED = 2;
	public static final int CHM_THREAD_LOW = 1;

	/*
	 * LoginErrorMsg related
	 */
	public static final int LOGINERROR_INVALID_USERNAME_PASSWORD = 1;
	public static final int LOGINERROR_ACCOUNT_SUSPENDED = 2;

	/*
	 * Message is Version:
	 */
	public static final int LOGINERROR_INCORRECT_CLIENT_VERSION = 3;
	public static final int LOGINERROR_NOT_ALLOWED_TO_LOGIN_YET = 4;

	/*
	 * Message is 'Error ='
	 */
	public static final int LOGINERROR_LOGINSERVER_IS_UNAVAILABLE = 5;
	public static final int LOGINERROR_INVALID_ADMIN_USERNAME_PASSWORD = 6;
	public static final int LOGINERROR_NO_MORE_PLAYTIME_ON_ACCOUNT = 7;
	public static final int LOGINERROR_ACCOUNT_DOESNT_HAVE_SUBSCRIPTION = 8;
	public static final int LOGINERROR_ACCOUNT_INSECURE_CHANGE_PASSWORD = 9;
	public static final int LOGINERROR_TOO_MANY_LOGIN_TRIES = 10;

	/*
	 * Message is 'Error ='
	 */
	public static final int LOGINERROR_NOMOREPLAYTIME = 7;
	public static final int LOGINERROR_INACTIVE = 8;
	public static final int LOGINERROR_UNABLE_TO_LOGIN = 11;
	public static final int LOGINERROR_LOGINSERVER_BUSY = 12;
	public static final int LOGINERROR_BLANK = 13;

	/*
	 * >13 = 'blank' 12 = 'Login Server is currently busy, please try again in a
	 * few minutes.' 11 = 'Unable to login. Please try again. If this problem
	 * persists, contact customer support (error = )' 10 = 'You have made too
	 * many unsuccessful login attempts, you must wait 15 minutes.' 9 = 'Your
	 * Account is insecure, you must change your password before logging in
	 * again.' 8 = 'This Account does not have an active Shadowbane
	 * Subscription' 7 = 'No More PlayTime on this account 6 = 'Invalid
	 * Administrator Username/Password' 5 = 'Login Server Is Unavailable (Error
	 * = 0)' 4 = 'YouAreNotAllowedToLoginYet' 3 = 'Incorrect ClientVersion,
	 * latest is' 2 = 'This Account Has Been Suspended' 1 = 'Invalid
	 * Username/Password'
	 */

	/*
	 * Name Validation Related
	 */
	public static final int INVALIDNAME_FIRSTNAME_MUST_BE_LONGER = 1;
	public static final int INVALIDNAME_FIRSTANDLAST_MUST_BE_SHORTER = 2;
	public static final int INVALIDNAME_FIRSTNAME_MUST_NOT_HAVE_SPACES = 3;
	public static final int INVALIDNAME_FIRSTNAME_INVALID_CHARACTERS = 4;
	public static final int INVALIDNAME_PLEASE_CHOOSE_ANOTHER_FIRSTNAME = 5;
	public static final int INVALIDNAME_PLEASE_CHOOSE_ANOTHER_LASTNAME = 7;
	public static final int INVALIDNAME_LASTNAME_UNAVAILABLE = 8;
	public static final int INVALIDNAME_FIRSTNAME_UNAVAILABLE = 9;
	public static final int INVALIDNAME_WRONG_WORLD_ID = 10;
	public static final int INVALIDNAME_GENERIC = 11;

	/*
	 * 1: A first name of at least 3 character(s) must be entered 2: Your first
	 * and last name cannot be more than 15 characters each. 3: Your first name
	 * may not contain spaces 4: There are invalid characters in the first name.
	 * 5: Please choose another first name 7: Please choose another last name 8:
	 * That last name is unavailable 9: That first name is unavailable 10: Your
	 * client sent an invalid world id 11: Invalid name. Choose another
	 */
	public static final int MIN_NAME_LENGTH = 3;
	public static final int MAX_NAME_LENGTH = 15;

	/*
	 * ClientConnection related
	 */
	public static final boolean TCP_NO_DELAY_DEFAULT = true;
	public static final byte MAX_CRYPTO_INIT_TRIES = 10;
	

	/*
	 * EmuConnectionManager related
	 */
	public static final long delayBetweenConnectionChecks = 5000L; // in ms
	public static final long delayBetweenReconnectAttempts = 2000L; // in ms
	public static final int maxReconnectAttempts = 20;
	public static final long reconnectTimeout = 15000L;
	public static boolean DEBUG_PROTOCOL = false;
	/*
	 * Account Related
	 */

	public static final byte MAX_LOGIN_ATTEMPTS = 5;
    public static final int RESET_LOGIN_ATTEMPTS_AFTER = (15 * 60 * 1000); // in
    // ms
    public static final int MAX_ACTIVE_GAME_ACCOUNTS_PER_DISCORD_ACCOUNT = 4; // 0
	// to
	// disable
	/*
	 * Character related
	 */

	public static final byte MAX_NUM_OF_CHARACTERS = 7;

	public static final int STAT_STR_ID = 0x8AC3C0E6;
	public static final int STAT_SPI_ID = 0xACB82E33;
	public static final int STAT_CON_ID = 0xB15DC77E;
	public static final int STAT_DEX_ID = 0xE07B3336;
	public static final int STAT_INT_ID = 0xFF665EC3;

	/*
	 * Skill attributeIDs
	 */

	public static final int SKILL_RUNNING = 5;

	/*
	 * EquipSlot
	 */

	public static final int SLOT_UNEQUIPPED = 0;
	public static final int SLOT_MAINHAND = 1;
	public static final int SLOT_OFFHAND = 2;
	public static final int SLOT_HELMET = 3;
	public static final int SLOT_CHEST = 4;
	public static final int SLOT_ARMS = 5;
	public static final int SLOT_GLOVES = 6;
	public static final int SLOT_RING1 = 7;
	public static final int SLOT_RING2 = 8;
	public static final int SLOT_NECKLACE = 9;
	public static final int SLOT_LEGGINGS = 10;
	public static final int SLOT_FEET = 11;
	public static final int SLOT_HAIRSTYLE = 18; // 17 & 18? Weird.
	public static final int SLOT_BEARDSTYLE = 17; // 17 & 18? Weird.

	// Equip[0] = Slot1 = Weapon MainHand
	// Equip[1] = Slot2 = OffHand
	// Equip[2] = Slot3 = Helmet
	// Equip[3] = Slot4 = Chest
	// Equip[4] = Slot5 = Arms
	// Equip[5] = Slot6 = Gloves
	// Equip[6] = Slot7 = Ring1
	// Equip[7] = Slot8 = Ring2
	// Equip[8] = Slot9 = Necklace
	// Equip[9] = Slot10 = Leggings
	// Equip[10] = Slot11 = Feet
	// Equip[11] = Slot17 = HairStyle
	// Equip[12] = Slot18 = BeardStyle

	/*
	 * Group Formation Names
	 */
	public static final String[] FORMATION_NAMES = { "Column", "Line", "Box",
			"Triangle", "Circle", "Ranks", "Wedge", "Inverse Wedge", "T" };

	/*
	 * Runes
	 */

	public static final int RUNETYPE_TRAIT = 1;

	public static final int RUNE_COST_ATTRIBUTE_ID = 0;

	public static final int RUNE_STR_ATTRIBUTE_ID = 1;
	public static final int RUNE_DEX_ATTRIBUTE_ID = 2;
	public static final int RUNE_CON_ATTRIBUTE_ID = 3;
	public static final int RUNE_INT_ATTRIBUTE_ID = 4;
	public static final int RUNE_SPI_ATTRIBUTE_ID = 5;

	public static final int RUNE_STR_MAX_ATTRIBUTE_ID = 6;
	public static final int RUNE_DEX_MAX_ATTRIBUTE_ID = 7;
	public static final int RUNE_CON_MAX_ATTRIBUTE_ID = 8;
	public static final int RUNE_INT_MAX_ATTRIBUTE_ID = 9;
	public static final int RUNE_SPI_MAX_ATTRIBUTE_ID = 10;

	public static final int RUNE_STR_MIN_NEEDED_ATTRIBUTE_ID = 11;
	public static final int RUNE_DEX_MIN_NEEDED_ATTRIBUTE_ID = 12;
	public static final int RUNE_CON_MIN_NEEDED_ATTRIBUTE_ID = 13;
	public static final int RUNE_INT_MIN_NEEDED_ATTRIBUTE_ID = 14;
	public static final int RUNE_SPI_MIN_NEEDED_ATTRIBUTE_ID = 15;

	/*
	 * DBMan
	 */
	public static final int NO_DB_ROW_ASSIGNED_YET = Integer.MAX_VALUE;

	/*
	 * PreparedStatement query debugging
	 */
	public static final boolean DB_DEBUGGING_ON_BY_DEFAULT = false; // warning:
	// not
	// recommended
	// for a
	// live
	// production
	// server
	public static final boolean ENABLE_QUERY_TIME_WARNING = true;
	public static final boolean ENABLE_UPDATE_TIME_WARNING = true;
	public static final boolean ENABLE_EXECUTION_TIME_WARNING = true;

	/*
	 * ClientEncryption
	 */
	public static final int AUTHENTICATION_WAIT_TIMEOUT = 1000 * 2; // seconds
	public static final int MaxGetKeyFromClientTries = 4;
	public static final int MaxProtocolMessagesPerSecond = 20;  // 60 per second

	/*
	 * Guild Colors
	 */

	// public static final int GUILD_COLOR_LIGHTGREEN = 0;
	// public static final int GUILD_COLOR_GREEN = 1;
	// public static final int GUILD_COLOR_DARKGREEN = 2;
	// public static final int GUILD_COLOR_LIGHTBLUE = 3;
	// public static final int GUILD_COLOR_BLUE = 4;
	// public static final int GUILD_COLOR_DARKBLUE = 5;
	// public static final int GUILD_COLOR_PURPLE = 6;
	// public static final int GUILD_COLOR_DARKRED = 7;
	// public static final int GUILD_COLOR_LIGHTRED = 8;
	// public static final int GUILD_COLOR_ORANGE = 9;
	// public static final int GUILD_COLOR_BROWNORANGE = 10;
	// public static final int GUILD_COLOR_BROWN = 11;
	// public static final int GUILD_COLOR_BROWNYELLOW = 12;
	// public static final int GUILD_COLOR_YELLOW = 13;
	// public static final int GUILD_COLOR_LIGHTGREY = 14;
	// public static final int GUILD_COLOR_GREY = 15;
	// public static final int GUILD_COLOR_DARKGREY = 16;
	// public static final int GUILD_COLOR_BLACK = 17;
	// public static final int GUILD_COLOR_BLUEGREEN = 18;
	// public static final int GUILD_COLOR_WHITE = 19;

	/*
	 * Timeout Related
	 */
	public static final int AFK_TIMEOUT_MS = (30 * 60 * 1000) * 100; // Added
	// *100
	// to
	// discount
	// it as
	// a
	// "random DC reason"
	public static final int KEEPALIVE_TIMEOUT_MS = (2 * 60 * 1000)
			+ (15 * 1000);
	public static final int TIMEOUT_CHECKS_TIMER_MS = (60 * 1000);

	/*
	 * Masks for Quad Tree. Masks should be multiple of 2.
	 */

	public static final int MASK_PLAYER = 1;
	public static final int MASK_MOB = 2;
	public static final int MASK_PET = 4;
	public static final int MASK_CORPSE = 8;
	public static final int MASK_BUILDING = 16;
	public static final int MASK_UNDEAD = 64;
	public static final int MASK_BEAST = 128;
	public static final int MASK_HUMANOID = 256;
	public static final int MASK_NPC = 512;
	public static final int MASK_IAGENT = 2048;

	public static final int MASK_DRAGON = 4096;
	public static final int MASK_RAT = 8192;
	public static final int MASK_SIEGE = 16384;
	public static final int MASK_CITY = 32768;
	public static final int MASK_ZONE = 65536;

	/*
	 * Combined QT Masks. For convenience
	 */

	public static final int MASK_AGGRO = 5; // Player, Pet
	public static final int MASK_MOBILE = 7; // Player, Mob, Pet
	public static final int MASK_STATIC = 568; // Corpse, Building, Trigger, NPC

	/*
	 * World Coordinate Data
	 */
	public static final double MAX_WORLD_HEIGHT = -98304.0;
	public static final double MAX_WORLD_WIDTH = 131072.0;
	public static final float SEA_FLOOR_ALTITUDE = -1000f;
	public static int SPATIAL_HASH_BUCKETSX = 16384;
	public static int SPATIAL_HASH_BUCKETSY = 12288;
	public static float MAX_PLAYER_X_LOC = 129999;
	public static float MAX_PLAYER_Y_LOC = -97000;
	public static String NO_DELETE_COMBAT =  "Can't delete items when in Combat with another player.";

	/*
	 * Rates
	 */

	public static float EXP_RATE_MOD = 2f; // Probably don't want to declare
	// as final.
	public static float GOLD_RATE_MOD = 1.0f; // Probably don't want to declare
	// as final.
	public static float DROP_RATE_MOD = 1.0f; // Probably don't want to declare
	// as final.

	// Hotzones
	public static float HOT_EXP_RATE_MOD = 2.0f; // Probably don't want to
	// declare as final.
	public static float HOT_GOLD_RATE_MOD = 1.5f; // Probably don't want to
	// declare as final.
	public static float HOT_DROP_RATE_MOD = 1.8f; // Probably don't want to
	// declare as final.

	/*
	 * Ranges
	 */
	public static final int CHARACTER_LOAD_RANGE = 400; // load range of mobile objects
	// (default: 300)
	public static final int STRUCTURE_LOAD_RANGE = 700; // load range of
	// (default: 600)

	public static float LOOT_RANGE = 100;
	public static final int EXP_RANGE = 400;
	public static final int GOLD_SPLIT_RANGE = 600;
	// non-moving objects
	public static final int SAY_RANGE = 200;
	public static final int SHOUT_RANGE = 300;
	public static final int STATIC_THRESHOLD = 75; // Range must travel before
	// reloading statics
	public static final int FORMATION_RANGE = 75; // Max Distance a player can
	// be from group lead on
	// formation move
	public static final int OPENCLOSEDOORDISTANCE = 128; // Max distance a
	public static final int DOOR_CLOSE_TIMER = 30000; // 30 seconds
	// player can be from a door in order to toggle its state
	public static final int TRADE_RANGE = 10; // Max distance a player can be
	// from another player to trade
	public static final int NPC_TALK_RANGE = 20; // Range player can be to talk
	// to npc
	public static final int MAX_TELEPORT_RANGE = 1020; // Max range teleports
	// will work at
	public static final int RANGED_WEAPON_RANGE = 35; // any weapon attack
	// range beyond this
	// is ranged.
	public static final int CALL_FOR_HELP_RADIUS = 100; // Range mobs will
	// respond to calls
	// for help

	public static final int TREE_TELEPORT_RADIUS = 30;

	public static float MOB_SPEED_WALK = 6.5f;

	public static float MOB_SPEED_WALKCOMBAT = 4.4f;

	public static float MOB_SPEED_RUN = 14.67f;

	public static float MOB_SPEED_RUNCOMBAT = 14.67f;


	/*
	 * Noob Island Start Location for new players
	 */

	public static final int[] DEFAULTGRID = {-1,1}; 
	public static final float startX = 19128;// 70149f; //19318.0f;
	public static final float startY = 94f; // 94f;
	public static final float startZ = -73553; // -73661.0f;
	public static final Vector3fImmutable DEFAULT_START = new Vector3fImmutable(
			MBServerStatics.startX, MBServerStatics.startY,
			MBServerStatics.startZ);

	/*
	 * Base movement speeds. Do NOT modify these. They must match the client
	 */
	public static final float FLYWALKSPEED = 6.33f;
	public static final float FLYRUNSPEED = 18.38f;
	public static final float SWIMSPEED = 6.5f;
	public static final float WALKSPEED = 6.5f;
	public static final float RUNSPEED = 14.67f;
	public static final float COMBATWALKSPEED = 4.44f;
	public static final float COMBATRUNSPEED = 14.67f;
	public static final float RUNSPEED_MOB = 15.4f;

	public static final float MOVEMENT_DESYNC_TOLERANCE = 2f; // Distance out of
	public static String ITEMNOTINVENTORY = "Item must be in your inventory.";
	public static String ZEROITEM = "This item has zero quantity.";
	// sync with
	// client can be
	// before
	// generating
	// debug
	// messages
	// max units a player can desync before the server stops forcing
	// client->server sync
	public static final float MOVEMENT_MAX_DESYNC = 1000;

	public static final int IGNORE_LIST_MAX = 60;

	public static final float NO_WEAPON_RANGE = 8f; // Range for attack with no
	// weapon

	
	public static final float REGEN_IDLE = .06f;
	/*
	 * Base regen rates. Do NOT modify these. They must match the client %per
	 * second for health/mana. x per second for stamina.
	 */
	public static final float HEALTH_REGEN_SIT = 0.0033333f; // 100% in 3
	// minutes
	public static final float HEALTH_REGEN_IDLE = 0.000666667f; // 100% in 25
	// minutes
	public static final float HEALTH_REGEN_WALK = 0.0005f; // 100% in 33.33
	// minutes
	public static final float HEALTH_REGEN_RUN = 0f;
	public static final float HEALTH_REGEN_SWIM_NOSTAMINA = -.03f; // 100% in
	// 33.33
	// seconds.
	// Needs
	// verified

	public static final float HEALTH_REGEN_SIT_STATIC = 0.33333f; // 100% in 3
	// minutes
	public static final float HEALTH_REGEN_IDLE_STATIC = 0.0666667f; // 100% in
	// 25
	// minutes
	public static final float HEALTH_REGEN_WALK_STATIC = 0.05f; // 100% in 33.33
	// minutes
	public static final float HEALTH_REGEN_RUN_STATIC = 0f;
	public static final float HEALTH_REGEN_SWIM_NOSTAMINA_STATIC = 0f; // 100%
	
	public static final float MANA_REGEN_STATIC = 0.16666666666666666666666666666667f;
	// in 30
	// seconds.
	// Needs
	// verified

	public static final float MANA_REGEN_SIT = 0.008333333f; // 100% in 2
	// minutes <=
	// needs
	// verified
	public static final float MANA_REGEN_IDLE = 0.00166667f; // 100% in 10
	// minutes <=
	// needs
	// verified
	public static final float MANA_REGEN_WALK = 0.00125f; // 100% in 13.333
	// minutes <= needs
	// verified
	public static final float MANA_REGEN_RUN = 0f;

	public static final float STAMINA_REGEN_SIT = 2f; // 2 per second
	public static final float STAMINA_REGEN_IDLE = 0.2f; // 1 per 5 seconds
	public static final float STAMINA_REGEN_WALK = 0f;
	public static final float STAMINA_REGEN_RUN_COMBAT = -0.6499999762f;
	public static final float STAMINA_REGEN_RUN_NONCOMBAT = -0.400000006f;
	public static final float STAMINA_REGEN_SWIM = -1f; // -1 per second
	public static  float STAMINA_REGEN_FLY_IDLE = -2f; // needs verifying
	public static  float STAMINA_REGEN_FLY_WALK = -1f; // needs verifying
	public static  float STAMINA_REGEN_FLY_RUN = -1.400000006f; // needs verifying
	public static  float STAMINA_REGEN_FLY_RUN_COMBAT = -1.6499999762f; // needs verifying

	public static final int REGEN_SENSITIVITY_PLAYER = 250; // calc regen ever X
	// ms
	public static final int REGEN_SENSITIVITY_MOB = 1000; // calc regen ever X
	// ms
	/*
	 * Tombstone type to show Tombstone (2022); Tombstone, Grave (2023);
	 * Tombstone, Skull (2024);
	 */
	public static final int TOMBSTONE = 2024;
	public static final int DEATH_SHROUD_DURATION = 1; // 3 minute death shroud
	public static final int SAFE_MODE_DURATION = 1; // 3 minute safe mode

	/*
	 * Timers
	 */
	public static final int LOGOUT_TIMER_MS = 1000; // logout delay applied
	// after the last
	// aggressive action
	public static final int CLEANUP_TIMER_MS = 15 * 60 * 1000; // Remove player
	// from cache
	// after 15
	// minutes
	public static final int CORPSE_CLEANUP_TIMER_MS = 15 * 60 * 1000; // Cleanup
	// corpse
	// in
	// world
	// after
	// 15
	// minutes
	public static final int DEFAULT_SPAWN_TIME_MS = 3 * 60 * 1000; // 3 minute
	// respawn
	// on mobs
	// default
	public static final int SESSION_CLEANUP_TIMER_MS = 30 * 1000; // cleanup
	// sessions
	// for login
	// 30
	// seconds
	// after
	// logout
	public static final int MOVEMENT_FREQUENCY_MS = 1000; // Update movement
	// once every X ms
	public static final int FLY_FREQUENCY_MS = 1000; // Update flight once every
	
	public static final float FLY_RATE = .0078f;
	// x ms
	public static final int HEIGHT_CHANGE_TIMER_MS = 125; // Time in ms to fly
	// up or down 1 unit
	public static final long OPCODE_HANDLE_TIME_WARNING_MS = 250L;
	public static final long DB_QUERY_WARNING_TIME_MS = 250L;
	public static final long DB_UPDATE_WARNING_TIME_MS = 250L;
	public static final long DB_EXECUTION_WARNING_TIME_MS = 250L;
	public static boolean DB_ENABLE_QUERY_OUTPUT = false;
	public static final int SUMMON_MAX_WAIT = 18000; // 18 seconds to accept
	// summons
	public static final int THIRTY_SECONDS = 30000;
	public static final int FOURTYFIVE_SECONDS = 45000;
	public static final int ONE_MINUTE = 60000;
	public static final int FIVE_MINUTES = 300000;
	public static final int FIFTEEN_MINUTES = 900000;
	public static final int THIRTY_MINUTES = 1800000;
	public static final long TWENTY_FOUR_HOURS = 86400000;
	public static final int LOAD_OBJECT_DELAY = 500; // long to wait to update
	public static int IPLimit = 5000;
	// group list after
	// LoadChar
	public static final int UPDATE_LINK_WORLD = 2500;
	public static final int UPDATE_LINK_LOGIN = 2500;
	public static final int TELEPORT_TIME_IN_SECONDS = 10;
	public static final int REPLEDGE_TIME_IN_SECONDS = 0;
	public static final int CHECK_DATABASE_UPDATES = 10000; // update database
	// changes every 10
	// seconds.
	public static final int RUNEGATE_CLOSE_TIME = 30000; // runegate close timer
	public static final long PLAYER_KILL_XP_TIMER = 60 * 60 * 1000; // 60
	// minutes
	// between
	// grant xp
	// on same
	// target
	public static final int UPDATE_GROUP_RATE = 10000; // Update group info
	// every 10 seconds
	public static float PLAYER_HATE_DELIMITER = 50; // reduces 50 hate a second
	// while player idling.
	public static float PLAYER_COMBAT_HATE_MODIFIER = 2;

	/*
	 * AI
	 */

	// The min distance from players at which the AI Manager feels safe to turn
	// off a mob.
	public static int AI_BASE_AGGRO_RANGE = 60;
	public static int AI_DROP_AGGRO_RANGE = 60;
	public static int AI_RECALL_RANGE = 400;
	public static int AI_PULSE_MOB_THRESHOLD = 200;
	public static int AI_THREAD_SLEEP = 1000;
	public static int AI_PATROL_DIVISOR = 10;
	public static int AI_POWER_DIVISOR = 20;
	public static int AI_PET_HEEL_DISTANCE = 10;
	public static int AI_PATROL_RADIUS = 60;
	
	public static float AI_MAX_ANGLE = 10f;

	public static final int AI_PET_TIME_BETWEEN_JOB_TICKS_MS = 250;

	// Pet Settings
	public static final float PET_TELEPORT_DISTANCE = 600; // distance a pet
	// teleports to
	// player
	public static final float PET_FOLLOW_DISTANCE = 10; // distance a pet starts
	// moving towards owner
	public static final float PET_REST_DISTANCE = 4; // distance a pet stops
	// moving towards owner

	/*
	 * Combat
	 */
	public static final int COMBAT_SEND_DODGE = 20;
	public static final int COMBAT_SEND_BLOCK = 21;
	public static final int COMBAT_SEND_PARRY = 22;
	public static final short LEVELCAP = 75;
	public static final int LEVEL_CON_WHITE = 7;
	public static final int RESPAWN_TIMER = 90 * 1000;
	public static final int DESPAWN_TIMER = 12 * 1000;
	public static final int DESPAWN_TIMER_WITH_LOOT = 90 * 1000;
	public static final int DESPAWN_TIMER_ONCE_LOOTED = 5 * 1000;
	public static final int MAX_COMBAT_HITBOX_RADIUS = 80;
	public static final int PROC_CHANCE = 5; // %chance to proc
	public static float PRODUCTION_TIME_MULTIPLIER = .5f;

	/*
	 * Mob loot -- gold calculations
	 */
	public static final String STRONGBOX_DELAY_STRING = "StrongboxSpam";
	public static final String STRONGBOX_DELAY_OUTPUT = "You must wait 1 minute to do this again.";
	public static final int BIG_SPAM_DELAY = 10000;
	public static String BIG_SPAM_DELAY_STRING = "BIGSPAM";

	public static final double GOLD_DROP_PERCENTAGE_CHANCE = 61d;
	public static final double GOLD_DROP_MULTIPLIER_GLOBAL = 1.0d; // tweak all
	// rates at
	// once
	public static final double GOLD_DROP_MULTIPLIER_HOTZONE = 2.0d;
	public static final double GOLD_DROP_MULTIPLIER_MAELSTROM = 1.1d;
	public static final double GOLD_DROP_MULTIPLIER_OBLIVION = 1.1d;
	public static final double[] GOLD_DROP_MINIMUM_PER_MOB_LEVEL = { 450, 450,
			450, 450, 450, // 0 - 4
			450, 450, 450, 450, 450, // 5 - 9
			450, 1000, 1000, 1000, 1000, // 10 - 14
			1000, 1000, 1000, 1000, 1000, // 15 - 19
			1000, 1000, 1000, 1000, 1000, // 20 - 24
			2000, 2000, 2000, 2000, 2000, // 25 - 29
			2000, 2000, 2000, 2000, 2000, // 30 - 34
			2000, 2000, 2000, 2000, 2000, // 35 - 39
			4000, 4000, 4000, 4000, 4000, // 40 - 44
			4000, 4000, 4000, 4000, 4000, // 45 - 49
			5000 // 50+
	};
	public static final double[] GOLD_DROP_MAXIMUM_PER_MOB_LEVEL = { 1000,
			1000, 1000, 1000, 1000, // 0 - 4
			1000, 1000, 1000, 1000, 1000, // 5 - 9
			1000, 2500, 2500, 2500, 2500, // 10 - 14
			2500, 2500, 2500, 2500, 2500, // 15 - 19
			2500, 2500, 2500, 2500, 2500, // 20 - 24
			4000, 4000, 4000, 4000, 4000, // 25 - 29
			4000, 4000, 4000, 4000, 4000, // 30 - 34
			4000, 4000, 4000, 4000, 4000, // 35 - 39
			9000, 9000, 9000, 9000, 9000, // 40 - 44
			9000, 9000, 9000, 9000, 9000, // 45 - 49
			12000 // 50+
	};

    // DO NOT FINAL THESE FIELD!
    public static Enum.AccountStatus accessLevel; // Min account level to login to server
    public static boolean blockLogin = false;
	public static boolean ENABLE_VAULT_FILL = false;
	public static boolean ENABLE_MOB_LOOT = true;
	public static boolean ENABLE_AUDIT_JOB_WORKERS = true;
	public static boolean ENABLE_COMBAT_TARGET_HITBOX = true;

	/*
	 * Track Sensitivity
	 */
	// Rate that track arrow refreshes. When inside TRACK_ARROW_FAST_RANGE, use
	// TRACK_ARROW_SENSITIVITY_FAST speed, otherwise use TRACK_ARROW_SENSITIVITY
	// speed.
	public static final float TRACK_ARROW_FAST_RANGE = 50f; // Range to go from
	// Fast arrow to
	// slow
	public static final int TRACK_ARROW_SENSITIVITY = 1000; // Refresh track
	// arrows every X ms
	public static final int TRACK_ARROW_SENSITIVITY_FAST = 250; // Refresh track
	// arrows every
	// X ms

	/*
	 * Population breakpoints
	 */
	public static final int LOW_POPULATION = 100;
	public static final int NORMAL_POPULATION = 500;
	public static final int HIGH_POPULATION = 1000;
	public static final int VERY_OVERPOPULATED_POPULATION = 3000;
	public static final int FULL_POPULATION = 5000;

	// Refresh sensetivities
	public static final int TRACK_WINDOW_THRESHOLD = 1000; // max refresh once
	// every 1 seconds.
	public static final int WHO_WINDOW_THRESHOLD = 3000; // max refresh once
	// every 3 seconds.
	public static final int VENDOR_WINDOW_THRESHOLD = 2000; // max refresh once
	// every 2 seconds.
	public static final int PURCHASE_THRESHOLD = 500; // max refresh once every
	// 0.5 seconds.
	public static final int SELL_THRESHOLD = 100; // max refresh once every 0.1
	// seconds.
	public static final int MAX_PLAYER_LOAD_SIZE = 1000;

	// Mine related
	public static final int MINE_EARLY_WINDOW = 16; // 3pm
	public static final int MINE_LATE_WINDOW = 0; // Midnight

	// Race
	public static final float RADIUS_ARACOIX = 0.68999999761581f;
	public static final float RADIUS_MINOTAUR = 0.69960004091263f;
	public static final float RADIUS_DWARF = 0;
	public static final float RADIUS_HUMAN = 0;
	public static final float RADIUS_NEPHILIM = 0;
	public static final float RADIUS_AELFBORN = 0;
    public static final float RADIUS_ELF = 0;
    public static final float RADIUS_VAMPIRE = 0;
    public static final float RADIUS_IREKEI = 0;
    public static final float RADIUS_HALF_GIANT = 0;
    public static final float RADIUS_SHADE = 0;
    public static final float RADIUS_CENTAUR = 0.68999999761581f;

    public static String JUNIOR = "Junior";
    public static String VETERAN = "Veteran";
    public static String ELITE = "Elite";

    public static int worldMapID = Integer.parseInt(ConfigManager.MB_WORLD_MAPID.getValue());
    public static int worldUUID = Integer.parseInt(ConfigManager.MB_WORLD_UUID.getValue());
    public static Enum.AccountStatus worldAccessLevel = Enum.AccountStatus.valueOf(ConfigManager.MB_WORLD_ACCESS_LVL.getValue());
}
