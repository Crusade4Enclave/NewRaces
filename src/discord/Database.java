// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package discord;

import engine.Enum;
import engine.gameManager.ConfigManager;
import org.pmw.tinylog.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Database {

    public static Boolean online;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            Logger.error(e.toString());
            online = false;
        }
    }

    // Load and instance the JDBC Driver

    public String sqlURI;

    public void configureDatabase() {

        // Build connection string from JSON object.

        sqlURI = "jdbc:mysql://";
        sqlURI += ConfigManager.MB_DATABASE_ADDRESS.getValue() + ':' + ConfigManager.MB_DATABASE_PORT.getValue();
        sqlURI += '/' + ConfigManager.MB_DATABASE_NAME.getValue() + '?';
        sqlURI += "useServerPrepStmts=true";
        sqlURI += "&cachePrepStmts=false";
        sqlURI += "&cacheCallableStmts=true";
        sqlURI += "&characterEncoding=utf8";

        online = true;
    }

    public boolean updateAccountPassword(String discordAccountID, String newPassword) {

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            CallableStatement updatePassword = connection.prepareCall("call discordUpdatePassword(?, ?)");

            updatePassword.setString(1, discordAccountID);
            updatePassword.setString(2, newPassword);

            updatePassword.executeUpdate();
            updatePassword.close();
            return true;

        } catch (SQLException e) {
            Logger.error(e.toString());
            online = false;
            return false;
        }
    }

    public boolean updateAccountStatus(String discordAccountID, Enum.AccountStatus accountStatus) {

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            PreparedStatement updateAccountStatus = connection.prepareCall("update obj_account set `status` = ? where `discordAccount` = ?");

            updateAccountStatus.setString(1, accountStatus.name());
            updateAccountStatus.setString(2, discordAccountID);

            updateAccountStatus.executeUpdate();
            updateAccountStatus.close();
            return true;

        } catch (SQLException e) {
            Logger.error(e.toString());
            online = false;
            return false;
        }
    }

    public boolean registerDiscordAccount(String discordAccountID, String discordUserName, String discordPassword) {

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            CallableStatement registerAccount = connection.prepareCall("call discordAccountRegister(?, ?, ?)");

            registerAccount.setString(1, discordAccountID);
            registerAccount.setString(2, discordUserName);
            registerAccount.setString(3, discordPassword);

            registerAccount.execute();
            registerAccount.close();
            return true;

        } catch (SQLException e) {
            Logger.error(e.toString());
            online = false;
            return false;
        }
    }

    public List<DiscordAccount> getDiscordAccounts(String discordAccountID) {

        DiscordAccount discordAccount;
        List<DiscordAccount> discordAccounts = new ArrayList<>();

        String queryString = "SELECT * FROM obj_account where discordAccount = ?";

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            // Discord account name based lookup

            PreparedStatement accountQuery = connection.prepareStatement(queryString);
            accountQuery.setString(1, discordAccountID);

            ResultSet rs = accountQuery.executeQuery();

            while (rs.next()) {
                discordAccount = new DiscordAccount();
                discordAccount.discordAccount = rs.getString("discordAccount");
                discordAccount.gameAccountName = rs.getString("acct_uname");
                discordAccount.status = Enum.AccountStatus.valueOf(rs.getString("status"));
                discordAccount.isDiscordAdmin = rs.getByte("discordAdmin");                // Registration date cannot be null

                Timestamp registrationDate = rs.getTimestamp("registrationDate");
                discordAccount.registrationDate = registrationDate.toLocalDateTime();

                // Load last Update Request datetime

                Timestamp lastUpdateRequest = rs.getTimestamp("lastUpdateRequest");

                if (lastUpdateRequest != null)
                    discordAccount.lastUpdateRequest = lastUpdateRequest.toLocalDateTime();
                else
                    discordAccount.lastUpdateRequest = null;

                discordAccounts.add(discordAccount);
            }

        } catch (SQLException e) {
            Logger.error(e.toString());
            online = false;
        }

        return discordAccounts;
    }

    public String getTrashDetail() {

        String outString = "accountName characterName machineID ip count\n";
        outString += "---------------------------------------------\n";
        String queryString = "SELECT * FROM dyn_trash_detail;";

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            // Discord account name based lookup

            PreparedStatement trashQuery = connection.prepareStatement(queryString);

            ResultSet rs = trashQuery.executeQuery();

            while (rs.next()) {
                outString += rs.getString("accountName") + "   ";
                outString += rs.getString("characterName") + "   ";
                outString += rs.getString("machineID") + "   ";
                outString += rs.getString("ip") + "   ";
                outString += rs.getInt("count") + "\n";
            }
        } catch (SQLException e) {
            Logger.error(e.toString());

            online = false;
        }
        return outString;
    }

    public String getTrashList() {

        String outString = "";
        String queryString = "SELECT DISTINCT `characterName` FROM dyn_trash_detail;";
        int counter = 0;

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            // Discord account name based lookup

            PreparedStatement trashQuery = connection.prepareStatement(queryString);

            ResultSet rs = trashQuery.executeQuery();

            while (rs.next()) {
                outString += rs.getString("characterName");
                counter++;

                if (counter > 2) {
                    outString += "\n";
                    counter = 0;
                } else
                    outString += "     ";

            }
        } catch (SQLException e) {
            Logger.error(e.toString());

            online = false;
        }

        if (outString.length() > 1500)
            return outString.substring(0, 1500);
        else
            return outString;
    }

    public int getTrashCount() {

        int trashCount = 0;

        String queryString = "SELECT count(distinct characterName) FROM dyn_trash_detail;";

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            // Discord account name based lookup

            PreparedStatement trashQuery = connection.prepareStatement(queryString);

            ResultSet rs = trashQuery.executeQuery();

            while (rs.next()) {
                trashCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.error(e.toString());

            online = false;
        }

        return trashCount;
    }

    public void setAdminEventAsRead(int adminEvent) {

        String queryString = "UPDATE dyn_admin_log SET `SentFlag` = 1 WHERE `entry` = ? ";

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            PreparedStatement updateAdminEvent = connection.prepareCall(queryString);

            updateAdminEvent.setInt(1, adminEvent);

            updateAdminEvent.executeUpdate();
            updateAdminEvent.close();
            return;

        } catch (SQLException e) {
            Logger.error(e.toString());
            online = false;
            return;
        }

    }

    public HashMap<Integer, String> getAdminEvents() {

        HashMap<Integer, String> outMap = new HashMap<>();
        String queryString = "SELECT * from dyn_admin_log where `SentFlag` = 0";

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            // Discord Admin Log lookup of unreported events

            PreparedStatement adminLogQuery = connection.prepareStatement(queryString);
            ResultSet rs = adminLogQuery.executeQuery();
            String workString;

            while (rs.next()) {
                workString = "___Admin Event___\n" +
                        "Character: " + rs.getString("charName") + "\n" +
                        "Event: " + rs.getString("eventString");

                outMap.put(rs.getInt("entry"), workString);
            }
        } catch (SQLException e) {
            Logger.error(e.toString());
        }

        return outMap;
    }

    public String getTrashFile() {

        String outString = "machineID : count\n";
        String queryString = "SELECT * FROM dyn_trash;";

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            // Discord account name based lookup

            PreparedStatement trashQuery = connection.prepareStatement(queryString);

            ResultSet rs = trashQuery.executeQuery();

            while (rs.next()) {
                outString += rs.getString("machineID") + " : ";
                outString += rs.getInt("count") + "\n";
            }
        } catch (SQLException e) {
            Logger.error(e.toString());

            online = false;
        }
        return outString;
    }

    public List<DiscordAccount> getAccountsByDiscordName(String accountName, Boolean exact) {

        DiscordAccount discordAccount;
        List<DiscordAccount> discordAccounts = new ArrayList<>();
        String searchString;
        String queryString;

        if (exact.equals(true))
            searchString = accountName + "#%";
        else
            searchString = accountName + "%#%";

        queryString = "SELECT * FROM obj_account where `acct_uname` LIKE ?";

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            // Discord account name based lookup

            PreparedStatement nameQuery = connection.prepareStatement(queryString);
            nameQuery.setString(1, searchString);

            ResultSet rs = nameQuery.executeQuery();

            while (rs.next()) {
                discordAccount = new DiscordAccount();
                discordAccount.discordAccount = rs.getString("discordAccount");
                discordAccount.gameAccountName = rs.getString("acct_uname");
                discordAccount.status = Enum.AccountStatus.valueOf(rs.getString("status"));

                // Registration date cannot be null

                Timestamp registrationDate = rs.getTimestamp("registrationDate");
                discordAccount.registrationDate = registrationDate.toLocalDateTime();

                // Load last Update Request datetime

                Timestamp lastUpdateRequest = rs.getTimestamp("lastUpdateRequest");

                if (lastUpdateRequest != null)
                    discordAccount.lastUpdateRequest = lastUpdateRequest.toLocalDateTime();
                else
                    discordAccount.lastUpdateRequest = null;

                discordAccounts.add(discordAccount);
            }

        } catch (SQLException e) {
            Logger.error(e.toString());
            online = false;
        }

        return discordAccounts;
    }

    public String getPopulationSTring() {

        String popString = "";

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            // Discord account name based lookup
            CallableStatement getPopString = connection.prepareCall("CALL GET_POPULATION_STRING()");
            ResultSet rs = getPopString.executeQuery();

            if (rs.next())
                popString = rs.getString("popstring");

        } catch (SQLException e) {
            Logger.error(e.toString());
            online = false;
        }

        return popString;
    }

    public void invalidateLoginCache(String discordAccountID) {

        try (Connection connection = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
                ConfigManager.MB_DATABASE_PASS.getValue())) {

            String queryString = "INSERT IGNORE INTO login_cachelist (`UID`) SELECT `UID` from `obj_account` WHERE `discordAccount` = ?";

            PreparedStatement invalidateAccounts = connection.prepareStatement(queryString);
            invalidateAccounts.setString(1, discordAccountID);
            invalidateAccounts.executeUpdate();

        } catch (SQLException e) {
            Logger.error(e.toString());
            online = false;
        }
    }
}
