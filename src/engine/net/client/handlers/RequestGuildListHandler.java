// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;


import engine.Enum;
import engine.exception.MsgSendException;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.ReqGuildListMsg;
import engine.net.client.msg.guild.SendGuildEntryMsg;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;


public class RequestGuildListHandler extends AbstractClientMsgHandler {

	public RequestGuildListHandler() {
		super(ReqGuildListMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
        Dispatch dispatch;

		// get PlayerCharacter of person accepting invite
		PlayerCharacter pc = SessionManager.getPlayerCharacter(
				origin);
		if (pc == null)
			return true;
		
		if (GuildStatusController.isGuildLeader(pc.getGuildStatus()) == false){
			ErrorPopupMsg.sendErrorMsg(pc, "You do not have such authority!");
		}
		SendGuildEntryMsg msg = new SendGuildEntryMsg(pc);


        dispatch = Dispatch.borrow(pc, msg);
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		return true;
	}

}
