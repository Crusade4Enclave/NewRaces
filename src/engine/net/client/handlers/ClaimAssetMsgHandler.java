// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.db.archive.CityRecord;
import engine.db.archive.DataWarehouse;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClaimAssetMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.Blueprint;
import engine.objects.Building;
import engine.objects.PlayerCharacter;
import org.pmw.tinylog.Logger;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import static engine.math.FastMath.sqr;

/*
 * @Author:
 * @Summary: Processes application protocol message which keeps
 * client's tcp connection open.
 */
public class ClaimAssetMsgHandler extends AbstractClientMsgHandler {

    // Instance variables

    private final ReentrantReadWriteLock claimLock = new ReentrantReadWriteLock();
    
	public ClaimAssetMsgHandler() {
		super(ClaimAssetMsg.class);
             
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
	
            // Member variable declaration
            this.claimLock.writeLock().lock();
            
            try{
            	PlayerCharacter sourcePlayer;
                Building targetBuilding;
                Blueprint blueprint;
                ClaimAssetMsg msg;
                int targetUUID;
                
                msg = (ClaimAssetMsg) baseMsg;
                targetUUID = msg.getUUID();
                
                sourcePlayer = origin.getPlayerCharacter();
                targetBuilding = BuildingManager.getBuildingFromCache(targetUUID);

                // Oops!  *** Refactor: Log error
       
                if ((sourcePlayer == null) ||
                     (targetBuilding == null))
                     return true;

                // Player must be reasonably close to building in order to claim

                if (sourcePlayer.getLoc().distanceSquared2D(targetBuilding.getLoc()) > sqr(100))
                    return true;

                // Early exit if object to be claimed is not errant
                
                if (targetBuilding.getOwnerUUID() != 0)
                    return true;


                // Early exit if UUID < the last database derived building UUID.

                if (targetBuilding.getProtectionState() == Enum.ProtectionState.NPC) {
                    return true;
                }

                // Early exit if claiming player does not
                // have a guild.

                // Errant players cannot claim
                
                if (sourcePlayer.getGuild().isEmptyGuild())
                    return true;
                
                // Can't claim an object without a blueprint

                if (targetBuilding.getBlueprintUUID() == 0)
                    return true;
                            
                blueprint = targetBuilding.getBlueprint();
                
                //cant claim mine this way.
                if (blueprint.getBuildingGroup() == BuildingGroup.MINE)
                	return true;

                // Players cannot claim shrines

                if ((targetBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE))
                    return true;

                // Can't claim a tree if your guild already owns one
                // *** Refactor : Send error to player here
                
                if ((sourcePlayer.getGuild().isNation()) &&
                    (blueprint.getBuildingGroup() == BuildingGroup.TOL))
                     return true;
                
                // Process the transfer of the building(s)
                
                if (blueprint.getBuildingGroup() == BuildingGroup.TOL) {
                    targetBuilding.getCity().claim(sourcePlayer);

                    // Push transfer of city to data warehouse
                    CityRecord cityRecord = CityRecord.borrow(targetBuilding.getCity(), Enum.RecordEventType.TRANSFER);
                    DataWarehouse.pushToWarehouse(cityRecord);

                } else
                    targetBuilding.claim(sourcePlayer);
               
            } catch(Exception e){
            	Logger.error("ClaimAssetMsgHandler", e.getMessage());
            }finally{
            	this.claimLock.writeLock().unlock();	
            }
            return true;
        }

}