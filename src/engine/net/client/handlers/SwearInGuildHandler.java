// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.Enum.GuildState;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.GuildManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.SendGuildEntryMsg;
import engine.net.client.msg.guild.SwearInGuildMsg;
import engine.objects.City;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;

public class SwearInGuildHandler extends AbstractClientMsgHandler {

    public SwearInGuildHandler() {
        super(SwearInGuildMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
        PlayerCharacter player;
        SwearInGuildMsg swearInMsg;
        Guild targetGuild;
        Guild nation;
        Dispatch dispatch;

        swearInMsg = (SwearInGuildMsg) baseMsg;
        player = SessionManager.getPlayerCharacter(origin);

        if (player == null)
            return true;

        targetGuild = (Guild) DbManager.getObject(GameObjectType.Guild, swearInMsg.getGuildUUID());

        if (targetGuild == null) {
             ErrorPopupMsg.sendErrorMsg(player, "A Serious error has occured. Please post details for to ensure transaction integrity");
            return true;
        }

        nation = player.getGuild();

        if (nation == null) {
             ErrorPopupMsg.sendErrorMsg(player, "You do not belong to a guild!");
            return true;
        }

        try {
            if (!nation.isNation()) {
                 ErrorPopupMsg.sendErrorMsg(player, "Your guild is not a nation!");
                return true;
            }
            if (!nation.getSubGuildList().contains(targetGuild)) {
                ErrorPopupMsg.sendErrorMsg(player, "Your do not have such authority!");
                return true;
            }

            if (!Guild.canSwearIn(targetGuild)) {
                ErrorPopupMsg.sendErrorMsg(player, targetGuild.getGuildState().name() + "cannot be sworn in");
                return true;
            }

            if (GuildStatusController.isGuildLeader(player.getGuildStatus()) == false){
                ErrorPopupMsg.sendErrorMsg(player, "Your do not have such authority!");
                return true;
            }

            if (!DbManager.GuildQueries.UPDATE_PARENT(targetGuild.getObjectUUID(), nation.getObjectUUID())) {
                ErrorPopupMsg.sendErrorMsg(player, "A Serious error has occured. Please post details for to ensure transaction integrity");
                return true;
            }

            switch (targetGuild.getGuildState()) {
                case Petitioner:
                    GuildManager.updateAllGuildBinds(targetGuild, nation.getOwnedCity());
                    break;
                case Protectorate:
                    break;
                default:
                    //shouldn't get here.
                    break;
            }

            //update Guild state.
            targetGuild.setNation(nation);
            GuildManager.updateAllGuildTags(targetGuild);
            targetGuild.upgradeGuildState(false);

            if (nation.getGuildState() == GuildState.Sovereign)
                nation.upgradeGuildState(true);

            SendGuildEntryMsg msg = new SendGuildEntryMsg(player);
            dispatch = Dispatch.borrow(player, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

           City.lastCityUpdate = System.currentTimeMillis();

            ArrayList<PlayerCharacter> guildMembers = SessionManager.getActivePCsInGuildID(nation.getObjectUUID());

            for (PlayerCharacter member : guildMembers) {
                ChatManager.chatGuildInfo(member, "Your Guild is now a Nation!");
            }

            ArrayList<PlayerCharacter> swornMembers = SessionManager.getActivePCsInGuildID(targetGuild.getObjectUUID());

            for (PlayerCharacter member : swornMembers) {
                ChatManager.chatGuildInfo(member, "Your Guild has sword fealty to " + nation.getName() + '.');
            }
        } catch (Exception e) {
            Logger.error( e.getMessage());
            return true;
        }

        return true;
    }

}
