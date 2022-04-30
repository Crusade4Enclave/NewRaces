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
 * Holds row in the modGroup database table
 */
public class ModifierGroup {

    private final int modGroup;
    private final String groupName;
    private final int minRoll;
    private final int maxRoll;
    private final int subTableID;
    private final String subTableName;

    public ModifierGroup(ResultSet rs) throws SQLException {
        
        this.modGroup = rs.getInt("modGroup");
        this.groupName = rs.getString("groupName");
        this.minRoll = rs.getInt("minRoll");
        this.maxRoll = rs.getInt("maxRoll");
        this.subTableID = rs.getInt("subTableID");
        this.subTableName = rs.getString("subTableName");                
    }

    /**
     * @return the modGroup
     */
    public int getModGroup() {
        return modGroup;
    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
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
     * @return the subTableID
     */
    public int getSubTableID() {
        return subTableID;
    }

    /**
     * @return the subTableName
     */
    public String getSubTableName() {
        return subTableName;
    }
}