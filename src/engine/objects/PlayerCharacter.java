// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum;
import engine.Enum.*;
import engine.InterestManagement.HeightMap;
import engine.InterestManagement.InterestManager;
import engine.InterestManagement.RealmMap;
import engine.InterestManagement.WorldGrid;
import engine.ai.MobileFSM.STATE;
import engine.db.archive.CharacterRecord;
import engine.db.archive.DataWarehouse;
import engine.db.archive.PvpRecord;
import engine.exception.MsgSendException;
import engine.exception.SerializationException;
import engine.gameManager.*;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.DeferredPowerJob;
import engine.jobs.FinishSpireEffectJob;
import engine.jobs.NoTimeJob;
import engine.math.Bounds;
import engine.math.FastMath;
import engine.math.Vector3fImmutable;
import engine.net.ByteBufferWriter;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.net.client.msg.login.CommitNewCharacterMsg;
import engine.powers.EffectsBase;
import engine.server.MBServerStatics;
import engine.server.login.LoginServer;
import engine.server.login.LoginServerMsgHandler;
import engine.util.MiscUtils;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class PlayerCharacter extends AbstractCharacter {

	//This object is to be used as the lock in a synchronized statement
	//any time the name of a PlayerCharacter needs to be set or
	//changed.  It ensures the uniqueness check and subsequent
	//database update can happen exclusively.
	private static final Object FirstNameLock = new Object();

	private final Account account;
	private final Race race;
	private BaseClass baseClass;
	private PromotionClass promotionClass;
	protected ArrayList<CharacterRune> runes;

	private final byte skinColor;
	private final byte hairColor;
	private final byte beardColor;

	private final byte hairStyle;
	private final byte beardStyle;
	private long channelMute = 0; // none muted.

	//All Guild information should be held here
	private final AtomicInteger guildStatus;

	private final AtomicInteger strMod = new AtomicInteger(); // Stat Modifiers
	private final AtomicInteger dexMod = new AtomicInteger();
	private final AtomicInteger conMod = new AtomicInteger();
	private final AtomicInteger intMod = new AtomicInteger();
	private final AtomicInteger spiMod = new AtomicInteger();
	private final ReadWriteLock teleportLock = new ReentrantReadWriteLock(true);
	public final ReadWriteLock respawnLock = new ReentrantReadWriteLock(true);

	private ConcurrentHashMap<Integer, String> ignoredPlayerIDs = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

	public boolean notDeleted; // <-Use this for deleting character

	// ===========================================
	// Variables NOT to put into the database!!!! (session only)
	// ===========================================
	public short statStrMax; // Max Base Stats
	public short statDexMax;
	public short statConMax;
	public short statIntMax;
	public short statSpiMax;
	public short statStrMin; // Min Base Stats
	public short statDexMin;
	public short statConMin;
	public short statIntMin;
	public short statSpiMin;

	// Current Stats before Equip and Effect
	// Modifiers
	public short statStrBase;
	public short statDexBase;
	public short statConBase;
	public short statIntBase;
	public short statSpiBase;
	public short trainedStatPoints = 0;

	private boolean lfGroup = false;
	private boolean lfGuild = false;
	private boolean recruiting = false;
	private MovementState movementState = MovementState.IDLE;
	private MovementState lastMovementState = MovementState.IDLE;

	private int overFlowEXP = 0;

	private int lastGuildToInvite;
	private int lastGroupToInvite;
	private boolean follow = false;
	private final HashMap<Integer, Long> summoners = new HashMap<>();
	private final HashSet<AbstractWorldObject> loadedObjects = new HashSet<>();
	private HashSet<AbstractWorldObject> loadedStaticObjects = new HashSet<>();
	private Vector3fImmutable lastStaticLoc = new Vector3fImmutable(0.0f, 0.0f, 0.0f);
	private final ConcurrentHashMap<Integer, LinkedList<Long>> chatChanFloodList = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private final ConcurrentHashMap<Integer, Long> killMap = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private GameObjectType lastTargetType;
	private int lastTargetID;
	private int hidden = 0; // current rank of hide/sneak/invis
	private int seeInvis = 0; // current rank of see invis
	private float speedMod;
	private float raceRunMod;
	private boolean teleportMode = false; // Teleport on MoveToPoint
	private final AtomicInteger trainsAvailable = new AtomicInteger(0); // num skill trains not used
	private float dexPenalty;
	private long lastPlayerAttackTime = 0;
	private long lastMobAttackTime = 0;
	private long lastUsedPowerTime = 0;
	private long lastTargetOfUsedPowerTime = 0;
	private long lastUpdateTime = System.currentTimeMillis();
	private long lastStamUpdateTime = System.currentTimeMillis();
	private boolean safeZone = false;
	public boolean isCSR = false;
	private int bindBuildingID;
	private int lastContract;
	private boolean noTeleScreen = false;

	private int lastRealmID = -2;
	private int subRaceID = 0;

	private boolean hasAnniversery = false;

	//TODO Public fields break OO!!!
	public boolean newChar;

	private DeferredPowerJob weaponPower;
	private NPC lastNPCDialog;

	private Mob pet;
	public final ArrayList<Mob> necroPets = new ArrayList<>();
	//Used for skill/Power calculation optimization
	private CharacterTitle title = CharacterTitle.NONE;
	private boolean asciiLastName = true;

	private int spamCount = 0;

	private boolean initialized = false;

	private boolean enteredWorld = false;

	private boolean canBreathe = true;
	/*
    DataWarehouse based kill/death tracking.
    These sets contain the last 10 UUID's
	 */

	public LinkedList<Integer> pvpKills;
	public LinkedList<Integer> pvpDeaths;
	private String hash;
	public int lastBuildingAccessed = 0;

	private ArrayList<GuildHistory> guildHistory = new ArrayList<>();

	public double timeLoggedIn = 0;

	public boolean RUN_MAGICTREK = true;

	public int spellsCasted = 0;
	public int pingCount = 0;
	public long startPing = 0;
	public double ping = 0;
	private boolean wasTripped75 = false;
	private boolean wasTripped50 = false;
	private boolean wasTripped25 = false;

	private float characterHeight = 0;
	public float centerHeight = 0;
	private boolean lastSwimming = false;

	private boolean isTeleporting = false;
	public float landingAltitude = 0;

	public int bindBuilding = 0;
	public FriendStatus friendStatus = FriendStatus.Available;

	/**
	 * No Id Constructor
	 */
	public PlayerCharacter( String firstName, String lastName, short strMod, short dexMod, short conMod, short intMod,
			short spiMod, Guild guild, byte runningTrains, Account account, Race race, BaseClass baseClass, byte skinColor, byte hairColor,
			byte beardColor, byte hairStyle, byte beardStyle) {
		super(firstName, lastName, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1, 0,
				Vector3fImmutable.ZERO, Vector3fImmutable.ZERO, Vector3fImmutable.ZERO,
				guild, runningTrains);

		this.runes = new ArrayList<>();
		this.account = account;
		this.notDeleted = true;
		this.race = race;
		this.baseClass = baseClass;
		this.skinColor = skinColor;
		this.hairColor = hairColor;
		this.beardColor = beardColor;
		this.hairStyle = hairStyle;
		this.beardStyle = beardStyle;
		this.lfGroup = false;
		this.lfGuild = false;

		this.strMod.set(strMod);
		this.dexMod.set(dexMod);
		this.conMod.set(conMod);
		this.intMod.set(intMod);
		this.spiMod.set(spiMod);

		this.guildStatus = new AtomicInteger(0);
		this.bindBuildingID = -1;
	}

	/**
	 * ResultSet Constructor
	 */
	public PlayerCharacter(ResultSet rs) throws SQLException {
		super(rs, true);

		this.runes = DbManager.CharacterRuneQueries.GET_RUNES_FOR_CHARACTER(this.getObjectUUID());
		int accountID = rs.getInt("parent");
		this.account = DbManager.AccountQueries.GET_ACCOUNT(accountID);
		this.gridObjectType = GridObjectType.DYNAMIC;

		this.notDeleted = rs.getBoolean("char_isActive");

		int raceID = rs.getInt("char_raceID");
		this.race = Race.getRace(raceID);

		int baseClassID = rs.getInt("char_baseClassID");
		this.baseClass = DbManager.BaseClassQueries.GET_BASE_CLASS(baseClassID);

		int promotionClassID = rs.getInt("char_promotionClassID");
		this.promotionClass = DbManager.PromotionQueries.GET_PROMOTION_CLASS(promotionClassID);

		this.skinColor = rs.getByte("char_skinColor");
		this.hairColor = rs.getByte("char_hairColor");
		this.beardColor = rs.getByte("char_beardColor");
		this.hairStyle = rs.getByte("char_hairStyle");
		this.beardStyle = rs.getByte("char_beardStyle");

		this.lfGroup = false;
		this.lfGuild = false;

		//TODO Unify game object with database after DB overhaul
		this.guildStatus = new AtomicInteger(0);

		Guild guild = Guild.getGuild(this.getGuildUUID());
		if (guild != null && guild.isGuildLeader(this.getObjectUUID()))
			this.setGuildLeader(true);
		else
			this.setGuildLeader(false);

		this.hasAnniversery = rs.getBoolean("anniversery");

		this.setInnerCouncil(rs.getBoolean("guild_isInnerCouncil"));
		this.setFullMember(rs.getBoolean("guild_isFullMember"));
		this.setTaxCollector(rs.getBoolean("guild_isTaxCollector"));
		this.setRecruiter(rs.getBoolean("guild_isRecruiter"));
		this.setGuildTitle(rs.getInt("guild_title"));

		if (this.account != null)
			this.ignoredPlayerIDs = DbManager.PlayerCharacterQueries.GET_IGNORE_LIST(this.account.getObjectUUID(), false);

		this.strMod.set(rs.getShort("char_strMod"));
		this.dexMod.set(rs.getShort("char_dexMod"));
		this.conMod.set(rs.getShort("char_conMod"));
		this.intMod.set(rs.getShort("char_intMod"));
		this.spiMod.set(rs.getShort("char_spiMod"));

		this.bindBuildingID = rs.getInt("char_bindBuilding");

		this.hash = rs.getString("hash");


		// For debugging skills
		// CharacterSkill.printSkills(this);
	}

	public void setGuildTitle(int value) {
		if (GuildStatusController.getTitle(this.guildStatus) == value)
			return;
		DbManager.PlayerCharacterQueries.SET_GUILD_TITLE(this, value);
		GuildStatusController.setTitle(guildStatus, value);
	}

	public void setFullMember(boolean value) {
		if (GuildStatusController.isFullMember(this.guildStatus) == value)
			return;
		DbManager.PlayerCharacterQueries.SET_FULL_MEMBER(this, value);
		GuildStatusController.setFullMember(guildStatus, value);
	}

	public void setRecruiter(boolean value) {
		if (GuildStatusController.isRecruiter(this.guildStatus) == value)
			return;
		DbManager.PlayerCharacterQueries.SET_RECRUITER(this, value);
		GuildStatusController.setRecruiter(guildStatus, value);
	}

	public void setTaxCollector(boolean value) {
		if (GuildStatusController.isTaxCollector(this.guildStatus) == value)
			return;
		DbManager.PlayerCharacterQueries.SET_TAX_COLLECTOR(this, value);
		GuildStatusController.setTaxCollector(guildStatus, value);
	}

	public void setInnerCouncil(boolean value) {

		// dont update if its the same.
		if (GuildStatusController.isInnerCouncil(this.guildStatus) == value)
			return;

		DbManager.PlayerCharacterQueries.SET_INNERCOUNCIL(this, value);
		GuildStatusController.setInnerCouncil(guildStatus, value);
	}

	public void setGuildLeader(boolean value) {
		if (GuildStatusController.isGuildLeader(this.guildStatus) == value)
			return;

		GuildStatusController.setGuildLeader(guildStatus, value);
		if (value == true){
			this.setInnerCouncil(true);
			this.setFullMember(true);
		}
	}

	//END -> Guild Status Interface
	public void resetGuildStatuses() {
		this.setInnerCouncil(false);
		this.setFullMember(false);
		this.setGuildTitle(0);
		this.setTaxCollector(false);
		this.setRecruiter(false);
		this.setGuildLeader(false);
	}

	/*
	 * Getters
	 */
	public byte getHairStyle() {
		return hairStyle;
	}

	public byte getBeardStyle() {
		return beardStyle;
	}

	public void setWeaponPower(DeferredPowerJob value) {
		this.weaponPower = value;
	}

	public DeferredPowerJob getWeaponPower() {
		return this.weaponPower;
	}

	public void setSafeZone(boolean value) {
		this.safeZone = value;
	}

	public boolean inSafeZone() {
		return this.safeZone;
	}

	public boolean isInSafeZone() {

		Zone zone = ZoneManager.findSmallestZone(this.getLoc());

		if (zone != null){
			return zone.getSafeZone() == (byte) 1;
		}

		return false;
		//return this.safeZone;
	}

	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	public void deactivateCharacter() {
		this.notDeleted = false;
		DbManager.PlayerCharacterQueries.SET_DELETED(this);
		DbManager.removeFromCache(this);
	}

	public void activateCharacter() {
		this.notDeleted = true;
		DbManager.PlayerCharacterQueries.SET_DELETED(this);
	}

	public boolean isDeleted() {
		return !this.notDeleted;
	}

	public ArrayList<CharacterRune> getRunes() {
		return this.runes;
	}

	public CharacterRune getRune(int runeID) {
		if (this.runes == null)
			return null;
		for (CharacterRune cr : this.runes) {
			if (cr.getRuneBase() != null && cr.getRuneBase().getObjectUUID() == runeID)
				return cr;
		}
		return null;
	}

	public boolean addRune(CharacterRune value) {
		if (this.runes.size() > 12) // Max Runes
			return false;
		if (this.runes.indexOf(value) != -1) // Already contains rune
			return false;
		this.runes.add(value);
		return true;
	}

	public boolean removeRune(CharacterRune value) {
		int index = this.runes.indexOf(value);
		if (index == -1)
			return false;
		this.runes.remove(index);
		return true;
	}

	public CharacterRune removeRune(int runeID) {
		Iterator<CharacterRune> it = this.runes.iterator();
		while (it.hasNext()) {
			CharacterRune cr = it.next();
			if (cr != null) {
				RuneBase rb = cr.getRuneBase();
				if (rb != null)
					if (runeID == rb.getObjectUUID()) {
						it.remove();
						DbManager.CharacterRuneQueries.DELETE_CHARACTER_RUNE(cr);
						return cr;
					}
			}
		}
		return null;
	}

	/**
	 * @ Kill this Character
	 */
	@Override
	public void killCharacter(AbstractCharacter attacker) {

		killCleanup();

		// *** Mobs have a separate combat path?  Crazy shit!
		// *** Mobs don't get Experience for killing players. everything else is done in killCleanup();

		if (attacker.getObjectType().equals(GameObjectType.PlayerCharacter) == false){

			Zone zone = ZoneManager.findSmallestZone(this.getLoc());

			//DeathShroud

			if (zone.getSafeZone() == 0)
				PowersManager.applyPower(this, this, Vector3fImmutable.ZERO, 1672601862, 40, false);

			//enable this to give players deathshroud if mobs kill player.

			//        	  Zone zone = ZoneManager.findSmallestZone(this.getLoc());
			//        	if (zone.getSafeZone() == 0)
			//                PowersManager.applyPower(this, this, Vector3fImmutable.ZERO, 1672601862, 40, false);
			return;
		}


		// Death to other player.
		// TODO Send PvP and guild/nation message
		PlayerCharacter att = (PlayerCharacter) attacker;
		String message = this.getFirstName();
		if (this.guild != null && (!(this.guild.getName().equals("Errant"))))
			message += " of " + this.guild.getName();
		message += " was killed by " + att.getFirstName();
		if (att.guild != null && (!(att.guild.getName().equals("Errant"))))
			message += " of " + att.guild.getName();
		message += "!";


		//see if we shold grant xp to attacker
		boolean doPVPEXP = false;
		long lastKill = att.getLastKillOfTarget(this.getObjectUUID());
		if ((System.currentTimeMillis() - lastKill) > MBServerStatics.PLAYER_KILL_XP_TIMER)
			if (attacker.getLevel() > 39 && this.getLevel() > 39) {
				Guild aN = null;
				Guild tN = null;
				if (attacker.getGuild() != null)
					aN = attacker.getGuild().getNation();
				if (this.getGuild() != null)
					tN = this.getGuild().getNation();
				if (aN == null || tN == null || aN.isErrant() || Guild.sameGuild(aN, tN) || this.isDeathShroud()) {
					//skip giving xp if same guild or attacker is errant, or target is in death shroud.
				} else {
					doPVPEXP = true;
				}
			}
		//apply death shroud to non safeholds.
		Zone zone = ZoneManager.findSmallestZone(this.getLoc());

		//DeathShroud

		if (zone.getSafeZone() == 0)
			PowersManager.applyPower(this, this, Vector3fImmutable.ZERO, 1672601862, 40, false);

		if (doPVPEXP){
			Group g = GroupManager.getGroup((PlayerCharacter) attacker);
			Experience.doExperience((PlayerCharacter) attacker, this, g);
		}

		ChatManager.chatPVP(message);

		/*
            Update kill / death tracking lists
            Each character on list is unique.  Only once!
		 */

		PlayerCharacter aggressorCharacter = (PlayerCharacter)attacker;

		boolean containsVictim = true;
		boolean containsAttacker = true;

		containsVictim = aggressorCharacter.pvpKills.contains(this.getObjectUUID());
		containsAttacker = aggressorCharacter.pvpKills.contains(this.getObjectUUID());

		// Rorate attacker's kill list

		if ((aggressorCharacter.pvpKills.size() == 10) && containsVictim == false)
			aggressorCharacter.pvpKills.removeLast();

		if (containsVictim == false)
			aggressorCharacter.pvpKills.addFirst(this.getObjectUUID());

		// Rotate the poor victim's deathlist

		if ((this.pvpDeaths.size() == 10) && containsAttacker == false)
			this.pvpDeaths.removeLast();

		if (containsAttacker == false)
			this.pvpDeaths.addFirst(this.getObjectUUID());

		// DataWarehouse: store pvp event

		PvpRecord pvpRecord = PvpRecord.borrow((PlayerCharacter) attacker, this, this.getLoc(), doPVPEXP);
		DataWarehouse.pushToWarehouse(pvpRecord);

		// Mark kill time in killmap

		att.updateKillMap(this.getObjectUUID());
	}

	@Override
	public void killCharacter(String reason) {

		killCleanup();
		Zone zone = ZoneManager.findSmallestZone(this.getLoc());

		if (zone.getSafeZone() == 0)
			PowersManager.applyPower(this, this, Vector3fImmutable.ZERO, 1672601862, 40, false);

		// Send death message if needed
		if (reason.equals("Water")) {

			TargetedActionMsg targetedActionMsg = new TargetedActionMsg(this, true);
			Dispatch dispatch = Dispatch.borrow(this, targetedActionMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

			String message = this.getFirstName();

			if (this.guild != null && (!(this.guild.getName().equals("Errant"))))
				message += " of " + this.guild.getName();
			else
				message += this.getLastName();
			message += " was killed by water!";

			ChatManager.chatPVP(message);

		}
	}

	private void killCleanup() {
		this.stopMovement(this.getLoc());

		this.health.set(-1);
		//remove pet
		if (this.pet != null)
			this.dismissPet();

		this.dismissNecroPets();
		// remove flight job.

		this.setTakeOffTime(0);
		this.setDesiredAltitude(0);
		this.altitude = (float) 0;

	this.getCharItemManager().closeTradeWindow();

		//increment live counter. This is to prevent double kills from casts
		this.liveCounter++;

		//remove any effects
		try {
			this.clearEffects();
		}catch(Exception e){
			Logger.error("PlayerCharacter.KillCleanup", e.getMessage());
		}

		//remove the SIT flag
		this.setSit(false);



		// sends a kill message to ensure the Player falls over.

		this.respawnLock.writeLock().lock();

		try{
			if (SessionManager.getPlayerCharacterByID(this.getObjectUUID()) == null && !this.enteredWorld){
				WorldGrid.RemoveWorldObject(this);
				this.respawn(false, false,true);
			}else{
				TargetedActionMsg killmsg = new TargetedActionMsg(this, true);
				DispatchMessage.dispatchMsgToInterestArea(this, killmsg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
			}
		}catch(Exception e){
			Logger.error(e);
		}finally{
			this.respawnLock.writeLock().unlock();
		}

		// TODO damage equipped items
		if (this.charItemManager != null)
			this.charItemManager.damageAllGear();

		// TODO cleanup any timers
		//recalculate inventory weights
		if (this.charItemManager != null) {
			this.charItemManager.endTrade(true);
			this.charItemManager.calculateWeights();
			this.charItemManager.updateInventory();
		}






	}


	public void updateKillMap(int target) {
		this.killMap.put(target, System.currentTimeMillis());
	}

	public long getLastKillOfTarget(int target) {
		if (this.killMap.containsKey(target))
			return this.killMap.get(target);
		return 0L;
	}

	public boolean isDeathShroud() {
		return this.effects != null && this.effects.containsKey("DeathShroud");
	}

	public void setSafeMode() {
		PowersManager.applyPower(this, this, Vector3fImmutable.ZERO, -1661758934, 40, false);
	}

	public boolean safemodeInvis() {

		if (!this.effects.containsKey("Invisible"))
			return false;

		Effect eff = this.effects.get("Invisible");

		if (eff == null)
			return false;

		return eff.getEffectToken() == -1661751254;

	}

	public void respawn(boolean setAlive, boolean enterWorld, boolean makeCorpse) {

		// Recalculate everything


		this.recalculatePlayerStats(true);
		this.setCombat(false);

		// Set Health to 1/4 max



		Corpse corpse = null;

		if (makeCorpse){
			try {
				corpse = Corpse.makeCorpse(this, enterWorld);
			} catch (Exception e) {
				Logger.error( e);
			}
			//if we're not making corpse, just purge inventory. used for characters dead while logged out.
		}

		if (!setAlive){
			if (corpse == null && makeCorpse) {
				Logger.error("Corpse not created.");
			}
			else {
				if(makeCorpse && corpse != null){
					InterestManager.forceLoad(corpse);
				}
			}
			return;
		}

		this.setHealth((float) (healthMax * .25));
			this.isAlive.set(true);


		// Put player in safe mode
		// Teleport the player to his bind loc
		// or to a ruin as apporpriate.

			Building bindBuilding = BuildingManager.getBuildingFromCache(this.getBindBuildingID());

			if (enterWorld) {
				this.stopMovement(this.getBindLoc());
			}
			else if (bindBuilding != null) {
				if (bindBuilding.getParentZone().equals(ZoneManager.findSmallestZone(this.getLoc())))
					this.teleport(Ruins.getRandomRuin().getLocation());
				else
					this.teleport(this.getBindLoc());
			} else // no bind building found for player, teleport to ruins.
					this.teleport(Ruins.getRandomRuin().getLocation());

		this.lastUpdateTime = System.currentTimeMillis();
		this.lastStamUpdateTime = System.currentTimeMillis();

		this.update();

		PowersManager.applyPower(this, this, Vector3fImmutable.ZERO, -1661758934, 40, false);

		if (corpse == null && makeCorpse) {
			Logger.error("Corpse not created.");
		}
		else {
			if(makeCorpse && corpse != null){
				InterestManager.forceLoad(corpse);
			}
		}
	}

	public Effect addCityEffect(String name, EffectsBase eb, int trains, int duration, boolean onEnter, City city) {
		JobContainer jc = null;
		if (onEnter) {
			NoTimeJob ntj = new NoTimeJob(this, name, eb, trains); //infinite timer
			ntj.setEffectSourceType(city.getObjectType().ordinal());
			ntj.setEffectSourceID(city.getObjectUUID());
			jc = new JobContainer(ntj);
		} else {
			FinishSpireEffectJob fsej = new FinishSpireEffectJob(this, name, eb, trains);
			fsej.setEffectSourceType(city.getObjectType().ordinal());
			fsej.setEffectSourceID(city.getObjectUUID());
			jc = JobScheduler.getInstance().scheduleJob(fsej, duration);
		}

		if (this.effects.get(name) != null)
			this.effects.get(name).cancelJob();

		Effect eff = new Effect(jc, eb, trains);
		this.effects.put(name, eff);
		applyAllBonuses();
		eff.sendSpireEffect(this.getClientConnection(), onEnter);
		return eff;
	}

	/**
	 * @return the race
	 */
	public Race getRace() {
		return race;
	}

	public int getRaceID() {
		if (race != null)
			return race.getRaceRuneID();
		return 0;
	}

	/**
	 * @return the baseClass
	 */
	public BaseClass getBaseClass() {
		return baseClass;
	}

	public int getBaseClassID() {
		if (baseClass != null)
			return baseClass.getObjectUUID();
		return 0;
	}

	public int getBaseClassToken() {
		if (this.baseClass == null)
			return 0;
		else
			return this.baseClass.getToken();
	}

	public boolean setBaseClass(int value) {
		BaseClass bs = BaseClass.getBaseClass(value);
		if (bs != null) {
			this.baseClass = bs;
			return true;
		}
		return false;
	}

	@Override
	public Vector3fImmutable getBindLoc() {

		Vector3fImmutable bindLocation;

		// Return garbage and early exit if this is the login server.
		// getBindLoc() does a TOL lookup, which also then loads the
		// city and other garbage not needed on the login server.

		if (ConfigManager.serverType.equals(ServerType.LOGINSERVER))
			return Vector3fImmutable.ZERO;

		Building bindBuilding = PlayerCharacter.getUpdatedBindBuilding(this);

		//handle rented room binds.


		if (bindBuilding == null){
			bindLocation = Enum.Ruins.getRandomRuin().getLocation();
			return bindLocation;
		}



		bindLocation = BuildingManager.GetBindLocationForBuilding(bindBuilding);

		if (bindLocation == null)
			bindLocation = Enum.Ruins.getRandomRuin().getLocation();

		return bindLocation;

	}

	public int getInventoryCapacity() {
		return statStrBase * 3;
	}

	public int getInventoryCapacityRemaining() {
		return (this.getInventoryCapacity() - this.charItemManager.getInventoryWeight());
	}

	/**
	 * @return the PromotionClass
	 */
	public PromotionClass getPromotionClass() {
		return promotionClass;
	}

	public int getPromotionClassID() {
		if (promotionClass != null)
			return promotionClass.getObjectUUID();
		return 0;
	}

	public boolean setPromotionClass(int value) {

		PromotionClass promotionClass = PromotionClass.GetPromtionClassFromCache(value);

		if (promotionClass == null)
			return false;


		if (!DbManager.PlayerCharacterQueries.SET_PROMOTION_CLASS(this, value))
			return false;

			this.promotionClass = promotionClass;

			// Warehouse this event
			CharacterRecord.updatePromotionClass(this);
			return true;
	}

	/**
	 * @return the skinColor
	 */
	public byte getSkinColor() {
		return skinColor;
	}

	/**
	 * @return the hairColor
	 */
	public byte getHairColor() {
		return hairColor;
	}

	/**
	 * @return the beardColor
	 */
	public byte getBeardColor() {
		return beardColor;
	}

	/**
	 * @return the lfGroup
	 */
	public boolean isLfGroup() {
		return lfGroup;
	}

	public int getIsLfGroupAsInt() {
		if (lfGroup)
			return 2;
		return 1;
	}


	public final void toggleLFGroup() {
		this.lfGroup = !this.lfGroup;
	}

	public final void toggleLFGuild() {
		this.lfGuild = !this.lfGuild;
	}

	public final void toggleRecruiting() {
		this.recruiting = !this.recruiting;
	}

	public final void setLFGroup(final boolean value) {
		this.lfGroup = value;
	}

	public final void setLFGuild(final boolean value) {
		this.lfGuild = value;
	}

	public final void setRecruiting(final boolean value) {
		this.recruiting = value;
	}

	public final boolean isLFGroup() {
		return this.lfGroup;
	}

	public final boolean isLFGuild() {
		return this.lfGuild;
	}

	public final boolean isRecruiting() {
		return this.recruiting;
	}

	/**
	 * @return the lfGuild
	 */
	public boolean isLfGuild() {
		return lfGuild;
	}

	public final int getHeadlightsAsInt() {
		if (this.lfGroup)
			if (this.lfGuild)
				if (this.recruiting)
					return 14; // LFGroup + LFGuild + Recruiting
				else
					return 6; // LFGroup + LFGuild
			else if (this.recruiting)
				return 10; // LFGroup + Recruiting
			else
				return 2; // LFGroup only
		else if (this.lfGuild)
			if (this.recruiting)
				return 12; // LFGuild + Recruiting
			else
				return 4; // LFGuild only
		else if (this.recruiting)
			return 8; // Recruiting only
		else
			return 0; // No Headlights
	}

	public int getIsLfGuildAsInt() {
		if (lfGuild)
			return 2;
		return 1;
	}

	public int getStrMax() {
		return this.statStrMax;
	}
	public int getDexMax() {
		return this.statDexMax;
	}
	public int getConMax() {
		return this.statConMax;
	}
	public int getIntMax() {
		return this.statIntMax;
	}
	public int getSpiMax() {
		return this.statSpiMax;
	}

	public void addStr(int amount) {

		boolean worked = false;
		short newStr = (short) 0;
		while (!worked) {

			if ((this.unusedStatPoints - this.trainedStatPoints) <= 0)
				return;

			newStr = (short) (this.statStrBase + amount);
			short mod = (short) this.strMod.get();
			short newStrMod = (short) (mod + amount);

			if (newStr > this.statStrMax) {
				newStrMod += (this.statStrMax - newStr);
				newStr = this.statStrMax;
			}
			worked = this.strMod.compareAndSet(mod, newStrMod);
		}
		this.trainedStatPoints++;
		this.statStrBase = newStr;
		this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);
		this.applyBonuses();
		this.calculateSkills();
	}

	public void addDex(int amount) {

		boolean worked = false;
		short newDex = (short) 0;

		while (!worked) {

			if ((this.unusedStatPoints - this.trainedStatPoints) <= 0)
				return;

			newDex = (short) (this.statDexBase + amount);
			short mod = (short) this.dexMod.get();
			short newDexMod = (short) (mod + amount);

			if (newDex > this.statDexMax) {
				newDexMod += (this.statDexMax - newDex);
				newDex = this.statDexMax;
			}

			worked = this.dexMod.compareAndSet(mod, newDexMod);
		}
		this.trainedStatPoints++;
		this.statDexBase = newDex;
		this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);
		this.applyBonuses();
		this.calculateSkills();
	}

	public void addCon(int amount) {
		boolean worked = false;
		short newCon = (short) 0;
		while (!worked) {

			if ((this.unusedStatPoints - this.trainedStatPoints) <= 0)
				return;

			newCon = (short) (this.statConBase + amount);
			short mod = (short) this.conMod.get();
			short newConMod = (short) (mod + amount);

			if (newCon > this.statConMax) {
				newConMod += (this.statConMax - newCon);
				newCon = this.statConMax;
			}
			worked = this.conMod.compareAndSet(mod, newConMod);
		}
		this.trainedStatPoints++;
		this.statConBase = newCon;
		this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);
		this.applyBonuses();
		this.calculateSkills();
	}

	public void addInt(int amount) {
		boolean worked = false;
		short newInt = (short) 0;
		while (!worked) {

			if ((this.unusedStatPoints - this.trainedStatPoints) <= 0)
				return;

			newInt = (short) (this.statIntBase + amount);
			short mod = (short) this.intMod.get();
			short newIntMod = (short) (mod + amount);

			if (newInt > this.statIntMax) {
				newIntMod += (this.statIntMax - newInt);
				newInt = this.statIntMax;
			}
			worked = this.intMod.compareAndSet(mod, newIntMod);
		}
		this.trainedStatPoints++;
		this.statIntBase = newInt;
		this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);
		this.applyBonuses();
		this.calculateSkills();
	}

	public void addSpi(int amount) {
		boolean worked = false;
		short newSpi = (short) 0;

		while (!worked) {

			if ((this.unusedStatPoints - this.trainedStatPoints) <= 0)
				return;

			newSpi = (short) (this.statSpiBase + amount);
			short mod = (short) this.spiMod.get();
			short newSpiMod = (short) (mod + amount);

			if (newSpi > this.statSpiMax) {
				newSpiMod += (this.statSpiMax - newSpi);
				newSpi = this.statSpiMax;
			}
			worked = this.spiMod.compareAndSet(mod, newSpiMod);
		}
		this.trainedStatPoints++;
		this.statSpiBase = newSpi;
		this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);
		this.applyBonuses();
		this.calculateSkills();
	}

	public boolean refineStr() {
		boolean worked = false;
		short newStr = (short) 0;

		while (!worked) {

			newStr = (short) (this.statStrBase - 1);
			short mod = (short) this.strMod.get();

			if (mod == 0)
				return false;

			short newStrMod = (short) (mod - 1);

			if (newStr < this.statStrMin)
				return false;

			if (!canRefineLower(MBServerStatics.STAT_STR_ID))
				return false;

			worked = this.strMod.compareAndSet(mod, newStrMod);
		}
		this.trainedStatPoints--;
		this.statStrBase = newStr;
		this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);
		this.applyBonuses();
		this.calculateSkills();
		return true;
	}

	public boolean refineDex() {
		boolean worked = false;
		short newDex = (short) 0;

		while (!worked) {
			newDex = (short) (this.statDexBase - 1);
			short mod = (short) this.dexMod.get();

			if (mod == 0)
				return false;

			short newDexMod = (short) (mod - 1);

			if (newDex < this.statDexMin)
				return false;

			if (!canRefineLower(MBServerStatics.STAT_DEX_ID))
				return false;

			worked = this.dexMod.compareAndSet(mod, newDexMod);
		}
		this.trainedStatPoints--;
		this.statDexBase = newDex;
		this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);
		this.applyBonuses();
		this.calculateSkills();
		return true;
	}

	public boolean refineCon() {
		boolean worked = false;
		short newCon = (short) 0;

		while (!worked) {
			newCon = (short) (this.statConBase - 1);
			short mod = (short) this.conMod.get();

			if (mod == 0)
				return false;

			short newConMod = (short) (mod - 1);

			if (newCon < this.statConMin)
				return false;

			if (!canRefineLower(MBServerStatics.STAT_CON_ID))
				return false;

			worked = this.conMod.compareAndSet(mod, newConMod);
		}
		this.trainedStatPoints--;
		this.statConBase = newCon;
		this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);
		this.applyBonuses();
		this.calculateSkills();
		return true;
	}

	public boolean refineInt(RefineMsg msg) {
		boolean worked = false;
		short newInt = (short) 0;

		while (!worked) {
			newInt = (short) (this.statIntBase - 1);
			short mod = (short) this.intMod.get();

			if (mod == 0)
				return false;
			short newIntMod = (short) (mod
					- 1);

			if (newInt < this.statIntMin)
				return false;

			if (!canRefineLower(MBServerStatics.STAT_INT_ID))
				return false;

			worked = this.intMod.compareAndSet(mod, newIntMod);
		}
		this.trainedStatPoints--;
		this.statIntBase = newInt;
		this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);

		verifySkillMax(msg);

		this.applyBonuses();
		this.calculateSkills();
		return true;
	}
	public boolean refineSpi() {
		boolean worked = false;
		short newSpi = (short) 0;
		while (!worked) {
			newSpi = (short) (this.statSpiBase - 1);
			short mod = (short) this.spiMod.get();
			if (mod == 0)
				return false;
			short newSpiMod = (short) (mod - 1);
			if (newSpi < this.statSpiMin)
				return false;
			if (!canRefineLower(MBServerStatics.STAT_SPI_ID))
				return false;
			worked = this.spiMod.compareAndSet(mod, newSpiMod);
		}
		this.trainedStatPoints--;
		this.statSpiBase = newSpi;
		this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);
		this.applyBonuses();
		this.calculateSkills();
		return true;
	}

	//this verifies stat doesn't fall too low to keep runes applied while refining
	private boolean canRefineLower(int stat) {
		for (CharacterRune cr : this.runes) {
			if (cr != null) {
				RuneBase rb = cr.getRuneBase();
				if (rb != null) {
					ArrayList<RuneBaseAttribute> attrs = rb.getAttrs();

					if (attrs != null)
						for (RuneBaseAttribute rba : attrs) {
							int attrID = rba.getAttributeID();
							int mod = rba.getModValue();
							if (stat == MBServerStatics.STAT_STR_ID) {
								if (attrID == MBServerStatics.RUNE_STR_MIN_NEEDED_ATTRIBUTE_ID && ((int) this.statStrBase <= mod))
									return false;
							} else if (stat == MBServerStatics.STAT_DEX_ID) {
								if (attrID == MBServerStatics.RUNE_DEX_MIN_NEEDED_ATTRIBUTE_ID && ((int) this.statDexBase <= mod))
									return false;
							} else if (stat == MBServerStatics.STAT_CON_ID) {
								if (attrID == MBServerStatics.RUNE_CON_MIN_NEEDED_ATTRIBUTE_ID && ((int) this.statConBase <= mod))
									return false;
							} else if (stat == MBServerStatics.STAT_INT_ID) {
								if (attrID == MBServerStatics.RUNE_INT_MIN_NEEDED_ATTRIBUTE_ID && ((int) this.statIntBase <= mod))
									return false;
							} else if (stat == MBServerStatics.STAT_SPI_ID)
								if (attrID == MBServerStatics.RUNE_SPI_MIN_NEEDED_ATTRIBUTE_ID && ((int) this.statSpiBase <= mod))
									return false;
						}
				}
			}
		}
		
		return true;
	}

	//checked on refining int to see if skills need refined also.
	private void verifySkillMax(RefineMsg msg) {

		ConcurrentHashMap<String, CharacterSkill> skills = getSkills();

		//make sure no skills are over the max number of trains
		int maxTrains = CharacterSkill.getMaxTrains((int) this.statIntBase);

		RefineMsg rm = new RefineMsg(msg.getNpcType(), msg.getNpcID(), 0, 0);

		for (CharacterSkill skill : skills.values()) {

			while (skill.getNumTrains() > maxTrains) {
				boolean worked = skill.refine(this, false); //refine skill, do not recalculate everything
				if (worked) {
					rm.setToken(skill.getToken());

					Dispatch dispatch = Dispatch.borrow(this, rm);
					DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

				} else {
					Logger.error("Failed to force refine of skill " + skill.getObjectUUID() + " by character " + this.getObjectUUID());
					break;
				}
			}
		}
	}

	public int getClassToken() {
		if (this.promotionClass != null)
			return this.promotionClass.getToken();
		else if (this.baseClass != null)
			return this.baseClass.getToken();
		return 0;
	}

	public int getRaceToken() {

		if (this.race == null)
			return 0;

		return this.race.getToken();
	}

	public void setLastTarget(GameObjectType type, int id) {
		this.lastTargetType = type;
		this.lastTargetID = id;
	}

	public GameObjectType getLastTargetType() {
		return this.lastTargetType;
	}

	public int getLastTargetID() {
		return this.lastTargetID;
	}

	public synchronized int getBindBuildingID() {
		return this.bindBuildingID;
	}

	public synchronized void setBindBuildingID(int value) {
		DbManager.PlayerCharacterQueries.SET_BIND_BUILDING(this, value);
		this.bindBuildingID = value;
	}
	
	public static Building getUpdatedBindBuilding(PlayerCharacter player){
		Building returnBuilding = null;

		//update bindBuilding based on Guild or nation TOL;

		if(player.getBindBuildingID() == 0) {

			returnBuilding = PlayerCharacter.getBindBuildingForGuild(player);
			
			if (returnBuilding != null)
				player.setBindBuildingID(returnBuilding.getObjectUUID());
			return returnBuilding;
		}
			returnBuilding = BuildingManager.getBuildingFromCache(player.getBindBuildingID());
			
			if (returnBuilding == null){
				returnBuilding = PlayerCharacter.getBindBuildingForGuild(player);
				
				if (returnBuilding != null)
					player.setBindBuildingID(returnBuilding.getObjectUUID());
			}	
			return returnBuilding;
	}
	
	public static Building getBindBuildingForGuild(PlayerCharacter player){

		Building returnBuilding;

		if (player.getGuild() == null || player.getGuild().isErrant())
			return null;

		if (player.getGuild().getOwnedCity() == null){

		if (player.getGuild().getNation().getOwnedCity() == null)
			return null;

		if (player.getGuild().getNation().getOwnedCity().getTOL() == null)
			return null;
		
		returnBuilding = player.getGuild().getNation().getOwnedCity().getTOL();
		player.setBindBuildingID(returnBuilding.getObjectUUID());
		return  returnBuilding;
		}
		
		if (player.getGuild().getOwnedCity().getTOL() == null)
			return null;
		
		returnBuilding = player.getGuild().getOwnedCity().getTOL();
		return returnBuilding;
	}

	public AbstractGameObject getLastTarget() {
		if (this.lastTargetType == GameObjectType.unknown)
			return null;

		switch (this.lastTargetType) {
		// Make sure these only return an object that is
		// already in the GOM, and doesn't reload from the DB
		case PlayerCharacter:
			return DbManager.getFromCache(GameObjectType.PlayerCharacter, this.lastTargetID);

		case Building:
			return DbManager.getFromCache(GameObjectType.Building, this.lastTargetID);

		case NPC:
			return NPC.getFromCache(this.lastTargetID);

		case Mob:
			return Mob.getFromCache(this.lastTargetID);

		case Item:
			return DbManager.getFromCache(GameObjectType.Item, this.lastTargetID);

		case Corpse:
			return DbManager.getFromCache(GameObjectType.Corpse, this.lastTargetID);

		default:

			// Ignore exception for MobLoot?  ***Check
			if (this.lastTargetType != GameObjectType.MobLoot)
				Logger.error( "getLastTarget() unhandled object type: "
						+ this.lastTargetType.toString());
		}
		return null;
	}

	public Vector3fImmutable getLastStaticLoc() {
		return this.lastStaticLoc;
	}

	public void setLastStaticLoc(Vector3fImmutable value) {
		this.lastStaticLoc = value;
	}

	public int getHidden() {
		return this.hidden;
	}

	public void setHidden(int value) {
		this.hidden = value;
	}

	public int getSeeInvis() {
		if (this.getDebug(8)) //<-added for see invis debug devcmd
			return 10000;
		return this.seeInvis;
	}

	public void setSeeInvis(int value) {
		this.seeInvis = value;
	}

	public long getLastPlayerAttackTime() {
		return this.lastPlayerAttackTime;
	}

	public void setLastPlayerAttackTime() {
		this.lastPlayerAttackTime = System.currentTimeMillis();
	}

	public void setLastMobAttackTime() {
		this.lastMobAttackTime = System.currentTimeMillis();
	}

	public void setLastUsedPowerTime() {
		this.lastUsedPowerTime = System.currentTimeMillis();
	}

	public void setLastTargetOfUsedPowerTime() {
		this.lastTargetOfUsedPowerTime = System.currentTimeMillis();
	}

	public void setLastNPCDialog(NPC value) {
		this.lastNPCDialog = value;
	}

	public NPC getLastNPCDialog() {
		return this.lastNPCDialog;
	}

	public void setLastContract(int value) {
		this.lastContract = value;
	}

	public void setPet(Mob mob) {

		if (mob == null)
			return;

		this.pet = mob;
	}

	public Mob getPet() {
		return this.pet;
	}

	public Mob getNecroPet(int i) {
		return this.necroPets.get(i);
	}

	
public static void auditNecroPets(PlayerCharacter player){
	int removeIndex =0;
	while(player.necroPets.size() >= 10){
		
		
		if (removeIndex == player.necroPets.size())
			break;
	
		Mob toRemove = player.necroPets.get(removeIndex);
		
		if (toRemove == null){
			removeIndex++;
			continue;
		}
		toRemove.dismissNecroPet(true);
		player.necroPets.remove(toRemove);
		removeIndex++;
		
		
	}
}

public static void resetNecroPets(PlayerCharacter player){
	for (Mob necroPet: player.necroPets)
		if (necroPet.isPet())
			necroPet.setMob();
}
	
	public void spawnNecroPet(Mob mob) {
		if (mob == null)
			return;
		if (mob.getMobBaseID() != 12021 && mob.getMobBaseID() != 12022)
			return;
		
		PlayerCharacter.auditNecroPets(this);
		PlayerCharacter.resetNecroPets(this);
	
		this.necroPets.add(mob);	
	}
	

	public void dismissPet() {
		if (this.pet != null) {
			this.pet.dismiss();
			this.pet = null;
		}
	}
	
public void dismissNecroPets() {

	
	if (this.necroPets.isEmpty())
		return;

		for (Mob necroPet: this.necroPets){
			
			try{
				necroPet.dismissNecroPet(true);
			}catch(Exception e){
				necroPet.setState(STATE.Disabled);
				Logger.error(e);
			}
				}
		this.necroPets.clear();
	}


	//called to verify player has correct item equipped for casting.
	public boolean validEquip(int slot, String type) {

		if (this.charItemManager == null)
			return false;

		Item item = this.charItemManager.getEquipped(slot);

		if (item == null)
			return false;

		ItemBase ib = item.getItemBase();
		if (ib != null) {

			if ((ib.getType().equals(ItemType.WEAPON))
					&& (ib.getSkillRequired().equals(type) || ib.getMastery().equals(type)))
				return true;

			return (ib.getType().equals(ItemType.ARMOR))
					&& (ib.getSkillRequired().equals(type));
		}

		return false;
	}

	public short getPCLevel() {
		short level = (short) Experience.getLevel(this.exp);
		if (this.promotionClass == null && level >= 10)
			return (short) 10;
		
		if (this.overFlowEXP > 0)
			return this.level;
		
		return level;
	}

	@Override
	public float getSpeed() {

		float speed;

		if (this.getAltitude() > 0)
			if (this.walkMode) {
				speed = race.getRaceType().getRunSpeed().getFlyWalk();
			}
			else {
				speed = race.getRaceType().getRunSpeed().getFlyRun();
			}
		else if (this.lastSwimming == true)
			speed = MBServerStatics.SWIMSPEED;
		else
			if (this.walkMode) {
				if (this.isCombat())
					speed = race.getRaceType().getRunSpeed().getWalkCombat();
				else
				speed = race.getRaceType().getRunSpeed().getWalkStandard();
			}
			else {
				if (this.isCombat())
					speed = race.getRaceType().getRunSpeed().getRunCombat();
				else
				speed = race.getRaceType().getRunSpeed().getRunStandard();
			}

		float endSpeed = speed * this.speedMod;

		if (endSpeed > 41 && !this.isCSR)
			endSpeed = 41;

		return endSpeed;
	}

	public synchronized void grantXP(int xp) {
		// Stop players from getting experience past the cap
		if (this.exp + xp >= Experience.getBaseExperience(MBServerStatics.LEVELCAP))
			xp = Experience.getBaseExperience(MBServerStatics.LEVELCAP) - this.exp + 1;

		if (xp == 0)
			xp = 1;

		boolean isNewLevel = false;
		boolean charReloadRequired = false;
		int remainingXP = xp;
		int neededXP = 0;

		// handle players that have not yet promoted.
		ClientConnection origin = this.getClientConnection();

		//not promoted at level 10, start checking for negative EXP
		if (this.promotionClass == null && this.getLevel() == 10) {

			if (this.getExp() == Experience.getBaseExperience(11)){
				if (this.overFlowEXP == 110000)
					return;

				if (this.overFlowEXP + xp > 110000){
					remainingXP = 110000 - this.overFlowEXP;
					this.overFlowEXP = 110000;


				}
				else{
					this.overFlowEXP += remainingXP;
				}

				GrantExperienceMsg gem = new GrantExperienceMsg(this, remainingXP);
				Dispatch dispatch = Dispatch.borrow(this, gem);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

				this.addDatabaseJob("EXP", MBServerStatics.FIVE_MINUTES);
				return;
				//didnt reach level 11 EXP to start overflow, add exp normally till we get here;
			}else{

				//Player exp begins negative exp, add remaing exp after level 11 to overflow
				if (this.getExp() + remainingXP >= Experience.getBaseExperience(11)){

					this.overFlowEXP = remainingXP - (Experience.getBaseExperience(11) - this.getExp());
					this.exp = Experience.getBaseExperience(11);

					GrantExperienceMsg grantExperienceMsg = new GrantExperienceMsg(this, remainingXP);
					Dispatch dispatch = Dispatch.borrow(this, grantExperienceMsg);
					DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

					this.addDatabaseJob("EXP", MBServerStatics.FIVE_MINUTES);
					return;

					//didnt reach negative exp yet, just do normal exp gain.
				}else{
					this.exp += remainingXP;
					GrantExperienceMsg grantExperienceMsg = new GrantExperienceMsg(this, remainingXP);
					remainingXP = 0;
					Dispatch dispatch = Dispatch.borrow(this, grantExperienceMsg);
					DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

					this.addDatabaseJob("EXP", MBServerStatics.FIVE_MINUTES);
					return;
				}
			}
		}

		if (this.overFlowEXP > 0){

			int nextLevel;

			if (level == 10)
				nextLevel = 12;
			else
				nextLevel = level + 2;

			int nextLevelEXP = Experience.getBaseExperience(nextLevel);

			// if overflow > 0, u have level 11 experience + overflow, but level is still 10 due to just promoting.
			//Use level + 2 experience for next level.
			this.overFlowEXP += 1;

			if (this.getExp() + this.overFlowEXP >= nextLevelEXP){

				int expToNextLevel = nextLevelEXP - this.getExp();
				this.overFlowEXP -= expToNextLevel;
				this.exp  += expToNextLevel;
				this.level++;
				charReloadRequired = true;

				GrantExperienceMsg grantExperienceMsg = new GrantExperienceMsg(this, 1);
				Dispatch dispatch = Dispatch.borrow(this, grantExperienceMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

				SetObjectValueMsg upm = new SetObjectValueMsg(this, 9);
				DispatchMessage.dispatchMsgToInterestArea(this, upm,  DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
				checkGuildStatus();
				this.addDatabaseJob("EXP", MBServerStatics.FIVE_MINUTES);
				//  double overflow exp used up, remaining overflow will just add to level + 1.
			}else if (this.getExp() + this.overFlowEXP >= Experience.getBaseExperience(level + 1)){
				int nextExperience = Experience.getBaseExperience(level + 1) + this.overFlowEXP;
				this.exp = nextExperience;
				this.level ++;
				charReloadRequired = true;
				this.overFlowEXP = 0;
				GrantExperienceMsg grantExperienceMsg = new GrantExperienceMsg(this, 1);
				Dispatch dispatch = Dispatch.borrow(this, grantExperienceMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
				SetObjectValueMsg upm = new SetObjectValueMsg(this, 9);
				DispatchMessage.dispatchMsgToInterestArea(this, upm,  DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
				checkGuildStatus();
				this.addDatabaseJob("EXP", MBServerStatics.FIVE_MINUTES);
			}

		}else{
			// Hand out each Level one at a time.
			isNewLevel = Experience.getLevel(exp + remainingXP) > this.getLevel();

			if (isNewLevel) {
				neededXP = Experience.getBaseExperience(this.getLevel() + 1) - this.exp;

				charReloadRequired = true;
				this.exp += neededXP;
				this.level++;

				GrantExperienceMsg grantExperienceMsg = new GrantExperienceMsg(this, neededXP);
				Dispatch dispatch = Dispatch.borrow(this, grantExperienceMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

				remainingXP -= neededXP;

				//Send newLevel.
				SetObjectValueMsg upm = new SetObjectValueMsg(this, 9);
				DispatchMessage.dispatchMsgToInterestArea(this, upm,  DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
				checkGuildStatus();
			} else {

				this.exp += remainingXP;
				GrantExperienceMsg grantExperienceMsg = new GrantExperienceMsg(this, remainingXP);
				remainingXP = 0;
				Dispatch dispatch = Dispatch.borrow(this, grantExperienceMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

				this.addDatabaseJob("EXP", MBServerStatics.FIVE_MINUTES);
			}
		}
		
		if (charReloadRequired) {
			this.update();
			this.incVer();
			this.recalculate();
			this.calculateMaxHealthManaStamina();
			this.setHealth(this.healthMax);
			this.mana.set(this.manaMax);
			this.stamina.set(this.staminaMax);
			//LoadJob.reloadCharacter(this);
			DbManager.PlayerCharacterQueries.SET_PROPERTY(this, "char_experience", this.exp);
			//			updateDatabase();
			DbManager.AccountQueries.INVALIDATE_LOGIN_CACHE(this.getObjectUUID(), "character");
		}
	}

	//This checks if a player meets the requirements to be in current guild.
	public void checkGuildStatus() {

		Guild g = this.guild;

		if (g == null || g.isErrant() || GuildStatusController.isGuildLeader(guildStatus))
			return;

		//check level
		int curLevel = (int) getPCLevel();
		if (curLevel < g.getRepledgeMin() || curLevel >= g.getRepledgeKick()) {
			//TODO kick from guild
			g.removePlayer(this,GuildHistoryType.LEAVE);
			ChatManager.chatGuildInfo(this, "You no longer meet the level requirements for the guild.");
		}
	}

	public void calculateSpeedMod() {
		// get base race speed modifer


		//this is retarded. *** Refactor
		//        if (this.race != null) {
		//            int ID = this.race.getObjectUUID();
		//            if (ID == 2004 || ID == 2005)
		//                this.raceRunMod = 1.21f; // centaur run bonus 22%
		////            else if (ID == 2017)
		////                this.raceRunMod = 1.14f; // mino run bonus 15%
		//            else
		//                this.raceRunMod = 1;
		//        } else
		//            this.raceRunMod = 1;


		float bonus = 1f;

		//        // TODO: hardcoded, as didnt have time to introduce DB column to base object
		//        if (baseClass.getName().equals("Fighter") || baseClass.getName().equals("Rogue"))
		//            bonus += .05f;

		// get running skill
		if (this.skills != null) {
			CharacterSkill running = this.skills.get("Running");
			if (running != null){

				float runningBonus = (float) (Math.log(Math.round(running.getModifiedAmount())*.01f) / Math.log(2) *.50f);
				runningBonus = (float) (Math.pow(2, runningBonus) - 1);
				runningBonus += 1;
				runningBonus *= .25f;
				bonus += runningBonus;

			}
		}

		if (this.bonuses != null)
			// get rune and effect bonuses
			bonus += this.bonuses.getFloatPercentNullZero(ModType.Speed, SourceType.None);

		// TODO get equip bonus
		this.update();
		this.speedMod = bonus;
	}

	public ClientConnection getClientConnection() {
		return SessionManager.getClientConnection(this);
	}

	/*
	 * Serializing
	 */
	
	public static void __serializeForClientMsg(PlayerCharacter playerCharacter,ByteBufferWriter writer) throws SerializationException {
		serializeForClientCommon(playerCharacter,writer, true, false, false, false);
	}

	public static void serializeForClientMsgLogin(PlayerCharacter playerCharacter,ByteBufferWriter writer) throws SerializationException {
		serializeForClientCommon(playerCharacter,writer, true, false, false, false);
	}

	public static void serializeForClientMsgCommit(PlayerCharacter playerCharacter,ByteBufferWriter writer) throws SerializationException {
		serializeForClientCommon(playerCharacter,writer, true, true, false, false);
	}

	public static void serializeForClientMsgFull(PlayerCharacter playerCharacter,ByteBufferWriter writer) throws SerializationException {
		serializeForClientCommon(playerCharacter,writer, false, false, false, false);
	}

	
	public static void serializeForClientMsgOtherPlayer(PlayerCharacter playerCharacter,ByteBufferWriter writer) throws SerializationException {
		serializeForClientCommon(playerCharacter,writer, false, false, true, false);
	}

	
	public static void serializePlayerForClientMsgOtherPlayer(PlayerCharacter playerCharacter,ByteBufferWriter writer, boolean hideAsciiLastName) throws SerializationException {
		serializeForClientCommon(playerCharacter,writer, false, false, true, hideAsciiLastName);
	}

	// TODO what is a Fresh Char?
	private static void serializeForClientCommon(PlayerCharacter playerCharacter,ByteBufferWriter writer, boolean loginData, boolean freshChar, boolean otherPlayer, boolean hideAsciiLastName)
			throws SerializationException {

		/*
		 * RUNES
		 */
		// Handle Applied Runes
		writer.putInt(0); // Pad
		writer.putInt(0); // Pad

		// Put number of runes
		//We need to send all runes to everyone, otherwise playerCharacter will cause major issues
		if (playerCharacter.promotionClass != null)
			writer.putInt(playerCharacter.runes.size() + 3);
		else
			writer.putInt(playerCharacter.runes.size() + 2);

		// Cant forget that Race and baseClass are technically Runes :0
		if (playerCharacter.subRaceID != 0){
			writer.putInt(1); // For Race
			writer.putInt(0); // Pad
			writer.putInt(playerCharacter.subRaceID);

			writer.putInt(Enum.GameObjectType.Race.ordinal());
			writer.putInt(playerCharacter.subRaceID);
		}else
			playerCharacter.race.serializeForClientMsg(writer);
		if (playerCharacter.promotionClass != null) {
			BaseClass.serializeForClientMsg(playerCharacter.baseClass,writer, 2);
			PromotionClass.serializeForClientMsg(playerCharacter.promotionClass,writer);
		} else
			BaseClass.serializeForClientMsg(playerCharacter.baseClass,writer, 3);

		// Put runes.

		for (CharacterRune rb : playerCharacter.runes) {
			CharacterRune.serializeForClientMsg(rb,writer);
		}

		/*
		 * STATS
		 */
		// Number of Stats to follow
		writer.putInt(5);

		writer.putInt(MBServerStatics.STAT_STR_ID); // Strength ID
		writer.putInt(freshChar ? 0 : playerCharacter.getStrMod());

		writer.putInt(MBServerStatics.STAT_SPI_ID); // Spirit ID
		writer.putInt(freshChar ? 0 : playerCharacter.getSpiMod());

		writer.putInt(MBServerStatics.STAT_CON_ID); // Constitution ID
		writer.putInt(freshChar ? 0 : playerCharacter.getConMod());

		writer.putInt(MBServerStatics.STAT_DEX_ID); // Dexterity ID
		writer.putInt(freshChar ? 0 : playerCharacter.getDexMod());

		writer.putInt(MBServerStatics.STAT_INT_ID); // Intelligence ID
		writer.putInt(freshChar ? 0 : playerCharacter.getIntMod());

		// Handle Info
		playerCharacter.title._serializeFirstName(writer, playerCharacter.firstName);
		playerCharacter.title._serializeLastName(writer, playerCharacter.lastName, hideAsciiLastName, playerCharacter.asciiLastName);

		// Unknown
		writer.putInt(0);

		writer.putString(ConfigManager.MB_WORLD_NAME.getValue());
		writer.putInt(MBServerStatics.worldMapID);

		writer.put((byte) 1); // End Datablock byte
		writer.putInt(0); // Unsure, Pad?
		writer.putInt(playerCharacter.getObjectType().ordinal());
		writer.putInt(playerCharacter.getObjectUUID());

		// Perhaps playerCharacter is loc and the next 3 are Facing dir?
		writer.putFloat(1); // Unknown
		writer.putFloat(playerCharacter.race.getRaceType().getScaleHeight()); // Unknown
		writer.putFloat(1); // Unknown
		
		writer.putVector3f(playerCharacter.getLoc());
		writer.putFloat(playerCharacter.faceDir.getRotation()); // Rotation, direction

		// facing

		// Running trains.

		if (otherPlayer){
			CharacterSkill runSkill = playerCharacter.skills.get("Running");
			if (runSkill == null)
				// Logger.log.log(
				// LogEventType.WARNING,
				// "Failed to find the 'Running Skill' when serializing PlayerCharacter '"
				// + playerCharacter.getCombinedName() + "'");
				// TODO put int=0 for now.
				writer.putInt(0);
			else
				writer.putInt(runSkill.getNumTrains());
		}else
			writer.putInt(0);


		ArrayList<Item> equipped = playerCharacter.charItemManager.getEquippedList();
		
		writer.putInt(equipped.size());
		for (Item item: equipped){
			Item._serializeForClientMsg(item, writer);
		}
		writer.putInt(playerCharacter.getRank());

		writer.putInt(playerCharacter.getLevel());
		if (loginData)
			writer.putInt(5);
		else
			writer.putInt(playerCharacter.getIsSittingAsInt()); // 5
		writer.putInt(playerCharacter.getIsWalkingAsInt()); // 1
		writer.putInt(playerCharacter.getIsCombatAsInt()); // 1
		writer.putInt(playerCharacter.getIsFlightAsInt()); // 2 or 3

		writer.putInt(playerCharacter.getIsLfGroupAsInt()); // 1

		// if (loginData)
		// writer.putInt(0);
		// else
		writer.putInt(playerCharacter.getHeadlightsAsInt());


		if (playerCharacter.getRegion() != null && !loginData){
			Building building = Regions.GetBuildingForRegion(playerCharacter.getRegion());
			
			if (building == null){
				writer.putInt(0);
				writer.putInt(0);
			}else{
				writer.putInt(GameObjectType.Building.ordinal());
				writer.putInt(building.getObjectUUID());
			}
		
		}
		else{
			writer.putInt(0);
			writer.putInt(0);
		}


		writer.put((byte)0);
		writer.put((byte)0);
		writer.put((byte)0);
		writer.putInt(0);
		writer.put((byte)0);
		writer.put((byte)0);
		writer.put((byte)0);
		
//		writer.putInt(0);
//		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		
		if (!playerCharacter.isAlive() && otherPlayer) {
			writer.putInt(0);
			writer.putInt(0);
		}
		

		

		//TODO FIGURE OUT THE REAL SEARLIZATION FOR NEXT 2 SHORTS?
		writer.putInt(playerCharacter.skinColor); // Skin Color
		writer.putFloat(20);
		writer.put((byte)0); //Unknown
		
		//unknown object
		writer.putInt(0);
		writer.putInt(0);
		
		//unknown type
		writer.putInt(0);
		//0x4080 should be the next short here, instead it wraps 0's down their in for loops.. seriously.. who wrote playerCharacter shit.
		// playerCharacter aint right!
		// ByteBufferUtils.putString(writer, playerCharacter.guild.getName());
		// writer.putInt(playerCharacter.getGuild().getUUID());
		// ByteBufferUtils.putString(writer, playerCharacter.guild.getNation().getName());
		// writer.putInt(playerCharacter.getGuild().getNation().getUUID());
		Guild.serializeForClientMsg(playerCharacter.getGuild(),writer, playerCharacter, false);

		//Send Tokens for race/class/promotion (disciplines?)
		if (playerCharacter.promotionClass != null)
			writer.putInt(3);
		else
			writer.putInt(2);
		writer.putInt(playerCharacter.race.getToken());
		writer.putInt(playerCharacter.baseClass.getToken());
		if (playerCharacter.promotionClass != null)
			writer.putInt(playerCharacter.promotionClass.getToken());
		//		writer.putInt(2); // Unknown Counter
		//		writer.putInt(0x04C1BE88); // Unknown
		//		writer.putInt(0x0F651512); // Unknown

		writer.putFloat(playerCharacter.altitude); // altitude?
		writer.putFloat(playerCharacter.altitude); // altitude?
		writer.put((byte) 0); // End Datablock byte

		writer.putFloat(playerCharacter.healthMax);
		writer.putFloat(playerCharacter.health.get());

		writer.put((byte) 0); // End Datablock byte
		//size


		if (loginData){
			writer.putInt(0);
		}else{
			int	indexPosition = writer.position();
			writer.putInt(0); //placeholder for item cnt
			int total = 0;
			//	Logger.info("",""+ playerCharacter.getEffects().size());
			for (Effect eff : playerCharacter.getEffects().values()) {
				if (eff.getPower() == null && otherPlayer)
					continue;
				if ( !eff.serializeForLoad(writer))
					continue;
				++total;

			}

			writer.putIntAt(total, indexPosition);
		}

		if (otherPlayer) {
			writer.put((byte) 0); // End Datablock Byte
			return;

		}

		//made up for sendalleffects
		//writer.putInt(0); // Pad
		//writer.put((byte) 0); // End Datablock byte
		writer.putInt(playerCharacter.getUnusedStatPoints());
		writer.putInt(playerCharacter.getLevel());
		writer.putInt(playerCharacter.getExp() + playerCharacter.overFlowEXP);
		writer.putFloat(playerCharacter.manaMax);
		writer.putFloat(playerCharacter.mana.get());
		writer.putFloat(playerCharacter.staminaMax);
		writer.putFloat(playerCharacter.stamina.get());
		writer.putInt(playerCharacter.getAtrHandOne());
		writer.putInt(playerCharacter.getAtrHandTwo());
		writer.putInt(playerCharacter.getDefenseRating());

		if (MBServerStatics.POWERS_DEBUG) //debug mode, grant lots of trains
			writer.putInt(1000);
		else
			writer.putInt(playerCharacter.trainsAvailable.get());

		/*
		 * Skills
		 */
		if (loginData)
			writer.putInt(0); // Skip skills
		else {
			writer.putInt(playerCharacter.skills.size());
			Iterator<String> it = playerCharacter.skills.keySet().iterator();
			while (it.hasNext()) {
				String name = it.next();
				CharacterSkill.serializeForClientMsg(playerCharacter.skills.get(name),writer);
			}
		}

		/*
		 * Powers
		 */
		if (loginData)
			writer.putInt(0); // Skip Powers
		else
			if (MBServerStatics.POWERS_DEBUG) //debug mode, grant all powers
				PowersManager.testPowers(writer);
			else {
				writer.putInt(playerCharacter.powers.size());
				for (CharacterPower sp : playerCharacter.powers.values()) {
					CharacterPower.serializeForClientMsg(sp,writer);
				}
			}

		/*
		 * Inventory
		 */
		if (loginData) {
			writer.putInt(0); // Skip Inventory
			writer.putInt(playerCharacter.getInventoryCapacity()); // Inventory Capacity

		} else {
			ArrayList<Item> inv = playerCharacter.charItemManager.getInventory(true);
			Item.putList(writer, inv, false, playerCharacter.getObjectUUID());
			writer.putInt(playerCharacter.getInventoryCapacityRemaining());
		}

		/*
		 * Bank
		 */
		if (loginData) {
			writer.putInt(0); // Skip Bank
			writer.putInt(AbstractCharacter.getBankCapacity()); // Bank Capacity

		} else {
			ArrayList<Item> bank = playerCharacter.charItemManager.getBank();

			Item.putList(writer, bank, false, playerCharacter.getObjectUUID());
			writer.putInt(playerCharacter.getBankCapacityRemaining());
		}
		//load player friends.
		if (loginData)
			writer.putInt(0);
			else{
				HashSet<Integer> friendMap = PlayerFriends.PlayerFriendsMap.get(playerCharacter.getObjectUUID());
				if (friendMap == null)
					writer.putInt(0);
				else{
					writer.putInt(friendMap.size());
					for (int friendID : friendMap){
						PlayerCharacter friend = PlayerCharacter.getFromCache(friendID);
						//shouldn't get here, but if null serialize blank friend.
						if (friend == null){
							writer.putInt(0);
							writer.putInt(0);
							writer.putInt(0);
							writer.putInt(0);
							writer.putInt(0);
						}else{
							writer.putInt(friend.getObjectType().ordinal());
							writer.putInt(friend.getObjectUUID());
							writer.putString(friend.getName());
							boolean online = SessionManager.getPlayerCharacterByID(friend.getObjectUUID()) != null ? true : false;
							writer.putInt(online ? 0 : 1);
							writer.putInt(friend.friendStatus.ordinal());
						}
					
					}
				}
			}
		
		
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);

		writer.putShort((short) 0);
		writer.put((byte) 0);
		// playerCharacter is for send self in enter world (full character)
		if (!loginData && !freshChar) {
			int size = playerCharacter.getRecycleTimers().size();
			writer.putInt(size);
			if (size > 0)
				for (int token : playerCharacter.getRecycleTimers().keySet()) {

					JobContainer frtj = playerCharacter.getRecycleTimers().get(token);
					long timeLeft = frtj.timeOfExection() - System.currentTimeMillis();
					writer.putInt(token);
					writer.putInt((int) timeLeft / 1000);
				}
			DateTime enterWorld = new DateTime(playerCharacter.timestamps.get("EnterWorld"));
			writer.putDateTime(enterWorld);

			writer.putInt(0x49EF1E98); //DUnno what playerCharacter is.
			writer.putFloat(DateTime.now().hourOfDay().get()); //daylight in float.
			writer.putFloat(6); //interval of light to change per game hour //float
			//writer.putInt(1637194901); //playerCharacter is actually an opcode taht is in recordings, no clue what it is, dumped it and it changes nothing
		} else {
            writer.put((byte) 0); //added to compensate the cooldown check.

            //add server up or down
            int serverUp = LoginServer.worldServerRunning ? 1 : 0;

            if (playerCharacter.account == null)
                serverUp = 0;

            if ((playerCharacter.account.status.equals(AccountStatus.ADMIN) == false) &&
                    (playerCharacter.account.status.equals(MBServerStatics.worldAccessLevel) == false))
                serverUp = 0;

            writer.putInt(serverUp);
            writer.putInt(0); // effects, not sure used by players
            writer.put((byte) 0); // End Player Datablock
        }

	}



	public static PlayerCharacter generatePCFromCommitNewCharacterMsg(Account a, CommitNewCharacterMsg msg, ClientConnection clientConnection)
			 {

		String firstName = msg.getFirstName().trim();
		String lastName = msg.getLastName().trim();

		if (firstName.length() < 3){
			LoginServerMsgHandler.sendInvalidNameMsg(firstName, lastName,  MBServerStatics.INVALIDNAME_FIRSTNAME_MUST_BE_LONGER,
					clientConnection);
			return null;
		}
			
		// Ensure names are below required length
		if (firstName.length() > 15 || lastName.length() > 15){
			LoginServerMsgHandler.sendInvalidNameMsg(firstName, lastName, MBServerStatics.INVALIDNAME_FIRSTANDLAST_MUST_BE_SHORTER,
					clientConnection);
			return null;
		}

		// Check if firstname is valid
		if (MiscUtils.checkIfFirstNameInvalid(firstName)){
			LoginServerMsgHandler.sendInvalidNameMsg(firstName, lastName,  MBServerStatics.INVALIDNAME_PLEASE_CHOOSE_ANOTHER_FIRSTNAME,
					clientConnection);
			return null;
		}
			
		// Check if last name is valid
		if (MiscUtils.checkIfLastNameInvalid(lastName)){
			LoginServerMsgHandler.sendInvalidNameMsg(firstName, lastName, MBServerStatics.INVALIDNAME_LASTNAME_UNAVAILABLE,
					clientConnection);
			return null;
		}
			
		// Verify Race
		int raceID = msg.getRace();

		Race race = Race.getRace(raceID);

		if (race == null) {
			Logger.info("Invalid RaceID: " + raceID);
			return null;
		}

		// Verify BaseClass Object.
		int baseClassID = msg.getBaseClass();
		BaseClass baseClass = DbManager.BaseClassQueries.GET_BASE_CLASS(baseClassID);

		if (baseClass == null) {
			Logger.info("Invalid BaseClasID: " + baseClassID);
			return null;
		}

		// Verify Race/baseClass combo.
		boolean valid = false;

		for (BaseClass bc : race.getValidBaseClasses()) {

			if (bc.getObjectUUID() == baseClassID) {
				valid = true;
				break;
			}
		}

		if (!valid) {
			Logger.info("Invalid BaseClass/Race Combo");
			return null;
		}

		// Verify HairStyle/BeardStyle/SkinColor/HairColor/BeardColor
		int hairStyleID = msg.getHairStyle();
		int beardStyleID = msg.getBeardStyle();
		int skinColorID = msg.getSkinColor();
		int hairColorID = msg.getHairColor();
		int beardColorID = msg.getBeardColor();

		if (!race.isValidHairStyle(hairStyleID)) {
			Logger.info("Invalid HairStyleID: " + hairStyleID + " for race: " + race.getName());
			return null;
		}

		if (!race.isValidSkinColor(skinColorID)) {
			Logger.info("Invalid skinColorID: " + skinColorID + " for race: " + race.getName());
			return null;
				 }

		 if (!race.isValidHairColor(hairColorID)) {
			Logger.info("Invalid hairColorID: " + hairColorID + " for race: " + race.getName());
			return null;
				 }

		 if (!race.isValidBeardColor(beardColorID)) {
			Logger.info("Invalid beardColorID: " + beardColorID + " for race: " + race.getName());
			return null;
				 }

		// Get stat modifiers
		int strMod = msg.getStrengthMod();
		int dexMod = msg.getDexterityMod();
		int conMod = msg.getConstitutionMod();
		int intMod = msg.getIntelligenceMod();
		int spiMod = msg.getSpiritMod();
		

		if (intMod < -5 || dexMod < -5 || conMod < -5 || strMod <-5 || spiMod < -5) {
			Logger.error("NEGATIVE STAT CHEAT ATTEMPTED! ACCOUNT: " +a.getUname() + "(" + a.getObjectUUID() +  ") IP ADDRESS: " +clientConnection.getClientIpAddress());
			return null;
		}

		// calculate current stats:
		short strCur = (short) (race.getStrStart() + baseClass.getStrMod() + strMod);
		short dexCur = (short) (race.getDexStart() + baseClass.getDexMod() + dexMod);
		short conCur = (short) (race.getConStart() + baseClass.getConMod() + conMod);
		short intCur = (short) (race.getIntStart() + baseClass.getIntMod() + intMod);
		short spiCur = (short) (race.getSpiStart() + baseClass.getSpiMod() + spiMod);

		// calculate max stats:
		short strMax = race.getStrMax();
		short dexMax = race.getDexMax();
		short conMax = race.getConMax();
		short intMax = race.getIntMax();
		short spiMax = race.getSpiMax();

		// Verify not too many runes applied
		int numRunes = msg.getNumRunes();

		if (numRunes > 16) {
			Logger.info("Too many Runes applied");
			return null;
		}

		// Get Runes
		// ArrayList<RuneBase> characterRunesUsed = new ArrayList<RuneBase>();
		// ArrayList<Byte> subtypesUsed = new ArrayList<Byte>();
		int remainingPoints = race.getStartingPoints() - strMod - dexMod - conMod - intMod - spiMod;

		int[] characterRunes = msg.getRunes();

		HashSet<Byte> usedRunesSubType = new HashSet<>();
		HashSet<RuneBase> usedRunes = new HashSet<>();

		// So that all the penalties can be added at the end.
		ConcurrentHashMap<String, Integer> penalties = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

		penalties.put("StrCur", 0);
		penalties.put("StrMax", 0);
		penalties.put("DexCur", 0);
		penalties.put("DexMax", 0);
		penalties.put("ConCur", 0);
		penalties.put("ConMax", 0);
		penalties.put("IntCur", 0);
		penalties.put("IntMax", 0);
		penalties.put("SpiCur", 0);
		penalties.put("SpiMax", 0);

		PriorityQueue<Map.Entry<Integer, RuneBase>> orderedRunes = new PriorityQueue<>(14,
				new Comparator<Map.Entry<Integer, RuneBase>>() {

			@Override
			public int compare(Entry<Integer, RuneBase> o1, Entry<Integer, RuneBase> o2) {
				return o1.getKey() - o2.getKey();
			}
		});

		// Figure out which Runes we are adding.
		for (int i : characterRunes) {
			// Zero skip
			if (i == 0)
				continue;

			// Skip the Race and BaseClass runes... already dealt with.
			if (i == raceID || i == baseClassID)
				continue;

			RuneBase runeBase = RuneBase.getRuneBase(i);

			// Null check
			if (runeBase == null) {
				Logger.info("GOM returned NULL RuneBase");
				return null;
			}

			// Validate Rune against Race
			if (!race.isAllowedRune(runeBase)) {
				Logger.info("Trait Not valid for Race");
				return null;
			}

			// Validate BaseClass against Race
			if (!baseClass.isAllowedRune(runeBase)) {
				Logger.info("Trait Not valid for BaseClass");
				return null;
			}

			int previous_size = usedRunes.size();
			int previous_subtype = usedRunesSubType.size();

			usedRunes.add(runeBase);
			usedRunesSubType.add(runeBase.getSubtype());

			// Duplicate Rune check
			if (usedRunes.size() <= previous_size) {
				Logger.info("Duplicate RuneBase");
				return null;
			}

			// Duplicate Subtype check
			if (runeBase.getSubtype() != 0 && usedRunesSubType.size() <= previous_subtype) {
				Logger.info("Duplicate RuneBase Subtype");
				return null;
			}

			int maxValue = 0;

			// Every attempt is made to load MIN_NEEDED_ATTRIBUTES first.

			if (runeBase.getAttrs() != null)
				for (RuneBaseAttribute rba : runeBase.getAttrs()) {
					if (rba.getAttributeID() == MBServerStatics.RUNE_STR_MIN_NEEDED_ATTRIBUTE_ID
							|| rba.getAttributeID() == MBServerStatics.RUNE_DEX_MIN_NEEDED_ATTRIBUTE_ID
							|| rba.getAttributeID() == MBServerStatics.RUNE_CON_MIN_NEEDED_ATTRIBUTE_ID
							|| rba.getAttributeID() == MBServerStatics.RUNE_INT_MIN_NEEDED_ATTRIBUTE_ID
							|| rba.getAttributeID() == MBServerStatics.RUNE_SPI_MIN_NEEDED_ATTRIBUTE_ID) {
						maxValue = rba.getModValue();
						if (runeBase.getName().equals("Giant's Blood"))
							maxValue = 45; // Take care of the Giant's Blood special
						// case.
						break;
					}
				}

			orderedRunes.add(new AbstractMap.SimpleEntry<>(maxValue, runeBase));
		}

		while (orderedRunes.size() > 0) {
			RuneBase rb = orderedRunes.remove().getValue();
			ArrayList<RuneBaseAttribute> attrs = rb.getAttrs();

			if (attrs != null)
				for (RuneBaseAttribute abr : attrs) {

					int attrID = abr.getAttributeID();
					int value = abr.getModValue();

					switch (attrID) {
					case MBServerStatics.RUNE_COST_ATTRIBUTE_ID:

						Logger.info( "Bought " + rb.getName() + " for " + value + " points. "
								+ (remainingPoints - value) + " left.");

						if ((remainingPoints - value) >= 0) {
							remainingPoints -= value;
							continue;
						}
						Logger.info("Not enough points left");
						return null;
					case MBServerStatics.RUNE_STR_MIN_NEEDED_ATTRIBUTE_ID:

						if (strCur >= value)
							continue;

						Logger.info("STR fails to meet Rune Minimum --> " + rb.getName());
						return null;
					case MBServerStatics.RUNE_DEX_MIN_NEEDED_ATTRIBUTE_ID:

						if (dexCur >= value)
							continue;

						Logger.info("DEX fails to meet Rune Minimum --> " + rb.getName());
						return null;
					case MBServerStatics.RUNE_CON_MIN_NEEDED_ATTRIBUTE_ID:

						if (conCur >= value)
							continue;

						Logger.info("CON fails to meet Rune Minimum --> " + rb.getName());
						return null;
					case MBServerStatics.RUNE_INT_MIN_NEEDED_ATTRIBUTE_ID:

						if (intCur >= value)
							continue;

						Logger.info("INT fails to meet Rune Minimum --> " + rb.getName());
						return null;
					case MBServerStatics.RUNE_SPI_MIN_NEEDED_ATTRIBUTE_ID:

						if (spiCur >= value)
							continue;

						Logger.info("SPI fails to meet Rune Minimum --> " + rb.getName());
						return null;
					case MBServerStatics.RUNE_STR_ATTRIBUTE_ID:

						if (value < 0)
							penalties.put("StrCur", (penalties.get("StrCur") + value));
						else
							strCur += value;
						continue;

					case MBServerStatics.RUNE_DEX_ATTRIBUTE_ID:
						if (value < 0)
							penalties.put("DexCur", (penalties.get("DexCur") + value));
						else
							dexCur += value;
						continue;
					case MBServerStatics.RUNE_CON_ATTRIBUTE_ID:
						if (value < 0)
							penalties.put("ConCur", (penalties.get("ConCur") + value));
						else
							conCur += value;
						continue;
					case MBServerStatics.RUNE_INT_ATTRIBUTE_ID:
						if (value < 0)
							penalties.put("IntCur", (penalties.get("IntCur") + value));
						else
							intCur += value;
						continue;
					case MBServerStatics.RUNE_SPI_ATTRIBUTE_ID:
						if (value < 0)
							penalties.put("SpiCur", (penalties.get("SpiCur") + value));
						else
							spiCur += value;
						continue;
					case MBServerStatics.RUNE_STR_MAX_ATTRIBUTE_ID:
						if (value < 0)
							penalties.put("StrMax", (penalties.get("StrMax") + value));
						else
							strMax += value;
						continue;
					case MBServerStatics.RUNE_DEX_MAX_ATTRIBUTE_ID:
						if (value < 0)
							penalties.put("DexMax", (penalties.get("DexMax") + value));
						else
							dexMax += value;
						continue;
					case MBServerStatics.RUNE_CON_MAX_ATTRIBUTE_ID:
						if (value < 0)
							penalties.put("ConMax", (penalties.get("ConMax") + value));
						else
							conMax += value;
						continue;
					case MBServerStatics.RUNE_INT_MAX_ATTRIBUTE_ID:
						if (value < 0)
							penalties.put("IntMax", (penalties.get("IntMax") + value));
						else
							intMax += value;
						continue;
					case MBServerStatics.RUNE_SPI_MAX_ATTRIBUTE_ID:
						if (value < 0)
							penalties.put("SpiMax", (penalties.get("SpiMax") + value));
						else
							spiMax += value;
						continue;

					default:
						Logger.info("Unknown ATTRIBUTE_ID while checking RuneBaseAttributes: " + attrID);
						return null;
					}
				}
		}

		// Add in all of the penalties.
		strCur += penalties.get("StrCur");
		strMax += penalties.get("StrMax");
		dexCur += penalties.get("DexCur");
		dexMax += penalties.get("DexMax");
		conCur += penalties.get("ConCur");
		conMax += penalties.get("ConMax");
		intCur += penalties.get("IntCur");
		intMax += penalties.get("IntMax");
		spiCur += penalties.get("SpiCur");
		spiMax += penalties.get("SpiMax");

		int kitID = msg.getKit();

		// get the correctKit
		int raceClassID = Kit.GetKitIDByRaceClass(raceID, baseClassID);
		ArrayList<Kit> allKits = Kit.RaceClassIDMap.get(raceClassID);

		Kit kit = null;

		for (Kit k : allKits) {
			if (k.getKitNumber() == kitID) {
				kit = k;
				break;
			}
		}

		if (kit == null) {
			Logger.info("Unable to find matching kitID: " + kitID);
			return null;
		}

		byte runningTrains = 0;
		PlayerCharacter playerCharacter;

		//Synchronized block to allow exclusive access when confirming
		//uniqueness of FirstName and subsequently saving the new record
		//to the database with that FirstName
		synchronized (FirstNameLock) {
			// Test if FirstName already exists.
			// This must be the very last check before calling the
			// DB to create the character record
			if (DbManager.PlayerCharacterQueries.IS_CHARACTER_NAME_UNIQUE(firstName) == false){
					LoginServerMsgHandler.sendInvalidNameMsg(firstName, lastName, MBServerStatics.INVALIDNAME_FIRSTNAME_UNAVAILABLE,
							clientConnection);
				return null;
			}
			
			// Make PC
			PlayerCharacter pcWithoutID = new PlayerCharacter( firstName, lastName, (short) strMod, (short) dexMod, (short) conMod,
					(short) intMod, (short) spiMod, Guild.getErrantGuild(), runningTrains, a, race, baseClass, (byte) skinColorID, (byte) hairColorID,
					(byte) beardColorID, (byte) beardStyleID, (byte) hairStyleID);

			try {
				playerCharacter = DbManager.PlayerCharacterQueries.ADD_PLAYER_CHARACTER(pcWithoutID);
			} catch (Exception e) {
				Logger.error("generatePCFromCommitNewCharacterMsg", "An error occurred while saving new PlayerCharacter to DB", e);
				return null;
			}

			if (playerCharacter == null) {
				Logger.info("GOM Failed to create PlayerCharacter");
				return null;
			}

		} // END synchronized(FirstNameLock)

		// Add creation runes
		for (RuneBase rb : usedRunes) {
			CharacterRune runeWithoutID = new CharacterRune(rb, playerCharacter.getObjectUUID());
			CharacterRune characterRune;
			try {
				characterRune = DbManager.CharacterRuneQueries.ADD_CHARACTER_RUNE(runeWithoutID);
			} catch (Exception e) {
				characterRune = null;
			}

			if (characterRune == null) {
				playerCharacter.deactivateCharacter();
				Logger.info("GOM Failed to create CharacterRune");
				return null;
			}

			playerCharacter.addRune(characterRune);
		}

		if (hairStyleID != 0) {
			// Create Hair
			Item tempHair = new Item( ItemBase.getItemBase(hairStyleID), playerCharacter.getObjectUUID(), OwnerType.PlayerCharacter,
					(byte) 0, (byte) 0, (short) 1, (short) 1, false, false, ItemContainerType.EQUIPPED,
					(byte) MBServerStatics.SLOT_HAIRSTYLE, new ArrayList<>(),"");

			Item hair;

			try {
				hair = DbManager.ItemQueries.ADD_ITEM(tempHair);
			} catch (Exception e) {
				hair = null;
			}

			if (hair == null) {
				playerCharacter.deactivateCharacter();
				Logger.info("GameObjectManager failed to create Hair:" + hairStyleID + " in Slot:"
						+ MBServerStatics.SLOT_HAIRSTYLE);
				return null;
			}
		}

		if (beardStyleID != 0) {
			// Create Beard
			Item tempBeard = new Item( ItemBase.getItemBase(beardStyleID), playerCharacter.getObjectUUID(), OwnerType.PlayerCharacter,
					(byte) 0, (byte) 0, (short) 1, (short) 1, false, false,ItemContainerType.EQUIPPED,
					(byte) MBServerStatics.SLOT_BEARDSTYLE, new ArrayList<>(),"");
			Item beard;
			try {
				beard = DbManager.ItemQueries.ADD_ITEM(tempBeard);
			} catch (Exception e) {
				beard = null;
			}

			if (beard == null) {
				playerCharacter.deactivateCharacter();
				Logger.info("GameObjectManager failed to create Beard:" + beardStyleID + " in Slot:"
						+ MBServerStatics.SLOT_BEARDSTYLE);
				return null;
			}
		}
		// Create items from Kit and equip on character.
		try {
			kit.equipPCwithKit(playerCharacter);
		} catch (Exception e) {
			Logger.info("Unable to find KIT ID for Race: " + raceID + "||" + "Class:" + baseClassID );
			playerCharacter.deactivateCharacter();
		return null;
		}

		// Get any new skills that belong to the player
		playerCharacter.calculateSkills();

		a.setLastCharacter(playerCharacter.getObjectUUID());
		playerCharacter.charItemManager.load();

		playerCharacter.activateCharacter();

		return playerCharacter;
	}

	public String getCombinedName() {
		return this.getName();
	}

	public long getLastGuildToInvite() {
		return this.lastGuildToInvite;
	}

	public void setLastGuildToInvite(int value) {
		this.lastGuildToInvite = value;
	}

	public boolean getFollow() {
		return this.follow;
	}

	public boolean toggleFollow() {
		this.follow = !this.follow;
		return this.follow;
	}

	public void setFollow(boolean value) {
		this.follow = value;
	}

	public int getLastGroupToInvite() {
		return this.lastGroupToInvite;
	}

	public void setLastGroupToInvite(int value) {
		this.lastGroupToInvite = value;
	}

	@Override
	public float getAltitude() {
		if (this.altitude < 0)
			this.altitude = 0;
		
		//player has reached desired altitude, return normal altitude.
		if (this.getTakeOffTime() == 0)
			return this.altitude;
		
		//sanity check  if desired altitude is the same as current altitude. return desired altitude.
		if (this.altitude == this.getDesiredAltitude()){
			return this.getDesiredAltitude();
		}
		
		//calculate how much the player has moved up
		float amountMoved = (System.currentTimeMillis() - this.getTakeOffTime()) * MBServerStatics.FLY_RATE; //FUCK DIVIDING
			
		//Player is moving up
		if (this.getDesiredAltitude() > this.altitude){
			
			//if amount moved passed desiredAltitude, return the desired altitude.
			if (this.altitude + amountMoved >= this.getDesiredAltitude())
				return this.getDesiredAltitude();
			
			return this.altitude + amountMoved;				
			//Player is moving down
		}else{
			//if amount moved passed desiredAltitude, return the desired altitude.
			if (this.altitude - amountMoved <= this.getDesiredAltitude())
				return this.getDesiredAltitude();
			return this.altitude - amountMoved;
		}
		
		
		
	}

	public void setAltitude(float value) {
			this.altitude = value;
	}

	public HashSet<AbstractWorldObject> getLoadedObjects() {
		return this.loadedObjects;
	}

	public HashSet<AbstractWorldObject> getLoadedStaticObjects() {
		return this.loadedStaticObjects;
	}

	public void setLoadedStaticObjects(HashSet<AbstractWorldObject> value) {
		this.loadedStaticObjects = value;
	}

	public void setTeleportMode(boolean teleportMode) {
		this.teleportMode = teleportMode;
	}

	public boolean isTeleportMode() {
		return teleportMode;
	}

	// public ConcurrentHashMap<Integer, FinishRecycleTimeJob>
	// getRecycleTimers() {
	// return this.recycleTimers;
	// }
	// public UsePowerJob getLastPower() {
	// return this.lastPower;
	// }
	// public void setLastPower(UsePowerJob value) {
	// this.lastPower = value;
	// }
	// public void clearLastPower() {
	// this.lastPower = null;
	// }
	public long chatFloodTime(int chatOpcode, long chatTimeMilli, int qtyToSave) {
		if (qtyToSave < 1)
			return 0L; // disabled
		LinkedList<Long> times = null;
		long oldestTime;
		synchronized (chatChanFloodList) {
			if (!chatChanFloodList.containsKey(chatOpcode)) {
				times = new LinkedList<>();
				for (int i = 0; i < qtyToSave; i++) {
					times.add(0L);
				}
				chatChanFloodList.put(chatOpcode, times);
			} else
				times = chatChanFloodList.get(chatOpcode);
			oldestTime = times.getLast();
			times.removeLast();
			times.addFirst(chatTimeMilli);
		}
		return oldestTime;
	}

	public void addIgnoredPlayer(Account ac, String name) {
		if (ac == null)
			return;
		int acID = ac.getObjectUUID();
		if (acID < 1)
			return;
		if (ignoredPlayerIDs == null)
			return;
		if (acID == getObjectUUID())
			return; // yourself

		ignoredPlayerIDs.put(acID, name);
	}

	public static boolean isIgnoreListFull() {
		return false; //Why were we setting a limit on ignores? -
		//return (ignoredPlayerIDs.size() >= MBServerStatics.IGNORE_LIST_MAX);
	}

	public void removeIgnoredPlayer(Account ac) {
		if (ac == null)
			return;
		int acID = ac.getObjectUUID();
		if (acID < 1)
			return;
		if (ignoredPlayerIDs == null)
			return;
		if (acID == getObjectUUID())
			return; // yourself

		ignoredPlayerIDs.remove(acID);
	}

	public boolean isIgnoringPlayer(PlayerCharacter pc) {

        if (pc == null)
            return false;

        if (pc.account == null)
            return false;

        return isIgnoringPlayer(pc.account);
    }

	public boolean isIgnoringPlayer(Account ac) {
		if (ac == null)
			return false;
		int acID = ac.getObjectUUID();
		if (acID < 1)
			return false;
		return ignoredPlayerIDs.containsKey(acID);
	}

	public static boolean isIgnorable() {
		return true;
		//		// if (account == null) return false;
		//		if (account.getAccessLevel() > 0) {
		//			return false;
		//		}
		//		return true;
	}

	public String[] getIgnoredPlayerNames() {
		int size = ignoredPlayerIDs.size();
		String[] ary = new String[size];
		for (int i = 0; i < size; i++) {
			//			ary[i] = PlayerCharacter.getFirstName(ignoredPlayerIDs.get(i));
			ary[i] = ignoredPlayerIDs.get(i);
		}
		return ary;
	}

	public int getStrMod() {
		return this.strMod.get();
	}

	public int getDexMod() {
		return this.dexMod.get();
	}

	public int getConMod() {
		return this.conMod.get();
	}

	public int getIntMod() {
		return this.intMod.get();
	}

	public int getSpiMod() {
		return this.spiMod.get();
	}

	public boolean isMale() {
		if (this.race == null)
			return true;
		return (this.race.getRaceType().getCharacterSex().equals(CharacterSex.MALE));
	}
	

	public boolean canSee(PlayerCharacter tar) {

		if (tar == null)
			return false;

		if (this.equals(tar))
			return true;

		return this.getSeeInvis() >= tar.hidden && !tar.safemodeInvis();
	}

	/**
	 * @ Initialize player upon creation
	 */
	public static void initializePlayer(PlayerCharacter player) {

		if (player.initialized)
			return;
		//	Logger.info("", " Initializing " + player.getCombinedName());
		player.skills = DbManager.CharacterSkillQueries.GET_SKILLS_FOR_CHARACTER(player);
		player.powers = player.initializePowers();
		
		
		if (ConfigManager.serverType.equals(ServerType.WORLDSERVER))
		player.setLoc(player.bindLoc);
		player.endLoc = Vector3fImmutable.ZERO;

		//get level based on experience
		player.level = (short) Experience.getLevel(player.exp);

		player.setHealth(999999f);
		player.mana.set(999999f);
		player.stamina.set(999999f);
		player.bonuses = new PlayerBonuses(player);
		PlayerBonuses.InitializeBonuses(player);
		player.resists = new Resists(player);
		player.charItemManager.load();

		if (ConfigManager.serverType.equals(ServerType.WORLDSERVER)) {

			//CharacterSkill.updateAllBaseAmounts(this);
			CharacterPower.grantTrains(player);

			// calculate skills. Make sure none are missing.
			AbstractCharacter.runBonusesOnLoad(player);
			
			PlayerCharacter.InitializeSkillsOnLoad(player);

			//apply all bonuses
			player.recalculatePlayerStats(true);
			player.trainsAvailable.set(CharacterSkill.getTrainsAvailable(player));

			if (player.trainsAvailable.get() < 0)
				player.recalculateTrains();

			//this.resists.calculateResists(this);
			player.newChar = true;

			//check current guild valid for player
			player.checkGuildStatus();

			player.setHealth(player.getHealthMax());
			player.mana.set(player.manaMax);
			player.stamina.set(player.staminaMax);
		} else
			player.setBindLoc(Vector3fImmutable.ZERO);

		player.initialized = true;

		String lastAscii = player.lastName.replaceAll("[^\\p{ASCII}]", "");
		player.asciiLastName = lastAscii.equals(player.lastName);
	}

	public void recalculatePlayerStats(boolean initialized) {

		//calculate base stats
		calculateBaseStats();

		//calculate base skills
		CharacterSkill.updateAllBaseAmounts(this);
		calculateModifiedStats();

		//calculate modified skills
		CharacterSkill.updateAllModifiedAmounts(this);
		this.updateScaleHeight();

		//calculate modified stats


		//calculate ATR, damage and defense
		calculateAtrDefenseDamage();

		//calculate movement bonus
		calculateSpeedMod();

		// recalculate Max Health/Mana/Stamina
		calculateMaxHealthManaStamina();

		// recalculate Resists
		Resists.calculateResists(this);

	}

	public static void recalculatePlayerStatsOnLoad(PlayerCharacter pc) {

		//calculate base stats
		pc.calculateBaseStats();

		//calculate base skills
		CharacterSkill.updateAllBaseAmounts(pc);
		pc.calculateModifiedStats();

		//calculate modified skills
		CharacterSkill.updateAllModifiedAmounts(pc);


		//calculate modified stats


		//calculate ATR, damage and defense
		pc.calculateAtrDefenseDamage();

		//calculate movement bonus
		pc.calculateSpeedMod();

		// recalculate Max Health/Mana/Stamina
		pc.calculateMaxHealthManaStamina();

		// recalculate Resists
		Resists.calculateResists(pc);

	}

	/**
	 * @ Recalculate player after promoting or gaining a level
	 */
	public void recalculate() {
		this.applyBonuses();
		this.trainsAvailable.set(CharacterSkill.getTrainsAvailable(this));
		if (this.trainsAvailable.get() < 0)
			recalculateTrains();
		//this.resists.calculateResists(this);

		// calculate skills and powers. Make sure none are missing.
		this.calculateSkills();

		// calculate powers again. See if any new powers unlocked
		this.calculateSkills();
	}

	//This is run to auto-fix any overage on skill training.
	private void recalculateTrains() {
		int trainsAvailable = CharacterSkill.getTrainsAvailable(this);
		if (trainsAvailable < 0) {

			//refine powers first, run twice to catch any prereqs
			ConcurrentHashMap<Integer, CharacterPower> powers = this.getPowers();
			for (int i = 0; i < 2; i++) {
				for (CharacterPower p : powers.values()) {
					if (trainsAvailable >= 0)
						return;
					while (p.getTrains() > 0 && p.refine(this)) {
						trainsAvailable++;
						if (trainsAvailable >= 0)
							return;
					}
				}
			}

			//refine skills
			ConcurrentHashMap<String, CharacterSkill> skills = this.getSkills();
			for (CharacterSkill s : skills.values()) {
				if (trainsAvailable >= 0)
					return;
				while (s.getNumTrains() > 0 && s.refine(this)) {
					if (CharacterSkill.getTrainsAvailable(this) >= 0)
						return;
				}
			}
		}
	}

	/**
	 * @ Calculates Base Stats Call this when modifying stats or adding/removing
	 * runes
	 */
	public void calculateBaseStats() {
		if (this.race == null || this.baseClass == null)
			// Logger.getInstance().log( LogEventType.ERROR,
			// "PlayerCharacter.updateBaseStats: Missing race or baseclass for Player "
			// + this.getUUID());
			return;

		// get base stats and total available
		int strMin = this.race.getStrStart() + this.baseClass.getStrMod() - 5;
		int dexMin = this.race.getDexStart() + this.baseClass.getDexMod() - 5;
		int conMin = this.race.getConStart() + this.baseClass.getConMod() - 5;
		int intMin = this.race.getIntStart() + this.baseClass.getIntMod() - 5;
		int spiMin = this.race.getSpiStart() + this.baseClass.getSpiMod() - 5;
		int str = this.race.getStrStart() + this.baseClass.getStrMod() + this.strMod.get();
		int dex = this.race.getDexStart() + this.baseClass.getDexMod() + this.dexMod.get();
		int con = this.race.getConStart() + this.baseClass.getConMod() + this.conMod.get();
		int intt = this.race.getIntStart() + this.baseClass.getIntMod() + this.intMod.get();
		int spi = this.race.getSpiStart() + this.baseClass.getSpiMod() + this.spiMod.get();
		int strMax = this.race.getStrMax();
		int dexMax = this.race.getDexMax();
		int conMax = this.race.getConMax();
		int intMax = this.race.getIntMax();
		int spiMax = this.race.getSpiMax();
		int available = this.race.getStartingPoints() - this.strMod.get() - this.dexMod.get() - this.conMod.get() - this.intMod.get() - this.spiMod.get();
		if (level < 20)
			available += (level - 1) * 5;
		else if (level < 30)
			available += 90 + (level - 19) * 4;
		else if (level < 40)
			available += 130 + (level - 29) * 3;
		else if (level < 50)
			available += 160 + (level - 39) * 2;
		else
			available += 180 + (level - 49);

		// modify for any runes applied.
		for (CharacterRune rune : this.runes) {
			if (rune.getRuneBase() == null)
				// Logger.getInstance().log( LogEventType.ERROR,
				// "PlayerCharacter.updateBaseStats: Missing runebase for rune "
				// + rune.getUUID());
				continue;
			ArrayList<RuneBaseAttribute> attrs = rune.getRuneBase().getAttrs();
			if (attrs == null)
				// Logger.getInstance().log( LogEventType.ERROR,
				// "PlayerCharacter.updateBaseStats: Missing attributes for runebase "
				// + rune.getRuneBase().getUUID());
				continue;
			for (RuneBaseAttribute abr : attrs) {
				int attrID = abr.getAttributeID();
				int value = abr.getModValue();
				switch (attrID) {
				case MBServerStatics.RUNE_COST_ATTRIBUTE_ID:
					available -= value;
					break;
				case MBServerStatics.RUNE_STR_ATTRIBUTE_ID:
					str += value;
					strMin += value;
					break;
				case MBServerStatics.RUNE_DEX_ATTRIBUTE_ID:
					dex += value;
					dexMin += value;
					break;
				case MBServerStatics.RUNE_CON_ATTRIBUTE_ID:
					con += value;
					conMin += value;
					break;
				case MBServerStatics.RUNE_INT_ATTRIBUTE_ID:
					intt += value;
					intMin += value;
					break;
				case MBServerStatics.RUNE_SPI_ATTRIBUTE_ID:
					spi += value;
					spiMin += value;
					break;
				case MBServerStatics.RUNE_STR_MAX_ATTRIBUTE_ID:
					strMax += value;
					break;
				case MBServerStatics.RUNE_DEX_MAX_ATTRIBUTE_ID:
					dexMax += value;
					break;
				case MBServerStatics.RUNE_CON_MAX_ATTRIBUTE_ID:
					conMax += value;
					break;
				case MBServerStatics.RUNE_INT_MAX_ATTRIBUTE_ID:
					intMax += value;
					break;
				case MBServerStatics.RUNE_SPI_MAX_ATTRIBUTE_ID:
					spiMax += value;
					break;
				default:
				}
			}

			//Set titles based on rune..
			switch (rune.getRuneBaseID()) {
			default:
				break;

			case 2901:	//CSR 1
				this.title = CharacterTitle.CSR_1;
				break;
			case 2902:	//CSR 1
				this.title = CharacterTitle.CSR_2;
				break;
			case 2903:	//CSR 1
				this.title = CharacterTitle.CSR_3;
				break;
			case 2904:	//CSR 1
				this.title = CharacterTitle.CSR_4;
				break;

			case 2910: //Wolfpack Developer
				this.title = CharacterTitle.DEVELOPER;
				break;
			case 2911: //QA Test Rune
				this.title = CharacterTitle.QA;
				break;
			}
		}

		//hack check. Make sure available does not go below 0.
		//subtract from each stat until available is 0 or greater.
		if (available < 0) {
			while (this.spiMod.get() > 0 && available < 0) {
				this.spiMod.decrementAndGet();
				spi--;
				available++;
			}
			while (this.conMod.get() > 0 && available < 0) {
				this.conMod.decrementAndGet();
				con--;
				available++;
			}
			while (this.strMod.get() > 0 && available < 0) {
				this.strMod.decrementAndGet();
				str--;
				available++;
			}
			while (this.dexMod.get() > 0 && available < 0) {
				this.dexMod.decrementAndGet();
				dex--;
				available++;
			}
			while (this.intMod.get() > 0 && available < 0) {
				this.intMod.decrementAndGet();
				intt--;
				available++;
			}

			//update database
			this.addDatabaseJob("Stats", MBServerStatics.THIRTY_SECONDS);
		}

		this.statStrBase = (short) str;
		this.statDexBase = (short) dex;
		this.statConBase = (short) con;
		this.statIntBase = (short) intt;
		this.statSpiBase = (short) spi;
		this.statStrMax = (short) (strMax);
		this.statDexMax = (short) (dexMax);
		this.statConMax = (short) (conMax);
		this.statIntMax = (short) (intMax);
		this.statSpiMax = (short) (spiMax);
		this.statStrMin = (short) strMin;
		this.statDexMin = (short) dexMin;
		this.statConMin = (short) conMin;
		this.statIntMin = (short) intMin;
		this.statSpiMin = (short) spiMin;
		this.unusedStatPoints = (short) available;
		this.trainedStatPoints = 0;

		// Testing, allow characters to have more stats then normal for formula checking
		if (this.statStrBase > this.statStrMax)
			this.statStrMax = this.statStrBase;
		if (this.statDexBase > this.statDexMax)
			this.statDexMax = this.statDexBase;
		if (this.statConBase > this.statConMax)
			this.statConMax = this.statConBase;
		if (this.statIntBase > this.statIntMax)
			this.statIntMax = this.statIntBase;
		if (this.statSpiBase > this.statSpiMax)
			this.statSpiMax = this.statSpiBase;

		// Modified stats must be recalculated when base stats are
		//calculateModifiedStats();
		//update hide and seeInvis levels
		if (this.bonuses != null) {
			this.hidden = (int)bonuses.getFloat(ModType.Invisible, SourceType.None);
			this.seeInvis = (int) bonuses.getFloat(ModType.SeeInvisible, SourceType.None);
		} else {
			this.hidden = (byte) 0;
			this.seeInvis = (byte) 0;
		}

		//check is player is a CSR
		this.isCSR = this.containsCSRRune();
	}

	private boolean containsCSRRune() {

		if (this.race != null && this.race.getRaceType().equals(RaceType.CSRMALE))
			return true;

		if (this.baseClass != null && this.baseClass.getObjectUUID() > 2900 && this.baseClass.getObjectUUID() < 2905)
			return true;

		if (this.promotionClass != null && this.promotionClass.getObjectUUID() > 2900 && this.promotionClass.getObjectUUID() < 2905)
			return true;

		if (this.runes == null)
			return false;

		for (CharacterRune rune : this.runes) {

			if (rune == null || rune.getRuneBase() == null)
				continue;

			RuneBase rb = rune.getRuneBase();

			if (rb.getObjectUUID() > 2900 && rb.getObjectUUID() < 2905)
				return true;
			if (rb.getObjectUUID() == 2910)
				return true;

		}
		return false;
	}

	public boolean isCSR() {
		return this.isCSR;
	}

	public void setAsciiLastName(boolean value) {
		this.asciiLastName = value;
	}

	public boolean _asciiLastName() {
		return this.asciiLastName;
	}

	public static boolean hideNonAscii() {

		return false;
	}

	/**
	 * @ Calculates Modified Stats Call this when changing equipment or
	 * add/removing effect. skips base stat modification.
	 */
	public void calculateModifiedStats() {
		float strVal = this.statStrBase;
		float dexVal = this.statDexBase;
		float conVal = this.statConBase;
		float intVal = this.statIntBase;
		float spiVal = this.statSpiBase;

		this.dexPenalty = getDexPenalty();

		// TODO modify for equipment
		if (this.bonuses != null) {
			// modify for effects
			strVal += Math.round(this.bonuses.getFloat(ModType.Attr, SourceType.Strength));
			dexVal += Math.round(this.bonuses.getFloat(ModType.Attr, SourceType.Dexterity));
			conVal += Math.round(this.bonuses.getFloat(ModType.Attr, SourceType.Constitution));
			intVal += Math.round(this.bonuses.getFloat(ModType.Attr, SourceType.Intelligence));
			spiVal += Math.round(this.bonuses.getFloat(ModType.Attr, SourceType.Spirit));
			

			// apply dex penalty for armor
			dexVal *= this.dexPenalty;

			// modify percent amounts. DO THIS LAST!
			strVal *= (1 +Math.round(this.bonuses.getFloatPercentAll(ModType.Attr, SourceType.Strength)));
			dexVal *= (1 +Math.round(this.bonuses.getFloatPercentAll(ModType.Attr, SourceType.Dexterity)));
			conVal *= (1 + Math.round(this.bonuses.getFloatPercentAll(ModType.Attr, SourceType.Constitution)));
			intVal *= (1+Math.round(this.bonuses.getFloatPercentAll(ModType.Attr, SourceType.Intelligence)));
			spiVal *= (1+Math.round(this.bonuses.getFloatPercentAll(ModType.Attr, SourceType.Spirit)));
		} else
			// apply dex penalty for armor
			dexVal *= this.dexPenalty;

		// Set current stats
		this.statStrCurrent = (strVal < 1) ? (short) 1 : (short) strVal;
		this.statDexCurrent = (dexVal < 1) ? (short) 1 : (short) dexVal;
		this.statConCurrent = (conVal < 1) ? (short) 1 : (short) conVal;
		this.statIntCurrent = (intVal < 1) ? (short) 1 : (short) intVal;
		this.statSpiCurrent = (spiVal < 1) ? (short) 1 : (short) spiVal;

		// recalculate skills
		//CharacterSkill.updateAllBaseAmounts(this);
		// recalculate Max Health/Mana/Stamina
		//calculateMaxHealthManaStamina();
		// recalculate Resists
		//this.resists.calculateResists(this);
	}

	public float getDexPenalty() {

		if (this.charItemManager == null || this.charItemManager.getEquipped() == null) {
			Logger.error( "Player " + this.getObjectUUID() + " missing equipment");
			return 1f;
		}

		ConcurrentHashMap<Integer, Item> equipped = this.charItemManager.getEquipped();
		float dexPenalty = 0f;
		dexPenalty += getDexPenalty(equipped.get(MBServerStatics.SLOT_HELMET));
		dexPenalty += getDexPenalty(equipped.get(MBServerStatics.SLOT_CHEST));
		dexPenalty += getDexPenalty(equipped.get(MBServerStatics.SLOT_ARMS));
		dexPenalty += getDexPenalty(equipped.get(MBServerStatics.SLOT_GLOVES));
		dexPenalty += getDexPenalty(equipped.get(MBServerStatics.SLOT_LEGGINGS));
		dexPenalty += getDexPenalty(equipped.get(MBServerStatics.SLOT_FEET));
		return (1 - (dexPenalty / 100));
	}

	public static float getDexPenalty(Item armor) {
		if (armor == null)
			return 0f;
		ItemBase ab = armor.getItemBase();
		if (ab == null)
			return 0f;
		return ab.getDexPenalty();
	}

	public int getStrForClient() {
		return this.statStrCurrent - this.race.getStrStart() - this.baseClass.getStrMod();
	}

	public int getDexForClient() {
		return this.statDexCurrent - this.race.getDexStart() - this.baseClass.getDexMod();
	}

	public int getConForClient() {
		return this.statConCurrent - this.race.getConStart() - this.baseClass.getConMod();
	}

	public int getIntForClient() {
		return this.statIntCurrent - this.race.getIntStart() - this.baseClass.getIntMod();
	}

	public int getSpiForClient() {
		return this.statSpiCurrent - this.race.getSpiStart() - this.baseClass.getSpiMod();
	}

	public int getTrainsAvailable() {
		return this.trainsAvailable.get();
	}

	public void modifyTrainsAvailable(int amount) {
		boolean worked = false;
		while (!worked) {
			int old = this.trainsAvailable.get();
			int newVal = old + amount;
			//			if (newVal < 0)
			//				newVal = 0;
			worked = this.trainsAvailable.compareAndSet(old, newVal);
		}
	}

	// Reset any data that should not persist from a previous session
	public void resetDataAtLogin() {
		loadedObjects.clear();
		loadedStaticObjects.clear();
		lastStaticLoc = Vector3fImmutable.ZERO;
		setLastTarget(GameObjectType.unknown, 0);
		this.follow = false;
	}

	/**
	 * @ Calculates Atr (both hands) Defense, and Damage for pc
	 */
	public void calculateAtrDefenseDamage() {
		if (this.charItemManager == null || this.charItemManager.getEquipped() == null || this.skills == null) {
			Logger.error("Player " + this.getObjectUUID() + " missing skills or equipment");
			defaultAtrAndDamage(true);
			defaultAtrAndDamage(false);
			this.defenseRating = 0;
			return;
		}
		ConcurrentHashMap<Integer, Item> equipped = this.charItemManager.getEquipped();

		//		// Reset passives
		//		if (this.bonuses != null) {
		//			this.bonuses.setBool("Block", false);
		//			this.bonuses.setBool("Parry", false);
		//			if (this.baseClass != null && this.baseClass.getUUID() == 2502)
		//				this.bonuses.setBool("Dodge", true);
		//			else
		//				this.bonuses.setBool("Dodge", false);
		//		}
		// calculate atr and damage for each hand
		calculateAtrDamageForWeapon(equipped.get(MBServerStatics.SLOT_MAINHAND), true, equipped.get(MBServerStatics.SLOT_OFFHAND));
		calculateAtrDamageForWeapon(equipped.get(MBServerStatics.SLOT_OFFHAND), false, equipped.get(MBServerStatics.SLOT_MAINHAND));

		// No Defense while in DeathShroud
		if (this.effects != null && this.effects.containsKey("DeathShroud"))
			this.defenseRating = (short) 0;
		else {
			// calculate defense for equipment
			float defense = this.statDexCurrent * 2;
			defense += getShieldDefense(equipped.get(MBServerStatics.SLOT_OFFHAND));
			defense += getArmorDefense(equipped.get(MBServerStatics.SLOT_HELMET));
			defense += getArmorDefense(equipped.get(MBServerStatics.SLOT_CHEST));
			defense += getArmorDefense(equipped.get(MBServerStatics.SLOT_ARMS));
			defense += getArmorDefense(equipped.get(MBServerStatics.SLOT_GLOVES));
			defense += getArmorDefense(equipped.get(MBServerStatics.SLOT_LEGGINGS));
			defense += getArmorDefense(equipped.get(MBServerStatics.SLOT_FEET));
			defense += getWeaponDefense(equipped);

			if (this.bonuses != null) {
				// add any bonuses
				defense += (short) this.bonuses.getFloat(ModType.DCV, SourceType.None);

				// Finally multiply any percent modifiers. DO THIS LAST!
				float pos_Bonus = this.bonuses.getFloatPercentPositive(ModType.DCV, SourceType.None);
				defense = (short) (defense * (1 + pos_Bonus));

				//Lucky rune applies next
				//applied runes will be calculated and added to the normal bonuses. no need for this garbage anymore
				//defense = (short) (defense * (1 + ((float) this.bonuses.getShort("rune.Defense") / 100)));

				//and negative percent modifiers
				//already done...
				float neg_Bonus = this.bonuses.getFloatPercentNegative(ModType.DCV, SourceType.None);
				defense = (short) (defense *(1 + neg_Bonus));

			} else
				// TODO add error log here
				Logger.error( "Error: missing bonuses");

			defense = (defense < 1) ? 1 : defense;
			this.defenseRating = (short) (defense + 0.5f);
		}
	}

	/**
	 * @ Calculates Atr, and Damage for each weapon
	 */
	private void calculateAtrDamageForWeapon(Item weapon, boolean mainHand, Item otherHand) {

		// make sure weapon exists
		boolean noWeapon = false;
		ItemBase wb = null;
		if (weapon == null)
			noWeapon = true;
		else {
			ItemBase ib = weapon.getItemBase();
			if (ib == null)
				noWeapon = true;
			else
				if (!ib.getType().equals(ItemType.WEAPON)) {
					defaultAtrAndDamage(mainHand);
					return;
				} else
					wb = ib;
		}
		float skillPercentage, masteryPercentage;
		float mastDam;
		float min, max;
		float speed = 20f;
		boolean strBased = false;

		ItemBase wbMain = (weapon != null) ? weapon.getItemBase() : null;
		ItemBase wbOff = (otherHand != null) ? otherHand.getItemBase() : null;

		// get skill percentages and min and max damage for weapons
		if (noWeapon) {
			if (mainHand) {
				Item off = this.charItemManager.getEquipped().get(MBServerStatics.SLOT_OFFHAND);
				if (off != null && off.getItemBase() != null && off.getItemBase().getType().equals(ItemType.WEAPON))
					this.rangeHandOne = 10 * (1 + (this.statStrBase / 600)); // Set
				// to
				// no
				// weapon
				// range
				else
					this.rangeHandOne = -1; // set to do not attack
			} else
				this.rangeHandTwo = -1; // set to do not attack

			skillPercentage = getModifiedAmount(this.skills.get("Unarmed Combat"));
			masteryPercentage = getModifiedAmount(this.skills.get("Unarmed Combat Mastery"));
			if (masteryPercentage == 0f)
				mastDam = CharacterSkill.getQuickMastery(this, "Unarmed Combat Mastery");
			else
				mastDam = masteryPercentage;
			// TODO Correct these
			min = 1;
			max = 3;
		} else {
			if (mainHand)
				this.rangeHandOne = weapon.getItemBase().getRange() * (1 + (this.statStrBase / 600));
			else
				this.rangeHandTwo = weapon.getItemBase().getRange() * (1 + (this.statStrBase / 600));

			if (this.bonuses != null){
				float range_bonus = 1 + this.bonuses.getFloatPercentAll(ModType.WeaponRange, SourceType.None);
	
				if (mainHand)
					this.rangeHandOne *= range_bonus;
				else
					this.rangeHandTwo *= range_bonus;

			}
			skillPercentage = getModifiedAmount(this.skills.get(wb.getSkillRequired()));
			masteryPercentage = getModifiedAmount(this.skills.get(wb.getMastery()));
			if (masteryPercentage == 0f)
				mastDam = 0f;
			//				mastDam = CharacterSkill.getQuickMastery(this, wb.getMastery());
			else
				mastDam = masteryPercentage;
			min = (float) wb.getMinDamage();
			max = (float) wb.getMaxDamage();
			strBased = wb.isStrBased();

			//
			// Add parry bonus for weapon and allow parry if needed
				
			//					// Only Fighters and Thieves can Parry
			//					if ((this.baseClass != null && this.baseClass.getUUID() == 2500)
			//							|| (this.promotionClass != null && this.promotionClass.getUUID() == 2520)) {
			//						if (wbMain == null || wbMain.getRange() < MBServerStatics.RANGED_WEAPON_RANGE)
			//							if (wbOff == null || wbOff.getRange() < MBServerStatics.RANGED_WEAPON_RANGE)
			//								this.bonuses.setBool("Parry", true);
			//					}
			//				}
		}

		if (this.effects != null && this.effects.containsKey("DeathShroud"))
			// No Atr in deathshroud.
			if (mainHand)
				this.atrHandOne = (short) 0;
			else
				this.atrHandTwo = (short) 0;
		else {
			// calculate atr
			float atr = 0;
			atr += (int) skillPercentage * 4f; //<-round down skill% -
			atr += (int) masteryPercentage * 3f;
			if (this.statStrCurrent > this.statDexCurrent)
				atr += statStrCurrent / 2;
			else
				atr += statDexCurrent / 2;

			// add in any bonuses to atr
			if (this.bonuses != null) {
				// Add any base bonuses
				atr += this.bonuses.getFloat(ModType.OCV, SourceType.None);

				// Finally use any multipliers. DO THIS LAST!
				float pos_Bonus =  (1 + this.bonuses.getFloatPercentPositive(ModType.OCV, SourceType.None));
				atr *= pos_Bonus; 

				// next precise
				//runes will have their own bonuses.
			//	atr *= (1 + ((float) this.bonuses.getShort("rune.Attack") / 100));

				//and negative percent modifiers
				float neg_Bonus = this.bonuses.getFloatPercentNegative(ModType.OCV, SourceType.None);
				
				atr *= (1 +  neg_Bonus);
			}

			atr = (atr < 1) ? 1 : atr;

			// set atr
			if (mainHand)
				this.atrHandOne = (short) (atr + 0.5f);
			else
				this.atrHandTwo = (short) (atr + 0.5f);
		}

		//calculate speed
		if (wb != null)
			speed = wb.getSpeed();
		else
			speed = 20f; //unarmed attack speed
			if (weapon != null)
				speed *= (1 + this.bonuses.getFloatPercentAll(ModType.WeaponSpeed, SourceType.None));
			speed *=  (1 + this.bonuses.getFloatPercentAll(ModType.AttackDelay, SourceType.None));
		if (speed < 10)
			speed = 10;

		//add min/max damage bonuses for weapon
		if (weapon != null) {
			// Add any base bonuses
			
			min +=  weapon.getBonus(ModType.MinDamage, SourceType.None);
			max +=  weapon.getBonus(ModType.MaxDamage, SourceType.None);
			
			min +=  weapon.getBonus(ModType.MeleeDamageModifier, SourceType.None);
			max +=  weapon.getBonus(ModType.MeleeDamageModifier, SourceType.None);
			// Finally use any multipliers. DO THIS LAST!
			
			float percentMinDamage = 1;
			float percentMaxDamage = 1;
			
			percentMinDamage +=  weapon.getBonusPercent(ModType.MinDamage, SourceType.None);
			percentMinDamage +=  weapon.getBonusPercent(ModType.MeleeDamageModifier, SourceType.None);
			
			percentMaxDamage += weapon.getBonusPercent(ModType.MaxDamage, SourceType.None);
			percentMaxDamage += weapon.getBonusPercent(ModType.MeleeDamageModifier, SourceType.None);
			
			
			
			min *= percentMinDamage;
			max *= percentMaxDamage;
		}

		//if duel wielding, cut damage by 30%
		if (otherHand != null) {
			ItemBase ibo = otherHand.getItemBase();
			if (ibo != null && ibo.getType().equals(ItemType.WEAPON)) {
				min *= 0.7f;
				max *= 0.7f;
			}
		}

		// calculate damage
		float minDamage;
		float maxDamage;
		float pri = (strBased) ? (float) this.statStrCurrent : (float) this.statDexCurrent;
		float sec = (strBased) ? (float) this.statDexCurrent : (float) this.statStrCurrent;
		minDamage = (float) (min * ((0.0315f * Math.pow(pri, 0.75f)) + (0.042f * Math.pow(sec, 0.75f)) + (0.01f * ((int) skillPercentage + (int) mastDam))));
		maxDamage = (float) (max * ((0.0785f * Math.pow(pri, 0.75f)) + (0.016f * Math.pow(sec, 0.75f)) + (0.0075f * ((int) skillPercentage + (int) mastDam))));
		minDamage = (float) ((int) (minDamage + 0.5f)); //round to nearest decimal
		maxDamage = (float) ((int) (maxDamage + 0.5f)); //round to nearest decimal

		// Half damage if in death shroud
		if (this.effects != null && this.effects.containsKey("DeathShroud")) {
			minDamage *= 0.5f;
			maxDamage *= 0.5f;
		}

		// add in any bonuses to damage
		if (this.bonuses != null) {
			// Add any base bonuses
			minDamage += this.bonuses.getFloat(ModType.MinDamage, SourceType.None);
			maxDamage += this.bonuses.getFloat(ModType.MaxDamage, SourceType.None);
			
			minDamage += this.bonuses.getFloat(ModType.MeleeDamageModifier, SourceType.None);
			maxDamage += this.bonuses.getFloat(ModType.MeleeDamageModifier, SourceType.None);
			// Finally use any multipliers. DO THIS LAST!
			
			float percentMinDamage = 1;
			float percentMaxDamage = 1;
			
			percentMinDamage +=  this.bonuses.getFloatPercentAll(ModType.MinDamage, SourceType.None);
			percentMinDamage +=  this.bonuses.getFloatPercentAll(ModType.MeleeDamageModifier, SourceType.None);
			
			percentMaxDamage += this.bonuses.getFloatPercentAll(ModType.MaxDamage, SourceType.None);
			percentMaxDamage += this.bonuses.getFloatPercentAll(ModType.MeleeDamageModifier, SourceType.None);
			
			minDamage *= percentMinDamage;
			maxDamage *= percentMaxDamage;
			
		}

		// set damages
		if (mainHand) {
			this.minDamageHandOne =  (int) minDamage;
			this.maxDamageHandOne =  (int) maxDamage;
			this.speedHandOne = speed;
		} else {
			this.minDamageHandTwo =  (int) minDamage;
			this.maxDamageHandTwo =  (int) maxDamage;
			this.speedHandTwo = speed;
		}
	}

	/**
	 * @ Calculates Defense for shield
	 */
	private float getShieldDefense(Item shield) {
		if (shield == null)
			return 0;
		ItemBase ab = shield.getItemBase();
		if (ab == null || !ab.isShield())
			return 0;
		CharacterSkill blockSkill = this.skills.get("Block");
		float skillMod;
		if (blockSkill == null) {
			skillMod = 0;
		} else
			skillMod = blockSkill.getModifiedAmount();

		float def = ab.getDefense();
		//apply item defense bonuses
		if (shield != null){
			def += shield.getBonus(ModType.DR, SourceType.None);
			def *= (1 + shield.getBonusPercent(ModType.DR, SourceType.None));

		}
		
		// float val = ((float)ab.getDefense()) * (1 + (skillMod / 100));
		return (def * (1 + ((int) skillMod / 100f)));
	}

	public void setPassives() {
		if (this.bonuses != null) {
			ConcurrentHashMap<Integer, Item> equipped = this.charItemManager.getEquipped();
			Item off = equipped.get(MBServerStatics.SLOT_OFFHAND);
			Item main = equipped.get(MBServerStatics.SLOT_MAINHAND);
			ItemBase wbMain = null;
			ItemBase wbOff = null;
			if (main != null)
				wbMain = main.getItemBase();
			if (off != null)
				wbOff = off.getItemBase();

			//set block if block found
			this.bonuses.setBool(ModType.Block,SourceType.None, false);
			if (this.baseClass != null && (this.baseClass.getObjectUUID() == 2500 || this.baseClass.getObjectUUID() == 2501))
				if (off != null && off.getItemBase() != null && off.getItemBase().isShield())
					this.bonuses.setBool(ModType.Block,SourceType.None, true);

			//set dodge if rogue
			if (this.baseClass != null && this.baseClass.getObjectUUID() == 2502)
				this.bonuses.setBool(ModType.Dodge,SourceType.None, true);
			else
				this.bonuses.setBool(ModType.Dodge,SourceType.None, false);

			//set parry if fighter or thief and no invalid weapon found
			this.bonuses.setBool(ModType.Parry,SourceType.None, false);
			if ((this.baseClass != null && this.baseClass.getObjectUUID() == 2500)
					|| (this.promotionClass != null && this.promotionClass.getObjectUUID() == 2520))
				if (wbMain == null || wbMain.getRange() < MBServerStatics.RANGED_WEAPON_RANGE)
					if (wbOff == null || wbOff.getRange() < MBServerStatics.RANGED_WEAPON_RANGE)
						this.bonuses.setBool(ModType.Parry,SourceType.None, true);

		}

	}

	/**
	 * @ Calculates Defense for armor
	 */
	private float getArmorDefense(Item armor) {

		if (armor == null)
			return 0;

		ItemBase ib = armor.getItemBase();

		if (ib == null)
			return 0;

		if (!ib.getType().equals(ItemType.ARMOR))
			return 0;
		if (ib.getSkillRequired().isEmpty())
			return ib.getDefense();
		CharacterSkill armorSkill = this.skills.get(ib.getSkillRequired());
		if (armorSkill == null) {
			Logger.error( "Player " + this.getObjectUUID()
			+ " has armor equipped without the nescessary skill to equip it");
			return ib.getDefense();
		}

		float def = ib.getDefense();
		//apply item defense bonuses
		if (armor != null){
			def += armor.getBonus(ModType.DR, SourceType.None);
			def *= (1 + armor.getBonusPercent(ModType.DR, SourceType.None));
		}
	

		return (def * (1 + ((int) armorSkill.getModifiedAmount() / 50f)));
	}

	/**
	 * @ Calculates Defense for weapon
	 */
	private float getWeaponDefense(ConcurrentHashMap<Integer, Item> equipped) {
		Item weapon = equipped.get(MBServerStatics.SLOT_MAINHAND);
		ItemBase wb = null;
		CharacterSkill skill, mastery;
		float val = 0;
		boolean unarmed = false;
		if (weapon == null) {
			weapon = equipped.get(MBServerStatics.SLOT_OFFHAND);
			if (weapon == null || weapon.getItemBase().isShield())
				unarmed = true;
			else
				wb = weapon.getItemBase();
		} else
			wb = weapon.getItemBase();
		if (wb == null)
			unarmed = true;
		if (unarmed) {
			skill = this.skills.get("Unarmed Combat");
			mastery = this.skills.get("Unarmed Combat Mastery");
		} else {
			skill = this.skills.get(wb.getSkillRequired());
			mastery = this.skills.get(wb.getMastery());
		}
		if (skill != null)
			val += (int) skill.getModifiedAmount() / 2f;
		if (mastery != null)
			val += (int) mastery.getModifiedAmount() / 2f;
		return val;
	}

	private static float getModifiedAmount(CharacterSkill skill) {
		if (skill == null)
			return 0f;
		return skill.getModifiedAmount();
	}

	//Call this function to recalculate granted skills and powers for player
	public synchronized void calculateSkills() {
		//tell the player to applyBonuses because something has changed

		runSkillCalc();

		//start running the skill/power calculations
	}

	//Don't call this function directly. linked from pc.calculateSkills()
	//through SkillCalcJob. Designed to only run from one worker thread
	public void runSkillCalc() {
		try {

			//see if any new skills or powers granted
			CharacterSkill.calculateSkills(this);
			// calculate granted Trains in powers.
			CharacterPower.grantTrains(this);
			//see if any new powers unlocked from previous check
			CharacterPower.calculatePowers(this);

		} catch (Exception e) {
		}

	}

	public static void InitializeSkillsOnLoad(PlayerCharacter pc) {
		try {
			{

				//see if any new skills or powers granted
				CharacterSkill.calculateSkills(pc);

				// calculate granted Trains in powers.
				CharacterPower.grantTrains(pc);

				//see if any new powers unlocked from previous check
				CharacterPower.calculatePowers(pc);
			}
		} catch (Exception e) {
			Logger.error( e.getMessage());
		}

	}

	//calculate item bonuses here
	public void calculateItemBonuses() {
		if (this.charItemManager == null || this.bonuses == null)
			return;
		ConcurrentHashMap<Integer, Item> equipped = this.charItemManager.getEquipped();
		for (Item item : equipped.values()) {
			ItemBase ib = item.getItemBase();
			if (ib == null)
				continue;
			//TODO add effect bonuses in here for equipped items
		}
	}

	/**
	 * @ Defaults ATR, Defense and Damage for player
	 */
	private void defaultAtrAndDamage(boolean mainHand) {
		if (mainHand) {
			this.atrHandOne = 0;
			this.minDamageHandOne = 0;
			this.maxDamageHandOne = 0;
			this.rangeHandOne = -1;
			this.speedHandOne = 20;
		} else {
			this.atrHandTwo = 0;
			this.minDamageHandTwo = 0;
			this.maxDamageHandTwo = 0;
			this.rangeHandTwo = -1;
			this.speedHandTwo = 20;
		}
	}

	public void calculateMaxHealthManaStamina() {
		float h = 1f;
		float m = 0f;
		float s = 0f;
		float baseHealth = 15f;
		float baseMana = 5f;
		float baseStamina = 1f;
		float promoHealth = 0f;
		float promoMana = 0f;
		float promoStamina = 0f;
		float raceHealth = 0f;
		float raceMana = 0f;
		float raceStamina = 0f;
		float toughness = 0f;
		float athletics = 0f;

		//get baseclass modifiers
		if (this.baseClass != null) {
			baseHealth = this.baseClass.getHealthMod();
			baseMana = this.baseClass.getManaMod();
			baseStamina = this.baseClass.getStaminaMod();
		} else {
			//TODO log error here
		}

		//get promotion modifiers
		if (this.promotionClass != null) {
			promoHealth = this.promotionClass.getHealthMod();
			promoMana = this.promotionClass.getManaMod();
			promoStamina = this.promotionClass.getStaminaMod();
		}

		// next get racial modifer
		if (this.race != null) {
			raceHealth += this.race.getHealthBonus();
			raceMana += this.race.getManaBonus();
			raceStamina += this.race.getStaminaBonus();
		} else {
			//TODO log error here
		}

		//Get level modifers
		float f = 0;
		float g = 0;
		if (this.level < 10 || this.promotionClass == null)
			f = this.level;
		else if (this.level < 20) {
			f = this.level;
			g = this.level - 9;
		} else if (level < 30) {
			f = (float) (19 + (this.level - 19) * 0.8);
			g = (float) (10 + (this.level - 19) * 0.8);
		} else if (level < 40) {
			f = (float) (27 + (this.level - 29) * 0.6);
			g = (float) (18 + (this.level - 29) * 0.6);
		} else if (level < 50) {
			f = (float) (33 + (this.level - 39) * 0.4);
			g = (float) (24 + (this.level - 39) * 0.4);
		} else if (level < 60) {
			f = (float) (37 + (this.level - 49) * 0.2);
			g = (float) (28 + (this.level - 49) * 0.2);
		} else {
			f = (float) (39 + (this.level - 59) * 0.1);
			g = (float) (30 + (this.level - 59) * 0.1);
		}

		//get toughness and athletics amount
		if (this.skills != null) {
			if (this.skills.containsKey("Toughness"))
				toughness = this.skills.get("Toughness").getModifiedAmount();
			if (this.skills.containsKey("Athletics"))
				athletics = this.skills.get("Athletics").getModifiedAmount();
		}

		h = (((f * baseHealth) + (g * promoHealth)) * (0.3f + (0.005f * this.statConCurrent)) + (this.statConCurrent + raceHealth)) * (1 + (int) toughness / 400f);
		m = ((f * baseMana) + (g * promoMana)) * (0.3f + (0.005f * this.statSpiCurrent)) + (this.statSpiCurrent + raceMana);
		s = (((f * baseStamina) + (g * promoStamina)) * (0.3f + (0.005f * this.statConCurrent)) + (this.statConCurrent + raceStamina)) * (1 + (int) athletics / 300f);

	//	s = f * (baseStamina + 1.75f) * .5f + this.statConCurrent + raceStamina;
		// Apply any bonuses from runes and effects
		if (this.bonuses != null) {
			

			//apply effects
			h += this.bonuses.getFloat(ModType.HealthFull, SourceType.None);
			m += this.bonuses.getFloat(ModType.ManaFull, SourceType.None);
			s += this.bonuses.getFloat(ModType.StaminaFull, SourceType.None);

			h *= (1 +this.bonuses.getFloatPercentAll(ModType.HealthFull, SourceType.None)) ;
			m *= (1+this.bonuses.getFloatPercentAll(ModType.ManaFull, SourceType.None));
			s *= (1+this.bonuses.getFloatPercentAll(ModType.StaminaFull, SourceType.None));
		
		}

		// Set max health, mana and stamina
		if (h > 0)
			this.healthMax = h;
		else
			this.healthMax = 1;
		if (m > -1)
			this.manaMax = m;
		else
			this.manaMax = 0;
		if (s > -1)
			this.staminaMax = s;
		else
			this.staminaMax = 0;

		// Update health, mana and stamina if needed
		if (this.getCurrentHitpoints() > this.healthMax)
			this.setHealth(this.healthMax);
		if (this.mana.get() > this.manaMax)
			this.mana.set(this.manaMax);
		if (this.stamina.get() > this.staminaMax)
			this.stamina.set(staminaMax);
	}

	@Override
	public float getPassiveChance(String type, int attackerLevel, boolean fromCombat) {
		if (this.skills == null || this.bonuses == null)
			return 0f;
		
		ModType modType = ModType.GetModType(type);

		// must be allowed to use this passive
		if (!this.bonuses.getBool(modType, SourceType.None))
			return 0f;

		// must not be stunned
		if (this.bonuses.getBool(ModType.Stunned, SourceType.None))
			return 0f;

		// Get base skill amount
		CharacterSkill sk = this.skills.get(type);
		float amount;
		if (sk == null)
			amount = CharacterSkill.getQuickMastery(this, type);
		else
			amount = sk.getModifiedAmount();

		// Add bonuses
		amount += this.bonuses.getFloat(modType, SourceType.None);

		// Add item bonuses and return
		if (type.equals(ModType.Dodge) && !fromCombat)
			return ((amount / 4) - attackerLevel + this.getLevel()) / 4;
		else
			return (amount - attackerLevel + this.getLevel()) / 4;
	}

	public float getPassiveChance1(ModType modType, SourceType sourceType, int attackerLevel, boolean fromCombat) {
		if (this.skills == null || this.bonuses == null)
			return 0f;

		// must be allowed to use this passive
		if (!this.bonuses.getBool(modType, sourceType))
			return 0f;

		// must not be stunned
		if (this.bonuses.getBool(ModType.Stunned, SourceType.None))
			return 0f;

		// Get base skill amount
		CharacterSkill sk = this.skills.get(sourceType.name());
		float amount;
		if (sk == null)
			amount = CharacterSkill.getQuickMastery(this, modType.name());
		else
			amount = sk.getModifiedAmount();

		// Add bonuses
		amount += this.bonuses.getFloat(modType, sourceType);

		// Add item bonuses and return
		if (sourceType.equals(SourceType.Dodge) && !fromCombat)
			return ((amount / 4) - attackerLevel + this.getLevel()) / 4;
		else
			return (amount - attackerLevel + this.getLevel()) / 4;
	}

	public float getRegenModifier(ModType type) {
		float regen = 1f;

		if (this.bonuses != null)
			// get regen bonus from effects
			regen = this.bonuses.getRegen(type);
		return regen;
	}

	@Override
	public boolean canBeLooted() {
		return !this.isAlive();
	}

	@Override
	public void setLevel(short targetLevel) {

		short tmpLevel;

		tmpLevel = targetLevel;

		tmpLevel = (short) Math.min(tmpLevel, 75);

		while (this.level < tmpLevel) {
			grantXP(Experience.getBaseExperience(tmpLevel) - this.exp);
		}

	}
	
	public void ResetLevel(short targetLevel) {
		
		if (targetLevel > 13){
			ChatManager.chatSystemError(this, "Please choose a level between 1 and 13.");
			return;
		}
		this.promotionClass = null;
		if (targetLevel > 10){
		this.level = 10;
		this.exp = Experience.getBaseExperience(11);
		int maxEXP = Experience.getBaseExperience(targetLevel); //target level exp;
		this.overFlowEXP = maxEXP - this.exp;
		}else{
			this.level = targetLevel;
			this.exp = Experience.getBaseExperience(level);
			this.overFlowEXP = 0;
		}
		
		
		for (CharacterSkill skill: this.getSkills().values()){
			skill.reset(this, true);
		}
		
		for (CharacterPower power : this.getPowers().values()){
			power.reset(this);
		}
		
		this.recalculatePlayerStats(initialized);
		this.recalculate();
		
		ChatManager.chatSystemInfo(this, "Character reset to " + targetLevel+ ". All training points have been refunded. Relog to update changes on client.");

	}

	@Override
	public void removeFromCache() {
		Logger.info("Removing " + this.getName() + " from Object Cache.");

		for (Item e : this.charItemManager.getEquipped().values()) {
			e.removeFromCache();
		}

		for (Item i : this.charItemManager.getInventory(true)) {
			i.removeFromCache();
		}

		for (Item b : this.charItemManager.getBank()) {
			b.removeFromCache();
		}

		if (this.account.getLastCharIDUsed() == this.getObjectUUID())
			for (Item v : this.charItemManager.getVault()) {
				v.removeFromCache();
			}

		for (CharacterSkill cs : this.getSkills().values()) {
			cs.removeFromCache();
		}

		for (CharacterPower ps : this.getPowers().values()) {
			ps.removeFromCache();
		}

		for (CharacterRune cr : this.runes) {
			cr.removeFromCache();
		}

		super.removeFromCache();
	}

	public static String getFirstName(int tableId) {

		PlayerCharacter player;

		if (tableId == 0)
			return "";

		player = (PlayerCharacter) DbManager.getObject(GameObjectType.PlayerCharacter, tableId);

		return player.getFirstName();
	}

	public static PlayerCharacter getFromCache(int id) {
		return (PlayerCharacter) DbManager.getFromCache(GameObjectType.PlayerCharacter, id);
	}
	
	public static PlayerCharacter getByFirstName(String name) {
		
		PlayerCharacter returnPlayer = null;
		for (AbstractGameObject ago : DbManager.getList(GameObjectType.PlayerCharacter)){
			PlayerCharacter cachePlayer = (PlayerCharacter)ago;
			if (!name.equalsIgnoreCase(cachePlayer.getFirstName()))
				continue;
			if (cachePlayer.isDeleted())
				continue;
			returnPlayer = cachePlayer;
			break;
		}
		
		return returnPlayer;
	}

	public static PlayerCharacter getPlayerCharacter(int uuid) {

		PlayerCharacter outPlayer;

		outPlayer = DbManager.PlayerCharacterQueries.GET_PLAYER_CHARACTER(uuid);

		if (outPlayer != null)
			return outPlayer;

		return (PlayerCharacter) DbManager.getFromCache(GameObjectType.PlayerCharacter, uuid);
	}

	public void storeIgnoreListDB() {

	}

	public void updateSkillsAndPowersToDatabase() {
		if (this.skills != null)
			for (CharacterSkill skill : this.skills.values()) {
				DbManager.CharacterSkillQueries.UPDATE_TRAINS(skill);
				if (this.powers != null)
					for (CharacterPower power : this.powers.values()) {
						DbManager.CharacterPowerQueries.UPDATE_TRAINS(power);
					}
			}
	}

	@Override
	public void updateDatabase() {
	}

	@Override
	public void runAfterLoad() {

		// Create player bounds object

		//		if ((MBServer.getApp() instanceof engine.server.world.WorldServer))
		//			DbManager.GuildQueries.LOAD_GUILD_HISTORY_FOR_PLAYER(this);

		Bounds playerBounds = Bounds.borrow();
		playerBounds.setBounds(this.getLoc());
		this.setBounds(playerBounds);
	}

	@Override
	protected ConcurrentHashMap<Integer, CharacterPower> initializePowers() {
		return DbManager.CharacterPowerQueries.GET_POWERS_FOR_CHARACTER(this);
	}

	@Override
	public final void setFirstName(final String name) {
		super.setFirstName(name);
	}

	@Override
	public void setLastName(final String name) {
		super.setLastName(name);
	}

	@Override
	public short getLevel() {
		return this.getPCLevel();
	}

	@Override
	public boolean asciiLastName() {
		return this._asciiLastName();
	}

	@Override
	public void setGuild(Guild value) {
		
		if (value == null)
			value = Guild.getErrantGuild();
		
		int guildID = 0;
		
		if (!value.isErrant())
			guildID = value.getObjectUUID();
		DbManager.PlayerCharacterQueries.UPDATE_GUILD(this, guildID);
		super.setGuild(value);

		// Player changed guild so let's invalidate the login server
		// cache to reflect this event.

		//Update player bind location;
		
		Building cityTol = null;
		
		if (value.getOwnedCity() != null)
		 cityTol = value.getOwnedCity().getTOL();
		
		this.setBindBuildingID(cityTol != null ? cityTol.getObjectUUID() : 0);
			//update binds, checks for nation tol if guild tol == null;
		 PlayerCharacter.getUpdatedBindBuilding(this);
		
		
		DbManager.AccountQueries.INVALIDATE_LOGIN_CACHE(this.getObjectUUID(), "character");
	}

	public long getSummoner(int summoner) {
		synchronized (this.summoners) {
			if (!this.summoners.containsKey(summoner))
				return 0;
			return this.summoners.get(summoner);
		}
	}

	public void addSummoner(int summoner, long time) {
		synchronized (this.summoners) {
			this.summoners.put(summoner, time);
		}
	}

	public void removeSummoner(int summoner) {
		synchronized (this.summoners) {
			if (this.summoners.containsKey(summoner))
				this.summoners.remove(summoner);
		}
	}

	public boolean commandSiegeMinion(Mob toCommand) {
		if (!toCommand.isSiege())
			return false;
		if (toCommand.isPet() || !toCommand.isAlive())
			return false;
		
		if (toCommand.getGuild().getNation() != this.getGuild().getNation())
			return false;

		if (this.pet != null) {
			Mob currentPet = this.pet;
			if (!currentPet.isSiege()) {

				currentPet.setCombatTarget(null);
				currentPet.setState(STATE.Disabled);

				if (currentPet.getParentZone() != null)

					currentPet.getParentZone().zoneMobSet.remove(currentPet);

				try {
					currentPet.clearEffects();
				}catch(Exception e){
					Logger.error( e.getMessage());
				}
				currentPet.getPlayerAgroMap().clear();
				WorldGrid.RemoveWorldObject(currentPet);
				DbManager.removeFromCache(currentPet);

			} else
				if (currentPet.isSiege()) {
					currentPet.setMob();
					currentPet.setOwner(null);
					currentPet.setCombatTarget(null);
					if (currentPet.isAlive())
						WorldGrid.updateObject(currentPet);
				}
		}

		toCommand.setPet(this, false);
		this.setPet(toCommand);
		toCommand.setCombatTarget(null);
		PetMsg petMsg = new PetMsg(6, toCommand);
		Dispatch dispatch = Dispatch.borrow(this, petMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

		if (toCommand.isAlive())
			WorldGrid.updateObject(toCommand);
		return true;
	}

	public boolean isNoTeleScreen() {
		return noTeleScreen;
	}

	public void setNoTeleScreen(boolean noTeleScreen) {
		this.noTeleScreen = noTeleScreen;
	}

	private double getDeltaTime() {

		return (System.currentTimeMillis() - lastUpdateTime) * .001f;
	}
	
	private double getStamDeltaTime() {

		return (System.currentTimeMillis() - lastStamUpdateTime) * .001f;
	}

	public boolean isFlying() {

		return this.getAltitude() > 0;

	}

	public boolean isSwimming() {

		// If char is flying they aren't quite swimming
		try {
			if (this.isFlying())
				return false;

			Zone zone = ZoneManager.findSmallestZone(this.getLoc());

			if (zone.getSeaLevel() != 0) {

				float localAltitude = this.getLoc().y + this.centerHeight;
				if (localAltitude < zone.getSeaLevel())
					return true;
			} else {
				if (this.getLoc().y + this.centerHeight < 0)
					return true;
			}
		} catch (Exception e){
			Logger.info(this.getName() + e);
		}

		return false;
	}
	
	public boolean isSwimming(Vector3fImmutable currentLoc) {

		// If char is flying they aren't quite swimming
		try{

			float localAltitude = HeightMap.getWorldHeight(currentLoc);

			Zone zone = ZoneManager.findSmallestZone(currentLoc);

			if (zone.getSeaLevel() != 0){

				if (localAltitude < zone.getSeaLevel())
					return true;
			}else{
				if (localAltitude < 0)
					return true;
			}
		}catch(Exception e){
			Logger.info(this.getName() + e);
		}

		return false;
	}

	// Method is called by Server Heartbeat simulation tick.
	// Stat regen and transform updates should go in here.

	@Override
	public void update() {

		if (this.updateLock.writeLock().tryLock()){
			try{
				
				if (!this.isAlive())
					return;
				
					updateLocation();
					updateMovementState();
					updateRegen();

				if (this.getStamina() < 10){
					if (this.getAltitude() > 0 || this.getDesiredAltitude() > 0){
						PlayerCharacter.GroundPlayer(this);
						updateRegen();
					}
				}

					RealmMap.updateRealm(this);
					updateBlessingMessage();
			
				this.safeZone = this.isInSafeZone();

			}catch(Exception e){
				Logger.error(e);
			}finally{
				this.updateLock.writeLock().unlock();
			}
		}
	}
	@Override
	public void updateFlight() {
		
		if (this.getAltitude() == 0 && this.getTakeOffTime() == 0)
			return;
		
		if (this.getTakeOffTime() == 0)
			return;
		
		if (this.getAltitude() == this.getDesiredAltitude()){
			if (this.getDesiredAltitude() == 0)
				this.syncClient();
			//landing in a building, mark altitude to 0 as player is no longer flying.
			if (this.landingRegion != null){
				this.altitude = 0;
				this.region = this.landingRegion;
				this.loc = this.loc.setY(this.landingRegion.lerpY(this));
			}
			else
			this.altitude = this.getDesiredAltitude();
			
			this.loc = this.loc.setY(HeightMap.getWorldHeight(this) + this.getAltitude());

			this.setTakeOffTime(0);
			MovementManager.finishChangeAltitude(this, this.getDesiredAltitude());
			
			return;
		}
		
		this.loc = this.loc.setY(HeightMap.getWorldHeight(this) + this.getAltitude());
	}

	public boolean hasBoon(){
		for (Effect eff : this.getEffects().values()){
			if (eff.getPowerToken() == -587743986 || eff.getPowerToken() == -1660519801 || eff.getPowerToken() == -1854683250)
				return true;
		}
		return false;
	}

	public void updateBlessingMessage(){

		if (this.getTimeStamp("RealmClaim") > System.currentTimeMillis())
			return;

		int count = 0;

		for (Effect eff : this.getEffects().values()){
			if (eff.getPowerToken() == -587743986 || eff.getPowerToken() == -1660519801 || eff.getPowerToken() == -1854683250)
				count++;
		}

		if (count > 0){
			this.timestamps.put("RealmClaim", DateTime.now().plusMinutes(3).getMillis());
			for (PlayerCharacter toSend : SessionManager.getAllActivePlayerCharacters()){
				ChatManager.chatSystemInfo(toSend, this.getCombinedName() + " is seeking to claim a realm and already has " + count + " blessngs!");
			}
		}
	}
	@Override
	public void updateLocation(){

		
		if (!this.isMoving())
			return;

		if (!this.isActive)
			return;
		
		Vector3fImmutable newLoc = this.getMovementLoc();
		
		if (this.isAlive() == false || this.getBonuses().getBool(ModType.Stunned, SourceType.None) || this.getBonuses().getBool(ModType.CannotMove, SourceType.None)) {
			//Target is stunned or rooted. Don't move
			this.stopMovement(newLoc);
			this.region = AbstractWorldObject.GetRegionByWorldObject(this);
			return;
		}
		if (newLoc.equals(this.getEndLoc())){
			this.stopMovement(newLoc);
			this.region = AbstractWorldObject.GetRegionByWorldObject(this);
			if (this.getDebug(1))
				ChatManager.chatSystemInfo( this,
						"Arrived at End location. " + this.getEndLoc());
			return;
			//Next upda
		}
		
		setLoc(newLoc);
		this.region = AbstractWorldObject.GetRegionByWorldObject(this);

		if (this.getDebug(1))
			ChatManager.chatSystemInfo(this,
					"Distance to target " + this.getEndLoc().distance2D(this.getLoc()) + " speed " + this.getSpeed());

		if (this.getStamina() < 10)
			MovementManager.sendOOS(this);

		//	if (MBServerStatics.MOVEMENT_SYNC_DEBUG || this.getDebug(1))
		//                Logger.info("MovementManager", "Updating movement current loc:" + this.getLoc().getX() + " " + this.getLoc().getZ()
		//                        + " end loc: " + this.getEndLoc().getX() + " " + this.getEndLoc().getZ() + " distance " + this.getEndLoc().distance2D(this.getLoc()));

	}
	@Override
	public void updateMovementState() {
		
		
		if (this.enteredWorld) {
			if (!this.lastSwimming) {
				boolean enterWater = PlayerCharacter.enterWater(this);
				
				if (enterWater){
					this.lastSwimming = enterWater;
					MovementManager.sendRWSSMsg(this);
					
				}
			} else {
				if (PlayerCharacter.LeaveWater(this)){
					this.lastSwimming = false;
					if (!this.isMoving())
					MovementManager.sendRWSSMsg(this);
				}
					
			}
			
			boolean breathe = PlayerCharacter.CanBreathe(this);
			
			if (breathe != this.canBreathe){
				this.canBreathe = breathe;
			//	ChatManager.chatSystemInfo(this, "Breathe : " + this.canBreathe);
				this.syncClient();
			}
		}

		//char is flying
		if (this.isFlying() == true) {
			this.movementState = MovementState.FLYING;
			return;
		}
		// Char is not moving.  Set sitting or idle
		if (!this.isMoving()) {

			if (this.sit == true)
				this.movementState = MovementState.SITTING;
			else
				this.movementState = MovementState.IDLE;

			return;
		} else {
			this.movementState = MovementState.RUNNING;
		}

		// Char is swimming // we now are saving lastSwimstate boolean, use this instead of calling getSwimming again.
		if (this.lastSwimming == true) {
			this.movementState = MovementState.SWIMMING;
			return;
		}

		// Char is moving, yet not swimming or flying he must be running
		this.movementState = MovementState.RUNNING;

	}
	@Override
	public void updateRegen() {
		
		float healthRegen = 0f;
		float manaRegen = 0f;
		float stamRegen = 0f;
		
		boolean updateClient = false;

		// Early exit if char is dead or disconnected
		if ((this.isAlive() == false)
				|| (this.isActive() == false) || this.getLoc().x == 0 && this.getLoc().z == 0)
			return;

		// Calculate Regen amount from last simulation tick
		switch (this.movementState) {

		case IDLE:

			healthRegen = ((this.healthMax * MBServerStatics.HEALTH_REGEN_IDLE) + MBServerStatics.HEALTH_REGEN_IDLE_STATIC) * (getRegenModifier(ModType.HealthRecoverRate));
			
			if (this.isCasting() || this.isItemCasting())
				healthRegen *= .75f;
			// Characters regen mana when in only walk mode and idle
			if (this.walkMode)
				manaRegen = ((this.manaMax  * MBServerStatics.MANA_REGEN_IDLE)  *  getRegenModifier(ModType.ManaRecoverRate));
			else if (!this.isCasting() && !this.isItemCasting())
				manaRegen = ((this.manaMax  * MBServerStatics.MANA_REGEN_IDLE)  *  getRegenModifier(ModType.ManaRecoverRate));
			else
				manaRegen = 0;

			 if (!PlayerCharacter.CanBreathe(this))
				stamRegen = MBServerStatics.STAMINA_REGEN_SWIM;
			else if ((!this.isCasting() && !this.isItemCasting()) || this.lastMovementState.equals(MovementState.FLYING))
				stamRegen = MBServerStatics.STAMINA_REGEN_IDLE * getRegenModifier(ModType.StaminaRecoverRate);
			else
				stamRegen =0 ;
			break;
		case SITTING:
			healthRegen = ((this.healthMax * MBServerStatics.HEALTH_REGEN_SIT) + MBServerStatics.HEALTH_REGEN_SIT_STATIC) * getRegenModifier(ModType.HealthRecoverRate);
			manaRegen = (this.manaMax * MBServerStatics.MANA_REGEN_SIT)  *  ( getRegenModifier(ModType.ManaRecoverRate));
			stamRegen = MBServerStatics.STAMINA_REGEN_SIT * getRegenModifier(ModType.StaminaRecoverRate);
			break;
		case RUNNING:
			if (this.walkMode == true) {
				healthRegen = ((this.healthMax * MBServerStatics.HEALTH_REGEN_WALK) + MBServerStatics.HEALTH_REGEN_IDLE_STATIC) * getRegenModifier(ModType.HealthRecoverRate);
				manaRegen = this.manaMax * MBServerStatics.MANA_REGEN_WALK *  getRegenModifier(ModType.ManaRecoverRate);
				stamRegen = MBServerStatics.STAMINA_REGEN_WALK;
			} else {
				healthRegen =0;
				manaRegen = 0;
				
				if (this.combat == true)
					stamRegen = MBServerStatics.STAMINA_REGEN_RUN_COMBAT;
				else
					stamRegen = MBServerStatics.STAMINA_REGEN_RUN_NONCOMBAT;
			}
			break;
		case FLYING:
			
			float seventyFive = this.staminaMax * .75f;
			float fifty = this.staminaMax *.5f;
			float twentyFive = this.staminaMax *.25f;

			if (this.getDesiredAltitude() == 0 && this.getAltitude() <= 10){
				if (this.isCombat())
					stamRegen = 0;
				else
					stamRegen = MBServerStatics.STAMINA_REGEN_IDLE * getRegenModifier(ModType.StaminaRecoverRate);
			}
				else if (!this.useFlyMoveRegen()){
					
					healthRegen = ((this.healthMax * MBServerStatics.HEALTH_REGEN_IDLE) + MBServerStatics.HEALTH_REGEN_IDLE_STATIC) * ( getRegenModifier(ModType.HealthRecoverRate));
					
					if (this.isCasting() || this.isItemCasting())
						healthRegen *= .75f;
					// Characters regen mana when in only walk mode and idle
					if (this.walkMode)
						manaRegen = (this.manaMax * MBServerStatics.MANA_REGEN_IDLE + (this.getSpiMod() * .015f))* ( getRegenModifier(ModType.ManaRecoverRate));
					else if (!this.isCasting() && !this.isItemCasting())
						manaRegen = (this.manaMax * MBServerStatics.MANA_REGEN_IDLE + (this.getSpiMod() * .015f)) * (  getRegenModifier(ModType.ManaRecoverRate));
					else
						manaRegen = 0;
					
					if (!this.isItemCasting() && !this.isCasting() || this.getTakeOffTime() != 0)
						stamRegen = MBServerStatics.STAMINA_REGEN_FLY_IDLE;		
					else
						stamRegen = -1f;
				}
				else
			if (this.walkMode == true) {
				healthRegen = ((this.healthMax * MBServerStatics.HEALTH_REGEN_WALK) + MBServerStatics.HEALTH_REGEN_IDLE_STATIC) * getRegenModifier(ModType.HealthRecoverRate);
				manaRegen = ((this.manaMax * MBServerStatics.MANA_REGEN_WALK)+ (this.getSpiMod() * .015f))  * (getRegenModifier(ModType.ManaRecoverRate));
				stamRegen = MBServerStatics.STAMINA_REGEN_FLY_WALK;
			} else {
				healthRegen = 0;
				manaRegen = 0;
				if (this.isCombat())
					stamRegen = MBServerStatics.STAMINA_REGEN_FLY_RUN_COMBAT;
				else
				stamRegen = MBServerStatics.STAMINA_REGEN_FLY_RUN;
			}

			float oldStamina = this.stamina.get();
			
			if (FastMath.between(oldStamina, 0, twentyFive) && !this.wasTripped25){
				updateClient = true;
				this.wasTripped25 = true;
				this.wasTripped50 = false;
				this.wasTripped75 = false;
			}else if (FastMath.between(oldStamina, twentyFive, fifty) && !this.wasTripped50){
				updateClient = true;
				this.wasTripped25 = false;
				this.wasTripped50 = true;
				this.wasTripped75 = false;
			}else if (FastMath.between(oldStamina, fifty, seventyFive) && !this.wasTripped75){
				updateClient = true;
				this.wasTripped25 = false;
				this.wasTripped50 = false;
				this.wasTripped75 = true;
			}
			break;
		case SWIMMING:
			if (this.walkMode == true) {
				healthRegen = ((this.healthMax * MBServerStatics.HEALTH_REGEN_WALK) + MBServerStatics.HEALTH_REGEN_IDLE_STATIC) *  getRegenModifier(ModType.HealthRecoverRate);
				manaRegen = ((this.manaMax * MBServerStatics.MANA_REGEN_WALK)+ (this.getSpiMod() * .015f)) * ( getRegenModifier(ModType.ManaRecoverRate));
				stamRegen = MBServerStatics.STAMINA_REGEN_SWIM;
			} else {
				healthRegen = 0;
				manaRegen = 0;
				stamRegen = MBServerStatics.STAMINA_REGEN_SWIM;
				
				if (this.combat == true)
					stamRegen += MBServerStatics.STAMINA_REGEN_RUN_COMBAT;
				else
					stamRegen += MBServerStatics.STAMINA_REGEN_RUN_NONCOMBAT;
			}
			break;
		}

		// Are we drowning?
		if ((this.getStamina() <= 0)
				&& (PlayerCharacter.CanBreathe(this) == false))
			healthRegen = (this.healthMax * -.03f);

		// Multiple regen values by current deltaTime
		//     Logger.info("", healthRegen + "");
		healthRegen *= getDeltaTime();
		manaRegen *= getDeltaTime();
		stamRegen *= getStamDeltaTime();

		boolean workedHealth = false;
		boolean workedMana = false;
		boolean workedStamina = false;

		float old, mod;
		while(!workedHealth || !workedMana || !workedStamina) {
			if (!this.isAlive() || !this.isActive())
				return;
			if (!workedHealth) {
				old = this.health.get();
				mod = old + healthRegen;
				if (mod > this.healthMax)
					mod = healthMax;
				else if (mod <= 0) {
					if (this.isAlive.compareAndSet(true, false))
						killCharacter("Water");
					return;
				}
				workedHealth = this.health.compareAndSet(old, mod);
			}
			if (!workedStamina) {
				old = this.stamina.get();
				mod = old + stamRegen;
				if (mod > this.staminaMax)
					mod = staminaMax;
				else if (mod < 0)
					mod = 0;
				workedStamina = this.stamina.compareAndSet(old, mod);
			}
			if (!workedMana) {
				old = this.mana.get();
				mod = old + manaRegen;
				if (mod > this.manaMax)
					mod = manaMax;
				else if (mod < 0)
					mod = 0;
				workedMana = this.mana.compareAndSet(old, mod);
			}
		}

		if (updateClient)
			this.syncClient();

		// Reset this char's frame time.
		this.lastUpdateTime = System.currentTimeMillis();
		this.lastStamUpdateTime = System.currentTimeMillis();

	}
	
	public synchronized void updateStamRegen(long time) {

		boolean disable = true;
		
		if (disable)
			return;
	
		float stamRegen = 0f;

		// Early exit if char is dead or disconnected
		if ((this.isAlive() == false)
				|| (this.isActive() == false) || this.getLoc().x == 0 && this.getLoc().z == 0)
			return;

		// Calculate Regen amount from last simulation tick
		switch (this.movementState) {

		case IDLE:
			 if (!PlayerCharacter.CanBreathe(this))
				stamRegen = MBServerStatics.STAMINA_REGEN_SWIM;
			else if ((!this.isCasting() && !this.isItemCasting()) || this.lastMovementState.equals(MovementState.FLYING))
				stamRegen = MBServerStatics.STAMINA_REGEN_IDLE * getRegenModifier(ModType.StaminaRecoverRate);
			else
				stamRegen =0 ;
			break;
		case SITTING:
			stamRegen = MBServerStatics.STAMINA_REGEN_SIT * getRegenModifier(ModType.StaminaRecoverRate);
			break;
		case RUNNING:
			if (this.walkMode == true) {
							stamRegen = MBServerStatics.STAMINA_REGEN_WALK;
			} else {
				if (this.combat == true)
					stamRegen = MBServerStatics.STAMINA_REGEN_RUN_COMBAT;
				else
					stamRegen = MBServerStatics.STAMINA_REGEN_RUN_NONCOMBAT;
			}
			break;
		case FLYING:
			
			if (this.getDesiredAltitude() == 0 && this.getAltitude() <= 10){
				if (this.isCombat())
					stamRegen = 0;
				else
					stamRegen = MBServerStatics.STAMINA_REGEN_IDLE * getRegenModifier(ModType.StaminaRecoverRate);
			}
				else 	if (!this.isMoving()){
					
					
					if (!this.isItemCasting() && !this.isCasting() || this.getTakeOffTime() != 0)
						stamRegen = MBServerStatics.STAMINA_REGEN_FLY_IDLE;		
					else
						stamRegen = -1f;
					
				}
				else
				
			if (this.walkMode == true) {
				
				stamRegen = MBServerStatics.STAMINA_REGEN_FLY_WALK;
			} else {
				if (this.isCombat())
					stamRegen = MBServerStatics.STAMINA_REGEN_FLY_RUN_COMBAT;
				else
				stamRegen = MBServerStatics.STAMINA_REGEN_FLY_RUN;
			}
			break;
		case SWIMMING:
			if (this.walkMode == true) {
				stamRegen = MBServerStatics.STAMINA_REGEN_SWIM;
			} else {
				stamRegen = MBServerStatics.STAMINA_REGEN_SWIM;
			}
			break;
		}

	
	

		// Multiple regen values by current deltaTime
		//     Logger.info("", healthRegen + "");
		
		stamRegen *= (time * .001f);


		
		boolean workedStamina = false;


		float old, mod;
		while( !workedStamina) {
			if (!this.isAlive() || !this.isActive())
				return;
			
			if (!workedStamina) {
				old = this.stamina.get();
				mod = old + stamRegen;
				if (mod > this.staminaMax)
					mod = staminaMax;
				else if (mod < 0)
					mod = 0;
				workedStamina = this.stamina.compareAndSet(old, mod);
			}
		
		}
		
	}

	public void syncClient() {

		ModifyHealthMsg modifyHealthMsg = new ModifyHealthMsg(null, this, 0, 1, 1, -1984683793, "", 0, 652920987);
		//mhm.setOmitFromChat(0);
		Dispatch dispatch = Dispatch.borrow(this, modifyHealthMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

	}

	public MovementState getMovementState() {
		return movementState;
	}

	public boolean isHasAnniversery() {
		return hasAnniversery;
	}

	public void setHasAnniversery(boolean hasAnniversery) {
		DbManager.PlayerCharacterQueries.SET_ANNIVERSERY(this, hasAnniversery);
		this.hasAnniversery = hasAnniversery;
	}

	public int getSpamCount() {
		return spamCount;
	}

	public void setSpamCount(int spamCount) {
		this.spamCount = spamCount;
	}


	public String getHash() {
		return hash;
	}

	public void setHash() {

		this.hash = DataWarehouse.hasher.encrypt(this.getObjectUUID());

		// Write hash to player character table

		DataWarehouse.writeHash(DataRecordType.CHARACTER, this.getObjectUUID());
	}

	public AtomicInteger getGuildStatus() {
		return guildStatus;
	}

	public static int GetPlayerRealmTitle(PlayerCharacter player){
	
		if (player.getGuild().isErrant())
			return 0;
		if (!player.getGuild().isGuildLeader(player.getObjectUUID()))
			return 0;
		if (player.getGuild().getOwnedCity() == null)
			return 10;
		if (player.getGuild().getOwnedCity().getRealm() == null)
			return 10;
		if (player.getGuild().getOwnedCity().getRealm().getRulingCity() == null)
			return 10;

		if (player.getGuild().getOwnedCity().getRealm().getRulingCity().getObjectUUID() != player.getGuild().getOwnedCity().getObjectUUID())
			return 10;
		int realmTitle = 1;
		if (player.getGuild().getSubGuildList() == null || player.getGuild().getSubGuildList().isEmpty())
			return 11;
		for (Guild subGuild: player.getGuild().getSubGuildList()){
			if (subGuild.getOwnedCity() == null)
				continue;
			if (subGuild.getOwnedCity().getRealm() == null)
				continue;
			if (subGuild.getOwnedCity().getRealm().getRulingCity() == null)
				continue;
			if (subGuild.getOwnedCity().getRealm().getRulingCity().getObjectUUID() != subGuild.getOwnedCity().getObjectUUID())
				continue;
			realmTitle++;
		}

		if (realmTitle < 3)
			return 11;
		else if (realmTitle < 5)
			return 12;
		else
			return 13;
	}
	public static void UpdateClientPlayerRank(PlayerCharacter pc){
		if (pc == null)
			return;
		boolean disable = true;
		
		if (disable)
			return;
		UpdateCharOrMobMessage ucm = new UpdateCharOrMobMessage(pc,2,pc.getRank());
		DispatchMessage.sendToAllInRange(pc, ucm);
	}

	public void setLastRealmID(int lastRealmID) {
		this.lastRealmID = lastRealmID;
	}

	public int getLastRealmID() {
		return lastRealmID;
	}

	public int getSubRaceID() {
		return subRaceID;
	}

	public void setSubRaceID(int subRaceID) {
		this.subRaceID = subRaceID;
	}

	public ArrayList<GuildHistory> getGuildHistory() {
		return guildHistory;
	}

	public void setGuildHistory(ArrayList<GuildHistory> guildHistory) {
		this.guildHistory = guildHistory;
	}

	public void moveTo(Vector3fImmutable endLoc){
		this.setInBuilding(-1);
		this.setInFloorID(-1);
		MoveToPointMsg moveToMsg = new MoveToPointMsg();
		moveToMsg.setStartCoord(this.getLoc());
		moveToMsg.setEndCoord(endLoc);
		moveToMsg.setInBuilding(-1);
		moveToMsg.setUnknown01(-1);
		moveToMsg.setSourceType(GameObjectType.PlayerCharacter.ordinal());
		moveToMsg.setSourceID(this.getObjectUUID());

		Dispatch dispatch = Dispatch.borrow(this, moveToMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

		try {
			MovementManager.movement(moveToMsg, this);
		} catch (MsgSendException e) {
			// TODO Auto-generated catch block
			Logger.error("Player.MoveTo", this.getName() + " tripped error " + e.getMessage());
		}

	}

	public void updateScaleHeight(){
		
		float strengthScale = 0;
		float unknownScale1 = 0;
		float unknownScale2 = 0;
		float unknownScale3 = 0;
		
		float scaleHeight = 0;

		if ((int) this.statStrBase > 40)
			strengthScale = ((int) this.statStrBase - 40)* 0.0024999999f; //Y scale ?

		unknownScale1 = (float) (((int) this.statStrBase * 0.0024999999f + strengthScale + 0.89999998) * race.getRaceType().getScaleHeight());
		strengthScale = (int) this.statStrBase * 0.0037499999f + strengthScale + 0.85000002f; //strengthScale is different for x and z

		unknownScale2 = strengthScale * race.getRaceType().getScaleHeight(); //x scale?
		unknownScale3 = strengthScale * race.getRaceType().getScaleHeight(); //z Scale?
		
	

		scaleHeight = (1.5f + unknownScale1);
		
	
		
		this.characterHeight = scaleHeight;
		
		this.centerHeight = scaleHeight;

	}

	public int getOverFlowEXP() {
		return overFlowEXP;
	}

	public void setOverFlowEXP(int overFlowEXP) {
		this.overFlowEXP = overFlowEXP;
	}
	
	public static void GroundPlayer(PlayerCharacter groundee){
		if (groundee.getDesiredAltitude() == 0 && groundee.getAltitude() == 0)
			return;
		groundee.setAltitude(groundee.getAltitude());
		groundee.setDesiredAltitude(0);
		groundee.setTakeOffTime(System.currentTimeMillis());
		
		ChangeAltitudeMsg msg = ChangeAltitudeMsg.GroundPlayerMsg(groundee);
		// force a landing
		DispatchMessage.dispatchMsgToInterestArea(groundee, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
	
	}

	public MovementState getLastMovementState() {
		return lastMovementState;
	}

	public void setLastMovementState(MovementState lastMovementState) {
		this.lastMovementState = lastMovementState;
	}
	@Override
	public final void setIsCasting(final boolean isCasting) {
		if (this.isCasting != isCasting)
			this.update();
		this.isCasting = isCasting;
	}
	@Override
	public void setItemCasting(boolean itemCasting) {
		if (this.itemCasting != itemCasting)
			this.dynamicUpdate(UpdateType.REGEN);
		this.itemCasting = itemCasting;
	}
	
	public void resetRegenUpdateTime(){
		this.lastUpdateTime = System.currentTimeMillis();
		this.lastStamUpdateTime = System.currentTimeMillis();
	}

	public float getCharacterHeight() {
		return characterHeight;
	}

	public void setCharacterHeight(float characterHeight) {
		this.characterHeight = characterHeight;
	}

	public void setCenterHeight(float centerHeight) {
		this.centerHeight = centerHeight;
	}
	
	public static boolean CanBreathe(PlayerCharacter breather){
		try{
			if (breather.isFlying())
				return true;
			Zone zone = ZoneManager.findSmallestZone(breather.getLoc());

			if (zone.getSeaLevel() != 0){

				float localAltitude = breather.getLoc().y;


				if (localAltitude  + breather.characterHeight  < zone.getSeaLevel() -2)
					return false;
				
				if (breather.isMoving()){
					if (localAltitude + breather.characterHeight <  zone.getSeaLevel())
						return false;
				}
			}else{
				if (breather.getLoc().y + breather.characterHeight < -2)
					return false;
				
				if (breather.isMoving()){
					if (breather.getLoc().y + breather.characterHeight < 0)
						return false;
				}
			}
			
			
		}catch(Exception e){
			Logger.info(breather.getName() + e);
		}

	
		return true;
	}
	
	public static boolean enterWater(PlayerCharacter enterer){
		
		try{
			if (enterer.isFlying())
				return false;

			

			Zone zone = ZoneManager.findSmallestZone(enterer.getLoc());

			if (zone.getSeaLevel() != 0){

				float localAltitude = enterer.getLoc().y + enterer.characterHeight ;


				if (localAltitude < zone.getSeaLevel())
					return true;
			}else{
				if (enterer.getLoc().y + enterer.characterHeight  < 0)
					return true;
			}
		}catch(Exception e){
			Logger.info(enterer.getName() + e);
		}

		return false;
		
	}
	
	public static boolean LeaveWater(PlayerCharacter leaver){
		
		try{
			

			Zone zone = ZoneManager.findSmallestZone(leaver.getLoc());
			
			float leaveWater = leaver.centerHeight;
			
			if (leaver.isMoving())
				leaveWater = 1f;
			

			if (zone.getSeaLevel() != 0){

				float localAltitude = leaver.getLoc().y;


				if (localAltitude + leaveWater < zone.getSeaLevel())
					return false;
			}else{
				if (leaver.getLoc().y + leaveWater < 0)
					return false;
			}
		}catch(Exception e){
			Logger.info(leaver.getName() + e);
		}

		return true;
	}

	public boolean isEnteredWorld() {
		return enteredWorld;
	}

	public void setEnteredWorld(boolean enteredWorld) {
		this.enteredWorld = enteredWorld;
	}

	public long getChannelMute() {
		return channelMute;
	}

	public void setChannelMute(long channelMute) {
		this.channelMute = channelMute;
	}

	public boolean isLastSwimming() {
		return lastSwimming;
	}

	public boolean isTeleporting() {
		return isTeleporting;
	}

	public void setTeleporting(boolean isTeleporting) {
		this.isTeleporting = isTeleporting;
	}
	
	@Override
	public final void teleport(final Vector3fImmutable targetLoc) {
		locationLock.writeLock().lock();
		try{
			MovementManager.translocate(this, targetLoc,null);
		}catch(Exception e){
			Logger.error(e);
		}finally{
			locationLock.writeLock().unlock();
		}
	}

	public ReadWriteLock getTeleportLock() {
		return teleportLock;
	}
	
	public static boolean CanBindToBuilding(PlayerCharacter player, int buildingID){
		if (buildingID == 0)
			return false;
		
		Building bindBuilding = BuildingManager.getBuildingFromCache(buildingID);
		
		if (bindBuilding == null)
			return false;
		
		if(!BuildingManager.playerCanManage(player, bindBuilding))
		return false;
		
		return true;
	}
	
	public float getBargain(){
		float bargain = 0;
		
		
		CharacterSkill bargainSkill = this.getSkills().get(engine.Enum.CharacterSkills.Bargaining.name());
	
		if (bargainSkill != null)
			bargain = bargainSkill.getModifiedAmountBeforeMods();
		
		if (bargain > 100)
			bargain = 100;
		
		bargain *= .01f;
		
		return bargain;
	}
}
