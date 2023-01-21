// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





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
import engine.Enum.GuildHistoryType;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.GuildManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.AcceptInviteToGuildMsg;
import engine.net.client.msg.guild.GuildInfoMsg;
import engine.objects.Guild;
import engine.objects.GuildHistory;
import engine.objects.PlayerCharacter;
import org.joda.time.DateTime;

public class AcceptInviteToGuildHandler extends AbstractClientMsgHandler {

	public AcceptInviteToGuildHandler() {
		super(AcceptInviteToGuildMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player;
		AcceptInviteToGuildMsg msg;
		Guild guild;

		msg = (AcceptInviteToGuildMsg) baseMsg;

		// get PlayerCharacter of person accepting invite

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;

		guild = (Guild) DbManager.getObject(GameObjectType.Guild, msg.getGuildUUID());


		if (guild == null)
			return true;

		if (guild.getGuildType() == null){
			ErrorPopupMsg.sendErrorPopup(player, GuildManager.NO_CHARTER_FOUND);
			return true;
		}

		// verify they accepted for the correct guild

		if (player.getLastGuildToInvite() != msg.getGuildUUID())
			return true;

		if ( (player.getGuild() != null) &&
				(player.getGuild().isEmptyGuild() == false)) {
			ChatManager.chatGuildError(player,
					"You already belongs to a guild!");
			return true;
		}

		// verify they are acceptable level for guild

		if (player.getLevel() < guild.getRepledgeMin() || player.getLevel() > guild.getRepledgeMax())
			return true;

		// Add player to guild
		player.setGuild(guild);

		// Cleanup guild stuff
		player.resetGuildStatuses();

		Dispatch dispatch = Dispatch.borrow(player, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		DispatchMessage.sendToAllInRange(player, new GuildInfoMsg(player, guild, 2));

		player.incVer();

		//Add to guild History

		if (player.getGuild() != null){
			if (DbManager.GuildQueries.ADD_TO_GUILDHISTORY(player.getGuildUUID(), player, DateTime.now(), GuildHistoryType.JOIN)){
				GuildHistory guildHistory = new GuildHistory(player.getGuildUUID(),player.getGuild().getName(),DateTime.now(), GuildHistoryType.JOIN) ;
				player.getGuildHistory().add(guildHistory);
			}
		}

		// Send guild join message

		ChatManager.chatGuildInfo(player, player.getFirstName() + " has joined the guild");
		return true;
	}

}
