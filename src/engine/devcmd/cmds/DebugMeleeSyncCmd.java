// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

public class DebugMeleeSyncCmd extends AbstractDevCmd {

	public DebugMeleeSyncCmd() {
        super("debugmeleesync");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		String s = words[0].toLowerCase();

		if (s.equals("on")) {
			pc.setDebug(64, true);
			ChatManager.chatSayInfo(pc, "Melee Sync Debug ON");
		} else if (s.equals("off")) {
			pc.setDebug(64, false);
			ChatManager.chatSayInfo(pc, "Melee Sync Debug OFF");
		} else {
			this.sendUsage(pc);
		}
	}

	@Override
	protected String _getHelpString() {
		return  "Turns on/off melee sync debugging.";

	}

	@Override
	protected String _getUsageString() {
		return "'./debugmeleesync on|off'";

	}

}
