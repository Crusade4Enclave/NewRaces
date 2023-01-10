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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

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
            case "lastout":
                MagicBot.sendResponse(event, getLastOutput());
                return;
            case "console":
                commandString = "./mbdevconsole.sh";
                break;
            case "help":
                MagicBot.sendResponse(event,
                        "#dev build <target> (blank==master) \n" +
                                      "#dev shutdown (Shutdown dev server)\n" +
                                      "#dev restart (Restarts the server)\n"+
                        "#dev debug (Restarts server in debug mode)\n" +
                        "#dev console # (Displays # lines from console)\n" +
                        "#dev lastout (Displays output from last command) \n");
                return;
            default:
                break;
        }

        if (commandString.isEmpty()) {
            MagicBot.sendResponse(event, "Unrecognized Dev command: " + serverCommand + " " + commandArgument);
            return;
        }

        processBuilder = new ProcessBuilder("/bin/sh", "-c", commandString + " " + commandArgument + " > devLastOut");
        logString = String.join(" ",processBuilder.command().toArray(new String[0]));

        try {
                processBuilder.start();
            } catch (IOException e) {
                Logger.info(e.toString());
            }

        MagicBot.sendResponse(event, "Executed on dev: " + logString + "\n" +
                                                   "Use #dev lastout to view results");

        }
    private static String getLastOutput() {

        String outString = null;
        try {
            outString = Files.lines(Paths.get("devLastOut"))
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outString;
    }
}