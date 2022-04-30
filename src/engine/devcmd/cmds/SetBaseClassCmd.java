// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.InterestManagement.InterestManager;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

public class SetBaseClassCmd extends AbstractDevCmd {

	public SetBaseClassCmd() {
        super("setBaseClass");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		int classID = 0;
		try {
			classID = Integer.parseInt(words[0]);
		} catch (Exception e) {
			throwbackError(pc,
					"Invalid setBaseClass Command. must specify an ID between 2500 and 2503.");
			return;
		}
		if (classID < 2500 || classID > 2503) {
			throwbackError(pc,
					"Invalid setBaseClass Command. must specify an ID between 2500 and 2503.");
			return;
		}
		pc.setBaseClass(classID);
		this.setTarget(pc); //for logging
		ChatManager.chatSayInfo(pc,
				"BaseClass changed to " + classID);
		InterestManager.reloadCharacter(pc);

	}

	@Override
	protected String _getHelpString() {
		return "Sets your character's BaseClass to 'id'";
	}

	@Override
	protected String _getUsageString() {
		return "' /setBaseClass id'";
	}

}
