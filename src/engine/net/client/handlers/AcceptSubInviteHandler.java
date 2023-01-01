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
import engine.Enum.GuildState;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.AcceptSubInviteMsg;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

import java.util.ArrayList;

public class AcceptSubInviteHandler extends AbstractClientMsgHandler {

	public AcceptSubInviteHandler() {
		super(AcceptSubInviteMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		AcceptSubInviteMsg msg = (AcceptSubInviteMsg) baseMsg;
		PlayerCharacter sourcePlayer;
		Guild sourceGuild;
		Guild targetGuild;
		Dispatch dispatch;

		// get PlayerCharacter of person sending sub invite

		sourcePlayer = SessionManager.getPlayerCharacter(origin);

		if (sourcePlayer == null)
			return true;

		sourceGuild = sourcePlayer.getGuild();
		targetGuild = (Guild) DbManager.getObject(GameObjectType.Guild, msg.guildUUID());

		//must be source guild to sub to

		if (targetGuild == null) {
			ErrorPopupMsg.sendErrorPopup(sourcePlayer, 45); // Failure to swear guild
			return true;
		}
		if (sourceGuild == null) {
			ErrorPopupMsg.sendErrorPopup(sourcePlayer, 45); // Failure to swear guild
			return true;
		}
		
		if (sourceGuild.equals(targetGuild))
			return true;

		if (GuildStatusController.isGuildLeader(sourcePlayer.getGuildStatus()) == false) {
			ErrorPopupMsg.sendErrorMsg(sourcePlayer, "Only a guild leader can accept fealty!");
			return true;
		}

		//source guild is limited to 7 subs
		//TODO this should be based on TOL rank

		if (!targetGuild.canSubAGuild(sourceGuild,targetGuild)) {
			ErrorPopupMsg.sendErrorPopup(sourcePlayer, 45); // Failure to swear guild
			return true;
		}

		//all tests passed, let's Handle code
		//Update Target Guild State.

		sourceGuild.upgradeGuildState(false);

		//Add sub so GuildMaster can Swear in.

		ArrayList<Guild> subs = targetGuild.getSubGuildList();
		subs.add(sourceGuild);

		targetGuild.setGuildState(GuildState.Nation);


		//Let's send the message back.

		msg.setUnknown02(1);
		msg.setResponse("Your guild is now a " + sourceGuild.getGuildState().name() + '.');
		dispatch = Dispatch.borrow(sourcePlayer, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		ChatManager.chatSystemInfo(sourcePlayer, "Your guild is now a " + sourceGuild.getGuildState().name() + '.');
		return true;
	}
}
