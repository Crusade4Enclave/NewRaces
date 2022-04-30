// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.net.client.ClientConnection;
import engine.objects.Account;
import engine.objects.Guild;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import engine.session.CSSession;
import engine.session.Session;
import engine.session.SessionID;
import engine.util.ByteUtils;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public enum SessionManager {

	SESSIONMANAGER;

	// TODO add session activity timestamping & timeout monitors

	private static ConcurrentHashMap<SessionID, Session> sessionIDtoSession = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_HIGH);
	private static ConcurrentHashMap<PlayerCharacter, Session> pcToSession = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_HIGH);
	private static ConcurrentHashMap<Account, Session> accountToSession = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_HIGH);
	private static ConcurrentHashMap<ClientConnection, Session> connToSession = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_HIGH);
	public static int _maxPopulation = 0;

	// 0 = login server
	// 1 = gateway server
	// 2 = all other servers
	private static int crossServerBehavior = 2;

	public static Session getNewSession(SessionID sesID, Account a, ClientConnection c) {
		Session ses = new Session(sesID, a, c);

		SessionManager.sessionIDtoSession.put(sesID, ses);
		SessionManager.accountToSession.put(a, ses);
		SessionManager.connToSession.put(c, ses);

		if (crossServerBehavior == 0)
			if (!CSSession.addCrossServerSession(ByteUtils.byteArrayToSafeStringHex(c.getSecretKeyBytes()), a, c.getSocketChannel()
					.socket().getInetAddress(), c.machineID))
				Logger.warn("Failed to create cross server session: " + a.getUname());

		return ses;
	}

	public static Session getNewSession(Account a, ClientConnection c) {
		SessionID sesID = c.getSessionID();
		return SessionManager.getNewSession(sesID, a, c);
	}


	public static void cSessionCleanup(String key) {
		if (!CSSession.deleteCrossServerSession(key))
			Logger.warn(
				"Failed to remove cross server session for key: " + key);
	}

	public static void remSession(Session s) {

		if (s == null) {
			return;
		}

		SessionManager.remSessionID(s);
		SessionManager.remAccount(s);
		SessionManager.remClientConnection(s);
		SessionManager.remPlayerCharacter(s);

		//TODO LATER fix
		s.setAccount(null);
		s.setConn(null);
		s.setPlayerCharacter(null);
		s.setSessionID(null);
	}

	/*
	 * Get Sessions
	 */
	public static Session getSession(SessionID id) {
		return SessionManager.sessionIDtoSession.get(id);
	}

	public static Session getSession(PlayerCharacter pc) {
		return SessionManager.pcToSession.get(pc);
	}

	public  static Session getSession(Account a) {
		return SessionManager.accountToSession.get(a);
	}

	public  static Session getSession(ClientConnection cc) {
		return SessionManager.connToSession.get(cc);
	}

	/*
	 * Get Connections
	 */
	public static  ClientConnection getClientConnection(SessionID id) {
		Session s = SessionManager.getSession(id);
		return (s == null) ? null : s.getConn();
	}

	public static  ClientConnection getClientConnection(PlayerCharacter pc) {
		Session s = SessionManager.getSession(pc);
		return (s == null) ? null : s.getConn();
	}

	public static  ClientConnection getClientConnection(Account a) {
		Session s = SessionManager.getSession(a);
		return (s == null) ? null : s.getConn();
	}

	/*
	 * Get PlayerCharacter
	 */
	public static PlayerCharacter getPlayerCharacter(SessionID id) {
		Session s = SessionManager.getSession(id);
		return (s == null) ? null : s.getPlayerCharacter();
	}

	public static PlayerCharacter getPlayerCharacter(ClientConnection conn) {
		Session s = SessionManager.getSession(conn);
		return (s == null) ? null : s.getPlayerCharacter();
	}

	public static PlayerCharacter getPlayerCharacter(Account a) {
		Session s = SessionManager.getSession(a);
		return (s == null) ? null : s.getPlayerCharacter();
	}

	/*
	 * Get Account
	 */
	public static Account getAccount(SessionID id) {
		Session s = SessionManager.getSession(id);
		return (s == null) ? null : s.getAccount();
	}

	public static Account getAccount(ClientConnection conn) {
		Session s = SessionManager.getSession(conn);
		return (s == null) ? null : s.getAccount();
	}

	public static Account getAccount(PlayerCharacter pc) {
		Session s = SessionManager.getSession(pc);
		return (s == null) ? null : s.getAccount();
	}

	public static void setPlayerCharacter(Session s, PlayerCharacter pc) {
		SessionManager.pcToSession.put(pc, s);
		s.setPlayerCharacter(pc);
                
		// Update max player
		SessionManager._maxPopulation = Math.max(_maxPopulation, SessionManager.pcToSession.size());
                
	}

	public static void remPlayerCharacter(Session s) {
		if (s.getPlayerCharacter() != null) {
			SessionManager.pcToSession.remove(s.getPlayerCharacter());
			s.setPlayerCharacter(null);
		}
	}

	protected static void remAccount(Session s) {
		if (s.getAccount() != null) {
			SessionManager.accountToSession.remove(s.getAccount());
			s.setAccount(null);
		}
	}

	protected static void remSessionID(Session s) {

		if (s.getSessionID() != null) {
			SessionManager.sessionIDtoSession.remove(s.getSessionID());
			s.setSessionID(null);
		}
	}

	protected static void remClientConnection(Session s) {
		if (s.getConn() != null) {
			SessionManager.connToSession.remove(s.getConn());
			s.setConn(null);
		}
	}



	/*
	 * Utils
	 */

	public static void setCrossServerBehavior(int type) {
		crossServerBehavior = type;
	}

	/**
	 *
	 * @return a new HashSet<ClientConnection> object so the caller cannot
	 *         modify the internal Set
	 */
	public static Collection<ClientConnection> getAllActiveClientConnections() {
			return SessionManager.connToSession.keySet();
	}

	/**
	 *
	 * @return a new HashSet<PlayerCharacter> object so the caller cannot modify
	 *         the internal Set
	 */
	public static Collection<PlayerCharacter> getAllActivePlayerCharacters() {
	
			return SessionManager.pcToSession.keySet();
	}

	public static Collection<PlayerCharacter> getAllActivePlayers() {
		
			return SessionManager.pcToSession.keySet();
	}

	public static int getActivePlayerCharacterCount() {

			return SessionManager.pcToSession.keySet().size();
	}

	public static ArrayList<PlayerCharacter> getActivePCsInGuildID(int id) {
		ArrayList<PlayerCharacter> pcs = new ArrayList<>();

		for (PlayerCharacter pc : SessionManager.getAllActivePlayerCharacters()) {
			Guild g = pc.getGuild();
			if (g != null && g.getObjectUUID() == id) {
				pcs.add(pc);
			}
		}

		return pcs;
	}

	public static PlayerCharacter getPlayerCharacterByLowerCaseName(String name) {

		String queryName = name.toLowerCase();

		for (PlayerCharacter playerCharacter : SessionManager.getAllActivePlayerCharacters()) {

			if ((playerCharacter.getFirstName().toLowerCase()).equals(queryName)) {
				return playerCharacter;
			}
		}
		return null;
	}

	public static PlayerCharacter getPlayerCharacterByID(int UUID) {

		for (PlayerCharacter playerCharacter : SessionManager.getAllActivePlayerCharacters()) {

			if (playerCharacter.getObjectUUID() == UUID) {
				return playerCharacter;
			}
		}
		return null;
	}

	public static Collection<Account> getAllActiveAccounts() {
			return SessionManager.accountToSession.keySet();
	}

	public static Account getAccountByID(int UUID) {

		for (Account acc :  SessionManager.getAllActiveAccounts()) {

			if (acc.getObjectUUID() == UUID)
				return acc;

		}
		return null;
	}
}
