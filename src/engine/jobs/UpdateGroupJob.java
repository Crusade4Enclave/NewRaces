// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.jobs;

import engine.gameManager.GroupManager;
import engine.job.AbstractScheduleJob;
import engine.job.JobScheduler;
import engine.objects.Group;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

public class UpdateGroupJob extends AbstractScheduleJob {

    private final Group group;

    public UpdateGroupJob(Group group) {
        super();
        this.group = group;
    }

    @Override
    protected void doJob() {
        
        if (this.group == null)
            return;
        
        PlayerCharacter lead = group.getGroupLead();
        
        if (lead == null)
            return;

        try {
            GroupManager.RefreshWholeGroupList(lead, lead.getClientConnection(), this.group);
        } catch (Exception e) {
            Logger.error( e);
        }

        JobScheduler.getInstance().scheduleJob(this, MBServerStatics.UPDATE_GROUP_RATE);
    }

    @Override
    protected void _cancelJob() {
    }
}
