// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum.DispatchChannel;
import engine.Enum.FriendStatus;
import engine.exception.MsgSendException;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.UpdateFriendStatusMessage;
import engine.objects.PlayerCharacter;
import engine.objects.PlayerFriends;
import org.pmw.tinylog.Logger;

import java.util.HashSet;

public class UpdateFriendStatusHandler extends AbstractClientMsgHandler {

	public UpdateFriendStatusHandler() {
		super(UpdateFriendStatusMessage.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg,
			ClientConnection origin) throws MsgSendException {
		
		PlayerCharacter player = origin.getPlayerCharacter();
		
		if (player == null)
			return true;
		

		UpdateFriendStatusMessage msg = (UpdateFriendStatusMessage)baseMsg;
		
		
			HandleUpdateFriend(player,msg);
			
		return true;
	}
	

	//change to Request
	public static void HandleUpdateFriend(PlayerCharacter player, UpdateFriendStatusMessage msg){
	FriendStatus friendStatus = FriendStatus.Available;
	
	try {
		friendStatus = FriendStatus.values()[msg.statusType];
	}catch (Exception e){
		Logger.error(e);
	}
	player.friendStatus = friendStatus;
	SendUpdateToFriends(player);
	}
	
	public static void SendUpdateToFriends(PlayerCharacter player){
		
		HashSet<Integer> friends = PlayerFriends.PlayerFriendsMap.get(player.getObjectUUID());
		
		if (friends == null)
			return;
		
		UpdateFriendStatusMessage outMsg = new UpdateFriendStatusMessage(player);
		
		for (int friendID : friends){
			PlayerCharacter playerFriend = SessionManager.getPlayerCharacterByID(friendID);
			if (playerFriend == null)
				return;
			Dispatch dispatch = Dispatch.borrow(playerFriend, outMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		}
		
		
	}
}
