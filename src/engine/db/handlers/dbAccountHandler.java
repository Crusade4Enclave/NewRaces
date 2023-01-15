// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.gameManager.ConfigManager;
import engine.gameManager.DbManager;
import engine.objects.Account;
import engine.objects.PlayerCharacter;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class dbAccountHandler extends dbHandlerBase {

	public dbAccountHandler() {
		this.localClass = Account.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public Account GET_ACCOUNT(int id) {
		if (id == 0)
			return null;
		Account account = (Account) DbManager.getFromCache(GameObjectType.Account, id);
		if (account != null)
			return account;

		prepareCallable("SELECT * FROM `obj_account` WHERE `UID`=?");
		setLong(1, (long) id);

		Account ac = null;
		ac = (Account) getObjectSingle(id);

		if (ac != null)
			ac.runAfterLoad();

		return ac;
	}

	public void WRITE_ADMIN_LOG(String adminName, String logEntry) {

		prepareCallable("INSERT INTO dyn_admin_log(`DateTime`, `Account`, `Event`)"
				+ " VALUES (?, ?, ?)");
		setTimeStamp(1, System.currentTimeMillis());
		setString(2, adminName);
		setString(3, logEntry);
		executeUpdate();

	}

	public void SET_TRASH(String machineID) {

		prepareCallable("INSERT INTO dyn_trash(`machineID`, `count`)"
				+ " VALUES (?, 1) ON DUPLICATE KEY UPDATE `count` = `count` + 1;");

		setTimeStamp(4, System.currentTimeMillis());
		setString(1, machineID);
		executeUpdate();

	}

	public ArrayList<String> GET_TRASH_LIST() {

		ArrayList<String> machineList = new ArrayList<>();

		prepareCallable("select `machineID` from `dyn_trash`");

		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {
				machineList.add(rs.getString(1));
			}
		} catch (SQLException e) {
			Logger.error( e);
		} finally {
			closeCallable();
		}

		return machineList;
	}

	public boolean DELETE_VAULT_FOR_ACCOUNT(final int accountID) {
		prepareCallable("DELETE FROM `object` WHERE `parent`=? && `type`='item'");
		setLong(1, (long) accountID);
		return (executeUpdate() > 0);
	}

	public ArrayList<PlayerCharacter> GET_ALL_CHARS_FOR_MACHINE(String machineID) {

		ArrayList<PlayerCharacter> trashList = new ArrayList<>();

		prepareCallable("select DISTINCT UID from object \n" +
				"where parent IN (select AccountID from dyn_login_history " +
		        " WHERE`machineID`=?)");
		setString(1, machineID);

		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {

				PlayerCharacter trashPlayer;
				int playerID;

				playerID = rs.getInt(1);
				trashPlayer = PlayerCharacter.getPlayerCharacter(playerID);

				if (trashPlayer == null)
					continue;;

				if (trashPlayer.isDeleted() == false)
					trashList.add(trashPlayer);
			}
		} catch (SQLException e) {
			Logger.error( e);
		} finally {
			closeCallable();
		}
		return  trashList;
	}

	public void CLEAR_TRASH_TABLE() {
		prepareCallable("DELETE FROM dyn_trash");
		executeUpdate();
	}

	public void CREATE_SINGLE(String accountName, String password) {

		prepareCallable("CALL singleAccountCreate(?,?)");
		setString(1, accountName);
		setString(2, password);
		executeUpdate();
	}

	public Account GET_ACCOUNT(String uname) {

		if (Account.AccountsMap.get(uname) != null)
			return this.GET_ACCOUNT(Account.AccountsMap.get(uname));

		prepareCallable("SELECT * FROM `obj_account` WHERE `acct_uname`=?");
		setString(1, uname);
		ArrayList<Account> temp = getObjectList();

		if (temp.isEmpty())
			return null;

		if (temp.get(0) != null){
			temp.get(0).runAfterLoad();

			if (ConfigManager.serverType.equals(Enum.ServerType.LOGINSERVER))
				Account.AccountsMap.put(uname, temp.get(0).getObjectUUID());

		}
		return temp.get(0);
	}

	public void SET_ACCOUNT_LOGIN(final Account acc, String playerName, final String ip, final String machineID) {

		if (acc.getObjectUUID() == 0 || ip == null || ip.length() == 0)
			return;

		prepareCallable("INSERT INTO dyn_login_history(`AccountID`, `accountName`, `characterName`, `ip`, `machineID`, `timeStamp`)"
				+ " VALUES (?, ?, ?, ?, ?, ?)");

		setInt(1, acc.getObjectUUID());
		setString(2, acc.getUname());
		setString(3, playerName);
		setString(4, ip);
		setString(5, machineID);
		setTimeStamp(6, System.currentTimeMillis());
		executeUpdate();
	}

	public String SET_PROPERTY(final Account a, String name, Object new_value) {
		prepareCallable("CALL account_SETPROP(?,?,?)");
		setLong(1, (long) a.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		return getResult();
	}

	public String SET_PROPERTY(final Account a, String name, Object new_value, Object old_value) {
		prepareCallable("CALL account_GETSETPROP(?,?,?,?)");
		setLong(1, (long) a.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		setString(4, String.valueOf(old_value));
		return getResult();
	}


	public void updateDatabase(final Account acc) {
		prepareCallable("UPDATE `obj_account` SET `acct_passwd`=?, "
				+ " `acct_lastCharUID`=?, `acct_salt`=?, `discordAccount`=?, " +
				" status = ? WHERE `UID`=?");

		setString(1, acc.getPasswd());
		setInt(2, acc.getLastCharIDUsed());
		setString(3, acc.getSalt());
		setString(4, acc.discordAccount);
		setString(5, acc.status.name());
		setInt(6, acc.getObjectUUID());
		executeUpdate();
	}

	public void INVALIDATE_LOGIN_CACHE(long accountUID, String objectType) {
		prepareCallable("INSERT IGNORE INTO login_cachelist (`UID`, `type`) VALUES(?,?);");
		setLong(1, accountUID);
		setString(2, objectType);
		executeUpdate();
	}

}
