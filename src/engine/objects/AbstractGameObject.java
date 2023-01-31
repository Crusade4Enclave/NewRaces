// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.GameObjectType;
import engine.ai.StaticMobActions;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.DatabaseUpdateJob;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;


public abstract class AbstractGameObject {
	private GameObjectType objectType = GameObjectType.unknown;
	private int objectUUID;

	private byte ver = 1;

	private ConcurrentHashMap<String, JobContainer> databaseJobs = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

	/**
	 * No Table ID Constructor
	 */
	public AbstractGameObject() {
		super();
		setObjectType();
		this.objectUUID = MBServerStatics.NO_DB_ROW_ASSIGNED_YET;
	}

	/**
	 * Normal Constructor
	 */
	public AbstractGameObject(int objectUUID) {
		this();
		this.objectUUID = objectUUID;
	}

	/**
	 * ResultSet Constructor
	 *
	 * @param rs
	 *            ResultSet containing record for this object
     */
	public AbstractGameObject(ResultSet rs, int objectUUID) throws SQLException {
		this();
		this.objectUUID = objectUUID;
	}

	/**
	 * ResultSet Constructor; assumes first column in ResultSet is ID
	 *
	 * @param rs
	 *            ResultSet containing record for this object
	 */
	public AbstractGameObject(ResultSet rs) throws SQLException {
		this(rs, rs.getInt(1));
	}

	/*
	 * Getters
	 */
	public GameObjectType getObjectType() {
		return this.objectType;
	}

	protected final void setObjectType() {
		try {
          this.objectType = GameObjectType.valueOf(this.getClass().getSimpleName());
		} catch (SecurityException | IllegalArgumentException e) {
			Logger.error("Failed to find class " + this.getClass().getSimpleName()
					+ " in GameObjectTypes file. Defaulting ObjectType to 0.");
		}
	}
	
	public int getObjectUUID() {
		return this.objectUUID;
	}

	protected void setObjectUUID(int objectUUID) {
		this.objectUUID = objectUUID;
	}

	public byte getVer() {
		return this.ver;
	}

	public void incVer() {
		this.ver++;
		if (this.ver == (byte)-1) //-1 reserved
			this.ver++;
	}

	/*
	 * Util
	 */

	public static int extractUUID(GameObjectType type, long compositeID) {
		if (type == null || type == GameObjectType.unknown || compositeID == 0L) {
			return -1;
		}
		int out = (int) compositeID;
		if (out > Long.MAX_VALUE || out < 0) {
			Logger.error("There was a problem reverse calculating a UUID from a compositeID. \tcomposID: "
					+ compositeID + " \ttype: " + type.toString() + "\tresult: " + out);
		}
		return out;
	}

	public static GameObjectType extractTypeID(long compositeID) {
                int ordinal = (int) (compositeID >>> 32);
		return GameObjectType.values()[ordinal];
	}

	public boolean equals(AbstractGameObject obj) {
		
            if (obj == null)
			return false;

        if (obj.objectType != this.objectType) {
			return false;
		}

        return obj.getObjectUUID() == this.getObjectUUID();
    }

	public void removeFromCache() {
		DbManager.removeFromCache(this);
	}

	/**
	 * Generates a {@link PreparedStatementShared} based on the specified query.
	 * <p>
	 * If {@link AbstractGameObject} Database functions  will properly release
	 * the PreparedStatementShared upon completion. If these functions are not
	 * used, then {@link PreparedStatementShared#release release()} must be
	 * called when finished with this object.
	 *
	 * @param sql
	 *            The SQL string used to generate the PreparedStatementShared
	 * @return {@link PreparedStatementShared}
	 * @throws {@link SQLException}
	 **/
	protected static PreparedStatementShared prepareStatement(String sql) throws SQLException {
		return new PreparedStatementShared(sql);
	}

	public ConcurrentHashMap<String, JobContainer> getDatabaseJobs() {
		return this.databaseJobs;
	}

	public void addDatabaseJob(String type, int duration) {
                DatabaseUpdateJob updateJob;
                
		if (databaseJobs.containsKey(type))
			return;
                
		updateJob = new DatabaseUpdateJob(this, type);
		JobContainer jc = JobScheduler.getInstance().scheduleJob(updateJob, duration);
		databaseJobs.put(type, jc);
	}

	public void removeDatabaseJob(String type, boolean canceled) {
		if (databaseJobs.containsKey(type)) {
			if (canceled) {
				JobContainer jc = databaseJobs.get(type);
				if (jc != null)
					jc.cancelJob();
			}
			databaseJobs.remove(type);
		}
	}
	
	public static AbstractGameObject getFromTypeAndID(long compositeID) {
		int objectTypeID = extractTypeOrdinal(compositeID);
		int tableID = extractTableID(objectTypeID, compositeID);
		GameObjectType objectType = GameObjectType.values()[objectTypeID];

		switch (objectType) {
		case PlayerCharacter:
			return PlayerCharacter.getPlayerCharacter(tableID);

		case NPC:
			return NPC.getNPC(tableID);

		case Mob:
			return StaticMobActions.getMob(tableID);

		case Building:
			return BuildingManager.getBuilding(tableID);

		case Guild:
			return Guild.getGuild(tableID);

		case Item:
			return Item.getFromCache(tableID);

		case MobLoot:
			return MobLoot.getFromCache(tableID);

		default:
			Logger.error("Failed to convert compositeID to AbstractGameObject. " + "Unsupported type encountered. "
					+ "CompositeID: " + compositeID + " ObjectType: 0x" + Integer.toHexString(objectTypeID) + " TableID: " + tableID);
		}
		return null;
	}
	
	public static int extractTypeOrdinal(long compositeID) {
		return (int) (compositeID >>> 32);
	}
	public static int extractTableID(int type, long compositeID) {
		if (type == 0 || compositeID == 0L) {
			return -1;
		}
        return (int) compositeID;
	}

	/*
	 * Abstract Methods
	 */
	
	public abstract void updateDatabase();
}
