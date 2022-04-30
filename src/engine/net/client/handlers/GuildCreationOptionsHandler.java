// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.exception.MsgSendException;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.guild.GuildCreationOptionsMsg;
import engine.objects.PlayerCharacter;

public class GuildCreationOptionsHandler extends AbstractClientMsgHandler {

	public GuildCreationOptionsHandler() {
		super(GuildCreationOptionsMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		GuildCreationOptionsMsg msg = (GuildCreationOptionsMsg) baseMsg;
        PlayerCharacter sourcePlayer = origin.getPlayerCharacter();
        Dispatch dispatch;

		if(msg.getScreenType() == 1) {
			msg.setScreenType(3);
		} else if(msg.getScreenType() == 2) {
			msg.setScreenType(4);
		}

        if (sourcePlayer == null)
            return true;

        dispatch = Dispatch.borrow(sourcePlayer, msg);
        DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

		return true;
	}

}
