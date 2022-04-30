// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net;

import engine.job.AbstractJob;
import engine.job.JobScheduler;
import engine.server.MBServerStatics;

public class ConnectionMonitorJob extends AbstractJob {

    private final AbstractConnectionManager connMan;
    private byte cnt;

    public ConnectionMonitorJob(AbstractConnectionManager connMan, byte cnt) {
        super();
        this.connMan = connMan;
        this.cnt = cnt;
        this.cnt++;
    }

    @Override
    protected void doJob() {

        if (this.cnt >= 5) {
            this.connMan.auditSocketChannelToConnectionMap();
            this.cnt = 0;
        } else
            this.connMan.auditSocketChannelToConnectionMap();

        // Self Sustain
        ConnectionMonitorJob cmj = new ConnectionMonitorJob(this.connMan, cnt);
        JobScheduler.getInstance().scheduleJob(cmj, MBServerStatics.TIMEOUT_CHECKS_TIMER_MS);

        this.setCompletionStatus(JobCompletionStatus.SUCCESS);
    }

}
