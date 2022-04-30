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
import engine.gameManager.GuildManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.BreakFealtyMsg;
import engine.net.client.msg.guild.SendGuildEntryMsg;
import engine.objects.*;
import engine.server.MBServerStatics;
import engine.session.Session;

import java.util.ArrayList;

public class BreakFealtyHandler extends AbstractClientMsgHandler {

	public BreakFealtyHandler() {
		super(BreakFealtyMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		BreakFealtyMsg bfm;
		PlayerCharacter player;
		Guild toBreak;
		Guild guild;
		Dispatch dispatch;

		bfm = (BreakFealtyMsg) baseMsg;

		// get PlayerCharacter of person accepting invite

		player = SessionManager.getPlayerCharacter(
				origin);

		if (player == null)
			return true;

		toBreak = (Guild) DbManager.getObject(GameObjectType.Guild, bfm.getGuildUUID());

		if (toBreak == null) {
			ErrorPopupMsg.sendErrorMsg(player, "A Serious error has occured. Please post details for to ensure transaction integrity");
			return true;
		}

		guild = player.getGuild();

		if (guild == null) {
			ErrorPopupMsg.sendErrorMsg(player, "You do not belong to a guild!");
			return true;
		}

		if (toBreak.isNPCGuild()){
			if (GuildStatusController.isGuildLeader(player.getGuildStatus()) == false) {
				ErrorPopupMsg.sendErrorMsg(player, "Only guild leader can break fealty!");
				return true;
			}




			if (!DbManager.GuildQueries.UPDATE_PARENT(guild.getObjectUUID(),MBServerStatics.worldUUID)) {
				ErrorPopupMsg.sendErrorMsg(player, "A Serious error has occurred. Please post details for to ensure transaction integrity");
				return true;
			}

			switch (guild.getGuildState()) {
			case Sworn:
				guild.setNation(null);
				GuildManager.updateAllGuildTags(guild);
				GuildManager.updateAllGuildBinds(guild, null);
				break;
			case Province:
				guild.setNation(guild);
				GuildManager.updateAllGuildTags(guild);
				GuildManager.updateAllGuildBinds(guild, guild.getOwnedCity());
				break;
			}

			guild.downgradeGuildState();

			SendGuildEntryMsg msg = new SendGuildEntryMsg(player);
			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

			//Update Map.

			final Session s = SessionManager.getSession(player);

			City.lastCityUpdate = System.currentTimeMillis();


			ArrayList<PlayerCharacter> guildMembers = SessionManager.getActivePCsInGuildID(guild.getObjectUUID());

			for (PlayerCharacter member : guildMembers) {
				ChatManager.chatGuildInfo(member, guild.getName() + " has broke fealty from " + toBreak.getName() + '!');
			}

			ArrayList<PlayerCharacter> breakFealtyMembers = SessionManager.getActivePCsInGuildID(toBreak.getObjectUUID());

			for (PlayerCharacter member : breakFealtyMembers) {
				ChatManager.chatGuildInfo(member, guild.getName() + " has broken fealty from " + toBreak.getName() + '!');
			}

			return true;


		}

		if (!toBreak.getSubGuildList().contains(guild)) {
			ErrorPopupMsg.sendErrorMsg(player, "Failure to break fealty!");
			return true;
		}

		if (GuildStatusController.isGuildLeader(player.getGuildStatus()) == false) {
			ErrorPopupMsg.sendErrorMsg(player, "Only guild leader can break fealty!");
			return true;
		}

		if (Bane.getBaneByAttackerGuild(guild) != null)
		{
			ErrorPopupMsg.sendErrorMsg(player, "You may break fealty with active bane!");
			return true;
		}

		if (!DbManager.GuildQueries.UPDATE_PARENT(guild.getObjectUUID(),MBServerStatics.worldUUID)) {
			ErrorPopupMsg.sendErrorMsg(player, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			return true;
		}

		switch (guild.getGuildState()) {
		case Sworn:
			guild.setNation(null);
			GuildManager.updateAllGuildTags(guild);
			GuildManager.updateAllGuildBinds(guild, null);
			break;
		case Province:
			guild.setNation(guild);
			GuildManager.updateAllGuildTags(guild);
			GuildManager.updateAllGuildBinds(guild, guild.getOwnedCity());
			break;
		}

		guild.downgradeGuildState();
		toBreak.getSubGuildList().remove(guild);

		if (toBreak.getSubGuildList().isEmpty())
			toBreak.downgradeGuildState();

		SendGuildEntryMsg msg = new SendGuildEntryMsg(player);
		dispatch = Dispatch.borrow(player, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		//Update Map.

		final Session s = SessionManager.getSession(player);

		City.lastCityUpdate = System.currentTimeMillis();


		ArrayList<PlayerCharacter> guildMembers = SessionManager.getActivePCsInGuildID(guild.getObjectUUID());

		for (PlayerCharacter member : guildMembers) {
			ChatManager.chatGuildInfo(member, guild.getName() + " has broke fealty from " + toBreak.getName() + '!');
		}

		ArrayList<PlayerCharacter> breakFealtyMembers = SessionManager.getActivePCsInGuildID(toBreak.getObjectUUID());

		for (PlayerCharacter member : breakFealtyMembers) {
			ChatManager.chatGuildInfo(member, guild.getName() + " has broken fealty from " + toBreak.getName() + '!');
		}

		return true;
	}
}
