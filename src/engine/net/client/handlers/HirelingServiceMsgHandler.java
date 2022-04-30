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


import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.HirelingServiceMsg;
import engine.net.client.msg.ManageNPCMsg;
import engine.objects.Building;
import engine.objects.NPC;
import engine.objects.PlayerCharacter;

public class HirelingServiceMsgHandler extends AbstractClientMsgHandler {

	public HirelingServiceMsgHandler() {
		super(HirelingServiceMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player;
		HirelingServiceMsg msg;

		msg = (HirelingServiceMsg) baseMsg;

		// get PlayerCharacter of person accepting invite

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;
		
	switch (msg.messageType){
	case HirelingServiceMsg.SETREPAIRCOST:
		Building building = BuildingManager.getBuildingFromCache(msg.buildingID);
		
		if (building == null)
			return true;
		
		NPC npc = NPC.getFromCache(msg.npcID);
		
		if (npc == null)
			return true;
		
		if (!BuildingManager.playerCanManage(player, building))
			return true;
		
	
		
		npc.setRepairCost(msg.repairCost);
		ManageNPCMsg outMsg = new ManageNPCMsg(npc);
		Dispatch dispatch = Dispatch.borrow(player, msg);
		
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		dispatch = Dispatch.borrow(player, outMsg);
		
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		break;
	}
		
		
		return true;

		
	}

}
