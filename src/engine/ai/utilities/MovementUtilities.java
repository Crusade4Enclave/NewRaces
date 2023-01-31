// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.ai.utilities;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.exception.MsgSendException;
import engine.gameManager.MovementManager;
import engine.math.Vector3fImmutable;
import engine.net.client.msg.MoveToPointMsg;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.concurrent.ThreadLocalRandom;

import static engine.math.FastMath.sqr;
import static engine.math.FastMath.sqrt;

public class MovementUtilities {


	public static boolean inRangeOfBindLocation(Mob agent){
		
		
		
		if (agent.isPlayerGuard){
			
			Mob guardCaptain = null;
			if (agent.contract != null)
				guardCaptain = agent;
			else
		guardCaptain = (Mob) agent.npcOwner;
			
			if (guardCaptain != null){
				Building barracks = guardCaptain.building;
				
				if (barracks != null){
					City city = barracks.getCity();
					
					if (city != null){
						Building tol = city.getTOL();
						
						//Guards recall distance = 814.
						if (tol != null){
							if (agent.getLoc().distanceSquared2D(tol.getLoc()) > sqr(Enum.CityBoundsType.SIEGE.extents)) {
					                return false;
					            }
						}
						
					}
				}
			}
			
			return true;
		}

		Vector3fImmutable sl = new Vector3fImmutable(agent.getLoc().getX(), 0, agent.getLoc().getZ());
		Vector3fImmutable tl = new Vector3fImmutable(agent.bindLoc.x,0,agent.bindLoc.z);

		float distanceSquaredToTarget = sl.distanceSquared2D(tl); //distance to center of target
		float zoneRange = 250;

		if (agent.parentZone != null){
			if (agent.parentZone.getBounds() != null)
				zoneRange = agent.parentZone.getBounds().getHalfExtents().x * 2;
		}

		if (zoneRange > 300)
			zoneRange = 300;
		
		if (agent.spawnRadius > zoneRange)
			zoneRange = agent.spawnRadius;
		

		return distanceSquaredToTarget < sqr(MBServerStatics.AI_DROP_AGGRO_RANGE + zoneRange);

	}

	public static boolean inRangeToAggro(Mob agent,PlayerCharacter target){

		Vector3fImmutable sl = agent.getLoc();
		Vector3fImmutable tl =target.getLoc();

		float distanceSquaredToTarget = sl.distanceSquared2D(tl) - sqr(agent.calcHitBox() + target.calcHitBox()); //distance to center of target
		float range = MBServerStatics.AI_BASE_AGGRO_RANGE;

		if (agent.isPlayerGuard)
			range = 150;

		return distanceSquaredToTarget < sqr(range);

	}

	public static boolean inRangeDropAggro(Mob agent,PlayerCharacter target){

		Vector3fImmutable sl = agent.getLoc();
		Vector3fImmutable tl = target.getLoc();

		float distanceSquaredToTarget = sl.distanceSquared2D(tl) - sqr(agent.calcHitBox() + target.calcHitBox()); //distance to center of target

		float range = agent.getRange() + 150;

		if (range > 200)
			range = 200;


		return distanceSquaredToTarget < sqr(range);

	}

	public static Vector3fImmutable GetMoveLocation(Mob aiAgent, AbstractCharacter aggroTarget){

		// Player isnt moving and neither is mob.  Just return
		// the mobile's current location.  Ain't goin nowhere!
		// *** Refactor: Check to ensure methods calling us
		// all don't sent move messages when not moving.

		if ((aggroTarget.isMoving() == false))
			return aggroTarget.getLoc();

		if (aggroTarget.getEndLoc().x != 0){

			float aggroTargetDistanceSquared = aggroTarget.getLoc().distanceSquared2D(aggroTarget.getEndLoc());
			float aiAgentDistanceSquared = aiAgent.getLoc().distanceSquared2D(aggroTarget.getEndLoc());

			if (aiAgentDistanceSquared >= aggroTargetDistanceSquared)
				return aggroTarget.getEndLoc();
			else{
				float distanceToMove = sqrt(aggroTargetDistanceSquared + aiAgentDistanceSquared) *.5f;

				return aggroTarget.getFaceDir().scaleAdd(distanceToMove, aggroTarget.getLoc());

			}
		}

		// One of us is moving so let's calculate our destination loc for this
		// simulation frame.  We will simply project our position onto the
		// character's movement vector and return the closest point.

		return aiAgent.getLoc().ClosestPointOnLine(aggroTarget.getLoc(), aggroTarget.getEndLoc());
	}

	public static void moveToLocation(Mob agent,Vector3fImmutable newLocation, float offset){
		try {
			
			//don't move farther than 30 units from player.
			if (offset > 30)
				offset = 30;
			Vector3fImmutable newLoc = Vector3fImmutable.getRandomPointInCircle(newLocation, offset);


			agent.setFaceDir(newLoc.subtract2D(agent.getLoc()).normalize());
		
			aiMove(agent,newLoc,false);
		} catch (Exception e) {
			Logger.error( e.toString());
		}
	}



	public static boolean canMove(Mob agent) {
		if (agent.getMobBase() != null && Enum.MobFlagType.SENTINEL.elementOf(agent.getMobBase().getFlags()))
			return false;

		return (agent.isAlive() && !agent.getBonuses().getBool(ModType.Stunned,SourceType.None) && !agent.getBonuses().getBool(ModType.CannotMove, SourceType.None));
	}

	public static Vector3fImmutable randomPatrolLocation(Mob agent,Vector3fImmutable center, float radius){

		//Determing where I want to move.
		return new Vector3fImmutable((center.x - radius) + ((ThreadLocalRandom.current().nextFloat()+.1f*2)*radius),
				center.y,
				(center.z - radius) + ((ThreadLocalRandom.current().nextFloat()+.1f *2)*radius));
	}
	public static Long estimateMovementTime(Mob agent) {
		if(agent.getEndLoc().x == 0 && agent.getEndLoc().y == 0)
			return 0L;

		return (long) ((agent.getLoc().distance2D(agent.getEndLoc())*1000)/agent.getSpeed());
	}

	public static void aiMove(Mob agent,Vector3fImmutable vect, boolean isWalking) {

		//update our walk/run state.
		if (isWalking && !agent.isWalk()){
			agent.setWalkMode(true);
			MovementManager.sendRWSSMsg(agent);
		}else if(!isWalking && agent.isWalk()){
			agent.setWalkMode(false);
			MovementManager.sendRWSSMsg(agent);
		}

		MoveToPointMsg msg = new MoveToPointMsg();


//		Regions currentRegion = Mob.InsideBuildingRegion(agent);
//
//		if (currentRegion != null){
//
//
//			if (currentRegion.isGroundLevel()){
//				agent.setInBuilding(0);
//				agent.setInFloorID(-1);
//			}else{
//				agent.setInBuilding(currentRegion.getLevel());
//				agent.setInFloorID(currentRegion.getRoom());
//			}
//		}else{
//			agent.setInBuilding(-1);
//			agent.setInFloorID(-1);
//			agent.setInBuildingID(0);
//		}
//		agent.setLastRegion(currentRegion);



		Vector3fImmutable startLoc = null;
		Vector3fImmutable endLoc = null;

//		if (agent.getLastRegion() != null){
//			Building inBuilding = Building.getBuildingFromCache(agent.getInBuildingID());
//			if (inBuilding != null){
//				startLoc = ZoneManager.convertWorldToLocal(inBuilding, agent.getLoc());
//				endLoc = ZoneManager.convertWorldToLocal(inBuilding, vect);
//			}
//		}else{
//			agent.setBuildingID(0);
//			agent.setInBuildingID(0);
//			startLoc = agent.getLoc();
//			endLoc = vect;
//		}
		
		startLoc = agent.getLoc();
		endLoc = vect;

		msg.setSourceType(GameObjectType.Mob.ordinal());
		msg.setSourceID(agent.getObjectUUID());
		msg.setStartCoord(startLoc);
		msg.setEndCoord(endLoc);
		msg.setUnknown01(-1);
		msg.setInBuilding(-1);
		msg.setTargetType(0);
		msg.setTargetID(0);


		try {
			MovementManager.movement(msg, agent);
		} catch (MsgSendException e) {
			// TODO Figure out how we want to handle the msg send exception
			e.printStackTrace();
		}
	}
	
	public static Vector3fImmutable GetDestinationToCharacter(Mob aiAgent, AbstractCharacter character){
		
		if (!character.isMoving())
			return character.getLoc();
			
		
		float agentDistanceEndLoc = aiAgent.getLoc().distanceSquared2D(character.getEndLoc());
		float characterDistanceEndLoc = character.getLoc().distanceSquared2D(character.getEndLoc());
		
		if (agentDistanceEndLoc > characterDistanceEndLoc)
			return character.getEndLoc();
		
		return character.getLoc();
	}
	
	public static boolean updateMovementToCharacter(Mob aiAgent, AbstractCharacter aggroTarget){
		
		if (aiAgent.destination.equals(Vector3fImmutable.ZERO))
			return true;
		
		if (!aiAgent.isMoving())
			return true;
		
		
		
		
		if (aggroTarget.isMoving()){
		if (!aiAgent.destination.equals(aggroTarget.getEndLoc()) && !aiAgent.destination.equals(aggroTarget.getLoc()))
			return true;
		}else{
			if (aiAgent.destination.equals(aggroTarget.getLoc()))
				return false;
		}
		
		return false;
	}

}
