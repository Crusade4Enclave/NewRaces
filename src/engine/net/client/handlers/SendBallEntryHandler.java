// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.exception.MsgSendException;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.SendBallEntryMessage;
import engine.objects.PlayerCharacter;
public class SendBallEntryHandler extends AbstractClientMsgHandler {

	public SendBallEntryHandler() {
		super(SendBallEntryMessage.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg,
			ClientConnection origin) throws MsgSendException {
		
		PlayerCharacter player = origin.getPlayerCharacter();
		
		if (player == null)
			return true;
		

		SendBallEntryMessage msg = (SendBallEntryMessage)baseMsg;
		
			HandleAddBall(player,msg);
			
		return true;
	}
	

	
	public static void HandleAddBall(PlayerCharacter player, SendBallEntryMessage msg){
		//currently not handled.
	
	}
	
}
