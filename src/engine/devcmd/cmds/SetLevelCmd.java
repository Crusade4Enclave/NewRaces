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

public class SetLevelCmd extends AbstractDevCmd {

	public SetLevelCmd() {
		super("setLevel");
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		PlayerCharacter tar;
		if (target != null) {
			if (target instanceof PlayerCharacter)
				tar = (PlayerCharacter) target;
			else
				tar = pc;
		} else
			tar = pc;

		int level = 0;
		try {
			level = Integer.parseInt(words[0]);
		} catch (NumberFormatException e) {
			this.sendUsage(pc);
			return;
		}
		if (level < 1 || level > 75) {
			this.sendHelp(pc);
			return;
		}

		if (level > 10 && pc.getPromotionClass() == null)
			level = 10;

		tar.setLevel((short) level);
		this.setTarget(tar); //for logging
		ChatManager.chatSayInfo(pc, tar.getFirstName() + " level changed to " + level);
		InterestManager.reloadCharacter(tar);
	}

	@Override
	protected String _getHelpString() {
		return "Sets your character's level to 'amount'.  'amount' must be between 1-75";
	}

	@Override
	protected String _getUsageString() {
		return "' /setLevel amount'";
	}

}
