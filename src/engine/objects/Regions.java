// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.InterestManagement.WorldGrid;
import engine.gameManager.BuildingManager;
import engine.math.Bounds;
import engine.math.FastMath;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.server.MBServerStatics;

import java.util.ArrayList;
import java.util.HashMap;



public class Regions  {

	public int room;
	public boolean outside;
	public int level;
	public final boolean stairs;
	public final boolean exit;
	public static HashMap<Integer,Regions> FurnitureRegionMap = new HashMap<>();
	public  Vector3fImmutable lowLerp;
	public  Vector3fImmutable highLerp;
	public Vector3f center;
	public float regionDistanceSquared;
	
	public ArrayList<Vector3f> regionPoints;
	
	public int parentBuildingID;


	public Regions(ArrayList<Vector3f> regionPoints, int level, int room, boolean outside, boolean exit,boolean stairs, Vector3f center, int parentBuildingID) {
		super();
        
        this.level = level;
        this.room = room;
		
		this.outside = (outside);
		this.regionPoints = regionPoints;
		this.exit = exit;
		this.stairs = stairs;
		this.center = center;
		this.parentBuildingID = parentBuildingID;
		//order regionpoints clockwise starting from top left, and ending bottom left.
		
		ArrayList<Vector3f> top = new ArrayList<>();
		ArrayList<Vector3f> bottom = new ArrayList<>();
		
		for (Vector3f point : this.regionPoints){
			if (point.y > center.y)
				top.add(point);
			else if (point.y < center.y)
				bottom.add(point);
		}
		
		
		if (top.size() == 2 && bottom.size() == 2){
			Vector3f topLeft = Vector3f.min(top.get(0), top.get(1));
			Vector3f topRight = Vector3f.max(top.get(0), top.get(1));
			
			Vector3f topCenter =  topLeft.lerp(topRight, .5f);
			
			Vector3f bottomLeft = Vector3f.min(bottom.get(0), bottom.get(1));
			Vector3f bottomRight = Vector3f.max(bottom.get(0), bottom.get(1));
			
			Vector3f bottomCenter =  bottomLeft.lerp(bottomRight, .5f);
			
			this.lowLerp = new Vector3fImmutable(bottomCenter);
			this.highLerp = new Vector3fImmutable(topCenter);
			 
		} else if (top.size() == 2 && bottom.size() == 1){
			Vector3f topLeft = Vector3f.min(top.get(0), top.get(1));
			Vector3f topRight = Vector3f.max(top.get(0), top.get(1));
			
			Vector3f topCenter =  topLeft.lerp(topRight, .5f);
			
			Vector3f topCopy = topRight.subtract2D(topLeft);
			topCopy.normalize();
			
			float topMagnitude = topRight.subtract2D(topLeft).length();
			
			topCopy.multLocal(topMagnitude);
			
			Vector3f bottomLeft = null;
			Vector3f bottomRight = null;
			if (bottom.get(0).distance2D(topLeft) <= bottom.get(0).distance2D(topRight))
				bottomLeft = bottom.get(0);
			else if (bottom.get(0).distance2D(topRight) <= bottom.get(0).distance2D(topLeft))
				bottomRight = bottom.get(0);
			//find bottom right point
			
			if (bottomLeft != null){
				bottomRight = bottomLeft.add(topCopy);
			}else if (bottomRight != null){
				bottomLeft = bottomRight.subtract(topCopy);
			}
			
			
			
			Vector3f bottomCenter =  bottomLeft.lerp(bottomRight, .5f);
			
			this.lowLerp = new Vector3fImmutable(bottomCenter);
			this.highLerp = new Vector3fImmutable(topCenter);
			 
		}else if (bottom.size() == 2 && top.size() == 1){
			Vector3f topLeft = Vector3f.min(bottom.get(0), bottom.get(1));
			Vector3f topRight = Vector3f.max(bottom.get(0), bottom.get(1));
			
			Vector3f topCenter =  topLeft.lerp(topRight, .5f);
			
			Vector3f topCopy = topRight.subtract2D(topLeft);
			topCopy.normalize();
			
			float topMagnitude = topRight.subtract2D(topLeft).length();
			
			topCopy.multLocal(topMagnitude);
			
			Vector3f bottomLeft = null;
			Vector3f bottomRight = null;
			if (top.get(0).distance2D(topLeft) < top.get(0).distance2D(topRight))
				bottomLeft = bottom.get(0);
			else if (top.get(0).distance2D(topRight) < top.get(0).distance2D(topLeft))
				bottomRight = bottom.get(0);
			//find bottom right point
			
			if (bottomLeft != null){
				bottomRight = bottomLeft.add(topCopy);
			}else if (bottomRight != null){
				bottomLeft = bottomRight.subtract(topCopy);
			}
			
			
			
			Vector3f bottomCenter =  bottomLeft.lerp(bottomRight, .5f);
			
			this.lowLerp = new Vector3fImmutable(bottomCenter);
			this.highLerp = new Vector3fImmutable(topCenter);
		}
		
		if (this.lowLerp == null)
			this.lowLerp = new Vector3fImmutable(this.regionPoints.get(0));
		
		if (this.highLerp == null){
			this.highLerp = new Vector3fImmutable(this.regionPoints.get(2));
		}
						
		this.regionDistanceSquared = this.lowLerp.distanceSquared2D(this.highLerp);
	}

	public int getRoom() {
		return room;
	}

	


	public boolean isOutside() {
		return outside;
	}


	public int getLevel() {
		return level;
	}
	
	public boolean collides(Vector3fImmutable collisionPoint){
		
		//test if inside triangle // Regions either have 3 or 4 points
		if (this.regionPoints.size() == 3){
			float regionArea = FastMath.area(regionPoints.get(0).x, regionPoints.get(0).z, regionPoints.get(1).x, regionPoints.get(1).z, regionPoints.get(2).x, regionPoints.get(2).z);
		float collisionArea1 = FastMath.area(collisionPoint.x, collisionPoint.z, regionPoints.get(0).x, regionPoints.get(0).z,regionPoints.get(1).x, regionPoints.get(1).z);
		float collisionArea2 = FastMath.area(collisionPoint.x, collisionPoint.z, regionPoints.get(1).x, regionPoints.get(1).z,regionPoints.get(2).x, regionPoints.get(2).z);
		float collisionArea3 = FastMath.area(collisionPoint.x, collisionPoint.z, regionPoints.get(0).x, regionPoints.get(0).z,regionPoints.get(2).x, regionPoints.get(2).z);
		
		if ((collisionArea1 + collisionArea2 + collisionArea3) == regionArea)
			return true;
		
		}else{
			
			  int i;
		      int j;
		      for (i = 0, j = this.regionPoints.size() - 1; i < this.regionPoints.size(); j = i++) {
		        if ((regionPoints.get(i).z > collisionPoint.z) != (regionPoints.get(j).z > collisionPoint.z) &&
		            (collisionPoint.x < (regionPoints.get(j).x - regionPoints.get(i).x) * (collisionPoint.z - regionPoints.get(i).z) / (regionPoints.get(j).z-regionPoints.get(i).z) + regionPoints.get(i).x)) {
		         return true;
		         }
		      }
		     
		}
		
		return false;
	}
	
	public boolean isPointInPolygon( Vector3fImmutable point)
	{
		   boolean inside = false;
		   for (int i = 0, j = regionPoints.size()-1; i < regionPoints.size(); j = i++)
		   {
		      if (((regionPoints.get(i).z > point.z) != (regionPoints.get(j).z > point.z)) &&
		         (point.x < (regionPoints.get(j).x - regionPoints.get(i).x) * (point.z - regionPoints.get(i).z) / (regionPoints.get(j).z - regionPoints.get(i).z) + regionPoints.get(i).x))
		         inside = !inside;
		   }
		   return inside;
		}
	
	public static boolean CanEnterRegion(AbstractWorldObject worldObject, Regions toEnter){

        if (worldObject.getRegion() == null)
			if (toEnter.level == 0 || toEnter.room == -1 || toEnter.exit)
				return true;
			else
				return false;
		
		if (worldObject.getRegion().equals(toEnter))
			return true;

        if (worldObject.getRegion().level == toEnter.level)
			return true;
		
		//next region is stairs, if they are on the same level as stairs or 1 up, world object can enter.
        if (toEnter.stairs)
			if (worldObject.getRegion().level == toEnter.level || toEnter.level - 1 == worldObject.getRegion().level)
				return true;
		if (worldObject.getRegion().stairs){
			
			boolean movingUp = false;
			
			boolean movingDown = false;
			float yLerp = worldObject.getRegion().lerpY(worldObject);
			
			if (yLerp == (worldObject.getRegion().highLerp.y))
				movingUp = true;
			else if (yLerp == (worldObject.getRegion().lowLerp.y))
					movingDown = true;
			//Stairs are always considered on the bottom floor.


            if (movingUp){
                if(toEnter.level == worldObject.getRegion().level + 1)
				return true;
			}else if (movingDown)
			 if (toEnter.level == worldObject.getRegion().level)
				return true;
			
		}
		
		return false;
	}
	
	public float lerpY (AbstractWorldObject lerper){
		
		Vector3fImmutable lengthVector = this.highLerp.subtract2D(this.lowLerp);
		Vector3fImmutable characterVector = lerper.getLoc().subtract2D(this.lowLerp);
		float lengthVectorMagnitude = lengthVector.magnitude();
		float characterVectorMagnitude = characterVector.magnitude();
		float percentDistance = characterVectorMagnitude/lengthVectorMagnitude;
		float interpolatedY = this.lowLerp.interpolate(this.highLerp, percentDistance).y;
		
		if (interpolatedY > this.highLerp.y)
			interpolatedY = this.highLerp.y;
		else if (interpolatedY < this.lowLerp.y)
			interpolatedY = this.lowLerp.y;
		return interpolatedY;
	}


	public boolean isStairs() {
		return stairs;
	}


	public boolean isExit() {
		return exit;
	}

public static float GetMagnitudeOfRegionSlope(Regions region){
	Vector3fImmutable lengthVector = region.highLerp.subtract2D(region.lowLerp);
	return lengthVector.magnitude();
	}

public static float GetMagnitudeOfPlayerOnRegionSlope(Regions region, PlayerCharacter player){
	Vector3fImmutable characterVector = player.getLoc().subtract2D(region.lowLerp);
	return characterVector.magnitude();
	}

public static float SlopeLerpPercent(PlayerCharacter player, Regions region){
	
	float lengthVectorMagnitude = Regions.GetMagnitudeOfRegionSlope(region);
	float characterVectorMagnitude = Regions.GetMagnitudeOfPlayerOnRegionSlope(region, player);
	float percentDistance = characterVectorMagnitude/lengthVectorMagnitude * 2;
	return percentDistance;
}

public static boolean CanEnterFromOutsideBuilding(Building building,Regions region){
    if (!region.outside)
		return false;
	if (region.lowLerp.y - building.getLoc().y > 1 )
		return false;
	
	return true;
}

public static boolean CanEnterNextLevel(Regions fromRegion,Regions toRegion, AbstractWorldObject worldObject){
	
	if (fromRegion == null)
		return false;
	
	if (toRegion == null)
		return false;
	
	// regions are the same, no need to go any further.
	if (fromRegion.equals(toRegion))
		return true;
	
	//cant move up a level without stairs.
    if (!fromRegion.stairs)
		return false;
	
	boolean movingUp = false;
	
	Vector3fImmutable closestPoint = Vector3fImmutable.ClosestPointOnLine(fromRegion.lowLerp, fromRegion.highLerp, worldObject.getLoc());
	
	//Closest point of a region higher than current region will always return highlerp.
	if (closestPoint.equals(fromRegion.highLerp))
		movingUp = true;
	//Stairs are always considered on the bottom floor.
	
	if (movingUp){
	if(toRegion.level != fromRegion.level + 1)
		return false;
	}else if (toRegion.level != fromRegion.level)
		return false;
	return true;
}

public static boolean IsGroundLevel(Regions region, Building building){
	
	if (region.lowLerp.y - building.getLoc().y > 1)
		return false;
	return true;
}

public static Building GetBuildingForRegion(Regions region){
	return BuildingManager.getBuildingFromCache(region.parentBuildingID);
}
public static Regions GetRegionForTeleport(Vector3fImmutable location){
	Regions region = null;

	
	//Find building
	for (AbstractWorldObject awo:WorldGrid.getObjectsInRangePartial(location, MBServerStatics.STRUCTURE_LOAD_RANGE, MBServerStatics.MASK_BUILDING)){
		Building building = (Building)awo;
		if (!Bounds.collide(location, building.getBounds()))
			continue;

		//find regions that intersect x and z, check if object can enter.
		for (Regions toEnter: building.getBounds().getRegions()){
			if (toEnter.isPointInPolygon(location)){

				if (region == null)
					region = toEnter;
				else // we're using a low level to high level tree structure, database not always in order low to high.
					//check for highest level index.
					if(region != null && toEnter.highLerp.y > region.highLerp.y)
						region = toEnter;


			}
		}
	}
	return region;
}
}
