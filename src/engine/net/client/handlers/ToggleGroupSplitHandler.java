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
import engine.net.client.msg.group.GroupUpdateMsg;
import engine.net.client.msg.group.ToggleGroupSplitMsg;
import engine.objects.Group;
import engine.objects.PlayerCharacter;

public class ToggleGroupSplitHandler extends AbstractClientMsgHandler {

    public ToggleGroupSplitHandler() {
        super(ToggleGroupSplitMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg,
            ClientConnection origin) throws MsgSendException {

        // Member variable declaration
        
        PlayerCharacter source;
        Group group;
        boolean split;
        
        source = SessionManager.getPlayerCharacter(origin);
        
        if (source == null)
            return false;

        group = GroupManager.getGroup(source);
        
        if (group == null)
            return false;

        if (group.getGroupLead() != source) // Only group lead can toggle
            return false;

        split = group.toggleSplitGold();

        // update split button
        GroupUpdateMsg gum = new GroupUpdateMsg();
        gum.setGroup(group);
        gum.setMessageType(6);

        group.sendUpdate(gum);

        // Send split message
        
        if (split)
            ChatManager.chatGroupInfo(source, "Treasure is now being split.");
         else 
            ChatManager.chatGroupInfo(source, "Treasure is no longer being split.");

        return false;
    }

}
