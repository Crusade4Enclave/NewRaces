// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.exception.MsgSendException;
import engine.gameManager.MovementManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.MoveToPointMsg;
import engine.objects.PlayerCharacter;

public class MoveToPointHandler extends AbstractClientMsgHandler {

    public MoveToPointHandler() {
        super(MoveToPointMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg,
            ClientConnection origin) throws MsgSendException {
        MoveToPointMsg msg = (MoveToPointMsg) baseMsg;

        PlayerCharacter pc = (origin != null) ? (origin.getPlayerCharacter()) : null;
        if (pc == null)
            return false;

        MovementManager.movement(msg, pc);
        return true;
    }

}
