package engine.net.client.handlers;

import engine.Enum;
import engine.exception.MsgSendException;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ArcViewAssetTransactionsMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.PlayerCharacter;
import engine.objects.Warehouse;

/*
 * @Author:
 * @Summary: Processes application protocol message which transfers
 * gold between a building's strongbox and a player character.
 */

public class ArcViewAssetTransactionsMsgHandler extends AbstractClientMsgHandler {

    public ArcViewAssetTransactionsMsgHandler() {
        super(ArcViewAssetTransactionsMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

        PlayerCharacter player;
        ArcViewAssetTransactionsMsg msg;
        ArcViewAssetTransactionsMsg newMsg;
        player = SessionManager.getPlayerCharacter(origin);
        Dispatch dispatch;

        if (player == null)
            return true;

        msg = (ArcViewAssetTransactionsMsg) baseMsg;

        Warehouse warehouse = Warehouse.warehouseByBuildingUUID.get(msg.getWarehouseID());

        if (warehouse == null)
        	return true;
        
        newMsg = new ArcViewAssetTransactionsMsg(warehouse,msg);
        newMsg.configure();

        dispatch = Dispatch.borrow(player, newMsg);
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

        return true;
    }

}
