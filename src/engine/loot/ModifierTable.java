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
 * Holds row in the modTables database table
 */
public class ModifierTable{

    private final int modTable;
    private final String tableName;
    private final float minRoll;
    private final float maxRoll;
    private final String action;
    private final int level;
    private final int value;
    
    public ModifierTable(ResultSet rs) throws SQLException {
        
        this.modTable = rs.getInt("modTable");
        this.tableName = rs.getString("tableName");
        this.minRoll = rs.getInt("minRoll");
        this.maxRoll = rs.getInt("maxRoll");
        this.action = rs.getString("action"); 
        this.level = rs.getInt("level");
        this.value = rs.getInt("value");            
    }

    /**
     * @return the modTable
     */
    public int getModTable() {
        return modTable;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return the minRoll
     */
    public float getMinRoll() {
        return minRoll;
    }

    /**
     * @return the maxRoll
     */
    public float getMaxRoll() {
        return maxRoll;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

}
