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
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;

public class BoundsCmd extends AbstractDevCmd {

	public BoundsCmd() {
        super("bounds");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {
		if (target == null || !target.getObjectType().equals(GameObjectType.Building)){
			this.throwbackError(pcSender, "No Building Selected");
			return;
		}
		
		Building building = (Building)target;
		
		if (building.getBounds() == null){
			this.throwbackInfo(pcSender, "No valid Bounds for building UUID " + building.getObjectUUID());
			return;
		}
		float topLeftX = building.getLoc().x - building.getBounds().getHalfExtents().x;
		float topLeftY = building.getLoc().z - building.getBounds().getHalfExtents().y;
		
		float topRightX = building.getLoc().x + building.getBounds().getHalfExtents().x;
		float topRightY = building.getLoc().z - building.getBounds().getHalfExtents().y;
		
		float bottomLeftX = building.getLoc().x - building.getBounds().getHalfExtents().x;
		float bottomLeftY = building.getLoc().z + building.getBounds().getHalfExtents().y;
		
		float bottomRightX = building.getLoc().x + building.getBounds().getHalfExtents().x;
		float bottomRightY = building.getLoc().z + building.getBounds().getHalfExtents().y;
		
		String newLine = "\r\n ";
		
		String output = "Bounds Information for Building UUID " + building.getObjectUUID();
		output += newLine;
		
		output+= "Top Left : " + topLeftX + " , " + topLeftY + newLine;
		output+= "Top Right : " + topRightX + " , " + topRightY + newLine;
		output+= "Bottom Left : " + bottomLeftX + " , " + bottomLeftY + newLine;
		output+= "Bottom Right : " + bottomRightX + " , " + bottomRightY + newLine;
		
		this.throwbackInfo(pcSender, output);
		

	}


	@Override
	protected String _getUsageString() {
		return "' /bounds'";
	}

	@Override
	protected String _getHelpString() {
		return "Audits all the mobs in a zone.";

	}

}
