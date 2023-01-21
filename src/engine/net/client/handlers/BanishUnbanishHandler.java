// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.GuildHistoryType;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.BanishUnbanishMsg;
import engine.net.client.msg.guild.GuildListMsg;
import engine.objects.Guild;
import engine.objects.GuildHistory;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;
import org.joda.time.DateTime;

public class BanishUnbanishHandler extends AbstractClientMsgHandler {

	public BanishUnbanishHandler() {
		super(BanishUnbanishMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		BanishUnbanishMsg msg = (BanishUnbanishMsg) baseMsg;
		Dispatch dispatch;

		int target = msg.getTarget();
		PlayerCharacter source = origin.getPlayerCharacter();

		if(source == null || source.getGuild().isEmptyGuild() || source.getGuild().getObjectUUID() == 0)
			return true;

		if (GuildStatusController.isGuildLeader(source.getGuildStatus()) == false && GuildStatusController.isInnerCouncil(source.getGuildStatus()) == false)
			return true;

		if (source.getObjectUUID() == target) {
			ErrorPopupMsg.sendErrorPopup(source, 103); // You may not banish this char
			return true;
		}

		boolean success = false;
		Guild guild = source.getGuild();
		PlayerCharacter realizedTarget = PlayerCharacter.getFromCache(target);

		if(realizedTarget != null) {
			// Guild leader can't leave guild. must pass GL or disband
			if ( GuildStatusController.isGuildLeader(realizedTarget.getGuildStatus()) == false) {
				//ICs cannot banish other ICs
				if (!(GuildStatusController.isInnerCouncil(realizedTarget.getGuildStatus()) && GuildStatusController.isGuildLeader(source.getGuildStatus()) == false)) {
					success = true;
					if (msg.getMsgType() == 1){
						if (!DbManager.GuildQueries.ADD_TO_BANISHED_FROM_GUILDLIST(guild.getObjectUUID(), realizedTarget.getObjectUUID())){
							ChatManager.chatGuildError(source, "Failed To unbanish " + realizedTarget.getName());
							return true;
						}
						guild.getBanishList().remove(realizedTarget);
					}


					else{

						if (!DbManager.GuildQueries.ADD_TO_BANISHED_FROM_GUILDLIST(guild.getObjectUUID(), realizedTarget.getObjectUUID())){
							ChatManager.chatGuildError(source, "Failed To Banish " + realizedTarget.getName());
							return true;
						}
						guild.removePlayer(realizedTarget, GuildHistoryType.BANISHED);
						guild.getBanishList().add(realizedTarget);  //TODO we might encapsulate this a bit better; also not sure that a list of PC objects is really ideal
					}

				}
			}
		} else {
			if (guild.getGuildLeaderUUID() != target) {
				PlayerCharacter toBanish = PlayerCharacter.getPlayerCharacter(target);
				if (toBanish == null)
					return true;
				//already added previously.
				if (SessionManager.getPlayerCharacterByID(toBanish.getObjectUUID()) != null)
					return true;

				if(DbManager.GuildQueries.BANISH_FROM_GUILD_OFFLINE(target, GuildStatusController.isGuildLeader(source.getGuildStatus())) != 0) {

					success = true;

					//Set guild history

					if (DbManager.GuildQueries.ADD_TO_GUILDHISTORY(guild.getObjectUUID(), toBanish, DateTime.now(), GuildHistoryType.BANISHED)){
						GuildHistory guildHistory = new GuildHistory(toBanish.getGuildUUID(),toBanish.getGuild().getName(),DateTime.now(), GuildHistoryType.BANISHED) ;
						toBanish.getGuildHistory().add(guildHistory);
					}
				}
			}
		}


		if(success) {
			//TODO re enable this once we get unbanish working!!!!
			//DbManager.GuildQueries.ADD_TO_BANISHED_FROM_GUILDLIST(guild.getobjectUUID(), target);

			// Send left guild message to rest of guild
			String targetName = PlayerCharacter.getFirstName(target);
			ChatManager.chatGuildInfo(guild,
					targetName + " has been banished from " + guild.getName() + '.');
			GuildListMsg guildListMsg = new GuildListMsg(guild);
			dispatch = Dispatch.borrow(source, guildListMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		} else {
			ErrorPopupMsg.sendErrorPopup(source, 103); // You may not banish this char
		}
		return true;

	}

}
