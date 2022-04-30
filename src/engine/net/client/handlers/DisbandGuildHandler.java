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
import engine.InterestManagement.WorldGrid;
import engine.db.archive.DataWarehouse;
import engine.db.archive.GuildRecord;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.DisbandGuildMsg;
import engine.net.client.msg.guild.LeaveGuildMsg;
import engine.objects.Bane;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

public class DisbandGuildHandler extends AbstractClientMsgHandler {

	public DisbandGuildHandler() {
		super(DisbandGuildMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player;
		Guild guild;
		Dispatch dispatch;

		player = SessionManager.getPlayerCharacter(origin);

		//don't allow non guild leaders to disband guild.

		if (player == null ||  GuildStatusController.isGuildLeader(player.getGuildStatus()) == false)
			return true;

		guild = player.getGuild();

		if (guild == null || guild.isErrant())
			return true;

		// Don't allow disbanding if a city is owned
		// *** Refactor: We should allow this by abandoning the tree first.

		if (guild.getOwnedCity() != null) {
			ErrorPopupMsg.sendErrorMsg(player, "You cannot disband a soverign guild!");
			return true;
		}

		Bane guildBane = Bane.getBaneByAttackerGuild(guild);

		if (guildBane != null) {
			ErrorPopupMsg.sendErrorMsg(player, "You cannot disband a guild with an active bane!");
			return true;
		}

		if (guild.getSubGuildList().size() > 0) {
			ErrorPopupMsg.sendErrorMsg(player, "You cannot disband a nation!");
			return true;
		}

		// Send message to guild (before kicking everyone out of it)

		ChatManager.chatGuildInfo(guild, guild.getName() + " has been disbanded!");

		// Log event to data warehous

		GuildRecord guildRecord = GuildRecord.borrow(guild, Enum.RecordEventType.DISBAND);
		DataWarehouse.pushToWarehouse(guildRecord);

		// Remove us as a subguild of our nation

		if (guild.getNation() != null && Guild.sameGuild(guild, guild.getNation()) == false && guild.getNation().isErrant() == false)
			guild.getNation().removeSubGuild(guild);

		// Update all online guild players

		for (PlayerCharacter pcs : Guild.GuildRoster(guild)) {

			guild.removePlayer(pcs,GuildHistoryType.DISBAND);
		}

		//Save Guild data

		player.setGuildLeader(false);
		player.setInnerCouncil(false);
		guild.setGuildLeaderUUID(0);
		guild.setNation(null);

		DbManager.GuildQueries.DELETE_GUILD(guild);

		DbManager.removeFromCache(guild);
		WorldGrid.removeObject(guild, player);

		// Send message back to client

		LeaveGuildMsg leaveGuildMsg = new LeaveGuildMsg();
		leaveGuildMsg.setMessage("You guild has been disbanded!");
		dispatch = Dispatch.borrow(player, leaveGuildMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		return true;
	}

}
