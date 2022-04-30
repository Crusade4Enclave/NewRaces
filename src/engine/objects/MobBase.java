// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import ch.claude_martin.enumbitset.EnumBitSet;
import engine.Enum;
import engine.gameManager.DbManager;
import engine.server.MBServerStatics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class MobBase extends AbstractGameObject {

	private final int loadID;
	private final String firstName;
	private final byte level;
	private float healthMax;
	private int attackRating;
	private int defenseRating;
	private float damageMin;
	private float damageMax;
	private float hitBoxRadius;
	private final int lootTable;
	private final float scale;

	private int minGold;
	private int maxGold;

	private EnumBitSet<Enum.MobFlagType> flags;
	private EnumBitSet<Enum.AggroType> noAggro;
	private int mask;

	private int goldMod;
	private int seeInvis;
	private int spawnTime = 0;
	private int defense = 0;
	private int atr = 0;
	private float minDmg = 0;
	private float maxDmg = 0;
	private ArrayList<MobBaseEffects> raceEffectsList;
	private float attackRange;
	private boolean isNecroPet = false;

	private MobBaseStats mobBaseStats;
	private ArrayList<RuneBase> runes;
	private HashMap<Integer, Integer> staticPowers;

	private float walk = 0;
	private float run = 0;
	private float walkCombat = 0;
	private float runCombat = 0;

	/**
	 * ResultSet Constructor
	 */
	public MobBase(ResultSet rs) throws SQLException {
		super(rs, rs.getInt("ID"));

		this.loadID = rs.getInt("loadID");

		this.firstName = rs.getString("name");
		this.level = rs.getByte("level");
		this.lootTable = rs.getInt("lootTableID");

		this.goldMod = rs.getInt("goldMod");
		this.spawnTime = rs.getInt("spawnTime");

		LevelDefault levelDefault = LevelDefault.getLevelDefault(this.level);
		this.healthMax = rs.getInt("health");
		this.damageMin = rs.getFloat("minDmg");
		this.damageMax = rs.getFloat("maxDmg");

		this.attackRating = rs.getInt("atr");
		this.defenseRating = rs.getInt("defense");
		this.attackRange = rs.getFloat("attackRange");

		if (MobbaseGoldEntry.MobbaseGoldMap.containsKey(this.loadID)){
			MobbaseGoldEntry goldEntry = MobbaseGoldEntry.MobbaseGoldMap.get(this.loadID);

			if (goldEntry != null){
				this.minGold = goldEntry.getMin();
				this.maxGold = goldEntry.getMax();
			}
		}
		else
			if (levelDefault != null) {
				this.minGold = (levelDefault.goldMin * this.goldMod / 100);
				this.maxGold = (levelDefault.goldMax * this.goldMod / 100);
			} else {
				this.minGold = 10;
				this.maxGold = 30;
			}

		this.flags = EnumBitSet.asEnumBitSet(rs.getLong("flags"), Enum.MobFlagType.class);
		this.noAggro = EnumBitSet.asEnumBitSet(rs.getLong("noaggro"), Enum.AggroType.class);

		this.seeInvis = rs.getInt("seeInvis");
		this.scale = rs.getFloat("scale");
		this.hitBoxRadius = 5f;
		this.mask = 0;

		if (this.getObjectUUID() == 12021 || this.getObjectUUID() == 12022) {
			this.isNecroPet = true;
		}

		if (Enum.MobFlagType.HUMANOID.elementOf(this.flags))
			this.mask += MBServerStatics.MASK_HUMANOID;

		if (Enum.MobFlagType.UNDEAD.elementOf(this.flags))
			this.mask += MBServerStatics.MASK_UNDEAD;

		if (Enum.MobFlagType.BEAST.elementOf(this.flags))
			this.mask += MBServerStatics.MASK_BEAST;

		if (Enum.MobFlagType.DRAGON.elementOf(this.flags))
			this.mask += MBServerStatics.MASK_DRAGON;

		if (Enum.MobFlagType.RAT.elementOf(this.flags))
			this.mask += MBServerStatics.MASK_RAT;

		this.runes = DbManager.MobBaseQueries.LOAD_RUNES_FOR_MOBBASE(this.loadID);
		this.raceEffectsList = DbManager.MobBaseQueries.LOAD_STATIC_EFFECTS(this.loadID);
		this.mobBaseStats = DbManager.MobBaseQueries.LOAD_STATS(this.loadID);
		DbManager.MobBaseQueries.LOAD_ALL_MOBBASE_LOOT(this.loadID);
		DbManager.MobBaseQueries.LOAD_ALL_MOBBASE_SPEEDS(this);

	}

	public static HashMap<Integer, MobEquipment> loadEquipmentSet(int equipmentSetID){

		ArrayList<EquipmentSetEntry> equipList;
		HashMap<Integer, MobEquipment> equip = new HashMap<>();

		if (equipmentSetID == 0)
			return equip;

		equipList = EquipmentSetEntry.EquipmentSetMap.get(equipmentSetID);

		if (equipList == null)
			return equip;

		for (EquipmentSetEntry equipmentSetEntry : equipList) {

			MobEquipment mobEquipment = new MobEquipment(equipmentSetEntry.getItemID(), equipmentSetEntry.getDropChance());
			ItemBase itemBase = mobEquipment.getItemBase();

			if (itemBase != null) {
				if (itemBase.getType().equals(Enum.ItemType.WEAPON))
					if (mobEquipment.getSlot() == 1 && itemBase.getEquipFlag() == 2)
						mobEquipment.setSlot(2);

				equip.put(mobEquipment.getSlot(), mobEquipment);
			}
		}

		return equip;
	}

	public HashMap<Integer, Integer> getStaticPowers() {
		return staticPowers;
	}

	public void updateStaticEffects() {
		this.raceEffectsList = DbManager.MobBaseQueries.LOAD_STATIC_EFFECTS(this.getObjectUUID());
	}

	public void updateRunes() {
		this.runes = DbManager.MobBaseQueries.LOAD_RUNES_FOR_MOBBASE(this.getObjectUUID());
	}

	public void updatePowers() {
		this.staticPowers = DbManager.MobBaseQueries.LOAD_STATIC_POWERS(this.getObjectUUID());
	}

	public void updateSpeeds(float walk, float walkCombat,float run, float runCombat){
		this.walk = walk;
		this.walkCombat = walkCombat;
		this.run = run;
		this.runCombat = runCombat;

	}

	/*
	 * Getters
	 */
	public String getFirstName() {
		return this.firstName;
	}

	public int getLoadID() {
		return this.loadID;
	}

	public int getLevel() {
		return this.level;
	}

	public int getLootTable() {
		return this.lootTable;
	}

	public float getHealthMax() {
		return this.healthMax;
	}

	public float getDamageMin() {
		return this.damageMin;
	}

	public float getDamageMax() {
		return this.damageMax;
	}

	public int getAttackRating() {
		return this.attackRating;
	}

	public int getDefenseRating() {
		return this.defenseRating;
	}

	public int getMinGold() {
		return this.minGold;
	}

	public int getMaxGold() {
		return this.maxGold;
	}

	public EnumBitSet<Enum.MobFlagType> getFlags() {
		return this.flags;
	}

	public EnumBitSet getNoAggro() {
		return this.noAggro;
	}

	public int getGoldMod() {
		return this.goldMod;
	}

	public float getScale() {
		return this.scale;
	}

	public int getTypeMasks() {
		return this.mask;
	}

	public int getSeeInvis() {
		return this.seeInvis;
	}

	public int getSpawnTime() {
		return this.spawnTime;
	}



	/*
	 * Database
	 */
	public static MobBase getMobBase(int id) {
		return MobBase.getMobBase(id, false);
	}

	public static MobBase getMobBase(int id, boolean forceDB) {
		return DbManager.MobBaseQueries.GET_MOBBASE(id, forceDB);
	}

	public static MobBase copyMobBase(MobBase mobbase, String name) {
		return DbManager.MobBaseQueries.COPY_MOBBASE(mobbase, name);
	}

	public static boolean renameMobBase(int ID, String newName) {
		return DbManager.MobBaseQueries.RENAME_MOBBASE(ID, newName);
	}

	@Override
	public void updateDatabase() {
		// TODO Create update logic.
	}

	public float getHitBoxRadius() {
		if (this.hitBoxRadius < 0f) {
			return 0f;
		} else {
			return this.hitBoxRadius;
		}
	}

	public MobBaseStats getMobBaseStats() {
		return mobBaseStats;
	}

	public float getMaxDmg() {
		return maxDmg;
	}

	public float getMinDmg() {
		return minDmg;
	}

	public int getAtr() {
		return atr;
	}

	public void setAtr(int atr) {
		this.atr = atr;
	}

	public int getDefense() {
		return defense;
	}

	public void setDefense(int defense) {
		this.defense = defense;
	}

	/**
	 * @return the raceEffectsList
	 */
	public ArrayList<MobBaseEffects> getRaceEffectsList() {
		return raceEffectsList;
	}

	/**
	 * @return the runes
	 */
	public ArrayList<RuneBase> getRunes() {
		return runes;
	}

	public float getAttackRange() {
		return attackRange;
	}

	public boolean isNecroPet() {
		return isNecroPet;
	}

	public static int GetClassType(int mobbaseID){

		switch (mobbaseID){
		case 17235:
		case 17233:
		case 17256:
		case 17259:
		case 17260:
		case 17261:
			return 2518;
		case 17258:
		case 17257:
		case 17237:
		case 17234:
			return 2521;
		default:
			return 2518;
		}
	}

	public float getWalk() {
		return walk;
	}

	public void setWalk(float walk) {
		this.walk = walk;
	}

	public float getRun() {
		return run;
	}

	public void setRun(float run) {
		this.run = run;
	}

	public float getWalkCombat() {
		return walkCombat;
	}

	public float getRunCombat() {
		return runCombat;
	}

}
