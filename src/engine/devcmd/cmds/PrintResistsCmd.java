// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.objects.*;

public class PrintResistsCmd extends AbstractDevCmd {

	public PrintResistsCmd() {
		super("printresists");
		//		super("printresists", MBServerStatics.ACCESS_LEVEL_ADMIN);
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		AbstractCharacter tar;

		if (target != null && target instanceof AbstractCharacter) {
			tar = (AbstractCharacter) target;
		} else
			tar = pc;

		//Get name and type
		String type = "PlayerCharacter";
		String name = tar.getFirstName();
		if (tar instanceof Mob) {
			Mob mob = (Mob) tar;
			MobBase mb = mob.getMobBase();
			if (mb != null)
				name = mb.getFirstName();
			type = "Mob";
		} else if (tar instanceof NPC) {
			NPC npc = (NPC) tar;
			Contract contract = npc.getContract();
			if (contract != null) {
				if (contract.isTrainer())
					name = tar.getName() + ", " + contract.getName();
				else
					name = tar.getName() + " the " + contract.getName();
			}
			type = "NPC";
		}

		throwbackInfo(pc, "Server resists for " + type + ' ' + name);
		if (tar.getResists() != null) {
			tar.getResists().printResistsToClient(pc);
		} else
			throwbackInfo(pc, "Resists for " + type + ' ' + name + " not found");
	}

	@Override
	protected String _getHelpString() {
		return "Returns the player's current resists";
	}

	@Override
	protected String _getUsageString() {
		return "' /printresists'";
	}

}
