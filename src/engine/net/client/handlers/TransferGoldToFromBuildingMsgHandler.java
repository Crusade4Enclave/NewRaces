package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.PlaceAssetMsg;
import engine.net.client.msg.TransferGoldToFromBuildingMsg;
import engine.net.client.msg.UpdateGoldMsg;
import engine.objects.Building;
import engine.objects.CharacterItemManager;
import engine.objects.Item;
import engine.objects.PlayerCharacter;
import org.pmw.tinylog.Logger;

/*
 * @Author:
 * @Summary: Processes application protocol message which transfers
 * gold between a building's strongbox and a player character.
 */

public class TransferGoldToFromBuildingMsgHandler extends AbstractClientMsgHandler {

	public TransferGoldToFromBuildingMsgHandler() {
		super(TransferGoldToFromBuildingMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player;
		Building building;
		CharacterItemManager itemMan;
		Item goldItem;
		TransferGoldToFromBuildingMsg msg;
		Dispatch dispatch;

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;

		msg = (TransferGoldToFromBuildingMsg) baseMsg;

		building =  BuildingManager.getBuildingFromCache(msg.getObjectID());

		if (building == null)
			return true;

		if (msg.getDirection() == 2){

			if(!ManageCityAssetMsgHandler.playerCanManageNotFriends(player, building))
				return true;
			if (building.setReserve(msg.getUnknown01(),player)){
				 dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			}
			
			return true;
		}

		//        if (building.getTimeStamp(MBServerStatics.STRONGBOX_DELAY_STRING) > System.currentTimeMillis()){
		//        	ErrorPopupMsg.sendErrorMsg(player, MBServerStatics.STRONGBOX_DELAY_OUTPUT);
		//        	return true;
		//        }

		//building.getTimestamps().put(MBServerStatics.STRONGBOX_DELAY_STRING, System.currentTimeMillis() + MBServerStatics.ONE_MINUTE);

		itemMan = player.getCharItemManager();

		goldItem = itemMan.getGoldInventory();

		if (goldItem == null) {
			Logger.error("Could not access gold item");
			return true;
		}


		// Update in-game gold values for player and building


		try {


			if (!itemMan.transferGoldToFromBuilding(msg.getAmount(), building))
				return true;


			UpdateGoldMsg ugm = new UpdateGoldMsg(player);
			ugm.configure();
			dispatch = Dispatch.borrow(player, ugm);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

			// Refresh the player's inventory if it's currently open

			// Refresh the tree's window to update strongbox


			msg.setAmount(building.getStrongboxValue());
			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		} catch (Exception e) {
			PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
		}

		return true;
	}

}
