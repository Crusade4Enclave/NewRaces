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
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.DeclineFriendMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.objects.PlayerCharacter;

public class FriendDeclineHandler extends AbstractClientMsgHandler {

	public FriendDeclineHandler() {
		super(DeclineFriendMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg,
			ClientConnection origin) throws MsgSendException {
		
		PlayerCharacter player = origin.getPlayerCharacter();
		
		if (player == null)
			return true;
		

		DeclineFriendMsg msg = (DeclineFriendMsg)baseMsg;
		
		
			HandleDeclineFriend(player,msg);
			
		return true;
	}
	

	
	//change to Request
	public static void HandleDeclineFriend(PlayerCharacter player, DeclineFriendMsg msg){
		PlayerCharacter sourceFriend = SessionManager.getPlayerCharacterByLowerCaseName(msg.sourceName);
		
		if (sourceFriend == null){
			ErrorPopupMsg.sendErrorMsg(player, "Could not find player " + msg.sourceName);
			return;
		}
	
	Dispatch dispatch = Dispatch.borrow(sourceFriend, msg);
	DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	
	}
	
}
