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
import engine.net.client.msg.guild.LeaveGuildMsg;
import engine.net.client.msg.guild.MOTDMsg;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

public class MOTDEditHandler extends AbstractClientMsgHandler {

	public MOTDEditHandler() {
		super(MOTDMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		MOTDMsg msg = (MOTDMsg) baseMsg;
        Dispatch dispatch;

		// get source player
		PlayerCharacter playerCharacter = SessionManager.getPlayerCharacter(
				origin);

		if (playerCharacter == null)
			return true;

		int type = msg.getType();

		msg.setResponse((byte) 1);
		if (type == 0 || type == 1 || type == 3) {
			if (GuildStatusController.isInnerCouncil(playerCharacter.getGuildStatus()) == false) {
				 ErrorPopupMsg.sendErrorMsg(playerCharacter, "You do not have such authority!");
				return true;
			}

			Guild guild = playerCharacter.getGuild();

			if (guild == null || guild.getObjectUUID() == 0) {

                LeaveGuildMsg leaveGuildMsg = new LeaveGuildMsg();
                leaveGuildMsg.setMessage("You do not belong to a guild!");
                dispatch = Dispatch.borrow(playerCharacter, leaveGuildMsg);
                DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

				return true;
			}

			if (type == 1) // Guild MOTD
				msg.setMessage(guild.getMOTD());
			else if (type == 3) // IC MOTD
				msg.setMessage(guild.getICMOTD());
			else if (type == 0) { // Nation MOTD
				Guild nation = guild.getNation();
				if (nation == null || !nation.isNation()) {
					ErrorPopupMsg.sendErrorMsg(playerCharacter, "You do not have such authority!");
					return true;
				}
				msg.setMessage(nation.getMOTD());
			}
            dispatch = Dispatch.borrow(playerCharacter, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		}
		
		return true;
	}
}
