// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;


import engine.Enum;
import engine.Enum.GameObjectType;
import engine.Enum.ItemType;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.ItemProductionMsg;
import engine.net.client.msg.ManageNPCMsg;
import engine.objects.*;
import org.pmw.tinylog.Logger;

import java.util.HashMap;

/*
 * @Summary: Processes application protocol message which modifies
 * hireling inventory through rolling, junking or depositing.
 */
public class ItemProductionMsgHandler extends AbstractClientMsgHandler {

	private static final int ACTION_PRODUCE = 1;
	private static final int ACTION_JUNK = 2;
	private static final int ACTION_RECYCLE = 3;
	private static final int ACTION_COMPLETE = 4;
	private static final int ACTION_DEPOSIT = 6;
	private static final int ACTION_SETPRICE = 5;
	private static final int ACTION_TAKE = 7;
	private static final int ACTION_CONFIRM_SETPRICE = 9;  // Unsure. Sent by client
	private static final int ACTION_CONFIRM_DEPOSIT = 10;  // Unsure. Sent by client
	private static final int ACTION_CONFIRM_TAKE = 11;     // Unsure. Sent by client

	public ItemProductionMsgHandler() {
		super(ItemProductionMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		PlayerCharacter player;
		NPC vendorNPC;
		ItemProductionMsg msg;
		Dispatch dispatch;

		// Member variable assignment

		msg = (ItemProductionMsg) baseMsg;
		player = origin.getPlayerCharacter();

		if (player == null)
			return true;

		// Grab reference to vendor we are interacting with

		vendorNPC = (NPC) DbManager.getObject(engine.Enum.GameObjectType.NPC, msg.getNpcUUID());

		// Oops?

		if (vendorNPC == null)
			return true;

		// Process Request

		switch (msg.getActionType()) {

		case ACTION_PRODUCE:
			boolean isRandom = false;
			if (msg.getUnknown03() != 0 && msg.getpToken() == 0 && msg.getsToken() == 0)
				isRandom = true;
			//Create Multiple Item Function.. Fill all empty slots
			if (msg.isMultiple()){
				int emptySlots = vendorNPC.getRank() - vendorNPC.getRolling().size();
				if (emptySlots > 0){
					for (int i = 0;i<emptySlots;i++){
						vendorNPC.produceItem(player.getObjectUUID(),msg.getTotalProduction(),isRandom,msg.getpToken(),msg.getsToken(),msg.getName(),msg.getItemUUID());
					}
				}
			}else
				vendorNPC.produceItem(player.getObjectUUID(),msg.getTotalProduction(),isRandom,msg.getpToken(),msg.getsToken(),msg.getName(),msg.getItemUUID());
			break;
		case ACTION_JUNK:
			junkItem(msg.getItemUUID(), vendorNPC, origin);
			break;
		case ACTION_RECYCLE:
			recycleItem(msg.getItemIDtoTypeMap(), vendorNPC, origin);
			msg.setActionType(7);
			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;
		case ACTION_COMPLETE:

			vendorNPC.completeItem(msg.getItemUUID());

			//			ManageNPCMsg outMsg = new ManageNPCMsg(vendorNPC);
			//			outMsg.setMessageType(1);
			//
			//			dispatch = Dispatch.borrow(player, outMsg);
			//			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);






			break;
		case ACTION_DEPOSIT:
			depositItem(msg.getItemUUID(), vendorNPC, origin);
			break;
		case ACTION_SETPRICE:
			setItemPrice(msg.getItemType(),msg.getItemUUID(), msg.getItemPrice(), vendorNPC, origin);
			break;
		case ACTION_TAKE:
			takeItem(msg.getItemIDtoTypeMap(), vendorNPC, origin);
			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;

		}
		return true;
	}

	// Method sets the price on an item in the vendor inventory

	private static void setItemPrice(int itemType, int itemUUID, int itemPrice, NPC vendor, ClientConnection origin) {

		Item targetItem;
		ItemProductionMsg outMsg;
		Dispatch dispatch;

		PlayerCharacter player = origin.getPlayerCharacter();

		if (player == null)
			return;

		if (itemType == GameObjectType.Item.ordinal())
			targetItem = Item.getFromCache(itemUUID);
		else if (itemType == GameObjectType.MobLoot.ordinal())
			targetItem = MobLoot.getFromCache(itemUUID);
		else
			targetItem = null;


		if (targetItem == null)
			return;

		if (targetItem.getObjectType() == GameObjectType.Item){
			if (!DbManager.ItemQueries.UPDATE_VALUE(targetItem, itemPrice)) {
				ChatManager.chatInfoError(origin.getPlayerCharacter(), "Failed to set price! Contact CCR For help.");
				return;
			}
			targetItem.setValue(itemPrice);
			outMsg = new ItemProductionMsg(vendor.getBuilding(), vendor, targetItem, ACTION_DEPOSIT, true);
			dispatch = Dispatch.borrow(player, outMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

			outMsg = new ItemProductionMsg(vendor.getBuilding(), vendor, targetItem, ACTION_SETPRICE, true);
			dispatch = Dispatch.borrow(player, outMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		}else if (targetItem.getObjectType() == GameObjectType.MobLoot){
			MobLoot mobLoot = (MobLoot)targetItem;
			if (!DbManager.NPCQueries.UPDATE_ITEM_PRICE(mobLoot.getObjectUUID(), vendor.getObjectUUID(), itemPrice)) {
				ChatManager.chatInfoError(origin.getPlayerCharacter(), "Failed to set price! Contact CCR For help.");
				return;
			}
			targetItem.setValue(itemPrice);
			outMsg = new ItemProductionMsg(vendor.getBuilding(), vendor, targetItem, ACTION_DEPOSIT, true);
			dispatch = Dispatch.borrow(player, outMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

			outMsg = new ItemProductionMsg(vendor.getBuilding(), vendor, targetItem, ACTION_SETPRICE, true);
			dispatch = Dispatch.borrow(player, outMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		}

		// Set item's price


	}

	// Method adds an item from the players inventory to the vendor.

	private static void depositItem(int itemUUID, NPC vendor, ClientConnection origin) {

		Item targetItem;
		ItemProductionMsg outMsg;
		CharacterItemManager itemMan;
		Dispatch dispatch;

		PlayerCharacter player = origin.getPlayerCharacter();

		if (player == null)
			return;

		if (origin.sellLock.tryLock()) {
			try {
				targetItem = Item.getFromCache(itemUUID);

				if (targetItem == null)
					return;

				if (targetItem.getItemBase().getType() == ItemType.GOLD)
					return;

				if (!vendor.getCharItemManager().hasRoomInventory(targetItem.getItemBase().getWeight())){

					ErrorPopupMsg.sendErrorPopup(player, 21);
					return;
				}

				itemMan = origin.getPlayerCharacter().getCharItemManager();

				if (itemMan == null)
					return;

				if (vendor.getCharItemManager().getInventoryWeight() > 500) {
					ErrorPopupMsg.sendErrorPopup(player, 21);
					return;
				}

				if (!targetItem.validForInventory(origin, player, itemMan)){
					ErrorPopupMsg.sendErrorPopup(player, 19);
					return;
				}

				// Transfer item from player to vendor's inventory

				if (!itemMan.sellToNPC(targetItem, vendor)){
					ErrorPopupMsg.sendErrorPopup(player, 109);
					return;
				}


				outMsg = new ItemProductionMsg(vendor.getBuilding(), vendor, targetItem, ACTION_DEPOSIT, true);
				dispatch = Dispatch.borrow(player, outMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

				outMsg = new ItemProductionMsg(vendor.getBuilding(), vendor, targetItem, ACTION_CONFIRM_DEPOSIT, true);
				dispatch = Dispatch.borrow(player, outMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

				origin.getPlayerCharacter().getCharItemManager().updateInventory();
			}catch (Exception e){
				Logger.error(e);
			}finally {
				origin.sellLock.unlock();
			}
				
			}
		


	}

	// Method completes an item that has been previously rolled
	// adding it to the NPC's inventory

	private static void completeItem(int itemUUID, NPC vendor, ClientConnection origin, ItemProductionMsg msg) {

		Item targetItem;
		ManageNPCMsg outMsg;
		Dispatch dispatch;

		PlayerCharacter player = origin.getPlayerCharacter();

		if (player == null)
			return;

		if (origin.buyLock.tryLock()) {
			try {
				targetItem = Item.getFromCache(itemUUID);

				if (targetItem == null)
					return;


				if (!vendor.getCharItemManager().forgeContains(targetItem, vendor))
					return;

				boolean worked = DbManager.ItemQueries.UPDATE_FORGE_TO_INVENTORY(targetItem);
				if (!worked) {
					Guild guild = vendor.getGuild();
					if (guild == null)
						return;
					//ChatManager.chatGuildInfo(guild, "Failed to complete Item " + targetItem.getName());
					return;
				}

				targetItem.containerType = Enum.ItemContainerType.INVENTORY;
				targetItem.setOwner(vendor);
				vendor.getCharItemManager().addItemToInventory(targetItem);

				vendor.removeItemFromForge(targetItem);

				outMsg = new ManageNPCMsg(vendor);
				outMsg.setMessageType(ACTION_PRODUCE);
				dispatch = Dispatch.borrow(player, outMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			} finally {
				origin.buyLock.unlock();
			}
		}
	}

	// Method handles recycling of an item

	private static void recycleItem(HashMap<Integer, Integer> itemList, NPC vendor, ClientConnection origin) {

		Item targetItem;
		ItemProductionMsg outMsg;
		int totalValue = 0;
		int currentStrongbox;
		Dispatch dispatch;

		if (vendor.getBuilding() == null)
			return;

		PlayerCharacter player = origin.getPlayerCharacter();

		if (player == null)
			return;

		if (itemList == null)
			return;
		
		if (origin.sellLock.tryLock()) {
			try {



				for (int itemUUID : itemList.keySet()) {
					int itemValue = 0;

					int type = itemList.get(itemUUID);

					if (type == GameObjectType.Item.ordinal())
						targetItem = Item.getFromCache(itemUUID);
					else
						targetItem = MobLoot.getFromCache(itemUUID);

					if (targetItem == null)
						continue;

					if (targetItem.getItemBase().getType() == ItemType.GOLD)
						return;

					if (!vendor.getCharItemManager().doesCharOwnThisItem(targetItem.getObjectUUID()))
						continue;
					if (vendor.getCharItemManager().inventoryContains(targetItem) == false)
						continue;

					itemValue = targetItem.getBaseValue();

					if (vendor.getBuilding().getStrongboxValue() + itemValue > vendor.getBuilding().getMaxGold()) {
						ErrorPopupMsg.sendErrorPopup(player, 201);
						break;
					}

					switch (targetItem.getItemBase().getType()) {
					case CONTRACT:
					case GUILDCHARTER:
					case DEED:
					case REALMCHARTER:
					case SCROLL:
					case TEARS:
						itemValue = 0;
						continue;
					}
					totalValue += itemValue;
					long start = System.currentTimeMillis();
					vendor.getCharItemManager().recycle(targetItem);
					long end = System.currentTimeMillis();
					long timetook = end - start;

					//					ChatManager.chatSystemInfo(player, "Took " + timetook + " ms to finish");

					outMsg = new ItemProductionMsg(vendor.getBuilding(), vendor, targetItem, ACTION_TAKE, true);

					dispatch = Dispatch.borrow(origin.getPlayerCharacter(), outMsg);
					DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				}

				// Refund a portion of the gold

				if (!vendor.getBuilding().transferGold(totalValue,false))
				return;
				
				

		}catch (Exception e){
			Logger.error(e);
		}finally {
		
			origin.sellLock.unlock();
		}

		}

		// Refresh vendor's inventory to client

	}

	// Method junks an item that has been rolled but not completed

	private static void junkItem(int itemUUID, NPC vendor, ClientConnection origin) {

		MobLoot targetItem;
		ManageNPCMsg outMsg;
		Dispatch dispatch;

		if (origin.sellLock.tryLock()) {
			try {
				targetItem = MobLoot.getFromCache(itemUUID);

				PlayerCharacter player = origin.getPlayerCharacter();

				if (player == null)
					return;

				// Can't junk nothing!

				if (targetItem == null)
					return;



				if (!vendor.getCharItemManager().forgeContains(targetItem, vendor))
					return;

				// Cannot junk items without a forge!

				if (vendor.getBuilding() == null)
					return;

				// Delete the item and cancel any pending rolling timer jobs

				targetItem.recycle(vendor);
				vendor.removeItemFromForge(targetItem);

				// Refresh vendor's inventory to client

				outMsg = new ManageNPCMsg(vendor);
				outMsg.setMessageType(1);
				dispatch = Dispatch.borrow(player, outMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				;
			} finally {
				origin.sellLock.unlock();
			}
		}

	}

	// Method removes item from an NPC's inventory and transferes it to a player

	private static void takeItem(HashMap<Integer, Integer> itemList, NPC vendor, ClientConnection origin) {

		Item targetItem;


		PlayerCharacter player = origin.getPlayerCharacter();

		if (player == null)
			return;



		for (int itemUUID : itemList.keySet()) {

			int type = itemList.get(itemUUID);
			if (type == GameObjectType.Item.ordinal()){
				targetItem = Item.getFromCache(itemUUID);

			}
			else{
				targetItem = MobLoot.getFromCache(itemUUID);

			}

			if (targetItem == null)
				return;


			if (targetItem.getItemBase().getType() == ItemType.GOLD)
				return;
			if (vendor.getCharItemManager().inventoryContains(targetItem) == false)
				return;

			if (player.getCharItemManager().hasRoomInventory(targetItem.getItemBase().getWeight()) == false)
				return;

			player.getCharItemManager().buyFromNPC(targetItem, vendor);

		}

		player.getCharItemManager().updateInventory();

		// Update NPC inventory to client


	}

	// Method handles rolling item requests from the client

}
