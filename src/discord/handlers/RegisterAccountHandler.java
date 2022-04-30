// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package discord.handlers;

import discord.Database;
import discord.DiscordAccount;
import discord.MagicBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.pmw.tinylog.Logger;

import java.util.List;

public class RegisterAccountHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String discordAccountID = event.getAuthor().getId();
        String discordUserName = event.getAuthor().getName();
        String discordPassword = MagicBot.generatePassword(8);
        String accountName;

        if (Database.online == false) {
            MagicBot.sendResponse(event,
                    "Database currently: OFFLINE\n" +
                            "Try again later!");
            return;
        }

        List<DiscordAccount> discordAccounts = MagicBot.database.getDiscordAccounts(discordAccountID);

        // If we have previously registered this discord account let them know
        // the current status.

        if (discordAccounts.isEmpty() == false) {
            MagicBot.sendResponse(event,
                    "It seems you already have an account registered.\n" +
                            "Do you need #account detailings or more general #help?");
            MagicBot.magicbaneDiscord.addRoleToMember(discordAccountID, MagicBot.memberRole).queue();
            return;
        }

        // if user supplied argument let's validate it.
        // otherwise build an account name based on their discord account.

        if (args.length != 1) {

            // Build account name using Discord name along with their discriminator.

            accountName = discordUserName.replaceAll("\\s+", "");
            accountName += "#" + event.getAuthor().getDiscriminator();
        } else {

            // Validate account name with regex

            accountName = args[0].replaceAll("\\s+", "");

            if (MagicBot.accountNameRegex.matcher(accountName).matches() == false) {

                MagicBot.sendResponse(event,
                        "Your supplied account name does not compute.\n" +
                                "Account names must satisfy following regex:\n" +
                                "^[\\p{Alnum}]{6,20}$");
                return;
            }

            if (accountName.toLowerCase().equals("accountname")) {
                MagicBot.sendResponse(event,
                        "accountname is not valid account name.\n" +
                                "Have brain player!");
                return;
            }
        }

        // Make sure account doesn't already exist.

        if (MagicBot.database.getAccountsByDiscordName(accountName, true).isEmpty() == false) {

            MagicBot.sendResponse(event,
                    "It seems this account name is already taken.\n" +
                            "Perhaps try one less common in frequency.");
            return;
        }

        // If there is no registered discord account we oblige and create 4
        // account based upon his current discord *name* not the ID.

        if (MagicBot.database.registerDiscordAccount(discordAccountID, accountName, discordPassword) == true) {

            Logger.info("Account " + accountName + " created for: " + discordUserName + " " + discordAccountID);

            MagicBot.sendResponse(event,
                    "Welcome to MagicBane!\n" +
                            "-------------------\n" +
                            "I have registered the following accounts to your discord.\n\n" +
                            "1) " + accountName + "#1" + "     2) " + accountName + "#2\n" +
                            "3) " + accountName + "#3" + "     4) " + accountName + "#4\n\n" +
                            "Your default password is: " + discordPassword + "\n" +
                            "Ask me #help for to receive list of robot featurings.\n\n" +
                            "http://magicbane.com/tinyinstaller.zip" +
                            "\n\nPlay for to Crush!");

            // Add Discord member privileges.

            MagicBot.magicbaneDiscord.addRoleToMember(discordAccountID, MagicBot.memberRole).queue();

            return;
        }

        // The call to the stored procedure abended.  Report to player
        // and return.

        Logger.error("Creating account: " + accountName + " for: " + discordUserName + " " + discordAccountID);
        Database.online = false;

        MagicBot.sendResponse(event,
                "-------------------\n" +
                        "I for to had internal error while registering your\n" +
                        "account.  This has been reported.  Try again later!");
    }
}
