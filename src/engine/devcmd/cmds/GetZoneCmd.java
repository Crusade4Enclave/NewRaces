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

import java.util.ArrayList;

public class GetZoneCmd extends AbstractDevCmd {

	public GetZoneCmd() {
        super("getzone");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {
		if (pcSender == null) return;

		if (words.length != 1) {
			this.sendUsage(pcSender);
			return;
		}

		ArrayList<Zone> allIn = new ArrayList<>();
		switch (words[0].toLowerCase()) {
		case "all":
			throwbackInfo(pcSender, "All zones currently in");
			allIn = ZoneManager.getAllZonesIn(pcSender.getLoc());
			break;
		case "smallest":
			throwbackInfo(pcSender, "Smallest zone currently in");
			Zone zone = ZoneManager.findSmallestZone(pcSender.getLoc());
			allIn.add(zone);
			break;
		default:
			this.sendUsage(pcSender);
			return;
		}

		for (Zone zone : allIn)
			throwbackInfo(pcSender, zone.getName() + "; UUID: " + zone.getObjectUUID() + ", loadNum: " + zone.getLoadNum());
	}

	@Override
	protected String _getUsageString() {
		return "' /getzone smallest/all'";
	}

	@Override
	protected String _getHelpString() {
		return "lists what zones a player is in";
	}

}
