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
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.RemoveFriendMessage;
import engine.objects.PlayerCharacter;
import engine.objects.PlayerFriends;



public class RemoveFriendHandler extends AbstractClientMsgHandler {

	public RemoveFriendHandler() {
		super(RemoveFriendMessage.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg,
			ClientConnection origin) throws MsgSendException {
		
		PlayerCharacter player = origin.getPlayerCharacter();
		
		if (player == null)
			return true;
		

		RemoveFriendMessage msg = (RemoveFriendMessage)baseMsg;
		
			HandleRemoveFriend(player,msg);
			
		return true;
	}
	

	
	public static void HandleRemoveFriend(PlayerCharacter player, RemoveFriendMessage msg){
		
		//No friends in list. Early exit.
		PlayerFriends.RemoveFromFriends(player.getObjectUUID(), msg.friendID);
		PlayerFriends.RemoveFromFriends(msg.friendID, player.getObjectUUID());
	
	Dispatch dispatch = Dispatch.borrow(player, msg);
	DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	
	msg = new RemoveFriendMessage(msg.friendID);
	
	 dispatch = Dispatch.borrow(player, msg);
	DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	}
	
}
