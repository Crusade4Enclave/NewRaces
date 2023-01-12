// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.DamageType;
import engine.Enum.GameObjectType;
import engine.Enum.ItemType;
import engine.gameManager.DbManager;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ItemBase {

	public static final byte GOLD_BASE_TYPE = 4;
	public static ItemBase GOLD_ITEM_BASE = null;
	public static int GOLD_BASE_ID = 7;
	public static ArrayList<Integer> AnniverseryGifts = new ArrayList<>();
	// Internal cache
	private static HashMap<Integer, Integer> itemHashIDMap = new HashMap<>();
	private static HashMap<String, Integer> _IDsByNames = new HashMap<>();
	public static HashMap<Integer, ItemBase> _itemBaseByUUID = new HashMap<>();
	private static ArrayList<ItemBase> _resourceList = new ArrayList<>();
	private final int uuid;
	private final String name;
	private float durability;
	private int value;
	private short weight;
	private short color;
	private ItemType type;
	private int vendorType;
	private int modTable;
	private int useID;
	private int hashID;
	private byte useAmount;
	// Armor and weapon related values
	private int equipFlag;
	private int restrictFlag;
	private String skillRequired;
	private short percentRequired;
	private float slashResist;
	private float crushResist;
	private float pierceResist;
	private float blockMod;
	private short defense;
	private float dexPenalty;
	private float speed;
	private float range;
	private short minDamage;
	private short maxDamage;
	private String mastery;
	private engine.Enum.DamageType damageType;
	private boolean twoHanded;
	private boolean isConsumable;
	private boolean isStackable;
	private int numCharges;
	// Item stat modifiers
	private HashMap<Integer, Integer> bakedInStats = new HashMap<>();
	private HashMap<Integer, Integer> usedStats = new HashMap<>();
	private float parryBonus;
	private boolean isStrBased;
	private ArrayList<Integer> animations = new ArrayList<>();
	private ArrayList<Integer> offHandAnimations = new ArrayList<>();
	private boolean autoID = false;
	public static HashMap<engine.Enum.ItemType, HashSet<ItemBase>> ItemBaseTypeMap = new HashMap<>();
	/**
	 * ResultSet Constructor
	 */
	public ItemBase(ResultSet rs) throws SQLException {

		this.uuid = rs.getInt("ID");
		this.name = rs.getString("name");
		this.durability = rs.getInt("durability");
		this.value = rs.getInt("value");
		this.weight = rs.getShort("weight");
		this.color = rs.getShort("color");
		this.type = ItemType.valueOf(rs.getString("Type"));
		this.useID = rs.getInt("useID");
		this.vendorType = rs.getInt("vendorType");
		this.useAmount = rs.getByte("useAmount");
		this.modTable = rs.getInt("modTable");
		this.hashID = rs.getInt("itemHashID");

		this.isConsumable = false;
		this.isStackable = false;
		this.numCharges = rs.getShort("numCharges");

		this.equipFlag = rs.getInt("equipFlag");
		this.restrictFlag = rs.getInt("restrictFlag");
		this.skillRequired = rs.getString("skillRequired");
		this.percentRequired = rs.getShort("percentRequired");
		this.slashResist = rs.getFloat("slashResist");
		this.crushResist = rs.getFloat("crushResist");
		this.pierceResist = rs.getFloat("pierceResist");
		this.blockMod = rs.getFloat("blockMod");
		this.defense = rs.getShort("defense");
		this.dexPenalty = rs.getFloat("dexPenalty");
		this.parryBonus = rs.getFloat("parryBonus");
		this.isStrBased = (rs.getInt("isStrBased") == 1);
		this.speed = rs.getFloat("speed");
		this.range = rs.getFloat("range");
		this.minDamage = rs.getShort("minDamage");
		this.maxDamage = rs.getShort("maxDamage");

		this.mastery = rs.getString("mastery");
		damageType = DamageType.valueOf(rs.getString("damageType"));

		this.twoHanded = (rs.getInt("twoHanded") == 1);

		switch (this.type) {
		case RUNE:
		case SCROLL:
		case COMMANDROD:
		case POTION:
		case TEARS:
		case GUILDCHARTER:
		case DEED:
		case CONTRACT:
		case WATERBUCKET:
		case REALMCHARTER:
		case GIFT:
			this.isConsumable = true;
			break;
		case OFFERING:
			this.isConsumable = true;
			Boon.HandleBoonListsForItemBase(uuid);
			break;
		case RESOURCE:
			this.isStackable = true;
			break;

		}

		this.autoIDItemsCheck();

		try{
			DbManager.ItemBaseQueries.LOAD_ANIMATIONS(this);
		}catch(Exception e){
			Logger.error( e.getMessage());
		}
		initBakedInStats();
		initializeHashes();

	}

	public static void addToCache(ItemBase itemBase) {

		_itemBaseByUUID.put(itemBase.uuid, itemBase);

		if (itemBase.type.equals(ItemType.RESOURCE))
			_resourceList.add(itemBase);

		_IDsByNames.put(itemBase.name.toLowerCase().replace(" ", "_"), itemBase.uuid);
	}

	public static HashMap<Integer, Integer> getItemHashIDMap() {
		return itemHashIDMap;
	}

	/*
	 * Database
	 */
	public static ItemBase getItemBase(int uuid) {

		return _itemBaseByUUID.get(uuid);
	}

	/**
	 * Get the ItemBase instance for Gold.
	 *
	 * @return ItemBase for Gold
	 */
	public static ItemBase getGoldItemBase() {
		if (ItemBase.GOLD_ITEM_BASE == null)
			ItemBase.GOLD_ITEM_BASE = getItemBase(7);
		return ItemBase.GOLD_ITEM_BASE;
	}

	public static int getIDByName(String name) {
		if (ItemBase._IDsByNames.containsKey(name))
			return ItemBase._IDsByNames.get(name);
		return 0;
	}

	/**
	 * @return the _itemBaseByUUID
	 */
	public static HashMap<Integer, ItemBase> getUUIDCache() {
		return _itemBaseByUUID;
	}

	/**
	 * @return the _resourceList
	 */
	public static ArrayList<ItemBase> getResourceList() {
		return _resourceList;
	}

	public static void loadAllItemBases() {
		DbManager.ItemBaseQueries.LOAD_ALL_ITEMBASES();
		AnniverseryGifts.add(971000);
		AnniverseryGifts.add(971001);
		AnniverseryGifts.add(971002);
		AnniverseryGifts.add(971003);
		AnniverseryGifts.add(971004);
		AnniverseryGifts.add(971005);
		AnniverseryGifts.add(971006);
		AnniverseryGifts.add(971007);
		AnniverseryGifts.add(971008);
		AnniverseryGifts.add(971009);
		AnniverseryGifts.add(971010);
		AnniverseryGifts.add(5101000);
		AnniverseryGifts.add(5101020);
		AnniverseryGifts.add(5101100);
		AnniverseryGifts.add(5101120);
		AnniverseryGifts.add(5101040);
		AnniverseryGifts.add(5101140);
		AnniverseryGifts.add(5101060);
		AnniverseryGifts.add(5101080);


	}

	/*
	 * Getters
	 */
	public String getName() {
		return this.name;
	}

	public float getDurability() {
		return this.durability;
	}

	private void initBakedInStats() {
		DbManager.ItemBaseQueries.LOAD_BAKEDINSTATS(this);
	}

	//TODO fix this later. Shouldn't be gotten from item base
	public int getMagicValue() {
		return this.value;
	}

	public int getBaseValue() {
		return this.value;
	}

	public short getWeight() {
		return this.weight;
	}

	public int getColor() {
		return this.color;
	}

	public boolean isConsumable() {
		return this.isConsumable;
	}

	public boolean isStackable() {
		return this.isStackable;
	}

	public int getNumCharges() {

		return this.numCharges;

	}

	public int getEquipFlag() {

		if ((this.type == ItemType.ARMOR)
				|| (this.type == ItemType.WEAPON)
				|| (this.type == ItemType.JEWELRY))
			return this.equipFlag;
		else
			return 0;
	}

	public boolean isRune() {
		int ID = uuid;
		if (ID > 2499 && ID < 3050) //class, discipline runes
			return true;
		else return ID > 249999 && ID < 252137;
	}

	public boolean isStatRune() {
		int ID = uuid;
		return ID > 249999 && ID < 250045;
	}
	public boolean isDiscRune(){
		int ID = uuid;
		if (ID > 2499 && ID < 3050) { //class, discipline runes
			return true;
		}
		else{
			return false;
		}
	}
	public boolean isGlass() {
		int ID = uuid;
		return ID > 7000099 && ID < 7000281;
	}


	public boolean isMasteryRune() {
		int ID = uuid;
		if (ID > 250114 && ID < 252128)
			switch (ID) {
			case 250115:
			case 250118:
			case 250119:
			case 250120:
			case 250121:
			case 250122:
			case 252123:
			case 252124:
			case 252125:
			case 252126:
			case 252127:
				return true;
			default:
				return false;
			}
		return false;
	}

	//returns powers tokens baked in to item
	public HashMap<Integer, Integer> getBakedInStats() {
		return this.bakedInStats;
	}

	//returns power tokens granted when using item, such as scrolls and potions
	public HashMap<Integer, Integer> getUsedStats() {
		return this.usedStats;
	}

	public final void initializeHashes() {
		itemHashIDMap.put(this.hashID, uuid);

	}

	public ItemType getType() {
		return this.type;
	}

	public int getUseID() {
		return this.useID;
	}

	public byte getUseAmount() {
		return this.useAmount;
	}

	public int getModTable() {
		return modTable;
	}

	public int getVendorType() {
		return vendorType;
	}

	public void setVendorType(int vendorType) {
		this.vendorType = vendorType;
	}

	public int getHashID() {
		return hashID;
	}

	public void setHashID(int hashID) {
		this.hashID = hashID;
	}

	private void autoIDItemsCheck(){
		//AUto ID Vorg and Glass
		switch (uuid){

		case 27550:
		case 27560:
		case 27580:
		case 27590:
		case 188500:
		case 188510:
		case 188520:
		case 188530:
		case 188540:
		case 188550:
		case 189100:
		case 189110:
		case 189120:
		case 189130:
		case 189140:
		case 189150:
		case 189510:
		case 27600:
		case 181840:
		case 188700:
		case 188720:
		case 189550:
		case 189560:
		case 7000100:
		case 7000110:
		case 7000120:
		case 7000130:
		case 7000140:
		case 7000150:
		case 7000160:
		case 7000170:
		case 7000180:
		case 7000190:
		case 7000200:
		case 7000210:
		case 7000220:
		case 7000230:
		case 7000240:
		case 7000250:
		case 7000270:
		case 7000280:
			this.autoID = true;
			break;
		default:
			this.autoID = false;
		}
	}

	public boolean validForSkills(ConcurrentHashMap<String, CharacterSkill> skills) {

		CharacterSkill characterSkill;

		if (this.skillRequired.isEmpty())
			return true;

		characterSkill = skills.get(this.skillRequired);

		if (characterSkill == null)
			return false;
		
		return !(this.percentRequired > characterSkill.getModifiedAmountBeforeMods());
	}

	public boolean canEquip(int slot, CharacterItemManager itemManager, AbstractCharacter abstractCharacter, Item item) {

		if (itemManager == null || abstractCharacter == null)
			return false;

		if (abstractCharacter.getObjectType().equals(GameObjectType.PlayerCharacter)) {

			if (!validForSlot(slot, itemManager.getEquipped(), item))
				return false;

			if (!validForSkills(abstractCharacter.getSkills()))
				return false;

			return item.getItemBase().value != 0 || Kit.IsNoobGear(item.getItemBase().uuid);
			//players can't wear 0 value items.

		}

		return true; //Mobiles and NPC's don't need to check equip
	}

	public int getValidSlot() {
		int slotValue = 0;

		switch (this.type) {
		case WEAPON:
			if ((this.equipFlag & 1) != 0)
				slotValue = MBServerStatics.SLOT_MAINHAND;
			else if ((this.equipFlag & 2) != 0)
				slotValue = MBServerStatics.SLOT_OFFHAND;
			break;
		case ARMOR:
			if ((this.equipFlag & 2) != 0)
				slotValue = MBServerStatics.SLOT_OFFHAND;
			else if ((this.equipFlag & 4) != 0)
				slotValue = MBServerStatics.SLOT_HELMET;
			else if ((this.equipFlag & 8) != 0)
				slotValue = MBServerStatics.SLOT_CHEST;
			else if ((this.equipFlag & 16) != 0)
				slotValue = MBServerStatics.SLOT_ARMS;
			else if ((this.equipFlag & 32) != 0)
				slotValue = MBServerStatics.SLOT_GLOVES;
			else if ((this.equipFlag & 64) != 0)
				slotValue = MBServerStatics.SLOT_RING2;
			else if ((this.equipFlag & 128) != 0)
				slotValue = MBServerStatics.SLOT_RING1;
			else if ((this.equipFlag & 256) != 0)
				slotValue = MBServerStatics.SLOT_NECKLACE;
			else if ((this.equipFlag & 512) != 0)
				slotValue = MBServerStatics.SLOT_LEGGINGS;
			else if ((this.equipFlag & 1024) != 0)
				slotValue = MBServerStatics.SLOT_FEET;
			break;

		case HAIR:
			if (this.equipFlag == 131072)
				slotValue = MBServerStatics.SLOT_HAIRSTYLE;
			else if(this.equipFlag == 65536)
				slotValue = MBServerStatics.SLOT_BEARDSTYLE;
			break;

		}
		return slotValue;

	}

	public boolean validSlotFlag(long flags) {

		boolean validSlot = false;

		switch (this.type) {
		case WEAPON:
			if (this.isMelee())
				validSlot = ((flags & 1) != 0);
			else if (this.isThrowing())
				validSlot = ((flags & 2) != 0);
			else if (this.isArchery())
				validSlot = ((flags & 4) != 0);
			else if (this.isScepter())
				validSlot = ((flags & 8) != 0);
			else if (this.isStaff())
				validSlot = ((flags & 16) != 0);
			break;
		case JEWELRY:
			if (this.isNecklace())
				validSlot = ((flags & 2147483648L) != 0L);
			else
				validSlot = ((flags & 4294967296L) != 0L);
			break;
		case ARMOR:

			if (this.isShield()) {
				validSlot = ((flags & 32) != 0);
				break;
			}

			if (this.isClothArmor()) {

				if (this.getEquipFlag() == 4) //hood
					validSlot = ((flags & 64) != 0);
				else if (this.getEquipFlag() == 8) {
					if ((restrictFlag & 512) != 0) //Robe
						validSlot = ((flags & 128) != 0);
					else
						validSlot = ((flags & 1024) != 0); //Tunic/Shirt

					break;
				} else if (this.getEquipFlag() == 16) //Sleeves
					validSlot = ((flags & 2048) != 0);
				else if (this.getEquipFlag() == 32) //Gloves
					validSlot = ((flags & 512) != 0);
				else if (this.getEquipFlag() == 512) //Pants
					validSlot = ((flags & 4096) != 0);
				else if (this.getEquipFlag() == 1024) //Boots
					validSlot = ((flags & 256) != 0);

				break;
			}

			if (this.isLightArmor()) {
				if (this.getEquipFlag() == 4) //helm
					validSlot = ((flags & 8192) != 0);
				else if (this.getEquipFlag() == 8) //Chest
					validSlot = ((flags & 16384) != 0);
				else if (this.getEquipFlag() == 16) //Sleeves
					validSlot = ((flags & 32768) != 0);
				else if (this.getEquipFlag() == 32) //Gloves
					validSlot = ((flags & 65536) != 0);
				else if (this.getEquipFlag() == 512) //Pants
					validSlot = ((flags & 131072) != 0);
				else if (this.getEquipFlag() == 1024) //Boots
					validSlot = ((flags & 262144) != 0);

				break;
			}

			if (this.isMediumArmor()) {
				if (this.getEquipFlag() == 4) //helm
					validSlot = ((flags & 524288) != 0);
				else if (this.getEquipFlag() == 8) //Chest
					validSlot = ((flags & 1048576) != 0);
				else if (this.getEquipFlag() == 16) //Sleeves
					validSlot = ((flags & 2097152) != 0);
				else if (this.getEquipFlag() == 32) //Gloves
					validSlot = ((flags & 4194304) != 0);
				else if (this.getEquipFlag() == 512) //Pants
					validSlot = ((flags & 8388608) != 0);
				else if (this.getEquipFlag() == 1024) //Boots
					validSlot = ((flags & 16777216) != 0);

				break;
			}

			if (this.isHeavyArmor())
				if (this.getEquipFlag() == 4) //helm
					validSlot = ((flags & 33554432) != 0);
				else if (this.getEquipFlag() == 8) //Chest
					validSlot = ((flags & 67108864) != 0);
				else if (this.getEquipFlag() == 16) //Sleeves
					validSlot = ((flags & 134217728) != 0);
				else if (this.getEquipFlag() == 32) //Gloves
					validSlot = ((flags & 268435456) != 0);
				else if (this.getEquipFlag() == 512) //Pants
					validSlot = ((flags & 536870912) != 0);
				else if (this.getEquipFlag() == 1024) //Boots
					validSlot = ((flags & 1073741824) != 0);
			break;
		}
		return validSlot;
	}

	public boolean validForSlot(int slot, ConcurrentHashMap<Integer, Item> equipped, Item item) {

		boolean validSlot = false;

		if (equipped == null)
			return validSlot;

		// Cannot equip an item in a slot already taken
		if (equipped.get(slot) != null && equipped.get(slot).equals(item) == false)
			return validSlot;

		switch (item.getItemBase().type) {
		case WEAPON:

			// Only two slots available for weapons
			if ((slot != MBServerStatics.SLOT_MAINHAND) && (slot != MBServerStatics.SLOT_OFFHAND))
				break;

			//make sure weapon is valid for slot
			if ((slot & this.equipFlag) == 0)
				break;

			// Two handed weapons take up two slots
			if ((this.twoHanded == true) &&
					((slot == MBServerStatics.SLOT_OFFHAND && equipped.get(MBServerStatics.SLOT_MAINHAND) != null) ||
							(slot == MBServerStatics.SLOT_MAINHAND && equipped.get(MBServerStatics.SLOT_OFFHAND) != null)))
				break;

			// Validation passed, must be a valid weapon

			validSlot = true;
			break;
		case JEWELRY:
			// Not a valid slot for ring

			if (this.isRing() &&
					((slot != MBServerStatics.SLOT_RING1) && (slot != MBServerStatics.SLOT_RING2)))
				break;

			// Not a valid slot for necklace

			if (this.isNecklace() && slot != MBServerStatics.SLOT_NECKLACE)
				break;

			// Passed validation, must be valid bling bling

			validSlot = true;
			break;
		case ARMOR:

			// Invalid slot for armor?
			if (slot == MBServerStatics.SLOT_OFFHAND && ((2 & this.equipFlag) == 0))
				break;
			if (slot == MBServerStatics.SLOT_HELMET && ((4 & this.equipFlag) == 0))
				break;
			if (slot == MBServerStatics.SLOT_CHEST && ((8 & this.equipFlag) == 0))
				break;
			if (slot == MBServerStatics.SLOT_ARMS && ((16 & this.equipFlag) == 0))
				break;
			if (slot == MBServerStatics.SLOT_GLOVES && ((32 & this.equipFlag) == 0))
				break;
			if (slot == MBServerStatics.SLOT_LEGGINGS && ((512 & this.equipFlag) == 0))
				break;
			if (slot == MBServerStatics.SLOT_FEET && ((1024 & this.equipFlag) == 0))
				break;

			// Is slot for this piece already taken?
			if (((this.restrictFlag & 2) != 0) && (equipped.get(MBServerStatics.SLOT_OFFHAND) != null) && slot != MBServerStatics.SLOT_OFFHAND)
				break;
			if (((this.restrictFlag & 4) != 0) && (equipped.get(MBServerStatics.SLOT_HELMET) != null) && slot != MBServerStatics.SLOT_HELMET)
				break;
			if (((this.restrictFlag & 8) != 0) && (equipped.get(MBServerStatics.SLOT_CHEST) != null) && slot != MBServerStatics.SLOT_CHEST)
				break;
			if (((this.restrictFlag & 16) != 0) && (equipped.get(MBServerStatics.SLOT_ARMS) != null) && slot != MBServerStatics.SLOT_ARMS)
				break;
			if (((this.restrictFlag & 32) != 0) && (equipped.get(MBServerStatics.SLOT_GLOVES) != null) && slot != MBServerStatics.SLOT_GLOVES)
				break;
			if (((this.restrictFlag & 512) != 0) && (equipped.get(MBServerStatics.SLOT_LEGGINGS) != null) && slot != MBServerStatics.SLOT_LEGGINGS)
				break;
			if (((this.restrictFlag & 1024) != 0) && (equipped.get(MBServerStatics.SLOT_FEET) != null) && slot != MBServerStatics.SLOT_FEET)
				break;

			// Passed validation.  Is a valid armor piece

			validSlot = true;
			break;
		}
		return validSlot;
	}

	/**
	 * @return the uuid
	 */
	public final int getUUID() {
		return uuid;
	}

	public boolean isRing() {
		return ((this.equipFlag & (64 | 128 | 192)) != 0);
	}

	public boolean isNecklace() {
		return (this.equipFlag == 256);
	}

	public boolean isShield() {
		return this.type.equals(ItemType.ARMOR) && this.equipFlag == 2;
	}

	public boolean isLightArmor() {
		return this.skillRequired.equals("Wear Armor, Light");
	}

	public boolean isMediumArmor() {
		return this.skillRequired.equals("Wear Armor, Medium");
	}

	public boolean isHeavyArmor() {
		return this.skillRequired.equals("Wear Armor, Heavy");
	}

	public boolean isClothArmor() {
		return this.skillRequired.isEmpty();
	}

	public boolean isThrowing() {
		return this.mastery.equals("Throwing") ? true : false;
	}

	public boolean isStaff() {
		return this.mastery.equals("Staff") ? true : false;
	}

	public boolean isScepter() {
		return this.mastery.equals("Benediction") ? true : false;
	}

	public boolean isArchery() {
		return this.mastery.equals("Archery") ? true : false;
	}

	public boolean isMelee() {
		return (this.isThrowing() == false && this.isStaff() == false && this.isScepter() == false && this.isArchery() == false);
	}

	public boolean isTwoHanded() {
		return this.twoHanded;
	}

	/**
	 * @return the restrictFlag
	 */
	public int getRestrictFlag() {
		return restrictFlag;
	}

	/**
	 * @return the slashResist
	 */
	public float getSlashResist() {
		return slashResist;
	}

	/**
	 * @return the crushResist
	 */
	public float getCrushResist() {
		return crushResist;
	}

	/**
	 * @return the pierceResist
	 */
	public float getPierceResist() {
		return pierceResist;
	}

	/**
	 * @return the skillRequired
	 */
	public String getSkillRequired() {
		return skillRequired;
	}

	/**
	 * @return the mastery
	 */
	public String getMastery() {
		return mastery;
	}

	/**
	 * @return the blockMod
	 */
	public float getBlockMod() {
		return blockMod;
	}

	/**
	 * @return the defense
	 */
	public short getDefense() {
		return defense;
	}

	/**
	 * @return the dexPenalty
	 */
	public float getDexPenalty() {
		return dexPenalty;
	}

	/**
	 * @return the speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * @return the range
	 */
	public float getRange() {
		return range;
	}

	/**
	 * @return the isStrBased
	 */
	public boolean isStrBased() {
		return isStrBased;
	}

	/**
	 * @return the parryBonus
	 */
	public float getParryBonus() {
		return parryBonus;
	}

	/**
	 * @return the maxDamage
	 */
	public short getMaxDamage() {
		return maxDamage;
	}

	/**
	 * @return the minDamage
	 */
	public short getMinDamage() {
		return minDamage;
	}

	/**
	 * @return the damageType
	 */
	public engine.Enum.DamageType getDamageType() {
		return damageType;
	}

	public short getPercentRequired() {
		return percentRequired;
	}

	public ArrayList<Integer> getAnimations() {
		return animations;
	}

	public void setAnimations(ArrayList<Integer> animations) {
		this.animations = animations;
	}

	public ArrayList<Integer> getOffHandAnimations() {
		return offHandAnimations;
	}

	public void setOffHandAnimations(ArrayList<Integer> offHandAnimations) {
		this.offHandAnimations = offHandAnimations;
	}

	public boolean isAutoID() {
		return autoID;
	}

	public void setAutoID(boolean autoID) {
		this.autoID = autoID;
	}
}
