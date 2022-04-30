// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.SiegeResult;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.objects.*;

/**
 * @author Eighty
 *
 */
public class RemoveBaneCmd extends AbstractDevCmd {

	public RemoveBaneCmd() {
        super("removebane");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

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
			throwbackError(pc, "Could not find bane to remove.");
			return;
		}

		bane.endBane(SiegeResult.DEFEND);

		throwbackInfo(pc, "The bane has been removed.");
	}

	@Override
	protected String _getHelpString() {
        return "Removes a bane from the city grid you're standing on.";
	}

	@Override
	protected String _getUsageString() {
        return "'./removebane'";
	}

}
