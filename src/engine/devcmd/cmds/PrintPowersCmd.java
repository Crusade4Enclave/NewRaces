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
import engine.objects.CharacterPower;
import engine.objects.PlayerCharacter;
import engine.powers.PowersBase;

import java.util.concurrent.ConcurrentHashMap;

public class PrintPowersCmd extends AbstractDevCmd {

	public PrintPowersCmd() {
		super("printpowers");
		//		super("printpowers", MBServerStatics.ACCESS_LEVEL_ADMIN);
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		PlayerCharacter tar;

		if (target != null && target instanceof PlayerCharacter)
			tar = (PlayerCharacter) target;
		else
			tar = pc;

		throwbackInfo(pc, "Server powers for " + tar.getFirstName());

		ConcurrentHashMap<Integer, CharacterPower> powers = tar.getPowers();
		if (powers != null) {
			throwbackInfo(pc,
					"Power(token): Trains; TrainsGranted; MaxTrains");
			for (CharacterPower power : powers.values()) {
				PowersBase pb = power.getPower();
				if (pb != null) {
					throwbackInfo(pc, "  " + pb.getName() + '('
							+ pb.getToken() + "): "
							+ power.getTrains() + "; "
							+ power.getGrantedTrains() + "; "
							+ pb.getMaxTrains());
				}
			}
		} else
			throwbackInfo(pc, "Powers not found for player");
	}

	@Override
	protected String _getHelpString() {
        return "Returns the player's current granted powers";
	}

	@Override
	protected String _getUsageString() {
        return "' /printpowers'";
	}

}
