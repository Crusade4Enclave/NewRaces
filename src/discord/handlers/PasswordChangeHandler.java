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
import org.pmw.tinylog.Logger;

import java.time.LocalDateTime;
import java.util.List;

public class PasswordChangeHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String discordAccountID = event.getAuthor().getId();
        DiscordAccount discordAccount;
        String newPassword;
        boolean defaulted = false;

        if (Database.online == false) {

            MagicBot.sendResponse(event,
                    "Database currently: OFFLINE\n" +
                            "Try again later!");
            return;
        }

        List<DiscordAccount> discordAccounts = MagicBot.database.getDiscordAccounts(discordAccountID);

        // User has no account registered.  Change password?

        if (discordAccounts.isEmpty()) {
            MagicBot.sendResponse(event,
                    "I checked my files twice but could not find your detailings!\n" +
                            "You can easily fix this by asking me for to #register one.\n" +
                            "Only one though.  Multiple registrations are not allowed!");
            return;
        }

        // All accounts are updated in one lot.  Retrieve the first.

        discordAccount = discordAccounts.get(0);

        // Banned or suspended user's get no love.

        if (discordAccount.status.equals(Enum.AccountStatus.BANNED)) {
            MagicBot.sendResponse(event,
                    "Sorry but that is too much work. \n" +
                            "Your account detailings cannot for to log into game!");
            return;
        }

        // User has requested password change within last 24 hours.

        if (discordAccount.lastUpdateRequest != null &&
                LocalDateTime.now().isBefore(discordAccount.lastUpdateRequest.plusDays(1))) {

            MagicBot.sendResponse(event,
                    "You must wait 24 hours between password requests. \n" +
                            "Last account updatings: " + discordAccount.lastUpdateRequest.toString());
            return;
        }

        // No argument choose new random password *he he he*

        if (args.length != 1) {
            newPassword = MagicBot.generatePassword(8);
            defaulted = true;
        } else
            newPassword = args[0];

        // Validate password with regex

        if (MagicBot.passwordRegex.matcher(newPassword).matches() == false) {

            MagicBot.sendResponse(event,
                    "Your supplied password does not compute.\n" +
                            "New password must satisfy following regex:\n" +
                            "^[\\p{Alnum}]{6,20}$");
            return;
        }

        if (newPassword.toLowerCase().equals("newpass")) {
            MagicBot.sendResponse(event,
                    "newpass is not valid password.\n" +
                            "Have brain player!");
            return;
        }

        // Password validates let's change it

        if (MagicBot.database.updateAccountPassword(discordAccount.discordAccount, newPassword) == true) {

            MagicBot.sendResponse(event,
                    "Please allow short minute for to update account detailings.\n" +
                            "Login Server is hosted in bathroom above toilet.  Must flush!\n" +
                            (defaulted == true ? "As you did not for to supply new pass I chose one for you.\n" : "") +
                            "New Password: " + newPassword);
        }

        Logger.info(event.getAuthor().getName() + " reset their password");
    }
}
