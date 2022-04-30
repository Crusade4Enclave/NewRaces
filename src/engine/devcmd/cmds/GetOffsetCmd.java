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
import engine.objects.PlayerCharacter;
import engine.objects.Zone;

public class GetOffsetCmd extends AbstractDevCmd {

	public GetOffsetCmd() {
        super("getoffset");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {
		if (pcSender == null) return;

		Zone zone = null;
		try {
			int loadID = Integer.parseInt(words[0]);
			zone = ZoneManager.getZoneByZoneID(loadID);
			if (zone == null) {
				throwbackError(pcSender, "Error:  can't find the zone of ID " + loadID + '.');
				return;
			}
		} catch (Exception e) {
			zone = ZoneManager.findSmallestZone(pcSender.getLoc());
		}

		if (zone == null) {
			throwbackError(pcSender, "Error:  can't find the zone you're in.");
			return;
		}

		float difX = pcSender.getLoc().x - zone.absX;
		float difY = pcSender.getLoc().y - zone.absY;
		float difZ = pcSender.getLoc().z - zone.absZ;

		throwbackInfo(pcSender, zone.getName() + ": (x: " + difX + ", y: " + difY + ", z: " + difZ + ')');
	}

	@Override
	protected String _getUsageString() {
		return "'./getoffset [zoneID]'";
	}

	@Override
	protected String _getHelpString() {
		return "lists offset from center of zone";
	}

}
