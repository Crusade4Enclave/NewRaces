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

public class DevBuildHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String buildTarget;
        String execString = "";

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        // No command supplied?

        if (args.length != 1)
            return;

        buildTarget = args[0].toLowerCase().trim();
        execString = "/bin/sh -c ./mbdevbuild.sh " + buildTarget;


        if (execString.isEmpty() == false) {
            try {
                Runtime.getRuntime().exec(execString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            MagicBot.sendResponse(event, "MagicBot has for to built " + buildTarget + "on Dev");
            Logger.info(event.getAuthor().getName() + " built " + buildTarget + " on Dev");
        }
    }
}