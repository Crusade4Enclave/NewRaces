// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.job.AbstractScheduleJob;
import engine.job.JobContainer;
import engine.objects.PlayerCharacter;

import java.util.concurrent.ConcurrentHashMap;

public class SummonSendJob extends AbstractScheduleJob {

    PlayerCharacter source;
    PlayerCharacter target;

    public SummonSendJob(PlayerCharacter source, PlayerCharacter target) {
        super();
        this.source = source;
        this.target = target;
    }

    @Override
    protected void doJob() {
        
        if (this.source == null) 
            return;

        //clear summon send timer
        ConcurrentHashMap<String, JobContainer> timers = this.source.getTimers();
        
        if (timers != null && timers.containsKey("SummonSend")) 
            timers.remove("SummonSend");

    }

    @Override
    protected void _cancelJob() {
    }

    public PlayerCharacter getSource() {
        return this.source;
    }

    public PlayerCharacter getTarget() {
        return this.target;
    }
}
