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
import engine.net.client.msg.AcceptFriendMsg;
import engine.net.client.msg.AddFriendMessage;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.objects.PlayerCharacter;
import engine.objects.PlayerFriends;

public class FriendAcceptHandler extends AbstractClientMsgHandler {

	public FriendAcceptHandler() {
		super(AcceptFriendMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg,
			ClientConnection origin) throws MsgSendException {
		
		PlayerCharacter player = origin.getPlayerCharacter();
		
		if (player == null)
			return true;
		

		AcceptFriendMsg msg = (AcceptFriendMsg)baseMsg;
		
		
			HandleAcceptFriend(player,msg);
			
		return true;
	}
	

	
	//change to Request
	public static void HandleAcceptFriend(PlayerCharacter player, AcceptFriendMsg msg){
	PlayerCharacter sourceFriend = SessionManager.getPlayerCharacterByLowerCaseName(msg.sourceName);
	
	if (sourceFriend == null){
		ErrorPopupMsg.sendErrorMsg(player, "Could not find player " + msg.sourceName);
		return;
	}
	
	PlayerFriends.AddToFriends(sourceFriend.getObjectUUID(), player.getObjectUUID());
	PlayerFriends.AddToFriends(player.getObjectUUID(), sourceFriend.getObjectUUID());
	
	
	AddFriendMessage outMsg = new AddFriendMessage(player);
	
	Dispatch dispatch = Dispatch.borrow(sourceFriend, outMsg);
	DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	
	outMsg = new AddFriendMessage(sourceFriend);
	 dispatch = Dispatch.borrow(player, outMsg);
	DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	
	ChatManager.chatSystemInfo(sourceFriend, player.getFirstName() + " has agreed to be your friend.");
	
	}
	
}
