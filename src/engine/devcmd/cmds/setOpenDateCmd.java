// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.BuildingGroup;
import engine.Enum.GameObjectType;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.BuildingManager;
import engine.objects.*;

public class setOpenDateCmd extends AbstractDevCmd {

	public setOpenDateCmd() {
		super("minedate");
	}

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {
		
		
		if (words[0].equalsIgnoreCase("list")){
			for (int buildingID : Mine.towerMap.keySet()){
				Building building = BuildingManager.getBuildingFromCache(buildingID);
				if (building == null){
					this.throwbackError(pcSender, "null building for ID " + buildingID);
					continue;
				}
				
				Zone zone = building.getParentZone();
				
				Zone parentZone = zone.getParent();
				
				Mine mine = Mine.towerMap.get(buildingID);
				this.throwbackInfo(pcSender, "Mine UUID : " + mine.getObjectUUID() + " Mine Type: " + zone.getName() + " Zone : " + parentZone.getName()
				+ " Open Date :  " + mine.openDate);
			}
				
		}
	if (target == null){
		this.throwbackError(pcSender, "null target");
		return;
	}
	if (target.getObjectType().equals(GameObjectType.Building) == false){
		this.throwbackError(pcSender, "target must be object type building");
		return;
	}

	Building building = (Building)target;
	if (building.getBlueprint() == null){
		this.throwbackError(pcSender, "null blueprint");
		return;
	}
	
	if (building.getBlueprint().getBuildingGroup().equals(BuildingGroup.MINE) == false){
		
			this.throwbackError(pcSender, "target not mine");
			return;
	}
	
	Mine mine = Mine.getMineFromTower(building.getObjectUUID());
	
	if (mine == null){
		this.throwbackError(pcSender, "null mine");
		return;
	}
	int days = Integer.parseInt(words[0]);
	int hours = Integer.parseInt(words[1]);

		mine.openDate = mine.openDate.plusDays(days).plusHours(hours);
		
		this.throwbackInfo(pcSender, "Mine Open Date Changed to " + mine.openDate.toString());
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
