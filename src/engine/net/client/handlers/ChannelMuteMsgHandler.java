package engine.net.client.handlers;

import engine.Enum.BuildingGroup;
import engine.Enum.GuildState;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.GuildManager;
import engine.gameManager.ZoneManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ChatFilterMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.*;
import org.pmw.tinylog.Logger;

/*
 * @Author:
 * @Summary: Processes application protocol message which processes
 * client requests to abandon a building.
 */
public class ChannelMuteMsgHandler extends AbstractClientMsgHandler {

	// Instance variables

	public ChannelMuteMsgHandler() {
		super(ChatFilterMsg.class);

	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		return true;
	}

	private static void AbandonSingleAsset(PlayerCharacter sourcePlayer,
                                           Building targetBuilding) {

		// Transfer the building asset ownership and refresh all clients

		DbManager.BuildingQueries.CLEAR_FRIENDS_LIST(targetBuilding.getObjectUUID());
		targetBuilding.getFriends().clear();

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

		if (targetBuilding.getCity().hasBeenTransfered == true) {
			ChatManager.chatCityError(sourcePlayer, "City can only be abandoned once per rebooting.");
			return;
		}

		// Guild no longer owns his tree.
		if (!DbManager.GuildQueries.SET_GUILD_OWNED_CITY(sourceGuild.getObjectUUID(), 0)) {
			Logger.error( "Failed to update Owned City to Database");
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
