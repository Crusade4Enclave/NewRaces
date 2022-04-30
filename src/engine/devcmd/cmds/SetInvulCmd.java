// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;


import engine.Enum;
import engine.Enum.ProtectionState;
import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.City;
import engine.objects.PlayerCharacter;

public class SetInvulCmd extends AbstractDevCmd {

	public SetInvulCmd() {
        super("setinvul");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {
		if (pcSender == null) return;

		if (words.length != 1) {
			this.sendUsage(pcSender);
			return;
		}

		boolean invul;
		switch (words[0].toLowerCase()) {
		case "true":
			invul = true;
			break;
		case "false":
			invul = false;
			break;
		default:
			this.sendUsage(pcSender);
			return;
		}

		if (target == null || !(target instanceof Building)) {
			throwbackError(pcSender, "No building targeted");
			return;
		}

		Building b = (Building) target;

		// if strucutre is a TOL then we're modifying the protection
		// status of the entire city

		if ( (b.getBlueprint() != null) &&
				(b.getBlueprint().getBuildingGroup().equals(Enum.BuildingGroup.TOL))) {

			City city;

			city = b.getCity();
			city.protectionEnforced = !city.protectionEnforced;
			throwbackInfo(pcSender, "City protection contracts enforced: " + city.protectionEnforced);
			return;
		}

		if (invul) {

			b.setProtectionState(ProtectionState.PROTECTED);
			throwbackInfo(pcSender, "The targetted building is now invulnerable.");
			return;
		}
		b.setProtectionState(ProtectionState.NONE);
		throwbackInfo(pcSender, "The targetted building is no longer invulernable.");
	}

	@Override
	protected String _getUsageString() {
		return "'./setInvul true|false'";
	}

	@Override
	protected String _getHelpString() {
		return "Turns invulernability on or off for building";
	}

}
