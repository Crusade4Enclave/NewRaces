// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.gameManager;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.InterestManagement.WorldGrid;
import engine.db.archive.BaneRecord;
import engine.db.archive.PvpRecord;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.MessageDispatcher;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.net.client.msg.chat.*;
import engine.net.client.msg.commands.ClientAdminCommandMsg;
import engine.objects.*;
import engine.server.MBServerStatics;
import engine.server.world.WorldServer;
import engine.session.Session;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public enum ChatManager {

    CHATMANAGER;

    // Sending a quantity of (FLOOD_QTY_THRESHOLD) messages within
    // (FLOOD_TIME_THRESHOLD) ms will flag as a flood message
    // Example, set to 3 & 2000 to flag the 3rd message within 2000 ms.

    private static final int FLOOD_QTY_THRESHOLD = 3;
    private static final int FLOOD_TIME_THRESHOLD = 2000;
    private static final String FLOOD_USER_ERROR = "You talk too much!";
    private static final String SILENCED = "You find yourself mute!";
    private static final String UNKNOWN_COMMAND = "No such command.";
    /**
     * This method used when handling a ChatMsg received from the network.
     */
    public static void handleChatMsg(Session sender, AbstractChatMsg msg) {

        if (msg == null) {
            Logger.warn(
                    "null message: ");
            return;
        }

        if (sender == null) {
            Logger.warn(
                    "null sender: ");
            return;
        }

        PlayerCharacter pc = sender.getPlayerCharacter();

        if (pc == null) {
            Logger.warn(
                    "invalid sender: ");
            return;
        }

        Protocol protocolMsg = msg.getProtocolMsg();

        // Flood control, implemented per channel

        boolean isFlood = false;
        long curMsgTime = System.currentTimeMillis();
        long checkTime = pc.chatFloodTime(protocolMsg.opcode, curMsgTime, FLOOD_QTY_THRESHOLD - 1);

        if ((checkTime > 0L) && (curMsgTime - checkTime < FLOOD_TIME_THRESHOLD))
            isFlood = true;

        switch (protocolMsg) {
            case CHATSAY:
                ChatManager.chatSay(pc, msg.getMessage(), isFlood);
                return;
            case CHATCSR:
                ChatManager.chatCSR(msg);
                return;
            case CHATTELL:
                ChatTellMsg ctm = (ChatTellMsg) msg;
                ChatManager.chatTell(pc, ctm.getTargetName(), ctm.getMessage(), isFlood);
                return;
            case CHATSHOUT:
                ChatManager.chatShout(pc, msg.getMessage(), isFlood);
                return;
            case CHATGUILD:
                ChatManager.chatGuild(pc, (ChatGuildMsg) msg);
                return;
            case CHATGROUP:
                ChatManager.chatGroup(pc, (ChatGroupMsg) msg);
                return;
            case CHATIC:
                ChatManager.chatIC(pc, (ChatICMsg) msg);
                return;
            case LEADERCHANNELMESSAGE:
            	ChatManager.chatGlobal(pc, msg.getMessage(), isFlood);
            	return;
            case GLOBALCHANNELMESSAGE:
            case CHATPVP:
            case CHATCITY:
            case CHATINFO:
            case SYSTEMBROADCASTCHANNEL:
            default:
        }

    }

    /*
     * Channels
     */
    /*
     * CSR
     */
    public static void chatCSR(AbstractChatMsg msg) {
        PlayerCharacter sender = (PlayerCharacter) msg.getSource();
        if (sender == null)
            return;
        //		if (promotionClass == null)
        //			return false;
        //		int promotionClassID = promotionClass.getUUID();
        //		switch (promotionClassID) {
        //		case 2901:
        //		case 2902:
        //		case 2903:
        //		case 2904:
        //		case 2910:
        //			return true;
        //		}
        //		return false;
        if (!sender.isCSR) {
            ChatManager.chatSystemError(sender, UNKNOWN_COMMAND);
            return;
        }
        ArrayList<AbstractWorldObject> distroList = new ArrayList<>();
        for (PlayerCharacter pc : SessionManager
                .getAllActivePlayerCharacters()) {
            if (pc.getAccount().status.equals(Enum.AccountStatus.BANNED) == false)
                distroList.add(pc);
        }
        // Send dispatch to each player

        for (AbstractWorldObject abstractWorldObject : distroList) {
            PlayerCharacter playerCharacter = (PlayerCharacter) abstractWorldObject;
            Dispatch dispatch = Dispatch.borrow(playerCharacter, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
        }
    }

    public static boolean testSilenced(PlayerCharacter pc) {

        PlayerBonuses bonus = pc.getBonuses();

        if (bonus != null && bonus.getBool(ModType.Silenced, SourceType.None)) {
            ChatManager.chatSayError(pc, SILENCED);
            return true;
        }
        return false;
    }

    /*
     * Say
     */
    public static void chatSay(AbstractWorldObject player, String text, boolean isFlood) {

        PlayerCharacter pcSender = null;

        if (player.getObjectType() == GameObjectType.PlayerCharacter)
            pcSender = (PlayerCharacter) player;

        // Parser eats all dev commands

        if (isFlood) {
            ChatManager.chatSayError(pcSender, FLOOD_USER_ERROR);
            return;
        }

        if (ChatManager.isDevCommand(text) == true) {
            ChatManager.processDevCommand(player, text);
            return;
        }

        if (ChatManager.isUpTimeRequest(text) == true) {
            sendSystemMessage(pcSender, WorldServer.getUptimeString());
            return;
        }

        if (ChatManager.isVersionRequest(text) == true) {
            sendSystemMessage(pcSender,  ConfigManager.MB_WORLD_GREETING.getValue());
            return;
        }

        if (ChatManager.isNetStatRequest(text) == true) {
            sendSystemMessage(pcSender, MessageDispatcher.getNetstatString());
            return;
        }

        // Needs to be refactored

        if (ChatManager.isPopulationRequest(text) == true) {
            sendSystemMessage(pcSender, SimulationManager.getPopulationString());
            return;
        }

        if (ChatManager.isPvpRequest(text) == true) {
            sendSystemMessage(pcSender, PvpRecord.getPvpHistoryString(pcSender.getObjectUUID()));
            return;
        }

        if (ChatManager.isBaneRequest(text) == true) {
            sendSystemMessage(pcSender, BaneRecord.getBaneHistoryString());
            return;
        }

        if (pcSender != null && testSilenced(pcSender))
            return;

        // Make the Message
        ChatSayMsg chatSayMsg = new ChatSayMsg(player, text);
        DispatchMessage.dispatchMsgToInterestArea(pcSender, chatSayMsg, Enum.DispatchChannel.SECONDARY, MBServerStatics.SAY_RANGE, true, true);

    }

    public static void sendSystemMessage(PlayerCharacter targetPlayer, String messageString) {

        ChatSystemMsg msg = new ChatSystemMsg(null, messageString);
        msg.setMessageType(4);
        msg.setChannel(engine.Enum.ChatChannelType.SYSTEM.getChannelID());
        Dispatch dispatch = Dispatch.borrow(targetPlayer, msg);
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
    }

    /*
     * Shout
     */
    public static void chatShout(AbstractWorldObject sender, String text,
                          boolean isFlood) {

        PlayerCharacter pcSender = null;

        if (sender.getObjectType().equals(GameObjectType.PlayerCharacter))
            pcSender = (PlayerCharacter) sender;

        if (isFlood) {
            ChatManager.chatSayError(pcSender, FLOOD_USER_ERROR);
            return;
        }

        if (pcSender != null && testSilenced(pcSender))
            return;

        // Make the Message
        ChatShoutMsg msg = new ChatShoutMsg(sender, text);
        DispatchMessage.dispatchMsgToInterestArea(pcSender, msg, engine.Enum.DispatchChannel.SECONDARY, MBServerStatics.SHOUT_RANGE, true, true);

    }
    
    public static void chatGlobal(PlayerCharacter sender, String text,
            boolean isFlood) {

PlayerCharacter pcSender = null;

if (sender.getObjectType().equals(GameObjectType.PlayerCharacter))
pcSender = (PlayerCharacter) sender;

if (isFlood) {
ChatManager.chatSayError(pcSender, FLOOD_USER_ERROR);
return;
}

if (pcSender != null && testSilenced(pcSender))
return;

// Make the Message
ChatGlobalMsg msg = new ChatGlobalMsg(sender, text);
DispatchMessage.dispatchMsgToAll(sender, msg, true);

}

    /*
     * Tell
     */
    public static void chatTell(AbstractWorldObject sender, String recipient,
                         String text, boolean isFlood) {
        if (text.isEmpty())
            return;

        PlayerCharacter pcSender = null;

        if (sender.getObjectType().equals(GameObjectType.PlayerCharacter))
            pcSender = (PlayerCharacter) sender;

        if (pcSender != null && testSilenced(pcSender))
            return;

        PlayerCharacter pcRecip = SessionManager
                .getPlayerCharacterByLowerCaseName(recipient);

        if (pcRecip != null) {
            if (isFlood) {
                ChatManager.chatTellError(pcSender, FLOOD_USER_ERROR);
                return;
            }

            ChatManager.chatTell(sender, pcRecip, text);
        } else
            ChatManager.chatTellError((PlayerCharacter) sender,
                    "There is no such player.");

    }

    public static void chatTell(AbstractWorldObject sender,
                         AbstractWorldObject recipient, String text) {

        PlayerCharacter pcSender = null;

        if (sender.getObjectType().equals(GameObjectType.PlayerCharacter))
            pcSender = (PlayerCharacter) sender;

        if (pcSender != null && testSilenced(pcSender))
            return;

        // Verify we are sending to a PlayerCharacter
        PlayerCharacter pcRecip;
        if (recipient.getObjectType().equals(GameObjectType.PlayerCharacter))
            pcRecip = (PlayerCharacter) recipient;
        else
            return;

        ClientConnection cc = SessionManager.getClientConnection(
                pcRecip);

        // make sure we have a good ClientConnection
        if (cc != null) {

            ChatTellMsg chatTellMsg = new ChatTellMsg(sender, pcRecip, text);

            // Don't send to recipient if they're ignoring sender
            if (!pcRecip.isIgnoringPlayer(pcSender)) {
                // Send dispatch to each player

                Dispatch dispatch = Dispatch.borrow(pcRecip, chatTellMsg);
                DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
            }

            // Also send /tell to sender
            if (pcSender != null) {
                Dispatch dispatch = Dispatch.borrow(pcSender, chatTellMsg);
                DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
            }

        } else
            ChatManager.chatTellError(pcSender,
                    "There is no such player.");
    }

    public static void chatGuild(PlayerCharacter sender, ChatGuildMsg msg) {

        // Verify sender has PlayerCharacter
        if (sender == null)
            return;

        if (testSilenced(sender))
            return;

        // Verify player is in guild and get guild
        Guild guild = sender.getGuild();
        if (guild == null || guild.getObjectUUID() == 0)
            return;

        // Get Distro List for guild
        ArrayList<PlayerCharacter> distroList = SessionManager.getActivePCsInGuildID(guild.getObjectUUID());
        if (msg.getUnknown02() == 5) {

            Guild nation = guild.getNation();
            if (nation == null)
                return;
            distroList = ChatManager.getNationListChat(nation, sender);
        }

        // Check the DistroList size
        if (distroList.size() < 1) {
            ChatManager.chatGuildError(sender,
                    "You find yourself mute!");
            Logger.error("Guild Chat returned a list of Players <1 in length.");
            return;
        }

        // Make the Message

        // make the ed message
        ChatGuildMsg chatGuildMsg = new ChatGuildMsg(msg);

        chatGuildMsg.setSourceType(sender.getObjectType().ordinal());
        chatGuildMsg.setSourceID(sender.getObjectUUID());
        chatGuildMsg.setSourceName(sender.getFirstName());
        chatGuildMsg.setUnknown03(MBServerStatics.worldMapID); // Server ID
        chatGuildMsg.setUnknown04(sender.getGuild() != null ? sender.getGuild()
                .getCharter() : 0); // Charter?
        chatGuildMsg.setUnknown05(GuildStatusController.getTitle(sender.getGuildStatus())); // Title?
        chatGuildMsg.setUnknown06(sender.getRace().getRaceType().getCharacterSex().equals(Enum.CharacterSex.MALE) ? 1 : 2); // isMale?, seen 1 and 2

        // Send dispatch to each player

        for (AbstractWorldObject abstractWorldObject : distroList) {
            PlayerCharacter playerCharacter = (PlayerCharacter) abstractWorldObject;
            Dispatch dispatch = Dispatch.borrow(playerCharacter, chatGuildMsg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
        }

    }

    public static void chatIC(PlayerCharacter sender, ChatICMsg msg) {

        // Verify sender has PlayerCharacter
        if (sender == null)
            return;

        if (testSilenced(sender))
            return;

        // Verify player is in guild and get guild
        Guild guild = sender.getGuild();
        if (guild == null || guild.getObjectUUID() == 0)
            return;

        //Verify player is an IC

        if (GuildStatusController.isInnerCouncil(sender.getGuildStatus()) == false)
            return;

        // Get Distro List for guild
        HashSet<AbstractWorldObject> distroList = ChatManager.getGuildICList(guild, sender);

        // Check the DistroList size
        if (distroList.size() < 1) {
            ChatManager.chatICError(sender, "You find yourself mute!");
            Logger.error("Guild Chat returned a list of Players <1 in length.");
            return;
        }

        // TODO Hrm, are we modifying the incoming message or making a response?
        // Not anymore we aren't!

        // Create new outgoing message

        ChatICMsg chatICMsg = new ChatICMsg(msg);

        chatICMsg.setSourceType(sender.getObjectType().ordinal());
        chatICMsg.setSourceID(sender.getObjectUUID());
        chatICMsg.setSourceName(sender.getFirstName());
        chatICMsg.setUnknown02(MBServerStatics.worldMapID); // Server ID
        chatICMsg.setUnknown03(GuildStatusController.getRank(sender.getGuildStatus())); // correct?
        chatICMsg.setUnknown04(GuildStatusController.getTitle(sender.getGuildStatus())); // correct?
        chatICMsg.setUnknown05(2); // unknown, seen 1 and 2


        // Send dispatch to each player

        for (AbstractWorldObject abstractWorldObject : distroList) {
            PlayerCharacter playerCharacter = (PlayerCharacter) abstractWorldObject;
            Dispatch dispatch = Dispatch.borrow(playerCharacter, chatICMsg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
        }

    }

    private static boolean isVersionRequest(String text) {
        return text.equalsIgnoreCase("lua_version()");
    }

    private static boolean isPvpRequest(String text) {
        return text.equalsIgnoreCase("lua_pvp()");
    }

    private static boolean isBaneRequest(String text) {
        return text.equalsIgnoreCase("lua_banes()");
    }

    public static void chatGroup(PlayerCharacter sender, ChatGroupMsg msg) {

        // Verify sender has PlayerCharacter
        if (sender == null)
            return;

        if (testSilenced(sender))
            return;

        // Verify player is in guild and get guild

        Group group = GroupManager.getGroup(sender);

        if (group == null)
            return;

        // Get Distro List for guild
        HashSet<AbstractWorldObject> distroList = ChatManager.getGroupList(group, sender);

        // Check the DistroList size
        if (distroList.size() < 1) {
            ChatManager.chatGroupError(sender,
                    "You find yourself mute!");
            Logger.error("Group Chat returned a list of Players <1 in length.");
            return;
        }

        // Make the Message

        ChatGroupMsg chatGroupMsg = new ChatGroupMsg(msg);

        chatGroupMsg.setSourceType(sender.getObjectType().ordinal());
        chatGroupMsg.setSourceID(sender.getObjectUUID());
        chatGroupMsg.setSourceName(sender.getFirstName());
        chatGroupMsg.setUnknown02(MBServerStatics.worldMapID); // Server ID


        // Send dispatch to each player

        for (AbstractWorldObject abstractWorldObject : distroList) {
            PlayerCharacter playerCharacter = (PlayerCharacter) abstractWorldObject;
            Dispatch dispatch = Dispatch.borrow(playerCharacter, chatGroupMsg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
        }

    }

    public static void GuildEnterWorldMsg(PlayerCharacter sender,
                                   ClientConnection origin) {
        // Verify sender has PlayerCharacter
        if (sender == null)
            return;
        // Verify player is in guild and get guild
        Guild guild = sender.getGuild();
        if (guild == null || guild.getObjectUUID() == 0)
            return;
        // Get Distro List for guild
        HashSet<AbstractWorldObject> distroList = ChatManager.getGuildList(guild, null);
        // Check the DistroList size
        if (distroList.size() < 1) {
            Logger.error("Guild EnterWorldMsg returned a list of Players <1 in length.");
            return;
        }

        // Make and send enter world message
        GuildEnterWorldMsg msg = new GuildEnterWorldMsg(sender);
        msg.setName(sender.getFirstName());
        msg.setGuildTitle(GuildStatusController.getTitle(sender.getGuildStatus()));
        msg.setGuildUUID(guild.getObjectUUID());
        msg.setCharter(guild.getCharter());

        // Send dispatch to each player

        for (AbstractWorldObject abstractWorldObject : distroList) {
            PlayerCharacter playerCharacter = (PlayerCharacter) abstractWorldObject;
            Dispatch dispatch = Dispatch.borrow(playerCharacter, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
        }
    }

    public static void chatPeekSteal(PlayerCharacter sender, AbstractCharacter tar, Item item, boolean success, boolean detect, int amount) {
        if (sender == null || tar == null)
            return;

        PlayerCharacter target = null;

        if (tar.getObjectType().equals(GameObjectType.PlayerCharacter))
            target = (PlayerCharacter) tar;

        // Get Distro List
        HashSet<AbstractWorldObject> distroList = WorldGrid
                .getObjectsInRangePartial(sender.getLoc(),
                        MBServerStatics.SAY_RANGE, MBServerStatics.MASK_PLAYER);

        // Check the DistroList size
        if (distroList.size() < 1)
            return;

        //remove Thief and Victim from other's list
        int size = distroList.size();
        for (int i = size - 1; i > -1; i--) {
            AbstractWorldObject awo = (AbstractWorldObject) distroList.toArray()[i];
            if (awo.getObjectUUID() == sender.getObjectUUID())
                distroList.remove(awo);
            else if (awo.getObjectUUID() == tar.getObjectUUID())
                distroList.remove(awo);

        }

        String textToThief = "";
        String textToVictim = "";
        String textToOthers = "";

        if (item != null) //Steal
            if (success) {
                String name = "";
                if (item.getItemBase() != null)
                    if (item.getItemBase().getUUID() == 7)
                        name = amount + " gold ";
                    else {
                        String vowels = "aeiou";
                        String iName = item.getItemBase().getName();
                        if (iName.length() > 0)
                            if (vowels.indexOf(iName.substring(0, 1).toLowerCase()) >= 0)
                                name = "an " + iName + ' ';
                            else
                                name = "a " + iName + ' ';
                    }
                textToThief = "You have stolen " + name + "from " + tar.getFirstName() + '!';
                textToVictim = sender.getFirstName() + "is caught with thier hands in your pocket!";
                //textToOthers = sender.getFirstName() + " steals " + name + "from " + target.getFirstName() + ".";
            } else
                textToThief = "Your attempt at stealing failed..."; //textToVictim = sender.getFirstName() + " fails to steal from you.";
            //textToOthers = sender.getFirstName() + " fails to steal from " + target.getFirstName() + ".";
        else //Peek
            if (success) {
                if (detect) {
                    textToThief = tar.getFirstName() + " catches you peeking through their belongings!";
                    textToVictim = "You catch " + sender.getFirstName() + " peeking through your belongings!";
                }
            } else {
                textToThief = "Your attempt at peeking failed...";
                textToVictim = sender.getFirstName() + " is seen eyeing up your backpack...";
                textToOthers = sender.getFirstName() + " is seen eyeing up the backpack of " + tar.getFirstName() + "...";
            }

        //Send msg to thief
        HashSet<AbstractWorldObject> senderList = new HashSet<>();
        senderList.add(sender);
        if (!textToThief.isEmpty())
            ChatManager.chatSystemSend(senderList, textToThief, 1, 2);

        if (target != null && !textToVictim.isEmpty()) {
        	HashSet<AbstractWorldObject> victimList = new HashSet<>();
            victimList.add(target);
            ChatManager.chatSystemSend(victimList, textToVictim, 1, 2);
        }

        //Send msg to Others in range\
        if (!textToOthers.isEmpty())
            ChatManager.chatSystemSend(distroList, textToOthers, 1, 2);
    }

    // Send System Announcement as a flash at top of screen
    public static void chatSystemFlash(String text) {

        chatSystem(null, text, 2, 1);
    }

    public static void chatSystemChannel(String text) {
        chatSystem(null, text, 1, 4); // Type 4 simply displays text as is
    }

    // Send Error Message to player
    public static void chatSystemError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 1);
    }

    public static void chatSystemInfo(PlayerCharacter pc, String text) {
        sendInfoMsgToPlayer(pc, text, 1);
    }

    public static void chatCommanderError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 3);
    }

    public static void chatNationError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 5);
    }

    public static void chatLeaderError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 6);
    }

    public static void chatShoutError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 7);
    }

    public static void chatInfoError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 10);
    }

    public static void chatGuildError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 12);
    }

    public static void chatICError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 13);
    }

    public static void chatGroupError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 14);
    }

    public static void chatCityError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 15);
    }

    public static void chatSayError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 16);
    }

    public static void chatEmoteError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 17);
    }

    public static void chatTellError(PlayerCharacter pc, String text) {
        sendErrorMsgToPlayer(pc, text, 19);
    }

    // Send Info Message to channels
    public static void chatCommanderInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 3, 2);
    }

    public static void chatNationInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 5, 2);
    }

    public static void chatNationInfo(Guild nation, String text) {
        chatSystemGuild(nation, text, 5, 2);
    }

    public static void chatLeaderInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 6, 2);
    }

    public static void chatShoutInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 7, 2);
    }

    public static void chatInfoInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 10, 2);
    }

    public static void chatGuildInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 12, 2);
    }

    public static void chatICInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 13, 2);
    }

    public static void chatGroupInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 14, 2);
    }

    public static void chatCityInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 15, 2);
    }

    public static void chatSayInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 16, 2);
    }

    public static void chatEmoteInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 17, 2);
    }

    public static void chatTellInfo(PlayerCharacter pc, String text) {
        chatSystem(pc, text, 19, 2);
    }

    public static void chatGroupInfoCanSee(PlayerCharacter pc, String text) {
    	HashSet<AbstractWorldObject> distroList = null;

        Group group = GroupManager.getGroup(pc);
        if (group != null) {
            distroList = ChatManager.getGroupList(group, pc);
            if (distroList != null) {
                Iterator<AbstractWorldObject> it = distroList.iterator();
                while (it.hasNext()) {
                    AbstractWorldObject awo = it.next();
                    if (!(awo.getObjectType().equals(GameObjectType.PlayerCharacter)))
                        it.remove();
                    else {
                        PlayerCharacter pcc = (PlayerCharacter) awo;
                        if (pcc.getSeeInvis() < pc.getHidden())
                            it.remove();
                    }
                }
            }
        }
        ChatManager.chatSystemSend(distroList, text, 14, 2);
    }

    // Send MOTD Message to channels
    public static void chatNationMOTD(PlayerCharacter pc, String text) {
        chatNationMOTD(pc, text, false);
    }

    public static void chatNationMOTD(PlayerCharacter pc, String text, boolean all) {
        if (all) // Send to all Nation

            chatSystem(pc, text, 5, 3);
        else // Send to just pc

            chatSystemMOTD(pc, text, 5, 3);
    }

    public static void chatGuildMOTD(PlayerCharacter pc, String text) {
        chatGuildMOTD(pc, text, false);
    }

    public static void chatGuildMOTD(PlayerCharacter pc, String text, boolean all) {
        if (all) // Send to all Guild

            chatSystem(pc, text, 12, 3);
        else // Send to just pc

            chatSystemMOTD(pc, text, 12, 3);
    }

    public static void chatICMOTD(PlayerCharacter pc, String text) {
        chatICMOTD(pc, text, false);
    }

    public static void chatICMOTD(PlayerCharacter pc, String text, boolean all) {
        if (all) // Send to all IC's

            chatSystem(pc, text, 13, 3);
        else // Send to just pc

            chatSystemMOTD(pc, text, 13, 3);
    }

    // Send Info Message to guild channel based on guild
    public static void chatGuildInfo(Guild guild, String text) {
    	HashSet<AbstractWorldObject> distroList = null;
        if (guild != null)
            distroList = ChatManager.getGuildList(guild, null);
        ChatManager.chatSystemSend(distroList, text, 12, 2);
    }

    public static void chatSystemMOTD(PlayerCharacter sender, String text,
                                      int channel, int messageType) {
    	HashSet<AbstractWorldObject> distroList = ChatManager.getOwnPlayer(sender);
        ChatManager.chatSystemSend(distroList, text, channel, messageType);
    }

    public static void chatPVP(String text) {
        // Create message
        ChatPvPMsg msg = new ChatPvPMsg(null, text);
        DispatchMessage.dispatchMsgToAll(msg);
    }

    public static void chatInfo(String text) {
    	HashSet<AbstractWorldObject> distroList = ChatManager.getAllPlayers(null);
        chatSystemSend(distroList, text, 1, 2);
    }

    public static ChatSystemMsg CombatInfo(AbstractWorldObject source, AbstractWorldObject target) {
        String text = "The " + target.getName() + " attacks " + source.getName();
        ChatSystemMsg msg = new ChatSystemMsg(null, text);
        msg.setChannel(20);
        msg.setMessageType(2);
        return msg;
    }

    public static void chatSystem(PlayerCharacter sender, String text, int channel,
                                  int messageType) {
    	HashSet<AbstractWorldObject> distroList = null;
        if (channel == 1) // System Channel Message

            distroList = ChatManager.getAllPlayers(sender);
        else if (channel == 2) // System Flash, send to everyone

            distroList = ChatManager.getAllPlayers(sender);
        else if (channel == 3) { // Commander Channel
        } else if (channel == 5) { // Nation Channel, get Nation list
            Guild guild = sender.getGuild();
            if (guild != null) {
                Guild nation = guild.getNation();
                if (nation != null)
                    if (nation.getObjectUUID() != 0) // Don't /nation to errant
                        // nation

                        distroList = ChatManager.getNationList(nation, sender);
            }
        } else if (channel == 6) { // Leader Channel
        } else if (channel == 7) // Shout Channel
            distroList = ChatManager.getOwnPlayer(sender);
        else if (channel == 10) // Info Channel
            distroList = getOwnPlayer(sender);
        else if (channel == 12) { // guild Channel, get Guild list
            Guild guild = sender.getGuild();
            if (guild != null)
                if (guild.getObjectUUID() != 0) // Don't /guild to errant guild

                    distroList = ChatManager.getGuildList(guild, sender);
        } else if (channel == 13) { // IC Channel, get Guild IC list
            Guild guild = sender.getGuild();
            if (guild != null)
                if (guild.getObjectUUID() != 0) // Don't /IC to errant guild

                    distroList = ChatManager.getGuildICList(guild, sender);
        } else if (channel == 14) { // Group Channel, get group list
            Group group = GroupManager.getGroup(sender);
            if (group != null)
                distroList = ChatManager.getGroupList(group, sender);
        } else if (channel == 15) { // City Channel, get people bound to city
            // list
        } else if (channel == 16) // Say Channel
            distroList = ChatManager.getOwnPlayer(sender);
        else if (channel == 17) { // Emote Channel, get say List
        } else if (channel == 19) // Tell Channel
            distroList = ChatManager.getOwnPlayer(sender);
        else
            return;
        ChatManager.chatSystemSend(distroList, text, channel, messageType);
    }

    public static void chatSystemGuild(Guild sender, String text, int channel,
                                       int messageType) {
    	HashSet<AbstractWorldObject> distroList = null;

        if (channel == 5) { // Nation Channel, get Nation list
            if (sender != null) {
                Guild nation = sender.getNation();
                if (nation != null)
                    if (nation.getObjectUUID() != 0) // Don't /nation to errant
                        // nation

                        distroList = ChatManager.getNationList(nation, null);
            }
        } else if (channel == 12) { // guild Channel, get Guild list
            if (sender != null)
                if (sender.getObjectUUID() != 0) // Don't /guild to errant guild

                    distroList = ChatManager.getGuildList(sender, null);
        } else if (channel == 13) { // IC Channel, get Guild IC list
            if (sender != null)
                if (sender.getObjectUUID() != 0) // Don't /IC to errant guild

                    distroList = ChatManager.getGuildICList(sender, null);
        } else
            return;
        ChatManager.chatSystemSend(distroList, text, channel, messageType);

    }

    public static void chatSystemSend(HashSet<AbstractWorldObject> distroList,
                                      String text, int channel, int messageType) {
        // verify someone in distroList to send message to
        if (distroList == null)
            return;
        if (distroList.size() < 1)
            return;

        // Create message
        ChatSystemMsg chatSystemMsg = new ChatSystemMsg(null, text);
        chatSystemMsg.setChannel(channel);
        chatSystemMsg.setMessageType(messageType);

        // Send dispatch to each player

        for (AbstractWorldObject abstractWorldObject : distroList) {
            PlayerCharacter playerCharacter = (PlayerCharacter) abstractWorldObject;
            Dispatch dispatch = Dispatch.borrow(playerCharacter, chatSystemMsg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
        }

    }

    // Get distroList for guild
    public static HashSet<AbstractWorldObject> getGuildList(Guild guild, PlayerCharacter source) {
    	HashSet<AbstractWorldObject> distroList = new HashSet<>();

        for (PlayerCharacter playerCharacter : SessionManager.getAllActivePlayerCharacters()) {

            if (Guild.sameGuild(playerCharacter.getGuild(), guild)) {
                if (source != null && playerCharacter.isIgnoringPlayer(source))
                    continue; // dont add if recip has ignored source
                distroList.add(playerCharacter);
            }
        }
        return distroList;
    }

    // Get distroList for guild IC's
    public static HashSet<AbstractWorldObject> getGuildICList(Guild guild, PlayerCharacter source) {

    	HashSet<AbstractWorldObject> distroList = new HashSet<>();

        for (PlayerCharacter pc : SessionManager.getAllActivePlayerCharacters()) {

            if (Guild.sameGuild(pc.getGuild(), guild))
                if (GuildStatusController.isInnerCouncil(pc.getGuildStatus())) {
                    if (source != null && pc.isIgnoringPlayer(source))
                        continue; // dont add if recip has ignored source
                    distroList.add(pc);
                }
        }
        return distroList;
    }

    // Get distroList for group
    public static HashSet<AbstractWorldObject> getGroupList(Group group, PlayerCharacter source) {
    	HashSet<AbstractWorldObject> distroList = new HashSet<>();
        Set<PlayerCharacter> players = group.getMembers();
        for (PlayerCharacter pc : players) {
            if (source != null && pc.isIgnoringPlayer(source))
                continue; // dont add if recip has ignored source
            distroList.add(pc);
        }
        return distroList;
    }

    // Get distroList for nation
    public static HashSet<AbstractWorldObject> getNationList(Guild nation, PlayerCharacter source) {
    	HashSet<AbstractWorldObject> distroList = new HashSet<>();

        for (PlayerCharacter pc : SessionManager.getAllActivePlayerCharacters()) {

            Guild guild = pc.getGuild();

            if (guild != null)
                if (guild.getNation().getObjectUUID() == nation.getObjectUUID()) {
                    if (source != null && pc.isIgnoringPlayer(source))
                        continue; // dont add if recip has ignored source
                    distroList.add(pc);
                }
        }
        return distroList;
    }

    public static ArrayList<PlayerCharacter> getNationListChat(Guild nation, PlayerCharacter source) {
        ArrayList<PlayerCharacter> distroList = new ArrayList<>();

        for (PlayerCharacter pc : SessionManager.getAllActivePlayerCharacters()) {

            Guild guild = pc.getGuild();

            if (guild != null)
                if (guild.getNation().getObjectUUID() == nation.getObjectUUID()) {
                    if (source != null && pc.isIgnoringPlayer(source))
                        continue; // dont add if recip has ignored source
                    distroList.add(pc);
                }
        }
        return distroList;
    }

    // Get distroList for all players
    public static HashSet<AbstractWorldObject> getAllPlayers(PlayerCharacter source) {

    	HashSet<AbstractWorldObject> distroList = new HashSet<>();
        for (PlayerCharacter pc : SessionManager.getAllActivePlayerCharacters()) {
            if (source != null && pc.isIgnoringPlayer(source))
                continue; // dont add if recip has ignored source
            distroList.add(pc);
        }
        return distroList;
    }

    // Get just self for distrList
    public static HashSet<AbstractWorldObject> getOwnPlayer(PlayerCharacter pc) {
        if (pc == null)
            return null;
        HashSet<AbstractWorldObject> distroList = new HashSet<>();
        distroList.add(pc);
        return distroList;
    }

    /*
     * Utils
     */
    // Error Message for type channel
    private static void sendErrorMsgToPlayer(AbstractCharacter player, String message,
                                             int channel) {
        if (player == null)
            return;
        ChatManager.sendSystemMsgToPlayer(player, message, channel, 1);
    }

    // Info Message for type channel
    private static void sendInfoMsgToPlayer(AbstractCharacter player, String message,
                                            int channel) {
        ChatManager.sendSystemMsgToPlayer(player, message, channel, 2);
    }

    // Message of the Day Message for type channel
    //	private void sendMOTDMsgToPlayer(AbstractCharacter player, String message, int channel) {
    //		this.sendSystemMsgToPlayer(player, message, channel, 3);
    //	}
    private static void sendSystemMsgToPlayer(AbstractCharacter player,
                                              String message, int channel, int messageType) {

        PlayerCharacter playerCharacter;

        if (player == null)
            return;

        if (player.getObjectType().equals(GameObjectType.PlayerCharacter) == false) {
            Logger.error("Chat message sent to non player");
            return;
        }

        // Wtf recasting this?  If we're sending chat messages to players
        // or mobiles, then something is really wrong.

        playerCharacter = (PlayerCharacter) player;

        ChatSystemMsg chatSystemMsg = new ChatSystemMsg(null, message);
        chatSystemMsg.setMessageType(messageType); // Error message
        chatSystemMsg.setChannel(channel);

        Dispatch dispatch = Dispatch.borrow(playerCharacter, chatSystemMsg);
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

    }

    private static boolean isDevCommand(String text) {
        return text.startsWith(MBServerStatics.DEV_CMD_PREFIX);
    }

    private static boolean isUpTimeRequest(String text) {
        return text.equalsIgnoreCase("lua_uptime()");
    }

    private static boolean isNetStatRequest(String text) {
        return text.equalsIgnoreCase("lua_netstat()");
    }

    private static boolean isPopulationRequest(String text) {
        return text.equalsIgnoreCase("lua_population()");
    }

    private static boolean processDevCommand(AbstractWorldObject sender, String text) {

        if (sender.getObjectType().equals(GameObjectType.PlayerCharacter)) {

            PlayerCharacter pcSender = (PlayerCharacter) sender;

            // first remove the DEV_CMD_PREFIX
            String[] words = text.split(MBServerStatics.DEV_CMD_PREFIX, 2);

            if (words[1].length() == 0)
                return false;

            // next get the command
            String[] commands = words[1].split(" ", 2);
            String cmd = commands[0].toLowerCase();
            String cmdArgument = "";

            if (commands.length > 1)
                cmdArgument = commands[1].trim();

            AbstractGameObject target = pcSender.getLastTarget();
            // return DevCmd.processDevCommand(pcSender, cmd, cmdArgument,
            // target);
            return DevCmdManager.handleDevCmd(pcSender, cmd,
                    cmdArgument, target);
        }
        return false;
    }

    /**
     * Process an Admin Command, which is a preset command sent from the client
     */
    public static void HandleClientAdminCmd(ClientAdminCommandMsg data,
                                            ClientConnection cc) {

        PlayerCharacter pcSender = SessionManager.getPlayerCharacter(cc);

        if (pcSender == null)
            return;

        Account acct = SessionManager.getAccount(pcSender);

        if (acct == null)
            return;

        // require minimal access to continue
        // specific accessLevel checks performed by the DevCmdManager
        if (acct.status.equals(Enum.AccountStatus.ADMIN) == false) {
            Logger.warn(pcSender.getFirstName() + " Attempted to use a client admin command");
            //wtf?  ChatManager.chatSystemInfo(pcSender, "CHEATER!!!!!!!!!!!!!");
            return;
        }

        // First remove the initial slash
        String d = data.getMsgCommand();
        String[] words = d.split("/", 2);

        if (words[1].length() == 0)
            return;

        // Next get the command
        String[] commands = words[1].split(" ", 2);
        String cmd = commands[0].toLowerCase();
        String cmdArgument = "";

        if (commands.length > 1)
            cmdArgument = commands[1].trim();

        AbstractGameObject target = data.getTarget();

        // Map to a DevCmd
        String devCmd = "";

        if (cmd.compareTo("goto") == 0)
            devCmd = "goto";
        else if (cmd.compareTo("suspend") == 0)
            devCmd = "suspend";
        else if (cmd.compareTo("getinfo") == 0)
            devCmd = "info";
        else if (devCmd.isEmpty()) {
            Logger.info( "Unhandled admin command was used: /"
                    + cmd);
            return;
        }

        DevCmdManager.handleDevCmd(pcSender, devCmd, cmdArgument,
                target);
    }


}
