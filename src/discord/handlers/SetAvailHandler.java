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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.pmw.tinylog.Logger;

public class SetAvailHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String availStatus;
        String availPass;

        if (args.length != 2)
            return;

        availStatus = args[0].toLowerCase().trim();

        // only on/off

        if ("truefalse".contains(availStatus) == false)
            return;

        // Set avail is password driven

        availPass = args[1].toLowerCase().trim();

        if ("myshoes123".equals(availPass) == false)
            return;

        // Authenticated so change availstatus

        if (availStatus.equals("true"))
            Database.online = true;
        else
            Database.online = false;

        Logger.info(event.getAuthor().getName() + " set avail status to: " + Database.online);
        MagicBot.sendResponse(event, "Avail status set to: " + Database.online);

    }
}
