// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.guild.MOTDCommitMsg;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

public class MOTDCommitHandler extends AbstractClientMsgHandler {

	public MOTDCommitHandler() {
		super(MOTDCommitMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		MOTDCommitMsg msg = (MOTDCommitMsg) baseMsg;
		Dispatch dispatch;

		// get source player
		PlayerCharacter sourcePlayer = SessionManager.getPlayerCharacter(
				origin);

		if (sourcePlayer == null)
			return true;

		int type = msg.getType();

		if (type == 0 || type == 1 || type == 3) {

			if (GuildStatusController.isInnerCouncil(sourcePlayer.getGuildStatus()) == false)
				return true;

			Guild guild = sourcePlayer.getGuild();

			if (guild == null)
				return true;

			if (type == 1) { // Guild MOTD
				guild.setMOTD(msg.getMessage());
				ChatManager.chatGuildMOTD(sourcePlayer, msg.getMessage(),
						true);
			} else if (type == 3) { // IC MOTD
				guild.setICMOTD(msg.getMessage());
				ChatManager
						.chatICMOTD(sourcePlayer, msg.getMessage(), true);
			} else if (type == 0) { // Nation MOTD
				Guild nation = guild.getNation();
				if (nation == null)
					return true;
				if (nation.isNation()) { // only
																		// nation's
					// primary guild can
					// set nation motd
					nation.setMOTD(msg.getMessage());
					ChatManager.chatNationMOTD(sourcePlayer,
							msg.getMessage(), true);
				}
			}
            dispatch = Dispatch.borrow(sourcePlayer, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
		}
		
		return true;
	}

}
