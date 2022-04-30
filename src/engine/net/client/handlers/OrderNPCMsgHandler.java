package engine.net.client.handlers;

import engine.Enum.DispatchChannel;
import engine.Enum.GameObjectType;
import engine.Enum.ProfitType;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.math.FastMath;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.*;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;

/*
 * @Author:
 * @Summary: Processes application protocol message which keeps
 * client's tcp connection open.
 */
public class OrderNPCMsgHandler extends AbstractClientMsgHandler {

    // Constants used for incoming message type

    private static final int CLIENT_UPGRADE_REQUEST = 3;
    private static final int CLIENT_REDEED_REQUEST = 6;
    private static final int SVR_CLOSE_WINDOW = 4;

    public OrderNPCMsgHandler() {
        super(OrderNPCMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

        // Member variable declarations

        PlayerCharacter player;
        NPC npc;
        Mob mob;
        Building building;
        OrderNPCMsg orderNPCMsg;
        ManageCityAssetsMsg outMsg;

        // Member variable assignment
        orderNPCMsg = (OrderNPCMsg) baseMsg;

        if (origin.ordernpcspam > System.currentTimeMillis())
            return true;

        origin.ordernpcspam = System.currentTimeMillis() + 500;

        player = SessionManager.getPlayerCharacter(origin);

        if (player == null)
            return true;

        if (orderNPCMsg.getActionType() == 28) {
            OrderNPCMsgHandler.handleCityCommand(orderNPCMsg, player);
            return true;
        }

        if (orderNPCMsg.getObjectType() == GameObjectType.NPC.ordinal()) {

            npc = NPC.getFromCache(orderNPCMsg.getNpcUUID());

            if (npc == null)
                return true;

            building = BuildingManager.getBuildingFromCache(orderNPCMsg.getBuildingUUID());

            if (building == null)
                return true;

            if (building.getHirelings().containsKey(npc) == false)
                return true;


            if (player.getCharItemManager().getTradingWith() != null) {
                ErrorPopupMsg.sendErrorMsg(player, "Cannot barter and trade with same timings.");
                return true;
            }

            player.lastBuildingAccessed = building.getObjectUUID();

            switch (orderNPCMsg.getActionType()) {

                case 2:
                    player = SessionManager.getPlayerCharacter(origin);

                    if (ManageCityAssetMsgHandler.playerCanManageNotFriends(player, building) == false)
                        return true;

                    if (building.getHirelings().containsKey(npc) == false)
                        return true;

                    if (npc.remove() == false) {
                        PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
                        return true;
                    }

                    ManageCityAssetsMsg manageCityAssetsMsg = new ManageCityAssetsMsg();
                    manageCityAssetsMsg.actionType = SVR_CLOSE_WINDOW;
                    manageCityAssetsMsg.setTargetType(building.getObjectType().ordinal());
                    manageCityAssetsMsg.setTargetID(building.getObjectUUID());

                    Dispatch dispatch = Dispatch.borrow(player, manageCityAssetsMsg);
                    DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

                    return true;

                case CLIENT_UPGRADE_REQUEST:

                    if (BuildingManager.playerCanManage(player, building) == false)
                        return true;

                    processUpgradeNPC(player, npc);

                    outMsg = new ManageCityAssetsMsg(player, building);

                    // Action TYPE
                    outMsg.actionType = 3;
                    outMsg.setTargetType(building.getObjectType().ordinal());
                    outMsg.setTargetID(building.getObjectUUID());
                    outMsg.setTargetType3(building.getObjectType().ordinal());
                    outMsg.setTargetID3(building.getObjectUUID());
                    outMsg.setAssetName1(building.getName());
                    outMsg.setUnknown54(1);

                    dispatch = Dispatch.borrow(player, outMsg);
                    DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

                    break;
                case CLIENT_REDEED_REQUEST:

                    if (BuildingManager.PlayerCanControlNotOwner(building, player) == false)
                        return true;

                    processRedeedNPC(npc, building, origin);
                    return true;
                //MB TODO HANDLE all profits.
                case 7:
                case 8:
                case 9:

                    if (BuildingManager.PlayerCanControlNotOwner(building, player) == false)
                        return true;

                    modifySellProfit(orderNPCMsg, origin);
                    dispatch = Dispatch.borrow(player, orderNPCMsg);
                    DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
                    return true;
                case 10:
                case 11:
                case 12:

                    if (BuildingManager.PlayerCanControlNotOwner(building, player) == false)
                        return true;

                    modifyBuyProfit(orderNPCMsg, origin);
                    dispatch = Dispatch.borrow(player, orderNPCMsg);
                    DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
                    return true;
            }

            // Validation check Owner or IC or friends
            if (BuildingManager.PlayerCanControlNotOwner(building, player) == false)
                if (BuildingManager.playerCanManage(player, building) == false)
                    return true;

            ManageNPCMsg manageNPCMsg = new ManageNPCMsg(npc);
            Dispatch dispatch = Dispatch.borrow(player, manageNPCMsg);
            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
            return true;

        } else if (orderNPCMsg.getObjectType() == GameObjectType.Mob.ordinal()) {

            mob = Mob.getFromCacheDBID(orderNPCMsg.getNpcUUID());

            if (mob == null)
                return true;

            building = BuildingManager.getBuildingFromCache(orderNPCMsg.getBuildingUUID());

            if (building == null)
                return true;

            if (!building.getHirelings().containsKey(mob))
                return true;

            if (player.getCharItemManager().getTradingWith() != null) {
                ErrorPopupMsg.sendErrorMsg(player, "Cannot barter and trade with same timings.");
                return true;
            }

            player.lastBuildingAccessed = building.getObjectUUID();

            switch (orderNPCMsg.getActionType()) {
                case 2:

                    if (BuildingManager.playerCanManage(player, building) == false)
                        return true;

                    if (building.getHirelings().containsKey(mob) == false)
                        return true;

                    if (mob.remove(building) == false) {
                        PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
                        return true;
                    }

                    ManageCityAssetsMsg manageCityAssetsMsg = new ManageCityAssetsMsg();
                    manageCityAssetsMsg.actionType = SVR_CLOSE_WINDOW;
                    manageCityAssetsMsg.setTargetType(building.getObjectType().ordinal());
                    manageCityAssetsMsg.setTargetID(building.getObjectUUID());
                    Dispatch dispatch = Dispatch.borrow(player, manageCityAssetsMsg);
                    DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
                    break;
                case 3:

                    if (BuildingManager.PlayerCanControlNotOwner(building, player) == false)
                        return true;

                    processUpgradeNPC(player, mob);

                    outMsg = new ManageCityAssetsMsg(player, building);

                    // Action TYPE
                    outMsg.actionType = 3;
                    outMsg.setTargetType(building.getObjectType().ordinal());
                    outMsg.setTargetID(building.getObjectUUID());
                    outMsg.setTargetType3(building.getObjectType().ordinal());
                    outMsg.setTargetID3(building.getObjectUUID());
                    outMsg.setAssetName1(building.getName());
                    outMsg.setUnknown54(1);

                    dispatch = Dispatch.borrow(player, outMsg);
                    DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
                    break;
                case 6:

                    if (BuildingManager.PlayerCanControlNotOwner(building, player) == false)
                        return true;

                    processRedeedNPC(mob, building, origin);
                    return true;
                //MB TODO HANDLE all profits.
                case 7:
                case 8:
                case 9:
                    break;
                case 10:
                case 11:
                case 12:
                    break;
            }

            // Validation check Owner or IC
            if (BuildingManager.PlayerCanControlNotOwner(building, player) == false)
                if (BuildingManager.playerCanManage(player, building) == false)
                    return true;

            ManageNPCMsg manageNPCMsg = new ManageNPCMsg(mob);
            Dispatch dispatch = Dispatch.borrow(player, manageNPCMsg);
            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
            return true;
        }
        return true;
    }

    private static void modifyBuyProfit(OrderNPCMsg msg, ClientConnection origin) {
        NPC npc;
        PlayerCharacter player;
        Building building;
        float percent;

        ProfitType profitType = null;
        player = origin.getPlayerCharacter();

        if (player == null)
            return;

        npc = NPC.getFromCache(msg.getNpcUUID());

        if (npc == null)
            return;

        building = npc.getBuilding();

        if (building == null)
            return;

        NPCProfits profit = NPC.GetNPCProfits(npc);

        if (profit == null)
            return;

        switch (msg.getActionType()) {
            case 10:
                profitType = ProfitType.BuyNormal;
                break;
            case 11:
                profitType = ProfitType.BuyGuild;
                break;
            case 12:
                profitType = ProfitType.BuyNation;
        }

        percent = msg.getBuySellPercent();
        percent = FastMath.clamp(percent, 0.0f, 1.0f);

        NPCProfits.UpdateProfits(npc, profit, profitType, percent);
    }

    private static void modifySellProfit(OrderNPCMsg orderNPCMsg, ClientConnection origin) {
        NPC npc;
        PlayerCharacter player;
        Building building;
        float percent;

        ProfitType profitType = null;

        player = origin.getPlayerCharacter();

        if (player == null)
            return;

        npc = NPC.getFromCache(orderNPCMsg.getNpcUUID());

        if (npc == null)
            return;

        building = npc.getBuilding();

        if (building == null)
            return;

        NPCProfits profit = NPC.GetNPCProfits(npc);

        if (profit == null)
            return;

        switch (orderNPCMsg.getActionType()) {
            case 7:
                profitType = ProfitType.SellNormal;
                break;
            case 8:
                profitType = ProfitType.SellGuild;
                break;
            case 9:
                profitType = ProfitType.SellNation;
        }

        percent = orderNPCMsg.getBuySellPercent();

        percent -= 1f;
        percent = FastMath.clamp(percent, 0.0f, 3.0f);

        NPCProfits.UpdateProfits(npc, profit, profitType, percent);
    }

    private static void handleCityCommand(OrderNPCMsg orderNpcMsg, PlayerCharacter player) {

        Building building = BuildingManager.getBuildingFromCache(orderNpcMsg.getBuildingUUID());

        if (building == null)
            return;

        if (ManageCityAssetMsgHandler.playerCanManageNotFriends(player, building) == false)
            return;

        if (orderNpcMsg.getPatrolSize() >= 20)
            Logger.info(player.getName() + " is attempting to add patrol points amount " + orderNpcMsg.getPatrolSize());

        if (orderNpcMsg.getSentrySize() >= 20)
            Logger.info(player.getName() + " is attempting to add patrol points amount " + orderNpcMsg.getSentryPoints());

        if (orderNpcMsg.getPatrolPoints() != null) {

           if ( !AddPatrolPoints(building.getObjectUUID(), orderNpcMsg.getPatrolPoints())){
        	   ErrorPopupMsg.sendErrorMsg(player, "Patrol Points must be placed on city zone.");
        	   return;
           }

            for (AbstractCharacter guard : building.getHirelings().keySet()) {
                if (guard.getObjectType() == GameObjectType.Mob)
                    ((Mob) guard).setPatrolPointIndex(0);
            }
        } else if (building.getPatrolPoints() != null)
            ClearPatrolPoints(building.getObjectUUID());

        if (orderNpcMsg.getSentryPoints() != null) {
            AddSentryPoints(building.getObjectUUID(), orderNpcMsg.getSentryPoints());
        } else if (building.getSentryPoints() != null)
            ClearSentryPoints(building.getObjectUUID());

        //		Dispatch dispatch = Dispatch.borrow(pc, msg);
        //		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

    }

    private static void processUpgradeNPC(PlayerCharacter player, AbstractCharacter abstractCharacter) {

        Building building;

        switch (abstractCharacter.getObjectType()) {

            case NPC:
                NPC npc = (NPC) abstractCharacter;
                building = npc.getBuilding();

                // Cannot upgrade an npc not within a building

                if (building == null)
                    return;

                City buildingCity = building.getCity();

                if (buildingCity == null) {
                    npc.processUpgradeNPC(player);
                    return;
                }

                buildingCity.transactionLock.writeLock().lock();

                try {
                    npc.processUpgradeNPC(player);
                } catch (Exception e) {
                    Logger.error(e);
                } finally {
                    buildingCity.transactionLock.writeLock().unlock();
                }
                break;
            case Mob:

                Mob mob = (Mob) abstractCharacter;
                building = mob.getBuilding();

                if (mob.getBuilding() == null)
                    return;

                City mobCity = building.getCity();

                if (mobCity == null) {
                    mob.processUpgradeMob(player);
                    return;
                }

                mobCity.transactionLock.writeLock().lock();

                try {
                    mob.processUpgradeMob(player);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Logger.error(e);
                } finally {
                    mobCity.transactionLock.writeLock().unlock();
                }
                break;
        }
    }

    private synchronized void processRedeedNPC(AbstractCharacter abstractCharacter, Building building, ClientConnection origin) {

        // Member variable declaration

        switch (abstractCharacter.getObjectType()) {
            case NPC:
                NPC npc = (NPC) abstractCharacter;

                Building cityBuilding = npc.getBuilding();

                if (cityBuilding == null)
                    return;

                npc.processRedeedNPC(origin);
                break;
            case Mob:
                Mob mob = (Mob) abstractCharacter;
                mob.processRedeedMob(origin);
                break;
        }
    }

    private static boolean AddPatrolPoints(int buildingID, ArrayList<Vector3fImmutable> patrolPoints) {

        Building building = BuildingManager.getBuildingFromCache(buildingID);

        if (building == null)
            return false;
        
        Zone zone = building.getParentZone();
        
        if (zone == null)
        	return false;
        
        if (zone.getPlayerCityUUID() == 0)
        	return false;
        
        City city = building.getCity();
        
        if (city == null)
        	return false;
        
        

        //clear first.
        
        for (Vector3fImmutable point : patrolPoints) {
        	
        	if (city.isLocationOnCityZone(point) == false){
      		return false;
        	}

        }

        DbManager.BuildingQueries.CLEAR_PATROL(buildingID);

        for (Vector3fImmutable point : patrolPoints) {
        	
            if (!DbManager.BuildingQueries.ADD_TO_PATROL(buildingID, point))
                return false;
        }
        building.patrolPoints = patrolPoints;
        return true;
    }

    private static boolean AddSentryPoints(int buildingID, ArrayList<Vector3fImmutable> sentryPoints) {

        Building building = BuildingManager.getBuildingFromCache(buildingID);

        if (building == null)
            return false;

        building.sentryPoints = sentryPoints;
        return true;
    }

    private static boolean ClearPatrolPoints(int buildingID) {

        Building building = BuildingManager.getBuildingFromCache(buildingID);

        if (building == null)
            return false;

        if (building.patrolPoints == null)
            return true;

        if (DbManager.BuildingQueries.CLEAR_PATROL(buildingID) == false)
            return false;

        building.patrolPoints.clear();
        return true;
    }

    private static boolean ClearSentryPoints(int buildingID) {

        Building building = BuildingManager.getBuildingFromCache(buildingID);

        if (building == null)
            return false;

        if (building.sentryPoints == null)
            return true;

        building.sentryPoints.clear();
        return true;
    }

}