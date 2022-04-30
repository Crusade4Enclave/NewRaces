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
import engine.objects.CharacterRune;
import engine.objects.PlayerCharacter;

/**
 *
 * @author Eighty
 *
 */
public class SetAdminRuneCmd extends AbstractDevCmd {

	public SetAdminRuneCmd() {
        super("setadminrune");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		int runeID = 0;
		boolean add = true;
		try {
			runeID = Integer.parseInt(args[0]);
			if (args.length > 1)
				add = (args[1].toLowerCase().equals("false")) ? false : true;
		} catch (NumberFormatException e) {
			this.sendUsage(pcSender);
			return;
		}
		if (runeID < 2901 || runeID > 2911) {
			throwbackError(pcSender,
					"Invalid setrune Command. must specify an ID between 2901 and 2911.");
			return;
		}

		if(!(target instanceof PlayerCharacter)) {
			target = pcSender;
		}

		boolean worked = false;
		if (add) {
			worked = CharacterRune.grantRune((PlayerCharacter) target, runeID);
			if (worked)
				ChatManager.chatSayInfo(pcSender,
						"rune of ID " + runeID + " added");
			else
				throwbackError(pcSender, "Failed to add the rune of type "
						+ runeID);
		} else {
			worked = CharacterRune.removeRune((PlayerCharacter) target, runeID);
			if (worked) {
				ChatManager.chatSayInfo(pcSender,
						"rune of ID " + runeID + " removed");
				InterestManager.reloadCharacter(pcSender);
			} else
				throwbackError(pcSender, "Failed to remove the rune of type "
						+ runeID);
		}
		this.setTarget(target); //for logging

	}

	@Override
	protected String _getUsageString() {
        return "' /setrune runeID [true/false]'";
	}

	@Override
	protected String _getHelpString() {
        return "Grant or remove the rune with the specified runeID";
	}

}
