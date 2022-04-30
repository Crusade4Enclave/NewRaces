// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.ProtectionState;
import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import engine.server.MBServerStatics;

public class AddBuildingCmd extends AbstractDevCmd {

	public AddBuildingCmd() {
        super("addbuilding");
//		super("addbuilding", MBServerStatics.ACCESS_GROUP_DESIGNER_UP, 0, false, true);
        this.addCmdString("building");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		// Arg Count Check
		if (words.length != 2) {
			this.sendUsage(pc);
			return;
		}

		int ID;
		int rank;
                Blueprint blueprint;
                
		try {
			ID = Integer.parseInt(words[0]);
			rank = Integer.parseInt(words[1]);
		} catch (Exception e) {
			throwbackError(pc, "Invalid addBuilding Command. Need Building ID and rank.");
			return; // NaN
		}
		if (ID < 1) {
			throwbackError(pc,
					"Invalid addBuilding Command. Invalid Building ID.");
			return;
		}
		Vector3f rot = new Vector3f(0.0f, 0.0f, 0.0f);
		float w = 1f;
		Zone zone = ZoneManager.findSmallestZone(pc.getLoc());

		if (zone == null) {
			throwbackError(pc, "Failed to find zone to place building in.");
			return;
		}

               blueprint = Blueprint.getBlueprint(ID);
               
               if ((blueprint != null) && (rank > blueprint.getMaxRank())) {
                   throwbackError(pc, rank + " is not a valid rank for this building");
			return;
               }
		
		Building likeBuilding = DbManager.BuildingQueries.GET_BUILDING_BY_MESH(ID);
                
		if (likeBuilding != null) {
			rot = likeBuilding.getRot();
			w = likeBuilding.getw();
		}
                
                String buildingName = "";
                int blueprintUUID = 0;
                
                Vector3fImmutable localLoc = ZoneManager.worldToLocal(pc.getLoc(), zone);
                
                if (localLoc == null)
                	return;
                
                if (blueprint != null) {
                 buildingName = blueprint.getName();
                 blueprintUUID = blueprint.getBlueprintUUID();
                 }
                 
                Building building = DbManager.BuildingQueries.
                        CREATE_BUILDING(
                                   zone.getObjectUUID(), 0, buildingName, ID,
                                   localLoc, 1.0f, 0, ProtectionState.PROTECTED, 0, rank,
                                   null, blueprintUUID, w, rot.y);
         

		if (building == null) {
			throwbackError(pc, "Failed to add building.");
			return;
		}

		building.setRot(rot);
		building.setw(w);

		building.setObjectTypeMask(MBServerStatics.MASK_BUILDING);
	        WorldGrid.addObject(building, pc);
		ChatManager.chatSayInfo(pc,
				"Building with ID " + building.getObjectUUID() + " added");

		this.setResult(String.valueOf(building.getObjectUUID()));

	}

	@Override
	protected String _getHelpString() {
		return "Creates a building of type 'buildingID' at the location your character is standing.";
	}

	@Override
	protected String _getUsageString() {
		return "' /addbuilding buildingID rank' || ' /building buildingID rank'";
	}

}
