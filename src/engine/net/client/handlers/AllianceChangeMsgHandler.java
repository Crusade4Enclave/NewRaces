package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.AllianceType;
import engine.exception.MsgSendException;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.AllianceChangeMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

/*
 * @Author:
 * @Summary: Processes application protocol message which handles
 * protecting and unprotecting city assets
 */
public class AllianceChangeMsgHandler extends AbstractClientMsgHandler {

	public AllianceChangeMsgHandler() {
		super(AllianceChangeMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		PlayerCharacter player;
		AllianceChangeMsg msg;


		// Member variable assignment

		msg = (AllianceChangeMsg) baseMsg;

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;



		Guild toGuild = null;
		toGuild = Guild.getGuild(msg.getSourceGuildID());
		if (toGuild.isEmptyGuild())
			return true;

		if (player.getGuild().isEmptyGuild())
			return true;



		switch (msg.getMsgType()){
		case AllianceChangeMsg.MAKE_ALLY:
		case 1: //allyfromRecommended
			player.getGuild().addGuildToAlliance(msg, AllianceType.Ally, toGuild, player);
			break;
		case AllianceChangeMsg.MAKE_ENEMY:
		case 2: //enemy recommend
			player.getGuild().addGuildToAlliance(msg, AllianceType.Enemy, toGuild, player);
			break;
		case 3:
		case 5:
		case 7:
			player.getGuild().removeGuildFromAllAlliances(toGuild);
			break;

		}
		msg.setMsgType(AllianceChangeMsg.INFO_SUCCESS);
		Dispatch dispatch = Dispatch.borrow(player, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);







		return true;

	}

	private static void MakeEnemy(Guild fromGuild, Guild toGuild, AllianceChangeMsg msg, ClientConnection origin) {

		// Member variable declaration
		Dispatch dispatch;

		// Member variable assignment

		if (fromGuild == null)
			return;

		if (toGuild == null)
			return;

		if (!Guild.sameGuild(origin.getPlayerCharacter().getGuild(), fromGuild)){
			msg.setMsgType(AllianceChangeMsg.ERROR_NOT_SAME_GUILD);
			dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			return;
		}

		if (!GuildStatusController.isInnerCouncil(origin.getPlayerCharacter().getGuildStatus()) && !GuildStatusController.isGuildLeader(origin.getPlayerCharacter().getGuildStatus())){
			msg.setMsgType(AllianceChangeMsg.ERROR_NOT_AUTHORIZED);
			dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			return;
		}


		dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);


	}

	private static void makeAlly(Guild fromGuild, Guild toGuild, AllianceChangeMsg msg, ClientConnection origin) {

		// Member variable declaration
		Dispatch dispatch;

		// Member variable assignment

		if (fromGuild == null)
			return;

		if (toGuild == null)
			return;

		dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);


	}

	private static void removeFromAlliance(Guild fromGuild, Guild toGuild, AllianceChangeMsg msg, ClientConnection origin) {

		// Member variable declaration
		Dispatch dispatch;

		// Member variable assignment

		if (fromGuild == null)
			return;

		if (toGuild == null)
			return;

		dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);


	}



}
