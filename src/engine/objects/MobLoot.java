// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum;
import engine.Enum.ItemType;
import engine.Enum.OwnerType;
import engine.gameManager.DbManager;
import engine.gameManager.PowersManager;
import engine.powers.poweractions.AbstractPowerAction;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An immutable, non-persistant implementation of Item
 *
 * @author Burfo
 */
public final class MobLoot extends Item {

	private static final AtomicInteger LastUsedId = new AtomicInteger(0);

	private boolean isDeleted = false;
	private boolean noSteal;
	private String prefix = "";
	private String suffix = "";

	private int fidelityEquipID = 0;


	/**
	 * Create a new MobLoot.
	 * Do not use this to create Gold.
	 *
	 * @param mob Mob that owns this item
	 * @param ib ItemBase
	 */
	public MobLoot(AbstractCharacter mob, ItemBase ib, boolean noSteal) {
		this(mob, ib, 0, noSteal);
	}

	/**
	 * Create a new MobLoot item to hold Gold for the Mob.
	 *
	 * @param mob Mob that owns this item
	 * @param qtyOfGold Quantity of gold
	 */
	public MobLoot(AbstractCharacter mob, int qtyOfGold) {
		this(mob, ItemBase.getGoldItemBase(), qtyOfGold, false);
	}

	/**
	 * Create a new MobLoot.
	 * Primarily used for stackable items that have a quantity.
	 *
	 * @param mob Mob that owns this item
	 * @param ib ItemBase
	 * @param quantity Quantity of the item
	 */
	public MobLoot(AbstractCharacter mob, ItemBase ib, int quantity, boolean noSteal) {
		super( ib, mob.getObjectUUID(),
				OwnerType.Mob, (byte) 0, (byte) 0, (short) 0,
				(short) 0, true, false, false, false, true,
				false, (byte) 0, new ArrayList<>(), generateId());

		if (quantity == 0 && ib.getType() == ItemType.RESOURCE)
			quantity = 1;


		if (quantity > 0)
			this.setNumOfItems(quantity);

		this.noSteal = noSteal;
		this.setIsID(this.getItemBase().isAutoID());

		// Class is 'final'; passing 'this' should be okay at the end of the constructor

		DbManager.addToCache(this);
	}

	/**
	 * Converts this MotLoot to a persistable Item. Used when a MotLoot is
	 * looted
	 * from a Mob to a Player. Do not call for a Gold item.
	 *
	 * @return An orphaned Item, ready to be moved to the Player's inventory.
	 */
	public synchronized Item promoteToItem(PlayerCharacter looter) {

		if (looter == null)
			return null;

		if (isDeleted)
			return null;

		if (this.getItemBase().getType().equals(ItemType.GOLD))
			return null;

		
		Item item = this;
		
		item.setOwner(looter);
		//item.setIsID(false);

		item.containerType = Enum.ItemContainerType.INVENTORY;
		item.setValue(0);
		item.setName(this.getCustomName());
		item.setIsID(this.isID());

		if (this.getNumOfItems() > 1)
			item.setNumOfItems(this.getNumOfItems());

		try {
			item = DbManager.ItemQueries.ADD_ITEM(item);
		} catch (Exception e) {
			Logger.error("e");
			return null;
		}

		//		for (String effectName : this.effectNames)
		//			item.addPermanentEnchantment(effectName, 0);
		//transfer enchantments to item
		if (this.prefix.length() != 0)
			item.addPermanentEnchantment(this.prefix, 0);
		if (this.suffix.length() != 0)
			item.addPermanentEnchantment(this.suffix, 0);

		this.junk();
		return item;
	}

	public synchronized Item promoteToItemForNPC(NPC looter) {

		if (looter == null)
			return null;

		if (isDeleted)
			return null;

		if (this.getItemBase().getType().equals(ItemType.GOLD))
			return null;

		Item item = this;
		item.setOwner(looter);
		item.containerType = Enum.ItemContainerType.INVENTORY;
		item.setIsID(true);

		if (this.getNumOfItems() > 1)
			item.setNumOfItems(this.getNumOfItems());

		try {
			item = DbManager.ItemQueries.ADD_ITEM(item);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
		item.containerType = Enum.ItemContainerType.INVENTORY;

		//		for (String effectName : this.effectNames)
		//			item.addPermanentEnchantment(effectName, 0);
		//transfer enchantments to item
		try{
			for (String enchant:this.getEffectNames()){
				item.addPermanentEnchantment(enchant, 0);
			}
		}catch(Exception e){
			Logger.error(e.getMessage());
		}

		DbManager.NPCQueries.REMOVE_FROM_PRODUCTION_LIST(this.getObjectUUID(),looter.getObjectUUID());
		looter.removeItemFromForge(this);
		this.junk();
		return item;
	}

	public synchronized void recycle(NPC vendor){

		//remove from production list for npc in db

		DbManager.NPCQueries.REMOVE_FROM_PRODUCTION_LIST(this.getObjectUUID(),vendor.getObjectUUID());
		this.removeFromCache();
		isDeleted = true;
	}

	/**
	 * Junks the item and marks it as deleted
	 */
	@Override
	protected synchronized void junk() {
		this.removeFromCache();
		isDeleted = true;
	}

	/**
	 * Get the MobLoot object from its Id number
	 *
	 * @param id Id Number
	 * @return MobLoot object
	 */
	public static MobLoot getFromCache(int id) {
		return (MobLoot) DbManager.getFromCache(Enum.GameObjectType.MobLoot, id);
	}

	/**
	 * Determines if this object has been marked as deleted.
	 *
	 * @return True if deleted.
	 */
	public boolean isDeleted() {
		return this.isDeleted;
	}

	public boolean noSteal() {
		return this.noSteal;
	}

	public void addPermanentEnchantment(String enchantID, int rank, int value, boolean prefix) {
		AbstractPowerAction apa = PowersManager.getPowerActionByIDString(enchantID);
		if (apa == null)
			return;
		apa.applyEffectForItem(this, rank);

		//limit to 2 effects
		//		if (this.effectNames.size() < 2)
		//			this.effectNames.add(enchantID);
		if (prefix)
			this.prefix = enchantID;
		else
			this.suffix = enchantID;

		this.getEffectNames().add(enchantID);
	}

	/**
	 * Get the next available Id number.
	 *
	 * @return Id number
	 */
	private static int generateId() {
		int id = LastUsedId.decrementAndGet();

		//TODO Add a way to reclaim disposed IDs if this becomes a problem
		if (id == (-10000))
			Logger.warn("Only 10,000 Id numbers remain useable. Server restart suggested.");
		else if (id < Integer.MIN_VALUE + 1000)
			Logger.warn("Only " + (Integer.MIN_VALUE + id)
					+ " Id numbers remain useable! Server restart suggested.");
		else if (id == Integer.MIN_VALUE)
			throw new UnsupportedOperationException("MobLoot has no remaining Id numbers! Restart server immediately!");
		else if ((id % 10000) == 0)
			Logger.info( id + " of " + Integer.MIN_VALUE + " Id numbers consumed.");

		return id;
	}

	/* *****
	 * All of the following methods are overridden from
	 * the superclass and intentionally not implemented.
	 * *****
	 */
	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	public void setOwnerID(int id) {
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	public synchronized void decrementChargesRemaining() {
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	protected boolean equipItem(NPC npc, byte slot) {
		return false;
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	protected boolean equipItem(PlayerCharacter pc, byte slot) {
		return false;
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	protected boolean moveItemToBank(NPC npc) {
		return false;
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	protected boolean moveItemToBank(PlayerCharacter pc) {
		return false;
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	protected boolean moveItemToInventory(Corpse corpse) {
		return false;
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	protected boolean moveItemToInventory(NPC npc) {
		return false;
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	protected boolean moveItemToInventory(PlayerCharacter pc) {
		return false;
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	protected boolean moveItemToVault(Account a) {
		return false;
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	public void setLastOwner(AbstractWorldObject value) {
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	public void updateDatabase() {
	}

	/**
	 * Not implemented
	 */
	@Override
	@Deprecated
	protected void validateItemContainer() {
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public int getFidelityEquipID() {
		return fidelityEquipID;
	}

	public void setFidelityEquipID(int fidelityEquipID) {
		this.fidelityEquipID = fidelityEquipID;
	}



}
