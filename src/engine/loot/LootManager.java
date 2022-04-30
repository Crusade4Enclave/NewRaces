// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.loot;

import engine.gameManager.DbManager;
import engine.objects.Item;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class contains static methods for data from Magicbane's loot tables
 */
public class LootManager {

    private static final HashMap<Integer, TreeMap<Integer, LootGroup>> _lootGroups = new HashMap<>();
    private static final HashMap<Integer, TreeMap<Integer, LootTable>> _lootTables = new HashMap<>();
    private static final HashMap<Integer, TreeMap<Integer, ModifierGroup>> _modGroups = new HashMap<>();
    private static final HashMap<Integer, TreeMap> _modTables = new HashMap<>();

    private LootManager() {

    }

// Method adds a lootGroup to the class's internal collection
// and configures the treemap accordingly
    
    public static void addLootGroup(LootGroup lootGroup) {

    // If entry for this lootGroup does not currently exist
    // we need to create one.
        
        if (_lootGroups.containsKey(lootGroup.getGroupID()) == false)
            _lootGroups.put(lootGroup.getGroupID(), new TreeMap<>());

    // Add this lootgroup to the appropriate treemap
        
        _lootGroups.get(lootGroup.getGroupID()).put(lootGroup.getMaxRoll(), lootGroup);

    }

    public static void addLootTable(engine.loot.LootTable lootTable) {

    // If entry for this lootTabe does not currently exist
    // we need to create one.
        
        if (_lootTables.containsKey(lootTable.getLootTable()) == false)
            _lootTables.put(lootTable.getLootTable(),
                    new TreeMap<>());

    // Add this lootTable to the appropriate treemap
        
        _lootTables.get(lootTable.getLootTable()).put(lootTable.getMaxRoll(), lootTable);

    }

    public static void addModifierGroup(ModifierGroup modGroup) {

    // If entry for this lootTabe does not currently exist
    // we need to create one.
        
        if (_modGroups.containsKey(modGroup.getModGroup()) == false)
            _modGroups.put(modGroup.getModGroup(),
                    new TreeMap<>());

    // Add this lootTable to the appropriate treemap
        
        _modGroups.get(modGroup.getModGroup()).put(modGroup.getMaxRoll(), modGroup);

    }

    public static void addModifierTable(ModifierTable modTable) {

    // If entry for this lootTabe does not currently exist
    // we need to create one.
        
        if (_modTables.containsKey(modTable.getModTable()) == false)
            _modTables.put(modTable.getModTable(),
                    new TreeMap<Float, ModifierGroup>());

    // Add this lootTable to the appropriate treemap
        
        _modTables.get(modTable.getModTable()).put(modTable.getMaxRoll(), modTable);

    }

    /* Mainline interfaces for this class.  Methods below retrieve
     * entries from the loottable by random number and range.
    */
    
    public static LootGroup getRandomLootGroup(int lootGroupID, int randomRoll) {

        if ((randomRoll < 1) || (randomRoll > 100))
            return null;

    // Get random lootGroup for this roll
        
        return _lootGroups.get(lootGroupID).floorEntry(randomRoll).getValue();

    }

    public static engine.loot.LootTable getRandomLootTable(int lootTableID, int randomRoll) {

        if ((randomRoll < 1) || (randomRoll > 100))
            return null;

    // Get random lootTable for this roll
        
        return _lootTables.get(lootTableID).floorEntry(randomRoll).getValue();

    }

    public static ModifierGroup getRandomModifierGroup(int modGroupID, int randomRoll) {

        if ((randomRoll < 1) || (randomRoll > 100))
            return null;

    // Get random modGroup for this roll
        
        return _modGroups.get(modGroupID).floorEntry(randomRoll).getValue();

    }

    public static ModifierTable getRandomModifierTable(int modTableID, float randomRoll) {

        if ((randomRoll < 1.0f))
            return null;

        // Roll is outside of range
        
        if (randomRoll > getMaxRangeForModifierTable(modTableID))
            return null;

    // Get random lootGroup for this roll
        
        return (ModifierTable) _modTables.get(modTableID).floorEntry(randomRoll).getValue();

    }
    
    // Returns minmum rolling range for a particular modifier table entry
    
    public static float getMinRangeForModifierTable(int modTableID) {

        ModifierTable outTable;

        outTable = (ModifierTable) _modTables.get(modTableID).firstEntry();

        return outTable.getMinRoll();

    }

    // Returns maximum rolling range for a particular modifier table entry
    
    public static float getMaxRangeForModifierTable(int modTableID) {

        ModifierTable outTable;

        outTable = (ModifierTable) _modTables.get(modTableID).lastEntry();

        return outTable.getMaxRoll();
    }

    public static Item getRandomItemFromLootGroup(int lootGroupID, int randomRoll) {
    
        Item outItem = null;
        LootGroup lootGroup;
        LootTable lootTable;
        ModifierGroup modGroup;
        ModifierTable prefixTable;
        ModifierTable suffixTable;
        
        // Retrieve a random loot group
        
        lootGroup = getRandomLootGroup(lootGroupID, randomRoll);
        
        if (lootGroup == null)
            return null;
        
        // Retrieve a random loot table
        
        lootTable = getRandomLootTable(lootGroup.getLootTableID(), ThreadLocalRandom.current().nextInt(100));
        
        if (lootTable == null)
            return null;
        
        // Retrieve a random prefix
        
        modGroup = getRandomModifierGroup(lootGroup.getpModTableID(), ThreadLocalRandom.current().nextInt(100));
        
        if (modGroup == null)
            return null;
        
        prefixTable = getRandomModifierTable(modGroup.getSubTableID(), ThreadLocalRandom.current().nextFloat() * getMaxRangeForModifierTable(lootGroup.getpModTableID()));
        
        if (prefixTable == null)
            return null;
        
        // Retrieve a random suffix
        
        modGroup = getRandomModifierGroup(lootGroup.getsModTableID(), ThreadLocalRandom.current().nextInt(100));
        
        if (modGroup == null)
            return null;
        
        suffixTable = getRandomModifierTable(modGroup.getSubTableID(), ThreadLocalRandom.current().nextFloat() * getMaxRangeForModifierTable(lootGroup.getsModTableID()));
        
        if (suffixTable == null)
            return null;
        
        // Create the item!
        
        return outItem;
    }
    
    // Bootstrap routine to load loot data from database
    
    public static void loadLootData() {
        DbManager.LootQueries.LOAD_ALL_LOOTGROUPS();
        DbManager.LootQueries.LOAD_ALL_LOOTTABLES();
        DbManager.LootQueries.LOAD_ALL_MODGROUPS();
        DbManager.LootQueries.LOAD_ALL_MODTABLES();
    }

}
