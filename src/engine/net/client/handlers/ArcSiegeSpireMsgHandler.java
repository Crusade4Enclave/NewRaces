package engine.net.client.handlers;

import engine.Enum.GameObjectType;
import engine.exception.MsgSendException;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ArcSiegeSpireMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.objects.Building;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;
import org.pmw.tinylog.Logger;

/*
 * @Author:
 * @Summary: Processes application protocol message which requests that
 * siege spires be toggled on or off.
 */

public class ArcSiegeSpireMsgHandler extends AbstractClientMsgHandler {

	public ArcSiegeSpireMsgHandler() {
		super(ArcSiegeSpireMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player;
		Building spire;
		ArcSiegeSpireMsg msg;

		msg = (ArcSiegeSpireMsg) baseMsg;

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;

		spire = (Building) DbManager.getObject(GameObjectType.Building, msg.getBuildingUUID());

		if (spire == null)
			return true;

		if (spire.getCity() == null)
			return true;
		
		spire.getCity().transactionLock.writeLock().lock();
		
		try{
			
		
		//can't activate a spire that's not rank 1.

		if (spire.getRank() < 1)
			return true;

		// can't activate a spire without a city

		if (spire.getCity() == null)
			return true;

		// Must have management authority for the spire

		if ((player.getGuild().equals(spire.getGuild()) == false)
				|| (GuildStatusController.isInnerCouncil(player.getGuildStatus()) == false) )
			return true;

		// Handle case where spire is sabotaged

		if (spire.getTimeStamp("DISABLED") > System.currentTimeMillis()) {
			ErrorPopupMsg.sendErrorPopup(player, 174); //This siege spire cannot be toggled yet.
			return true;
		}

		// Handle case where spire's toggle delay hasn't yet passed

		if (spire.getTimeStamp("TOGGLEDELAY") > System.currentTimeMillis()) {
			ErrorPopupMsg.sendErrorPopup(player, 174); //This siege spire cannot be toggled yet.
			return true;
		}

		// This protocol message is a toggle.  If it's currently active then disable
		// the spire.

		if (spire.isSpireIsActive()) {
			spire.disableSpire(false);
			return true;
		}

		// Must be enough gold on the spire to turn it on

		if (!spire.hasFunds(5000)){
			ErrorPopupMsg.sendErrorPopup(player, 127); // Not enough gold in strongbox
			return true;
		}

		if (spire.getStrongboxValue() < 5000) {
			ErrorPopupMsg.sendErrorPopup(player, 127); // Not enough gold in strongbox
			return true;
		}

		spire.transferGold(-5000,false);
		spire.enableSpire();

		// Spire is now enabled.  Reset the toggle delay

		spire.setTimeStamp("TOGGLEDELAY", System.currentTimeMillis() + (long) 10 * 60 * 1000);
		}catch(Exception e){
			Logger.error(e);
		}finally{
			spire.getCity().transactionLock.writeLock().unlock();
		}
		return true;
		
	}

}
