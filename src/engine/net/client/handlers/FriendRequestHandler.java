// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.FriendRequestMsg;
import engine.objects.PlayerCharacter;

public class FriendRequestHandler extends AbstractClientMsgHandler {

	public FriendRequestHandler() {
		super(FriendRequestMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg,
			ClientConnection origin) throws MsgSendException {
		
		PlayerCharacter player = origin.getPlayerCharacter();
		
		if (player == null)
			return true;
		

		FriendRequestMsg msg = (FriendRequestMsg)baseMsg;
		
			HandleRequestFriend(player,msg);
			
		return true;
	}
	

	
	public static void HandleRequestFriend(PlayerCharacter player, FriendRequestMsg msg){
	PlayerCharacter targetFriend = SessionManager.getPlayerCharacterByLowerCaseName(msg.friendName);
	
	if (targetFriend == null){
		ErrorPopupMsg.sendErrorMsg(player, "Could not find player " + msg.friendName);
		return;
	}
	
	if (targetFriend.equals(player))
		return;
	
	
	
	
	Dispatch dispatch = Dispatch.borrow(targetFriend, msg);
	DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	ChatManager.chatSystemInfo(player, "Your friend request has been sent.");
	
	}
	
}
