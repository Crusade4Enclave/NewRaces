// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.InviteToSubMsg;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

public class InviteToSubHandler extends AbstractClientMsgHandler {

	public InviteToSubHandler() {
		super(InviteToSubMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter source;
		PlayerCharacter target;
		Guild sourceGuild;
		Guild targetGuild;
		InviteToSubMsg msg = (InviteToSubMsg) baseMsg;
		Dispatch dispatch;

		source = SessionManager.getPlayerCharacter(origin);

		if (source == null)
			return true;

		target = (PlayerCharacter) DbManager.getObject(GameObjectType.PlayerCharacter, msg.getTargetUUID());

		if (target == null) {
			ErrorPopupMsg.sendErrorMsg(source, "A Serious error has occured. Please post details for to ensure transaction integrity");
			return true;
		}

		//Ignore invites to sub if ignoring player

		if (target.isIgnoringPlayer(source))
			return true;

		sourceGuild = source.getGuild();
		targetGuild = target.getGuild();

		//source must be in guild

		if (sourceGuild == null) {
			sendChat(source, "You must be in a guild to invite to sub.");
			return true;
		}

		if (sourceGuild.isEmptyGuild()){
			sendChat(source, "You must be in a guild to invite to sub.");
			return true;
		}

		//source must be GL or IC

		if (GuildStatusController.isInnerCouncil(source.getGuildStatus()) == false) {
			sendChat(source, "Only guild leadership can invite to sub.");
			return true;
		}

		if (sourceGuild.getNation().isEmptyGuild())
			return true;

		//target must be in a guild

		if (targetGuild == null)
			return true;
		
		if (sourceGuild.equals(targetGuild))
			return true;

		//target must be GL or IC

		if (GuildStatusController.isInnerCouncil(target.getGuildStatus()) == false && GuildStatusController.isGuildLeader(target.getGuildStatus()) == false) {
			sendChat(source, "Target player is not guild leadership.");
			return true;
		}

		//Can't already be same nation or errant
		//source guild is limited to 7 subs
		//TODO this should be based on TOL rank


		if (!sourceGuild.canSubAGuild(targetGuild)) {
			sendChat(source, "This Guild can't be subbed.");
			return true;
		}

		//all tests passed, let's send invite.

		if (target.getClientConnection() != null) {
			msg.setGuildTag(sourceGuild.getGuildTag());
			msg.setGuildName(sourceGuild.getName());
			msg.setGuildUUID(sourceGuild.getObjectUUID());
			msg.setUnknown02(1);

			dispatch = Dispatch.borrow(target, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		} else {
			sendChat(source, "Failed to send sub invite to target.");
			return true;
		}

		return true;
	}

	private static void sendChat(PlayerCharacter source, String msg) {
		ChatManager.chatGuildError(source, msg);
	}
}
