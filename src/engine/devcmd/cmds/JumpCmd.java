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
import engine.objects.PlayerCharacter;

public class JumpCmd extends AbstractDevCmd {

	public JumpCmd() {
        super("jump");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		if (words.length != 2) {
			this.sendUsage(pc);
			return;
		}

		//test

		if (words[0].equalsIgnoreCase("face")){

			try {
				float range = Float.parseFloat(words[1]);
				Vector3fImmutable newLoc = pc.getFaceDir().scaleAdd(range, pc.getLoc());
				pc.teleport(newLoc);


			} catch (NumberFormatException e) {
				this.throwbackError(pc, ""
						+ " failed to parse to Floats");
				return;

			}
			return;
		}
		float lat = 0.0f, lon = 0.0f;
		String latLong = '\'' + words[0] + ", " + words[1] + '\'';

		try {
			lat = Float.parseFloat(words[0]);
			lon = Float.parseFloat(words[1]);

		} catch (NumberFormatException e) {
			this.throwbackError(pc, "Supplied LatLong: " + latLong
					+ " failed to parse to Floats");
			return;

		} catch (Exception e) {
			this.throwbackError(pc,
					"An unknown exception occurred while attempting to jump to LatLong of "
							+ latLong);
			return;
		}

		Vector3fImmutable loc = pc.getLoc();
		loc = loc.add(lat, 0f, -lon);
		pc.teleport(loc);
	}

	@Override
	protected String _getHelpString() {
		return "Alters your characters position by 'lat' and 'long'. This does not transport you TO 'lat' and 'long', but rather BY 'lat' and 'long' ";

	}

	@Override
	protected String _getUsageString() {
		return "' /jump lat long'";
	}

}
