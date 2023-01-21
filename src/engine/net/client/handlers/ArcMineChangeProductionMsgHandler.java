package engine.net.client.handlers;

import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ArcMineChangeProductionMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.KeepAliveServerClientMsg;
import engine.objects.GuildStatusController;
import engine.objects.Mine;
import engine.objects.PlayerCharacter;
import engine.objects.Resource;

/*
 * @Author:
 * @Summary: Processes application protocol message which keeps
 * client's tcp connection open.
 */

public class ArcMineChangeProductionMsgHandler extends AbstractClientMsgHandler {

	public ArcMineChangeProductionMsgHandler() {
		super(ArcMineChangeProductionMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		
		PlayerCharacter playerCharacter = origin.getPlayerCharacter();
		ArcMineChangeProductionMsg changeProductionMsg = (ArcMineChangeProductionMsg) baseMsg;

		if (playerCharacter == null)
			return true;

		//TODO verify this against the warehouse?

		if (GuildStatusController.isInnerCouncil(playerCharacter.getGuildStatus()) == false) // is this only GL?
			return true;

		Mine mine = Mine.getMine(changeProductionMsg.getMineID());

		if (mine == null)
			return true;

		//make sure mine belongs to guild

		if (mine.getOwningGuild().isEmptyGuild() ||
			mine.getOwningGuild().getObjectUUID() != playerCharacter.getGuild().getObjectUUID())
			return true;

		//make sure valid resource

		Resource resource = Resource.resourceByHash.get(changeProductionMsg.getResourceHash());

		if (resource == null)
			return true;

		//update resource

		mine.changeProductionType(resource);
		Mine.setLastChange(System.currentTimeMillis());
		Dispatch dispatch = Dispatch.borrow(playerCharacter, changeProductionMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		return true;
	}

}