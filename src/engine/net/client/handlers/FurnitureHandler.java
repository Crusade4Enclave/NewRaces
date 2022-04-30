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
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.FurnitureMsg;
import engine.objects.PlayerCharacter;

public class FurnitureHandler extends AbstractClientMsgHandler {

	public FurnitureHandler() {
		super(FurnitureMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg,
			ClientConnection origin) throws MsgSendException {

		FurnitureMsg msg = (FurnitureMsg) baseMsg;

		PlayerCharacter pc = origin.getPlayerCharacter();
		if (pc == null) {
			return false;
		}
		
		if (msg.getType() == 1)
		msg.setType(2);
		
		if (msg.getType() == 3)
			msg.setType(2);
		 Dispatch dispatch = Dispatch.borrow(pc, msg);
         DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		return true;
	}

}
