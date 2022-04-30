// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.DispatchChannel;
import engine.Enum.EffectSourceType;
import engine.Enum.GameObjectType;
import engine.Enum.GridObjectType;
import engine.InterestManagement.HeightMap;
import engine.InterestManagement.WorldGrid;
import engine.job.AbstractScheduleJob;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.NoTimeJob;
import engine.math.AtomicFloat;
import engine.math.Bounds;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.UpdateEffectsMsg;
import engine.powers.EffectsBase;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractWorldObject extends AbstractGameObject {

	private String name = "";

	protected final ReadWriteLock locationLock = new ReentrantReadWriteLock(true);
	protected final ReadWriteLock updateLock = new ReentrantReadWriteLock(true);
	
	protected Vector3fImmutable loc = new Vector3fImmutable(0.0f, 0.0f, 0.0f);
	private byte tier = 0;
	private Vector3f rot = new Vector3f(0.0f, 0.0f, 0.0f);
	protected AtomicFloat health = new AtomicFloat();
	public float healthMax;
	protected boolean load = true;
	protected ConcurrentHashMap<String, Effect> effects = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private int objectTypeMask = 0;
	private Bounds bounds;
	
	public int gridX = -1;
	public int gridZ = -1;
	
	protected GridObjectType gridObjectType;
	
	protected float altitude = 0;
	protected Regions region;
	protected boolean movingUp = false;
	public Regions landingRegion = null;
	public Vector3fImmutable lastLoc = Vector3fImmutable.ZERO;

	/**
	 * No Id Constructor
	 */
	public AbstractWorldObject() {
		super();
	}

	/**
	 * Normal Constructor
	 */
	public AbstractWorldObject(int objectUUID) {
		super(objectUUID);
	}

	
	/**
	 * ResultSet Constructor
	 */
	public AbstractWorldObject(ResultSet rs) throws SQLException {
		super(rs);
	}

	//this should be called to handle any after load functions.
	public abstract void runAfterLoad();

	/*
	 * Getters
	 */
	public float getHealth() {


		return this.health.get();

	}
	public float getCurrentHitpoints(){
		return this.health.get();
	}

	public float getHealthMax() {
		return this.healthMax;
	}

	public ConcurrentHashMap<String, Effect> getEffects() {
		return this.effects;
	}

	//Add new effect
	public void addEffect(String name, int duration, AbstractScheduleJob asj, EffectsBase eb, int trains) {

		if (!isAlive() && eb.getToken() != 1672601862) {
			return;
		}
		JobContainer jc = JobScheduler.getInstance().scheduleJob(asj, duration);
		Effect eff = new Effect(jc, eb, trains);
		this.effects.put(name, eff);
		applyAllBonuses();
	}

	public Effect addEffectNoTimer(String name, EffectsBase eb, int trains, boolean isStatic) {
		NoTimeJob ntj = new NoTimeJob(this, name, eb, trains); //infinite timer

		if (this.getObjectType() == GameObjectType.Item || this.getObjectType() == GameObjectType.City){
			ntj.setEffectSourceType(this.getObjectType().ordinal());
			ntj.setEffectSourceID(this.getObjectUUID());
		}

		JobContainer jc = new JobContainer(ntj);
		Effect eff = new Effect(jc, eb, trains);
		if (isStatic)
			eff.setIsStatic(isStatic);
		this.effects.put(name, eff);
		applyAllBonuses();
		return eff;
	}

	//called when an effect runs it's course
	public void endEffect(String name) {

		Effect eff = this.effects.get(name);
		if (eff == null) {
			return;
		}
		if (!isAlive() && eff.getEffectsBase().getToken() != 1672601862) {
			return;
		}

		if (eff.cancel()) {

			eff.endEffect();
			this.effects.remove(name);
			if (this.getObjectType().equals(GameObjectType.PlayerCharacter))
			if (name.equals("Flight")){
				((PlayerCharacter)this).update();
				PlayerCharacter.GroundPlayer((PlayerCharacter)this);
			}
		}
		applyAllBonuses();
	}

	public void endEffectNoPower(String name) {

		Effect eff = this.effects.get(name);
		if (eff == null) {
			return;
		}
		if (!isAlive() && eff.getEffectsBase().getToken() != 1672601862) {
			return;
		}

		if (eff.cancel()) {
			eff.cancelJob();
			eff.endEffectNoPower();
			this.effects.remove(name);
		}
		applyAllBonuses();
	}

	//Called to cancel an effect prematurely.
	public void cancelEffect(String name, boolean overwrite) {

		Effect eff = this.effects.get(name);
		if (eff == null) {
			return;
		}
		if (!isAlive() && eff.getEffectsBase().getToken() != 1672601862) {
			return;
		}

		if (eff.cancel()) {
			eff.cancelJob();
			this.effects.remove(name);
			if (AbstractWorldObject.IsAbstractCharacter(this)) {
				((AbstractCharacter) this).cancelLastChantIfSame(eff);
			}
		}
		if (!overwrite) {
			applyAllBonuses();
		}
	}

	//Called when an object dies/is destroyed
	public void clearEffects() {
		for (String name : this.effects.keySet()) {
			Effect eff = this.effects.get(name);
			if (eff == null) {
				return;
			}

			//Dont remove deathshroud here!
			if (eff.getEffectToken() == 1672601862)
				continue;

			if (eff.cancel()) {
				if (eff.getPower() == null) {
					if (!eff.isStatic())
						eff.endEffectNoPower();
				}
				if (!eff.isStatic())
					eff.cancelJob();
			}

			this.effects.remove(name);
		}
		if (AbstractWorldObject.IsAbstractCharacter(this)) {
			((AbstractCharacter) this).cancelLastChant();
		}
		applyAllBonuses();
	}

	public void removeEffectBySource(EffectSourceType source, int trains, boolean removeAll) {
		if (!isAlive() && source.equals(EffectSourceType.DeathShroud) == false) {
			return;
		}

		//hacky way to dispell trebs.
		if (this.getObjectType() == GameObjectType.Mob){
			Mob mob = (Mob)this;
			if (mob.isSiege()){
				if (mob.isPet()){
					PlayerCharacter petOwner = mob.getOwner();
					if (petOwner != null && source.equals(EffectSourceType.Effect)){
						petOwner.dismissPet();
						return;
					}
				}
			}
		}
		boolean changed = false;
		String toRemove = "";
		int toRemoveToken = Integer.MAX_VALUE;
		for (String name : this.effects.keySet()) {
			Effect eff = this.effects.get(name);
			if (eff == null) {
				continue;
			}
			if (eff.containsSource(source) && trains >= eff.getTrains()) {
				if (removeAll) {
					//remove all effects of source type
					if (eff.cancel()) {
						eff.cancelJob();
					}
					this.effects.remove(name);
					changed = true;
					
					if (source.equals("Flight")){
						//ground player
						if (this.getObjectType().equals(GameObjectType.PlayerCharacter)){
							((PlayerCharacter)this).update();
							PlayerCharacter.GroundPlayer((PlayerCharacter)this);
						}
					}
				} else {
					//find lowest token of source type to remove
					int tok = eff.getEffectToken();
					if (tok != 0 && tok < toRemoveToken) {
						toRemove = name;
						toRemoveToken = tok;
					}
				}
			}
		}
		
		//WTF IS THIS?
		if (toRemoveToken < Integer.MAX_VALUE && this.effects.containsKey(toRemove)) {
			//remove lowest found token of source type
			Effect eff = this.effects.get(toRemove);
			if (eff != null) {
				changed = true;
				if (eff.cancel()) {
					eff.cancelJob();
				}
				this.effects.remove(toRemove);
				
				if (source.equals("Flight")){
					//ground player
					if (this.getObjectType().equals(GameObjectType.PlayerCharacter)){
						((PlayerCharacter)this).update();
						PlayerCharacter.GroundPlayer((PlayerCharacter)this);
					}
				}
				
			}
		}
		if (changed) {
			applyAllBonuses();
		}
	}

	public void sendAllEffects(ClientConnection cc) {
		UpdateEffectsMsg msg = new UpdateEffectsMsg(this);
		Dispatch dispatch = Dispatch.borrow((PlayerCharacter)this, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
	}

	public void applyAllBonuses() {
		if (AbstractWorldObject.IsAbstractCharacter(this)) {
			((AbstractCharacter) this).applyBonuses();
		}
	}

	public JobContainer getEffectJobContainer(String name) {
		Effect ef = this.effects.get(name);
		if (ef != null) {
			return ef.getJobContainer();
		}
		return null;
	}

	public AbstractScheduleJob getEffectJob(String name) {
		Effect ef = this.effects.get(name);
		if (ef == null) {
			return null;
		}
		JobContainer jc = ef.getJobContainer();
		if (jc != null) {
			return (AbstractScheduleJob) jc.getJob();
		}
		return null;
	}

	public boolean containsEffect(int token) {
		for (Effect eff : this.effects.values()) {
			if (eff != null) {
				if (eff.getEffectsBase() != null) {
					if (eff.getEffectsBase().getToken() == token) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public int getObjectTypeMask() {
		return objectTypeMask;
	}


	public Vector3fImmutable getLoc() {
		return this.loc;
	}

	public Vector3f getRot() {
		return rot;
	}

	public byte getTier() {
		return tier;
	}

	public boolean isAlive() {
		if (AbstractWorldObject.IsAbstractCharacter(this)) {
			return this.isAlive();
		} else if (this.getObjectType().equals(GameObjectType.Building)) {
			return (!(((Building) this).getRank() < 0));
		} else {
			return true;
		}
	}

	/*
	 * Setters
	 */
	public void setObjectTypeMask(int mask) {
		this.objectTypeMask = mask;
	}

	//TODO return false if something goes wrong? resync player?
	public void setLoc(Vector3fImmutable loc) {
		locationLock.writeLock().lock();
		try {
			if (Float.isNaN(loc.x) || Float.isNaN(loc.z))
				return;
			
			if (loc.equals(Vector3fImmutable.ZERO))
				return;
			
			if (loc.x > MBServerStatics.MAX_WORLD_WIDTH || loc.z < MBServerStatics.MAX_WORLD_HEIGHT)
				return;
			this.lastLoc = new Vector3fImmutable(this.loc);
			this.loc = loc;
			this.loc = this.loc.setY(HeightMap.getWorldHeight(this) + this.getAltitude());
			
			//lets not add mob to world grid if he is currently despawned.
			if (this.getObjectType().equals(GameObjectType.Mob) && ((Mob)this).despawned)
				return;
			
			//do not add objectUUID 0 to world grid. dunno da fuck this doing why its doing but its doing... da fuck.
			if (this.getObjectUUID() == 0)
				return;
			WorldGrid.addObject(this,loc.x,loc.z);
			
		}catch(Exception e){
			Logger.error("Failed to set location for World Object. Type = " + this.getObjectType().name() + " : Name = " + this.getName());
			e.printStackTrace();
		} finally {
			locationLock.writeLock().unlock();
		}
		
	}
	
	public void setY(float y){
		this.loc = this.loc.setY(y);
	}


	public void setRot(Vector3f rotation) {
		synchronized (this.rot) {
			this.rot = rotation;
		}
	}

	public void setTier(byte tier) {
		synchronized (this.rot) {
			this.tier = tier;
		}
	}

	public static int getType() {
		return 0;
	}

	public boolean load() {
		return this.load;
	}

	/*
	 * Utils
	 */
	public String getName() {
		if (this.name.length() == 0) {
			return "Unnamed " + '('
					+ this.getObjectUUID() + ')';
		} else {
			return this.name;
		}
	}

	public String getSimpleName() {
		return this.name;
	}


	/**
	 * @return the bounds
	 */
	public Bounds getBounds() {
		return bounds;
	}

	public void setBounds(Bounds bounds) {

		this.bounds = bounds;
	}

	/**
	 * @param health the health to set
	 */
	public void setHealth(float health) {

		this.health.set(health);
	}

	public static boolean IsAbstractCharacter(AbstractWorldObject awo){
		
		if (awo == null)
			return false;

		if (awo.getObjectType() == GameObjectType.PlayerCharacter || awo.getObjectType() == GameObjectType.Mob || awo.getObjectType() == GameObjectType.NPC)
			return true;
		return false;
	}

	public static void RemoveFromWorldGrid(AbstractWorldObject gridObjectToRemove){
		if (gridObjectToRemove.gridX < 0 || gridObjectToRemove.gridZ < 0)
			return;
		
		ConcurrentHashMap<Integer,AbstractWorldObject> gridMap;
		switch(gridObjectToRemove.gridObjectType){
		case STATIC:
			gridMap = WorldGrid.StaticGridMap[gridObjectToRemove.gridX][gridObjectToRemove.gridZ];
			break;
		case DYNAMIC:
				gridMap = WorldGrid.DynamicGridMap[gridObjectToRemove.gridX][gridObjectToRemove.gridZ];
			break;
		default:
			gridMap = WorldGrid.StaticGridMap[gridObjectToRemove.gridX][gridObjectToRemove.gridZ];
			break;
		
		}
		
		if (gridMap == null){
			Logger.info("Null gridmap for Object UUD: " + gridObjectToRemove);
			return;
		}
			
		
	
		
		gridMap.remove(gridObjectToRemove.getObjectUUID());
		gridObjectToRemove.gridX = -1;
		gridObjectToRemove.gridZ = -1;
		
	}
	
	public static boolean AddToWorldGrid(AbstractWorldObject gridObjectToAdd, int x, int z){
		try{
			
			ConcurrentHashMap<Integer,AbstractWorldObject> gridMap;
            if (gridObjectToAdd.gridObjectType.equals(GridObjectType.STATIC))
				gridMap = WorldGrid.StaticGridMap[x][z];
			else
				gridMap = WorldGrid.DynamicGridMap[x][z];
			
			gridMap.put(gridObjectToAdd.getObjectUUID(),gridObjectToAdd);
			gridObjectToAdd.gridX = x;
			gridObjectToAdd.gridZ = z;
			return true;
		}catch(Exception e){
			Logger.error(e);
			return false;
		}
		
	}
	
	public static Regions GetRegionByWorldObject(AbstractWorldObject worldObject){
		Regions region = null;
		
		if (worldObject.getObjectType().equals(GameObjectType.PlayerCharacter))
			if (((PlayerCharacter)worldObject).isFlying())
				return null;
		//Find building
		for (AbstractWorldObject awo:WorldGrid.getObjectsInRangePartial(worldObject.getLoc(), MBServerStatics.STRUCTURE_LOAD_RANGE, MBServerStatics.MASK_BUILDING)){
		Building building = (Building)awo;
		if (!Bounds.collide(worldObject.getLoc(), building.getBounds()))
			continue;
		
		//find regions that intersect x and z, check if object can enter.
		for (Regions toEnter: building.getBounds().getRegions()){
			if (toEnter.isPointInPolygon(worldObject.getLoc())){
				if (Regions.CanEnterRegion(worldObject, toEnter))
					if (region == null)
						region = toEnter;
					else // we're using a low level to high level tree structure, database not always in order low to high.
						//check for highest level index.
					if(region != null && toEnter.highLerp.y > region.highLerp.y)
					region = toEnter;
					
				
			}
		}
	}
	
		//set players new altitude to region lerp altitude.
		if (region != null)
			if (region.center.y == region.highLerp.y)
				worldObject.loc = worldObject.loc.setY(region.center.y + worldObject.getAltitude());
			else
			worldObject.loc = worldObject.loc.setY(region.lerpY(worldObject) + worldObject.getAltitude());
		
		return region;
	}
	
	public static Regions GetRegionFromBuilding(Vector3fImmutable worldLoc, Building building){
		Regions region = null;
		
		
		return region;
	}

	public float getAltitude() {
		return altitude;
	}


	public ReadWriteLock getUpdateLock() {
		return updateLock;
	}

	public GridObjectType getGridObjectType() {
		return gridObjectType;
	}

	public Regions getRegion() {
		return region;
	}


	public boolean isMovingUp() {
		return movingUp;
	}

	public void setMovingUp(boolean movingUp) {
		this.movingUp = movingUp;
	}

	public void setRegion(Regions region) {
		this.region = region;
	}
	
	//used for interestmanager loading and unloading objects to client.
	// if not in grid, unload from player.
	public boolean isInWorldGrid(){
		if (this.gridX == -1 && this.gridZ == -1)
			return false;
		
		return true;
	}
	

}
