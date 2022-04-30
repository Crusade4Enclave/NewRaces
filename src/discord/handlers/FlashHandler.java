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
import java.nio.file.Files;
import java.nio.file.Paths;

public class FlashHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String flashText;

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        // Nothing to send?

        if (args.length == 0)
            return;

        // Convert argument array into string;

        flashText = String.join(" ", args);

        // Write string to flash file.

        try {
            Files.write(Paths.get("flash"), flashText.getBytes());
        } catch (IOException e) {
            Logger.error(e.toString());
        }

        Logger.info(event.getAuthor().getName() + " sent flash: " + flashText);
        MagicBot.sendResponse(event, "Flash: " + flashText);

    }
}
