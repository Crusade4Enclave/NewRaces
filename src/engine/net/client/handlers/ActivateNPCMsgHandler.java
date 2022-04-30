package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.ItemType;
import engine.exception.MsgSendException;
import engine.gameManager.*;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ActivateNPCMessage;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ManageCityAssetsMsg;
import engine.objects.*;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;

/*
 * @Author:
 * @Summary: Processes application protocol message which keeps
 * client's tcp connection open.
 */
public class ActivateNPCMsgHandler extends AbstractClientMsgHandler {

	public ActivateNPCMsgHandler() {
		super(ActivateNPCMessage.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		ActivateNPCMessage msg;
		PlayerCharacter player;
		Building building;
		Contract contract;
		CharacterItemManager itemMan;
		Zone zone;

		msg = (ActivateNPCMessage) baseMsg;
		player = SessionManager.getPlayerCharacter(origin);
		building =  BuildingManager.getBuildingFromCache(msg.buildingUUID());

		if (player == null || building == null)
			return false;

		ArrayList<Item> ItemLists = new ArrayList<>();

		// Filter hirelings by slot type

		for (Item hirelings : player.getInventory()) {
			if (hirelings.getItemBase().getType().equals(ItemType.CONTRACT)) {
				contract = DbManager.ContractQueries.GET_CONTRACT(hirelings.getItemBase().getUUID());
				if (contract == null)
					continue;
				if (contract.canSlotinBuilding(building))
					ItemLists.add(hirelings);
			}
		}

		if (msg.getUnknown01() == 1) {
			//Request npc list to slot
			ActivateNPCMessage anm = new ActivateNPCMessage();
			anm.setSize(ItemLists.size());
			anm.setItemList(ItemLists);
			Dispatch dispatch = Dispatch.borrow(player, anm);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		}

		if (msg.getUnknown01() == 0) {

			//Slot npc

			if (building.getBlueprintUUID() == 0) {
				ChatManager.chatSystemError(player, "Unable to load Blueprint for Building Mesh " + building.getMeshUUID());
				return false;
			}

			if (building.getBlueprint().getMaxSlots() == building.getHirelings().size())
				return false;

			Vector3fImmutable NpcLoc = new Vector3fImmutable(building.getLoc());

			Item contractItem = Item.getFromCache(msg.getUnknown04());

			if (contractItem == null)
				return false;

			if (!player.getCharItemManager().doesCharOwnThisItem(contractItem.getObjectUUID())) {
				Logger.error(player.getName() + "has attempted to place Hireling : " + contractItem.getName() + "without a valid contract!");
				return false;
			}

			itemMan = player.getCharItemManager();

			zone = ZoneManager.findSmallestZone(NpcLoc);

			if (zone == null)
				return false;

			contract = DbManager.ContractQueries.GET_CONTRACT(contractItem.getItemBase().getUUID());

			if (contract == null)
				return false;

			// Check if contract can be slotted in this building

			if (contract.canSlotinBuilding(building) == false)
				return false;

			if (!BuildingManager.addHireling(building, player, NpcLoc, zone, contract, contractItem))
				return false;

			itemMan.delete(contractItem);
			itemMan.updateInventory();

			ManageCityAssetsMsg mca1 = new ManageCityAssetsMsg(player, building);

			mca1.actionType = 3;

			mca1.setTargetType(building.getObjectType().ordinal());
			mca1.setTargetID(building.getObjectUUID());
			mca1.setTargetType3(building.getObjectType().ordinal());
			mca1.setTargetID3(building.getObjectUUID());
			mca1.setAssetName1(building.getName());
			mca1.setUnknown54(1);
			Dispatch dispatch = Dispatch.borrow(player, mca1);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);


		}

		return true;
	}

}
