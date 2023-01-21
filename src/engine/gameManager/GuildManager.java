// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.Enum.GuildHistoryType;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.guild.AcceptInviteToGuildMsg;
import engine.net.client.msg.guild.GuildInfoMsg;
import engine.objects.*;
import org.joda.time.DateTime;

public enum GuildManager  {

	GUILDMANAGER;

	//Guild Error Message
	public static final int FAILURE_TO_SWEAR_GUILD = 45; //45: Failure to swear guild
	public static final int MUST_LEAVE_GUILD = 75;//75: You must leave your current guild before you can repledge
	public static final int NO_CHARTER_FOUND = 148; //148: Unable to find a matching petition to complete guild creation
	public static final int PROFANE_NAME = 149; //149: Guild name fails profanity check
	public static final int PROFANE_MOTTO = 150; //150: Guild motto fails profanity check
	public static final int UNIQUE_NAME = 151;//151: Guild name is not unique
	public static final int UNIQUE_CREST = 152;//152: Guild crest is not unique
	public static final int CREST_RESERVED = 153;	  //153: Guild crest is reserved
	public static final int CREST_COLOR_ERROR = 154; //154: All three crest colors cannot be the same

	public static boolean joinGuild(PlayerCharacter pc, Guild guild, GuildHistoryType historyType) {
		return joinGuild(pc, guild, 0, historyType);
	}

	//Used when repledging
	public static boolean joinGuild(PlayerCharacter pc, Guild guild, int cityID, GuildHistoryType historyType) {
		return joinGuild(pc, guild, cityID, true,historyType);
	}

	public static boolean joinGuild(PlayerCharacter playerCharacter, Guild guild, int cityID, boolean fromTeleportScreen, GuildHistoryType historyType) {

		// Member variable delcaration

		ClientConnection origin;
		AcceptInviteToGuildMsg msg;
		Dispatch dispatch;

		if (playerCharacter == null || guild == null)
			return false;

		// Member variable assignment

		origin = SessionManager.getClientConnection(playerCharacter);

		if (origin == null)
			return false;

		if (playerCharacter.getGuild().isEmptyGuild() == false && GuildStatusController.isGuildLeader(playerCharacter.getGuildStatus()))
			return false;

		if (playerCharacter.getGuild() != null && playerCharacter.getGuild().isGuildLeader(playerCharacter.getObjectUUID()))
			return false;

		if (playerCharacter.getGuild() != null && !playerCharacter.getGuild().isEmptyGuild()){
			if (DbManager.GuildQueries.ADD_TO_GUILDHISTORY(playerCharacter.getGuildUUID(), playerCharacter, DateTime.now(), GuildHistoryType.LEAVE)){
				GuildHistory guildHistory = new GuildHistory(playerCharacter.getGuildUUID(),playerCharacter.getGuild().getName(),DateTime.now(), GuildHistoryType.LEAVE) ;
				playerCharacter.getGuildHistory().add(guildHistory);
			}
		}

		playerCharacter.setInnerCouncil(false);
		playerCharacter.setGuildLeader(false);
		playerCharacter.setGuild(guild);

		// Cleanup guild stuff
		playerCharacter.resetGuildStatuses();

		// send success message to client
		if (fromTeleportScreen && guild.isNPCGuild())
			playerCharacter.setFullMember(true);

		msg = new AcceptInviteToGuildMsg(guild.getObjectUUID(), 1, 0);

		if (fromTeleportScreen) {
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		}
		if (DbManager.GuildQueries.ADD_TO_GUILDHISTORY(guild.getObjectUUID(), playerCharacter, DateTime.now(), historyType)){
			GuildHistory guildHistory = new GuildHistory(guild.getObjectUUID(),guild.getName(),DateTime.now(), historyType) ;
			playerCharacter.getGuildHistory().add(guildHistory);
		}

		DispatchMessage.sendToAllInRange(playerCharacter, new GuildInfoMsg(playerCharacter, guild, 2));

		// Send guild join message
		ChatManager.chatGuildInfo(playerCharacter,
				playerCharacter.getFirstName() + " has joined the guild");

		playerCharacter.incVer();

		return true;
		// TODO update player to world
	}

	public static void enterWorldMOTD(PlayerCharacter pc) {

		Guild guild;
		Guild nation;

		if (pc == null) {
			return;
		}

		guild = pc.getGuild();

		if (guild == null || guild.getObjectUUID() == 0) // Don't send to errant
			return;

		// Send Guild MOTD
		String motd = guild.getMOTD();
		if (motd.length() > 0) {
			ChatManager.chatGuildMOTD(pc, motd);
		}

		// Send Nation MOTD
		nation = guild.getNation();

		if (nation != null) {
			if (nation.getObjectUUID() != 0) { // Don't send to errant nation
				motd = nation.getMOTD();
				if (motd.length() > 0) {
					ChatManager.chatNationMOTD(pc, motd);
				}
			}
		}

		// Send IC MOTD if player is IC
		if (GuildStatusController.isInnerCouncil(pc.getGuildStatus())) {
			motd = guild.getICMOTD();
			if (motd.length() > 0) {
				ChatManager.chatICMOTD(pc, motd);
			}
		}
	}

	//Updates the bind point for everyone in guild

	public static void updateAllGuildBinds(Guild guild, City city) {

		if (guild == null)
			return;

		int cityID = (city != null) ? city.getObjectUUID() : 0;
		
	

		//update binds ingame
		

		for (PlayerCharacter playerCharacter : Guild.GuildRoster(guild)) {
			boolean updateBindBuilding = false;
			
			Building oldBoundBuilding = BuildingManager.getBuildingFromCache(playerCharacter.getBindBuildingID());
			
			if (oldBoundBuilding == null || oldBoundBuilding.getBlueprint() == null || oldBoundBuilding.getBlueprint().getBuildingGroup().equals(BuildingGroup.TOL))
				updateBindBuilding = true;
			
			
			
			if (updateBindBuilding){
				Building bindBuilding = null;
				if (city != null)
					if (city.getTOL() != null)
						bindBuilding = city.getTOL();
				
				if (bindBuilding == null)
					bindBuilding = PlayerCharacter.getBindBuildingForGuild(playerCharacter);
				
				playerCharacter.setBindBuildingID(bindBuilding != null ? bindBuilding.getObjectUUID() : 0);
			}
				

		}
	}

	//This updates tags for all online players in a guild.
	public static void updateAllGuildTags(Guild guild) {

		if (guild == null)
			return;

		for (PlayerCharacter player : SessionManager.getAllActivePlayerCharacters()) {

			if (player.getGuild().equals(guild))
				DispatchMessage.sendToAllInRange(player, new GuildInfoMsg(player , guild, 2));

		}
	}

}
