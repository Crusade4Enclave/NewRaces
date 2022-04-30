// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.objects.Regions;

/**
 * @author Eighty
 *
 */
public class PrintLocationCmd extends AbstractDevCmd {

	public PrintLocationCmd() {
		super("printloc");
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		PlayerCharacter tar;

		if (target != null && target instanceof PlayerCharacter)
			tar = (PlayerCharacter) target;
		else
			tar = pc;

		throwbackInfo(pc, "Server location for " + tar.getFirstName());
		if (tar.getLoc() != null) {
			throwbackInfo(pc, "Lat: " + tar.getLoc().getX());
			throwbackInfo(pc, "Lon: " + -tar.getLoc().getZ());
			throwbackInfo(pc, "Alt: " + tar.getLoc().getY());
			if (pc.getRegion() != null) {
				this.throwbackInfo(pc, "Player Region Slope Position : " + Regions.SlopeLerpPercent(pc, pc.getRegion()));
				this.throwbackInfo(pc, "Region Slope Magnitude : " + Regions.GetMagnitudeOfRegionSlope(pc.getRegion()));
				this.throwbackInfo(pc, "Player Region Slope Magnitude : " + Regions.GetMagnitudeOfPlayerOnRegionSlope(pc.getRegion(), pc));
			}else{
				this.throwbackInfo(pc, "No Region Found for player.");
			}
			
		} else {
			throwbackInfo(pc, "Server location for " + tar.getFirstName()
			+ " not found");
		}
	}

	@Override
	protected String _getHelpString() {
        return "Returns the player's current location according to the server";
	}

	@Override
	protected String _getUsageString() {
        return "' /printloc'";
	}

}
