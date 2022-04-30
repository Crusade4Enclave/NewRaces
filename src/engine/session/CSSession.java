// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.session;

import engine.gameManager.DbManager;
import engine.objects.AbstractGameObject;
import engine.objects.Account;
import engine.objects.PlayerCharacter;

import java.net.InetAddress;


public class CSSession extends AbstractGameObject {

	private String sessionID;
	private PlayerCharacter playerCharacter;
	private Account account;


	private String machineID;

	public CSSession(String sessionID, Account acc, PlayerCharacter pc, String machineID) {
		super();
		this.sessionID = sessionID;
		this.playerCharacter = pc;
		this.account = acc;
		this.machineID = machineID;

		if (this.playerCharacter != null)
			PlayerCharacter.initializePlayer(this.playerCharacter);
	}

	public PlayerCharacter getPlayerCharacter() {
		return this.playerCharacter;
	}

	public void setPlayerCharacter(PlayerCharacter pc) {
		this.playerCharacter = pc;
	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account acc) {
		this.account = acc;
	}

	@Override
	public void removeFromCache() {}

	/*
	 * Database
	 */
	public static boolean addCrossServerSession(String secKey, Account acc, InetAddress inet, String machineID) {
		return DbManager.CSSessionQueries.ADD_CSSESSION(secKey, acc, inet, machineID);
		//		PreparedStatementShared ps = null;
		//		try {
		//			ps = prepareStatement("INSERT INTO sessions (secretKey, accountID, vbID, sessionIP) VALUES (?,?,?,INET_ATON(?))");
		//			ps.setString(1, secKey);
		//			ps.setInt(2, acc.getUUID(), true);
		//			ps.setInt(3, acc.getVBID());
		//			ps.setString(4, StringUtils.InetAddressToClientString(inet));
		//			if (ps.executeUpdate() > 0)
		//				return true;
		//		} catch (SQLException e) {
		//			Logger.error("CSSession", "Failed to create cross server session");
		//		} finally {
		//			ps.release();
		//		}
		//		return false;
	}

	public static boolean deleteCrossServerSession(String secKey) {
		return DbManager.CSSessionQueries.DELETE_CSSESSION(secKey);
		//		PreparedStatementShared ps = null;
		//		try {
		//			ps = prepareStatement("DELETE FROM sessions WHERE secretKey = ?");
		//			ps.setString(1, secKey);
		//			if (ps.executeUpdate() > 0)
		//				return true;
		//		} catch (SQLException e) {
		//			Logger.error("CSSession", "Failed to delete cross server session");
		//		} finally {
		//			ps.release();
		//		}
		//		return false;
	}

	public static boolean updateCrossServerSession(String secKey, int charID) {
		return DbManager.CSSessionQueries.UPDATE_CSSESSION(secKey, charID);
	}

	public static CSSession getCrossServerSession(String secKey) {

		CSSession sessionInfo;

		try {
			sessionInfo =  DbManager.CSSessionQueries.GET_CSSESSION(secKey);
		} catch (Exception e) {
			sessionInfo = null;
		}

		return  sessionInfo;
	}

	public String getMachineID() {
		return machineID;
	}

	@Override
	public void updateDatabase() {
		// TODO Auto-generated method stub

	}
}
