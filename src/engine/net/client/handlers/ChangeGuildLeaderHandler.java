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
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ChangeGuildLeaderMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.guild.GuildInfoMsg;
import engine.net.client.msg.guild.GuildListMsg;
import engine.objects.City;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

public class ChangeGuildLeaderHandler extends AbstractClientMsgHandler {

	public ChangeGuildLeaderHandler() {
		super(ChangeGuildLeaderMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		ChangeGuildLeaderMsg msg;
		PlayerCharacter sourcePlayer;
		PlayerCharacter targetPlayer;
		City city;

		msg = (ChangeGuildLeaderMsg) baseMsg;
		sourcePlayer = origin.getPlayerCharacter();
		if (sourcePlayer == null)
			return true;
		if (GuildStatusController.isGuildLeader(sourcePlayer.getGuildStatus()) == false)
			return true;



		Guild glGuild = sourcePlayer.getGuild();

		if (glGuild == null)
			return true;

		if (!glGuild.isGuildLeader(sourcePlayer.getObjectUUID()))
			return true;
		targetPlayer = (PlayerCharacter) DbManager.getObject(GameObjectType.PlayerCharacter, msg.getTargetID());


		if (targetPlayer == null)
			return true;

		if (GuildStatusController.isGuildLeader(targetPlayer.getGuildStatus()))
			return true;

		if (!Guild.sameGuild(sourcePlayer.getGuild(),targetPlayer.getGuild()))
			return false;


		//updateSource will generate a new promote/demote screen for sourcePlayer
		//updateTarget will sync guild info for the target and all players in range




		String targetName = null;
		boolean isMale = true;
		boolean updateTarget;

		Enum.GuildType t = Enum.GuildType.getGuildTypeFromInt(sourcePlayer.getGuild().getCharter());


		if (!DbManager.GuildQueries.SET_GUILD_LEADER(targetPlayer.getObjectUUID(), glGuild.getObjectUUID())){
			ChatManager.chatGuildError(sourcePlayer, "Failed to change guild leader!");
			return false;
		}

		glGuild.setGuildLeader(targetPlayer);



		if (glGuild.getOwnedCity() != null){
			city = glGuild.getOwnedCity();
			if (!city.transferGuildLeader(targetPlayer)){
				ChatManager.chatGuildError(sourcePlayer, "Failed to Transfer City Objects. Contact CCR!");
				return false;
			}

		}



		targetPlayer.setGuildLeader(true);
		targetPlayer.setInnerCouncil(true);
		targetPlayer.setFullMember(true);
		targetPlayer.incVer();
		targetName = targetPlayer.getFirstName();
		updateTarget = true;


		ChatManager.chatGuildInfo(sourcePlayer.getGuild(),
				targetName + " has been promoted to "
						+ "guild leader!");

		//These values record a change, not the new value...



		//updateOldGuildLeader
		sourcePlayer.setInnerCouncil(true);
		sourcePlayer.setFullMember(true);
		sourcePlayer.setGuildLeader(false);
		sourcePlayer.incVer();
		
		GuildInfoMsg guildInfoMsg = new GuildInfoMsg(sourcePlayer, sourcePlayer.getGuild(), 2);
		 Dispatch dispatch = Dispatch.borrow(sourcePlayer, guildInfoMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

		GuildListMsg guildListMsg = new GuildListMsg(sourcePlayer.getGuild());

		dispatch = Dispatch.borrow(sourcePlayer, guildListMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
		
		

		if (targetPlayer != null) {
			if (updateTarget)
				DispatchMessage.sendToAllInRange(targetPlayer, new GuildInfoMsg(targetPlayer, targetPlayer.getGuild(), 2));
		}

		return true;

	}

}
