package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.Enum.GuildState;
import engine.exception.MsgSendException;
import engine.gameManager.*;
import engine.net.client.ClientConnection;
import engine.net.client.msg.AbandonAssetMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.objects.*;
import org.pmw.tinylog.Logger;

import java.util.concurrent.ConcurrentHashMap;

/*
 * @Author:
 * @Summary: Processes application protocol message which processes
 * client requests to abandon a building.
 */
public class AbandonAssetMsgHandler extends AbstractClientMsgHandler {

	// Instance variables

	public AbandonAssetMsgHandler() {
		super(AbandonAssetMsg.class);

	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration
		PlayerCharacter player;
		Building building;
		AbandonAssetMsg msg;

		// Member variable assignment
		msg = (AbandonAssetMsg) baseMsg;

		player = origin.getPlayerCharacter();
		building = BuildingManager.getBuildingFromCache(msg.getUUID());

		// Oops!  *** Refactor: Log error
		if ((player == null) || (building == null))
			return true;

		// Early exit if object is not owned by the player
		if (building.getOwnerUUID() != player.getObjectUUID())
			return true;

		// Cannot abandon a building without a blueprint.
		// Players do not own rocks or shrubbery.
		if (building.getBlueprintUUID() == 0)
			return true;

		// Players cannot abandon shrines

		if ((building.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to abandon shrine!");
			return true;
		}

		if ((building.getBlueprint().getBuildingGroup() == BuildingGroup.MINE)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot abandon mine!");
			return true;
		}

		if (Blueprint.isMeshWallPiece(building.getBlueprintUUID())) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to abandon fortress asset!");
			return true;
		}

		if ((building.getBlueprint().getBuildingGroup() == BuildingGroup.BARRACK)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to abandon fortress asset!");
			return true;
		}

		if ((building.getBlueprint().getBuildingGroup() == BuildingGroup.BULWARK)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to abandon siege asset!");
			return true;
		}

		if ((building.getBlueprint().getBuildingGroup() == BuildingGroup.SIEGETENT)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to abandon siege asset!");
			return true;
		}

		if ((building.getBlueprint().getBuildingGroup() == BuildingGroup.BANESTONE)) {
			ErrorPopupMsg.sendErrorMsg(player, "Cannot for to abandon banestone!");
			return true;
		}

		// Trees require special handling beyond an individual building
		if ((building.getBlueprint().getBuildingGroup() == BuildingGroup.TOL))
		{
			// CHECK IF GUILD HAS A BANE DROPPED
			City city = ZoneManager.getCityAtLocation(building.getLoc());
			if(city.getGuild().getSubGuildList().isEmpty() == false)
			{
				//nations cant abandon their tree
				ErrorPopupMsg.sendErrorMsg(player, "Nations Cannot Abandon Their Capital!");
				return true;
			}
			if(Bane.getBaneByAttackerGuild(city.getGuild()) != null)
			{
				ErrorPopupMsg.sendErrorMsg(player, "You Cannot Abandon Your Tree With An Active Siege!");
				return true;
			}

			AbandonAllCityObjects(player, building);
		}
		else
			AbandonSingleAsset(player, building);

		return true;
	}

	private static void AbandonSingleAsset(PlayerCharacter sourcePlayer,
                                           Building targetBuilding) {

		// Transfer the building asset ownership and refresh all clients

		DbManager.BuildingQueries.CLEAR_FRIENDS_LIST(targetBuilding.getObjectUUID());
		targetBuilding.getFriends().clear();

		// Clear protection status but only if a seige building

        if (targetBuilding.getBlueprint().getBuildingGroup().equals(BuildingGroup.BULWARK) ||
			targetBuilding.getBlueprint().getBuildingGroup().equals(BuildingGroup.SIEGETENT))
				targetBuilding.setProtectionState(Enum.ProtectionState.NONE);

		DbManager.BuildingQueries.CLEAR_CONDEMNED_LIST(targetBuilding.getObjectUUID());
		targetBuilding.getCondemned().clear();
		targetBuilding.setOwner(null);
		targetBuilding.refreshGuild();

	}

	private void AbandonAllCityObjects(PlayerCharacter sourcePlayer,
			Building targetBuilding) {
		Guild sourceGuild;
		Zone cityZone;

		sourceGuild = sourcePlayer.getGuild();

		if (sourceGuild == null)
			return;

		if (sourceGuild.getSubGuildList().size() > 0) {
			ChatManager.chatCityError(sourcePlayer, "You Cannot abandon a nation city.");
			return;
		}
		
		

		cityZone = ZoneManager.findSmallestZone(targetBuilding.getLoc());

		// Can't abandon a tree not within a player city zone
		if (cityZone.isPlayerCity() == false)
			return;
		
		if (targetBuilding.getCity() == null)
			return;
		
		if (targetBuilding.getCity().getBane() != null){
			ErrorPopupMsg.sendErrorMsg(sourcePlayer, "Can't abandon Tree while a bane exists.");
			return;
		}

		if (targetBuilding.getCity().hasBeenTransfered == true) {
			ChatManager.chatCityError(sourcePlayer, "City can only be abandoned once per rebooting.");
			return;
		}

		// Guild no longer owns his tree.
		if (!DbManager.GuildQueries.SET_GUILD_OWNED_CITY(sourceGuild.getObjectUUID(), 0)) {
			Logger.error("Failed to update Owned City to Database");
			return;
		}

		sourceGuild.setCityUUID(0);
		sourceGuild.setGuildState(GuildState.Errant);
		sourceGuild.setNation(null);

		// Transfer the city assets
		TransferCityAssets(sourcePlayer, targetBuilding);

		GuildManager.updateAllGuildTags(sourceGuild);
		GuildManager.updateAllGuildBinds(sourceGuild, null);

	}

	private void TransferCityAssets(PlayerCharacter sourcePlayer,
			Building cityTOL) {

		Zone cityZone;

		// Build list of buildings within this parent zone
		cityZone = ZoneManager.findSmallestZone(cityTOL.getLoc());

		for (Building cityBuilding : cityZone.zoneBuildingSet) {

			Blueprint cityBlueprint;
			cityBlueprint = cityBuilding.getBlueprint();

			// Buildings without blueprints cannot be abandoned
			if (cityBlueprint == null)
				continue;

			// Transfer ownership of valid city assets
			if ((cityBlueprint.getBuildingGroup() == BuildingGroup.TOL)
					|| (cityBlueprint.getBuildingGroup() == BuildingGroup.SPIRE)
					|| (cityBlueprint.getBuildingGroup() == BuildingGroup.BARRACK)
					|| (cityBlueprint.isWallPiece())
					|| (cityBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE))
				AbandonSingleAsset(sourcePlayer, cityBuilding);

		}

	}

}
