package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.Building;
import engine.objects.PlayerCharacter;
import org.pmw.tinylog.Logger;

import java.time.LocalDateTime;

import static engine.net.client.msg.ErrorPopupMsg.sendErrorPopup;

/*
 *
 * @Summary: Processes application protocol message where a
 * client requests that a building be upgraded.
 */
public class UpgradeAssetMsgHandler extends AbstractClientMsgHandler {

	// Constructor
	public UpgradeAssetMsgHandler() {

		super(UpgradeAssetMessage.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		UpgradeAssetMessage msg;
		ManageCityAssetsMsg outMsg;
		PlayerCharacter player;
		int buildingUUID;
		Building buildingToRank;
		LocalDateTime dateToUpgrade;
		int nextRank;
		int rankCost;
		Dispatch dispatch;

		// Assign member variables

		msg = (UpgradeAssetMessage) baseMsg;

		// Grab pointer to the requesting player

		player = SessionManager.getPlayerCharacter(origin);

		// Grab pointer to the building from the cache

		buildingUUID = msg.getBuildingUUID();

		buildingToRank = (Building) DbManager.getObject(GameObjectType.Building, buildingUUID);

		// Early exit if building not in cache.

		if (buildingToRank == null) {
			Logger.error("Attempt to upgrade null building by " + player.getName());
			return true;
		}

		// Early exit for building that is already ranking

		if (buildingToRank.isRanking()) {
			Logger.error("Attempt to upgrade a building already ranking by " + player.getName());
			return true;
		}

		// Calculate and set time/cost to upgrade

		nextRank = (buildingToRank.getRank() + 1);

		if (buildingToRank.getBlueprint() == null)
			return true;
		if (buildingToRank.getBlueprint().getMaxRank() < nextRank || nextRank == 8){
			ErrorPopupMsg.sendErrorMsg(player, "Building is already at it's Max rank.");
			return true;
		}

		rankCost = buildingToRank.getBlueprint().getRankCost(nextRank);

		// SEND NOT ENOUGH GOLD ERROR

		if (!buildingToRank.hasFunds(rankCost)){
			ErrorPopupMsg.sendErrorPopup(player, 127); // Not enough gold in strongbox
			return true;
		}

		if (rankCost > buildingToRank.getStrongboxValue()) {
			sendErrorPopup(player, 127);
			return true;
		}

		// Validation appears good.  Let's now process the upgrade
		
		try {
			if (buildingToRank.getCity() != null){
				buildingToRank.getCity().transactionLock.writeLock().lock();
				try{
					if (!buildingToRank.transferGold(-rankCost,false)) {
						sendErrorPopup(player, 127);
						return true;
					}
				}catch(Exception e){
					Logger.error(e);
				}finally{
					buildingToRank.getCity().transactionLock.writeLock().unlock();
				}
			}else
			if (!buildingToRank.transferGold(-rankCost,false)) {
				sendErrorPopup(player, 127);
				return true;
			}

			dateToUpgrade = LocalDateTime.now().plusHours(buildingToRank.getBlueprint().getRankTime(nextRank));

			BuildingManager.setUpgradeDateTime(buildingToRank, dateToUpgrade, 0);

			// Schedule upgrade job

			BuildingManager.submitUpgradeJob(buildingToRank);

			// Refresh the client's manage asset window
			// *** Refactor : We have some of these unknowns

			outMsg = new ManageCityAssetsMsg(player, buildingToRank);

			// Action TYPE
			outMsg.actionType = 3;
			outMsg.setTargetType(buildingToRank.getObjectType().ordinal());
			outMsg.setTargetID(buildingToRank.getObjectUUID());
			outMsg.setTargetType3(buildingToRank.getObjectType().ordinal());
			outMsg.setTargetID3(buildingToRank.getObjectUUID());
			outMsg.setAssetName1(buildingToRank.getName());
			outMsg.setUnknown54(1);

			dispatch = Dispatch.borrow(player, outMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		} catch (Exception e) {
			PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
		}

		return true;
	}
}
