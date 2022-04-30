// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.objects;

import engine.Enum;
import engine.Enum.ProtectionState;
import engine.Enum.SiegePhase;
import engine.Enum.SiegeResult;
import engine.InterestManagement.HeightMap;
import engine.InterestManagement.WorldGrid;
import engine.db.archive.BaneRecord;
import engine.db.archive.DataWarehouse;
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.job.JobScheduler;
import engine.jobs.ActivateBaneJob;
import engine.jobs.BaneDefaultTimeJob;
import engine.math.Vector3fImmutable;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.PlaceAssetMsg;
import engine.net.client.msg.chat.ChatSystemMsg;
import engine.server.MBServerStatics;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public final class Bane {

    private final int cityUUID;
    private int ownerUUID;
    private final int stoneUUID;
    private DateTime placementDate = null;
    private DateTime liveDate = null;
    private BaneDefaultTimeJob defaultTimeJob;
    private ActivateBaneJob activateBaneJob;

    // Internal cache for banes
    
    public static ConcurrentHashMap<Integer, Bane> banes = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

    /**
     * ResultSet Constructor
     */
    public Bane(ResultSet rs) throws SQLException {

        Date sqlDateTime;
        ActivateBaneJob abtj;

        this.cityUUID = rs.getInt("cityUUID");
        this.ownerUUID = rs.getInt("ownerUUID");
        this.stoneUUID = rs.getInt("stoneUUID");

        sqlDateTime = rs.getTimestamp("placementDate");

        if (sqlDateTime != null)
            this.placementDate = new DateTime(sqlDateTime);

        sqlDateTime = rs.getTimestamp("liveDate");

        if (sqlDateTime != null)
            this.liveDate = new DateTime(sqlDateTime);

        switch (this.getSiegePhase()) {

            case CHALLENGE:
                break;
            case WAR:

                // cancel old job if exists

                if (this.activateBaneJob != null)
                    this.activateBaneJob.cancelJob();

                abtj = new ActivateBaneJob(cityUUID);
                JobScheduler.getInstance().scheduleJob(abtj, this.liveDate.getMillis());
                this.activateBaneJob = abtj;

                break;
            case STANDOFF:

                // cancel old job if exists

                if (this.activateBaneJob != null)
                    this.activateBaneJob.cancelJob();

                abtj = new ActivateBaneJob(cityUUID);
                JobScheduler.getInstance().scheduleJob(abtj, this.liveDate.getMillis());
                this.activateBaneJob = abtj;

                break;
        }

        if (this.liveDate == null)
            setDefaultTime();
    }

    public static boolean summonBanestone(PlayerCharacter player, ClientConnection origin, int rank) {

        Guild baningGuild;
        Zone cityZone;
        City targetCity;

        ArrayList<Bane> nationBanes;

        baningGuild = player.getGuild();

        if (baningGuild.getNation().isErrant()) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 55, ""); // You must be in a Nation
            return false;
        }

        if (baningGuild.getNation().isNPCGuild()) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 72, ""); // Cannot be in an NPC nation
            return false;
        }

        if (GuildStatusController.isInnerCouncil(player.getGuildStatus()) == false) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 10, ""); // You must be a guild leader
            return false;
        }

        // Cannot place banestones underwater;

        if (HeightMap.isLocUnderwater(player.getLoc())) {
            PlaceAssetMsg.sendPlaceAssetError(origin, 6, ""); // Cannot place underwater
            return false;
        }

        //  figure out which city we're standing on
        //  must be within a city's seige Bounds

        targetCity = ZoneManager.getCityAtLocation(player.getLoc());

        if (targetCity == null) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 59, ""); // No city to siege at this location!
            return false;
        }

        if (targetCity.isSafeHold()) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 15, ""); // Cannot place assets in peace zone
            return false;
        }

        if (targetCity.getRank() > rank) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 60, ""); // Bane rank is too low
            return false;
        }

        cityZone = targetCity.getParent();

        // Cannot place assets on a dead tree

        if ((cityZone.isPlayerCity()) &&
                (City.getCity(cityZone.getPlayerCityUUID()).getTOL().getRank() == -1)) {
            PlaceAssetMsg.sendPlaceAssetError(origin, 1, "Cannot bane a dead tree!");
            return false;
        }

        if (baningGuild.getNation() == targetCity.getGuild().getNation()) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 20, ""); //Cannot bane yourself!
            return false;
        }

        if (targetCity.getTOL() == null) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 65, ""); // Cannot find tree to target
            return false;
        }

        if (targetCity.getBane() != null) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 23, ""); // Tree is already baned.
            return false;
        }

        if (getBaneByAttackerGuild(baningGuild) != null) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 1, "Your guild has already placed a bane!");
            return false;
        }

        nationBanes = getBanesByNation(baningGuild.getNation());

        // A nation can only have 3 concurrent banes

        if (nationBanes.size() == 3) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 64, ""); // Your nation is already at war and your limit has been reached
            return false;
        }

        if (targetCity.isLocationOnCityGrid(player.getLoc()) == true) {
            PlaceAssetMsg.sendPlaceAssetError( origin, 1, "Cannot place banestone on city grid.");
            return false;
        }

        Blueprint blueprint = Blueprint.getBlueprint(24300);  // Banestone

        //Let's drop a banestone!

        Vector3fImmutable localLocation = ZoneManager.worldToLocal(player.getLoc(), cityZone);

        if (localLocation == null) {
            PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
            Logger.info("Failed to Convert World coordinates to local zone coordinates");
            return false;
        }

        Building stone = DbManager.BuildingQueries.CREATE_BUILDING(
                cityZone.getObjectUUID(), player.getObjectUUID(), blueprint.getName(), blueprint.getBlueprintUUID(),
                localLocation, 1.0f, blueprint.getMaxHealth(rank), ProtectionState.PROTECTED, 0, rank,
                null, blueprint.getBlueprintUUID(), 1, 0.0f);

        if (stone == null) {
            PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
            return false;
        }

        stone.addEffectBit((1 << 19));
        stone.setMaxHitPoints(stone.getBlueprint().getMaxHealth(stone.getRank()));
        stone.setCurrentHitPoints(stone.getMaxHitPoints());
        BuildingManager.setUpgradeDateTime(stone, null, 0);

        //Make the bane

        Bane bane = makeBane(player, targetCity, stone);

        if (bane == null) {
            //delete bane stone, failed to make bane object
            DbManager.BuildingQueries.DELETE_FROM_DATABASE(stone);
            PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
            return false;
        }

        WorldGrid.addObject(stone, player);

        //Add bane effect to TOL

        targetCity.getTOL().addEffectBit((1 << 16));
        targetCity.getTOL().updateEffects();
        
        Vector3fImmutable movePlayerOutsideStone = player.getLoc();
        movePlayerOutsideStone = movePlayerOutsideStone.setX(movePlayerOutsideStone.x + 10);
        movePlayerOutsideStone = movePlayerOutsideStone.setZ(movePlayerOutsideStone.z + 10);
        player.teleport(movePlayerOutsideStone);

        // Notify players

        ChatSystemMsg chatMsg = new ChatSystemMsg(null, "[Bane Channel] " + targetCity.getCityName() + " has been baned by " + baningGuild.getName() + ". Standoff phase has begun!");
        chatMsg.setMessageType(4);
        chatMsg.setChannel(Enum.ChatChannelType.SYSTEM.getChannelID());

        DispatchMessage.dispatchMsgToAll(chatMsg);

        // Push this event to the DataWarehouse

        BaneRecord baneRecord = BaneRecord.borrow(bane, Enum.RecordEventType.PENDING);
        DataWarehouse.pushToWarehouse(baneRecord);

        return true;
    }

    public SiegePhase getSiegePhase() {

        SiegePhase phase;

        if (this.liveDate == null) {
            phase = SiegePhase.CHALLENGE; //challenge
            return phase;
        }

        if (DateTime.now().isAfter(this.liveDate)) {
            phase = SiegePhase.WAR; //war 
            return phase;
        }

        // If code reaches this point we are in standoff mode.
        phase = SiegePhase.STANDOFF; //standoff   

        return phase;
    }

    private void setDefaultTime() {

        DateTime timeToSetDefault = new DateTime(this.placementDate);
        timeToSetDefault = timeToSetDefault.plusDays(1);

        DateTime currentTime = DateTime.now();
        DateTime defaultTime = new DateTime(this.placementDate);
        defaultTime = defaultTime.plusDays(2);
        defaultTime = defaultTime.hourOfDay().setCopy(22);
        defaultTime = defaultTime.minuteOfHour().setCopy(0);
        defaultTime = defaultTime.secondOfMinute().setCopy(0);

        if (currentTime.isAfter(timeToSetDefault))
            this.setLiveDate(defaultTime);
        else {

            if (this.defaultTimeJob != null)
                this.defaultTimeJob.cancelJob();

            BaneDefaultTimeJob bdtj = new BaneDefaultTimeJob(this);
            JobScheduler.getInstance().scheduleJob(bdtj, timeToSetDefault.getMillis());
            this.defaultTimeJob = bdtj;
        }
    }

    public void setLiveDate(DateTime baneTime) {

        if (DbManager.BaneQueries.SET_BANE_TIME(baneTime, this.getCity().getObjectUUID())) {
            this.liveDate = new DateTime(baneTime);

            // Push event to warehouse

            BaneRecord.updateLiveDate(this, baneTime);

            ChatManager.chatGuildInfo(this.getOwner().getGuild(), "The bane on " + this.getCity().getGuild().getName() + " has been set! Standoff phase has begun!");
            Guild attackerNation = this.getOwner().getGuild().getNation();

            if (attackerNation != null)

                for (Guild subGuild : attackerNation.getSubGuildList()) {

                    //Don't send the message twice.
                    if (subGuild.equals(attackerNation))
                        continue;

                    ChatManager.chatGuildInfo(subGuild, "The bane on " + this.getCity().getGuild().getName() + " has been set! Standoff phase has begun!");
                }

            ChatManager.chatGuildInfo(this.getCity().getGuild(), "The bane on " + this.getCity().getGuild().getName() + " has been set! Standoff phase has begun!");

            Guild defenderNation = this.getCity().getGuild().getNation();

            if (defenderNation != null) {

                if (defenderNation != this.getCity().getGuild())
                    ChatManager.chatGuildInfo(defenderNation, "The bane on " + this.getCity().getGuild().getName() + " has been set! Standoff phase has begun!");

                for (Guild subGuild : defenderNation.getSubGuildList()) {
                    //Don't send the message twice.
                    if (subGuild == this.getCity().getGuild())
                        continue;
                    ChatManager.chatGuildInfo(subGuild, "The bane on " + this.getCity().getGuild().getName() + " has been set! Standoff phase has begun!");
                }
            }

            if (activateBaneJob != null)
                this.activateBaneJob.cancelJob();

            ActivateBaneJob abtj = new ActivateBaneJob(cityUUID);

            JobScheduler.getInstance().scheduleJob(abtj, this.liveDate.getMillis());
            this.activateBaneJob = abtj;
        } else {
            Logger.debug( "error with city " + this.getCity().getName());
            ChatManager.chatGuildInfo(this.getOwner().getGuild(), "A Serious error has occurred. Please post details for to ensure transaction integrity");
            ChatManager.chatGuildInfo(this.getCity().getGuild(), "A Serious error has occurred. Please post details for to ensure transaction integrity");
        }

    }

    /*
     * Getters
     */
    public City getCity() {
        return City.getCity(this.cityUUID);
    }

    public PlayerCharacter getOwner() {
        return (PlayerCharacter) DbManager.getObject(Enum.GameObjectType.PlayerCharacter, ownerUUID);
    }

    //Call this to prematurely end a bane
    
    public boolean remove() {

        Building baneStone;

        baneStone = this.getStone();

        if (baneStone == null) {
            Logger.debug("Removing bane without a stone.");
            return false;
        }

        // Reassert protection contracts
        
        this.getCity().protectionEnforced = true;

        // Remove visual effects on Bane and TOL.
        
        this.getStone().removeAllVisualEffects();
        this.getStone().updateEffects();

        this.getCity().getTOL().removeAllVisualEffects();
        this.getCity().getTOL().updateEffects();
        
        // Remove bane from the database
        
        if (DbManager.BaneQueries.REMOVE_BANE(this) == false) {
            Logger.error("Database call failed for city UUID: " + this.getCity().getObjectUUID());
            return false;
        }

        // Remove bane from ingame cache

        Bane.banes.remove(cityUUID);
        
        // Delete stone from database
        
        if (DbManager.BuildingQueries.DELETE_FROM_DATABASE(baneStone) == false) {
            Logger.error( "Database error when deleting stone object");
            return false;
        }

        // Remove object from simulation
        
        baneStone.removeFromCache();
        WorldGrid.RemoveWorldObject(baneStone);
        WorldGrid.removeObject(baneStone);
        return true;
    }

    // Cache access
    
    public static Bane getBane(int cityUUID) {

        Bane outBane;

        // Check cache first

        outBane = banes.get(cityUUID);

        // Last resort attempt to load from database

        if (outBane == null)
            outBane = DbManager.BaneQueries.LOAD_BANE(cityUUID);
        else
            return outBane;

        // As we loaded from the db, store it in the internal cache

        if (outBane != null)
            banes.put(cityUUID, outBane);

        return outBane;
    }

    //Returns a guild's bane
    
    public static Bane getBaneByAttackerGuild(Guild guild) {
            
    	
    	if (guild == null || guild.isErrant())
    		return null;
        ArrayList<Bane> baneList;
        
        baneList = new ArrayList<>(banes.values());
        
            for (Bane bane : baneList) {
                if (bane.getOwner().getGuild().equals(guild))
                    return bane;
            }
            
        return null;
    }

    public static ArrayList<Bane> getBanesByNation(Guild guild) {

        ArrayList<Bane> baneList;
        ArrayList<Bane> returnList;
        
        baneList = new ArrayList<>(banes.values());
        returnList = new ArrayList<>();
        
            for (Bane bane : baneList) {
                if (bane.getOwner().getGuild().getNation().equals(guild))
                    returnList.add(bane);
            }
            
        return returnList;
        }

    public static void addBane(Bane bane) {

        Bane.banes.put(bane.cityUUID, bane);

    }

    public static Bane makeBane(PlayerCharacter owner, City city, Building stone) {

        Bane newBane;

        if (DbManager.BaneQueries.CREATE_BANE(city, owner, stone) == false) {
            Logger.error("Error writing to database");
            return null;
        }

        newBane = DbManager.BaneQueries.LOAD_BANE(city.getObjectUUID());

        return newBane;
    }

    public final DateTime getPlacementDate() {
        return placementDate;
    }

    public final boolean isAccepted() {

        return (this.getSiegePhase() != SiegePhase.CHALLENGE);
    }

    public final DateTime getLiveDate() {
        return liveDate;
    }

    public final void endBane(SiegeResult siegeResult) {

        boolean baneRemoved;

        // No matter what the outcome of a bane, we re-asset
        // protection contracts at this time.  They don't quite
        // matter if the city falls, as they are invalidated.
        
        this.getCity().protectionEnforced = true;
        
        switch (siegeResult) {
            case DEFEND:

                // Push event to warehouse

                BaneRecord.updateResolution(this, Enum.RecordEventType.DEFEND);

                baneRemoved = remove();

                if (baneRemoved) {

                    // Update seieges withstood
                
                    this.getCity().setSiegesWithstood(this.getCity().getSiegesWithstood() + 1);
                    
                    // Notify players
                    
                ChatSystemMsg msg = new ChatSystemMsg(null, "[Bane Channel]" + this.getCity().getGuild().getName() + " has rallied against " + this.getOwner().getGuild().getName() + ". The siege on " + this.getCity().getCityName() + " has been broken!");
                msg.setMessageType(4);
                msg.setChannel(engine.Enum.ChatChannelType.SYSTEM.getChannelID());
        
                DispatchMessage.dispatchMsgToAll(msg);
                
                }
                
                break;
            case CAPTURE:

                // Push event to warehouse

                BaneRecord.updateResolution(this, Enum.RecordEventType.CAPTURE);

                baneRemoved = this.remove();

                if (baneRemoved) {

                ChatSystemMsg msg = new ChatSystemMsg(null, "[Bane Channel]" + this.getOwner().getGuild().getName() + " have defeated " + this.getCity().getGuild().getName() + " and captured " + this.getCity().getCityName() + '!');
                msg.setMessageType(4);
                msg.setChannel(engine.Enum.ChatChannelType.SYSTEM.getChannelID());
        
                DispatchMessage.dispatchMsgToAll(msg);
                }
                break;
            case DESTROY:

                // Push event to warehouse

                BaneRecord.updateResolution(this, Enum.RecordEventType.DESTROY);

                baneRemoved = this.remove();

                if (baneRemoved) {

                ChatSystemMsg msg = new ChatSystemMsg(null, "[Bane Channel]" + this.getOwner().getGuild().getName() + " have defeated " + this.getCity().getGuild().getName() + " and razed " + this.getCity().getCityName() + '!');
                msg.setMessageType(4);
                msg.setChannel(engine.Enum.ChatChannelType.SYSTEM.getChannelID());
                
                DispatchMessage.dispatchMsgToAll(msg);
                }
                break;
        }

        Zone cityZone = this.getCity().getParent();

        if (cityZone == null)
        	return;

        //UNPROTECT ALL SIEGE EQUIPMENT AFTER A BANE
        for (Building toUnprotect: cityZone.zoneBuildingSet){
        	if (toUnprotect.getBlueprint() != null && toUnprotect.getBlueprint().isSiegeEquip() && toUnprotect.assetIsProtected() == true)
        	toUnprotect.setProtectionState(ProtectionState.NONE);
        }

    }

    public boolean isErrant() {

        boolean isErrant = true;

        if (this.getOwner() == null)
            return isErrant;

       
        if (this.getOwner().getGuild().isErrant() == true)
            return isErrant;

        if (this.getOwner().getGuild().getNation().isErrant() == true)
            return isErrant;

        // Bane passes validation

        isErrant = false;

        return isErrant;
    }

    /**
     * @return the stone
     */
    public Building getStone() {
        return BuildingManager.getBuilding(this.stoneUUID);
    }

    /**
     * @return the cityUUID
     */
    public int getCityUUID() {
        return cityUUID;
    }

}
