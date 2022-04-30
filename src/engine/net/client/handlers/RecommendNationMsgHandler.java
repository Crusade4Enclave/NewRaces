package engine.net.client.handlers;

import engine.Enum.AllianceType;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.client.ClientConnection;
import engine.net.client.msg.AllianceChangeMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.RecommendNationMsg;
import engine.objects.Guild;
import engine.objects.PlayerCharacter;

/*
 * @Author:
 * @Summary: Processes application protocol message which handles
 * protecting and unprotecting city assets
 */
public class RecommendNationMsgHandler extends AbstractClientMsgHandler {

	public RecommendNationMsgHandler() {
		super(RecommendNationMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		PlayerCharacter player;
		RecommendNationMsg msg;


		// Member variable assignment

		msg = (RecommendNationMsg) baseMsg;

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;


		RecommendNationMsgHandler.RecommendNation(player.getGuild(), Guild.getGuild(msg.getGuildID()), msg, origin);




		//		dispatch = Dispatch.borrow(player, baseMsg);
		//		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		return true;

	}

	private static void RecommendNation(Guild fromGuild, Guild toGuild, RecommendNationMsg msg, ClientConnection origin) {

		// Member variable declaration
		Dispatch dispatch;

		// Member variable assignment

		if (fromGuild == null)
			return;

		if (toGuild == null)
			return;

		AllianceType allianceType;
		if (msg.getAlly() == 1)
			allianceType = AllianceType.RecommendedAlly;
		else
			allianceType = AllianceType.RecommendedEnemy;

		if (!fromGuild.addGuildToAlliance(new AllianceChangeMsg(origin.getPlayerCharacter(),fromGuild.getObjectUUID(), toGuild.getObjectUUID(), (byte)0, 0), allianceType, toGuild, origin.getPlayerCharacter()))
			return;
		String alliance = msg.getAlly() == 1? "ally" : "enemy";

		ChatManager.chatGuildInfo(fromGuild, origin.getPlayerCharacter().getFirstName() + " has recommended " + toGuild.getName() + " as an " + alliance );

		//		dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
		//		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);


	}



}
