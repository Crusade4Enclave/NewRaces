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
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.powers.PowersBase;

public class UseItemJob extends AbstractScheduleJob {

    private final AbstractCharacter ac;
    private final AbstractWorldObject target;
    private final PowersBase pb;
    private final int trains;
    private final int liveCounter;

    public UseItemJob(AbstractCharacter ac, AbstractWorldObject target, PowersBase pb, int trains, int liveCounter) {
        super();
        this.ac = ac;
        this.target = target;
        this.pb = pb;
        this.trains = trains;
        this.liveCounter = liveCounter;
    }

    @Override
    protected void doJob() {
        PowersManager.finishApplyPower(ac, target, Vector3fImmutable.ZERO, pb, trains, liveCounter);
    }

    @Override
    protected void _cancelJob() {
    	this.ac.setItemCasting(false);
    }

    public PowersBase getPowersBase() {
        return this.pb;
    }

    public int getTrains() {
        return this.trains;
    }

    public AbstractWorldObject getTarget() {
        return this.target;
    }
}
