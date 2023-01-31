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
import engine.ai.StaticMobActions;
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
import engine.net.client.msg.chat.ChatSystemMsg;
import engine.powers.EffectsBase;
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

	public int dbID; //the database ID
	public int loadID;
	protected boolean isMob;
	public MobBase mobBase;

	//mob specific

	public float spawnRadius;
	public int spawnTime;

	//used by static mobs
	protected int parentZoneID;
	public Zone parentZone;
	public float statLat;
	public float statLon;
	public float statAlt;
	public Building building;
	public Contract contract;
	private static ReentrantReadWriteLock createLock = new ReentrantReadWriteLock();
	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	// Variables NOT to be stored in db
	public static int staticID = 0;
	public int currentID;
	public int ownerUID = 0; //only used by pets
	public boolean hasLoot = false;
	public static ConcurrentHashMap<Integer, Mob> mobMapByDBID = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	public AbstractWorldObject fearedObject = null;
	private int buildingID;
	public boolean isSiege = false;
	public boolean isPlayerGuard = false;
	public long timeToSpawnSiege;
	public AbstractCharacter npcOwner;
	public Vector3fImmutable inBuildingLoc = null;
	public final ConcurrentHashMap<Integer, Boolean> playerAgroMap = new ConcurrentHashMap<>();
	public boolean noAggro = false;
	public STATE state = STATE.Disabled;
	public int aggroTargetID = 0;
	public boolean walkingHome = true;
	public long lastAttackTime = 0;
	public long deathTime = 0;
	public ConcurrentHashMap<Mob, Integer> siegeMinionMap = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	public ReentrantReadWriteLock minionLock = new ReentrantReadWriteLock();
	public int patrolPointIndex = 0;
	public int lastMobPowerToken = 0;
	public HashMap<Integer, MobEquipment> equip = null;
	public String nameOverride = "";
	private Regions lastRegion = null;
	public long despawnTime = 0;
	public DeferredPowerJob weaponPower;
	public DateTime upgradeDateTime = null;
	public boolean lootSync = false;
	public int fidalityID = 0;
	public int equipmentSetID = 0;
	public int lootSet = 0;
	private boolean isGuard;
	public ArrayList<Integer> fidelityRunes = null;
	
	public boolean despawned = false;
	public Vector3fImmutable destination = Vector3fImmutable.ZERO;
	public Vector3fImmutable localLoc = Vector3fImmutable.ZERO;
	public HashMap<Integer,Integer> mobPowers;
	//No ID Constructor
	public Mob(String firstName, String lastName, short statStrCurrent, short statDexCurrent, short statConCurrent, short statIntCurrent, short statSpiCurrent, short level, int exp, boolean sit, boolean walk, boolean combat, Vector3fImmutable bindLoc, Vector3fImmutable currentLoc, Vector3fImmutable faceDir, short healthCurrent, short manaCurrent, short stamCurrent, Guild guild, byte runningTrains, int npcType, boolean isMob, Zone parent,Building building, int contractID) {super( firstName, lastName, statStrCurrent, statDexCurrent, statConCurrent, statIntCurrent, statSpiCurrent, level, exp, sit, walk, combat, bindLoc, currentLoc, faceDir, healthCurrent, manaCurrent, stamCurrent, guild, runningTrains);
		
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
		StaticMobActions.clearStatic(this);
	}
	//Normal Constructor
	public Mob(String firstName, String lastName, short statStrCurrent, short statDexCurrent, short statConCurrent, short statIntCurrent, short statSpiCurrent, short level, int exp, boolean sit, boolean walk, boolean combat, Vector3fImmutable bindLoc, Vector3fImmutable currentLoc, Vector3fImmutable faceDir, short healthCurrent, short manaCurrent, short stamCurrent, Guild guild, byte runningTrains, int npcType, boolean isMob, Zone parent, int newUUID, Building building, int contractID) {super( firstName, lastName, statStrCurrent, statDexCurrent, statConCurrent, statIntCurrent, statSpiCurrent, level, exp, sit, walk, combat, bindLoc, currentLoc, faceDir, healthCurrent, manaCurrent, stamCurrent, guild, runningTrains, newUUID);
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
		StaticMobActions.initializeMob(this,false,false,false);
		StaticMobActions.clearStatic(this);
	}
	//Pet Constructor
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
		StaticMobActions.initializeMob(this,true,false,false);
		StaticMobActions.clearStatic(this);
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
		StaticMobActions.initializeMob(this,false,true, isPlayerGuard);
		StaticMobActions.clearStatic(this);
	}
	//Result Set Constructor
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
				StaticMobActions.submitUpgradeJob(this);

			this.mobBase = MobBase.getMobBase(loadID);
			
			this.setObjectTypeMask(MBServerStatics.MASK_MOB | this.mobBase.getTypeMasks());

			if (this.mobBase != null && this.spawnTime == 0)
				this.spawnTime = this.mobBase.getSpawnTime();
			
			this.bindLoc = new Vector3fImmutable(this.statLat, this.statAlt,this.statLon);

			this.parentZone = ZoneManager.getZoneByUUID(this.parentZoneID);
			

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
			StaticMobActions.initializeMob(this,false,false,this.isPlayerGuard);
		} catch(Exception e){
			Logger.error(e);
		}

	}
	@Override
	public int getDBID() {
		return this.dbID;
	}
	@Override
	public int getObjectUUID() {
		return currentID;
	}
	@Override
	public MobBase getMobBase() {
		return this.mobBase;
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
	@Override
	public AbstractWorldObject getFearedObject() {
		return this.fearedObject;
	}
	@Override
	public Vector3fImmutable getBindLoc() {

		if(this.isPet() && !this.isSiege)
			return this.getOwner() != null? this.getOwner().getLoc() : this.getLoc();
			return this.bindLoc;
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
	@Override
	public void killCharacter(AbstractCharacter attacker) {

		
		this.stopMovement(this.getMovementLoc());

		if (attacker != null){

			if (attacker.getObjectType() == GameObjectType.PlayerCharacter) {
				Group g = GroupManager.getGroup((PlayerCharacter) attacker);

				// Give XP, now handled inside the Experience Object
				if (!this.isPet() && !this.mobBase.isNecroPet() && !this.isSummonedPet() && !this.isPlayerGuard)
					Experience.doExperience((PlayerCharacter) attacker, this, g);
			} else if (attacker.getObjectType().equals(GameObjectType.Mob)){
				Mob mobAttacker = (Mob)attacker;

				if (mobAttacker.isPet()){

					PlayerCharacter owner = mobAttacker.getOwner();

					if (owner != null){

						if (!this.isPet() && !this.mobBase.isNecroPet() && !this.isSummonedPet() && !this.isPlayerGuard){
							Group g = GroupManager.getGroup(owner);

							// Give XP, now handled inside the Experience Object
							Experience.doExperience(owner, this, g);
						}

					}
				}
			}
		}
		StaticMobActions.killCleanup(this);
	}
	@Override
	public void killCharacter(String reason) {
		StaticMobActions.killCleanup(this);
	}
	@Override
	public boolean canBeLooted() {
		return !this.isAlive();
	}
	public static Mob createMob(int loadID, Vector3fImmutable spawn, Guild guild, boolean isMob, Zone parent,Building building, int contractID) {

		Mob mobWithoutID = new Mob("", "", (short) 0, (short) 0, (short) 0, (short) 0,
				(short) 0, (short) 1, 0, false, false, false, spawn, spawn, Vector3fImmutable.ZERO,
				(short) 1, (short) 1, (short) 1, guild, (byte) 0, loadID, isMob, parent,building,contractID);

		if (parent != null) {
			StaticMobActions.setRelPos(mobWithoutID,parent, spawn.x - parent.absX, spawn.y - parent.absY, spawn.z - parent.absZ);
		}


		if (mobWithoutID.mobBase == null) {
			return null;
		}
		Mob mob;
		try {
			mob = DbManager.MobQueries.ADD_MOB(mobWithoutID, isMob);
			mob.setObjectTypeMask(MBServerStatics.MASK_MOB | mob.mobBase.getTypeMasks());
			mob.setMob();
			mob.parentZone = parent;
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
				StaticMobActions.setRelPos(mob,parent, loc.x - parent.absX, loc.y - parent.absY, loc.z - parent.absZ);
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
	@Override
	public void updateDatabase() {
		//		DbManager.MobQueries.updateDatabase(this);
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
		mobPowers = DbManager.MobBaseQueries.LOAD_STATIC_POWERS(this.mobBase.getObjectUUID());
		if (this.equip == null) {
			Logger.error("Null equipset returned for uuid " + currentID);
			this.equip = new HashMap<>(0);
		}

		try{
			StaticMobActions.initializeStaticEffects(this);

			try {
				StaticMobActions.initializeSkills(this);
			} catch (Exception e) {
				Logger.error( e.getMessage());
			}

			StaticMobActions.recalculateStats(this);
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
			StaticMobActions.setRelPos(mob,parent, loc.x - parent.absX, loc.y - parent.absY, loc.z - parent.absZ);

		mob.setObjectTypeMask(MBServerStatics.MASK_MOB | mob.mobBase.getTypeMasks());

		// mob.setMob();
		mob.isPlayerGuard = true;
		mob.parentZone = parent;
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
		StaticMobActions.setInBuildingLoc(mob,this.building, this);
		mob.setBindLoc(loc.add(mob.inBuildingLoc));
		mob.deathTime = System.currentTimeMillis();
		mob.spawnTime = 900;
		mob.npcOwner = this;
		mob.state = STATE.Respawn;

		return mob;
	}
}