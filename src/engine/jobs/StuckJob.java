// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

 package engine.jobs;

import engine.InterestManagement.WorldGrid;
import engine.job.AbstractScheduleJob;
import engine.math.Bounds;
import engine.math.Vector3fImmutable;
import engine.net.client.msg.ErrorPopupMsg;
import engine.objects.AbstractWorldObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;

import java.util.HashSet;

public class StuckJob extends AbstractScheduleJob {

	private final PlayerCharacter player;

	public StuckJob(PlayerCharacter player) {
		super();
		this.player = player;
	}

	@Override
	protected void doJob() {
        
        Vector3fImmutable stuckLoc;
        Building stuckBuilding = null;
 
        if (player == null)
            return;
        
        if (player.getClientConnection() == null)
            return;
        
        HashSet<AbstractWorldObject>awoList = WorldGrid.getObjectsInRangePartial(player, 150, MBServerStatics.MASK_BUILDING);
       
        for (AbstractWorldObject awo:awoList){

        	Building toStuckOutOf = (Building)awo;

        	if (toStuckOutOf.getStuckLocation() == null)
        		continue;

        	if (Bounds.collide(player.getLoc(), toStuckOutOf)){
        		stuckBuilding = toStuckOutOf;
        		break;
        		
        	}
        }

        //Could not find closest building get stuck location of nearest building.
        
        if (stuckBuilding == null){
            ErrorPopupMsg.sendErrorMsg(player, "Unable to find desired location");
    	return;
        } else
        	stuckLoc = stuckBuilding.getStuckLocation();
        
        if (stuckLoc == null){
            ErrorPopupMsg.sendErrorMsg(player, "Unable to find desired location");
        	return;
        }
        	
        
        player.teleport(stuckLoc);
        
        
        	
        
        // Needs to be re-written with stuck locations
        // Disabled for now.
 
        
        /*
        
        // Cannot have a null zone or player
        
        if (this.player == null)
            return;
        
        if (ZoneManager.findSmallestZone(player.getLoc()) == null)
            return;
        
        // If player is on a citygrid make sure the stuck direction
        // is facing away from the tree
        
        if ((ZoneManager.findSmallestZone(player.getLoc()).isNPCCity()) ||
            (ZoneManager.findSmallestZone(player.getLoc()).isPlayerCity())) {
           
            zoneVector = player.getLoc().subtract(ZoneManager.findSmallestZone(player.getLoc()).getLoc());
            zoneVector = zoneVector.normalize();
            
            if (zoneVector.dot(player.getFaceDir()) > 0)
               return;
           
        }
 
        player.teleport(player.getLoc().add(player.getFaceDir().mult(34)));
*/
	}

	@Override
	protected void _cancelJob() {
	}

	private void sendTrackArrow(float rotation) {
	}

}

