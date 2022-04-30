// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;

public class SetBuildingAltitudeCmd extends AbstractDevCmd {

	public SetBuildingAltitudeCmd() {
        super("setbuildingaltitude");
        this.addCmdString("buildingaltitude");
    }

    private static boolean UpdateBuildingAltitude(Building building, float altitude) {

        if (!DbManager.BuildingQueries.UPDATE_BUILDING_ALTITUDE(building.getObjectUUID(), altitude))
            return false;

        building.statAlt = altitude;

        if (building.parentZone != null) {
            if (building.parentBuildingID != 0) {
                Building parentBuilding = BuildingManager.getBuilding(building.parentBuildingID);
                if (parentBuilding != null) {
                    building.setLoc(new Vector3fImmutable(building.statLat + building.parentZone.absX + parentBuilding.statLat, building.statAlt + building.parentZone.absY + parentBuilding.statAlt, building.statLon + building.parentZone.absZ + parentBuilding.statLon));
                } else {
                    building.setLoc(new Vector3fImmutable(building.statLat + building.parentZone.absX, building.statAlt + building.parentZone.absY, building.statLon + building.parentZone.absZ));

                }
            } else
                building.setLoc(new Vector3fImmutable(building.statLat + building.parentZone.absX, building.statAlt + building.parentZone.absY, building.statLon + building.parentZone.absZ));

        }

        return true;
    }

    @Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		if (target.getObjectType() != GameObjectType.Building){
			this.sendUsage(pc);
			return;
		}

		Building targetBuilding = (Building)target;


		float altitude = 0;
		try {
			altitude  = Float.parseFloat(words[0]);

			if (!UpdateBuildingAltitude(targetBuilding, targetBuilding.getStatAlt() + altitude)){
				this.throwbackError(pc, "Failed to update building altitude");
				return;
			}


			WorldGrid.updateObject(targetBuilding);

			this.setTarget(pc); //for logging

			// Update all surrounding clients.

		} catch (NumberFormatException e) {
			this.throwbackError(pc, "Supplied data: " + words[0]
					+ " failed to parse to an Integer.");
		} catch (Exception e) {
			this.throwbackError(pc,
					"An unknown exception occurred while attempting to setSlot to "
							+ words[0]);
		}
	}

	@Override
	protected String _getHelpString() {
		return "Sets slot position for an NPC to 'slot'";
	}

	@Override
	protected String _getUsageString() {
		return "' /changeslot slot'";
	}

}
