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
import engine.Enum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class AccountInfoRequest {

    public static void handleRequest(MessageReceivedEvent event) {

        String discordAccountID = event.getAuthor().getId();
        Enum.AccountStatus accountStatus;

        if (Database.online == false) {

            MagicBot.sendResponse(event,
                    "Database currently: OFFLINE\n" +
                            "Try again later!");
            return;
        }

        List<DiscordAccount> discordAccounts = MagicBot.database.getDiscordAccounts(discordAccountID);

        // User has no account registered.  Status of what?

        if (discordAccounts.isEmpty()) {
            MagicBot.sendResponse(event,
                    "I checked my files twice but could not find your detailings!\n" +
                            "You can easily fix this by asking me for to #register one.\n" +
                            "Only one though.  Multiple registrations are not allowed!");
            return;
        }

        // Send account detailings to user.

        String outString =
                "I have for to located your account detailings\n" +
                        "Registered on: " + discordAccounts.get(0).registrationDate.toString() +
                        "\n-------------------\n";

        for (DiscordAccount userAccount : discordAccounts)
            outString += userAccount.gameAccountName + "\n";

        outString += "\n";

        accountStatus = discordAccounts.get(0).status;

        switch (accountStatus) {
            case BANNED:
                outString += "Your account status is BANNED. \n\n" +
                        "It is ok player.\n" +
                        "You may cheat on us, but your wife cheats on you!";
                break;
            case ACTIVE:
                outString += "Your account status is ACTIVE.\n" +
                        "Do not cheat or status will change.";
                break;
            case ADMIN:
                outString += "You are an admin.  By your command?";
                break;
        }

        MagicBot.sendResponse(event, outString);

    }
}
