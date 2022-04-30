package engine.net.client.handlers;

import engine.exception.MsgSendException;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;

/* @Summary: This is the abstract class from which all message handlers
 *           for mainline application protocol derive.  Namely those
 *           routed and executed via ClientMessageHandler.
 */

public abstract class AbstractClientMsgHandler {
	private final Class<? extends ClientNetMsg> handler;
	
	public AbstractClientMsgHandler(Class<? extends ClientNetMsg> handler) {
		this.handler = handler;
	}
	
	public boolean handleNetMsg(ClientNetMsg msg) {
            
            boolean executionSucceded;
                
		try {
			executionSucceded = _handleNetMsg(msg, (ClientConnection) msg.getOrigin());
		} catch (MsgSendException e) {
			e.printStackTrace();
			executionSucceded = false;
		}
		
		return executionSucceded;
	}
        
protected abstract boolean _handleNetMsg(ClientNetMsg msg, ClientConnection origin) throws MsgSendException;}
