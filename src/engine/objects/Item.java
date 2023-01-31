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
import engine.ai.StaticMobActions;
import engine.exception.SerializationException;
import engine.gameManager.ConfigManager;
import engine.gameManager.DbManager;
import engine.gameManager.PowersManager;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.DeleteItemMsg;
import engine.powers.EffectsBase;
import engine.powers.effectmodifiers.AbstractEffectModifier;
import engine.powers.poweractions.AbstractPowerAction;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


public class Item extends AbstractWorldObject {

	private int ownerID;  //may be character, account, npc, mob
	private int flags; //1 = isIDed
	private int numberOfItems;
	private short durabilityCurrent;
	private final short durabilityMax;
	private final byte chargesMax;
	private byte chargesRemaining;
	private byte equipSlot;
	private boolean canDestroy;
	private boolean rentable;
	private boolean isRandom = false;

	private int value;

	public Enum.ItemContainerType containerType;

	private OwnerType ownerType;
	private int itemBaseID;
	private AbstractWorldObject lastOwner;
	private ArrayList<EnchantmentBase> enchants = new ArrayList<>();
	private final ConcurrentHashMap<AbstractEffectModifier, Float> bonuses = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private final ArrayList<String> effectNames = new ArrayList<>();
	private static ConcurrentHashMap<String, Integer> enchantValues = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private long dateToUpgrade;
	public ReentrantLock lootLock = new ReentrantLock();
	private String customName = "";
	private int magicValue;

	/**
	 * No Id Constructor
	 */
	public Item( ItemBase itemBase, int ownerID,
				OwnerType ownerType, byte chargesMax, byte chargesRemaining,
				short durabilityCurrent, short durabilityMax, boolean canDestroy,
				boolean rentable, Enum.ItemContainerType containerType, byte equipSlot,
				ArrayList<EnchantmentBase> enchants, String name) {
		super();
		this.itemBaseID = itemBase.getUUID();
		this.ownerID = ownerID;
		this.ownerType = ownerType;

		if (itemBase.getType().getValue() == 20){
			this.chargesMax = chargesMax;
			this.chargesRemaining = chargesRemaining;
		}
		else{
			this.chargesMax = (byte) itemBase.getNumCharges();
			this.chargesRemaining = (byte) itemBase.getNumCharges();
		}

		this.durabilityMax = (short) itemBase.getDurability();
		this.durabilityCurrent = (short) itemBase.getDurability();
		this.containerType = containerType;
		this.canDestroy = canDestroy;
		this.rentable = rentable;
		this.equipSlot = equipSlot;
		this.enchants = enchants;
		this.flags = 1;
        this.value = this.magicValue;
		this.customName = name;

		loadEnchantments();
		bakeInStats();
	}

	public Item( ItemBase itemBase, int ownerID,
			OwnerType ownerType, byte chargesMax, byte chargesRemaining,
			short durabilityCurrent, short durabilityMax, boolean canDestroy,
			boolean rentable, boolean inBank, boolean inVault,
			boolean inInventory, boolean isEquipped, boolean isForge, byte equipSlot,
			ArrayList<EnchantmentBase> enchants) {

		super();
		this.itemBaseID = itemBase.getUUID();
		this.ownerID = ownerID;
		this.ownerType = ownerType;

		this.chargesMax = (byte) itemBase.getNumCharges();
		this.chargesRemaining = (byte) itemBase.getNumCharges();

		this.durabilityMax = (short) itemBase.getDurability();
		this.durabilityCurrent = (short) itemBase.getDurability();

		this.canDestroy = canDestroy;
		this.rentable = rentable;

		this.equipSlot = equipSlot;
		this.enchants = enchants;
		this.flags = 1;

        this.value = this.magicValue;

		loadEnchantments();
		bakeInStats();
	}

	/**
	 * Normal Constructor
	 */
	public Item(ItemBase itemBase, int ownerID,
			OwnerType ownerType, byte chargesMax, byte chargesRemaining,
			short durabilityCurrent, short durabilityMax, boolean canDestroy,
			boolean rentable, boolean inBank, boolean inVault,
			boolean inInventory, boolean isEquipped, byte equipSlot,
			ArrayList<EnchantmentBase> enchants, int newUUID) {

		super(newUUID);
		this.itemBaseID = itemBase.getUUID();
		this.ownerID = ownerID;
		this.ownerType = ownerType;
		this.customName = "";

		this.chargesMax = (byte) itemBase.getNumCharges();
		this.chargesRemaining = (byte) itemBase.getNumCharges();

		this.durabilityMax = (short) itemBase.getDurability();
		this.durabilityCurrent = (short) itemBase.getDurability();
		this.canDestroy = canDestroy;
		this.rentable = rentable;
		this.equipSlot = equipSlot;
		this.enchants = enchants;
		this.flags = 1;
        this.value = this.magicValue;

		loadEnchantments();
		bakeInStats();
	}
	/**
	 * ResultSet Constructor
	 */
	public Item(ResultSet rs) throws SQLException {
		super(rs);

		this.itemBaseID = rs.getInt("item_itemBaseID");

		// Set container enumeration

		String container = rs.getString("item_container");

		switch (container) {
			case "inventory":
				this.containerType = Enum.ItemContainerType.INVENTORY;
				break;
			case "bank":
				this.containerType = Enum.ItemContainerType.BANK;
				break;
			case "vault":
				this.containerType = Enum.ItemContainerType.VAULT;
				break;
			case "equip":
				this.containerType = Enum.ItemContainerType.EQUIPPED;
				break;
			case "forge":
				this.containerType = Enum.ItemContainerType.FORGE;
				break;
			case "warehouse":
				this.containerType = Enum.ItemContainerType.FORGE;
				break;
		}

		this.ownerID = rs.getInt("parent");

		if (this.getItemBase() != null)
			this.chargesMax = (byte) this.getItemBase().getNumCharges();
		else
			this.chargesMax = 0;

		this.chargesRemaining = rs.getByte("item_chargesRemaining");

		this.durabilityCurrent = rs.getShort("item_durabilityCurrent");
		this.durabilityMax = rs.getShort("item_durabilityMax");

		String ot = DbManager.ItemQueries.GET_OWNER(this.ownerID);

		if (ot.equals("character"))
			this.ownerType = OwnerType.PlayerCharacter;
		else if (ot.equals("npc"))
			this.ownerType = OwnerType.Npc;
		else if (ot.equals("account"))
			this.ownerType = OwnerType.Account;

		this.canDestroy = true;

		this.equipSlot = rs.getByte("item_equipSlot");

		this.numberOfItems = rs.getInt("item_numberOfItems");

		this.flags = rs.getInt("item_flags");
		this.dateToUpgrade = rs.getLong("item_dateToUpgrade");
		this.value = rs.getInt("item_value");
		this.customName = rs.getString("item_name");


	}

	public String getCustomName() {
		return customName;
	}

	public void setName(String name) {
		this.customName = name;
	}

	public ItemBase getItemBase() {
		return ItemBase.getItemBase(itemBaseID);
	}

	public int getItemBaseID() {
		return this.itemBaseID;
	}

	public int getOwnerID() {
		return ownerID;
	}

	public OwnerType getOwnerType() {
		return ownerType;
	}

	public AbstractGameObject getOwner() {
		if (this.ownerType == OwnerType.Npc)
			return NPC.getFromCache(this.ownerID);
		else if (this.ownerType == OwnerType.PlayerCharacter)
			return PlayerCharacter.getFromCache(this.ownerID);
		else if (this.ownerType == OwnerType.Mob)
			return StaticMobActions.getFromCache(this.ownerID);
		else if (this.ownerType == OwnerType.Account)
			return DbManager.AccountQueries.GET_ACCOUNT(this.ownerID);
		else
			return null;
	}

	//Only to be used for trading
	public void setOwnerID(int ownerID) {
		this.ownerID = ownerID;
	}

	public boolean setOwner(AbstractGameObject owner) {
		if (owner == null)
			return false;
		if (owner.getObjectType().equals(GameObjectType.NPC))
			this.ownerType = OwnerType.Npc;
		else if (owner.getObjectType().equals(GameObjectType.PlayerCharacter))
			this.ownerType = OwnerType.PlayerCharacter;
		else if (owner.getObjectType().equals(GameObjectType.Mob))
			this.ownerType = OwnerType.Mob;
		else if (owner.getObjectType().equals(GameObjectType.Account))
			this.ownerType = OwnerType.Account;
		else
			return false;
		this.ownerID = owner.getObjectUUID();
		return true;
	}

	public boolean isOwnerNPC() {
		return (ownerType == OwnerType.Npc);
	}

	public boolean isOwnerCharacter() {
		return (ownerType == OwnerType.PlayerCharacter);
	}

	public boolean isOwnerAccount() {
		return (ownerType == OwnerType.Account);
	}

	public byte getChargesMax() {
		return chargesMax;
	}

	public byte getChargesRemaining() {
		return chargesRemaining;
	}

	public short getDurabilityCurrent() {
		return durabilityCurrent;
	}

	public short getDurabilityMax() {
		return durabilityMax;
	}

	public void setDurabilityCurrent(short value) {
		this.durabilityCurrent = value;
	}

	public boolean isCanDestroy() {
		return canDestroy;
	}

	public boolean isRentable() {
		return rentable;
	}

	public byte getEquipSlot() {
		return equipSlot;
	}

	public ArrayList<EnchantmentBase> getEnchants() {
		return enchants;
	}

	public int getNumOfItems() {
		return this.numberOfItems;
	}

	public synchronized void setNumOfItems(int numberOfItems) {
		this.numberOfItems = numberOfItems;
	}

	public ConcurrentHashMap<AbstractEffectModifier, Float> getBonuses() {
		return this.bonuses;
	}

	public void clearBonuses() {
		this.bonuses.clear();
	}

	
	public float getBonus(ModType modType, SourceType sourceType) {
		
		int amount = 0;
		for (AbstractEffectModifier modifier: this.getBonuses().keySet()){
			if (modifier.getPercentMod() != 0)
				continue;
			if (modifier.modType.equals(modType) == false || modifier.sourceType.equals(sourceType)== false)
				continue;
			amount += this.bonuses.get(modifier);
		}
		return amount;
	}
	
public float getBonusPercent(ModType modType, SourceType sourceType) {
		
		int amount = 0;
		for (AbstractEffectModifier modifier: this.getBonuses().keySet()){
			
			if (modifier.getPercentMod() == 0)
				continue;
			if (modifier.modType.equals(modType) == false || modifier.sourceType.equals(sourceType)== false)
				continue;
			amount += this.bonuses.get(modifier);
		}
		return amount;
	}

	public boolean isID() {
		return ((this.flags & 1) > 0);
	}

	public void setIsID(boolean value) {
		if (value)
			this.flags |= 1;
		else
			this.flags &= ~1;
	}

	public void setIsComplete(boolean value) {
		if (value)
			this.flags |= 2;
		else
			this.flags &= ~2;
	}

	public boolean isComplete() {
        return this.dateToUpgrade < System.currentTimeMillis() + 1000;
    }

	public String getContainerInfo() {
		String ret = "OwnerID: " + this.ownerID + ", container: ";
		ret += this.containerType.toString();
		ret += "Equip Slot: " + this.equipSlot;
		return ret;
	}

	public int getFlags() {
		return this.flags;
	}

	public void setFlags(int value) {
		this.flags = value;
	}

	public void addBonus(AbstractEffectModifier key, float amount) {
		if (this.bonuses.containsKey(key))
			this.bonuses.put(key, (this.bonuses.get(key) + amount));
		else
			this.bonuses.put(key, amount);
	}

	public void multBonus(AbstractEffectModifier key, float amount) {
		if (this.bonuses.containsKey(key))
			this.bonuses.put(key, (this.bonuses.get(key) * amount));
		else
			this.bonuses.put(key, amount);
	}

	public synchronized void decrementChargesRemaining() {
		this.chargesRemaining -= 1;
		if (this.chargesRemaining < 0)
			this.chargesRemaining = 0;
		DbManager.ItemQueries.UPDATE_REMAINING_CHARGES(this);
	}

	protected void validateItemContainer() {

        if (this.containerType == Enum.ItemContainerType.NONE)

			if (this.ownerID != 0)
				// Item has an owner, just somehow the flags got messed up.
				// Default to bank.
				// TODO NEED LOG EVENT HERE.
				this.containerType = Enum.ItemContainerType.BANK;
			else
				// This item is on the ground. Nothing to worry about.
				this.zeroItem();
	}

	// Removes all ownership of item and 'orphans' it.
	protected synchronized void junk() {

		DbManager.ItemQueries.UPDATE_OWNER(this, 0, false, false, false, ItemContainerType.NONE, 0);
		this.zeroItem();

		//TODO do we want to delete the item here?
		this.lastOwner = null;
		//cleanup item from server.
		this.removeFromCache();
	}

	public synchronized void zeroItem() {
		this.ownerID = 0;

		this.ownerType = null;
		this.containerType = Enum.ItemContainerType.NONE;
		this.equipSlot = MBServerStatics.SLOT_UNEQUIPPED;
	}

	protected synchronized boolean moveItemToInventory(PlayerCharacter pc) {
		if (!DbManager.ItemQueries.UPDATE_OWNER(this,
				pc.getObjectUUID(), //tableID
				false, //isNPC
				true, //isPlayer
				false, //isAccount
				ItemContainerType.INVENTORY,
				0)) //Slot

			return false;

		this.zeroItem();
		this.ownerID = pc.getObjectUUID();
		this.ownerType = OwnerType.PlayerCharacter;
		this.containerType = ItemContainerType.INVENTORY;
		return true;
	}

	protected synchronized boolean moveItemToInventory(NPC npc) {
		if (npc.isStatic()) {
			if (!DbManager.ItemQueries.UPDATE_OWNER(this, 0, false, false, false,ItemContainerType.INVENTORY, 0))
				return false;
		} else
			if (!DbManager.ItemQueries.UPDATE_OWNER(this,
					npc.getObjectUUID(), //UUID
					true, //isNPC
					false, //isPlayer
					false, //isAccount
					ItemContainerType.INVENTORY,
					0)) //Slot

				return false;
		this.zeroItem();
		this.ownerID = npc.getObjectUUID();
		this.ownerType = OwnerType.Npc;
		this.containerType = Enum.ItemContainerType.INVENTORY;
		return true;
	}

	protected synchronized boolean moveItemToInventory(Corpse corpse) {
		if (!DbManager.ItemQueries.UPDATE_OWNER(this,
				0, //no ID for corpse
				false, //isNPC
				true, //isPlayer
				false, //isAccount
				ItemContainerType.INVENTORY,
				0)) //Slot

			return false;
		this.zeroItem();
		this.ownerID = 0;
		this.ownerType = null;
		this.containerType = Enum.ItemContainerType.INVENTORY;
		return true;
	}

	protected synchronized boolean moveItemToBank(PlayerCharacter pc) {
		if (!DbManager.ItemQueries.UPDATE_OWNER(this,
				pc.getObjectUUID(), //UUID
				false, //isNPC
				true, //isPlayer
				false, //isAccount
				ItemContainerType.BANK,
				0)) //Slot

			return false;
		this.zeroItem();
		this.ownerID = pc.getObjectUUID();
		this.ownerType = OwnerType.PlayerCharacter;
		this.containerType = Enum.ItemContainerType.BANK;
		return true;
	}

	protected synchronized boolean moveItemToBank(NPC npc) {
		if (!DbManager.ItemQueries.UPDATE_OWNER(this,
				npc.getObjectUUID(), //UUID
				true, //isNPC
				false, //isPlayer
				false, //isAccount
				ItemContainerType.BANK,
				0)) //Slot

			return false;
		this.zeroItem();
		this.ownerID = npc.getObjectUUID();
		this.ownerType = OwnerType.Npc;
		this.containerType = Enum.ItemContainerType.BANK;
		return true;
	}

	protected synchronized boolean moveItemToVault(Account a) {
		if (!DbManager.ItemQueries.UPDATE_OWNER(this,
				a.getObjectUUID(), //UUID
				false, //isNPC
				false, //isPlayer
				true, //isAccount
				ItemContainerType.VAULT,
				0)) //Slot

			return false;
		this.zeroItem();
		this.ownerID = a.getObjectUUID();
		this.ownerType = OwnerType.Account;
		this.containerType = Enum.ItemContainerType.VAULT;
		return true;
	}

	protected synchronized boolean equipItem(PlayerCharacter pc, byte slot) {

		if (!DbManager.ItemQueries.UPDATE_OWNER(this,
				pc.getObjectUUID(), //tableID
				false, //isNPC
				true, //isPlayer
				false, //isAccount
				ItemContainerType.EQUIPPED,
				slot)) //Slot

			return false;
		this.zeroItem();
		this.ownerID = pc.getObjectUUID();
		this.ownerType = OwnerType.PlayerCharacter;
		this.containerType = Enum.ItemContainerType.EQUIPPED;
		this.equipSlot = slot;
		return true;
	}

	protected synchronized boolean equipItem(NPC npc, byte slot) {
		if (!DbManager.ItemQueries.UPDATE_OWNER(this,
				npc.getObjectUUID(), //UUID
				true, //isNPC
				false, //isPlayer
				false, //isAccount
				ItemContainerType.EQUIPPED,
				slot)) //Slot

			return false;
		this.zeroItem();
		this.ownerID = npc.getObjectUUID();
		this.ownerType = OwnerType.Npc;
		this.containerType = Enum.ItemContainerType.EQUIPPED;
		this.equipSlot = slot;
		return true;
	}

	protected synchronized boolean equipItem(Mob npc, byte slot) {

		this.zeroItem();
		this.ownerID = npc.getObjectUUID();
		this.ownerType = OwnerType.Mob;
		this.containerType = Enum.ItemContainerType.EQUIPPED;
		this.equipSlot = slot;
		return true;
	}

	
	public static void _serializeForClientMsg(Item item, ByteBufferWriter writer)
			throws SerializationException {
		Item._serializeForClientMsg(item, writer, true);
	}

	public static void serializeForClientMsgWithoutSlot(Item item, ByteBufferWriter writer) {
		Item._serializeForClientMsg(item, writer, false);
	}

	public static void serializeForClientMsgForVendor(Item item, ByteBufferWriter writer, float percent) {
		Item._serializeForClientMsg(item, writer, true);
        int baseValue = item.magicValue;
		writer.putInt(baseValue);
		writer.putInt((int) (baseValue * percent));
	}

	public static void serializeForClientMsgForVendorWithoutSlot(Item item,ByteBufferWriter writer, float percent) {
		Item._serializeForClientMsg(item, writer, false);
		writer.putInt(item.getValue());
		writer.putInt(item.getValue());
	}

	public static void _serializeForClientMsg(Item item,ByteBufferWriter writer,
			boolean includeSlot) {
		if (includeSlot)
			writer.putInt(item.equipSlot);
		writer.putInt(0); // Pad
		writer.putInt(item.getItemBase().getUUID());

		writer.putInt(item.getObjectType().ordinal());
		writer.putInt(item.getObjectUUID());

		// Unknown statics
		for (int i = 0; i < 3; i++) {
			writer.putInt(0); // Pad
		}
		for (int i = 0; i < 4; i++) {
			writer.putInt(0x3F800000); // Static
		}
		for (int i = 0; i < 5; i++) {
			writer.putInt(0); // Pad
		}
		for (int i = 0; i < 2; i++) {
			writer.putInt(0xFFFFFFFF); // Static
		}

		// Handle Hair / Beard / horns Color.
		boolean isHair = (item.equipSlot == (byte) MBServerStatics.SLOT_HAIRSTYLE);
		boolean isBeard = (item.equipSlot == (byte) MBServerStatics.SLOT_BEARDSTYLE);
		int itemColor = 0;
		if (isHair || isBeard) {
            PlayerCharacter pc = PlayerCharacter.getFromCache(item.ownerID);
			if (pc != null)
				if (isHair)
					itemColor = pc.getHairColor();
				else if (isBeard)
					itemColor = pc.getBeardColor();
		}
		writer.putInt(itemColor);

		writer.put((byte) 1); // End Datablock byte
		if (item.customName.isEmpty() || item.customName.isEmpty()){
			writer.putInt(0);
		}

		else
			writer.putString(item.customName); // Unknown. pad?
		writer.put((byte) 1); // End Datablock byte

		writer.putFloat((float)item.durabilityMax);
		writer.putFloat((float)item.durabilityCurrent);

		writer.put((byte) 1); // End Datablock byte

		writer.putInt(0); // Pad
		writer.putInt(0); // Pad

		if (item.getItemBase().equals(ItemBase.GOLD_ITEM_BASE)){
			
			if (item.getOwner() != null && item.getOwner().getObjectType() == GameObjectType.PlayerCharacter){
			PlayerCharacter player = (PlayerCharacter)item.getOwner();
			int tradingAmount = player.getCharItemManager().getGoldTrading();
			writer.putInt(item.numberOfItems - tradingAmount);
			}else
			writer.putInt(item.numberOfItems); // Amount of gold
		}
			
		else
			writer.putInt(item.getItemBase().getBaseValue());

		writer.putInt(item.getValue());

		int effectsSize = item.effects.size();
		ArrayList<Effect> effs = null;
		Effect nextE = null;
		if (effectsSize > 0 && item.isID()) {
			effs = new ArrayList<>(item.effects.values());

			//Don't send effects that have a token of 1
			Iterator<Effect> efi = effs.iterator();
			while (efi.hasNext()) {
				nextE = efi.next();
				if (nextE.getEffectToken() == 1 || nextE.bakedInStat())
					efi.remove();
			}
		} else
			effs = new ArrayList<>();

		int effectsToSendSize = effs.size();
		writer.putInt(effectsToSendSize);
		for (int i = 0; i < effectsToSendSize; i++) {
			effs.get(i).serializeForItem(writer, item);
		}
		writer.putInt(0x00000000);


		if (effectsSize > 0)
			if (item.isID())
				writer.putInt(36); //Magical, blue name
			else
				writer.putInt(40); //Magical, unidentified
		else if (item.getItemBase().getBakedInStats().size() > 0)
			writer.putInt(36); //Magical, blue name
		else
			writer.putInt(4); //Non-Magical, grey name
		writer.putInt(item.chargesRemaining);
		writer.putInt(0); // Pad
		writer.putInt(item.numberOfItems);
		writer.put((byte)0);


		if (item.getItemBase().getType().getValue() != 20){
			writer.putShort((short)0);
			return;
		}
		writer.put((byte)1); //
		writer.putInt(0);
		writer.putInt(0);
		if (item.chargesRemaining == 0)
			writer.putInt(1);
		else
			writer.putInt(item.chargesRemaining);
		writer.put((byte) 0);
	}
	
	public static void SerializeTradingGold(PlayerCharacter player,ByteBufferWriter writer) {
		
		writer.putInt(0); // Pad
		writer.putInt(7);

		writer.putInt(GameObjectType.Item.ordinal());
		writer.putInt(player.getObjectUUID());

		// Unknown statics
		for (int i = 0; i < 3; i++) {
			writer.putInt(0); // Pad
		}
		for (int i = 0; i < 4; i++) {
			writer.putInt(0x3F800000); // Static
		}
		for (int i = 0; i < 5; i++) {
			writer.putInt(0); // Pad
		}
		for (int i = 0; i < 2; i++) {
			writer.putInt(0xFFFFFFFF); // Static
		}

		// Handle Hair / Beard / horns Color.
	
		int itemColor = 0;
		writer.putInt(itemColor);

		writer.put((byte) 1); // End Datablock byte
			writer.putInt(0);
		writer.put((byte) 1); // End Datablock byte

		writer.putFloat((float)1);
		writer.putFloat((float)1);

		writer.put((byte) 1); // End Datablock byte

		writer.putInt(0); // Pad
		writer.putInt(0); // Pad

		
			writer.putInt(player.getCharItemManager().getGoldTrading()); // Amount of gold
	

		writer.putInt(0);

		
		writer.putInt(0);
	
		writer.putInt(0x00000000);

			writer.putInt(4); //Non-Magical, grey name
		writer.putInt(1);
		writer.putInt(0); // Pad
		writer.putInt(player.getCharItemManager().getGoldTrading());
		writer.put((byte)0);

			writer.putShort((short)0);

	}

	public static boolean MakeItemForPlayer(ItemBase toCreate, PlayerCharacter reciever, int amount) {

		boolean itemWorked = false;

		Item item = new Item( toCreate, reciever.getObjectUUID(), OwnerType.PlayerCharacter, (byte) 0, (byte) 0,
				(short) 1, (short) 1, true, false,  Enum.ItemContainerType.INVENTORY, (byte) 0,
                new ArrayList<>(),"");

		synchronized (item) {
			item.numberOfItems = amount;
		}
		item.containerType = Enum.ItemContainerType.INVENTORY;

		try {
			item = DbManager.ItemQueries.ADD_ITEM(item);
			itemWorked = true;
		} catch (Exception e) {
			Logger.error(e);
		}

		if (!itemWorked)
			return false;

		reciever.getCharItemManager().addItemToInventory(item);
		reciever.getCharItemManager().updateInventory();

		return true;
	}

	public static Item deserializeFromClientMsg(ByteBufferReader reader,
			boolean includeSlot) {
		if (includeSlot)
			reader.getInt();
		reader.getInt();
		int itemBase = reader.getInt(); //itemBase
		int objectType = reader.getInt(); //object type;
		int UUID = reader.getInt();
		for (int i = 0; i < 14; i++) {
			reader.getInt(); // Pads and statics
		}
		int unknown = reader.getInt();
		
		byte readString = reader.get();
		if (readString == 1)
		reader.getString();
		byte readDurability = reader.get();
		if (readDurability == 1){
			reader.getInt();
			reader.getInt();
		}
		
		byte readEnchants = reader.get();
		if (readEnchants == 1){
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			int enchantSize = reader.getInt();
			for (int i = 0; i < enchantSize; i++) {
				reader.getInt(); //effect token
				reader.getInt(); //trains
				int type = reader.getInt();
				reader.get();
				if (type == 1)
					reader.getLong(); //item comp
				else
					reader.getInt(); //power token
				reader.getString(); //name
				reader.getFloat(); //duration
			}
			for (int i = 0; i < 5; i++) {
				reader.getInt();
			}
		}
		
		reader.get();
		byte isContract = reader.get();
		if (isContract == 1){
			reader.getInt();
			reader.getInt();
			reader.getInt();
		}
		reader.get();

		if (UUID == 0 || objectType == 0)
			return null;
		if (objectType == GameObjectType.MobLoot.ordinal())
			return MobLoot.getFromCache(UUID);
		return Item.getFromCache(UUID);
	}

	public final int getMagicValue() {
		return this.magicValue;
	}

	public int getBaseValue() {
		if (this.getItemBase() != null)
			return this.getItemBase().getBaseValue();
		return 0;
	}

	public static void putListForVendor(ByteBufferWriter writer, ArrayList<Item> list, NPC vendor) {
		putList(writer, list, false, vendor.getObjectUUID(), true, vendor);
	}

	public static void putList(ByteBufferWriter writer, ArrayList<Item> list, boolean includeSlot, int ownerID) {
		putList(writer, list, includeSlot, ownerID, false, null);
	}

	private static void putList(ByteBufferWriter writer, ArrayList<Item> list, boolean includeSlot, int ownerID, boolean forVendor, NPC vendor) {
		int indexPosition = writer.position();
		//reserve 4 bytes for index.
		writer.putInt(0);

		int serialized = 0;
		for (Item item : list) {

            if (item.getItemBase().getType().equals(ItemType.GOLD))
				if (item.numberOfItems == 0)
					continue;
			try {
				if (includeSlot && !forVendor)
					Item._serializeForClientMsg(item,writer);
				else if (!includeSlot && !forVendor)
					Item.serializeForClientMsgWithoutSlot(item,writer);

				if (!includeSlot && forVendor) //TODO separate for sell/buy percent

					Item.serializeForClientMsgForVendorWithoutSlot(item,writer, vendor.getSellPercent());

				if (includeSlot && forVendor) //TODO separate for sell/buy percent

					Item.serializeForClientMsgForVendor(item,writer, vendor.getSellPercent());

			} catch (SerializationException se) {
				continue;
			}
			++serialized;
		}

		writer.putIntAt(serialized, indexPosition);
	}
	
	public static void putTradingList(PlayerCharacter player, ByteBufferWriter writer, ArrayList<Item> list, boolean includeSlot, int ownerID, boolean forVendor, NPC vendor) {
		int indexPosition = writer.position();
		//reserve 4 bytes for index.
		writer.putInt(0);

		int serialized = 0;
		for (Item item : list) {

            if (item.getItemBase().getType().equals(ItemType.GOLD))
				if (item.numberOfItems == 0)
					continue;
			try {
				if (includeSlot && !forVendor)
					Item._serializeForClientMsg(item,writer);
				else if (!includeSlot && !forVendor)
					Item.serializeForClientMsgWithoutSlot(item,writer);

				if (!includeSlot && forVendor) //TODO separate for sell/buy percent

					Item.serializeForClientMsgForVendorWithoutSlot(item,writer, vendor.getSellPercent());

				if (includeSlot && forVendor) //TODO separate for sell/buy percent

					Item.serializeForClientMsgForVendor(item,writer, vendor.getSellPercent());

			} catch (SerializationException se) {
				continue;
			}
			++serialized;
		}
		if (player.getCharItemManager().getGoldTrading() > 0){
			Item.SerializeTradingGold(player, writer);
			++serialized;
		}
		

		writer.putIntAt(serialized, indexPosition);
	}

	public AbstractWorldObject getLastOwner() {
		return this.lastOwner;
	}

	public void setLastOwner(AbstractWorldObject value) {
		this.lastOwner = value;
	}


	@Override
	public String getName() {
		if (this.customName.isEmpty())
			if (this.getItemBase() != null)
				return this.getItemBase().getName();
		return this.customName;
	}



	private void bakeInStats() {

		EffectsBase effect;

		if (ConfigManager.serverType.equals(Enum.ServerType.LOGINSERVER))
			return;

		if (this.getItemBase() != null)

			for (Integer token : this.getItemBase().getBakedInStats().keySet()) {

				effect = PowersManager.getEffectByToken(token);

				if (effect == null) {
					Logger.error("missing effect of token " + token);
					continue;
				}
				AbstractPowerAction apa = PowersManager.getPowerActionByIDString(effect.getIDString());
				apa.applyBakedInStatsForItem(this, this.getItemBase().getBakedInStats().get(token));
			}
	}

	public final void loadEnchantments() {
		//dont load mobloot enchantments, they arent in db.
		if (this.getObjectType().equals(GameObjectType.MobLoot)){
			this.magicValue =  this.getItemBase().getBaseValue() + calcMagicValue();
			return;
		}
		

		ConcurrentHashMap<String, Integer> enchantList = DbManager.EnchantmentQueries.GET_ENCHANTMENTS_FOR_ITEM(this.getObjectUUID());

		for (String enchant : enchantList.keySet()) {
			AbstractPowerAction apa = PowersManager.getPowerActionByIDString(enchant);
			if (apa != null) {
				apa.applyEffectForItem(this, enchantList.get(enchant));
				this.effectNames.add(enchant);
			}
		}
		
		this.magicValue =  this.getItemBase().getBaseValue() + calcMagicValue();
	}

	public HashMap<Integer, Integer> getBakedInStats() {
		if (this.getItemBase() != null)
			return this.getItemBase().getBakedInStats();
		return null;
	}

	public void clearEnchantments() {

		//Clear permanent enchantment out of database
		DbManager.EnchantmentQueries.CLEAR_ENCHANTMENTS((long) this.getObjectUUID());

		for (String name : this.getEffects().keySet()) {
			Effect eff = this.getEffects().get(name);
			if (!eff.bakedInStat())
				this.endEffect(name);
		}
		this.effectNames.clear();
	}

	public void addPermanentEnchantment(String enchantID, int rank) {
		AbstractPowerAction apa = PowersManager.getPowerActionByIDString(enchantID);
		if (apa == null)
			return;

		DbManager.EnchantmentQueries.CREATE_ENCHANTMENT_FOR_ITEM((long) this.getObjectUUID(), enchantID, rank);
		apa.applyEffectForItem(this, rank);
		this.effectNames.add(enchantID);
	}

	public void addPermanentEnchantmentForDev(String enchantID, int rank) {
		AbstractPowerAction apa = PowersManager.getPowerActionByIDString(enchantID);
		if (apa == null)
			return;

		DbManager.EnchantmentQueries.CREATE_ENCHANTMENT_FOR_ITEM((long) this.getObjectUUID(), enchantID, rank);
		apa.applyEffectForItem(this, rank);
		this.effectNames.add(enchantID);
	}

	protected int calcMagicValue() {
		int ret = 0;
		for (String enchant : this.effectNames) {
			ret += Item.getEnchantValue(enchant+ 'A');
		}
		return ret;
	}

	public static Item createItemForPlayer(PlayerCharacter pc, ItemBase ib) {
		Item item = null;
		byte charges = 0;

		charges = (byte) ib.getNumCharges();

		short durability = (short) ib.getDurability();

		Item temp = new Item( ib, pc.getObjectUUID(),
				OwnerType.PlayerCharacter, charges, charges, durability, durability,
				true, false,  Enum.ItemContainerType.INVENTORY, (byte) 0,
                new ArrayList<>(),"");
		try {
			item = DbManager.ItemQueries.ADD_ITEM(temp);
		} catch (Exception e) {
			Logger.error(e);
		}
		return item;
	}

	public static Item createItemForPlayerBank(PlayerCharacter pc, ItemBase ib) {
		Item item = null;
		byte charges = 0;

		charges = (byte) ib.getNumCharges();

		short durability = (short) ib.getDurability();

		Item temp = new Item( ib, pc.getObjectUUID(),
				OwnerType.PlayerCharacter, charges, charges, durability, durability,
				true, false, Enum.ItemContainerType.BANK, (byte) 0,
                new ArrayList<>(),"");
		try {
			item = DbManager.ItemQueries.ADD_ITEM(temp);
		} catch (Exception e) {
		}
		return item;
	}

	public static Item createItemForMob(Mob mob, ItemBase ib) {
		Item item = null;
		byte charges = 0;

		charges = (byte) ib.getNumCharges();
		short durability = (short) ib.getDurability();

		Item temp = new Item( ib, mob.getObjectUUID(),
				OwnerType.Mob, charges, charges, durability, durability,
				true, false, Enum.ItemContainerType.INVENTORY, (byte) 0,
                new ArrayList<>(),"");
		try {
			item = DbManager.ItemQueries.ADD_ITEM(temp);
		} catch (Exception e) {
			Logger.error(e);
		}
		return item;
	}

	public static Item getFromCache(int id) {
		return (Item) DbManager.getFromCache(GameObjectType.Item, id);
	}



	public void addToCache() {
		DbManager.addToCache(this);
	}

	public static Item newGoldItem(AbstractWorldObject awo, ItemBase ib, Enum.ItemContainerType containerType) {
		return newGoldItem(awo, ib, containerType, true);
	}

	//used for vault!
	public static Item newGoldItem(int accountID,ItemBase ib, Enum.ItemContainerType containerType) {
		return newGoldItem(accountID, ib, containerType, true);
	}

	private static Item newGoldItem(int accountID, ItemBase ib, Enum.ItemContainerType containerType, boolean persist) {

		int ownerID;
		OwnerType ownerType;

		ownerID = accountID;
		ownerType = OwnerType.Account;


		Item newGold = new Item( ib, ownerID, ownerType,
				(byte) 0, (byte) 0, (short) 0, (short) 0, true, false,  containerType, (byte) 0,
                new ArrayList<>(),"");

		synchronized (newGold) {
			newGold.numberOfItems = 0;
		}

		if (persist) {
			try {
				newGold = DbManager.ItemQueries.ADD_ITEM(newGold);
				if (newGold != null) {
					synchronized (newGold) {
						newGold.numberOfItems = 0;
					}
				}
			} catch (Exception e) {
				Logger.error(e);
			}
			DbManager.ItemQueries.ZERO_ITEM_STACK(newGold);
		}

		return newGold;
	}

	private static Item newGoldItem(AbstractWorldObject awo, ItemBase ib, Enum.ItemContainerType containerType,boolean persist) {

		int ownerID;
		OwnerType ownerType;

		if (awo.getObjectType().equals(GameObjectType.Mob))
			return null;

		if (containerType == Enum.ItemContainerType.VAULT) {
			if (!(awo.getObjectType().equals(GameObjectType.PlayerCharacter))) {
				Logger.error("AWO is not a PlayerCharacter");
				return null;
			}
			ownerID = ((PlayerCharacter) awo).getAccount().getObjectUUID();
			ownerType = OwnerType.Account;
		} else {

			ownerID = awo.getObjectUUID();

			switch (awo.getObjectType()) {

			case NPC:
				ownerType = OwnerType.Npc;
				break;
			case PlayerCharacter:
				ownerType = OwnerType.PlayerCharacter;
				break;
			case Mob:
				ownerType = OwnerType.Mob;
				break;
			default:
				Logger.error("Unsupported AWO object type.");
				return null;
			}
		}

		Item newGold = new Item( ib, ownerID, ownerType,
				(byte) 0, (byte) 0, (short) 0, (short) 0, true, false, containerType, (byte) 0,
                new ArrayList<>(),"");

		synchronized (newGold) {
			newGold.numberOfItems = 0;
		}

		if (persist) {
			try {
				newGold = DbManager.ItemQueries.ADD_ITEM(newGold);
				if (newGold != null) {
					synchronized (newGold) {
						newGold.numberOfItems = 0;
					}
				}
			} catch (Exception e) {
				Logger.error(e);
			}
			DbManager.ItemQueries.ZERO_ITEM_STACK(newGold);
		}
		newGold.containerType = containerType;

		return newGold;
	}

	// This is to be used for trades - the new item is not stored in the database
	public static Item newGoldItemTemp(AbstractWorldObject awo, ItemBase ib) {
		return Item.newGoldItem(awo, ib, Enum.ItemContainerType.NONE,false);
	}

	public static Item getItem(int UUID) {
		if (UUID == 0)
			return null;

		Item item  = (Item) DbManager.getFromCache(GameObjectType.Item, UUID);
		if (item != null)
			return item;
		return DbManager.ItemQueries.GET_ITEM(UUID);
	}

	@Override
	public void updateDatabase() {
		//DbManager.ItemQueries.updateDatabase(this);
	}

	public static void addEnchantValue(String enchant, int value) {
		Item.enchantValues.put(enchant, value);
	}

	public static int getEnchantValue(String enchant) {
		if (Item.enchantValues.containsKey(enchant))
			return Item.enchantValues.get(enchant);
		return 0;
	}

	@Override
	public void runAfterLoad() {
		loadEnchantments();
		bakeInStats();
	}


	public ArrayList<String> getEffectNames() {
		return effectNames;
	}

	public boolean validForItem(long flags) {
		if (this.getItemBase() == null)
			return false;
		return this.getItemBase().validSlotFlag(flags);
	}

	public boolean validForInventory(ClientConnection origin, PlayerCharacter pc, CharacterItemManager charItemMan) {

		if (origin == null || pc == null || charItemMan == null)
			return false;

        if (ownerID != pc.getObjectUUID()) {
			Logger.warn("Inventory Item " + this.getObjectUUID() + " not owned by Character " + charItemMan.getOwner().getObjectUUID());
			charItemMan.updateInventory();
			return false;
		}

		if (!charItemMan.inventoryContains(this)){
			charItemMan.updateInventory();
			return false;
		}
		return true;
	}

	public boolean validForBank(ClientConnection origin, PlayerCharacter pc, CharacterItemManager charItemMan) {
		if (origin == null || pc == null || charItemMan == null)
			return false;

        if (!charItemMan.bankContains(this))
			return false;
		else if (ownerID != pc.getObjectUUID()) {
			Logger.warn("Bank Item " + this.getObjectUUID() + " not owned by Character " + charItemMan.getOwner().getObjectUUID());
			return false;
		}
		return true;
	}

	public boolean validForEquip(ClientConnection origin, PlayerCharacter pc, CharacterItemManager charItemMan) {
		if (origin == null || pc == null || charItemMan == null)
			return false;

        if (!charItemMan.equippedContains(this))
			return false;
		else if (ownerID != pc.getObjectUUID()) {
			//duped item, cleanup
			Logger.warn("Duped item id "
					+ this.getObjectUUID() + " removed from PC " + pc.getObjectUUID() + '.');
			DeleteItemMsg deleteItemMsg = new DeleteItemMsg(this.getObjectType().ordinal(), this.getObjectUUID());
			charItemMan.cleanupDupe(this);
			Dispatch dispatch = Dispatch.borrow(pc, deleteItemMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

			return false;
		}
		return true;
	}

	public boolean validForVault(ClientConnection origin, PlayerCharacter pc, CharacterItemManager charItemMan) {
		if (origin == null || pc == null || charItemMan == null)
			return false;

		if (pc.getAccount() == null)
			return false;

        if (!pc.getAccount().getVault().contains(this))
			return false;
		else if (ownerID != pc.getAccount().getObjectUUID()) {
			//duped item, cleanup
			Logger.warn("Duped item id "
					+ this.getObjectUUID() + " removed from PC " + pc.getObjectUUID() + '.');
			DeleteItemMsg deleteItemMsg = new DeleteItemMsg(this.getObjectType().ordinal(), this.getObjectUUID());
			charItemMan.cleanupDupe(this);
			Dispatch dispatch = Dispatch.borrow(pc, deleteItemMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			return false;
		}
		return true;
	}

	public long getDateToUpgrade() {
		return dateToUpgrade;
	}

	public void setDateToUpgrade(long dateToUpgrade) {
		this.dateToUpgrade = dateToUpgrade;
	}

	/**
	 * @return the value
	 */
	public int getValue() {

		if (this.value == 0)
			if (this.isID()) {
                return this.getMagicValue();
            }
			else
				return this.getBaseValue();

		return this.value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}

	public boolean isRandom() {
		return isRandom;
	}

	public void setRandom(boolean isRandom) {
		this.isRandom = isRandom;
	}
	
	public boolean isCustomValue(){
		if (this.value == 0)
			return false;
		return true;
	}
}
