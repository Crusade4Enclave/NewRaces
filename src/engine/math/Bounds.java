// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.math;

import engine.InterestManagement.WorldGrid;
import engine.gameManager.ZoneManager;
import engine.net.client.msg.PlaceAssetMsg.PlacementInfo;
import engine.objects.*;
import engine.server.MBServerStatics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class contains all methods of storing bounds
 * information within MagicBane and performing collision
 * detection against them.
 * <p>
 * These objects are essentially an AABB, given rotations
 * in MagicBane for placed objects come in a quantum of 90.
 */

public class Bounds {

	private static final LinkedBlockingQueue<Bounds> boundsPool = new LinkedBlockingQueue<>();
	public static HashMap<Integer,MeshBounds> meshBoundsCache = new HashMap<>();

	private Vector2f origin = new Vector2f();
	private Vector2f halfExtents = new Vector2f();
	private float rotation;
	private float rotationDegrees = 0;
	private Quaternion quaternion;
	private boolean flipExtents;

	private ArrayList<Regions> regions = new ArrayList<>();
	private ArrayList<Colliders> colliders = new ArrayList<>();

	// Default constructor

	public Bounds() {

		origin.zero();
		halfExtents.zero();
		rotation = 0.0f;
		flipExtents = false;
	}

	public static Bounds borrow() {
		Bounds outBounds;

		outBounds = boundsPool.poll();

		if (outBounds == null)
			outBounds = new Bounds();

		return outBounds;
	}

	public void release() {
		Bounds.zero(this);
		boundsPool.add(this);

	}

	public void setBounds(Vector2f origin, Vector2f extents, float rotation) {

		this.origin.set(origin);
		this.halfExtents.set(extents);
		this.rotation = rotation;
		
		this.flipExtents = Bounds.calculateFlipExtents(this);

	}

	public void setBounds(PlacementInfo sourceInfo) {

		Blueprint sourceBlueprint;

		sourceBlueprint = Blueprint.getBlueprint(sourceInfo.getBlueprintUUID());
		this.origin.set(sourceInfo.getLoc().x, sourceInfo.getLoc().z);
		this.halfExtents.set(sourceBlueprint.getExtents());
		
		this.quaternion = new Quaternion(sourceInfo.getRot().x, sourceInfo.getRot().y,sourceInfo.getRot().z,sourceInfo.getW());
			this.rotation = sourceInfo.getRot().y;
		this.flipExtents = Bounds.calculateFlipExtents(this);

	}

	public void setBounds(Bounds sourceBounds) {

		origin.set(sourceBounds.origin);
		halfExtents.set(sourceBounds.halfExtents);
		this.rotation = sourceBounds.rotation;
		this.flipExtents = sourceBounds.flipExtents;

	}

	public void setBounds(AbstractCharacter sourcePlayer) {

		this.origin.set(sourcePlayer.getLoc().x, sourcePlayer.getLoc().z);
		this.halfExtents.set(.5f, .5f);
		this.rotation = 0;
		this.flipExtents = false;

	}

	public void setBounds(Vector3fImmutable sourceLocation) {

		this.origin.set(sourceLocation.x, sourceLocation.z);
		this.halfExtents.set(.5f, .5f);
		this.rotation = 0;
		this.flipExtents = false;

	}

	public void setBounds(Vector3fImmutable sourceLocation, float halfExtent) {

		this.origin.set(sourceLocation.x, sourceLocation.z);
		this.halfExtents.set(halfExtent, halfExtent);
		this.rotation = 0;
		this.flipExtents = false;

	}

	public void setBounds(Building building) {

		Blueprint blueprint;
		MeshBounds meshBounds;
		int halfExtentX;
		int halfExtentY;
		// Need a blueprint for proper bounds

		blueprint = building.getBlueprint();
		
		this.quaternion = new Quaternion(building.getRot().x, building.getRot().y,building.getRot().z,building.getw());

		// Calculate Bounds for non-blueprint objects

		if (blueprint == null) {

			// If a mesh is a non-blueprint structure then we calculate
			// it's bounding box based upon defaults from original source
			// lookup.

            meshBounds = meshBoundsCache.get(building.getMeshUUID());
			this.origin.set(building.getLoc().x, building.getLoc().z);
          

            // Magicbane uses half halfExtents

            if (meshBounds == null){
            	halfExtentX = 1;
            	halfExtentY = 1;
            }else{
            	
            	float halfExtent = Math.max((meshBounds.maxX - meshBounds.minX)/2, (meshBounds.maxZ - meshBounds.minZ) /2);
            	halfExtentX = Math.round(halfExtent);
    			halfExtentY = Math.round(halfExtent);
            }
			

			// The rotation is reset after the new aabb is calculated.
            
			this.rotation = building.getRot().y;
            // Caclculate and set the new half halfExtents for the rotated bounding box
			// and reset the rotation to 0 for this bounds.
			this.halfExtents.set(halfExtentX, (halfExtentY));
			this.rotation = 0;

			this.setRegions(building);
			this.setColliders(building);
			return;
		}

		this.origin.set(building.getLoc().x, building.getLoc().z);
		this.rotation = building.getRot().y;
		this.halfExtents.set(blueprint.getExtents());
		this.flipExtents = Bounds.calculateFlipExtents(this);

	
		this.setRegions(building);
		this.setColliders(building);

	}






	// Identity Bounds at location
	public static void zero(Bounds bounds) {
		bounds.origin.zero();
		bounds.halfExtents.zero();
		bounds.rotation = 0.0f;
		bounds.flipExtents = false;
	}

	public static boolean collide(Vector3fImmutable location, Bounds targetBounds) {
		
		if (targetBounds == null)
			return false;

		boolean collisionState = false;
		Bounds identityBounds =  Bounds.borrow();
		identityBounds.setBounds(location);

		collisionState = collide(targetBounds, identityBounds, 0.0f);
		identityBounds.release();
		return collisionState;
	}


	public static boolean collide(Vector3fImmutable location, Building targetBuilding) {

		boolean collisionState = false;
		Bounds targetBounds = targetBuilding.getBounds();
		
		if (targetBounds == null)
			return false;
		Bounds identityBounds =  Bounds.borrow();
		identityBounds.setBounds(location);

		collisionState = collide(targetBounds, identityBounds, 0.1f);
		identityBounds.release();
		return collisionState;
	}

	public static boolean collide(Bounds sourceBounds, Bounds targetBounds, float threshold) {

		float deltaX;
		float deltaY;
		float extentX;
		float extentY;
		float sourceExtentX;
		float sourceExtentY;
		float targetExtentX;
		float targetExtentY;

		deltaX = Math.abs(sourceBounds.origin.x - targetBounds.origin.x);
		deltaY = Math.abs(sourceBounds.origin.y - targetBounds.origin.y);

		if (sourceBounds.flipExtents) {
			sourceExtentX = sourceBounds.halfExtents.y;
			sourceExtentY = sourceBounds.halfExtents.x;
		}
		else {
			sourceExtentX = sourceBounds.halfExtents.x;
			sourceExtentY = sourceBounds.halfExtents.y;
		}
		if (targetBounds.flipExtents) {
			targetExtentX = targetBounds.halfExtents.y;
			targetExtentY = targetBounds.halfExtents.x;
		}
		else {
			targetExtentX = targetBounds.halfExtents.x;
			targetExtentY = targetBounds.halfExtents.y;
		}

		extentX = sourceExtentX + targetExtentX;
		extentY = sourceExtentY + targetExtentY;

		// Return false on overlapping edge cases
		if ((Math.abs(deltaX + threshold) < extentX))
			if ((Math.abs(deltaY + threshold) < extentY))
				return true;

		return false;

    }

	// Method detects overlap of two given Bounds objects.
	// Just your generic AABB collision algorythm.

	public static boolean collide(PlacementInfo sourceInfo, Building targetBuilding) {

		Bounds sourceBounds;
		Bounds targetBounds;

		boolean collisionState = false;

		// Early exit sanity check.  Can't quite collide against nothing

		if ((sourceInfo == null) || (targetBuilding == null))
			return false;

		sourceBounds = Bounds.borrow();
		sourceBounds.setBounds(sourceInfo);

		// WARNING: DO NOT EVER RELEASE THESE WORLDOBJECT BOUNDS
		// THEY ARE NOT IMMUTABLE

		targetBounds = targetBuilding.getBounds();

		// If target building has no bounds, we certainly cannot collide.
		// Note: We remove and release bounds objects to the pool when
		// buildings are destroyed.

		if (targetBounds == null)
			return false;

		collisionState = collide(sourceBounds, targetBounds,.1f);

		// Release bounds and return collision state

		sourceBounds.release();
		return collisionState;
	}
	
	public static boolean collide(Bounds bounds, Vector3fImmutable start, Vector3fImmutable end) {
		boolean collide = false;
		for (Colliders collider: bounds.colliders) {
			
			collide = linesTouching(collider.startX, collider.startY, collider.endX,collider.endY, start.x, start.z, end.x,end.z);
			
			if (collide)
				break;
			
			
		}
		
		return collide;
	}
	
	//used for wall collision with players.
	public static Vector3fImmutable PlayerBuildingCollisionPoint(PlayerCharacter player, Vector3fImmutable start, Vector3fImmutable end) {
		Vector3fImmutable collidePoint = null;
	
		//player can fly over walls when at max altitude. skip collision checks.
		if (player.getAltitude() >= 60)
			return null;
		
		
				float distance = player.getLoc().distance2D(end);
				// Players should not be able to move more than 2000 units at a time, stop them dead in their tracks if they do. (hacks)
				if (distance > 2000)
					return player.getLoc();
					

				
					HashSet<AbstractWorldObject> awoList = WorldGrid.getObjectsInRangePartial(player, distance + 1000, MBServerStatics.MASK_BUILDING);
					float collideDistance = 0;
					float lastDistance = -1;


					for (AbstractWorldObject awo : awoList) {
						
						Building building = (Building)awo;
						
						

						//player is inside building region, skip collision check. we only do collision from the outside. 
						if (player.getRegion() != null && player.getRegion().parentBuildingID == building.getObjectUUID())
							continue;
						if (building.getBounds().colliders == null)
							continue;
						
						for (Colliders collider: building.getBounds().colliders) {
							
							//links are what link together buildings, allow players to run through them only if they are in a building already.
							if (collider.isLink() && player.getRegion() != null)
								continue;
							if (collider.getDoorID() != 0 && building.isDoorOpen(collider.getDoorID()))
								continue;
							
							Vector3fImmutable tempCollidePoint = lineIntersection(collider.startX, collider.startY, collider.endX,collider.endY, start.x, start.z, end.x,end.z);
							
							//didnt collide, skip distance checks.
							if (tempCollidePoint == null)
								continue;
				
							//first collision detection, inititialize all variables.
							if (lastDistance == -1) {
								 collideDistance = start.distance2D(tempCollidePoint);
								 lastDistance = collideDistance;
								 collidePoint = tempCollidePoint;
							}else
								//get closest collide point.
								 collideDistance = start.distance2D(tempCollidePoint);
							
							if (collideDistance < lastDistance) {
								lastDistance = collideDistance;
								 collidePoint = tempCollidePoint;
							}
						}
					}
						
		 
					
					//
					if (collidePoint != null) {
						
						if(collideDistance >= 2)
						 collidePoint = player.getFaceDir().scaleAdd(-2f, new Vector3fImmutable((float) collidePoint.getX(), end.y, (float) collidePoint.getZ()));
						else
							collidePoint = player.getLoc();
					}
	
		
		return collidePoint;
	}
	
	public static boolean linesTouching(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		  float denominator = ((x2 - x1) * (y4 - y3)) - ((y2 - y1) * (x4 - x3));
		  float numerator1 = ((y1 - y3) * (x4 - x3)) - ((x1 - x3) * (y4 - y3));
		  float numerator2 = ((y1 - y3) * (x2 - x1)) - ((x1 - x3) * (y2 - y1));

		  // Detect coincident lines (has a problem, read below)
		  if (denominator == 0) return numerator1 == 0 && numerator2 == 0;

		  float r = numerator1 / denominator;
		  float s = numerator2 / denominator;

		  return (r >= 0 && r <= 1) && (s >= 0 && s <= 1);
		}
	
	public static Vector3fImmutable lineIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {

		  // calculate the distance to intersection point
		  float uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
		  float uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));

		  // if uA and uB are between 0-1, lines are colliding
		  if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
		    return new Vector3fImmutable(x1 + (uA * (x2-x1)),0, y1 + (uA * (y2-y1)));
		  }
		  return null;
		}
	
	

	private static boolean calculateFlipExtents(Bounds bounds) {

		int degrees;
		double radian =0;
        if (bounds.quaternion != null){
            radian = bounds.quaternion.angleY;
		}
			 
		degrees = (int) Math.toDegrees(radian);
		bounds.rotationDegrees = degrees;
		if (degrees < 0)
			degrees += 360;
        return (degrees >= 85 && degrees <= 95) ||
                (degrees >= 265 && degrees <= 275);

    }

	public void modify(float x, float y, float extents) {
		this.origin.x = x;
		this.origin.y = y;
		this.halfExtents.x = extents;
		this.halfExtents.y = extents;
	}


	/**
	 * @return the origin
	 */
	public Vector2f getOrigin() {
		return origin;
	}

	/**
	 * @return the halfExtents
	 */
	public Vector2f getHalfExtents() {
		return halfExtents;
	}

	/**
	 * @return the rotation
	 */
	public float getRotation() {
		return rotation;
	}

	/**
	 * @param rotation the rotation to set
	 */
	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	


	public void setRegions(Building building ){
		//Collidables are for player movement collision
		ArrayList<BuildingRegions> tempList = BuildingRegions._staticRegions.get(building.getMeshUUID());

		ArrayList<Regions> tempRegions = new ArrayList<>();
		if (tempList != null){
				
			for (BuildingRegions buildingRegion:tempList){

				ArrayList<Vector3f> regionPoints = new ArrayList<>();

				Vector3f centerPoint = ZoneManager.convertLocalToWorld(building, buildingRegion.center, this);
					for (Vector3f point: buildingRegion.getRegionPoints()){
						Vector3f rotatedPoint = ZoneManager.convertLocalToWorld(building, point,this);
						regionPoints.add(rotatedPoint);
					}
					tempRegions.add(new Regions(regionPoints, buildingRegion.getLevel(),buildingRegion.getRoom(),buildingRegion.isOutside(),buildingRegion.isExitRegion(), buildingRegion.isStairs(), centerPoint,building.getObjectUUID()));
				}
			
		}
		
		
		this.regions = tempRegions;
	}
	
	public void setColliders(Building building ){
		//Collidables are for player movement collision
		ArrayList<StaticColliders> tempList = StaticColliders._staticColliders.get(building.getMeshUUID());

		ArrayList<Colliders> tempColliders = new ArrayList<>();
		if (tempList != null){
				
			for (StaticColliders staticCollider :tempList){

				ArrayList<Vector3f> regionPoints = new ArrayList<>();

				Vector3f colliderStart = new Vector3f(staticCollider.getStartX(), 0, staticCollider.getStartY());
				Vector3f colliderEnd = new Vector3f(staticCollider.getEndX(), 0, staticCollider.getEndY());
				Vector3f worldStart = ZoneManager.convertLocalToWorld(building, colliderStart, this);
				Vector3f worldEnd = ZoneManager.convertLocalToWorld(building, colliderEnd, this);
				tempColliders.add(new Colliders(worldStart.x, worldStart.z, worldEnd.x, worldEnd.z, staticCollider.getDoorID(), staticCollider.isLink()));
				}
			
		}
		
		
		this.colliders = tempColliders;
	}

	public ArrayList<Regions> getRegions() {
		return regions;
	}

	public void setRegions(ArrayList<Regions> regions) {
		this.regions = regions;
	}
	
	public static Vector3f getRotatedPoint(Vector3f point, float centerX, float centerZ, float angle){
	
		//TRANSLATE TO ORIGIN
		float x1 = point.getX() - centerX;
		float y1 = point.getZ() - centerZ;

		//APPLY ROTATION
		float temp_x1 = (float) (x1 * Math.cos(angle) - y1 * Math.sin(angle));
		float temp_z1 = (float) (x1 * Math.sin(angle) + y1 * Math.cos(angle));
		
		temp_x1 += centerX;
		temp_z1 += centerZ;
		
		return new Vector3f(temp_x1,point.y,temp_z1);
		
	}
	

	public float getRotationDegrees() {
		return rotationDegrees;
	}

	public boolean isFlipExtents() {
		return flipExtents;
	}

	public Quaternion getQuaternion() {
		return quaternion;
	}

}
