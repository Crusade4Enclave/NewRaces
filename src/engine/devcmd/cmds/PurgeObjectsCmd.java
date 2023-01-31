package engine.devcmd.cmds;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author
 * Summary: Game designer utility command to purge all
        objects of a given type within a supplied range
 */

public class PurgeObjectsCmd extends AbstractDevCmd {

	// Instance variables

	private Vector3fImmutable _currentLocation;
	private float _targetRange;
	private int _targetMask;

	// Concurrency support

	private ReadWriteLock lock = new ReentrantReadWriteLock();

	// Constructor

	public PurgeObjectsCmd() {
        super("purge");
    }

    private static void PurgeWalls(Zone zone, PlayerCharacter pc){

        if (!zone.isPlayerCity())
            return;

        for (Building building: zone.zoneBuildingSet){
            if (!BuildingManager.IsWallPiece(building))
                continue;
            for (AbstractCharacter ac: building.getHirelings().keySet()){
                NPC npc = null;
                Mob mobA = null;

                if (ac.getObjectType() == GameObjectType.NPC)
                    npc = (NPC)ac;
                else if (ac.getObjectType() == GameObjectType.Mob)
                    mobA = (Mob)ac;



                if (npc != null){
                    for (Mob mob: npc.getSiegeMinionMap().keySet()){
                        WorldGrid.RemoveWorldObject(mob);
                        WorldGrid.removeObject(mob, pc);
                        //Mob.getRespawnMap().remove(mob);
                        if (mob.parentZone != null)
                            mob.parentZone.zoneMobSet.remove(mob);
                    }
                    DbManager.NPCQueries.DELETE_NPC(npc);
                    DbManager.removeFromCache(GameObjectType.NPC,
                            npc.getObjectUUID());
                    WorldGrid.RemoveWorldObject(npc);
                }else if (mobA != null){
                    for (Mob mob: mobA.siegeMinionMap.keySet()){
                        WorldGrid.RemoveWorldObject(mob);
                        WorldGrid.removeObject(mob, pc);
                        //Mob.getRespawnMap().remove(mob);
                        if (mob.parentZone != null)
                            mob.parentZone.zoneMobSet.remove(mob);
                    }
                    DbManager.MobQueries.DELETE_MOB(mobA);
                    DbManager.removeFromCache(GameObjectType.Mob,
                            mobA.getObjectUUID());
                    WorldGrid.RemoveWorldObject(mobA);
                }

            }


            DbManager.BuildingQueries.DELETE_FROM_DATABASE(building);
            DbManager.removeFromCache(building);
            WorldGrid.RemoveWorldObject(building);
            WorldGrid.removeObject(building, pc);
        }

    }


    // AbstractDevCmd Overridden methods

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] args,
			AbstractGameObject target) {

		// Grab write lock due to use of instance variables

		lock.writeLock().lock();

		try {
			
			if (args[0].toLowerCase().equals("walls")){
				Zone zone = ZoneManager.findSmallestZone(pc.getLoc());
				
				PurgeWalls(zone, pc);
				return;
			}

			if(validateUserInput(args) == false) {
				this.sendUsage(pc);
				return;
			}

			parseUserInput(args);

			// Arguments have been validated and parsed at this point
			// Build array of requested objects

			_currentLocation = pc.getLoc();

			HashSet<AbstractWorldObject> objectList =
					WorldGrid.getObjectsInRangePartial(_currentLocation, _targetRange, _targetMask);

			// Iterate through array and remove objects from game world and database

			for (AbstractWorldObject awo : objectList) {

				switch(awo.getObjectType()) {
				case Building:
					removeBuilding(pc, (Building) awo);
					break;
				case NPC:
					removeNPC(pc, (NPC) awo);
					break;
				case Mob:
					removeMob(pc, (Mob) awo);
					break;
				default:
					break;
				}
			}

			// Send results to user
			throwbackInfo(pc, "Purge: " + objectList.size() + " objects were removed in range " + _targetRange);
		}catch(Exception e){
			Logger.error(e);
		}

		// Release Reentrant lock

		finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	protected String _getHelpString() {
        return "Purges game objects within range";
	}

	@Override
	protected String _getUsageString() {
        return "/purge [npc|mob|mesh|all] [range <= 200]";
	}

	// Class methods

	private static boolean validateUserInput(String[] userInput) {

		int stringIndex;
		String commandSet = "npcmobmeshall";

		// incorrect number of arguments test

		if (userInput.length != 2)
			return false;

		// Test of game object type argument

		stringIndex = commandSet.indexOf(userInput[0].toLowerCase());

		if (stringIndex == -1)
			return false;

		// Test if range argument can convert to a float

		try {
			Float.parseFloat(userInput[1]); }
		catch (NumberFormatException | NullPointerException e) {
			return false;
		}

		// User input passes validation

		return true;
	}

	private void parseUserInput(String[] userInput) {

		_targetMask = 0;
		_targetRange = 0f;

		// Build mask from user input

		switch (userInput[0].toLowerCase()) {
		case "npc":
			_targetMask = MBServerStatics.MASK_NPC;
			break;
		case "mob":
			_targetMask = MBServerStatics.MASK_MOB;
			break;
		case "mesh":
			_targetMask = MBServerStatics.MASK_BUILDING;
			break;
		case "all":
			_targetMask = MBServerStatics.MASK_NPC | MBServerStatics.MASK_MOB | MBServerStatics.MASK_BUILDING;
			break;
		default:
			break;
		}

		// Parse second argument into range parameter. Cap at 200 units.

		_targetRange = Float.parseFloat(userInput[1]);
		_targetRange = Math.min(_targetRange, 200f);
	}

	private static void removeBuilding(PlayerCharacter pc, Building building) {

		if ((building.getBlueprintUUID() != 0) &&
				(building.getBlueprint().getBuildingGroup() == BuildingGroup.TOL))
			return;
		if ((building.getBlueprintUUID() != 0) &&
				(building.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE))
			Shrine.RemoveShrineFromCacheByBuilding(building);

		if ((building.getBlueprint() != null) && (building.getBlueprint().getBuildingGroup() == BuildingGroup.SPIRE))
			building.disableSpire(false);

		for (AbstractCharacter ac: building.getHirelings().keySet()){
			NPC npc = null;
			Mob mobA = null;

			if (ac.getObjectType() == GameObjectType.NPC)
				npc = (NPC)ac;
			else if (ac.getObjectType() == GameObjectType.Mob)
				mobA = (Mob)ac;



			if (npc != null){
				for (Mob mob: npc.getSiegeMinionMap().keySet()){
					WorldGrid.RemoveWorldObject(mob);
					WorldGrid.removeObject(mob, pc);
					//Mob.getRespawnMap().remove(mob);
					if (mob.parentZone != null)
						mob.parentZone.zoneMobSet.remove(mob);
				}
				DbManager.NPCQueries.DELETE_NPC(npc);
				DbManager.removeFromCache(Enum.GameObjectType.NPC,
						npc.getObjectUUID());
				WorldGrid.RemoveWorldObject(npc);
			}else if (mobA != null){
				for (Mob mob: mobA.siegeMinionMap.keySet()){
					WorldGrid.RemoveWorldObject(mob);
					WorldGrid.removeObject(mob, pc);
					//Mob.getRespawnMap().remove(mob);
					if (mob.parentZone != null)
						mob.parentZone.zoneMobSet.remove(mob);
				}
				DbManager.MobQueries.DELETE_MOB(mobA);
				DbManager.removeFromCache(Enum.GameObjectType.Mob,
						mobA.getObjectUUID());
				WorldGrid.RemoveWorldObject(mobA);
			}

		}


		DbManager.BuildingQueries.DELETE_FROM_DATABASE(building);
		DbManager.removeFromCache(building);
		WorldGrid.RemoveWorldObject(building);
		WorldGrid.removeObject(building, pc);
	}

	private static void removeNPC(PlayerCharacter pc, NPC npc) {
		DbManager.NPCQueries.DELETE_NPC(npc);
		DbManager.removeFromCache(npc);
		WorldGrid.RemoveWorldObject(npc);
		WorldGrid.removeObject(npc, pc);
	}

	private static void removeMob(PlayerCharacter pc, Mob mob) {
		mob.setLoc(Vector3fImmutable.ZERO);	//Move it off the plane..
		mob.setBindLoc(Vector3fImmutable.ZERO);	//Reset the bind loc..
		//mob.setHealth(-1, pc); //Kill it!

		DbManager.MobQueries.DELETE_MOB(mob);
		DbManager.removeFromCache(mob);
		WorldGrid.RemoveWorldObject(mob);
		WorldGrid.removeObject(mob, pc);
	}

}
