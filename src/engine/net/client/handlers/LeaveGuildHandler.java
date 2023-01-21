// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum.GuildHistoryType;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.guild.LeaveGuildMsg;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.Mine;
import engine.objects.PlayerCharacter;

public class LeaveGuildHandler extends AbstractClientMsgHandler {

	public LeaveGuildHandler() {
		super(LeaveGuildMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		LeaveGuildMsg msg = (LeaveGuildMsg) baseMsg;
		Dispatch dispatch;

		// get PlayerCharacter of person leaving invite

		PlayerCharacter playerCharacter = SessionManager.getPlayerCharacter(origin);

		if (playerCharacter == null)
			return true;

		// Guild leader can't leave guild. must pass GL or disband

		if (GuildStatusController.isGuildLeader(playerCharacter.getGuildStatus())) {
			msg.setMessage("You must switch leadership of your guild before leaving!");
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
			return true;
		}

		// Release all mine claims

		Mine.releaseMineClaims(playerCharacter);

		// get old Guild
		Guild oldGuild = playerCharacter.getGuild();

		if (oldGuild == null || oldGuild.isEmptyGuild()) {
			return true;
		}

		// Send left guild message to rest of guild
		ChatManager.chatGuildInfo(oldGuild, playerCharacter.getFirstName() + " has left the guild.");

		oldGuild.removePlayer(playerCharacter, GuildHistoryType.LEAVE);

		// Send message back to client
		msg.setMessage("You have left the guild.");
		dispatch = Dispatch.borrow(playerCharacter, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

		return true;
	}

}
