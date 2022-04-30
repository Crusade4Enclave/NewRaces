// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.AbstractWorldObject;
import engine.objects.PlayerCharacter;

public class DistanceCmd extends AbstractDevCmd {

	public DistanceCmd() {
        super("distance");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		
		
		
		
		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}
		if (pc == null) {
			return;
		}

		if (target == null || !(target instanceof AbstractWorldObject)) {
			throwbackError(pc, "No target found.");
			return;
		}
		
		AbstractWorldObject awoTarget = (AbstractWorldObject)target;

		Vector3fImmutable pcLoc = pc.getLoc();
		Vector3fImmutable tarLoc = awoTarget.getLoc();
		String out = "Distance: " + pcLoc.distance(tarLoc) +
				"\r\nYour Loc: " + pcLoc.x + 'x' + pcLoc.y + 'x' + pcLoc.z +
				"\r\nTarget Loc: " + tarLoc.x + 'x' + tarLoc.y + 'x' + tarLoc.z;
		throwbackInfo(pc, out);
	}

	@Override
	protected String _getHelpString() {
		return "Gets distance from a target.";

	}

	@Override
	protected String _getUsageString() {
		return  "' /distance'";

	}

}
