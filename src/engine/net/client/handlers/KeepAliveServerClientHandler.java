package engine.net.client.handlers;

import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.KeepAliveServerClientMsg;
import engine.objects.PlayerCharacter;

/*
 * @Author:
 * @Summary: Processes application protocol message which keeps
 * client's tcp connection open.
 */

public class KeepAliveServerClientHandler extends AbstractClientMsgHandler {

	public KeepAliveServerClientHandler() {
		super(KeepAliveServerClientMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		
		PlayerCharacter pc = origin.getPlayerCharacter();
		
	
	
            // Member variable declaration
            
            KeepAliveServerClientMsg msg;
            
            // Member variable assignment
            
            msg = (KeepAliveServerClientMsg) baseMsg;
            
        
            // Send ping to client
            
            Dispatch dispatch = Dispatch.borrow(pc, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
            
            return true;
	}

}