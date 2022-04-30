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
import engine.Enum.DispatchChannel;
import engine.Enum.GuildHistoryType;
import engine.Enum.ItemType;
import engine.Enum.OwnerType;
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
import engine.net.client.msg.guild.GuildCreationFinalizeMsg;
import engine.net.client.msg.guild.GuildInfoMsg;
import engine.objects.*;
import engine.util.StringUtils;

public class GuildCreationFinalizeHandler extends AbstractClientMsgHandler {

	public GuildCreationFinalizeHandler() {
		super(GuildCreationFinalizeMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player;
		GuildCreationFinalizeMsg msg;
		Enum.GuildType charterType;
		Guild newGuild;
		ItemBase itemBase;
		Item charter;
		Dispatch dispatch;

		msg = (GuildCreationFinalizeMsg) baseMsg;

		player = SessionManager.getPlayerCharacter(origin);

		boolean isGuildLeader = GuildStatusController.isGuildLeader(player.getGuildStatus());
		if (GuildStatusController.isGuildLeader(player.getGuildStatus()) || player.getGuild() != null && player.getGuild().getGuildLeaderUUID() == player.getObjectUUID()) {
			ErrorPopupMsg.sendErrorPopup(player, GuildManager.MUST_LEAVE_GUILD);
			return true;
		}

		//Validate the Charter

		charter = msg.getCharter();

		if (charter == null || charter.getOwnerType() != OwnerType.PlayerCharacter || charter.getOwnerID() != player.getObjectUUID()) {
			ErrorPopupMsg.sendErrorPopup(player, GuildManager.NO_CHARTER_FOUND);
			return true;
		}

		

		itemBase = charter.getItemBase();


		// Item must be a valid charterType (type 10 in db)

		if (itemBase == null || (itemBase.getType().equals(ItemType.GUILDCHARTER) == false)) {
			ErrorPopupMsg.sendErrorPopup(player, GuildManager.NO_CHARTER_FOUND);
			return true;
		}
		charterType = Enum.GuildType.getGuildTypeFromCharter(itemBase);



		if (charterType == null){
			ErrorPopupMsg.sendErrorPopup(player, GuildManager.NO_CHARTER_FOUND);
			return true;
		}





		//Validate Guild Tags

		if (!msg.getGuildTag().isValid()) {
			ErrorPopupMsg.sendErrorPopup(player, GuildManager.CREST_RESERVED);
			return true;
		}

		// Validation passes.  Leave current guild and create new one.

		if (player.getGuild() != null && player.getGuild().getObjectUUID() != 0)
			player.getGuild().removePlayer(player,GuildHistoryType.LEAVE);



		int leadershipType = ((msg.getICVoteFlag() << 1) | msg.getMemberVoteFlag());

		newGuild = new Guild( msg.getName(),null, charterType.ordinal(),
				charterType.getLeadershipType(leadershipType), msg.getGuildTag(),
				StringUtils.truncate(msg.getMotto(), 120));

		newGuild.setGuildLeaderForCreate(player);

		synchronized (this) {
			if (!DbManager.GuildQueries.IS_NAME_UNIQUE(msg.getName())) {
				ErrorPopupMsg.sendErrorPopup(player, GuildManager.UNIQUE_NAME);
				return true;
			}

			if (!DbManager.GuildQueries.IS_CREST_UNIQUE(msg.getGuildTag())) {
				ErrorPopupMsg.sendErrorPopup(player, GuildManager.UNIQUE_CREST);
				return true;
			}

			newGuild = DbManager.GuildQueries.SAVE_TO_DATABASE(newGuild);
		}

		if (newGuild == null) {
			ErrorPopupMsg.sendErrorPopup(player, GuildManager.FAILURE_TO_SWEAR_GUILD);
			return true;
		}
		
		dispatch = Dispatch.borrow(player, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		GuildManager.joinGuild(player, newGuild, GuildHistoryType.CREATE);
		
		newGuild.setGuildLeader(player);
		player.setGuildLeader(true);
		player.setInnerCouncil(true);
		player.setFullMember(true);
		player.setGuildTitle(charterType.getNumberOfRanks() - 1);
		player.getCharItemManager().delete(charter);
		player.getCharItemManager().updateInventory();
		player.incVer();

		DispatchMessage.sendToAllInRange(player, new GuildInfoMsg(player, newGuild, 2));

		ChatManager.chatSystemInfo(player, msg.getName() + " has arrived on Grief server!");

		return true;
	}
}
