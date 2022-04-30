// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.exception.MsgSendException;
import engine.gameManager.GroupManager;
import engine.gameManager.SessionManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.group.DisbandGroupMsg;
import engine.net.client.msg.group.GroupUpdateMsg;
import engine.objects.Group;
import engine.objects.PlayerCharacter;

public class DisbandGroupHandler extends AbstractClientMsgHandler {

    public DisbandGroupHandler() {
        super(DisbandGroupMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
        
        PlayerCharacter source = SessionManager.getPlayerCharacter(origin);
        if (source == null) {
            return false;
        }
        Group group = GroupManager.getGroup(source);
        if (group == null) {
            return false;
        }
        if (group.getGroupLead() != source) {
            return false;
        }

        // Clear all group member lists
        GroupUpdateMsg gim = new GroupUpdateMsg();
        gim.setGroup(group);
        gim.setMessageType(4);

        // send the disbanded popup to everyone in group
        group.sendUpdate(gim);

        // cleanup group
        GroupManager.deleteGroup(group);
        return true;

    }
}
