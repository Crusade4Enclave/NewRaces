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
import engine.InterestManagement.RealmMap;
import engine.InterestManagement.WorldGrid;
import engine.db.archive.CityRecord;
import engine.db.archive.DataWarehouse;
import engine.db.archive.MineRecord;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.DoorCloseJob;
import engine.jobs.SiegeSpireWithdrawlJob;
import engine.math.Bounds;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.net.ByteBufferWriter;
import engine.net.DispatchMessage;
import engine.net.client.msg.ApplyBuildingEffectMsg;
import engine.net.client.msg.UpdateObjectMsg;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Building extends AbstractWorldObject {

	// Used for thread safety

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/*  The Blueprint class has methods able to derive
	 *  all defining characteristics of this building,
	 */
	private int blueprintUUID = 0;
	public int meshUUID;
	private float w = 1.0f;
	private Vector3f meshScale = new Vector3f(1.0f, 1.0f, 1.0f);
	private int doorState = 0;
	private int ownerUUID = 0;  //NPC or Character--check ownerIsNPC flag
	private int _strongboxValue = 0;
	private int maxGold;
	private int effectFlags = 0;
	private String name = "";
	private int rank;
	private boolean ownerIsNPC = true;
	private boolean spireIsActive = false;
	public Zone parentZone;
	public boolean reverseKOS;
	public int reserve = 0;

	// Variables NOT to be stored in db

	protected Resists resists;
	public float statLat;
	public float statLon;
	public float statAlt;
	private ConcurrentHashMap<String, JobContainer> timers = null;
	private ConcurrentHashMap<String, Long> timestamps = null;
	private final ConcurrentHashMap<AbstractCharacter, Integer> hirelings = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private final HashMap<Integer, DoorCloseJob> doorJobs = new HashMap<>();
	private ConcurrentHashMap<Integer,BuildingFriends> friends = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer,Condemned> condemned = new ConcurrentHashMap<>();
	public LocalDateTime upgradeDateTime = null;
	public LocalDateTime taxDateTime = null;
	private ProtectionState protectionState = ProtectionState.NONE;

	public ArrayList<Vector3fImmutable> patrolPoints = new ArrayList<>();
	public ArrayList<Vector3fImmutable> sentryPoints = new ArrayList<>();
	public TaxType taxType = TaxType.NONE;
	public int taxAmount;
	public boolean enforceKOS = false;

	public int parentBuildingID;
	public boolean isFurniture = false;

	public int floor;
	public int level;
	public HashMap<Integer,Integer> fidelityNpcs = new HashMap<>();
	public AtomicBoolean isDeranking = new AtomicBoolean(false);
	private ArrayList<Building> children = null;
	public LocalDateTime maintDateTime;

	/**
	 * ResultSet Constructor
	 */

	public Building(ResultSet rs) throws SQLException {
		super(rs);

		float scale;
		Blueprint blueprint = null;

		try {
			this.meshUUID = rs.getInt("meshUUID");
			this.setObjectTypeMask(MBServerStatics.MASK_BUILDING);
			this.blueprintUUID = rs.getInt("blueprintUUID");
			this.gridObjectType = GridObjectType.STATIC;
			this.parentZone = DbManager.ZoneQueries.GET_BY_UID(rs.getLong("parent"));
			this.name = rs.getString("name");
			this.ownerUUID = rs.getInt("ownerUUID");

			// Orphaned Object Sanity Check
			//This was causing ABANDONED Tols.
			//        if (objectType == DbObjectType.INVALID)
			//            this.ownerUUID = 0;

			this.doorState = rs.getInt("doorState");
			this.setHealth(rs.getInt("currentHP"));
			this.w = rs.getFloat("w");
			this.setRot(new Vector3f(0f, rs.getFloat("rotY"), 0f));
			this.reverseKOS = rs.getByte("reverseKOS") == 1 ? true : false;

			scale = rs.getFloat("scale");
			this.meshScale = new Vector3f(scale, scale, scale);

			this.rank = rs.getInt("rank");
			this.parentBuildingID = rs.getInt("parentBuildingID");
			
			//create a new list if the building is a parent and not a child.

			if (this.parentBuildingID == 0)
				this.children = new ArrayList<>();

			this.floor = rs.getInt("floor");
			this.level = rs.getInt("level");
			this.isFurniture = (rs.getBoolean("isFurniture"));

			// Lookup building blueprint

			if (this.blueprintUUID == 0)
				blueprint = Blueprint._meshLookup.get(meshUUID);
			else
				blueprint = this.getBlueprint();

			// Log error if something went horrible wrong

			if ((this.blueprintUUID != 0) && (blueprint == null))
				Logger.error( "Invalid blueprint for object: " + this.getObjectUUID());

			// Note: We handle R8 tree edge case for mesh and health
			// after city is loaded to avoid recursive result set call
			// in City resulting in a stack ovreflow.

			if (blueprint != null) {

				// Only switch mesh for player dropped structures

				if (this.blueprintUUID != 0)
					this.meshUUID = blueprint.getMeshForRank(rank);

				this.healthMax = blueprint.getMaxHealth(this.rank);

				// If this object has no blueprint but is a blueprint
                // mesh then set it's current health to max health

				if (this.blueprintUUID == 0)
                    this.setHealth(healthMax);

				if (blueprint.getBuildingGroup().equals(BuildingGroup.BARRACK))
					this.patrolPoints = DbManager.BuildingQueries.LOAD_PATROL_POINTS(this);

			} else{
			    this.healthMax = 100000;  // Structures with no blueprint mesh
                this.setHealth(healthMax);
            }

			// Null out blueprint if not needed (npc building)

			if (blueprintUUID == 0)
				blueprint = null;

			resists = new Resists("Building");
			this.statLat = rs.getFloat("locationX");
			this.statAlt = rs.getFloat("locationY");
			this.statLon = rs.getFloat("locationZ");

			if (this.parentZone != null){
				if (this.parentBuildingID != 0){
					Building parentBuilding = BuildingManager.getBuilding(this.parentBuildingID);
					if (parentBuilding != null){
						this.setLoc(new Vector3fImmutable(this.statLat + this.parentZone.absX + parentBuilding.statLat, this.statAlt + this.parentZone.absY + parentBuilding.statAlt, this.statLon + this.parentZone.absZ + parentBuilding.statLon));
					}else{
						this.setLoc(new Vector3fImmutable(this.statLat + this.parentZone.absX, this.statAlt + this.parentZone.absY, this.statLon + this.parentZone.absZ));

					}
				} else {

					// Altitude of this building is derived from the heightmap engine.

					Vector3fImmutable tempLoc = new Vector3fImmutable(this.statLat + this.parentZone.absX, 0, this.statLon + this.parentZone.absZ);
					tempLoc = new Vector3fImmutable(tempLoc.x, HeightMap.getWorldHeight(tempLoc), tempLoc.z);
					this.setLoc(tempLoc);
				}
			}

			this._strongboxValue = rs.getInt("currentGold");
			this.maxGold = 15000000; // *** Refactor to blueprint method
			this.reserve = rs.getInt("reserve");

			// Does building have a protection contract?
			this.taxType = TaxType.valueOf(rs.getString("taxType"));
			this.taxAmount = rs.getInt("taxAmount");
			this.protectionState = ProtectionState.valueOf(rs.getString("protectionState"));

			java.sql.Timestamp maintTimeStamp = rs.getTimestamp("maintDate");

			if (maintTimeStamp != null)
				this.maintDateTime = LocalDateTime.ofInstant(maintTimeStamp.toInstant(), ZoneId.systemDefault());

			java.sql.Timestamp taxTimeStamp = rs.getTimestamp("taxDate");

			if (taxTimeStamp != null)
				this.taxDateTime = LocalDateTime.ofInstant(taxTimeStamp.toInstant(), ZoneId.systemDefault());

			java.sql.Timestamp upgradeTimeStamp = rs.getTimestamp("upgradeDate");

			if (upgradeTimeStamp != null)
				this.upgradeDateTime = LocalDateTime.ofInstant(upgradeTimeStamp.toInstant(), ZoneId.systemDefault());

		} catch (Exception e) {

			Logger.error( "Failed for object " + this.blueprintUUID + ' ' + this.getObjectUUID() + e.toString());
		}
	}

	/*
	 * Getters
	 */

	public final boolean isRanking() {

		return this.upgradeDateTime != null;
	}

	public final int getRank() {
		return rank;
	}

	public final int getOwnerUUID() {
		return ownerUUID;
	}

	public final boolean isOwnerIsNPC() {
		return ownerIsNPC;
	}

	public final City getCity() {

		if (this.parentZone == null)
			return null;

		if (this.getBlueprint() != null && this.getBlueprint().isSiegeEquip() && this.protectionState.equals(ProtectionState.PROTECTED)){
			if (this.getGuild() != null){
				if (this.getGuild().getOwnedCity() != null){
					if (this.getLoc().isInsideCircle(this.getGuild().getOwnedCity().getLoc(), Enum.CityBoundsType.SIEGE.extents))
						return this.getGuild().getOwnedCity();
				}else{
					Bane bane = Bane.getBaneByAttackerGuild(this.getGuild());
					
					if (bane != null){
						if (bane.getCity() != null){
							if (this.getLoc().isInsideCircle(bane.getCity().getLoc(), Enum.CityBoundsType.SIEGE.extents))
								return bane.getCity();
						}
					}
				}
			}
		}
		if (this.parentZone.isPlayerCity() == false)
			return null;

		return City.getCity(this.parentZone.getPlayerCityUUID());

	}

	public final String getCityName() {

		City city = getCity();

		if (city != null)
			return city.getName();

		return "";
	}

	public final Blueprint getBlueprint() {

		if (this.blueprintUUID == 0)
			return null;

		return Blueprint.getBlueprint(this.blueprintUUID);

	}

	public final int getBlueprintUUID() {

		return this.blueprintUUID;
	}

	public final void setCurrentHitPoints(Float CurrentHitPoints) {
		this.addDatabaseJob("health", MBServerStatics.ONE_MINUTE);
		this.setHealth(CurrentHitPoints);
	}

	public final LocalDateTime getUpgradeDateTime() {
		lock.readLock().lock();
		try {
			return upgradeDateTime;
		} finally {
			lock.readLock().unlock();
		}
	}

	public final float modifyHealth(final float value, final AbstractCharacter attacker) {

		if (this.rank == -1)
			return 0f;

		boolean worked = false;
		Float oldHealth=0f, newHealth=0f;
		while (!worked) {
			if (this.rank == -1)
				return 0f;
			oldHealth = this.health.get();
			newHealth = oldHealth + value;
			if (newHealth > this.healthMax)
				newHealth = healthMax;
			worked = this.health.compareAndSet(oldHealth, newHealth);
		}

		if (newHealth < 0) {
			if (this.isDeranking.compareAndSet(false, true)) {
				this.destroyOrDerank(attacker);
			}

			return newHealth - oldHealth;
		} else
			this.addDatabaseJob("health", MBServerStatics.ONE_MINUTE);

		if (value < 0)
			Mine.SendMineAttackMessage(this);

		return newHealth - oldHealth;


	}

	//This method is to handle when a building is damaged below 0 health.
	//Either destroy or derank it.

	public final void destroyOrDerank(AbstractCharacter attacker) {

		Blueprint blueprint;
		City city;

		// Sanity check: Early exit if a non
		// blueprinted object is attempting to
		// derank.

		if (this.blueprintUUID == 0)
			return;

		blueprint = this.getBlueprint();
		city = this.getCity();

		// Special handling of destroyed Banes

		if (blueprint.getBuildingGroup() == BuildingGroup.BANESTONE) {
			city.getBane().endBane(SiegeResult.DEFEND);
			return;
		}

		// Special handling of warehouses

		if (blueprint.getBuildingGroup() == BuildingGroup.WAREHOUSE)
			if (city != null)
				city.setWarehouseBuildingID(0);

		// Special handling of destroyed Spires

		if ((blueprint.getBuildingGroup() == BuildingGroup.SPIRE) && this.rank == 1)
			this.disableSpire(true);

		// Special handling of destroyed Mines

		if (blueprint.getBuildingGroup() == BuildingGroup.MINE
				&& this.rank == 1) {

			Mine mine = Mine.getMineFromTower(this.getObjectUUID());

			if (mine != null) {

				// Warehouse mine destruction event

				MineRecord mineRecord = MineRecord.borrow(mine, attacker, RecordEventType.DESTROY);
				DataWarehouse.pushToWarehouse(mineRecord);

				this.setRank(-1);
				this.setCurrentHitPoints((float) 1);
				this.healthMax = (float) 1;
				this.meshUUID = this.getBlueprint().getMeshForRank(this.rank);
				mine.handleDestroyMine();
				this.getBounds().setBounds(this);
				this.refresh(true);
				return;
			}
		}

		// Special handling of deranking Trees

		if (blueprint.getBuildingGroup() == BuildingGroup.TOL) {
			derankTreeOfLife();
			return;
		}

		// If codepath reaches here then it's a regular
		//  structure not requiring special handling.
		//  Time to either derank or destroy the building.

		if ((this.rank - 1) < 1)
			this.setRank(-1);
		else
			this.setRank(this.rank - 1);

	}

	private void derankTreeOfLife() {

		City city;
		Bane bane;
		Realm cityRealm;ArrayList<Building> spireBuildings = new ArrayList<>();
		ArrayList<Building> shrineBuildings = new ArrayList<>();
		ArrayList<Building> barracksBuildings = new ArrayList<>();
		Building spireBuilding;
		Building shrineBuilding;
		SiegeResult siegeResult;
		AbstractCharacter newOwner;

		city = this.getCity();

		if (city == null) {
			Logger.error("No city for tree of uuid" + this.getObjectUUID());
			return;
		}

		bane = city.getBane();

		// We need to collect the spires and shrines on the citygrid in case
		// they will be deleted as excess as the tree deranks.

		for (Building building : city.getParent().zoneBuildingSet) {

			//dont add -1 rank buildings.
			if (building.rank <= 0)
				continue;
			if (building.getBlueprint() != null && building.getBlueprint().getBuildingGroup() == BuildingGroup.SPIRE)
				spireBuildings.add(building);

			if (building.getBlueprint() != null && building.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE)
				shrineBuildings.add(building);

			if (building.getBlueprint() != null && building.getBlueprint().getBuildingGroup() == BuildingGroup.BARRACK)
				barracksBuildings.add(building);
		}

		// A tree can only hold so many spires.  As it deranks we need to delete
		// the excess

		if (spireBuildings.size() > Blueprint.getMaxShrines(this.rank - 1)) {

			spireBuilding = spireBuildings.get(0);

			// Disable and delete a random spire

			if (spireBuilding != null) {
				spireBuilding.disableSpire(true);
				spireBuilding.setRank(-1);
			}
		}

		if (shrineBuildings.size() > Blueprint.getMaxShrines(this.rank - 1)) {

			shrineBuilding = shrineBuildings.get(0);

			// Delete a random shrine

			if (shrineBuilding != null)
				shrineBuilding.setRank(-1);
		}

		if (barracksBuildings.size() > this.rank - 1) {

			Building barracksBuilding = barracksBuildings.get(0);

			// Delete a random barrack

			if (barracksBuilding != null)
				barracksBuilding.setRank(-1);
		}

		// If the tree is R8 and deranking, we need to update it's
		// mesh along with buildings losing their health bonus

		if (this.rank == 8) {

			cityRealm = city.getRealm();

			if (cityRealm != null)
				cityRealm.abandonRealm();

			for (Building cityBuilding : this.parentZone.zoneBuildingSet) {

				if ((cityBuilding.getBlueprint() != null && cityBuilding.getBlueprint().getBuildingGroup() != BuildingGroup.TOL)
						&& (cityBuilding.getBlueprint().getBuildingGroup() != BuildingGroup.BANESTONE)) {
					cityBuilding.healthMax = cityBuilding.getBlueprint().getMaxHealth(cityBuilding.rank);
				}

				if (cityBuilding.health.get() > cityBuilding.healthMax)
					cityBuilding.setHealth(cityBuilding.healthMax);
			}
		}

		// Tree is simply deranking.
		// Let's do so and early exit

		if (this.rank > 1) {
			this.setRank(rank - 1);
			City.lastCityUpdate = System.currentTimeMillis();
			return;
		}

		// Handling of exploding TOL's

		// Must remove a bane before considering destruction of a TOL

		if (bane != null) {

			// Cache the new owner

			newOwner = Guild.GetGL(bane.getOwner().getGuild());

			this.isDeranking.compareAndSet(false, true);

			if ((bane.getOwner().getGuild().getGuildState() == GuildState.Sovereign) ||
					(bane.getOwner().getGuild().getGuildState() == GuildState.Protectorate) ||
					(bane.getOwner().getGuild().getGuildState() == GuildState.Province) ||
					(bane.getOwner().getGuild().getGuildState() == GuildState.Nation))
				siegeResult = SiegeResult.DESTROY;
			else
				siegeResult = SiegeResult.CAPTURE;

			// Remove realm if city had one

			Realm realm = RealmMap.getRealmAtLocation(city.getLoc());

			if (realm != null)
				if (realm.isRealmFullAfterBane())
					siegeResult = SiegeResult.DESTROY;

			city.getBane().endBane(siegeResult);

			// If it's a capture bane transfer the tree and exit

			if (siegeResult.equals(SiegeResult.CAPTURE)) {
				city.transfer(newOwner);
				CityRecord cityRecord = CityRecord.borrow(city, RecordEventType.CAPTURE);
				DataWarehouse.pushToWarehouse(cityRecord);
				return;
			}
		} // end removal of bane

		//  if codepath reaches here then we can now destroy the tree and the city

		CityRecord cityRecord = CityRecord.borrow(city, RecordEventType.DESTROY);
		DataWarehouse.pushToWarehouse(cityRecord);

		city.destroy();

	}

	public float getCurrentHitpoints(){
		return this.health.get();
	}

	// Return the maint cost in gold associated with this structure

	public int getMaintCost() {

		int maintCost =0;

		// Add cost for building structure

		maintCost += this.getBlueprint().getMaintCost(rank);

		// Add costs associated with hirelings

		for (AbstractCharacter npc : this.hirelings.keySet()) {

			if (npc.getObjectType() != GameObjectType.NPC)
				continue;



			maintCost += Blueprint.getNpcMaintCost(npc.getRank());
		}

		return maintCost;
	}


	public final void submitOpenDoorJob(int doorID) {

		//cancel any outstanding door close jobs for this door

		if (this.doorJobs.containsKey(doorID)) {
			this.doorJobs.get(doorID).cancelJob();
			this.doorJobs.remove(doorID);
		}

		//add new door close job

		DoorCloseJob dcj = new DoorCloseJob(this, doorID);
		this.doorJobs.put(doorID, dcj);
		JobScheduler.getInstance().scheduleJob(dcj, MBServerStatics.DOOR_CLOSE_TIMER);
	}

	public final float getMaxHitPoints() {
		return this.healthMax;
	}

	public final void setMaxHitPoints(float maxHealth) {
		this.healthMax = maxHealth;
	}

	public final void setName(String value) {

		if (DbManager.BuildingQueries.CHANGE_NAME(this, value) == false)
			return;

		this.name = value;
		this.updateName();
	}

	public final void setw(float value) {
		this.w = value;
	}

	public final float getw() {
		return this.w;
	}

	public final void setMeshScale(Vector3f value) {
		this.meshScale = value;
	}

	public final Vector3f getMeshScale() {
		return this.meshScale;
	}

	public final int getMeshUUID() {
		return this.meshUUID;
	}

	public final Resists getResists() {
		return this.resists;
	}

	public final Zone getParentZone() {
		return this.parentZone;
	}

	public final int getParentZoneID() {

		if (this.parentZone == null)
			return 0;

		return this.parentZone.getObjectUUID();
	}

	public final void setParentZone(Zone zone) {

		//update ZoneManager's zone building list
		if (zone != null)
			if (this.parentZone != null) {

				this.parentZone.zoneBuildingSet.remove(this);
				zone.zoneBuildingSet.add(this);

			} else
				zone.zoneBuildingSet.add(this);
		else if (this.parentZone != null)
			this.parentZone.zoneBuildingSet.remove(this);

		if (this.parentZone == null) {
			this.parentZone = zone;
			this.setLoc(new Vector3fImmutable(this.statLat + zone.absX, this.statAlt + zone.absY, this.statLon + zone.absZ));
		} else
			this.parentZone = zone;
	}

	//Sets the relative position to a parent zone

	public final void setRelPos(Zone zone, float locX, float locY, float locZ) {

		//update ZoneManager's zone building list

		if (zone != null)
			if (this.parentZone != null) {
				if (zone.getObjectUUID() != this.parentZone.getObjectUUID()) {
					this.parentZone.zoneBuildingSet.remove(this);
					zone.zoneBuildingSet.add(this);
				}
			} else
				zone.zoneBuildingSet.add(this);
		else if (this.parentZone != null)
			this.parentZone.zoneBuildingSet.remove(this);

		this.statLat = locX;
		this.statAlt = locY;
		this.statLon = locZ;
		this.parentZone = zone;
	}

	public float getStatLat() {
		return statLat;
	}

	public float getStatLon() {
		return statLon;
	}

	public float getStatAlt() {
		return statAlt;
	}

	public Guild getGuild() {

		AbstractCharacter buildingOwner;

		buildingOwner = this.getOwner();

		if (buildingOwner != null)
			return buildingOwner.getGuild();
		else
			return Guild.getErrantGuild();
	}

	public int getEffectFlags() {
		return this.effectFlags;
	}

	public void addEffectBit(int bit) {
		this.effectFlags |= bit;
	}

	public void removeAllVisualEffects() {
		this.effectFlags = 0;
		ApplyBuildingEffectMsg applyBuildingEffectMsg = new ApplyBuildingEffectMsg(3276859, 1, this.getObjectType().ordinal(), this.getObjectUUID(), 0);
		DispatchMessage.sendToAllInRange(this, applyBuildingEffectMsg);
	}

	public void removeEffectBit(int bit) {
		this.effectFlags &= (~bit);

	}

	@Override
	public String getName() {
		return this.name;
	}

	/*
	 * Utils
	 */

	public final AbstractCharacter getOwner() {

		if (this.ownerUUID == 0)
			return null;
		if (this.ownerIsNPC)
			return NPC.getFromCache(this.ownerUUID);

		return PlayerCharacter.getFromCache(this.ownerUUID);

	}

	public final String getOwnerName() {
		AbstractCharacter owner = this.getOwner();
		if (owner != null)
			return owner.getName();
		return "";
	}

	public final String getGuildName() {
		Guild g = getGuild();
		if (g != null)
			return g.getName();
		return "None";
	}


	/*
	 * Serializing
	 */
	
	public static void _serializeForClientMsg(Building building, ByteBufferWriter writer) {
		writer.putInt(building.getObjectType().ordinal());
		writer.putInt(building.getObjectUUID());
		writer.putInt(0); // pad

		writer.putInt(building.meshUUID);

		writer.putInt(0); // pad

		if (building.parentBuildingID != 0){

			writer.putFloat(building.statLat);
			writer.putFloat(building.statAlt);
			writer.putFloat(building.statLon);

		}else{
			writer.putFloat(building.getLoc().getX());
			writer.putFloat(building.getLoc().getY()); // Y location
			writer.putFloat(building.getLoc().getZ());
		}

		writer.putFloat(building.w);
		writer.putFloat(0f);
		writer.putFloat(building.getRot().y);

		writer.putFloat(0f);
		writer.putFloat(building.meshScale.getX());
		writer.putFloat(building.meshScale.getY());
		writer.putFloat(building.meshScale.getZ());

		if (building.parentBuildingID != 0){
			writer.putInt(GameObjectType.Building.ordinal());
			writer.putInt(building.parentBuildingID);
			writer.putInt(building.floor);
			writer.putInt(building.level);

		}else{
			writer.putInt(0); // Pad //Parent
			writer.putInt(0); // Pad
			writer.putInt(-1); // Static
			writer.putInt(-1); // Static
		}

		writer.put((byte)0);  // 0
		writer.putFloat(3);  // 3
		writer.putInt(GameObjectType.Building.ordinal());
		writer.putInt(building.getObjectUUID());

		if (building.ownerIsNPC)
			writer.putInt(GameObjectType.NPC.ordinal());
		else
			writer.putInt(GameObjectType.PlayerCharacter.ordinal());

		writer.putInt(building.ownerUUID);

		writer.put((byte) 1); // End Datablock
		writer.putFloat(building.health.get());
		writer.putFloat(building.healthMax);

		if (building.blueprintUUID == 0)
			writer.putInt(0);
		else
			writer.putInt(building.getBlueprint().getIcon());

		writer.putInt(building.effectFlags);

		writer.put((byte) 1); // End Datablock
		Guild g = building.getGuild();
		Guild nation = null;

		if (g == null) {

			for (int i = 0; i < 3; i++) {
				writer.putInt(16);
			}
			for (int i = 0; i < 2; i++) {
				writer.putInt(0);
			}
		} else {
			GuildTag._serializeForDisplay(g.getGuildTag(),writer);
			nation = g.getNation();
		}
		writer.put((byte) 1); // End Datablock?
		if (nation == null) {
			for (int i = 0; i < 3; i++) {
				writer.putInt(16);
			}
			for (int i = 0; i < 2; i++) {
				writer.putInt(0);
			}
		} else
			GuildTag._serializeForDisplay(nation.getGuildTag(),writer);
		writer.putString(building.name);
		writer.put((byte) 0); // End datablock
	}

	/*
	 * Database
	 */

	@Override
	public void updateDatabase() {

		// *** Refactor : Log error here to see if it's ever called
	}

	public final LocalDateTime getDateToUpgrade() {
		return upgradeDateTime;
	}

	public final boolean setStrongboxValue(int newValue) {

		boolean success = true;

		try {
			DbManager.BuildingQueries.SET_PROPERTY(this, "currentGold", newValue);
			this._strongboxValue = newValue;
		} catch (Exception e) {
			success = false;
			Logger.error( "Error writing to database");
		}

		return success;
	}

	public final int getStrongboxValue() {
		return _strongboxValue;
	}

	public final void setMeshUUID(int value) {
		this.meshUUID = value;
	}

	public final void setRank(int newRank) {

		int newMeshUUID;
		boolean success;


		// If this building has no blueprint then set rank and exit immediatly.

		if (this.blueprintUUID == 0 || this.getBlueprint() != null && this.getBlueprint().getBuildingGroup().equals(BuildingGroup.MINE)) {
			this.rank = newRank;
			DbManager.BuildingQueries.CHANGE_RANK(this.getObjectUUID(), newRank);
			return;
		}

		// Delete any upgrade jobs before doing anything else.  It won't quite work
		// if in a few lines we happen to delete this building.

		JobContainer jc = this.getTimers().get("UPGRADE");

		if (jc != null) {
			if (!JobScheduler.getInstance().cancelScheduledJob(jc))
				Logger.error( "failed to cancel existing upgrade job.");
		}

		// Attempt write to database, or delete the building
		// if we are destroying it.

		if (newRank == -1)
			success = DbManager.BuildingQueries.DELETE_FROM_DATABASE(this);
		else
			success = DbManager.BuildingQueries.updateBuildingRank(this, newRank);

		if (success == false) {
			Logger.error("Error writing to database UUID: " + this.getObjectUUID());
			return;
		}

		this.isDeranking.compareAndSet(false, true);

		// Change the building's rank

		this.rank = newRank;

		// New rank means new mesh

		newMeshUUID = this.getBlueprint().getMeshForRank(this.rank);
		this.meshUUID = newMeshUUID;

		// New rank mean new max hitpoints.

		this.healthMax = this.getBlueprint().getMaxHealth(this.rank);
		this.setCurrentHitPoints(this.healthMax);

		if (this.getUpgradeDateTime() != null)
			BuildingManager.setUpgradeDateTime(this, null, 0);

		// If we destroyed this building make sure to turn off
		// protection

		if (this.rank == -1)
			this.protectionState = ProtectionState.NONE;

		if ((this.getBlueprint().getBuildingGroup() == BuildingGroup.TOL)
				&& (this.rank == 8))
			this.meshUUID = Realm.getRealmMesh(this.getCity());;

		// update object to clients

		this.refresh(true);
		if (this.getBounds() != null)
			this.getBounds().setBounds(this);

		// Cleanup hirelings resulting from rank change

		BuildingManager.cleanupHirelings(this);

		this.isDeranking.compareAndSet(true, false);
	}

	public final void refresh(boolean newMesh) {

		if (newMesh)
			WorldGrid.updateObject(this);
		else {
			UpdateObjectMsg uom = new UpdateObjectMsg(this, 3);
			DispatchMessage.sendToAllInRange(this, uom);
		}
	}

	public final void updateName() {

		UpdateObjectMsg uom = new UpdateObjectMsg(this, 2);
		DispatchMessage.sendToAllInRange(this, uom);

	}

	// *** Refactor: Can't we just use setRank() for this?

	public final void rebuildMine(){
		this.setRank(1);
		this.meshUUID = this.getBlueprint().getMeshForRank(this.rank);
		// New rank mean new max hitpoints.
		this.healthMax = this.getBlueprint().getMaxHealth(this.rank);
		this.setCurrentHitPoints(this.healthMax);
		this.getBounds().setBounds(this);
	}

	public final void refreshGuild() {

		UpdateObjectMsg uom = new UpdateObjectMsg(this, 5);
		DispatchMessage.sendToAllInRange(this, uom);

	}

	public int getMaxGold() {
		return maxGold;
	}

	//This returns if a player is allowed access to control the building

	@Override
	public void runAfterLoad() {

		try {

			this.parentZone.zoneBuildingSet.add(this);

			// Submit upgrade job if building is currently set to rank.

		

			try {
				DbObjectType objectType = DbManager.BuildingQueries.GET_UID_ENUM(this.ownerUUID);
				this.ownerIsNPC = (objectType == DbObjectType.NPC);
			} catch (Exception e) {
				this.ownerIsNPC = false;
				Logger.error("Failed to find Object Type for owner " + this.ownerUUID+ " Location " + this.getLoc().toString());
			}

			try{
				DbManager.BuildingQueries.LOAD_ALL_FRIENDS_FOR_BUILDING(this);
				DbManager.BuildingQueries.LOAD_ALL_CONDEMNED_FOR_BUILDING(this);
			}catch(Exception e){
				Logger.error( this.getObjectUUID() + " failed to load friends/condemned." + e.getMessage());
			}

			//LOad Owners in Cache so we do not have to continuely look in the db for owner.

			if (this.ownerIsNPC){
				if (NPC.getNPC(this.ownerUUID) == null)
					Logger.info( "Building UID " + this.getObjectUUID() + " Failed to Load NPC Owner with ID " + this.ownerUUID+ " Location " + this.getLoc().toString());

			}else if (this.ownerUUID != 0){
				if (PlayerCharacter.getPlayerCharacter(this.ownerUUID) == null){
					Logger.info( "Building UID " + this.getObjectUUID() + " Failed to Load Player Owner with ID " + this.ownerUUID + " Location " + this.getLoc().toString());
				}
			}

			// Apply health bonus and special mesh for realm if applicable
			if ((this.getCity() != null) && this.getCity().getTOL() != null && (this.getCity().getTOL().rank == 8)) {

				// Update mesh accordingly
				if (this.getBlueprint() != null && this.getBlueprint().getBuildingGroup() == BuildingGroup.TOL)
					this.meshUUID = Realm.getRealmMesh(this.getCity());

				// Apply realm capital health bonus.
				// Do not apply bonus to banestones or TOL's.  *** Refactor:
				// Possibly only protected buildings?  Needs some thought.

				float missingHealth = 0;

				if (this.health.get() != 0)
					missingHealth = this.healthMax-this.health.get();

				if ((this.getBlueprint() != null && this.getBlueprint().getBuildingGroup() != BuildingGroup.TOL)
						&& (this.getBlueprint().getBuildingGroup() != BuildingGroup.BANESTONE)){
					this.healthMax += (this.healthMax * Realm.getRealmHealthMod(this.getCity()));

					if (this.health.get() != 0)
						this.health.set(this.healthMax - missingHealth);

					if (this.health.get() > this.healthMax)
						this.health.set(this.healthMax);
				}
			}

			// Set bounds for this building

			Bounds buildingBounds = Bounds.borrow();
			buildingBounds.setBounds(this);
			this.setBounds(buildingBounds);
			
			//create a new list for children if the building is not a child. children list default is null.
			//TODO Remove Furniture/Child buildings from building class and move them into a seperate class.
			if (this.parentBuildingID == 0)
				this.children = new ArrayList<>();

			if (this.parentBuildingID != 0){
				Building parent = BuildingManager.getBuildingFromCache(this.parentBuildingID);
				
				if (parent != null){
					parent.children.add(this);
					//add furniture to region cache. floor and level are reversed in database, //TODO Fix
					Regions region = BuildingManager.GetRegion(parent, this.level,this.floor, this.getLoc().x, this.getLoc().z);
				if (region != null)
					Regions.FurnitureRegionMap.put(this.getObjectUUID(), region);
				}
					
			}
			
			if (this.upgradeDateTime != null)
				BuildingManager.submitUpgradeJob(this);

			// Run Once move buildings
			// 64 / -64 to align with pads

			// Don't move furniture
/*
			if (parentBuildingID != 0)
				return;

			// Don't move buildings not on a city zone
			// or buildings that are in npc owned city

			City city = getCity();

			if (city == null)
				return;

			if (city.getIsNpcOwned() == 1)
				return;

			PullCmd.MoveBuilding(this, null, getLoc().add(new Vector3fImmutable(0, 0, 0)), getParentZone());
*/

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public synchronized  boolean setOwner(AbstractCharacter newOwner) {

		int  newOwnerID;
		if (newOwner == null)
			newOwnerID = 0;
		else
			newOwnerID = newOwner.getObjectUUID();

		// ***BONUS CODE BELOW!
		/*
        if (newOwner == null) {
            this.ownerIsNPC = false;
            this.ownerUUID = 0;
        } else if (newOwner instanceof PlayerCharacter) {
            this.ownerIsNPC = false;
            this.ownerUUID = newOwner.getObjectUUID();
        } else {
            this.ownerIsNPC = true;
            this.ownerUUID = newOwner.getObjectUUID();
        }
		 */

		try {
			// Save new owner to database

			if (!DbManager.BuildingQueries.updateBuildingOwner(this, newOwnerID))
				return false;

			if (newOwner == null) {
				this.ownerIsNPC = false;
				this.ownerUUID = 0; }
			else {
				this.ownerUUID = newOwner.getObjectUUID();
				this.ownerIsNPC = (newOwner.getObjectType() == GameObjectType.NPC);
			}


			// Set new guild for hirelings and refresh all clients

			this.refreshGuild();
			BuildingManager.refreshHirelings(this);

		} catch (Exception e) {
			Logger.error( "Error updating owner! UUID: " + this.getObjectUUID());
			return false;
		}

		return true;

	}

	//This turns on and off low damage effect for building

	public void toggleDamageLow(boolean on) {
		if (on)
			addEffectBit(2);
		else
			removeEffectBit(2);
	}

	//This turns on and off medium damage effect for building

	public void toggleDamageMedium(boolean on) {
		if (on)
			addEffectBit(4);
		else
			removeEffectBit(4);
	}

	//This turns on and off high damage effect for building

	public void toggleDamageHigh(boolean on) {
		if (on)
			addEffectBit(8);
		else
			removeEffectBit(8);
	}

	//This clears all damage effects on a building
	public void clearDamageEffect() {
		toggleDamageLow(false);
		toggleDamageMedium(false);
		toggleDamageHigh(false);
	}

	public Vector3fImmutable getStuckLocation() {

		BuildingModelBase bmb = BuildingModelBase.getModelBase(this.meshUUID);
		Vector3fImmutable convertLoc = null;


		if (bmb != null) {
			BuildingLocation bl = bmb.getStuckLocation();

			if (bl != null){

				Vector3fImmutable buildingWorldLoc = ZoneManager.convertLocalToWorld(this, bl.getLoc());
				return buildingWorldLoc;
			}


		}

		return null;
	}

	public boolean isDoorOpen(int doorNumber) {

		if (this.doorState == 0)
			return false;

		return (this.doorState & (1 << doorNumber + 16)) != 0;

	}

	public boolean isDoorLocked(int doorNumber) {

		if (this.doorState == 0)
			return false;

		return (this.doorState & (1 << doorNumber)) != 0;

	}

	public boolean setDoorState(int doorNumber, DoorState doorState) {

		boolean updateRecord;

		updateRecord = false;

		// Can't have an invalid door number
		// Log error?
		if (doorNumber < 1 || doorNumber > 16)
			return false;

		switch (doorState) {

		case OPEN:
			this.doorState |= (1 << (doorNumber + 16));
			break;
		case CLOSED:
			this.doorState &= ~(1 << (doorNumber + 16));
			break;
		case UNLOCKED:
			this.doorState &= ~(1 << doorNumber);
			updateRecord = true;
			break;
		case LOCKED:
			this.doorState |= (1 << doorNumber);
			updateRecord = true;
			break;
		}

		// Save to database ?
		if (updateRecord == true)
			return DbManager.BuildingQueries.UPDATE_DOOR_LOCK(this.getObjectUUID(), this.doorState);
		else
			return true;
	}

	public int getDoorstate(){
		return this.doorState;
	}

	public void updateEffects() {

		ApplyBuildingEffectMsg applyBuildingEffectMsg = new ApplyBuildingEffectMsg(0x00720063, 1, this.getObjectType().ordinal(), this.getObjectUUID(), this.effectFlags);
		DispatchMessage.sendToAllInRange(this, applyBuildingEffectMsg);

	}

	public final void enableSpire() {

		SpireType spireType;

		if (this.getCity() == null)
			return;

		// Blueprint sanity check

		if (this.blueprintUUID == 0)
			return;

		spireType = SpireType.getByBlueprintUUID(this.blueprintUUID);

		SiegeSpireWithdrawlJob spireJob = new SiegeSpireWithdrawlJob(this);
		JobContainer jc = JobScheduler.getInstance().scheduleJob(spireJob, 300000);
		this.getTimers().put("SpireWithdrawl", jc);

		this.getCity().addCityEffect(spireType.getEffectBase(), rank);
		addEffectBit(spireType.getEffectFlag());
		this.spireIsActive = true;
		this.updateEffects();


	}

	public final void disableSpire(boolean refreshEffect) {

		SpireType spireType;

		if (this.getCity() == null)
			return;

		// Blueprint sanity check

		if (this.blueprintUUID == 0)
			return;

		spireType = SpireType.getByBlueprintUUID(this.blueprintUUID);

		this.getCity().removeCityEffect(spireType.getEffectBase(), rank, refreshEffect);

		JobContainer toRemove = this.getTimers().get("SpireWithdrawl");

		if (toRemove != null) {
			toRemove.cancelJob();
			this.getTimers().remove("SpireWithdrawl");
		}

		this.spireIsActive = false;
		this.removeEffectBit(spireType.getEffectFlag());
		this.updateEffects();
	}

	public ConcurrentHashMap<AbstractCharacter, Integer> getHirelings() {
		return hirelings;
	}

	public final boolean isSpireIsActive() {
		return spireIsActive;
	}

	public final boolean isVulnerable() {

		// NPC owned buildings are never vulnerable

		if (ownerIsNPC)
			return false;

		// Buildings on an npc citygrid are never vulnerable

		if (this.getCity() != null) {
			if (this.getCity().getParent().isNPCCity() == true)
				return false;
		}

		// Destroyed buildings are never vulnerable

		if (rank < 0)
			return false;

		// Any structure without a blueprint was not placed by a
		// player and we can assume to be invulnerable regardless
		// of a protection contract or not.

		if (this.getBlueprint() == null)
			return false;

		// Runegates are never vulerable.

		if (this.getBlueprint().getBuildingGroup() == BuildingGroup.RUNEGATE)
			return false;

		// Shrines are never vulerable.  They blow up as a
		// tree deranks.

		if (this.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE)
			return false;

		// Mines are vulnerable only if they are active

		if (this.getBlueprint().getBuildingGroup() == BuildingGroup.MINE) {

			// Cannot access mine

			if (Mine.getMineFromTower(this.getObjectUUID()) == null)
				return false;

			return Mine.getMineFromTower(this.getObjectUUID()).getIsActive() == true;
		}

		// Errant banestones are vulnerable by default

		if ((this.getBlueprint().getBuildingGroup() == BuildingGroup.BANESTONE) &&
				this.getCity().getBane().isErrant() == true)
			return true;

		// There is an active protection contract.  Is there also
		// an active bane?  If so, it's meaningless.

		if (this.assetIsProtected() == true) {

			// Building protection is meaningless without a city

			if (this.getCity() == null)
				return true;

			// All buildings are vulnerable during an active bane

			return (this.getCity().protectionEnforced == false);

		}

		// No protection contract?  Oh well, you're vunerable!

		return true;
	}

	public final void setSpireIsActive(boolean spireIsActive) {
		this.spireIsActive = spireIsActive;
	}

	public final ConcurrentHashMap<String, JobContainer> getTimers() {
		if (this.timers == null)
			this.timers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		return this.timers;
	}

	public final ConcurrentHashMap<String, Long> getTimestamps() {
		if (this.timestamps == null)
			this.timestamps = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		return this.timestamps;
	}

	public final long getTimeStamp(final String name) {
		if (this.getTimestamps().containsKey(name))
			return this.timestamps.get(name);
		return 0L;
	}

	public final void setTimeStamp(final String name, final long value) {
		this.getTimestamps().put(name, value);
	}

	public  ConcurrentHashMap<Integer,BuildingFriends> getFriends() {
		return this.friends;
	}

	public final void claim(AbstractCharacter sourcePlayer) {

		// Clear any existing friend or condemn entries

		this.friends.clear();
		DbManager.BuildingQueries.CLEAR_FRIENDS_LIST(this.getObjectUUID());

		condemned.clear();
		DbManager.BuildingQueries.CLEAR_CONDEMNED_LIST(this.getObjectUUID());

		// Transfer the building asset ownership

		this.setOwner(sourcePlayer);

	}

	/**
	 * @return the protectionState
	 */
	public ProtectionState getProtectionState() {
		return protectionState;
	}

	/**
	 * @param protectionState the protectionState to set
	 */
	public void setProtectionState(ProtectionState protectionState) {

		// Early exit if protection state is already set to input value

		if (this.protectionState.equals(protectionState))
			return;

		// if building is destroyed, just set the protection state.  There isn't a DB
		// record to write anything to.

		if (rank == -1) {
			this.protectionState = protectionState;
			return;
		}

		if (DbManager.BuildingQueries.UPDATE_PROTECTIONSTATE(this.getObjectUUID(), protectionState) == true) {
			this.protectionState = protectionState;
			return;
		}

		Logger.error("Protection update failed for UUID: " + this.getObjectUUID() + "\n" +
				this.getBlueprint().getName() + " From " + this.protectionState.name() + " To: " + protectionState.name());

	}

	public ConcurrentHashMap<Integer,Condemned> getCondemned() {
		return condemned;
	}

	public boolean setReverseKOS(boolean reverseKOS) {
		if (!DbManager.BuildingQueries.updateReverseKOS(this, reverseKOS))
			return false;
		this.reverseKOS = reverseKOS;
		return true;
	}

	public boolean assetIsProtected() {

		boolean outValue = false;

		if (protectionState.equals(ProtectionState.PROTECTED))
			outValue = true;

		if (protectionState.equals(ProtectionState.CONTRACT))
			outValue = true;

		return outValue;
	}

	public synchronized boolean transferGold(int amount,boolean tax){

		if (amount < 0)
			if (!this.hasFunds(-amount))
				return false;

		if (_strongboxValue + amount < 0)
			return false;

		if (_strongboxValue + amount > maxGold)
			return false;

		//Deduct Profit taxes.
		if (tax)
			if (taxType == TaxType.PROFIT && protectionState == ProtectionState.CONTRACT && amount > 0)
				amount = this.payProfitTaxes(amount);


		if (amount != 0)
			return this.setStrongboxValue(_strongboxValue + amount);
		return true;
	}

	public synchronized int payProfitTaxes(int amount){

		if (this.getCity() == null)
			return amount;
		if (this.getCity().getWarehouse() == null)
			return amount;

		if (this.getCity().getWarehouse().getResources().get(ItemBase.getGoldItemBase()) >= Warehouse.getMaxResources().get(ItemBase.getGoldItemBase().getUUID()))
			return amount;

		int profitAmount = (int) (amount * (taxAmount *.01f));

		if (this.getCity().getWarehouse().getResources().get(ItemBase.getGoldItemBase()) + profitAmount <= Warehouse.getMaxResources().get(ItemBase.getGoldItemBase().getUUID())){
			this.getCity().getWarehouse().depositProfitTax(ItemBase.getGoldItemBase(), profitAmount,this);
			return amount - profitAmount;
		}
		//overDrafting
		int warehouseDeposit =  Warehouse.getMaxResources().get(ItemBase.getGoldItemBase().getUUID()) - this.getCity().getWarehouse().getResources().get(ItemBase.getGoldItemBase());
		this.getCity().getWarehouse().depositProfitTax(ItemBase.getGoldItemBase(), warehouseDeposit,this);
		return amount - warehouseDeposit;
	}

	public synchronized boolean setReserve(int amount, PlayerCharacter player){

		if (!BuildingManager.playerCanManageNotFriends(player, this))
			return false;

		if (amount < 0)
			return false;

		if (!DbManager.BuildingQueries.SET_RESERVE(this, amount))
			return false;

		this.reserve = amount;

		return true;
	}

	public synchronized boolean hasFunds(int amount){
		return amount <= (this._strongboxValue - reserve);
	}

	public ArrayList<Vector3fImmutable> getPatrolPoints() {
		return patrolPoints;
	}

	public void setPatrolPoints(ArrayList<Vector3fImmutable> patrolPoints) {
		this.patrolPoints = patrolPoints;
	}

	public ArrayList<Vector3fImmutable> getSentryPoints() {
		return sentryPoints;
	}

	public void setSentryPoints(ArrayList<Vector3fImmutable> sentryPoints) {
		this.sentryPoints = sentryPoints;
	}

	public synchronized boolean addProtectionTax(Building building, PlayerCharacter pc, final TaxType taxType, int amount, boolean enforceKOS){
		if (building == null)
			return false;

		if (this.getBlueprint() == null)
			return false;

		if (this.getBlueprint().getBuildingGroup() != BuildingGroup.TOL)
			return false;

		if (building.assetIsProtected())
			return false;

		if (!DbManager.BuildingQueries.addTaxes(building, taxType, amount, enforceKOS))
			return false;

		building.taxType = taxType;
		building.taxAmount = amount;
		building.enforceKOS = enforceKOS;

		return true;

	}

	public synchronized boolean declineTaxOffer(){
		return true;
	}

	public synchronized boolean acceptTaxOffer(){
		return true;
	}

	public synchronized boolean acceptTaxes(){

		if (!DbManager.BuildingQueries.acceptTaxes(this))
			return false;

		this.setProtectionState(Enum.ProtectionState.CONTRACT);
		this.taxDateTime = LocalDateTime.now().plusDays(7);

		return true;
	}

	public synchronized boolean removeTaxes(){

		if (!DbManager.BuildingQueries.removeTaxes(this))
			return false;

		this.taxType = TaxType.NONE;
		this.taxAmount = 0;
		this.taxDateTime = null;
		this.enforceKOS = false;

		return true;
	}

	public boolean isTaxed(){
		if (this.taxType == TaxType.NONE)
			return false;
		if (this.taxAmount == 0)
			return false;
		return this.taxDateTime != null;
	}
}
