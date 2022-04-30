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
import engine.objects.City;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;


public class SetForceRenameCityCmd extends AbstractDevCmd {

	public SetForceRenameCityCmd() {
        super("forcerename");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		
		Zone zone = ZoneManager.findSmallestZone(pc.getLoc());
		if (zone == null)
			return;
		boolean rename = words[0].equalsIgnoreCase("true") ? true : false;
		if (zone.getPlayerCityUUID() == 0)
			return;
		City city = City.getCity(zone.getPlayerCityUUID());
		if (city == null)
			return;
		city.setForceRename(rename);
	
	}

	@Override
	protected String _getHelpString() {
		return "Temporarily Changes SubRace";
	}

	@Override
	protected String _getUsageString() {
		return "' /subrace mobBaseID";
	}

}
