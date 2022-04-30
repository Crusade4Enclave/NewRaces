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
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.group.GroupInviteResponseMsg;
import engine.net.client.msg.group.GroupUpdateMsg;
import engine.objects.Group;
import engine.objects.PlayerCharacter;

import java.util.Set;

import static engine.net.client.handlers.KeyCloneAudit.KEYCLONEAUDIT;

public class GroupInviteResponseHandler extends AbstractClientMsgHandler {

    public GroupInviteResponseHandler() {
        super(GroupInviteResponseMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg,
            ClientConnection origin) throws MsgSendException {

        GroupInviteResponseMsg msg = (GroupInviteResponseMsg) baseMsg;

        PlayerCharacter player = origin.getPlayerCharacter();

        if (player == null)
            return false;

        // if we are already in a group we are leaving it

        Group currGroup = GroupManager.getGroup(player);

        if (currGroup != null) // if we are already in a group we are leaving it
            GroupManager.LeaveGroup(player);

	// not sure we need to test for invites to wrong grp as only
        // 1 invite can be on screen at a time
        //if (invitesPending.get(player) != msg.getGroupID()) // Can't accept
        // invite to
        // wrong group
        //	return;

        Group group = GroupManager.getGroup(msg.getGroupID());

        if (group == null)
            return false;

        if (group.addGroupMember(player) == false)
            return false;
        {
            player.setFollow(true);
            GroupManager.addPlayerGroupMapping(player, group);
            Set<PlayerCharacter> members = group.getMembers();
            GroupUpdateMsg groupUpdateMsg;

            // Send all group members to player added
            for (PlayerCharacter groupMember : members) {

                groupUpdateMsg = new GroupUpdateMsg();
                groupUpdateMsg.setGroup(group);
                groupUpdateMsg.setMessageType(1);

                if (groupMember == null)
                    continue;

                if (groupMember.equals(player))
                    continue;

                groupUpdateMsg.setPlayer(groupMember);

               Dispatch dispatch = Dispatch.borrow(player, groupUpdateMsg);
               DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

            }

            // send new group member to everyone in group.
            groupUpdateMsg = new GroupUpdateMsg();
            groupUpdateMsg.setGroup(group);
            groupUpdateMsg.setMessageType(1);
            groupUpdateMsg.setPlayer(player);
            group.sendUpdate(groupUpdateMsg);

            String text = player.getFirstName() + " has joined the group.";
            ChatManager.chatGroupInfo(player, text);

            // Run Keyclone Audit

            KEYCLONEAUDIT.audit(player, group);

        return true;
    }
    }
}


