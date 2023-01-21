package engine.net.client.handlers;

import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ArcOwnedMinesListMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.GuildStatusController;
import engine.objects.Mine;
import engine.objects.PlayerCharacter;

/*
 * @Author:
 * @Summary: Processes application protocol message which keeps
 * client's tcp connection open.
 */

public class ArcOwnedMinesListHandler extends AbstractClientMsgHandler {

    public ArcOwnedMinesListHandler() {
        super(ArcOwnedMinesListMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

        PlayerCharacter playerCharacter = origin.getPlayerCharacter();
        ArcOwnedMinesListMsg msg = (ArcOwnedMinesListMsg) baseMsg;

        if (playerCharacter == null)
            return true;

        if (GuildStatusController.isInnerCouncil(playerCharacter.getGuildStatus()) == false)// is this only GL?
            return true;

        msg.setMineList(Mine.getMinesForGuild(playerCharacter.getGuild().getObjectUUID()));
        Dispatch dispatch = Dispatch.borrow(playerCharacter, msg);
        DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

        return true;
    }

}