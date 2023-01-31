// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.ai;


import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.ai.utilities.CombatUtilities;
import engine.ai.utilities.MovementUtilities;
import engine.gameManager.BuildingManager;
import engine.gameManager.CombatManager;
import engine.gameManager.MovementManager;
import engine.gameManager.PowersManager;
import engine.math.Vector3fImmutable;
import engine.net.DispatchMessage;
import engine.net.client.msg.PerformActionMsg;
import engine.net.client.msg.PowerProjectileMsg;
import engine.net.client.msg.UpdateStateMsg;
import engine.objects.*;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static engine.math.FastMath.sqr;

public class MobileFSM {


    public enum STATE {
        Disabled,
        Respawn,
        Idle,
        Awake,
        Aggro,
        Patrol,
        Help,
        Attack,
        Home,
        Dead,
        Recalling,
        Retaliate
    }

    public static void run(Mob mob) {
        if (mob == null) {
            return;
        }


        STATE state = mob.state;
        switch (state) {
            case Idle:
                if (mob.isAlive())
                    StaticMobActions.updateLocation(mob);

                if (mob.isPlayerGuard) {
                    guardAwake(mob);
                    break;
                }

                idle(mob);
                break;
            case Awake:

                if (mob.isAlive())
                    StaticMobActions.updateLocation(mob);

                if (mob.isPlayerGuard)
                    guardAwake(mob);
                else if (mob.isSiege == false) {
                    if (mob.isPet())
                        petAwake(mob);
                    else if (mob.isGuard())
                        awakeNPCguard(mob);
                    else
                        awake(mob);
                }

                break;
            case Aggro:


                if (mob.isAlive())
                    StaticMobActions.updateLocation(mob);

                if (mob.isPlayerGuard)
                    guardAggro(mob, mob.aggroTargetID);
                else
                    aggro(mob, mob.aggroTargetID);
                break;
            case Patrol:

                if (mob.isAlive())
                    StaticMobActions.updateLocation(mob);

                if (mob.isPlayerGuard)
                    guardPatrol(mob);
                else
                    patrol(mob);
                break;
            case Attack:
                if (mob.isAlive())
                    StaticMobActions.updateLocation(mob);


                if (!mob.isCombat()) {
                    mob.setCombat(true);
                    UpdateStateMsg rwss = new UpdateStateMsg();
                    rwss.setPlayer(mob);
                    DispatchMessage.sendToAllInRange(mob, rwss);
                }

                if (mob.isPlayerGuard)
                    guardAttack(mob);
                else if (mob.isPet() || mob.isSiege)
                    petAttack(mob);
                else if (mob.isGuard())
                    guardAttackMob(mob);
                else
                    mobAttack(mob);
                break;
            case Home:
                if (mob.isPlayerGuard)
                    guardHome(mob, mob.walkingHome);
                else
                    home(mob, mob.walkingHome);
                break;
            case Dead:
                dead(mob);
                break;
            case Respawn:
                respawn(mob);
                break;
            case Recalling:
                recalling(mob);
                break;
            case Retaliate:
                retaliate(mob);
                break;
        }
    }

    public static boolean setAwake(Mob aiAgent, boolean force) {
        if (force) {
            aiAgent.state = STATE.Awake;
            return true;
        }
        if (aiAgent.state == STATE.Idle) {
            aiAgent.state = STATE.Awake;
            return true;
        }
        return false;
    }

    public static boolean setAggro(Mob aiAgent, int targetID) {
        if (aiAgent.state != STATE.Dead) {
            aiAgent.noAggro = false;
            aiAgent.aggroTargetID = targetID;
            aiAgent.state = STATE.Aggro;
            return true;
        }
        return false;
    }

    public static Mob getMobile(int mobileID) {
        return StaticMobActions.getFromCache(mobileID);
    }

    private static void idle(Mob mob) {

        if (mob.getLoc().distanceSquared2D(mob.getBindLoc()) > sqr(2000)) {

            mob.walkingHome = false;
            mob.state = STATE.Home;
        }
    }


    private static void awake(Mob aiAgent) {
        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        if (aiAgent.getLoc().distanceSquared2D(aiAgent.getBindLoc()) > sqr(2000)) {
            aiAgent.walkingHome = false ;
            aiAgent.state = STATE.Home;
            return;
        }
        //Don't attempt to aggro if No aggro is on and aiAgent is not home yet.
        if (aiAgent.noAggro && aiAgent.isMoving()) {
            return;
        }

        //Mob stopped Moving let's turn aggro back on.
        if (aiAgent.noAggro == true) {
            aiAgent.noAggro = false;
        }
        //no players currently have this mob loaded. return to IDLE.
        if (aiAgent.playerAgroMap.isEmpty()) {
            aiAgent.state = STATE.Idle;
            return;
        }


        //currently npc guards wont patrol or aggro
        if (aiAgent.isGuard()) {
            return;
        }

        //Get the Map for Players that loaded this mob.

        ConcurrentHashMap<Integer, Boolean> loadedPlayers = aiAgent.playerAgroMap;


        if (!Enum.MobFlagType.AGGRESSIVE.elementOf(aiAgent.getMobBase().getFlags()) && aiAgent.getCombatTarget() == null) {
            //attempt to patrol even if aiAgent isn't aggresive;

            int patrolRandom = ThreadLocalRandom.current().nextInt(1000);
            if (patrolRandom <= MBServerStatics.AI_PATROL_DIVISOR) {
                aiAgent.state = STATE.Patrol;
            }
            return;
        }
        //aiAgent finished moving home, set aggro on.

        for (Entry playerEntry : loadedPlayers.entrySet()) {
            int playerID = (int) playerEntry.getKey();
            PlayerCharacter loadedPlayer = PlayerCharacter.getFromCache(playerID);

            //Player is null, let's remove them from the list.
            if (loadedPlayer == null) {
                //     Logger.error("MobileFSM", "Player with UID " + playerID + " returned null in mob.getPlayerAgroMap()");
                loadedPlayers.remove(playerID);
                continue;
            }
            //Player is Dead, Mob no longer needs to attempt to aggro. Remove them from aggro map.
            if (!loadedPlayer.isAlive()) {
                loadedPlayers.remove(playerID);
                continue;
            }
            //Can't see target, skip aggro.
            if (aiAgent.mobBase.getSeeInvis() >= loadedPlayer.getHidden()) {
                continue;
            }

            // No aggro for this race type
            if (loadedPlayer.getRace().getRaceType().getAggroType().elementOf(aiAgent.getMobBase().getNoAggro()))
                continue;


            if (MovementUtilities.inRangeToAggro(aiAgent, loadedPlayer)) {
                aiAgent.aggroTargetID = playerID;
                aiAgent.state = STATE.Aggro;
                return;
            }


        }

        int patrolRandom = ThreadLocalRandom.current().nextInt(1000);
        if (patrolRandom <= MBServerStatics.AI_PATROL_DIVISOR) {
            aiAgent.state = STATE.Patrol;
        }

    }

    private static void guardAttackMob(Mob aiAgent) {
        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        AbstractGameObject target = aiAgent.getCombatTarget();
        if (target == null) {
            aiAgent.state = STATE.Awake;
            return;
        }

        if (target.getObjectType().equals(GameObjectType.Mob) == false) {
            aiAgent.state = STATE.Awake;
            return;
        }

        if (target.equals(aiAgent)) {
            aiAgent.state = STATE.Awake;
            return;
        }

        Mob mob = (Mob) target;

        if (!mob.isAlive() || mob.state == STATE.Dead) {
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (CombatUtilities.inRangeToAttack(aiAgent, mob)) {
            //not time to attack yet.
            if (System.currentTimeMillis() < aiAgent.lastAttackTime) {
                return;
            }

            if (!CombatUtilities.RunAIRandom())
                return;

            if (aiAgent.getRange() >= 30 && aiAgent.isMoving())
                return;
            //no weapons, defualt mob attack speed 3 seconds.
            ItemBase mainHand = StaticMobActions.getWeaponItemBase(aiAgent,true);
            ItemBase offHand = StaticMobActions.getWeaponItemBase(aiAgent,false);
            if (mainHand == null && offHand == null) {
                CombatUtilities.combatCycle(aiAgent, mob, true, null);
                int delay = 3000;
                if (aiAgent.isSiege)
                    delay = 11000;
                aiAgent.lastAttackTime = System.currentTimeMillis() + delay;

            } else
                //TODO set offhand attack time.
                if (StaticMobActions.getWeaponItemBase(aiAgent,true) != null) {
                    int attackDelay = 3000;
                    if (aiAgent.isSiege)
                        attackDelay = 11000;
                    CombatUtilities.combatCycle(aiAgent, mob, true, StaticMobActions.getWeaponItemBase(aiAgent,true));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;
                } else if (StaticMobActions.getWeaponItemBase(aiAgent, false) != null) {
                    int attackDelay = (int) (aiAgent.getSpeedHandTwo() * 100);
                    if (aiAgent.isSiege)
                        attackDelay = 3000;
                    CombatUtilities.combatCycle(aiAgent, mob, false, StaticMobActions.getWeaponItemBase(aiAgent, false));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;
                }
            return;

        }
        if (!MovementUtilities.updateMovementToCharacter(aiAgent, mob))
            return;

        if (!MovementUtilities.canMove(aiAgent))
            return;


        if (CombatUtilities.inRangeToAttack2D(aiAgent, mob))
            return;


        aiAgent.destination = MovementUtilities.GetDestinationToCharacter(aiAgent, mob);

        MovementUtilities.moveToLocation(aiAgent, aiAgent.destination, aiAgent.getRange());
    }

    private static void awakeNPCguard(Mob aiAgent) {
        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        // Player guards are bound to their city zone
        // and recall when leaving it.

        if (aiAgent.getLoc().distanceSquared2D(aiAgent.getBindLoc()) > sqr(2000)) {
            aiAgent.walkingHome = false;
            aiAgent.state = STATE.Home;
            return;
        }

        //Don't attempt to aggro if No aggro is on and aiAgent is not home yet.
        //no players currently have this mob loaded. return to IDLE.
        //currently npc guards wont patrol or aggro
        //Get the Map for Players that loaded this mob.

        HashSet<AbstractWorldObject> awoList = WorldGrid.getObjectsInRangePartial(aiAgent, 100, MBServerStatics.MASK_MOB);

        for (AbstractWorldObject awoMob : awoList) {

            //dont scan self.
            if (aiAgent.equals(awoMob))
                continue;

            Mob mob = (Mob) awoMob;
            //dont attack other guards
            if (mob.isGuard())
                continue;
            if (aiAgent.getLoc().distanceSquared2D(mob.getLoc()) > sqr(50))
                continue;
            aiAgent.setCombatTarget(mob);
            aiAgent.state = STATE.Attack;
        }
    }

    private static void petAwake(Mob aiAgent) {

        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        PlayerCharacter petOwner = aiAgent.getOwner();

        if (petOwner == null)
            return;

        //lets make mobs ai less twitchy, Don't call another movement until mob reaches it's destination.
        if (aiAgent.isMoving())
            return;

        if (!MovementUtilities.canMove(aiAgent))
            return;

        if (petOwner.getLoc().distanceSquared2D(aiAgent.getLoc()) > MBServerStatics.AI_RECALL_RANGE * MBServerStatics.AI_RECALL_RANGE) {
            aiAgent.teleport(petOwner.getLoc());
            return;
        }

        if (petOwner.getLoc().distanceSquared2D(aiAgent.getLoc()) > 30 * 30) {
            if (aiAgent.isMoving())
                return;

            if (!MovementUtilities.canMove(aiAgent))
                return;
            if (aiAgent.getLoc().distanceSquared2D(petOwner.getLoc()) < aiAgent.getRange() * aiAgent.getRange())
                return;

            MovementUtilities.moveToLocation(aiAgent, petOwner.getLoc(), aiAgent.getRange());
        }
    }

    private static void aggro(Mob aiAgent, int targetID) {

        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        if (aiAgent.getLoc().distanceSquared2D(aiAgent.getBindLoc()) > sqr(2000)) {
            aiAgent.walkingHome = false;
            aiAgent.state = STATE.Home;
            return;
        }

        if (!aiAgent.isCombat()) {
            aiAgent.setCombat(true);
            UpdateStateMsg rwss = new UpdateStateMsg();
            rwss.setPlayer(aiAgent);
            DispatchMessage.sendToAllInRange(aiAgent, rwss);
        }

        //a player got in aggro range. Move to player until in range of attack.
        PlayerCharacter aggroTarget = PlayerCharacter.getFromCache(targetID);

        if (aggroTarget == null) {
            // Logger.error("MobileFSM.aggro", "aggro target with UUID " + targetID + " returned null");
            aiAgent.playerAgroMap.remove(targetID);
            aiAgent.aggroTargetID = 0;
            aiAgent.state = STATE.Patrol;
            return;
        }
        if (aiAgent.mobBase.getSeeInvis() >= aggroTarget.getHidden()) {
            aiAgent.setCombatTarget(null);
            targetID = 0;
            aiAgent.state = STATE.Patrol;
            return;
        }

        if (!aggroTarget.isActive()) {
            aiAgent.setCombatTarget(null);
            targetID = 0;
            aiAgent.state = STATE.Patrol;
            return;
        }

        if (CombatUtilities.inRangeToAttack(aiAgent, aggroTarget)) {
            aiAgent.state = STATE.Attack;
            attack(aiAgent, targetID);
            return;
        }

        if (!MovementUtilities.inRangeDropAggro(aiAgent, aggroTarget)) {
            aiAgent.aggroTargetID = 0;
            aiAgent.setCombatTarget(null);
            MovementUtilities.moveToLocation(aiAgent, aiAgent.bindLoc, 0);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (!MovementUtilities.inRangeOfBindLocation(aiAgent)) {
            aiAgent.setCombatTarget(null);
            aiAgent.aggroTargetID = 0;
            aiAgent.state = STATE.Home;
            return;
        }

        //use this so mobs dont continue to try to move if they are underneath a flying target. only use 2D range check.
        if (CombatUtilities.inRangeToAttack2D(aiAgent, aggroTarget))
            return;

        if (!MovementUtilities.updateMovementToCharacter(aiAgent, aggroTarget))
            return;

        if (!MovementUtilities.canMove(aiAgent))
            return;

        if (aiAgent.getLoc().distanceSquared2D(aggroTarget.getLoc()) < aiAgent.getRange() * aiAgent.getRange())
            return;

        aiAgent.destination = MovementUtilities.GetDestinationToCharacter(aiAgent, aggroTarget);
        MovementUtilities.moveToLocation(aiAgent, aiAgent.destination, aiAgent.getRange());

    }

    private static void petAttack(Mob aiAgent) {

        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        AbstractGameObject target = aiAgent.getCombatTarget();

        if (target == null) {
            aiAgent.state = STATE.Awake;
            return;
        }

        switch (target.getObjectType()) {

            case PlayerCharacter:

                PlayerCharacter player = (PlayerCharacter) target;

                if (!player.isActive()) {
                    aiAgent.setCombatTarget(null);
                    aiAgent.state = STATE.Awake;
                    return;
                }

                if (player.inSafeZone()) {
                    aiAgent.setCombatTarget(null);
                    aiAgent.state = STATE.Awake;
                    return;
                }

                handlePlayerAttackForPet(aiAgent, player);

                break;
            case Building:
                Building building = (Building) target;
                petHandleBuildingAttack(aiAgent, building);
                break;
            case Mob:
                Mob mob = (Mob) target;
                handleMobAttackForPet(aiAgent, mob);
                break;
        }
    }

    private static void mobAttack(Mob aiAgent) {

        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        if (aiAgent.getLoc().distanceSquared2D(aiAgent.getBindLoc()) > sqr(2000)) {

            aiAgent.walkingHome = false;
            aiAgent.state = STATE.Home;
            return;
        }

        AbstractGameObject target = aiAgent.getCombatTarget();

        if (target == null) {
            aiAgent.state = STATE.Patrol;
            return;
        }

        switch (target.getObjectType()) {

            case PlayerCharacter:

                PlayerCharacter player = (PlayerCharacter) target;

                if (!player.isActive()) {
                    aiAgent.setCombatTarget(null);
                    aiAgent.state = STATE.Patrol;
                    return;
                }

                if (aiAgent.mobBase.isNecroPet() && player.inSafeZone()) {
                    aiAgent.setCombatTarget(null);
                    aiAgent.state = STATE.Idle;
                    return;
                }

                handlePlayerAttackForMob(aiAgent, player);
                break;
            case Building:
                Building building = (Building) target;
                petHandleBuildingAttack(aiAgent, building);
                break;
            case Mob:
                Mob mob = (Mob) target;
                handleMobAttackForMob(aiAgent, mob);
        }
    }

    private static void petHandleBuildingAttack(Mob aiAgent, Building building) {

        int buildingHitBox = (int) CombatManager.calcHitBox(building);

        if (building.getRank() == -1) {
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (!building.isVulnerable()) {
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (BuildingManager.getBuildingFromCache(building.getObjectUUID()) == null) {
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (building.getParentZone() != null && building.getParentZone().isPlayerCity()) {

            for (Mob mob : building.getParentZone().zoneMobSet) {

                if (!mob.isPlayerGuard)
                    continue;

                if (mob.getCombatTarget() != null)
                    continue;

                if (mob.getGuild() != null && building.getGuild() != null)
                    if (!Guild.sameGuild(mob.getGuild().getNation(), building.getGuild().getNation()))
                        continue;

                mob.setCombatTarget(aiAgent);
                mob.state = STATE.Attack;
            }
        }

        if (CombatUtilities.inRangeToAttack(aiAgent, building)) {
            //not time to attack yet.

            if (!CombatUtilities.RunAIRandom())
                return;

            if (System.currentTimeMillis() < aiAgent.lastAttackTime)
                return;

            if (aiAgent.getRange() >= 30 && aiAgent.isMoving())
                return;

            //reset attack animation
            if (aiAgent.isSiege)
                MovementManager.sendRWSSMsg(aiAgent);

            //			Fire siege balls
            //			 TODO: Fix animations not following stone

            //no weapons, defualt mob attack speed 3 seconds.
            ItemBase mainHand = StaticMobActions.getWeaponItemBase(aiAgent, true);
            ItemBase offHand = StaticMobActions.getWeaponItemBase(aiAgent, false);

            if (mainHand == null && offHand == null) {

                CombatUtilities.combatCycle(aiAgent, building, true, null);
                int delay = 3000;

                if (aiAgent.isSiege)
                    delay = 15000;

                aiAgent.lastAttackTime = System.currentTimeMillis() + delay;
            } else
                //TODO set offhand attack time.
                if (StaticMobActions.getWeaponItemBase(aiAgent, true) != null) {

                    int attackDelay = 3000;

                    if (aiAgent.isSiege)
                        attackDelay = 15000;

                    CombatUtilities.combatCycle(aiAgent, building, true, StaticMobActions.getWeaponItemBase(aiAgent, true));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;

                } else if (StaticMobActions.getWeaponItemBase(aiAgent, false) != null) {

                    int attackDelay = 3000;

                    if (aiAgent.isSiege)
                        attackDelay = 15000;

                    CombatUtilities.combatCycle(aiAgent, building, false, StaticMobActions.getWeaponItemBase(aiAgent, false));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;
                }

            if (aiAgent.isSiege) {
                PowerProjectileMsg ppm = new PowerProjectileMsg(aiAgent, building);
                ppm.setRange(50);
                DispatchMessage.dispatchMsgToInterestArea(aiAgent, ppm, DispatchChannel.SECONDARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
            }
            return;
        }

        //Outside of attack Range, Move to players predicted loc.

        if (!aiAgent.isMoving())
            if (MovementUtilities.canMove(aiAgent))
                MovementUtilities.moveToLocation(aiAgent, building.getLoc(), aiAgent.getRange() + buildingHitBox);
    }

    private static void handlePlayerAttackForPet(Mob aiAgent, PlayerCharacter player) {

        if (aiAgent.getMobBase().getSeeInvis() < player.getHidden()) {
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (!player.isAlive()) {
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (CombatUtilities.inRangeToAttack(aiAgent, player)) {
            //not time to attack yet.
            if (System.currentTimeMillis() < aiAgent.lastAttackTime)
                return;

            if (!CombatUtilities.RunAIRandom())
                return;

            if (aiAgent.getRange() >= 30 && aiAgent.isMoving())
                return;
            // add timer for last attack.
            //player.setTimeStamp("LastCombatPlayer", System.currentTimeMillis());
            //no weapons, defualt mob attack speed 3 seconds.
            ItemBase mainHand = StaticMobActions.getWeaponItemBase(aiAgent, true);
            ItemBase offHand = StaticMobActions.getWeaponItemBase(aiAgent, false);

            if (mainHand == null && offHand == null) {

                CombatUtilities.combatCycle(aiAgent, player, true, null);

                int delay = 3000;

                if (aiAgent.isSiege)
                    delay = 11000;

                aiAgent.lastAttackTime = System.currentTimeMillis() + delay;
            }
            //TODO set offhand attack time.

            if (StaticMobActions.getWeaponItemBase(aiAgent, true) != null) {

                int attackDelay = 3000;

                if (aiAgent.isSiege)
                    attackDelay = 11000;

                CombatUtilities.combatCycle(aiAgent, player, true, StaticMobActions.getWeaponItemBase(aiAgent, true));
                aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;

            } else if (StaticMobActions.getWeaponItemBase(aiAgent, false) != null) {

                int attackDelay = (int) (aiAgent.getSpeedHandTwo() * 100);

                if (aiAgent.isSiege)
                    attackDelay = 3000;

                CombatUtilities.combatCycle(aiAgent, player, false, StaticMobActions.getWeaponItemBase(aiAgent, false));
                aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;
            }
            return;
        }

        if (!MovementUtilities.updateMovementToCharacter(aiAgent, player))
            return;

        //out of range to attack move
        if (!MovementUtilities.canMove(aiAgent))
            return;

        aiAgent.destination = MovementUtilities.GetDestinationToCharacter(aiAgent, player);
        MovementUtilities.moveToLocation(aiAgent, aiAgent.destination, aiAgent.getRange());
    }

    private static void handlePlayerAttackForMob(Mob aiAgent, PlayerCharacter player) {

        if (aiAgent.getMobBase().getSeeInvis() < player.getHidden()) {
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (!player.isAlive()) {
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (aiAgent.lastMobPowerToken != 0) {

            PowersBase mobPower = PowersManager.getPowerByToken(aiAgent.lastMobPowerToken);

            if (System.currentTimeMillis() > aiAgent.getTimeStamp("FInishCast")) {
                PerformActionMsg msg = PowersManager.createPowerMsg(mobPower, 40, aiAgent, player);
                msg.setUnknown04(2);
                PowersManager.finishUseMobPower(msg, aiAgent, 0, 0);
                aiAgent.lastMobPowerToken = 0;
                aiAgent.setIsCasting(false);
            }
            return;
        }

        if (System.currentTimeMillis() > aiAgent.getTimeStamp("CallForHelp")) {
            CombatUtilities.CallForHelp(aiAgent);
            aiAgent.getTimestamps().put("CallForHelp", System.currentTimeMillis() + 60000);
        }

        HashMap<Integer, Integer> staticPowers = aiAgent.getMobBase().getStaticPowers();

        if (staticPowers != null && !staticPowers.isEmpty()) {
            int chance = ThreadLocalRandom.current().nextInt(300);

            if (chance <= 1) {

                int randomPower = ThreadLocalRandom.current().nextInt(staticPowers.size());
                int powerToken = (int) staticPowers.keySet().toArray()[randomPower];
                PowersBase pb = PowersManager.getPowerByToken(powerToken);

                if (pb == null)
                    return;

                if (System.currentTimeMillis() > aiAgent.getTimeStamp(pb.getIDString())) {

                    PowersManager.useMobPower(aiAgent, player, pb, staticPowers.get(powerToken));

                    int cooldown = pb.getRecycleTime(staticPowers.get(powerToken));
                    aiAgent.getTimestamps().put(pb.getIDString(), System.currentTimeMillis() + cooldown + (pb.getToken() == 429023263 ? 10000 : 120000));
                    return;
                }
            }
        }

        if (!MovementUtilities.inRangeOfBindLocation(aiAgent)) {
            aiAgent.setCombatTarget(null);
            aiAgent.aggroTargetID = 0;
            aiAgent.walkingHome = false;
            aiAgent.state = STATE.Home;
            return;
        }

        if (!MovementUtilities.inRangeDropAggro(aiAgent, player)) {
            aiAgent.aggroTargetID = 0;
            aiAgent.setCombatTarget(null);
            MovementUtilities.moveToLocation(aiAgent, aiAgent.bindLoc, 0);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (CombatUtilities.inRangeToAttack(aiAgent, player)) {

            //no weapons, defualt mob attack speed 3 seconds.

            if (System.currentTimeMillis() < aiAgent.lastAttackTime)
                return;

            if (!CombatUtilities.RunAIRandom())
                return;

            // ranged mobs cant attack while running. skip until they finally stop.
            if (aiAgent.getRange() >= 30 && aiAgent.isMoving())
                return;

            // add timer for last attack.
            //	player.setTimeStamp("LastCombatPlayer", System.currentTimeMillis());
            ItemBase mainHand = StaticMobActions.getWeaponItemBase(aiAgent, true);
            ItemBase offHand = StaticMobActions.getWeaponItemBase(aiAgent, false);

            if (mainHand == null && offHand == null) {

                CombatUtilities.combatCycle(aiAgent, player, true, null);
                int delay = 3000;

                if (aiAgent.isSiege)
                    delay = 11000;

                aiAgent.lastAttackTime = System.currentTimeMillis() + delay;

            } else
                //TODO set offhand attack time.
                if (StaticMobActions.getWeaponItemBase(aiAgent, true) != null) {

                    int delay = 3000;

                    if (aiAgent.isSiege)
                        delay = 11000;

                    CombatUtilities.combatCycle(aiAgent, player, true, StaticMobActions.getWeaponItemBase(aiAgent, true));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + delay;
                } else if (StaticMobActions.getWeaponItemBase(aiAgent, false) != null) {

                    int attackDelay = 3000;

                    if (aiAgent.isSiege)
                        attackDelay = 11000;

                    CombatUtilities.combatCycle(aiAgent, player, false, StaticMobActions.getWeaponItemBase(aiAgent, false));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;
                }
            return;
        }

        if (!MovementUtilities.updateMovementToCharacter(aiAgent, player))
            return;

        if (!MovementUtilities.canMove(aiAgent))
            return;

        //this stops mobs from attempting to move while they are underneath a player.
        if (CombatUtilities.inRangeToAttack2D(aiAgent, player))
            return;

        aiAgent.destination = MovementUtilities.GetDestinationToCharacter(aiAgent, player);
        MovementUtilities.moveToLocation(aiAgent, aiAgent.destination, aiAgent.getRange());

    }

    private static void handleMobAttackForPet(Mob aiAgent, Mob mob) {

        if (!mob.isAlive()) {
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (CombatUtilities.inRangeToAttack(aiAgent, mob)) {
            //not time to attack yet.
            if (System.currentTimeMillis() < aiAgent.lastAttackTime)
                return;

            if (!CombatUtilities.RunAIRandom())
                return;

            if (aiAgent.getRange() >= 30 && aiAgent.isMoving())
                return;

            //no weapons, defualt mob attack speed 3 seconds.
            ItemBase mainHand = StaticMobActions.getWeaponItemBase(aiAgent, true);
            ItemBase offHand = StaticMobActions.getWeaponItemBase(aiAgent, false);

            if (mainHand == null && offHand == null) {

                CombatUtilities.combatCycle(aiAgent, mob, true, null);

                int delay = 3000;

                if (aiAgent.isSiege)
                    delay = 11000;

                aiAgent.lastAttackTime = System.currentTimeMillis() + delay;

            } else
                //TODO set offhand attack time.
                if (StaticMobActions.getWeaponItemBase(aiAgent, true) != null) {

                    int attackDelay = 3000;

                    if (aiAgent.isSiege)
                        attackDelay = 11000;

                    CombatUtilities.combatCycle(aiAgent, mob, true, StaticMobActions.getWeaponItemBase(aiAgent, true));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;

                } else if (StaticMobActions.getWeaponItemBase(aiAgent, false) != null) {

                    int attackDelay = (int) (aiAgent.getSpeedHandTwo() * 100);

                    if (aiAgent.isSiege)
                        attackDelay = 3000;

                    CombatUtilities.combatCycle(aiAgent, mob, false, StaticMobActions.getWeaponItemBase(aiAgent, false));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;
                }
            return;
        }

        if (!MovementUtilities.updateMovementToCharacter(aiAgent, mob))
            return;

        if (!MovementUtilities.canMove(aiAgent))
            return;

        if (CombatUtilities.inRangeToAttack2D(aiAgent, mob))
            return;

        aiAgent.destination = MovementUtilities.GetDestinationToCharacter(aiAgent, mob);
        MovementUtilities.moveToLocation(aiAgent, aiAgent.destination, aiAgent.getRange());
    }

    private static void handleMobAttackForMob(Mob aiAgent, Mob mob) {


        if (!mob.isAlive()) {
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (CombatUtilities.inRangeToAttack(aiAgent, mob)) {
            //not time to attack yet.
            if (System.currentTimeMillis() < aiAgent.lastAttackTime) {
                return;
            }

            if (!CombatUtilities.RunAIRandom())
                return;

            if (aiAgent.getRange() >= 30 && aiAgent.isMoving())
                return;
            //no weapons, defualt mob attack speed 3 seconds.
            ItemBase mainHand = StaticMobActions.getWeaponItemBase(aiAgent, true);
            ItemBase offHand = StaticMobActions.getWeaponItemBase(aiAgent, false);

            if (mainHand == null && offHand == null) {

                CombatUtilities.combatCycle(aiAgent, mob, true, null);
                int delay = 3000;

                if (aiAgent.isSiege)
                    delay = 11000;

                aiAgent.lastAttackTime = System.currentTimeMillis() + delay;
            } else
                //TODO set offhand attack time.
                if (StaticMobActions.getWeaponItemBase(aiAgent, true) != null) {

                    int attackDelay = 3000;

                    if (aiAgent.isSiege)
                        attackDelay = 11000;

                    CombatUtilities.combatCycle(aiAgent, mob, true, StaticMobActions.getWeaponItemBase(aiAgent, true));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;

                } else if (StaticMobActions.getWeaponItemBase(aiAgent, false) != null) {

                    int attackDelay = 3000;

                    if (aiAgent.isSiege)
                        attackDelay = 11000;

                    CombatUtilities.combatCycle(aiAgent, mob, false, StaticMobActions.getWeaponItemBase(aiAgent, false));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;
                }
            return;
        }

        //use this so mobs dont continue to try to move if they are underneath a flying target. only use 2D range check.
        if (CombatUtilities.inRangeToAttack2D(aiAgent, mob))
            return;

        if (!MovementUtilities.updateMovementToCharacter(aiAgent, mob))
            return;

        //out of range to attack move
        if (!MovementUtilities.canMove(aiAgent))
            return;

        aiAgent.destination = MovementUtilities.GetDestinationToCharacter(aiAgent, mob);
        MovementUtilities.moveToLocation(aiAgent, aiAgent.destination, aiAgent.getRange());
    }

    private static void attack(Mob aiAgent, int targetID) {

        //in range to attack, start attacking now!
        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        PlayerCharacter aggroTarget = PlayerCharacter.getFromCache(targetID);

        if (aggroTarget == null) {
            //  Logger.error("MobileFSM.aggro", "aggro target with UUID " + targetID + " returned null");
            aiAgent.playerAgroMap.remove(targetID);
            aiAgent.aggroTargetID = 0;
            aiAgent.state = STATE.Patrol;
            return;
        }

        if (aiAgent.getMobBase().getSeeInvis() < aggroTarget.getHidden()) {
            aiAgent.aggroTargetID = 0;
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Patrol;
            return;
        }

        if (!aggroTarget.isAlive()) {
            aiAgent.aggroTargetID = 0;
            aiAgent.setCombatTarget(null);
            aiAgent.state = STATE.Patrol;
            return;
        }

        HashMap<Integer, Integer> staticPowers = aiAgent.getMobBase().getStaticPowers();
        if (staticPowers != null && !staticPowers.isEmpty()) {

            int chance = ThreadLocalRandom.current().nextInt(100);

            if (chance <= MBServerStatics.AI_POWER_DIVISOR) {

                ArrayList<Integer> powerList = new ArrayList<>();

                for (Integer key : staticPowers.keySet()) {
                    powerList.add(key);
                }

                int randomPower = ThreadLocalRandom.current().nextInt(powerList.size());
                int powerToken = powerList.get(randomPower);

                PowersBase pb = PowersManager.getPowerByToken(powerToken);

                if (pb != null)
                    PowersManager.useMobPower(aiAgent, aggroTarget, pb, staticPowers.get(powerToken));

                return;
            }
        }

        if (!MovementUtilities.inRangeOfBindLocation(aiAgent)) {
            aiAgent.setCombatTarget(null);
            aiAgent.aggroTargetID = 0;
            aiAgent.walkingHome = false;
            aiAgent.state = STATE.Home;
            return;
        }

        if (!MovementUtilities.inRangeDropAggro(aiAgent, aggroTarget)) {
            aiAgent.aggroTargetID = 0;
            aiAgent.setCombatTarget(null);
            MovementUtilities.moveToLocation(aiAgent, aiAgent.bindLoc, 0);
            aiAgent.state = STATE.Awake;
            return;
        }


        if (CombatUtilities.inRangeToAttack(aiAgent, aggroTarget)) {

            if (aiAgent.getCombatTarget() == null)
                aiAgent.setCombatTarget(aggroTarget);

            if (!CombatUtilities.RunAIRandom())
                return;

            //not time to attack yet.
            if (System.currentTimeMillis() < aiAgent.lastAttackTime)
                return;

            if (aiAgent.getRange() >= 30 && aiAgent.isMoving())
                return;

            //no weapons, defualt mob attack speed 3 seconds.
            ItemBase mainHand = StaticMobActions.getWeaponItemBase(aiAgent, true);
            ItemBase offHand = StaticMobActions.getWeaponItemBase(aiAgent, false);

            if (mainHand == null && offHand == null) {
                CombatUtilities.combatCycle(aiAgent, aggroTarget, true, null);
                aiAgent.lastAttackTime = System.currentTimeMillis() + 3000;
            } else
                //TODO set offhand attack time.
                if (StaticMobActions.getWeaponItemBase(aiAgent, true) != null) {

                    int attackDelay = 3000;

                    CombatUtilities.combatCycle(aiAgent, aggroTarget, true, StaticMobActions.getWeaponItemBase(aiAgent, true));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;
                } else if (StaticMobActions.getWeaponItemBase(aiAgent, false) != null) {

                    int attackDelay = 3000;

                    CombatUtilities.combatCycle(aiAgent, aggroTarget, false, StaticMobActions.getWeaponItemBase(aiAgent, false));
                    aiAgent.lastAttackTime = System.currentTimeMillis() + attackDelay;
                }
            return;
        }

        //use this so mobs dont continue to try to move if they are underneath a flying target. only use 2D range check.
        if (CombatUtilities.inRangeToAttack2D(aiAgent, aggroTarget))
            return;

        if (!MovementUtilities.canMove(aiAgent))
            return;

        if (!MovementUtilities.updateMovementToCharacter(aiAgent, aggroTarget))
            return;

        aiAgent.destination = MovementUtilities.GetDestinationToCharacter(aiAgent, aggroTarget);
        MovementUtilities.moveToLocation(aiAgent, aiAgent.destination, aiAgent.getRange());
    }

    private static void home(Mob aiAgent, boolean walk) {

        //recall home.
        MovementManager.translocate(aiAgent, aiAgent.getBindLoc(), null);
        aiAgent.aggroTargetID = 0;
        aiAgent.setCombatTarget(null);
        aiAgent.state =STATE.Awake;
    }

    private static void recall(Mob aiAgent) {
        //recall home.
        PowersBase recall = PowersManager.getPowerByToken(-1994153779);
        PowersManager.useMobPower(aiAgent, aiAgent, recall, 40);
        aiAgent.state = MobileFSM.STATE.Recalling;
    }

    private static void recalling(Mob aiAgent) {
        //recall home.
        if (aiAgent.getLoc() == aiAgent.getBindLoc())
            aiAgent.state = STATE.Awake;

        if (aiAgent.getLoc().distanceSquared2D(aiAgent.getBindLoc()) > sqr(2000)) {

            aiAgent.walkingHome = false;
            aiAgent.state =STATE.Home;
        }
    }

    private static void patrol(Mob aiAgent) {

        MobBase mobbase = aiAgent.getMobBase();

        if (mobbase != null && (Enum.MobFlagType.SENTINEL.elementOf(mobbase.getFlags()) || !Enum.MobFlagType.CANROAM.elementOf(mobbase.getFlags()))) {
            aiAgent.state = STATE.Awake;
            return;
        }

        if (MovementUtilities.canMove(aiAgent) && !aiAgent.isMoving()) {

            float patrolRadius = aiAgent.spawnRadius;

            if (patrolRadius > 256)
                patrolRadius = 256;

            if (patrolRadius < 60)
                patrolRadius = 60;

            MovementUtilities.aiMove(aiAgent, Vector3fImmutable.getRandomPointInCircle(aiAgent.getBindLoc(), patrolRadius), true);
        }
        aiAgent.state = STATE.Awake;
    }

    public static void goHome(Mob aiAgent, boolean walk) {

        if (aiAgent.state != STATE.Dead) {
            aiAgent.walkingHome = walk;
            aiAgent.aggroTargetID = 0;
            aiAgent.state = STATE.Home;
        }
    }

    private static void dead(Mob aiAgent) {
        //Despawn Timer with Loot currently in inventory.
        if (aiAgent.getCharItemManager().getInventoryCount() > 0) {
            if (System.currentTimeMillis() > aiAgent.deathTime + MBServerStatics.DESPAWN_TIMER_WITH_LOOT) {
                StaticMobActions.despawn(aiAgent);
                //update time of death after mob despawns so respawn time happens after mob despawns.
                aiAgent.deathTime = System.currentTimeMillis();
                aiAgent.state = STATE.Respawn;
            }

            //No items in inventory.
        } else {
            //Mob's Loot has been looted.
            if (aiAgent.hasLoot) {
                if (System.currentTimeMillis() > aiAgent.deathTime + MBServerStatics.DESPAWN_TIMER_ONCE_LOOTED) {
                    StaticMobActions.despawn(aiAgent);
                    //update time of death after mob despawns so respawn time happens after mob despawns.
                    aiAgent.deathTime = System.currentTimeMillis();
                    aiAgent.state = STATE.Respawn;
                }
                //Mob never had Loot.
            } else {
                if (System.currentTimeMillis() > aiAgent.deathTime + MBServerStatics.DESPAWN_TIMER) {
                    StaticMobActions.despawn(aiAgent);
                    //update time of death after mob despawns so respawn time happens after mob despawns.
                    aiAgent.deathTime = System.currentTimeMillis();
                    aiAgent.state = STATE.Respawn;
                }
            }
        }
    }

    private static void guardAwake(Mob aiAgent) {

        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        if (aiAgent.getLoc().distanceSquared2D(aiAgent.getBindLoc()) > sqr(2000)) {

            aiAgent.walkingHome = false;
            aiAgent.state = STATE.Home;
            return;
        }

        //Don't attempt to aggro if No aggro is on and aiAgent is not home yet.

        //Mob stopped Moving let's turn aggro back on.
        if (aiAgent.noAggro)
            aiAgent.noAggro = false;

        // do nothing if no players are around.
        if (aiAgent.playerAgroMap.isEmpty())
            return;

        //Get the Map for Players that loaded this mob.

        ConcurrentHashMap<Integer, Boolean> loadedPlayers = aiAgent.playerAgroMap;

        //no players currently have this mob loaded. return to IDLE.
        //aiAgent finished moving home, set aggro on.

        for (Entry playerEntry : loadedPlayers.entrySet()) {

            int playerID = (int) playerEntry.getKey();

            PlayerCharacter loadedPlayer = PlayerCharacter.getFromCache(playerID);

            //Player is null, let's remove them from the list.
            if (loadedPlayer == null) {
                //     Logger.error("MobileFSM", "Player with UID " + playerID + " returned null in mob.getPlayerAgroMap()");
                loadedPlayers.remove(playerID);
                continue;
            }

            //Player is Dead, Mob no longer needs to attempt to aggro. Remove them from aggro map.
            if (!loadedPlayer.isAlive()) {
                loadedPlayers.remove(playerID);
                continue;
            }

            //Can't see target, skip aggro.
            if (aiAgent.mobBase.getSeeInvis() >= loadedPlayer.getHidden()) {
                continue;
            }

            //Guard aggro check

            boolean aggro = false;
            Zone cityZone = aiAgent.parentZone;

            if (cityZone != null) {
                City city = City.GetCityFromCache(cityZone.getPlayerCityUUID());
                if (city != null) {

                    Building tol = city.getTOL();

                    if (tol != null) {
                        if (tol.reverseKOS) {

                            aggro = true;

                            for (Condemned condemned : tol.getCondemned().values()) {
                                switch (condemned.getFriendType()) {
                                    case Condemned.NATION:
                                        if (loadedPlayer.getGuild() != null && loadedPlayer.getGuild().getNation() != null)
                                            if (loadedPlayer.getGuild().getNation().getObjectUUID() == condemned.getGuildUID())
                                                if (condemned.isActive())
                                                    aggro = false;
                                        break;
                                    case Condemned.GUILD:
                                        if (loadedPlayer.getGuild() != null)
                                            if (loadedPlayer.getGuild().getObjectUUID() == condemned.getGuildUID())
                                                if (condemned.isActive())
                                                    aggro = false;
                                        break;
                                    case Condemned.INDIVIDUAL:
                                        if (loadedPlayer.getObjectUUID() == condemned.getPlayerUID())
                                            if (condemned.isActive())
                                                aggro = false;
                                        break;
                                }
                            }
                        } else {
                            aggro = false;

                            for (Condemned condemned : tol.getCondemned().values()) {
                                switch (condemned.getFriendType()) {
                                    case Condemned.NATION:
                                        if (loadedPlayer.getGuild() != null && loadedPlayer.getGuild().getNation() != null)
                                            if (loadedPlayer.getGuild().getNation().getObjectUUID() == condemned.getGuildUID())
                                                if (condemned.isActive())
                                                    aggro = true;
                                        break;
                                    case Condemned.GUILD:
                                        if (loadedPlayer.getGuild() != null)
                                            if (loadedPlayer.getGuild().getObjectUUID() == condemned.getGuildUID())
                                                if (condemned.isActive())
                                                    aggro = true;
                                        break;
                                    case Condemned.INDIVIDUAL:
                                        if (loadedPlayer.getObjectUUID() == condemned.getPlayerUID())
                                            if (condemned.isActive())
                                                aggro = true;
                                        break;
                                }
                            }
                        }
                    }
                }

                if (loadedPlayer.getGuild() != null && loadedPlayer.getGuild().getNation() != null && city.getGuild() != null)
                    if (Guild.sameGuild(loadedPlayer.getGuild().getNation(), city.getGuild().getNation()))
                        aggro = false;

            }

            //lets make sure we dont aggro players in the nation.

            if (aggro) {
                if (CombatUtilities.inRangeToAttack(aiAgent, loadedPlayer)) {
                    aiAgent.aggroTargetID = playerID;
                    aiAgent.state = STATE.Aggro;
                    return;
                }

                if (MovementUtilities.inRangeToAggro(aiAgent, loadedPlayer)) {
                    aiAgent.aggroTargetID = playerID;
                    aiAgent.state = STATE.Aggro;
                    return;
                }
            }
        }

        //attempt to patrol even if aiAgent isn't aggresive;
        if (aiAgent.isMoving() == false)
            aiAgent.state = STATE.Patrol;
    }

    private static void guardAggro(Mob aiAgent, int targetID) {

        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        if (!aiAgent.isCombat()) {
            aiAgent.setCombat(true);
            UpdateStateMsg rwss = new UpdateStateMsg();
            rwss.setPlayer(aiAgent);
            DispatchMessage.sendToAllInRange(aiAgent, rwss);
        }

        //a player got in aggro range. Move to player until in range of attack.
        PlayerCharacter aggroTarget = PlayerCharacter.getFromCache(targetID);

        if (aggroTarget == null) {
            aiAgent.state = STATE.Patrol;
            return;
        }

        if (aiAgent.mobBase.getSeeInvis() >= aggroTarget.getHidden()) {
            aiAgent.setCombatTarget(null);
            targetID = 0;
            aiAgent.state = STATE.Patrol;
            return;
        }

        if (!aggroTarget.isActive()) {
            aiAgent.setCombatTarget(null);
            targetID = 0;
            aiAgent.state = STATE.Patrol;
            return;
        }

        if (System.currentTimeMillis() > aiAgent.getTimeStamp("CallForHelp")) {
            CombatUtilities.CallForHelp(aiAgent);
            aiAgent.getTimestamps().put("CallForHelp", System.currentTimeMillis() + 60000);
        }


        if (CombatUtilities.inRangeToAttack(aiAgent, aggroTarget)) {
            aiAgent.setCombatTarget(aggroTarget);
            aiAgent.state = STATE.Attack;
            guardAttack(aiAgent);
            return;
        }

        //use this so mobs dont continue to try to move if they are underneath a flying target. only use 2D range check.
        if (CombatUtilities.inRangeToAttack2D(aiAgent, aggroTarget))
            return;


        if (!MovementUtilities.canMove(aiAgent))
            return;

        if (!MovementUtilities.inRangeDropAggro(aiAgent, aggroTarget)) {
            aiAgent.aggroTargetID = 0;
            aiAgent.setCombatTarget(null);
            MovementUtilities.moveToLocation(aiAgent, aiAgent.bindLoc, 0);
            aiAgent.state = STATE.Awake;
            return;
        }

        if (!MovementUtilities.inRangeOfBindLocation(aiAgent)) {
            aiAgent.setCombatTarget(null);
            aiAgent.aggroTargetID = 0;
            aiAgent.walkingHome = false;
            aiAgent.state = STATE.Home;
            return;
        }

        if (!MovementUtilities.updateMovementToCharacter(aiAgent, aggroTarget))
            return;

        //Outside of attack Range, Move to players predicted loc.

        if (aiAgent.getLoc().distanceSquared2D(aggroTarget.getLoc()) < aiAgent.getRange() * aiAgent.getRange())
            return;
        aiAgent.destination = MovementUtilities.GetDestinationToCharacter(aiAgent, aggroTarget);
        MovementUtilities.moveToLocation(aiAgent, aiAgent.destination, aiAgent.getRange());

    }

    private static void guardPatrol(Mob aiAgent) {

        if (aiAgent.playerAgroMap.isEmpty()) {
            aiAgent.state = STATE.Awake;
            return;
        }

        if (aiAgent.isCombat() && aiAgent.getCombatTarget() == null) {
            aiAgent.setCombat(false);
            UpdateStateMsg rwss = new UpdateStateMsg();
            rwss.setPlayer(aiAgent);
            DispatchMessage.sendToAllInRange(aiAgent, rwss);
        }

        if (aiAgent.npcOwner == null) {

            if (!aiAgent.isWalk() || (aiAgent.isCombat() && aiAgent.getCombatTarget() == null)) {
                aiAgent.setWalkMode(true);
                aiAgent.setCombat(false);
                UpdateStateMsg rwss = new UpdateStateMsg();
                rwss.setPlayer(aiAgent);
                DispatchMessage.sendToAllInRange(aiAgent, rwss);
            }

            if (aiAgent.isMoving()) {
                aiAgent.state = STATE.Awake;
                return;
            }

            Building barrack = aiAgent.building;

            if (barrack == null) {
                aiAgent.state = STATE.Awake;
                return;
            }

            int patrolRandom = ThreadLocalRandom.current().nextInt(1000);

            if (patrolRandom <= 10) {
                int buildingHitBox = (int) CombatManager.calcHitBox(barrack);
                if (MovementUtilities.canMove(aiAgent)) {
                    MovementUtilities.aiMove(aiAgent, MovementUtilities.randomPatrolLocation(aiAgent, aiAgent.getBindLoc(), buildingHitBox * 2), true);
                }
            }

            aiAgent.state = STATE.Awake;
            return;

        }

        if (!aiAgent.isWalk() || (aiAgent.isCombat() && aiAgent.getCombatTarget() == null)) {
            aiAgent.setWalkMode(true);
            aiAgent.setCombat(false);
            UpdateStateMsg rwss = new UpdateStateMsg();
            rwss.setPlayer(aiAgent);
            DispatchMessage.sendToAllInRange(aiAgent, rwss);

        }

        Building barrack = ((Mob) aiAgent.npcOwner).building;

        if (barrack == null) {
            aiAgent.state = STATE.Awake;
            return;
        }

        if (barrack.getPatrolPoints() == null) {
            aiAgent.state = STATE.Awake;
            return;
        }

        if (barrack.getPatrolPoints().isEmpty()) {
            aiAgent.state = STATE.Awake;
            return;
        }

        if (aiAgent.isMoving()) {
            aiAgent.state = STATE.Awake;
            return;
        }

        int patrolRandom = ThreadLocalRandom.current().nextInt(1000);

        if (patrolRandom <= 10) {
            if (aiAgent.patrolPointIndex < barrack.getPatrolPoints().size()) {
                Vector3fImmutable patrolLoc = barrack.getPatrolPoints().get(aiAgent.patrolPointIndex);
                aiAgent.patrolPointIndex = aiAgent.patrolPointIndex + 1;
                if (aiAgent.patrolPointIndex == barrack.getPatrolPoints().size())
                    aiAgent.patrolPointIndex = 0;

                if (patrolLoc != null) {
                    if (MovementUtilities.canMove(aiAgent)) {
                        MovementUtilities.aiMove(aiAgent, patrolLoc, true);
                        aiAgent.state = STATE.Awake;
                    }
                }
            }
        }
        aiAgent.state = STATE.Awake;
    }

    private static void guardAttack(Mob aiAgent) {

        if (!aiAgent.isAlive()) {
            aiAgent.state = STATE.Dead;
            return;
        }

        AbstractGameObject target = aiAgent.getCombatTarget();

        if (target == null) {
            aiAgent.state = STATE.Patrol;
            return;
        }

        switch (target.getObjectType()) {
            case PlayerCharacter:

                PlayerCharacter player = (PlayerCharacter) target;

                if (!player.isActive()) {
                    aiAgent.setCombatTarget(null);
                    aiAgent.state = STATE.Patrol;
                    return;
                }

                if (aiAgent.mobBase.isNecroPet() && player.inSafeZone()) {
                    aiAgent.setCombatTarget(null);
                    aiAgent.state = STATE.Idle;
                    return;
                }

                handlePlayerAttackForMob(aiAgent, player);
                break;
            case Building:
                Logger.info("PLAYER GUARD ATTEMPTING TO ATTACK BUILDING IN " + aiAgent.parentZone.getName());
                aiAgent.state = STATE.Awake;
                break;
            case Mob:
                Mob mob = (Mob) target;
                handleMobAttackForMob(aiAgent, mob);
        }
    }

    private static void guardHome(Mob aiAgent, boolean walk) {

        //recall home.
        PowersBase recall = PowersManager.getPowerByToken(-1994153779);
        PowersManager.useMobPower(aiAgent, aiAgent, recall, 40);

        aiAgent.aggroTargetID = 0;
        aiAgent.setCombatTarget(null);
        aiAgent.state = STATE.Awake;
    }

    private static void guardRespawn(Mob aiAgent) {

        if (!StaticMobActions.canRespawn(aiAgent))
            return;

        if (aiAgent.isPlayerGuard && aiAgent.npcOwner != null && !aiAgent.npcOwner.isAlive())
            return;

        long spawnTime = StaticMobActions.getSpawnTime(aiAgent);

        if (System.currentTimeMillis() > aiAgent.deathTime + spawnTime) {
            StaticMobActions.respawn(aiAgent);
            aiAgent.state = STATE.Idle;
        }
    }

    private static void respawn(Mob aiAgent) {

        if (StaticMobActions.canRespawn(aiAgent))
            return;

        long spawnTime = StaticMobActions.getSpawnTime(aiAgent);

        if (aiAgent.isPlayerGuard && aiAgent.npcOwner != null && !aiAgent.npcOwner.isAlive())
            return;

        if (System.currentTimeMillis() > aiAgent.deathTime + spawnTime) {
            StaticMobActions.respawn(aiAgent);
            aiAgent.state = STATE.Idle;
        }
    }

    private static void retaliate(Mob aiAgent) {

        if (aiAgent.getCombatTarget() == null)
            aiAgent.state = STATE.Awake;

        //out of range to attack move
        if (!MovementUtilities.canMove(aiAgent)) {
            aiAgent.state = STATE.Attack;
            return;
        }

        aiAgent.state = STATE.Attack;

        //lets make mobs ai less twitchy, Don't call another movement until mob reaches it's destination.
        if (aiAgent.isMoving())
            return;

        MovementUtilities.moveToLocation(aiAgent, aiAgent.getCombatTarget().getLoc(), aiAgent.getRange());
    }

    private static void moveToWorldObjectRegion(Mob mob, AbstractWorldObject regionObject) {

        if (regionObject.getRegion() == null)
            return;

        MovementManager.translocate(mob, regionObject.getLoc(), null);
    }
}
