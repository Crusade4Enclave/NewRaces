// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.Enum.ItemType;
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.gameManager.ConfigManager;
import engine.gameManager.DbManager;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.ClientMessagePump;
import engine.net.client.msg.*;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static engine.math.FastMath.sqr;
import static engine.net.client.msg.ErrorPopupMsg.sendErrorPopup;



public class CharacterItemManager {

	private final AbstractCharacter absCharacter;
	private Account account;

	// Mapping of all the items associated with this Manager
	private final ConcurrentHashMap<Integer, Integer> itemIDtoType = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

	// Mapping of all items equipped in this Manager
	// Key = Item Slot
	private final ConcurrentHashMap<Integer, Item> equipped = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

	private final HashSet<Item> inventory = new HashSet<>();
	private final HashSet<Item> bank = new HashSet<>();
	private final HashSet<Item> vault = new HashSet<>();

	private Item goldInventory;
	private Item goldBank;
	public Item goldVault;

	private boolean bankOpened;
	private boolean vaultOpened;

	private short bankWeight;
	private short inventoryWeight;
	private short equipWeight;
	private short vaultWeight;

	private ClientConnection tradingWith;
	private byte tradeCommitted;
	private boolean tradeSuccess;
	private HashSet<Integer> trading;
	private int goldTradingAmount;
	private int tradeID = 0;
	private final HashSet<Integer> equipOrder = new HashSet<>();

	/*
	 * Item Manager Version data
	 */
	private byte equipVer = (byte) 0;
	private static final byte inventoryVer = (byte) 0;
	private static final byte bankVer = (byte) 0;
	private static final byte vaultVer = (byte) 0;

	public CharacterItemManager(AbstractCharacter ac) {
		super();
		this.absCharacter = ac;
	}

	public void load() {
		loadForGeneric();

		if (this.absCharacter .getObjectType().equals(GameObjectType.PlayerCharacter))
			loadForPlayerCharacter();
		else if (this.absCharacter.getObjectType().equals(GameObjectType.NPC))
			loadForNPC();

	}

	public void loadGoldItems() {

		if (ConfigManager.serverType.equals(Enum.ServerType.LOGINSERVER)) {
			//other server, just make generic
			this.goldInventory = new MobLoot(this.absCharacter, 0);
			this.goldBank = new MobLoot(this.absCharacter, 0);
			this.goldVault = new MobLoot(this.absCharacter, 0);
			return;
		}

		//create inventory gold if needed
		if (this.goldInventory == null)
			if (this.absCharacter != null && (this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter) || this.absCharacter.getObjectType().equals(GameObjectType.NPC)))
				this.goldInventory = Item.newGoldItem(this.absCharacter, ItemBase.getItemBase(7), Enum.ItemContainerType.INVENTORY);
			else
				this.goldInventory = new MobLoot(this.absCharacter, 0);

		//create bank gold if needed
		if (this.goldBank == null)
			if (this.absCharacter != null && this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter))
				this.goldBank = Item.newGoldItem(this.absCharacter, ItemBase.getItemBase(7), Enum.ItemContainerType.BANK);
			else
				this.goldBank = new MobLoot(this.absCharacter, 0);

		//create vault gold if needed
		if (this.goldVault == null)
			if (this.absCharacter != null && this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter)){
				this.goldVault = this.account.vaultGold;
			}

			else
				this.goldVault = new MobLoot(this.absCharacter, 0);

		this.itemIDtoType.put(this.goldInventory.getObjectUUID(), this.goldInventory.getObjectType().ordinal());
		this.itemIDtoType.put(this.goldBank.getObjectUUID(), this.goldBank.getObjectType().ordinal());
		this.itemIDtoType.put(this.goldVault.getObjectUUID(), this.goldVault.getObjectType().ordinal());

	}

	private void loadForPlayerCharacter() {
		ArrayList<Item> al = null;

		// TODO Verify this is an actual account.
		this.account = ((PlayerCharacter) this.absCharacter).getAccount();

		// Get Items for player and vault
		if (ConfigManager.serverType.equals(Enum.ServerType.LOGINSERVER)) //login, only need equipped items
			al = DbManager.ItemQueries.GET_EQUIPPED_ITEMS(this.absCharacter.getObjectUUID());
		else
			al = DbManager.ItemQueries.GET_ITEMS_FOR_PC(this.absCharacter.getObjectUUID());

		for (Item i : al) {

			i.validateItemContainer();
			this.itemIDtoType.put(i.getObjectUUID(), i.getObjectType().ordinal());

			switch (i.containerType) {
				case EQUIPPED:
					if (this.equipped.containsValue(i) == false) {
						this.equipped.put((int) i.getEquipSlot(), i);
						addEquipOrder((int) i.getEquipSlot());
					}
					break;
				case BANK:
					if (i.getItemBase().getType().equals(ItemType.GOLD))
						this.goldBank = i;
					else if (this.bank.contains(i) == false)
						this.bank.add(i);
					break;
				case INVENTORY:
					if (i.getItemBase().getType().equals(ItemType.GOLD))
						this.goldInventory = i;
					else if (this.inventory.contains(i) == false)
						this.inventory.add(i);
						break;
				case VAULT:
					if (i.getItemBase().getType().equals(ItemType.GOLD))
						this.goldVault = i;
					else if (this.vault.contains(i) == false)
						this.vault.add(i);
					break;
					default:
						i.junk();
						break;
			}

		}

		this.goldVault = this.account.vaultGold;

		//check all gold is created
		//loadGoldItems();
		calculateWeights();
	}

	private void loadForNPC() {
		ArrayList<Item> al = null;

		// Get all items related to this NPC:
		al = DbManager.ItemQueries.GET_ITEMS_FOR_NPC(this.absCharacter.getObjectUUID());

		for (Item i : al) {
			i.validateItemContainer();
			this.itemIDtoType.put(i.getObjectUUID(), i.getObjectType().ordinal());

			switch (i.containerType) {
				case EQUIPPED:
				if (this.equipped.containsValue(i) == false)
					this.equipped.put((int) i.getEquipSlot(), i);
				break;
				case BANK:
					if (i.getItemBase().getType().equals(ItemType.GOLD))
						this.goldBank = i;
					else if (this.bank.contains(i) == false)
						this.bank.add(i);
					break;
				case INVENTORY:
					if (i.getItemBase().getType().equals(ItemType.GOLD))
						this.goldInventory = i;
					else if (this.inventory.contains(i) == false)
						this.inventory.add(i);
					break;
					default:
						i.junk();
						break;
			}
		}

		//check all gold is created
		//loadGoldItems();
	}

	private void loadForGeneric() {
		this.bankWeight = 0;
		this.inventoryWeight = 0;
		this.equipWeight = 0;
		this.vaultWeight = 0;

		//check all gold is created
		//loadGoldItems();
		// Always initialize with bank and vault closed
		bankOpened = false;
		vaultOpened = false;
	}

	//Positve Amount = TO BUILDING; Negative Amount = FROM BUILDING. flip signs for Player inventory.
	public synchronized boolean transferGoldToFromBuilding(int amount, AbstractWorldObject object){
        if (this.absCharacter.getObjectType() != GameObjectType.PlayerCharacter)
			return false;

        PlayerCharacter player = (PlayerCharacter) this.absCharacter;

		switch (object.getObjectType()){
		case Building:
			Building building = (Building)object;

			if (!this.getGoldInventory().validForInventory(player.getClientConnection(), player, this))
				return false;

			if (amount <0 && amount > building.getStrongboxValue())
				return false;

			// Not enough gold in inventory to transfer to tree

			if ((amount > 0) &&
					(this.getGoldInventory().getNumOfItems() - amount < 0)) {
				sendErrorPopup(player, 28);
				return false;
			}

			if (this.getGoldInventory().getNumOfItems() - amount > MBServerStatics.PLAYER_GOLD_LIMIT){
				ErrorPopupMsg.sendErrorPopup(player, 202);
				return false;
			}
			
			

			// Not enough gold to transfer to inventory from tree

			if ((amount < 0) &&
					(building.getStrongboxValue() + amount < 0)) {
				sendErrorPopup(player, 127);
				return false;
			}

			if (amount < 0)
				if (!building.hasFunds(-amount))
					return false;

			//Verify player can access building to transfer goldItem

			if (!BuildingManager.playerCanManage(player, building))
				return false;

			if (building.getStrongboxValue() + amount > building.getMaxGold()){
				ErrorPopupMsg.sendErrorPopup(player, 201);
				return false;
			}
			
			if (this.getOwner().getCharItemManager().getGoldTrading() > 0){
				if (this.getOwner().getObjectType().equals(GameObjectType.PlayerCharacter))
				ErrorPopupMsg.sendErrorPopup((PlayerCharacter)this.getOwner(), 195);
				return false;
			}

			if (!this.modifyInventoryGold(-amount)){

				Logger.error(player.getName() + " transfer amount = " + amount +" ; Gold Inventory = " + this.getGoldInventory().getNumOfItems());

				//  ChatManager.chatSystemError(player, "You do not have this Gold.");
				return false;
			}

			if (!building.transferGold(amount,false)){

				Logger.error(player.getName() + " transfer amount = " + amount +" ; Gold Inventory = " + this.getGoldInventory().getNumOfItems() + "; Building Strongbox = " + building.getStrongboxValue());

				//ChatManager.chatSystemError(player, "Something went terribly wrong. Contact CCR.");
				return false;
			}

			break;
		case Warehouse:

			Warehouse warehouse = (Warehouse)object;

			if (amount < 0){
                if (!warehouse.deposit((PlayerCharacter) this.absCharacter, this.getGoldInventory(), amount*-1, true,true)){

                    ErrorPopupMsg.sendErrorPopup((PlayerCharacter) this.absCharacter, 203);
					return false;
				}
			}else{
                if (!warehouse.withdraw((PlayerCharacter) this.absCharacter, this.getGoldInventory().getItemBase(), amount*-1, true,true)){

                    ErrorPopupMsg.sendErrorPopup((PlayerCharacter) this.absCharacter, 203);
					return false;
				}

			}

			break;

		}
		return true;
	}

	/*
	 * Item Controls
	 */
	public synchronized boolean modifyInventoryGold(int modifyValue) {

		Item goldItem;
		PlayerCharacter player;
		boolean success = false;

		goldItem = getGoldInventory();

		if (goldItem == null) {
			Logger.error("ModifyInventoryGold", "Could not create gold item");
			return success;
		}

		if (this.getGoldInventory().getNumOfItems() + modifyValue > MBServerStatics.PLAYER_GOLD_LIMIT){
			return false;
		}

		if (this.getGoldInventory().getNumOfItems() + modifyValue < 0)
			return false;

		// No database update for npc's gold values so we use the player object
		// for flow control later on.

		if (this.absCharacter.getObjectType() == GameObjectType.PlayerCharacter)
			player = (PlayerCharacter) this.absCharacter;
		else
			player = null;

		// If this is an update for a player character update the database

		if (player != null)
			try {
				if (!DbManager.ItemQueries.UPDATE_GOLD(this.getGoldInventory(), this.goldInventory.getNumOfItems() + modifyValue)){
					return false;
				}


				success = true;
			} catch (Exception e) {
				Logger.error("ModifyInventoryGold", "Error writing to database");
			}

		// Update in-game gold values for character
		goldItem.setNumOfItems(goldItem.getNumOfItems() + modifyValue);
		UpdateGoldMsg ugm = new UpdateGoldMsg(this.absCharacter);
		ugm.configure();

		Dispatch dispatch = Dispatch.borrow(player, ugm);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		return success;
	}
	
	 public synchronized boolean tradeRequest(TradeRequestMsg msg) {

	        PlayerCharacter source = (PlayerCharacter) this.getOwner();
	        PlayerCharacter target = PlayerCharacter.getFromCache(msg.getPlayerID());
	        Dispatch dispatch;

	        if (!canTrade(source, target)) {
	            ChatManager.chatSystemError(source, "Can't currently trade with target player");
	            return false;
	        }

	        // TODO uncomment this block after we determine when we
	        // setBankOpen(false) and setVaultOpen(false)
	        CharacterItemManager cim1 = source.getCharItemManager();
	        CharacterItemManager cim2 = target.getCharItemManager();

	        if (cim1 == null)
	            return false;

	        if (cim2 == null)
	            return false;

	        dispatch = Dispatch.borrow(target, msg);
	        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	        return true;

	    }
	
	 public synchronized boolean invalidTradeRequest(InvalidTradeRequestMsg msg) {
	        PlayerCharacter requester = PlayerCharacter.getFromCache(msg.getRequesterID());
	        Dispatch dispatch;

	        dispatch = Dispatch.borrow(requester, msg);
	        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	        return true;

	    }
	 
	 public synchronized boolean canTrade(PlayerCharacter playerA, PlayerCharacter playerB) {

	        if (playerA == null || playerB == null)
	            return false;

	        //make sure both are alive
	        if (!playerA.isAlive() || !playerB.isAlive())
	            return false;

	        //distance check
	        Vector3fImmutable aLoc = playerA.getLoc();
	        Vector3fImmutable bLoc = playerB.getLoc();

	        if (aLoc.distanceSquared2D(bLoc) > sqr(MBServerStatics.TRADE_RANGE))
	            return false;

	        //visibility check
	        if (!playerA.canSee(playerB) || !playerB.canSee(playerA))
	            return false;

	        if (playerA.lastBuildingAccessed != 0) {
	            ManageCityAssetsMsg mca = new ManageCityAssetsMsg();
				mca.actionType = 4;
				mca.setTargetType(Enum.GameObjectType.Building.ordinal());
	            mca.setTargetID(playerA.lastBuildingAccessed);
	            Dispatch dispatch = Dispatch.borrow(playerA, mca);
	            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	            playerA.lastBuildingAccessed = 0;
	        }

	        return true;
	    }
	
	 public synchronized boolean acceptTradeRequest(AcceptTradeRequestMsg msg) {

	        PlayerCharacter source = (PlayerCharacter)this.getOwner();
	        PlayerCharacter target = PlayerCharacter.getFromCache(msg.getTargetID());

	        Dispatch dispatch;

	        if (source == null || !source.isAlive())
	            return false;

	        if (target == null || !target.isAlive())
	            return false;

	        if (this.tradingWith != null)
	        	return false;
	        
	        if (!canTrade(source, target))
	            return false;

	        // verify characterTarget is in range
	        if (source.getLoc().distanceSquared2D(target.getLoc()) > sqr(MBServerStatics.TRADE_RANGE))
	            return false;

	        // TODO uncomment this block after we determine when we
	        // setBankOpen(false) and setVaultOpen(false)
	        /*
	         * CharacterItemManager cim1 = source.getCharItemManager();
	         * CharacterItemManager cim2 = characterTarget.getCharItemManager(); if (cim1 ==
	         * null) return false; if (cim2 == null) return false; if (cim1.isBankOpen())
	         * return false; if (cim2.isVaultOpen()) return false;
	         */
	        ClientConnection sourceConn = source.getClientConnection();
	        ClientConnection targetConn = target.getClientConnection();

	        if (sourceConn == null)
	            return false;

	        if (targetConn == null)
	            return false;

	       
	        CharacterItemManager toTradeWith = target.getCharItemManager();

	        if (toTradeWith == null)
	            return false;

	        Account sourceAccount = source.getAccount();
	        Account targetAccount = target.getAccount();

	        UpdateVaultMsg uvmSource = new UpdateVaultMsg(sourceAccount);
	        UpdateVaultMsg uvmTarget = new UpdateVaultMsg(targetAccount);

	        dispatch = Dispatch.borrow(source, uvmSource);
	        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);

	        dispatch = Dispatch.borrow(target, uvmTarget);
	        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);

	        this.setVaultOpen(false);
	        toTradeWith.setVaultOpen(false);
	        this.setBankOpen(false);
	        toTradeWith.setBankOpen(false);

	        OpenTradeWindowMsg otwm = new OpenTradeWindowMsg(msg.getUnknown01(), source, target);

	        // Only start trade if both players aren't already trading with
	        // someone
	        
	        if (this.getTradingWith() != null || toTradeWith.getTradingWith() != null)
	        	return false;
	        	
	            this.initializeTrade();
	            toTradeWith.initializeTrade();
	            this.setTradingWith(targetConn);
	            toTradeWith.setTradingWith(sourceConn);
	            this.tradeID = msg.getUnknown01();
	            toTradeWith.tradeID = msg.getUnknown01();

	            dispatch = Dispatch.borrow(source, otwm);
	            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);

	            dispatch = Dispatch.borrow(target, otwm);
	            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);

			return true;
	    }
	
	  public synchronized boolean addItemToTradeWindow(AddItemToTradeWindowMsg msg) {
	        PlayerCharacter source =(PlayerCharacter)this.getOwner();
	        Dispatch dispatch;

	        if (source == null || !source.isAlive())
	            return false;

	       


	        ClientConnection ccOther = this.getTradingWith();

	        if (ccOther == null)
	            return false;

	        PlayerCharacter other = ccOther.getPlayerCharacter();

	        if (other == null || !other.isAlive())
	            return false;

	        CharacterItemManager tradingWith = other.getCharItemManager();

	        if (tradingWith == null)
	            return false;

	        if (!canTrade(source, other))
	            return false;

	        Item i = Item.getFromCache(msg.getItemID());

	        if (i == null)
	            return false;

	        if (!this.doesCharOwnThisItem(i.getObjectUUID()))
	            return false;

	        //can't add item to trade window twice
	        if (this.tradingContains(i))
	            return false;

	        //dupe check
	        if (!i.validForInventory(source.getClientConnection(), source, this))
	            return false;

	        if (!tradingWith.hasRoomTrade(i.getItemBase().getWeight())) {
	            dispatch = Dispatch.borrow(source, msg);
	            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
	            return false;
	        }

	        UpdateTradeWindowMsg utwm = new UpdateTradeWindowMsg(source, other);

	        this.setTradeCommitted((byte) 0);
	        tradingWith.setTradeCommitted((byte) 0);

	        this.addItemToTrade(i);
	
	        dispatch = Dispatch.borrow(other, msg);
	        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);

	        modifyCommitToTrade();

	        dispatch = Dispatch.borrow(other, utwm);
	        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
			return true;
	    }
	
	public synchronized boolean addGoldToTradeWindow(AddGoldToTradeWindowMsg msg) {

        PlayerCharacter source = (PlayerCharacter) this.getOwner();
        Dispatch dispatch;

        if (source == null || !source.isAlive())
            return false;

       
       

        ClientConnection ccOther = this.getTradingWith();

        if (ccOther == null)
            return false;

        PlayerCharacter other = ccOther.getPlayerCharacter();

        if (other == null || !other.isAlive())
            return false;

        CharacterItemManager tradingWith = other.getCharItemManager();

        if (tradingWith == null)
            return false;

        UpdateTradeWindowMsg utwm = new UpdateTradeWindowMsg(other, source);
        UpdateTradeWindowMsg utwmOther = new UpdateTradeWindowMsg(source, other);

        if (!canTrade(source, other))
            return false;

        this.setTradeCommitted((byte) 0);
        tradingWith.setTradeCommitted((byte) 0);

        int amt = msg.getAmount();

        if (amt <= 0){
            Logger.info( source.getFirstName() + " added negative gold to trade window. Dupe attempt FAILED!");
            return false;
        }
        
        if (amt > MBServerStatics.PLAYER_GOLD_LIMIT)
        	return false;
        
        if (this.getGoldInventory().getNumOfItems() - amt < 0)
        	return false;

        this.addGoldToTrade(amt);

        // BONUS CODE BELOW:  Thanks some unknown retard!
        //		sourceItemMan.updateInventory(sourceItemMan.getInventory(), true);

        UpdateGoldMsg ugm = new UpdateGoldMsg(source);
        ugm.configure();

        modifyCommitToTrade();

        dispatch = Dispatch.borrow(source, utwm);
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

        dispatch = Dispatch.borrow(source, ugm);
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

        dispatch = Dispatch.borrow(other, utwmOther);
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		return true;

    }
	
	
	public synchronized boolean uncommitToTrade(UncommitToTradeMsg msg) {

        PlayerCharacter source = (PlayerCharacter) this.getOwner();

        if (source == null || !source.isAlive())
            return false;

        CharacterItemManager sourceItemMan = source.getCharItemManager();

        if (sourceItemMan == null)
            return false;

        sourceItemMan.setTradeCommitted((byte) 0);

        ClientConnection ccOther = sourceItemMan.getTradingWith();

        if (ccOther == null)
            return false;

        PlayerCharacter other = ccOther.getPlayerCharacter();

        if (other == null)
            return false;

        if (!canTrade(source, other))
            return false;

        return modifyCommitToTrade();
    }
	
	public synchronized boolean commitToTrade(CommitToTradeMsg msg) {

        PlayerCharacter source = (PlayerCharacter)this.getOwner();

        if (source == null || !source.isAlive())
            return false;


        

        this.setTradeCommitted((byte) 1);

        ClientConnection ccOther = this.getTradingWith();

        if (ccOther == null)
            return false;

        PlayerCharacter other = ccOther.getPlayerCharacter();

        if (other == null || !other.isAlive())
            return false;

        CharacterItemManager tradingWith = other.getCharItemManager();

        if (tradingWith == null)
            return false;

        if (!canTrade(source, other))
            return false;

        modifyCommitToTrade();

        if (this.getTradeCommitted() == (byte) 1 && tradingWith.getTradeCommitted() == (byte) 1) {
        	int tradeID = this.tradeID;
            CloseTradeWindowMsg ctwm1 = new CloseTradeWindowMsg(source, tradeID);
            CloseTradeWindowMsg ctwm2 = new CloseTradeWindowMsg(other, tradeID);
            this.commitTrade();
            this.closeTradeWindow(ctwm1, false);
            other.getCharItemManager().closeTradeWindow(ctwm2, false);
        }
		return true;
    }
	
	  private synchronized boolean modifyCommitToTrade() {
	        CharacterItemManager man1 = this;
	        
	        if (this.getTradingWith() == null)
	        	return false;
	        
	        if (this.getTradingWith().getPlayerCharacter() == null)
	        	return false;
	        CharacterItemManager man2 = this.getTradingWith().getPlayerCharacter().getCharItemManager();
	        Dispatch dispatch;

	        if (man1 == null || man2 == null)
	            return false;

	        ModifyCommitToTradeMsg modify = new ModifyCommitToTradeMsg(this.getOwner(), man2.getOwner(), man1.getTradeCommitted(),
	                man2.getTradeCommitted());

	        dispatch = Dispatch.borrow((PlayerCharacter) this.getOwner(), modify);
	        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

	        dispatch = Dispatch.borrow((PlayerCharacter) man2.getOwner(), modify);
	        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	        
	        return true;

	    }
	
	 public synchronized boolean closeTradeWindow(CloseTradeWindowMsg msg, boolean sourceTrade) {

	     
	        Dispatch dispatch;

	        PlayerCharacter source = (PlayerCharacter) this.getOwner();
	        if (source == null)
	            return false;

	        CharacterItemManager sourceItemMan = source.getCharItemManager();

	        if (sourceItemMan == null)
	            return false;
	        
	        int tradeID = this.tradeID;
	        CloseTradeWindowMsg closeMsg = new CloseTradeWindowMsg(source,tradeID);

	        dispatch = Dispatch.borrow(source, closeMsg);
	        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	        
	        if (!sourceTrade){
	        	sourceItemMan.endTrade();
	        	return true;
	        }
	        
	        ClientConnection cc2 = sourceItemMan.getTradingWith();

	        if (cc2 == null || cc2.getPlayerCharacter() == null){
		        sourceItemMan.endTrade();
	            return true;
	        }
	        
	        sourceItemMan.endTrade();

	        
	        
	        cc2.getPlayerCharacter().getCharItemManager().closeTradeWindow(msg, false);
	        
	       
	        
	     
			return true;
	    }

	public Item getGoldInventory() {
		if (this.goldInventory == null)
			loadGoldItems();
		return this.goldInventory;
	}

	public Item getGoldBank() {
		if (this.goldBank == null)
			loadGoldItems();
		return this.goldBank;
	}

	public Item getGoldVault() {
		if (this.goldVault == null)
			loadGoldItems();
		return this.goldVault;
	}

	public void addEquipOrder(int slot) {
		synchronized (this.equipOrder) {
			Integer iSlot = slot;
			if (this.equipOrder.contains(iSlot))
				this.equipOrder.remove(iSlot);
			this.equipOrder.add(slot);
		}
	}

	public synchronized boolean doesCharOwnThisItem(int itemID) {
		return this.itemIDtoType.containsKey(itemID);
	}

	public synchronized boolean junk(Item i) {
		return junk(i, true);
	}

	public synchronized boolean recycle(Item i) {
		if (i.getObjectType() == GameObjectType.Item)
			return junk(i, false);
		else{
			if(this.removeItemFromInventory(i) == false)
				return false;
			((MobLoot)i).recycle((NPC)this.absCharacter);
			calculateInventoryWeight();
			return true;
		}
	}

	// The DeleteItemMsg takes care of updating inventory, so we don't want to do it separately
	public synchronized boolean delete(Item i) {
		return junk(i, false);
	}

	//cleanup an item from CharacterItemManager if it doesn't belong here
	public synchronized boolean cleanupDupe(Item i) {
		if (i == null)
			return false;

		if(i.getItemBase().getType().equals(ItemType.GOLD)){
			if (this.getGoldInventory() != null){
				if (i.getObjectUUID() == this.getGoldInventory().getObjectUUID())
					this.goldInventory = null;
			}else if (this.getGoldBank() != null){
				if (i.getObjectUUID() == this.getGoldBank().getObjectUUID())
					this.goldBank = null;
			}
			return true;
		}

		byte slot = i.getEquipSlot();

		if (this.doesCharOwnThisItem(i.getObjectUUID()) == false)
			return false;

		// remove it from other lists:
		this.remItemFromLists(i, slot);
		this.itemIDtoType.remove(i.getObjectUUID());

		calculateWeights();
		return true;
	}

	public synchronized boolean consume(Item i) {
		i.decrementChargesRemaining();
		if (i.getChargesRemaining() > 0)
			return true;
		return junk(i, true);
	}

	private synchronized boolean junk(Item i, boolean updateInventory) {
		if (i.getItemBase().getType().equals(ItemType.GOLD)) {
			if (this.getGoldInventory().getObjectUUID() == i.getObjectUUID())
				if (DbManager.ItemQueries.UPDATE_GOLD(i, 0)) {
					this.getGoldInventory().setNumOfItems(0);
					if (updateInventory)
					updateInventory();
					return true;
				}else{
					return false;
				}
			if (!(this.absCharacter.getObjectType().equals(GameObjectType.Mob)))
				return false;
		}

		byte slot = i.getEquipSlot();

        if (this.doesCharOwnThisItem(i.getObjectUUID()) == false && this.absCharacter.getObjectType() != GameObjectType.Mob && (i.containerType != Enum.ItemContainerType.FORGE))
			return false;

		// remove it from other lists:
		this.remItemFromLists(i, slot);
		this.itemIDtoType.remove(i.getObjectUUID());

		i.junk();

		//Why are we adding junked items?!

		//		if (i.getObjectType() != GameObjectType.MobLoot)
		//			CharacterItemManager.junkedItems.add(i);


		calculateWeights();

		if (updateInventory)
			// Send the new inventory
			//updateInventory(i, false); this line was causing entire inventory to disappear
			updateInventory(this.getInventory(), true);

		return true;
	}

	public synchronized boolean moveItemToInventory(Item i) {

		boolean fromEquip = false;
		synchronized (this) {
			byte slot = i.getEquipSlot();

			//Skip if NOT in vault.
			if (i.containerType != Enum.ItemContainerType.VAULT)
				if (this.doesCharOwnThisItem(i.getObjectUUID()) == false)
					return false;

			// Only valid from bank, equip and vault
			if (!bankContains(i) && !equippedContains(i) && !vaultContains(i))
				return false;

			if (equippedContains(i)) {
				fromEquip = true;
				ItemBase ib = i.getItemBase();
				if (ib != null && ib.getType().equals(ItemType.GOLD))
					this.absCharacter.cancelOnUnEquip();
			}

			// check to see what type of AbstractCharacter subclass we have stored
			if (this.absCharacter.getClass() == PlayerCharacter.class) {
				if (!i.moveItemToInventory((PlayerCharacter) this.absCharacter))
					return false;
			} else if (!i.moveItemToInventory((NPC) this.absCharacter))
				return false;

			// remove it from other lists:
			this.remItemFromLists(i, slot);

			// add to Inventory
			this.inventory.add(i);
			i.addToCache();
			this.itemIDtoType.put(i.getObjectUUID(), i.getObjectType().ordinal());

			calculateWeights();
		}

		//Apply bonuses if from equip
		if (fromEquip && this.absCharacter != null) {
			this.absCharacter.applyBonuses();
			if (this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter))
				this.absCharacter.incVer();
		}

		return true;
	}

	public synchronized boolean moveItemToBank(Item i) {
		byte slot = i.getEquipSlot();

		if (this.doesCharOwnThisItem(i.getObjectUUID()) == false)
			return false;

		// Item must be in inventory to move to bank
		if (!this.inventory.contains(i))
			return false;

		// check to see what type of AbstractCharacter subclass we have stored
		if (this.absCharacter.getClass() == PlayerCharacter.class) {
			if (!i.moveItemToBank((PlayerCharacter) this.absCharacter))
				return false;
		} else if (!i.moveItemToBank((NPC) this.absCharacter))
			return false;

		// remove it from other lists:
		this.remItemFromLists(i, slot);

		// add to Bank
		this.bank.add(i);
		i.addToCache();

		calculateWeights();

		return true;
	}

	public synchronized boolean moveGoldToBank(Item from, int amt) {
		if (from == null)
			return false;
		if (from.getNumOfItems() - amt < 0)
			return false;
		if (this.goldBank.getNumOfItems() + amt > MBServerStatics.BANK_GOLD_LIMIT){
			if (this.absCharacter.getObjectType() == GameObjectType.PlayerCharacter){
				PlayerCharacter pc = (PlayerCharacter)this.absCharacter;
				if (pc.getClientConnection() != null)
					ErrorPopupMsg.sendErrorPopup(pc, 202);
				return false;
			}
		}

		if (!DbManager.ItemQueries.MOVE_GOLD(from, this.getGoldBank(), amt))
			return false;
		from.setNumOfItems(from.getNumOfItems() - amt);
		this.goldBank.setNumOfItems(this.goldBank.getNumOfItems() + amt);
		return true;
	}

	public synchronized boolean moveGoldToVault(Item from, int amt) {
		if (from == null)
			return false;
		if (from.getNumOfItems() - amt < 0)
			return false;
		if (!DbManager.ItemQueries.MOVE_GOLD(from, this.account.vaultGold, amt))
			return false;
		from.setNumOfItems(from.getNumOfItems() - amt);
		this.account.vaultGold.setNumOfItems(this.goldVault.getNumOfItems() + amt);
		return true;
	}

	public synchronized boolean moveGoldToInventory(Item from, int amt) {
		if (from == null)
			return false;
		if (from.getNumOfItems() - amt < 0 || amt < 1)
			return false;

		if (this.goldInventory.getNumOfItems() + amt > MBServerStatics.PLAYER_GOLD_LIMIT){
			if (this.absCharacter.getObjectType() == GameObjectType.PlayerCharacter){
				PlayerCharacter pc = (PlayerCharacter)this.absCharacter;
				if (pc.getClientConnection() != null)
					ErrorPopupMsg.sendErrorPopup(pc, 202);
				return false;
			}
		}

		if (from instanceof MobLoot) {
			if (!DbManager.ItemQueries.UPDATE_GOLD(this.getGoldInventory(),
					this.goldInventory.getNumOfItems() + amt))
				return false;
		} else if (!DbManager.ItemQueries.MOVE_GOLD(from, this.goldInventory, amt))
			return false;
		from.setNumOfItems(from.getNumOfItems() - amt);
		this.goldInventory.setNumOfItems(this.goldInventory.getNumOfItems() + amt);
		return true;
	}

	//This is called by the addGold devCmd.
	public synchronized boolean addGoldToInventory(int amt, boolean fromDevCmd) {

		if (this.absCharacter == null || (!(this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter))))
			return false;

		if (this.getGoldInventory().getNumOfItems() + amt > MBServerStatics.PLAYER_GOLD_LIMIT){
			return false;
		}

		if (this.getGoldInventory().getNumOfItems() + amt < 0)
			return false;


		boolean worked = DbManager.ItemQueries.UPDATE_GOLD(this.getGoldInventory(), this.goldInventory.getNumOfItems() + amt);
		if (worked) {
			//log this since it's technically a dupe. Only use on test server!
			if (fromDevCmd) {
				String logString = this.absCharacter.getName() + " added " + amt + " gold to their inventory";
				Logger.info(logString);
			}
			this.goldInventory.setNumOfItems(this.goldInventory.getNumOfItems() + amt);
		}
		return worked;
	}

	//Used to trainsfer gold from one inventory to another, for steal, etc.
	public boolean transferGoldToMyInventory(AbstractCharacter tar, int amount) {
		if (tar == null)
			return false;

		CharacterItemManager tarCim = tar.getCharItemManager();
		if (tarCim == null)
			return false;

		if (this.getGoldInventory().getNumOfItems()  + amount < 0)
			return false;

		if (this.getGoldInventory().getNumOfItems() + amount > MBServerStatics.PLAYER_GOLD_LIMIT)
			return false;

		if (tarCim.getGoldInventory().getNumOfItems() -amount < 0)
			return false;

		if (tarCim.getGoldInventory().getNumOfItems() - amount > MBServerStatics.PLAYER_GOLD_LIMIT)
			return false;

		synchronized (this) {
			synchronized (tarCim) {
				if (!tarCim.addGoldToInventory(0 - amount, false)) //remove gold from target
					return false;
				if (!addGoldToInventory(amount, false)) //add to this inventory
					return false;
			}
		}
		return true;
	}

	public synchronized boolean moveItemToVault(Item i) {
		byte slot = i.getEquipSlot();

		//		if (this.doesCharOwnThisItem(i.getObjectUUID()) == false)
		//			return false;

		// Item must be in inventory to move to vault
		if (!this.inventory.contains(i))
			return false;

		// check to see what type of AbstractCharacter subclass we have stored
		if (this.absCharacter.getClass() == PlayerCharacter.class) {
			if (!i.moveItemToVault(this.account))
				return false;
		} else
			return false; // NPC's dont have vaults!

		// remove it from other lists:
		this.remItemFromLists(i, slot);

		// add to Vault
		i.addToCache();

		calculateWeights();

		return true;
	}

	// This removes ingame item from inventory for loot.
	private synchronized boolean removeItemFromInventory(Item i) {
		if (i.getItemBase().getType().equals(ItemType.GOLD)) {
			if (i.getObjectUUID() != this.getGoldInventory().getObjectUUID())
				return false;
			if (!DbManager.ItemQueries.UPDATE_GOLD(this.goldInventory, 0)){
				return false;
			}

		} else {
			if (this.doesCharOwnThisItem(i.getObjectUUID()) == false)
				return false;
			if (this.inventory.contains(i)) {
				this.inventory.remove(i);
				this.itemIDtoType.remove(i.getObjectUUID());
				return true;
			}
		}
		// tell client we're removing item
		updateInventory(i, false);
		return false;
	}

	// This adds item to inventory for loot. Validity checks already handled
	public synchronized boolean addItemToInventory(Item i) {
		if (i.getItemBase().getType().equals(ItemType.GOLD))
			if (this.absCharacter.getObjectType() == GameObjectType.Mob) {
				if (this.goldInventory == null)
					loadGoldItems();
				this.goldInventory.setNumOfItems(this.goldInventory.getNumOfItems() + i.getNumOfItems());
			} else {
				int amt = i.getNumOfItems();
				if (DbManager.ItemQueries.UPDATE_GOLD(this.goldInventory, this.goldInventory.getNumOfItems() + amt)) {
					updateInventory();
					return true;
				}

				return false;
			}

		this.inventory.add(i);
		this.itemIDtoType.put(i.getObjectUUID(), i.getObjectType().ordinal());

		ItemBase ib = i.getItemBase();
		if (ib != null)
			this.inventoryWeight += ib.getWeight();
		return true;
	}



	//called for adding gold of a specified amount
	public synchronized boolean addItemToInventory(Item i, int amount) {
		if (i.getItemBase().getType().equals(ItemType.GOLD))
            return DbManager.ItemQueries.UPDATE_GOLD(this.getGoldInventory(), this.goldInventory.getNumOfItems() + amount);
		return false;
	}

	public boolean equipItem(Item i, byte slot) {

		synchronized (this) {
			byte curSlot = i.getEquipSlot(); // Should be 0

            if (this.doesCharOwnThisItem(i.getObjectUUID()) == false && this.absCharacter.getObjectType() != GameObjectType.Mob) {
				Logger.error("Doesnt own item");
				return false;
			}

			// Item must be in inventory to equip
            if (!this.inventory.contains(i) && this.absCharacter.getObjectType() != GameObjectType.Mob)
				return false;

			// make sure player can equip item
			if (i.getItemBase() == null)
				return false;
            if (!i.getItemBase().canEquip(slot, this, absCharacter, i) && this.absCharacter.getObjectType() != GameObjectType.Mob)
				return false;

			// check to see if item is already there.
			Item old = this.equipped.get((int) slot);
			if (old != null) {
				Logger.error( "already equipped");
				return false;
			}

			// check to see what type of AbstractCharacter subclass we have stored
			if (this.absCharacter.getClass() == PlayerCharacter.class) {
				if (!i.equipItem((PlayerCharacter) this.absCharacter, slot))
					return false;
			} else if (this.absCharacter.getObjectType() == GameObjectType.Mob) {
				if (!i.equipItem((Mob) this.absCharacter, slot)) {
					Logger.error("Failed to set Equip");
					return false;
				}

			} else if (!i.equipItem((NPC) this.absCharacter, slot))
				return false;

			// remove it from other lists:
			this.remItemFromLists(i, slot);

			// add to Equipped
			this.equipped.put((int) slot, i);
			i.addToCache();

			addEquipOrder(i.getEquipSlot());

			//calculateWeights();
		}

		//Apply Bonuses and update player
		if (this.absCharacter != null) {
			this.absCharacter.applyBonuses();
			if (this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter))
				this.absCharacter.incVer();
		}

		return true;
	}

	//Used for buying MobEquipment from NPC
	//Handles the gold transfer aspect

	public synchronized boolean buyFromNPC(Building vendorBuilding, int cost,int buildingDeposit) {

		Item gold = this.getGoldInventory();

		if (cost <= 0 || (gold.getNumOfItems() - cost) < 0)
			return false;
		
		
		if (this.getOwner() != null && this.getOwner().getObjectType().equals(GameObjectType.PlayerCharacter)){
			if (this.goldTradingAmount > 0){
				ErrorPopupMsg.sendErrorPopup((PlayerCharacter)this.getOwner(), 195);
				return false;
			}
		}

		// Create gold from screatch instead of building strongbox
		// if the NPC is not slotted.

		if (vendorBuilding == null) {

            return this.modifyInventoryGold(-cost);
        }


		if (vendorBuilding.getStrongboxValue() + cost > vendorBuilding.getMaxGold()){

			if (this.absCharacter.getObjectType() == GameObjectType.PlayerCharacter){
				PlayerCharacter pc = (PlayerCharacter)this.absCharacter;
				if (pc.getClientConnection() != null)
					ErrorPopupMsg.sendErrorPopup(pc, 206);
			}

			return false;
		}


		// Update strongbox and inventory gold
		if (!this.modifyInventoryGold(-cost))
			return false;
		
		City buildingCity = vendorBuilding.getCity();
		
		if (buildingCity != null){
			buildingCity.transactionLock.writeLock().lock();
			try{
				if (!vendorBuilding.transferGold(buildingDeposit, true))
			        return false;
			}catch(Exception e){
				Logger.error(e);
				return false;
			}finally{
				buildingCity.transactionLock.writeLock().unlock();
			}
		}else
		if (!vendorBuilding.transferGold(buildingDeposit, true))
        return false;
		
		return true;
    }

	//Used for selling items to NPC
	public synchronized boolean sellToNPC(Building building, int cost, Item item) {

		// Create gold from screatch instead of building strongbox
		// if the NPC is not slotted.

		if (this.getGoldInventory().getNumOfItems() + cost < 0)
			return false;

		if (this.getGoldInventory().getNumOfItems() + cost > MBServerStatics.PLAYER_GOLD_LIMIT)
			return false;
		

		if (this.getOwner().getCharItemManager().getGoldTrading() > 0){
			if (this.getOwner().getObjectType().equals(GameObjectType.PlayerCharacter))
			ErrorPopupMsg.sendErrorPopup((PlayerCharacter)this.getOwner(), 195);
			return false;
		}


		if (building == null) {
            return this.modifyInventoryGold(cost);
        }

		//make sure strongbox can afford gold.

		if (!building.hasFunds(cost))
			return false;

		if ((building.getStrongboxValue() - cost) < 0)
			return false;

		// Update strongbox and inventory gold

		if (!building.transferGold(-cost,false))
			return false;

        return this.modifyInventoryGold(cost);
    }

	/**
	 * This sells an item to an npc
	 *
	 * @return True on success
	 */
	public synchronized boolean sellToNPC(Item itemToSell, NPC npc) {

		CharacterItemManager itemMan;

		if (itemToSell == null || npc == null)
			return false;

		itemMan = npc.getCharItemManager();

		if (itemMan == null)
			return false;

		//test npc inventory is not full

		synchronized (this) {
			synchronized (itemMan) {
				if (!this.doesCharOwnThisItem(itemToSell.getObjectUUID()))
					return false;
				// attempt to transfer item in db

				boolean sdrMerchant = false;

				if (npc.getContractID() >= 1900 && npc.getContractID() <= 1906)
					sdrMerchant = true;

				if (sdrMerchant){
					this.delete(itemToSell);
					this.updateInventory();

				}else
					if (!itemToSell.moveItemToInventory(npc))
						return false;

				// db transfer successfull, remove from this character
				// skip this check if this is a mobLoot item (which is not in any inventory)
				if (!sdrMerchant)
					if (!removeItemFromInventory(itemToSell))
						return false;

				// add item to looter.
				if(!sdrMerchant)
					if (!itemMan.addItemToInventory(itemToSell))
						return false;
			}
		}

		// calculate new weights
		calculateInventoryWeight();
		itemMan.calculateInventoryWeight();
		return true;
	}

	/**
	 * This buys an item from an npc
	 * Handles transfer of item.
	 *
	 * @return True on success
	 */
	public synchronized boolean buyFromNPC(Item purchasedItem, NPC npc) {

		CharacterItemManager itemMan;
		ItemBase itemBase;

		if (purchasedItem == null || npc == null)
			return false;

		itemMan = npc.getCharItemManager();

		if (itemMan == null)
			return false;



		synchronized (this) {
			synchronized (itemMan) {
				itemBase = purchasedItem.getItemBase();

				if (itemBase == null)
					return false;

				//test inventory is not full

				if (!hasRoomInventory(itemBase.getWeight()))
					return false;

				if (!itemMan.inventory.contains(purchasedItem))
					return false;
				// attempt to transfer item in db

				if (purchasedItem.getObjectType() == GameObjectType.MobLoot){

					Item newItem = ((MobLoot) purchasedItem).promoteToItem((PlayerCharacter)this.absCharacter);
					if (newItem == null)
						return false;

					if (!itemMan.removeItemFromInventory(purchasedItem))
						return false;

					if (!addItemToInventory(newItem))
						return false;
					//Item was created and still a mobloot item, remove from npc production list in db.

					DbManager.NPCQueries.REMOVE_FROM_PRODUCTION_LIST(purchasedItem.getObjectUUID(),npc.getObjectUUID());


				}else{
					if (!purchasedItem.moveItemToInventory((PlayerCharacter) this.absCharacter))
						return false;

					if (purchasedItem.getValue() != purchasedItem.getMagicValue()){
						DbManager.ItemQueries.UPDATE_VALUE(purchasedItem,0);
						purchasedItem.setValue(0);
					}

					// db transfer successfull, remove from this character
					// skip this check if this is a mobLoot item (which is not in any inventory)
					if (!itemMan.removeItemFromInventory(purchasedItem))
						return false;

					// add item to looter.

					if (!addItemToInventory(purchasedItem))
						return false;
				}

			}
		}

		// calculate new weights
		calculateInventoryWeight();
		itemMan.calculateInventoryWeight();
		return true;
	}

	/**
	 * Loot an item from an AbstractCharacter. Call this function on
	 * the CharacterItemManager of the current item owner, not the looter.
	 * This method will verify that the looter can receive the item
	 * (e.g. inventory isn't full).
	 *
	 * @param i Item being looted
	 * @param looter Player looting the item
	 * @param origin ClientConnection
	 * @return True on success
	 */
	public synchronized Item lootItemFromMe(Item i, PlayerCharacter looter, ClientConnection origin) {
		return lootItemFromMe(i, looter, origin, false, -1);
	}

	//This function is used for both looting and stealing
	public synchronized Item lootItemFromMe(Item lootItem, PlayerCharacter lootingPlayer, ClientConnection origin, boolean fromSteal, int amount) {

		//TODO this function should have more logging
		// make sure lootingPlayer exists
		if (lootingPlayer == null)
			return null;

		// get looters item manager
		CharacterItemManager looterItems = lootingPlayer.getCharItemManager();

		if (looterItems == null)
			return null;

		if (fromSteal) {
			if (!this.absCharacter.isAlive())
				return null;
		} else if (!this.absCharacter.canBeLooted())
			return null;

		MobLoot mobLoot = null;
		if (lootItem instanceof MobLoot) {
			mobLoot = (MobLoot) lootItem;
			if (mobLoot.isDeleted())
				return null;
		}

		//Lock both ItemManagers; lower ID first
		CharacterItemManager lockFirst;
		CharacterItemManager lockSecond;
		if (this.absCharacter.getObjectUUID()
				< looterItems.absCharacter.getObjectUUID()) {
			lockFirst = this;
			lockSecond = looterItems;
		} else {
			lockFirst = looterItems;
			lockSecond = this;
		}

		synchronized (lockFirst) {
			synchronized (lockSecond) {
				// make sure current player has item in inventory
				if (lootItem.getItemBase().getType().equals(ItemType.GOLD) && lootItem.getObjectUUID() != this.getGoldInventory().getObjectUUID() && !(this.absCharacter.getObjectType().equals(GameObjectType.Mob)))
					return null;
				else if (!this.inventory.contains(lootItem) && !this.getEquippedList().contains(lootItem) && !lootItem.getItemBase().getType().equals(ItemType.GOLD))
					return null;

				// get weight of item
				ItemBase ib = lootItem.getItemBase();
				if (ib == null)
					return null;
				short weight = ib.getWeight();

				// make sure lootingPlayer has room for item
				if (!lootItem.getItemBase().getType().equals(ItemType.GOLD) && !looterItems.hasRoomInventory(weight))
					return null;

				if (lootItem.getItemBase().getType().equals(ItemType.GOLD))
					if (amount != -1) { //from steal
						int total = lootItem.getNumOfItems();
						amount = (amount > total) ? total : amount;
						if (!looterItems.moveGoldToInventory(lootItem, amount))
							return null;
						if (mobLoot != null && amount == total)
							this.delete(mobLoot);
					} else { //from loot
						if (!looterItems.moveGoldToInventory(lootItem, lootItem.getNumOfItems()))
							return null;
						if (mobLoot != null) // delete mobloot after it has been looted
							this.delete(mobLoot);
					}
				else {  //not Gold item
					boolean created = false;
					if (mobLoot != null) {
                        lootItem = mobLoot.promoteToItem(lootingPlayer);

						// delete mobloot after it has been looted
						this.delete(mobLoot);
						if (lootItem == null)
							return null;

						created = true;
					}

					// attempt to transfer item in db

					if (!lootItem.moveItemToInventory(lootingPlayer))
						return null;

					// db transfer successfull, remove from this character
					// skip this check if this is a mobLoot item (which is not in any inventory)
					if (mobLoot == null)
						if (!removeItemFromInventory(lootItem))
							return null;

					// add item to lootingPlayer.
					if (!looterItems.addItemToInventory(lootItem))
						return null;
				}
			}
		}

		// calculate new weights
		calculateInventoryWeight();
		looterItems.calculateInventoryWeight();

		return lootItem;
	}

	private synchronized void remItemFromLists(Item i, byte slot) {

		this.equipped.remove((int) slot);
		this.vault.remove(i);
		this.bank.remove(i);
		this.inventory.remove(i);
	}

	/*
	 * Delegates
	 */
	public synchronized boolean bankContains(Item i) {
		if (i.getItemBase().getType().equals(ItemType.GOLD))
			return (this.getGoldBank() != null && this.goldBank.getObjectUUID() == i.getObjectUUID());
		return bank.contains(i);
	}


	public synchronized boolean inventoryContains(Item i) {
		if (i.getItemBase().getType().equals(ItemType.GOLD))
			return (this.getGoldInventory() != null && this.goldInventory.getObjectUUID() == i.getObjectUUID());
		return inventory.contains(i);
	}

	public synchronized boolean forgeContains(Item i,NPC vendor) {
		if (i.getItemBase().getType().equals(ItemType.GOLD))
			return (this.getGoldInventory() != null && this.goldInventory.getObjectUUID() == i.getObjectUUID());
		return vendor.getRolling().contains(i);
	}

	

	public synchronized boolean vaultContains(Item i) {
		if (i.getItemBase().getType().equals(ItemType.GOLD))
			return (this.getGoldVault() != null && this.goldVault.getObjectUUID() == i.getObjectUUID());
		return this.account.getVault().contains(i);
	}

	public synchronized boolean vaultContainsType(ItemBase ib) {
		if (ib.getUUID() == 7)
			return (this.getGoldVault() != null);
		for (Item i : vault) {
			if (i.getItemBase().getUUID() == ib.getUUID())
				return true;
		}
		return false;
	}

	//for calling from devCmd fill vault. Already synchronized
	public boolean vaultContainsTypeA(ItemBase ib) {
		if (ib.getUUID() == 7)
			return (this.getGoldVault() != null);
		for (Item i : vault) {
			if (i.getItemBase().getUUID() == ib.getUUID())
				return true;
		}
		return false;
	}

	
	public synchronized boolean equippedContains(Item i) {
		return equipped.containsValue(i);
	}

	public synchronized Item getItemFromEquipped(int slot) {
		return equipped.get(slot);
	}

	public synchronized Item getItemByUUID(int objectUUID) {
		if (this.itemIDtoType.containsKey(objectUUID)){

			Integer integer = this.itemIDtoType.get(objectUUID);
			if (integer == GameObjectType.Item.ordinal()) {
				return Item.getFromCache(objectUUID);
			} else if (integer == GameObjectType.MobLoot.ordinal()) {
				return MobLoot.getFromCache(objectUUID);
			}

		}

		if (this.getGoldInventory() != null && this.goldInventory.getObjectUUID() == objectUUID)
			return this.goldInventory;
		if (this.getGoldBank() != null && this.goldBank.getObjectUUID() == objectUUID)
			return this.goldBank;
		if (this.getGoldVault() != null && this.goldVault.getObjectUUID() == objectUUID)
			return this.goldVault;
		return null;
	}

	public boolean tradingContains(Item i) {
		if (this.trading == null || i == null)
			return false;
		return this.trading.contains(i.getObjectUUID());
	}

	public boolean isBankOpen() {
		return this.bankOpened;
	}

	public synchronized void setBankOpen(boolean bankOpened) {
		this.bankOpened = bankOpened;
	}

	public boolean isVaultOpen() {
		return this.vaultOpened;
	}

	public synchronized void setVaultOpen(boolean vaultOpened) {
		this.vaultOpened = vaultOpened;
	}

	public ClientConnection getTradingWith() {
		return tradingWith;
	}

	public synchronized void setTradingWith(ClientConnection tradingWith) {
		this.tradingWith = tradingWith;
	}

	public synchronized void clearTradingWith() {
		this.tradingWith = null;
	}

	public int getGoldTrading() {
		return goldTradingAmount;
	}

	public synchronized void setTradeCommitted(byte tradeCommitted) {
		this.tradeCommitted = tradeCommitted;
	}

	public byte getTradeCommitted() {
		return tradeCommitted;
	}

	public HashSet<Integer> getTrading() {
		return trading;
	}


	public synchronized void addItemToTrade(Item i) {
		this.trading.add(i.getObjectUUID());
	}


	public synchronized void setTradeSuccess(boolean tradeSuccess) {
		this.tradeSuccess = tradeSuccess;
	}

	public boolean getTradeSuccess() {
		return tradeSuccess;
	}

	
	public synchronized boolean RemoveEquipmentFromLackOfSkill(PlayerCharacter pc, boolean initialized) {
		

		if (pc == null)
			return false;
		
		if (this.equipped == null)
			return false;

		
		for (int slot : this.equipped.keySet()) {

			if (slot == MBServerStatics.SLOT_HAIRSTYLE || slot == MBServerStatics.SLOT_BEARDSTYLE)
				continue;

			Item item = this.equipped.get(slot);

			if (item == null){
				this.equipped.remove(slot);
				pc.applyBonuses();
				continue;
			}
				
			if (!item.getItemBase().validForSkills(pc.getSkills())){
				this.forceToInventory(slot, item, pc, initialized);
				pc.applyBonuses();
			}
		}

		return true;
	}

	/*
	 * List Copiers
	 */
	/**
	 * Note that this method returns a <b>copy</b> of the internally stored
	 * list.
	 *
	 * @return the equipped
	 */
	public ConcurrentHashMap<Integer, Item> getEquipped() {
		synchronized (this.equipped) {
			return new ConcurrentHashMap<>(this.equipped);
		}
	}

	public ArrayList<Item> getEquippedList() {
		ArrayList<Item> ret = new ArrayList<>();
		synchronized (this.equipOrder) {
			synchronized (this.equipped) {
				for (int slot : this.equipOrder) {
					if (this.equipped.containsKey(slot))
						ret.add(this.equipped.get(slot));
				}
				if (ret.size() != this.equipped.size())
					//missed adding some items, figure out what.
					for (int slot : this.equipped.keySet()) {
						if (!(this.equipOrder.contains(slot))) {
							this.equipOrder.add(slot);
							ret.add(this.equipped.get(slot));
						}
					}
			}
		}
		return ret;
	}

	public Item getEquipped(int slot) {
		synchronized (this.equipped) {
			return this.equipped.get(slot);
		}
	}

	/**
	 * Note that this method returns a <b>copy</b> of the internally stored
	 * list.
	 *
	 * @return the inventory
	 */
	public ArrayList<Item> getInventory() {
		return getInventory(false);
	}

	public ArrayList<Item> getInventory(boolean sendGold) {
		synchronized (this.inventory) {
			ArrayList<Item> ret = new ArrayList<>(this.inventory);
			if (sendGold && this.getGoldInventory() != null && this.goldInventory.getNumOfItems() > 0)
				ret.add(this.goldInventory);
			return ret;
		}
	}

	public int getInventoryCount() {
		synchronized (this.inventory) {
			return this.inventory.size();
		}
	}

	/**
	 * Clears ownership of items. Called when player dies, but before
	 * respawning.
	 *
	 * @return the inventory
	 */
	public synchronized void orphanInventory() {
		PlayerCharacter pc = null;
		if (this.absCharacter != null && this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter))
			pc = (PlayerCharacter) this.absCharacter;
		synchronized (this.inventory) {
			//dupe check, validate player properly owns all items
			if (pc != null) {
				Iterator<Item> iter = this.inventory.iterator();
				while (iter.hasNext()) {
					Item item = iter.next();
					//this call may remove the item from this.inventory
					if (!item.validForInventory(pc.getClientConnection(), pc, this)) {
					}
				}
			}

			if (this.inventory.size() > 0)
				DbManager.ItemQueries.ORPHAN_INVENTORY(this.inventory);
			//make a copy of gold inventory for looting
			//so we don't remove the goldInventory
			if (this.getGoldInventory().getNumOfItems() > 0) {
				int amt = this.goldInventory.getNumOfItems();
				if (DbManager.ItemQueries.UPDATE_GOLD(this.goldInventory, 0)) {
					this.goldInventory.setNumOfItems(0);
					MobLoot gold = new MobLoot(this.absCharacter, amt);
					this.inventory.add(gold);
				}
			}
		}
	}

	/**
	 * This transfers the entire inventory to another list For populating
	 * corpse' inventory when player dies
	 *
	 * @return the inventory
	 */
	public synchronized void transferEntireInventory(
			ArrayList<Item> newInventory, Corpse corpse, boolean enterWorld) {

		PlayerCharacter pc = null;
		if (this.absCharacter != null && this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter))
			pc = (PlayerCharacter) this.absCharacter;

		if (this.getGoldInventory().getNumOfItems() > 0) {
			int amt = this.goldInventory.getNumOfItems();
			if (DbManager.ItemQueries.UPDATE_GOLD(this.goldInventory, 0)) {
				this.goldInventory.setNumOfItems(0);
				MobLoot gold = new MobLoot(this.absCharacter, amt);
				newInventory.add(gold);
			}
		}

		for (Item item : this.inventory) {
			if (item != null)
				if (item instanceof MobLoot) {

					//MobLoot
					item.zeroItem();
					item.containerType = Enum.ItemContainerType.INVENTORY;

					if (item.getItemBase().getType().equals(ItemType.GOLD))
						//only add gold item once
						if (!corpse.hasGold())
							corpse.setHasGold(true);
					newInventory.add(item);
				} else //item
					if (item.getItemBase().getType().equals(ItemType.GOLD)) {
						int amt = item.getNumOfItems();
						item.setNumOfItems(0);
						MobLoot ml = new MobLoot(this.absCharacter, amt);
						ml.zeroItem();
						ml.containerType = Enum.ItemContainerType.INVENTORY;
						if (!corpse.hasGold()) {
							corpse.setHasGold(true);
							newInventory.add(ml);
						}
					} else {
						boolean transferred = item.moveItemToInventory(corpse);
						if (!transferred)
							Logger.error(
									"CharItemManager.transferEntireInvetory",
									"DB Error, Failed to transfer item "
											+ item.getObjectUUID() + " to new owner "
											+ corpse.getObjectUUID());
						newInventory.add(item);

					}
		}

		// tell client we're clearing inventory


		// clear the inventory.
		this.inventory.clear();

		//re-calculate inventory weight
		calculateInventoryWeight();
		if (!enterWorld)
			updateInventory(this.getInventory(), false);
	}
	
	public synchronized void purgeInventory() {
		
		if (!this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter))
			return;
		
		if (this.goldInventory != null)
		if (this.getGoldInventory().getNumOfItems() > 0) {
			if (DbManager.ItemQueries.UPDATE_GOLD(this.goldInventory, 0)) {
				this.goldInventory.setNumOfItems(0);
			}
		}

		if (this.inventory.size() > 0)
			DbManager.ItemQueries.ORPHAN_INVENTORY(this.inventory);

		// clear the inventory.
		this.inventory.clear();
		//re-calculate inventory weight
		calculateInventoryWeight();
	}

	/**
	 * Note that this method returns a <b>copy</b> of the internally stored
	 * list.
	 *
	 * @return the bank
	 */
	public ArrayList<Item> getBank() {
		synchronized (this.bank) {
			ArrayList<Item> ret = new ArrayList<>(this.bank);
			if (this.getGoldBank() != null && this.goldBank.getNumOfItems() > 0)
				ret.add(this.goldBank);
			return ret;
		}
	}

	/**
	 * Note that this method returns a <b>copy</b> of the internally stored
	 * list.
	 *
	 * @return the vault
	 */
	public ArrayList<Item> getVault() {
		synchronized (this.vault) {
			ArrayList<Item> ret = new ArrayList<>(this.vault);
			if (this.getGoldVault() != null && this.goldVault.getNumOfItems() > 0)
				ret.add(this.goldVault);
			return ret;
		}
	}

	public boolean hasRoomInventory(short weight) {
		if (this.absCharacter == null)
			return false;
		if (this.absCharacter.getObjectType() == GameObjectType.PlayerCharacter) {
			PlayerCharacter pc = (PlayerCharacter) this.absCharacter;
			int newWeight = this.getCarriedWeight() + weight;
			return newWeight <= (int) pc.statStrBase * 3;
        } else if (this.absCharacter.getObjectType() == GameObjectType.NPC){
			int newWeight = this.getCarriedWeight() + weight;
            return newWeight <= 1900 + (this.absCharacter.getLevel() * 3);
        }else
			return true; // npc's need checked
	}

	public boolean hasRoomTrade(short itemWeight) {

		PlayerCharacter playerCharacter;
		PlayerCharacter tradeCharacter;

		int tradeWeight;

		if (this.absCharacter == null)
			return false;

		if (this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter) == false)
			return false;

		   playerCharacter = (PlayerCharacter) this.absCharacter;

		if ((this.tradingWith == null) ||
    		(this.tradingWith.isConnected() == false))
		    return false;

		   tradeCharacter = this.tradingWith.getPlayerCharacter();

		   tradeWeight = this.getCarriedWeight() + itemWeight;
		   tradeWeight = tradeWeight + tradeCharacter.getCharItemManager().getTradingWeight();
		   tradeWeight = tradeWeight - this.getTradingWeight();

		   return tradeWeight <= (int) playerCharacter.statStrBase * 3;
	}

	public boolean hasRoomBank(short weight) {
		if (this.absCharacter == null)
			return false;
        return weight <= this.absCharacter.getBankCapacityRemaining();
    }

	public boolean hasRoomVault(short weight) {
		if (this.absCharacter == null)
			return false;
        return weight <= this.absCharacter.getVaultCapacityRemaining();
    }

	public int getCarriedWeight() {
		return getInventoryWeight() + getEquipWeight();
	}

	public int getInventoryWeight() {
		return this.inventoryWeight;
	}

	public int getBankWeight() {
		return this.bankWeight;
	}

	public int getEquipWeight() {
		return this.equipWeight;
	}

	public int getVaultWeight() {
		return this.vaultWeight;
	}

	public int getTradingForWeight() {
		return calculateTradingForWeight();
	}

	public int getTradingWeight() {

		int weight = 0;
		Item item;

		for (int i : this.trading) {
			item = Item.getFromCache(i);

			if (item == null)
				continue;

			ItemBase ib = item.getItemBase();
			weight += ib.getWeight();
		}
		return weight;
	}

	public AbstractCharacter getOwner() {
		return this.absCharacter;
	}

	public void calculateWeights() {
		calculateBankWeight();
		calculateInventoryWeight();
		calculateEquipWeight();
		calculateVaultWeight();
	}

	public void calculateBankWeight() {
		this.bankWeight = 0;
		for (Item i : this.bank) {
			ItemBase ib = i.getItemBase();
			if (ib != null)
				this.bankWeight += ib.getWeight();
		}
	}

	public void calculateEquipWeight() {
		this.equipWeight = 0;
		Collection<Item> c = this.equipped.values();
		Iterator<Item> it = c.iterator();
		while (it.hasNext()) {
			Item i = it.next();
			ItemBase ib = i.getItemBase();
			if (ib != null)
				this.equipWeight += ib.getWeight();
		}
	}

	public void calculateInventoryWeight() {
		this.inventoryWeight = 0;
		for (Item i : this.inventory) {
			ItemBase ib = i.getItemBase();
			if (ib != null)
				this.inventoryWeight += ib.getWeight();
		}
	}

	public void calculateVaultWeight() {
		this.vaultWeight = 0;
		for (Item i : this.vault) {
			ItemBase ib = i.getItemBase();
			if (ib != null)
				this.vaultWeight += ib.getWeight();
		}
	}

	private int calculateTradingForWeight() {
		int tradingForWeight = 0;
		
		return tradingForWeight;
	}


	public void updateInventory(Item item, boolean add) {
		ArrayList<Item> list = new ArrayList<>();
		list.add(item);
		updateInventory(list, add);
	}

	private void updateInventory(ArrayList<Item> inventory, boolean add) {

		if (this.absCharacter == null)
			return;

		if (this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter) == false)
			return;

		PlayerCharacter pc = (PlayerCharacter) this.absCharacter;

		UpdateInventoryMsg updateInventoryMsg = new UpdateInventoryMsg(inventory, this.getBank(), this.getGoldInventory(), add);
		Dispatch dispatch = Dispatch.borrow(pc, updateInventoryMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

	}

	public void forceToInventory(int slot, Item item, PlayerCharacter pc, boolean initialized) {
		if (item == null || pc == null)
			return;

		if (!item.moveItemToInventory(pc)) {
			//TODO well why did this fail? clean it up
		}

		// remove it from other lists:
		this.remItemFromLists(item, (byte) slot);

		// add to Inventory
		this.inventory.add(item);
		item.addToCache();

		calculateWeights();

		//Update players with unequipped item
		if (initialized) {
			TransferItemFromEquipToInventoryMsg back = new TransferItemFromEquipToInventoryMsg(pc, slot);
			DispatchMessage.dispatchMsgToInterestArea(pc, back,  engine.Enum.DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
		}

	}

	/**
	 * Update the player's inventory window by resending the entire contents.
	 */
	public void updateInventory() {
		this.updateInventory(this.getInventory(), true);
	}

	public synchronized void initializeTrade() {
		this.trading = new HashSet<>();
	}

	public synchronized boolean commitTrade() {
		int goldFrom1 = 0;
		int goldFrom2 = 0;

		if (this.getTradingWith() == null || this.getTradingWith().isConnected() == false
				|| this.getTradingWith().getPlayerCharacter() == null){
			this.endTrade();
			return false;
		}
			
		
		CharacterItemManager tradingWith = this.getTradingWith().getPlayerCharacter().getCharItemManager();
		
		if (tradingWith == null)
			return false;
		
		if (this.goldTradingAmount != 0) {
			
			if (tradingWith.goldInventory == null){
				Logger.error("Null Gold for player " + this.getOwner().getObjectUUID());
				return false;
			}
			goldFrom1 = this.goldTradingAmount;
		}
		if (tradingWith.goldTradingAmount != 0) {
			
			if (this.getGoldInventory() == null){
				Logger.error("Null Gold for player " + this.getOwner().getObjectUUID());
				return false;
			}
			goldFrom2 = tradingWith.goldTradingAmount;
		}
		

		if (this.getGoldInventory().getNumOfItems() + goldFrom2 > 10000000){
			PlayerCharacter pc = (PlayerCharacter)this.absCharacter;
			if (pc.getClientConnection() != null)
				ErrorPopupMsg.sendErrorPopup(pc, 202);
			return false;
		}
		
		
		if (tradingWith.getGoldInventory().getNumOfItems() + goldFrom1 > 10000000){
			PlayerCharacter pc = (PlayerCharacter)tradingWith.absCharacter;
			if (pc.getClientConnection() != null)
				ErrorPopupMsg.sendErrorPopup(pc, 202);
			return false;
		}

		if (this.trading.size() > 0 || tradingWith.trading.size() > 0 || goldFrom1 > 0 || goldFrom2 > 0) {
			if (!DbManager.ItemQueries.DO_TRADE(this.trading, tradingWith.trading, this, tradingWith,
					this.goldInventory, tradingWith.goldInventory, goldFrom1, goldFrom2))
				return false;
		} else
			return true;

		for (int i : this.trading) {
			Item item = Item.getFromCache(i);
			if (item == null)
				continue;
			this.trade(item);
			tradingWith.tradeForItem(item);
		}
		for (int i : tradingWith.trading) {
			Item item = Item.getFromCache(i);
			if (item == null)
				continue;
			tradingWith.trade(item);
			this.tradeForItem(item);
		}
		
		//subtract gold your trading from your inventory.
		if (this.goldTradingAmount > 0)
			this.getGoldInventory().setNumOfItems(this.getGoldInventory().getNumOfItems() - this.goldTradingAmount);
		//subtract gold your trading from your inventory.
		if (tradingWith.goldTradingAmount > 0)
			tradingWith.getGoldInventory().setNumOfItems(tradingWith.getGoldInventory().getNumOfItems() - tradingWith.goldTradingAmount);
		
		if (tradingWith.goldTradingAmount > 0)
			this.getGoldInventory().setNumOfItems(this.goldInventory.getNumOfItems()
					+ tradingWith.goldTradingAmount);
		if (this.goldTradingAmount > 0)
			tradingWith.getGoldInventory().setNumOfItems(tradingWith.goldInventory.getNumOfItems()
					+ this.goldTradingAmount);

		this.tradeSuccess = true;
		tradingWith.tradeSuccess = true;
		
		return true;
		
	}

	public synchronized void endTrade() {
		updateInventory(this.getInventory(), true);
		this.tradeCommitted = (byte) 0;
		this.tradeSuccess = false;
		this.tradingWith = null;
		this.trading = null;
		this.goldTradingAmount = 0;
		this.tradeID = 0;
	}

	public synchronized void endTrade(boolean fromDeath) {
		this.tradeCommitted = (byte) 0;
		this.tradeSuccess = false;
		this.tradingWith = null;
		this.trading = null;
		this.goldTradingAmount = 0;
	}

	// Remove item from your possession
	private synchronized boolean trade(Item i) {
		if (this.doesCharOwnThisItem(i.getObjectUUID()) == false)
			return false;

		// Only valid from inventory
		if (!inventoryContains(i))
			return false;

		// remove from Inventory
		this.inventory.remove(i);
		this.itemIDtoType.remove(i.getObjectUUID());
		i.setOwnerID(0);

		calculateWeights();

		return true;
	}

	//Damage an equipped item a specified amount
	public void damageItem(Item item, int amount) {
		if (item == null || amount < 1 || amount > 5)
			return;

		//verify the item is equipped by this player
		int slot = item.getEquipSlot();
		if (!this.equipped.containsKey(slot))
			return;
		Item verify = this.equipped.get(slot);
		if (verify == null || item.getObjectUUID() != verify.getObjectUUID())
			return;

		//don't damage noob gear, hair or beards.
		if (item.getDurabilityMax() == 0)
			return;

		if (!item.isCanDestroy())
			return;

		int dur = (int) item.getDurabilityCurrent();
		if (dur - amount <= 0) {
			//destroy the item
			junk(item);

			//TODO remove item from the client
			//This may not be correct
			dur = 0;
		} else {
			dur -= amount;
			if (!DbManager.ItemQueries.SET_DURABILITY(item, dur))
				return;
			item.setDurabilityCurrent((short) dur);

		}

		if (this.absCharacter.getObjectType().equals(GameObjectType.PlayerCharacter) == false)
			return;

		//send damage item msg to client
		PlayerCharacter pc = (PlayerCharacter) this.absCharacter;

		ItemHealthUpdateMsg itemHealthUpdateMsg = new ItemHealthUpdateMsg(slot, (float) dur);
		Dispatch dispatch = Dispatch.borrow(pc, itemHealthUpdateMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

	}

	//Damage a random piece of armor a specified amount
	public void damageRandomArmor(int amount) {
		ArrayList<Item> armor = new ArrayList<>();
		if (this.equipped.containsKey(MBServerStatics.SLOT_OFFHAND)) {
			Item item = this.equipped.get(MBServerStatics.SLOT_OFFHAND);
			ItemBase ib = item.getItemBase();
			if (ib.isShield())
				armor.add(item);
		}
		if (this.equipped.containsKey(MBServerStatics.SLOT_HELMET))
			armor.add(this.equipped.get(MBServerStatics.SLOT_HELMET));
		if (this.equipped.containsKey(MBServerStatics.SLOT_CHEST))
			armor.add(this.equipped.get(MBServerStatics.SLOT_CHEST));
		if (this.equipped.containsKey(MBServerStatics.SLOT_ARMS))
			armor.add(this.equipped.get(MBServerStatics.SLOT_ARMS));
		if (this.equipped.containsKey(MBServerStatics.SLOT_GLOVES))
			armor.add(this.equipped.get(MBServerStatics.SLOT_GLOVES));
		if (this.equipped.containsKey(MBServerStatics.SLOT_GLOVES))
			armor.add(this.equipped.get(MBServerStatics.SLOT_GLOVES));
		if (this.equipped.containsKey(MBServerStatics.SLOT_LEGGINGS))
			armor.add(this.equipped.get(MBServerStatics.SLOT_LEGGINGS));
		if (this.equipped.containsKey(MBServerStatics.SLOT_FEET))
			armor.add(this.equipped.get(MBServerStatics.SLOT_FEET));

		if (armor.isEmpty())
			return; //nothing to damage

		int roll = ThreadLocalRandom.current().nextInt(armor.size());
		damageItem(armor.get(roll), amount);
	}

	//Damage all equipped gear a random amount between 1 and 5
	public void damageAllGear() {
		for (Item gear : this.equipped.values()) {
			damageItem(gear, (ThreadLocalRandom.current().nextInt(5) + 1));
		}
	}

	// Add item to your possession
	public synchronized boolean tradeForItem(Item i) {
		// add to Inventory
		this.inventory.add(i);
		this.itemIDtoType.put(i.getObjectUUID(), i.getObjectType().ordinal());
        i.setOwnerID(this.absCharacter.getObjectUUID());

		calculateWeights();

		return true;
	}

	public synchronized boolean addGoldToTrade(int amount) {
		
		if (this.goldTradingAmount + amount > MBServerStatics.PLAYER_GOLD_LIMIT)
			return false;
		
		this.goldTradingAmount += amount;
		
		return true;
	}

	/**
	 * Completely empties inventory, deleting any items. Use with caution!
	 */
	public synchronized void clearInventory() {
		this.getGoldInventory().setNumOfItems(0);
		Iterator<Item> ii = this.inventory.iterator();
		while (ii.hasNext()) {
			Item itm = ii.next();
			ii.remove();
			this.delete(itm);
		}
	}

	public synchronized void clearEquip() {

		ArrayList<Item> equipCopy = new ArrayList<>(this.getEquippedList());
		Iterator<Item> ii = equipCopy.iterator();
		while (ii.hasNext()) {
			Item itm = ii.next();
			this.getEquippedList().remove(itm);
			this.delete(itm);
		}
	}

	public byte getEquipVer() {
		return this.equipVer;
	}

	public static byte getInventoryVer() {
		return inventoryVer;
	}

	public static byte getBankVer() {
		return bankVer;
	}

	public static byte getVaultVer() {
		return vaultVer;
	}

	public void incEquipVer() {
		this.equipVer++;
	}

	public void incInventoryVer() {
		this.equipVer++;
	}

	public void incBankVer() {
		this.equipVer++;
	}

	public void incVaultVer() {
		this.equipVer++;
	}

	public static void takeFromNPC(NPC npc, PlayerCharacter pc, Item take, ClientMessagePump clientMessagePump) {
		ItemBase ib = take.getItemBase();
		if (ib == null)
			return;
		CharacterItemManager itemMan = pc.getCharItemManager();
		if (itemMan == null)
			return;
		CharacterItemManager npcCim = npc.getCharItemManager();
		if (npcCim == null)
			return;
		if (!npcCim.inventoryContains(take)) {
			return;
		}

		if (!itemMan.hasRoomInventory(ib.getWeight()))
			return;
		if (take != null) {
			itemMan.buyFromNPC(take, npc);
			itemMan.updateInventory();
		}
	}

	public int getTradeID() {
		return tradeID;
	}
	
	public synchronized boolean closeTradeWindow(){
		if (this.getTradingWith() != null || this.getTradeID() != 0)
			this.closeTradeWindow(new CloseTradeWindowMsg(this.getOwner(), this.getTradeID()), true);
		return true;

	}

}
