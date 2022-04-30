// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.SimulationManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

public class HeartbeatCmd extends AbstractDevCmd {

	public HeartbeatCmd() {
        super("heartbeat");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		this.throwbackInfo(pc, "Current Heartbeat : " + SimulationManager.currentHeartBeatDelta + " ms.");
		this.throwbackInfo(pc, "Max Heartbeat : " + SimulationManager.HeartbeatDelta + " ms.");

	}

	@Override
	protected String _getHelpString() {
		return "Temporarily Changes SubRace";
	}

	@Override
	protected String _getUsageString() {
		return "' /subrace mobBaseID";
	}

}
