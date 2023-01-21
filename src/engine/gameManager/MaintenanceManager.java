// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

// Defines static methods which comprise the magicbane
// building maintenance system.

import engine.Enum;
import engine.objects.*;
import org.pmw.tinylog.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;

public enum MaintenanceManager {

    MAINTENANCEMANAGER;

    public static void setMaintDateTime(Building building, LocalDateTime maintDate) {

        building.maintDateTime = maintDate;
        DbManager.BuildingQueries.updateMaintDate(building);

    }

    public static void processBuildingMaintenance() {

        ArrayList<AbstractGameObject> buildingList;
        ArrayList<Building> maintList;
        ArrayList<Building> derankList = new ArrayList<>();

        Logger.info("Starting Maintenance on Player Buildings");

        // Build list of buildings to apply maintenance on.

        buildingList = new ArrayList(DbManager.getList(Enum.GameObjectType.Building));
        maintList = buildMaintList(buildingList);

        // Deduct upkeep and build list of buildings
        // which did not have funds available

        for (Building building : maintList) {

            if (chargeUpkeep(building) == false)
                derankList.add(building);
        }

        // Reset maintenance dates for these buildings

        for (Building building : maintList)
            setMaintDateTime(building, building.maintDateTime.plusDays(7));

        // Derak or destroy buildings that did not
        // have funds available.

        for (Building building : derankList)
            building.destroyOrDerank(null);

        Logger.info("Structures: " + buildingList.size() + " Maint: " + maintList.size() + " Derank: " + derankList.size());
    }

    // Iterate over all buildings in game and apply exclusion rules
    // returning a list of building for which maintenance is due.

    private static ArrayList<Building> buildMaintList(ArrayList<AbstractGameObject> buildingList) {

        ArrayList<Building> maintList = new ArrayList<>();

        for (AbstractGameObject gameObject : buildingList) {

            Building building = (Building) gameObject;

            // No Maintenance on fidelity structures

            if (building.getProtectionState() == Enum.ProtectionState.NPC)
                continue;

            // No maintenance on constructing meshes

            if (building.getRank() < 1)
                continue;

            // No Maintenance on furniture

            if (building.parentBuildingID != 0)
                continue;

            // No Blueprint?

            if (building.getBlueprint() == null) {
                Logger.error("Blueprint missing for uuid: " + building.getObjectUUID());
                continue;
            }

            // No maintenance on banestones omfg

            if (building.getBlueprint().getBuildingGroup().equals(Enum.BuildingGroup.BANESTONE))
                continue;

            // no maintenance on Mines omfg

            if (building.getBlueprint().getBuildingGroup().equals(Enum.BuildingGroup.MINE))
                continue;

            // Null Maintenance date?

            if (building.maintDateTime == null) {
                Logger.error("Null maint date for building UUID: " + building.getObjectUUID());
                continue;
            }

            // Maintenance date is in the future

            if (building.maintDateTime.isAfter(LocalDateTime.now()))
                continue;

            //  Add building to maintenance queue

            maintList.add(building);
        }

        return maintList;
    }

    // Method removes the appropriate amount of gold/resources from
    // a building according to it's maintenance schedule.  True/False
    // is returned indicating if the building had enough funds to cover.

    public static boolean chargeUpkeep(Building building) {

        City city = null;
        Warehouse warehouse = null;
        int maintCost = 0;
        int overDraft = 0;
        boolean hasFunds = false;
        boolean hasResources = false;
        int resourceValue = 0;

        city = building.getCity();

        if (city != null)
            warehouse = city.getWarehouse();

        // Cache maintenance cost value

        maintCost = building.getMaintCost();

        // Something went wrong.  Missing buildinggroup from switch?

        if (maintCost == 0) {
            Logger.error("chargeUpkeep", "Error retrieving rankcost for " + building.getName() + " uuid:" + building.getObjectUUID() + "buildinggroup:" + building.getBlueprint().getBuildingGroup().name());
            // check if there is enough gold on the building
            return true;
        }

        if (building.getStrongboxValue() >= maintCost)
            hasFunds = true;

        // If we cannot cover with just the strongbox
        // see if there is a warehouse that will cover
        // the overdraft for us.


        if (hasFunds == false && (building.assetIsProtected() || building.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.WAREHOUSE)) {
            overDraft = maintCost - building.getStrongboxValue();
        }

        if ((overDraft > 0))
            if ((building.getBlueprint().getBuildingGroup().equals(Enum.BuildingGroup.SHRINE) == false) &&
                    (warehouse != null) && building.assetIsProtected() == true &&
                    (warehouse.getResources().get(ItemBase.GOLD_ITEM_BASE)) >= overDraft) {
                hasFunds = true;
            }

        // If this is an R8 tree, validate that we can
        // cover the resources required

        if (building.getRank() == 8) {

            hasResources = true;

            if (warehouse == null)
                hasResources = false;
            else {

                resourceValue = warehouse.getResources().get(Warehouse.stoneIB);

                if (resourceValue < 1500)
                    hasResources = false;

                resourceValue = warehouse.getResources().get(Warehouse.lumberIB);

                if (resourceValue < 1500)
                    hasResources = false;

                resourceValue = warehouse.getResources().get(Warehouse.galvorIB);

                if (resourceValue < 5)
                    hasResources = false;

                resourceValue = warehouse.getResources().get(Warehouse.wormwoodIB);

                if (resourceValue < 5)
                    hasResources = false;

            }
        }
        // Validation completed but has failed.  We can derank
        // the target building and early exit

        if ((hasFunds == false) ||
                ((building.getRank() == 8) && !hasResources)) {

            // Add cash back to strongbox for lost rank if the building isn't being destroyed
            // and it's not an R8 deranking

            if ((building.getRank() > 1) && (building.getRank() < 8)) {
                building.setStrongboxValue(building.getStrongboxValue() + building.getBlueprint().getRankCost(Math.min(building.getRank(), 7)));
            }

            return false; // Early exit for having failed to meet maintenance
        }

        // Remove cash and resources

        // withdraw what we can from the building

        building.setStrongboxValue(building.getStrongboxValue() - (maintCost - overDraft));

        // withdraw overdraft from the whorehouse

        if (overDraft > 0) {

            resourceValue = warehouse.getResources().get(Warehouse.goldIB);

            if (DbManager.WarehouseQueries.updateGold(warehouse, resourceValue - overDraft) == true) {
                warehouse.getResources().put(Warehouse.goldIB, resourceValue - overDraft);
                warehouse.AddTransactionToWarehouse(Enum.GameObjectType.Building, building.getObjectUUID(), Enum.TransactionType.WITHDRAWL, Resource.GOLD, overDraft);
            } else {
                Logger.error("gold update failed for warehouse of UUID:" + warehouse.getObjectUUID());
                return true;
            }
        }

        // Early exit as we're done if we're not an R8 tree

        if (building.getRank() < 8)
            return true;

        // Now for the resources if it's an R8 tree

        // Withdraw Stone

        resourceValue = warehouse.getResources().get(Warehouse.stoneIB);

        if (DbManager.WarehouseQueries.updateStone(warehouse, resourceValue - 1500) == true) {
            warehouse.getResources().put(Warehouse.stoneIB, resourceValue - 1500);
            warehouse.AddTransactionToWarehouse(Enum.GameObjectType.Building, building.getObjectUUID(), Enum.TransactionType.WITHDRAWL, Resource.STONE, 1500);
        } else {
            Logger.error("stone update failed for warehouse of UUID:" + warehouse.getObjectUUID());
            return true;
        }

        // Withdraw Lumber

        resourceValue = warehouse.getResources().get(Warehouse.lumberIB);

        if (DbManager.WarehouseQueries.updateLumber(warehouse, resourceValue - 1500) == true) {
            warehouse.getResources().put(Warehouse.lumberIB, resourceValue - 1500);
            warehouse.AddTransactionToWarehouse(Enum.GameObjectType.Building, building.getObjectUUID(), Enum.TransactionType.WITHDRAWL, Resource.LUMBER, 1500);
        } else {
            Logger.error("lumber update failed for warehouse of UUID:" + warehouse.getObjectUUID());
            return true;
        }

        // Withdraw Galvor

        resourceValue = warehouse.getResources().get(Warehouse.galvorIB);

        if (DbManager.WarehouseQueries.updateGalvor(warehouse, resourceValue - 5) == true) {
            warehouse.getResources().put(Warehouse.galvorIB, resourceValue - 5);
            warehouse.AddTransactionToWarehouse(Enum.GameObjectType.Building, building.getObjectUUID(), Enum.TransactionType.WITHDRAWL, Resource.GALVOR, 5);
        } else {
            Logger.error("galvor update failed for warehouse of UUID:" + warehouse.getObjectUUID());
            return true;
        }

        resourceValue = warehouse.getResources().get(Warehouse.wormwoodIB);

        if (DbManager.WarehouseQueries.updateWormwood(warehouse, resourceValue - 5) == true) {
            warehouse.getResources().put(Warehouse.wormwoodIB, resourceValue - 5);
            warehouse.AddTransactionToWarehouse(Enum.GameObjectType.Building, building.getObjectUUID(), Enum.TransactionType.WITHDRAWL, Resource.WORMWOOD, 5);
        } else {
            Logger.error("wyrmwood update failed for warehouse of UUID:" + warehouse.getObjectUUID());
        }

        return true;
    }

    public static void dailyMaintenance() {

        Logger.info("Maintenance has started");

        // Run maintenance on player buildings

        if (ConfigManager.MB_WORLD_MAINTENANCE.getValue().equalsIgnoreCase("true"))
            processBuildingMaintenance();
        else
            Logger.info("Maintenance Costings: DISABLED");

        Logger.info("Maintenance has completed!");
    }
}
