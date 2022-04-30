// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.gameManager.PowersManager;
import engine.job.AbstractScheduleJob;
import engine.net.client.msg.PerformActionMsg;
import engine.objects.PlayerCharacter;
import engine.powers.PowersBase;

public class UsePowerJob extends AbstractScheduleJob {

    private final PlayerCharacter pc;
    private final PerformActionMsg msg;
    private final int token;
    private final PowersBase pb;
    private final int casterLiveCounter;
    private final int targetLiveCounter;

    public UsePowerJob(PlayerCharacter pc, PerformActionMsg msg, int token, PowersBase pb, int casterLiveCounter, int targetLiveCounter) {
        super();
        this.pc = pc;
        this.msg = msg;
        this.token = token;
        this.pb = pb;
        this.casterLiveCounter = casterLiveCounter;
        this.targetLiveCounter = targetLiveCounter;
    }

    @Override
    protected void doJob() {
        PowersManager.finishUsePower(this.msg, this.pc, casterLiveCounter, targetLiveCounter);
    }

    @Override
    protected void _cancelJob() {
        //cast stopped early, reset recycle timer
        PowersManager.finishRecycleTime(this.msg, this.pc, true);
    }

    public PowersBase getPowersBase() {
        return this.pb;
    }

    public int getToken() {
        return this.token;
    }

    public PerformActionMsg getMsg() {
        return this.msg;
    }
}
