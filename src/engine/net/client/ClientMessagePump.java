// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.net.client;

import engine.Enum.*;
import engine.InterestManagement.WorldGrid;
import engine.ai.MobileFSM.STATE;
import engine.exception.MsgSendException;
import engine.gameManager.*;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.RefreshGroupJob;
import engine.jobs.StuckJob;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.NetMsgHandler;
import engine.net.client.handlers.AbstractClientMsgHandler;
import engine.net.client.msg.*;
import engine.net.client.msg.chat.AbstractChatMsg;
import engine.net.client.msg.commands.ClientAdminCommandMsg;
import engine.objects.*;
import engine.powers.effectmodifiers.AbstractEffectModifier;
import engine.server.MBServerStatics;
import engine.server.world.WorldServer;
import engine.session.Session;
import engine.util.StringUtils;
import org.pmw.tinylog.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static engine.math.FastMath.sqr;

/**
 * @author:
 * @summary: This class is the mainline router for application protocol
 * messages received by the client.
 */

public class ClientMessagePump implements NetMsgHandler {

	// Instance variable declaration

	private final WorldServer server;

	public ClientMessagePump(WorldServer server) {
		super();
		this.server = server;
	}

	/*
	 *  Incoming client protocol message are processed here
	 */

	@Override
	public boolean handleClientMsg(ClientNetMsg msg) {

		if (msg == null) {
			Logger.error("handleClientMsg", "Recieved null msg. Returning.");
			return false;
		}

		ClientConnection origin;
		Protocol protocolMsg = Protocol.NONE;
		Session s;

		try {

			// Try registered opcodes first as we take a hatchet to this GodObject

			AbstractClientMsgHandler msgHandler = msg.getProtocolMsg().handler;

			if (msgHandler != null)
				return msgHandler.handleNetMsg(msg);

			// Any remaining opcodes fall through and are routed
			// through this ungodly switch of doom.

			origin = (ClientConnection) msg.getOrigin();
			s = SessionManager.getSession(origin);

			protocolMsg = msg.getProtocolMsg();

			switch (protocolMsg) {
				case SETSELECTEDOBECT:
				ClientMessagePump.targetObject((TargetObjectMsg) msg, origin);
				break;
				case CITYDATA:
				ClientMessagePump.MapData(s, origin);
				break;

				/*
				 * Chat
				 */

				// Simplify by fall through. Route in ChatManager
				case CHATSAY:
				case CHATSHOUT:
				case CHATTELL:
				case CHATGUILD:
				case CHATGROUP:
				case CHATPVP:
				case CHATIC:
				case CHATCITY:
				case CHATINFO:
				case SYSTEMBROADCASTCHANNEL:
				case CHATCSR:
			case SYSTEMCHANNEL:
			case GLOBALCHANNELMESSAGE:
			case LEADERCHANNELMESSAGE:
				ChatManager.handleChatMsg(s, (AbstractChatMsg) msg);
				break;
			case UPDATESTATE:
				UpdateStateMsg rwss = (UpdateStateMsg) msg;
				runWalkSitStand(rwss, origin);
				break;
			case ACTIVATECHARTER:
				UseCharterMsg ucm = (UseCharterMsg) msg;
				ucm.setUnknown02(1);
				ucm.configure();
				Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), ucm);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
				break;
			case CHECKUNIQUEGUILD:
				break;
			case CREATEPETITION:
				break;
			case CANCELGUILDCREATION:
				break;
			case LEAVEREQUEST:
				origin.disconnect();
				break;
			case POWER:
				PowersManager.usePower((PerformActionMsg) msg, origin, false);
				break;
			case REQUESTMELEEATTACK:
				CombatManager.setAttackTarget((AttackCmdMsg) msg, origin);
				break;
			case READYTOENTER:
				break;
			case OPENVAULT:
				break;
			case WHOREQUEST:
				WhoRequest((WhoRequestMsg) msg, origin);
				break;
			case CLIENTADMINCOMMAND:
				ChatManager.HandleClientAdminCmd((ClientAdminCommandMsg) msg, origin);
				break;
			case SOCIALCHANNEL:
				social((SocialMsg) msg, origin);
				break;
			case COMBATMODE:
				CombatManager.toggleCombat((ToggleCombatMsg) msg, origin);
				break;
			case ARCCOMBATMODEATTACKING:
				CombatManager.toggleCombat((SetCombatModeMsg) msg, origin);
				break;
			case MODIFYGUILDSTATE:
				ToggleLfgRecruitingMsg tlrm = (ToggleLfgRecruitingMsg) msg;
				toggleLfgRecruiting(tlrm, origin);
				break;
			case TOGGLESITSTAND:
				ToggleSitStandMsg tssm = (ToggleSitStandMsg) msg;
				toggleSitStand(tssm, origin);
				break;
			case GUILDTREESTATUS:
				GuildTreeStatusMsg((GuildTreeStatusMsg) msg, origin);
				break;
			case CUSTOMERPETITION:
				Logger.info("CCR Petition received: " + msg.toString());
				// TODO need to send something back to client
				// TODO what to do with petition?
				break;
			case IGNORE:
				((IgnoreMsg) msg).handleRequest(origin);
				break;
			case UNEQUIP:
				TransferItemFromEquipToInventory((TransferItemFromEquipToInventoryMsg) msg, origin);
				break;
			case EQUIP:
				TransferItemFromInventoryToEquip((TransferItemFromInventoryToEquipMsg) msg, origin);
				break;
			case DELETEOBJECT:
				DeleteItem((DeleteItemMsg) msg, origin);
				break;
			case VIEWRESOURCES:
				ViewResourcesMessage((ViewResourcesMessage) msg, origin);
				break;
			case RAISEATTR:
				modifyStat((ModifyStatMsg) msg, origin);
				break;
			case COSTTOOPENBANK:
				ackBankWindowOpened((AckBankWindowOpenedMsg) msg, origin);
				break;
			case RESETAFTERDEATH:
				respawn((RespawnMsg) msg, origin);
				break;
			case REQUESTCONTENTS:
				lootWindowRequest((LootWindowRequestMsg) msg, origin);
				break;
			case MOVEOBJECTTOCONTAINER:
				loot((LootMsg) msg, origin);
				break;
			case SHOWCOMBATINFO:
				show((ShowMsg) msg, origin);
				break;
			case TRANSFERITEMTOBANK:
				transferItemFromInventoryToBank((TransferItemFromInventoryToBankMsg) msg, origin);
				break;
			case TRANSFERITEMFROMBANK:
				transferItemFromBankToInventory((TransferItemFromBankToInventoryMsg) msg, origin);
				break;
			case TRANSFERITEMFROMVAULTTOINVENTORY:
				transferItemFromVaultToInventory((TransferItemFromVaultToInventoryMsg) msg, origin);
				break;
			case ITEMTOVAULT:
				transferItemFromInventoryToVault((TransferItemFromInventoryToVaultMsg) msg, origin);
				break;
			case TRANSFERGOLDFROMVAULTTOINVENTORY:
				transferGoldFromVaultToInventory((TransferGoldFromVaultToInventoryMsg) msg, origin);
				break;
			case GOLDTOVAULT:
				transferGoldFromInventoryToVault((TransferGoldFromInventoryToVaultMsg) msg, origin);
				break;
			case REQUESTTOTRADE:
				TradeManager.tradeRequest((TradeRequestMsg) msg, origin);
				break;
			case REQUESTTRADEOK:
				TradeManager.acceptTradeRequest((AcceptTradeRequestMsg) msg, origin);
				break;
			case REQUESTTRADECANCEL:
				TradeManager.rejectTradeRequest((RejectTradeRequestMsg) msg, origin);
				break;
			case TRADEADDOBJECT:
				TradeManager.addItemToTradeWindow((AddItemToTradeWindowMsg) msg, origin);
				break;
			case TRADEADDGOLD:
				TradeManager.addGoldToTradeWindow((AddGoldToTradeWindowMsg) msg, origin);
				break;
			case TRADECONFIRM:
				TradeManager.commitToTrade((CommitToTradeMsg) msg, origin);
				break;
			case TRADEUNCONFIRM:
				TradeManager.uncommitToTrade((UncommitToTradeMsg) msg, origin);
				break;
			case TRADECLOSE:
				TradeManager.closeTradeWindow((CloseTradeWindowMsg) msg, origin);
				break;
			case ARCREQUESTTRADEBUSY:
				TradeManager.invalidTradeRequest((InvalidTradeRequestMsg) msg);
				break;
			case VENDORDIALOG:
				VendorDialogMsg.replyDialog((VendorDialogMsg) msg, origin);
				break;
			case SHOPLIST:
				openBuyFromNPCWindow((BuyFromNPCWindowMsg) msg, origin);
				break;
			case BUYFROMNPC:
				buyFromNPC((BuyFromNPCMsg) msg, origin);
				break;
			case SHOPINFO:
				openSellToNPCWindow((SellToNPCWindowMsg) msg, origin);
				break;
			case SELLOBJECT:
				sellToNPC((SellToNPCMsg) msg, origin);
				break;
			case REPAIROBJECT:
				Repair((RepairMsg) msg, origin);
				break;
			case TRAINERLIST:
				WorldServer.trainerInfo((TrainerInfoMsg) msg, origin);
				break;
			case ARCUNTRAINLIST:
				WorldServer.refinerScreen((RefinerScreenMsg) msg, origin);
				break;
			case TRAINSKILL:
				TrainMsg.train((TrainMsg) msg, origin);
				break;
			case ARCUNTRAINABILITY:
				RefineMsg.refine((RefineMsg) msg, origin);
				break;
			case POWERTARGNAME:
				PowersManager.summon((SendSummonsRequestMsg) msg, origin);
				break;
			case ARCSUMMON:
				PowersManager.recvSummon((RecvSummonsRequestMsg) msg, origin);
				break;
			case ARCTRACKINGLIST:
				PowersManager.trackWindow((TrackWindowMsg) msg, origin);
				break;
			case STUCK:
				stuck(origin);
				break;
			case RANDOM:
				ClientMessagePump.randomRoll((RandomMsg) msg, origin);
				break;
			case ARCPETATTACK:
				petAttack((PetAttackMsg) msg, origin);
				break;
			case ARCPETCMD:
				petCmd((PetCmdMsg) msg, origin);
				break;
			case MANAGENPC:
				ManageNPCCmd((ManageNPCMsg) msg, origin);
				break;
			case ARCPROMPTRECALL:
				HandlePromptRecall((PromptRecallMsg) msg, origin);
				break;
			case CHANNELMUTE:
				break;
			case KEEPALIVESERVERCLIENT:
				break;
			case UNKNOWN:
				break;
				
			case CONFIRMPROMOTE:
				break;

			default:
				String ocHex = StringUtils.toHexString(protocolMsg.opcode);
				Logger.error("Cannot handle Opcode: " + ocHex + " " + protocolMsg.name());
				return false;

			}

		} catch (MsgSendException | SQLException e) {
			Logger.error("handler for " + protocolMsg + " failed:  " + e);
			return false;
		}

		return true;
	}

	// *** Refactor need to figure this out.
	//     Commented out for some reson or another.

	//TODO what is this used for?
	private void ManageNPCCmd(ManageNPCMsg msg, ClientConnection origin) {

	}

	private static void MapData(Session s, ClientConnection origin) {

		Dispatch dispatch;
try{
	

		if (s == null || origin == null)
			return;

		PlayerCharacter pc = s.getPlayerCharacter();

		if (pc == null)
			return;
boolean updateMine = false;
boolean updateCity = false;

//do not update Cities and mines everytime you open map. only update them to client when something's changed.
		long lastRefresh = pc.getTimeStamp("mineupdate");
		if (lastRefresh <= Mine.getLastChange()){
			pc.setTimeStamp("mineupdate", System.currentTimeMillis());
			updateMine = true;
		}
				long lastCityRefresh = pc.getTimeStamp("cityUpdate");
				if (lastCityRefresh <= City.lastCityUpdate){
					pc.setTimeStamp("cityUpdate", System.currentTimeMillis());
					updateCity = true;
				}
					
				
		
		WorldObjectMsg wom = new WorldObjectMsg(s, false);
		wom.updateMines(true);
		wom.updateCities(updateCity);
		dispatch = Dispatch.borrow(pc, wom);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		//	}

		lastRefresh = pc.getTimeStamp("hotzoneupdate");
		if (lastRefresh <= WorldServer.getLastHZChange()) {
			Zone hotzone = ZoneManager.getHotZone();
			if (hotzone != null) {
				HotzoneChangeMsg hcm = new HotzoneChangeMsg(hotzone.getObjectType().ordinal(), hotzone.getObjectUUID());
				dispatch = Dispatch.borrow(pc, hcm);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
				pc.setTimeStamp("hotzoneupdate", System.currentTimeMillis() - 100);
			}
		}

		WorldRealmMsg wrm = new WorldRealmMsg();
		dispatch = Dispatch.borrow(pc, wrm);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
}catch(Exception e){
	e.printStackTrace();
}
	}

	private static void WhoRequest(WhoRequestMsg msg, ClientConnection origin) {

		// Handle /who request
		PlayerCharacter pc = origin.getPlayerCharacter();

		if (pc == null)
			return;

		if (pc.getTimeStamp("WHO") > System.currentTimeMillis()) {
			ErrorPopupMsg.sendErrorMsg(pc, "Who too fast! Please wait 3 seconds.");
			return;
		}

		WhoResponseMsg.HandleResponse(msg.getSet(), msg.getFilterType(), msg.getFilter(), origin);
		pc.getTimestamps().put("WHO", System.currentTimeMillis() + 3000);
	}

	private static void runWalkSitStand(UpdateStateMsg msg, ClientConnection origin) throws MsgSendException {
		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);
		if (pc == null)
			return;
		
		pc.update();
		if (msg.getSpeed() == 2)
			pc.setWalkMode(false);
		else
			pc.setWalkMode(true);
		DispatchMessage.dispatchMsgToInterestArea(pc, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
	}

	private static void toggleLfgRecruiting(ToggleLfgRecruitingMsg msg, ClientConnection origin) throws MsgSendException {
		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);
		if (pc == null)
			return;
		int num = msg.toggleLfgRecruiting();
		if (num == 1)
			pc.toggleLFGroup();
		else if (num == 2)
			pc.toggleLFGuild();
		else if (num == 3)
			pc.toggleRecruiting();
		UpdateStateMsg rwss = new UpdateStateMsg();
		rwss.setPlayer(pc);
		DispatchMessage.dispatchMsgToInterestArea(pc, rwss, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
	}

	private static void toggleSitStand(ToggleSitStandMsg msg, ClientConnection origin) throws MsgSendException {
		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);
		if (pc == null)
			return;
		
		pc.update();

		pc.setSit(msg.toggleSitStand());

		// cancel effects that break on sit
		if (pc.isSit()) {
			pc.setCombat(false);
			pc.cancelOnSit();
		}

		UpdateStateMsg rwss = new UpdateStateMsg();
		if (pc.isSit()) {
			pc.setCombat(false);
			rwss.setAware(1);
		}
		rwss.setPlayer(pc);

		DispatchMessage.dispatchMsgToInterestArea(pc, rwss, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
	}

	private static void targetObject(TargetObjectMsg msg, ClientConnection origin) {
		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);
		if (pc == null)
			return;

		// TODO improve this later. hacky way to make sure player ingame is
		// active.

		if (!pc.isActive())
			pc.setActive(true);

		pc.setLastTarget(GameObjectType.values()[msg.getTargetType()], msg.getTargetID());
	}

	private static void social(SocialMsg msg, ClientConnection origin) throws MsgSendException {
		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);
		if (pc == null)
			return;
		DispatchMessage.dispatchMsgToInterestArea(pc, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, true);
	}

	private static void TransferItemFromEquipToInventory(TransferItemFromEquipToInventoryMsg msg, ClientConnection origin) {
		PlayerCharacter pc = origin.getPlayerCharacter();
		if (pc == null)
			return;

		CharacterItemManager itemManager = pc.getCharItemManager();
		if (itemManager == null)
			return;

		int slot = msg.getSlotNumber();

		Item i = itemManager.getItemFromEquipped(slot);
		if (i == null)
			return;

		if (!itemManager.doesCharOwnThisItem(i.getObjectUUID()))
			return;

		//dupe check
		if (!i.validForEquip(origin, pc, itemManager))
			return;

		if (i.containerType == ItemContainerType.EQUIPPED)
			itemManager.moveItemToInventory(i);

		int ItemType = i.getObjectType().ordinal();
		int ItemID = i.getObjectUUID();
		for (String name : i.getEffects().keySet()) {
			Effect eff = i.getEffects().get(name);
			if (eff == null)
				return;
			ApplyEffectMsg pum = new ApplyEffectMsg();
			pum.setEffectID(eff.getEffectToken());
			pum.setSourceType(pc.getObjectType().ordinal());
			pum.setSourceID(pc.getObjectUUID());
			pum.setTargetType(pc.getObjectType().ordinal());
			pum.setTargetID(pc.getObjectUUID());
			pum.setNumTrains(eff.getTrains());
			pum.setUnknown05(1);
			pum.setUnknown02(2);
			pum.setUnknown06((byte) 1);
			pum.setEffectSourceType(ItemType);
			pum.setEffectSourceID(ItemID);
			pum.setDuration(-1);


			DispatchMessage.dispatchMsgToInterestArea(pc, pum, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);;

		}
		// Update player formulas
		pc.applyBonuses();
		DispatchMessage.dispatchMsgToInterestArea(pc, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);

	}

	//call this if the transfer fails server side to kick the item back to inventory from equip
	private void forceTransferFromInventoryToEquip(TransferItemFromEquipToInventoryMsg msg, ClientConnection origin, String reason) {
		//TODO add this later
		//PATCHED CODEZZ
	}

	private static void TransferItemFromInventoryToEquip(TransferItemFromInventoryToEquipMsg msg, ClientConnection origin) {
		PlayerCharacter pc = origin.getPlayerCharacter();
		if (pc == null)
			return;

		CharacterItemManager itemManager = pc.getCharItemManager();
		if (itemManager == null) {
			forceTransferFromEquipToInventory(msg, origin, "Can't find your item manager");
			return;
		}

		int uuid = msg.getUUID();
		int slot = msg.getSlotNumber();
		//System.out.println("loading to slot: " + slot);

		Item i = itemManager.getItemByUUID(uuid);

		if (i == null) {
			forceTransferFromEquipToInventory(msg, origin, "Item not found in your item manager");
			return;
		}

		if (!itemManager.doesCharOwnThisItem(i.getObjectUUID())) {
			forceTransferFromEquipToInventory(msg, origin, "You do not own this item");
			return;
		}

		//dupe check
		if (!i.validForInventory(origin, pc, itemManager))
			return;

		if (i.containerType == ItemContainerType.INVENTORY) {
			if (!itemManager.equipItem(i, (byte) slot)) {
				forceTransferFromEquipToInventory(msg, origin, "Failed to transfer item.");
				return;
			}
		}
		else {
			forceTransferFromEquipToInventory(msg, origin, "This item is not in your inventory");
			return;
		}

		// Update player formulas
		pc.applyBonuses();
		DispatchMessage.dispatchMsgToInterestArea(pc, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);


		for (String name : i.getEffects().keySet()) {
			Effect eff = i.getEffects().get(name);
			if (eff == null)
				return;

			ApplyEffectMsg pum = new ApplyEffectMsg();
			pum.setEffectID(eff.getEffectToken());
			pum.setSourceType(pc.getObjectType().ordinal());
			pum.setSourceID(pc.getObjectUUID());
			pum.setTargetType(pc.getObjectType().ordinal());
			pum.setTargetID(pc.getObjectUUID());
			pum.setNumTrains(eff.getTrains());
			pum.setUnknown05(1);
			pum.setUnknown06((byte) 1);
			pum.setEffectSourceType(i.getObjectType().ordinal());
			pum.setEffectSourceID(i.getObjectUUID());
			pum.setDuration(-1);

			DispatchMessage.dispatchMsgToInterestArea(pc, pum, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);;
		}
	}

	//call this if the transfer fails server side to kick the item back to inventory from equip
	private static void forceTransferFromEquipToInventory(TransferItemFromInventoryToEquipMsg msg, ClientConnection origin, String reason) {

		PlayerCharacter pc = origin.getPlayerCharacter();

		if (pc == null)
			return;

		TransferItemFromEquipToInventoryMsg back = new TransferItemFromEquipToInventoryMsg(pc, msg.getSlotNumber());
		Dispatch dispatch = Dispatch.borrow(pc, back);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		ChatManager.chatInfoError(pc, "Can't equip item: " + reason);
	}

	public static Boolean NPCVaultBankRangeCheck(PlayerCharacter pc, ClientConnection origin, String bankorvault) {

		if (pc == null)
			return false;

		NPC npc = pc.getLastNPCDialog();

		if (npc == null)
			return false;

		// System.out.println(npc.getContract().getName());
		// last npc must be either a banker or vault keeper

		if (bankorvault.equals("vault")) {
			if (npc.getContract().getContractID() != 861)
				return false;
		}
		else
			// assuming banker

			if (!npc.getContract().getName().equals("Bursar"))
				return false;

		if (pc.getLoc().distanceSquared2D(npc.getLoc()) > MBServerStatics.NPC_TALK_RANGE * MBServerStatics.NPC_TALK_RANGE) {
			ErrorPopupMsg.sendErrorPopup(pc, 14);
			return false;
		} else
			return true;

	}

	private static void transferItemFromInventoryToBank(TransferItemFromInventoryToBankMsg msg, ClientConnection origin) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		if (!NPCVaultBankRangeCheck(player, origin, "bank"))
			return;

		CharacterItemManager itemManager = player.getCharItemManager();

		if (itemManager == null)
			return;

		if (itemManager.getBankWeight() > 500) {
			ErrorPopupMsg.sendErrorPopup(player, 21);
			return;
		}

		int uuid = msg.getUUID();

		Item item = itemManager.getItemByUUID(uuid);

		if (item == null)
			return;

		//dupe check  WTF CHECK BUT NO LOGGING?

		if (!item.validForInventory(origin, player, itemManager))
			return;

		if (item.containerType == ItemContainerType.INVENTORY && itemManager.isBankOpen())
			if (item.getItemBase().getType().equals(engine.Enum.ItemType.GOLD)) {
				if (!itemManager.moveGoldToBank(item, msg.getNumItems()))
					return;
				UpdateGoldMsg goldMes = new UpdateGoldMsg(player);
				goldMes.configure();

				dispatch = Dispatch.borrow(player, goldMes);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

			}
			else {

				if (!itemManager.hasRoomBank(item.getItemBase().getWeight()))
					return;

				if (!itemManager.moveItemToBank(item))
					return;

				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

			}
	}

	private static void transferItemFromBankToInventory(TransferItemFromBankToInventoryMsg msg, ClientConnection origin) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		if (!NPCVaultBankRangeCheck(player, origin, "bank"))
			return;

		CharacterItemManager itemManager = player.getCharItemManager();

		if (itemManager == null)
			return;

		int uuid = msg.getUUID();

		Item item = itemManager.getItemByUUID(uuid);

		if (item == null)
			return;

		//dupe check
		// WTF Checking but not logging?

		if (!item.validForBank(origin, player, itemManager))
			return;

		if (item.containerType == ItemContainerType.BANK  && itemManager.isBankOpen() == false)
			return;

		if (item.getItemBase().getType().equals(engine.Enum.ItemType.GOLD)) {

			if (!itemManager.moveGoldToInventory(item, msg.getNumItems()))
				return;

			UpdateGoldMsg goldMes = new UpdateGoldMsg(player);
			goldMes.configure();

			dispatch = Dispatch.borrow(player, goldMes);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

			return;
		}

		// Not gold, process update here

		if (!itemManager.hasRoomInventory(item.getItemBase().getWeight()))
			return;

		if (itemManager.moveItemToInventory(item) == false)
			return;

		dispatch = Dispatch.borrow(player, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

	}

	private static void transferItemFromVaultToInventory(TransferItemFromVaultToInventoryMsg msg, ClientConnection origin) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		if (player.getAccount() == null)
			return;
		player.getAccount().transferItemFromVaultToInventory(msg, origin);


	}

	//call this if the transfer fails server side to kick the item back to inventory from vault
	public static void forceTransferFromInventoryToVault(TransferItemFromVaultToInventoryMsg msg, ClientConnection origin, String reason) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		TransferItemFromInventoryToVaultMsg back = new TransferItemFromInventoryToVaultMsg(msg);
		dispatch = Dispatch.borrow(player, back);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		ChatManager.chatInfoError(player, "Can't transfer to inventory: " + reason);
	}

	private static void transferItemFromInventoryToVault(TransferItemFromInventoryToVaultMsg msg, ClientConnection origin) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		if (player.getAccount() == null)
			return;
		player.getAccount().transferItemFromInventoryToVault(msg, origin);

	}

	//call this if the transfer fails server side to kick the item back to vault from inventory
	public static void forceTransferFromVaultToInventory(TransferItemFromInventoryToVaultMsg msg, ClientConnection origin, String reason) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		TransferItemFromVaultToInventoryMsg back = new TransferItemFromVaultToInventoryMsg(msg);
		dispatch = Dispatch.borrow(player, back);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		ChatManager.chatInfoError(player, "Can't transfer to vault: " + reason);
	}

	private static void transferGoldFromVaultToInventory(TransferGoldFromVaultToInventoryMsg msg, ClientConnection origin) {

		PlayerCharacter player = origin.getPlayerCharacter();


		if (player == null)
			return;

		Account account = player.getAccount();

		if (account == null)
			return;

		account.transferGoldFromVaultToInventory(msg, origin);
	}

	private static void transferGoldFromInventoryToVault(TransferGoldFromInventoryToVaultMsg msg, ClientConnection origin) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		Account account = player.getAccount();

		if (account == null)
			return;

		account.transferGoldFromInventoryToVault(msg, origin);

	}

	private static void DeleteItem(DeleteItemMsg msg, ClientConnection origin) {

		CharacterItemManager itemManager = origin.getPlayerCharacter().getCharItemManager();
		int uuid = msg.getUUID();


		PlayerCharacter sourcePlayer = origin.getPlayerCharacter();

		if (sourcePlayer == null)
			return;

		if (!sourcePlayer.isAlive())
			return;

		Item i = Item.getFromCache(msg.getUUID());

		if (i == null)
			return;

		if (!itemManager.doesCharOwnThisItem(i.getObjectUUID()))
			return;

		if (!itemManager.inventoryContains(i))
			return;

		if (i.isCanDestroy())
			if (itemManager.delete(i) == true) {
				Dispatch dispatch = Dispatch.borrow(sourcePlayer, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			}

	}

	private static void ackBankWindowOpened(AckBankWindowOpenedMsg msg, ClientConnection origin) {
		// According to the Wiki, the client should not send this message.
		// Log the instance to investigate, and modify Wiki accordingly.
		Logger.error( msg.toString());
	}

	private static void modifyStat(ModifyStatMsg msg, ClientConnection origin) {

		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);

		if (pc == null)
			return;

		int type = msg.getType();

		switch (type) {
		case MBServerStatics.STAT_STR_ID:
			pc.addStr(msg.getAmount());
			break;
		case MBServerStatics.STAT_DEX_ID:
			pc.addDex(msg.getAmount());
			break;
		case MBServerStatics.STAT_CON_ID:
			pc.addCon(msg.getAmount());
			break;
		case MBServerStatics.STAT_INT_ID:
			pc.addInt(msg.getAmount());
			break;
		case MBServerStatics.STAT_SPI_ID:
			pc.addSpi(msg.getAmount());
			break;
		}
	}

	

	// called when player clicks respawn button
	private static void respawn(RespawnMsg msg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter sourcePlayer = SessionManager.getPlayerCharacter(origin);

		if (sourcePlayer == null)
			return;

		if (msg.getObjectType() != sourcePlayer.getObjectType().ordinal() || msg.getObjectID() != sourcePlayer.getObjectUUID()) {
			Logger.error( "Player " + sourcePlayer.getObjectUUID() + " respawning character of id " + msg.getObjectType() + ' '
					+ msg.getObjectID());
			return;
		}

		if (sourcePlayer.isAlive()) {
			Logger.error( "Player " + sourcePlayer.getObjectUUID() + " respawning while alive");
			return;
		}
		// ResetAfterDeath player
		sourcePlayer.respawnLock.writeLock().lock();
		try{
			sourcePlayer.respawn(true, false, true);

		}catch(Exception e){
			Logger.error(e);
		}finally{
			sourcePlayer.respawnLock.writeLock().unlock();

		}
		// Echo ResetAfterDeath message back
		msg.setPlayerHealth(sourcePlayer.getHealth());
		// TODO calculate any experience loss before this point
		msg.setPlayerExp(sourcePlayer.getExp() + sourcePlayer.getOverFlowEXP());
		Dispatch dispatch = Dispatch.borrow(sourcePlayer, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
		
		MoveToPointMsg moveMsg = new MoveToPointMsg();
		moveMsg.setPlayer(sourcePlayer);
		moveMsg.setStartCoord(sourcePlayer.getLoc());
		moveMsg.setEndCoord(sourcePlayer.getLoc());
		moveMsg.setInBuilding(-1);
		moveMsg.setUnknown01(-1);
		
		dispatch = Dispatch.borrow(sourcePlayer, moveMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
		
		MovementManager.sendRWSSMsg(sourcePlayer);

		// refresh the whole group with what just happened
		JobScheduler.getInstance().scheduleJob(new RefreshGroupJob(sourcePlayer), MBServerStatics.LOAD_OBJECT_DELAY);
	}

	private static void lootWindowRequest(LootWindowRequestMsg msg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);

		if (pc == null)
			return;

		if (!pc.isAlive())
			return;

		if (msg.getSourceType() != pc.getObjectType().ordinal() || msg.getSourceID() != pc.getObjectUUID()) {
			Logger.error("Player " + pc.getObjectUUID() + " looting from character of id "
					+ msg.getSourceType() + ' ' + msg.getSourceID());
			return;
		}

		if (pc.getAltitude() > 0)
			return;
		if (!pc.isAlive()) {
			return;
		}


		LootWindowResponseMsg lwrm = null;
		GameObjectType targetType = GameObjectType.values()[msg.getTargetType()];
		AbstractCharacter characterTarget = null;
		Corpse corpseTarget = null;

		switch (targetType) {
		case PlayerCharacter:

			characterTarget = PlayerCharacter.getFromCache(msg.getTargetID());
			if (characterTarget == null)
				return;
			if (characterTarget.isAlive())
				return;
			if (pc.getLoc().distanceSquared2D(characterTarget.getLoc()) > sqr(MBServerStatics.LOOT_RANGE)){
				ErrorPopupMsg.sendErrorMsg(pc, "You are too far away to loot this corpse.");

				Logger.info(pc.getFirstName() + " tried looting at " + pc.getLoc().distance2D(characterTarget.getLoc()) + " distance." );
				return;
			}
			lwrm = new LootWindowResponseMsg(characterTarget.getObjectType().ordinal(), characterTarget.getObjectUUID(), characterTarget.getInventory(true));
			break;
		case NPC:
			characterTarget = NPC.getFromCache(msg.getTargetID());
			if (characterTarget == null)
				return;
			break;
		case Mob:
			characterTarget = Mob.getFromCache(msg.getTargetID());
			if ((characterTarget == null) || characterTarget.isAlive()) {
				return;
			}

			if (pc.getLoc().distanceSquared2D(characterTarget.getLoc()) > sqr(MBServerStatics.LOOT_RANGE)){
				ErrorPopupMsg.sendErrorMsg(pc, "You are too far away to loot this corpse.");

				Logger.info(pc.getFirstName() + " tried looting at " + pc.getLoc().distance2D(characterTarget.getLoc()) + " distance." );

				if (!((Mob)characterTarget).isLootSync()){

					((Mob)characterTarget).setLootSync(true);
					WorldGrid.updateObject(characterTarget, pc);
				}


				return;
			}

			lwrm = new LootWindowResponseMsg(characterTarget.getObjectType().ordinal(), characterTarget.getObjectUUID(), characterTarget.getInventory());
			break;
		case Corpse:
			corpseTarget = Corpse.getCorpse(msg.getTargetID());

			if ((corpseTarget == null)) {
				return;
			}

			if (pc.getLoc().distanceSquared(corpseTarget.getLoc()) > sqr(MBServerStatics.LOOT_RANGE)){
				ErrorPopupMsg.sendErrorMsg(pc, "You are too far away to loot this corpse.");

				Logger.info(pc.getFirstName() + " tried looting at " + pc.getLoc().distance2D(characterTarget.getLoc()) + " distance." );
				return;
			}
			lwrm = new LootWindowResponseMsg(corpseTarget.getObjectType().ordinal(), msg.getTargetID(), corpseTarget.getInventory());
			break;
		}

		if (lwrm == null)
			return;

		DispatchMessage.dispatchMsgToInterestArea(pc, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
		Dispatch dispatch = Dispatch.borrow(pc, lwrm);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

	}

	private static void loot(LootMsg msg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player = SessionManager.getPlayerCharacter(origin);
		if (player == null)
			return;

		if (!player.isAlive())
			return;

		Item item = msg.getItem();

		if (item == null)
			return;

		if (item.lootLock.tryLock()) {
			try {
				Item itemRet = null;
				// get current owner
				int targetType = msg.getTargetType();
				int targetID = msg.getTargetID();

				if (targetType == GameObjectType.PlayerCharacter.ordinal() || targetType == GameObjectType.Mob.ordinal() || targetType == GameObjectType.Corpse.ordinal()) {
				}
				else { //needed for getting contracts for some reason
					targetType = msg.getSourceID2();
					targetID = msg.getUnknown01();
				}

				//can't loot while flying
				if (player.getAltitude() > 0)
					return;

				AbstractCharacter tar = null;
				Corpse corpse = null;

				if (targetType == GameObjectType.PlayerCharacter.ordinal() || targetType == GameObjectType.Mob.ordinal()) {

					if (targetType == GameObjectType.PlayerCharacter.ordinal()) {
						tar = PlayerCharacter.getFromCache(targetID);

						if (tar == null)
							return;

						if (player.getObjectUUID() != tar.getObjectUUID() && ((PlayerCharacter) tar).isInSafeZone())
							return;

					}

					else if (targetType == GameObjectType.NPC.ordinal())
						tar = NPC.getFromCache(targetID);
					else if (targetType == GameObjectType.Mob.ordinal())
						tar = Mob.getFromCache(targetID);
					if (tar == null)
						return;
					
					if (tar.equals(player)){
						ErrorPopupMsg.sendErrorMsg(player, "Cannot loot this item.");
						return;
					}
						

					if (player.getLoc().distanceSquared2D(tar.getLoc()) > sqr(MBServerStatics.LOOT_RANGE)){
						ErrorPopupMsg.sendErrorMsg(player, "You are too far away to loot this corpse.");

						Logger.info( player.getFirstName() + " tried looting at " + player.getLoc().distance2D(tar.getLoc()) + " distance." );
						return;
					}

					//can't loot from someone who is alive.
					if (AbstractWorldObject.IsAbstractCharacter(tar)) {
						if (tar.isAlive())
							return;
						//					Logger.error("WorldServer.loot", "Looting from live player");
					}

					if (!GroupManager.goldSplit(player, item, origin, tar)) {

						if (tar.getCharItemManager() != null) {

							itemRet = tar.getCharItemManager().lootItemFromMe(item, player, origin);

							//Take equipment off mob
							if (tar.getObjectType() == GameObjectType.Mob && itemRet != null){
								Mob mobTarget = (Mob)tar;
								if (mobTarget.getFidalityID() != 0){
									if (item != null && item.getObjectType() == GameObjectType.MobLoot){
										int fidelityEquipID = ((MobLoot)item).getFidelityEquipID();

										if (fidelityEquipID != 0){
											for (MobEquipment equip: mobTarget.getEquip().values()){
												if (equip.getObjectUUID() == fidelityEquipID){
													TransferItemFromEquipToInventoryMsg back = new TransferItemFromEquipToInventoryMsg(mobTarget, equip.getSlot());

													DispatchMessage.dispatchMsgToInterestArea(mobTarget, back, DispatchChannel.SECONDARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);

													LootMsg lootMsg = new LootMsg(0,0,tar.getObjectType().ordinal(), tar.getObjectUUID(), equip);
													Dispatch dispatch = Dispatch.borrow(player, lootMsg);
													DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
													break;
												}
											}
										}


									}
								}


							}
						}

					}
					else {

					}

				}
				else if (targetType == GameObjectType.Corpse.ordinal()) {
					corpse = Corpse.getCorpse(targetID);
					if (corpse == null)
						return;

					if (player.getLoc().distanceSquared2D(corpse.getLoc()) > sqr(MBServerStatics.LOOT_RANGE)){
						ErrorPopupMsg.sendErrorMsg(player, "You are too far away to loot this corpse.");

						Logger.info( player.getFirstName() + " tried looting at " + player.getLoc().distance2D(corpse.getLoc()) + " distance." );
						return;
					}


					//can't loot other players in safe zone.
					if (corpse.getBelongsToType() == GameObjectType.PlayerCharacter.ordinal()){
						
						if (player.getObjectUUID() == corpse.getBelongsToID())
							itemRet = corpse.lootItem(item, player);
						else if (!GroupManager.goldSplit(player, item, origin, corpse)) {
							itemRet = corpse.lootItem(item, player);

						}
						
						if (itemRet == null)
							return;
						
						
						if (item.getItemBase().getType().equals(engine.Enum.ItemType.GOLD)) {
							// this is done to prevent the temporary goldItem item
							// (from the mob) from appearing in player's inventory.
							// It also updates the goldItem quantity display
							UpdateGoldMsg updateTargetGold = null;

							
							 if (corpse != null)
								updateTargetGold = new UpdateGoldMsg(corpse);

							updateTargetGold.configure();
							DispatchMessage.dispatchMsgToInterestArea(corpse, updateTargetGold, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);

							UpdateGoldMsg ugm = new UpdateGoldMsg(player);
							ugm.configure();
							Dispatch dispatch = Dispatch.borrow(player, ugm);
							DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

							// respond back loot message. Try sending to everyone.

						}
						else {
						
							DispatchMessage.dispatchMsgToInterestArea(corpse, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, true);
							

							//player.getCharItemManager().updateInventory();
						}

						//TODO send group loot message if player is grouped and visible
						Group group = GroupManager.getGroup(player);

						if (group != null && group.getSplitGold() && (item.getItemBase().getType().equals(engine.Enum.ItemType.GOLD) == false)) {
							String name = item.getName();
							String text = player.getFirstName() + " has looted " + name + '.';
							ChatManager.chatGroupInfoCanSee(player, text);
						}
						
						return;
					}

						

				}
				else
					return;


				if (itemRet == null) {
					return;
				}

				if (item.getItemBase().getType().equals(engine.Enum.ItemType.GOLD)) {
					// this is done to prevent the temporary goldItem item
					// (from the mob) from appearing in player's inventory.
					// It also updates the goldItem quantity display
					UpdateGoldMsg updateTargetGold = null;

					if (tar != null)
						updateTargetGold = new UpdateGoldMsg(tar);
					else if (corpse != null)
						updateTargetGold = new UpdateGoldMsg(corpse);

					updateTargetGold.configure();
					DispatchMessage.dispatchMsgToInterestArea(tar, updateTargetGold, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);

					UpdateGoldMsg ugm = new UpdateGoldMsg(player);
					ugm.configure();
					Dispatch dispatch = Dispatch.borrow(player, ugm);
					DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

					// respond back loot message. Try sending to everyone.

				}
				else {
					msg.setSourceType1(0);
					msg.setSourceType2(0);
					msg.setSourceID1(0);
					msg.setSourceID2(0);
					Dispatch dispatch = Dispatch.borrow(player, msg);
					//DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
					DispatchMessage.dispatchMsgToInterestArea(tar, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, true);
					LootMsg newItemMsg = new LootMsg(GameObjectType.PlayerCharacter.ordinal(), player.getObjectUUID(),0,0, itemRet);
					dispatch = Dispatch.borrow(player, newItemMsg);
					DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

					//player.getCharItemManager().updateInventory();
				}

				//TODO send group loot message if player is grouped and visible
				Group group = GroupManager.getGroup(player);

				if (group != null && group.getSplitGold() && (item.getItemBase().getType().equals(engine.Enum.ItemType.GOLD) == false)) {
					String name = item.getName();
					String text = player.getFirstName() + " has looted " + name + '.';
					ChatManager.chatGroupInfoCanSee(player, text);
				}
			} catch (Exception e) {
				Logger.info( e.getMessage());
			} finally {
				item.lootLock.unlock();
			}
		}


	}

	//returns true if looted item is goldItem and is split. Otherwise returns false

	// called when player types /show
	private static void show(ShowMsg msg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);

		if (pc == null)
			return;

		int targetType = msg.getTargetType();
		AbstractCharacter tar = null;

		if (targetType == GameObjectType.PlayerCharacter.ordinal())
			tar = PlayerCharacter.getFromCache(msg.getTargetID());
		else if (targetType == GameObjectType.NPC.ordinal())
			tar = NPC.getFromCache(msg.getTargetID());
		else if (targetType == GameObjectType.Mob.ordinal())
			tar = Mob.getFromCache(msg.getTargetID());

		if (tar == null || !tar.isAlive() || !tar.isActive())
			return;

		msg.setUnknown01(pc.getLoc());
		msg.setUnknown02(pc.getLoc());
		msg.setRange01(pc.getRange());
		msg.setUnknown03(tar.getLoc());
		msg.setUnknown04(tar.getLoc());
		msg.setRange01(tar.getRange());

		Dispatch dispatch = Dispatch.borrow(pc, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

	}

	private static void ViewResourcesMessage(ViewResourcesMessage msg, ClientConnection origin) throws SQLException {

		PlayerCharacter player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return;

		Guild guild = player.getGuild();
		City city = guild.getOwnedCity();

		if (city == null)
			return;

		Building warehouse = BuildingManager.getBuilding(city.getWarehouseBuildingID());

		if (warehouse == null)
			return;

		ViewResourcesMessage vrm = new ViewResourcesMessage(player);
		vrm.setWarehouseBuilding(warehouse);
		vrm.setGuild(player.getGuild());
		vrm.configure();

		Dispatch dispatch = Dispatch.borrow(player, vrm);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

	}

	private static void randomRoll(RandomMsg msg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter source = origin.getPlayerCharacter();

		if (source == null || !source.isAlive())
			return;

		//2 second cooldown on random rolls
		long lastRandom = source.getTimeStamp("RandomRoll");

		if (System.currentTimeMillis() - lastRandom < 2000)
			return;
		source.setTimeStamp("RandomRoll", System.currentTimeMillis());

		//handle random roll
		int max = msg.getMax();

		if (max > 0)
			msg.setRoll(ThreadLocalRandom.current().nextInt(max) + 1);
		else if (max < 0) {
			max = 1 - max;
			msg.setRoll((ThreadLocalRandom.current().nextInt(max) - max) + 1);
		}

		msg.setSourceType(source.getObjectType().ordinal());
		msg.setSourceID(source.getObjectUUID());

		//send to all in range
		DispatchMessage.dispatchMsgToInterestArea(source, msg, DispatchChannel.SECONDARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, true);
	}

	private static void stuck(ClientConnection origin) {

		PlayerCharacter sourcePlayer = origin.getPlayerCharacter();

		if (sourcePlayer == null)
			return;

		if (sourcePlayer.getTimers().containsKey("Stuck"))
			return;

		StuckJob sj = new StuckJob(sourcePlayer);
		JobContainer jc = JobScheduler.getInstance().scheduleJob(sj, 10000); // Convert
		ConcurrentHashMap<String, JobContainer> timers = sourcePlayer.getTimers();

		if (timers != null) {
			if (timers.containsKey("Stuck")) {
				timers.get("Stuck").cancelJob();
				timers.remove("Stuck");
			}
			timers.put("Stuck", jc);
		}
	}

	private static void GuildTreeStatusMsg(GuildTreeStatusMsg msg, ClientConnection origin) throws SQLException {

		PlayerCharacter player = SessionManager.getPlayerCharacter(origin);
		Dispatch dispatch;

		if (player == null)
			return;

		if (origin.guildtreespam > System.currentTimeMillis()) {
			return;
		}
		origin.guildtreespam = System.currentTimeMillis() + 5000;

		Building b = BuildingManager.getBuildingFromCache(msg.getTargetID());
		if (b == null)
			return;

		GuildTreeStatusMsg gtsm = new GuildTreeStatusMsg(b, player);
		gtsm.configure();

		dispatch = Dispatch.borrow(player, gtsm);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	}


	private static void openSellToNPCWindow(SellToNPCWindowMsg msg, ClientConnection origin) {

		PlayerCharacter sourcePlayer = SessionManager.getPlayerCharacter(origin);
		Dispatch dispatch;

		if (sourcePlayer == null)
			return;

		NPC npc = NPC.getFromCache(msg.getNPCID());

		if (npc == null)
			return;

		// test within talking range

		if (sourcePlayer.getLoc().distanceSquared2D(npc.getLoc()) > MBServerStatics.NPC_TALK_RANGE * MBServerStatics.NPC_TALK_RANGE) {
			ErrorPopupMsg.sendErrorPopup(sourcePlayer, 14);
			return;
		}

		Contract con = npc.getContract();

		if (con == null)
			return;
		float bargain = sourcePlayer.getBargain();
		
		float profit = npc.getBuyPercent(sourcePlayer) + bargain;
		
		if (profit > 1)
			profit = 1;
		
		msg.setupOutput();

		msg.setUnknown05(profit);
		msg.setUnknown06(500000); //TODO set goldItem on npc later
		msg.setItemType(con.getBuyItemType());
		msg.setSkillTokens(con.getBuySkillToken());
		msg.setUnknownArray(con.getBuyUnknownToken());

		dispatch = Dispatch.borrow(sourcePlayer, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

	}

	private static void sellToNPC(SellToNPCMsg msg, ClientConnection origin) {

		PlayerCharacter player = SessionManager.getPlayerCharacter(origin);
		Dispatch dispatch;

		if (player == null)
			return;

		CharacterItemManager itemMan = player.getCharItemManager();

		if (itemMan == null)
			return;

		NPC npc = NPC.getFromCache(msg.getNPCID());

		if (npc == null)
			return;

		Item gold = itemMan.getGoldInventory();

		if (gold == null)
			return;

		if (origin.sellLock.tryLock()) {
			try {
				Item sell;
				int cost = 0;


				if (npc.getCharItemManager().getInventoryCount() > 150) {
					if (npc.getParentZone() != null && npc.getParentZone().getPlayerCityUUID() == 0) {
						ArrayList<Item> inv = npc.getInventory();
						for (int i = 0; i < 20; i++) {
							try {
								Item toRemove = inv.get(i);
								if (toRemove != null)
									npc.getCharItemManager().delete(toRemove);
							} catch (Exception e) {
								break;
							}

						}
					}

				}

				// Early exit sanity check

				if (msg.getItemType() == GameObjectType.Item.ordinal() == false)
					return;

				sell = Item.getFromCache(msg.getItemID());

				if (sell == null)
					return;

				//get item to sell

				ItemBase ib = sell.getItemBase();

				if (ib == null)
					return;

				if (npc.getParentZone() != null && npc.getParentZone().getPlayerCityUUID() != 0)
					if (!npc.getCharItemManager().hasRoomInventory(ib.getWeight())){

						ErrorPopupMsg.sendErrorPopup(player, 21);
						return;
					}

				if (!sell.validForInventory(origin, player, itemMan))
					return;

				//get goldItem cost to sell
				
				
				cost = sell.getBaseValue();
				float bargain = player.getBargain();
				
				float profit = npc.getBuyPercent(player) + bargain;
				
				if (profit > 1)
					profit = 1;
					
				
				
				cost *= profit;

				if (gold.getNumOfItems() + cost > 10000000) {
					return;
				}

				if (gold.getNumOfItems() + cost < 0)
					return;

				//TODO make sure npc can buy item type
				//test room available for item on npc

				//                                 if (!npc.isStatic() && npc.getCharItemManager().getInventoryCount() > 150) {
				//                                   //  chatMan.chatSystemInfo(pc, "This vendor's inventory is full");
				//                                     return;
				//                                 }

				//make sure item is in player inventory

				Building building = (!npc.isStatic()) ? npc.getBuilding() : null;

				if (building != null && building.getProtectionState().equals(ProtectionState.NPC))
					building = null;
				if (npc.getParentZone().getPlayerCityUUID() == 0)
					building = null;

				//make sure npc can afford item

				if (building != null && !building.hasFunds(cost)){
					ErrorPopupMsg.sendErrorPopup(player, 17);
					return;
				}
				if (building != null && (building.getStrongboxValue() - cost) < 0){
					ErrorPopupMsg.sendErrorPopup(player, 17);
					return;
				}

				//TODO transfer item and goldItem transfer should be handled together incase failure
				//transfer the item

				if (!itemMan.sellToNPC(sell, npc))
					return;

				if (!itemMan.sellToNPC(building, cost, sell))
					return;

				//handle goldItem transfer

				if (sell == null)
					return;

				// ***REFACTOR: SellToNpc sends this message, is this a duplicate?

				//update player's goldItem count
				UpdateGoldMsg ugm = new UpdateGoldMsg(player);
				ugm.configure();

				dispatch = Dispatch.borrow(player, ugm);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

				//send the sell message back to update player
				msg.setItemType(sell.getObjectType().ordinal());
				msg.setItemID(sell.getObjectUUID());
				msg.setUnknown01(cost); //not sure if this is correct

				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

			} finally {
				origin.sellLock.unlock();
			}
		}
		else {
			ErrorPopupMsg.sendErrorPopup(player, 12);
		}
	}

	private static void openBuyFromNPCWindow(BuyFromNPCWindowMsg msg, ClientConnection origin) {

		PlayerCharacter sourcePlayer = SessionManager.getPlayerCharacter(origin);
		Dispatch dispatch;

		if (sourcePlayer == null)
			return;

		NPC npc = NPC.getFromCache(msg.getNpcID());

		if (npc == null)
			return;

		// test within talking range

		if (sourcePlayer.getLoc().distanceSquared2D(npc.getLoc()) > MBServerStatics.NPC_TALK_RANGE * MBServerStatics.NPC_TALK_RANGE) {
			ErrorPopupMsg.sendErrorPopup(sourcePlayer, 14);
			return;
		}

		dispatch = Dispatch.borrow(sourcePlayer, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	}

	private static void buyFromNPC(BuyFromNPCMsg msg, ClientConnection origin) {

		PlayerCharacter sourcePlayer = SessionManager.getPlayerCharacter(origin);

		if (sourcePlayer == null)
			return;

		if (origin.buyLock.tryLock()) {

			try {
				CharacterItemManager itemMan = sourcePlayer.getCharItemManager();

				if (itemMan == null)
					return;

				NPC npc = NPC.getFromCache(msg.getNPCID());

				if (npc == null)
					return;

				Item gold = itemMan.getGoldInventory();

				if (gold == null)
					return;

				Item buy = null;

				if (msg.getItemType() == GameObjectType.MobEquipment.ordinal()) {
					ArrayList<MobEquipment> sellInventory = npc.getContract().getSellInventory();
					if (sellInventory == null)
						return;
					for (MobEquipment me : sellInventory) {
						if (me.getObjectUUID() == msg.getItemID()) {
							ItemBase ib = me.getItemBase();
							if (ib == null)
								return;

							//test room available for item
							if (!itemMan.hasRoomInventory(ib.getWeight()))
								return;

							int cost = me.getMagicValue();
							
							float bargain = sourcePlayer.getBargain();
							
							float profit = npc.getSellPercent(sourcePlayer) - bargain;
							
						if (profit < 1)
							profit = 1;
							
							
							cost *= profit;
							
							
							
						

							
							if (gold.getNumOfItems() - cost < 0) {
								//dont' have enough goldItem exit!
								// chatMan.chatSystemInfo(pc, "" + "You dont have enough gold.");
								return;
							}

							Building b = (!npc.isStatic()) ? npc.getBuilding() : null;
							
							if (b != null && b.getProtectionState().equals(ProtectionState.NPC))
								b = null;
							int buildingDeposit = cost - me.getMagicValue();
							if (b != null && (b.getStrongboxValue() + buildingDeposit) > b.getMaxGold()) {
								ErrorPopupMsg.sendErrorPopup(sourcePlayer, 206);
								return;
							}

							if (!itemMan.buyFromNPC(b, cost, buildingDeposit)) {
								// chatMan.chatSystemInfo(pc, "" + "You Failed to buy the item.");
								return;
							}

							buy = Item.createItemForPlayer(sourcePlayer, ib);

							if (buy != null) {
								me.transferEnchants(buy);
								itemMan.addItemToInventory(buy);
								//itemMan.updateInventory();
							}
						}
					}
				}
				else if (msg.getItemType() == GameObjectType.Item.ordinal()) {

					CharacterItemManager npcCim = npc.getCharItemManager();

					if (npcCim == null)
						return;

					buy = Item.getFromCache(msg.getItemID());

					if (buy == null)
						return;

					ItemBase ib = buy.getItemBase();

					if (ib == null)
						return;

					if (!npcCim.inventoryContains(buy))
						return;

					//test room available for item
					if (!itemMan.hasRoomInventory(ib.getWeight()))
						return;

					//TODO test cost and subtract goldItem

					//TODO CHnage this if we ever put NPc city npcs in buildings.
					int cost = buy.getBaseValue();
					
					if (buy.isID() || buy.isCustomValue())
						cost = buy.getMagicValue();
					
					float bargain = sourcePlayer.getBargain();
					
					float profit = npc.getSellPercent(sourcePlayer) - bargain;
					
				if (profit < 1)
					profit = 1;
					
					if (!buy.isCustomValue())
						cost *= profit;
					else
						cost = buy.getValue();
					
					
						
					if (gold.getNumOfItems() - cost < 0) {
						ErrorPopupMsg.sendErrorPopup(sourcePlayer, 128);  // Insufficient Gold
						return;
					}

					Building b = (!npc.isStatic()) ? npc.getBuilding() : null;
					
					if (b != null)
					if (b.getProtectionState().equals(ProtectionState.NPC))
						b = null;
					
					int buildingDeposit = cost;

					if (b != null && (b.getStrongboxValue() + buildingDeposit) > b.getMaxGold()) {
						ErrorPopupMsg.sendErrorPopup(sourcePlayer, 206);
						return;
					}

					if (!itemMan.buyFromNPC(b, cost, buildingDeposit)) {
						ErrorPopupMsg.sendErrorPopup(sourcePlayer, 110);
						return;
					}

					if (buy != null)
						itemMan.buyFromNPC(buy, npc);

				}else if (msg.getItemType() == GameObjectType.MobLoot.ordinal()) {

					CharacterItemManager npcCim = npc.getCharItemManager();

					if (npcCim == null)
						return;

					buy = MobLoot.getFromCache(msg.getItemID());

					if (buy == null)
						return;

					ItemBase ib = buy.getItemBase();

					if (ib == null)
						return;

					if (!npcCim.inventoryContains(buy))
						return;

					//test room available for item
					if (!itemMan.hasRoomInventory(ib.getWeight()))
						return;

					//TODO test cost and subtract goldItem

					//TODO CHnage this if we ever put NPc city npcs in buildings.

					int cost = buy.getMagicValue();
						cost *= npc.getSellPercent(sourcePlayer);
					

					if (gold.getNumOfItems() - cost < 0) {
						ErrorPopupMsg.sendErrorPopup(sourcePlayer, 128);  // Insufficient Gold
						return;
					}

					Building b = (!npc.isStatic()) ? npc.getBuilding() : null;
					
					if (b != null && b.getProtectionState().equals(ProtectionState.NPC))
						b = null;
					int buildingDeposit = cost;

					if (b != null && (b.getStrongboxValue() + buildingDeposit) > b.getMaxGold()) {
						ErrorPopupMsg.sendErrorPopup(sourcePlayer, 206);
						return;
					}

					if (!itemMan.buyFromNPC(b, cost, buildingDeposit))
						return;

					if (buy != null)
						itemMan.buyFromNPC(buy, npc);

				}
				else
					return;

				if (buy != null) {

					msg.setItem(buy);
					//send the buy message back to update player
					//					msg.setItemType(buy.getObjectType().ordinal());
					//					msg.setItemID(buy.getObjectUUID());
					Dispatch dispatch = Dispatch.borrow(sourcePlayer, msg);
					DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
					itemMan.updateInventory();
				}

			} finally {
				origin.buyLock.unlock();
			}
		}
		else {
			ErrorPopupMsg.sendErrorPopup(origin.getPlayerCharacter(), 12); // All production slots taken
		}

	}

	//Handle RepairObject Window and RepairObject Requests

	private static void Repair(RepairMsg msg, ClientConnection origin) {

		PlayerCharacter player = SessionManager.getPlayerCharacter(origin);
		Dispatch dispatch;

		if (player == null)
			return;

		NPC npc = NPC.getFromCache(msg.getNPCID());

		if (npc == null)
			return;

		if (msg.getMsgType() == 1) { //Open RepairObject Window

			if (player.getLoc().distanceSquared2D(npc.getLoc()) > MBServerStatics.NPC_TALK_RANGE * MBServerStatics.NPC_TALK_RANGE) {
				ErrorPopupMsg.sendErrorPopup(player, 14);
				return;
			}

			//send open repair window response
			msg.setRepairWindowAck(npc);
			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		}
		else if (msg.getMsgType() == 0) { //Request RepairObject

			CharacterItemManager itemMan = player.getCharItemManager();

			if (itemMan == null)
				return;

			Item gold = itemMan.getGoldInventory();

			if (gold == null)
				return;

			Item toRepair = Item.getFromCache(msg.getItemID());

			if (toRepair == null)
				return;
			
			if (toRepair.getItemBase().isGlass())
				return;

			//make sure item is in player's inventory or equipment
			if (!itemMan.inventoryContains(toRepair) && !itemMan.equippedContains(toRepair))
				return;

			//make sure item is damaged and not destroyed
			short dur = toRepair.getDurabilityCurrent();
			short max = toRepair.getDurabilityMax();
			//account for durability modifications
			float durMod = toRepair.getBonusPercent(ModType.Durability,SourceType.None);
			max *= (1 + (durMod * 0.01f));
			if (dur >= max || dur < 1) {
				//redundancy message to clear item from window in client
				toRepair.setDurabilityCurrent(max);
				msg.setupRepairAck(max - dur);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
				return;
			}
			//TODO get cost to repair
			int cost = (int) ((max - dur) * 80.1);
			Building b = (!npc.isStatic()) ? npc.getBuilding() : null;
			
			if (b != null)
			if (b.getProtectionState().equals(ProtectionState.NPC))
				b = null;
			

			if (b != null && (b.getStrongboxValue() + cost) > b.getMaxGold()) {
				ErrorPopupMsg.sendErrorPopup(player, 206);
				return;
			}

			if (player.getCharItemManager().getGoldInventory().getNumOfItems() - cost < 0)
				return;

			if (player.getCharItemManager().getGoldInventory().getNumOfItems() - cost > MBServerStatics.PLAYER_GOLD_LIMIT)
				return;

			if (!itemMan.buyFromNPC(b, cost, cost)) {
				ErrorPopupMsg.sendErrorPopup(player, 128);
				return;
			}

			//update player's goldItem count
			UpdateGoldMsg ugm = new UpdateGoldMsg(player);
			ugm.configure();
			dispatch = Dispatch.borrow(player, ugm);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			//update durability to database
			if (!DbManager.ItemQueries.SET_DURABILITY(toRepair, max))
				return;
			//repair the item
			toRepair.setDurabilityCurrent(max);
			//send repair msg
			msg.setupRepairAck(max - dur);
			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		}
	}

	protected static void petAttack(PetAttackMsg msg, ClientConnection conn) throws MsgSendException {

		PlayerCharacter pc = SessionManager.getPlayerCharacter(conn);

		if (pc == null)
			return;

		Mob pet = pc.getPet();

		if (pet == null)
			return;

		if (!pet.isAlive())
			return;

		if ((pc.inSafeZone())
				&& (msg.getTargetType() == GameObjectType.PlayerCharacter.ordinal()))
			return;

		CombatManager.setAttackTarget(msg, conn);
		
		if (pet.getCombatTarget() == null)
			return;
		pet.setState(STATE.Attack);
	}

	protected static void petCmd(PetCmdMsg msg, ClientConnection conn) throws MsgSendException {

		PlayerCharacter pc = SessionManager.getPlayerCharacter(conn);

		if (pc == null)
			return;

		Mob pet = pc.getPet();

		if (pet == null)
			return;

		if (!pet.isAlive())
			return;

		if (pet.getState() == STATE.Disabled)
			return;

		int type = msg.getType();

		if (type == 1) { //stop attack
			pet.setCombatTarget(null);
			pc.setCombat(false);
			pet.setState(STATE.Awake);

		}
		else if (type == 2) { //dismiss
			pet.dismiss();
			pc.dismissPet();
			
			if (pet.isAlive())
				WorldGrid.updateObject(pet);
		}
		else if (type == 3) //toggle assist
			pet.toggleAssist();
		else if (type == 5) { //rest
			boolean sit = (!(pet.isSit()));
			pet.setSit(sit);

			// cancel effects that break on sit
			if (pet.isSit())
				pet.cancelOnSit();

			UpdateStateMsg rwss = new UpdateStateMsg();
			rwss.setPlayer(pet);
			DispatchMessage.sendToAllInRange(pet, rwss);
		}
	}

	protected static void HandlePromptRecall(PromptRecallMsg msg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player = SessionManager.getPlayerCharacter(origin);
		boolean recallAccepted;

		if (player == null)
			return;

		boolean confirmed = msg.getConfirmed();

		if (confirmed == true) {
			long timeElapsed = System.currentTimeMillis() - player.getTimeStamp("PromptRecall");
			//send fail message
			recallAccepted = timeElapsed < 15000;
		}
		else
			recallAccepted = false;

		if (recallAccepted == true) {
			//handle recall
			long type = player.getTimeStamp("LastRecallType");

			if (type == 1) { //recall to bind
				player.teleport(player.getBindLoc());
				player.setSafeMode();
			}
			else { //recall to rg
				float dist = 9999999999f;
				Building rg = null;
				Vector3fImmutable rgLoc;

				for (Runegate runegate : Runegate.getRunegates()) {

					if ((runegate.getGateType() == RunegateType.OBLIV) ||
							(runegate.getGateType() == RunegateType.CHAOS))
						continue;

					for (Runegate thisGate : Runegate.getRunegates()) {

						rgLoc = thisGate.getGateType().getGateBuilding().getLoc();

						float distanceSquaredToRunegate = player.getLoc().distanceSquared2D(rgLoc);

						if (distanceSquaredToRunegate < sqr(dist))
							rg = thisGate.getGateType().getGateBuilding();

					}
				}
				//nearest runegate found. teleport characterTarget

				if (rg != null) {
				player.teleport(rg.getLoc());
					player.setSafeMode();
				}
			}
		}
	}

}
