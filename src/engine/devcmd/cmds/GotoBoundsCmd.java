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
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;

public class GotoBoundsCmd extends AbstractDevCmd {

	public GotoBoundsCmd() {
        super("gotobounds");
    }

	@Override
	protected void _doCmd(PlayerCharacter player, String[] words,
			AbstractGameObject target) {

		String corner = words[0];
		Vector3fImmutable targetLoc =  Vector3fImmutable.ZERO;
		
		if (target == null || !target.getObjectType().equals(GameObjectType.Building)){
			this.throwbackError(player, "No Building Selected");
			return;
		}
		
		Building building = (Building)target;
		
		if (building.getBounds() == null){
			this.throwbackInfo(player, "No valid Bounds for building UUID " + building.getObjectUUID());
			return;
		}
		 float x = building.getBounds().getHalfExtents().x;
		 float z = building.getBounds().getHalfExtents().y;
		 
		 if (building.getBlueprint() != null){
			 x = building.getBlueprint().getExtents().x;
			 z = building.getBlueprint().getExtents().y;
		 }
		 
		float topLeftX = building.getLoc().x - x;
		float topLeftY = building.getLoc().z -z;
		
		float topRightX = building.getLoc().x + x;
		float topRightY = building.getLoc().z - z;
		
		float bottomLeftX = building.getLoc().x - x;
		float bottomLeftY = building.getLoc().z + z;
		
		float bottomRightX = building.getLoc().x +x;
		float bottomRightY = building.getLoc().z + z;
		
		
		switch (corner){
		case "topleft":
			targetLoc = new Vector3fImmutable(topLeftX, 0, topLeftY);
			break;
		case "topright":
			targetLoc = new Vector3fImmutable(topRightX, 0, topRightY);
			break;
		case "bottomleft":
			targetLoc = new Vector3fImmutable(bottomLeftX, 0, bottomLeftY);
			break;
		case "bottomright":
			targetLoc = new Vector3fImmutable(bottomRightX, 0, bottomRightY);
			break;
			default:
				this.throwbackInfo(player, "wrong corner name. use topleft , topright , bottomleft , bottomright");
				return;
				
		}
		
		targetLoc = Vector3fImmutable.transform(building.getLoc(),targetLoc , building.getBounds().getRotationDegrees());
		
		// Teleport player

		if (targetLoc == Vector3fImmutable.ZERO) {
			this.throwbackError(player, "Failed to locate UUID");
			return;
		}

		player.teleport(targetLoc);

	}

	@Override
	protected String _getHelpString() {
        return "Teleports player to a UUID";
	}

	@Override
	protected String _getUsageString() {
		return "' /gotoobj <UID>'";

	}

}
