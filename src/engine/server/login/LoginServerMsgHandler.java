// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.server.login;

import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.Enum.GameObjectType;
import engine.gameManager.ConfigManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.job.JobScheduler;
import engine.jobs.DisconnectJob;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.NetMsgHandler;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ServerInfoMsg;
import engine.net.client.msg.login.*;
import engine.objects.Account;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import engine.session.CSSession;
import engine.session.Session;
import engine.util.ByteUtils;
import engine.util.StringUtils;
import org.pmw.tinylog.Logger;

public class LoginServerMsgHandler implements NetMsgHandler {

    private final LoginServer server;

    LoginServerMsgHandler(LoginServer server) {
        super();
        this.server = server;
    }

    /*
     * =========================================================================
     * Client Messages
     * =========================================================================
     */
    @Override
    public boolean handleClientMsg(ClientNetMsg clientNetMsg) {

        if (clientNetMsg == null) {
            Logger.error("Recieved null msg. Returning.");
            return false;
        }

        ClientConnection origin = (ClientConnection) clientNetMsg.getOrigin();
        Protocol protocolMsg = clientNetMsg.getProtocolMsg();

        try {

            switch (protocolMsg) {

                case VERSIONINFO:
                    this.VerifyCorrectClientVersion((VersionInfoMsg) clientNetMsg);
                    break;

                case LOGIN:
                    if (LoginServer.loginServerRunning == true)
                        this.Login((ClientLoginInfoMsg) clientNetMsg, origin);
                    else
                        this.KickToLogin(MBServerStatics.LOGINERROR_LOGINSERVER_BUSY, "", origin);
                    break;

                case KEEPALIVESERVERCLIENT:
                    // echo the keep alive back
                    origin.sendMsg(clientNetMsg);
                    break;

                case SELECTSERVER:
                    this.SendServerInfo(origin);
                    break;

                case CREATECHAR:
                    this.CommitNewCharacter((CommitNewCharacterMsg) clientNetMsg, origin);
                    break;

                case REMOVECHAR:
                    this.DeleteCharacter((DeleteCharacterMsg) clientNetMsg, origin);
                    break;

                case SELECTCHAR:
                    this.RequestGameServer((GameServerIPRequestMsg) clientNetMsg, origin);
                    break;

                case SETSELECTEDOBECT:
                    // Why is this being sent to login server?
                    break;

                default:
                    String ocHex = StringUtils.toHexString(protocolMsg.opcode);
                    Logger.error("Cannot not handle Opcode: " + ocHex);
                    return false;
            }

        } catch (Exception e) {
            Logger.error("protocolMsg:" + protocolMsg + e.toString());
            return false;
        }

        return true;
    }

    private void VerifyCorrectClientVersion(VersionInfoMsg vim) {
        ClientConnection cc;
        String cMajorVer;
        String cMinorVer;
        VersionInfoMsg outVim;

        cc = (ClientConnection) vim.getOrigin();
        cMajorVer = vim.getMajorVersion();
        cMinorVer = vim.getMinorVersion();

       if (!cMajorVer.equals(this.server.getDefaultVersionInfo().getMajorVersion())) {
            this.KickToLogin(MBServerStatics.LOGINERROR_INCORRECT_CLIENT_VERSION, "Major Version Failure: " + cMajorVer, cc);
            return;
        }

       /* if (!cMinorVer.equals(this.server.getDefaultVersionInfo().getMinorVersion())) {
            this.KickToLogin(MBServerStatics.LOGINERROR_INCORRECT_CLIENT_VERSION, "Minor Version Failure: " + cMinorVer, cc);
            return;
        } */

        if (cMinorVer == null) {
            this.KickToLogin(MBServerStatics.LOGINERROR_INCORRECT_CLIENT_VERSION, "Minor Version Failure: ", cc);
            return;
        }

        if (cMinorVer.length()  < 8 || cMinorVer.length()  > 16) {
            this.KickToLogin(MBServerStatics.LOGINERROR_INCORRECT_CLIENT_VERSION, "Minor Version Failure: ", cc);
            return;
        }

        // set MachineID for this connection

        cc.machineID = cMinorVer;

        //  send fake right back to the client
        outVim = new VersionInfoMsg(vim.getMajorVersion(), this.server.getDefaultVersionInfo().getMinorVersion() );
        cc.sendMsg(outVim);
    }

    // our data access should be in a separate object
    private void Login(ClientLoginInfoMsg clientLoginInfoMessage, ClientConnection clientConnection) {

        // Add zero length strings to eliminate the need for null checking.
        String uname = clientLoginInfoMessage.getUname();
        String pass = clientLoginInfoMessage.getPword();

        // Check to see if there is actually any data in uname.pass
        if (uname.length() == 0) {
            this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "The username provided was zero length.", clientConnection);
            return;
        }

        if (pass.length() == 0) {
            this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "The password provided was zero length.", clientConnection);
            return;
        }

        Account account;

        account = DbManager.AccountQueries.GET_ACCOUNT(uname);

        // Create the account if it doesn't exist and MB_LOGIN_AUTOREG is TRUE;
        // This is to support MagicBox users without a web hosting skillset.

        if (account == null) {

            if (ConfigManager.MB_LOGIN_AUTOREG.getValue().equals("FALSE")) {
                this.KickToLogin(MBServerStatics.LOGINERROR_INVALID_USERNAME_PASSWORD, "Could not find account (" + uname + ')', clientConnection);
                Logger.info("Could not find account (" + uname + ')');
                return;
            }

                Logger.info("AutoRegister: " + uname + "/" + pass);
                DbManager.AccountQueries.CREATE_SINGLE(uname, pass);
                account = DbManager.AccountQueries.GET_ACCOUNT(uname);

            if (account == null) {
                this.KickToLogin(MBServerStatics.LOGINERROR_INVALID_USERNAME_PASSWORD, "Could not find account (" + uname + ')', clientConnection);
                Logger.info("Could not auto-create (" + uname + ')');
                return;
            }
        }

        if (account.getLastLoginFailure() + MBServerStatics.RESET_LOGIN_ATTEMPTS_AFTER < System.currentTimeMillis())
            account.resetLoginAttempts();

        // TODO: Log the login attempts IP, name, password and timestamp
        // Check number invalid login attempts. If 5 or greater, kick to login.
        if (account.getLoginAttempts() >= MBServerStatics.MAX_LOGIN_ATTEMPTS) {

            this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Too many login in attempts for '" + uname + '\'', clientConnection);
            Logger.info("Too many login in attempts for '" + uname + '\'');
            return;
        }

        if (account.lastPasswordCheck < System.currentTimeMillis()) {
            account.lastPasswordCheck = System.currentTimeMillis() + MBServerStatics.ONE_MINUTE;
        }

        // Attempt to validate login
        try {
            if (!account.passIsValid(pass, clientConnection.getClientIpAddress(), clientConnection.machineID)) {

                account.incrementLoginAttempts();
                this.KickToLogin(MBServerStatics.LOGINERROR_INVALID_USERNAME_PASSWORD, "", clientConnection);
                Logger.info("Incorrect password(" + uname + ')');
                return;
            }
        } catch (IllegalArgumentException e1) {
            this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "", clientConnection);
            Logger.info("Failed forum account validation(" + uname + ')');
        }

        // Account deactivated

        if (account.status.equals(Enum.AccountStatus.BANNED)) {
            this.KickToLogin(MBServerStatics.LOGINERROR_NO_MORE_PLAYTIME_ON_ACCOUNT, "", clientConnection);
            return;
        }

        // Check to see if we have a Session mapped with this Account:
        Session session = SessionManager.getSession(account);

        // If there is, then the account is in use and must be handled:
        // kick the 'other connection'
        if (session != null)
            this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Your account has been accessed from a different IP & Port.", session.getConn()); // Logout the character


        // TODO implement character logout
        // Get a new session
        session = SessionManager.getNewSession(account, clientConnection);

        // Set Invalid Login Attempts to 0
        account.resetLoginAttempts();

        // Send Login Response
        ClientLoginInfoMsg loginResponse = new ClientLoginInfoMsg(clientLoginInfoMessage);
        loginResponse.setUnknown06(8323072);
        loginResponse.setUnknown07(3276800);
        loginResponse.setUnknown08(196608);
        loginResponse.setUnknown09((short) 15);

        clientConnection.sendMsg(loginResponse);

        // send character select screen
        try {
            this.sendCharacterSelectScreen(session);
        } catch (Exception e) {
            Logger.error("Unable to Send Character Select Screen to client");
            this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Unable to send Character Select Screen to client.", clientConnection);
            return;
        }

        // Logging
        String addyPort = clientConnection.getRemoteAddressAndPortAsString();
        int id = account.getObjectUUID();

        Logger.info(uname + '(' + id + ") has successfully logged in from " + addyPort);

    }

    private void KickToLogin(int errCode, String message, ClientConnection origin) {
        LoginErrorMsg msg = new LoginErrorMsg(errCode, message);

        PlayerCharacter player = origin.getPlayerCharacter();

        if (player == null) {
            origin.sendMsg(msg);
        } else {
            Dispatch dispatch = Dispatch.borrow(player, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
        }


        Logger.info("Kicking to Login. Message: '" + message + '\'');

        DisconnectJob dj = new DisconnectJob(origin);
        JobScheduler.getInstance().scheduleJob(dj, 250);
    }

    protected void sendCharacterSelectScreen(Session s) {
        sendCharacterSelectScreen(s, false);
    }

    private void sendCharacterSelectScreen(Session s, boolean fromCommit) {

        if (s.getAccount() != null) {
            CharSelectScreenMsg cssm = new CharSelectScreenMsg(s, fromCommit);
            s.getConn().sendMsg(cssm);
        } else {
            Logger.error("No Account Found: Unable to Send Character Select Screen");
            this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Unable to send Character Select Screen to client.", s.getConn());
        }
    }

    private void SendServerInfo(ClientConnection conn) {
        ServerInfoMsg sim = new ServerInfoMsg();

        if (!conn.sendMsg(sim)) {
            Logger.error("Failed to send message");

            this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Unable to send ServerInfoMsg to client.", conn);
        }
    }

    private void CommitNewCharacter(CommitNewCharacterMsg commitNewCharacterMessage, ClientConnection clientConnection) {

        Session session = SessionManager.getSession(clientConnection);

        if (session.getAccount() == null)
            return;

        try {
            // Check to see if there is an available slot.
            if (session.getAccount().characterMap.size() >= MBServerStatics.MAX_NUM_OF_CHARACTERS) {
                this.sendCharacterSelectScreen(session);
                return;
            }

            PlayerCharacter pc = PlayerCharacter.generatePCFromCommitNewCharacterMsg(session.getAccount(), commitNewCharacterMessage, clientConnection);

            if (pc == null) {
                Logger.info("Player returned null while creating character.");
                this.sendCharacterSelectScreen(session, true);
                return;
            }

            PlayerCharacter.initializePlayer(pc);
            session.getAccount().characterMap.putIfAbsent(pc.getObjectUUID(), pc);
            // Send back to Character Select Screen
            this.sendCharacterSelectScreen(session, true);

        } catch (Exception e) {
            Logger.error(e);
            this.sendCharacterSelectScreen(session, true);
        }
    }

    public static void sendInvalidNameMsg(String firstName, String lastName, int errorCode, ClientConnection clientConnection) {

        InvalidNameMsg invalidNameMessage;

        if (firstName.length() > 256 || lastName.length() > 256)
            invalidNameMessage = new InvalidNameMsg(firstName, lastName, errorCode);
        else
            invalidNameMessage = new InvalidNameMsg(firstName, lastName, errorCode);

        clientConnection.sendMsg(invalidNameMessage);
    }

    private void DeleteCharacter(DeleteCharacterMsg msg, ClientConnection origin) {

        try {
            PlayerCharacter player;
            Session session;

            session = SessionManager.getSession(origin);
            player = (PlayerCharacter) DbManager.getObject(GameObjectType.PlayerCharacter, msg.getCharacterUUID());

            if (player == null) {
                Logger.error("Delete Error: PlayerID=" + msg.getCharacterUUID() + " not found.");
                this.sendCharacterSelectScreen(session);
                return;
            }

            if (session.getAccount() == null) {
                Logger.error("Delete Error: Account not found.");
                this.sendCharacterSelectScreen(session);
                return;
            }

            if (player.getAccount() != origin.getAccount()) {
                Logger.error("Delete Error: Character " + player.getName() + " does not belong to account " + origin.getAccount().getUname());
                this.sendCharacterSelectScreen(session);
                return;
            }

            //Can't delete as Guild Leader
            //TODO either find an error or just gdisband.

            if (GuildStatusController.isGuildLeader(player.getGuildStatus())) {
                this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Cannot delete a guild leader.", origin);
                return;
            }

            // check for active banes

            if (LoginServer.getActiveBaneQuery(player)) {
                Logger.info("Character " + player.getName() + " has unresolved bane");
                this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Player has unresolved bane.", origin);
                return;
            }

            player.getAccount().characterMap.remove(player.getObjectUUID());
            player.deactivateCharacter();

            // TODO Delete Equipment
            // Resend Character Select Screen.
            this.sendCharacterSelectScreen(session);

        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void RequestGameServer(GameServerIPRequestMsg gameServerIPRequestMessage, ClientConnection conn) {

        Session session;
        PlayerCharacter player;

        session = SessionManager.getSession(conn);
        player = (PlayerCharacter) DbManager.getObject(GameObjectType.PlayerCharacter, gameServerIPRequestMessage.getCharacterUUID());

        if (player == null) {
            Logger.info("Unable to find character ID " + gameServerIPRequestMessage.getCharacterUUID());
            this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "PlayerCharacter lookup failed in .RequestGameServer().", conn);
            return;
        }

        try {
            if (!CSSession.updateCrossServerSession(ByteUtils.byteArrayToSafeStringHex(conn.getSecretKeyBytes()), gameServerIPRequestMessage.getCharacterUUID())) {
                Logger.info("Failed to update Cross server session, Kicking to Login for Character " + player.getObjectUUID());
                this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Failed to update Session Information", conn);
                return;
            }
        } catch (Exception e) {
            Logger.info("Failed to update Cross server session, Kicking to Login for Character " + player.getObjectUUID());
            Logger.error(e);
        }

        // Set the last character used.
        Account account = session.getAccount();
        account.setLastCharIDUsed(gameServerIPRequestMessage.getCharacterUUID());

        GameServerIPResponseMsg gsiprm = new GameServerIPResponseMsg();

        if (!conn.sendMsg(gsiprm)) {
            Logger.error("Failed to send message");
            this.KickToLogin(MBServerStatics.LOGINERROR_UNABLE_TO_LOGIN, "Unable to send GameServerIPResponseMsg to client.", conn);
        }
    }
}
