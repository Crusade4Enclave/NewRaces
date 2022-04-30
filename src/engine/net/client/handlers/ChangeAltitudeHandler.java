// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ChangeAltitudeMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.AbstractCharacter;
import engine.objects.Building;
import engine.objects.PlayerCharacter;
import engine.objects.Regions;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

public class ChangeAltitudeHandler extends AbstractClientMsgHandler {

	public ChangeAltitudeHandler() {
		super(ChangeAltitudeMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg,
			ClientConnection origin) throws MsgSendException {

		ChangeAltitudeMsg msg = (ChangeAltitudeMsg) baseMsg;

		PlayerCharacter pc = origin.getPlayerCharacter();
		if (pc == null) {
			return false;
		}

		if (!AbstractCharacter.CanFly(pc))
			return false;
		
		if (pc.isSwimming())
			return false;
		if (pc.getRegion() != null && !pc.getRegion().isOutside())
			return false;
		




		

		// Find out if we already have an altitude timer running and if so
		// do not process more alt change requests
		
		if (pc.getTakeOffTime() != 0)
			return false;

		
		// remove all movement timers and jobs
		//TODO: test if they can fly

		float targetAlt;
		float amount = msg.getAmountToMove();
		if (amount != 10 && amount != 60)
			return false;
		if (pc.getAltitude() == 60 && msg.up())
			return true;
		if (pc.getAltitude() == 0 && !msg.up())
			return true;
		
		pc.update();
		pc.stopMovement(pc.getLoc());
		msg.setStartAlt(pc.getAltitude());
		if (msg.up()) {

			pc.landingRegion = null;
			if (pc.getAltitude() == 0){
				Regions upRegion = pc.getRegion();
				if (upRegion != null){
					float startAlt = 0;
					Building regionBuilding = Regions.GetBuildingForRegion(upRegion);
					if (upRegion != null)
					 startAlt = upRegion.lerpY(pc) - regionBuilding.getLoc().y;
					float rounded = startAlt *.10f;

					rounded = ((int)rounded) * 10;

					if (rounded < 0)
						rounded = 0;

					
						msg.setStartAlt(startAlt);
					targetAlt = rounded + amount;

					if (targetAlt > 60)
						targetAlt = 60;
					
					pc.setAltitude(startAlt);
					pc.setDesiredAltitude(targetAlt);
				}else{
					msg.setStartAlt(pc.getAltitude());
					targetAlt = pc.getAltitude() + amount;
					if (targetAlt > 60)
						targetAlt = 60;
				}
			}else{
				msg.setStartAlt(pc.getAltitude());

				targetAlt = pc.getAltitude() + amount;
				if (targetAlt > 60)
					targetAlt = 60;
			}


		} else {
			msg.setStartAlt(pc.getAltitude());
			targetAlt = pc.getAltitude() - amount;
			if (targetAlt < 0)
				targetAlt = 0;
		}
		msg.setTargetAlt(targetAlt);
		if (pc.getAltitude() < 1 && targetAlt > pc.getAltitude()) {
			// char is on the ground and is about to start flight
			if (pc.getStamina() < 10) {
				return false;
			}
		}

		if (MBServerStatics.MOVEMENT_SYNC_DEBUG) {
			Logger.info ("Changing Altitude, moving=" + pc.isMoving() +
					" Current Loc " + pc.getLoc().getX() + ' ' + pc.getLoc().getZ() +
					" End Loc " + pc.getEndLoc().getX() + ' ' + pc.getEndLoc().getZ());
		}

		if (msg.up()){
			pc.update();
			pc.setDesiredAltitude(targetAlt);
			pc.setTakeOffTime(System.currentTimeMillis());
		}
			
		else{
			Regions region = PlayerCharacter.InsideBuildingRegionGoingDown(pc);
			
		
			
			if (region != null){
				float landingAltitude = 0;
				Building building = Regions.GetBuildingForRegion(region);
				if (building != null)
					landingAltitude = region.lerpY(pc) - building.getLoc().y;
				
				if (landingAltitude >= targetAlt){
					pc.landingRegion = region;
					pc.landingAltitude = landingAltitude;
					pc.setDesiredAltitude(landingAltitude);
				}else{
					pc.landingRegion = null;
					pc.setDesiredAltitude(targetAlt);
				}
				
				
			}else
				pc.setDesiredAltitude(targetAlt);

			pc.update();
			
		
			pc.setTakeOffTime(System.currentTimeMillis());
		}



		// Add timer for height change cooldown, this also tells getLoc we are not moving
		//MovementManager.addChangeAltitudeTimer(pc, msg.getStartAlt(), msg.getTargetAlt(), (int)((MBServerStatics.HEIGHT_CHANGE_TIMER_MS * amount) + 100 )  );

		// Add flight timer job to check stam and ground you when you run out
		//MovementManager.addFlightTimer(pc, msg, MBServerStatics.FLY_FREQUENCY_MS);
		//Send change altitude to all in range
		DispatchMessage.dispatchMsgToInterestArea(pc, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
		
		

		return true;
	}

}
