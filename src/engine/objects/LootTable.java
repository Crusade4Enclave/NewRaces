// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.ItemContainerType;
import engine.Enum.ItemType;
import engine.Enum.OwnerType;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class LootTable {

	private static final ConcurrentHashMap<Integer, LootTable> lootGroups = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private static final ConcurrentHashMap<Integer, LootTable> lootTables = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private static final ConcurrentHashMap<Integer, LootTable> modTables = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private static final ConcurrentHashMap<Integer, LootTable> modGroups = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private static final ConcurrentHashMap<Integer, Integer> statRuneChances = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private final ConcurrentHashMap<Integer, LootRow> lootTable = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);



	private static final int oneDrop = 95;
	private static final int twoDrop = 100;
	private static final int noDropHotZone = 79;
	private static final int oneDropHotZone = 98;

	public float minRoll = 320;
	public float maxRoll = 1;
	public static boolean initialized = false;

	public int lootTableID = 0;

	public static HashMap<ItemBase,Integer> itemsDroppedMap = new HashMap<>();
	public static HashMap<ItemBase,Integer> resourceDroppedMap = new HashMap<>();
	public static HashMap<ItemBase,Integer> runeDroppedMap = new HashMap<>();
	public static HashMap<ItemBase,Integer> contractDroppedMap = new HashMap<>();
	public static HashMap<ItemBase,Integer> glassDroppedMap = new HashMap<>();

	public static int rollCount = 0;
	public static int dropCount = 0;
	public static int runeCount = 0;
	public static int contractCount = 0;
	public static int resourceCount = 0;
	public static int glassCount = 0;


	/**
	 * Generic Constructor
	 */
	public LootTable(int lootTableID) {
		this.lootTableID = lootTableID;
	}

	public void addRow(float min, float max, int valueOne, int valueTwo, int valueThree, String action) {

		//hackey way to set the minimum roll for SHIAT!
		if (min < this.minRoll)
			this.minRoll = min;

		if (max > this.maxRoll)
			this.maxRoll = max;

		int minInt = (int) min;
		int maxInt = (int) max;

		//Round up min
		if (minInt != min){
			min = minInt + 1;
		}

		//Round down max;
		if (maxInt != max)
			max = maxInt;



		LootRow lootRow = new LootRow(valueOne, valueTwo, valueThree, action);
		for (int i = (int) min; i <= max; i++) {
			lootTable.put(i, lootRow);
		}
	}

	public static LootTable getLootGroup(int UUID) {

		if (lootGroups.containsKey(UUID))
			return lootGroups.get(UUID);

		LootTable lootGroup = new LootTable(UUID);
		lootGroups.put(UUID, lootGroup);
		return lootGroup;
	}

	public static LootTable getLootTable(int UUID) {

		if (lootTables.containsKey(UUID))
			return lootTables.get(UUID);

		LootTable lootTable = new LootTable(UUID);
		lootTables.put(UUID, lootTable);
		return lootTable;
	}

	/**
	 * @return the lootGroups
	 */
	public static ConcurrentHashMap<Integer, LootTable> getLootGroups() {
		return lootGroups;
	}

	/**
	 * @return the lootTables
	 */
	public static ConcurrentHashMap<Integer, LootTable> getLootTables() {
		return lootTables;
	}

	/**
	 * @return the modTables
	 */
	public static ConcurrentHashMap<Integer, LootTable> getModTables() {
		return modTables;
	}

	/**
	 * @return the modGroups
	 */
	public static ConcurrentHashMap<Integer, LootTable> getModGroups() {
		return modGroups;
	}


	public static LootTable getModGroup(int UUID) {
		if (modGroups.containsKey(UUID))
			return modGroups.get(UUID);
		LootTable modTable = new LootTable(UUID);
		modGroups.put(UUID, modTable);
		return modTable;
	}

	public static LootTable getModTable(int UUID) {
		if (modTables.containsKey(UUID))
			return modTables.get(UUID);
		LootTable modTypeTable = new LootTable(UUID);
		modTables.put(UUID, modTypeTable);
		return modTypeTable;
	}


	public LootRow getLootRow(int probability) {
		if (lootTable.containsKey(probability))
			return lootTable.get(probability);
		return null;
	}

	//call this on server startup to populate the tables
	public static void populateLootTables() {
		DbManager.LootQueries.populateLootGroups();
		DbManager.LootQueries.populateLootTables();
		DbManager.LootQueries.populateModTables();
		DbManager.LootQueries.populateModGroups();

		//preset chances for rune drops
		populateStatRuneChances();
	}

	//Returns a list of random loot for a mob based on level, lootTable and hotzone
	public static ArrayList<MobLoot> getMobLoot(Mob mob, int mobLevel, int lootTable, boolean hotzone) {

		// Member variable declaration
		ArrayList<MobLoot> loot;
		int calculatedLootTable;
		int roll;

		// Member variable assignment
		loot = new ArrayList<>();

		// Setup default loot table if none exists
		calculatedLootTable = lootTable;

		LootTable.rollCount++;
		if (MobLootBase.MobLootSet.get(mob.getMobBase().getLoadID()).isEmpty()){


			roll = ThreadLocalRandom.current().nextInt(100);
			if (roll > 90)
				if (roll > LootTable.oneDropHotZone)
					addMobLoot(mob, loot, mobLevel, calculatedLootTable, 1, true);
				else
					addMobLoot(mob, loot, mobLevel, calculatedLootTable, 1, true);
		}else{
			for (MobLootBase mlb:MobLootBase.MobLootSet.get(mob.getMobBase().getLoadID())){


				float chance = mlb.getChance() *.01f;

				chance *= MBServerStatics.DROP_RATE_MOD;

				calculatedLootTable = mlb.getLootTableID();


				if (ThreadLocalRandom.current().nextFloat() > chance)
					continue;

				addMobLoot(mob, loot, mobLevel, calculatedLootTable, 1, false);


			}
		}

		//calculatedLootTable = lootTable;

		if (calculatedLootTable <= 1)
			calculatedLootTable = 1300;  // GENERIC WORLD




		//handle hotzone random loot

		if (hotzone) {

			LootTable.rollCount++;

			if (MobLootBase.MobLootSet.get(mob.getMobBase().getLoadID()).isEmpty()){


				roll = ThreadLocalRandom.current().nextInt(100);
				if (roll > 90)
					if (roll > LootTable.oneDropHotZone)
						addMobLoot(mob, loot, mobLevel, calculatedLootTable + 1, 1, true);
					else
						addMobLoot(mob, loot, mobLevel, calculatedLootTable + 1, 1, true);
			}else{
				for (MobLootBase mlb:MobLootBase.MobLootSet.get(mob.getMobBase().getLoadID())){
					if (!LootTable.lootGroups.containsKey(mlb.getLootTableID() + 1))
						continue;
					calculatedLootTable = mlb.getLootTableID();
					break;
				}
				roll = ThreadLocalRandom.current().nextInt(100);
				if (roll > 90)
					if (roll > LootTable.oneDropHotZone)
						addMobLoot(mob, loot, mobLevel, (calculatedLootTable + 1), 1, true);
					else
						addMobLoot(mob, loot, mobLevel, (calculatedLootTable + 1), 1, true);

			}

		}




		//handle mob specific special loot
		handleSpecialLoot(loot, mob, false);

		return loot;
	}

	public static ArrayList<MobLoot> getMobLootDeath(Mob mob, int mobLevel, int lootTable) {
		ArrayList<MobLoot> loot = new ArrayList<>();

		if (mob == null)
			return loot;

		//handle hotzone random loot
		boolean hotzone = ZoneManager.inHotZone(mob.getLoc());
		if (hotzone) {

			if (MobLootBase.MobLootSet.get(mob.getMobBase().getLoadID()).isEmpty()){
				lootTable += 1;

				if (lootTable <= 1)
					lootTable = 1301;  // GENERIC WORLD
				int roll = ThreadLocalRandom.current().nextInt(100);
				if (roll > 90)
					if (roll > LootTable.oneDropHotZone)
						addMobLoot(mob, loot, mobLevel, lootTable, 1, true);
					else
						addMobLoot(mob, loot, mobLevel, lootTable, 1, true);
			}else{
				for (MobLootBase mlb:MobLootBase.MobLootSet.get(mob.getMobBase().getLoadID())){
					lootTable = mlb.getLootTableID() + 1;
					if (!LootTable.lootGroups.containsKey(lootTable))
						continue;

					int roll = ThreadLocalRandom.current().nextInt(100);
					if (roll > 90)
						if (roll > LootTable.oneDropHotZone)
							addMobLoot(mob, loot, mobLevel, (lootTable), 1, true);
						else
							addMobLoot(mob, loot, mobLevel, (lootTable), 1, true);

					break;
				}
			}


			if (loot.isEmpty()){

				LootTable.rollCount++; //add another rollCount here.
				int resourceRoll = ThreadLocalRandom.current().nextInt(100);
				if (resourceRoll <=5)
					addMobLootResources(mob, loot, mobLevel, (lootTable), 1, true);
			}

		}


		//handle mob specific special loot on death
		handleSpecialLoot(loot, mob, true);

		return loot;
	}

	private static void handleSpecialLoot(ArrayList<MobLoot> loot, Mob mob, boolean onDeath) {

		if (SpecialLoot.LootMap.containsKey(mob.getLootSet())) {
			ArrayList<SpecialLoot> specialLoot = SpecialLoot.LootMap.get(mob.getLootSet());
			for (SpecialLoot sl : specialLoot) {
				if ((onDeath && sl.dropOnDeath()) || (!onDeath && !sl.dropOnDeath()))
					if (ThreadLocalRandom.current().nextInt(100) < sl.getDropChance()) {
						ItemBase ib = ItemBase.getItemBase(sl.getItemID());
						if (ib != null) {

							switch (ib.getUUID()){
							case 19290:
								continue;
							case 19291:
								continue;
							case 19292:
								continue;
							case 27530:
								continue;
							case 973000:
								continue;
							case 973200:
								continue;
							case 26360:
								continue;
							}
							MobLoot ml = new MobLoot(mob, ib, sl.noSteal());
							loot.add(ml);

							

						}
					}
			}
		}
	}



	//called by getMobLoot to add the actual loot
	private static void addMobLoot(Mob mob, ArrayList<MobLoot> loot, int mobLevel, int lootTableID, int cnt, boolean hotzone) {

		// Member variable declaration
		float calculatedMobLevel;
		int minSpawn;
		int maxSpawn;
		int spawnQuanity = 0;
		int prefixValue = 0;
		int suffixValue = 0;
		int subTableID;
		String modifierPrefix = "";
		String modifierSuffix = "";

		// Lookup Table Variables
		LootTable lootTable;
		LootRow lootRow;
		LootTable lootGroup;
		LootRow groupRow = null;
		LootTable modTable;
		LootTable modGroup;
		LootRow modRow = null;

		// Used for actual generation of items
		int itemBaseUUID;
		ItemBase itemBase = null;
		MobLoot mobLoot;

		Zone zone = mob.getParentZone();
		// Member variable assignment
		if (!LootTable.lootGroups.containsKey(lootTableID))
			return;

		lootGroup = LootTable.lootGroups.get(lootTableID);





		calculatedMobLevel = mobLevel;

		if (calculatedMobLevel > 49)
			calculatedMobLevel = 49;


		int roll = 0;
		for (int i = 0; i < cnt; i++) {


			Random random = new Random();


			roll = random.nextInt(100) + 1; //random roll between 1 and 100
			groupRow = lootGroup.getLootRow(roll);





			if (groupRow == null)
				return;

			//get loot table for this group
			if (!LootTable.lootTables.containsKey(groupRow.getValueOne()))
				return;


			lootTable = LootTable.lootTables.get(groupRow.getValueOne());

			//get item ID //FUCK THIS RETARDED SHIT
			//			roll = gaussianLevel(calculatedMobLevel);




			int minRoll =  (int) ((calculatedMobLevel - 5) * 5);
			int maxRoll = (int) ((calculatedMobLevel + 15) * 5);

			if (minRoll < (int)lootTable.minRoll){
				minRoll = (int)lootTable.minRoll;
			}

			if (maxRoll < minRoll)
				maxRoll = minRoll;

			if (maxRoll > lootTable.maxRoll)
				maxRoll = (int) lootTable.maxRoll;



			if (maxRoll > 320)
				maxRoll = 320;

			roll = (int) ThreadLocalRandom.current().nextDouble(minRoll, maxRoll + 1); //Does not return Max, but does return min?


			lootRow = lootTable.getLootRow(roll); //get the item row from the bell's curve of level +-15

			if (lootRow == null)
				continue; //no item found for roll

			itemBaseUUID = lootRow.getValueOne();



			if (lootRow.getValueOne() == 0)
				continue;

			//handle quantities > 1 for resource drops
			minSpawn = lootRow.getValueTwo();
			maxSpawn = lootRow.getValueThree();

			// spawnQuanity between minspawn (inclusive) and maxspawn (inclusive)
			if (maxSpawn > 1)
				spawnQuanity = ThreadLocalRandom.current().nextInt((maxSpawn + 1 - minSpawn)) + minSpawn;



			//get modifierPrefix

			calculatedMobLevel = mobLevel;

			if (calculatedMobLevel < 16)
				calculatedMobLevel = 16;

			if (calculatedMobLevel > 49)
				calculatedMobLevel = 49;

			int chanceMod = ThreadLocalRandom.current().nextInt(100) + 1;

			if (chanceMod < 25){
				modGroup = LootTable.modGroups.get(groupRow.getValueTwo());

				if (modGroup != null) {


					for (int a = 0;a<10;a++){
						roll = ThreadLocalRandom.current().nextInt(100) + 1;
						modRow = modGroup.getLootRow(roll);
						if (modRow != null)
							break;
					}


					if (modRow != null) {
						subTableID = modRow.getValueOne();

						if (LootTable.modTables.containsKey(subTableID)) {

							modTable = LootTable.modTables.get(subTableID);

							roll = gaussianLevel((int)calculatedMobLevel);

							if (roll < modTable.minRoll)
								roll = (int) modTable.minRoll;

							if (roll > modTable.maxRoll)
								roll = (int) modTable.maxRoll;



							modRow = modTable.getLootRow(roll);

							if (modRow != null) {
								prefixValue = modRow.getValueOne();
								modifierPrefix = modRow.getAction();
							}
						}
					}
				}
			}else if(chanceMod < 50){
				modGroup = LootTable.modGroups.get(groupRow.getValueThree());

				if (modGroup != null) {

					for (int a = 0;a<10;a++){
						roll = ThreadLocalRandom.current().nextInt(100) + 1;
						modRow = modGroup.getLootRow(roll);
						if (modRow != null)
							break;
					}

					if (modRow != null) {

						subTableID = modRow.getValueOne();

						if (LootTable.modTables.containsKey(subTableID)) {

							modTable = LootTable.modTables.get(subTableID);
							roll = gaussianLevel((int)calculatedMobLevel);

							if (roll < modTable.minRoll)
								roll = (int) modTable.minRoll;

							if (roll > modTable.maxRoll)
								roll = (int) modTable.maxRoll;

							modRow = modTable.getLootRow(roll);

							if (modRow == null){
								modRow = modTable.getLootRow((int) ((modTable.minRoll + modTable.maxRoll) *.05f));
							}

							if (modRow != null) {
								suffixValue = modRow.getValueOne();
								modifierSuffix = modRow.getAction();
							}
						}
					}
				}
			}else{
				modGroup = LootTable.modGroups.get(groupRow.getValueTwo());

				if (modGroup != null) {


					for (int a = 0;a<10;a++){
						roll = ThreadLocalRandom.current().nextInt(100) + 1;
						modRow = modGroup.getLootRow(roll);
						if (modRow != null)
							break;
					}


					if (modRow != null) {
						subTableID = modRow.getValueOne();

						if (LootTable.modTables.containsKey(subTableID)) {

							modTable = LootTable.modTables.get(subTableID);

							roll = gaussianLevel((int)calculatedMobLevel);

							if (roll < modTable.minRoll)
								roll = (int) modTable.minRoll;

							if (roll > modTable.maxRoll)
								roll = (int) modTable.maxRoll;



							modRow = modTable.getLootRow(roll);

							if (modRow == null){
								modRow = modTable.getLootRow((int) ((modTable.minRoll + modTable.maxRoll) *.05f));
							}

							if (modRow != null) {
								prefixValue = modRow.getValueOne();
								modifierPrefix = modRow.getAction();
							}
						}
					}
				}

				//get modifierSuffix
				modGroup = LootTable.modGroups.get(groupRow.getValueThree());

				if (modGroup != null) {

					for (int a = 0;a<10;a++){
						roll = ThreadLocalRandom.current().nextInt(100) + 1;
						modRow = modGroup.getLootRow(roll);
						if (modRow != null)
							break;
					}

					if (modRow != null) {

						subTableID = modRow.getValueOne();

						if (LootTable.modTables.containsKey(subTableID)) {

							modTable = LootTable.modTables.get(subTableID);
							roll = gaussianLevel((int)calculatedMobLevel);

							if (roll < modTable.minRoll)
								roll = (int) modTable.minRoll;

							if (roll > modTable.maxRoll)
								roll = (int) modTable.maxRoll;

							modRow = modTable.getLootRow(roll);

							if (modRow == null){
								modRow = modTable.getLootRow((int) ((modTable.minRoll + modTable.maxRoll) *.05f));
							}

							if (modRow != null) {
								suffixValue = modRow.getValueOne();
								modifierSuffix = modRow.getAction();
							}
						}
					}
				}
			}


			itemBase = ItemBase.getItemBase(itemBaseUUID);

			if (itemBase == null)
				return;

			//Handle logging of drops
			LootTable.HandleDropLogs(itemBase);

		


			// Handle drop rates of resources/runes/contracts.
			// We intentionally drop them in half
			//			if ((itemBase.getMessageType() == ItemType.CONTRACT) ||
			//					(itemBase.getMessageType() == ItemType.RUNE) ){
			//				if (ThreadLocalRandom.current().nextBoolean() == false)
			//					continue;
			//			}

			

			if (itemBase.getType() == ItemType.OFFERING)
				spawnQuanity = 1;

			if (spawnQuanity > 0)
				mobLoot = new MobLoot(mob, itemBase, spawnQuanity, false);
			else
				mobLoot = new MobLoot(mob, itemBase, false);

			if (!modifierPrefix.isEmpty())
				mobLoot.addPermanentEnchantment(modifierPrefix, 0, prefixValue, true);

			if (!modifierSuffix.isEmpty())
				mobLoot.addPermanentEnchantment(modifierSuffix, 0, suffixValue, false);
			mobLoot.loadEnchantments();

			loot.add(mobLoot);



		}
	}

	private static void addMobLootResources(Mob mob, ArrayList<MobLoot> loot, int mobLevel, int lootTableID, int cnt, boolean hotzone) {

		// Member variable declaration
		float calculatedMobLevel;
		int minSpawn;
		int maxSpawn;
		int spawnQuanity = 0;
		int prefixValue = 0;
		int suffixValue = 0;
		int subTableID;
		String modifierPrefix = "";
		String modifierSuffix = "";

		// Lookup Table Variables
		LootTable lootTable;
		LootRow lootRow;
		LootTable lootGroup;
		LootRow groupRow = null;
		LootTable modTable;
		LootTable modGroup;
		LootRow modRow = null;

		// Used for actual generation of items
		int itemBaseUUID;
		ItemBase itemBase;
		MobLoot mobLoot;

		Zone zone = mob.getParentZone();
		// Member variable assignment
		if (!LootTable.lootGroups.containsKey(lootTableID))
			return;

		lootGroup = LootTable.lootGroups.get(lootTableID);

		calculatedMobLevel = mobLevel;

		if (calculatedMobLevel > 49)
			calculatedMobLevel = 49;

		int roll = 0;
		for (int i = 0; i < cnt; i++) {



			if  (lootTableID == 1901)
				groupRow = lootGroup.getLootRow(66);
			else if (lootTableID == 1501)
				groupRow = lootGroup.getLootRow(98);
			else
				groupRow = lootGroup.getLootRow(80);





			if (groupRow == null)
				return;

			//get loot table for this group
			if (!LootTable.lootTables.containsKey(groupRow.getValueOne()))
				return;


			lootTable = LootTable.lootTables.get(groupRow.getValueOne());

			//get item ID //FUCK THIS RETARDED SHIT
			//			roll = gaussianLevel(calculatedMobLevel);




			int minRoll =  (int) ((calculatedMobLevel-5) * 5);
			int maxRoll = (int) ((calculatedMobLevel + 15) *5);

			if (minRoll < (int)lootTable.minRoll){
				minRoll = (int)lootTable.minRoll;
			}

			if (maxRoll < minRoll)
				maxRoll = minRoll;



			if (maxRoll > 320)
				maxRoll = 320;

			roll = ThreadLocalRandom.current().nextInt(minRoll, maxRoll + 1); //Does not return Max, but does return min?
			lootRow = lootTable.getLootRow(roll); //get the item row from the bell's curve of level +-15

			if (lootRow == null)
				continue; //no item found for roll

			itemBaseUUID = lootRow.getValueOne();

			if (lootRow.getValueOne() == 0)
				continue;

			//handle quantities > 1 for resource drops
			minSpawn = lootRow.getValueTwo();
			maxSpawn = lootRow.getValueThree();

			// spawnQuanity between minspawn (inclusive) and maxspawn (inclusive)
			if (maxSpawn > 1)
				spawnQuanity = ThreadLocalRandom.current().nextInt((maxSpawn + 1 - minSpawn)) + minSpawn;


			itemBase = ItemBase.getItemBase(itemBaseUUID);
			if (itemBase == null)
				return;
			LootTable.HandleDropLogs(itemBase);


			switch (itemBase.getUUID()){
			case 19290:
				continue;
			case 19291:
				continue;
			case 19292:
				continue;
			case 27530:
				continue;
			case 973000:
				continue;
			case 973200:
				continue;

			case 26360:
				continue;
			}

			// Handle drop rates of resources/runes/contracts.
			// We intentionally drop them in half



			if (itemBase.getType() == ItemType.OFFERING)
				spawnQuanity = 1;

			if (spawnQuanity > 0)
				mobLoot = new MobLoot(mob, itemBase, spawnQuanity, false);
			else
				mobLoot = new MobLoot(mob, itemBase, false);

			loot.add(mobLoot);

		}
	}

	public static int gaussianLevel(int level) {
		int ret = -76;

		while (ret < -75 || ret > 75) {
			ret = (int) (ThreadLocalRandom.current().nextGaussian() * 75);
		}

		return (level * 5) + ret;
		//		float useLevel = (float)(level + (ThreadLocalRandom.current().nextGaussian() * 5));
		//
		//		if (useLevel < (level - 15))
		//			useLevel = level - 15;
		//		else if (useLevel > (level + 15))
		//			useLevel = level + 15;
		//		return (int)(useLevel * 5);
	}



	

	//This set's the drop chances for stat runes.
	public static void populateStatRuneChances() {
		//+3, Increased
		statRuneChances.put(250018, 60);
		statRuneChances.put(250009, 60);
		statRuneChances.put(250027, 60);
		statRuneChances.put(250036, 60);
		statRuneChances.put(250000, 60);

		//+5, Enhanced
		statRuneChances.put(250019, 60);
		statRuneChances.put(250010, 60);
		statRuneChances.put(250028, 60);
		statRuneChances.put(250037, 60);
		statRuneChances.put(250001, 60);

		//+10 Exceptional
		statRuneChances.put(250020, 60);
		statRuneChances.put(250011, 60);
		statRuneChances.put(250029, 60);
		statRuneChances.put(250038, 60);
		statRuneChances.put(250002, 60);

		//+15, Amazing
		statRuneChances.put(250021, 60);
		statRuneChances.put(250012, 60);
		statRuneChances.put(250030, 60);
		statRuneChances.put(250039, 60);
		statRuneChances.put(250003, 60);

		//+20, Incredible
		statRuneChances.put(250022, 60);
		statRuneChances.put(250013, 60);
		statRuneChances.put(250031, 60);
		statRuneChances.put(250040, 60);
		statRuneChances.put(250004, 60);

		//+25, Great
		statRuneChances.put(250023, 60);
		statRuneChances.put(250014, 60);
		statRuneChances.put(250032, 60);
		statRuneChances.put(250041, 60);
		statRuneChances.put(250005, 60);

		//+30, Heroic
		statRuneChances.put(250024, 60);
		statRuneChances.put(250015, 60);
		statRuneChances.put(250033, 60);
		statRuneChances.put(250042, 60);
		statRuneChances.put(250006, 60);

		//+35, Legendary
		statRuneChances.put(250025, 60);
		statRuneChances.put(250016, 60);
		statRuneChances.put(250034, 60);
		statRuneChances.put(250043, 60);
		statRuneChances.put(250007, 60);

		//+40, of the Gods
		statRuneChances.put(250026, 60);
		statRuneChances.put(250017, 60);
		statRuneChances.put(250035, 60);
		statRuneChances.put(250044, 60);
		statRuneChances.put(250008, 60);
	}

	public ConcurrentHashMap<Integer, LootRow> getLootTable() {
		return lootTable;
	}

	private static void HandleDropLogs(ItemBase itemBase){

		if (itemBase == null)
			return;

		LootTable.dropCount++; //item dropped, add to all item count.


		if (LootTable.itemsDroppedMap.get(itemBase) == null){
			LootTable.itemsDroppedMap.put(itemBase, 1); //First time dropping, make count 1.
		}else{
			int dropCount = LootTable.itemsDroppedMap.get(itemBase);
			dropCount++;
			LootTable.itemsDroppedMap.put(itemBase, dropCount);
		}

		switch (itemBase.getType()){
		case RESOURCE:
			LootTable.resourceCount++;

			if (LootTable.resourceDroppedMap.get(itemBase) == null){
				LootTable.resourceDroppedMap.put(itemBase, 1); //First time dropping, make count 1.
			}else{
				int dropCount = LootTable.resourceDroppedMap.get(itemBase);
				dropCount++;
				LootTable.resourceDroppedMap.put(itemBase, dropCount);
			}
			break;
		case RUNE:
			LootTable.runeCount++;
			if (LootTable.runeDroppedMap.get(itemBase) == null){
				LootTable.runeDroppedMap.put(itemBase, 1); //First time dropping, make count 1.
			}else{
				int dropCount = LootTable.runeDroppedMap.get(itemBase);
				dropCount++;
				LootTable.runeDroppedMap.put(itemBase, dropCount);
			}
			break;
		case CONTRACT:
			LootTable.contractCount++;

			if (LootTable.contractDroppedMap.get(itemBase) == null){
				LootTable.contractDroppedMap.put(itemBase, 1); //First time dropping, make count 1.
			}else{
				int dropCount = LootTable.contractDroppedMap.get(itemBase);
				dropCount++;
				LootTable.contractDroppedMap.put(itemBase, dropCount);
			}

			break;
		case WEAPON: //Glass Drop

			if (itemBase.isGlass()){
				LootTable.glassCount++;
				if (LootTable.glassDroppedMap.get(itemBase) == null){
					LootTable.glassDroppedMap.put(itemBase, 1); //First time dropping, make count 1.
				}else{
					int dropCount = LootTable.glassDroppedMap.get(itemBase);
					dropCount++;
					LootTable.glassDroppedMap.put(itemBase, dropCount);
				}
			}

			break;
		}

	}

	public static Item CreateGamblerItem(Item item, PlayerCharacter gambler){

		if (item == null)
			return null;

		int groupID = 0;

		switch (item.getItemBase().getUUID()){
		case 971050: //Wrapped Axe
			groupID = 3000;
			break;
		case 971051://Wrapped Great Axe
			groupID = 3005;
			break;
		case 971052://Wrapped Throwing Axe
			groupID = 3010;
			break;
		case 971053://	Wrapped Bow
			groupID = 3015;
			break;
		case 971054://Wrapped Crossbow
			groupID = 3020;
			break;
		case 971055:	//Wrapped Dagger
			groupID = 3025;
			break;
		case 971056:	//	Wrapped Throwing Dagger
			groupID = 3030;
			break;
		case 971057:	//	Wrapped Hammer
			groupID = 3035;
			break;
		case 971058://			Wrapped Great Hammer
			groupID = 3040;
			break;
		case 971059://			Wrapped Throwing Hammer
			groupID = 3045;
			break;
		case 971060://			Wrapped Polearm
			groupID = 3050;
			break;
		case 971061://			Wrapped Spear
			groupID = 3055;
			break;
		case 971062://			Wrapped Staff
			groupID = 3060;
			break;
		case 971063://			Wrapped Sword
			groupID = 3065;
			break;
		case 971064://			Wrapped Great Sword
			groupID = 3070;
			break;
		case 971065://			Wrapped Unarmed Weapon
			groupID = 3075;
			break;
		case 971066://			Wrapped Cloth Armor
			groupID = 3100;
			break;
		case 971067://			Wrapped Light Armor
			groupID = 3105;
			break;
		case 971068://			Wrapped Medium Armor
			groupID = 3110;
			break;
		case 971069://			Wrapped Heavy Armor
			groupID = 3115;
			break;
		case 971070://			Wrapped Rune
			groupID = 3200;
			break;
		case 971071://			Wrapped City Improvement
			groupID = 3210;
			break;
		}
		//couldnt find group
		if (groupID == 0)
			return null;


		LootTable lootGroup = LootTable.lootGroups.get(groupID);

		if (lootGroup == null)
			return null;
		float calculatedMobLevel;
		int minSpawn;
		int maxSpawn;
		int spawnQuanity = 0;
		int prefixValue = 0;
		int suffixValue = 0;
		int subTableID;
		String modifierPrefix = "";
		String modifierSuffix = "";

		// Lookup Table Variables
		LootTable lootTable;
		LootRow lootRow;
		LootRow groupRow = null;
		LootTable modTable;
		LootTable modGroup;
		LootRow modRow = null;

		// Used for actual generation of items
		int itemBaseUUID;
		ItemBase itemBase = null;
		MobLoot mobLoot;



		int roll = ThreadLocalRandom.current().nextInt(100) + 1; //Does not return Max, but does return min?

		groupRow = lootGroup.getLootRow(roll);

		lootTable = LootTable.lootTables.get(groupRow.getValueOne());
		roll = ThreadLocalRandom.current().nextInt(100) + 1;
		lootRow = lootTable.getLootRow(roll + 220); //get the item row from the bell's curve of level +-15

		if (lootRow == null)
			return null; //no item found for roll

		itemBaseUUID = lootRow.getValueOne();



		if (lootRow.getValueOne() == 0)
			return null;

		//handle quantities > 1 for resource drops
		minSpawn = lootRow.getValueTwo();
		maxSpawn = lootRow.getValueThree();

		// spawnQuanity between minspawn (inclusive) and maxspawn (inclusive)
		if (maxSpawn > 1)
			spawnQuanity = ThreadLocalRandom.current().nextInt((maxSpawn + 1 - minSpawn)) + minSpawn;



		//get modifierPrefix

		calculatedMobLevel = 49;



		int chanceMod = ThreadLocalRandom.current().nextInt(100) + 1;

		if (chanceMod < 25){
			modGroup = LootTable.modGroups.get(groupRow.getValueTwo());

			if (modGroup != null) {


				for (int a = 0;a<10;a++){
					roll = ThreadLocalRandom.current().nextInt(100) + 1;
					modRow = modGroup.getLootRow(roll);
					if (modRow != null)
						break;
				}


				if (modRow != null) {
					subTableID = modRow.getValueOne();

					if (LootTable.modTables.containsKey(subTableID)) {

						modTable = LootTable.modTables.get(subTableID);

						roll = gaussianLevel((int)calculatedMobLevel);

						if (roll < modTable.minRoll)
							roll = (int) modTable.minRoll;

						if (roll > modTable.maxRoll)
							roll = (int) modTable.maxRoll;



						modRow = modTable.getLootRow(roll);

						if (modRow != null) {
							prefixValue = modRow.getValueOne();
							modifierPrefix = modRow.getAction();
						}
					}
				}
			}
		}else if(chanceMod < 50){
			modGroup = LootTable.modGroups.get(groupRow.getValueThree());

			if (modGroup != null) {

				for (int a = 0;a<10;a++){
					roll = ThreadLocalRandom.current().nextInt(100) + 1;
					modRow = modGroup.getLootRow(roll);
					if (modRow != null)
						break;
				}

				if (modRow != null) {

					subTableID = modRow.getValueOne();

					if (LootTable.modTables.containsKey(subTableID)) {

						modTable = LootTable.modTables.get(subTableID);
						roll = gaussianLevel((int)calculatedMobLevel);

						if (roll < modTable.minRoll)
							roll = (int) modTable.minRoll;

						if (roll > modTable.maxRoll)
							roll = (int) modTable.maxRoll;

						modRow = modTable.getLootRow(roll);

						if (modRow == null){
							modRow = modTable.getLootRow((int) ((modTable.minRoll + modTable.maxRoll) *.05f));
						}

						if (modRow != null) {
							suffixValue = modRow.getValueOne();
							modifierSuffix = modRow.getAction();
						}
					}
				}
			}
		}else{
			modGroup = LootTable.modGroups.get(groupRow.getValueTwo());

			if (modGroup != null) {


				for (int a = 0;a<10;a++){
					roll = ThreadLocalRandom.current().nextInt(100) + 1;
					modRow = modGroup.getLootRow(roll);
					if (modRow != null)
						break;
				}


				if (modRow != null) {
					subTableID = modRow.getValueOne();

					if (LootTable.modTables.containsKey(subTableID)) {

						modTable = LootTable.modTables.get(subTableID);

						roll = gaussianLevel((int)calculatedMobLevel);

						if (roll < modTable.minRoll)
							roll = (int) modTable.minRoll;

						if (roll > modTable.maxRoll)
							roll = (int) modTable.maxRoll;



						modRow = modTable.getLootRow(roll);

						if (modRow == null){
							modRow = modTable.getLootRow((int) ((modTable.minRoll + modTable.maxRoll) *.05f));
						}

						if (modRow != null) {
							prefixValue = modRow.getValueOne();
							modifierPrefix = modRow.getAction();
						}
					}
				}
			}

			//get modifierSuffix
			modGroup = LootTable.modGroups.get(groupRow.getValueThree());

			if (modGroup != null) {

				for (int a = 0;a<10;a++){
					roll = ThreadLocalRandom.current().nextInt(100) + 1;
					modRow = modGroup.getLootRow(roll);
					if (modRow != null)
						break;
				}

				if (modRow != null) {

					subTableID = modRow.getValueOne();

					if (LootTable.modTables.containsKey(subTableID)) {

						modTable = LootTable.modTables.get(subTableID);
						roll = gaussianLevel((int)calculatedMobLevel);

						if (roll < modTable.minRoll)
							roll = (int) modTable.minRoll;

						if (roll > modTable.maxRoll)
							roll = (int) modTable.maxRoll;

						modRow = modTable.getLootRow(roll);

						if (modRow == null){
							modRow = modTable.getLootRow((int) ((modTable.minRoll + modTable.maxRoll) *.05f));
						}

						if (modRow != null) {
							suffixValue = modRow.getValueOne();
							modifierSuffix = modRow.getAction();
						}
					}
				}
			}
		}


		itemBase = ItemBase.getItemBase(itemBaseUUID);
		byte charges = (byte) itemBase.getNumCharges();
		short dur = (short) itemBase.getDurability();



		short weight = itemBase.getWeight();
		if (!gambler.getCharItemManager().hasRoomInventory(weight)) {
			return null;
		}


		Item gambledItem = new Item(itemBase, gambler.getObjectUUID(),
				OwnerType.PlayerCharacter, charges, charges, dur, dur,
				true, false,ItemContainerType.INVENTORY,(byte) 0,
                new ArrayList<>(),"");

		if (spawnQuanity == 0 && itemBase.getType().equals(ItemType.RESOURCE))
			spawnQuanity = 1;

		if (spawnQuanity > 0)
			item.setNumOfItems(spawnQuanity);

		try {
			gambledItem = DbManager.ItemQueries.ADD_ITEM(gambledItem);

		} catch (Exception e) {
			Logger.error(e);
		}

		if (gambledItem == null) {

			return null;
		}
		if (!modifierPrefix.isEmpty())
			gambledItem.addPermanentEnchantment(modifierPrefix, 0);

		if (!modifierSuffix.isEmpty())
			gambledItem.addPermanentEnchantment(modifierSuffix, 0);



		//add item to inventory
		gambler.getCharItemManager().addItemToInventory(gambledItem);

		gambler.getCharItemManager().updateInventory();

		return gambledItem;
	}

}
