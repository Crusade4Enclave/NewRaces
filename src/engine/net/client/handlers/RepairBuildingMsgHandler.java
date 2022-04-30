package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.SessionManager;
import engine.gameManager.ZoneManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.RepairBuildingMsg;
import engine.net.client.msg.UpdateObjectMsg;
import engine.objects.Building;
import engine.objects.City;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;

/*
 * @Author:
 * @Summary: Processes application protocol message which handles
 * protecting and unprotecting city assets
 */
public class RepairBuildingMsgHandler extends AbstractClientMsgHandler {

	public RepairBuildingMsgHandler() {
		super(RepairBuildingMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		PlayerCharacter player;
		Building targetBuilding;
		RepairBuildingMsg msg;


		// Member variable assignment

		msg = (RepairBuildingMsg) baseMsg;

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;



		switch (msg.getType()) {
		case 0:
			targetBuilding =  BuildingManager.getBuildingFromCache(msg.getBuildingID());
			RepairBuilding(targetBuilding, origin, msg);
			break;

			//		targetBuilding.createFurniture(item.getItemBase().getUseID(), 0, msg.getFurnitureLoc(), Vector3f.ZERO, 0, player);


		}




		//		dispatch = Dispatch.borrow(player, baseMsg);
		//		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		return true;

	}

	private static void RepairBuilding(Building targetBuilding, ClientConnection origin, RepairBuildingMsg msg) {

		// Member variable declaration

		Zone serverZone;
		Dispatch dispatch;

		// Member variable assignment

		if (targetBuilding == null)
			return;

		if (!targetBuilding.hasFunds(BuildingManager.GetRepairCost(targetBuilding)))
			return;

		PlayerCharacter pc = origin.getPlayerCharacter();

		serverZone = ZoneManager.findSmallestZone(pc.getLoc());

		if (serverZone.getPlayerCityUUID() == 0 && targetBuilding.getBlueprint() != null && targetBuilding.getBlueprint().getBuildingGroup() != BuildingGroup.MINE)
			return;


		City city = City.GetCityFromCache(serverZone.getPlayerCityUUID());

		if (city != null){
			if(city.getBane() != null && city.protectionEnforced == false)
				return;

		}

		//cannot repair mines during 24/7 activity.

		if (targetBuilding.getBlueprint() != null && targetBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.MINE){
			return;
		}





		int maxHP = (int) targetBuilding.getMaxHitPoints();
		int repairCost = BuildingManager.GetRepairCost(targetBuilding);
		int missingHealth = (int) BuildingManager.GetMissingHealth(targetBuilding);

		if (!targetBuilding.transferGold(-repairCost,false))
			return;

		targetBuilding.modifyHealth(BuildingManager.GetMissingHealth(targetBuilding), null);

		UpdateObjectMsg uom = new UpdateObjectMsg(targetBuilding,3);

		dispatch = Dispatch.borrow(origin.getPlayerCharacter(), uom);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);



		RepairBuildingMsg rbm = new RepairBuildingMsg( targetBuilding.getObjectUUID(),  maxHP, missingHealth, repairCost, targetBuilding.getStrongboxValue());


		dispatch = Dispatch.borrow(origin.getPlayerCharacter(), rbm);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	}



}
