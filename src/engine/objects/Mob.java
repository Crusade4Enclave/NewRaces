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
import engine.InterestManagement.WorldGrid;
import engine.ai.MobileFSM;
import engine.ai.MobileFSM.STATE;
import engine.exception.SerializationException;
import engine.gameManager.*;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.DeferredPowerJob;
import engine.jobs.UpgradeNPCJob;
import engine.math.Bounds;
import engine.math.Vector3fImmutable;
import engine.net.ByteBufferWriter;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.ManageCityAssetsMsg;
import engine.net.client.msg.PetMsg;
import engine.net.client.msg.PlaceAssetMsg;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;
import engine.server.world.WorldServer;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static engine.net.client.msg.ErrorPopupMsg.sendErrorPopup;

public class Mob extends AbstractIntelligenceAgent {

	protected int dbID; //the database ID
	protected int loadID;
	protected boolean isMob;
	protected MobBase mobBase;

	//mob specific

	protected float spawnRadius;
	protected int spawnTime;

	//used by static mobs
	protected int parentZoneID;
	protected Zone parentZone;
	protected float statLat;
	protected float statLon;
	protected float statAlt;
	protected Building building;
	protected Contract contract;
	private static ReentrantReadWriteLock createLock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	// Variables NOT to be stored in db
	private static int staticID = 0;
	private int currentID;
	private int ownerUID = 0; //only used by pets
	private boolean hasLoot = false;
	private static ConcurrentHashMap<Integer, Mob> mobMapByDBID = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private AbstractWorldObject fearedObject = null;
	private int buildingID;
	private boolean isSiege = false;
	private boolean isPlayerGuard = false;
	private long timeToSpawnSiege;
	private AbstractCharacter npcOwner;
	private Vector3fImmutable inBuildingLoc = null;
	private final ConcurrentHashMap<Integer, Boolean> playerAgroMap = new ConcurrentHashMap<>();
	private boolean noAggro = false;
	private STATE state = STATE.Disabled;
	private int aggroTargetID = 0;
	private boolean walkingHome = true;
	private long lastAttackTime = 0;
	private long deathTime = 0;
	private ConcurrentHashMap<Mob, Integer> siegeMinionMap = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	public ReentrantReadWriteLock minionLock = new ReentrantReadWriteLock();
	private int patrolPointIndex = 0;
	private int lastMobPowerToken = 0;
	private  HashMap<Integer, MobEquipment> equip = null;
	private String nameOverride = "";
	private Regions lastRegion = null;
	private long despawnTime = 0;
	private DeferredPowerJob weaponPower;
	private DateTime upgradeDateTime = null;
	private boolean lootSync = false;
	private int fidalityID = 0;
	private int equipmentSetID = 0;
	private int lootSet = 0;
	private boolean isGuard;
	private ArrayList<Integer> fidelityRunes = null;
	
	public boolean despawned = false;
	public Vector3fImmutable destination = Vector3fImmutable.ZERO;
	public Vector3fImmutable localLoc = Vector3fImmutable.ZERO;
	public HashMap<Integer,Integer> mobPowers;
	/**
	 * No Id Constructor
	 */
	public Mob(String firstName, String lastName, short statStrCurrent, short statDexCurrent, short statConCurrent,
			short statIntCurrent, short statSpiCurrent, short level, int exp, boolean sit, boolean walk, boolean combat, Vector3fImmutable bindLoc,
			Vector3fImmutable currentLoc, Vector3fImmutable faceDir, short healthCurrent, short manaCurrent, short stamCurrent, Guild guild,
			byte runningTrains, int npcType, boolean isMob, Zone parent,Building building, int contractID) {
		super( firstName, lastName, statStrCurrent, statDexCurrent, statConCurrent, statIntCurrent, statSpiCurrent, level, exp, sit,
				walk, combat, bindLoc, currentLoc, faceDir, healthCurrent, manaCurrent, stamCurrent, guild, runningTrains);
		
		this.dbID = MBServerStatics.NO_DB_ROW_ASSIGNED_YET;
		this.state = STATE.Idle;
		this.loadID = npcType;
		this.isMob = isMob;
		this.mobBase = MobBase.getMobBase(loadID);
		this.currentID = MBServerStatics.NO_DB_ROW_ASSIGNED_YET;
		this.parentZone = parent;
		this.parentZoneID = (parent != null) ? parent.getObjectUUID() : 0;
		this.building = building;

		if (building != null)
			this.buildingID = building.getObjectUUID();
		else this.buildingID = 0;

		if (contractID == 0)
			this.contract = null;
		else
			this.contract = DbManager.ContractQueries.GET_CONTRACT(contractID);

		if (this.contract != null)
			this.level = 10;

		//initializeMob(false, false);
		clearStatic();
	}

	/**
	 * Normal Constructor
	 */
	public Mob(String firstName, String lastName, short statStrCurrent, short statDexCurrent, short statConCurrent,
			short statIntCurrent, short statSpiCurrent, short level, int exp, boolean sit, boolean walk, boolean combat, Vector3fImmutable bindLoc,
			Vector3fImmutable currentLoc, Vector3fImmutable faceDir, short healthCurrent, short manaCurrent, short stamCurrent, Guild guild,
			byte runningTrains, int npcType, boolean isMob, Zone parent, int newUUID, Building building, int contractID) {
		super( firstName, lastName, statStrCurrent, statDexCurrent, statConCurrent, statIntCurrent, statSpiCurrent, level, exp, sit,
				walk, combat, bindLoc, currentLoc, faceDir, healthCurrent, manaCurrent, stamCurrent, guild, runningTrains, newUUID);
		this.state = STATE.Idle;
		this.dbID = newUUID;
		this.loadID = npcType;
		this.isMob = isMob;

		if (contractID == 0)
			this.contract = null;
		else
			this.contract = DbManager.ContractQueries.GET_CONTRACT(contractID);

		this.mobBase = MobBase.getMobBase(loadID);
		this.parentZone = parent;
		this.parentZoneID = (parent != null) ? parent.getObjectUUID() : 0;
		this.building = building;
		initializeMob(false,false,false);
		clearStatic();
	}

	/**
	 * Pet Constructor
	 */
	public Mob( MobBase mobBase, Guild guild, Zone parent, short level, PlayerCharacter owner, int tableID) {
		super(mobBase.getFirstName(), "", (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, level, 0, false, true, false, owner.getLoc(), owner.getLoc(), owner.getFaceDir(), (short) mobBase.getHealthMax(), (short) 0, (short) 0, guild, (byte) 0, tableID);
		this.state = STATE.Idle;
		this.dbID = tableID;
		this.loadID = mobBase.getObjectUUID();
		this.isMob = true;
		this.mobBase = mobBase;
		this.parentZone = parent;
		this.parentZoneID = (parent != null) ? parent.getObjectUUID() : 0;
		this.ownerUID = owner.getObjectUUID();
		initializeMob(true,false,false);
		clearStatic();
	}
	//SIEGE CONSTRUCTOR
	public Mob( MobBase mobBase, Guild guild, Zone parent, short level, Vector3fImmutable loc, int tableID,boolean isPlayerGuard) {
		super( mobBase.getFirstName(), "", (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, level, 0, false, true, false, loc, loc, Vector3fImmutable.ZERO, (short) mobBase.getHealthMax(), (short) 0, (short) 0, guild, (byte) 0, tableID);
		this.dbID = tableID;
		this.loadID = mobBase.getObjectUUID();
		this.isMob = true;
		this.mobBase = mobBase;
		this.parentZone = parent;
		this.parentZoneID = (parent != null) ? parent.getObjectUUID() : 0;
		this.ownerUID = 0;
		this.equip = new HashMap<>();
		initializeMob(false,true, isPlayerGuard);
		clearStatic();
	}

	/**
	 * ResultSet Constructor
	 */
	public Mob(ResultSet rs) throws SQLException {

		super(rs);

		try{
			this.dbID = rs.getInt(1);
			this.state = STATE.Idle;
			this.loadID = rs.getInt("mob_mobbaseID");
			this.gridObjectType = GridObjectType.DYNAMIC;
			this.spawnRadius = rs.getFloat("mob_spawnRadius");
			this.spawnTime = rs.getInt("mob_spawnTime");
			this.isMob = true;
			this.parentZone = null;
			this.statLat = rs.getFloat("mob_spawnX");
			this.statAlt = rs.getFloat("mob_spawnY");
			this.statLon = rs.getFloat("mob_spawnZ");
			
			this.localLoc = new Vector3fImmutable(this.statLat,this.statAlt,this.statLon);
			
			this.parentZoneID = rs.getInt("parent");
			this.level = (short) rs.getInt("mob_level");
			int buildingID = rs.getInt("mob_buildingID");

			try {
				this.building = BuildingManager.getBuilding(buildingID);
			}catch(Exception e){
				this.building = null;
				Logger.error(e.getMessage());
			}

			int contractID = rs.getInt("mob_contractID");

			if (contractID == 0)
				this.contract = null;
			else
				this.contract = DbManager.ContractQueries.GET_CONTRACT(contractID);

			if (this.contract != null)
				if (NPC.ISGuardCaptain(contract.getContractID())){
					this.spawnTime = 60*15;
					this.isPlayerGuard = true;
					this.nameOverride = contract.getName();
				}

			int guildID = rs.getInt("mob_guildUID");
		

			if (this.fidalityID != 0){
				if (this.building != null)
					this.guild = this.building.getGuild();
				else
					this.guild = Guild.getGuild(guildID);
			}else
				if (this.building != null)
					this.guild = this.building.getGuild();
				else
					this.guild = Guild.getGuild(guildID);
			
			if (this.guild == null)
				this.guild = Guild.getErrantGuild();

			java.util.Date sqlDateTime;
			sqlDateTime = rs.getTimestamp("upgradeDate");

			if (sqlDateTime != null)
				upgradeDateTime = new DateTime(sqlDateTime);
			else
				upgradeDateTime = null;

			// Submit upgrade job if NPC is currently set to rank.

			if (this.upgradeDateTime != null)
				Mob.submitUpgradeJob(this);

			this.mobBase = MobBase.getMobBase(loadID);
			
			this.setObjectTypeMask(MBServerStatics.MASK_MOB | this.getTypeMasks());

			if (this.mobBase != null && this.spawnTime == 0)
				this.spawnTime = this.mobBase.getSpawnTime();
			
			this.bindLoc = new Vector3fImmutable(this.statLat, this.statAlt,this.statLon);

			this.setParentZone(ZoneManager.getZoneByUUID(this.parentZoneID));
			

			this.fidalityID = rs.getInt("fidalityID");

			this.equipmentSetID = rs.getInt("equipmentSet");
			
			if (this.contract != null)
				this.equipmentSetID = this.contract.getEquipmentSet();

			this.lootSet = (rs.getInt("lootSet"));

			if (this.fidalityID != 0)
				this.nameOverride = rs.getString("mob_name");

			if (this.fidalityID != 0){

				Zone parentZone = ZoneManager.getZoneByUUID(this.parentZoneID);
				if (parentZone != null){
					this.fidelityRunes = WorldServer.ZoneFidelityMobRunes.get(parentZone.getLoadNum()).get(this.fidalityID);

					if (this.fidelityRunes != null)
						for (Integer runeID : this.fidelityRunes){
							if (runeID == 252623 ){
								this.isGuard = true;
								this.noAggro = true;
							}
						}
				}
			}
		} catch(Exception e){
			Logger.error( currentID + "");
		}

		try{
			initializeMob(false,false,this.isPlayerGuard);
		} catch(Exception e){
			Logger.error(e);
		}

	}

	private void clearStatic() {

		if (this.parentZone != null)
			this.parentZone.zoneMobSet.remove(this);

		this.parentZone = null;
		this.statLat = 0f;
		this.statLon = 0f;
		this.statAlt = 0f;
	}

	private void initializeMob(boolean isPet, boolean isSiege, boolean isGuard) {

		if (this.mobBase != null) {
			
			this.gridObjectType = GridObjectType.DYNAMIC;
			this.healthMax = this.mobBase.getHealthMax();
			this.manaMax = 0;
			this.staminaMax = 0;
			this.setHealth(this.healthMax);
			this.mana.set(this.manaMax);
			this.stamina.set(this.staminaMax);

			if(!this.nameOverride.isEmpty())
				this.firstName = this.nameOverride;
			else
				this.firstName = this.mobBase.getFirstName();
			if (isPet) {
				this.setObjectTypeMask(MBServerStatics.MASK_PET | this.getTypeMasks());
				if (ConfigManager.serverType.equals(ServerType.LOGINSERVER))
				this.setLoc(this.getLoc());
				mobPowers = DbManager.MobBaseQueries.LOAD_STATIC_POWERS(this.getMobBaseID());
			}
			if (!isPet && this.contract == null) {
				this.level = (short) this.mobBase.getLevel();
			}

		} else
			this.level = 1;

		//add this npc to building
		if (this.building != null && this.loadID != 0 && this.fidalityID == 0) {

			int maxSlots;
			maxSlots = building.getBlueprint().getSlotsForRank(this.building.getRank());

			for (int slot = 1; slot < maxSlots + 1; slot++) {
				if (!this.building.getHirelings().containsValue(slot)) {
					this.building.getHirelings().put(this, slot);
					break;
				}
			}
		}

		//set bonuses
		this.bonuses = new PlayerBonuses(this);

		//TODO set these correctly later
		this.rangeHandOne = 8;
		this.rangeHandTwo = -1;
			this.minDamageHandOne = 0;
			this.maxDamageHandOne = 0;
			this.minDamageHandTwo = 1;
			this.maxDamageHandTwo = 4;
		this.atrHandOne = 300;
		this.atrHandOne = 300;
		this.defenseRating = (short) this.mobBase.getDefenseRating();
		this.isActive = true;

		this.charItemManager.load();

		//load AI for general mobs.

		if (isPet || isSiege || (isGuard && this.contract == null))
			this.currentID =  (--Mob.staticID);
		 else
			this.currentID = this.dbID;

		if (!isPet && !isSiege && !this.isPlayerGuard)
			loadInventory();

		//store mobs by Database ID

		if (!isPet && !isSiege)
			Mob.mobMapByDBID.put(this.dbID, this);
	}

	private void initializeSkills() {

		if (this.mobBase.getMobBaseStats() == null)
			return;

		long skillVector = this.mobBase.getMobBaseStats().getSkillSet();
		int skillValue = this.mobBase.getMobBaseStats().getSkillValue();

		if (this.mobBase.getObjectUUID() >= 17233) {
			for (CharacterSkills cs : CharacterSkills.values()) {
				SkillsBase sb = DbManager.SkillsBaseQueries.GET_BASE_BY_TOKEN(cs.getToken());
				CharacterSkill css = new CharacterSkill(sb, this, 50);
				this.skills.put(sb.getName(), css);
			}
		} else {
			for (CharacterSkills cs : CharacterSkills.values()) {
				if ((skillVector & cs.getFlag()) != 0) {
					SkillsBase sb = DbManager.SkillsBaseQueries.GET_BASE_BY_TOKEN(cs.getToken());
					CharacterSkill css = new CharacterSkill(sb, this, skillValue);
					this.skills.put(sb.getName(), css);
				}
			}
		}
	}

	private void initializeStaticEffects() {

		EffectsBase eb = null;
		for (MobBaseEffects mbe : this.mobBase.getRaceEffectsList()) {

			eb = PowersManager.getEffectByToken(mbe.getToken());

			if (eb == null) {
				Logger.info( "EffectsBase Null for Token " + mbe.getToken());
				continue;
			}

			//check to upgrade effects if needed.
			if (this.effects.containsKey(Integer.toString(eb.getUUID()))) {
				if (mbe.getReqLvl() > (int) this.level)
					continue;

				Effect eff = this.effects.get(Integer.toString(eb.getUUID()));

				if (eff == null)
					continue;

				if (eff.getTrains() > mbe.getRank())
					continue;

				//new effect is of a higher rank. remove old effect and apply new one.
				eff.cancelJob();
				this.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
			} else {
				if (mbe.getReqLvl() > (int) this.level)
					continue;

				this.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
			}
		}

		//Apply all rune effects.
		// Only Captains have contracts
		if (contract != null || this.isPlayerGuard){
			RuneBase guardRune = RuneBase.getRuneBase(252621);
			for (MobBaseEffects mbe : guardRune.getEffectsList()) {

				eb = PowersManager.getEffectByToken(mbe.getToken());

				if (eb == null) {
					Logger.info( "EffectsBase Null for Token " + mbe.getToken());
					continue;
				}

				//check to upgrade effects if needed.
				if (this.effects.containsKey(Integer.toString(eb.getUUID()))) {

					if (mbe.getReqLvl() > (int) this.level)
						continue;

					Effect eff = this.effects.get(Integer.toString(eb.getUUID()));

					if (eff == null)
						continue;

					//Current effect is a higher rank, dont apply.
					if (eff.getTrains() > mbe.getRank())
						continue;

					//new effect is of a higher rank. remove old effect and apply new one.
					eff.cancelJob();
					this.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
				} else {

					if (mbe.getReqLvl() > (int) this.level)
						continue;

					this.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
				}
			}

			RuneBase WarriorRune = RuneBase.getRuneBase(2518);
			for (MobBaseEffects mbe : WarriorRune.getEffectsList()) {

				eb = PowersManager.getEffectByToken(mbe.getToken());

				if (eb == null) {
					Logger.info( "EffectsBase Null for Token " + mbe.getToken());
					continue;
				}

				//check to upgrade effects if needed.
				if (this.effects.containsKey(Integer.toString(eb.getUUID()))) {

					if (mbe.getReqLvl() > (int) this.level)
						continue;

					Effect eff = this.effects.get(Integer.toString(eb.getUUID()));

					if (eff == null)
						continue;

					//Current effect is a higher rank, dont apply.
					if (eff.getTrains() > mbe.getRank())
						continue;

					//new effect is of a higher rank. remove old effect and apply new one.
					eff.cancelJob();
					this.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
				} else {

					if (mbe.getReqLvl() > (int) this.level)
						continue;

					this.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
				}
			}
		}

		if (this.fidelityRunes != null){

			for (int fidelityRune : this.fidelityRunes) {

				RuneBase rune = RuneBase.getRuneBase(fidelityRune);

				if (rune != null)
					for (MobBaseEffects mbe : rune.getEffectsList()) {

						eb = PowersManager.getEffectByToken(mbe.getToken());
						if (eb == null) {
							Logger.info("EffectsBase Null for Token " + mbe.getToken());
							continue;
						}

						//check to upgrade effects if needed.
						if (this.effects.containsKey(Integer.toString(eb.getUUID()))) {
							if (mbe.getReqLvl() > (int) this.level)
								continue;

							Effect eff = this.effects.get(Integer.toString(eb.getUUID()));

							if (eff == null)
								continue;

							//Current effect is a higher rank, dont apply.
							if (eff.getTrains() > mbe.getRank())
								continue;

							//new effect is of a higher rank. remove old effect and apply new one.
							eff.cancelJob();
							this.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);

						} else {

							if (mbe.getReqLvl() > (int) this.level)
								continue;

							this.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
						}
					}
			}
		}else
			for (RuneBase rune : this.mobBase.getRunes()) {
				for (MobBaseEffects mbe : rune.getEffectsList()) {

					eb = PowersManager.getEffectByToken(mbe.getToken());
					if (eb == null) {
						Logger.info( "EffectsBase Null for Token " + mbe.getToken());
						continue;
					}

					//check to upgrade effects if needed.
					if (this.effects.containsKey(Integer.toString(eb.getUUID()))) {
						if (mbe.getReqLvl() > (int) this.level)
							continue;

						Effect eff = this.effects.get(Integer.toString(eb.getUUID()));

						if (eff == null)
							continue;

						//Current effect is a higher rank, dont apply.
						if (eff.getTrains() > mbe.getRank())
							continue;

						//new effect is of a higher rank. remove old effect and apply new one.
						eff.cancelJob();
						this.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
					} else {

						if (mbe.getReqLvl() > (int) this.level)
							continue;

						this.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
					}
				}
			}
	}

	/*
	 * Getters
	 */
	@Override
	public int getDBID() {
		return this.dbID;
	}

	public int getLoadID() {
		return loadID;
	}

	@Override
	public int getObjectUUID() {
		return currentID;
	}

	public float getSpawnX() {
		return this.statLat;
	}

	public float getSpawnY() {
		return this.statAlt;
	}

	public float getSpawnZ() {
		return this.statLon;
	}

	public float getSpawnRadius() {
		return this.spawnRadius;
	}

	public int getSpawnTime() {

		if (this.spawnTime == 0)
			return MBServerStatics.RESPAWN_TIMER;
		 else
			return this.spawnTime * 1000;
	}

	//use getSpawnTime instead. This is just for init tables
	public int getTrueSpawnTime() {
		return this.spawnTime;
	}

	public String getSpawnTimeAsString() {
		if (this.spawnTime == 0)
			return MBServerStatics.DEFAULT_SPAWN_TIME_MS / 1000 + " seconds (Default)";
		 else
			return this.spawnTime + " seconds";

	}


	public void setSpawnTime(int value) {
		this.spawnTime = value;
	}

	@Override
	public MobBase getMobBase() {
		return this.mobBase;
	}

	public int getMobBaseID() {

		if (this.mobBase != null)
			return this.mobBase.getObjectUUID();

		return 0;
	}

	public Vector3fImmutable getTrueBindLoc() {
		return this.bindLoc;
	}

	public Zone getParentZone() {
		return this.parentZone;
	}

	public int getParentZoneID() {

		if (this.parentZone != null)
			return this.parentZone.getObjectUUID();

		return 0;
	}

	@Override
	public int getGuildUUID() {

		if (this.guild == null)
			return 0;

		return this.guild.getObjectUUID();
	}

	@Override
	public PlayerCharacter getOwner() {

		if (!this.isPet())
			return null;

		if (this.ownerUID == 0)
			return null;

		return PlayerCharacter.getFromCache(this.ownerUID);
	}

	public void setOwner(PlayerCharacter value) {

		if (value == null)
			this.ownerUID = 0;
		else
			this.ownerUID = value.getObjectUUID();
	}

	@Override
	public AbstractWorldObject getFearedObject() {
		return this.fearedObject;
	}

	public void setFearedObject(AbstractWorldObject awo) {
		this.fearedObject = awo;
	}

	public void setParentZone(Zone zone) {

		if (this.parentZone == null){
			zone.zoneMobSet.add(this);
			this.parentZone = zone;
		}
		
		this.bindLoc = Mob.GetSpawnRadiusLocation(this);
		this.lastBindLoc = bindLoc;
		this.setLoc(bindLoc);
		this.stopMovement(bindLoc);
	}

	@Override
	public Vector3fImmutable getBindLoc() {

		if(this.isPet() && !this.isSiege)
			return this.getOwner() != null? this.getOwner().getLoc() : this.getLoc();
			return this.bindLoc;
	}

	/*
	 * Serialization
	 */
	
	public static void __serializeForClientMsg(Mob mob,ByteBufferWriter writer) throws SerializationException {
	}


	
	public static void serializeMobForClientMsgOtherPlayer(Mob mob,ByteBufferWriter writer, boolean hideAsciiLastName)
			throws SerializationException {
		Mob.serializeForClientMsgOtherPlayer(mob,writer);
	}

	
	public static void serializeForClientMsgOtherPlayer(Mob mob, ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(0);
		writer.putInt(0);

		int tid = (mob.mobBase != null) ? mob.mobBase.getLoadID() : 0;
		int classID = MobBase.GetClassType(mob.mobBase.getObjectUUID());
		if (mob.isPet()) {
			writer.putInt(2);
			writer.putInt(3);
			writer.putInt(0);
			writer.putInt(2522);
			writer.putInt(GameObjectType.NPCClassRune.ordinal());
			writer.putInt(mob.currentID);
		} else if (tid == 100570) { //kur'adar
			writer.putInt(3);
			Mob.serializeRune(mob,writer, 3, GameObjectType.NPCClassRuneTwo.ordinal(), 2518); //warrior class
			serializeRune(mob,writer, 5, GameObjectType.NPCClassRuneThree.ordinal(), 252621); //guard rune
		} else if (tid == 100962 || tid == 100965) { //Spydraxxx the Mighty, Denigo Tantric
			writer.putInt(2);
			serializeRune(mob,writer, 5, GameObjectType.NPCClassRuneTwo.ordinal(), 252621); //guard rune
		}else if (mob.contract != null || mob.isPlayerGuard){
			writer.putInt(3);
			serializeRune(mob,writer, 3, GameObjectType.NPCClassRuneTwo.ordinal(),MobBase.GetClassType(mob.getMobBaseID())); //warrior class
			serializeRune(mob,writer, 5, GameObjectType.NPCClassRuneThree.ordinal(), 252621); //guard rune
		}else {

			writer.putInt(1);
		}

		//Generate Race Rune
		writer.putInt(1);
		writer.putInt(0);

		if (mob.mobBase != null)
			writer.putInt(mob.mobBase.getLoadID());
		 else
			writer.putInt(mob.loadID);

		writer.putInt(mob.getObjectType().ordinal());
		writer.putInt(mob.currentID);

		//Send Stats
		writer.putInt(5);
		writer.putInt(0x8AC3C0E6); //Str
		writer.putInt(0);
		writer.putInt(0xACB82E33); //Dex
		writer.putInt(0);
		writer.putInt(0xB15DC77E); //Con
		writer.putInt(0);
		writer.putInt(0xE07B3336); //Int
		writer.putInt(0);
		writer.putInt(0xFF665EC3); //Spi
		writer.putInt(0);

		if (!mob.nameOverride.isEmpty()){
			writer.putString(mob.nameOverride);
			writer.putInt(0);
		} else {
			writer.putString(mob.firstName);
			writer.putString(mob.lastName);

		}


		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);

		writer.put((byte) 0);
		writer.putInt(mob.getObjectType().ordinal());
		writer.putInt(mob.currentID);

		if (mob.mobBase != null) {
			writer.putFloat(mob.mobBase.getScale());
			writer.putFloat(mob.mobBase.getScale());
			writer.putFloat(mob.mobBase.getScale());
		} else {
			writer.putFloat(1.0f);
			writer.putFloat(1.0f);
			writer.putFloat(1.0f);
		}

		//Believe this is spawn loc, ignore for now
		writer.putVector3f(mob.getLoc());

		//Rotation
		writer.putFloat(mob.getRot().y);

		//Inventory Stuff
		writer.putInt(0);

		// get a copy of the equipped items.


		if (mob.equip != null){

			writer.putInt(mob.equip.size());
			
			for (MobEquipment me:mob.equip.values()){
				MobEquipment.serializeForClientMsg(me,writer);
			}
		 }else{
			writer.putInt(0);
		}

		writer.putInt(mob.getRank());
		writer.putInt(mob.getLevel());
		writer.putInt(mob.getIsSittingAsInt()); //Standing
		writer.putInt(mob.getIsWalkingAsInt()); //Walking
		writer.putInt(mob.getIsCombatAsInt()); //Combat
		writer.putInt(2); //Unknown
		writer.putInt(1); //Unknown - Headlights?
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.put((byte) 0);
		writer.put((byte) 0);
		writer.put((byte) 0);
		writer.putInt(0);

		if (mob.contract != null && mob.npcOwner == null){
			writer.put((byte) 1);
			writer.putLong(0);
			writer.putLong(0);

			if (mob.contract != null)
				writer.putInt(mob.contract.getIconID());
			 else
				writer.putInt(0); //npc icon ID

		} else
			writer.put((byte)0);


		if (mob.npcOwner != null){
			writer.put((byte) 1);
			writer.putInt(GameObjectType.PlayerCharacter.ordinal());
			writer.putInt(131117009);
			writer.putInt(mob.npcOwner.getObjectType().ordinal());
			writer.putInt(mob.npcOwner.getObjectUUID());
			writer.putInt(8);
		}else
			writer.put((byte)0);

		if (mob.isPet()) {

			writer.put((byte) 1);

			if (mob.getOwner() != null) {
				writer.putInt(mob.getOwner().getObjectType().ordinal());
				writer.putInt(mob.getOwner().getObjectUUID());
			} else {
				writer.putInt(0); //ownerType
				writer.putInt(0); //ownerID
			}
		} else {
			writer.put((byte) 0);
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

		if (!mob.isAlive() && !mob.isPet() && !mob.isNecroPet() && !mob.isSiege && !mob.isPlayerGuard) {
			writer.putInt(0);
			writer.putInt(0);
		}

		writer.put((byte) 0);
		Guild._serializeForClientMsg(mob.getGuild(),writer);
		//		writer.putInt(0);
		//		writer.putInt(0);
		if (mob.mobBase != null && mob.mobBase.getObjectUUID() == 100570) {
			writer.putInt(2);
			writer.putInt(0x00008A2E);
			writer.putInt(0x1AB84003);
		} else if (mob.isSiege) {
			writer.putInt(1);
			writer.putInt(74620179);
		} else
			writer.putInt(0);

		//		writer.putInt(1);
		//		writer.putInt(0); //0xAC13C5E9 - alternate textures
		writer.putInt(0); //0xB8400300
		writer.putInt(0);

		//TODO Guard
		writer.put((byte) 0);
		//		writer.put((byte)0); //Is guard..

		writer.putFloat(mob.healthMax);
		writer.putFloat(mob.health.get());

		//TODO Peace Zone
		writer.put((byte) 1); //0=show tags, 1=don't

		//DON't LOAD EFFECTS FOR DEAD MOBS.

		if (!mob.isAlive())
			writer.putInt(0);
		else{
			int	indexPosition = writer.position();
			writer.putInt(0); //placeholder for item cnt
			int total = 0;

			//	Logger.info("",""+ mob.getEffects().size());
			for (Effect eff : mob.getEffects().values()) {
				if (eff.isStatic())
					continue;
				if ( !eff.serializeForLoad(writer))
					continue;
				++total;
			}

			writer.putIntAt(total, indexPosition);
		}

		//        // Effects
		writer.put((byte) 0);
	}

	private static void serializeRune(Mob mob,ByteBufferWriter writer, int type, int objectType, int runeID) {
		writer.putInt(type);
		writer.putInt(0);
		writer.putInt(runeID);
		writer.putInt(objectType);
		writer.putInt(mob.currentID);
	}


	public void calculateModifiedStats() {

		float strVal = this.mobBase.getMobBaseStats().getBaseStr();
		float dexVal = this.mobBase.getMobBaseStats().getBaseDex();
		float conVal = 0; // I believe this will desync the Mobs Health if we call it.
		float intVal = this.mobBase.getMobBaseStats().getBaseInt();
		float spiVal = this.mobBase.getMobBaseStats().getBaseSpi();

		// TODO modify for equipment
		if (this.bonuses != null) {
			// modify for effects
			strVal += this.bonuses.getFloat(ModType.Attr, SourceType.Strength);
			dexVal += this.bonuses.getFloat(ModType.Attr, SourceType.Dexterity);
			conVal += this.bonuses.getFloat(ModType.Attr, SourceType.Constitution);
			intVal += this.bonuses.getFloat(ModType.Attr, SourceType.Intelligence);
			spiVal += this.bonuses.getFloat(ModType.Attr, SourceType.Spirit);

			// apply dex penalty for armor
			// modify percent amounts. DO THIS LAST!
			strVal *= (1+this.bonuses.getFloatPercentAll(ModType.Attr, SourceType.Strength)); 
			dexVal *= (1+this.bonuses.getFloatPercentAll(ModType.Attr, SourceType.Dexterity)); 
			conVal *= (1+this.bonuses.getFloatPercentAll(ModType.Attr, SourceType.Constitution)); 
			intVal *= (1+this.bonuses.getFloatPercentAll(ModType.Attr, SourceType.Intelligence)); 
			spiVal *= (1+this.bonuses.getFloatPercentAll(ModType.Attr, SourceType.Spirit)); 
		} else {
			// apply dex penalty for armor
		}

		// Set current stats
		this.statStrCurrent = (strVal < 1) ? (short) 1 : (short) strVal;
		this.statDexCurrent = (dexVal < 1) ? (short) 1 : (short) dexVal;
		this.statConCurrent = (conVal < 1) ? (short) 1 : (short) conVal;
		this.statIntCurrent = (intVal < 1) ? (short) 1 : (short) intVal;
		this.statSpiCurrent = (spiVal < 1) ? (short) 1 : (short) spiVal;

	}

	@Override
	public float getSpeed() {
		float bonus = 1;
		if (this.bonuses != null)
			// get rune and effect bonuses
			bonus *= (1 + this.bonuses.getFloatPercentAll(ModType.Speed, SourceType.None));

		if (this.isPlayerGuard){
			switch (this.mobBase.getLoadID()){
			case 2111:
				if (this.isWalk())
					if (this.isCombat())
						return Enum.Guards.HumanArcher.getWalkCombatSpeed() * bonus;
					else return Enum.Guards.HumanArcher.getWalkSpeed() * bonus;
				else
					return Enum.Guards.HumanArcher.getRunSpeed() * bonus;

			case 14103:
				if (this.isWalk())
					if (this.isCombat())
						return Enum.Guards.UndeadArcher.getWalkCombatSpeed() * bonus;
					else return Enum.Guards.UndeadArcher.getWalkSpeed() * bonus;
				else
					return Enum.Guards.UndeadArcher.getRunSpeed() * bonus;
			}
		}
		//return combat speeds
		if (this.isCombat()){
			if (this.isWalk()){
				if (this.mobBase.getWalkCombat() <= 0)
					return MBServerStatics.MOB_SPEED_WALKCOMBAT * bonus;
				return this.mobBase.getWalkCombat() * bonus;
			}else{
				if (this.mobBase.getRunCombat() <= 0)
					return MBServerStatics.MOB_SPEED_RUNCOMBAT * bonus;
				return this.mobBase.getRunCombat() * bonus;
			}
			//not combat return normal speeds
		}else{
			if (this.isWalk()){
				if (this.mobBase.getWalk() <= 0)
					return MBServerStatics.MOB_SPEED_WALK * bonus;
				return this.mobBase.getWalk() * bonus;
			}else{
				if (this.mobBase.getRun() <= 0)
					return MBServerStatics.MOB_SPEED_RUN * bonus;
				return this.mobBase.getRun() * bonus;
			}
		}

	}

	@Override
	public float getPassiveChance(String type, int AttackerLevel, boolean fromCombat) {
		//TODO add this later for dodge
		return 0f;
	}

	/**
	 * @ Kill this Character
	 */
	@Override
	public void killCharacter(AbstractCharacter attacker) {

		
		this.stopMovement(this.getMovementLoc());

		if (attacker != null){

			if (attacker.getObjectType() == GameObjectType.PlayerCharacter) {
				Group g = GroupManager.getGroup((PlayerCharacter) attacker);

				// Give XP, now handled inside the Experience Object
				if (!this.isPet() && !this.isNecroPet() && !this.isSummonedPet() && !this.isPlayerGuard)
					Experience.doExperience((PlayerCharacter) attacker, this, g);
			} else if (attacker.getObjectType().equals(GameObjectType.Mob)){
				Mob mobAttacker = (Mob)attacker;

				if (mobAttacker.isPet()){

					PlayerCharacter owner = mobAttacker.getOwner();

					if (owner != null){

						if (!this.isPet() && !this.isNecroPet() && !this.isSummonedPet() && !this.isPlayerGuard){
							Group g = GroupManager.getGroup(owner);

							// Give XP, now handled inside the Experience Object
							Experience.doExperience(owner, this, g);
						}

					}
				}
			}
		}
		killCleanup();
	}

	public void updateLocation(){

		if (!this.isMoving())
			return;

		if (state == STATE.Disabled)
			return;

		if ( this.isAlive() == false || this.getBonuses().getBool(ModType.Stunned, SourceType.None) || this.getBonuses().getBool(ModType.CannotMove, SourceType.None)) {
			//Target is stunned or rooted. Don't move

			this.stopMovement(this.getMovementLoc());
			
			return;
		}
		
		Vector3fImmutable newLoc = this.getMovementLoc();

		if (newLoc.equals(this.getEndLoc())){
			this.stopMovement(newLoc);
			return;
			//Next upda
		}

		setLoc(newLoc);
		//Next update will be end Loc, lets stop him here.

	}

	@Override
	public void killCharacter(String reason) {
		killCleanup();
	}

	private void killCleanup() {
		Dispatch dispatch;

		try {
			if (this.isSiege) {
				this.deathTime = System.currentTimeMillis();
				this.state = STATE.Dead;
				try {
					this.clearEffects();
				}catch(Exception e){
					Logger.error( e.getMessage());
				}
				this.combatTarget = null;
				this.hasLoot = false;
				this.playerAgroMap.clear();

				this.timeToSpawnSiege = System.currentTimeMillis() + 60 * 15 * 1000;

				if (this.isPet()) {

					PlayerCharacter petOwner = this.getOwner();

					if (petOwner != null){
						this.setOwner(null);
						petOwner.setPet(null);
						PetMsg petMsg = new PetMsg(5, null);
						dispatch = Dispatch.borrow(this.getOwner(), petMsg);
						DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
					}
				}

			} else if (this.isPet() || this.isNecroPet()) {
				this.state = STATE.Disabled;

				this.combatTarget = null;
				this.hasLoot = false;

				if (this.parentZone != null)
					this.parentZone.zoneMobSet.remove(this);

				try {
					this.clearEffects();
				}catch(Exception e){
					Logger.error( e.getMessage());
				}
				this.playerAgroMap.clear();
				WorldGrid.RemoveWorldObject(this);

				DbManager.removeFromCache(this);

				// YEAH BONUS CODE!  THANKS UNNAMED ASSHOLE!
				//WorldServer.removeObject(this);
				//WorldGrid.INSTANCE.removeWorldObject(this);
				//owner.getPet().disableIntelligence();

				PlayerCharacter petOwner = this.getOwner();

				if (petOwner != null){
					this.setOwner(null);
					petOwner.setPet(null);
					PetMsg petMsg = new PetMsg(5, null);
					dispatch = Dispatch.borrow(petOwner, petMsg);
					DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
				}
			}  else {

				//cleanup effects

				this.deathTime = System.currentTimeMillis();
				this.state = STATE.Dead;

				playerAgroMap.clear();

				if (!this.isPlayerGuard){

					ArrayList<MobLoot> alml = LootTable.getMobLootDeath(this, this.getLevel(), this.getLootTable());

					for (MobLoot ml : alml) {
						this.charItemManager.addItemToInventory(ml);
					}

					if (this.equip != null){

						for (MobEquipment me: equip.values()){
							if (me.getDropChance() == 0)
								continue;

							float chance = ThreadLocalRandom.current().nextFloat();

							if (chance <= me.getDropChance()){
								MobLoot ml = new MobLoot(this, me.getItemBase(), false);
								ml.setFidelityEquipID(me.getObjectUUID());
								this.charItemManager.addItemToInventory(ml);
							}
						}
					}
				}

			}
			try {
				this.clearEffects();
			}catch(Exception e){
				Logger.error( e.getMessage());
			}

			this.combat = false;
			this.walkMode = true;
			this.combatTarget = null;

			this.hasLoot = (this.charItemManager.getInventoryCount() > 0) ? true : false;

		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public void respawn() {
		//Commenting out Mob ID rotation.

		this.despawned = false;
		this.playerAgroMap.clear();
		this.setCombatTarget(null);
		this.setHealth(this.healthMax);
		this.stamina.set(this.staminaMax);
		this.mana.set(this.manaMax);
		this.combat = false;
		this.walkMode = true;
		this.combatTarget = null;
		this.isAlive.set(true);
		
		if (!this.isSiege)
		this.lastBindLoc = Mob.GetSpawnRadiusLocation(this);
		else
			this.lastBindLoc = this.bindLoc;
		this.bindLoc = this.lastBindLoc;
		this.setLoc(this.lastBindLoc);
		this.stopMovement(this.lastBindLoc);
		this.initializeStaticEffects();
		this.recalculateStats();

		this.setHealth(this.healthMax);

		if (!this.isSiege && !this.isPlayerGuard && contract == null)
			loadInventory();

		//		LoadJob.getInstance();
		//		LoadJob.forceLoad(this);
	}

	public void despawn() {
		
		this.despawned = true;

		//WorldServer.removeObject(this);
		WorldGrid.RemoveWorldObject(this);
		this.charItemManager.clearInventory();
		this.despawnTime = System.currentTimeMillis();
		//		this.setLoc(Vector3fImmutable.ZERO);
	}

	//Sets the relative position to a parent zone
	public void setRelPos(Zone zone, float locX, float locY, float locZ) {

		//update mob zone map

		if (this.parentZone != null)
			this.parentZone.zoneMobSet.remove(this);

		zone.zoneMobSet.add(this);

		this.statLat = locX;
		this.statAlt = locY;
		this.statLon = locZ;
		this.parentZone = zone;
		this.setBindLoc(new Vector3fImmutable(this.statLat + zone.absX, this.statAlt + zone.absY, this.statLon + zone.absZ));
	}

	public boolean canRespawn(){
		return System.currentTimeMillis() > this.despawnTime + 4000;
	}

	@Override
	public boolean canBeLooted() {
		return !this.isAlive();
	}

	public int getTypeMasks() {

		if (this.mobBase == null)
			return 0;

		return this.mobBase.getTypeMasks();
	}

	/**
	 * Clears and sets the inventory of the Mob. Must be called every time the
	 * mob is spawned or respawned.
	 */
	private void loadInventory() {

		if (!MBServerStatics.ENABLE_MOB_LOOT)
			return;

		this.charItemManager.clearInventory();
		this.charItemManager.clearEquip();

		if (isPlayerGuard)
			return;

		int gold = Mob.randomGoldAmount(this);

		if (gold > 0 && this.getLootTable() != 0) {
			addGoldToInventory(gold);
		}

		//add random loot to mob
		ArrayList<MobLoot> alml = LootTable.getMobLoot(this, this.getLevel(), this.getLootTable(), false); //add hotzone check in later

		for (MobLoot ml : alml) {
			this.charItemManager.addItemToInventory(ml);
		}



		//add special loot to mob
	}

	private int getLootTable() {

		if (this.mobBase == null)
			return 0;

		return this.mobBase.getLootTable();
	}

	/**
	 * Sets the quantity of gold in the inventory. Calling this multiple times
	 * will overwrite the gold amount.
	 *
	 * @param quantity Quantity of gold.
	 */
	private void addGoldToInventory(int quantity) {
		MobLoot gold = new MobLoot(this, quantity);
		this.charItemManager.addItemToInventory(gold);
	}

	/**
	 * Generate a random quantity of gold for this mob.
	 *
	 * @return Quantity of gold
	 */
	public static int randomGoldAmount( Mob mob) {

		// percentage chance to drop gold

		//R8 mobs have 100% gold drop.
		if (mob.getLevel() < 80)
			if ((ThreadLocalRandom.current().nextDouble() * 100d) > MBServerStatics.GOLD_DROP_PERCENTAGE_CHANCE)
				return 0;


		int level = (int) mob.getLevel();
		level = (level < 0) ? 0 : level;
		level = (level > 50) ? 50 : level;

		double minGold;
		double maxGold;

		if (mob.mobBase != null) {
			minGold = mob.mobBase.getMinGold();
			maxGold = mob.mobBase.getMaxGold();
		} else {
			minGold = MBServerStatics.GOLD_DROP_MINIMUM_PER_MOB_LEVEL[level];
			maxGold = MBServerStatics.GOLD_DROP_MAXIMUM_PER_MOB_LEVEL[level];
		}

		double gold = (ThreadLocalRandom.current().nextDouble() * (maxGold - minGold) + minGold);


		//server specific gold multiplier
		double goldMod = MBServerStatics.GOLD_RATE_MOD;
		gold *= goldMod;

		//modify for hotzone

		if (ZoneManager.inHotZone(mob.getLoc()))
			gold *= MBServerStatics.HOT_GOLD_RATE_MOD;

		gold *= MBServerStatics.GOLD_RATE_MOD;

		return (int) gold;
	}

	public static Mob createMob(int loadID, Vector3fImmutable spawn, Guild guild, boolean isMob, Zone parent,Building building, int contractID) {

		Mob mobWithoutID = new Mob("", "", (short) 0, (short) 0, (short) 0, (short) 0,
				(short) 0, (short) 1, 0, false, false, false, spawn, spawn, Vector3fImmutable.ZERO,
				(short) 1, (short) 1, (short) 1, guild, (byte) 0, loadID, isMob, parent,building,contractID);

		if (parent != null) {
			mobWithoutID.setRelPos(parent, spawn.x - parent.absX, spawn.y - parent.absY, spawn.z - parent.absZ);
		}


		if (mobWithoutID.mobBase == null) {
			return null;
		}
		Mob mob;
		try {
			mob = DbManager.MobQueries.ADD_MOB(mobWithoutID, isMob);
			mob.setObjectTypeMask(MBServerStatics.MASK_MOB | mob.getTypeMasks());
			mob.setMob();
			mob.setParentZone(parent);
		} catch (Exception e) {
			Logger.error("SQLException:" + e.getMessage());
			mob = null;
		}
		return mob;
	}

	public static Mob createPet( int loadID, Guild guild, Zone parent, PlayerCharacter owner, short level) {
		MobBase mobBase = MobBase.getMobBase(loadID);
		Mob mob = null;
		if (mobBase == null || owner == null) {
			return null;
		}
		createLock.writeLock().lock();
		level += 20;
		try {
			mob = new Mob( mobBase, guild, parent, level, owner, 0);
			if (mob.mobBase == null) {
				return null;
			}
			mob.runAfterLoad();

			Vector3fImmutable loc = owner.getLoc();
			if (parent != null) {
				mob.setRelPos(parent, loc.x - parent.absX, loc.y - parent.absY, loc.z - parent.absZ);
			}
			DbManager.addToCache(mob);
			mob.setPet(owner, true);
			mob.setWalkMode(false);
			mob.state = STATE.Awake;

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			createLock.writeLock().unlock();
		}

		return mob;
	}



	public static int nextStaticID() {
		int id = Mob.staticID;
		Mob.staticID++;
		return id;
	}

	/*
	 * Database
	 */


	public static Mob getMob(int id) {

		if (id == 0)
			return null;

		Mob mob  = (Mob) DbManager.getFromCache(GameObjectType.Mob, id);
		if (mob != null)
			return mob;
		return DbManager.MobQueries.GET_MOB(id);
	}

	public static Mob getFromCache(int id) {


		return (Mob) DbManager.getFromCache(GameObjectType.Mob, id);
	}

	public static Mob getFromCacheDBID(int id) {
		if (Mob.mobMapByDBID.containsKey(id)) {
			return Mob.mobMapByDBID.get(id);
		}
		return null;
	}

	@Override
	public void updateDatabase() {
		//		DbManager.MobQueries.updateDatabase(this);
	}

	public int removeFromDatabase() {
		return DbManager.MobQueries.DELETE_MOB(this);
	}

	public void refresh() {
		if (this.isAlive())
			WorldGrid.updateObject(this);
	}

	public void recalculateStats() {

		try {
			calculateModifiedStats();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}

		try {
			calculateAtrDefenseDamage();
		} catch (Exception e) {
			Logger.error( this.getMobBaseID() + " /" + e.getMessage());
		}
		try {
			calculateMaxHealthManaStamina();
		} catch (Exception e) {
			Logger.error( e.getMessage());
		}

		Resists.calculateResists(this);
	}

	public void calculateMaxHealthManaStamina() {
		float h = 1f;
		float m = 0f;
		float s = 0f;

		h = this.mobBase.getHealthMax();
		m = this.statSpiCurrent;
		s = this.statConCurrent;

		// Apply any bonuses from runes and effects
		if (this.bonuses != null) {
			h += this.bonuses.getFloat(ModType.HealthFull, SourceType.None);
			m += this.bonuses.getFloat(ModType.ManaFull,SourceType.None);
			s += this.bonuses.getFloat(ModType.StaminaFull, SourceType.None);

			//apply effects percent modifiers. DO THIS LAST!
			h *= (1 + this.bonuses.getFloatPercentAll(ModType.HealthFull,SourceType.None));
			m *= (1 + this.bonuses.getFloatPercentAll(ModType.ManaFull,SourceType.None));
			s *= (1 + this.bonuses.getFloatPercentAll(ModType.StaminaFull,SourceType.None));
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
		if (this.getHealth() > this.healthMax)
			this.setHealth(this.healthMax);

		if (this.mana.get() > this.manaMax)
			this.mana.set(this.manaMax);

		if (this.stamina.get() > this.staminaMax)
			this.stamina.set(staminaMax);

	}

	public void calculateAtrDefenseDamage() {

		if (this.charItemManager == null || this.equip == null) {
			Logger.error("Player " + currentID + " missing skills or equipment");
			defaultAtrAndDamage(true);
			defaultAtrAndDamage(false);
			this.defenseRating = 0;
			return;
		}

		try {
			calculateAtrDamageForWeapon(
					this.equip.get(MBServerStatics.SLOT_MAINHAND), true, this.equip.get(MBServerStatics.SLOT_OFFHAND));
		} catch (Exception e) {

			this.atrHandOne = (short) this.mobBase.getAttackRating();
			this.minDamageHandOne = (short) this.mobBase.getMinDmg();
			this.maxDamageHandOne = (short) this.mobBase.getMaxDmg();
			this.rangeHandOne = 6.5f;
			this.speedHandOne = 20;
			Logger.info("Mobbase ID " + this.getMobBaseID() + " returned an error. setting to default ATR and Damage." + e.getMessage());
		}

		try {
			calculateAtrDamageForWeapon(this.equip.get(MBServerStatics.SLOT_OFFHAND), false, this.equip.get(MBServerStatics.SLOT_MAINHAND));

		} catch (Exception e) {

			this.atrHandTwo = (short) this.mobBase.getAttackRating();
			this.minDamageHandTwo = (short) this.mobBase.getMinDmg();
			this.maxDamageHandTwo = (short) this.mobBase.getMaxDmg();
			this.rangeHandTwo = 6.5f;
			this.speedHandTwo = 20;
			Logger.info( "Mobbase ID " + this.getMobBaseID() + " returned an error. setting to default ATR and Damage." + e.getMessage());
		}

		try {
			float defense = this.mobBase.getDefenseRating();
			defense += getShieldDefense(equip.get(MBServerStatics.SLOT_OFFHAND));
			defense += getArmorDefense(equip.get(MBServerStatics.SLOT_HELMET));
			defense += getArmorDefense(equip.get(MBServerStatics.SLOT_CHEST));
			defense += getArmorDefense(equip.get(MBServerStatics.SLOT_ARMS));
			defense += getArmorDefense(equip.get(MBServerStatics.SLOT_GLOVES));
			defense += getArmorDefense(equip.get(MBServerStatics.SLOT_LEGGINGS));
			defense += getArmorDefense(equip.get(MBServerStatics.SLOT_FEET));
			defense += getWeaponDefense(equip);

			if (this.bonuses != null) {
				// add any bonuses
				defense += (short) this.bonuses.getFloat(ModType.DCV, SourceType.None);

				// Finally multiply any percent modifiers. DO THIS LAST!
				float pos_Bonus = 1 + this.bonuses.getFloatPercentPositive(ModType.DCV, SourceType.None);

			

				defense = (short) (defense * pos_Bonus);

				//Lucky rune applies next
				
				float neg_Bonus = this.bonuses.getFloatPercentNegative(ModType.DCV, SourceType.None);
				defense = (short) (defense *(1 + neg_Bonus));

			

			} else {
				// TODO add error log here
				Logger.error( "Error: missing bonuses");
			}

			defense = (defense < 1) ? 1 : defense;
			this.defenseRating = (short) (defense + 0.5f);
		} catch (Exception e) {
			Logger.info("Mobbase ID " + this.getMobBaseID() + " returned an error. Setting to Default Defense." + e.getMessage());
			this.defenseRating = (short) this.mobBase.getDefense();
		}
		// calculate defense for equipment
	}

	private float getWeaponDefense(HashMap<Integer, MobEquipment> equipped) {
		MobEquipment weapon = equipped.get(MBServerStatics.SLOT_MAINHAND);
		ItemBase wb = null;
		CharacterSkill skill, mastery;
		float val = 0;
		boolean unarmed = false;
		if (weapon == null) {
			weapon = equipped.get(MBServerStatics.SLOT_OFFHAND);

			if (weapon == null)
				unarmed = true;
			 else
				wb = weapon.getItemBase();

		} else
			wb = weapon.getItemBase();

		if (wb == null)
			unarmed = true;

		if (unarmed) {
			skill = null;
			mastery = null;
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

	private float getShieldDefense(MobEquipment shield) {

		if (shield == null)
			return 0;

		ItemBase ab = shield.getItemBase();

		if (ab == null || !ab.isShield())
			return 0;

		CharacterSkill blockSkill = this.skills.get("Block");
		float skillMod;

		if (blockSkill == null) {
			skillMod = CharacterSkill.getQuickMastery(this, "Block");

			if (skillMod == 0f)
				return 0;

		} else
			skillMod = blockSkill.getModifiedAmount();

			//			// Only fighters and healers can block
			//			if (this.baseClass != null && (this.baseClass.getUUID() == 2500 || this.baseClass.getUUID() == 2501))
			//				this.bonuses.setBool("Block", true);

		float def = ab.getDefense();
		//apply item defense bonuses
		// float val = ((float)ab.getDefense()) * (1 + (skillMod / 100));
		return (def * (1 + ((int) skillMod / 100f)));
	}

	private float getArmorDefense(MobEquipment armor) {

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

		if (armorSkill == null)
			return ib.getDefense();

		float def = ib.getDefense();

		//apply item defense bonuses

		return (def * (1 + ((int) armorSkill.getModifiedAmount() / 50f)));
	}

	private void calculateAtrDamageForWeapon(MobEquipment weapon, boolean mainHand, MobEquipment otherHand) {

		int baseStrength = 0;

		float skillPercentage, masteryPercentage;
		float mastDam;

		// make sure weapon exists
		boolean noWeapon = false;
		ItemBase wb = null;

		if (weapon == null)
			noWeapon = true;
		 else {

			ItemBase ib = weapon.getItemBase();

			if (ib == null)
				noWeapon = true;
			 else {

				if (ib.getType().equals(ItemType.WEAPON) == false) {
					defaultAtrAndDamage(mainHand);
					return;
				} else
					wb = ib;
			}
		}
		float min, max;
		float speed = 20f;
		boolean strBased = false;

		// get skill percentages and min and max damage for weapons

		if (noWeapon) {

			if (mainHand)
				this.rangeHandOne = this.mobBase.getAttackRange();
			 else
				this.rangeHandTwo = -1; // set to do not attack

			skillPercentage = getModifiedAmount(this.skills.get("Unarmed Combat"));
			masteryPercentage = getModifiedAmount(this.skills.get("Unarmed Combat Mastery"));

			if (masteryPercentage == 0f)
				mastDam = CharacterSkill.getQuickMastery(this, "Unarmed Combat Mastery");
			 else
				mastDam = masteryPercentage;

			// TODO Correct these
			min = this.mobBase.getMinDmg();
			max = this.mobBase.getMaxDmg();
		} else {

			if (mainHand)
				this.rangeHandOne = weapon.getItemBase().getRange() * (1 + (baseStrength / 600));
			 else
				this.rangeHandTwo = weapon.getItemBase().getRange() * (1 + (baseStrength / 600));

			skillPercentage = getModifiedAmount(this.skills.get(wb.getSkillRequired()));
			masteryPercentage = getModifiedAmount(this.skills.get(wb.getMastery()));

			if (masteryPercentage == 0f)
				mastDam = 0f;
			else
				mastDam = masteryPercentage;

			min = (float) wb.getMinDamage();
			max = (float) wb.getMaxDamage();
			strBased = wb.isStrBased();
		}

		// calculate atr
		float atr = this.mobBase.getAttackRating();

		atr += ((int) skillPercentage * 4f); //<-round down skill% -
		atr += ((int) masteryPercentage * 3f);

		if (this.statStrCurrent > this.statDexCurrent)
			atr += statStrCurrent / 2;
		 else
			atr += statDexCurrent / 2;

		// add in any bonuses to atr
		if (this.bonuses != null) {
			// Add any base bonuses
			atr += this.bonuses.getFloat(ModType.OCV, SourceType.None);

			// Finally use any multipliers. DO THIS LAST!
			float pos_Bonus = 1 + this.bonuses.getFloatPercentPositive(ModType.OCV, SourceType.None);

		
			atr *= pos_Bonus;

			// next precise
			
//			atr *= (1 + ((float) this.bonuses.getShort("rune.Attack") / 100));

			//and negative percent modifiers
			//TODO DO DEBUFFS AFTER?? wILL TEst when finished
			float neg_Bonus = this.bonuses.getFloatPercentNegative(ModType.OCV, SourceType.None);

			

			atr *= (1 + neg_Bonus);
		}

		atr = (atr < 1) ? 1 : atr;

		// set atr
		if (mainHand)
			this.atrHandOne = (short) (atr + 0.5f);
		 else
			this.atrHandTwo = (short) (atr + 0.5f);

		//calculate speed

		if (wb != null)
			speed = wb.getSpeed();
		 else
			speed = 20f; //unarmed attack speed

		if (this.bonuses != null && this.bonuses.getFloat(ModType.AttackDelay, SourceType.None) != 0f) //add effects speed bonus
			speed *= (1 + this.bonuses.getFloatPercentAll(ModType.AttackDelay, SourceType.None));

		if (speed < 10)
			speed = 10;

		//add min/max damage bonuses for weapon  **REMOVED

		//if duel wielding, cut damage by 30%
		// calculate damage
		float minDamage;
		float maxDamage;
		float pri = (strBased) ? (float) this.statStrCurrent : (float) this.statDexCurrent;
		float sec = (strBased) ? (float) this.statDexCurrent : (float) this.statStrCurrent;

		minDamage = (float) (min * ((0.0315f * Math.pow(pri, 0.75f)) + (0.042f * Math.pow(sec, 0.75f)) + (0.01f * ((int) skillPercentage + (int) mastDam))));
		maxDamage = (float) (max * ((0.0785f * Math.pow(pri, 0.75f)) + (0.016f * Math.pow(sec, 0.75f)) + (0.0075f * ((int) skillPercentage + (int) mastDam))));

		minDamage = (float) ((int) (minDamage + 0.5f)); //round to nearest decimal
		maxDamage = (float) ((int) (maxDamage + 0.5f)); //round to nearest decimal
		//	Logger.info("MobCalculateDamage", "Mob with ID "+ this.getObjectUUID() +   " and MOBBASE with ID " + this.getMobBaseID() + " returned " + minDamage + "/" + maxDamage + " modified Damage.");

		//add Base damage last.
		float minDamageMod = this.mobBase.getDamageMin();
		float maxDamageMod = this.mobBase.getDamageMax();

		minDamage += minDamageMod;
		maxDamage += maxDamageMod;

		// add in any bonuses to damage
		if (this.bonuses != null) {
			// Add any base bonuses
			minDamage += this.bonuses.getFloat(ModType.MinDamage, SourceType.None);
			maxDamage += this.bonuses.getFloat(ModType.MaxDamage, SourceType.None);

			// Finally use any multipliers. DO THIS LAST!
			minDamage *= (1 + this.bonuses.getFloatPercentAll(ModType.MinDamage, SourceType.None));
			maxDamage *= (1 + this.bonuses.getFloatPercentAll(ModType.MaxDamage, SourceType.None));
		}

		// set damages
		if (mainHand) {
			this.minDamageHandOne = (short) minDamage;
			this.maxDamageHandOne = (short) maxDamage;
			this.speedHandOne = 30;
		} else {
			this.minDamageHandTwo = (short) minDamage;
			this.maxDamageHandTwo = (short) maxDamage;
			this.speedHandTwo = 30;
		}
	}

	private static float getModifiedAmount(CharacterSkill skill) {

		if (skill == null)
			return 0f;

		return skill.getModifiedAmount();
	}

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
	
	public static int getBuildingSlot(Mob mob){
		int slot = -1;

		if (mob.building == null)
			return -1;



		BuildingModelBase buildingModel = BuildingModelBase.getModelBase(mob.building.getMeshUUID());

		if (buildingModel == null)
			return -1;

		
			if (mob.building.getHirelings().containsKey(mob))
				slot =  (mob.building.getHirelings().get(mob));
		

		if (buildingModel.getNPCLocation(slot) == null)
			return -1;


		return slot;
	}

	public void setInBuildingLoc(Building inBuilding, AbstractCharacter ac) {
		
		Mob mob = null;
		
		NPC npc = null;
		
		
		if (ac.getObjectType().equals(GameObjectType.Mob))
			mob = (Mob)ac;
		
		else if (ac.getObjectType().equals(GameObjectType.NPC))
				npc = (NPC)ac;

		// *** Refactor : Need to take a look at this, make sure
		// npc's are loaded in correct spots.

		BuildingModelBase buildingModel = BuildingModelBase.getModelBase(inBuilding.getMeshUUID());

		Vector3fImmutable slotLocation = Vector3fImmutable.ZERO;

		if (buildingModel != null){


			int putSlot = -1;
			BuildingLocation buildingLocation = null;

			//-1 slot means no slot available in building.
			
			if (npc != null){
				if (npc.getSiegeMinionMap().containsKey(this))
					putSlot = npc.getSiegeMinionMap().get(this);
			}else if (mob != null)
				if (mob.getSiegeMinionMap().containsKey(this))
					putSlot = mob.getSiegeMinionMap().get(this);
			
			int count = 0;
			
			for (BuildingLocation slotLoc: buildingModel.getLocations())
				if (slotLoc.getType() == 6)
					count++;
			
		
			buildingLocation = buildingModel.getSlotLocation((count) - putSlot);

			if (buildingLocation != null){
				slotLocation = buildingLocation.getLoc();
			}

		}
		
		this.inBuildingLoc = slotLocation;

	}

	public Vector3fImmutable getInBuildingLoc() {
		return inBuildingLoc;
	}

	public ItemBase getWeaponItemBase(boolean mainHand) {

		if (this.equipmentSetID != 0){

			if (equip != null) {
				MobEquipment me = null;

				if (mainHand)
					me = equip.get(1); //mainHand
				 else
					me = equip.get(2); //offHand

				if (me != null) {

					ItemBase ib = me.getItemBase();

					if (ib != null)
						return ib;

				}
			}
		}
		MobBase mb = this.mobBase;

		if (mb != null) {

			if (equip != null) {

				MobEquipment me = null;

				if (mainHand)
					me = equip.get(1); //mainHand
				 else
					me = equip.get(2); //offHand

				if (me != null) {

					ItemBase ib = me.getItemBase();

					if (ib != null)
						return ib;
				}
			}
		}
		return null;
	}

	@Override
	public void runAfterLoad() {

		try{
			if (this.equipmentSetID != 0)
				this.equip = MobBase.loadEquipmentSet(this.equipmentSetID);
			else
				this.equip = new HashMap<>();

		} catch(Exception e){
			Logger.error( e.getMessage());
		}

		if (this.equip == null) {
			Logger.error("Null equipset returned for uuid " + currentID);
			this.equip = new HashMap<>(0);
		}

		try{
			this.initializeStaticEffects();

			try {
				this.initializeSkills();
			} catch (Exception e) {
				Logger.error( e.getMessage());
			}

			recalculateStats();
			this.setHealth(this.healthMax);

			// Set bounds for this mobile
			Bounds mobBounds = Bounds.borrow();
			mobBounds.setBounds(this.getLoc());
			this.setBounds(mobBounds);

		} catch (Exception e){
			Logger.error(e.getMessage());
		}
	}

	@Override
	protected ConcurrentHashMap<Integer, CharacterPower> initializePowers() {
		return new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	}

	public boolean canSee(PlayerCharacter target) {
		return this.mobBase.getSeeInvis() >= target.getHidden();
	}

	public int getBuildingID() {
		return buildingID;
	}

	public void setBuildingID(int buildingID) {
		this.buildingID = buildingID;
	}

	public boolean isSiege() {
		return isSiege;
	}

	public void setSiege(boolean isSiege) {
		this.isSiege = isSiege;
	}

	public long getTimeToSpawnSiege() {
		return timeToSpawnSiege;
	}

	public void setTimeToSpawnSiege(long timeToSpawnSiege) {
		this.timeToSpawnSiege = timeToSpawnSiege;
	}

	public AbstractCharacter getNpcOwner() {
		return npcOwner;
	}

	public void setNpcOwner(AbstractCharacter npcOwner) {
		this.npcOwner = npcOwner;
	}

	public boolean isNecroPet() {
		return this.mobBase.isNecroPet();
	}

	public static void HandleAssistedAggro(PlayerCharacter source, PlayerCharacter target) {

		HashSet<AbstractWorldObject> mobsInRange = WorldGrid.getObjectsInRangePartial(source, MBServerStatics.AI_DROP_AGGRO_RANGE, MBServerStatics.MASK_MOB);

		for (AbstractWorldObject awo : mobsInRange) {
			Mob mob = (Mob) awo;

			//Mob is not attacking anyone, skip.
			if (mob.getCombatTarget() == null)
				continue;

			//Mob not attacking target's target, let's not be failmu and skip this target.
			if (mob.getCombatTarget() != target)
				continue;

			//target is mob's combat target, LETS GO.
			if (source.getHateValue() > target.getHateValue()) {
				mob.setCombatTarget(source);
				MobileFSM.setAggro(mob, source.getObjectUUID());
			}
		}
	}

	public void handleDirectAggro(AbstractCharacter ac) {

		if (ac.getObjectType().equals(GameObjectType.PlayerCharacter) == false)
			return;

		PlayerCharacter player = (PlayerCharacter)ac;

		if (this.getCombatTarget() == null) {
			MobileFSM.setAggro(this, player.getObjectUUID());
			return;
		}

		if (player.getObjectUUID() == this.getCombatTarget().getObjectUUID())
			return;

		if (this.getCombatTarget().getObjectType() == GameObjectType.PlayerCharacter) {

			if (ac.getHateValue() > ((PlayerCharacter) this.getCombatTarget()).getHateValue()) {
				this.setCombatTarget(player);
				MobileFSM.setAggro(this, player.getObjectUUID());
			}
		}
	}

	public boolean remove(Building building) {

		// Remove npc from it's building
		this.state = STATE.Disabled;

		try {
			this.clearEffects();
		}catch(Exception e){
			Logger.error(e.getMessage());
		}

		if (this.parentZone != null)
			this.parentZone.zoneMobSet.remove(this);

		if (building != null) {
			building.getHirelings().remove(this);
			this.removeMinions();
		}

		// Delete npc from database

		if (DbManager.MobQueries.DELETE_MOB(this) == 0)
			return false;

		// Remove npc from the simulation

		this.removeFromCache();
		DbManager.removeFromCache(this);
		WorldGrid.RemoveWorldObject(this);
		WorldGrid.removeObject(this);
		return true;
	}

	public void removeMinions() {

		for (Mob toRemove : this.siegeMinionMap.keySet()) {

			toRemove.state = STATE.Disabled;

			if (this.isMoving()){
			
				this.stopMovement(this.getLoc());
				this.state = STATE.Disabled;

				if (toRemove.parentZone != null)
					toRemove.parentZone.zoneMobSet.remove(toRemove);
			}

			try {
				toRemove.clearEffects();
			} catch(Exception e){
				Logger.error(e.getMessage());
			}

			if (toRemove.parentZone != null)
				toRemove.parentZone.zoneMobSet.remove(toRemove);

			WorldGrid.RemoveWorldObject(toRemove);
			WorldGrid.removeObject(toRemove);
			DbManager.removeFromCache(toRemove);

			PlayerCharacter petOwner = toRemove.getOwner();

			if (petOwner != null) {

				petOwner.setPet(null);
				toRemove.setOwner(null);

				PetMsg petMsg = new PetMsg(5, null);
				Dispatch dispatch = Dispatch.borrow(petOwner, petMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
			}
		}
	}

	public static void submitUpgradeJob(Mob mob) {

		JobContainer jc;

		if (mob.getUpgradeDateTime() == null) {
			Logger.error("Failed to get Upgrade Date");
			return;
		}

		// Submit upgrade job for future date or current instant

		if (mob.getUpgradeDateTime().isAfter(DateTime.now()))
			jc = JobScheduler.getInstance().scheduleJob(new UpgradeNPCJob(mob),
					mob.getUpgradeDateTime().getMillis());
		else
			JobScheduler.getInstance().scheduleJob(new UpgradeNPCJob(mob), 0);

	}

	public void setRank(int newRank) {

		DbManager.MobQueries.SET_PROPERTY(this, "mob_level", newRank);
		this.level = (short) newRank;

	}

	public static int getUpgradeTime(Mob mob) {

		if (mob.getRank() < 7)
			return (mob.getRank() * 8);

		return 0;
	}

	public static int getUpgradeCost(Mob mob) {

		int upgradeCost;

		upgradeCost = Integer.MAX_VALUE;

		if (mob.getRank() < 7)
			return (mob.getRank() * 100650) + 21450;

		return upgradeCost;
	}

	public boolean isRanking() {

		return this.upgradeDateTime != null;
	}

	public boolean isNoAggro() {
		return noAggro;
	}

	public void setNoAggro(boolean noAggro) {
		this.noAggro = noAggro;
	}

	public STATE getState() {
		return state;
	}

	public void setState(STATE state) {
		this.state = state;
	}

	public int getAggroTargetID() {
		return aggroTargetID;
	}

	public void setAggroTargetID(int aggroTargetID) {
		this.aggroTargetID = aggroTargetID;
	}

	public boolean isWalkingHome() {
		return walkingHome;
	}

	public void setWalkingHome(boolean walkingHome) {
		this.walkingHome = walkingHome;
	}

	public long getLastAttackTime() {
		return lastAttackTime;
	}

	public void setLastAttackTime(long lastAttackTime) {
		this.lastAttackTime = lastAttackTime;
	}

	public ConcurrentHashMap<Integer, Boolean> getPlayerAgroMap() {
		return playerAgroMap;
	}

	public long getDeathTime() {
		return deathTime;
	}

	public boolean isHasLoot() {
		return hasLoot;
	}
	public void setDeathTime(long deathTime) {
		this.deathTime = deathTime;
	}

	public DeferredPowerJob getWeaponPower() {
		return weaponPower;
	}

	public void setWeaponPower(DeferredPowerJob weaponPower) {
		this.weaponPower = weaponPower;
	}
	public ConcurrentHashMap<Mob, Integer> getSiegeMinionMap() {
		return siegeMinionMap;
	}

	public Building getBuilding() {
		return this.building;
	}

	public DateTime getUpgradeDateTime() {

		lock.readLock().lock();

		try {
			return upgradeDateTime;
		} finally {
			lock.readLock().unlock();
		}
	}

	public synchronized Mob createGuardMob(int loadID, Guild guild, Zone parent, Vector3fImmutable loc, short level, String pirateName) {

		MobBase minionMobBase;
		Mob mob;
		int maxSlots = 1;

		switch (this.getRank()){
		case 1:
		case 2:
			maxSlots = 1;
			break;
		case 3:
			maxSlots = 2;
			break;
		case 4:
		case 5:
			maxSlots = 3;
			break;
		case 6:
			maxSlots = 4;
			break;
		case 7:
			maxSlots = 5;
			break;
		default:
			maxSlots = 1;

		}

		if (siegeMinionMap.size() == maxSlots)
			return null;

		minionMobBase = this.mobBase;

		if (minionMobBase == null)
			return null;

		mob = new Mob(minionMobBase, guild, parent, level,new Vector3fImmutable(1,1,1), 0,true);
		
		mob.despawned = true;

		mob.setLevel(level);
		//grab equipment and name from minionbase.
		if (this.contract != null){
			MinionType minionType = MinionType.ContractToMinionMap.get(this.contract.getContractID());
			if (minionType != null){
				mob.equipmentSetID = minionType.getEquipSetID();
				String rank = "";
				
				if (this.getRank() < 3)
					rank = MBServerStatics.JUNIOR;
				else if (this.getRank() < 6)
					rank = "";
				else if (this.getRank() == 6)
					rank = MBServerStatics.VETERAN;
				else
					rank = MBServerStatics.ELITE;
				
				if (rank.isEmpty())
					mob.nameOverride = pirateName + " " + minionType.getRace() + " " + minionType.getName();
				else
					mob.nameOverride = pirateName + " " + minionType.getRace() + " " + rank + " " + minionType.getName();
			}
		}
	
		

		if (parent != null)
			mob.setRelPos(parent, loc.x - parent.absX, loc.y - parent.absY, loc.z - parent.absZ);

		mob.setObjectTypeMask(MBServerStatics.MASK_MOB | mob.getTypeMasks());

		// mob.setMob();
		mob.isPlayerGuard = true;
		mob.setParentZone(parent);
		DbManager.addToCache(mob);
		mob.runAfterLoad();
		
		

		RuneBase guardRune = RuneBase.getRuneBase(252621);

		for (MobBaseEffects mbe : guardRune.getEffectsList()) {

			EffectsBase eb = PowersManager.getEffectByToken(mbe.getToken());

			if (eb == null) {
				Logger.info( "EffectsBase Null for Token " + mbe.getToken());
				continue;
			}

			//check to upgrade effects if needed.
			if (mob.effects.containsKey(Integer.toString(eb.getUUID()))) {
				if (mbe.getReqLvl() > (int) mob.level) {
					continue;
				}

				Effect eff = mob.effects.get(Integer.toString(eb.getUUID()));

				if (eff == null)
					continue;

				//Current effect is a higher rank, dont apply.
				if (eff.getTrains() > mbe.getRank())
					continue;

				//new effect is of a higher rank. remove old effect and apply new one.
				eff.cancelJob();
				mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
			} else {

				if (mbe.getReqLvl() > (int) mob.level)
					continue;

				mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
			}
		}

		int slot = 0;
		slot += siegeMinionMap.size() + 1;

		siegeMinionMap.put(mob, slot);
		mob.setInBuildingLoc(this.building, this);
		mob.setBindLoc(loc.add(mob.inBuildingLoc));
		mob.deathTime = System.currentTimeMillis();
		mob.spawnTime = 900;
		mob.npcOwner = this;
		mob.state = STATE.Respawn;

		return mob;
	}

	public static void setUpgradeDateTime(Mob mob,DateTime upgradeDateTime) {

		if (!DbManager.MobQueries.updateUpgradeTime(mob, upgradeDateTime)){
			Logger.error("Failed to set upgradeTime for building " + mob.currentID);
			return;
		}
		mob.upgradeDateTime = upgradeDateTime;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public boolean isPlayerGuard() {
		return isPlayerGuard;
	}

	public void setPlayerGuard(boolean isPlayerGuard) {
		this.isPlayerGuard = isPlayerGuard;
	}

	public int getPatrolPointIndex() {
		return patrolPointIndex;
	}

	public void setPatrolPointIndex(int patrolPointIndex) {
		this.patrolPointIndex = patrolPointIndex;
	}

	public int getLastMobPowerToken() {
		return lastMobPowerToken;
	}

	public void setLastMobPowerToken(int lastMobPowerToken) {
		this.lastMobPowerToken = lastMobPowerToken;
	}

	public Regions getLastRegion() {
		return lastRegion;
	}

	public void setLastRegion(Regions lastRegion) {
		this.lastRegion = lastRegion;
	}

	public boolean isLootSync() {
		return lootSync;
	}

	public void setLootSync(boolean lootSync) {
		this.lootSync = lootSync;
	}

	public int getFidalityID() {
		return fidalityID;
	}

	public HashMap<Integer, MobEquipment> getEquip() {
		return equip;
	}

	public int getEquipmentSetID() {
		return equipmentSetID;
	}

	public int getLootSet() {
		return lootSet;
	}

	public boolean isGuard(){
		return this.isGuard;
	}

	public String getNameOverride() {
		return nameOverride;
	}
	
	public static Vector3fImmutable GetSpawnRadiusLocation(Mob mob){
		
		Vector3fImmutable returnLoc = Vector3fImmutable.ZERO;
		
		if (mob.fidalityID != 0 && mob.building != null){

			
			Vector3fImmutable spawnRadiusLoc = Vector3fImmutable.getRandomPointInCircle(mob.localLoc, mob.spawnRadius);

			Vector3fImmutable buildingWorldLoc = ZoneManager.convertLocalToWorld(mob.building, spawnRadiusLoc);
			
			return buildingWorldLoc;
			
		
			
		}else{
			
			boolean run = true;
			
			while(run){
				Vector3fImmutable localLoc = new Vector3fImmutable(mob.statLat + mob.parentZone.absX, mob.statAlt + mob.parentZone.absY, mob.statLon + mob.parentZone.absZ);
				Vector3fImmutable spawnRadiusLoc = Vector3fImmutable.getRandomPointInCircle(localLoc, mob.spawnRadius);
				
				//not a roaming mob, just return the random loc.
				if (mob.spawnRadius < 12000)
					return spawnRadiusLoc;
				
				Zone spawnZone = ZoneManager.findSmallestZone(spawnRadiusLoc);
				//dont spawn roaming mobs in npc cities
				if (spawnZone.isNPCCity())
					continue;
				
				//dont spawn roaming mobs in player cities.
				if (spawnZone.isPlayerCity())
					continue;
				
				//don't spawn mobs in water.
				if (HeightMap.isLocUnderwater(spawnRadiusLoc))
					continue;
				
				run = false;
				
				return spawnRadiusLoc;
				
			}

		}
		
		//shouldn't ever get here.
		
		return returnLoc;
	}
	
	public void processUpgradeMob(PlayerCharacter player){
		
		lock.writeLock().lock();
		
		try{
			
		building = this.getBuilding();

		// Cannot upgrade an npc not within a building

		if (building == null)
			return;

		// Cannot upgrade an npc at max rank

		if (this.getRank() == 7)
			return;

		// Cannot upgrade an npc who is currently ranking

		if (this.isRanking())
			return;

		int rankCost = Mob.getUpgradeCost(this);

		// SEND NOT ENOUGH GOLD ERROR

		if (rankCost > building.getStrongboxValue()) {
			sendErrorPopup(player, 127);
			return;
		}

		try {

			if (!building.transferGold(-rankCost,false)){
				return;
			}

			DateTime dateToUpgrade = DateTime.now().plusHours(Mob.getUpgradeTime(this));
			Mob.setUpgradeDateTime(this,dateToUpgrade);

			// Schedule upgrade job

			Mob.submitUpgradeJob(this);

		} catch (Exception e) {
			PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
		}
		
		}catch(Exception e){
			Logger.error(e);
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	public void processRedeedMob(ClientConnection origin) {

		// Member variable declaration
		PlayerCharacter player;
		Contract contract;
		CharacterItemManager itemMan;
		ItemBase itemBase;
		Item item;

		this.lock.writeLock().lock();
		
		try{
			
			player = SessionManager.getPlayerCharacter(origin);
			itemMan = player.getCharItemManager();
			

			contract = this.getContract();

			if (!player.getCharItemManager().hasRoomInventory((short)1)){
				ErrorPopupMsg.sendErrorPopup(player, 21);
				return;
			}


			if (!building.getHirelings().containsKey(this))
				return;

			if (!this.remove(building)) {
				PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
				return;
			}

			building.getHirelings().remove(this);

			itemBase = ItemBase.getItemBase(contract.getContractID());

			if (itemBase == null) {
				Logger.error( "Could not find Contract for npc: " + this.getObjectUUID());
				return;
			}

			boolean itemWorked = false;

			item = new Item( itemBase, player.getObjectUUID(), OwnerType.PlayerCharacter, (byte) ((byte) this.getRank() - 1), (byte) ((byte) this.getRank() - 1),
					(short) 1, (short) 1, true, false, Enum.ItemContainerType.INVENTORY, (byte) 0,
                    new ArrayList<>(),"");
			item.setNumOfItems(1);
			item.containerType = Enum.ItemContainerType.INVENTORY;

			try {
				item = DbManager.ItemQueries.ADD_ITEM(item);
				itemWorked = true;
			} catch (Exception e) {
				Logger.error(e);
			}
			if (itemWorked) {
				itemMan.addItemToInventory(item);
				itemMan.updateInventory();
			}

			ManageCityAssetsMsg mca = new ManageCityAssetsMsg();
			mca.actionType = NPC.SVR_CLOSE_WINDOW;
			mca.setTargetType(building.getObjectType().ordinal());
			mca.setTargetID(building.getObjectUUID());
			origin.sendMsg(mca);
		

		}catch(Exception e){
			Logger.error(e);
		}finally{
			this.lock.writeLock().unlock();
		}

	}
	public void dismiss() {

		if (this.isPet()) {

			if (this.isSummonedPet()) { //delete summoned pet

				WorldGrid.RemoveWorldObject(this);
				DbManager.removeFromCache(this);
				if (this.getObjectType() == GameObjectType.Mob){
					((Mob)this).setState(STATE.Disabled);
					if (((Mob)this).getParentZone() != null)
						((Mob)this).getParentZone().zoneMobSet.remove(this);
				}

			} else { //revert charmed pet
				this.setMob();
				this.setCombatTarget(null);
				//				if (this.isAlive())
				//					WorldServer.updateObject(this);
			}
			//clear owner
			PlayerCharacter owner = this.getOwner();

			//close pet window
			if (owner != null) {
				Mob pet = owner.getPet();
				PetMsg pm = new PetMsg(5, null);
				Dispatch dispatch = Dispatch.borrow(owner, pm);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

				if (pet != null && pet.getObjectUUID() == this.getObjectUUID())
					owner.setPet(null);

				if (this.getObjectType().equals(GameObjectType.Mob))
					((Mob)this).setOwner(null);
			}


		}
	}
	
	public void dismissNecroPet(boolean updateOwner) {

		this.state = STATE.Disabled;

		this.combatTarget = null;
		this.hasLoot = false;

		if (this.parentZone != null)
			this.parentZone.zoneMobSet.remove(this);

		try {
			this.clearEffects();
		}catch(Exception e){
			Logger.error( e.getMessage());
		}
		this.playerAgroMap.clear();
		WorldGrid.RemoveWorldObject(this);

		DbManager.removeFromCache(this);

		// YEAH BONUS CODE!  THANKS UNNAMED ASSHOLE!
		//WorldServer.removeObject(this);
		//WorldGrid.INSTANCE.removeWorldObject(this);
		//owner.getPet().disableIntelligence();

		PlayerCharacter petOwner = this.getOwner();

		if (petOwner != null){
			((Mob)this).setOwner(null);
			petOwner.setPet(null);
			
			if (updateOwner == false)
				return;
			PetMsg petMsg = new PetMsg(5, null);
			Dispatch dispatch = Dispatch.borrow(petOwner, petMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
		}
	}
	
	

	
}
