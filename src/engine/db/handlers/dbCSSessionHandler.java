// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.gameManager.DbManager;
import engine.objects.Account;
import engine.objects.PlayerCharacter;
import engine.session.CSSession;
import engine.util.StringUtils;
import org.pmw.tinylog.Logger;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;

public class dbCSSessionHandler extends dbHandlerBase {

	public dbCSSessionHandler() {
		this.localClass = CSSession.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public boolean ADD_CSSESSION(String secKey, Account acc, InetAddress inet, String machineID) {
        prepareCallable("INSERT INTO `dyn_session` (`secretKey`, `accountID`, `discordAccount`, `sessionIP`, machineID) VALUES (?,?,?,INET_ATON(?),?)");
        setString(1, secKey);
		setLong(2, acc.getObjectUUID());
		setString(3, acc.discordAccount);
        setString(4, StringUtils.InetAddressToClientString(inet));
        setString(5, machineID);
        return (executeUpdate() != 0);
    }
	// This method returns population metrics from the database

	public String GET_POPULATION_STRING() {

		String outString = null;

		// Set up call to stored procedure
		prepareCallable("CALL GET_POPULATION_STRING()");

		try {

			// Evaluate database ordinal and return enum
			outString = getString("popstring");

		} catch (Exception e) {
			Logger.error( "Failure in stored procedure:" + e.getMessage());
		} finally {
			closeCallable();
		}
		return outString;
	}

	public boolean DELETE_UNUSED_CSSESSION(String secKey) {
		prepareCallable("DELETE FROM `dyn_session` WHERE `secretKey`=? && `characterID` IS NULL");
		setString(1, secKey);
		return (executeUpdate() != 0);
	}

	public boolean DELETE_CSSESSION(String secKey) {
		prepareCallable("DELETE FROM `dyn_session` WHERE `secretKey`=?");
		setString(1, secKey);
		return (executeUpdate() != 0);
	}

	public boolean UPDATE_CSSESSION(String secKey, int charID) {
		prepareCallable("UPDATE `dyn_session` SET `characterID`=? WHERE `secretKey`=?");
		setInt(1, charID);
		setString(2, secKey);
		return (executeUpdate() != 0);
	}

	public CSSession GET_CSSESSION(String secKey) {
		CSSession css = null;
		prepareCallable("SELECT `accountID`, `characterID`, `machineID` FROM `dyn_session` WHERE `secretKey`=?");
		setString(1, secKey);
		try {

			ResultSet rs = executeQuery();

			if (rs.next()) {
				css = new CSSession(secKey, DbManager.AccountQueries.GET_ACCOUNT(rs.getInt("accountID")), PlayerCharacter.getPlayerCharacter(rs
						.getInt("characterID")), getString("machineID"));
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error("Error with seckey: " + secKey);
		} finally {
			closeCallable();
		}
		return css;
	}
}
