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
import discord.RobotSpeak;
import engine.Enum;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.pmw.tinylog.Logger;

import java.util.List;

public class BanToggleHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String discordAccountID;
        Enum.AccountStatus accountStatus;

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        // Must supply a discord id

        if (args.length != 1) {
            MagicBot.sendResponse(event, "Must for to supply a valid discord account.");
            return;
        }

        // Must be a number!

        discordAccountID = args[0].trim();

        if (discordAccountID.chars().allMatch(Character::isDigit) == false) {
            MagicBot.sendResponse(event, "Must for to supply a number!");
            return;
        }

        List<DiscordAccount> discordAccounts = MagicBot.database.getDiscordAccounts(discordAccountID);

        if (discordAccounts.isEmpty()) {
            MagicBot.sendResponse(event, "No match for supplied discord account.");
            return;
        }

        // toggle ban status

        if (discordAccounts.get(0).status.equals(Enum.AccountStatus.BANNED))
            accountStatus = Enum.AccountStatus.ACTIVE;
        else
            accountStatus = Enum.AccountStatus.BANNED;

        // We have a valid discord ID at this point.  Banstick?

        if (MagicBot.database.updateAccountStatus(discordAccountID, accountStatus) == false) {
            MagicBot.sendResponse(event, "Error occurred while banning player.");
            return;
        }

        // Invalidate login server cache

        MagicBot.database.invalidateLoginCache(discordAccountID);

        // Successful ban.  Ancillary processing begins

        User bannedUser = MagicBot.jda.getUserById(discordAccountID);
        String bannedName = (bannedUser == null ? discordAccounts.get(0).gameAccountName : bannedUser.getName());
        String banString = discordAccounts.size() + " accounts set to " + accountStatus + "  for " + discordAccountID + "/" + bannedName;

        MagicBot.sendResponse(event, banString);
        Logger.info(event.getAuthor().getName() + " " + banString);

        // If we're toggling status to active we're done here.

        if (accountStatus.equals(Enum.AccountStatus.ACTIVE))
            return;

        // Set users role to noob

        if (bannedUser != null)
            MagicBot.magicbaneDiscord.removeRoleFromMember(discordAccountID, MagicBot.memberRole).queue();

        // Anounce event in septic tank channel

        banString = "```\n" + "Goodbye Player " + bannedName + "\n\n";
        banString += RobotSpeak.getRobotInsult() + "\n```";

        if (MagicBot.septicChannel.canTalk())
            MagicBot.septicChannel.sendMessage(banString).queue();
    }

}
