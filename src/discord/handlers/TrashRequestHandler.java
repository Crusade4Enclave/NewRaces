// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package discord.handlers;

import discord.MagicBot;
import discord.RobotSpeak;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static discord.ChatChannel.SEPTIC;

public class TrashRequestHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String outString;
        int trashCount = 0;

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        if (args.length == 0) {
            outString = MagicBot.database.getTrashFile();
            MagicBot.sendResponse(event, outString);
            return;
        }

        if (args[0].equals("flush") == true) {

            // Empty the trash!

            trashCount = MagicBot.database.getTrashCount();

            if (trashCount == 0)
                return;

            // Anounce event in septic tank channel

            outString = "```\n" +  trashCount + " Player Character were for to deleted due to verified cheatings. \n\n";
            outString += MagicBot.database.getTrashList() + "\n\n";
            outString += RobotSpeak.getRobotInsult() + "\n```";

            if (SEPTIC.textChannel.canTalk())
                SEPTIC.textChannel.sendMessage(outString).queue();

            try {
                Files.write(Paths.get("trash"), "".getBytes());
                outString = "Flushing trash players...\n";
                MagicBot.sendResponse(event, outString);
            } catch (IOException e) {
                Logger.error(e.toString());
            }
        }

        if (args[0].equals("detail") == true) {

            outString = MagicBot.database.getTrashDetail();
            MagicBot.sendResponse(event, outString);
            return;
        }

    }
}
