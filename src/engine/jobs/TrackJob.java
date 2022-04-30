// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.Enum;
import engine.gameManager.PowersManager;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.msg.TrackArrowMsg;
import engine.objects.AbstractWorldObject;
import engine.objects.PlayerCharacter;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import engine.powers.poweractions.TrackPowerAction;
import engine.server.MBServerStatics;

import static engine.math.FastMath.sqr;

public class TrackJob extends AbstractEffectJob {

    private final TrackPowerAction tpa;

    public TrackJob(AbstractWorldObject source, AbstractWorldObject target, String stackType, int trains, ActionsBase action, PowersBase power, EffectsBase eb, TrackPowerAction tpa) {
        super(source, target, stackType, trains, action, power, eb);
        this.tpa = tpa;
    }

    @Override
    protected void doJob() {
        
        if (this.tpa == null || this.target == null || this.action == null || this.source == null || this.eb == null || !(this.source instanceof PlayerCharacter))
            return;

         if (this.target.isAlive() == false) {
            sendTrackArrow(Float.intBitsToFloat(0x7E967699));
            PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
            return;
        }
         
            String stackType = action.getStackType();

            float distanceSquared = this.target.getLoc().distanceSquared2D(this.source.getLoc());

            int speed;
            
            if (distanceSquared < sqr(MBServerStatics.TRACK_ARROW_FAST_RANGE))
                speed = MBServerStatics.TRACK_ARROW_SENSITIVITY_FAST;
             else 
                speed = MBServerStatics.TRACK_ARROW_SENSITIVITY;

            this.source.addEffect(stackType, speed, this, this.eb, this.trains);

            Vector3fImmutable dir = this.target.getLoc().subtract2D(this.source.getLoc());
            dir = dir.normalize();

            sendTrackArrow(dir.getRotation());

    }

    @Override
    protected void _cancelJob() {
        sendTrackArrow(Float.intBitsToFloat(0x7E967699));
        PowersManager.cancelEffectTime(this.source, this.target, this.power, this.eb, this.action, this.trains, this);
    }

    private void sendTrackArrow(float rotation) {
        
        if (this.source != null && this.source instanceof PlayerCharacter) {
            PlayerCharacter pc = (PlayerCharacter) this.source;

            if (pc == null)
                return;

            // We send track arrows over primary channel

            TrackArrowMsg tam = new TrackArrowMsg(rotation);
            Dispatch dispatch = Dispatch.borrow(pc, tam);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);

        }
}

}
