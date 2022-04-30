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

import engine.Enum.GameObjectType;
import engine.Enum.GridObjectType;
import engine.Enum.ItemType;
import engine.InterestManagement.WorldGrid;
import engine.exception.SerializationException;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.RemoveCorpseJob;
import engine.net.ByteBufferWriter;
import engine.net.DispatchMessage;
import engine.net.client.msg.UnloadObjectsMsg;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Corpse extends AbstractWorldObject {

	private static AtomicInteger corpseCounter = new AtomicInteger(0);

	private String firstName;
	private String lastName;
	private int level;
	private int belongsToType;
	private int belongsToID;
	private ArrayList<Item> inventory;
	public JobContainer cleanup;
	private boolean asciiLastName = true;
	private boolean hasGold = false;
	private int inBuildingID = 0;
	private int inFloorID = -1;
	private int inBuilding = -1;

	/**
	 * No Id Constructor
	 */
	public Corpse( int newUUID, AbstractCharacter belongsTo, boolean safeZone,boolean enterWorld) {
		super(newUUID);
		this.setObjectType();
		this.inventory = new ArrayList<>();
		this.gridObjectType = GridObjectType.STATIC;
		this.setObjectTypeMask(MBServerStatics.MASK_CORPSE);
		if (belongsTo != null) {
			this.firstName = belongsTo.getFirstName();
			this.lastName = belongsTo.getLastName();
			this.asciiLastName = belongsTo.asciiLastName();
			this.level = belongsTo.getLevel();
			this.belongsToType = belongsTo.getObjectType().ordinal();
			this.belongsToID = belongsTo.getObjectUUID();
            this.inBuilding = belongsTo.getInBuilding();
            this.inFloorID = belongsTo.getInFloorID();
            this.inBuildingID = belongsTo.getInBuildingID();
			this.setLoc(belongsTo.getLoc());
		} else {
			Logger.error("No player passed in for corpse");
			this.firstName = "";
			this.lastName = "";
			this.level = 1;
			this.belongsToType = 0;
			this.belongsToID = 0;
		}
		this.setObjectTypeMask(MBServerStatics.MASK_CORPSE);

		if (!safeZone)
			transferInventory(belongsTo,enterWorld);


	}

	public boolean removeItemFromInventory(Item item) {
		synchronized (this.inventory) {
			if (this.inventory.contains(item)) {
				this.inventory.remove(item);
				return true;
			}
			return false;
		}
	}

	public void transferInventory(AbstractCharacter belongsTo,boolean enterWorld) {
		if (belongsTo == null) {
			Logger.error( "Can't find player that corpse " + this.getObjectUUID() + " belongs to");
			return;
		}

		//TODO transfer items from players inventory and trade window to corpse
		CharacterItemManager cim = belongsTo.getCharItemManager();
		if (cim != null)
			cim.transferEntireInventory(this.inventory, this,enterWorld);
		else
			Logger.error( "Can't find inventory for player " + belongsTo.getObjectUUID());
	}

	public static int getNextCorpseCount() {
		return Corpse.corpseCounter.addAndGet(2); //newUUID and runeID
	}

	//Create a new corpse
	public static Corpse makeCorpse(AbstractCharacter belongsTo,boolean enterWorld) {
		boolean safeZone = false;
		if (belongsTo != null && belongsTo.getObjectType() == GameObjectType.PlayerCharacter)
			safeZone = ((PlayerCharacter)belongsTo).isInSafeZone();



		Corpse corpse = new Corpse(Corpse.getNextCorpseCount(), belongsTo, safeZone,enterWorld);

		//create cleanup job
		if (corpse != null) {
			RemoveCorpseJob rcj = new RemoveCorpseJob(corpse);
            corpse.cleanup = JobScheduler.getInstance().scheduleJob(rcj, MBServerStatics.CORPSE_CLEANUP_TIMER_MS);
			DbManager.addToCache(corpse);
		}

		return corpse;
	}

	//Get existing corpse
	public static Corpse getCorpse(int newUUID) {
		return (Corpse) DbManager.getFromCache(GameObjectType.Corpse, newUUID);
	}

	public Item lootItem(Item i, PlayerCharacter looter) {
		//make sure looter exists
		if (looter == null)
			return null;

		//get looters item manager
		CharacterItemManager looterItems = looter.getCharItemManager();
		if (looterItems == null)
			return null;

		synchronized (this.inventory) {

			//make sure player has item in inventory
			if (!this.inventory.contains(i))
				return null;

			//get weight of item
			ItemBase ib = i.getItemBase();
			if (ib == null)
				return null;
			short weight = ib.getWeight();

			//make sure looter has room for item
			if (ib.getType().equals(ItemType.GOLD) == false && !looterItems.hasRoomInventory(weight))
				return null;

			//attempt to transfer item in db
			if (ib.getType().equals(ItemType.GOLD)) {
				if (!looterItems.moveGoldToInventory(i, i.getNumOfItems()))
					return null;
			} else if (!i.moveItemToInventory(looter))
				return null;

			//db transfer successful, remove from this character
			this.inventory.remove(this.inventory.indexOf(i));
		}

		//add item to looter.
		if (!looterItems.addItemToInventory(i))
			return null;

		//calculate new weights
		looterItems.calculateInventoryWeight();

		return i;
	}


	//remove corpse from world
	public static void removeCorpse(int newUUID, boolean fromTimer) {
		Corpse c = (Corpse) DbManager.getFromCache(GameObjectType.Corpse, newUUID);
		if (c == null)
			Logger.error( "No corpse found of ID " + newUUID);
		else
			Corpse.removeCorpse(c, fromTimer);
	}

	public static void removeCorpse(Corpse corpse, boolean fromTimer) {
		if (corpse == null)
			return;

		corpse.purgeInventory();

		//cleanup timer
		if (!fromTimer) {
			JobScheduler.getInstance().cancelScheduledJob(corpse.cleanup);
		}
		corpse.cleanup = null;

		//Remove from world
		UnloadObjectsMsg uom = new UnloadObjectsMsg();
		uom.addObject(corpse);
		DispatchMessage.sendToAllInRange(corpse, uom);
		WorldGrid.RemoveWorldObject(corpse);

		//clear from cache
		DbManager.removeFromCache(corpse);
	}

	
	public static void _serializeForClientMsg(Corpse corpse, ByteBufferWriter writer)
			throws SerializationException {}

	public static void _serializeForClientMsg(Corpse corpse, ByteBufferWriter writer, boolean aln)
			throws SerializationException {

		Building building = null;
		if (corpse.inBuildingID != 0)
			building =  BuildingManager.getBuildingFromCache(corpse.inBuildingID);

		//Send Rune Count
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(1);

		//Send Corpse Rune
		writer.putInt(1);
		writer.putInt(0);
		writer.putInt(MBServerStatics.TOMBSTONE);
		writer.putInt(corpse.getObjectType().ordinal());
		writer.putInt((corpse.getObjectUUID() + 1));

		//Send Stats
		writer.putInt(5);
		writer.putInt(MBServerStatics.STAT_STR_ID); // Strength ID
		writer.putInt(5000);
		writer.putInt(MBServerStatics.STAT_SPI_ID); // Spirit ID
		writer.putInt(0);
		writer.putInt(MBServerStatics.STAT_CON_ID); // Constitution ID
		writer.putInt(0);
		writer.putInt(MBServerStatics.STAT_DEX_ID); // Dexterity ID
		writer.putInt(0);
		writer.putInt(MBServerStatics.STAT_INT_ID); // Intelligence ID
		writer.putInt(0);

		//Send Name
		writer.putString(corpse.firstName);
		if (aln && !corpse.asciiLastName)
			writer.putString("");
		else
			writer.putString(corpse.lastName);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.put((byte)1);

		//Send Corpse Info
		writer.putInt(0);
		writer.putInt(corpse.getObjectType().ordinal());
		writer.putInt((corpse.getObjectUUID()));
		writer.putFloat(10f); //FaceDir or scale
		writer.putFloat(10); //FaceDir or scale
		writer.putFloat(10); //FaceDir or scale

		writer.putFloat(corpse.getLoc().x);
		writer.putFloat(corpse.getLoc().y);
		writer.putFloat(corpse.getLoc().z);

		writer.putFloat(6.235f); //1.548146f); //w
		writer.putInt(0);
		writer.putInt(0);

		//Send BelongsToInfo
		writer.putInt(((corpse.level / 10))); //Rank
		writer.putInt(corpse.level); //Level
		writer.putInt(1);
		writer.putInt(1);
		writer.putInt(1); //Missing this?
		writer.putInt(2);
		writer.putInt(1);
		//		writer.putInt(0); //not needed?
		writer.putInt(0);

		writer.putInt(corpse.belongsToType);
		writer.putInt(corpse.belongsToID);

		writer.putInt(0);
		writer.putInt(0);



		for (int i=0;i<9;i++)
			writer.putInt(0);
		writer.putShort((short)0);
		writer.put((byte)0);

		//Send Errant Guild Info
		for (int i=0;i<13;i++)
			writer.putInt(0);
		writer.putInt(16);
		writer.putInt(16);
		writer.putInt(16);
		writer.putInt(0);
		writer.putInt(0); //Missing this?
		writer.putInt(16);
		writer.putInt(16);
		writer.putInt(16);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);

		//Send unknown counter
		writer.putInt(1);
		writer.putInt(0x047A0E67); //What is this?
		writer.put((byte)0);

		//Send unknown
		writer.putInt(0);
		writer.putInt(0);
		writer.putFloat(1293.4449f); //Unknown
		writer.putFloat(-100f); //Unknown
		writer.putInt(0);
		writer.put((byte)0);

		writer.put((byte)0); //End datablock

	}


	public boolean hasGold() {
		return this.hasGold;
	}

	public void setHasGold(boolean value) {
		this.hasGold = value;
	}

	public ArrayList<Item> getInventory() {
		synchronized(this.inventory) {
			return this.inventory;
		}
	}

	/**
	 * Delete and remove all items in the inventory
	 */
	private void purgeInventory() {
		//make a copy so we're not inside synchronized{} while waiting for all items to be junked
		ArrayList<Item> inventoryCopy;
		synchronized(this.inventory) {
			inventoryCopy = new ArrayList<>(this.inventory);
			this.inventory.clear();
		}

		for (Item item : inventoryCopy) {
			item.junk();
		}
	}

	@Override
	public void updateDatabase() {
	}

	@Override
	public void runAfterLoad() {}

	public int getBelongsToType() {
		return this.belongsToType;
	}

	public int getBelongsToID() {
		return this.belongsToID;
	}

	@Override
	public String getName() {
		if (this.firstName.length() == 0) {
			return "Unknown corpse";
		}
		if (this.lastName.length() == 0) {
			return this.firstName;
		}
		return this.firstName + ' ' + this.lastName;
	}

	public int getInBuilding() {
		return inBuilding;
	}

	public void setInBuilding(int inBuilding) {
		this.inBuilding = inBuilding;
	}

	public int getInFloorID() {
		return inFloorID;
	}

	public void setInFloorID(int inFloorID) {
		this.inFloorID = inFloorID;
	}
}
