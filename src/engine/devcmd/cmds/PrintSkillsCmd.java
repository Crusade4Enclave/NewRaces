// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.GameObjectType;
import engine.devcmd.AbstractDevCmd;
import engine.objects.*;

import java.util.concurrent.ConcurrentHashMap;

public class PrintSkillsCmd extends AbstractDevCmd {

	public PrintSkillsCmd() {
		super("printskills");
		//		super("printskills", MBServerStatics.ACCESS_LEVEL_ADMIN);
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		AbstractCharacter tar = null;

		if (target != null && target instanceof PlayerCharacter)
			tar = (PlayerCharacter) target;
		else if (target.getObjectType() == GameObjectType.Mob)
			tar = (Mob) target;
		else
			tar = pc;

		throwbackInfo(pc, "Server skills for " + tar.getFirstName());
		ConcurrentHashMap<String, CharacterSkill> skills = tar.getSkills();
		if (skills != null) {
			throwbackInfo(pc,
					"Skills Name: Trains; Base(Trains); ModBase(Trains)");
			for (CharacterSkill skill : skills.values()) {
				throwbackInfo(pc, "  " + skill.getName() + ": "
						+ skill.getNumTrains() + "; "
						+ skill.getBaseAmountBeforeMods() + " ("
						+ skill.getModifiedAmountBeforeMods() + "); "
						+ skill.getBaseAmount() + " ("
						+ skill.getModifiedAmount() + '('
						+ skill.getTotalSkillPercet() + " )");
			}
		} else
			throwbackInfo(pc, "Skills not found for player");
	}

	@Override
	protected String _getHelpString() {
		return "Returns the player's current skills";
	}

	@Override
	protected String _getUsageString() {
		return "' /printskills'";
	}

}
