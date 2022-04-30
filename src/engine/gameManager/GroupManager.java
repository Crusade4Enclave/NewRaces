// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.Enum;
import engine.exception.MsgSendException;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.UpdateGoldMsg;
import engine.net.client.msg.group.GroupUpdateMsg;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum GroupManager {

    GROUPMANAGER;

    // used for quick lookup of groups by the ID of the group sent in the msg
    private static final ConcurrentHashMap<Integer, Group> groupsByID = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_HIGH);

    // an index for playercharacters to group membership
    private static final ConcurrentHashMap<AbstractCharacter, Group> groupsByAC = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_HIGH);
    private static int groupCount = 0;

    /*
     * Class Implementation
     */
    public static void removeFromGroups(AbstractCharacter ac) {
        Group gr = null;

        synchronized (GroupManager.groupsByAC) {
            gr = GroupManager.groupsByAC.remove(ac);
        }

    }

    public static void LeaveGroup(ClientConnection origin) throws MsgSendException {
        PlayerCharacter source = SessionManager.getPlayerCharacter(origin);
        LeaveGroup(source);
    }

    public static void LeaveGroup(PlayerCharacter source) throws MsgSendException {

        if (source == null)
            return;

        Group group = GroupManager.groupsByAC.get(source);

        if (group == null) // source is not in a group
            return;

        // Cleanup group window for player quiting
        GroupUpdateMsg groupUpdateMsg = new GroupUpdateMsg();
        groupUpdateMsg.setGroup(group);
        groupUpdateMsg.setPlayer(source);
        groupUpdateMsg.setMessageType(3);

        Set<PlayerCharacter> groupMembers = group.getMembers();

        for (PlayerCharacter groupMember : groupMembers) {

            if (groupMember == null)
                continue;

            groupUpdateMsg = new GroupUpdateMsg();
            groupUpdateMsg.setGroup(group);
            groupUpdateMsg.setPlayer(source);
            groupUpdateMsg.setMessageType(3);
            groupUpdateMsg.setPlayer(groupMember);
            Dispatch dispatch = Dispatch.borrow(source, groupUpdateMsg);
            DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

        }

        // Remove from group
        int size = group.removeGroupMember(source);
        // remove from the group -> ac mapping list
        GroupManager.groupsByAC.remove(source);

        if (size == 0) {
            GroupManager.deleteGroup(group);
            return; // group empty so cleanup group and we're done
        }

        // set new group lead if needed
        if (group.getGroupLead() == source) {
            PlayerCharacter newLead = group.getMembers().iterator().next();
            group.setGroupLead(newLead.getObjectUUID());
            groupUpdateMsg = new GroupUpdateMsg();
            groupUpdateMsg.setGroup(group);
            groupUpdateMsg.setPlayer(newLead);
            groupUpdateMsg.addPlayer(source);
            groupUpdateMsg.setMessageType(2);
            group.sendUpdate(groupUpdateMsg);

            // Disable Formation
            newLead.setFollow(false);
            groupUpdateMsg = new GroupUpdateMsg();
            groupUpdateMsg.setGroup(group);
            groupUpdateMsg.setPlayer(newLead);
            groupUpdateMsg.setMessageType(8);
            group.sendUpdate(groupUpdateMsg);
        }

        //send message to group
        PlayerCharacter pc = group.getGroupLead();
        //Fixed
        String text = source.getFirstName() + " has left the group.";
        ChatManager.chatGroupInfo(pc, text);

        // cleanup other group members screens
        groupUpdateMsg = new GroupUpdateMsg();
        groupUpdateMsg.setGroup(group);
        groupUpdateMsg.setPlayer(source);
        groupUpdateMsg.setMessageType(3);
        group.sendUpdate(groupUpdateMsg);

    }

    //This updates health/stamina/mana and loc of all players in group

    public static void RefreshWholeGroupList(PlayerCharacter source, ClientConnection origin, Group gexp) {

        if (source == null || origin == null)
            return;

        Group group = GroupManager.groupsByAC.get(source);

        if (group == null)
            return;

        if (gexp.getObjectUUID() != group.getObjectUUID())
            return;

        Set<PlayerCharacter> groupMembers = group.getMembers();

        if (groupMembers.size() < 2)
            return;

        // Send all group members health/mana/stamina/loc.

        for (PlayerCharacter groupMember : groupMembers) {

            if (groupMember == null)
                continue;

            GroupUpdateMsg gum = new GroupUpdateMsg(5, 1, groupMembers, group);
            gum.setPlayerUUID(groupMember.getObjectUUID());

            Dispatch dispatch = Dispatch.borrow(groupMember, gum);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

        }
    }

    public static void RefreshMyGroupList(PlayerCharacter source, ClientConnection origin) {

        if (source == null || origin == null)
            return;

        Group group = GroupManager.groupsByAC.get(source);

        if (group == null)
            return;

        Set<PlayerCharacter> members = group.getMembers();


        // Send all group members to player added
        for (PlayerCharacter groupMember : members) {

            if (groupMember == null)
                continue;

            GroupUpdateMsg gum = new GroupUpdateMsg();
            gum.setGroup(group);
            gum.setMessageType(1);
            gum.setPlayer(groupMember);
            Dispatch dispatch = Dispatch.borrow(groupMember, gum);
            DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

        }
    }

    public static void RefreshMyGroupListSinglePlayer(PlayerCharacter source, ClientConnection origin, PlayerCharacter playerToRefresh) {

        // send msg type 1 to the source player on this connection to update the group
        // list stats for the player that has just been loaded

        if (source == null || origin == null || playerToRefresh == null)
            return;

        Group group = GroupManager.groupsByAC.get(source);

        if (group == null)
            return;

        // only send if the 2 players are in the same group
        if (group != GroupManager.groupsByAC.get(playerToRefresh))
            return;

        GroupUpdateMsg gum = new GroupUpdateMsg();
        gum.setGroup(group);
        gum.setMessageType(1);
        gum.setPlayer(playerToRefresh);

        Dispatch dispatch = Dispatch.borrow(source, gum);
        DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
    }

    public static void RefreshOthersGroupList(PlayerCharacter source) {

        // refresh my stats on everyone elses group list

        if (source == null)
            return;

        Group group = GroupManager.groupsByAC.get(source);

        if (group == null)
            return;

        //construct message
        GroupUpdateMsg gim = new GroupUpdateMsg();
        gim.setGroup(group);
        gim.setMessageType(1);
        gim.setPlayer(source);
        group.sendUpdate(gim);

    }

    public static int incrGroupCount() {
        GroupManager.groupCount++;
        return GroupManager.groupCount;
    }

    public static boolean deleteGroup(Group g) {

        // remove all players from the mapping
        Set<PlayerCharacter> members = g.getMembers();

        for (PlayerCharacter pc : members) {
            if (pc != null) {
                GroupManager.removeFromGroups(pc);
            }
        }
        // remove the group ID from the list
        GroupManager.groupsByID.remove(g.getObjectUUID());
        g.clearMembers();

        g.removeUpdateGroupJob();

        return true;
    }

    public static Group addNewGroup(Group group) {

        PlayerCharacter pc = group.getGroupLead();

        GroupManager.addGroup(group);

        if (pc != null) {
            GroupManager.addPlayerGroupMapping(pc, group);
            return group;
        }
        return null;

    }

    private static Group addGroup(Group group) {

        if (GroupManager.groupsByID.containsKey(group.getObjectUUID())) {
            return null;
        }

        GroupManager.groupsByID.put(group.getObjectUUID(), group);
        return group;
    }

    public static Group getGroup(int groupID) {
        return GroupManager.groupsByID.get(groupID);
    }

    public static Group getGroup(PlayerCharacter pc) {

        return GroupManager.groupsByAC.get(pc);
    }

    public static void addPlayerGroupMapping(PlayerCharacter pc, Group grp) {
        GroupManager.groupsByAC.put(pc, grp);
    }

    public static boolean goldSplit(PlayerCharacter pc, Item item, ClientConnection origin, AbstractWorldObject tar) {
        if (item == null || pc == null || tar == null || item.getItemBase() == null) {
            Logger.error( "null something");
            return false;
        }

        if (item.getItemBase().getUUID() != 7) //only split goldItem
            return false;

        Group group = getGroup(pc);

        if (group == null || !group.getSplitGold()) //make sure player is grouped and split is on
            return false;


        ArrayList<PlayerCharacter> playersSplit = new ArrayList<>();

        //get group members

        for (PlayerCharacter groupMember: group.getMembers()){
            if (pc.getLoc().distanceSquared2D(groupMember.getLoc()) > MBServerStatics.CHARACTER_LOAD_RANGE * MBServerStatics.CHARACTER_LOAD_RANGE)
                continue;

            if (!groupMember.isAlive())
                continue;

            playersSplit.add(groupMember);
        }


        //make sure more then one group member in loot range
        int size = playersSplit.size();

        if (size < 2)
            return false;

        int total = item.getNumOfItems();
        int amount = total / size;
        int dif = total - (size * amount);

        if (AbstractWorldObject.IsAbstractCharacter(tar)) {
        }
        else if (tar.getObjectType().equals(Enum.GameObjectType.Corpse)) {
            Corpse corpse = (Corpse) tar;
            corpse.getInventory().remove(item);
        }
        else {
            Logger.error("target not corpse or character");
            return false;
        }

        if (item.getObjectType() == Enum.GameObjectType.MobLoot){
            if (tar.getObjectType() == Enum.GameObjectType.Mob){
                ((Mob)tar).getCharItemManager().delete(item);
            }else
                item.setNumOfItems(0);
        }else
            item.setNumOfItems(0);
        for (PlayerCharacter splitPlayer : playersSplit) {



            int amt = (group.isGroupLead(splitPlayer)) ? (amount + dif) : amount;
            if (amt > 0)
                splitPlayer.getCharItemManager().addGoldToInventory(amt, false);
        }

        for (PlayerCharacter splitPlayer : playersSplit) {


            UpdateGoldMsg ugm = new UpdateGoldMsg(splitPlayer);
            ugm.configure();

            Dispatch dispatch = Dispatch.borrow(splitPlayer, ugm);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
        }

        UpdateGoldMsg updateTargetGold = new UpdateGoldMsg(tar);
        updateTargetGold.configure();
        DispatchMessage.dispatchMsgToInterestArea(tar, updateTargetGold, Enum.DispatchChannel.SECONDARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);


        //		//TODO send group split message
        String text = "Group Split: " + amount;
        ChatManager.chatGroupInfo(pc, text);

        return true;
    }
}
