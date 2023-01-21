// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum;
import engine.InterestManagement.WorldGrid;
import engine.db.archive.DataWarehouse;
import engine.db.archive.MineRecord;
import engine.gameManager.*;
import engine.net.ByteBufferWriter;
import engine.net.DispatchMessage;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.chat.ChatSystemMsg;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static engine.gameManager.DbManager.*;
import static engine.math.FastMath.sqr;

public class Mine extends AbstractGameObject {

    private String zoneName;
    private Resource production;
    public boolean isActive = false;

    private float latitude;
    private float longitude;
    private float altitude;
    private Guild owningGuild;
    public PlayerCharacter lastClaimer;
    public boolean wasClaimed = false;
    private int flags;
    private int buildingID;
    private Zone parentZone;
    private MineProduction mineType;

    //flags 1: never been claimed (make active).


    // Not persisted to DB
    public String guildName;
    public GuildTag guildTag;
    public String nationName;
    public GuildTag nationTag;
    public static ConcurrentHashMap<Mine, Integer> mineMap = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
    public static ConcurrentHashMap<Integer, Mine> towerMap = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

    private static long lastChange = System.currentTimeMillis();

    /**
     * ResultSet Constructor
     */
    public Mine(ResultSet rs) throws SQLException, UnknownHostException {
        super(rs);

        this.mineType = MineProduction.getByName(rs.getString("mine_type"));

        float offsetX = rs.getFloat("mine_offsetX");
        float offsetZ = rs.getFloat("mine_offsetZ");
        int ownerUID = rs.getInt("mine_ownerUID");
        this.buildingID = rs.getInt("mine_buildingUID");
        this.flags = rs.getInt("flags");
        int parent = rs.getInt("parent");
        this.parentZone = ZoneManager.getZoneByUUID(parent);
        if (parentZone != null) {
            this.latitude = parentZone.getLoc().x + offsetX;
            this.longitude = parentZone.getLoc().z + offsetZ;
            this.altitude = parentZone.getLoc().y;
            if (this.parentZone.getParent() != null)
                this.zoneName = this.parentZone.getParent().getName();
            else
                this.zoneName = this.parentZone.getName();
        } else {
            Logger.error("Missing parentZone of ID " + parent);
            this.latitude = -1000;
            this.longitude = 1000;
            this.altitude = 0;
            this.zoneName = "Unknown Mine";
        }

        this.owningGuild = Guild.getGuild(ownerUID);
        Guild nation = null;

        if (this.owningGuild.isEmptyGuild()) {
            this.guildName = "";
            this.guildTag = GuildTag.ERRANT;
            nation = Guild.getErrantGuild();
            this.owningGuild = Guild.getErrantGuild();
        } else {
            this.guildName = this.owningGuild.getName();
            this.guildTag = this.owningGuild.getGuildTag();
            nation = this.owningGuild.getNation();
        }

        if (!nation.isEmptyGuild()) {
            this.nationName = nation.getName();
            this.nationTag = nation.getGuildTag();
        } else {
            this.nationName = "";
            this.nationTag = GuildTag.ERRANT;
        }

        this.production = Resource.valueOf(rs.getString("mine_resource"));
        this.lastClaimer = null;

    }

    public static void releaseMineClaims(PlayerCharacter playerCharacter) {

        if (playerCharacter == null)
            return;

        for (Mine mine : Mine.getMines()) {

            if (mine.lastClaimer != null)
                if (mine.lastClaimer.equals(playerCharacter)) {
                    mine.lastClaimer = null;
                    mine.updateGuildOwner(null);
                }

        }
    }
    public static void SendMineAttackMessage(Building mine) {

        if (mine.getBlueprint() == null)
            return;

        if (mine.getBlueprint().getBuildingGroup() != Enum.BuildingGroup.MINE)
            return;


        if (mine.getGuild().isEmptyGuild())
            return;

        if (mine.getGuild().getNation().isEmptyGuild())
            return;

        if (mine.getTimeStamp("MineAttack") > System.currentTimeMillis())
            return;

        mine.getTimestamps().put("MineAttack", System.currentTimeMillis() + MBServerStatics.ONE_MINUTE);

        ChatManager.chatNationInfo(mine.getGuild().getNation(), mine.getName() + " in " + mine.getParentZone().getParent().getName() + " is Under attack!");
    }

    public static void loadAllMines() {

        try {

            //Load mine resources
            MineProduction.addResources();

            //pre-load all building sets
            ArrayList<Mine> serverMines = MineQueries.GET_ALL_MINES_FOR_SERVER();

            for (Mine mine : serverMines) {
                Mine.mineMap.put(mine, mine.buildingID);
                Mine.towerMap.put(mine.buildingID, mine);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Getters
     */

    public boolean changeProductionType(Resource resource) {
        if (!this.validForMine(resource))
            return false;
        //update resource in database;
        if (!MineQueries.CHANGE_RESOURCE(this, resource))
            return false;

        this.production = resource;
        return true;
    }

    public MineProduction getMineType() {
        return this.mineType;
    }

    public String getZoneName() {
        return this.zoneName;
    }

    public Resource getProduction() {
        return this.production;
    }

    public boolean getIsActive() {
        return this.isActive;
    }

    public float getAltitude() {
        return this.altitude;
    }

    public Guild getOwningGuild() {
        if (this.owningGuild == null)
            return Guild.getErrantGuild();
        else
            return this.owningGuild;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Zone getParentZone() {
        return parentZone;
    }

    public GuildTag getGuildTag() {
        return guildTag;
    }

    public void setMineType(String type) {
        this.mineType = MineProduction.getByName(type);
    }

    public void setActive(boolean isAc) {

        this.isActive = isAc;
        Building building = BuildingManager.getBuildingFromCache(this.buildingID);
        if (building != null && !this.isActive)
            building.isDeranking.compareAndSet(true, false);
    }

    public void setOwningGuild(Guild owningGuild) {
        this.owningGuild = owningGuild;
    }

    public static Mine getMineFromTower(int towerID) {
        return Mine.towerMap.get(towerID);
    }

    public boolean validForMine(Resource r) {
        if (this.mineType == null)
            return false;
        return this.mineType.validForMine(r, this.isExpansion());
    }

    /*
     * Serialization
     */

    public static void serializeForClientMsg(Mine mine, ByteBufferWriter writer) {
        writer.putInt(mine.getObjectType().ordinal());
        writer.putInt(mine.getObjectUUID());
        writer.putInt(mine.getObjectUUID()); //actually a hash of mine
        writer.putString(mine.mineType.name);
        writer.putString(mine.zoneName);
        writer.putInt(mine.production.hash);
        writer.putInt(mine.production.baseProduction);
        writer.putInt(mine.getModifiedProductionAmount()); //TODO calculate range penalty here
        writer.putInt(3600); //window in seconds

        // Errant mines are currently open.  Set time to now.

        LocalDateTime mineOpenTime = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        
        // Mine times are those of the nation not individual guild.
        
        Guild mineNatonGuild = mine.getOwningGuild().getNation();

        // Adjust the serialized mine time based upon whether
        // the Guild's mine window has passed or not and if it was claimed.
		// If a mine is active serialize current datetime irrespective
		// of any claim.

        if (mineNatonGuild.isEmptyGuild() == false && mine.isActive == false) {

            int guildWOO = mineNatonGuild.getNation().getMineTime();
            LocalDateTime guildMineTime = mineOpenTime.withHour(guildWOO);

            if (mineOpenTime.isAfter(guildMineTime) || mine.wasClaimed == true)
                mineOpenTime = guildMineTime.plusDays(1);
            else
                mineOpenTime = guildMineTime;

        }

        writer.putLocalDateTime(mineOpenTime);
        writer.putLocalDateTime(mineOpenTime.plusHours(1));
        writer.put(mine.isActive ? (byte) 0x01 : (byte) 0x00);

        writer.putFloat(mine.latitude);
        writer.putFloat(mine.altitude);
        writer.putFloat(mine.longitude);
        writer.putInt(mine.isExpansion() ? mine.mineType.xpacHash : mine.mineType.hash);

        writer.putString(mine.guildName);
        GuildTag._serializeForDisplay(mine.guildTag, writer);
        writer.putString(mine.nationName);
        GuildTag._serializeForDisplay(mine.nationTag, writer);
    }

    public void serializeForMineProduction(ByteBufferWriter writer) {
        writer.putInt(this.getObjectType().ordinal());
        writer.putInt(this.getObjectUUID());
        writer.putInt(this.getObjectUUID()); //actually a hash of mine
        //		writer.putInt(0x215C92BB); //this.unknown1);
        writer.putString(this.mineType.name);
        writer.putString(this.zoneName);
        writer.putInt(this.production.hash);
        writer.putInt(this.production.baseProduction);
        writer.putInt(this.getModifiedProductionAmount()); //TODO calculate range penalty here
        writer.putInt(3600); //window in seconds
        writer.putInt(this.isExpansion() ? this.mineType.xpacHash : this.mineType.hash);
    }

    public static ArrayList<Mine> getMinesForGuild(int guildID) {
        ArrayList<Mine> mineList = new ArrayList<>();
        for (Mine mine : Mine.mineMap.keySet()) {
            if (mine.owningGuild.getObjectUUID() == guildID)
                mineList.add(mine);
        }
        return mineList;
    }

    public static long getLastChange() {
        return lastChange;
    }

    public static void setLastChange(long lastChange) {
        Mine.lastChange = lastChange;
    }

    /*
     * Database
     */
    public static Mine getMine(int UID) {
        return MineQueries.GET_MINE(UID);

    }

    public static ArrayList<Mine> getMines() {
        return new ArrayList<>(mineMap.keySet());
    }

    @Override
    public void updateDatabase() {
        // TODO Create update logic.
    }

    public int getBuildingID() {
        return buildingID;
    }

    public void setBuildingID(int buildingID) {
        this.buildingID = buildingID;
    }

    public static boolean validClaimer(PlayerCharacter playerCharacter) {

        Guild playerGuild;

        //verify the player exists

        if (playerCharacter == null)
            return false;

        //verify the player is in valid guild

        playerGuild = playerCharacter.getGuild();

        // Can't claim something if you don't have a guild!

        if (playerGuild.isEmptyGuild())
            return false;

        if (playerGuild.getNation().isEmptyGuild())
            return false;

       // Guild must own a city to hold a mine.

        City guildCity = playerGuild.getOwnedCity();

        if (guildCity == null)
            return false;

        if (guildCity.getWarehouse() == null) {
            ErrorPopupMsg.sendErrorMsg(playerCharacter, "No Warehouse exists for this claim.");
            return false;
        }

        // Number of mines is based on the rank of the nation's tree.

        City nationCapitol = playerGuild.getNation().getOwnedCity();

        Building nationCapitolTOL = nationCapitol.getTOL();

        if (nationCapitolTOL == null)
            return false;

        int treeRank = nationCapitolTOL.getRank();

        if (treeRank < 1)
            return false;

        if (guildUnderMineLimit(playerGuild.getNation(), treeRank) == false){
            ErrorPopupMsg.sendErrorMsg(playerCharacter, "Your nation cannot support another mine.");
            return false;
        }

        return true;
    }

    private static boolean guildUnderMineLimit(Guild playerGuild, int tolRank) {

        int mineCnt = 0;

        mineCnt += Mine.getMinesForGuild(playerGuild.getObjectUUID()).size();

        for (Guild guild : playerGuild.getSubGuildList())
            mineCnt += Mine.getMinesForGuild(guild.getObjectUUID()).size();

        if (mineCnt > tolRank)
            return false;

        return true;
    }
    public void handleDestroyMine() {

        if (!this.isActive)
            return;

        //remove tags from mine

        this.guildName = "";
        this.nationName = "";
        this.owningGuild = Guild.getErrantGuild();
        Mine.setLastChange(System.currentTimeMillis());
        this.lastClaimer = null;
        this.wasClaimed = false;

        // Update database

        DbManager.MineQueries.CHANGE_OWNER(this, 0);

        // Update mesh

        Building mineBuilding = BuildingManager.getBuildingFromCache(this.buildingID);

        if (mineBuilding == null) {
            Logger.debug("Null mine building " + this.getObjectUUID() + ". Unable to Load Building with UID " + this.buildingID);
            return;
        }

        mineBuilding.setOwner(null);
        mineBuilding.refresh(false);

        // remove hirelings

        Building building = (Building) getObject(Enum.GameObjectType.Building, this.buildingID);
        BuildingManager.cleanupHirelings(building);
    }

    public boolean claimMine(PlayerCharacter claimer) {

        if (claimer == null)
            return false;

        if (!validClaimer(claimer))
            return false;

        if (!this.isActive) {
            ErrorPopupMsg.sendErrorMsg(claimer, "Can not for to claim inactive mine.");
            return false;
        }

        if (!updateGuildOwner(claimer))
            return false;

        // Successful claim

        this.lastClaimer = claimer;

        return true;
    }
    public boolean depositMineResources() {

        if (this.owningGuild.isEmptyGuild())
            return false;

        if (this.owningGuild.getOwnedCity() == null)
            return false;

        if (this.owningGuild.getOwnedCity().getWarehouse() == null)
            return false;

        ItemBase resourceIB = ItemBase.getItemBase(this.production.UUID);
        return this.owningGuild.getOwnedCity().getWarehouse().depositFromMine(this, resourceIB, this.getModifiedProductionAmount());
    }

    public boolean updateGuildOwner(PlayerCharacter playerCharacter) {

        Building mineBuilding = BuildingManager.getBuildingFromCache(this.buildingID);

        //should never return null, but let's check just in case.

        if (mineBuilding == null) {
            ChatManager.chatSystemError(playerCharacter, "Unable to find mine tower.");
            Logger.debug("Failed to Update Mine with UID " + this.getObjectUUID() + ". Unable to Load Building with UID " + this.buildingID);
            return false;
        }

        if (playerCharacter == null) {
            this.owningGuild = Guild.getErrantGuild();
            this.guildName = "None";
            this.guildTag = GuildTag.ERRANT;
            this.nationName = "None";
            this.nationTag = GuildTag.ERRANT;
            //Update Building.
            mineBuilding.setOwner(null);
            WorldGrid.updateObject(mineBuilding);
            return true;
        }

        Guild guild = playerCharacter.getGuild();

        if (guild.getOwnedCity() == null)
            return false;

        if (!MineQueries.CHANGE_OWNER(this, guild.getObjectUUID())) {
            Logger.debug("Database failed to Change Ownership of Mine with UID " + this.getObjectUUID());
            ChatManager.chatSystemError(playerCharacter, "Failed to claim Mine.");
            return false;
        }

        //update mine.
        this.owningGuild = guild;

        //Update Building.
        PlayerCharacter guildLeader = (PlayerCharacter) Guild.GetGL(this.owningGuild);

        if (guildLeader != null)
            mineBuilding.setOwner(guildLeader);
        WorldGrid.updateObject(mineBuilding);
        return true;
    }

    public boolean isExpansion() {
        return (this.flags & 2) != 0;
    }

    public int getModifiedProductionAmount() {
        //TODO Calculate Distance modifications.

        //calculate base values.
        int baseProduction = this.production.baseProduction;
        float baseModValue = this.production.baseProduction * .1f;
        float rankModValue = this.production.baseProduction * .0143f;
        float totalModded = 0;

        //get Mine Building.
        Building mineBuilding = BuildingManager.getBuilding(this.buildingID);
        if (mineBuilding == null)
            return this.production.baseProduction;
        for (AbstractCharacter harvester : mineBuilding.getHirelings().keySet()) {
            totalModded += baseModValue;
            totalModded += rankModValue * harvester.getRank();
        }
        //add base production on top;
        totalModded += baseProduction;
        //skip distance check for expansion.
        if (this.isExpansion())
            return (int) totalModded;

        if (this.owningGuild.isEmptyGuild() == false) {
            if (this.owningGuild.getOwnedCity() != null) {
                float distanceSquared = this.owningGuild.getOwnedCity().getLoc().distanceSquared2D(mineBuilding.getLoc());

                if (distanceSquared > sqr(10000 * 3))
                    totalModded *= .25f;
                else if (distanceSquared > sqr(10000 * 2))
                    totalModded *= .50f;
                else if (distanceSquared > sqr(10000))
                    totalModded *= .75f;
            }
        }
        return (int) totalModded;
    }

}
