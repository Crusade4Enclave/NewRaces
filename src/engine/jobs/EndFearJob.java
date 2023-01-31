// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.objects.AbstractWorldObject;
import engine.objects.Mob;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;

public class EndFearJob extends AbstractEffectJob {

    public EndFearJob(AbstractWorldObject source, AbstractWorldObject target, String stackType, int trains, ActionsBase action, PowersBase power, EffectsBase eb) {
        super(source, target, stackType, trains, action, power, eb);
    }

    @Override
    protected void doJob() {
        
        //cancel fear for mob.
        
        if (this.target == null || (!(this.target instanceof Mob)))
            return;
        
        ((Mob) this.target).fearedObject = null;
    }

    @Override
    protected void _cancelJob() {
        
        //cancel fear for mob.
        
        if (this.target == null || (!(this.target instanceof Mob))) 
            return;
        
        ((Mob) this.target).fearedObject = null;
    }
}
