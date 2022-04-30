package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.InterestManagement.InterestManager;
import engine.InterestManagement.WorldGrid;
import engine.db.archive.CharacterRecord;
import engine.db.archive.DataWarehouse;
import engine.db.archive.PvpRecord;
import engine.exception.MsgSendException;
import engine.gameManager.SessionManager;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.AbstractWorldObject;
import engine.objects.Account;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import engine.session.Session;
import org.pmw.tinylog.Logger;

/*
 * @Author:
 * @Summary: Processes application protocol message which requests
 * that a character enters the game world from login screen
 */

public class RequestEnterWorldHandler extends AbstractClientMsgHandler {

	public RequestEnterWorldHandler() {
		super(RequestEnterWorldMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		RequestEnterWorldMsg msg;

		msg = (RequestEnterWorldMsg) baseMsg;

		Session session = SessionManager.getSession(origin);

		if (session == null)
			return true;

		PlayerCharacter player = origin.getPlayerCharacter();

		WorldGrid.RemoveWorldObject(player);
		Dispatch dispatch;

		if (player == null) {
			Logger.error("Unable to find player for session" + session.getSessionID());
			origin.kickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Player not found.");
			return true;
		}

		player.setEnteredWorld(false);

		Account acc = SessionManager.getAccount(origin);

		if (acc.status.ordinal() < MBServerStatics.worldAccessLevel.ordinal() || MBServerStatics.blockLogin) {
			origin.disconnect();
			return true;
		}

		// Brand new character.  Send the city select screen

			if (player.getLevel() == 1 && player.getBindBuildingID() == -1) {
			SelectCityMsg scm = new SelectCityMsg(player, true);
				dispatch = Dispatch.borrow(player, scm);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
				return true;
			}

		player.resetRegenUpdateTime();

		// Map Data

		try {
			WorldDataMsg wdm = new WorldDataMsg();
			dispatch = Dispatch.borrow(player, wdm);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.error("WORLDDATAMESSAGE" + e.getMessage());
		}

		// Realm Data

		try {
			WorldRealmMsg wrm = new WorldRealmMsg();
			dispatch = Dispatch.borrow(player, wrm);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.error("REALMMESSAGE" + e.getMessage());
		}

		// Object Data
		WorldObjectMsg wom = new WorldObjectMsg(session, true);
		dispatch = Dispatch.borrow(player, wom);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

		player.getTimestamps().put("EnterWorld", System.currentTimeMillis());

		if (player.getLoc().equals(Vector3fImmutable.ZERO) || System.currentTimeMillis() > player.getTimeStamp("logout") + (15 * 60 * 1000)) {
			player.stopMovement(player.getBindLoc());
			player.setSafeMode();
			player.updateLocation();
			player.setRegion(AbstractWorldObject.GetRegionByWorldObject(player));
		}

		player.setTimeStamp("logout", 0);
		player.respawnLock.writeLock().lock();
		try{
			if (!player.isAlive()){
				Logger.info("respawning player on enter world.");
				player.respawn(true, true,true);
			}
				
		}catch (Exception e){
			Logger.error(e);
		}finally{
			player.respawnLock.writeLock().unlock();
		}
		

		player.resetDataAtLogin();

		InterestManager.INTERESTMANAGER.HandleLoadForEnterWorld(player);

		// If this is a brand new character...
		// when they enter world is a great time to write their
		// character record to the data warehouse.

		if (player.getHash() == null) {

			if (DataWarehouse.recordExists(Enum.DataRecordType.CHARACTER, player.getObjectUUID()) == false) {
				CharacterRecord characterRecord = CharacterRecord.borrow(player);
				DataWarehouse.pushToWarehouse(characterRecord);
			}
			player.setHash();
		}

        //
		// We will load the kill/death lists here as data is only pertinent
		// to characters actually logged into the game.
        //

		player.pvpKills = PvpRecord.getCharacterPvPHistory(player.getObjectUUID(), Enum.PvpHistoryType.KILLS);
		player.pvpDeaths = PvpRecord.getCharacterPvPHistory(player.getObjectUUID(), Enum.PvpHistoryType.DEATHS);

		SendOwnPlayerMsg sopm = new SendOwnPlayerMsg(SessionManager.getSession(origin));
		dispatch = Dispatch.borrow(player, sopm);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

		return true;
	}

	}
