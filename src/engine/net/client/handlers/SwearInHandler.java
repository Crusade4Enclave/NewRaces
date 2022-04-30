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
import engine.gameManager.ChatManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.GuildInfoMsg;
import engine.net.client.msg.guild.GuildListMsg;
import engine.net.client.msg.guild.SwearInMsg;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

public class SwearInHandler extends AbstractClientMsgHandler {

	public SwearInHandler() {
		super(SwearInMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		SwearInMsg msg = (SwearInMsg) baseMsg;
        Dispatch dispatch;

		// get source player
		PlayerCharacter source = SessionManager.getPlayerCharacter(origin);

		if (source == null)
			return true;

		// get target player
		PlayerCharacter target = SessionManager.getPlayerCharacterByID(msg.getTargetID());

		if (target == null) {
			ChatManager.chatGuildError(source,
					"No such character found!");
			return true;
		}

		if(source.getGuild() != target.getGuild()) {
			ChatManager.chatGuildError(source,
				"That player is not a member of " + source.getGuild().getName());
			return true;
		}

		// Verify source has authority to swear in
		if (GuildStatusController.isInnerCouncil(source.getGuildStatus()) == false) {
			ErrorPopupMsg.sendErrorMsg(source, "Your do not have such authority!");
			return true;
		}

		// Swear target in and send message to guild
		target.setFullMember(true);
		target.incVer();

		ChatManager.chatGuildInfo(source,target.getFirstName() + " has been sworn in as a full member!");

        dispatch = Dispatch.borrow(source, new GuildListMsg(source.getGuild()));
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		DispatchMessage.sendToAllInRange(target, new GuildInfoMsg(target, target.getGuild(), 2));

		return true;
	}

}
