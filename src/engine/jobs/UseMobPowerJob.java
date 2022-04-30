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
import engine.objects.Mob;
import engine.powers.PowersBase;

public class UseMobPowerJob extends AbstractScheduleJob {

    private final Mob caster;
    private final PerformActionMsg msg;
    private final int token;
    private final PowersBase pb;
    private final int casterLiveCounter;
    private final int targetLiveCounter;

    public UseMobPowerJob(Mob caster, PerformActionMsg msg, int token, PowersBase pb, int casterLiveCounter, int targetLiveCounter) {
        super();
        this.caster = caster;
        this.msg = msg;
        this.token = token;
        this.pb = pb;
        this.casterLiveCounter = casterLiveCounter;
        this.targetLiveCounter = targetLiveCounter;
    }

    @Override
    protected void doJob() {
        PowersManager.finishUseMobPower(this.msg, this.caster, casterLiveCounter, targetLiveCounter);
    }

    @Override
    protected void _cancelJob() {
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
