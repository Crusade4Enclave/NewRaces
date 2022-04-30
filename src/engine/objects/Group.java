// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.GroupManager;
import engine.job.JobScheduler;
import engine.jobs.UpdateGroupJob;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.msg.group.GroupUpdateMsg;
import engine.server.MBServerStatics;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Group extends AbstractWorldObject {

	private PlayerCharacter groupLead;
    public final Set<PlayerCharacter> members;

	private boolean splitGold = true;
	private int formation = 2;

	private UpdateGroupJob updateGroupJob = null;

	/**
	 * No Id Constructor
	 */
	public Group( PlayerCharacter pc) {
		super();
		this.groupLead = pc;
		this.members = Collections.newSetFromMap(new ConcurrentHashMap<>());
	}

	/**
	 * Normal Constructor
	 */
	public Group( PlayerCharacter pc, int newUUID) {
		super(newUUID);
		this.groupLead = pc;
        this.members = Collections.newSetFromMap(new ConcurrentHashMap<>());
	}

	/*
	 * Getters
	 */
	public PlayerCharacter getGroupLead() {
		return this.groupLead;
	}

	public Set<PlayerCharacter> getMembers() {
		return this.members;
	}

	public boolean getSplitGold() {
		return this.splitGold;
	}

	public int getFormation() {
		return this.formation;
	}

	public String getFormationName() {
		return MBServerStatics.FORMATION_NAMES[this.formation];
	}

	/*
	 * Setters
	 */
	public void setFormation(int value) {
		if (value < 0 || value > 8)
			value = 2; // Default Box
		this.formation = value;
	}

	public boolean setGroupLead(int ID) {
		for (PlayerCharacter pc : this.members) {
			if (pc.getObjectUUID() == ID) {
				this.groupLead = pc;
				return true;
			}
		}
		return false;
	}

	public void setSplitGold(boolean value) {
		this.splitGold = value;
	}

	/*
	 * Utils
	 */
	public boolean isGroupLead(int ID) {
		return (this.groupLead.getObjectUUID() == ID);
	}

	public boolean isGroupLead(PlayerCharacter pc) {
		if (pc == null || this.groupLead == null)
			return false;
		return (this.groupLead.getObjectUUID() == pc.getObjectUUID());
	}

	public boolean toggleSplitGold() {
        this.splitGold = this.splitGold == false;
		return this.splitGold;
	}

	public void sendUpdate(GroupUpdateMsg msg) {

        for (PlayerCharacter pc : this.members) {
            Dispatch dispatch = Dispatch.borrow(pc, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
		}
	}

	public boolean addGroupMember(PlayerCharacter pc) {

		if (this.members.size() > 9) // group full
			return false;

		if (this.members.contains(pc)) // Can't add player twice
			return false;

		this.members.add(pc);
		return true;
	}

	public int removeGroupMember(PlayerCharacter pc) {

		this.members.remove(pc); // remove player
		return this.members.size();
	}

	public void clearMembers() {
		this.members.clear();
	}

	public static boolean sameGroup(PlayerCharacter a, PlayerCharacter b) {

		if (a == null || b == null)
			return false;

		Group aG = GroupManager.getGroup(a);
		Group bG = GroupManager.getGroup(b);

		if (aG == null || bG == null)
			return false;

        return aG.getObjectUUID() == bG.getObjectUUID();

    }

	public void addUpdateGroupJob() {
		this.updateGroupJob = new UpdateGroupJob(this);
		JobScheduler.getInstance().scheduleJob(this.updateGroupJob, MBServerStatics.UPDATE_GROUP_RATE);
	}

	public void removeUpdateGroupJob() {
		this.updateGroupJob.cancelJob();
		this.updateGroupJob = null;
	}

	/*
	 * Database
	 */
	@Override
	public void updateDatabase() {
		// TODO Create update logic.
	}

	@Override
	public void runAfterLoad() {}
}
