package engine.net.client.handlers;

import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.gameManager.*;
import engine.job.JobScheduler;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ArcLoginNotifyMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.HotzoneChangeMsg;
import engine.net.client.msg.PetMsg;
import engine.objects.*;
import engine.server.MBServerStatics;
import engine.session.Session;
import org.pmw.tinylog.Logger;

public class ArcLoginNotifyMsgHandler extends AbstractClientMsgHandler {

	public ArcLoginNotifyMsgHandler() {
		super(ArcLoginNotifyMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player = SessionManager.getPlayerCharacter(origin);

		if (player == null) {
			Logger.error(ConfigManager.MB_WORLD_NAME.getValue()+ ".EnterWorld", "Unable to find player for session");
			origin.kickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Player not found.");
			return true;
		}

		// cancel logout Timer if exists
		if (player.getTimers().containsKey("Logout")) {

			JobScheduler.getInstance().cancelScheduledJob(player.getTimers().get("Logout"));
			player.getTimers().remove("Logout");
		}
		player.setTimeStamp("logout", 0);

		// refresh group window if still in group for both this player
		// and everyone else in the group

		if (GroupManager.getGroup(player) != null) {
			GroupManager.RefreshMyGroupList(player, origin);
			GroupManager.RefreshOthersGroupList(player);
		}

		player.setEnteredWorld(true);
		// Set player active
		player.resetRegenUpdateTime();
		player.setActive(true);

		//player.sendAllEffects(player.getClientConnection());
		// Send Enter world message to guild

		ChatManager.GuildEnterWorldMsg(player, origin);

		// Send Guild, Nation and IC MOTD
		GuildManager.enterWorldMOTD(player);
		ChatManager.sendSystemMessage(player, ConfigManager.MB_WORLD_GREETING.getValue());

		// Set player mask for QT
		if (player.getRace() != null && player.getRace().getToken() == -524731385)
			player.setObjectTypeMask(MBServerStatics.MASK_PLAYER | MBServerStatics.MASK_UNDEAD);
		else
			player.setObjectTypeMask(MBServerStatics.MASK_PLAYER);

		// If player not already in world, then set them to bind loc and add
		// to world

		if (player.newChar)
			player.newChar = false; // TODO Fix safe mode

		// PowersManager.applyPower(player, player, new
		// Vector3f(0f, 0f, 0f), -1661758934, 50, false);

		// Add player to the QT for tracking

		player.setLoc(player.getLoc());

		//send online status to friends.
		PlayerFriends.SendFriendsStatus(player, true);

		// Handle too many simultaneous logins from the same forum account by disconnecting the other account(s)

		Account thisAccount = SessionManager.getAccount(player);
		int maxAccounts = MBServerStatics.MAX_ACTIVE_GAME_ACCOUNTS_PER_DISCORD_ACCOUNT;

		if (maxAccounts > 0) {

			int count = 1;
			for (Account othAccount : SessionManager.getAllActiveAccounts()) {

				if (othAccount.equals(thisAccount))
					continue;

				if (thisAccount.discordAccount.equals(othAccount.discordAccount) == false)
					continue;

				count++;

				if (count > maxAccounts) {
					Session otherSession = SessionManager.getSession(othAccount);
					if (otherSession != null) {
						ClientConnection otherConn = otherSession.getConn();
						if (otherConn != null) {
							ChatManager.chatSystemInfo(player, "Only 4 accounts may be used simultaneously. Account '" + othAccount.getUname() + "' has been disconnected.");
							otherConn.disconnect();
						}
					}
				}
			}
		}

		player.setTimeStamp("logout", 0);

		if (player.getPet() != null) {
			PetMsg pm = new PetMsg(5, player.getPet());
			Dispatch dispatch = Dispatch.borrow(player, pm);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		}

		//Send current hotzone
		Zone hotzone = ZoneManager.getHotZone();

		if (hotzone != null) {
			HotzoneChangeMsg hcm = new HotzoneChangeMsg(hotzone.getObjectType().ordinal(), hotzone.getObjectUUID());
			Dispatch dispatch = Dispatch.borrow(player, hcm);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		}

		if (player.getGuild() != null && !player.getGuild().isErrant()) {
			Guild.UpdateClientAlliancesForPlayer(player);
		}
		return true;
	}
}