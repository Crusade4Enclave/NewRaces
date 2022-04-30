// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.objects.*;

/**
 * @author Eighty
 *
 */
public class SetBaneActiveCmd extends AbstractDevCmd {

	public SetBaneActiveCmd() {
        super("setbaneactive");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		boolean setActive = false;
		if (words[0].equals("true"))
			setActive = true;

		Zone zone = ZoneManager.findSmallestZone(pc.getLoc());

		if (zone == null) {
			throwbackError(pc, "Unable to find the zone you're in.");
			return;
		}

		if (!zone.isPlayerCity()) {
			throwbackError(pc, "This is not a player city.");
			return;
		}

		City city = City.getCity(zone.getPlayerCityUUID());
		if (city == null) {
			throwbackError(pc, "Unable to find the city associated with this zone.");
			return;
		}

		Bane bane = city.getBane();
		if (bane == null) {
			throwbackError(pc, "Could not find bane to modify.");
			return;
		}

        bane.getCity().protectionEnforced = !setActive;

		if (setActive)
			throwbackInfo(pc, "The bane has been set active.");
		else
			throwbackInfo(pc, "The bane has been set inactive.");
	}

	@Override
	protected String _getHelpString() {
        return "Sets a bane active or deactivates a bane.";
	}

	@Override
	protected String _getUsageString() {
        return "'./setbaneactive true|false'";
	}

}
