// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.InterestManagement;

import engine.Enum.DispatchChannel;
import engine.Enum.GameObjectType;
import engine.ai.MobileFSM;
import engine.ai.MobileFSM.STATE;
import engine.gameManager.GroupManager;
import engine.gameManager.SessionManager;
import engine.job.JobScheduler;
import engine.jobs.RefreshGroupJob;
import engine.net.AbstractNetMsg;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.LoadCharacterMsg;
import engine.net.client.msg.LoadStructureMsg;
import engine.net.client.msg.MoveToPointMsg;
import engine.net.client.msg.UnloadObjectsMsg;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;

import static engine.math.FastMath.sqr;

public enum InterestManager implements Runnable {

    INTERESTMANAGER;

    private static long lastTime;
    private static boolean keepGoing = true;

    public void shutdown() {
        this.keepGoing = false;
    }

    InterestManager() {
        Logger.info(" Interest Management thread is running.");
    }

    @Override
    public void run() {
        beginLoadJob();
    }

    private void beginLoadJob() {

        InterestManager.lastTime = System.currentTimeMillis();

        while (InterestManager.keepGoing) {
            try {
                updateAllPlayers();
            } catch (Exception e) {
                Logger.error("InterestManager.BeginLoadJob:updateAllPlayers", e);
            }
            try {
                Thread.sleep(advanceOneSecond());
            } catch (Exception e) {
                Logger.error("InterestManager.BeginLoadJob:advanceOneSecond", e);
            }
        }
    }

    private long advanceOneSecond() {

        long curTime = System.currentTimeMillis();
        long dur = 1000 + this.lastTime - curTime;

        if (dur < 0) {
            // Last update took more then one second, not good...
            Logger.warn("LoadJob took more then one second to complete.");
            this.lastTime = curTime + 100;
            return 100;
        }
        this.lastTime += 1000;
        return dur;
    }

    private void updateAllPlayers() {
        // get all players

        for (PlayerCharacter pc : SessionManager.getAllActivePlayerCharacters()) {

            if (pc == null)
                continue;

            ClientConnection origin = pc.getClientConnection();

            if (origin == null)
                continue;

            if (!pc.isEnteredWorld())
                continue;

            if (pc.getTeleportLock().readLock().tryLock()) {

                try {
                    updateStaticList(pc, origin);
                    updateMobileList(pc, origin);
                } catch (Exception e) {
                    Logger.error(e);
                } finally {
                    pc.getTeleportLock().readLock().unlock();
                }
            }
        }
    }

    private void updateStaticList(PlayerCharacter player, ClientConnection origin) {

        // Only update if we've moved far enough to warrant it

        float distanceSquared = player.getLoc().distanceSquared2D(player.getLastStaticLoc());

        if (distanceSquared > sqr(25))
            player.setLastStaticLoc(player.getLoc());
        else
            return;

        // Get Statics in range
        HashSet<AbstractWorldObject> toLoad = WorldGrid.getObjectsInRangePartial(player.getLoc(), MBServerStatics.STRUCTURE_LOAD_RANGE,
                MBServerStatics.MASK_STATIC);

        // get list of obects loaded that need removed
        HashSet<AbstractWorldObject> loadedStaticObjects = player.getLoadedStaticObjects();

        HashSet<AbstractWorldObject> toRemove = null;

        toRemove = new HashSet<>(loadedStaticObjects);

        toRemove.removeAll(toLoad);

        // unload static objects now out of range
        if (toRemove.size() > 0) {
            UnloadObjectsMsg uom = new UnloadObjectsMsg();
            for (AbstractWorldObject obj : toRemove) {
                if (obj.getObjectType().equals(GameObjectType.Building))
                    InterestManager.HandleSpecialUnload((Building) obj, origin);
                if (obj != null && !obj.equals(player))
                    uom.addObject(obj);
            }

            Dispatch dispatch = Dispatch.borrow(player, uom);
            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
        }

        loadedStaticObjects.removeAll(toRemove);

        // remove any object to load that are already loaded
        toLoad.removeAll(loadedStaticObjects);

        LoadStructureMsg lsm = new LoadStructureMsg();
        LoadCharacterMsg lcm = null;
        ArrayList<LoadCharacterMsg> lcmList = new ArrayList<>();

        for (AbstractWorldObject awo : toLoad) {
            if (awo.getObjectType().equals(GameObjectType.Building))
                lsm.addObject((Building) awo);
            else if (awo.getObjectType().equals(GameObjectType.Corpse)) {
                Corpse corpse = (Corpse) awo;
                lcm = new LoadCharacterMsg(corpse, PlayerCharacter.hideNonAscii());

                Dispatch dispatch = Dispatch.borrow(player, lcm);
                DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);


            } else if (awo.getObjectType().equals(GameObjectType.NPC)) {
                NPC npc = (NPC) awo;
                lcm = new LoadCharacterMsg(npc, PlayerCharacter.hideNonAscii());

                lcmList.add(lcm);
            }
        }

        if (lsm.getStructureList().size() > 0) {
            Dispatch dispatch = Dispatch.borrow(player, lsm);
            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
        }

        for (LoadCharacterMsg lc : lcmList) {

            Dispatch dispatch = Dispatch.borrow(player, lc);
            DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
        }

        loadedStaticObjects.addAll(toLoad);
    }

    private void updateMobileList(PlayerCharacter player, ClientConnection origin) {

        if (player == null)
            return;

        // Get list of players in range
        // TODO for now use a generic getALL list, later tie into Quad Tree
        HashSet<AbstractWorldObject> toLoad = WorldGrid.getObjectsInRangePartial(player.getLoc(), MBServerStatics.CHARACTER_LOAD_RANGE,
                MBServerStatics.MASK_MOBILE);

        HashSet<AbstractWorldObject> toRemove = new HashSet<>();

        HashSet<AbstractWorldObject> toLoadToPlayer = new HashSet<>();

        for (AbstractWorldObject loadedObject : toLoad) {

            switch (loadedObject.getObjectType()) {
                case PlayerCharacter:
                    PlayerCharacter loadedPlayer = (PlayerCharacter) loadedObject;

                    if (loadedPlayer.getObjectUUID() == player.getObjectUUID())
                        continue;

                    if (player.getSeeInvis() < loadedPlayer.getHidden())
                        continue;

                    if (loadedPlayer.safemodeInvis())
                        continue;

                    if (player.getLoadedObjects().contains(loadedPlayer))
                        continue;

                    if (!loadedPlayer.isInWorldGrid())
                        continue;

                    toLoadToPlayer.add(loadedPlayer);
                    break;
                //not playerCharacter, mobs,npcs and corpses cant be invis or safemode, just add normaly
                default:
                    if (player.getLoadedObjects().contains(loadedObject))
                        continue;

                    if (!loadedObject.isInWorldGrid())
                        continue;

                    toLoadToPlayer.add(loadedObject);
                    break;
            }
        }

        float unloadDistance = MBServerStatics.CHARACTER_LOAD_RANGE;
        for (AbstractWorldObject playerLoadedObject : player.getLoadedObjects()) {

            if (playerLoadedObject.getObjectType().equals(GameObjectType.PlayerCharacter)) {
                PlayerCharacter loadedPlayer = (PlayerCharacter) playerLoadedObject;
                if (player.getSeeInvis() < loadedPlayer.getHidden())
                    toRemove.add(playerLoadedObject);
                else if (loadedPlayer.safemodeInvis())
                    toRemove.add(playerLoadedObject);
            }

            if (!playerLoadedObject.isInWorldGrid())
                toRemove.add(playerLoadedObject);
            else if (playerLoadedObject.getLoc().distanceSquared2D(player.getLoc()) > unloadDistance * unloadDistance)
                toRemove.add(playerLoadedObject);

        }

        player.getLoadedObjects().addAll(toLoadToPlayer);
        player.getLoadedObjects().removeAll(toRemove);

        // get list of obects loaded to remove

        // unload objects now out of range

        if (toRemove.size() > 0) {

            UnloadObjectsMsg uom = new UnloadObjectsMsg();

            for (AbstractWorldObject obj : toRemove) {

                try {
                    if (obj != null)
                        if (obj.equals(player)) // don't unload self
                            continue;

                    uom.addObject(obj);

                    if (obj.getObjectType() == GameObjectType.Mob)
                        ((Mob) obj).playerAgroMap.remove(player.getObjectUUID());
                } catch (Exception e) {
                    Logger.error("UnloadCharacter", obj.getObjectUUID() + " " + e.getMessage());
                }
            }

            if (!uom.getObjectList().isEmpty()) {
                Dispatch dispatch = Dispatch.borrow(player, uom);
                DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
            }
        }

        LoadCharacterMsg lcm = null;
        ArrayList<AbstractWorldObject> players = new ArrayList<>();
        ArrayList<AbstractWorldObject> addToList = new ArrayList<>();

        for (AbstractWorldObject awo : toLoadToPlayer) {
            // dont load yourself
            try {
                if (awo.equals(player))
                    continue;

                if ((awo.getObjectTypeMask() & MBServerStatics.MASK_PLAYER) != 0) {

                    // object to load is a player
                    PlayerCharacter awopc = (PlayerCharacter) awo;

                    // dont load if invis
                    if (player.getSeeInvis() < awopc.getHidden())
                        continue;

                    lcm = new LoadCharacterMsg(awopc, PlayerCharacter.hideNonAscii());
                    players.add(awo);

                    // check if in a group with the person being loaded
                    // and if so set updateGroup flag

                    if (GroupManager.getGroup(player) != null
                            && GroupManager.getGroup(player) == GroupManager.getGroup(awopc))

                        // submit a job as for some reason the client needs a delay
                        // with group updates
                        // as it wont update if we do RefreshGroup directly after
                        // sending the lcm below

                        JobScheduler.getInstance().scheduleJob(new RefreshGroupJob(player, awopc), MBServerStatics.LOAD_OBJECT_DELAY);

                } else if ((awo.getObjectTypeMask() & MBServerStatics.MASK_MOB) != 0) {
                    Mob awonpc = (Mob) awo;

                    if (!awonpc.isAlive() && (awonpc.isPet() || awonpc.isSiege || awonpc.mobBase.isNecroPet() || awonpc.isPlayerGuard))
                        continue;

                    if (awonpc.state.equals(STATE.Respawn) || awonpc.state.equals(STATE.Disabled))
                        continue;

                    awonpc.playerAgroMap.put(player.getObjectUUID(), false);
                    MobileFSM.setAwake(awonpc, false);
                    //				IVarController.setVariable(awonpc, "IntelligenceDisableDelay", (double) (System.currentTimeMillis() + 5000));
                    //				awonpc.enableIntelligence();
                    lcm = new LoadCharacterMsg(awonpc, PlayerCharacter.hideNonAscii());
                } else if ((awo.getObjectTypeMask() & MBServerStatics.MASK_NPC) != 0) {
                    NPC awonpc = (NPC) awo;
                    lcm = new LoadCharacterMsg(awonpc, PlayerCharacter.hideNonAscii());
                } else if ((awo.getObjectTypeMask() & MBServerStatics.MASK_PET) != 0) {
                    Mob awonpc = (Mob) awo;

                    if (!awonpc.isAlive())
                        continue;

                    awonpc.playerAgroMap.put(player.getObjectUUID(), false);

                    if (awonpc.isMob())
                        MobileFSM.setAwake(awonpc, false);
                    //				IVarController.setVariable(awonpc, "IntelligenceDisableDelay", (double) (System.currentTimeMillis() + 5000));
                    //				awonpc.enableIntelligence();
                    lcm = new LoadCharacterMsg(awonpc, PlayerCharacter.hideNonAscii());
                }

                addToList.add(awo);

                if (lcm != null) {
                    Dispatch dispatch = Dispatch.borrow(player, lcm);
                    DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
                }


            } catch (Exception e) {
                Logger.error(awo.getObjectUUID() + " " + e.getMessage());
            }
            //Delaying character loading to reduce bandwidth consumption
        }

        // send effects for all players being loaded
        // do it on a timer otherwise we may get failures as te client needs
        // time to process lcm
        //Added effects to LoadCharacter Serialization.
        //JobScheduler.getInstance().scheduleJob(new LoadEffectsJob(players, origin), MBServerStatics.LOAD_OBJECT_DELAY);
    }

    // Forces the loading of static objects (corpses and buildings).
    // Needed to override threshold limits on loading statics

    public static void forceLoad(AbstractWorldObject awo) {

        AbstractNetMsg msg = null;
        LoadStructureMsg lsm;
        LoadCharacterMsg lcm;
        NPC npc;
        Corpse corpse;
        HashSet<AbstractWorldObject> toUpdate;

        switch (awo.getObjectType()) {
            case Building:
                lsm = new LoadStructureMsg();
                lsm.addObject((Building) awo);
                msg = lsm;
                break;
            case Corpse:
                corpse = (Corpse) awo;
                lcm = new LoadCharacterMsg(corpse, false);
                msg = lcm;
                break;
            case NPC:
                npc = (NPC) awo;
                lcm = new LoadCharacterMsg(npc, false);
                msg = lcm;
                break;
            default:
                return;
        }

        toUpdate = WorldGrid.getObjectsInRangePartial(awo.getLoc(), MBServerStatics.CHARACTER_LOAD_RANGE, MBServerStatics.MASK_PLAYER);

        boolean send;

        for (AbstractWorldObject tar : toUpdate) {
            PlayerCharacter player = (PlayerCharacter) tar;
            HashSet<AbstractWorldObject> loadedStaticObjects = player.getLoadedStaticObjects();
            send = false;

            if (!loadedStaticObjects.contains(awo)) {
                loadedStaticObjects.add(awo);
                send = true;
            }

            if (send) {

                Dispatch dispatch = Dispatch.borrow(player, msg);
                DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
            }
        }
    }

    public static void HandleSpecialUnload(Building building, ClientConnection origin) {

        if (Regions.FurnitureRegionMap.get(building.getObjectUUID()) == null)
            return;

        Regions buildingRegion = Regions.FurnitureRegionMap.get(building.getObjectUUID());

        if (!buildingRegion.isOutside())
            return;

        MoveToPointMsg moveMsg = new MoveToPointMsg(building);

        if (origin != null)
            origin.sendMsg(moveMsg);
    }

    public synchronized void HandleLoadForEnterWorld(PlayerCharacter player) {

        if (player == null)
            return;

        ClientConnection origin = player.getClientConnection();

        if (origin == null)
            return;

        //Update static list
        try {
            updateStaticList(player, origin);
        } catch (Exception e) {
            Logger.error("InterestManager.updateAllStaticPlayers: " + player.getObjectUUID(), e);
        }

        //Update mobile list
        try {
            updateMobileList(player, origin);
        } catch (Exception e) {
            Logger.error("InterestManager.updateAllMobilePlayers: " + player.getObjectUUID(), e);
        }
    }

    public synchronized void HandleLoadForTeleport(PlayerCharacter player) {

        if (player == null)
            return;

        ClientConnection origin = player.getClientConnection();

        if (origin == null)
            return;

        //Update static list
        try {
            updateStaticList(player, origin);
        } catch (Exception e) {
            Logger.error("InterestManager.updateAllStaticPlayers: " + player.getObjectUUID(), e);
        }

        //Update mobile list
        try {
            updateMobileList(player, origin);
        } catch (Exception e) {
            Logger.error("InterestManager.updateAllMobilePlayers: " + player.getObjectUUID(), e);
        }
    }

    public static void reloadCharacter(AbstractCharacter absChar) {

        UnloadObjectsMsg uom = new UnloadObjectsMsg();
        uom.addObject(absChar);
        LoadCharacterMsg lcm = new LoadCharacterMsg(absChar, false);

        HashSet<AbstractWorldObject> toSend = WorldGrid.getObjectsInRangePartial(absChar.getLoc(), MBServerStatics.CHARACTER_LOAD_RANGE,
                MBServerStatics.MASK_PLAYER);

        PlayerCharacter pc = null;

        if (absChar.getObjectType().equals(GameObjectType.PlayerCharacter))
            pc = (PlayerCharacter) absChar;

        for (AbstractWorldObject awo : toSend) {

            PlayerCharacter pcc = (PlayerCharacter) awo;

            if (pcc == null)
                continue;

            ClientConnection cc = SessionManager.getClientConnection(pcc);

            if (cc == null)
                continue;

            if (pcc.getObjectUUID() == absChar.getObjectUUID())
                continue;

            else {
                if (pc != null)
                    if (pcc.getSeeInvis() < pc.getHidden())
                        continue;

                if (!cc.sendMsg(uom)) {
                    String classType = uom.getClass().getSimpleName();
                    Logger.error("Failed to send message ");
                }

                if (!cc.sendMsg(lcm)) {
                    String classType = lcm.getClass().getSimpleName();
                    Logger.error("Failed to send message");
                }
            }
        }
    }
}