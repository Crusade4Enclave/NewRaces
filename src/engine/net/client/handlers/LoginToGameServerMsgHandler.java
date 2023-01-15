package engine.net.client.handlers;

import engine.Enum;
import engine.InterestManagement.WorldGrid;
import engine.exception.MsgSendException;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.LoginToGameServerMsg;
import engine.net.client.msg.login.LoginErrorMsg;
import engine.objects.Account;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import engine.session.CSSession;
import engine.session.Session;
import org.pmw.tinylog.Logger;

/*
 * @Author:
 * @Summary: Processes application protocol message which keeps
 * logs the character nto the game using the CSession key generated
 * by the Login server.
 */

public class LoginToGameServerMsgHandler extends AbstractClientMsgHandler {

	public LoginToGameServerMsgHandler() {
		super(LoginToGameServerMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		LoginToGameServerMsg msg = (LoginToGameServerMsg) baseMsg;

		CSSession sessionInfo = CSSession.getCrossServerSession(msg.getSecKey());

		if (sessionInfo == null) {
			Logger.error("Failed to validate session information from " + origin.getLocalAddressAndPortAsString());
			origin.kickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Unable to validate session data");
			// TODO Evaluate if we need to delete CSSessions here. We couldn't
			// find it before, why would this attempt be different?

			return true;
		}

		Account acc = sessionInfo.getAccount();

		if (acc == null) {
			String err = "Session returned NULL Account.  Conn:" + origin.getLocalAddressAndPortAsString();
			Logger.error(err);
			origin.kickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, err);
			return true;
		}

		PlayerCharacter pc = sessionInfo.getPlayerCharacter();

		if (pc == null) {
			String err = "Session returned NULL PlayerCharacter.  Conn:" + origin.getLocalAddressAndPortAsString();

			Logger.error(err);
			origin.kickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, err);
			return true;
		}

		// If account is suspended, kick

		if (acc.status.equals(Enum.AccountStatus.BANNED)) {
			origin.kickToLogin(MBServerStatics.LOGINERROR_NO_MORE_PLAYTIME_ON_ACCOUNT, "Account banned.");
			return true;
		}

		ClientConnection old = SessionManager.getClientConnection(acc);

		if (old != null)
			if (old != origin) {
				Logger.info("Disconnecting other client connection Using Same Account " + old.getRemoteAddressAndPortAsString());
				old.disconnect();
			}

		// Set machine ID here from CSS info
		origin.machineID = sessionInfo.getMachineID();

		// Send response
		msg.setSecKey("");

		if (!origin.sendMsg(msg)) {
			Logger.error("Failed to send message");
			origin.kickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Unable to send ValidateGameServer to client.");
			return true;
		}

		//# Why was this all changed?
		// CLEAN UP OTHER INSTANCES OF THIS CHARACTER

		Session toKill = SessionManager.getSession(sessionInfo.getPlayerCharacter());

		if (toKill != null) {
			if (toKill.getConn() != null) {
				LoginErrorMsg lom = new LoginErrorMsg(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "You may not login the same character twice!");
				ClientConnection conn = toKill.getConn();
				if (conn != null && !conn.sendMsg(lom))
					Logger.error("Failed to send message"); // TODO Do we just accept this failure to send Msg?
			}
			SessionManager.remSession(toKill);
			WorldGrid.RemoveWorldObject(sessionInfo.getPlayerCharacter());
		}
		Session s = SessionManager.getNewSession(sessionInfo.getAccount(), origin);
		SessionManager.setPlayerCharacter(s, sessionInfo.getPlayerCharacter());

		Logger.info("Login from Account: " + sessionInfo.getAccount().getUname() + " Character: " +
				     sessionInfo.getPlayerCharacter().getName() + " machineID: " + sessionInfo.getMachineID());

		// Log Admin Login Event

		if (pc.getAccount().status.equals(Enum.AccountStatus.ADMIN))
			DbManager.AccountQueries.WRITE_ADMIN_LOG(pc.getCombinedName(), "ADMIN LOGIN EVENT");
		DbManager.AccountQueries.SET_ACCOUNT_LOGIN(sessionInfo.getAccount(), sessionInfo.getPlayerCharacter().getName(), origin.getClientIpAddress(), sessionInfo.getMachineID());
		return true;
	}

}