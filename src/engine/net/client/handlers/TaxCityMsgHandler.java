package engine.net.client.handlers;

import engine.Enum;
import engine.InterestManagement.RealmMap;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.TaxCityMsg;
import engine.net.client.msg.ViewResourcesMessage;
import engine.objects.*;

/*
 * @Author:
 * @Summary: Processes application protocol message which handles
 * protecting and unprotecting city assets
 */
public class TaxCityMsgHandler extends AbstractClientMsgHandler {

	public TaxCityMsgHandler() {
		super(TaxCityMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		PlayerCharacter player;
		TaxCityMsg msg;

		player = origin.getPlayerCharacter();


		msg = (TaxCityMsg) baseMsg;

		ViewTaxes(msg,player);



		return true;

	}

	private static boolean ViewTaxes(TaxCityMsg msg, PlayerCharacter player) {

		// Member variable declaration
		Building building = BuildingManager.getBuildingFromCache(msg.getGuildID());
		Guild playerGuild = player.getGuild();

		if (building == null){
			ErrorPopupMsg.sendErrorMsg(player, "Not a valid Building!");
			return true;
		}

		City city = building.getCity();
		if (city == null){
			ErrorPopupMsg.sendErrorMsg(player, "This building does not belong to a city.");
			return true;
		}

		if (city.getWarehouse() == null){
			ErrorPopupMsg.sendErrorMsg(player, "This city does not have a warehouse!");
			return true;
		}


		if (playerGuild == null || playerGuild.isEmptyGuild()){
			ErrorPopupMsg.sendErrorMsg(player, "You must belong to a guild to do that!");
			return true;
		}

		if (playerGuild.getOwnedCity() == null){
			ErrorPopupMsg.sendErrorMsg(player, "Your Guild needs to own a city!");
			return true;
		}

		if (playerGuild.getOwnedCity().getWarehouse() == null){
			ErrorPopupMsg.sendErrorMsg(player, "Your Guild needs to own a warehouse!");
			return true;
		}

		if (playerGuild.getOwnedCity().getTOL() == null){
			ErrorPopupMsg.sendErrorMsg(player, "Cannot find Tree of Life for your city!");
			return true;
		}

//		if (playerGuild.getOwnedCity().getTOL().getRank() != 8){
//			ErrorPopupMsg.sendErrorMsg(player, "Your City needs to Own a realm!");
//			return true;
//		}

		if (playerGuild.getOwnedCity().getRealm() == null){
			ErrorPopupMsg.sendErrorMsg(player, "Cannot find realm for your city!");
			return true;
		}
		Realm targetRealm = RealmMap.getRealmForCity(city);

		if (targetRealm == null){
			ErrorPopupMsg.sendErrorMsg(player, "Cannot find realm for city you are attempting to tax!");
			return true;
		}

//		if (targetRealm.getRulingCity() == null){
//			ErrorPopupMsg.sendErrorMsg(player, "Realm Does not have a ruling city!");
//			return true;
//		}

//		if (targetRealm.getRulingCity().getObjectUUID() != playerGuild.getOwnedCity().getObjectUUID()){
//			ErrorPopupMsg.sendErrorMsg(player, "Your guild does not rule this realm!");
//			return true;
//		}

//		if (playerGuild.getOwnedCity().getObjectUUID() == city.getObjectUUID()){
//			ErrorPopupMsg.sendErrorMsg(player, "You cannot tax your own city!");
//			return true;
//		}




		if (!GuildStatusController.isTaxCollector(player.getGuildStatus())){
			ErrorPopupMsg.sendErrorMsg(player, "You Must be a tax Collector!");
			return true;
		}


//		if (!city.isAfterTaxPeriod(DateTime.now(), player))
//			return true;



		ViewResourcesMessage vrm = new ViewResourcesMessage(player);
		vrm.setGuild(building.getGuild());
		vrm.setWarehouseBuilding(BuildingManager.getBuildingFromCache(building.getCity().getWarehouse().getBuildingUID()));
		vrm.configure();
		Dispatch dispatch = Dispatch.borrow(player, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		dispatch = Dispatch.borrow(player, vrm);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		return true;



	}





}
