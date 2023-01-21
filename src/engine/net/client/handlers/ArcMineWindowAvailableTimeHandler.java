package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ArcMineWindowAvailableTimeMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.KeepAliveServerClientMsg;
import engine.objects.Building;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

/*
 * @Author:
 * @Summary: Processes application protocol message which keeps
 * client's tcp connection open.
 */

public class ArcMineWindowAvailableTimeHandler extends AbstractClientMsgHandler {

    public ArcMineWindowAvailableTimeHandler() {
        super(KeepAliveServerClientMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		ArcMineWindowAvailableTimeMsg msg = (ArcMineWindowAvailableTimeMsg) baseMsg;
        PlayerCharacter playerCharacter = origin.getPlayerCharacter();
        Building treeOfLife = BuildingManager.getBuildingFromCache(msg.getBuildingUUID());
        Dispatch dispatch;

        if (treeOfLife == null)
            return true;

        if (treeOfLife.getBlueprintUUID() == 0)
            return true;

        if (treeOfLife.getBlueprint().getBuildingGroup() != Enum.BuildingGroup.TOL)
            return true;

        if (playerCharacter == null)
            return true;

        if (!Guild.sameGuild(treeOfLife.getGuild(), playerCharacter.getGuild()))
            return true;

        if (GuildStatusController.isInnerCouncil(playerCharacter.getGuildStatus()) == false) // is this only GL?
            return true;

        ArcMineWindowAvailableTimeMsg outMsg = new ArcMineWindowAvailableTimeMsg(treeOfLife, 10);
        outMsg.configure();
        dispatch = Dispatch.borrow(playerCharacter, outMsg);
        DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
        return true;

    }
}