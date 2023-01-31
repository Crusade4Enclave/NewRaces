// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.gameManager;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.ai.StaticMobActions;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.UpgradeBuildingJob;
import engine.math.Bounds;
import engine.math.Vector3fImmutable;
import engine.net.client.msg.ErrorPopupMsg;
import engine.objects.*;
import org.pmw.tinylog.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;

public enum BuildingManager {

    BUILDINGMANAGER;

    public static boolean playerCanManage(PlayerCharacter player, Building building) {

        if (player == null)
            return false;

        if (building == null)
            return false;


        if (building.getRank() == -1)
            return false;

        if (IsOwner(building, player))
            return true;

        //individual friend.
        if (building.getFriends().get(player.getObjectUUID()) != null)
            return true;

        //Admin's can access stuff

        if (player.isCSR())
            return true;

        //Guild stuff


        if (building.getGuild() != null && building.getGuild().isGuildLeader(player.getObjectUUID()))
            return true;

        if (building.getFriends().get(player.getGuild().getObjectUUID()) != null
                && building.getFriends().get(player.getGuild().getObjectUUID()).getFriendType() == 8)
            return true;

        if (building.getFriends().get(player.getGuild().getObjectUUID()) != null
                && building.getFriends().get(player.getGuild().getObjectUUID()).getFriendType() == 9
                && GuildStatusController.isInnerCouncil(player.getGuildStatus()))
            return true;

        if (Guild.sameGuild(building.getGuild(), player.getGuild()) && GuildStatusController.isInnerCouncil(player.getGuildStatus()))
            return true;

        if (Guild.sameGuild(building.getGuild(), player.getGuild()) && GuildStatusController.isGuildLeader(player.getGuildStatus()))
            return true;

        return false;

        //TODO test friends list once added
        //does not meet above criteria. Cannot access.
    }

    public static boolean playerCanManageNotFriends(PlayerCharacter player, Building building) {

        //Player Can only Control Building if player is in Same Guild as Building and is higher rank than IC.

        if (player == null)
            return false;

        if (building == null)
            return false;


        if (building.getRank() == -1)
            return false;

        if (IsOwner(building, player))
            return true;

        //Somehow guild leader check fails? lets check if Player is true Guild GL.
        if (building.getGuild() != null && building.getGuild().isGuildLeader(player.getObjectUUID()))
            return true;
        if (GuildStatusController.isGuildLeader(player.getGuildStatus()) == false && GuildStatusController.isInnerCouncil(player.getGuildStatus()) == false)
            return false;


        return false;

    }

    public static synchronized boolean lootBuilding(PlayerCharacter player, Building building) {

        if (building == null)
            return false;

        if (player == null)
            return false;

        if (building.getRank() != -1)
            return false;

        if (building.getBlueprintUUID() == 0)
            return false;

        switch (building.getBlueprint().getBuildingGroup()) {
            case SHRINE:
                Shrine shrine = Shrine.shrinesByBuildingUUID.get(building.getObjectUUID());
                if (shrine == null)
                    return false;

                int amount = shrine.getFavors();
                //no more favors too loot!
                if (amount == 0) {

                    try {
                        ErrorPopupMsg.sendErrorPopup(player, 166);
                    } catch (Exception e) {

                    }
                    return false;
                }

                ItemBase elanIB = ItemBase.getItemBase(1705032);

                if (elanIB == null)
                    return false;

                if (!player.getCharItemManager().hasRoomInventory(elanIB.getWeight()))
                    return false;

                if (!Item.MakeItemForPlayer(elanIB, player, amount))
                    return false;

                shrine.setFavors(0);
                break;
            case WAREHOUSE:

                Warehouse warehouse = Warehouse.warehouseByBuildingUUID.get(building.getObjectUUID());

                if (warehouse == null)
                    return false;

                for (ItemBase resourceBase : ItemBase.getResourceList()) {
                    if (!player.getCharItemManager().hasRoomInventory(resourceBase.getWeight())) {
                        ChatManager.chatSystemInfo(player, "You can not carry any more of that item.");
                        return false;
                    }
                    if (warehouse.getResources().get(resourceBase) == null)
                        continue;

                    int resourceAmount = warehouse.getResources().get(resourceBase);

                    if (resourceAmount <= 0)
                        continue;

                    if (warehouse.loot(player, resourceBase, resourceAmount, true))
                        ChatManager.chatInfoInfo(player, "You have looted " + resourceAmount + ' ' + resourceBase.getName());
                }
                break;

        }
        //Everything was looted, Maybe we should
        return true;
    }

    //This method restarts an upgrade timer when a building is loaded from the database.
    // Submit upgrade job for this building based upon it's current upgradeDateTime

    public static void submitUpgradeJob(Building building) {

        if (building == null)
            return;


        if (building.getUpgradeDateTime() == null) {
            Logger.error("Attempt to submit upgrade job for non-ranking building");
            return;
        }

        // Submit upgrade job for future date or current instant

        if (building.getUpgradeDateTime().isAfter(LocalDateTime.now())) {
            JobContainer jc = JobScheduler.getInstance().scheduleJob(new UpgradeBuildingJob(building),
                    building.getUpgradeDateTime().atZone(ZoneId.systemDefault())
                            .toInstant().toEpochMilli());
        } else
            JobScheduler.getInstance().scheduleJob(new UpgradeBuildingJob(building), 0);
    }

    public static void setUpgradeDateTime(Building building, LocalDateTime upgradeDateTime, int rankCost) {

        if (building == null)
            return;
        if (!DbManager.BuildingQueries.updateBuildingUpgradeTime(upgradeDateTime, building, rankCost)) {
            Logger.error("Failed to set upgradeTime for building " + building.getObjectUUID());
            return;
        }

        building.upgradeDateTime = upgradeDateTime;

    }

    // Method transfers ownership of all hirelings in a building

    public static void refreshHirelings(Building building) {

        if (building == null)
            return;

        Guild newGuild;

        if (building.getOwner() == null)
            newGuild = Guild.getErrantGuild();
        else
            newGuild = building.getOwner().getGuild();

        for (AbstractCharacter hireling : building.getHirelings().keySet()) {
            hireling.setGuild(newGuild);
            WorldGrid.updateObject(hireling);
        }

    }

    public static void cleanupHirelings(Building building) {

        // Early exit:  Cannot have hirelings in a building
        // without a blueprint.

        if (building.getBlueprintUUID() == 0)
            return;

        // Remove all hirelings for destroyed buildings

        if (building.getRank() < 1) {

            for (AbstractCharacter slottedNPC : building.getHirelings().keySet()) {

                if (slottedNPC.getObjectType() == Enum.GameObjectType.NPC)
                    ((NPC) slottedNPC).remove();
                else if (slottedNPC.getObjectType() == Enum.GameObjectType.Mob)
                    StaticMobActions.remove(((Mob) slottedNPC),building);
            }
            return;
        }

        // Delete hireling if building has deranked.
        for (AbstractCharacter hireling : building.getHirelings().keySet()) {

            NPC npc = null;
            Mob mob = null;

            if (hireling.getObjectType() == Enum.GameObjectType.NPC)
                npc = (NPC) hireling;
            else if (hireling.getObjectType() == Enum.GameObjectType.Mob)
                mob = (Mob) hireling;

            if (building.getHirelings().get(hireling) > building.getBlueprint().getSlotsForRank(building.getRank()))

                if (npc != null) {
                    if (!npc.remove())
                        Logger.error("Failed to remove npc " + npc.getObjectUUID()
                                + "from Building " + building.getObjectUUID());
                    else
                        building.getHirelings().remove(npc);
                } else if (mob != null) {
                    if (!StaticMobActions.remove(mob,building))
                        Logger.error("Failed to remove npc " + npc.getObjectUUID()
                                + "from Building " + building.getObjectUUID());
                    else
                        building.getHirelings().remove(npc);
                }

        }

        refreshHirelings(building);
    }

    public static Building getBuilding(int id) {

        if (id == 0)
            return null;

        Building building;

        building = (Building) DbManager.getFromCache(Enum.GameObjectType.Building, id);

        if (building != null)
            return building;

        return DbManager.BuildingQueries.GET_BUILDINGBYUUID(id);

    }

    public static boolean PlayerCanControlNotOwner(Building building, PlayerCharacter player) {

        if (player == null)
            return false;

        if (building == null)
            return false;

        if (building.getOwner() == null)
            return false;

        //lets pass true if player is owner anyway.
        if (building.getOwner().equals(player))
            return true;

        if (player.getGuild().isErrant())
            return false;

        if (building.getGuild().isGuildLeader(player.getObjectUUID()))
            return true;

        if (!Guild.sameGuild(building.getGuild(), player.getGuild()))
            return false;

        return GuildStatusController.isGuildLeader(player.getGuildStatus()) != false || GuildStatusController.isInnerCouncil(player.getGuildStatus()) != false;
    }

    //This is mainly used for Rolling and gold sharing between building and warehouse.

    public static int GetWithdrawAmountForRolling(Building building, int cost) {

        //all funds are available to roll.

        if (cost <= GetAvailableGold(building))
            return cost;

        // cost is more than available gold, return available gold

        return GetAvailableGold(building);

    }

    public static int GetAvailableGold(Building building) {

        if (building.getStrongboxValue() == 0)
            return 0;

        if (building.getStrongboxValue() < building.reserve)
            return 0;

        return building.getStrongboxValue() - building.reserve;
    }

    public static int GetOverdraft(Building building, int cost) {
        int availableGold = GetWithdrawAmountForRolling(building, cost);
        return cost - availableGold;
    }

    public static boolean IsPlayerHostile(Building building, PlayerCharacter player) {

        //Nation Members and Guild members are not hostile.
        //		if (building.getGuild() != null){
        //			if (pc.getGuild() != null)
        //			if (building.getGuild().getObjectUUID() == pc.getGuildUUID()
        //			|| pc.getGuild().getNation().getObjectUUID() == building.getGuild().getNation().getObjectUUID())
        //				return false;
        //		}
        if (Guild.sameNationExcludeErrant(building.getGuild(), player.getGuild()))
            return false;

        if (!building.reverseKOS) {

            Condemned condemn = building.getCondemned().get(player.getObjectUUID());

            if (condemn != null && condemn.isActive())
                return true;

            if (player.getGuild() != null) {

                Condemned guildCondemn = building.getCondemned().get(player.getGuild().getObjectUUID());

                if (guildCondemn != null && guildCondemn.isActive())
                    return true;

                Condemned nationCondemn = building.getCondemned().get(player.getGuild().getNation().getObjectUUID());
                return nationCondemn != null && nationCondemn.isActive() && nationCondemn.getFriendType() == Condemned.NATION;
            } else {
                //TODO ADD ERRANT KOS CHECK
            }
        } else {

            Condemned condemn = building.getCondemned().get(player.getObjectUUID());

            if (condemn != null && condemn.isActive())
                return false;

            if (player.getGuild() != null) {

                Condemned guildCondemn = building.getCondemned().get(player.getGuild().getObjectUUID());

                if (guildCondemn != null && guildCondemn.isActive())
                    return false;

                Condemned nationCondemn = building.getCondemned().get(player.getGuild().getNation().getObjectUUID());
                return nationCondemn == null || !nationCondemn.isActive() || nationCondemn.getFriendType() != Condemned.NATION;
            } else {
                //TODO ADD ERRANT KOS CHECK
            }
            return true;
        }

        //When we get to here, This means The building was not reverse KOS
        //and passed the hostile test.

        return false;
    }

    public static final synchronized boolean addHirelingForWorld(Building building, PlayerCharacter contractOwner, Vector3fImmutable NpcLoc, Zone zone, Contract NpcID, int rank) {

        String pirateName = NPC.getPirateName(NpcID.getMobbaseID());

        NPC npc = null;

        npc = NPC.createNPC( pirateName, NpcID.getObjectUUID(), NpcLoc, null, false, zone, (short) rank, false, building);

        if (npc == null)
            return false;


        npc.setBuilding(building);
        npc.setParentZone(zone);
        WorldGrid.addObject(npc, contractOwner);

        return true;

    }

    public static synchronized boolean addHireling(Building building, PlayerCharacter contractOwner, Vector3fImmutable NpcLoc, Zone zone, Contract NpcID, Item item) {

        int rank = 1;

        if (building.getBlueprintUUID() == 0)
            return false;

        if (building.getBlueprint().getMaxSlots() == building.getHirelings().size())
            return false;

        String pirateName = NPC.getPirateName(NpcID.getMobbaseID());

        if (item.getChargesRemaining() > 0)
            rank = item.getChargesRemaining() * 10;
        else rank = 10;

        Mob mob = null;
        NPC npc = null;

        if (NPC.ISGuardCaptain(NpcID.getContractID())) {

            mob = Mob.createMob( NpcID.getMobbaseID(), NpcLoc, contractOwner.getGuild(), true, zone, building, NpcID.getContractID());

            if (mob == null)
                return false;

            mob.level = (short) (rank * 10);
            mob.isPlayerGuard = true;
            mob.parentZone = zone;
            return true;
        } else {
            npc = NPC.createNPC( pirateName, NpcID.getObjectUUID(), NpcLoc, contractOwner.getGuild(), false, zone, (short) rank, false, building);

            if (npc == null)
                return false;


            npc.setBindLoc(NpcLoc.x - zone.getAbsX(), NpcLoc.y - zone.getAbsY(), NpcLoc.z - zone.getAbsZ());

            npc.setBuilding(building);
            npc.setParentZone(zone);

            if (NPC.GetNPCProfits(npc) == null)
                NPCProfits.CreateProfits(npc);

            WorldGrid.addObject(npc, contractOwner);

            return true;
        }
    }

    public static boolean IsWallPiece(Building building) {

        if (building.getBlueprint() == null)
            return false;

        BuildingGroup buildingGroup = building.getBlueprint().getBuildingGroup();

        switch (buildingGroup) {
            case WALLSTRAIGHT:
            case WALLCORNER:
            case SMALLGATE:
            case ARTYTOWER:
            case WALLSTRAIGHTTOWER:
            case WALLSTAIRS:
                return true;
            default:
                return false;
        }
    }

    public static Building getBuildingFromCache(int id) {
        return (Building) DbManager.getFromCache(GameObjectType.Building, id);
    }

    public static boolean IsOwner(Building building, PlayerCharacter player) {
        if (building == null || player == null)
            return false;
        
        if (building.getOwner() == null)
        	return false;
        

        return building.getOwner().getObjectUUID() == player.getObjectUUID();

    }

    public static float GetMissingHealth(Building building) {
        return building.healthMax - building.getCurrentHitpoints();
    }

    public static int GetRepairCost(Building building) {
        return (int) (GetMissingHealth(building) * .10f);
    }

    public static Regions GetRegion(Building building, float x, float y, float z) {
        if (building.getBounds() == null)
            return null;

        if (building.getBounds().getRegions() == null)
            return null;

        Regions currentRegion = null;
        for (Regions region : building.getBounds().getRegions()) {

            if (region.isPointInPolygon(new Vector3fImmutable(x, y, z))) {
                if (y > (region.highLerp.y - 5))
                    currentRegion = region;
            }
        }
        return currentRegion;
    }

    public static Regions GetRegion(Building building, int room, int level, float x, float z) {
        if (building.getBounds() == null)
            return null;

        if (building.getBounds().getRegions() == null)
            return null;

        for (Regions region : building.getBounds().getRegions()) {

            if (region.getLevel() != level)
                continue;
            if (region.getRoom() != room)
                continue;

            if (region.isPointInPolygon(new Vector3fImmutable(x, 0, z))) {
                return region;
            }
        }
        return null;
    }
    
    public static Vector3fImmutable GetBindLocationForBuilding(Building building){
   
    		Vector3fImmutable bindLoc = null;
    		
    		if (building == null)
    			return Enum.Ruins.getRandomRuin().getLocation();
    		
    		
    		bindLoc = building.getLoc();
    		
    	
    			float radius = Bounds.meshBoundsCache.get(building.getMeshUUID()).radius;
    			if ( building.getRank() == 8){
    				bindLoc = building.getStuckLocation();
    				if (bindLoc != null)
    					return bindLoc;
    			}

        			float x = bindLoc.getX();
        			float z = bindLoc.getZ();
        			float offset = ((ThreadLocalRandom.current().nextFloat() * 2) - 1) * radius;
        			int direction = ThreadLocalRandom.current().nextInt(4);

        			switch (direction) {
        			case 0:
        				x += radius;
        				z += offset;
        				break;
        			case 1:
        				x += offset;
        				z -= radius;
        				break;
        			case 2:
        				x -= radius;
        				z += offset;
        				break;
        			case 3:
        				x += offset;
        				z += radius;
        				break;
        			}
        			bindLoc = new Vector3fImmutable(x, bindLoc.getY(), z);
    		
			return bindLoc;

    		
    	
    }

}
