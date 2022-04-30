// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.GroupManager;
import engine.gameManager.SessionManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.group.FormationFollowMsg;
import engine.net.client.msg.group.GroupUpdateMsg;
import engine.objects.Group;
import engine.objects.PlayerCharacter;

public class FormationFollowHandler extends AbstractClientMsgHandler {

    public FormationFollowHandler() {
        super(FormationFollowMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin)
            throws MsgSendException {
        FormationFollowMsg msg = (FormationFollowMsg) baseMsg;

        PlayerCharacter source = SessionManager.getPlayerCharacter(origin);

        if (source == null)
            return false;

        Group group = GroupManager.getGroup(source);

        if (group == null)
            return false;

        if (msg.isFollow()) {// Toggle follow
            source.toggleFollow();
            if (group.getGroupLead() == source) {
                if (source.getFollow()) {
                    ChatManager.chatGroupInfo(source, "falls into formation");
                } else {
                    ChatManager.chatGroupInfo(source, "breaks formation");
                }
            } else {
                if (source.getFollow()) {
                    ChatManager.chatGroupInfo(source, source.getFirstName() + " falls into formation");
                } else {
                    ChatManager.chatGroupInfo(source, source.getFirstName() + " breaks formation");
                }
            }
            GroupUpdateMsg gum = new GroupUpdateMsg();
            gum.setGroup(group);
            gum.setPlayer(source);
            gum.setMessageType(8);

            group.sendUpdate(gum);
        } else {// Set Formation
            if (group.getGroupLead() != source) {
                return false;
            }
            group.setFormation(msg.getFormation());

        }
        return true;
    }

}
