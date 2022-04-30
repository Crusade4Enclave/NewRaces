// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.net;

import engine.gameManager.ConfigManager;
import engine.job.AbstractJob;
import engine.net.client.msg.ClientNetMsg;
import org.pmw.tinylog.Logger;

public class CheckNetMsgFactoryJob extends AbstractJob {

	private final AbstractConnection conn;

	public CheckNetMsgFactoryJob(AbstractConnection conn) {
		super();
		this.conn = conn;
	}

	@Override
	protected void doJob() {
		NetMsgFactory factory = conn.getFactory();

		// Make any/all msg possible
		factory.parseBuffer();
	
		// get and route.
		AbstractNetMsg msg = factory.getMsg();
		while (msg != null) {
			
			// Conditionally check to see if origin is set.
			if (msg.getOrigin() == null) {
				Logger.warn(msg.getClass().getSimpleName() + " had a NULL for its 'origin'.");
				msg.setOrigin(this.conn);
			}

			 if (msg instanceof engine.net.client.msg.ClientNetMsg) {
				ConfigManager.handler.handleClientMsg((ClientNetMsg) msg);

			} else {
				Logger.error("Unrouteable message of type '" + msg.getClass().getSimpleName() + '\'');
			}

			msg = factory.getMsg();
		}
	}

}
