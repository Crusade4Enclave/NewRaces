// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package discord;

import engine.gameManager.ConfigManager;
import net.dv8tion.jda.api.entities.TextChannel;

public enum ChatChannel {

    ANNOUNCE("MB_MAGICBOT_ANNOUNCE"),
    SEPTIC("MB_MAGICBOT_SEPTIC"),
    CHANGELOG("MB_MAGICBOT_CHANGELOG"),
    POLITICAL("MB_MAGICBOT_POLITICAL"),
    GENERAL("MB_MAGICBOT_GENERAL"),
    FORTOFIX("MB_MAGICBOT_FORTOFIX"),
    RECRUIT("MB_MAGICBOT_RECRUIT"),

    ADMINLOG("MB_MAGICBOT_ADMINLOG");

    public final String configName;
    public  long channelID;
    public TextChannel textChannel;

    ChatChannel(String configName) {
        this.configName = configName;
    }

    // Create text channel objects we will use

    public static void Init() {

        for (ChatChannel chatChannel : ChatChannel.values()) {
            chatChannel.channelID = Long.parseLong(ConfigManager.valueOf(chatChannel.configName).getValue());
            chatChannel.textChannel = MagicBot.jda.getTextChannelById(chatChannel.channelID);
        }
    }
}
