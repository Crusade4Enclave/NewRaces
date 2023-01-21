// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.*;
import org.pmw.tinylog.Logger;

import java.util.concurrent.locks.ReentrantReadWriteLock;


/*
 * @Author:
 * @Summary: Processes application protocol message which keeps
 * client's tcp connection open.
 */
public class ClaimGuildTreeMsgHandler extends AbstractClientMsgHandler {

	// Instance variables

	private final ReentrantReadWriteLock claimLock = new ReentrantReadWriteLock();
	private static final int RENAME_TREE = 2;
	private static final int BIND_TREE = 3;
	private static final int OPEN_CITY = 4;
	private static final int CLOSE_CITY = 5;

	public ClaimGuildTreeMsgHandler() {
		super(ClaimGuildTreeMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration
		this.claimLock.writeLock().lock();

		try{
			PlayerCharacter sourcePlayer;
			Building building;
			Blueprint blueprint;
			Zone playerZone= null;
			City playerCity= null;
			ClaimGuildTreeMsg msg;
			int targetUUID;
			Dispatch dispatch;

			msg = (ClaimGuildTreeMsg) baseMsg;
			targetUUID = msg.getTargetID();

			sourcePlayer = origin.getPlayerCharacter();
			building = BuildingManager.getBuildingFromCache(targetUUID);

			if (building != null)
				playerZone = building.getParentZone();

			if (playerZone != null)
				playerCity = City.getCity(playerZone.getPlayerCityUUID());

			// Oops!  *** Refactor: Log error
			switch (msg.getMessageType()){
			case RENAME_TREE:
				if ((sourcePlayer == null) ||
						(building == null) || playerZone == null ||playerCity == null)
					return true;

				// Early exit if object to be claimed is not errant

				if (building.getOwnerUUID() == 0)
					return true;

				// Errant players cannot rename

				if (sourcePlayer.getGuild().isEmptyGuild())
					return true;

				// Can't rename an object without a blueprint

				if (building.getBlueprintUUID() == 0)
					return true;

				blueprint = building.getBlueprint();

				//can only rename tree this way.
				if (blueprint.getBuildingGroup() != BuildingGroup.TOL)
					return true;

				//dont rename if guild is null
				if (building.getGuild().isEmptyGuild())
					return true;

				if (!ManageCityAssetMsgHandler.playerCanManageNotFriends(sourcePlayer, building))
					return true;


				if (!playerCity.renameCity(msg.getTreeName())){
					ChatManager.chatSystemError(sourcePlayer, "Failed to rename city!");
					return true;
				}

				GuildTreeStatusMsg gtsm = new GuildTreeStatusMsg(building,sourcePlayer);
				gtsm.configure();
				dispatch = Dispatch.borrow(sourcePlayer, gtsm);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

				CityZoneMsg czm = new CityZoneMsg(2,playerZone.getLoc().x, playerZone.getLoc().y, playerZone.getLoc().z, playerCity.getCityName(),playerZone, Enum.CityBoundsType.ZONE.extents, Enum.CityBoundsType.ZONE.extents);
				DispatchMessage.dispatchMsgToAll(czm);

				break;
			case BIND_TREE:

				Guild pcGuild = sourcePlayer.getGuild();

				//test tree is valid for binding, same guild or same nation
				if (!Guild.sameNation(pcGuild, building.getGuild())) {

					return true;
				}

				if (building.getGuild().isEmptyGuild())
					return true;


				//get bind city
				Zone zone =building.getParentZone();

				if (zone == null) {
					ErrorPopupMsg.sendErrorMsg(sourcePlayer, "A Serious error has occurred. Please post details for to ensure transaction integrity");
					return true;
				}

				if (playerCity == null && building.getGuild() != null)
					playerCity = building.getGuild().getOwnedCity();

				if (playerCity == null)
					return true;

				
				sourcePlayer.setBindBuildingID(building.getObjectUUID());
				dispatch = Dispatch.borrow(sourcePlayer, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				break;
			case OPEN_CITY:
			case CLOSE_CITY:
				if ((sourcePlayer == null) ||
						(building == null) || playerZone == null ||playerCity == null)
					return true;

				if (!ManageCityAssetMsgHandler.playerCanManageNotFriends(sourcePlayer, building))
					return true;

				boolean open = (msg.getMessageType() == OPEN_CITY);

				if (!playerCity.openCity(open)){
					ErrorPopupMsg.sendErrorMsg(sourcePlayer, "A Serious error has occurred. Please post details for to ensure transaction integrity");
					return true;
				}

				dispatch = Dispatch.borrow(sourcePlayer, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				break;
			default:
				break;
			}

		} catch(Exception e){
			Logger.error( e.getMessage());
		}finally{
			try{
				this.claimLock.writeLock().unlock();
			}catch(Exception e){
				Logger.info("failClaimsync");
			}
		}
		return true;
	}
}