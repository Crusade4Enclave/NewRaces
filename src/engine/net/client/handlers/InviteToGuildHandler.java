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
import engine.gameManager.GuildManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.InviteToGuildMsg;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

public class InviteToGuildHandler extends AbstractClientMsgHandler {

	public InviteToGuildHandler() {
		super(InviteToGuildMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		InviteToGuildMsg msg;
		PlayerCharacter sourcePlayer;
		PlayerCharacter targetPlayer;
		Dispatch dispatch;

		msg = (InviteToGuildMsg) baseMsg;

		// First see if this is a refusal to another guild invite

		if (msg.getResponse() == 4)
			return true; // Player refused invite

		// get sourcePlayer player

		sourcePlayer = SessionManager.getPlayerCharacter(origin);

		if (sourcePlayer == null)
			return true;

		if (msg.getTargetUUID() == 0) {
			// get targetPlayer player by name
			targetPlayer = SessionManager.getPlayerCharacterByLowerCaseName(msg.getTargetName());

			if (targetPlayer == null) {
				ChatManager.chatGuildError(sourcePlayer,
						"No such player exists!");
				return true;
			}
		} else
			if (msg.getTargetType() == GameObjectType.PlayerCharacter.ordinal()) {

				targetPlayer = SessionManager.getPlayerCharacterByID(msg.getTargetUUID());

				if (targetPlayer == null) {
					ChatManager.chatGuildError(sourcePlayer,
							"No such player exists!");
					return true;
				}
			} else {
				ChatManager.chatGuildError(sourcePlayer,
						"You cannot invite that character!");
				return true;
			}

		// get sourcePlayer guild. Verify sourcePlayer player is in guild

		if (sourcePlayer.getGuild().getObjectUUID() == 0 || sourcePlayer.getGuild().isErrant()) {
			ChatManager.chatGuildError(sourcePlayer,
					"You cannot invite someone for errant!");
			return true;
		}

		Enum.GuildType guildType = Enum.GuildType.values()[sourcePlayer.getGuild().getCharter()];

		if (guildType == null){
			ErrorPopupMsg.sendErrorPopup(sourcePlayer, GuildManager.NO_CHARTER_FOUND);
			return true;
		}



		// verify sourcePlayer player is full member so they can invite

		if (GuildStatusController.isFullMember(sourcePlayer.getGuildStatus()) == false) {
			ChatManager.chatGuildError(sourcePlayer,
					"You do not have authority to invite!");
			return true;
		}

		//block invite is targetPlayer is ignoring sourcePlayer

		if (targetPlayer.isIgnoringPlayer(sourcePlayer))
			return true;

		if ((targetPlayer.getGuild().isErrant() == false)) {
			ChatManager.chatGuildError(sourcePlayer,
					targetPlayer.getFirstName() + " already belongs to a guild!");
			return true;
		}

		// verify targetPlayer player is not on banish list

		if (sourcePlayer.getGuild().getBanishList().contains(targetPlayer)) {
			ErrorPopupMsg.sendErrorPopup(sourcePlayer, 135);// Character is considered BANISHED by guild leadership
			return true;
		}

		//verify targetPlayer meets level requirements of guild

		if ((targetPlayer.getLevel() < sourcePlayer.getGuild().getRepledgeMin()) || targetPlayer.getLevel() > sourcePlayer.getGuild().getRepledgeMax()) {
			ErrorPopupMsg.sendErrorPopup(sourcePlayer, 135);// you do not meet the level required for this SWORN guild
			return true;
		}

		targetPlayer.setLastGuildToInvite(sourcePlayer.getGuild().getObjectUUID());

		// setup guild invite message to send to targetPlayer

		msg.setSourceType(sourcePlayer.getObjectType().ordinal());
		msg.setSourceUUID(sourcePlayer.getObjectUUID());
		msg.setTargetType(targetPlayer.getObjectType().ordinal());

		msg.setTargetUUID(targetPlayer.getObjectUUID());
		msg.setGuildTag(sourcePlayer.getGuild().getGuildTag());
		msg.setGuildName(sourcePlayer.getGuild().getName());
		msg.setGuildType(sourcePlayer.getGuild().getObjectType().ordinal());
		msg.setGuildUUID(sourcePlayer.getGuild().getObjectUUID());
		msg.setTargetName("");

		dispatch = Dispatch.borrow(targetPlayer, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		return true;
	}

}
