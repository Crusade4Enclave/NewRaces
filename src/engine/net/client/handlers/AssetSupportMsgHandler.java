package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.SupportMsgType;
import engine.Enum.TaxType;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.*;
import org.pmw.tinylog.Logger;

/*
 * @Author:
 * @Summary: Processes application protocol message which handles
 * protecting and unprotecting city assets
 */
public class AssetSupportMsgHandler extends AbstractClientMsgHandler {

	public AssetSupportMsgHandler() {
		super(AssetSupportMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		PlayerCharacter player;
		NPC vendor;
		Building targetBuilding;
		Dispatch dispatch;

		AssetSupportMsg msg;
		CityAssetMsg outMsg;

		// Member variable assignment

		msg = (AssetSupportMsg) baseMsg;

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;

		vendor = NPC.getFromCache(msg.getNpcID());

		if (msg.getMessageType() !=6 && msg.getMessageType() != 7){
			if (vendor == null)
				return true;

			vendor.getBuilding();

			if (vendor.getBuilding() == null)
				return true;
		}


	SupportMsgType supportType = SupportMsgType.typeLookup.get(msg.getMessageType());

		if (supportType == null) {
			supportType = Enum.SupportMsgType.NONE;
			Logger.error("No enumeration for support type" + msg.getMessageType());
		}
		switch (supportType) {

		case PROTECT:
			targetBuilding =  BuildingManager.getBuildingFromCache(msg.getProtectedBuildingID());
			protectAsset(msg,targetBuilding, vendor, origin);
			break;
		case UNPROTECT:
			targetBuilding =  BuildingManager.getBuildingFromCache(msg.getProtectedBuildingID());
			unprotectAsset(targetBuilding, vendor, origin);
			break;
		case VIEWUNPROTECTED:
			outMsg = new CityAssetMsg();
			outMsg.setBuildingID(msg.getBuildingID());
			outMsg.configure();

			dispatch = Dispatch.borrow(player, outMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;

		case REMOVETAX:
			targetBuilding = BuildingManager.getBuildingFromCache(msg.getBuildingID());

			if (targetBuilding == null)
				return true;

			targetBuilding.removeTaxes();
			unprotectAsset(targetBuilding, null, origin);

			ManageCityAssetsMsg mca = new ManageCityAssetsMsg(origin.getPlayerCharacter(),targetBuilding);

			// Action TYPE
			mca.actionType = 3;
			mca.setTargetType(targetBuilding.getObjectType().ordinal());
			mca.setTargetID(targetBuilding.getObjectUUID());
			mca.setTargetType3(targetBuilding.getObjectType().ordinal());
			mca.setTargetID3(targetBuilding.getObjectUUID());
			mca.setAssetName1(targetBuilding.getName());
			mca.setUnknown54(1);
			dispatch = Dispatch.borrow(player, mca);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			return true;

			case ACCEPTTAX: //AcceptTax

			targetBuilding = BuildingManager.getBuildingFromCache(msg.getBuildingID());

			if (targetBuilding == null)
				return true;

			targetBuilding.acceptTaxes();

			mca = new ManageCityAssetsMsg(origin.getPlayerCharacter(),targetBuilding);

				// Action TYPE
				mca.actionType = 3;
				mca.setTargetType(targetBuilding.getObjectType().ordinal());
			mca.setTargetID(targetBuilding.getObjectUUID());
			mca.setTargetType3(targetBuilding.getObjectType().ordinal());
			mca.setTargetID3(targetBuilding.getObjectUUID());
			mca.setAssetName1(targetBuilding.getName());
			mca.setUnknown54(1);

			dispatch = Dispatch.borrow(player, mca);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			return true;
		}

		dispatch = Dispatch.borrow(player, baseMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		return true;

	}

	private static void protectAsset(AssetSupportMsg msg, Building targetBuilding, NPC vendor, ClientConnection origin) {

		// Member variable declaration

		Zone serverZone;
		City serverCity;
		ManageNPCMsg outMsg;
		int protectionSlots;
		Dispatch dispatch;

		// Member variable assignment

		if (targetBuilding == null)
			return;

		serverZone = vendor.getParentZone();

		if (serverZone == null)
			return;

		serverCity = City.GetCityFromCache(serverZone.getPlayerCityUUID());

		if (serverCity == null)
			return;

		if (!serverCity.isLocationOnCityZone(targetBuilding.getLoc())){
			ErrorPopupMsg.sendErrorMsg(origin.getPlayerCharacter(), "Structura must be on city zone for to protect.");
			return;
		}

		if ((serverCity.getTOL() == null)|| (serverCity.getTOL().getRank() < 1))
			return;

		if (serverCity.protectionEnforced == false) {
			ErrorPopupMsg.sendErrorMsg(origin.getPlayerCharacter(), "Runemaster can not protect structura during bane!");
			return;
		}

		if (serverCity.getRuneMaster() == null) {
			ErrorPopupMsg.sendErrorMsg(origin.getPlayerCharacter(), "Runemaster is needed for to protect structura!");
			return;
		}

		// Enforce runemaster protection limits

		protectionSlots = (2 * serverCity.getRuneMaster().getRank())  + 6;

		if (serverCity.getRuneMaster().getProtectedBuildings().size() >=
				protectionSlots) {
			ErrorPopupMsg.sendErrorMsg(origin.getPlayerCharacter(), "Runemaster can only protect " + protectionSlots + " structura!");
			return;
		}

		if (msg.getWeeklyTax() != 0){
			if (!serverCity.getTOL().addProtectionTax(targetBuilding, origin.getPlayerCharacter(), TaxType.WEEKLY, msg.getWeeklyTax(), msg.getEnforceKOS() == 1 ? true: false)){
				ErrorPopupMsg.sendErrorMsg(origin.getPlayerCharacter(), "Failed to add taxes to building.");
				return;
			}
			targetBuilding.setProtectionState(Enum.ProtectionState.PENDING);
		}else if (msg.getProfitTax() != 0){
			if (!serverCity.getTOL().addProtectionTax(targetBuilding, origin.getPlayerCharacter(), TaxType.PROFIT, msg.getProfitTax(), msg.getEnforceKOS() == 1 ? true: false)){
				ErrorPopupMsg.sendErrorMsg(origin.getPlayerCharacter(), "Failed to add taxes to building.");
				return;
			}
			targetBuilding.setProtectionState(Enum.ProtectionState.PENDING);
		}else
			targetBuilding.setProtectionState(Enum.ProtectionState.CONTRACT);



		outMsg = new ManageNPCMsg(vendor);
		dispatch = Dispatch.borrow(origin.getPlayerCharacter(), outMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	}

	private static void unprotectAsset(Building targetBuilding, NPC vendor, ClientConnection origin) {

		if (targetBuilding == null)
			return;

		// Early exit if UUID < the last database derived building UUID.

		if (targetBuilding.getProtectionState() == Enum.ProtectionState.NPC) {
			return;
		}

		if (targetBuilding.getProtectionState() == engine.Enum.ProtectionState.NONE)
			return;

		if (GuildStatusController.isInnerCouncil(origin.getPlayerCharacter().getGuildStatus()) == false)
			return;

		targetBuilding.removeTaxes();

		targetBuilding.setProtectionState(engine.Enum.ProtectionState.NONE);

	}

}
