// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.Enum.DispatchChannel;
import engine.Enum.GameObjectType;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.InterestManagement.InterestManager;
import engine.InterestManagement.WorldGrid;
import engine.exception.MsgSendException;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.ChangeAltitudeJob;
import engine.jobs.FlightJob;
import engine.math.Bounds;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ChangeAltitudeMsg;
import engine.net.client.msg.MoveToPointMsg;
import engine.net.client.msg.TeleportToPointMsg;
import engine.net.client.msg.UpdateStateMsg;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;

import static engine.math.FastMath.sqr;

public enum MovementManager {

	MOVEMENTMANAGER;

	private static final String changeAltitudeTimerJobName = "ChangeHeight";
	private static final String flightTimerJobName = "Flight";

	public static void sendOOS(PlayerCharacter pc) {
		pc.setWalkMode(true);
		MovementManager.sendRWSSMsg(pc);
	}

	public static void sendRWSSMsg(AbstractCharacter ac) {

		if (!ac.isAlive())
			return;
		UpdateStateMsg rssm = new UpdateStateMsg();
		rssm.setPlayer(ac);
		if (ac.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(ac, rssm, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
		else
			DispatchMessage.sendToAllInRange(ac, rssm);
	}

	/*
	 * Sets the first combat target for the AbstractCharacter. Used to clear the
	 * combat
	 * target upon each move, unless something has set the firstHitCombatTarget
	 * Also used to determine the size of a monster's hitbox
	 */
	public static void movement(MoveToPointMsg msg, AbstractCharacter toMove) throws MsgSendException {

		// check for stun/root
		if (!toMove.isAlive())
			return;

		if (toMove.getObjectType().equals(GameObjectType.PlayerCharacter)){
			if (((PlayerCharacter)toMove).isCasting())
				((PlayerCharacter)toMove).update();
		}
			
		
		
		toMove.setIsCasting(false);
		toMove.setItemCasting(false);

		if (toMove.getBonuses().getBool(ModType.Stunned, SourceType.None) || toMove.getBonuses().getBool(ModType.CannotMove, SourceType.None)) {
			return;
		}
		
		if (msg.getEndLat() > MBServerStatics.MAX_WORLD_WIDTH)
			msg.setEndLat((float) MBServerStatics.MAX_WORLD_WIDTH);
		
		if (msg.getEndLon() < MBServerStatics.MAX_WORLD_HEIGHT){
			msg.setEndLon((float) MBServerStatics.MAX_WORLD_HEIGHT);
		}
		
//		if (msg.getEndLat() < 0)
//			msg.setEndLat(0);
//		
//		if (msg.getEndLon() > 0)
//			msg.setEndLon(0);
		
		

		
		
		if (!toMove.isMoving())
			toMove.resetLastSetLocUpdate();
		 else
			toMove.update();

		// Update movement for the player
			
			
		//    	else if (toMove.getObjectType() == GameObjectType.Mob)
		//    		((Mob)toMove).updateLocation();
		// get start and end locations for the move
		Vector3fImmutable startLocation = new Vector3fImmutable(msg.getStartLat(), msg.getStartAlt(), msg.getStartLon());
		Vector3fImmutable endLocation = new Vector3fImmutable(msg.getEndLat(), msg.getEndAlt(), msg.getEndLon());

		//		if (toMove.getObjectType() == GameObjectType.PlayerCharacter)
		//			if (msg.getEndAlt() == 0 && msg.getTargetID() == 0){
		//				MovementManager.sendRWSSMsg(toMove);
		//			}

		//If in Building, let's see if we need to Fix

		// if inside a building, convert both locations from the building local reference frame to the world reference frame

		if (msg.getTargetID() > 0) {
			Building building = BuildingManager.getBuildingFromCache(msg.getTargetID());
			if (building != null) {
				
				Vector3fImmutable convertLocEnd = new Vector3fImmutable(ZoneManager.convertLocalToWorld(building, endLocation));
				//                if (!Bounds.collide(convertLocEnd, b) || !b.loadObjectsInside()) {
				//                    toMove.setInBuilding(-1);
				//                    toMove.setInFloorID(-1);
				//                    toMove.setInBuildingID(0);
				//                }
				//                else {
				toMove.setInBuilding(msg.getInBuilding());
				toMove.setInFloorID(msg.getUnknown01());
				toMove.setInBuildingID(msg.getTargetID());
				msg.setStartCoord(ZoneManager.convertWorldToLocal(building, toMove.getLoc()));
		
				if (toMove.getObjectType() == GameObjectType.PlayerCharacter) {
					if (convertLocEnd.distanceSquared2D(toMove.getLoc()) > 6000 * 6000) {

						Logger.info( "ENDLOC:" + convertLocEnd.x + ',' + convertLocEnd.y + ',' + convertLocEnd.z +
								',' + "GETLOC:" + toMove.getLoc().x + ',' + toMove.getLoc().y + ',' + toMove.getLoc().z + " Name " + ((PlayerCharacter) toMove).getCombinedName());
						toMove.teleport(toMove.getLoc());

						return;
					}
				}

				startLocation = toMove.getLoc();
				endLocation = convertLocEnd;

			} else {

				toMove.setInBuilding(-1);
				toMove.setInFloorID(-1);
				toMove.setInBuildingID(0);
				//SYNC PLAYER
				toMove.teleport(toMove.getLoc());
				return;
			}

		} else {
			toMove.setInBuildingID(0);
			toMove.setInFloorID(-1);
			toMove.setInBuilding(-1);
			msg.setStartCoord(toMove.getLoc());
		}
		
		//make sure we set the correct player.
		msg.setSourceType(toMove.getObjectType().ordinal());
		msg.setSourceID(toMove.getObjectUUID());
		
		//if player in region, modify location to local location of building. set target to building.
		if (toMove.getRegion() != null){
			Building regionBuilding = Regions.GetBuildingForRegion(toMove.getRegion());
			if (regionBuilding != null){
				msg.setStartCoord(ZoneManager.convertWorldToLocal(Regions.GetBuildingForRegion(toMove.getRegion()), toMove.getLoc()));
				msg.setEndCoord(ZoneManager.convertWorldToLocal(regionBuilding, endLocation));
				msg.setInBuilding(toMove.getRegion().level);
				msg.setUnknown01(toMove.getRegion().room);
				msg.setTargetType(GameObjectType.Building.ordinal());
				msg.setTargetID(regionBuilding.getObjectUUID());
			}
			
		}else{
			toMove.setInBuildingID(0);
			toMove.setInFloorID(-1);
			toMove.setInBuilding(-1);
			msg.setStartCoord(toMove.getLoc());
			msg.setEndCoord(endLocation);
			msg.setTargetType(0);
			msg.setTargetID(0);
		}

		//checks sync between character and server, if out of sync, teleport player to original position and return.
		if (toMove.getObjectType() == GameObjectType.PlayerCharacter) {
			boolean startLocInSync = checkSync(toMove, startLocation, toMove.getLoc());
			
			if (!startLocInSync){
				syncLoc(toMove, toMove.getLoc(), startLocInSync);
				return;
			}

		}
	
		// set direction, based on the current location which has just been sync'd
		// with the client and the calc'd destination
		toMove.setFaceDir(endLocation.subtract2D(toMove.getLoc()).normalize());
		
		boolean collide = false;
		if (toMove.getObjectType().equals(GameObjectType.PlayerCharacter)) {
			Vector3fImmutable collidePoint = Bounds.PlayerBuildingCollisionPoint((PlayerCharacter)toMove, toMove.getLoc(), endLocation);
			
			if (collidePoint != null) {
				msg.setEndCoord(collidePoint);
				endLocation = collidePoint;
				collide = true;
			}
						
		}
					
		if (toMove.getObjectType() == GameObjectType.PlayerCharacter && ((PlayerCharacter) toMove).isTeleportMode()) {
			toMove.teleport(endLocation);
			return;
		}

		// move to end location, this can interrupt the current move
		toMove.setEndLoc(endLocation);

		//	ChatManager.chatSystemInfo((PlayerCharacter)toMove, "Moving to " + Vector3fImmutable.toString(endLocation));

		// make sure server knows player is not sitting
		toMove.setSit(false);

		// cancel any effects that break upon movement
		toMove.cancelOnMove();

		//cancel any attacks for manual move.
		if ((toMove.getObjectType() == GameObjectType.PlayerCharacter) && msg.getUnknown02() == 0)
			toMove.setCombatTarget(null);


		// If it's not a player moving just send the message

		if ((toMove.getObjectType() == GameObjectType.PlayerCharacter) == false) {
			DispatchMessage.sendToAllInRange(toMove, msg);
			return;
		}

		// If it's a player who is moving then we need to handle characters
		// who should see the message via group follow

		PlayerCharacter player = (PlayerCharacter) toMove;

		player.setTimeStamp("lastMoveGate", System.currentTimeMillis());

		if (collide)
			DispatchMessage.dispatchMsgToInterestArea(player, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
		else
			DispatchMessage.dispatchMsgToInterestArea(player, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);


		// Handle formation movement if needed

		if (player.getFollow() == false)
			return;


		City cityObject = null;
		Zone serverZone = null;

		serverZone = ZoneManager.findSmallestZone(player.getLoc());
		cityObject = (City) DbManager.getFromCache(GameObjectType.City, serverZone.getPlayerCityUUID());

		// Do not send group messages if player is on grid

		if (cityObject != null)
			return;

		// If player is not in a group we can exit here

		Group group = GroupManager.getGroup(player);

		if (group == null)
			return;

		// Echo group movement messages

		if (group.getGroupLead().getObjectUUID() == player.getObjectUUID())
			moveGroup(player, player.getClientConnection(), msg);
				
	}

	/**
	 * compare client and server location to verify that the two are in sync
	 *
	 * @param ac        the player character
	 * @param clientLoc location as reported by the client
	 * @param serverLoc location known to the server
	 * @return true if the two are in sync
	 */
	private static boolean checkSync(AbstractCharacter ac, Vector3fImmutable clientLoc, Vector3fImmutable serverLoc) {

		float desyncDist = clientLoc.distanceSquared2D(serverLoc);

		// desync logging
		if (MBServerStatics.MOVEMENT_SYNC_DEBUG)
			if (desyncDist > MBServerStatics.MOVEMENT_DESYNC_TOLERANCE * MBServerStatics.MOVEMENT_DESYNC_TOLERANCE)
				// our current location server side is a calc of last known loc + direction + speed and known time of last update
				Logger.debug("Movement out of sync for " + ac.getFirstName()
				+ ", Server Loc: " + serverLoc.getX() + ' ' + serverLoc.getZ()
				+ " , Client loc: " + clientLoc.getX() + ' ' + clientLoc.getZ()
				+ " desync distance " + desyncDist
				+ " moving=" + ac.isMoving());
			else
				Logger.debug( "Movement sync is good - desyncDist = " + desyncDist);

		if (ac.getDebug(1) && ac.getObjectType().equals(GameObjectType.PlayerCharacter))
			if (desyncDist > MBServerStatics.MOVEMENT_DESYNC_TOLERANCE * MBServerStatics.MOVEMENT_DESYNC_TOLERANCE) {
				PlayerCharacter pc = (PlayerCharacter) ac;
				ChatManager.chatSystemInfo(pc,
						"Movement out of sync for " + ac.getFirstName()
						+ ", Server Loc: " + serverLoc.getX() + ' ' + serverLoc.getZ()
						+ " , Client loc: " + clientLoc.getX() + ' ' + clientLoc.getZ()
						+ " desync distance " + desyncDist
						+ " moving=" + ac.isMoving());
			}

		// return indicator that the two are in sync or not
		return (desyncDist < 100f * 100f);

	}

	//Update for when the character is in flight
	public static void updateFlight(PlayerCharacter pc, ChangeAltitudeMsg msg, int duration) {
		if (pc == null)
			return;

		// clear flight timer job as we are about to update stuff and submit a new job
		pc.clearTimer(flightTimerJobName);

		if (!pc.isActive()) {
			pc.setAltitude(0);
			pc.setDesiredAltitude(0);
			pc.setTakeOffTime(0);
			return;
		}

		// Check to see if we are mid height change
		JobContainer cjc = pc.getTimers().get(changeAltitudeTimerJobName);
		if (cjc != null) {
			addFlightTimer(pc, msg, MBServerStatics.FLY_FREQUENCY_MS);
			return;
		}

		// Altitude is zero, do nothing
		if (pc.getAltitude() < 1)
			return;

		//make sure player is still allowed to fly
		boolean canFly = false;
		PlayerBonuses bonus = pc.getBonuses();

		if (bonus != null && !bonus.getBool(ModType.NoMod, SourceType.Fly) && bonus.getBool(ModType.Fly, SourceType.None) && pc.isAlive())
			canFly = true;

		// if stam less that 2 - time to force a landing
		if (pc.getStamina() < 10f || !canFly) {
			PlayerCharacter.GroundPlayer(pc);
			// dont call stop movement here as we want to
			// preserve endloc
			//pc.stopMovement();
			// sync world location
			pc.setLoc(pc.getLoc());
			// force a landing
			msg.setStartAlt(pc.getAltitude());
			msg.setTargetAlt(0);
			msg.setAmountToMove(pc.getAltitude());
			msg.setUp(false);
			DispatchMessage.dispatchMsgToInterestArea(pc, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
			MovementManager.addChangeAltitudeTimer(pc, msg.getStartAlt(), msg.getTargetAlt(), (int) (MBServerStatics.HEIGHT_CHANGE_TIMER_MS * pc.getAltitude()));
			pc.setAltitude(msg.getStartAlt() - 10);

		} else //Add a new flight timer to check stam / force land
			if (pc.getAltitude() > 0)
				addFlightTimer(pc, msg, MBServerStatics.FLY_FREQUENCY_MS);

	}

	public static void finishChangeAltitude(AbstractCharacter ac, float targetAlt) {

		if (ac.getObjectType().equals(GameObjectType.PlayerCharacter) == false)
			return;

		//reset the getLoc timer before we clear other timers
		// otherwise the next call to getLoc will not be correct
		ac.resetLastSetLocUpdate();

		// call getLoc once as it processes loc to the ms
		Vector3fImmutable curLoc = ac.getLoc();

		if (MBServerStatics.MOVEMENT_SYNC_DEBUG)
			Logger.info("Finished Alt change, setting the end location to "
					+ ac.getEndLoc().getX() + ' ' + ac.getEndLoc().getZ()
					+ " moving=" + ac.isMoving()
					+ " and current location is " + curLoc.getX() + ' ' + curLoc.getZ());

		if (ac.getDebug(1) && ac.getObjectType().equals(GameObjectType.PlayerCharacter))
			ChatManager.chatSystemInfo((PlayerCharacter) ac, "Finished Alt change, setting the end location to " + ac.getEndLoc().getX() + ' ' + ac.getEndLoc().getZ() + " moving=" + ac.isMoving() + " and current location is " + curLoc.getX() + ' ' + curLoc.getZ());

		//Send run/walk/sit/stand to tell the client we are flying / landing etc
		ac.update();
		ac.stopMovement(ac.getLoc());
		if (ac.isAlive())
			MovementManager.sendRWSSMsg(ac);

		//Check collision again
	}


	// Handle formation movement in group

	public static void moveGroup(PlayerCharacter pc, ClientConnection origin, MoveToPointMsg msg) throws MsgSendException {
		// get forward vector
		Vector3f faceDir = new Vector3f(pc.getFaceDir().x, 0, pc.getFaceDir().z).normalize();
		// get perpendicular vector
		Vector3f crossDir = new Vector3f(faceDir.z, 0, -faceDir.x);

		//get source loc with altitude
		Vector3f sLoc = new Vector3f(pc.getLoc().x, pc.getAltitude(), pc.getLoc().z);

		Group group = GroupManager.getGroup(pc);
		Set<PlayerCharacter> members = group.getMembers();
		int pos = 0;
		for (PlayerCharacter member : members) {

			if (member == null)
				continue;
			if (member.getObjectUUID() == pc.getObjectUUID())
				continue;

			MoveToPointMsg groupMsg = new MoveToPointMsg(msg);

			// Verify group member should be moved

			pos++;
			if (member.getFollow() != true)
				continue;

			//get member loc with altitude, then range against source loc
			Vector3f mLoc = new Vector3f(member.getLoc().x, member.getAltitude(), member.getLoc().z);

			if (sLoc.distanceSquared2D(mLoc) > sqr(MBServerStatics.FORMATION_RANGE))
				continue;

			//don't move if player has taken damage from another player in last 60 seconds
			long lastAttacked = System.currentTimeMillis() - pc.getLastPlayerAttackTime();
			if (lastAttacked < 60000)
				continue;

			if (!member.isAlive())
				continue;

			//don't move if player is stunned or rooted
			PlayerBonuses bonus = member.getBonuses();
			if (bonus.getBool(ModType.Stunned, SourceType.None) || bonus.getBool(ModType.CannotMove, SourceType.None))
				continue;
			
			member.update();


			// All checks passed, let's move the player
			// First get the offset position
			Vector3f offset = Formation.getOffset(group.getFormation(), pos);
			Vector3fImmutable destination = pc.getEndLoc();
			// offset forwards or backwards
			destination = destination.add(faceDir.mult(offset.z));
			// offset left or right
			destination = destination.add(crossDir.mult(offset.x));
			//			ArrayList<AbstractWorldObject> awoList = WorldGrid.INSTANCE.getObjectsInRangePartial(member, member.getLoc().distance2D(destination) +1000, MBServerStatics.MASK_BUILDING);
			//
			//			boolean skip = false;
			//
			//			for (AbstractWorldObject awo: awoList){
			//				Building building = (Building)awo;
			//
			//				if (building.getBounds() != null){
			//					if (Bounds.collide(building, member.getLoc(), destination)){
			//						skip = true;
			//						break;
			//					}
			//
			//				}
			//
			//			}
			//
			//			if (skip)
			//				continue;
			//			if (member.isMoving())
			//				member.stopMovement();

			// Update player speed to match group lead speed and make standing
			if (member.isSit() || (member.isWalk() != pc.isWalk())) {
				member.setSit(false);
				member.setWalkMode(pc.isWalk());
				MovementManager.sendRWSSMsg(member);
			}

			//cancel any effects that break upon movement
			member.cancelOnMove();

			// send movement for other players to see
			groupMsg.setSourceID(member.getObjectUUID());
			groupMsg.setStartCoord(member.getLoc());
			groupMsg.setEndCoord(destination);
			groupMsg.clearTarget();
			DispatchMessage.sendToAllInRange(member, groupMsg);

			// update group member
			member.setFaceDir(destination.subtract2D(member.getLoc()).normalize());
			member.setEndLoc(destination);
		}
	}

	//Getting rid of flgith timer.

	public static void addFlightTimer(PlayerCharacter pc, ChangeAltitudeMsg msg, int duration) {
		if (pc == null || pc.getTimers() == null)
			return;
		if (!pc.getTimers().containsKey(flightTimerJobName)) {
			FlightJob ftj = new FlightJob(pc, msg, duration);
			JobContainer jc = JobScheduler.getInstance().scheduleJob(ftj, duration);
			pc.getTimers().put(flightTimerJobName, jc);
		}
	}

	public static void addChangeAltitudeTimer(PlayerCharacter pc, float startAlt, float targetAlt, int duration) {
		if (pc == null || pc.getTimers() == null)
			return;
		ChangeAltitudeJob catj = new ChangeAltitudeJob(pc, startAlt, targetAlt);
		JobContainer jc = JobScheduler.getInstance().scheduleJob(catj, duration);
		pc.getTimers().put(changeAltitudeTimerJobName, jc);
	}

	
	public static void translocate(AbstractCharacter teleporter, Vector3fImmutable targetLoc, Regions region) {


		if (targetLoc == null)
			return;


		Vector3fImmutable oldLoc = new Vector3fImmutable(teleporter.getLoc());
			
		
			teleporter.stopMovement(targetLoc);
			
			teleporter.setRegion(region);
		
	

			//mobs ignore region sets for now.
			if (teleporter.getObjectType().equals(GameObjectType.Mob)){
				teleporter.setInBuildingID(0);
				teleporter.setInBuilding(-1);
				teleporter.setInFloorID(-1);
				TeleportToPointMsg msg = new TeleportToPointMsg(teleporter, targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(), 0, -1, -1);
				DispatchMessage.dispatchMsgToInterestArea(oldLoc, teleporter, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
				return;
			}
		TeleportToPointMsg msg = new TeleportToPointMsg(teleporter, targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(), 0, -1, -1);
		//we shouldnt need to send teleport message to new area, as loadjob should pick it up.
	//	DispatchMessage.dispatchMsgToInterestArea(teleporter, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
		DispatchMessage.dispatchMsgToInterestArea(oldLoc, teleporter, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
		
		if (teleporter.getObjectType().equals(GameObjectType.PlayerCharacter))
		InterestManager.INTERESTMANAGER.HandleLoadForTeleport((PlayerCharacter)teleporter);

	}
	
	public static void translocateToObject(AbstractCharacter teleporter, AbstractWorldObject worldObject) {


		
		Vector3fImmutable targetLoc = teleporter.getLoc();

		Vector3fImmutable oldLoc = new Vector3fImmutable(teleporter.getLoc());

			teleporter.stopMovement(teleporter.getLoc());

		
		
	

			//mobs ignore region sets for now.
			if (teleporter.getObjectType().equals(GameObjectType.Mob)){
				teleporter.setInBuildingID(0);
				teleporter.setInBuilding(-1);
				teleporter.setInFloorID(-1);
				TeleportToPointMsg msg = new TeleportToPointMsg(teleporter, targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(), 0, -1, -1);
				DispatchMessage.dispatchMsgToInterestArea(oldLoc, teleporter, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
				return;
			}
		boolean collide = false;
		int maxFloor = -1;
		int buildingID = 0;
		boolean isGroundLevel = false;
		HashSet<AbstractWorldObject> buildings = WorldGrid.getObjectsInRangePartial(teleporter, 200, MBServerStatics.MASK_BUILDING);
		for (AbstractWorldObject awo : buildings) {
			Building building = (Building) awo;
			if (collide)
				break;
		}
		if (!collide) {
			teleporter.setInBuildingID(0);
			teleporter.setInBuilding(-1);
			teleporter.setInFloorID(-1);
		} else {
			if (isGroundLevel) {
				teleporter.setInBuilding(0);
				teleporter.setInFloorID(-1);
			} else {
				teleporter.setInBuilding(maxFloor - 1);
				teleporter.setInFloorID(0);
			}
		}


		TeleportToPointMsg msg = new TeleportToPointMsg(teleporter, targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(), 0, -1, -1);
		//we shouldnt need to send teleport message to new area, as loadjob should pick it up.
	//	DispatchMessage.dispatchMsgToInterestArea(teleporter, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
		DispatchMessage.dispatchMsgToInterestArea(oldLoc, teleporter, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
		
		if (teleporter.getObjectType().equals(GameObjectType.PlayerCharacter))
		InterestManager.INTERESTMANAGER.HandleLoadForTeleport((PlayerCharacter)teleporter);

	}

	private static void syncLoc(AbstractCharacter ac, Vector3fImmutable clientLoc, boolean useClientLoc) {
			ac.teleport(ac.getLoc());
	}
}
