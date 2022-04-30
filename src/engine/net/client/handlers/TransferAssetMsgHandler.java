package engine.net.client.handlers;

import engine.Enum;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.TransferAssetMsg;
import engine.objects.Blueprint;
import engine.objects.Building;
import engine.objects.PlayerCharacter;

/*
 * @Author:
 * @Summary: Processes application protocol message which transers
 * assets between characters.
 */

public class TransferAssetMsgHandler extends AbstractClientMsgHandler {

	public TransferAssetMsgHandler() {
		super(TransferAssetMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		TransferAssetMsg transferAssetMsg = (TransferAssetMsg) baseMsg;

		int Buildingid = transferAssetMsg.getObjectID();
		int BuildingType = transferAssetMsg.getObjectType(); //ToDue Later
		int TargetID = transferAssetMsg.getTargetID();
		int TargetType = transferAssetMsg.getTargetType();  //ToDue later

		Building building = BuildingManager.getBuildingFromCache(Buildingid);
		PlayerCharacter newOwner = PlayerCharacter.getFromCache(TargetID);
		PlayerCharacter player = origin.getPlayerCharacter();

		if (player == null || building == null || newOwner == null)
			return true;

		Blueprint blueprint = building.getBlueprint();

		if (blueprint == null)
			return true;

		if (building.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.MINE) {
			ErrorPopupMsg.sendErrorMsg(player, "You cannot transfer a mine!");
			return true;
		}

		// Players cannot transfer shrines

		if ((building.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.SHRINE)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to transfer shrine!");
			return true;
		}

		if (Blueprint.isMeshWallPiece(building.getBlueprintUUID())) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to transfer fortress asset!");
			return true;
		}

		if ((building.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.BARRACK)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to transfer fortress asset!");
			return true;
		}

		if ((building.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.BULWARK)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to transfer siege asset!");
			return true;
		}

		if ((building.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.SIEGETENT)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to transfer siege asset!");
			return true;
		}

		if ((building.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.BANESTONE)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to transfer banestone!");
			return true;
		}
		if (building.getOwnerUUID() != player.getObjectUUID()) {
			ChatManager.chatSystemError(player, "You do not own this asset.");
			return true;
		}

		if (building.getOwnerUUID() == newOwner.getObjectUUID()) {
			ChatManager.chatSystemError(player, "You already own this asset.");
			return true;
		}

		building.setOwner(newOwner);
		return true;
	}

}