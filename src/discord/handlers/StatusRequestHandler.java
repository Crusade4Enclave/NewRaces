// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package discord.handlers;

import discord.Database;
import discord.MagicBot;
import engine.gameManager.ConfigManager;
import engine.server.login.LoginServer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StatusRequestHandler {

    public static void handleRequest(MessageReceivedEvent event) {

        String outString;

        // Add version information
        outString = "MagicBot: " + ConfigManager.MB_MAGICBOT_BOTVERSION.getValue() + "\n" +
                "MagicBane: " + ConfigManager.MB_MAGICBOT_GAMEVERSION.getValue() + "\n";

        // Add server status info
        outString += "\nServer Status: ";

        if (LoginServer.isPortInUse(Integer.parseInt(ConfigManager.MB_BIND_ADDR.getValue())))
            outString += "ONLINE\n";
        else
            outString += "OFFLINE\n";

        if (Database.online == true)
            outString += MagicBot.database.getPopulationSTring();
        else
            outString += "Database offline: no population data.";

        MagicBot.sendResponse(event, outString);
    }
}
