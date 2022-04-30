// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.jobs.DebugTimerJob;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

public class DebugCmd extends AbstractDevCmd {



	public DebugCmd() {
		super("debug");
		//		super("debug", MBServerStatics.ACCESS_GROUP_ALL_TEAM, 0, false, false);
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
						  AbstractGameObject target) {
		if (words.length < 2) {
			this.sendUsage(pc);
			return;
		}

		if (pc == null)
			return;

		//pc.setDebug must use bit sizes: 1, 2, 4, 8, 16, 32

		String command = words[0].toLowerCase();
		boolean toggle = (words[1].toLowerCase().equals("on")) ? true : false;

		switch (command) {
			case "magictrek":
				pc.RUN_MAGICTREK = toggle;

				break;
			case "combat":
				pc.setDebug(64, toggle);

				break;
			case "health":
				toggleDebugTimer(pc, "Debug_Health", 1, 1000, toggle);

				break;
			case "mana":
				toggleDebugTimer(pc, "Debug_Mana", 2, 1000, toggle);

				break;
			case "stamina":
				toggleDebugTimer(pc, "Debug_Stamina", 3, 500, toggle);

				break;
			case "spells":
				pc.setDebug(16, toggle);

				break;
			case "damageabsorber":
				pc.setDebug(2, toggle);

				break;
			case "recast":
			case "recycle":
				pc.setDebug(4, toggle);

				break;
			case "seeinvis":
				pc.setDebug(8, toggle);

				break;
			case "movement":
				pc.setDebug(1, toggle);

				break;
			case "noaggro":
				pc.setDebug(32, toggle);

				break;
			//		case "loot":
			//			MBServerStatics.debugLoot = toggle;
			//			break;

			default:
				String output = "Debug for " + command + " not found.";
				throwbackError(pc, output);
				return;
		}


		setTarget(pc); //for logging

		String output = "Debug for " + command + " turned " + ((toggle) ? "on." : "off.");
		throwbackInfo(pc, output);
	}

	@Override
	protected String _getHelpString() {
		return "Runs debug commands";

	}

	@Override
	protected String _getUsageString() {
		return "'./Debug command on/off'";
	}

	private static void toggleDebugTimer(PlayerCharacter pc, String name, int num, int duration, boolean toggle) {
		if (toggle) {
			DebugTimerJob dtj = new DebugTimerJob(pc, name, num, duration);
			pc.renewTimer(name, dtj, duration);
		} else
			pc.cancelTimer(name);
	}
}
