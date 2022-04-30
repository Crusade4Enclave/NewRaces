// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.GroupManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.group.GroupUpdateMsg;
import engine.net.client.msg.group.RemoveFromGroupMsg;
import engine.objects.Group;
import engine.objects.PlayerCharacter;

import java.util.Set;

public class RemoveFromGroupHandler extends AbstractClientMsgHandler {

    public RemoveFromGroupHandler() {
        super(RemoveFromGroupMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg,
                                    ClientConnection origin) throws MsgSendException {

        // Declar member variables

        PlayerCharacter source;
        PlayerCharacter target;
        RemoveFromGroupMsg msg;
        Group group;
        GroupUpdateMsg gim;
        ClientConnection gcc;
        Set<PlayerCharacter> groupMembers;

        // Assign member variables

        msg = (RemoveFromGroupMsg) baseMsg;

        source = SessionManager.getPlayerCharacter(origin);

        if (source == null)
            return false;

        group = GroupManager.getGroup(source);

        if (group == null)
            return false;

        if (group.getGroupLead() != source) // Only group lead can remove
            return false;

        target = SessionManager.getPlayerCharacterByID(msg.getTargetID());

        if (target == null)
            return false;

        if (target == source) { // can't remove self, must quit
            RemoveFromGroupMsg reply = new RemoveFromGroupMsg();
            reply.setResponse(1);
            Dispatch dispatch = Dispatch.borrow(source, reply);
            DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
            return false;
        }

        gcc = SessionManager.getClientConnection(target);

        if (gcc != null) {

            // Cleanup group window for player quiting

            groupMembers = group.getMembers();

            for (PlayerCharacter groupMember : groupMembers) {

                if (groupMember == null)
                    continue;

                gim = new GroupUpdateMsg();
                gim.setGroup(group);
                gim.setPlayer(target);
                gim.setMessageType(3);
                gim.setPlayer(groupMember);
                Dispatch dispatch = Dispatch.borrow(target, gim);
                DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

            }
        }

        // Remove from group and clean up everyone elses window
        group.removeGroupMember(target);
        GroupManager.removeFromGroups(target);

        gim = new GroupUpdateMsg();
        gim.setGroup(group);
        gim.setMessageType(3);
        gim.setPlayer(target);
        group.sendUpdate(gim);

        String text = target.getFirstName() + " has left your group.";
        ChatManager.chatGroupInfo(source, text);

        return true;
    }

}
