// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





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
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.guild.ChangeRankMsg;
import engine.net.client.msg.guild.GuildInfoMsg;
import engine.net.client.msg.guild.GuildListMsg;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

public class ChangeRankHandler extends AbstractClientMsgHandler {

    public ChangeRankHandler() {
        super(ChangeRankMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

        ChangeRankMsg msg;
        PlayerCharacter sourcePlayer;
        PlayerCharacter targetPlayer;

        msg = (ChangeRankMsg) baseMsg;
        sourcePlayer = origin.getPlayerCharacter();

        targetPlayer = (PlayerCharacter) DbManager.getObject(GameObjectType.PlayerCharacter, msg.getPlayerUUID());

        if (msg.getPlayerUUID() == 0)
            targetPlayer = sourcePlayer;

	//updateSource will generate a new promote/demote screen for sourcePlayer
        //updateTarget will sync guild info for the target and all players in range
        
        boolean updateSource = false, updateTarget = false;

        if (sourcePlayer == null ||GuildStatusController.isInnerCouncil(sourcePlayer.getGuildStatus()) == false)
            return true;

        if (targetPlayer != null && (targetPlayer.getGuild().equals(sourcePlayer.getGuild()) == false))
            return true;

        String targetName = null;
        boolean isMale;

        if (msg.getPreviousRank() != msg.getNewRank()) {
            Enum.GuildType t = Enum.GuildType.getGuildTypeFromInt(sourcePlayer.getGuild().getCharter());

            if (targetPlayer != null) {
                targetPlayer.setGuildTitle(msg.getNewRank());

                targetName = targetPlayer.getFirstName();
                isMale = targetPlayer.getRace().getRaceType().getCharacterSex().equals(Enum.CharacterSex.MALE);
            } else {
                DbManager.GuildQueries.UPDATE_GUILD_RANK_OFFLINE(msg.getPlayerUUID(), msg.getNewRank(), sourcePlayer.getGuild().getObjectUUID());

                targetName = PlayerCharacter.getFirstName(msg.getPlayerUUID());
                isMale = true;
            }

            ChatManager.chatGuildInfo(sourcePlayer.getGuild(),
                    targetName + " has been "
                    + ((msg.getNewRank() > msg.getPreviousRank()) ? "pro" : "de") + "moted to "
                    + t.getRankForGender(msg.getNewRank(), isMale) + '!');
            updateSource = true;
        }

        //These values record a change, not the new value...
        boolean icUpdate = false, recruitUpdate = false, taxUpdate = false;

        //Handle the offline case..
        if (targetPlayer == null) {
            int updateMask = DbManager.GuildQueries.UPDATE_GUILD_STATUS_OFFLINE(msg.getPlayerUUID(),
                    msg.getIc() > 0,
                    msg.getRec() > 0,
                    msg.getTax() > 0,
                    sourcePlayer.getGuild().getObjectUUID());

            //These values come from the updateIsStatusOffline function
            icUpdate = (updateMask & 4) > 0;
            recruitUpdate = (updateMask & 2) > 0;
            taxUpdate = (updateMask & 1) > 0;

            if (targetName == null && updateMask > 0)
                targetName = PlayerCharacter.getFirstName(msg.getPlayerUUID());
        } else {
            icUpdate = (GuildStatusController.isInnerCouncil(targetPlayer.getGuildStatus()) != (msg.getIc() > 0)) && GuildStatusController.isGuildLeader(sourcePlayer.getGuildStatus());
            recruitUpdate = (GuildStatusController.isRecruiter(targetPlayer.getGuildStatus()) != (msg.getRec() > 0)) && GuildStatusController.isGuildLeader(sourcePlayer.getGuildStatus());
            taxUpdate = (GuildStatusController.isTaxCollector(targetPlayer.getGuildStatus()) != (msg.getTax() > 0)) && GuildStatusController.isGuildLeader(sourcePlayer.getGuildStatus());

            //This logic branch only executes if targetPlayer has passed a null check...
            if (icUpdate){
            	 targetPlayer.setInnerCouncil(msg.getIc() > 0);
            	targetPlayer.setFullMember(true);
            	targetPlayer.incVer();
            }

            if (recruitUpdate)
                targetPlayer.setRecruiter(msg.getRec() > 0);

            if (taxUpdate)
                targetPlayer.setTaxCollector(msg.getTax() > 0);

            if (targetName == null)
                targetName = targetPlayer.getFirstName();
        }

        if (icUpdate) {
            ChatManager.chatGuildInfo(sourcePlayer.getGuild(),
                    (msg.getIc() > 0)
                    ? targetName + " has been appointed to the inner council."
                    : targetName + " is no longer a member of the inner council.");

            updateSource = true;
            updateTarget = true;
        }

        if (recruitUpdate) {
            updateSource = true;
            updateTarget = true;
        }

        if (taxUpdate) {
            updateSource = true;
            updateTarget = true;
        }

        if (targetPlayer != null) {
            targetPlayer.incVer();
            if (updateTarget)
                DispatchMessage.sendToAllInRange(targetPlayer, new GuildInfoMsg(targetPlayer, targetPlayer.getGuild(), 2));
        }

        if (updateSource) {

            Dispatch dispatch = Dispatch.borrow(sourcePlayer, new GuildInfoMsg(sourcePlayer, sourcePlayer.getGuild(), 2));
            DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

            dispatch = Dispatch.borrow(sourcePlayer, new GuildListMsg(sourcePlayer.getGuild()));
            DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

        }

        return true;

    }

}
