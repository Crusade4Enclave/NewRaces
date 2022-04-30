// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.BuildingGroup;
import engine.Enum.DbObjectType;
import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;

/**
 *
 */
public class RemoveObjectCmd extends AbstractDevCmd {

	public RemoveObjectCmd() {
		//set to Player access level so it can be run by non-admins on production.
		//Actual access level is set in _doCmd.
		super("remove");
		this.addCmdString("delete");
	}

	@Override
	protected void _doCmd(PlayerCharacter player, String[] words, AbstractGameObject target) {

		int targetID;
		DbObjectType targetObjType;
		Building targetBuilding;
		NPC targetNPC;
		Mob targetMob;

		if (target == null && words.length != 1) {
			this.sendUsage(player);
			return;
		}

		// Delete the targeted building

		if (target != null) {

			switch (target.getObjectType()) {

			case Building:
				removeBuilding(player, (Building) target);
				break;
			case NPC:
				removeNPC(player, (NPC) target);
				break;
			case Mob:
				removeMob(player, (Mob) target);
				break;
			default:
				throwbackError(player, "Target " + target.getObjectType()
				+ " is not a valid object type");
				break;
			}
			return;
		}

		// Attempt to delete object based upon parsed UUID from input

		// Parse Target UUID

		try {
			targetID = Integer.parseInt(words[0]);
		} catch (NumberFormatException e) {
			throwbackError(player, "Supplied object ID " + words[0]
					+ " failed to parse to an Integer");
			return;
		}

		// Determine object type of given UUID

		targetObjType = DbManager.BuildingQueries.GET_UID_ENUM(targetID);

		// Process accordingly

		switch (targetObjType) {
		case BUILDING:
			targetBuilding = BuildingManager.getBuilding(targetID);
			removeBuilding(player, targetBuilding);
			break;
		case NPC:
			targetNPC = NPC.getNPC(targetID);
			removeNPC(player, targetNPC);
			break;
		case MOB:
			targetMob = Mob.getMob(targetID);
			removeMob(player, targetMob);
			break;
		default:
			throwbackError(player, "Invalid UUID: Not found in database");
			break;
		}

	}

	@Override
	protected String _getHelpString() {
        return "Removes targeted or specified object";
	}

	@Override
	protected String _getUsageString() {
        return "' /remove [objectID]' || ' /delete [objectID]'";
	}

	private void removeBuilding(PlayerCharacter pc, Building building) {

		Zone currentZone = ZoneManager.findSmallestZone(pc.getLoc());

		if (currentZone == null) {
			this.throwbackError(pc, "Could not locate zone for player.");
			return;
		}

		if ((building.getBlueprint() != null) && (building.getBlueprint().getBuildingGroup() == BuildingGroup.SPIRE))
			building.disableSpire(false);

		if ((building.getBlueprint() != null) && (building.getBlueprint().getBuildingGroup() == BuildingGroup.WAREHOUSE)){
			City city =City.getCity(building.getParentZone().getPlayerCityUUID());
			if (city != null){
				city.setWarehouseBuildingID(0);
			}
			Warehouse.warehouseByBuildingUUID.remove(building.getObjectUUID());
		}


		//remove cached shrines.
		if ((building.getBlueprintUUID() != 0)
				&& (building.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE))
			Shrine.RemoveShrineFromCacheByBuilding(building);

		for (AbstractCharacter ac : building.getHirelings().keySet()) {
			NPC npc = null;
			Mob mobA = null;

			if (ac.getObjectType() == GameObjectType.NPC)
				npc = (NPC)ac;
			else if (ac.getObjectType() == GameObjectType.Mob)
				mobA = (Mob)ac;

			if (npc != null){
				for (Mob mob : npc.getSiegeMinionMap().keySet()) {
					WorldGrid.RemoveWorldObject(mob);
					WorldGrid.removeObject(mob, pc);
					//Mob.getRespawnMap().remove(mob);
					if (mob.getParentZone() != null)
						mob.getParentZone().zoneMobSet.remove(mob);
				}
				DbManager.NPCQueries.DELETE_NPC(npc);
				DbManager.removeFromCache(npc);
				WorldGrid.RemoveWorldObject(npc);
				WorldGrid.removeObject(npc, pc);
			}else if (mobA != null){
				for (Mob mob : mobA.getSiegeMinionMap().keySet()) {
					WorldGrid.RemoveWorldObject(mob);
					WorldGrid.removeObject(mob, pc);
					//Mob.getRespawnMap().remove(mob);
					if (mob.getParentZone() != null)
						mob.getParentZone().zoneMobSet.remove(mob);
				}
				DbManager.MobQueries.DELETE_MOB(mobA);
				DbManager.removeFromCache(mobA);
				WorldGrid.RemoveWorldObject(mobA);
				WorldGrid.removeObject(mobA, pc);
			}


		}
		Zone zone = building.getParentZone();
		DbManager.BuildingQueries.DELETE_FROM_DATABASE(building);
		DbManager.removeFromCache(building);
		zone.zoneBuildingSet.remove(building);
		WorldGrid.RemoveWorldObject(building);
		WorldGrid.removeObject(building, pc);

		ChatManager.chatSayInfo(pc,
				"Building with ID " + building.getObjectUUID() + " removed");
		this.setResult(String.valueOf(building.getObjectUUID()));
	}

	private void removeNPC(PlayerCharacter pc, NPC npc) {

		Zone currentZone = ZoneManager.findSmallestZone(pc.getLoc());
		if (currentZone == null) {
			this.throwbackError(pc, "Could not locate zone for player.");
			return;
		}



		for (Mob mob : npc.getSiegeMinionMap().keySet()) {
			WorldGrid.RemoveWorldObject(mob);
			WorldGrid.removeObject(mob, pc);
			if (mob.getParentZone() != null)
				mob.getParentZone().zoneMobSet.remove(mob);
		}

		DbManager.NPCQueries.DELETE_NPC(npc);
		DbManager.removeFromCache(npc);
		WorldGrid.RemoveWorldObject(npc);
		WorldGrid.removeObject(npc, pc);
		ChatManager.chatSayInfo(pc,
				"NPC with ID " + npc.getDBID() + " removed");
		this.setResult(String.valueOf(npc.getDBID()));
	}

	private void removeMob(PlayerCharacter pc, Mob mob) {

		Zone currentZone = ZoneManager.findSmallestZone(pc.getLoc());
		if (currentZone == null) {
			this.throwbackError(pc, "Could not locate zone for player.");
			return;
		}

		if (mob.getParentZone() != null && mob.getParentZone() != currentZone && !mob.isPet() && !mob.isNecroPet()) {
			this.throwbackError(pc, "Error 376954: Could not Remove Mob.Mob is not in the same zone as player.");
			return;
		}

		mob.setLoc(Vector3fImmutable.ZERO);	//Move it off the plane..
		mob.setBindLoc(Vector3fImmutable.ZERO);	//Reset the bind loc..
		//mob.setHealth(-1, pc); //Kill it!

		DbManager.MobQueries.DELETE_MOB(mob);

		DbManager.removeFromCache(mob);
		WorldGrid.RemoveWorldObject(mob);
		WorldGrid.removeObject(mob, pc);

		if (mob.getParentZone() != null)
			mob.getParentZone().zoneMobSet.remove(mob);

		ChatManager.chatSayInfo(pc,
				"Mob with ID " + mob.getDBID() + " removed");
		this.setResult(String.valueOf(mob.getDBID()));
	}

}
