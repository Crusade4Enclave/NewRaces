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

import static discord.ChatChannel.RECRUIT;

public class RecruitChannelHandler {

    public static void handleRequest(MessageReceivedEvent event, String[] args) {

        String chatText;
        String outString;

        // Early exit if database unavailable or is not an admin

        if (MagicBot.isAdminEvent(event) == false)
            return;

        // Nothing to send?

        if (args.length == 0)
            return;

        // Convert argument array into string;

        chatText = String.join(" ", args);

        // Build String

        if (chatText.startsWith("-r "))
            outString =
                "```\n" + "Hello Players \n\n" +
                        chatText.substring(3) + "\n\n" +
                        RobotSpeak.getRobotSpeak() + "\n```";
        else outString = chatText;

        // Write string to changelog channel

        if (RECRUIT.textChannel.canTalk())
            RECRUIT.textChannel.sendMessage(outString).queue();

        Logger.info(event.getAuthor().getName() + "recruit: " + chatText);

    }
}
