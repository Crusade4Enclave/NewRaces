// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.loot;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data storage object for Loot System.
 * Holds row in the lootTable database table
 */
public class LootTable {

    private final int lootTale;
    private final String tableName;
    private final String itemName;
    private final int minRoll;
    private final int maxRoll;
    private final int itemBaseUUID;
    private final int minSpawn;
    private final int maxSpawn;

    public LootTable(ResultSet rs) throws SQLException {
        
        this.lootTale = rs.getInt("lootTable");
        this.tableName = rs.getString("tableName");
        this.itemName = rs.getString("itemName");
        this.minRoll = rs.getInt("minRoll");
        this.maxRoll = rs.getInt("maxRoll");
        this.itemBaseUUID = rs.getInt("itemBaseUUID");
        this.minSpawn = rs.getInt("minSpawn");
        this.maxSpawn = rs.getInt("maxSpawn");
                        
    }

    /**
     * @return the lootTale
     */
    public int getLootTable() {
        return lootTale;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return the itemName
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * @return the minRoll
     */
    public int getMinRoll() {
        return minRoll;
    }

    /**
     * @return the maxRoll
     */
    public int getMaxRoll() {
        return maxRoll;
    }

    /**
     * @return the itemBaseUUID
     */
    public int getItemBaseUUID() {
        return itemBaseUUID;
    }

    /**
     * @return the minSpawn
     */
    public int getMinSpawn() {
        return minSpawn;
    }

    /**
     * @return the maxSpawn
     */
    public int getMaxSpawn() {
        return maxSpawn;
    }
    
}
