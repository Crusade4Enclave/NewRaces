package engine.net.client.handlers;

import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.LockUnlockDoorMsg;
import engine.objects.Blueprint;
import engine.objects.Building;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

/*
 * @Author:
 * @Summary: Processes application protocol message which handle
 * lock and unlock door requests to and from the client.
 * 
 */

public class LockUnlockDoorMsgHandler extends AbstractClientMsgHandler {

    public LockUnlockDoorMsgHandler() {
        super(LockUnlockDoorMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

        // Member variable declarations
        
        PlayerCharacter player;
        Building targetBuilding;
        int doorNum;
        LockUnlockDoorMsg msg;

        // Member variable assignment
        
        msg = (LockUnlockDoorMsg) baseMsg;
        player = SessionManager.getPlayerCharacter(origin);
        targetBuilding = BuildingManager.getBuilding((int) msg.getTargetID());

        if (player == null || targetBuilding == null) {
            Logger.warn("Player or Building returned NULL in LockUnlock msg handling.");
            return true;
        }

        if (player.getLoc().distanceSquared2D(targetBuilding.getLoc()) > MBServerStatics.OPENCLOSEDOORDISTANCE * MBServerStatics.OPENCLOSEDOORDISTANCE) {
            return true;
        }

        if (!BuildingManager.playerCanManage(player, targetBuilding)) {
            return true;
        }

        doorNum = Blueprint.getDoorNumberbyMesh(msg.getDoorID());

        // Debugging code
        
        // Logger.debug("DoorLockUnlock", "Door mesh: " + msg.getDoorID() + " Door number: " + doorNum);
        
        boolean stateChanged;

        if (targetBuilding.isDoorLocked(doorNum)) {
            stateChanged = targetBuilding.setDoorState(doorNum, engine.Enum.DoorState.UNLOCKED);
        } else {
            stateChanged = targetBuilding.setDoorState(doorNum, engine.Enum.DoorState.LOCKED);
        }

        if (stateChanged == false) {
            Logger.error("WorldServerMsgHandler.LockUnlockDoor", "Failed to update db for building: " + targetBuilding.getObjectUUID() + ", door: " + msg.getDoorID());
        }

        if (targetBuilding.isDoorLocked(doorNum)) {
            msg.setUnk1(1); // Which is this, locked or unlocaked?
        } else {
            msg.setUnk1(0);
        }

        Dispatch dispatch = Dispatch.borrow(player, msg);
        DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

        return true;
    }
}
