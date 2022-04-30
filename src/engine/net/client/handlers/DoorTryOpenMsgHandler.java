package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.DoorState;
import engine.InterestManagement.WorldGrid;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.DoorTryOpenMsg;
import engine.objects.AbstractWorldObject;
import engine.objects.Blueprint;
import engine.objects.Building;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.HashSet;

/*
 * @Author:
 * @Summary: Processes application protocol message which handle
 * open and close door requests to and from the client.
 * 
 */
public class DoorTryOpenMsgHandler extends AbstractClientMsgHandler {

    public DoorTryOpenMsgHandler() {
        super(DoorTryOpenMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

        // Member variable declaration
        
        PlayerCharacter player;
        DoorTryOpenMsg msg;
        Building targetBuilding;
        int doorNumber;
        
        // Member variable assignment
        
        msg = (DoorTryOpenMsg)baseMsg;
        player = origin.getPlayerCharacter();
        targetBuilding =  BuildingManager.getBuildingFromCache(msg.getBuildingUUID());

        if (player == null || targetBuilding == null) {
            Logger.error("Player or Building returned NULL in OpenCloseDoor handling.");
            return true;
        }

        // Must be within x distance from door to manipulate it
        
        if (player.getLoc().distanceSquared2D(targetBuilding.getLoc()) > MBServerStatics.OPENCLOSEDOORDISTANCE * MBServerStatics.OPENCLOSEDOORDISTANCE)
            return true;

        doorNumber = Blueprint.getDoorNumberbyMesh(msg.getDoorID());

        if ((targetBuilding.isDoorLocked(doorNumber) == true) && msg.getToggle() == 0x01) {
            msg.setUnk1(2);
            Dispatch dispatch = Dispatch.borrow(player, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
            return true; //don't open a locked door
        }

        if (msg.getToggle() == 0x01) {
            targetBuilding.setDoorState(doorNumber, DoorState.OPEN);
            targetBuilding.submitOpenDoorJob(msg.getDoorID());
        } else {
            targetBuilding.setDoorState(doorNumber, DoorState.CLOSED);
        }

        HashSet<AbstractWorldObject> container = WorldGrid.getObjectsInRangePartial(targetBuilding, MBServerStatics.CHARACTER_LOAD_RANGE,
                MBServerStatics.MASK_PLAYER);

        for (AbstractWorldObject awo : container) {
            PlayerCharacter playerCharacter = (PlayerCharacter)awo;
            Dispatch dispatch = Dispatch.borrow(playerCharacter, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
        }

        return true;
    }

}
