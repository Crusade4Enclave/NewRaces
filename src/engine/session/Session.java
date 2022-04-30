// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.session;

import engine.Enum;
import engine.gameManager.DbManager;
import engine.net.client.ClientConnection;
import engine.objects.Account;
import engine.objects.PlayerCharacter;


public class Session {

	private SessionID sessionID;
	private PlayerCharacter playerCharacter;
	private Account account;
	private ClientConnection conn;

	public Session(SessionID sessionID, Account acc, ClientConnection conn) {
		super();
		this.sessionID = sessionID;
		this.playerCharacter = null;
		this.account = acc;
		this.conn = conn;
	}

	public PlayerCharacter getPlayerCharacter() {
			if (this.playerCharacter != null) {

			if (DbManager.inCache(Enum.GameObjectType.PlayerCharacter, this.playerCharacter.getObjectUUID()))
				this.playerCharacter = (PlayerCharacter) DbManager.getFromCache(Enum.GameObjectType.PlayerCharacter, this.playerCharacter.getObjectUUID());
		}
		return this.playerCharacter;
	}

	public void setPlayerCharacter(PlayerCharacter pc) {
		this.playerCharacter = pc;
	}

	public SessionID getSessionID() {
		return this.sessionID;
	}

	public void setSessionID(SessionID sessionID) {
		this.sessionID = sessionID;
	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account acc) {
		this.account = acc;
	}

	public ClientConnection getConn() {
		return this.conn;
	}

	public void setConn(ClientConnection conn) {
		this.conn = conn;
	}

}
