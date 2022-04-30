package engine.net.client.handlers;

import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.TaxResourcesMsg;
import engine.objects.Building;
import engine.objects.City;
import engine.objects.PlayerCharacter;

/*
 * @Author:
 * @Summary: Processes application protocol message which handles
 * protecting and unprotecting city assets
 */
public class TaxResourcesMsgHandler extends AbstractClientMsgHandler {

	public TaxResourcesMsgHandler() {
		super(TaxResourcesMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		PlayerCharacter player;
		TaxResourcesMsg msg;

		player = origin.getPlayerCharacter();
		if (player == null)
			return true;


		msg = (TaxResourcesMsg) baseMsg;

		TaxWarehouse(msg,player);



		return true;

	}

	private static boolean TaxWarehouse(TaxResourcesMsg msg, PlayerCharacter player) {

		// Member variable declaration
		Building building = BuildingManager.getBuildingFromCache(msg.getBuildingID());


		if (building == null){
			ErrorPopupMsg.sendErrorMsg(player, "Not a valid Building!");
			return true;
		}

		City city = building.getCity();
		if (city == null){
			ErrorPopupMsg.sendErrorMsg(player, "This building does not belong to a city.");
			return true;
		}

		city.TaxWarehouse(msg, player);


		return true;


	}


}
