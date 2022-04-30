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
import engine.objects.AbstractCharacter;

public class ChangeAltitudeJob extends AbstractScheduleJob {

    private final AbstractCharacter ac;
    private final float targetAlt;
    private final float startAlt;

    public ChangeAltitudeJob(AbstractCharacter ac, float startAlt, float targetAlt) {
        super();
        this.ac = ac;
        this.startAlt = startAlt;
        this.targetAlt = targetAlt;
    }

    @Override
    protected void doJob() {
        if (this.ac != null)
            MovementManager.finishChangeAltitude(ac, targetAlt);
    }

    @Override
    protected void _cancelJob() {
    }

    public float getStartAlt() {
        return startAlt;
    }
}
