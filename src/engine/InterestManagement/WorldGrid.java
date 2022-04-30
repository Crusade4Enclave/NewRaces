// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.InterestManagement;

import engine.Enum.GridObjectType;
import engine.math.FastMath;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.LoadCharacterMsg;
import engine.net.client.msg.LoadStructureMsg;
import engine.net.client.msg.UnloadObjectsMsg;
import engine.objects.*;
import engine.server.MBServerStatics;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;


public class WorldGrid {
	
	public static ConcurrentHashMap<Integer,AbstractWorldObject>[][] DynamicGridMap;
	public static ConcurrentHashMap<Integer,AbstractWorldObject>[][] StaticGridMap;
	private static float dynamicBucketScale = 0.00390625f; // 256 bucket size, 1/256
	private static float staticBucketScale = 0.00390625f;
	public static void startLoadJob() {

		Thread loadJobThread;
		
		
		loadJobThread = new Thread(InterestManager.INTERESTMANAGER);
		loadJobThread.setName("InterestManager");
		loadJobThread.start();
	}

	public static boolean moveWorldObject(AbstractWorldObject awo, Vector3fImmutable location) {
		awo.setLoc(location);
		return true;
	}

	public static HashSet<AbstractWorldObject> getInRange(Vector3f loc, double r) {
		HashSet<AbstractWorldObject> outbound = new HashSet<>();
		return outbound;
	}

	public static HashSet<AbstractWorldObject> getObjectsInRangePartial(Vector3fImmutable loc, double r, int mask) {
		HashSet<AbstractWorldObject> outbound = new HashSet<>();
		float scale;
		
		if ((mask & MBServerStatics.MASK_STATIC) != 0)
			scale = WorldGrid.staticBucketScale;
		else
			scale = WorldGrid.dynamicBucketScale;
		int gridX = (int) Math.abs(loc.x * scale);
		int gridZ = (int)Math.abs(loc.z * scale);
			int bucketSize = (int) (r *scale) + 1;
			//start at top left most corner to scan.
			int startingX = gridX - bucketSize;
			int startingZ = gridZ + bucketSize;
			
			
			
			int limitX = Math.abs((int) (MBServerStatics.MAX_WORLD_WIDTH *scale));
			int limitZ = Math.abs((int) (MBServerStatics.MAX_WORLD_HEIGHT *scale)); //LimitZ is negative, remember to flip sign.
			
			if (startingX < 0)
				startingX = 0;
			
			if (startingZ < 0)
				startingZ = 0;
			
			if (startingX > limitX)
				startingX = limitX;
			
			if (startingZ > limitZ)
				startingZ = limitZ;
			
			int endX = startingX + (bucketSize * 2);
			int endZ = startingZ - (bucketSize * 2);
			
			if (endX < 0)
				endX = 0;
			
			if (endZ < 0)
				endZ = 0;
			
			if (endX > limitX)
				endX = limitX;
			
			if (endZ > limitZ)
				endZ = limitZ;
			
			int auditMob = 0;
			for (int x = startingX;x<=endX;x++){
 				for (int z = startingZ;z >= endZ;z--){
 					
 					ConcurrentHashMap<Integer,AbstractWorldObject> gridMap;
 					
 					if ((MBServerStatics.MASK_STATIC & mask) != 0)
 						gridMap = WorldGrid.StaticGridMap[x][z];
 					else
 						gridMap = WorldGrid.DynamicGridMap[x][z];
					for (AbstractWorldObject gridObject: gridMap.values()){
						if ((gridObject.getObjectTypeMask() & mask) == 0)
							continue;
						if (gridObject.getLoc().distanceSquared2D(loc) <= FastMath.sqr(r))
							outbound.add(gridObject);
					}
				}
			}
		return outbound;
	}

	public static HashSet<AbstractWorldObject> getObjectsInRangePartialNecroPets(Vector3fImmutable loc, double r) {
		HashSet<AbstractWorldObject> outbound = new HashSet<>();
		return outbound;
	}

	public static HashSet<AbstractWorldObject> getObjectsInRangeContains(Vector3fImmutable loc, double r, int mask) {
		HashSet<AbstractWorldObject> outbound = getObjectsInRangePartial(loc,r,mask);
		return outbound;
	}

	public static HashSet<AbstractWorldObject> getObjectsInRangePartial(AbstractWorldObject awo, double range, int mask) {
		return getObjectsInRangePartial(awo.getLoc(), range, mask);
	}

	
	public static void InitializeGridObjects(){
		
		int dynamicWidth = (int) Math.abs(MBServerStatics.MAX_WORLD_WIDTH *WorldGrid.dynamicBucketScale);
		int dynamicHeight = (int) Math.abs(MBServerStatics.MAX_WORLD_HEIGHT*WorldGrid.dynamicBucketScale);
		
		int staticWidth = (int) Math.abs(MBServerStatics.MAX_WORLD_WIDTH *WorldGrid.staticBucketScale);
		int staticHeight = (int) Math.abs(MBServerStatics.MAX_WORLD_HEIGHT*WorldGrid.staticBucketScale);
		WorldGrid.DynamicGridMap = new ConcurrentHashMap[dynamicWidth+ 1][dynamicHeight + 1];
		WorldGrid.StaticGridMap = new ConcurrentHashMap[staticWidth + 1][staticHeight + 1];
		//create new hash maps for each bucket
		for (int x = 0; x<= staticWidth; x++)
			for (int y = 0; y<= staticHeight; y++){
				WorldGrid.StaticGridMap[x][y] = new ConcurrentHashMap<Integer,AbstractWorldObject>();
			}
		
		for (int x = 0; x<= dynamicWidth; x++)
			for (int y = 0; y<= dynamicHeight; y++){
				WorldGrid.DynamicGridMap[x][y] = new ConcurrentHashMap<Integer,AbstractWorldObject>();
			}
				
	}
	
	public static void RemoveWorldObject(AbstractWorldObject gridObject){
		
		if (gridObject == null)
			return;
		AbstractWorldObject.RemoveFromWorldGrid(gridObject);
	}
	
	public static boolean addObject(AbstractWorldObject gridObject, float x, float z){
		
		if (gridObject == null)
			return false;
		
		if (x > MBServerStatics.MAX_WORLD_WIDTH)
			return false;
		
		if (z < MBServerStatics.MAX_WORLD_HEIGHT)
			return false;
		
		if (x < 0)
			return false;
		if (z > 0)
			return false;
		
		int gridX;
		int gridZ;
		
		if (gridObject.getGridObjectType().equals(GridObjectType.STATIC)){
			 gridX = Math.abs((int) (x *WorldGrid.staticBucketScale));
			 gridZ = Math.abs((int) (z*WorldGrid.staticBucketScale));
		}else{
			 gridX = Math.abs((int) (x *WorldGrid.dynamicBucketScale));
			 gridZ = Math.abs((int) (z*WorldGrid.dynamicBucketScale));
		}
		
		
		WorldGrid.RemoveWorldObject(gridObject);
		
		return AbstractWorldObject.AddToWorldGrid(gridObject, gridX, gridZ);
		
		
	}

    public static void unloadObject(AbstractWorldObject awo) {

        UnloadObjectsMsg uom = new UnloadObjectsMsg();
        uom.addObject(awo);
        DispatchMessage.sendToAllInRange(awo, uom);
    }

	public static void loadObject(AbstractWorldObject awo) {

		LoadStructureMsg lsm;
		LoadCharacterMsg lcm;

		switch (awo.getObjectType()) {
		case Building:
			lsm = new LoadStructureMsg();
			lsm.addObject((Building)awo);
			DispatchMessage.sendToAllInRange(awo, lsm);
			break;
		case NPC:
			lcm = new LoadCharacterMsg((NPC) awo, false);
			DispatchMessage.sendToAllInRange(awo, lcm);
			break;
		case Mob:
			lcm = new LoadCharacterMsg((Mob) awo, false);
			DispatchMessage.sendToAllInRange(awo, lcm);
			break;
		default:
			// *** Refactor: Log error?
			break;
		}
	}

	public static void loadObject(AbstractWorldObject awo, ClientConnection origin) {

		LoadStructureMsg lsm;
		LoadCharacterMsg lcm;

		switch (awo.getObjectType()) {

		case Building:
			lsm = new LoadStructureMsg();
			lsm.addObject((Building)awo);
			DispatchMessage.sendToAllInRange(awo, lsm);
			break;
		case NPC:
			lcm = new LoadCharacterMsg((NPC) awo, false);
			DispatchMessage.sendToAllInRange(awo, lcm);
			break;
		case Mob:
			lcm = new LoadCharacterMsg((Mob) awo, false);
			DispatchMessage.sendToAllInRange(awo, lcm);
			break;
		case PlayerCharacter:
			lcm = new LoadCharacterMsg((PlayerCharacter) awo, false);
			DispatchMessage.sendToAllInRange(awo, lcm);
			break;
		default:
			// *** Refactor: Log error?
			break;
		}
	}

	public static void unloadObject(AbstractWorldObject awo,
									ClientConnection origin) {
		UnloadObjectsMsg uom = new UnloadObjectsMsg();
		uom.addObject(awo);
		DispatchMessage.sendToAllInRange(awo, uom);
	}

	public static void addObject(AbstractWorldObject awo, PlayerCharacter pc) {
		if (pc == null || awo == null)
			return;
		ClientConnection origin = pc.getClientConnection();
		if (origin == null)
			return;
		loadObject(awo, origin);
	}

	public static void removeObject(AbstractWorldObject awo, PlayerCharacter pc) {
		if (pc == null || awo == null)
			return;
		ClientConnection origin = pc.getClientConnection();
		if (origin == null)
			return;
		unloadObject(awo, origin);
	}

	public static void updateObject(AbstractWorldObject awo, PlayerCharacter pc) {
		if (pc == null || awo == null)
			return;
		ClientConnection origin = pc.getClientConnection();
		if (origin == null)
			return;
		unloadObject(awo, origin);
		loadObject(awo, origin);
	}

	public static void updateObject(AbstractWorldObject awo) {
		if (awo == null)
			return;
		unloadObject(awo);
		loadObject(awo);
	}

	/*
	 *
	 */
	public static void removeObject(AbstractWorldObject awo) {
		if (awo == null)
			return;
		unloadObject(awo);
	}
}
