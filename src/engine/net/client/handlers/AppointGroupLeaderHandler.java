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
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.group.AppointGroupLeaderMsg;
import engine.net.client.msg.group.GroupUpdateMsg;
import engine.objects.Group;
import engine.objects.PlayerCharacter;

public class AppointGroupLeaderHandler extends AbstractClientMsgHandler {

    public AppointGroupLeaderHandler() {
        super(AppointGroupLeaderMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
        AppointGroupLeaderMsg msg = (AppointGroupLeaderMsg) baseMsg;

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
        PlayerCharacter target = SessionManager.getPlayerCharacterByID(msg.getTargetID());
        if (target == null) {
            return false;
        }
        if (target == source) { // Can't appoint self leader
            AppointGroupLeaderMsg reply = new AppointGroupLeaderMsg();
            reply.setResponse(1);
            Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), reply);
            DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
            return false;
        }

        // Change Group Leader
        if (!group.setGroupLead(target.getObjectUUID())) {
            return false; // failed to update group leader
        }
        // Refresh everyones group list
        GroupUpdateMsg gim = new GroupUpdateMsg();
        gim.setGroup(group);
        gim.setPlayer(target);
        gim.setMessageType(2);
        gim.addPlayer(source);

        group.sendUpdate(gim);

        // Disable Formation
        target.setFollow(false);
        gim = new GroupUpdateMsg();
        gim.setGroup(group);
        gim.setPlayer(target);
        gim.setMessageType(8);
        group.sendUpdate(gim);

        String text = target.getFirstName() + " is the new group leader.";
        ChatManager.chatGroupInfo(source, text);

        return true;
    }

}
