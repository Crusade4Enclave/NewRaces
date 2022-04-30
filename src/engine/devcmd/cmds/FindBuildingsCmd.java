// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.AbstractWorldObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;

import java.util.HashSet;

public class FindBuildingsCmd extends AbstractDevCmd {

	public FindBuildingsCmd() {
        super("findBuildings");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		try {
			Vector3fImmutable searchPt = pc.getLoc();
			float range = 50.0f;

			// Arg Count Check
			// Valid arg count is 0,1,2,3
			if (words.length == 0) {
				// Use Player's loc and default range

			} else if (words.length == 1) {
				// Use Player's loc and specified range
				range = Float.valueOf(words[0]);

			} else if (words.length == 2) {
				// Use specified loc and default range
				searchPt = new Vector3fImmutable(Float.valueOf(words[0]),
						searchPt.y,
						Float.valueOf(words[1]));

			} else if (words.length == 3) {
				// Use specified loc and specified range
				searchPt = new Vector3fImmutable(Float.valueOf(words[0]),
						searchPt.y,
						Float.valueOf(words[1]));
				range = Float.valueOf(words[2]);

			} else {
				this.sendUsage(pc);
				return;
			}

			String s = "";

			HashSet<AbstractWorldObject> container = WorldGrid.getObjectsInRangePartial(
					searchPt, range, MBServerStatics.MASK_BUILDING);

			s += "Found " + container.size();
			s += " buildings within " + range;
			s += " units of [" + searchPt.toString() + ']';
			throwbackInfo(pc, s);

			int index = 0;
			for (AbstractWorldObject awo : container) {
				Building b = (Building) awo;

				s = index + ")";
				s += " ObjectID: " + awo.getObjectUUID() + ']';
				s += " -> Name: " + b.getSimpleName();
				if (b.getBlueprint() == null) {
					s += " No Blueprint";
				} else {
					s += " Blueprint UUID: " + b.getBlueprint().getMeshForRank(0);
				}
				s += "[" + ((Building) awo).getBlueprintUUID() + ']';

				throwbackInfo(pc, s);
				++index;
			}

		} catch (NumberFormatException e) {
			this.throwbackError(pc, "Supplied data: '" + words
					+ "' failed to parse to a Float.");
		} catch (Exception e) {
			this.throwbackError(pc,
					"An unknown exception occurred while attempting to findBuildings with data: '"
							+ words + '\'');
		}
	}

	@Override
	protected String _getHelpString() {
		return "Sets your character's Mana to 'amount'";
	}

	@Override
	protected String _getUsageString() {
		return "' /findBuildings [lat long] [range]'";
	}

}
