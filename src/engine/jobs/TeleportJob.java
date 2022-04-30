// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.job.AbstractScheduleJob;
import engine.math.Vector3fImmutable;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ErrorPopupMsg;
import engine.objects.NPC;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;

public class TeleportJob extends AbstractScheduleJob {

    private final ClientConnection origin;
    private final NPC npc;
    private final PlayerCharacter pc;
    private final Vector3fImmutable loc;
    private int oldLiveCounter;
    private final boolean setSafeMode;

    public TeleportJob(PlayerCharacter pc, NPC npc, Vector3fImmutable loc, ClientConnection origin, boolean setSafeMode) {
        super();
        this.pc = pc;
        this.npc = npc;
        this.loc = loc;
        this.origin = origin;
        this.setSafeMode = setSafeMode;
        if (pc != null) {
            this.oldLiveCounter = pc.getLiveCounter();
        }
    }

    @Override
    protected void doJob() {
        
        if (this.pc == null || this.npc == null || this.origin == null)
            return;

        if (!pc.isAlive() || this.oldLiveCounter != pc.getLiveCounter())
            return;
        
        if (pc.getLoc().distanceSquared2D(npc.getLoc()) > MBServerStatics.NPC_TALK_RANGE * MBServerStatics.NPC_TALK_RANGE) {
            ErrorPopupMsg.sendErrorPopup(pc, 114);
            return;
        }

        pc.teleport(loc);
        
        if (this.setSafeMode)
            pc.setSafeMode();
        
    }

    @Override
    protected void _cancelJob() {
    }
}
