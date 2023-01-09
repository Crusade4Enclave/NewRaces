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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DevRequestHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String serverCommand;
        String commandArgument = "";
        String commandString = "";
        String logString = "";

        ProcessBuilder processBuilder;

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        serverCommand = args[0].toLowerCase().trim();

        if (args.length == 2)
            commandArgument = args[1].toLowerCase().trim();

        switch (serverCommand) {

            case "build" :
                commandString = "./mbdevbuild.sh";
                break;
            case "restart":
                commandString = "./mbdevrestart.sh";
                break;
            case "debug":
                commandString = "./mbdevdebug.sh";
                break;
            case "shutdown":
                commandString = "./mbdevkill.sh";
                break;
            default:
                break;
        }

        if (commandString.isEmpty()) {
            MagicBot.sendResponse(event, "Unrecognized Dev command: " + serverCommand + " " + commandArgument);
            return;
        }

        processBuilder = new ProcessBuilder("/bin/sh", "-c", commandString + " " + commandArgument);

        try {
                processBuilder.start();
            } catch (IOException e) {
                Logger.info(e.toString());
            }

            logString = String.join(" ",processBuilder.command().toArray(new String[0]));
            MagicBot.sendResponse(event, "Executed on dev: " + logString);
        }
}