// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.gameManager.MovementManager;
import engine.job.AbstractScheduleJob;
import engine.net.client.msg.ChangeAltitudeMsg;
import engine.objects.PlayerCharacter;

public class FlightJob extends AbstractScheduleJob {

    private final PlayerCharacter pc;
    private final ChangeAltitudeMsg msg;
    private final int duration;

    public FlightJob(PlayerCharacter pc, ChangeAltitudeMsg msg, int duration) {
        super();
        this.msg = msg;
        this.duration = duration;
        this.pc = pc;
    }

    @Override
    protected void doJob() {
        if (this.pc != null && this.msg != null)
            MovementManager.updateFlight(pc, msg, duration);
    }

    @Override
    protected void _cancelJob() {
    }

}
