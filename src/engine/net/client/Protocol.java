package engine.net.client;

/* This class defines Magicbane's application network protocol.
-->  Name / Opcode / Message / Handler
 */

import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.client.handlers.*;
import engine.net.client.msg.*;
import engine.net.client.msg.chat.*;
import engine.net.client.msg.commands.ClientAdminCommandMsg;
import engine.net.client.msg.group.*;
import engine.net.client.msg.guild.*;
import engine.net.client.msg.login.*;
import org.pmw.tinylog.Logger;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public enum Protocol {


    NONE(0x0, null, null),
    ABANDONASSET(0xFDDBB233, AbandonAssetMsg.class, AbandonAssetMsgHandler.class), // AbandonAsset
    ACTIVATECHARTER(0x296C0B22, UseCharterMsg.class, null),// Use Guild Charter
    ACTIVATENPC(0xC9AAE81E, ActivateNPCMessage.class, ActivateNPCMsgHandler.class),
    ACTIVATEPLEDGE(0x5A694DC0, SwearInMsg.class, SwearInHandler.class), // Swear In
    ADDFRIEND(0xCFA1C787,AddFriendMessage.class,null),
    ALLIANCECHANGE(0x0E7D0B57,  AllianceChangeMsg.class, AllianceChangeMsgHandler.class), // Remove From Allies/Enemies List
    ALLYENEMYLIST(0xAEA443FD, AllyEnemyListMsg.class, AllyEnemyListMsgHandler.class),
    ARCCOMBATMODEATTACKING(0xD8B10579,  SetCombatModeMsg.class, null), // Attack From Outside Combat Mode
    ARCHOTZONECHANGE(0xDCFF196F, null, null), //change hotzone
    ARCIGNORELISTUPDATE(0x4B1B17C2, IgnoreListMsg.class, null), //req/show ignore list
    ARCLOGINNOTIFY(0x010FED87, ArcLoginNotifyMsg.class, ArcLoginNotifyMsgHandler.class), //Client Confirms entering world
    ARCMINECHANGEPRODUCTION(0x1EAA993F,  ArcMineChangeProductionMsg.class, null),
    ARCMINETOWERCRESTUPDATE(0x34164D0D, null, null),
    ARCMINEWINDOWAVAILABLETIME(0x6C909DE7, ArcMineWindowAvailableTimeMsg.class, ArcMineWindowAvailableTimeHandler.class),
    ARCMINEWINDOWCHANGE(0x92B2148A, ArcMineWindowChangeMsg.class, MineWindowChangeHandler.class),
    ARCOWNEDMINESLIST(0x59184455, ArcOwnedMinesListMsg.class, null),
    ARCPETATTACK(0x18CD61AD, PetAttackMsg.class, null), // Pet Attack
    ARCPETCMD(0x4E80E001, PetCmdMsg.class, null), // Stop ArcPetAttack, Toggle Assist, Toggle Rest
    ARCPOWERPROJECTILE(0xA2312D3B, null, null),
    ARCPROMPTRECALL(0xE3196B6E, PromptRecallMsg.class, null), //Recall Prompt
    ARCREQUESTTRADEBUSY(0xD4BAB4DF, InvalidTradeRequestMsg.class, null), // Attempt trade with someone who is already trading
    ARCSERVERSTATUS(0x87BA4462, null, null), //Update Server Status
    ARCSIEGESPIRE(0x36A49BC6, ArcSiegeSpireMsg.class, ArcSiegeSpireMsgHandler.class), // Activate/Deactivate Spires
    ARCSUMMON(0xFD816A0A, RecvSummonsRequestMsg.class, null), // Suspect Recv Summons Request
    ARCTRACKINGLIST(0xC89CF08B, TrackWindowMsg.class, null), //Request/Send Track window
    ARCTRACKOBJECT(0x609B6BA2, TrackArrowMsg.class, null), //Send Track Arrow
    ARCUNTRAINABILITY(0x548DBF83, RefineMsg.class, null), //Refine
    ARCUNTRAINLIST(0x38879E90, RefinerScreenMsg.class, null), //Refiner screen
    ARCVIEWASSETTRANSACTIONS(0xBFA476E4, ArcViewAssetTransactionsMsg.class, ArcViewAssetTransactionsMsgHandler.class),
    ASSETSUPPORT(0xc481f89D, AssetSupportMsg.class, AssetSupportMsgHandler.class),
    BANISHMEMBER(0x31AA3368, BanishUnbanishMsg.class, BanishUnbanishHandler.class), // Banish/Unbanish
    BANKINVENTORY(0x32F3F503, ShowBankInventoryMsg.class, null), // ShowCombatInfo Bank Inventory
    BREAKFEALTY(0x479A4C19, BreakFealtyMsg.class, BreakFealtyHandler.class),
    BUYFROMNPC(0xA2B8DFA5, BuyFromNPCMsg.class, null), // Buy Item From NPC
    CANCELGUILDCREATION(0x385EA922, GuildCreationCloseMsg.class, GuildCreationCloseHandler.class), //Close the window
    CHANGEALTITUDE(0x624F08BA, ChangeAltitudeMsg.class, ChangeAltitudeHandler.class), //Change Altitude
    CHANGEGUILDLEADER(0xE40BC95D, ChangeGuildLeaderMsg.class, ChangeGuildLeaderHandler.class),
    CHANNELMUTE(0xC1BDC53A, ChatFilterMsg.class, ChannelMuteMsgHandler.class), //Chat Channels that are turned on
    CHARSELECTSCREEN(0x682C935D, null, null), // Character Selection Screen
    CHATCITY(0x9D402901, ChatCityMsg.class, null), // Chat Channel: /City
    CHATCSR(0x14EBA1C3, ChatCSRMsg.class, null),	//Chat Channel: CSR
    CHATGROUP(0xA895B634, ChatGroupMsg.class, null), // Chat Channel: /group
    CHATGUILD(0xA9D92ED4, ChatGuildMsg.class, null), // Chat Channel: /guild
    CHATIC(0x00A75F35, ChatICMsg.class, null), // Chat Channel: /IC
    CHATINFO(0x9D4B61EB, ChatInfoMsg.class, null), // Chat Channel: /Info
    CHATPVP(0x14EBA570, ChatPvPMsg.class, null), // Chat Channel: PVP
    CHATSAY(0x14EA0393, ChatSayMsg.class, null), // Chat Channel: /say
    CHATSHOUT(0xA8D5B560, ChatShoutMsg.class, null), // Chat Channel: /shout
    CHATTELL(0x9D4AC896, ChatTellMsg.class, null), // Chat Channel: /tell
    CHECKUNIQUEGUILD(0x689097D7, GuildCreationOptionsMsg.class, GuildCreationOptionsHandler.class), // Set Guild Name/Motto in Use Guild Charter
    CITYASSET(0x7cae1678, CityAssetMsg.class, null),
    CITYCHOICE(0x406610BB, CityChoiceMsg.class, CityChoiceMsgHandler.class),
    CITYDATA(0xB8A947D4, WorldObjectMsg.class, null),			//Realm Data - Optional(?)
    CITYZONE(0x254947F2, CityZoneMsg.class, null), //For Creating City Object Clientside(Terraform)/Rename City.
    CLAIMASSET(0x948C62CC, ClaimAssetMsg.class, ClaimAssetMsgHandler.class), // ClaimAsset
    CLAIMGUILDTREE(0xFD1C6442, ClaimGuildTreeMsg.class, ClaimGuildTreeMsgHandler.class),
    CLIENTADMINCOMMAND(0x624EAB5F, ClientAdminCommandMsg.class, null),	//Admin Command
    CLIENTUPDATEVAULT( 0x66EDBECD, UpdateVaultMsg.class, null),
    COMBATMODE(0xFE4BF353, ToggleCombatMsg.class, null), //Toggle Combat mode
    CONFIRMPROMOTE(0x153BB5F9, ConfirmPromoteMsg.class, null),
    COSTTOOPENBANK(0x135BE5E8, AckBankWindowOpenedMsg.class, null), // ACK Bank Window Opened
    CREATECHAR(0x5D18B5C8, CommitNewCharacterMsg.class, null), // Commit New Character,
    CREATEPETITION(0xD489CFED, GuildCreationFinalizeMsg.class, GuildCreationFinalizeHandler.class),	//Confirm guild creation
    CUSTOMERPETITION(0x7F9D7D6D, PetitionReceivedMsg.class, null),
    DELETEOBJECT(0x57F069D8, DeleteItemMsg.class, null), //Delete Item from Inventory
    DESTROYBUILDING(0x3CB6FAD3, DestroyBuildingMsg.class, DestroyBuildingHandler.class), // Destroy Building
    DISBANDGUILD(0x77AABD64, DisbandGuildMsg.class, DisbandGuildHandler.class), //Disband Guild
    DISMISSGUILD(0x8D2D3D61, DismissGuildMsg.class, DismissGuildHandler.class),
    DOORTRYOPEN(0xA83DD8C8, DoorTryOpenMsg.class, DoorTryOpenMsgHandler.class), // Open/Close Door
    ENTERWORLD(0xB9783F85, RequestEnterWorldMsg.class, RequestEnterWorldHandler.class), // Request Enter World
    EQUIP(0x3CB1AF8C, TransferItemFromInventoryToEquipMsg.class, null), // Transfer Item from Inventory to Equip
    EXPERIENCE(0xC57802A7, GrantExperienceMsg.class, null), //TODO rename once identified
    FORGETOBJECTS(0xE307A0E1, UnloadObjectsMsg.class, null), // Unload Objects
    FRIENDACCEPT(0xCA297870,AcceptFriendMsg.class,FriendAcceptHandler.class),
    FRIENDDECLINE(0xF08FC279,DeclineFriendMsg.class,FriendDeclineHandler.class),
    FURNITURE(0xCE7FA503, FurnitureMsg.class, FurnitureHandler.class),
    GAMESERVERIPRESPONSE(0x6C95CF87, GameServerIPResponseMsg.class, null), // Game Server IP Response
    GLOBALCHANNELMESSAGE(0x2bf03fd2, null, null),
    GOLDTOVAULT(0x3ABAEE49, TransferGoldFromInventoryToVaultMsg.class, null), // Transfer Gold from Inventory to Vault
    GROUPDISBAND(0xE2B85AA4, DisbandGroupMsg.class, DisbandGroupHandler.class), //Disband Group
    GROUPFOLLOW(0xC61B0476, FormationFollowMsg.class, FormationFollowHandler.class), //Toggle Follow, set Formation
    GROUPLEADERAPPOINT(0xEF778DD3, AppointGroupLeaderMsg.class, AppointGroupLeaderHandler.class), //Appoint new group leader
    GROUPREMOVE(0x6E50277C, RemoveFromGroupMsg.class, RemoveFromGroupHandler.class), //Remove from Group
    GROUPTREASURE(0x01041C66, ToggleGroupSplitMsg.class, ToggleGroupSplitHandler.class), // Toggle Group Split
    GUILDMEMBERONLINE(0x7B79EB3A, GuildEnterWorldMsg.class, null), // Send Enter World Message to Guild
    GUILDRANKCHANGE(0x0DEFB21F, ChangeRankMsg.class, ChangeRankHandler.class), // Change Rank
    GUILDTREESTATUS(0x4B95FB85, GuildTreeStatusMsg.class, null),
    HIRELINGSERVICE(0xD3D93322,HirelingServiceMsg.class,HirelingServiceMsgHandler.class),
    IGNORE(0xBD8881EE, IgnoreMsg.class, null), //client sent /ignore command
    INITIATETRADEHUDS(0x667D29D8, OpenTradeWindowMsg.class, null), // Open Trade Window
    INVITEGROUP(0x004A2012, GroupInviteMsg.class, GroupInviteHandler.class), // Send/Receive/Deny Group Invite
    INVITEGUILDFEALTY(0x0274D612, InviteToSubMsg.class, InviteToSubHandler.class), // Invite Guild to Swear
    INVITETOGUILD(0x6819062A, InviteToGuildMsg.class, InviteToGuildHandler.class), // Invite player to guild, refuse guild invite
    ITEMHEALTHUPDATE(0xB635F55E, ItemHealthUpdateMsg.class, null), //Update Durability of item
    ITEMPRODUCTION(0x3CCE8E30, ItemProductionMsg.class, ItemProductionMsgHandler.class),
    ITEMTOVAULT(0x3ABE4927, TransferItemFromInventoryToVaultMsg.class, null), // Transfer Item to Vault
    JOINFORPROVINCE(0x1FB369CD, AcceptSubInviteMsg.class, AcceptSubInviteHandler.class), //Response to invite to swear?
    JOINFORSWORN(0xF6A4170F, null, null),
    JOINGROUP(0x7EC5E636, GroupInviteResponseMsg.class, GroupInviteResponseHandler.class), // Accept Group Invite
    JOINGUILD(0xF0C5F2FF, AcceptInviteToGuildMsg.class, AcceptInviteToGuildHandler.class), // Accept guild invite
    KEEPALIVESERVERCLIENT(0x49EE129C, KeepAliveServerClientMsg.class, KeepAliveServerClientHandler.class), // Keep Alive
    LEADERBOARD(0x6F0C1386, LeaderboardMessage.class, null),
    LEADERCHANNELMESSAGE(0x17b306f9, ChatGlobalMsg.class, null),
    LEAVEGROUP(0xD8037303, LeaveGroupMsg.class, LeaveGroupHandler.class), //Leave Group
    LEAVEGUILD(0x1801EA32, LeaveGuildMsg.class, LeaveGuildHandler.class), // Leave Guild
    LEAVEREQUEST(0xC79D775C, LeaveWorldMsg.class, null), //Client Request Leave World
    LEAVEWORLD(0xB801EAEC, null, null), //Response to client for Request Leave World
    LOADCHARACTER(0x5756BC53, null, null), // Load Player/NPC/Mob, other then self
    LOADSTRUCTURE(0xB8A3A654, LoadStructureMsg.class, null), //Load Buildings and World Detail Objects
    LOCKUNLOCKDOOR(0x8D0E8C44, LockUnlockDoorMsg.class, LockUnlockDoorMsgHandler.class), // Lock/Unlock Door
    LOGIN(0x3D51E445, ClientLoginInfoMsg.class, null), // Login Information
    LOGINFAILED(0x47B867F6, null, null), // Login Error
    LOGINTOGAMESERVER(0x77910FDF, LoginToGameServerMsg.class, LoginToGameServerMsgHandler.class), // Login to Game Server
    MANAGECITYASSETS(0xCFF01225, ManageCityAssetsMsg.class, ManageCityAssetMsgHandler.class), // Manage city assets
    MANAGENPC(0x43A273FA, null, null), // Open Hireling Management Page
    MERCHANT(0x3E645EF4, MerchantMsg.class, MerchantMsgHandler.class), // Open Teleport List, Teleport, Open Shrine, Request Boon, open/manage warehouse window
    MINIONTRAINING(0xD355F528, MinionTrainingMessage.class, MinionTrainingMsgHandler.class),
    MODIFYGUILDSTATE(0x38936FEA, ToggleLfgRecruitingMsg.class, null), //Toggle LFGroup/LFGuild/Recruiting
    MOTD(0xEC841E8D, MOTDMsg.class, MOTDEditHandler.class), //Send/Rec Guild/Nation/IC MOTD Message
    MOVECORRECTION(0x47FAD1E3, null, null), //Force move to point?
    MOVEOBJECTTOCONTAINER(0xD1639F7C, LootMsg.class, null), //Send/Recv MoveObjectToContainer Msg
    MOVETOPOINT(0x49EF7241, MoveToPointMsg.class, MoveToPointHandler.class), // Move to point
    NAMEVERIFY(0x1B3BF0B1, null, null), // Invalid Name in Character Creation
    NEWWORLD(0x982E4A77, WorldDataMsg.class, null), // World Data
    OBJECTACTION(0x06855A36, ObjectActionMsg.class, ObjectActionMsgHandler.class), //Use item
    OKCOSTTOOPENBANK(0x6F97A502, null, null),
    OPENFRIENDSCONDEMNLIST(0x49E5FE4F, OpenFriendsCondemnListMsg.class, OpenFriendsCondemnListMsgHandler.class), // Friends/Con   demn/Kill/Death/Heraldry List
    OPENVAULT(0xBE048E50, OpenVaultMsg.class, null), // Open Vault Window
    ORDERNPC(0x61C707B1, OrderNPCMsg.class, OrderNPCMsgHandler.class),
    PASSIVEMESSAGETRIGGER(0x2FF9E2E4, null, null), //PassiveMessageTriggerMsg
    PET(0x624F3D8C, PetMsg.class, null), //Summon Pet?
    PLACEASSET(0x940962DF, PlaceAssetMsg.class, PlaceAssetMsgHandler.class),
    PLAYERDATA(0xB206D352, SendOwnPlayerMsg.class, null), //Enter World, Own Player Data
    PLAYERFRIENDS(0xDDEF9E7D, FriendRequestMsg.class, FriendRequestHandler.class),
    POWER(0x3C97A459, PerformActionMsg.class, null), // REQ / CMD Perform Action
    POWERACTION(0xA0B27EEB, ApplyEffectMsg.class, null), // Apply Effect, add to effects icons
    POWERACTIONDD(0xD43052F8, ModifyHealthMsg.class, null), //Modify Health/Mana/Stamina using power
    POWERACTIONDDDIE(0xC27D446B, null, null), //Modify Health/Mana/Stamina using power and kill target
    POWERTARGNAME(0x5A807CCE, SendSummonsRequestMsg.class, null), // Send Summons Request
    RAISEATTR(0x5EEB65E0, ModifyStatMsg.class, null), // Modify Stat
    RANDOM(0xAC5D0135, RandomMsg.class, null), //RequestSend random roll
    READYTOENTER(0x490E4FE0, EnterWorldReceivedMsg.class, null), //Client Ack Receive Enter World
    REALMDATA(0x2399B775, null, null),			//Realm Data - Optional(?)
    RECOMMENDNATION(0x6D4579E9, RecommendNationMsg.class, RecommendNationMsgHandler.class), // Recommend as Ally/Enemy, error
    RECYCLEPOWER(0x24033B67, RecyclePowerMsg.class, null), //Unlock power for reUse
    REMOVECHAR(0x5D3F9739, DeleteCharacterMsg.class, null), // Delete Character
    REMOVEFRIEND(0xE0D5DB42,RemoveFriendMessage.class,RemoveFriendHandler.class),
    REPAIRBUILDING(0xAF8C2560, RepairBuildingMsg.class, RepairBuildingMsgHandler.class),
    REPAIROBJECT(0x782219CE, RepairMsg.class, null), //Repair Window Req/Ack, RepairObject item Req/Ack
    REQUESTCONTENTS(0xA786B0A2, LootWindowRequestMsg.class, null), // MoveObjectToContainer Window Request
    REQUESTGUILDLIST(0x85DCC6D7, ReqGuildListMsg.class, RequestGuildListHandler.class),
    REQUESTMELEEATTACK(0x98C71545, AttackCmdMsg.class, null), // Attack
    REQUESTMEMBERLIST(0x3235E5EA, GuildControlMsg.class, GuildControlHandler.class), // Part of Promote/Demote, Also Player History
    REQUESTTOOPENBANK(0xF26E453F, null, null), // RequestToOpenBankMsg
    REQUESTTOTRADE(0x4D84259B, TradeRequestMsg.class, null), // Trade Request
    REQUESTTRADECANCEL(0xCB0C5735, RejectTradeRequestMsg.class, null), // Reject RequestToTrade
    REQUESTTRADEOK(0xFFD29841, AcceptTradeRequestMsg.class, null), // Accept Trade Request
    RESETAFTERDEATH(0xFDCBB98F,RespawnMsg.class, null), //Respawn Request/Response
    ROTATEMSG(0x57F2088E, RotateObjectMsg.class, null),
    SAFEMODE(0x9CF3922A, SafeModeMsg.class, null), //Tell client they're in safe mode
    SCALEOBJECT(0xE2B392D9, null, null), // Adjust scale of object
    SELECTCHAR(0x7E6A9338, GameServerIPRequestMsg.class, null), // Game Server IP Request
    SELECTCITY(0x7E6BE630, null, null),
    SELECTSERVER(0x440D28B7, ServerInfoMsg.class, null), // Server Info Request/Response
    SELLOBJECT(0x57111C67, SellToNPCMsg.class, null), //Sell to NPC
    SENDCITYENTRY(0xBC3B5E72, null, null), //Send Teleport/Repledge List
    SENDGUILDENTRY(0x6D5EF164, null, null),
    SENDMEMBERENTRY(0x6949C720, GuildListMsg.class, GuildListHandler.class), // ShowCombatInfo guild members list, I think
    SETITEMFLAG(0xE8C1B53B, null, null),
    SETMOTD(0xFD21FC7C, MOTDCommitMsg.class, MOTDCommitHandler.class), //Commit Guild/Nation/IC MOTD Message
    SETOBJVAL(0x08A50FD1, null, null),
    SETRUNE(0x888E7C64, ApplyRuneMsg.class, null),  //Apply Promotion, Stat Rune (maybe disc also)
    SETSELECTEDOBECT(0x64E10938, TargetObjectMsg.class, null), // Target an object
    SHOPINFO(0x267DAB90, SellToNPCWindowMsg.class, null), //open Sell to NPC Window
    SHOPLIST(0x682DAB4D, BuyFromNPCWindowMsg.class, null), // Open Buy From NPC Window
    SHOWCOMBATINFO(0x9BF1E5EA, ShowMsg.class, null), // Request/Response /show
    SHOWVAULTINVENTORY(0xD1FB4842, null, null), // Show Vault Inventory
    SOCIALCHANNEL(0x2BF58FA6, SocialMsg.class, null), // Socials
    STANDARDALERT(0xFA0A24BB, ErrorPopupMsg.class, null), //Popup messages
    STUCK(0x3D04AF3A, StuckCommandMsg.class, null), // /Stuck Command
    SWEARINGUILD(0x389B66B1, SwearInGuildMsg.class, SwearInGuildHandler.class),
    SYNC(0x49ec109f, null, null), //Client/Server loc sync
    SYSTEMBROADCASTCHANNEL(0x2FAD89D1, ChatSystemMsg.class, null), // Chat Channel: System Message
    SYSTEMCHANNEL(0x29BB4D66, ChatSystemChannelMsg.class, null), // Chat System Channel
    TARGETEDACTION(0xB79BA48F, TargetedActionMsg.class, null), //Message sent for attacks
    TAXCITY(0xCD41EAA6, TaxCityMsg.class, TaxCityMsgHandler.class),
    TAXRESOURCES(0x4AD458AF, TaxResourcesMsg.class, TaxResourcesMsgHandler.class),
    TELEPORT(0x23E726EA, TeleportToPointMsg.class, null), // Teleport to point
    TERRITORYCHANGE(0x6B388C8C,TerritoryChangeMessage.class, null), //Hey rich, look what I found? :)
    TOGGLESITSTAND(0x624F3C0F, ToggleSitStandMsg.class, null), //Toggle Sit/Stand
    TRADEADDGOLD(0x654ACB45, AddGoldToTradeWindowMsg.class, null), // Add Gold to Trade Window
    TRADEADDOBJECT(0x55D363E9, AddItemToTradeWindowMsg.class, null), // Add an Item to the Trade Window
    TRADECLOSE(0x5008D7FC, CloseTradeWindowMsg.class, null), // Cancel trade/ACK trade complete
    TRADECONFIRM(0x6911E65E, CommitToTradeMsg.class, null), // Commit to trade
    TRADECONFIRMSTATUS(0x9F85DAFC, null, null), // Other player commit/uncommit/add item
    TRADEUNCONFIRM(0xEBE280E0, UncommitToTradeMsg.class, null), // Uncommit to trade
    TRAINERLIST(0x41FABA62, TrainerInfoMsg.class, null), //Req/Send Trainer Info/Pricing
    TRAINSKILL(0xB0BF68CD, TrainMsg.class, null), //Train skills/powers
    TRANSFERASSET(0x3EA1C4C9, TransferAssetMsg.class, TransferAssetMsgHandler.class), // Transfer Building
    TRANSFERGOLDFROMVAULTTOINVENTORY(0x011D0123, TransferGoldFromVaultToInventoryMsg.class, null), // Transfer Gold from Vault to Inventory
    TRANSFERGOLDTOFROMBUILDING(0x1B1AC8C7, TransferGoldToFromBuildingMsg.class, TransferGoldToFromBuildingMsgHandler.class), // Transfer Gold to/From Building, Transfer Error
    TRANSFERITEMFROMBANK(0x9D04977B, TransferItemFromBankToInventoryMsg.class, null), // Transfer Item from Bank to Inventory
    TRANSFERITEMFROMVAULTTOINVENTORY(0x0119A64D, TransferItemFromVaultToInventoryMsg.class, null), // Transfer Item from Vault to Inventory
    TRANSFERITEMTOBANK(0xD48C46FA, TransferItemFromInventoryToBankMsg.class, null), // Transfer Item from Inventory to Bank
    UNEQUIP(0xC6BFB907, TransferItemFromEquipToInventoryMsg.class, null), // Transfer Item from Equip to Inventory
    UNKNOWN(0x238C9259, UnknownMsg.class,null),
    UPDATECHARORMOB(0xB6D78961, null, null),
    UPDATECLIENTALLIANCES(0xF3FEB5D4, null, GuildUnknownHandler.class), //AlliancesMsg
    UPDATECLIENTINVENTORIES(0xE66F533D, UpdateInventoryMsg.class, null), //Update player inventory
    UPDATEEFFECTS(0xD4675293, null, null), //Update all effects for an item
    UPDATEFRIENDSTATUS(0x654E2255, UpdateFriendStatusMessage.class, UpdateFriendStatusHandler.class),
    UPDATEGOLDVALUE(0x6915A3FB, null, null), // Update gold in inventory and/or bank
    UPDATEGROUP(0x004E6BCE, GroupUpdateMsg.class, GroupUpdateHandler.class), // Update Group Info
    UPDATEGUILD(0x001D4DF6, GuildInfoMsg.class, GuildInfoHandler.class), // REQ / CMD Promote/Demote Screen
    UPDATEOBJECT(0x1A724739, null, null),
    UPDATESTATE(0x001A45FB, UpdateStateMsg.class, null), // REQ / CMD Toggle Run/Walk Sit/Stand :: UpdateStateMessage
    UPDATETRADEWINDOW(0x406EBDE6, UpdateTradeWindowMsg.class, null), // Trade Complete
    UPGRADEASSET(0x2B85A865, UpgradeAssetMessage.class, UpgradeAssetMsgHandler.class),
    VENDORDIALOG(0x98ACD594, VendorDialogMsg.class, null), // Send/Recv Vendor Dialog
    VERSIONINFO(0x4B7EE463, VersionInfoMsg.class, null), // Version Information
    VIEWRESOURCES(0xCEFD0346, ViewResourcesMessage.class, null),
    VISUALUPDATE(0x33402fd2, null, null),
    WEIGHTINVENTORY(0xF1B6A85C, LootWindowResponseMsg.class, null), // MoveObjectToContainer Window Response
    WHOREQUEST(0xF431CCE9, WhoRequestMsg.class, null), // Request /who
    WHORESPONSE(0xD7C36568, WhoResponseMsg.class, null), // Response /who
	REQUESTBALLLIST(0xE366FF64,RequestBallListMessage.class,RequestBallListHandler.class),
	SENDBALLENTRY(0xAC2B5EDC,SendBallEntryMessage.class,SendBallEntryHandler.class),
	UNKNOWN1(-263523523, Unknown1Msg.class,null),
	DROPGOLD(1461654160,DropGoldMsg.class,null);

    public int opcode;
    private Class message;
    private Class handlerClass;
    public Constructor constructor;
    public AbstractClientMsgHandler handler;

    Protocol(int opcode, Class message, Class handlerClass) {
        this.opcode = opcode;
        this.message = message;
        this.handlerClass = handlerClass;

        // Create reference to message class constructor.

        if (this.message != null) {
            Class[] params = {AbstractConnection.class, ByteBufferReader.class};

            try {
                this.constructor = this.message.getConstructor(params);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

       // Create instance of message handler for incoming protocol messages

        if (this.handlerClass != null) {
            try {
                handler = (AbstractClientMsgHandler) handlerClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static HashMap<Integer, Protocol> _protocolMsgByOpcode = new HashMap<>();

    public static Protocol getByOpcode(int opcode) {

        Protocol protocol = _protocolMsgByOpcode.get(opcode);

        if (protocol != null)
            return protocol;

        return Protocol.NONE;
    }

    public static void initProtocolLookup() {

        for (Protocol protocol : Protocol.values()) {

        	if (_protocolMsgByOpcode.containsKey(protocol.opcode)){
        		Logger.error("Duplicate opcodes for " + protocol.name() + " and " + _protocolMsgByOpcode.get(protocol.opcode).name());
        	}
            _protocolMsgByOpcode.put(protocol.opcode, protocol);
        }
    }


public static int FindNextValidOpcode(ByteBufferReader reader){
    int startPos = reader.position();
    int bytesLeft = reader.remaining();

    if (bytesLeft < 4)
        return startPos;
    int nextPos = startPos;
    for (int i = 1; i< bytesLeft; i++ ){
    reader.position(nextPos);
    if (reader.remaining() < 4)
        return reader.position();
    int newOpcode = reader.getInt();

    Protocol foundProtocol = Protocol.getByOpcode(newOpcode);
    if (foundProtocol.equals(Protocol.NONE)){
        nextPos += 1;
        continue;
    }

    //found opcode. return position - 4 to rewind back to start of opcode, so we can handle it.
        return reader.position() - 4;
    }

    return startPos;
}
}
