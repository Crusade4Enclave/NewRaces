package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.Enum.GameObjectType;
import engine.ai.StaticMobActions;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.OpenFriendsCondemnListMsg;
import engine.objects.*;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;

/*
 * @Author:
 * @Summary: Processes application protocol message which handles
 * client requests for various lists
 */

public class OpenFriendsCondemnListMsgHandler extends AbstractClientMsgHandler {

	public OpenFriendsCondemnListMsgHandler() {
		super(OpenFriendsCondemnListMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player = SessionManager.getPlayerCharacter(origin);
		Building sourceBuilding;
		OpenFriendsCondemnListMsg msg;
		OpenFriendsCondemnListMsg openFriendsCondemnListMsg;
		Enum.FriendListType friendListType;
		Dispatch dispatch;

		if (player == null)
			return true;

		msg = (OpenFriendsCondemnListMsg) baseMsg;
		openFriendsCondemnListMsg = new OpenFriendsCondemnListMsg(msg);
		friendListType = Enum.FriendListType.getListTypeByID(msg.getMessageType());
		
		if (friendListType == null){
			Logger.error("Invalid FriendListType for messageType " + msg.getMessageType());
			return true;
		}

		switch (friendListType) {
		case VIEWHERALDRY: // Heraldry
			
			Heraldry.ValidateHeraldry(player.getObjectUUID());
			OpenFriendsCondemnListMsg outMsg = new OpenFriendsCondemnListMsg(msg);
			outMsg.setOrigin(origin);
			outMsg.setMessageType(2);
			dispatch = Dispatch.borrow(player, outMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		break;
		
		case ADDHERALDRY:
			Heraldry.ValidateHeraldry(player.getObjectUUID());
			if (msg.getPlayerID() <= 0){
				//ErrorPopupMsg.sendErrorMsg(player, "Invalid Heraldry Object.");
				return true;
			}
			AbstractCharacter toAdd = null;
			if (msg.getPlayerType() == GameObjectType.PlayerCharacter.ordinal())
				toAdd = PlayerCharacter.getFromCache(msg.getPlayerID());
			else if (msg.getPlayerType() == GameObjectType.NPC.ordinal())
				toAdd = NPC.getFromCache(msg.getPlayerID());
			else if (msg.getPlayerType() == GameObjectType.Mob.ordinal())
				toAdd = StaticMobActions.getFromCache(msg.getPlayerID());
			else{
				ErrorPopupMsg.sendErrorMsg(player, "Invalid Heraldry Object.");
				return true;
			}
			
			if (toAdd == null){
				ErrorPopupMsg.sendErrorMsg(player, "Invalid Heraldry Object.");
				return true;
			}
			
			Heraldry.AddToHeraldy(player.getObjectUUID(), toAdd);
			
			
			 outMsg = new OpenFriendsCondemnListMsg(msg);
			outMsg.setOrigin(origin);
			outMsg.setMessageType(2);
			dispatch = Dispatch.borrow(player, outMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

			break;
		case REMOVEHERALDRY:
			Heraldry.ValidateHeraldry(player.getObjectUUID());
			Heraldry.RemoveFromHeraldy(player.getObjectUUID(), msg.getPlayerID());
				
			
			 outMsg = new OpenFriendsCondemnListMsg(msg);
				outMsg.setOrigin(origin);
				outMsg.setMessageType(2);
				dispatch = Dispatch.borrow(player, outMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
				
			break;

		case DEALTHS: // Death List
			openFriendsCondemnListMsg.updateMsg(8, new ArrayList<>(player.pvpDeaths));
			dispatch = Dispatch.borrow(player, openFriendsCondemnListMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;

		case KILLS: // Kill List
			openFriendsCondemnListMsg.updateMsg(10, new ArrayList<>(player.pvpKills));
			dispatch = Dispatch.borrow(player, openFriendsCondemnListMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;

		case VIEWCONDEMN:

			sourceBuilding = BuildingManager.getBuildingFromCache(msg.getBuildingID());

			if (sourceBuilding == null)
				return true;

			openFriendsCondemnListMsg = new OpenFriendsCondemnListMsg(12, sourceBuilding.getCondemned(), sourceBuilding.reverseKOS);
			openFriendsCondemnListMsg.configure();
			dispatch = Dispatch.borrow(player, openFriendsCondemnListMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

			//msg.updateMsg(12, DbManager.GuildQueries.)
			break;
			//REMOVE CONDEMN
		case REMOVECONDEMN:

			sourceBuilding = BuildingManager.getBuildingFromCache(msg.getBuildingID());

			if (sourceBuilding == null)
				return true;

			if (!BuildingManager.PlayerCanControlNotOwner(sourceBuilding, player))
				return true;

			Condemned removeCondemn = sourceBuilding.getCondemned().get(msg.getRemoveFriendID());

			if (removeCondemn == null)
				return true;

			if (!DbManager.BuildingQueries.REMOVE_FROM_CONDEMNED_LIST(removeCondemn.getParent(), removeCondemn.getPlayerUID(), removeCondemn.getGuildUID(), removeCondemn.getFriendType()))
				return true;

			sourceBuilding.getCondemned().remove(msg.getRemoveFriendID());
			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;

		case TOGGLEACTIVE:

			sourceBuilding = BuildingManager.getBuildingFromCache(msg.getBuildingID());

			if (sourceBuilding == null)
				return true;

			if (!BuildingManager.PlayerCanControlNotOwner(sourceBuilding, player))
				return true;

			Condemned condemn = sourceBuilding.getCondemned().get(msg.getRemoveFriendID());

			if (condemn == null)
				return true;

			condemn.setActive(msg.isReverseKOS());
			openFriendsCondemnListMsg.setReverseKOS(condemn.isActive());
			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;
		case REVERSEKOS:


			sourceBuilding = BuildingManager.getBuildingFromCache(msg.getBuildingID());

			if (sourceBuilding == null)
				return true;

			if (!BuildingManager.PlayerCanControlNotOwner(sourceBuilding, player))
				return true;

			if (!sourceBuilding.setReverseKOS(msg.isReverseKOS()))
				return true;
			break;

			//ADD GUILD CONDEMN
		case ADDCONDEMN:

			sourceBuilding = BuildingManager.getBuildingFromCache(msg.getBuildingID());

			if (sourceBuilding == null)
				return true;

			if (!BuildingManager.PlayerCanControlNotOwner(sourceBuilding, player))
				return true;

			switch (msg.getInviteType()) {
			case 2:

				if (msg.getPlayerID() == 0)
					return true;
				
				if (msg.getPlayerType() != GameObjectType.PlayerCharacter.ordinal())
					return true;

				PlayerCharacter playerCharacter = PlayerCharacter.getFromCache(msg.getPlayerID());

				if (playerCharacter == null)
					return true;

				if (Guild.sameNationExcludeErrant(sourceBuilding.getGuild(), playerCharacter.getGuild()))
					return true;

				if (sourceBuilding.getCondemned().containsKey(playerCharacter.getObjectUUID()))
					return true;

				if (!DbManager.BuildingQueries.ADD_TO_CONDEMNLIST(sourceBuilding.getObjectUUID(), playerCharacter.getObjectUUID(), msg.getGuildID(), msg.getInviteType())) {
					Logger.debug( "Failed to add Condemned: " + playerCharacter.getFirstName() + " to Building With UID " + sourceBuilding.getObjectUUID());
					return true;
				}

				sourceBuilding.getCondemned().put(playerCharacter.getObjectUUID(), new Condemned(playerCharacter.getObjectUUID(), sourceBuilding.getObjectUUID(), msg.getGuildID(), msg.getInviteType()));
				break;
			case 4:
				if (msg.getGuildID() == 0)
					return true;

				if (sourceBuilding.getCondemned().containsKey(msg.getGuildID()))
					return true;

				Guild condemnedGuild = Guild.getGuild(msg.getGuildID());

				if (condemnedGuild == null)
					return true;

				if (!DbManager.BuildingQueries.ADD_TO_CONDEMNLIST(sourceBuilding.getObjectUUID(), msg.getPlayerID(), condemnedGuild.getObjectUUID(), msg.getInviteType())) {
					Logger.debug("Failed to add Condemned: " + condemnedGuild.getName() + " to Building With UID " + sourceBuilding.getObjectUUID());
					return true;
				}

				sourceBuilding.getCondemned().put(condemnedGuild.getObjectUUID(), new Condemned(msg.getPlayerID(), sourceBuilding.getObjectUUID(), condemnedGuild.getObjectUUID(), msg.getInviteType()));
				break;
			case 5:
				if (msg.getNationID() == 0)
					return true;

				if (sourceBuilding.getCondemned().containsKey(msg.getNationID()))
					return true;

				Guild condemnedNation = Guild.getGuild(msg.getNationID());

				if (condemnedNation == null)
					return true;

				if (!DbManager.BuildingQueries.ADD_TO_CONDEMNLIST(sourceBuilding.getObjectUUID(), msg.getPlayerID(), condemnedNation.getObjectUUID(), msg.getInviteType())) {
					Logger.debug( "Failed to add Condemned: " + condemnedNation.getName() + " to Building With UID " + sourceBuilding.getObjectUUID());
					return true;
				}

				sourceBuilding.getCondemned().put(condemnedNation.getObjectUUID(), new Condemned(msg.getPlayerID(), sourceBuilding.getObjectUUID(), condemnedNation.getObjectUUID(), msg.getInviteType()));
				break;

			}

			openFriendsCondemnListMsg = new OpenFriendsCondemnListMsg(12, sourceBuilding.getCondemned(), sourceBuilding.reverseKOS);
			openFriendsCondemnListMsg.configure();
			dispatch = Dispatch.borrow(player, openFriendsCondemnListMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;

			//ADD FRIEND BUILDING
		case ADDFRIEND:
			sourceBuilding =  BuildingManager.getBuildingFromCache(msg.getBuildingID());

			if (sourceBuilding == null)
				return true;

			if (msg.getGuildID() == 0)
				return true;

			if (!BuildingManager.PlayerCanControlNotOwner(sourceBuilding, player))
				return true;
			
			PlayerCharacter playerCharacter = null;

		

			Guild guildInvited = Guild.getGuild(msg.getGuildID());

			if (guildInvited == null)
				return true;

			//Check to see if the invited is already on the friends list.
			switch (msg.getInviteType()) {
			case 7:
				playerCharacter = PlayerCharacter.getFromCache(msg.getPlayerID());
				if (playerCharacter == null)
					return true;
				if (sourceBuilding.getFriends().containsKey(playerCharacter.getObjectUUID()))
					return true;
				break;
			case 8:
			case 9:
				if (sourceBuilding.getFriends().containsKey(guildInvited.getObjectUUID()))
					return true;
				break;
			}

			if (!DbManager.BuildingQueries.ADD_TO_FRIENDS_LIST(sourceBuilding.getObjectUUID(), msg.getPlayerID(), guildInvited.getObjectUUID(), msg.getInviteType())) {
				Logger.debug( "Failed to add Friend: " + playerCharacter.getFirstName() + " to Building With UID " + sourceBuilding.getObjectUUID());
				return true;
			}

			switch (msg.getInviteType()) {
			case 7:
				sourceBuilding.getFriends().put(playerCharacter.getObjectUUID(), new BuildingFriends(playerCharacter.getObjectUUID(), sourceBuilding.getObjectUUID(), playerCharacter.getGuild().getObjectUUID(), 7));
				break;
			case 8:
				sourceBuilding.getFriends().put(guildInvited.getObjectUUID(), new BuildingFriends(msg.getPlayerID(), sourceBuilding.getObjectUUID(), guildInvited.getObjectUUID(), 8));
				break;
			case 9:
				sourceBuilding.getFriends().put(guildInvited.getObjectUUID(), new BuildingFriends(msg.getPlayerID(), sourceBuilding.getObjectUUID(), guildInvited.getObjectUUID(), 9));
				break;
			}

			openFriendsCondemnListMsg = new OpenFriendsCondemnListMsg(26, sourceBuilding.getFriends());
			openFriendsCondemnListMsg.configure();

			dispatch = Dispatch.borrow(player, openFriendsCondemnListMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;
			//REMOVE from friends list.
		case REMOVEFRIEND:
			sourceBuilding = BuildingManager.getBuildingFromCache(msg.getBuildingID());

			if (sourceBuilding == null)
				return true;

			if (!BuildingManager.PlayerCanControlNotOwner(sourceBuilding, player))
				return true;

			BuildingFriends friend = sourceBuilding.getFriends().get(msg.getRemoveFriendID());

			if (friend == null)
				return true;

			if (!DbManager.BuildingQueries.REMOVE_FROM_FRIENDS_LIST(sourceBuilding.getObjectUUID(), friend.getPlayerUID(), friend.getGuildUID(), friend.getFriendType())) {
				Logger.debug( "Failed to remove Friend: " + msg.getRemoveFriendID() + " from Building With UID " + sourceBuilding.getObjectUUID());
				return true;
			}
			sourceBuilding.getFriends().remove(msg.getRemoveFriendID());

			openFriendsCondemnListMsg = new OpenFriendsCondemnListMsg(26, sourceBuilding.getFriends());
			openFriendsCondemnListMsg.configure();
			dispatch = Dispatch.borrow(player, openFriendsCondemnListMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

			break;
			//view Friends
		case VIEWFRIENDS:

			Building building = BuildingManager.getBuildingFromCache(msg.getBuildingID());

			if (building == null)
				return true;

			if (!BuildingManager.PlayerCanControlNotOwner(building, player))
				return true;

			//this message is sent twice back?????

			openFriendsCondemnListMsg = new OpenFriendsCondemnListMsg(26, building.getFriends());
			openFriendsCondemnListMsg.configure();


			dispatch = Dispatch.borrow(player, openFriendsCondemnListMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;

		default:
			break;
		}
		return false;
	}

}