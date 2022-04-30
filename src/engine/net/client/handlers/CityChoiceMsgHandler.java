package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.gameManager.GuildManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.CityChoiceMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.TeleportRepledgeListMsg;
import engine.objects.City;
import engine.objects.Guild;
import engine.objects.PlayerCharacter;

/*
 * @Author:
 * @Summary: Processes application protocol message which keeps
 * client's tcp connection open.
 */

public class CityChoiceMsgHandler extends AbstractClientMsgHandler {

	public CityChoiceMsgHandler() {
		super(CityChoiceMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		CityChoiceMsg msg = (CityChoiceMsg) baseMsg;

		if (player == null)
			return true;

		switch (msg.getMsgType()) {
			case 5:
				TeleportRepledgeListMsg trlm = new TeleportRepledgeListMsg(player, false);
				trlm.configure();
				dispatch = Dispatch.borrow(player, trlm);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
				break;
			case 3:
				City city = City.getCity(msg.getCityID());

				if (city == null)
					return true;

				Guild cityGuild = city.getGuild();

				if (cityGuild == null)
					return true;

				if (player.getLevel() < cityGuild.getRepledgeMin() || player.getLevel() > cityGuild.getRepledgeMax())
					return true;

				//if repledge, reguild the player but set his building now.

				GuildManager.joinGuild(player, cityGuild, city.getObjectUUID(), Enum.GuildHistoryType.JOIN);
				break;
		}

		return true;
	}
}