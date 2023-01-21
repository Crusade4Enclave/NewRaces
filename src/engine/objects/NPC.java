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
import engine.InterestManagement.WorldGrid;
import engine.ai.MobileFSM.STATE;
import engine.exception.SerializationException;
import engine.gameManager.*;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.UpgradeNPCJob;
import engine.math.Bounds;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.net.ByteBufferWriter;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.server.MBServerStatics;
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
import static engine.objects.MobBase.loadEquipmentSet;

public class NPC extends AbstractCharacter {

	//This is called every 10 minutes to remove items from static npc inventory to make room to buy more.
	private static int NUM_ITEMS_TO_JUNK = 30;

	// Used for thread safety
	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	protected int loadID;
	protected boolean isMob;
	protected MobBase mobBase;
	protected String name;
	protected Building building;
	protected Contract contract;
	protected int dbID;
	protected int currentID;
	private DateTime upgradeDateTime = null;

	//used by static npcs
	protected Zone parentZone;
	protected float statLat;
	protected float statLon;
	protected float statAlt;
	protected float sellPercent; //also train percent
	protected float buyPercent;
	protected int vendorID;
	protected ArrayList<Integer> modTypeTable;
	protected ArrayList<Integer> modSuffixTable;
	protected ArrayList<Byte> itemModTable;
	protected int symbol;
	public static int SVR_CLOSE_WINDOW = 4;

	public static ArrayList<Integer>Oprhans = new ArrayList<>();

	// Variables NOT to be stored in db
	protected boolean isStatic = false;
	private ArrayList<MobLoot> rolling = new ArrayList<>();
	private ArrayList<Mob> siegeMinions = new ArrayList<>();
	private ConcurrentHashMap<Mob, Integer> siegeMinionMap = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private HashSet<Integer> canRoll = null;
	public static HashMap<Integer, ArrayList<String>> _pirateNames = new HashMap<>();

	public ReentrantReadWriteLock minionLock = new ReentrantReadWriteLock();

	private int parentZoneID;

	public ArrayList<ProducedItem> forgedItems = new ArrayList<>();
	private int fidalityID;
	private int buildingLevel;
	private int buildingFloor;
	public HashMap<Integer, MobEquipment> equip = null;
	private String nameOverride = "";
	private int equipmentSetID = 0;
	private int slot;
	private Regions region = null;

	public Vector3fImmutable inBuildingLoc = Vector3fImmutable.ZERO;
	private int repairCost = 5;

	public int classID = 0;
	public int professionID = 0;
	public int extraRune = 0;
	public int extraRune2 = 0;


	/**
	 * No Id Constructor
	 */
	public NPC( String name, short statStrCurrent, short statDexCurrent, short statConCurrent,
			short statIntCurrent, short statSpiCurrent, short level, int exp, boolean sit, boolean walk, boolean combat, Vector3fImmutable bindLoc,
			Vector3fImmutable currentLoc, Vector3fImmutable faceDir, short healthCurrent, short manaCurrent, short stamCurrent, Guild guild,
			byte runningTrains, int npcType, boolean isMob, Building building, int contractID, Zone parent) {
		super(name, "", statStrCurrent, statDexCurrent, statConCurrent, statIntCurrent, statSpiCurrent, level, exp,
				bindLoc, currentLoc, faceDir, guild, runningTrains);
		this.loadID = npcType;
		this.isMob = isMob;
		this.contract = DbManager.ContractQueries.GET_CONTRACT(contractID);

		if (this.contract != null)
			this.mobBase = MobBase.getMobBase(this.contract.getMobbaseID());
		else
			this.mobBase = MobBase.getMobBase(loadID);

		this.name = name;
		this.buyPercent = 0.33f;
		this.sellPercent = 1f;
		this.building = building;

		this.parentZone = parent;

		initializeMob();
		clearStatic();

		this.dbID = MBServerStatics.NO_DB_ROW_ASSIGNED_YET;
		this.currentID = MBServerStatics.NO_DB_ROW_ASSIGNED_YET;
	}

	/**
	 * Normal Constructor
	 */
	public NPC( String name, short statStrCurrent, short statDexCurrent, short statConCurrent,
			short statIntCurrent, short statSpiCurrent, short level, int exp, boolean sit, boolean walk, boolean combat, Vector3fImmutable bindLoc,
			Vector3fImmutable currentLoc, Vector3fImmutable faceDir, short healthCurrent, short manaCurrent, short stamCurrent, Guild guild,
			byte runningTrains, int npcType, boolean isMob, Building building, int contractID, Zone parent, int newUUID) {
		super( name, "", statStrCurrent, statDexCurrent, statConCurrent, statIntCurrent, statSpiCurrent, level, exp,
				bindLoc, currentLoc, faceDir, guild, runningTrains, newUUID);
		this.loadID = npcType;
		this.isMob = isMob;

		if (this.contract != null)
			this.mobBase = MobBase.getMobBase(this.contract.getMobbaseID());
		else
			this.mobBase = MobBase.getMobBase(loadID);

		this.building = building;
		this.name = name;
		this.buyPercent = 0.33f;
		this.sellPercent = 1f;

		this.parentZone = parent;
		this.dbID = newUUID;
		this.currentID = newUUID;

		initializeMob();
		clearStatic();
	}

	/**
	 * ResultSet Constructor
	 */
	public NPC(ResultSet rs) throws SQLException {

		super(rs);

		java.util.Date sqlDateTime;

		try{
			this.dbID = rs.getInt(1);
			this.currentID = this.dbID;
			this.setObjectTypeMask(MBServerStatics.MASK_NPC);
			int contractID = rs.getInt("npc_contractID");
			this.parentZoneID = rs.getInt("parent");

			this.gridObjectType = GridObjectType.STATIC;
			this.contract = DbManager.ContractQueries.GET_CONTRACT(contractID);
			this.fidalityID = (rs.getInt("fidalityID"));
			this.equipmentSetID = rs.getInt("equipmentSet");

			if (this.equipmentSetID == 0 && this.contract != null)
				this.equipmentSetID = this.contract.equipmentSet;

			if (this.contract != null)
				this.loadID = this.contract.getMobbaseID();
			else
				this.loadID = 2011; //default to human

			int mobBaseOverride = rs.getInt("npc_raceID");

			if ((this.fidalityID != 0) || (mobBaseOverride != 0))
				this.loadID = mobBaseOverride;

			this.mobBase = MobBase.getMobBase(this.loadID);
			this.level = rs.getByte("npc_level");
			this.isMob = false;
			int buildingID = rs.getInt("npc_buildingID");

			try{
				this.building = BuildingManager.getBuilding(buildingID);

				if (this.building != null)
					this.building.fidelityNpcs.put(currentID, this.building.fidelityNpcs.size() + 1);

			}catch(Exception e){
				this.building = null;
				Logger.error( e.getMessage());
			}

			this.name = rs.getString("npc_name");
			this.buyPercent = rs.getFloat("npc_buyPercent");

			if (this.buyPercent == 1)
				this.buyPercent = .33f;

			this.buyPercent = .33f;
			this.sellPercent = 1;

			this.setRot(new Vector3f(0, rs.getFloat("npc_rotation"), 0));

			this.statLat = rs.getFloat("npc_spawnX");
			this.statAlt = rs.getFloat("npc_spawnY");
			this.statLon = rs.getFloat("npc_spawnZ");

			this.slot = rs.getInt("npc_slot");

			if (this.contract != null) {
				this.symbol = this.contract.getIconID();
				this.modTypeTable = this.contract.getNPCModTypeTable();
				this.modSuffixTable = this.contract.getNpcModSuffixTable();
				this.itemModTable = this.contract.getItemModTable();
				int VID = this.contract.getVendorID();

				if (VID != 0)
					this.vendorID = VID;
				else
					this.vendorID = 1; //no vendor items
			}

			int guildID = rs.getInt("npc_guildID");

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

			if (guildID != 0 && (this.guild == null || this.guild.isEmptyGuild()))
				NPC.Oprhans.add(currentID);
			else if(this.building == null && buildingID > 0) {
				NPC.Oprhans.add(currentID);
			}

			if (this.guild == null)
				this.guild = Guild.getErrantGuild();

			// Set upgrade date JodaTime DateTime object
			// if one exists in the database.

			sqlDateTime = rs.getTimestamp("upgradeDate");

			if (sqlDateTime != null)
				upgradeDateTime = new DateTime(sqlDateTime);
			else
				upgradeDateTime = null;

			// Submit upgrade job if NPC is currently set to rank.

			if (this.upgradeDateTime != null)
				submitUpgradeJob();
			this.buildingFloor = (rs.getInt("npc_buildingFloor"));
			this.buildingLevel = (rs.getInt("npc_buildingLevel"));
			this.setParentZone(ZoneManager.getZoneByUUID(this.parentZoneID));


			if (this.fidalityID != 0)
				this.nameOverride = rs.getString("npc_name");

		}catch(Exception e){
			Logger.error(e);
			e.printStackTrace();
		}

		try{
			initializeMob();
		}catch(Exception e){
			Logger.error( e.toString());
		}

	}

	//This method restarts an upgrade timer when a building is loaded from the database.
	// Submit upgrade job for this building based upon it's current upgradeDateTime

	public final void submitUpgradeJob() {

		JobContainer jc;

		if (this.getUpgradeDateTime() == null) {
			Logger.error("Attempt to submit upgrade job for non-ranking NPC");
			return;
		}

		// Submit upgrade job for future date or current instant

		if (this.getUpgradeDateTime().isAfter(DateTime.now()))
			jc = JobScheduler.getInstance().scheduleJob(new UpgradeNPCJob(this),
					this.getUpgradeDateTime().getMillis());
		else
			JobScheduler.getInstance().scheduleJob(new UpgradeNPCJob(this), 0);

	}

	public void setRank(int newRank) {

		DbManager.NPCQueries.SET_PROPERTY(this, "npc_level", newRank);
		this.level = (short) newRank;
	}

	public int getDBID() {
		return this.dbID;
	}

	@Override
	public int getObjectUUID() {
		return currentID;
	}

	private void clearStatic() {
		this.parentZone = null;
		this.statLat = 0f;
		this.statLon = 0f;
		this.statAlt = 0f;
	}

	private void initializeMob() {

		if (this.mobBase != null) {
			this.healthMax = this.mobBase.getHealthMax();
			this.manaMax = 0;
			this.staminaMax = 0;
			this.setHealth(this.healthMax);
			this.mana.set(this.manaMax);
			this.stamina.set(this.staminaMax);
			//this.firstName = this.minionMobBase.getFirstName();
			//this.lastName = "";
			//this.level = (short)(this.minionMobBase.getLevel());
		}

		//add this npc to building
		if (this.building != null && this.loadID != 0 && this.fidalityID == 0) {

			if (building.getBlueprint() != null){

				int maxSlots;
				maxSlots = building.getBlueprint().getSlotsForRank(this.building.getRank());

				for (int slot = 1; slot < maxSlots + 1; slot++) {

					if (!this.building.getHirelings().containsValue(slot)) {
						this.building.getHirelings().put(this, slot);
						break;
					}
				}
				//npc created by an admin without a blueprint, just default max slot size to 10.
			}else{

				int maxSlots = 10;

				for (int slot = 1; slot < maxSlots + 1; slot++) {
					if (!this.building.getHirelings().containsValue(slot)) {
						this.building.getHirelings().put(this, slot);
						break;
					}
				}
			}
		}

		//TODO set these correctly later
		this.rangeHandOne = 8;
		this.rangeHandTwo = -1;
		this.minDamageHandOne = 1;
		this.maxDamageHandOne = 4;
		this.minDamageHandTwo = 1;
		this.maxDamageHandTwo = 4;
		this.atrHandOne = 300;
		this.atrHandOne = 300;
		this.defenseRating = 200;
		this.isActive = true;

		this.charItemManager.load();
	}

	public static NPC getFromCache(int id) {
		return (NPC) DbManager.getFromCache(GameObjectType.NPC, id);
	}

	/*
	 * Getters
	 */
	public int getLoadID() {
		return loadID;
	}

	public boolean isMob() {
		return this.isMob;
	}

	public MobBase getMobBase() {
		return this.mobBase;
	}

	public Contract getContract() {
		return this.contract;
	}

	public int getContractID() {

		if (this.contract != null)
			return this.contract.getObjectUUID();

		return 0;
	}

	public boolean isStatic() {
		return this.isStatic;
	}

	public Building getBuilding() {
		return this.building;
	}

	public void setName(String value) {
		this.name = value;
	}

	public static boolean UpdateName(NPC npc, String value) {

		if (!DbManager.NPCQueries.UPDATE_NAME(npc, value))
			return false;

		npc.name = value;
		return true;

	}

	public void setBuilding(Building value) {
		this.building = value;
	}

	@Override
	public String getFirstName() {
		return this.name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getLastName() {
		return "";
	}

	@Override
	public Vector3fImmutable getBindLoc() {
		return this.bindLoc;
	}

	@Override
	public int getGuildUUID() {

		if (this.guild == null)
			return 0;

		return this.guild.getObjectUUID();
	}

	/*
	 * Serialization
	 */

	public static void __serializeForClientMsg(NPC npc,ByteBufferWriter writer) throws SerializationException {
	}


	public static void serializeNpcForClientMsgOtherPlayer(NPC npc,ByteBufferWriter writer, boolean hideAsciiLastName)
			throws SerializationException {
		serializeForClientMsgOtherPlayer(npc,writer);
	}


	public static void serializeForClientMsgOtherPlayer(NPC npc,ByteBufferWriter writer)
			throws SerializationException {

		writer.putInt(0);
		writer.putInt(0);

		//num Runes
		int cnt = 3;
		boolean isVamp = false, isHealer = false, isArcher = false, isTrainer = false;
		int contractID = 0, classID = 0;
		int extraRune = 0;

			if (npc.contract != null) {
				contractID = npc.contract.getContractID();
				classID = npc.contract.getClassID();
				extraRune = npc.contract.getExtraRune();
				
				if (extraRune == contractID)
					extraRune = 0;

			}

		if ((contractID > 252642 && contractID < 252647) || contractID == 252652) {
			isVamp = true;
			cnt++;
		}

		if (contractID == 252582 || contractID == 252579 || contractID == 252581
				|| contractID == 252584 || contractID == 252597 || contractID == 252598
				|| contractID == 252628 || extraRune == 252582 || extraRune == 252579
				|| extraRune == 252581 || extraRune == 252584 || extraRune == 252597
				|| extraRune == 252598 || extraRune == 252628) {
			isHealer = true;
			cnt++;
		}

		if (contractID == 252570) {
			isArcher = true;
			cnt++;
		}

		if (classID != 0)
			cnt++;
		
				if (extraRune != 0 && extraRune != contractID) {
					cnt++;
				}


		writer.putInt(cnt);

		//Race
		writer.putInt(1);
		writer.putInt(0);

		if (npc.mobBase != null)
			writer.putInt(npc.mobBase.getLoadID());
		else
			writer.putInt(2011);

		writer.putInt(GameObjectType.NPCRaceRune.ordinal());
		writer.putInt(npc.currentID);

		//Class/Trainer/Whatever
		writer.putInt(5);
		writer.putInt(0);

		if (npc.contract != null) {
			writer.putInt(contractID);
		}else
			writer.putInt(2500);

		writer.putInt(GameObjectType.NPCClassRune.ordinal());
		writer.putInt(npc.currentID);

		//vampire trainer
		cnt = 0;
		
		if (extraRune != 0)
			cnt = serializeExtraRune(npc,extraRune, cnt, writer);
		if (isVamp)
			cnt = serializeExtraRune(npc,252647, cnt, writer);

		//Healer trainer
		if (isHealer) {
			//			int healerRune = 2501;
			//			if (npc.getLevel() >= 60)
			//healerRune = 252592;
			cnt = serializeExtraRune(npc,252592, cnt, writer);
		}

		if (classID != 0) {
				writer.putInt(4);
			writer.putInt(0);
			writer.putInt(classID);
			writer.putInt(GameObjectType.NPCExtraRune.ordinal());
			writer.putInt(npc.currentID);
		}

		//Scout trainer
		if (isArcher) {
			cnt = serializeExtraRune(npc,252654, cnt, writer);
		}

		//		if (extraRune != 0 && extraRune != contractID) {
		//			writer.putInt(3);
		//			writer.putInt(0);
		//			writer.putInt(extraRune);
		//			writer.putInt(GameObjectType.NPCExtraRune.ordinal());
		//			writer.putInt(npc.getObjectUUID());
		//		}

		//Shopkeeper
		writer.putInt(5);
		writer.putInt(0);
		writer.putInt(0x3DACC);
		writer.putInt(GameObjectType.NPCShopkeeperRune.ordinal());
		writer.putInt(npc.currentID);

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

		if (!npc.nameOverride.isEmpty()){
			writer.putString(npc.nameOverride);
			writer.putInt(0);
		}else
			if (npc.contract != null) {

				if (npc.contract.isTrainer()) {
					writer.putString(npc.name + ", " + npc.contract.getName());
					writer.putString("");
				} else {
					writer.putString(npc.name);
					writer.putString(npc.contract.getName());
				}
			} else {
				writer.putString(npc.name);
				writer.putString("");
			}

		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		//writer.putInt(0); wHy do i see 4 in recording?

		writer.put((byte) 0);
		writer.putInt(npc.getObjectType().ordinal());
		writer.putInt(npc.currentID);
		//		writer.putLong(npc.getCompositeID());

		writer.putFloat(1.0f);
		writer.putFloat(1.0f);
		writer.putFloat(1.0f);

		if (npc.region != null){
			writer.putVector3f(npc.inBuildingLoc);
		}else{
			writer.putFloat(npc.getLoc().getX());
			writer.putFloat(npc.getLoc().getY());
			writer.putFloat(npc.getLoc().getZ());
		}

		//Rotation
		float radians = (float) Math.asin(npc.getRot().y) * 2;

		if (npc.building != null)
			if (npc.building.getBounds() != null && npc.building.getBounds().getQuaternion() != null)
				radians += (npc.building.getBounds().getQuaternion()).angleY;

		writer.putFloat(radians);

		//Running Speed
		writer.putInt(0);

		// get a copy of the equipped items.

		if (npc.equip != null){
			writer.putInt(npc.equip.size());

			for (MobEquipment me: npc.equip.values())
				MobEquipment.serializeForClientMsg(me,writer);
		}else
			writer.putInt(0);

		writer.putInt((npc.level / 10));
		writer.putInt(npc.level);
		writer.putInt(npc.getIsSittingAsInt()); //Standing
		writer.putInt(npc.getIsWalkingAsInt()); //Walking
		writer.putInt(npc.getIsCombatAsInt()); //Combat
		writer.putInt(2); //Unknown
		writer.putInt(1); //Unknown - Headlights?
		writer.putInt(0);

		if (npc.building != null && npc.region != null){
			writer.putInt(npc.building.getObjectType().ordinal());
			writer.putInt(npc.building.getObjectUUID());
		}else{
			writer.putInt(0); //<-Building Object Type
			writer.putInt(0); //<-Building Object ID
		}
		writer.put((byte) 0);
		writer.put((byte) 0);
		writer.put((byte) 0);

		//npc dialog menus from contracts

		if (npc.contract != null) {
			ArrayList<Integer> npcMenuOptions = npc.contract.getNPCMenuOptions();
			writer.putInt(npcMenuOptions.size());
			for (Integer val : npcMenuOptions) {
				writer.putInt(val);
			}

		} else {
			writer.putInt(0);
		}

		writer.put((byte) 1);

		if (npc.building != null) {
			writer.putInt(GameObjectType.StrongBox.ordinal());
			writer.putInt(npc.currentID);
			writer.putInt(GameObjectType.StrongBox.ordinal());
			writer.putInt(npc.building.getObjectUUID());
		} else {
			writer.putLong(0);
			writer.putLong(0);
		}

		if (npc.contract != null) {
			writer.putInt(npc.contract.getIconID());
		} else {
			writer.putInt(0); //npc icon ID
		}
		//		for (int i=0;i<5;i++)
		//			writer.putInt(0);
		writer.putInt(0);
		writer.putShort((short)0);

		
		if (npc.contract != null && npc.contract.isTrainer()) {
			writer.putInt(classID);
		} else {
			writer.putInt(0);
		}

		if (npc.contract != null && npc.contract.isTrainer()) {
			writer.putInt(classID);
		} else {
			writer.putInt(0);
		}
		writer.putInt(0);
		writer.putInt(0);

		writer.putFloat(4);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.put((byte) 0);

		//Pull guild info from building if linked to one



		Guild.serializeForClientMsg(npc.guild,writer, null, true);


		writer.putInt(1);
		writer.putInt(0x8A2E);
		//		writer.putInt((npc.contract != null) ? npc.contract.getContractID() : 0x8A2E);
		writer.putInt(0);
		writer.putInt(0);

		//TODO Guard
		writer.put((byte) 0); //Is guard..

		writer.putFloat(1500f); //npc.healthMax
		writer.putFloat(1500f); //npc.health

		//TODO Peace Zone
		writer.put((byte) 1); //0=show tags, 1=don't
		writer.putInt(0);
		writer.put((byte) 0);
	}

	public void removeMinions() {

		for (Mob toRemove : this.siegeMinionMap.keySet()) {

			toRemove.setState(STATE.Disabled);

			try {
				toRemove.clearEffects();
			}catch(Exception e){
				Logger.error( e.getMessage());
			}

			if (toRemove.getParentZone() != null)
				toRemove.getParentZone().zoneMobSet.remove(toRemove);

			WorldGrid.RemoveWorldObject(toRemove);
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

	private static int serializeExtraRune(NPC npc,int runeID, int cnt, ByteBufferWriter writer) {
		writer.putInt(5);
		writer.putInt(0);
		writer.putInt(runeID);
		if (cnt == 0) {
			writer.putInt(GameObjectType.NPCClassRuneTwo.ordinal());
		} else {
			writer.putInt(GameObjectType.NPCClassRuneThree.ordinal());
		}
		writer.putInt(npc.currentID);
		return cnt + 1;
	}


	@Override
	public float getSpeed() {
		if (this.isWalk()) {
			return MBServerStatics.WALKSPEED;
		} else {
			return MBServerStatics.RUNSPEED;
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
		//TODO Handle Death
		killCleanup();

		//TODO Send death message if needed
	}

	@Override
	public void killCharacter(String reason) {
		//TODO Handle Death
		killCleanup();

		//Orphan inventory so it can be looted
		//if (!this.inSafeZone)
		if (this.charItemManager != null) {
			this.charItemManager.orphanInventory();
		}

		//TODO Send death message if needed
		//Question? How would a mob die to water?
	}

	private void killCleanup() {
		//TODO handle cleanup from death here

		//set so character won't load
		this.load = false;

		//Create Corpse and add to world
		//Corpse.makeCorpse(this);
		//TODO damage equipped items
		//TODO cleanup any timers
	}

	public Zone getParentZone() {
		return this.parentZone;
	}

	public int getParentZoneID() {
		if (this.parentZone != null) {
			return this.parentZone.getObjectUUID();
		}
		return 0;
	}

	public void setParentZone(Zone zone) {
		if (ConfigManager.serverType.equals(ServerType.LOGINSERVER)) {
			return;
		}

		if (this.contract == null)
			return;
		//update ZoneManager's zone building list
		if (zone != null) {
			if (this.parentZone != null) {
				if (zone.getObjectUUID() != this.parentZone.getObjectUUID()) {
					this.parentZone.zoneNPCSet.remove(this);
					zone.zoneNPCSet.add(this);
				}
			} else {
				zone.zoneNPCSet.add(this);
			}
		} else if (this.parentZone != null) {
			this.parentZone.zoneNPCSet.remove(this);
		}

		if (this.parentZone == null)
			this.parentZone = zone;

		if (!this.parentZone.isPlayerCity())
			this.sellPercent = 1;


		if (this.building != null){

			BuildingModelBase buildingModel = BuildingModelBase.getModelBase(this.building.getMeshUUID());

			Vector3fImmutable slotLocation = Vector3fImmutable.ZERO;

			if (buildingModel != null){


				int putSlot;
				BuildingLocation buildingLocation = null;

				//-1 slot means no slot available in building.
				if (this.slot != -1)
					putSlot = this.slot;
				else
					putSlot = NPC.getBuildingSlot(this);


				buildingLocation = buildingModel.getSlotLocation(putSlot);

				if (buildingLocation != null){
					slotLocation = buildingLocation.getLoc();
					this.setRot(new Vector3f(buildingLocation.getRot()));
				}

				else{
					//only log if npc has been loaded in building.
					if (this.building.getHirelings().containsKey(this) && putSlot != -1) {
						Logger.error("could not slot npc : " + currentID + " in slot " + putSlot + " for building Mesh " + this.building.getMeshUUID());
					}
				}
			}


			Vector3fImmutable buildingWorldLoc = ZoneManager.convertLocalToWorld(this.building, slotLocation);

			//Set floor and level here after building World Location.

			this.region = BuildingManager.GetRegion(this.building, buildingWorldLoc.x, buildingWorldLoc.y, buildingWorldLoc.z);


			if (this.region != null){


				this.buildingFloor = region.getRoom();
				this.buildingLevel = region.getLevel();
				this.inBuildingLoc = ZoneManager.convertWorldToLocal(building, this.getLoc());

			}else{
				this.buildingFloor = -1;
				this.buildingLevel = -1;
			}
			this.setBindLoc(new Vector3fImmutable(buildingWorldLoc.x, buildingWorldLoc.y, buildingWorldLoc.z));
			if (ConfigManager.serverType.equals(ServerType.WORLDSERVER))
				this.setLoc(new Vector3fImmutable(buildingWorldLoc.x, buildingWorldLoc.y, buildingWorldLoc.z));

		}else{
			this.setBindLoc(new Vector3fImmutable(this.statLat + zone.absX, this.statAlt + zone.absY, this.statLon + zone.absZ));
			if (ConfigManager.serverType.equals(ServerType.WORLDSERVER))
				this.setLoc(new Vector3fImmutable(this.statLat + zone.absX, this.statAlt + zone.absY, this.statLon + zone.absZ));

		}
		//create npc profits 
		if (this.parentZone != null){
			if (this.parentZone.isPlayerCity())
				if (NPC.GetNPCProfits(this) == null)
					NPCProfits.CreateProfits(this);
		}

	}



	@Override
	public  Vector3fImmutable getLoc() {

		return super.getLoc();
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

	//Sets the relative position to a parent zone
	public void setRelPos(Zone zone, float locX, float locY, float locZ) {

		//update ZoneManager's zone building list
		if (zone != null) {
			if (this.parentZone != null) {
				if (zone.getObjectUUID() != this.parentZone.getObjectUUID()) {
					this.parentZone.zoneNPCSet.remove(this);
					zone.zoneNPCSet.add(this);
				}
			} else {
				zone.zoneNPCSet.add(this);
			}
		} else if (this.parentZone != null) {
			this.parentZone.zoneNPCSet.remove(this);
		}

		this.statLat = locX;
		this.statAlt = locY;	
		this.statLon = locZ;
		this.parentZone = zone;
	}

	public float getSellPercent() {
		return this.sellPercent;
	}

	public void setSellPercent(float sellPercent) {
		this.sellPercent = sellPercent;
	}

	public float getBuyPercent() {
		return this.buyPercent;
	}
	public float getBuyPercent(PlayerCharacter player) {
		if (NPC.GetNPCProfits(this) == null || this.guild == null)
			return this.buyPercent;
		NPCProfits profits = NPC.GetNPCProfits(this);
		if (player.getGuild().equals(this.guild))
			return  profits.buyGuild;
		if (player.getGuild().getNation().equals(this.guild.getNation()))
			return profits.buyNation;

		return profits.buyNormal;		
	}

	public float getSellPercent(PlayerCharacter player) {
		if (NPC.GetNPCProfits(this) == null || this.guild == null)
			return 1 + this.sellPercent;
		NPCProfits profits = NPC.GetNPCProfits(this);
		if (player.getGuild().equals(this.guild))
			return 1 + profits.sellGuild;
		if (player.getGuild().getNation().equals(this.guild.getNation()))
			return 1 + profits.sellNation;

		return 1 + profits.sellNormal;
	}

	public void setBuyPercent(float buyPercent) {
		this.buyPercent = buyPercent;
	}

	@Override
	public boolean canBeLooted() {
		return !this.isAlive();
	}

	// *** Refactor : this has a useInit flag that can be removed

	public static NPC createNPC(String name, int contractID, Vector3fImmutable spawn, Guild guild, boolean isMob, Zone parent, short level, boolean useInit, Building building) {
		NPC npcWithoutID = new NPC(name, (short) 0, (short) 0, (short) 0, (short) 0,
				(short) 0, (short) 1, 0, false, false, false, spawn, spawn, Vector3fImmutable.ZERO,
				(short) 1, (short) 1, (short) 1, guild, (byte) 0, 0, isMob, building, contractID, parent);

		npcWithoutID.setLevel(level);
		if (parent != null) {
			npcWithoutID.setRelPos(parent, spawn.x - parent.absX, spawn.y - parent.absY, spawn.z - parent.absZ);
		}

		if (npcWithoutID.mobBase == null) {
			return null;
		}
		NPC npc;
		try {
			npc = DbManager.NPCQueries.ADD_NPC(npcWithoutID, isMob);
			npc.setObjectTypeMask(MBServerStatics.MASK_NPC);
		} catch (Exception e) {
			Logger.error( e);
			npc = null;
		}


		return npc;
	}

	public static NPC getNPC(int id) {

		if (id == 0)
			return null;
		NPC npc = (NPC) DbManager.getFromCache(GameObjectType.NPC, id);
		if (npc != null)
			return npc;

		return DbManager.NPCQueries.GET_NPC(id);
	}

	public ArrayList<Integer> getModTypeTable() {
		return this.modTypeTable;
	}

	@Override
	public void updateDatabase() {
		DbManager.NPCQueries.updateDatabase(this);
	}

	public int getSymbol() {
		return symbol;
	}

	public ArrayList<Integer> getModSuffixTable() {
		return modSuffixTable;
	}

	public ArrayList<Byte> getItemModTable() {
		return itemModTable;
	}

	public boolean isRanking() {

		return this.upgradeDateTime != null;
	}

	@Override
	public void runAfterLoad() {

		if (ConfigManager.serverType.equals(ServerType.LOGINSERVER))
			return;

		if (this.fidalityID != 0)
			DbManager.NPCQueries.LOAD_RUNES_FOR_FIDELITY_NPC(this);

		try{

			this.equip = loadEquipmentSet(this.equipmentSetID);

		}catch(Exception e){
			Logger.error(e.getMessage());
		}

		if (this.equip == null)
			this.equip = new HashMap<>();

		try{

			DbManager.NPCQueries.LOAD_ALL_ITEMS_TO_PRODUCE(this);

			for (ProducedItem producedItem : this.forgedItems){
				MobLoot ml = new MobLoot(this, ItemBase.getItemBase(producedItem.getItemBaseID()), false);

				DbManager.NPCQueries.UPDATE_ITEM_ID(producedItem.getID(), currentID, ml.getObjectUUID());

				if (producedItem.isInForge()){

					if (producedItem.getPrefix() != null && !producedItem.getPrefix().isEmpty()){
						ml.addPermanentEnchantment(producedItem.getPrefix(), 0, 0, true);
						ml.setPrefix(producedItem.getPrefix());
					}

					if (producedItem.getSuffix() != null && !producedItem.getSuffix().isEmpty()){
						ml.addPermanentEnchantment(producedItem.getSuffix(), 0, 0, false);
						ml.setSuffix(producedItem.getSuffix());
					}

					if (!producedItem.isRandom())
						ml.setIsID(true);

					ml.loadEnchantments();

					ml.setValue(producedItem.getValue());
					ml.setDateToUpgrade(producedItem.getDateToUpgrade().getMillis());
					ml.containerType = Enum.ItemContainerType.FORGE;
					this.addItemToForge(ml);
				}else{
					if (producedItem.getPrefix() != null && !producedItem.getPrefix().isEmpty()){
						ml.addPermanentEnchantment(producedItem.getPrefix(), 0, 0, true);
						ml.setPrefix(producedItem.getPrefix());
					}

					if (producedItem.getSuffix() != null && !producedItem.getSuffix().isEmpty()){
						ml.addPermanentEnchantment(producedItem.getSuffix(), 0, 0, false);
						ml.setSuffix(producedItem.getSuffix());
					}

					ml.setDateToUpgrade(producedItem.getDateToUpgrade().getMillis());
					ml.containerType = Enum.ItemContainerType.INVENTORY;
					ml.setIsID(true);

					this.charItemManager.addItemToInventory(ml);
				}
				ml.setValue(producedItem.getValue());
			}

			// Create NPC bounds object
			Bounds npcBounds = Bounds.borrow();
			npcBounds.setBounds(this.getLoc());

		}catch (Exception e){
			Logger.error( e.getMessage());
		}
	}

	public void removeFromZone() {
		this.parentZone.zoneNPCSet.remove(this);
	}

	@Override
	protected ConcurrentHashMap<Integer, CharacterPower> initializePowers() {
		return new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	}

	public DateTime getUpgradeDateTime() {
		lock.readLock().lock();
		try {
			return upgradeDateTime;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void setUpgradeDateTime(DateTime upgradeDateTime) {

		if (!DbManager.NPCQueries.updateUpgradeTime(this, upgradeDateTime)){
			Logger.error( "Failed to set upgradeTime for building " + currentID);
			return;
		}

		this.upgradeDateTime = upgradeDateTime;
	}

	public ArrayList<MobLoot> getRolling() {
		synchronized(rolling){
			return rolling;
		}
	}

	public  int getRollingCount(){
		synchronized(this.rolling){
			return rolling.size();
		}
	}

	public void addItemToForge(MobLoot item){
		synchronized(this.rolling){
			this.rolling.add(item);
		}
	}

	public void removeItemFromForge(Item item){
		synchronized(this.rolling){
			this.rolling.remove(item);
		}
	}

	public ArrayList<Building> getProtectedBuildings() {

		ArrayList<Building> protectedBuildings = new ArrayList<>();

		if (this.building == null)
			return protectedBuildings;

		if (this.building.getCity() == null)
			return protectedBuildings;

		for (Building b : this.building.getCity().getParent().zoneBuildingSet) {

			if (b.getBlueprint() == null)
				continue;

			if (b.getProtectionState().equals(ProtectionState.CONTRACT))
				protectedBuildings.add(b);

			if (b.getProtectionState().equals(ProtectionState.PENDING))
				protectedBuildings.add(b);
		}

		return protectedBuildings;
	}

	@Override
	public Guild getGuild(){
		if (this.building != null)
			return building.getGuild();
		return this.guild;
	}

	public ArrayList<Mob> getSiegeMinions() {
		return siegeMinions;
	}

	public HashSet<Integer> getCanRoll() {

		if (this.canRoll == null){
			this.canRoll = DbManager.ItemQueries.GET_ITEMS_FOR_VENDOR(this.vendorID);

			if (this.contract.getVendorID() == 102){

				for (int i = 0;i<this.getRank();i++){
					int subID = i + 1;
					this.canRoll.add(910010 + subID);
				}

				if (this.getRank() == 7)
					this.canRoll.add(910018);
			}
		}

		return this.canRoll;
	}

	public int getRollingTimeInSeconds(int itemID){

		ItemBase ib = ItemBase.getItemBase(itemID);

		if (ib == null)
			return 0;

		if (ib.getType() == ItemType.SCROLL)
			return this.getRank() * 60 * 60 * 3;

		float time;

		if (this.building == null)
			return 600;

		float rank = this.building.getRank() - 1;
		float rate = (float) (2.5 * rank);
		time = (20 - rate);
		time *= 60;
		return (int) time;
	}

	public ConcurrentHashMap<Mob, Integer> getSiegeMinionMap() {
		return siegeMinionMap;
	}

	public void setSiegeMinionMap(ConcurrentHashMap<Mob, Integer> siegeMinionMap) {
		this.siegeMinionMap = siegeMinionMap;
	}

	// Method removes the npc from the game simulation
	// and deletes it from the database.

	public boolean remove() {

		Building building;

		// Remove npc from it's building

		building = this.building;

		if (building != null) {
			building.getHirelings().remove(this);
			this.removeMinions();
		}

		// Delete npc from database

		if (DbManager.NPCQueries.DELETE_NPC(this) == 0) {
			return false;
		}

		// Remove npc from the simulation

		this.removeFromCache();
		WorldGrid.RemoveWorldObject(this);
		WorldGrid.removeObject(this);
		return true;
	}

	public static void loadAllPirateNames() {

		DbManager.NPCQueries.LOAD_PIRATE_NAMES();
	}

	public static String getPirateName(int mobBaseID) {

		ArrayList<String> nameList = null;

		// If we cannot find name for this mobbase then
		// fallback to human male

		if (_pirateNames.containsKey(mobBaseID))
			nameList = _pirateNames.get(mobBaseID);
		else
			nameList = _pirateNames.get(2111);

		if (nameList == null) {
			Logger.error("Null name list for 2111!");
		}

		return nameList.get(ThreadLocalRandom.current().nextInt(nameList.size()));

	}

	public synchronized Mob createSiegeMob(int loadID, Guild guild, Zone parent, Vector3fImmutable loc, short level) {

		MobBase minionMobBase;
		Mob mob;

		if (siegeMinionMap.size() == 3)
			return null;

		minionMobBase = MobBase.getMobBase(loadID);

		if (minionMobBase == null)
			return null;

		mob = new Mob(minionMobBase, guild, parent, level,new Vector3fImmutable(1,1,1), 0,false);
		mob.runAfterLoad();
		mob.despawned = true;
		DbManager.addToCache(mob);

		if (parent != null)
			mob.setRelPos(parent, loc.x - parent.absX, loc.y - parent.absY, loc.z - parent.absZ);

		mob.setObjectTypeMask(MBServerStatics.MASK_MOB | mob.getTypeMasks());

		// mob.setMob();
		mob.setSiege(true);
		mob.setParentZone(parent);

		int slot = 0;

		if (!siegeMinionMap.containsValue(1))
			slot = 1;
		else if (!siegeMinionMap.containsValue(2))
			slot = 2;

		siegeMinionMap.put(mob, slot);
		mob.setInBuildingLoc(this.building, this);
	
		Vector3fImmutable buildingWorldLoc = ZoneManager.convertLocalToWorld(this.building, mob.getInBuildingLoc());
		mob.setBindLoc(buildingWorldLoc);
		mob.setLoc(buildingWorldLoc);
		
		mob.setSpawnTime(10);
		mob.setNpcOwner(this);
		mob.setState(STATE.Awake);

		return mob;
	}

	public int getUpgradeCost() {

		int upgradeCost;

		upgradeCost = Integer.MAX_VALUE;

		if (this.getRank() < 7)
			return (this.getRank() * 100650) + 21450;

		return upgradeCost;
	}

	public int getUpgradeTime() {

		int upgradeTime; // expressed in hours

		upgradeTime = Integer.MAX_VALUE;

		if (this.getRank() < 7)
			return (this.getRank() * 8);

		return 0;
	}

	public static boolean ISGuardCaptain(int contractID){
		return MinionType.ContractToMinionMap.containsKey(contractID);
	}

	public synchronized Item produceItem(int playerID,int amount, boolean isRandom, int pToken, int sToken, String customName, int itemID) {

		Zone serverZone;
		City city;
		Item item = null;

		PlayerCharacter player = null;

		if (playerID != 0)
			player = SessionManager.getPlayerCharacterByID(playerID);

		try{

			if (this.getRollingCount() >= this.getRank()) {

				if (player != null)
					ChatManager.chatSystemInfo(player, this.getName() + " " + this.getContract().getName() + " slots are full");

				return null;
			}

			// Cannot roll items without a warehouse.
			// Due to the fact fillForge references the
			// warehouse and early exits.  *** Refactor???

			serverZone = this.building.getParentZone();

			if (serverZone == null)
				return null;

			city = City.GetCityFromCache(serverZone.getPlayerCityUUID());

			if (city == null) {

				if (player != null)
					ErrorPopupMsg.sendErrorMsg(player, "Could not find city."); // Production denied: This building must be protected to gain access to warehouse resources.

				return null;
			}

			if (this.building == null){

				if (player != null)
					ErrorPopupMsg.sendErrorMsg(player, "Could not find building."); // Production denied: This building must be protected to gain ac

				return null;
			}

			//TODO create Normal Items.

			if (amount == 0)
				amount = 1;

			if (isRandom)
				item = ItemFactory.randomRoll(this, player,amount, itemID);
			else
				item = ItemFactory.fillForge(this, player,amount,itemID, pToken,sToken, customName);

			if (item == null)
				return null;

			ItemProductionMsg	outMsg = new ItemProductionMsg(this.building, this, item, 8, true);
			DispatchMessage.dispatchMsgToInterestArea(this, outMsg, DispatchChannel.SECONDARY, 700, false, false);
			
		} catch(Exception e){
			Logger.error(e);
		}
		return item;
	}

	public synchronized boolean completeItem(int itemUUID) {

		MobLoot targetItem;

		try {
			targetItem = MobLoot.getFromCache(itemUUID);

			if (targetItem == null)
				return false;

			if (!this.getCharItemManager().forgeContains(targetItem, this))
				return false;

			if (!DbManager.NPCQueries.UPDATE_ITEM_TO_INVENTORY(targetItem.getObjectUUID(), currentID))
				return false;

			targetItem.setIsID(true);

			this.rolling.remove(targetItem);
			this.getCharItemManager().addItemToInventory(targetItem);

			//remove from client forge window

			ItemProductionMsg	outMsg1 = new ItemProductionMsg(this.building, this, targetItem, 9, true);
			DispatchMessage.dispatchMsgToInterestArea(this, outMsg1, DispatchChannel.SECONDARY, MBServerStatics.STRUCTURE_LOAD_RANGE, false, false);
			ItemProductionMsg outMsg = new ItemProductionMsg(this.building, this, targetItem, 10, true);
			DispatchMessage.dispatchMsgToInterestArea(this, outMsg, DispatchChannel.SECONDARY, MBServerStatics.STRUCTURE_LOAD_RANGE, false, false);

		} catch(Exception e) {
			Logger.error( e.getMessage());
		}
		return true;
	}

	public int getFidalityID() {
		return fidalityID;
	}

	public int getBuildingLevel() {
		return buildingLevel;
	}

	public int getBuildingFloor() {
		return buildingFloor;
	}

	public HashMap<Integer, MobEquipment> getEquip() {
		return equip;
	}

	public static int getBuildingSlot(NPC npc){
		int slot = -1;

		if (npc.building == null)
			return -1;



		BuildingModelBase buildingModel = BuildingModelBase.getModelBase(npc.building.getMeshUUID());

		if (buildingModel == null)
			return -1;

		if (npc.fidalityID != 0){

			if (npc.building.fidelityNpcs.get(npc.currentID) != null){
				slot = npc.building.fidelityNpcs.get(npc.currentID);
			}

		} else{
			if (npc.building.getHirelings().containsKey(npc))
				slot =  (npc.building.getHirelings().get(npc));
		}

		if (buildingModel.getNPCLocation(slot) == null)
			return -1;


		return slot;
	}

	public int getEquipmentSetID() {
		return equipmentSetID;
	}

	public static boolean UpdateSlot(NPC npc,int slot){

		if (!DbManager.NPCQueries.UPDATE_SLOT(npc, slot))
			return false;

		npc.slot = slot;
		return true;
	}

	public static boolean UpdateEquipSetID(NPC npc, int equipSetID){

		if (!EquipmentSetEntry.EquipmentSetMap.containsKey(equipSetID))
			return false;

		if (!DbManager.NPCQueries.UPDATE_EQUIPSET(npc, equipSetID))
			return false;

		npc.equipmentSetID = equipSetID;

		return true;
	}

	public static boolean UpdateRaceID(NPC npc, int raceID){

		if (!DbManager.NPCQueries.UPDATE_MOBBASE(npc, raceID))
			return false;

		npc.loadID = raceID;
		npc.mobBase = MobBase.getMobBase(npc.loadID);
		return true;
	}

	public String getNameOverride() {
		return nameOverride;
	}

	public static NPCProfits GetNPCProfits(NPC npc){
		return NPCProfits.ProfitCache.get(npc.currentID);
	}

	public int getRepairCost() {
		return repairCost;
	}

	public void setRepairCost(int repairCost) {
		this.repairCost = repairCost;
	}
	
	public void processUpgradeNPC(PlayerCharacter player) {

		int rankCost;
		Building building;
		DateTime dateToUpgrade;


		this.lock.writeLock().lock();
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

			rankCost = this.getUpgradeCost();

			// SEND NOT ENOUGH GOLD ERROR

			if (!building.hasFunds(rankCost)){
				sendErrorPopup(player, 127);
				return;
			}

			if (rankCost > building.getStrongboxValue()) {
				sendErrorPopup(player, 127);
				return;
			}

			try {

				if (!building.transferGold(-rankCost,false))
					return;

				dateToUpgrade = DateTime.now().plusHours(this.getUpgradeTime());

				this.setUpgradeDateTime(dateToUpgrade);

				// Schedule upgrade job

				this.submitUpgradeJob();

			} catch (Exception e) {
				PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			}
		}catch(Exception e){
			Logger.error(e);
		}finally{
		this.lock.writeLock().unlock();	
		}
	}
	
	public void processRedeedNPC( ClientConnection origin) {

		// Member variable declaration
		PlayerCharacter player;
		Contract contract;
		CharacterItemManager itemMan;
		ItemBase itemBase;
		Item item;

		this.lock.writeLock().lock();
		
		try{
			
		
			if (building == null)
				return;
		player = SessionManager.getPlayerCharacter(origin);
		itemMan = player.getCharItemManager();

			contract = this.getContract();

			if (!player.getCharItemManager().hasRoomInventory((short)1)){
				ErrorPopupMsg.sendErrorPopup(player, 21);
				return;
			}


			if (!building.getHirelings().containsKey(this))
				return;

			if (!this.remove()) {
				PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
				return;
			}

			building.getHirelings().remove(this);

			itemBase = ItemBase.getItemBase(contract.getContractID());

			if (itemBase == null) {
				Logger.error("Could not find Contract for npc: " + this.getObjectUUID());
				return;
			}

			boolean itemWorked = false;

			item = new Item( itemBase, player.getObjectUUID(), OwnerType.PlayerCharacter, (byte) ((byte) this.getRank() - 1), (byte) ((byte) this.getRank() - 1),
					(short) 1, (short) 1, true, false,  Enum.ItemContainerType.INVENTORY, (byte) 0,
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
			mca.actionType = SVR_CLOSE_WINDOW;
			mca.setTargetType(building.getObjectType().ordinal());
			mca.setTargetID(building.getObjectUUID());
			origin.sendMsg(mca);
		
		}catch(Exception e){
			Logger.error(e);
		}finally{
			this.lock.writeLock().unlock();
		}

	}
		
		


}
