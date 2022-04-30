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
import engine.objects.AbstractGameObject;
import engine.objects.Mob;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;

public class GetZoneMobsCmd extends AbstractDevCmd {

	public GetZoneMobsCmd() {
        super("getzonemobs");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {
		if (pcSender == null) return;

		int loadID = 0;
		if (words.length == 1) {
			try {
				loadID = Integer.parseInt(words[0]);
			} catch (Exception e) {}
		}

		//find the zone
		Zone zone = null;
		if (loadID != 0) {
			zone = ZoneManager.getZoneByZoneID(loadID);
			if (zone == null)
				zone = ZoneManager.getZoneByUUID(loadID);
		} else
			zone = ZoneManager.findSmallestZone(pcSender.getLoc());

		if (zone == null) {
			if (loadID != 0)
				throwbackError(pcSender, "Error:  can't find the zone of ID " + loadID + '.');
			else
				throwbackError(pcSender, "Error:  can't find the zone you are in.");
			return;
		}

		//get all mobs for the zone

		throwbackInfo(pcSender, zone.getName() + " (" + zone.getLoadNum() + ") " + zone.getObjectUUID());

		for (Mob m : zone.zoneMobSet) {

			if (m != null) {
				String out = m.getName() + '(' + m.getDBID() + "): ";
				if (m.isAlive())
					out += m.getLoc().x + "x" + m.getLoc().z + "; isAlive: " + m.isAlive();
				else
					out += " isAlive: " + m.isAlive();
				throwbackInfo(pcSender, out);
			} else
				throwbackInfo(pcSender, "Unknown (" + m.getDBID() + "): not loaded");
		}
	}

	@Override
	protected String _getUsageString() {
		return "' /getzonemobs [zoneID]'";
	}

	@Override
	protected String _getHelpString() {
		return "lists all mobs for a zone";
	}

}
