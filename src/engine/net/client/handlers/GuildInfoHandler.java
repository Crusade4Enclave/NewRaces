// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum.GameObjectType;
import engine.exception.MsgSendException;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.guild.GuildInfoMsg;
import engine.objects.PlayerCharacter;

public class GuildInfoHandler extends AbstractClientMsgHandler {

	public GuildInfoHandler() {
		super(GuildInfoMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		GuildInfoMsg msg = (GuildInfoMsg) baseMsg;
        Dispatch dispatch;

		// get source player
		PlayerCharacter sourcePlayer = SessionManager
				.getPlayerCharacter(origin);

		if (sourcePlayer == null)
			return true;
		
		if(msg.getMsgType() == 1) {
			dispatch = Dispatch.borrow(sourcePlayer, new GuildInfoMsg(sourcePlayer, sourcePlayer.getGuild(), 4));
            DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
		} else if(msg.getMsgType() == 5) {
			
			if(msg.getObjectType() == GameObjectType.PlayerCharacter.ordinal()) {
				PlayerCharacter pc = PlayerCharacter.getPlayerCharacter(msg.getObjectID());
                dispatch = Dispatch.borrow(sourcePlayer, new GuildInfoMsg(pc, pc.getGuild(), 5));
                DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
			} else {
				//TODO Change this to a null object when we make a null object.

                dispatch = Dispatch.borrow(sourcePlayer,new GuildInfoMsg(sourcePlayer, sourcePlayer.getGuild(), 1));
                DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
			}
		}
		
		// Send PromoteDemoteScreen info message response. 0x001D4DF6

		// Send guild member list? 0x6949C720
		// GuildList(source, origin);

		// send 0x3235E5EA? See what that is
		
		return true;
	}

}
