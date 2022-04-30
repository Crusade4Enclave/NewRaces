// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

public class TeleportModeCmd extends AbstractDevCmd {

	public TeleportModeCmd() {
        super("teleportMode");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		boolean newTeleportMode = false;
		if (words.length == 0) { // toggle
			newTeleportMode = !pc.isTeleportMode();

		} else if (words[0].equalsIgnoreCase("on")) {
			newTeleportMode = true;

		} else if (words[0].equalsIgnoreCase("off")) {
			newTeleportMode = false;

		} else {
			this.sendUsage(pc);
			return;
		}
		pc.setTeleportMode(newTeleportMode);
		this.setTarget(pc); //for logging
		String output = (newTeleportMode ? "on" : "off");

		throwbackInfo(pc, "Teleport mode is now '" + output + "'.");
	}

	@Override
	protected String _getHelpString() {
		return "'on' enables teleport mode, 'off' disables.  No arguments to the /teleportMode command will toggle the setting.";
	}

	@Override
	protected String _getUsageString() {
		return "' /teleportMode [on | off]'";
	}

}
