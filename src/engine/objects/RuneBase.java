// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum;
import engine.gameManager.DbManager;
import engine.net.ByteBufferWriter;
import engine.server.MBServerStatics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class RuneBase extends AbstractGameObject {

	private final String name;
	private final String description;
	private final int type;
	private final byte subtype;

	private final ConcurrentHashMap<Integer, Boolean> race = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private final ConcurrentHashMap<Integer, Boolean> baseClass = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private final ConcurrentHashMap<Integer, Boolean> promotionClass = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private final ConcurrentHashMap<Integer, Boolean> discipline = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private final ArrayList<Integer> overwrite = new ArrayList<>();
	private int levelRequired = 1;

	private ArrayList<MobBaseEffects> effectsList = new ArrayList<>();

	public static HashMap<Integer,ArrayList<Integer>> AllowedBaseClassRunesMap = new HashMap<>();
	public static HashMap<Integer,ArrayList<Integer>> AllowedRaceRunesMap = new HashMap<>();
	/**
	 * No Table ID Constructor
	 */
	public RuneBase(String name, String description, int type, byte subtype, ArrayList<RuneBaseAttribute> attrs) {
		super();

		this.name = name;
		this.description = description;
		this.type = type;
		this.subtype = subtype;

	}

	/**
	 * Normal Constructor
	 */
	public RuneBase(String name, String description, int type, byte subtype, ArrayList<RuneBaseAttribute> attrs, int newUUID) {
		super(newUUID);

		this.name = name;
		this.description = description;
		this.type = type;
		this.subtype = subtype;

	}

	/**
	 * ResultSet Constructor
	 */
	public RuneBase(ResultSet rs) throws SQLException {
		super(rs);

		this.name = rs.getString("name");
		this.description = rs.getString("description");
		this.type = rs.getInt("type");
		this.subtype = rs.getByte("subtype");

		DbManager.RuneBaseQueries.GET_RUNE_REQS(this);
		this.effectsList = DbManager.MobBaseQueries.GET_RUNEBASE_EFFECTS(this.getObjectUUID());
	}

	@Override
	public boolean equals(Object obj) {

		if (!super.equals(obj)) {
			return false;
		}

		if(obj instanceof RuneBase) {
			RuneBase rbObj = (RuneBase) obj;
			if (!this.name.equals(rbObj.name)) {
				return false;
			}

			if (!this.description.equals(rbObj.description)) {
				return false;
			}

			if (this.type != rbObj.type) {
				return false;
			}

			if (this.subtype != rbObj.subtype) {
				return false;
			}


			return true;
		}

		return false;
	}

	/*
	 * Getters
	 */
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getType() {
		return type;
	}

	/**
	 * @return the subtype
	 */
	public byte getSubtype() {
		return subtype;
	}

	/**
	 * @return the attrs
	 */
	public ArrayList<RuneBaseAttribute> getAttrs() {
		return RuneBaseAttribute.runeBaseAttributeMap.get(this.getObjectUUID());
	}

	public ConcurrentHashMap<Integer, Boolean> getRace() {
		return this.race;
	}

	public ConcurrentHashMap<Integer, Boolean> getBaseClass() {
		return this.baseClass;
	}

	public ConcurrentHashMap<Integer, Boolean> getPromotionClass() {
		return this.promotionClass;
	}

	public ConcurrentHashMap<Integer, Boolean> getDiscipline() {
		return this.discipline;
	}

	public ArrayList<Integer> getOverwrite() {
		return this.overwrite;
	}

	public int getLevelRequired() {
		return this.levelRequired;
	}

	public void setLevelRequired(int levelRequired) {
		this.levelRequired = levelRequired;
	}

	public static RuneBase getRuneBase(int tableId) {

		if (tableId == 0)
			return null;

		RuneBase rb = (RuneBase) DbManager.getFromCache(Enum.GameObjectType.RuneBase, tableId);

		if (rb != null)
			return rb;

		return DbManager.RuneBaseQueries.GET_RUNEBASE(tableId);
	}

	/*
	 * Serializing
	 */
	
	public static void serializeForClientMsg(RuneBase runeBase,ByteBufferWriter writer) {
		writer.putInt(runeBase.type);
		writer.putInt(0); // Pad
		writer.putInt(runeBase.getObjectUUID());
		writer.putInt(runeBase.getObjectType().ordinal());
		writer.putInt(runeBase.getObjectUUID());

	}

	@Override
	public void updateDatabase() {
		// TODO Auto-generated method stub
	}

	/**
	 * @return the effectsList
	 */
	public ArrayList<MobBaseEffects> getEffectsList() {
		return effectsList;
	}

	public static void LoadAllRuneBases(){

		DbManager.RuneBaseQueries.LOAD_ALL_RUNEBASES();
		RuneBase.AllowedBaseClassRunesMap = DbManager.RuneBaseQueries.LOAD_ALLOWED_STARTING_RUNES_FOR_BASECLASS();
		RuneBase.AllowedRaceRunesMap = DbManager.RuneBaseQueries.LOAD_ALLOWED_STARTING_RUNES_FOR_RACE();
	}

}
