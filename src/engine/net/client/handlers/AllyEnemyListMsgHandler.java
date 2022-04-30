package engine.net.client.handlers;

import engine.Enum;
import engine.exception.MsgSendException;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.AllyEnemyListMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.Guild;
import engine.objects.PlayerCharacter;

/*
 * @Author:
 * @Summary: Processes application protocol message which handles
 * protecting and unprotecting city assets
 */
public class AllyEnemyListMsgHandler extends AbstractClientMsgHandler {

	public AllyEnemyListMsgHandler() {
		super(AllyEnemyListMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		PlayerCharacter player;
		AllyEnemyListMsg msg;


		// Member variable assignment

		msg = (AllyEnemyListMsg) baseMsg;

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;


		AllyEnemyListMsgHandler.showAllyEnemyList(player.getGuild(), Guild.getGuild(msg.getGuildID()), msg, origin);




		//		dispatch = Dispatch.borrow(player, baseMsg);
		//		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		return true;

	}

	private static void showAllyEnemyList(Guild fromGuild, Guild toGuild, AllyEnemyListMsg msg, ClientConnection origin) {

		// Member variable declaration
		Dispatch dispatch;

		// Member variable assignment

		if (fromGuild == null)
			return;

		if (toGuild == null)
			return;
		dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		//		UpdateClientAlliancesMsg ucam = new UpdateClientAlliancesMsg();
		//
		//		dispatch = Dispatch.borrow(origin.getPlayerCharacter(), ucam);
		//		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);


	}



}
