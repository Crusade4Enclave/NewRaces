// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.gameManager.ChatManager;
import engine.job.AbstractScheduleJob;
import engine.objects.PlayerCharacter;

public class DebugTimerJob extends AbstractScheduleJob {

    private final PlayerCharacter pc;
    private final String command;
    private final int commandNum;
    private final int duration;

    public DebugTimerJob(PlayerCharacter pc, String command, int commandNum, int duration) {
        super();
        this.pc = pc;
        this.command = command;
        this.commandNum = commandNum;
        this.duration = duration;
    }

    @Override
    protected void doJob() {
        if (this.pc == null) {
            return;
        }

        String text;
        switch (this.commandNum) {
            case 1: //health
                text = "Health: " + pc.getHealth();
                ChatManager.chatSystemInfo(pc, text);
                break;
            case 2: //mana
                text = "Mana: " + pc.getMana();
                ChatManager.chatSystemInfo(pc, text);
                break;
            case 3: //stamina
                text = "Stamina: " + pc.getStamina();
                ChatManager.chatSystemInfo(pc, text);
                break;
            default:
        }

        //re-up the timer for this
        this.pc.renewTimer(command, this, duration);
    }

    @Override
    protected void _cancelJob() {
    }
}
