// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.exception.MsgSendException;
import engine.gameManager.GroupManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.group.GroupInviteMsg;
import engine.net.client.msg.group.GroupUpdateMsg;
import engine.objects.Group;
import engine.objects.PlayerCharacter;

public class GroupInviteHandler extends AbstractClientMsgHandler {

	public GroupInviteHandler() {
		super(GroupInviteMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg,

			ClientConnection origin) throws MsgSendException {
		GroupInviteMsg msg = (GroupInviteMsg) baseMsg;
		PlayerCharacter source = SessionManager.getPlayerCharacter(origin);

		if (source == null)
			return false;

		Group group = GroupManager.getGroup(source);

		// Group is new, create it.

		if (group == null)
			group = GroupInviteHandler.createGroup(source, origin);

		if (group == null)
			return false;

		if (!group.isGroupLead(source)) // person doing invite must be group lead
			return true;

		PlayerCharacter target = null;

		if (msg.getInvited() == 1) { // Use name for invite
			target = SessionManager.getPlayerCharacterByLowerCaseName(msg.getName().toLowerCase());
		} else { // Use ID for invite
			target = SessionManager.getPlayerCharacterByID(msg.getTargetID());
		}

		if (target == null)
			return false;

		// Client must be online

		if (SessionManager.getClientConnection(target) == null)
			return false;

		if (source == target) // Inviting self, so we're done
			return false;


		//Skip invite if target is ignoring source

		if (target.isIgnoringPlayer(source))
			return false;


		// dont block invites to people already in a group and
		// dont check for pending invites, the client does it
		// Send invite message to target

		msg.setSourceType(GameObjectType.PlayerCharacter.ordinal());
		msg.setSourceID(source.getObjectUUID());
		msg.setTargetType(0);
		msg.setTargetID(0);
		msg.setGroupType(GameObjectType.Group.ordinal());
		msg.setGroupID(group.getObjectUUID());
		msg.setInvited(1);
		msg.setName(source.getFirstName());

		Dispatch dispatch = Dispatch.borrow(target, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

		return true;
	}

	// this can only be called if you already know you are not in a group
	// and have issued an invite

	private static Group createGroup(PlayerCharacter pc, ClientConnection origin) {

		if (pc == null)
			return null;

		Group group = new Group(pc, GroupManager.incrGroupCount());
		group.addGroupMember(pc);
		GroupManager.addNewGroup(group);

		pc.setFollow(false);
		// Send add self to group message
		GroupUpdateMsg msg = new GroupUpdateMsg();
		msg.setGroup(group);
		msg.setPlayer(pc);
		msg.setMessageType(1);
		Dispatch dispatch = Dispatch.borrow(pc, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

		group.addUpdateGroupJob();

		return group;
	}

}
