// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package discord.handlers;

import discord.DiscordAccount;
import discord.MagicBot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.pmw.tinylog.Logger;

import java.util.List;

public class LookupRequestHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String searchString = "";
        String outString;

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        // No argument supplied?

        if (args.length != 1)
            return;

        searchString = args[0].toLowerCase();

        List<DiscordAccount> discordAccounts = MagicBot.database.getAccountsByDiscordName(searchString, false);

        if (discordAccounts.isEmpty()) {
            MagicBot.sendResponse(event,
                    "No accounts found matching string: " + searchString);
            return;
        }

        if (discordAccounts.size() >= 20) {
            MagicBot.sendResponse(event,
                    discordAccounts.size() + "Sorry more than 20 records were returned! " + searchString);
            return;
        }

        // Valid request return results

        Logger.info(event.getAuthor().getName() + " lookup on account:" + searchString);

        outString =
                "The follow accounts matched: " + searchString + "\n\n" +
                        "-------------------\n";

        for (DiscordAccount userAccount : discordAccounts) {

            // Ternary became a bitch, so broke this out.

            User discordUser = MagicBot.jda.getUserById(userAccount.discordAccount);

            if (discordUser != null)
                outString += discordUser.getName() + discordUser.getDiscriminator() +
                        "/" + userAccount.discordAccount + "     ";
            else
                outString += userAccount.discordAccount + " *N/A*     ";

            outString += userAccount.gameAccountName + "     " + userAccount.status.name() + "     ";
            outString += "\n";
        }
        MagicBot.sendResponse(event, outString);
    }
}
