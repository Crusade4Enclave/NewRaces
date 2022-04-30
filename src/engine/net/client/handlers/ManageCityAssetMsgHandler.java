package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.Enum.GameObjectType;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.SessionManager;
import engine.gameManager.ZoneManager;
import engine.math.Bounds;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.ManageCityAssetsMsg;
import engine.net.client.msg.PlaceAssetMsg;
import engine.objects.*;
import org.joda.time.DateTime;

/*
 * @Author:
 * @Summary: Processes application protocol message which opens
 * and processes the various building asset management windows.
 */
public class ManageCityAssetMsgHandler extends AbstractClientMsgHandler {

	public ManageCityAssetMsgHandler() {
		super(ManageCityAssetsMsg.class);
	}

	public static boolean playerCanManageNotFriends(PlayerCharacter player, Building building){

		//Player Can only Control Building if player is in Same Guild as Building and is higher rank than IC.

		if (player == null)
			return false;

		if (building.getRank() == -1)
			return false;

		if (BuildingManager.IsOwner(building, player))
			return true;

		if (GuildStatusController.isGuildLeader(player.getGuildStatus()) == false && GuildStatusController.isInnerCouncil(player.getGuildStatus()) == false)
			return false;

		//Somehow guild leader check fails above? lets check if Player is true Guild GL.
		if (building.getGuild() != null && building.getGuild().isGuildLeader(player.getObjectUUID()))
			return true;

		return Guild.sameGuild(building.getGuild(), player.getGuild());

	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		ManageCityAssetsMsg msg;
		PlayerCharacter player;
		ManageCityAssetsMsg outMsg;
		Building building;

		msg = (ManageCityAssetsMsg) baseMsg;

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;

		building = BuildingManager.getBuildingFromCache(msg.getTargetID());

		if (building == null){
			if (msg.actionType == 14) {
			
				Zone zone = ZoneManager.findSmallestZone(player.getLoc());
				
				if (!zone.isPlayerCity()){
					ErrorPopupMsg.sendErrorMsg(player, "Unable to find city to command.");
					return true;
				}
				
				City city = City.GetCityFromCache(zone.getPlayerCityUUID());
				
				if (city == null){
					ErrorPopupMsg.sendErrorMsg(player, "Unable to find city to command.");
					return true;
				}
				
				if (!city.getGuild().equals(player.getGuild())){
					ErrorPopupMsg.sendErrorMsg(player, "You are not in the correct guild to command this city.");
					return true;
				}
				
				if (!GuildStatusController.isInnerCouncil(player.getGuildStatus()) && !GuildStatusController.isGuildLeader(player.getGuildStatus())){
					ErrorPopupMsg.sendErrorMsg(player, "You must be an Inner Council or Guild leader to access city commands.");
					return true;
				}
				ManageCityAssetsMsg mca = new ManageCityAssetsMsg(player, building);
				mca.actionType = 15;
				Dispatch dispatch = Dispatch.borrow(player, mca);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			}
			return true;
		}

		outMsg = new ManageCityAssetsMsg(player, building);

		if (player.isSafeMode()){
			outMsg.actionType = 4;
			outMsg.setTargetType(building.getObjectType().ordinal());
			outMsg.setTargetID(building.getObjectUUID());
			outMsg.setAssetName(building.getName());
			 Dispatch dispatch = Dispatch.borrow(player, outMsg);
	            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		}

		if (msg.actionType == 2 || msg.actionType == 22) {

			if (building.getBlueprint() != null && building.getBlueprint().getBuildingGroup() == engine.Enum.BuildingGroup.BANESTONE) {

				outMsg.actionType = 18;
				outMsg.setTargetType(building.getObjectType().ordinal());
				outMsg.setTargetID(building.getObjectUUID());

			} else if (BuildingManager.playerCanManage(player, building)) { //TODO allow Friends list.
				configWindowState(player, building, outMsg);
				outMsg.actionType = 3;
				outMsg.setTargetType(building.getObjectType().ordinal());
				outMsg.setTargetID(building.getObjectUUID());
				outMsg.setTargetType3(building.getObjectType().ordinal());
				outMsg.setTargetID3(building.getObjectUUID());
				outMsg.setUnknown54(1);

			} else {
				
				if (building.getBlueprintUUID() != 0)
					switch (building.getBlueprint().getBuildingGroup()) {
					case SHRINE:
						if (building.getRank() == -1) {
							if (!Bounds.collide(player.getLoc(), building)) {
								ErrorPopupMsg.sendErrorPopup(player, 64);
								return true;
							}

							Shrine shrine = Shrine.shrinesByBuildingUUID.get(building.getObjectUUID());

							if (shrine == null)
								return true;

							if (shrine.getFavors() == 0) {
								ErrorPopupMsg.sendErrorPopup(player, 166); // There is no more favor in this shrine to loot
								return true;
							}

							BuildingManager.lootBuilding(player, building);
							return true;
						}
						break;
					case WAREHOUSE:
						//TODO check
						if (building.getRank() == -1) {
							if (!Bounds.collide(player.getLoc(), building)) {
								ErrorPopupMsg.sendErrorPopup(player, 64);
								return true;
							}

							Warehouse warehouse = Warehouse.warehouseByBuildingUUID.get(building.getObjectUUID());

							if (warehouse == null)
								return true;

							if (warehouse.isEmpty()) {
								ErrorPopupMsg.sendErrorPopup(player, 167); // no more resources.
								return true;
							}

							BuildingManager.lootBuilding(player, building);
							return true;
						}
					}

				if (building.getRank() == -1)
					return true;

				AbstractCharacter owner = building.getOwner();

				//no owner, send building info
				if (owner == null) {
					msg.actionType = 4;

					Dispatch dispatch = Dispatch.borrow(player, msg);
		            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
					return true;
				}
				outMsg.actionType = 4;
				outMsg.setTargetType(building.getObjectType().ordinal());
				outMsg.setTargetID(building.getObjectUUID());
				outMsg.setAssetName(building.getName());

			}
			 Dispatch dispatch = Dispatch.borrow(player, outMsg);
	            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	            return true;
		}

		if (msg.actionType == 13) {
			outMsg.actionType = 13;
			Dispatch dispatch = Dispatch.borrow(player, outMsg);
            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
            return true;
		}

		

		//Rename Building.

		if (msg.actionType == 5) {

			//TODO we need to check names before allowing
			building.setName(msg.getAssetName());
			configWindowState(player, building, outMsg);

			outMsg.actionType = 3;
			outMsg.setTargetType(building.getObjectType().ordinal());
			outMsg.setTargetID(building.getObjectUUID());
			outMsg.setTargetType3(GameObjectType.Building.ordinal());
			outMsg.setTargetID3(building.getObjectUUID());
			outMsg.setAssetName1(building.getName());
			outMsg.setUnknown54(1);

            Dispatch dispatch = Dispatch.borrow(player, outMsg);
            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
            
            return true;

			//TOL, update city name also
			//TODO update city and zone in database
			//TODO update city map data in game server
		}

		if (msg.actionType == 14) {
			ManageCityAssetsMsg mca = new ManageCityAssetsMsg(player, building);
			mca.actionType = 15;
			Dispatch dispatch = Dispatch.borrow(player, mca);
	            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	            return true;
		}

		if (msg.actionType == 20) {

			Zone baneZone = building.getParentZone();

			if (baneZone == null)
				return true;

			City banedCity = City.getCity(baneZone.getPlayerCityUUID());

			if (banedCity == null)
				return true;

			Bane bane = banedCity.getBane();

			if (bane == null || bane.getLiveDate() != null || player.getGuild() != banedCity.getGuild() || GuildStatusController.isInnerCouncil(player.getGuildStatus()) == false)
				return true;

			int baneHour = msg.getBaneHour();

			if (baneHour < 16 || baneHour > 24) {
				PlaceAssetMsg.sendPlaceAssetError(origin, 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
				return true;
			}

			DateTime baneLive = new DateTime(bane.getPlacementDate());
			baneLive = baneHour == 24 ? baneLive.plusDays(3) : baneLive.plusDays(2);
			baneLive = baneHour == 24 ? baneLive.hourOfDay().setCopy(0) : baneLive.hourOfDay().setCopy(baneHour);
			baneLive = baneLive.minuteOfHour().setCopy(0);
			baneLive = baneLive.secondOfMinute().setCopy(1);
			bane.setLiveDate(baneLive);
			outMsg.actionType = 18;

			Dispatch dispatch = Dispatch.borrow(player, outMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		}
		return true;
	}

	public void configWindowState(PlayerCharacter player, Building building, ManageCityAssetsMsg manageCityAssetsMsg) {

		// Tests to turn on upgrade button if a building is not
		// at it's maximum allowed rank or currently ranking


		// Owner is obviously allowed to upgrade his own buildings

		if (building.getOwner().equals(player)) {

			// Players cannot destroy or transfer a TOL.

			if (building.getBlueprint() == null){
				manageCityAssetsMsg.buttonDestroy = 0;
				manageCityAssetsMsg.buttonTransfer = 0;
				manageCityAssetsMsg.buttonAbandon = 0;
				manageCityAssetsMsg.buttonUpgrade = 0;
			}
			else
			if (building.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.TOL) {
				manageCityAssetsMsg.buttonDestroy = 0;
				manageCityAssetsMsg.buttonTransfer = 0;
				manageCityAssetsMsg.buttonAbandon = 1;
				manageCityAssetsMsg.buttonUpgrade = 1;
			}
			else if (building.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.MINE) {
				manageCityAssetsMsg.buttonDestroy = 0;
				manageCityAssetsMsg.buttonTransfer = 0;
				manageCityAssetsMsg.buttonAbandon = 0;
				manageCityAssetsMsg.buttonUpgrade = 0;  // Cannot upgrade a mine
			}
			else{
				manageCityAssetsMsg.buttonDestroy = 1;
				manageCityAssetsMsg.buttonTransfer = 1;
				manageCityAssetsMsg.buttonAbandon = 1;
				manageCityAssetsMsg.buttonUpgrade = 1;
			}
		}

		// Inner Council of the same guild can also upgrade

		if ((player.getGuild().equals(building.getGuild())) &&
				GuildStatusController.isInnerCouncil(player.getGuildStatus()))
			manageCityAssetsMsg.buttonUpgrade = 1;

		// Disable upgrade button if at max rank.

		if (building.getBlueprint() == null)
			manageCityAssetsMsg.buttonUpgrade = 0;
		else
		if (building.getRank() >= building.getBlueprint().getMaxRank())
			manageCityAssetsMsg.buttonUpgrade = 0;;

		// If a building is not protected we can exit here

		if (building.assetIsProtected() == false)
			return;

		// Protection is displayed as "UNDER SIEGE" if
		// an active bane is invalidating the protection
		// contracts of the city.

		if ((building.getCity() != null) &&
				(building.getCity().protectionEnforced == false)) {
			manageCityAssetsMsg.labelProtected = 0;
			manageCityAssetsMsg.labelSiege = 1;
			manageCityAssetsMsg.labelCeaseFire = 0;
			return;
		}

		// Building is currently protected by a TOL

		manageCityAssetsMsg.labelProtected = 1;
	}
}
