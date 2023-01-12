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
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ArcMineWindowChangeMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.KeepAliveServerClientMsg;
import engine.objects.Building;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.time.LocalDateTime;

/*
 * @Author:
 * @Summary: Processes requests to change a mine's opendate
 */

public class MineWindowChangeHandler extends AbstractClientMsgHandler {

	public MineWindowChangeHandler() {
		super(ArcMineWindowChangeMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter playerCharacter = SessionManager.getPlayerCharacter(origin);
		ArcMineWindowChangeMsg mineWindowChangeMsg = (ArcMineWindowChangeMsg)baseMsg;
		int newMineTime;

		if (playerCharacter == null)
			return true;

		Building treeOfLife =  BuildingManager.getBuildingFromCache(mineWindowChangeMsg.getBuildingID());

		if (treeOfLife == null)
			return true;

		if (treeOfLife.getBlueprintUUID() == 0)
			return true;

		if (treeOfLife.getBlueprint().getBuildingGroup() != Enum.BuildingGroup.TOL)
			return true;

		Guild mineGuild = treeOfLife.getGuild();
		if (mineGuild == null)
			return true;

		if (!Guild.sameGuild(mineGuild, playerCharacter.getGuild()))
			return true;  //must be same guild

		if (GuildStatusController.isInnerCouncil(playerCharacter.getGuildStatus()) == false) // is this only GL?
			return true;

		newMineTime = mineWindowChangeMsg.getTime();

		// Enforce 15hr restriction between WOO edits

		if (LocalDateTime.now().isBefore(mineGuild.lastWooEditTime.plusHours(14))) {
			ErrorPopupMsg.sendErrorMsg(playerCharacter, "You must wait 15 hours between WOO changes.");
			return true;
		}

		//hodge podge sanity check to make sure they don't set it before early window and is not set at late window.

		if (newMineTime < MBServerStatics.MINE_EARLY_WINDOW &&
				newMineTime != MBServerStatics.MINE_LATE_WINDOW)
			return true;    //invalid mine time, must be in range

		// Update guild mine time

		if (!DbManager.GuildQueries.UPDATE_MINETIME(mineGuild.getObjectUUID(), newMineTime)) {
			Logger.error("MineWindowChange", "Failed to update mine time for guild " + mineGuild.getObjectUUID());
			ChatManager.chatGuildError(playerCharacter, "Failed to update the mine time");
			return true;
		}

		mineGuild.setMineTime(newMineTime);
		mineGuild.lastWooEditTime = LocalDateTime.now();

		// Update guild WOO timer for reboot persistence

		if (!DbManager.GuildQueries.SET_LAST_WOO_UPDATE(mineGuild, mineGuild.lastWooEditTime)) {
			Logger.error("MineWindowChange", "Failed to update woo timer for guild " + mineGuild.getObjectUUID());
			ChatManager.chatGuildError(playerCharacter, "A Serious error has for to occurred.");
			return true;
		}

		ChatManager.chatGuildInfo(playerCharacter, "Mine time updated.");
            
            return true;
	}

}