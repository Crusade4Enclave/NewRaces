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
import engine.net.client.msg.guild.DismissGuildMsg;
import engine.net.client.msg.guild.SendGuildEntryMsg;
import engine.objects.*;
import engine.server.MBServerStatics;
import engine.session.Session;

import java.util.ArrayList;

public class DismissGuildHandler extends AbstractClientMsgHandler {

    public DismissGuildHandler() {
        super(DismissGuildMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

        DismissGuildMsg dismissMsg;
        PlayerCharacter player;
        Guild toDismiss;
        Guild nation;
        Dispatch dispatch;

        dismissMsg = (DismissGuildMsg) baseMsg;

        player = SessionManager.getPlayerCharacter(origin);

        if (player == null)
            return true;

        toDismiss = (Guild) DbManager.getObject(GameObjectType.Guild, dismissMsg.getGuildID());

        if (toDismiss == null) {
             ErrorPopupMsg.sendErrorMsg(player, "A Serious error has occured. Please post details for to ensure transaction integrity");
            return true;
        }

        nation = player.getGuild();

        if (nation == null) {
             ErrorPopupMsg.sendErrorMsg(player, "Nothing to disband, your guild is not a nation!");
            return true;
        }

        if (!nation.getSubGuildList().contains(toDismiss)) {
             ErrorPopupMsg.sendErrorMsg(player, "You do not have authority to dismiss this guild!");
            return true;
        }

        if (GuildStatusController.isGuildLeader(player.getGuildStatus()) == false) {
           ErrorPopupMsg.sendErrorMsg(player, "Only a guild leader can dismiss a subguild!");
            return true;
        }

        // Restriction on active bane desubbing

        if (Bane.getBaneByAttackerGuild(toDismiss) != null)
        {
            ErrorPopupMsg.sendErrorMsg(player, "You may not dismiss subguild with active bane!");
            return true;
        }

        switch (toDismiss.getGuildState()) {
            case Sworn:

                if (!DbManager.GuildQueries.UPDATE_PARENT(toDismiss.getObjectUUID(), MBServerStatics.worldUUID)) {
                     ErrorPopupMsg.sendErrorMsg(player, "A Serious error has occured. Please post details for to ensure transaction integrity");
                    return true;
                }
                nation.getSubGuildList().remove(toDismiss);
                toDismiss.downgradeGuildState();
                toDismiss.setNation(null);
                GuildManager.updateAllGuildBinds(toDismiss, null);

                break;
            case Province:
                if (!DbManager.GuildQueries.UPDATE_PARENT(toDismiss.getObjectUUID(),MBServerStatics.worldUUID)) {
                    ErrorPopupMsg.sendErrorMsg(player, "A Serious error has occured. Please post details for to ensure transaction integrity");
                    return true;
                }
                nation.getSubGuildList().remove(toDismiss);
                toDismiss.downgradeGuildState();
                toDismiss.setNation(toDismiss);

                break;
            case Petitioner:
                nation.getSubGuildList().remove(toDismiss);
                toDismiss.downgradeGuildState();
                break;
            case Protectorate:
                nation.getSubGuildList().remove(toDismiss);
                toDismiss.downgradeGuildState();
                break;
        }

        GuildManager.updateAllGuildTags(toDismiss);

        if (nation.getSubGuildList().isEmpty())
            nation.downgradeGuildState();

        SendGuildEntryMsg msg = new SendGuildEntryMsg(player);
        dispatch = Dispatch.borrow(player, msg);
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

        final Session s = SessionManager.getSession(player);

        City.lastCityUpdate = System.currentTimeMillis();


        ArrayList<PlayerCharacter> guildMembers = SessionManager.getActivePCsInGuildID(nation.getObjectUUID());

        for (PlayerCharacter member : guildMembers) {
            ChatManager.chatGuildInfo(member, toDismiss.getName() + " has been dismissed as a subguild!");
        }

        ArrayList<PlayerCharacter> dismissedMembers = SessionManager.getActivePCsInGuildID(toDismiss.getObjectUUID());

        for (PlayerCharacter member : dismissedMembers) {
            ChatManager.chatGuildInfo(member, nation.getName() + "has dismissed you as a sub!");
        }

        return true;
    }
}
