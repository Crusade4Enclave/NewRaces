// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package discord.handlers;

import discord.MagicBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LogsRequestHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String logType;
        int tailCount;
        String logOutput;

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        // No arguments supplied?

        if (args.length != 2)
            return;

        logType = args[0].toLowerCase().trim();

        if ("worldloginmagicbot".contains(logType) == false)
            return;

        try {
            tailCount = Integer.parseInt(args[1].trim());
        } catch (NumberFormatException e) {
            return;
        }

        // Transform logtype to actual file name

        switch (logType) {
            case "magicbot":
                logType = "console_magicbot";
                break;
            case "world":
                logType = "console_server";
                break;
            case "login":
                logType = "console_login";
                break;
        }

        // Retrieve the data and send back to the user

        logOutput = MagicBot.readLogFile(logType, tailCount);
        MagicBot.sendResponse(event, logOutput);
    }
}
