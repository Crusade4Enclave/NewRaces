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
import engine.objects.MobBase;
import engine.objects.PlayerCharacter;

/**
 * @author Eighty
 *
 */
public class CopyMobCmd extends AbstractDevCmd {

	public CopyMobCmd() {
        super("copymob");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		if (words.length < 1) {
			this.sendUsage(pc);
			return;
		}

		int loadID = 0;
		String name = "";
		try {
			loadID = Integer.parseInt(words[0]);
			if (words.length > 1) {
				name = words[1];
				for (int i=2; i<words.length;i++)
					name += ' ' + words[i];
			}
		} catch (NumberFormatException e) {
			throwbackError(pc, "Supplied type " + words[0]
					+ " failed to parse to an Integer");
			return;
		} catch (Exception e) {
			throwbackError(pc,
					"Invalid copyMob Command. Need mob ID specified.");
			return; // NaN
		}
		MobBase mob = MobBase.getMobBase(loadID);
		if (mob == null) {
			throwbackError(pc,
					"Invalid copyMob Command. Mob ID specified is not valid.");
			return;
		}
		MobBase mb = null;
		try {
			mb = MobBase.copyMobBase(mob, name);
		} catch (Exception e) {}
		if (mb == null) {
			throwbackError(pc, "copyMob SQL Error. Failed to create new mob.");
			return;
		}
		ChatManager.chatSayInfo(
				pc,
				"MobBase created with ID " + mb.getObjectUUID() + " using name "
						+ mb.getFirstName());
	}

	@Override
	protected String _getHelpString() {
        return "Copies a Mob of type 'mobID' with optional new name";
	}

	@Override
	protected String _getUsageString() {
        return "' /mob mobID [name]'";
	}

}
