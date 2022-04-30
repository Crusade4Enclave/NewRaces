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
import org.pmw.tinylog.Logger;

import java.io.IOException;

public class ServerRequestHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String serverCommand;
        String execString = "";

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        // No command supplied?

        if (args.length != 1)
            return;

        serverCommand = args[0].toLowerCase().trim();

        // only reboot or shutdown

        if ("rebootshutdown".contains(serverCommand) == false)
            return;

        switch (serverCommand) {

            case "reboot":
                execString = "/bin/sh -c ./mbrestart.sh";
                break;
            case "shutdown":
                execString = "/bin/sh -c ./mbkill.sh";
                break;
            default:
                break;
        }

        if (execString.isEmpty() == false) {
            try {
                Runtime.getRuntime().exec(execString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            MagicBot.sendResponse(event, "MagicBot has executed your " + serverCommand);
            Logger.info(event.getAuthor().getName() + " told server to " + serverCommand);
        }
    }
}