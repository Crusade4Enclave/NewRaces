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

public class DevRequestHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String serverCommand;
        String buildTarget = "";
        String execString = "";

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        // No command supplied?

        if (args.length != 1)
            return;

        serverCommand = args[0].toLowerCase().trim();

        if (args.length == 2)
            buildTarget = args[1].toLowerCase().trim();


        // only reboot or shutdown

        if ("rebootshutdown".contains(serverCommand) == false)
            return;

        switch (serverCommand) {

            case "build" :
                execString = "/bin/sh -c ./mbdevbuild.sh " + buildTarget;
                break;
            case "reboot":
                execString = "/bin/sh -c ./mbdevrestart.sh";
                break;
            case "debug":
                execString = "/bin/sh -c ./mbdevdebug.sh";
                break;
            case "shutdown":
                execString = "/bin/sh -c ./mbdevkill.sh";
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
            Logger.info(event.getAuthor().getName() + " told dev to " + serverCommand + " " + buildTarget);
        }
    }
}