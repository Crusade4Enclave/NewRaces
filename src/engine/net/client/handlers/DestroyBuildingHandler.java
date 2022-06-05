package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.InterestManagement.WorldGrid;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.DestroyBuildingMsg;
import engine.objects.*;

/*
 * @Author:
 * @Summary: Processes application protocol message where the
 * client is requesting a building be destroyed.
 */

public class DestroyBuildingHandler extends AbstractClientMsgHandler {

	public DestroyBuildingHandler() {
		super(DestroyBuildingMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		
		PlayerCharacter pc = origin.getPlayerCharacter();

		DestroyBuildingMsg msg;
		msg = (DestroyBuildingMsg) baseMsg;

		int buildingUUID = msg.getUUID();

		Building building = BuildingManager.getBuildingFromCache(buildingUUID);
;
		if (pc == null || building == null)
			return true;

		Blueprint blueprint;

		blueprint = building.getBlueprint();
		City city = building.getCity();
		// Can't destroy buildings without a blueprint.

		if (blueprint == null)
			return true;

		// Cannot destroy Oblivion database derived buildings.

		if (building.getProtectionState() == Enum.ProtectionState.NPC) {
			return true;
		}

		if (!BuildingManager.PlayerCanControlNotOwner(building, pc))
			return true;
Bane bane = city.getBane();
if(bane.getSiegePhase() == Enum.SiegePhase.WAR && bane != null) {
	return true;
}
		// Can't destroy a tree of life
		if (blueprint.getBuildingGroup() == BuildingGroup.TOL)
			return true;

		// Can't destroy a shrine
		if (blueprint.getBuildingGroup() == BuildingGroup.SHRINE)
			return true;

		if (blueprint.getBuildingGroup() == BuildingGroup.MINE)
			return true;

		if (blueprint.getBuildingGroup() == BuildingGroup.RUNEGATE)
			return true;
//stop if active siege
		// Turn off spire if destoying
		if (blueprint.getBuildingGroup() == BuildingGroup.SPIRE)
			building.disableSpire(true);

		if (blueprint.getBuildingGroup() == BuildingGroup.WAREHOUSE) {
			if (city != null)
				city.setWarehouseBuildingID(0);
		}

		building.setRank(-1);
		WorldGrid.RemoveWorldObject(building);
		WorldGrid.removeObject(building);
		building.getParentZone().zoneBuildingSet.remove(building);

		return true;
	}

}