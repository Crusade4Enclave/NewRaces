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
 * Holds row in the lootGroup database table
 */
public class LootGroup {

    private final int groupID;
    private final String groupName;
    private final int minRoll;
    private final int maxRoll;
    private final int lootTableID;
    private final String lootTableName;
    private final int pMod;
    private final int pModTableID;
    private final int sMod;
    private final int sModTableID;

    public LootGroup(ResultSet rs) throws SQLException {
        
        this.groupID = rs.getInt("groupID");
        this.groupName = rs.getString("groupName");
        this.minRoll = rs.getInt("minRoll");
        this.maxRoll = rs.getInt("maxRoll");
        this.lootTableID = rs.getInt("lootTableID");
        this.lootTableName = rs.getString("lootTableName");
        this.pMod = rs.getInt("pMod");
        this.pModTableID = rs.getInt("pModTableID");
        this.sMod = rs.getInt("sMod");
        this.sModTableID = rs.getInt("sModTableID");
    }
    /**
     * @return the groupID
     */
    public int getGroupID() {
        return groupID;
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
     * @return the lootTableID
     */
    public int getLootTableID() {
        return lootTableID;
    }

    /**
     * @return the lootTableName
     */
    public String getLootTableName() {
        return lootTableName;
    }

    /**
     * @return the pMod
     */
    public int getpMod() {
        return pMod;
    }

    /**
     * @return the pModTableID
     */
    public int getpModTableID() {
        return pModTableID;
    }

    /**
     * @return the sMod
     */
    public int getsMod() {
        return sMod;
    }

    /**
     * @return the sModTableID
     */
    public int getsModTableID() {
        return sModTableID;
    }

}
