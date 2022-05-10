// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package discord;

import discord.handlers.*;
import engine.Enum;
import engine.gameManager.ConfigManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.StartupPolicy;
import org.pmw.tinylog.writers.RollingFileWriter;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/*
*  MagicBot is many things to Magicbane...
*
*  -Project Mascot
*  -Customer service and administration bot
*  -Benevolent dictator
*  -Investment manager.
*
*  MagicBot will never beg you for money.  He is a very
*  responsible robot. He was varnished but never garnished.
*  MagicBot does not for to overclock himself.  His chips
*  will therefore never overcook.
*  MagicBot will never be a pitiful robot trying for to use
*  you as emotional support human.
*
*  MagicBot is just not that sort of robot and Magicbane
*  just isn't that sort of project.
*
*  MagicBot runs a Shaodowbane emulator not a Second Life emulator.
*
*/
public class MagicBot extends ListenerAdapter {

    public static JDA jda;
    public static Database database;
    public static final Pattern accountNameRegex = Pattern.compile("^[\\p{Alnum}]{6,20}$");
    public static final Pattern passwordRegex = Pattern.compile("^[\\p{Alnum}]{6,20}$");
    public static  long discordServerID;
    public static  long discordRoleID;

    public static Guild magicbaneDiscord;
    public static Role memberRole;
    public static TextChannel septicChannel;


    public static void main(String[] args) throws LoginException, InterruptedException {

        // Configure tinylogger

        Configurator.defaultConfig()
                .addWriter(new RollingFileWriter("logs/discord/magicbot.txt", 30, new TimestampLabeler(), new StartupPolicy()))
                .level(Level.DEBUG)
                .formatPattern("{level} {date:yyyy-MM-dd HH:mm:ss.SSS} [{thread}] {class}.{method}({line}) : {message}")
                .activate();

        // Configuration Manager to the front desk

        if (ConfigManager.init() == false) {
            Logger.error("ABORT! Missing config entry!");
            return;
        }

        if (ConfigManager.MB_PUBLIC_ADDR.getValue().equals("0.0.0.0")) {

            // Autoconfigure IP address for use in worldserver response
            // .
            Logger.info("AUTOCONFIG PUBLIC IP ADDRESS");
            URL whatismyip = null;

            try {
                whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = null;
                in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
                ConfigManager.MB_PUBLIC_ADDR.setValue(in.readLine());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Configure Discord essential identifiers

        discordServerID = Long.parseLong(ConfigManager.MB_MAGICBOT_SERVERID.getValue());
        discordRoleID = Long.parseLong(ConfigManager.MB_MAGICBOT_ROLEID.getValue());

        // Configure and instance the database interface

        database = new Database();
        database.configureDatabase();

        // Use authentication token issued to MagicBot application to
        // connect to Discord.  Bot is pre-invited to the Magicbane server.

        // Configure and create JDA discord instance

        JDABuilder jdaBuilder = JDABuilder.create(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES)
                .setToken(ConfigManager.MB_MAGICBOT_BOTTOKEN.getValue())
                .addEventListeners(new MagicBot())
                .disableCache(EnumSet.of(CacheFlag.VOICE_STATE, CacheFlag.EMOTE,
                        CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS))
                .setMemberCachePolicy(MemberCachePolicy.ALL);

        jda = jdaBuilder.build();
        jda.awaitReady();

        // Cache guild and role values for later usage in #register

        magicbaneDiscord = jda.getGuildById(discordServerID);
        memberRole = magicbaneDiscord.getRoleById(discordRoleID);

        // Initialize chat channel support

        ChatChannel.Init();

        Logger.info("***MAGICBOT IS RUNNING***");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        // Exit if discord is offline

        if (jda.getStatus().equals(JDA.Status.CONNECTED) == false)
            return;

        // Early exit if message sent to us by another bot or ourselves.

        if (event.getAuthor().isBot()) return;

        // Extract message and origin channel from event

        Message message = event.getMessage();

        // Only private messages
        MessageChannel channel = event.getMessage().getChannel();

        if (channel.getType().equals(ChannelType.PRIVATE) == false)
            return;

        // Only real users

        if (event.getAuthor().isBot())
            return;

        // Only users who have actually joined Magicbane discord.

        if (magicbaneDiscord.isMember(event.getAuthor()) == false)
            return;

        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content
        // for e.g. console view or logging (strip discord formatting)

        String content = message.getContentRaw();
        String[] args = content.split(" ");
        String command = args[0].toLowerCase();

        if (args.length > 1)
            args = Arrays.copyOfRange(args, 1, args.length);
        else
            args = new String[0];

        switch (command) {
            case "#register":
                RegisterAccountHandler.handleRequest(event, args);
                break;
            case "#help":
                handleHelpRequest(event);
                break;
            case "#account":
                AccountInfoRequest.handleRequest(event);
                break;
            case "#password":
                PasswordChangeHandler.handleRequest(event, args);
                break;
            case "#changelog":
                ChangeLogHandler.handleRequest(event, args);
                break;
            case "#general":
                GeneralChannelHandler.handleRequest(event, args);
                break;
            case "#politics":
                PoliticalChannelHandler.handleRequest(event, args);
                break;
            case "#announce":
                AnnounceChannelHandler.handleRequest(event, args);
                break;
            case "#bug":
                ForToFixChannelHandler.handleRequest(event, args);
                break;
            case "#recruit":
                RecruitChannelHandler.handleRequest(event, args);
                break;
            case "#lookup":
                LookupRequestHandler.handleRequest(event, args);
                break;
            case "#rules":
                RulesRequestHandler.handleRequest(event);
                break;
            case "#status":
                StatusRequestHandler.handleRequest(event);
                break;
            case "#setavail":
                SetAvailHandler.handleRequest(event, args);
                break;
            case "#ban":
                BanToggleHandler.handleRequest(event, args);
                break;
            case "#server":
                ServerRequestHandler.handleRequest(event, args);
                break;
            case "#logs":
                LogsRequestHandler.handleRequest(event, args);
                break;
            case "#flash":
                FlashHandler.handleRequest(event, args);
                break;
            case "#trash":
                TrashRequestHandler.handleRequest(event, args);
                break;
            default:
                junkbot(command, args);
                break;
        }
    }

    public static void sendResponse(MessageReceivedEvent event, String responseContent) {

        // Send a formatted MagicBot response to a Discord user

        String discordUserName;
        MessageChannel channel;

        // Exit if discord is offline

        if (jda.getStatus().equals(JDA.Status.CONNECTED) == false)
            return;

        discordUserName = event.getAuthor().getName();
        channel = event.getMessage().getChannel();

        channel.sendMessage(
                "```\n" + "Hello Player " + discordUserName + "\n\n" +
                        responseContent + "\n\n" +
                        RobotSpeak.getRobotSpeak() + "\n```").queue();
    }

    public static boolean isAdminEvent(MessageReceivedEvent event) {

        String discordAccountID = event.getAuthor().getId();
        List<DiscordAccount> discordAccounts;
        DiscordAccount discordAccount;

        // Note that database errors will cause this to return false.
        // After the database is offline Avail status must be set
        // to true before any subsequent admin commands will function.

        if (Database.online == false)
            return false;

        discordAccounts = database.getDiscordAccounts(discordAccountID);

        if (discordAccounts.isEmpty())
            return false;

        discordAccount = discordAccounts.get(0);
        return (discordAccount.isDiscordAdmin == 1);
    }

    public void handleHelpRequest(MessageReceivedEvent event) {

        // Help is kept here in the main class instead of a handler as a
        // design decision for ease of maintenance.

        String helpString = "I wish for to do the following things for you, not to you!\n\n" +
                "#register <name>      Register account for to play Magicbane.\n" +
                "#password <newpass>   Change your current game password.\n" +
                "#account              List your account detailings.\n" +
                "#rules                List of MagicBane server rules.\n" +
                "#status               Display MagicBane server status.\n" +
                "#help                 List of MagicBot featurings.\n\n" +
                "http://magicbane.com/tinyinstaller.zip";

        if (isAdminEvent(event))
            helpString += "\n" +
                    "#lookup   <name>      Return accounts starting with string.\n" +
                    "#bug -r <text>        Post to the bug channel/\n" +
                    "#announce -r <text>   Post to the announcement channel/\n" +
                    "#changelog <text>     Post to the Changelog channel/\n" +
                    "#general -r <text>    Post to the general channel/\n" +
                    "#politics -r <text>   Post to the politics channel/\n" +
                    "#recruit  -r <text>   Post to the politics channel/\n" +
                    "#ban      ######      Toggle active status of account.\n" +
                    "#setavail true/false  Toggle status of database access.\n" +
                    "#server               reboot/shutdown are your options.\n" +
                    "#logs                 magicbot/world/login n  (tail)\n" +
                    "#flash <text>         send flash message\n" +
                    "#trash                <blank>/detail/flush";
        sendResponse(event, helpString);
    }

    public static String generatePassword(int length) {

        String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder passwordBuilder = new StringBuilder(length);
        Random random = new Random();

        // Generate alphanumeric password of a given length.
        // Could not find a good method of generating a password
        // based upon a given regex.

        for (int i = 0; i < length; i++)
            passwordBuilder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));

        return new String(passwordBuilder);
    }

    public static String readLogFile(String filePath, int lineCount) {

        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "tail -n  " + lineCount + " " + filePath);
        builder.redirectErrorStream(true);
        Process process = null;
        String line = null;
        String logOutput = "";

        try {
            process = builder.start();

            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                logOutput += line + "\n";
            }

        } catch (IOException e) {
            Logger.error(e.toString());
            return "Error while reading logfile";
        }

        return logOutput;
    }

    private static void junkbot(String command, String[] inString) {

        String outString;
        Writer fileWriter;

        if (inString == null)
            return;;

        outString = command + String.join(" ", inString);
        outString += "\n";

        try {
            fileWriter = new BufferedWriter(new FileWriter("junkbot.txt", true));
            fileWriter.append(outString);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
