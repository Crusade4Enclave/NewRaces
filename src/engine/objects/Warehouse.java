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
import engine.Enum.*;
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.server.MBServerStatics;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Warehouse extends AbstractWorldObject {


	private int UID;

	public EnumBitSet<Enum.ResourceType> lockedResourceTypes;

	private int buildingUID;
	private ArrayList<Transaction> transactions = new ArrayList<>();

	private  ConcurrentHashMap<ItemBase, Integer> resources = new ConcurrentHashMap<>();

	public static ItemBase goldIB = ItemBase.getItemBase(7);
	public static ItemBase stoneIB = ItemBase.getItemBase(1580000);
	public static ItemBase truesteelIB = ItemBase.getItemBase(1580001);
	public static ItemBase ironIB = ItemBase.getItemBase(1580002);
	public static ItemBase adamantIB = ItemBase.getItemBase(1580003);
	public static ItemBase lumberIB = ItemBase.getItemBase(1580004);
	public static ItemBase oakIB = ItemBase.getItemBase(1580005);
	public static ItemBase bronzewoodIB = ItemBase.getItemBase(1580006);
	public static ItemBase mandrakeIB = ItemBase.getItemBase(1580007);
	public static ItemBase coalIB = ItemBase.getItemBase(1580008);
	public static ItemBase agateIB = ItemBase.getItemBase(1580009);
	public static ItemBase diamondIB = ItemBase.getItemBase(1580010);
	public static ItemBase onyxIB = ItemBase.getItemBase(1580011);
	public static ItemBase azothIB = ItemBase.getItemBase(1580012);
	public static ItemBase orichalkIB = ItemBase.getItemBase(1580013);
	public static ItemBase antimonyIB = ItemBase.getItemBase(1580014);
	public static ItemBase sulferIB = ItemBase.getItemBase(1580015);
	public static ItemBase quicksilverIB = ItemBase.getItemBase(1580016);
	public static ItemBase galvorIB = ItemBase.getItemBase(1580017);
	public static ItemBase wormwoodIB = ItemBase.getItemBase(1580018);
	public static ItemBase obsidianIB = ItemBase.getItemBase(1580019);
	public static ItemBase bloodstoneIB = ItemBase.getItemBase(1580020);
	public static ItemBase mithrilIB = ItemBase.getItemBase(1580021);
	public static ConcurrentHashMap<Integer, Integer> maxResources = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer,Warehouse> warehouseByBuildingUUID = new ConcurrentHashMap<>();



	public static ConcurrentHashMap<Integer, Integer> getMaxResources() {
		if(maxResources.size() != 23){
			maxResources.put(7, 100000000);
			maxResources.put(1580000, 10000);
			maxResources.put(1580001, 2000);
			maxResources.put(1580002, 2000);
			maxResources.put(1580003, 1000);
			maxResources.put(1580004, 10000);
			maxResources.put(1580005, 3000);
			maxResources.put(1580006, 3000);
			maxResources.put(1580007, 1000);
			maxResources.put(1580008, 3000);
			maxResources.put(1580009, 2000);
			maxResources.put(1580010, 2000);
			maxResources.put(1580011, 1000);
			maxResources.put(1580012, 2000);
			maxResources.put(1580013, 3000);
			maxResources.put(1580014, 1000);
			maxResources.put(1580015, 1000);
			maxResources.put(1580016, 1000);
			maxResources.put(1580017, 500);
			maxResources.put(1580018, 500);
			maxResources.put(1580019, 500);
			maxResources.put(1580020, 500);
			maxResources.put(1580021, 500);
		}

		return maxResources;
	}

	/**
	 * ResultSet Constructor
	 */
	public Warehouse(ResultSet rs) throws SQLException {
		super(rs);
		this.UID = rs.getInt("UID");
		this.resources.put(stoneIB, rs.getInt("warehouse_stone"));
		this.resources.put(truesteelIB,rs.getInt("warehouse_truesteel"));
		this.resources.put(ironIB,rs.getInt("warehouse_iron"));
		this.resources.put(adamantIB,rs.getInt("warehouse_adamant"));
		this.resources.put(lumberIB,rs.getInt("warehouse_lumber"));
		this.resources.put(oakIB,rs.getInt("warehouse_oak"));
		this.resources.put(bronzewoodIB,rs.getInt("warehouse_bronzewood"));
		this.resources.put(mandrakeIB,rs.getInt("warehouse_mandrake"));
		this.resources.put(coalIB,rs.getInt("warehouse_coal"));
		this.resources.put(agateIB,rs.getInt("warehouse_agate"));
		this.resources.put(diamondIB,rs.getInt("warehouse_diamond"));
		this.resources.put(onyxIB,rs.getInt("warehouse_onyx"));
		this.resources.put(azothIB,rs.getInt("warehouse_azoth"));
		this.resources.put(orichalkIB,rs.getInt("warehouse_orichalk"));
		this.resources.put(antimonyIB,rs.getInt("warehouse_antimony"));
		this.resources.put(sulferIB,rs.getInt("warehouse_sulfur"));
		this.resources.put(quicksilverIB,rs.getInt("warehouse_quicksilver"));
		this.resources.put(galvorIB,rs.getInt("warehouse_galvor"));
		this.resources.put(wormwoodIB,rs.getInt("warehouse_wormwood"));
		this.resources.put(obsidianIB,rs.getInt("warehouse_obsidian"));
		this.resources.put(bloodstoneIB,rs.getInt("warehouse_bloodstone"));
		this.resources.put(mithrilIB,rs.getInt("warehouse_mithril"));
		this.resources.put(goldIB, rs.getInt("warehouse_gold"));
		this.lockedResourceTypes = EnumBitSet.asEnumBitSet(rs.getLong("warehouse_locks"), Enum.ResourceType.class);
		this.buildingUID = rs.getInt("parent");
		Warehouse.warehouseByBuildingUUID.put(this.buildingUID, this);
	}

    public static void warehouseDeposit(MerchantMsg msg, PlayerCharacter player, NPC npc, ClientConnection origin) {

		Building warehouseBuilding;
		Warehouse warehouse;
		int depositAmount;
		Dispatch dispatch;

		Item resource = Item.getFromCache(msg.getItemID());

		if (resource == null)
			return;

		depositAmount = msg.getAmount();
		CharacterItemManager itemMan = player.getCharItemManager();

		if (itemMan.doesCharOwnThisItem(resource.getObjectUUID()) == false)
			return;

		warehouseBuilding = npc.getBuilding();

		if (warehouseBuilding == null)
			return;

		warehouse = warehouseByBuildingUUID.get(warehouseBuilding.getObjectUUID());

		if (warehouse == null)
			return;

		ItemBase ib = resource.getItemBase();

		if (!warehouse.deposit(player, resource, depositAmount, true,true)) {
			//            ChatManager.chatGuildError(player, "Failed to deposit " + ib.getName() +".");
			//            Logger.debug("OpenWindow", player.getName() + " Failed to deposit Item with ID " + resource.getObjectUUID() + " from Warehouse With ID = " + warehouseBuilding.getObjectUUID());
			return;
		}

		ViewResourcesMessage vrm = new ViewResourcesMessage(player);
		vrm.setGuild(player.getGuild());
		vrm.setWarehouseBuilding(warehouseBuilding);
		vrm.configure();
		dispatch = Dispatch.borrow(player, vrm);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	}

    public static void warehouseWithdraw(MerchantMsg msg, PlayerCharacter player, NPC npc, ClientConnection origin) {

		int withdrawAmount;
		Building warehouseBuilding;
		Warehouse warehouse;
		Dispatch dispatch;

		withdrawAmount = msg.getAmount();
		warehouseBuilding = npc.getBuilding();

		if (warehouseBuilding == null)
			return;

		if (player.getGuild() != warehouseBuilding.getGuild() || GuildStatusController.isInnerCouncil(player.getGuildStatus()) == false)
			return;

		warehouse = warehouseByBuildingUUID.get(warehouseBuilding.getObjectUUID());

		if (warehouse == null)
			return;

		int hashID = msg.getHashID();
		int itemBaseID = ItemBase.getItemHashIDMap().get(hashID);
		ItemBase ib = ItemBase.getItemBase(itemBaseID);

		if (ib == null) {
			Logger.debug("Failed to find Resource ItemBaseID with Hash ID = " + hashID);
			return;
		}

		if (warehouse.isResourceLocked(ib) == true) {
			ChatManager.chatSystemInfo(player, "You cannot withdrawl a locked resource.");
			return;
		}
		if (!warehouse.withdraw(player, ib, withdrawAmount, true,true)) {
			ChatManager.chatGuildError(player, "Failed to withdrawl " + ib.getName() + '.');
			Logger.debug(player.getName() + " Failed to withdrawl  =" + ib.getName() + " from Warehouse With ID = " + warehouseBuilding.getObjectUUID());
			return;
		}

		ViewResourcesMessage vrm = new ViewResourcesMessage(player);
		vrm.setGuild(player.getGuild());
		vrm.setWarehouseBuilding(warehouseBuilding);
		vrm.configure();
		dispatch = Dispatch.borrow(player, vrm);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	}

    public static void warehouseLock(MerchantMsg msg, PlayerCharacter player, NPC npc, ClientConnection origin) {
		Building warehouse;
		int hashID;
		Dispatch dispatch;

		hashID = msg.getHashID();
		warehouse = npc.getBuilding();

		if (warehouse == null)
			return;

		if (player.getGuild() != warehouse.getGuild() || GuildStatusController.isInnerCouncil(player.getGuildStatus()) == false)
			return;

		Warehouse wh = warehouseByBuildingUUID.get(warehouse.getObjectUUID());

		if (wh == null)
			return;

		int itemBaseID = ItemBase.getItemHashIDMap().get(hashID);
		ItemBase ib = ItemBase.getItemBase(itemBaseID);

		if (ib == null)
			return;

		if (wh.isResourceLocked(ib) == true) {
			boolean worked = false;
			EnumBitSet<ResourceType> bitSet = EnumBitSet.asEnumBitSet(wh.lockedResourceTypes.toLong(), ResourceType.class);
			
			bitSet.remove(ResourceType.resourceLookup.get(itemBaseID));
			
			worked = DbManager.WarehouseQueries.updateLocks(wh, bitSet.toLong());
						
			if (worked) {
				wh.lockedResourceTypes.remove(Enum.ResourceType.resourceLookup.get(itemBaseID));
				ViewResourcesMessage vrm = new ViewResourcesMessage(player);
				vrm.setGuild(player.getGuild());
				vrm.setWarehouseBuilding(warehouse);
				vrm.configure();
				dispatch = Dispatch.borrow(player, vrm);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			}
			return;
		}

		EnumBitSet<ResourceType> bitSet = EnumBitSet.asEnumBitSet(wh.lockedResourceTypes.toLong(), ResourceType.class);
		
		bitSet.add(ResourceType.resourceLookup.get(itemBaseID));
		
		if (DbManager.WarehouseQueries.updateLocks(wh,bitSet.toLong()) == false)
			return;

		wh.lockedResourceTypes.add(Enum.ResourceType.resourceLookup.get(itemBaseID));
		ViewResourcesMessage vrm = new ViewResourcesMessage(player);
		vrm.setGuild(player.getGuild());
		vrm.setWarehouseBuilding(warehouse);
		vrm.configure();
		dispatch = Dispatch.borrow(player, vrm);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

	}

    public  ConcurrentHashMap<ItemBase, Integer> getResources() {
		return resources;
	}

	public int getUID() {
		return UID;
	}

	public void setUID(int uID) {
		UID = uID;
	}

	public synchronized boolean deposit(PlayerCharacter pc,Item resource, int amount,boolean removeFromInventory, boolean transaction){

		ClientConnection origin = pc.getClientConnection();
		if (origin == null)
			return false;

		if (amount < 0){
			Logger.info(pc.getFirstName() + " Attempting to Dupe!!!!!!");
			return false;
		}

		ItemBase ib = resource.getItemBase();

		if (ib == null)
			return false;

		if (this.resources.get(ib) == null)
			return false;

		CharacterItemManager itemMan = pc.getCharItemManager();

		if (itemMan == null)
			return false;
		
	
			if (itemMan.getGoldTrading() > 0){
				ErrorPopupMsg.sendErrorPopup(pc, 195);
				return false;
			}
		

		if (!itemMan.doesCharOwnThisItem(resource.getObjectUUID()))
			return false;

		if (!resource.validForInventory(origin, pc, itemMan))
			return false;

		if (resource.getNumOfItems() < amount)
			return false;

		int oldAmount = resources.get(ib);

		int newAmount = oldAmount + amount;

		if (newAmount > Warehouse.getMaxResources().get(ib.getUUID())){
			//ChatManager.chatSystemInfo(pc, "The Warehouse is at it's maximum for this type of resource.");
			return false;
		}


		if (removeFromInventory){
			if (ib.getUUID() == 7){

				if (itemMan.getGoldInventory().getNumOfItems() -amount < 0)
					return false;

				if (itemMan.getGoldInventory().getNumOfItems() - amount > MBServerStatics.PLAYER_GOLD_LIMIT)
					return false;

				if (!itemMan.modifyInventoryGold(-amount)){
					//ChatManager.chatSystemError(pc, "You do not have this Gold.");
					return false;
				}

				UpdateGoldMsg ugm = new UpdateGoldMsg(pc);
				ugm.configure();
				Dispatch dispatch = Dispatch.borrow(pc, ugm);
				DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

				itemMan.updateInventory();

			}else{
				itemMan.delete(resource);
				itemMan.updateInventory();
			}
		}
		itemMan.updateInventory();
		int itemID = ib.getUUID();
		boolean worked = false;
		switch(itemID){
		case 7:
			worked = DbManager.WarehouseQueries.updateGold(this, newAmount);
			break;
		case 1580000:
			worked = DbManager.WarehouseQueries.updateStone(this, newAmount);
			break;
		case 1580001:
			worked = DbManager.WarehouseQueries.updateTruesteel(this, newAmount);
			break;
		case 1580002:
			worked = DbManager.WarehouseQueries.updateIron(this, newAmount);
			break;
		case 1580003:
			worked = DbManager.WarehouseQueries.updateAdamant(this, newAmount);
			break;
		case 1580004:
			worked = DbManager.WarehouseQueries.updateLumber(this, newAmount);
			break;
		case 1580005:
			worked = DbManager.WarehouseQueries.updateOak(this, newAmount);
			break;
		case 1580006:
			worked = DbManager.WarehouseQueries.updateBronzewood(this, newAmount);
			break;
		case 1580007:
			worked = DbManager.WarehouseQueries.updateMandrake(this, newAmount);
			break;
		case 1580008:
			worked = DbManager.WarehouseQueries.updateCoal(this, newAmount);
			break;
		case 1580009:
			worked = DbManager.WarehouseQueries.updateAgate(this, newAmount);
			break;
		case 1580010:
			worked = DbManager.WarehouseQueries.updateDiamond(this, newAmount);
			break;
		case 1580011:
			worked = DbManager.WarehouseQueries.updateOnyx(this, newAmount);
			break;
		case 1580012:
			worked = DbManager.WarehouseQueries.updateAzoth(this, newAmount);
			break;
		case 1580013:
			worked = DbManager.WarehouseQueries.updateOrichalk(this, newAmount);
			break;
		case 1580014:
			worked = DbManager.WarehouseQueries.updateAntimony(this, newAmount);
			break;
		case 1580015:
			worked = DbManager.WarehouseQueries.updateSulfur(this, newAmount);
			break;
		case 1580016:
			worked = DbManager.WarehouseQueries.updateQuicksilver(this, newAmount);
			break;
		case 1580017:
			worked = DbManager.WarehouseQueries.updateGalvor(this, newAmount);
			break;
		case 1580018:
			worked = DbManager.WarehouseQueries.updateWormwood(this, newAmount);
			break;
		case 1580019:
			worked = DbManager.WarehouseQueries.updateObsidian(this, newAmount);
			break;
		case 1580020:
			worked = DbManager.WarehouseQueries.updateBloodstone(this, newAmount);
			break;
		case 1580021:
			worked = DbManager.WarehouseQueries.updateMithril(this, newAmount);
			break;
		}

		if (!worked)
			return false;

        resources.put(ib,newAmount);

		Resource resourceType;

		if (resource.getItemBase().getType().equals(engine.Enum.ItemType.GOLD))
			resourceType = Resource.GOLD;
		else
			resourceType = Resource.valueOf(resource.getItemBase().getName().toUpperCase());

		if (transaction)
			this.AddTransactionToWarehouse(pc.getObjectType(), pc.getObjectUUID(), TransactionType.DEPOSIT,resourceType, amount);

		return true;
	}

	//for mine deposit
	public synchronized boolean depositFromMine(Mine mine,ItemBase resource, int amount){

        if (resource == null)
			return false;

		if (this.resources.get(resource) == null)
			return false;

		int oldAmount = resources.get(resource);
		int newAmount = oldAmount + amount;

		if (newAmount > Warehouse.getMaxResources().get(resource.getUUID()))
			return false;

		int itemID = resource.getUUID();
		boolean worked = false;

		switch(itemID){
		case 7:
			worked = DbManager.WarehouseQueries.updateGold(this, newAmount);
			break;
		case 1580000:
			worked = DbManager.WarehouseQueries.updateStone(this, newAmount);
			break;
		case 1580001:
			worked = DbManager.WarehouseQueries.updateTruesteel(this, newAmount);
			break;
		case 1580002:
			worked = DbManager.WarehouseQueries.updateIron(this, newAmount);
			break;
		case 1580003:
			worked = DbManager.WarehouseQueries.updateAdamant(this, newAmount);
			break;
		case 1580004:
			worked = DbManager.WarehouseQueries.updateLumber(this, newAmount);
			break;
		case 1580005:
			worked = DbManager.WarehouseQueries.updateOak(this, newAmount);
			break;
		case 1580006:
			worked = DbManager.WarehouseQueries.updateBronzewood(this, newAmount);
			break;
		case 1580007:
			worked = DbManager.WarehouseQueries.updateMandrake(this, newAmount);
			break;
		case 1580008:
			worked = DbManager.WarehouseQueries.updateCoal(this, newAmount);
			break;
		case 1580009:
			worked = DbManager.WarehouseQueries.updateAgate(this, newAmount);
			break;
		case 1580010:
			worked = DbManager.WarehouseQueries.updateDiamond(this, newAmount);
			break;
		case 1580011:
			worked = DbManager.WarehouseQueries.updateOnyx(this, newAmount);
			break;
		case 1580012:
			worked = DbManager.WarehouseQueries.updateAzoth(this, newAmount);
			break;
		case 1580013:
			worked = DbManager.WarehouseQueries.updateOrichalk(this, newAmount);
			break;
		case 1580014:
			worked = DbManager.WarehouseQueries.updateAntimony(this, newAmount);
			break;
		case 1580015:
			worked = DbManager.WarehouseQueries.updateSulfur(this, newAmount);
			break;
		case 1580016:
			worked = DbManager.WarehouseQueries.updateQuicksilver(this, newAmount);
			break;
		case 1580017:
			worked = DbManager.WarehouseQueries.updateGalvor(this, newAmount);
			break;
		case 1580018:
			worked = DbManager.WarehouseQueries.updateWormwood(this, newAmount);
			break;
		case 1580019:
			worked = DbManager.WarehouseQueries.updateObsidian(this, newAmount);
			break;
		case 1580020:
			worked = DbManager.WarehouseQueries.updateBloodstone(this, newAmount);
			break;
		case 1580021:
			worked = DbManager.WarehouseQueries.updateMithril(this, newAmount);
			break;
		}
		if (!worked)
			return false;

		this.resources.put(resource, newAmount);
		Resource resourceType;

		if (resource.getUUID() == 7)
			resourceType = Resource.GOLD;
		else
			resourceType = Resource.valueOf(resource.getName().toUpperCase());

		if (mine != null)
			this.AddTransactionToWarehouse(GameObjectType.Building, mine.getBuildingID(), TransactionType.MINE, resourceType, amount);

		return true;
	}

	public synchronized boolean depositRealmTaxes(PlayerCharacter taxer, ItemBase ib,int amount){

		if (ib == null)
			return false;

		if (this.resources.get(ib) == null)
			return false;

		int oldAmount = resources.get(ib);
		int newAmount = oldAmount + amount;

		if (newAmount > Warehouse.getMaxResources().get(ib.getUUID()))
			return false;

		int itemID = ib.getUUID();
		boolean worked = false;

		switch(itemID){
		case 7:
			worked = DbManager.WarehouseQueries.updateGold(this, newAmount);
			break;
		case 1580000:
			worked = DbManager.WarehouseQueries.updateStone(this, newAmount);
			break;
		case 1580001:
			worked = DbManager.WarehouseQueries.updateTruesteel(this, newAmount);
			break;
		case 1580002:
			worked = DbManager.WarehouseQueries.updateIron(this, newAmount);
			break;
		case 1580003:
			worked = DbManager.WarehouseQueries.updateAdamant(this, newAmount);
			break;
		case 1580004:
			worked = DbManager.WarehouseQueries.updateLumber(this, newAmount);
			break;
		case 1580005:
			worked = DbManager.WarehouseQueries.updateOak(this, newAmount);
			break;
		case 1580006:
			worked = DbManager.WarehouseQueries.updateBronzewood(this, newAmount);
			break;
		case 1580007:
			worked = DbManager.WarehouseQueries.updateMandrake(this, newAmount);
			break;
		case 1580008:
			worked = DbManager.WarehouseQueries.updateCoal(this, newAmount);
			break;
		case 1580009:
			worked = DbManager.WarehouseQueries.updateAgate(this, newAmount);
			break;
		case 1580010:
			worked = DbManager.WarehouseQueries.updateDiamond(this, newAmount);
			break;
		case 1580011:
			worked = DbManager.WarehouseQueries.updateOnyx(this, newAmount);
			break;
		case 1580012:
			worked = DbManager.WarehouseQueries.updateAzoth(this, newAmount);
			break;
		case 1580013:
			worked = DbManager.WarehouseQueries.updateOrichalk(this, newAmount);
			break;
		case 1580014:
			worked = DbManager.WarehouseQueries.updateAntimony(this, newAmount);
			break;
		case 1580015:
			worked = DbManager.WarehouseQueries.updateSulfur(this, newAmount);
			break;
		case 1580016:
			worked = DbManager.WarehouseQueries.updateQuicksilver(this, newAmount);
			break;
		case 1580017:
			worked = DbManager.WarehouseQueries.updateGalvor(this, newAmount);
			break;
		case 1580018:
			worked = DbManager.WarehouseQueries.updateWormwood(this, newAmount);
			break;
		case 1580019:
			worked = DbManager.WarehouseQueries.updateObsidian(this, newAmount);
			break;
		case 1580020:
			worked = DbManager.WarehouseQueries.updateBloodstone(this, newAmount);
			break;
		case 1580021:
			worked = DbManager.WarehouseQueries.updateMithril(this, newAmount);
			break;
		}

		if (!worked)
			return false;

		this.resources.put(ib, newAmount);
		Resource resourceType;

		if (ib.getUUID() == 7)
			resourceType = Resource.GOLD;
		else
			resourceType = Resource.valueOf(ib.getName().toUpperCase());
		
		this.AddTransactionToWarehouse(taxer.getObjectType(), taxer.getObjectUUID(), TransactionType.TAXRESOURCEDEPOSIT, resourceType, amount);

		return true;
	}

	public synchronized boolean depositProfitTax(ItemBase ib,int amount,Building building){

		if (ib == null)
			return false;

		if (this.resources.get(ib) == null)
			return false;

		int oldAmount = resources.get(ib);
		int newAmount = oldAmount + amount;

		if (newAmount > Warehouse.getMaxResources().get(ib.getUUID()))
			return false;

		int itemID = ib.getUUID();
		boolean worked = false;

		switch(itemID){
		case 7:
			worked = DbManager.WarehouseQueries.updateGold(this, newAmount);
			break;
		case 1580000:
			worked = DbManager.WarehouseQueries.updateStone(this, newAmount);
			break;
		case 1580001:
			worked = DbManager.WarehouseQueries.updateTruesteel(this, newAmount);
			break;
		case 1580002:
			worked = DbManager.WarehouseQueries.updateIron(this, newAmount);
			break;
		case 1580003:
			worked = DbManager.WarehouseQueries.updateAdamant(this, newAmount);
			break;
		case 1580004:
			worked = DbManager.WarehouseQueries.updateLumber(this, newAmount);
			break;
		case 1580005:
			worked = DbManager.WarehouseQueries.updateOak(this, newAmount);
			break;
		case 1580006:
			worked = DbManager.WarehouseQueries.updateBronzewood(this, newAmount);
			break;
		case 1580007:
			worked = DbManager.WarehouseQueries.updateMandrake(this, newAmount);
			break;
		case 1580008:
			worked = DbManager.WarehouseQueries.updateCoal(this, newAmount);
			break;
		case 1580009:
			worked = DbManager.WarehouseQueries.updateAgate(this, newAmount);
			break;
		case 1580010:
			worked = DbManager.WarehouseQueries.updateDiamond(this, newAmount);
			break;
		case 1580011:
			worked = DbManager.WarehouseQueries.updateOnyx(this, newAmount);
			break;
		case 1580012:
			worked = DbManager.WarehouseQueries.updateAzoth(this, newAmount);
			break;
		case 1580013:
			worked = DbManager.WarehouseQueries.updateOrichalk(this, newAmount);
			break;
		case 1580014:
			worked = DbManager.WarehouseQueries.updateAntimony(this, newAmount);
			break;
		case 1580015:
			worked = DbManager.WarehouseQueries.updateSulfur(this, newAmount);
			break;
		case 1580016:
			worked = DbManager.WarehouseQueries.updateQuicksilver(this, newAmount);
			break;
		case 1580017:
			worked = DbManager.WarehouseQueries.updateGalvor(this, newAmount);
			break;
		case 1580018:
			worked = DbManager.WarehouseQueries.updateWormwood(this, newAmount);
			break;
		case 1580019:
			worked = DbManager.WarehouseQueries.updateObsidian(this, newAmount);
			break;
		case 1580020:
			worked = DbManager.WarehouseQueries.updateBloodstone(this, newAmount);
			break;
		case 1580021:
			worked = DbManager.WarehouseQueries.updateMithril(this, newAmount);
			break;
		}

		if (!worked)
			return false;

		this.resources.put(ib, newAmount);
		Resource resourceType;

		if (ib.getUUID() == 7)
			resourceType = Resource.GOLD;
		else
			resourceType = Resource.valueOf(ib.getName().toUpperCase());

		if (building != null)
			this.AddTransactionToWarehouse(GameObjectType.Building, building.getObjectUUID(), TransactionType.DEPOSIT, resourceType, amount);

		return true;
	}
	public synchronized boolean withdraw(PlayerCharacter pc, ItemBase ib, int amount, boolean addToInventory, boolean transaction){

	    if (pc == null)
			return false;

		if (ib == null)
			return false;

		if (this.resources.get(ib) == null)
			return false;

		if (amount <= 0)
			return false;

		CharacterItemManager itemMan = pc.getCharItemManager();

		if(itemMan == null)
			return false;

		if (addToInventory)
			if(!itemMan.hasRoomInventory(ib.getWeight())) {
				ChatManager.chatSystemInfo(pc, "You can not carry any more of that item.");
				return false;
			}

		if (addToInventory && ib.getUUID() == ItemBase.GOLD_BASE_ID){
			if (pc.getCharItemManager().getGoldInventory().getNumOfItems() + amount > MBServerStatics.PLAYER_GOLD_LIMIT){
				return false;
			}

			if (pc.getCharItemManager().getGoldInventory().getNumOfItems() + amount  < 0)
				return false;
		}
		int oldAmount = this.resources.get(ib);

		if (oldAmount < amount)
			return false;

		int hashID = ib.getHashID();
		int newAmount = oldAmount - amount;

		boolean worked = false;

		switch(hashID){
		case 2308551:
			worked = DbManager.WarehouseQueries.updateGold(this,newAmount);
			break;
		case 74856115:
			worked = DbManager.WarehouseQueries.updateStone(this, newAmount);
			break;
		case -317484979:
			worked = DbManager.WarehouseQueries.updateTruesteel(this, newAmount);
			break;
		case 2504297:
			worked = DbManager.WarehouseQueries.updateIron(this, newAmount);
			break;
		case -1741189964:
			worked = DbManager.WarehouseQueries.updateAdamant(this, newAmount);
			break;
		case -1603256692:
			worked = DbManager.WarehouseQueries.updateLumber(this, newAmount);
			break;
		case 74767:
			worked = DbManager.WarehouseQueries.updateOak(this, newAmount);
			break;
		case 1334770447:
			worked = DbManager.WarehouseQueries.updateBronzewood(this, newAmount);
			break;
		case 1191391799:
			worked = 	DbManager.WarehouseQueries.updateMandrake(this, newAmount);
			break;
		case 2559427:
			worked = DbManager.WarehouseQueries.updateCoal(this, newAmount);
			break;
		case 75173057:
			worked = DbManager.WarehouseQueries.updateAgate(this, newAmount);
			break;
		case -1730704107:
			worked = DbManager.WarehouseQueries.updateDiamond(this, newAmount);
			break;
		case 2977263:
			worked = DbManager.WarehouseQueries.updateOnyx(this, newAmount);
			break;
		case 78329697:
			worked = DbManager.WarehouseQueries.updateAzoth(this, newAmount);
			break;
		case -2036290524:
			worked = DbManager.WarehouseQueries.updateOrichalk(this, newAmount);
			break;
		case 452320058:
			worked = DbManager.WarehouseQueries.updateAntimony(this, newAmount);
			break;
		case -1586349421:
			worked = DbManager.WarehouseQueries.updateSulfur(this, newAmount);
			break;
		case -472884509:
			worked = DbManager.WarehouseQueries.updateQuicksilver(this, newAmount);
			break;
		case -1596311545:
			worked = DbManager.WarehouseQueries.updateGalvor(this, newAmount);
			break;
		case 1532478436:
			worked = DbManager.WarehouseQueries.updateWormwood(this, newAmount);
			break;
		case -697973233:
			worked = DbManager.WarehouseQueries.updateObsidian(this, newAmount);
			break;
		case -1569826353:
			worked = DbManager.WarehouseQueries.updateBloodstone(this, newAmount);
			break;
		case -1761257186:
			worked = DbManager.WarehouseQueries.updateMithril(this, newAmount);
			break;
		}
		if (!worked)
			return false;

		this.resources.put(ib, newAmount);

		if (addToInventory){
			if (ib.getUUID() == 7){

				itemMan.addGoldToInventory(amount, false);
				UpdateGoldMsg ugm = new UpdateGoldMsg(pc);
				ugm.configure();
				Dispatch dispatch = Dispatch.borrow(pc, ugm);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

				itemMan.updateInventory();
			}else{
				boolean itemWorked = false;
				Item item = new Item( ib, pc.getObjectUUID(), OwnerType.PlayerCharacter, (byte) 0, (byte) 0,
						(short) 1, (short) 1, true, false,ItemContainerType.INVENTORY, (byte) 0,
						new ArrayList<>(),"");
				item.setNumOfItems(amount);
				item.containerType = Enum.ItemContainerType.INVENTORY;

				try {
					item = DbManager.ItemQueries.ADD_ITEM(item);
					itemWorked = true;
				} catch (Exception e) {
					Logger.error(e);
				}
				if (itemWorked) {
					itemMan.addItemToInventory(item);
					itemMan.updateInventory();
				}
			}
		}
		Resource resourceType;

		if (ib.getUUID() == 7)
			resourceType = Resource.GOLD;
		else
			resourceType = Resource.valueOf(ib.getName().toUpperCase());

		if (transaction)
			this.AddTransactionToWarehouse(pc.getObjectType(), pc.getObjectUUID(), TransactionType.WITHDRAWL, resourceType, amount);

		return true;
	}

	public synchronized boolean withdraw(NPC npc, ItemBase ib, int amount, boolean addToInventory, boolean transaction){

	    if (npc == null)
			return false;

		if (ib == null)
			return false;

		if (this.resources.get(ib) == null)
			return false;

		if (amount <= 0)
			return false;

		int oldAmount = this.resources.get(ib);

		if (oldAmount < amount)
			return false;

		int hashID = ib.getHashID();
		int newAmount = oldAmount - amount;
		boolean worked = false;

		switch(hashID){
		case 2308551:
			worked = DbManager.WarehouseQueries.updateGold(this,newAmount);
			break;
		case 74856115:
			worked = DbManager.WarehouseQueries.updateStone(this, newAmount);
			break;
		case -317484979:
			worked = DbManager.WarehouseQueries.updateTruesteel(this, newAmount);
			break;
		case 2504297:
			worked = DbManager.WarehouseQueries.updateIron(this, newAmount);
			break;
		case -1741189964:
			worked = DbManager.WarehouseQueries.updateAdamant(this, newAmount);
			break;
		case -1603256692:
			worked = DbManager.WarehouseQueries.updateLumber(this, newAmount);
			break;
		case 74767:
			worked = DbManager.WarehouseQueries.updateOak(this, newAmount);
			break;
		case 1334770447:
			worked = DbManager.WarehouseQueries.updateBronzewood(this, newAmount);
			break;
		case 1191391799:
			worked = 	DbManager.WarehouseQueries.updateMandrake(this, newAmount);
			break;
		case 2559427:
			worked = DbManager.WarehouseQueries.updateCoal(this, newAmount);
			break;
		case 75173057:
			worked = DbManager.WarehouseQueries.updateAgate(this, newAmount);
			break;
		case -1730704107:
			worked = DbManager.WarehouseQueries.updateDiamond(this, newAmount);
			break;
		case 2977263:
			worked = DbManager.WarehouseQueries.updateOnyx(this, newAmount);
			break;
		case 78329697:
			worked = DbManager.WarehouseQueries.updateAzoth(this, newAmount);
			break;
		case -2036290524:
			worked = DbManager.WarehouseQueries.updateOrichalk(this, newAmount);
			break;
		case 452320058:
			worked = DbManager.WarehouseQueries.updateAntimony(this, newAmount);
			break;
		case -1586349421:
			worked = DbManager.WarehouseQueries.updateSulfur(this, newAmount);
			break;
		case -472884509:
			worked = DbManager.WarehouseQueries.updateQuicksilver(this, newAmount);
			break;
		case -1596311545:
			worked = DbManager.WarehouseQueries.updateGalvor(this, newAmount);
			break;
		case 1532478436:
			worked = DbManager.WarehouseQueries.updateWormwood(this, newAmount);
			break;
		case -697973233:
			worked = DbManager.WarehouseQueries.updateObsidian(this, newAmount);
			break;
		case -1569826353:
			worked = DbManager.WarehouseQueries.updateBloodstone(this, newAmount);
			break;
		case -1761257186:
			worked = DbManager.WarehouseQueries.updateMithril(this, newAmount);
			break;
		}

		if (!worked)
			return false;

		this.resources.put(ib, newAmount);
		Resource resourceType;

		if (ib.getUUID() == 7)
			resourceType = Resource.GOLD;
		else
			resourceType = Resource.valueOf(ib.getName().toUpperCase());

		if (transaction)
			this.AddTransactionToWarehouse(npc.getObjectType(), npc.getObjectUUID(), TransactionType.WITHDRAWL, resourceType, amount);

		return true;
	}

	public synchronized boolean transferResources(PlayerCharacter taxer, TaxResourcesMsg msg, ArrayList<Integer> realmResources, float taxPercent, Warehouse toWarehouse){

		for (int ibID: realmResources){

			ItemBase ib = ItemBase.getItemBase(ibID);

			if (ib == null)
				return false;

			if (this.resources.get(ib) == null)
				return false;

			int amount = (int) (this.resources.get(ib) * taxPercent);

			if (amount <= 0){
				msg.getResources().put(ib.getHashID(), 0);
				continue;
			}

			int oldAmount = this.resources.get(ib);

			if (oldAmount < amount)
				amount = oldAmount;

			int hashID = ib.getHashID();
			int newAmount = oldAmount - amount;

			if (newAmount < amount)
				continue;

			boolean worked = false;

			switch(hashID){
			case 2308551:
				worked = DbManager.WarehouseQueries.updateGold(this,newAmount);
				break;
			case 74856115:
				worked = DbManager.WarehouseQueries.updateStone(this, newAmount);
				break;
			case -317484979:
				worked = DbManager.WarehouseQueries.updateTruesteel(this, newAmount);
				break;
			case 2504297:
				worked = DbManager.WarehouseQueries.updateIron(this, newAmount);
				break;
			case -1741189964:
				worked = DbManager.WarehouseQueries.updateAdamant(this, newAmount);
				break;
			case -1603256692:
				worked = DbManager.WarehouseQueries.updateLumber(this, newAmount);
				break;
			case 74767:
				worked = DbManager.WarehouseQueries.updateOak(this, newAmount);
				break;
			case 1334770447:
				worked = DbManager.WarehouseQueries.updateBronzewood(this, newAmount);
				break;
			case 1191391799:
				worked = 	DbManager.WarehouseQueries.updateMandrake(this, newAmount);
				break;
			case 2559427:
				worked = DbManager.WarehouseQueries.updateCoal(this, newAmount);
				break;
			case 75173057:
				worked = DbManager.WarehouseQueries.updateAgate(this, newAmount);
				break;
			case -1730704107:
				worked = DbManager.WarehouseQueries.updateDiamond(this, newAmount);
				break;
			case 2977263:
				worked = DbManager.WarehouseQueries.updateOnyx(this, newAmount);
				break;
			case 78329697:
				worked = DbManager.WarehouseQueries.updateAzoth(this, newAmount);
				break;
			case -2036290524:
				worked = DbManager.WarehouseQueries.updateOrichalk(this, newAmount);
				break;
			case 452320058:
				worked = DbManager.WarehouseQueries.updateAntimony(this, newAmount);
				break;
			case -1586349421:
				worked = DbManager.WarehouseQueries.updateSulfur(this, newAmount);
				break;
			case -472884509:
				worked = DbManager.WarehouseQueries.updateQuicksilver(this, newAmount);
				break;
			case -1596311545:
				worked = DbManager.WarehouseQueries.updateGalvor(this, newAmount);
				break;
			case 1532478436:
				worked = DbManager.WarehouseQueries.updateWormwood(this, newAmount);
				break;
			case -697973233:
				worked = DbManager.WarehouseQueries.updateObsidian(this, newAmount);
				break;
			case -1569826353:
				worked = DbManager.WarehouseQueries.updateBloodstone(this, newAmount);
				break;
			case -1761257186:
				worked = DbManager.WarehouseQueries.updateMithril(this, newAmount);
				break;
			}

			if (!worked){
				msg.getResources().put(ib.getHashID(), 0);
				continue;
			}
			
			msg.getResources().put(ib.getHashID(), amount);
				
			this.resources.put(ib, newAmount);
			toWarehouse.depositRealmTaxes(taxer,ib, amount);
			Resource resourceType;

			if (ib.getUUID() == 7)
				resourceType = Resource.GOLD;
			else
				resourceType = Resource.valueOf(ib.getName().toUpperCase());
			
			this.AddTransactionToWarehouse(taxer.getObjectType(), taxer.getObjectUUID(), TransactionType.TAXRESOURCE, resourceType, amount);

		}
		return true;
	}

	public synchronized boolean loot(PlayerCharacter pc, ItemBase ib, int amount, boolean addToInventory){

	    if (pc == null)
			return false;

		if (ib == null)
			return false;

		if (this.resources.get(ib) == null)
			return false;

		if (amount <= 0)
			return false;

		CharacterItemManager itemMan = pc.getCharItemManager();

		if(itemMan == null)
			return false;

		if(!itemMan.hasRoomInventory(ib.getWeight())) {
			ChatManager.chatSystemInfo(pc, "You can not carry any more of that item.");
			return false;
		}

		int oldAmount = this.resources.get(ib);

		if (oldAmount < amount)
			return false;

		int newAmount = oldAmount - amount;

		this.resources.put(ib, newAmount);

		if (addToInventory){
			if (ib.getUUID() == 7){

				itemMan.addGoldToInventory(amount, false);
				UpdateGoldMsg ugm = new UpdateGoldMsg(pc);
				ugm.configure();
				Dispatch dispatch = Dispatch.borrow(pc, ugm);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

				itemMan.updateInventory();
			}else{
				boolean itemWorked = false;
				Item item = new Item(ib, pc.getObjectUUID(), OwnerType.PlayerCharacter, (byte) 0, (byte) 0,
						(short) 1, (short) 1, true, false,ItemContainerType.INVENTORY, (byte) 0,
                        new ArrayList<>(),"");
				item.setNumOfItems(amount);
				item.containerType = Enum.ItemContainerType.INVENTORY;

				try {
					item = DbManager.ItemQueries.ADD_ITEM(item);
					itemWorked = true;
				} catch (Exception e) {
					Logger.error(e);
				}
				if (itemWorked) {
					itemMan.addItemToInventory(item);
					itemMan.updateInventory();
				}
			}
		}

		return true;
	}

	@Override
	public void updateDatabase() {
		// TODO Auto-generated method stub

	}
	@Override
	public void runAfterLoad() {

		try{
			Building warehouseBuilding = BuildingManager.getBuilding(this.buildingUID);
            Logger.info("configuring warehouse " + UID + " for city "  + warehouseBuilding.getCity().getCityName()  + " structure UUID " + this.buildingUID);

			//Building is gone, but Warehouse still in DB?? Should never happen, sanity check anyway.
			if (warehouseBuilding == null){
				Logger.error( "Failed to load Building for Warehouse");
				return;
			}

			Zone cityZone = warehouseBuilding.getParentZone();

			if (cityZone == null){
				Logger.error( "Failed to load Zone for Warehouse with UUID " + this.getObjectUUID());
				return;
			}

			City city = City.getCity(cityZone.getPlayerCityUUID());

			if (city == null){
				Logger.error( "Failed to load City for Warehouse with UUID " + this.getObjectUUID());
				return;
			}

			warehouseByBuildingUUID.put(this.buildingUID, this);
			city.setWarehouseBuildingID(this.buildingUID);
		}catch(Exception E){
			Logger.info(this.getObjectUUID() + " failed");

		}
	}

	public boolean isEmpty(){
		int amount = 0;
		for(ItemBase ib: ItemBase.getResourceList()){
			if (amount > 0)
				return false;
            amount += resources.get(ib);
		}
		return true;
	}

	public int getBuildingUID() {
		return buildingUID;
	}

	public void loadAllTransactions(){
		this.transactions = DbManager.WarehouseQueries.GET_TRANSACTIONS_FOR_WAREHOUSE(this.buildingUID);
	}

	public  boolean AddTransactionToWarehouse(GameObjectType targetType, int targetUUID, TransactionType transactionType,Resource resource, int amount){
		
		
		if (!DbManager.WarehouseQueries.CREATE_TRANSACTION(this.buildingUID, targetType, targetUUID, transactionType, resource, amount, DateTime.now()))
			return false;
		
		Transaction transaction = new Transaction(this.buildingUID,targetType,targetUUID,transactionType,resource,amount, DateTime.now());
		this.transactions.add(transaction);
		return true;
	}

	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}

	public boolean isAboveCap(ItemBase ib, int deposit){
		int newAmount = this.resources.get(ib) + deposit;
        return newAmount > Warehouse.getMaxResources().get(ib.getUUID());

    }

    public boolean isResourceLocked(ItemBase itemBase) {

        Enum.ResourceType resourceType;

        resourceType = Enum.ResourceType.resourceLookup.get(itemBase.getUUID());

        return resourceType.elementOf(this.lockedResourceTypes);
    }
}
