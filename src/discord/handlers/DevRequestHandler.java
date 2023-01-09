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
        String commandArgument = "";
        String execString = "";

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        // No command supplied?

        if (args.length != 1)
            return;

        serverCommand = args[0].toLowerCase().trim();

        if (args.length == 2)
            commandArgument = args[1].toLowerCase().trim();

        switch (serverCommand) {

            case "build" :
                execString = "mbdevbuild.sh";
                break;
            case "restart":
                execString = "mbdevrestart.sh";
                break;
            case "debug":
                execString = "mbdevdebug.sh";
                break;
            case "shutdown":
                execString = "mbdevkill.sh";
                break;
            default:
                break;
        }

        if (execString.isEmpty() == false) {
            try {
                Runtime.getRuntime().exec(new String[]{execString, commandArgument});
            } catch (IOException e) {
                e.printStackTrace();
            }
            MagicBot.sendResponse(event, "Executed on dev: " + serverCommand + " " + commandArgument);
            Logger.info(event.getAuthor().getName() + " told dev to " + serverCommand + " " + commandArgument);
        }
    }
}