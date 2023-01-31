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
import engine.InterestManagement.InterestManager;
import engine.InterestManagement.WorldGrid;
import engine.ai.StaticMobActions;
import engine.exception.SerializationException;
import engine.gameManager.*;
import engine.job.AbstractJob;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.ChantJob;
import engine.jobs.PersistentAoeJob;
import engine.jobs.TrackJob;
import engine.math.AtomicFloat;
import engine.math.Bounds;
import engine.math.Vector3fImmutable;
import engine.net.ByteBufferWriter;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.msg.MoveToPointMsg;
import engine.powers.EffectsBase;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractCharacter extends AbstractWorldObject {

	public String firstName;
	public String lastName;
	public short statStrCurrent;
	public short statDexCurrent;
	public short statConCurrent;
	public short statIntCurrent;
	public short statSpiCurrent;
	protected short unusedStatPoints;
	public short level;
	protected int exp;
	public Vector3fImmutable bindLoc;
	protected Vector3fImmutable faceDir;
	protected Guild guild;
	protected byte runningTrains;
	protected ConcurrentHashMap<Integer, CharacterPower> powers;
	public ConcurrentHashMap<String, CharacterSkill> skills;
	public final CharacterItemManager charItemManager;

	// Variables NOT to be stored in db
	protected boolean sit = false;
	public boolean walkMode;
	public boolean combat = false;

	protected Vector3fImmutable startLoc = Vector3fImmutable.ZERO;
	protected Vector3fImmutable endLoc = Vector3fImmutable.ZERO;
    private float desiredAltitude = 0;
	private long takeOffTime = 0;
	protected boolean itemCasting = false;

	// nextEndLoc is used to store the next end location when someone is clicking
	// around the ground while other timers like changeAltitude are still
	// ticking down so that mobs/players following dont just move away to your projected location
	protected Vector3fImmutable nextEndLoc = Vector3fImmutable.ZERO;

	protected float speed;
	public AtomicFloat stamina = new AtomicFloat();
	public float staminaMax;
	public AtomicFloat mana = new AtomicFloat();
	public float manaMax;                                            // Health/Mana/Stamina
	public AtomicBoolean isAlive = new AtomicBoolean(true);
	protected Resists resists = new Resists("Genric");
	public AbstractWorldObject combatTarget;
	protected ConcurrentHashMap<String, JobContainer> timers;
	protected ConcurrentHashMap<String, Long> timestamps;
	public int atrHandOne;
	public int atrHandTwo;
	public int minDamageHandOne;
	public int maxDamageHandOne;
	public int minDamageHandTwo;
	public int maxDamageHandTwo;
	public float rangeHandOne;
	public float rangeHandTwo;
	public float speedHandOne;
	public float speedHandTwo;
	public int defenseRating;
	public boolean isActive; // <-Do not use this for deleting character!
	protected float altitude = 0; // 0=on terrain, 1=tier 1, 2=tier 2, etc.
	protected ConcurrentHashMap<Integer, JobContainer> recycleTimers;
	public PlayerBonuses bonuses;
	protected JobContainer lastChant;
	protected boolean isCasting = false;
	private final ReentrantReadWriteLock healthLock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock teleportLock = new ReentrantReadWriteLock();
	protected long lastSetLocUpdate = 0L;
	protected int inBuilding = -1; // -1 not in building 0 on ground floor, 1 on first floor etc
	protected int inBuildingID = 0;
	protected int inFloorID = -1;
	protected int liveCounter = 0;

	protected int debug = 0;
	private float hateValue = 0;
	private long lastHateUpdate = 0;
	private boolean collided = false;
	protected Regions lastRegion = null;
	
	protected boolean movingUp = false;
	

	/**
	 * No Id Constructor
	 */
	public AbstractCharacter(
			final String firstName,
			final String lastName,
			final short statStrCurrent,
			final short statDexCurrent,
			final short statConCurrent,
			final short statIntCurrent,
			final short statSpiCurrent,
			final short level,
			final int exp,
			final Vector3fImmutable bindLoc,
			final Vector3fImmutable currentLoc,
			final Vector3fImmutable faceDir,
			final Guild guild,
			final byte runningTrains
			) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;

		this.statStrCurrent = statStrCurrent;
		this.statDexCurrent = statDexCurrent;
		this.statConCurrent = statConCurrent;
		this.statIntCurrent = statIntCurrent;
		this.statSpiCurrent = statSpiCurrent;
		this.level = level;
		this.exp = exp;
		this.walkMode = true;
		this.bindLoc = bindLoc;
		if (ConfigManager.serverType.equals(Enum.ServerType.WORLDSERVER))
		this.setLoc(currentLoc);
		this.faceDir = faceDir;
		this.guild = guild;
		this.runningTrains = runningTrains;
		this.powers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		this.skills = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		this.initializeCharacter();

		// Dangerous to use THIS in a constructor!!!
		this.charItemManager = new CharacterItemManager(this);
	}

	/**
	 * Normal Constructor
	 */
	public AbstractCharacter(
			final String firstName,
			final String lastName,
			final short statStrCurrent,
			final short statDexCurrent,
			final short statConCurrent,
			final short statIntCurrent,
			final short statSpiCurrent,
			final short level,
			final int exp,
			final Vector3fImmutable bindLoc,
			final Vector3fImmutable currentLoc,
			final Vector3fImmutable faceDir,
			final Guild guild,
			final byte runningTrains,
			final int newUUID
			) {

		super(newUUID);
		this.firstName = firstName;
		this.lastName = lastName;

		this.statStrCurrent = statStrCurrent;
		this.statDexCurrent = statDexCurrent;
		this.statConCurrent = statConCurrent;
		this.statIntCurrent = statIntCurrent;
		this.statSpiCurrent = statSpiCurrent;
		this.level = level;
		this.exp = exp;
		this.walkMode = true;

		this.bindLoc = bindLoc;
		if (ConfigManager.serverType.equals(Enum.ServerType.WORLDSERVER))
		this.setLoc(currentLoc);
		this.faceDir = faceDir;
		this.guild = guild;

		this.runningTrains = runningTrains;
		this.powers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		this.skills = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		this.initializeCharacter();

		// Dangerous to use THIS in a constructor!!!
		this.charItemManager = new CharacterItemManager(this);
	}

	/**
	 * ResultSet Constructor for players
	 */
	public AbstractCharacter(
			final ResultSet rs,
			final boolean isPlayer
			) throws SQLException {
		super(rs);

		this.firstName = rs.getString("char_firstname");
		this.lastName = rs.getString("char_lastname");

		this.level = 1;
		this.exp = rs.getInt("char_experience");
		this.walkMode = false;

		this.bindLoc = new Vector3fImmutable(0f, 0f, 0f);
		this.endLoc = Vector3fImmutable.ZERO;

		this.faceDir = Vector3fImmutable.ZERO;

		final int guildID = rs.getInt("GuildUID");
		final Guild errantGuild = Guild.getErrantGuild();

		if (guildID == errantGuild.getObjectUUID()) {
			this.guild = errantGuild;
		}
		else {
			this.guild = Guild.getGuild(guildID);
			if (this.guild == null) {
				this.guild = Guild.getErrantGuild();
			}
		}
		
		if (this.guild == null)
			this.guild = errantGuild;

		this.skills = new ConcurrentHashMap<>();
		this.powers = new ConcurrentHashMap<>();
		this.initializeCharacter();

		// Dangerous to use THIS in a constructor!!!
		this.charItemManager = new CharacterItemManager(this);
	}

	/**
	 * ResultSet Constructor for NPC/Mobs
	 */
	public AbstractCharacter(final ResultSet rs) throws SQLException {
		super(rs);

		this.firstName = "";
		this.lastName = "";

		this.statStrCurrent = (short) 0;
		this.statDexCurrent = (short) 0;
		this.statConCurrent = (short) 0;
		this.statIntCurrent = (short) 0;
		this.statSpiCurrent = (short) 0;

		this.unusedStatPoints = (short) 0;

		this.level = (short) 0; // TODO get this from MobsBase later
		this.exp = 1;
		this.walkMode = true;

		//this.bindLoc = new Vector3fImmutable(rs.getFloat("spawnX"), rs.getFloat("spawnY"), rs.getFloat("spawnZ"));
		this.bindLoc = Vector3fImmutable.ZERO;
		//setLoc(this.bindLoc);

		this.faceDir = Vector3fImmutable.ZERO;

		this.runningTrains = (byte) 0;

		this.skills = new ConcurrentHashMap<>();
		this.powers = new ConcurrentHashMap<>();
		initializeCharacter();

		// Dangerous to use THIS in a constructor!!!
		this.charItemManager = new CharacterItemManager(this);
	}

	/**
	 * ResultSet Constructor for static Mobs
	 */
	public AbstractCharacter(final ResultSet rs, final int objectUUID) throws SQLException {

		super(objectUUID);

		this.firstName = "";
		this.lastName = "";

		this.statStrCurrent = (short) 0;
		this.statDexCurrent = (short) 0;
		this.statConCurrent = (short) 0;
		this.statIntCurrent = (short) 0;
		this.statSpiCurrent = (short) 0;

		this.unusedStatPoints = (short) 0;

		this.level = (short) 0; // TODO get this from MobsBase later
		this.exp = 1;
		this.walkMode = true;

		this.bindLoc = new Vector3fImmutable(rs.getFloat("spawnX"), rs.getFloat("spawnY"), rs.getFloat("spawnZ"));

		if (ConfigManager.serverType.equals(Enum.ServerType.WORLDSERVER))
		this.setLoc(this.bindLoc);
		this.endLoc = Vector3fImmutable.ZERO;


		this.faceDir = Vector3fImmutable.ZERO;

		final int guildID = rs.getInt("GuildID");
		
		if (guildID == Guild.getErrantGuild().getObjectUUID()) {
			this.guild = Guild.getErrantGuild();
		}
		else {
			this.guild = Guild.getGuild(guildID);
		}
		
		if (this.guild == null)
			this.guild = Guild.getErrantGuild();

		this.runningTrains = (byte) 0;
		this.skills = new ConcurrentHashMap<>();
		this.powers = new ConcurrentHashMap<>();

		this.initializeCharacter();

		// Dangerous to use THIS in a constructor!!!
		this.charItemManager = new CharacterItemManager(this);
	}

	private void initializeCharacter() {
		this.timers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		this.timestamps = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		final long l = System.currentTimeMillis();
		this.timestamps.put("Health Recovery", l);
		this.timestamps.put("Stamina Recovery", l);
		this.timestamps.put("Mana Recovery", l);
		this.recycleTimers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	}

	protected abstract ConcurrentHashMap<Integer, CharacterPower> initializePowers();

	private byte aoecntr = 0;

	public final void addPersistantAoe(
			final String name,
			final int duration,
			final PersistentAoeJob asj,
			final EffectsBase eb,
			final int trains
			) {
		if (!isAlive()) {
			return;
		}
		final JobContainer jc = JobScheduler.getInstance().scheduleJob(asj, duration);
		final Effect eff = new Effect(jc, eb, trains);
		aoecntr++;
		this.effects.put(name + aoecntr, eff);
		eff.setPAOE();
	}

	public final void setLastChant(
			final int duration,
			final ChantJob cj
			) {
		if (!isAlive()) {
			return;
		}
		if (this.lastChant != null) {
			this.lastChant.cancelJob();
		}
		this.lastChant = JobScheduler.getInstance().scheduleJob(cj, duration);
	}


	public final void cancelLastChant() {
		if (this.lastChant != null) {
			this.lastChant.cancelJob();
			this.lastChant = null;
		}
	}

	public final void cancelLastChantIfSame(final Effect eff) {
		if (eff == null || this.lastChant == null) {
			return;
		}
		final AbstractJob aj = this.lastChant.getJob();
		if (aj == null || (!(aj instanceof ChantJob))) {
			return;
		}
		final int token = ((ChantJob) aj).getPowerToken();
		if (eff.getPowerToken() == token && token != 0) {
			this.cancelLastChant();
		}
	}

	/*
	 * Getters
	 */
	public final short getUnusedStatPoints() {
		return this.unusedStatPoints;
	}

	public final void setUnusedStatPoints(final short value) {
		this.unusedStatPoints = value;
	}

	public final CharacterItemManager getCharItemManager() {
		return this.charItemManager;
	}

	public final void setDebug(
			final int value,
			final boolean toggle
			) {
		if (toggle) {
			this.debug |= value; //turn on debug
		}
		else {
			this.debug &= ~value; //turn off debug
		}
	}

	public final boolean getDebug(final int value) {
		return ((this.debug & value) != 0);
	}

	@Override
	public String getName() {
		if (this.firstName.length() == 0 && this.lastName.length() == 0) {
			return "Unnamed " + '(' + this.getObjectUUID() + ')';
		}
		else if (this.lastName.length() == 0) {
			return this.getFirstName();
		}
		else {
			return this.getFirstName() + ' ' + this.getLastName();
		}
	}

	public String getFirstName() {
		return this.firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setFirstName(final String name) {
		this.firstName = name;
	}

	public void setLastName(final String name) {
		this.lastName = name;
	}

	public final short getStatStrCurrent() {
		return this.statStrCurrent;
	}

	public final short getStatDexCurrent() {
		return this.statDexCurrent;
	}

	public final short getStatConCurrent() {
		return this.statConCurrent;
	}

	public final short getStatIntCurrent() {
		return this.statIntCurrent;
	}

	public final short getStatSpiCurrent() {
		return this.statSpiCurrent;
	}

	public final void setStatStrCurrent(final short value) {
		this.statStrCurrent = (value < 1) ? (short) 1 : value;
	}

	public final void setStatDexCurrent(final short value) {
		this.statDexCurrent = (value < 1) ? (short) 1 : value;
	}

	public final void setStatConCurrent(final short value) {
		this.statConCurrent = (value < 1) ? (short) 1 : value;
	}

	public final void setStatIntCurrent(final short value) {
		this.statIntCurrent = (value < 1) ? (short) 1 : value;
	}

	public final void setStatSpiCurrent(final short value) {
		this.statSpiCurrent = (value < 1) ? (short) 1 : value;
	}

	public short getLevel() {
		return this.level;
	}

	public void setLevel(final short value) {
		this.level = value;
	}

	public final boolean isActive() {
		return this.isActive;
	}

	public final void setActive(final boolean value) {
		this.isActive = value;
	}

	public final Resists getResists() {
		if (this.resists == null)
			return Resists.getResists(0);
		return this.resists;
	}

	public final void setResists(final Resists value) {
		this.resists = value;
	}

	public final int getExp() {
		return this.exp;
	}

	public final void setExp(final int value) {
		this.exp = value;
	}

	public final void setLastPower(final JobContainer jc) {
		if (this.timers != null) {
			this.timers.put("LastPower", jc);
		}
	}

	public final JobContainer getLastPower() {
		if (this.timers == null) {
			return null;
		}
		return this.timers.get("LastPower");
	}

	public final void clearLastPower() {
		if (this.timers != null) {
			this.timers.remove("LastPower");
		}
	}

	public final void setLastItem(final JobContainer jc) {
		if (this.timers != null) {
			this.timers.put("LastItem", jc);
		}
	}

	public final JobContainer getLastItem() {
		if (this.timers == null) {
			return null;
		}
		return this.timers.get("LastItem");
	}

	public final void clearLastItem() {
		if (this.timers != null) {
			this.timers.remove("LastItem");
		}
	}

	public final int getIsSittingAsInt() {
		if (!this.isAlive()) {
			return 1;
		}

		if (this.sit) {
			return 4;
		}
		else {
			if (this.isMoving())
			return 7;
			else
				return 5;
		}
	}

	public final int getIsWalkingAsInt() {
		if (this.walkMode) {
			return 1;
		}
		return 2;
	}

	public final int getIsCombatAsInt() {
		if (this.combat) {
			return 2;
		}
		return 1;
	}

	public final int getIsFlightAsInt() {
		if (this.altitude > 0) {
			return 3;
		}
	
		if (this.getObjectType().equals(GameObjectType.PlayerCharacter))
			if (((PlayerCharacter)this).isLastSwimming())
			return 1; //swimming
	
		return 2; //ground
	}


	public final void clearTimer(final String name) {
		if (this.timers != null) {
			this.timers.remove(name);
		}
	}

	public abstract Vector3fImmutable getBindLoc();


	public final Vector3fImmutable getFaceDir() {
		return this.faceDir;
	}

	public final Vector3fImmutable getStartLoc() {
		return this.startLoc;
	}

	public final Vector3fImmutable getEndLoc() {
		return this.endLoc;
	}

	public final Vector3fImmutable getNextEndLoc() {
		// this is only used when users are changing their end
		// location while a timer like changeAltitude is ticking down
		return this.nextEndLoc;
	}

	public final void stopMovement(Vector3fImmutable stopLoc) {
		
		
		locationLock.writeLock().lock();
		
		try{
				this.setLoc(stopLoc);
				this.endLoc = Vector3fImmutable.ZERO;
				this.resetLastSetLocUpdate();
		}catch(Exception e){
			Logger.error(e);
		}finally{
			locationLock.writeLock().unlock();
		}
	}

	public final boolean isMoving() {

		// I might be on my way but my movement is paused
		// due to a flight alt change
		//TODO who the fuck wrote changeHeightJob. FIX THIS.


		if (this.endLoc.equals(Vector3fImmutable.ZERO))
			return false;
		
		if (this.takeOffTime != 0)
			return false;
		
		if (this.isCasting && this.getObjectType().equals(GameObjectType.PlayerCharacter))
			return false;

		return true;
	}
	
	
	public final boolean useFlyMoveRegen() {

		
		if (this.endLoc.x != 0 && this.endLoc.z != 0)
			return true;
		
		return false;
	}

	public boolean asciiLastName() {
		return true;
	}

	public final ConcurrentHashMap<String, CharacterSkill> getSkills() {
		return this.skills;
	}

	public final ConcurrentHashMap<Integer, CharacterPower> getPowers() {
		return this.powers;
	}

	public final int getInBuilding() {
		return this.inBuilding;
	}

	public Guild getGuild() {
		return this.guild;
	}

	public int getGuildUUID() {
			return this.guild.getObjectUUID();
	}


	public final int getRank() {
		return (this.level / 10);
	}

	public final int getAtrHandOne() {
		return this.atrHandOne;
	}

	public final int getAtrHandTwo() {
		return this.atrHandTwo;
	}

	public final int getMinDamageHandOne() {
		return this.minDamageHandOne;
	}

	public final int getMaxDamageHandOne() {
		return this.maxDamageHandOne;
	}

	public final int getMinDamageHandTwo() {
		return this.minDamageHandTwo;
	}

	public final int getMaxDamageHandTwo() {
		return this.maxDamageHandTwo;
	}

	public final int getDefenseRating() {
		return this.defenseRating;
	}

	public final float getRangeHandOne() {
		return this.rangeHandOne;
	}

	public final float getRangeHandTwo() {
		return this.rangeHandTwo;
	}

	public final float getSpeedHandOne() {
		return this.speedHandOne;
	}

	public final float getSpeedHandTwo() {
		return this.speedHandTwo;
	}

	public final float getRange() {

		// Treb range does not appear to be set here
		// what gives?

		if (this.getObjectType() == GameObjectType.Mob) {
			Mob mob = (Mob) this;
			if (mob.isSiege) {
				return 300;
			}
		}
		if (this.rangeHandOne > this.rangeHandTwo) {
			return this.rangeHandOne;
		}
		return this.rangeHandTwo;
	}

	public abstract float getPassiveChance(
			final String type,
			final int attackerLevel,
			final boolean fromCombat);

	public abstract float getSpeed();

	public static int getBankCapacity() {
		return 500;
	}

	public final int getBankCapacityRemaining() {
		return (AbstractCharacter.getBankCapacity() - this.charItemManager.getBankWeight());
	}

	public static int getVaultCapacity() {
		return 5000;
	}

	public final int getVaultCapacityRemaining() {
		return (AbstractCharacter.getVaultCapacity() - this.charItemManager.getVaultWeight());
	}

	public final ArrayList<Item> getInventory() {
		return this.getInventory(false);
	}

	public final ArrayList<Item> getInventory(final boolean getGold) {
		if (this.charItemManager == null) {
			return new ArrayList<>();
		}
		return this.charItemManager.getInventory(getGold);
	}

	@Override
	public Vector3fImmutable getLoc() {

		return super.getLoc();
	}
	
	public Vector3fImmutable getMovementLoc() {

		if (this.endLoc.equals(Vector3fImmutable.ZERO))
			return super.getLoc();		
		if (this.takeOffTime != 0)
			return super.getLoc();
		
		return super.getLoc().moveTowards(this.endLoc, this.getSpeed() * ((System.currentTimeMillis() - lastSetLocUpdate) * .001f));

	}

	/*
	 * Setters
	 */
	public void setGuild(final Guild value) {
		this.guild = value;
	}

	public final void setBindLoc(final float x, final float y, final float z) {
		this.bindLoc = new Vector3fImmutable(x, y, z);
	}

	public final void setEndLoc(final Vector3fImmutable value) {
		if(value.x > MBServerStatics.MAX_PLAYER_X_LOC)
			return;
		if (value.z < MBServerStatics.MAX_PLAYER_Y_LOC)
			return;

		this.endLoc = value;
		// reset the location timer so our next call to getLoc is correct
		this.resetLastSetLocUpdate();
	}

	public final void resetLastSetLocUpdate() {
		this.lastSetLocUpdate = System.currentTimeMillis();
	}

	public final void setBindLoc(final Vector3fImmutable value) {
		this.bindLoc = value;
	}

	@Override
	public final void setLoc(final Vector3fImmutable value) {
		super.setLoc(value); // set the location in the world
		this.resetLastSetLocUpdate();
		//Logger.info("AbstractCharacter", "Setting char location to :" + value.getX()  + " " + value.getZ());
	}

	public final void setFaceDir(final Vector3fImmutable value) {
		this.faceDir = value;
	}

	public void setIsCasting(final boolean isCasting) {
		this.isCasting = isCasting;
	}

	public final boolean isCasting() {
		return this.isCasting;
	}

	@Override
	public final boolean isAlive() {
		return this.isAlive.get();
	}

	public final boolean isSafeMode() {

		if (this.resists == null)
			return false;

		for (Effect eff: this.getEffects().values()){
			if (eff.getEffectToken() == -1661750486)
				return true;
		}
		return this.resists.immuneToAll();
	}

	public abstract void killCharacter(final AbstractCharacter killer);

	public abstract void killCharacter(final String reason);

	/**
	 * Determines if the character is in a lootable state.
	 *
	 * @return True if lootable.
	 */
	public abstract boolean canBeLooted();
	/*
	 * Utils
	 */

	public float calcHitBox() {
		if (this.getObjectType() == GameObjectType.PlayerCharacter) {
			// hit box radius is str/100 (gets diameter of hitbox) /2 (as we want a radius)
			// note this formula is guesswork
			if (MBServerStatics.COMBAT_TARGET_HITBOX_DEBUG) {
				Logger.info( "Hit box radius for " + this.getFirstName() + " is " + (this.statStrCurrent / 200f));
			}
			return ((PlayerCharacter) this).getStrForClient() / 200f;
			//TODO CALCULATE MOB HITBOX BECAUSE FAIL  EMU IS FAIL!!!!!!!
		}
		else if (this.getObjectType() == GameObjectType.Mob) {
			if (MBServerStatics.COMBAT_TARGET_HITBOX_DEBUG) {
				Logger.info("Hit box radius for " + this.getFirstName() + " is " + ((Mob) this).getMobBase().getHitBoxRadius());
			}
			return ((Mob) this).getMobBase().getHitBoxRadius();
		}
		return 0f;
	}

	public final boolean isSit() {
		return this.sit;
	}

	public final boolean isWalk() {
		return this.walkMode;
	}

	public final boolean isCombat() {
		return this.combat;
	}

	public final void setSit(final boolean value) {

		if (this.sit != value) {
			// change sit/stand and sync location
			this.sit = value;
			if (value == true) // we have been told to sit
			{
				this.stopMovement(this.getLoc());
			}
		}

	}

	public final void setWalkMode(final boolean value) {
		// sync movement location as getLoc gets where we are at the exact moment in time (i.e. not the last updated loc)
		this.setLoc(this.getLoc());
		if (this.walkMode == value) {
			return;
		}
		else {
			this.walkMode = value;
		}
	}

	public final void setCombat(final boolean value) {
		this.combat = value;
	}

	public final void setInBuilding(final int floor) {
		this.inBuilding = floor;
	}

	public final AbstractWorldObject getCombatTarget() {
		return this.combatTarget;
	}

	public final void setCombatTarget(final AbstractWorldObject value) {
		this.combatTarget = value;
	}

	public final ConcurrentHashMap<String, JobContainer> getTimers() {
		if (this.timers == null) {
			this.timers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		}
		return this.timers;
	}

	public final int getLiveCounter() {
		return this.liveCounter;
	}

	public final void addTimer(
			final String name,
			final AbstractJob asj,
			final int duration
			) {
		final JobContainer jc = JobScheduler.getInstance().scheduleJob(asj, duration);
		if (this.timers == null) {
			this.timers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		}
		this.timers.put(name, jc);
	}

	public final void renewTimer(
			final String name,
			final AbstractJob asj,
			final int duration
			) {
		this.cancelTimer(name);
		this.addTimer(name, asj, duration);
	}

	public final ConcurrentHashMap<Integer, JobContainer> getRecycleTimers() {
		return this.recycleTimers;
	}

	public final ConcurrentHashMap<String, Long> getTimestamps() {
		return this.timestamps;
	}

	public final long getTimeStamp(final String name) {
		if (this.timestamps.containsKey(name)) {
			return this.timestamps.get(name);
		}
		return 0L;
	}

	public final void setTimeStamp(final String name, final long value) {
		this.timestamps.put(name, value);
	}

	public final void setTimeStampNow(final String name) {
		this.timestamps.put(name, System.currentTimeMillis());
	}

	public final void cancelTimer(final String name) {
		cancelTimer(name, true);
	}

	public final void cancelTimer(final String name, final boolean jobRunning) {
		if (this.timers == null) {
			this.timers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		}
		if (this.timers.containsKey(name)) {
			if (jobRunning) {
				this.timers.get(name).cancelJob();
			}
			this.timers.remove(name);
		}
	}

	public final float modifyHealth(
			final float value,
			final AbstractCharacter attacker,
			final boolean fromCost) {
		
		try{
			
			try{
			boolean ready = this.healthLock.writeLock().tryLock(1, TimeUnit.SECONDS);
			
			while (!ready)
					ready = this.healthLock.writeLock().tryLock(1, TimeUnit.SECONDS);

		if (!this.isAlive())
			return 0;
		
		Float oldHealth, newHealth;

			if (!this.isAlive())
				return 0f;

			oldHealth = this.health.get();
			newHealth = oldHealth + value;

			if (newHealth > this.healthMax)
				newHealth = healthMax;

			 this.health.set(newHealth);
		
		if (newHealth <= 0) {
			if (this.isAlive.compareAndSet(true, false)) {
				killCharacter(attacker);
				return newHealth - oldHealth;
			}
			else
				return 0f; //already dead, don't send damage again
		}                                 // past this lock!

		//TODO why is Handle REtaliate and cancelontakedamage in modifyHealth? shouldnt this be outside this method?
		if (value < 0f && !fromCost) {
			this.cancelOnTakeDamage();
			CombatManager.handleRetaliate(this, attacker);
		}

		return newHealth - oldHealth;
			}finally{
				this.healthLock.writeLock().unlock();
			}
		
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public float getCurrentHitpoints() {
		return this.health.get();
	}

	public final float modifyMana(
			final float value,
			final AbstractCharacter attacker
			) {
		return this.modifyMana(value, attacker, false);
	}

	public final float modifyMana(
			final float value,
			final AbstractCharacter attacker,
			final boolean fromCost
			) {

		if (!this.isAlive()) {
			return 0f;
		}
		boolean worked = false;
		Float oldMana = 0f, newMana = 0f;
		while (!worked) {
			oldMana = this.mana.get();
			newMana = oldMana + value;
			if (newMana > this.manaMax) {
				newMana = manaMax;
			}
			else if (newMana < 0) {
				newMana = 0f;
			}
			worked = this.mana.compareAndSet(oldMana, newMana);
		}
		if (value < 0f && !fromCost) {
			this.cancelOnTakeDamage();
			CombatManager.handleRetaliate(this, attacker);
		}
		return newMana - oldMana;
	}

	public final float modifyStamina(
			final float value,
			final AbstractCharacter attacker
			) {
		return this.modifyStamina(value, attacker, false);
	}

	public final float modifyStamina(
			final float value,
			final AbstractCharacter attacker,
			final boolean fromCost
			) {

		if (!this.isAlive()) {
			return 0f;
		}
		boolean worked = false;
		Float oldStamina = 0f, newStamina = 0f;
		while (!worked) {
			oldStamina = this.stamina.get();
			newStamina = oldStamina + value;
			if (newStamina > this.staminaMax) {
				newStamina = staminaMax;
			}
			else if (newStamina < 0) {
				newStamina = 0f;
			}
			worked = this.stamina.compareAndSet(oldStamina, newStamina);
		}
		if (value < 0f && !fromCost) {
			this.cancelOnTakeDamage();
			CombatManager.handleRetaliate(this, attacker);
		}
		return newStamina - oldStamina;
	}

	public final float setMana(
			final float value,
			final AbstractCharacter attacker
			) {
		return setMana(value, attacker, false);
	}

	public final float setMana(
			final float value,
			final AbstractCharacter attacker,
			final boolean fromCost
			) {

		if (!this.isAlive()) {
			return 0f;
		}
		boolean worked = false;
		Float oldMana = 0f, newMana = 0f;
		while (!worked) {
			oldMana = this.mana.get();
			newMana = value;
			if (newMana > this.manaMax) {
				newMana = manaMax;
			}
			else if (newMana < 0) {
				newMana = 0f;
			}
			worked = this.mana.compareAndSet(oldMana, newMana);
		}
		if (oldMana > newMana && !fromCost) {
			this.cancelOnTakeDamage();
			CombatManager.handleRetaliate(this, attacker);
		}
		return newMana - oldMana;
	}

	public final float setStamina(
			final float value,
			final AbstractCharacter attacker
			) {
		return setStamina(value, attacker, false);
	}

	public final float setStamina(
			final float value,
			final AbstractCharacter attacker,
			final boolean fromCost
			) {

		if (!this.isAlive()) {
			return 0f;
		}
		boolean worked = false;
		Float oldStamina = 0f, newStamina = 0f;
		while (!worked) {
			oldStamina = this.stamina.get();
			newStamina = value;
			if (newStamina > this.staminaMax) {
				newStamina = staminaMax;
			}
			else if (newStamina < 0) {
				newStamina = 0f;
			}
			worked = this.stamina.compareAndSet(oldStamina, newStamina);
		}
		if (oldStamina > newStamina && !fromCost) {
			this.cancelOnTakeDamage();
			CombatManager.handleRetaliate(this, attacker);
		}
		return newStamina - oldStamina;

	}

	public final float getStamina() {
		if (this.getObjectType() == GameObjectType.Mob)
			return this.getStaminaMax();
		return this.stamina.get();
	}

	public final float getMana() {
		if (this.getObjectType() == GameObjectType.Mob)
			return this.getManaMax();
		return this.mana.get();
	}

	public final float getStaminaMax() {
		if (this.getObjectType() == GameObjectType.Mob)
			return 2000;
		return this.staminaMax;
	}

	public final float getManaMax() {
		if (this.getObjectType() == GameObjectType.Mob)
			return 2000;
		return this.manaMax;
	}

	public final PlayerBonuses getBonuses() {
			return this.bonuses;
	}

	public void teleport(final Vector3fImmutable targetLoc) {
		locationLock.writeLock().lock();
		try{
			MovementManager.translocate(this, targetLoc, null);
			MovementManager.sendRWSSMsg(this);
		}catch(Exception e){
			Logger.error(e);
		}finally{
			locationLock.writeLock().unlock();
		}
	}
	
	
	public void teleportToObject(final AbstractWorldObject worldObject) {
		locationLock.writeLock().lock();
		try{
			MovementManager.translocateToObject(this, worldObject);
		}catch(Exception e){
			Logger.error(e);
		}finally{
			locationLock.writeLock().unlock();
		}
	}



	/*
	 * Serializing
	 */
	
	public static void _serializeForClientMsg(AbstractCharacter abstractCharacter,final ByteBufferWriter writer) throws SerializationException {
		AbstractCharacter.__serializeForClientMsg(abstractCharacter,writer);
	}

	public static void __serializeForClientMsg(AbstractCharacter abstractCharacter,final ByteBufferWriter writer) throws SerializationException {
	}


	public static void serializeForClientMsgOtherPlayer(AbstractCharacter abstractCharacter,final ByteBufferWriter writer) throws SerializationException {
	}

	public static void serializeForClientMsgOtherPlayer(AbstractCharacter abstractCharacter,final ByteBufferWriter writer, final boolean asciiLastName) throws SerializationException {
	
		switch (abstractCharacter.getObjectType()){
		case PlayerCharacter:
			PlayerCharacter.serializePlayerForClientMsgOtherPlayer((PlayerCharacter)abstractCharacter, writer, asciiLastName);
			break;
		case Mob:
			StaticMobActions.serializeMobForClientMsgOtherPlayer((Mob)abstractCharacter, writer,asciiLastName);
			break;
		case NPC:
			NPC.serializeNpcForClientMsgOtherPlayer((NPC)abstractCharacter, writer, asciiLastName);
			break;
		}
		
		
		//TODO INPUT SWITCH CASE ON GAME OBJECTS TO CALL SPECIFIC METHODS.
	}

	public static final void serializeForTrack(AbstractCharacter abstractCharacter, final ByteBufferWriter writer, boolean isGroup) {
		writer.putInt(abstractCharacter.getObjectType().ordinal());
		writer.putInt(abstractCharacter.getObjectUUID());

		if (abstractCharacter.getObjectType().equals(GameObjectType.PlayerCharacter)) {
			writer.putString(abstractCharacter.getName());
		}
		else {
			writer.putString(abstractCharacter.getFirstName());
		}
		writer.put(isGroup ? (byte) 1 : (byte) 0);
		if (abstractCharacter.guild != null) {
			Guild.serializeForTrack(abstractCharacter.guild,writer);
		}
		else {
			Guild.serializeErrantForTrack(writer);
		}
	}

	/*
	 * Cancel effects upon actions
	 */
	public final void cancelOnAttack() { // added to one spot

		boolean changed = false;

		for (String s : this.effects.keySet()) {

			Effect eff = this.effects.get(s);
			
			if (eff == null)
				continue;
			if (eff.cancelOnAttack() && eff.cancel()) {
				eff.cancelJob();
				this.effects.remove(s);
				changed = true;
			}
		}

		if (changed) {
			applyBonuses();
		}

		PowersManager.cancelOnAttack(this);
	}

	public final void cancelOnAttackSwing() { // added
		boolean changed = false;
		for (String s : this.effects.keySet()) {
			Effect eff = this.effects.get(s);
			if (eff == null)
				continue;
			if (eff.cancelOnAttackSwing() && eff.cancel()) {
				//System.out.println("canceling on AttackSwing");
				eff.cancelJob();
				this.effects.remove(s);
				changed = true;
			}
		}
		if (changed) {
			applyBonuses();
		}
		PowersManager.cancelOnAttackSwing(this);
	}

	public final void cancelOnCast() {
		boolean changed = false;
		for (String s : this.effects.keySet()) {
			Effect eff = this.effects.get(s);
			
			if (eff == null)
				continue;
			if (eff.cancelOnCast() && eff.cancel()) {

				// Don't cancel the track effect on the character being tracked
				if (eff.getJob() != null && eff.getJob() instanceof TrackJob) {
					if (((TrackJob) eff.getJob()).getSource().getObjectUUID()
							== this.getObjectUUID()) {
						continue;
					}
				}

				//System.out.println("canceling on Cast");
				eff.cancelJob();
				this.effects.remove(s);
				changed = true;
			}
		}
		if (changed) {
			applyBonuses();
		}
		PowersManager.cancelOnCast(this);
	}

	public final void cancelOnSpell() {
		boolean changed = false;
		for (String s : this.effects.keySet()) {
			Effect eff = this.effects.get(s);
			if (eff == null)
				continue;
			if (eff.cancelOnCastSpell() && eff.cancel()) {
				//System.out.println("canceling on CastSpell");
				eff.cancelJob();
				this.effects.remove(s);
				changed = true;
			}
		}
		if (changed) {
			applyBonuses();
		}
		PowersManager.cancelOnSpell(this);
	}

	public final void cancelOnMove() { // added
		boolean changed = false;
		for (String s : this.effects.keySet()) {
			Effect eff = this.effects.get(s);
			if (eff == null)
				continue;
			if (eff.cancelOnMove() && eff.cancel()) {
				//System.out.println("canceling on Move");
				eff.cancelJob();
				this.effects.remove(s);
				changed = true;
			}
		}
		if (changed) {
			applyBonuses();
		}
		PowersManager.cancelOnMove(this);
	}

	public final void cancelOnSit() { // added
		boolean changed = false;
		for (String s : this.effects.keySet()) {
			Effect eff = this.effects.get(s);
			if (eff == null)
				continue;
			if (eff.cancelOnSit() && eff.cancel()) {
				//System.out.println("canceling on Sit");
				eff.cancelJob();
				this.effects.remove(s);
				changed = true;
			}
		}
		if (changed) {
			applyBonuses();
		}
		PowersManager.cancelOnSit(this);
	}

	public final void cancelOnTakeDamage() {
		boolean changed = false;
		for (String s : this.effects.keySet()) {
			Effect eff = this.effects.get(s);
			if (eff == null)
				continue;
			if (eff.cancelOnTakeDamage() && eff.cancel()) {
				//System.out.println("canceling on Take Damage");
				eff.cancelJob();
				this.effects.remove(s);
				changed = true;
			}
		}
		if (changed) {
			applyBonuses();
		}
		PowersManager.cancelOnTakeDamage(this);
	}

	public final void cancelOnTakeDamage(final DamageType type, final float amount) {
		boolean changed = false;
		for (String s : this.effects.keySet()) {
			Effect eff = this.effects.get(s);
			if (eff == null)
				continue;
			if (eff.cancelOnTakeDamage(type, amount) && eff.cancel()) {
				eff.cancelJob();
				this.effects.remove(s);
				changed = true;
			}
		}
		if (changed) {
			applyBonuses();
		}
	}

	public final Effect getDamageAbsorber() {
		for (String s : this.effects.keySet()) {
			Effect eff = this.effects.get(s);
			if (eff == null)
				continue;
			if (eff.isDamageAbsorber()) {
				return eff;
			}
		}
		return null;
	}

	public final void cancelOnUnEquip() {
		boolean changed = false;
		for (String s : this.effects.keySet()) {
			Effect eff = this.effects.get(s);
			if (eff == null)
				continue;
			if (eff.cancelOnUnEquip() && eff.cancel()) {
				//System.out.println("canceling on UnEquip");
				eff.cancelJob();
				this.effects.remove(s);
				changed = true;
			}
		}
		if (changed) {
			applyBonuses();
		}
		PowersManager.cancelOnUnEquip(this);
	}

	public final void cancelOnStun() {
		boolean changed = false;
		for (String s : this.effects.keySet()) {
			Effect eff = this.effects.get(s);
			
			if (eff == null){
				Logger.error("null effect for " + this.getObjectUUID() + " : effect " + s);
				continue;
			}
			if (eff.cancelOnStun() && eff.cancel()) {
				//System.out.println("canceling on Stun");
				eff.cancelJob();
				this.effects.remove(s);
				changed = true;
			}
		}
		if (changed) {
			applyBonuses();
		}
		PowersManager.cancelOnStun(this);
	}

	//Call to apply any new effects to player
	public synchronized void applyBonuses() {
		PlayerCharacter player;
		//tell the player to applyBonuses because something has changed

		//start running the bonus calculations

		try{
		runBonuses();

		// Check if calculations affected flight.

			if (this.getObjectType().equals(GameObjectType.PlayerCharacter)) {
				player = (PlayerCharacter) this;

				// Ground players who cannot fly but are currently flying

				if (CanFly(player) == false &&
						player.getMovementState().equals(MovementState.FLYING))
					PlayerCharacter.GroundPlayer(player);
			}

		}catch(Exception e){
			Logger.error("Error in run bonuses for object UUID " + this.getObjectUUID());
			Logger.error(e);
		}
	}

	//Don't call this function directly. linked from ac.applyBonuses()
	//through BonusCalcJob. Designed to only run from one worker thread
	public final void runBonuses() {
		// synchronized with getBonuses()
		synchronized (this.bonuses) {
			try {
				//run until no new bonuses are applied

				// clear bonuses and reapply rune bonuses
				if (this.getObjectType().equals(GameObjectType.PlayerCharacter)) {
					this.bonuses.calculateRuneBaseEffects((PlayerCharacter) this);
				}
				else {
					this.bonuses.clearRuneBaseEffects();
				}

				// apply effect bonuses
				for (Effect eff : this.effects.values()) {
					eff.applyBonus(this);
				}

				//apply item bonuses for equipped items
				ConcurrentHashMap<Integer, Item> equip = null;

				if (this.charItemManager != null) {
					equip = this.charItemManager.getEquipped();
				}
				if (equip != null) {
					for (Item item : equip.values()) {
						item.clearBonuses();
						if (item != null) {
							ConcurrentHashMap<String, Effect> effects = item.getEffects();
							if (effects != null) {
								for (Effect eff : effects.values()) {
									eff.applyBonus(item, this);
								}
							}
						}
					}
				}

				//recalculate passive defenses
				if (this.getObjectType().equals(GameObjectType.PlayerCharacter)) {
					((PlayerCharacter) this).setPassives();
				}

			

				// recalculate everything
				if (this.getObjectType().equals(GameObjectType.PlayerCharacter)) {
					PlayerCharacter pc = (PlayerCharacter) this;

					//calculate item bonuses
					pc.calculateItemBonuses();

					//recalculate formulas
					pc.recalculatePlayerStats(true);
					

				}
				else if (this.getObjectType().equals(GameObjectType.Mob)) {
					Mob mob = (Mob) this;

					//recalculate formulas
					StaticMobActions.recalculateStats(mob);
				}
			} catch (Exception e) {
				Logger.error( e);
			}
		}
	}

	public static void runBonusesOnLoad(PlayerCharacter pc) {
		// synchronized with getBonuses()
		synchronized (pc.bonuses) {
		try {
			//run until no new bonuses are applied

			// clear bonuses and reapply rune bonuses
			if (pc.getObjectType() ==  GameObjectType.PlayerCharacter) {
				pc.bonuses.calculateRuneBaseEffects(pc);
			}
			else {
				pc.bonuses.clearRuneBaseEffects();
			}

			// apply effect bonuses
			for (Effect eff : pc.effects.values()) {
				eff.applyBonus(pc);
			}

			//apply item bonuses for equipped items
			ConcurrentHashMap<Integer, Item> equip = null;

			if (pc.charItemManager != null)
				equip = pc.charItemManager.getEquipped();

			if (equip != null) {
				for (Item item : equip.values()) {
					item.clearBonuses();
					if (item != null) {
						ConcurrentHashMap<String, Effect> effects = item.getEffects();
						if (effects != null) {
							for (Effect eff : effects.values()) {
								eff.applyBonus(item, pc);
							}
						}
					}
				}
			}

			//recalculate passive defenses
			pc.setPassives();

			//flip the active bonus set for synchronization purposes
			//do this after all bonus updates, but before recalculations.
			// recalculate everything
			//calculate item bonuses
			pc.calculateItemBonuses();

			//recalculate formulas
			PlayerCharacter.recalculatePlayerStatsOnLoad(pc);

		} catch (Exception e) {
			Logger.error( e);
		}
		
		}
		// TODO remove later, for debugging.
		//this.bonuses.printBonuses();

	}

	public int getInBuildingID() {
		return inBuildingID;
	}

	public void setInBuildingID(int inBuildingID) {
		this.inBuildingID = inBuildingID;
	}

	public float getHateValue() {
		if (this.hateValue <= 0) {
			this.hateValue = 0;
			return hateValue;
		}

		if (this.lastHateUpdate == 0) {
			this.lastHateUpdate = System.currentTimeMillis();
			return this.hateValue;
		}
		long duration = System.currentTimeMillis() - this.lastHateUpdate;
		//convert duration to seconds and multiply Hate Delimiter.
		float modAmount = duration / 1000 * MBServerStatics.PLAYER_HATE_DELIMITER;
		this.hateValue -= modAmount;
		this.lastHateUpdate = System.currentTimeMillis();
		return this.hateValue;
	}

	public void setHateValue(float hateValue) {
		this.lastHateUpdate = System.currentTimeMillis();
		this.hateValue = hateValue;
	}

	public int getInFloorID() {
		return inFloorID;
	}

	public void setInFloorID(int inFloorID) {
		this.inFloorID = inFloorID;
	}

	public boolean isCollided() {
		return collided;
	}

	public void setCollided(boolean collided) {
		this.collided = collided;
	}
	
	
	public static void SetBuildingLevelRoom(AbstractCharacter character, int buildingID, int buildingLevel, int room, Regions region){
		character.inBuildingID = buildingID;
		character.inBuilding = buildingLevel;
		character.inFloorID = room;
		character.lastRegion = region;
	}

	public static Regions InsideBuildingRegion(AbstractCharacter player){

		Regions currentRegion = null;
		HashSet<AbstractWorldObject> buildings = WorldGrid.getObjectsInRangePartial(player, 300, MBServerStatics.MASK_BUILDING);

		for (AbstractWorldObject awo: buildings){

			Building building = (Building)awo;

			if (building.getBounds() == null)
				continue;

			if (building.getBounds().getRegions() == null)
				continue;

			for (Regions region : building.getBounds().getRegions()){
				//TODO ADD NEW REGION CODE
			}
		}
		return currentRegion;
	}

	public static Regions InsideBuildingRegionGoingDown(AbstractCharacter player){

		HashSet<AbstractWorldObject> buildings = WorldGrid.getObjectsInRangePartial(player, 1000, MBServerStatics.MASK_BUILDING);

		Regions tempRegion = null;
		for (AbstractWorldObject awo: buildings){

			Building building = (Building)awo;
			if (building.getBounds() == null)
				continue;
			
			if (!Bounds.collide(player.getLoc(), building.getBounds()))
				continue;

			for (Regions region : building.getBounds().getRegions()){
				
				if (!region.isPointInPolygon(player.getLoc()))
					continue;
				
				if (!region.isOutside())
					continue;
				if (tempRegion == null)
					tempRegion = region;
				
				if (tempRegion.highLerp.y < region.highLerp.y)
					tempRegion = region;
			}
			
			if (tempRegion != null)
				break;
		}
		return tempRegion;
	}

	public float getDesiredAltitude() {
		return desiredAltitude;
	}

	public void setDesiredAltitude(float desiredAltitude) {
		this.desiredAltitude = desiredAltitude;
	}

	
	public long getTakeOffTime() {
		return takeOffTime;
	}

	public void setTakeOffTime(long takeOffTime) {
		this.takeOffTime = takeOffTime;
	}
	
	public static boolean CanFly(AbstractCharacter flyer){
		boolean canFly = false;
		PlayerBonuses bonus = flyer.getBonuses();

		if (bonus != null && !bonus.getBool(ModType.NoMod, SourceType.Fly) && bonus.getBool(ModType.Fly,SourceType.None) && flyer.isAlive())
			canFly = true;
		
		return canFly;
		
	}

	public boolean isItemCasting() {
		return itemCasting;
	}

	public void setItemCasting(boolean itemCasting) {
		this.itemCasting = itemCasting;
	}
	
	public static void MoveInsideBuilding(PlayerCharacter source, AbstractCharacter ac){
		MoveToPointMsg moveMsg = new MoveToPointMsg();
		moveMsg.setPlayer(ac);
        moveMsg.setTarget(ac, BuildingManager.getBuildingFromCache(ac.inBuildingID));
		
		Dispatch dispatch = Dispatch.borrow(source, moveMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
		
	}
	
	//updates
	public void update(){
	}
	public void updateRegen(){
	}
	public void updateMovementState(){
	}
	public void updateLocation(){
	}
	public void updateFlight(){
	}
	
	
	public void dynamicUpdate(UpdateType updateType){
	if (this.updateLock.writeLock().tryLock()){
		try{
			switch(updateType){
			case ALL:
				update();
				break;
			case REGEN:
				updateRegen();
				break;
			case LOCATION:
				update();
				break;
			case MOVEMENTSTATE:
				update();
				break;
			case FLIGHT:
				updateFlight();
				break;
			}
			
		}catch(Exception e){
			Logger.error(e);
		}finally{
			this.updateLock.writeLock().unlock();
		}
	}
	
	}

	public Regions getLastRegion() {
		return lastRegion;
	}

	public boolean isMovingUp() {
		return movingUp;
	}

	public void setMovingUp(boolean movingUp) {
		this.movingUp = movingUp;
	}	
	public static void UpdateRegion(AbstractCharacter worldObject){
			worldObject.region = AbstractWorldObject.GetRegionByWorldObject(worldObject);
	}
	
	public static void teleport(AbstractCharacter worldObject, final Vector3fImmutable targetLoc) {
		worldObject.locationLock.writeLock().lock();
		try{
			MovementManager.translocate(worldObject, targetLoc,null);
			if (worldObject.getObjectType().equals(GameObjectType.PlayerCharacter))
			InterestManager.INTERESTMANAGER.HandleLoadForTeleport((PlayerCharacter)worldObject);
		}catch(Exception e){
			Logger.error(e);
		}finally{
			worldObject.locationLock.writeLock().unlock();
		}
	}
}
