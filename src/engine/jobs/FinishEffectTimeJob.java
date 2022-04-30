// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.gameManager.PowersManager;
import engine.objects.AbstractWorldObject;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;

public class FinishEffectTimeJob extends AbstractEffectJob {

    public FinishEffectTimeJob(AbstractWorldObject source, AbstractWorldObject target, String stackType, int trains, ActionsBase action, PowersBase power, EffectsBase eb) {
        super(source, target, stackType, trains, action, power, eb);
    }

    @Override
    protected void doJob() {
        PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
    }

    @Override
    protected void _cancelJob() {
        PowersManager.cancelEffectTime(this.source, this.target, this.power, this.eb, this.action, this.trains, this);
    }
}
