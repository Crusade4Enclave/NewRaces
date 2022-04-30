// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.gameManager.GroupManager;
import engine.job.AbstractJob;
import engine.net.client.ClientConnection;
import engine.objects.Group;
import engine.objects.PlayerCharacter;

public class RefreshGroupJob extends AbstractJob {

    private final PlayerCharacter pc;
    private ClientConnection origin;
    private Group grp;
    private PlayerCharacter pcToRefresh;
    private final boolean wholeGroup;

    public RefreshGroupJob(PlayerCharacter pc, PlayerCharacter pcToRefresh) {
        super();
        this.pc = pc;
        if (pc != null) {
            this.origin = pc.getClientConnection();
            this.grp = GroupManager.getGroup(pc);
        }
        this.pcToRefresh = pcToRefresh;
        this.wholeGroup = false;
    }

    public RefreshGroupJob(PlayerCharacter pc) {
        super();
        this.pc = pc;
        if (pc != null) {
            this.origin = pc.getClientConnection();
            this.grp = GroupManager.getGroup(pc);
        }
        this.wholeGroup = true;
    }

    @Override
    protected void doJob() {

        if (this.pc == null || this.origin == null || grp == null) {
            return;
        }

        if (wholeGroup) {
            
            // refresh everyone in the group including me
            // check that we are in the same group as when we submitted the job
            
            if (GroupManager.getGroup(pc) != null && GroupManager.getGroup(pc) == grp) {
                
            // refresh pc's group list for just the one player that needed refreshing
                
                GroupManager.RefreshMyGroupList(pc, origin);
                GroupManager.RefreshOthersGroupList(pc);
            }

            return;
        } 
        
        // only refresh the single player
        if (this.pcToRefresh == null)
            return;

        // check that we are in the same group as when we submitted the job
        if (GroupManager.getGroup(pc) != null && GroupManager.getGroup(pc) == grp) {
            // refresh pc's group list for just the one player that needed refreshing
            GroupManager.RefreshMyGroupListSinglePlayer(pc, origin, pcToRefresh);
        }

    }

}
