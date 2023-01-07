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
import engine.Enum.*;
import engine.db.archive.DataWarehouse;
import engine.db.archive.GuildRecord;
import engine.db.handlers.dbGuildHandler;
import engine.gameManager.*;
import engine.net.ByteBufferWriter;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.msg.AllianceChangeMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.UpdateClientAlliancesMsg;
import engine.net.client.msg.guild.GuildInfoMsg;
import engine.server.MBServerStatics;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Guild extends AbstractWorldObject {

	private final String name;
	private Guild nation;
	private static Guild g;
	private final GuildTag guildTag;
	// TODO add these to database
	private String motto = "";
	private String motd = "";
	private String icmotd = "";
	private String nmotd = "";
	private int guildLeaderUUID;
	private int realmsOwned;
	private final int charter;
	private int cityUUID = 0;
	private final String leadershipType; // Have to see how this is sent to the client
	private final int repledgeMin;
	private final int repledgeMax;
	private final int repledgeKick;
	private final int teleportMin;
	private final int teleportMax;
	private int mineTime;
	private  ArrayList<PlayerCharacter> banishList;
	private  ArrayList<PlayerCharacter> characterKOSList;
	private  ArrayList<Guild> guildKOSList;
	private  ArrayList<Guild> allyList = new ArrayList<>();
	private  ArrayList<Guild> enemyList = new ArrayList<>();
	private ArrayList<Guild> recommendList = new ArrayList<>();
	private ArrayList<Guild> subGuildList;
	private int nationUUID = 0;
	private GuildState guildState = GuildState.Errant;
	private ConcurrentHashMap<Integer,Condemned> guildCondemned = new ConcurrentHashMap<>();
	private String hash;
	private boolean ownerIsNPC;

	public LocalDateTime lastWooEditTime;
	public HashMap<Integer,GuildAlliances> guildAlliances = new HashMap<>();

	/**
	 * No Id Constructor
	 */
	public Guild(String name, Guild nat, int charter,
			String leadershipType, GuildTag gt, String motto) {
		super();
		this.name = name;
		this.nation = nat;
		this.charter = charter;
		this.realmsOwned = 0;
		this.leadershipType = leadershipType;

		this.banishList = new ArrayList<>();
		this.characterKOSList = new ArrayList<>();
		this.guildKOSList = new ArrayList<>();
		this.allyList = new ArrayList<>();
		this.enemyList = new ArrayList<>();
		this.subGuildList = new ArrayList<>();

		this.guildTag = gt;

		//set for player city
		this.repledgeMin = 1;
		this.repledgeMax = 100;
		this.repledgeKick = 100;
		this.teleportMin = 1;
		this.teleportMax = 100;
		this.mineTime = 0;
		this.motto = motto;
	}

	/**
	 * Normal Constructor
	 */
	public Guild( String name, Guild nat, int charter,
			String leadershipType, GuildTag gt, int newUUID) {
		super(newUUID);
		this.name = name;
		this.nation = nat;

		this.charter = charter;
		this.realmsOwned = 0;
		this.leadershipType = leadershipType;

		this.banishList = new ArrayList<>();
		this.characterKOSList = new ArrayList<>();
		this.guildKOSList = new ArrayList<>();
		this.allyList = new ArrayList<>();
		this.enemyList = new ArrayList<>();
		this.subGuildList = new ArrayList<>();
		this.guildTag = gt;

		//set for player city
		this.repledgeMin = 1;
		this.repledgeMax = 100;
		this.repledgeKick = 100;
		this.teleportMin = 1;
		this.teleportMax = 100;
		this.mineTime = 0;
		this.hash = "ERRANT";
	}

	/**
	 * ResultSet Constructor
	 */
	public Guild(ResultSet rs) throws SQLException {
		super(rs);
		DbObjectType objectType;

		this.name = rs.getString("name");
		this.charter = rs.getInt("charter");
		this.leadershipType = rs.getString("leadershipType");

		this.guildTag = new GuildTag(rs.getInt("backgroundColor01"),
				rs.getInt("backgroundColor02"),
				rs.getInt("symbolColor"),
				rs.getInt("symbol"),
				rs.getInt("backgroundDesign"));

		//Declare Nations and Subguilds
		this.nationUUID = rs.getInt("parent");
		this.cityUUID = rs.getInt("ownedCity");
		this.guildLeaderUUID = rs.getInt("leaderUID");
		this.motto = rs.getString("motto");
		this.motd = rs.getString("motd");
		this.icmotd = rs.getString("icMotd");
		this.nmotd = rs.getString("nationMotd");

		this.repledgeMin = rs.getInt("repledgeMin");
		this.repledgeMax = rs.getInt("repledgeMax");
		this.repledgeKick = rs.getInt("repledgeKick");
		this.teleportMin = rs.getInt("teleportMin");
		this.teleportMax = rs.getInt("teleportMax");

		this.mineTime = rs.getInt("mineTime");

		Timestamp lastWooRequest = rs.getTimestamp("lastWooEditTime");

		if (lastWooRequest != null)
			this.lastWooEditTime = lastWooRequest.toLocalDateTime();

		this.hash = rs.getString("hash");
	}

	public void setNation(Guild nation) {
		if (nation == null)
			this.nation = Guild.getErrantGuild();
		else
		this.nation = nation;
	}

	/*
	 * Getters
	 */
	@Override
	public String getName() {
		return name;
	}

	public String getLeadershipType() {
		return leadershipType;
	}

	public Guild getNation() {
		
		if (this.nation == null)
			return Guild.getErrantGuild();
		return this.nation;
	}

	public boolean isNation() {
        return this.nation != null && this.cityUUID != 0 && this.nation == this;
    }

	public City getOwnedCity() {

		return City.getCity(this.cityUUID);
	}

	public void setCityUUID(int cityUUID) {
		this.cityUUID = cityUUID;
	}

	public ArrayList<PlayerCharacter> getBanishList() {
		if (banishList == null)
			return new ArrayList<>();
		return banishList;
	}

	public ArrayList<PlayerCharacter> getCharacterKOSList() {
		return characterKOSList;
	}

	public ArrayList<Guild> getGuildKOSList() {
		return guildKOSList;
	}

	public ArrayList<Guild> getAllyList() {
		return allyList;
	}

	public ArrayList<Guild> getEnemyList() {
		return enemyList;
	}

	public ArrayList<Guild> getSubGuildList() {

		return subGuildList;
	}

	public GuildTag getGuildTag() {
		return this.guildTag;
	}

	public int getCharter() {
		return charter;
	}

	public int getGuildLeaderUUID() {
		return this.guildLeaderUUID;
	}

	public static AbstractCharacter GetGL(Guild guild) {
		if (guild == null)
			return null;

		if (guild.guildLeaderUUID == 0)
			return null;

		if (guild.ownerIsNPC)
			return NPC.getFromCache(guild.guildLeaderUUID);

		return PlayerCharacter.getFromCache(guild.guildLeaderUUID);
	}

	public String getMOTD() {
		return this.motd;
	}

	public String getICMOTD() {
		return this.icmotd;
	}

	public boolean isNPCGuild() {

		return this.ownerIsNPC;
	}

	public int getRepledgeMin() {
		return this.repledgeMin;
	}

	public int getRepledgeMax() {
		return this.repledgeMax;
	}

	public int getRepledgeKick() {
		return this.repledgeKick;
	}

	public int getTeleportMin() {
		return this.teleportMin;
	}

	public int getTeleportMax() {
		return this.teleportMax;
	}

	public int getMineTime() {
		return this.mineTime;
	}

	/*
	 * Setters
	 */
	public void setGuildLeaderUUID(int value) {
		this.guildLeaderUUID = value;
	}

	public boolean setGuildLeader(AbstractCharacter ac) {
		if (ac == null)
			return false;
		// errant guilds cant be guild leader.
		if (this.isErrant())
			return false;
		
		if (!DbManager.GuildQueries.SET_GUILD_LEADER(ac.getObjectUUID(), this.getObjectUUID())){
			if (ac.getObjectType().equals(GameObjectType.PlayerCharacter))
			ChatManager.chatGuildError((PlayerCharacter)ac, "Failed to change guild leader!");
			return false;
		}
		
		PlayerCharacter oldGuildLeader = PlayerCharacter.getFromCache(this.guildLeaderUUID);
		
		//old guildLeader no longer has guildLeadership stauts.
		if (oldGuildLeader != null)
			oldGuildLeader.setGuildLeader(false);
		
		if (ac.getObjectType().equals(GameObjectType.PlayerCharacter))
			((PlayerCharacter)ac).setGuildLeader(true);
		this.guildLeaderUUID = ac.getObjectUUID();
		
		return true;
	}
	
	public boolean setGuildLeaderForCreate(AbstractCharacter ac) {
		if (ac == null)
			return false;
		// errant guilds cant be guild leader.
		if (this.isErrant())
			return false;
		
		if (ac.getObjectType().equals(GameObjectType.PlayerCharacter))
			((PlayerCharacter)ac).setGuildLeader(true);
		this.guildLeaderUUID = ac.getObjectUUID();
		
		return true;
	}

	public void setMOTD(String value) {
		this.motd = value;
	}

	public void setICMOTD(String value) {
		this.icmotd = value;
	}

	public int getBgc1() {
		if (this.guildTag != null)
			return this.guildTag.backgroundColor01;
		return 16;
	}

	public int getBgc2() {
		if (this.guildTag != null)
			return this.guildTag.backgroundColor02;
		else
			return 16;
	}

	public int getBgDesign() {
		if (this.guildTag != null)
			return this.guildTag.backgroundDesign;
		return 0;
	}

	public int getSc() {
		if (this.guildTag != null)
			return this.guildTag.symbolColor;
		return 16;
	}

	public int getSymbol() {
		if (this.guildTag != null)
			return this.guildTag.symbol;
		return 0;
	}

	/*
	 * Utils
	 */
	

	public static Guild getErrantGuild() {
		return g;
	}
	
	public static void CreateErrantGuild(){
		
			g = new Guild( "None", Guild.getErrantNation(), 0,
					"Anarchy", GuildTag.ERRANT, 0);
			g.getObjectType();
		
	}

	public boolean isErrant() {
		if (this.getObjectUUID() == Guild.g.getObjectUUID())
			return true;
        return this.getObjectUUID() == Guild.errant.getObjectUUID();
    }



	public static boolean sameGuild(Guild a, Guild b) {
		if (a == null || b == null)
			return false;
        return a.getObjectUUID() == b.getObjectUUID();
    }

	public static boolean sameGuildExcludeErrant(Guild a, Guild b) {
		if (a == null || b == null)
			return false;
		if (a.isErrant() || b.isErrant())
			return false;
        return a.getObjectUUID() == b.getObjectUUID();
    }

	public static boolean sameGuildIncludeErrant(Guild a, Guild b) {
		if (a == null || b == null)
			return false;
		if (a.isErrant() || b.isErrant())
			return true;
        return a.getObjectUUID() == b.getObjectUUID();
    }

	public static boolean sameNation(Guild a, Guild b) {
		if (a == null || b == null)
			return false;
		if (a.getObjectUUID() == b.getObjectUUID())
			return true;
        if (a.nation == null || b.nation == null)
			return false;
        return a.nation.getObjectUUID() == b.nation.getObjectUUID();
    }

	public static boolean sameNationExcludeErrant(Guild a, Guild b) {
		if (a == null || b == null)
			return false;
		if (a.getObjectUUID() == b.getObjectUUID())
			return true;
        if (a.nation == null || b.nation == null)
			return false;
        return a.nation.getObjectUUID() == b.nation.getObjectUUID() && !a.nation.isErrant();
    }

	public boolean isGuildLeader(int uuid) {

        return (this.guildLeaderUUID == uuid);
	}

	public static boolean isTaxCollector(int uuid) {
		//TODO add the handling for this later
		return false;
	}

	/**
	 * Removes a PlayerCharacter from this (non-Errant) Guild.
	 *
	 * @param pc PlayerCharacter to be removed
	 */
	public void removePlayer(PlayerCharacter pc, GuildHistoryType historyType) {

		if (this.isErrant()) {
			Logger.warn( "Attempted to remove a PlayerCharacter (" + pc.getObjectUUID() + ") from an errant guild.");
			return;
		}

		//Add to Guild History
		if (pc.getGuild() != null){
			if (DbManager.GuildQueries.ADD_TO_GUILDHISTORY(pc.getGuildUUID(), pc, DateTime.now(), historyType)){
                GuildHistory guildHistory = new GuildHistory(pc.getGuildUUID(), pc.getGuild().name,DateTime.now(), historyType) ;
				pc.getGuildHistory().add(guildHistory);
			}
		}

		// Clear Guild Ranks
		pc.resetGuildStatuses();
		pc.setGuild(Guild.getErrantGuild());

		pc.incVer();
		DispatchMessage.sendToAllInRange(pc, new GuildInfoMsg(pc, Guild.getErrantGuild(), 2));

	}

	public void upgradeGuildState(boolean nation){
		if (nation){
			this.guildState = GuildState.Nation;
			return;
		}
		switch(this.guildState){

		case Errant:
			this.guildState = GuildState.Petitioner;
			break;
		case Sworn:
			//Can't upgrade
			break;
		case Protectorate:
			this.guildState = GuildState.Province;
			break;
		case Petitioner:
			this.guildState = GuildState.Sworn;
			break;
		case Province:
			//Can't upgrade
			break;
		case Nation:
			//Can't upgrade
			break;
		case Sovereign:
			this.guildState = GuildState.Protectorate;
			break;
		}

	}

	public void downgradeGuildState(){

		switch(this.guildState){
		case Errant:
			break;
		case Sworn:
			this.guildState = GuildState.Errant;
			break;
		case Protectorate:
			this.guildState = GuildState.Sovereign;
			break;
		case Petitioner:
			this.guildState = GuildState.Errant;
			break;
		case Province:
			this.guildState = GuildState.Sovereign;
			break;
		case Nation:
			this.guildState = GuildState.Sovereign;
			break;
		case Sovereign:
			this.guildState = GuildState.Errant;
			break;
		}

	}

	public boolean canSubAGuild(Guild toSub){

		boolean canSub;
		
		if (this.equals(toSub))
			return false;

		switch(this.guildState){
		case Nation:
		case Sovereign:
			canSub = true;
			break;
		default:
			canSub = false;
		}

		switch(toSub.guildState){
		case Errant:
		case Sovereign:
			canSub = true;
			break;
		default:
			canSub = false;
		}

		return canSub;
	}

	public static boolean canSwearIn(Guild toSub){

		boolean canSwear = false;

		switch(toSub.guildState){

		case Protectorate:
		case Petitioner:
			canSwear = true;
			break;
		default:
			canSwear = false;
		}

		return canSwear;
	}

	/*
	 * Serialization
	 */
	
	public static void _serializeForClientMsg(Guild guild, ByteBufferWriter writer) {
Guild.serializeForClientMsg(guild,writer, null, false);
	}

	public static void serializeForClientMsg(Guild guild, ByteBufferWriter writer, PlayerCharacter pc, boolean reshowGuild) {
		writer.putInt(guild.getObjectType().ordinal());
		writer.putInt(guild.getObjectUUID());
        writer.putInt(guild.nation.getObjectType().ordinal());
        writer.putInt(guild.nation.getObjectUUID());

		if (pc == null) {
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0); // Defaults
			writer.putInt(0); // Defaults
		} else {
			writer.putString(guild.name);
            writer.putString(guild.nation.name);
			writer.putInt(GuildStatusController.getTitle(pc.getGuildStatus())); // TODO Double check this is
			// title and rank
			if (GuildStatusController.isGuildLeader(pc.getGuildStatus()))
				writer.putInt(PlayerCharacter.GetPlayerRealmTitle(pc));
			else
				writer.putInt(GuildStatusController.getRank(pc.getGuildStatus()));
			//writer.putInt(GuildStatusController.getRank(pc.getGuildStatus()));
		}

		City ownedCity = guild.getOwnedCity();

		if (ownedCity != null){
			Realm realm = guild.getOwnedCity().getRealm();
			if (realm != null && realm.getRulingCity() != null){
				if (realm.getRulingCity().equals(ownedCity)){
					writer.putInt(realm.getCharterType());
				}else
					writer.putInt(0);
			}else{
				writer.putInt(0);
			}
		}else
			writer.putInt(0);

		writer.putFloat(200);
		writer.putFloat(200); // Pad

		GuildTag._serializeForDisplay(guild.guildTag,writer);
        GuildTag._serializeForDisplay(guild.nation.guildTag,writer);
		if (reshowGuild) {
			writer.putInt(1);
			writer.putInt(guild.getObjectType().ordinal());
			writer.putInt(guild.getObjectUUID());

		} else
			writer.putInt(0); // Pad
	}

	public static void serializeForTrack(Guild guild,ByteBufferWriter writer) {
		Guild.serializeGuildForTrack(guild,writer);
		if (guild.nation != null)
			Guild.serializeGuildForTrack(guild.nation,writer);
		else
			Guild.addErrantForTrack(writer);
	}

	public static void serializeGuildForTrack(Guild guild, ByteBufferWriter writer) {
		writer.putInt(guild.getObjectType().ordinal());
		writer.putInt(guild.getObjectUUID());
		writer.put((byte) 1);
		GuildTag._serializeForDisplay(guild.guildTag,writer);
	}

	public  static void serializeErrantForTrack(ByteBufferWriter writer) {
		addErrantForTrack(writer); //Guild
		addErrantForTrack(writer); //Nation
	}

	public int getRealmsOwnedFlag(){
		int flag = 0;
        switch(realmsOwned){
		case 0:
			flag = 0;
		case 1:
		case 2:
			flag = 1;
			break;
		case 3:
		case 4:
			flag = 2;
			break;
		case 5:
			flag = 3;
			break;
		default:
			flag = 3;
			break;
		}
		return flag;
	}

	private static void addErrantForTrack(ByteBufferWriter writer) {
		writer.putInt(0); //type
		writer.putInt(0); //ID
		writer.put((byte) 1);
		writer.putInt(16); //Tags
		writer.putInt(16);
		writer.putInt(16);
		writer.putInt(0);
		writer.putInt(0);
	}

	public void serializeForPlayer(ByteBufferWriter writer) {
		writer.putInt(this.getObjectType().ordinal());
		writer.putInt(this.getObjectUUID());
        writer.putInt(this.nation.getObjectType().ordinal());
        writer.putInt(this.nation.getObjectUUID());

	}

	private static Guild errant;

	public static Guild getErrantNation() {
		if (Guild.errant == null)
			Guild.errant = new Guild("None", null, 10, "Despot Rule", GuildTag.ERRANT, 0);
		return Guild.errant;
	}

	/*
	 * Game Object Manager
	 */
	public static Guild getGuild(final int objectUUID) {

		if (objectUUID == 0)
			return Guild.getErrantGuild();
		Guild guild  = (Guild) DbManager.getFromCache(Enum.GameObjectType.Guild, objectUUID);
		if (guild != null)
			return guild;
		
		Guild dbGuild = DbManager.GuildQueries.GET_GUILD(objectUUID);
		
		if (dbGuild == null)
			return Guild.getErrantGuild();
		else
			return dbGuild;
	}


	@Override
	public void updateDatabase() {
		DbManager.GuildQueries.updateDatabase(this);
	}

	public boolean isRealmRuler() {

		City ownedCity;
		Building tol;

		ownedCity = this.getOwnedCity();

		if (ownedCity == null)
			return false;

		tol = ownedCity.getTOL();

		if (tol == null)
			return false;

        return tol.getRank() == 8;

    }

	@Override
	public void runAfterLoad() {

		try {
			DbObjectType objectType = DbManager.BuildingQueries.GET_UID_ENUM(this.guildLeaderUUID);
			this.ownerIsNPC = (objectType == DbObjectType.NPC);
		} catch (Exception e) {
			this.ownerIsNPC = false;
			Logger.error("Failed to find Object Type for owner " + this.guildLeaderUUID);
		}


		//LOad Owners in Cache so we do not have to continuely look in the db for owner.
		if (this.ownerIsNPC){
			if (NPC.getNPC(this.guildLeaderUUID) == null)
				Logger.info( "Guild UID " + this.getObjectUUID() + " Failed to Load NPC Owner with ID " + this.guildLeaderUUID);

		}else if (this.guildLeaderUUID != 0){
			if (PlayerCharacter.getPlayerCharacter(this.guildLeaderUUID) == null)
				Logger.info( "Guild UID " + this.getObjectUUID() + " Failed to Load Player Owner with ID " + this.guildLeaderUUID);
		}

		// If loading this guild for the first time write it's character record to disk

        if (ConfigManager.serverType.equals(ServerType.WORLDSERVER)
				&& (hash == null)) {

			this.setHash();

			if (DataWarehouse.recordExists(Enum.DataRecordType.GUILD, this.getObjectUUID()) == false) {
				GuildRecord guildRecord = GuildRecord.borrow(this, Enum.RecordEventType.CREATE);
				DataWarehouse.pushToWarehouse(guildRecord);
			}

		}

		if (MBServerStatics.worldUUID == nationUUID && this.cityUUID != 0)
			this.nation = this;
		else if (nationUUID == 0 || (MBServerStatics.worldUUID == nationUUID && this.cityUUID == 0)) {
			this.nation = Guild.getErrantGuild();
			this.nmotd = "";
		} else
			this.nation = Guild.getGuild(nationUUID);
		
		if (this.nation == null)
			this.nation = Guild.getErrantGuild();
		//Get guild states.
		try {
			this.subGuildList = DbManager.GuildQueries.GET_SUB_GUILDS(this.getObjectUUID());
		}catch(Exception e){

			this.subGuildList = new ArrayList<>();
			Logger.error( "FAILED TO LOAD SUB GUILDS FOR UUID " + this.getObjectUUID());
		}

        if (this.nation == this && subGuildList.size() > 0)
			this.guildState = GuildState.Nation;
		else if (this.nation.equals(this))
			this.guildState = GuildState.Sovereign;
		else if (!this.nation.isErrant() && this.cityUUID != 0)
			this.guildState = GuildState.Province;
		else if (!this.nation.isErrant())
			this.guildState = GuildState.Sworn;
		else
			this.guildState = GuildState.Errant;

		if (this.cityUUID == 0)
			return;


		// Calculate number of realms this guild controls
		// Only do this on the game server to avoid loading a TOL/City/Zone needlessly

		if ((ConfigManager.serverType.equals(ServerType.WORLDSERVER)) &&
				(this.isRealmRuler() == true)) {
			this.realmsOwned++;
            if (!this.nation.equals(this)) {
                this.nation.realmsOwned++;
            }
		}

		if (ConfigManager.serverType.equals(ServerType.WORLDSERVER)){

			//add alliance list, clear all lists as there seems to be a bug where alliances are doubled, need to find where.
			//possible runAfterLoad being called twice?!?!
			this.banishList = dbGuildHandler.GET_GUILD_BANISHED(this.getObjectUUID());
			this.characterKOSList = DbManager.GuildQueries.GET_GUILD_KOS_CHARACTER(this.getObjectUUID());
			this.guildKOSList = DbManager.GuildQueries.GET_GUILD_KOS_GUILD(this.getObjectUUID());

			this.allyList.clear();
			this.enemyList.clear();
			this.recommendList.clear();

			try{
				DbManager.GuildQueries.LOAD_ALL_ALLIANCES_FOR_GUILD(this);
				for (GuildAlliances guildAlliance:this.guildAlliances.values()){
					if (guildAlliance.isRecommended()){
						Guild recommendedGuild = Guild.getGuild(guildAlliance.getAllianceGuild());
						if (recommendedGuild != null)
							this.recommendList.add(recommendedGuild);
					}else if (guildAlliance.isAlly()){
						Guild alliedGuild = Guild.getGuild(guildAlliance.getAllianceGuild());
						if (alliedGuild != null)
							this.allyList.add(alliedGuild);
					}else{
						Guild enemyGuild = Guild.getGuild(guildAlliance.getAllianceGuild());
						if (enemyGuild != null)
							this.enemyList.add(enemyGuild);
					}

				}
			}catch(Exception e){
				Logger.error(this.getObjectUUID() + e.getMessage());
			}
		}
	}

	/**
	 * @return the motto
	 */
	public String getMotto() {
		return motto;
	}

	public GuildState getGuildState() {
		return guildState;
	}

	public void setGuildState(GuildState guildState) {
		this.guildState = guildState;
	}

	/**
	 * @return the realmsOwned
	 */
	public int getRealmsOwned() {
		return realmsOwned;
	}

	/**
	 * @param realmsOwned the realmsOwned to set
	 */
	public void setRealmsOwned(int realmsOwned) {
		this.realmsOwned = realmsOwned;
	}

	public void removeSubGuild(Guild subGuild) {

		// Update database

		if (!DbManager.GuildQueries.UPDATE_PARENT(subGuild.getObjectUUID(), MBServerStatics.worldUUID))
			Logger.debug("Failed to set Nation Guild for Guild with UID " + subGuild.getObjectUUID());

		// Guild without any subs is no longer a nation

		if (subGuild.getOwnedCity() == null) {
			subGuild.nation = null;
		}
		else {
			subGuild.nation = subGuild;
		}

		// Downgrade guild

		subGuild.downgradeGuildState();

		// Remove from collection

        subGuildList.remove(subGuild);

		GuildManager.updateAllGuildTags(subGuild);
		GuildManager.updateAllGuildBinds(subGuild, subGuild.getOwnedCity());

	}

	public void setMineTime(int mineTime) {
		this.mineTime = mineTime;
	}

	public ConcurrentHashMap<Integer,Condemned> getGuildCondemned() {
		return guildCondemned;
	}


	public String getHash() {
		return hash;
	}

	public void setHash() {
		this.hash = DataWarehouse.hasher.encrypt(this.getObjectUUID());

		DataWarehouse.writeHash(Enum.DataRecordType.GUILD, this.getObjectUUID());
	}

	public Enum.GuildType getGuildType(){
		try{
			return Enum.GuildType.values()[this.charter];
		}catch(Exception e){
			Logger.error(e);
			return Enum.GuildType.NONE;
		}

	}

	public ArrayList<Guild> getRecommendList() {
		return recommendList;
	}

	public void setRecommendList(ArrayList<Guild> recommendList) {
		this.recommendList = recommendList;
	}

	public synchronized boolean addGuildToAlliance(AllianceChangeMsg msg, final AllianceType allianceType, Guild toGuild, PlayerCharacter player){

		Dispatch dispatch;

		// Member variable assignment


		if (toGuild == null)
			return false;

		if (!Guild.sameGuild(player.getGuild(), this)){
			msg.setMsgType(AllianceChangeMsg.ERROR_NOT_SAME_GUILD);
			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			return false;
		}

		if (allianceType == AllianceType.Ally || allianceType == AllianceType.Enemy)
			if (!GuildStatusController.isInnerCouncil(player.getGuildStatus()) && !GuildStatusController.isGuildLeader(player.getGuildStatus())){
				msg.setMsgType(AllianceChangeMsg.ERROR_NOT_AUTHORIZED);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

		if (allianceType == AllianceType.RecommendedAlly || allianceType == AllianceType.RecommendedEnemy){
			if (!GuildStatusController.isFullMember(player.getGuildStatus())){
				msg.setMsgType(AllianceChangeMsg.ERROR_NOT_AUTHORIZED);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}
		}

		//		if (this.getGuildType() != toGuild.getGuildType()){
		//			msg.setMsgType(AllianceChangeMsg.ERROR_NOT_SAME_FACTION);
		//			dispatch = Dispatch.borrow(player, msg);
		//			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		//			return false;
		//		}






		switch(allianceType){
		case RecommendedAlly:
            if (recommendList.size() == 10){
				msg.setMsgType(AllianceChangeMsg.ERROR_TOO_MANY);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

            if (recommendList.contains(toGuild)){
				ErrorPopupMsg.sendErrorMsg(player, "This guild is already recommonded!");
				msg.setMsgType((byte)15);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

			if (!DbManager.GuildQueries.ADD_TO_ALLIANCE_LIST(this.getObjectUUID(), toGuild.getObjectUUID(), true, true, player.getFirstName())){
				msg.setMsgType((byte)15);
				dispatch = Dispatch.borrow(player, msg);
				return false;
			}

			GuildAlliances guildAlliance = new GuildAlliances(this.getObjectUUID(), toGuild.getObjectUUID(), true, true, player.getFirstName());
			this.guildAlliances.put(toGuild.getObjectUUID(), guildAlliance);
			this.removeGuildFromEnemy(toGuild);
			this.removeGuildFromAlliance(toGuild);
			this.recommendList.add(toGuild);



			return true;

		case RecommendedEnemy:
            if (recommendList.size() == 10){
				msg.setMsgType(AllianceChangeMsg.ERROR_TOO_MANY);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

            if (recommendList.contains(toGuild)){
				ErrorPopupMsg.sendErrorMsg(player, "This guild is already recommonded!");
				msg.setMsgType((byte)15);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

			if (!DbManager.GuildQueries.ADD_TO_ALLIANCE_LIST(this.getObjectUUID(), toGuild.getObjectUUID(), true, false, player.getFirstName())){
				msg.setMsgType((byte)15);
				dispatch = Dispatch.borrow(player, msg);
				return false;
			}

			GuildAlliances enemyAlliance = new GuildAlliances(this.getObjectUUID(), toGuild.getObjectUUID(), true, false, player.getFirstName());
			this.guildAlliances.put(toGuild.getObjectUUID(), enemyAlliance);
			this.removeGuildFromEnemy(toGuild);
			this.removeGuildFromAlliance(toGuild);
			this.recommendList.add(toGuild);

			return true;

		case Ally:
            if (allyList.size() == 10){
				msg.setMsgType(AllianceChangeMsg.ERROR_TOO_MANY);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

            if (allyList.contains(toGuild)){
				ErrorPopupMsg.sendErrorMsg(player, "This guild is already an Ally!");
				msg.setMsgType((byte)15);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

			if (!this.guildAlliances.containsKey(toGuild.getObjectUUID())){
				ErrorPopupMsg.sendErrorMsg(player, "A Serious error has Occured. Please contact CCR!");
				Logger.error(this.getObjectUUID() +  " Could not find alliance Guild");
				msg.setMsgType((byte)15);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

			GuildAlliances ally = this.guildAlliances.get(toGuild.getObjectUUID());
			if (!ally.UpdateAlliance(AllianceType.Ally, this.recommendList.contains(toGuild))){
				ErrorPopupMsg.sendErrorMsg(player, "A Serious error has Occured. Please contact CCR!");
				Logger.error( this.getObjectUUID() +  " failed to update alliance Database");
				msg.setMsgType((byte)15);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;

			}

			this.removeGuildFromEnemy(toGuild);
			this.removeGuildFromRecommended(toGuild);

			this.allyList.add(toGuild);
			Guild.UpdateClientAlliances(this);


			break;
		case Enemy:
            if (enemyList.size() == 10){
				msg.setMsgType(AllianceChangeMsg.ERROR_TOO_MANY);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

            if (enemyList.contains(toGuild)){
				ErrorPopupMsg.sendErrorMsg(player, "This guild is already an Enemy!");
				msg.setMsgType((byte)15);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

			if (!this.guildAlliances.containsKey(toGuild.getObjectUUID())){
				ErrorPopupMsg.sendErrorMsg(player, "A Serious error has Occured. Please contact CCR!");
				Logger.error( this.getObjectUUID() +  " Could not find alliance Guild");
				msg.setMsgType((byte)15);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;
			}

			GuildAlliances enemy = this.guildAlliances.get(toGuild.getObjectUUID());
			if (!enemy.UpdateAlliance(AllianceType.Enemy, this.recommendList.contains(toGuild))){
				ErrorPopupMsg.sendErrorMsg(player, "A Serious error has Occured. Please contact CCR!");
				Logger.error(this.getObjectUUID() +  " failed to update alliance Database");
				msg.setMsgType((byte)15);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
				return false;

			}

			//remove from other allied lists.
			this.removeGuildFromAlliance(toGuild);
			this.removeGuildFromRecommended(toGuild);

			this.enemyList.add(toGuild);

			Guild.UpdateClientAlliances(this);
			break;
		}

		// once here everything passed, send successMsg;
		msg.setMsgType(AllianceChangeMsg.INFO_SUCCESS);
		dispatch = Dispatch.borrow(player, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		return true;
	}

	public synchronized boolean removeGuildFromAlliance(Guild toRemove){
		if (this.allyList.contains(toRemove)){
			this.allyList.remove(toRemove);
		}
		return true;
	}
	public synchronized boolean removeGuildFromEnemy(Guild toRemove){
		if (this.enemyList.contains(toRemove)){
			this.enemyList.remove(toRemove);
		}
		return true;
	}
	public synchronized boolean removeGuildFromRecommended(Guild toRemove){
		if (this.recommendList.contains(toRemove)){
			this.recommendList.remove(toRemove);
		}
		return true;
	}

	public synchronized boolean removeGuildFromAllAlliances(Guild toRemove){

		if (!this.guildAlliances.containsKey(toRemove.getObjectUUID())){
			return false;
		}

		if (!DbManager.GuildQueries.REMOVE_FROM_ALLIANCE_LIST(this.getObjectUUID(), toRemove.getObjectUUID()))
			return false;



		this.guildAlliances.remove(toRemove.getObjectUUID());

		this.removeGuildFromAlliance(toRemove);
		this.removeGuildFromEnemy(toRemove);
		this.removeGuildFromRecommended(toRemove);

		Guild.UpdateClientAlliances(this);


		return true;

	}

	public static void UpdateClientAlliances(Guild toUpdate){
		UpdateClientAlliancesMsg ucam = new UpdateClientAlliancesMsg(toUpdate);



		for (PlayerCharacter player : SessionManager.getAllActivePlayerCharacters()) {

			if (Guild.sameGuild(player.getGuild(), toUpdate)){
				Dispatch dispatch = Dispatch.borrow(player, ucam);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			}


		}
	}

	public static void UpdateClientAlliancesForPlayer(PlayerCharacter toUpdate){
		UpdateClientAlliancesMsg ucam = new UpdateClientAlliancesMsg(toUpdate.getGuild());
		Dispatch dispatch = Dispatch.borrow(toUpdate, ucam);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);


	}
	
	public static Guild getFromCache(int id) {
		return (Guild) DbManager.getFromCache(GameObjectType.Guild, id);
	}
	
	public static ArrayList<PlayerCharacter> GuildRoster(Guild guild){
		ArrayList<PlayerCharacter> roster = new ArrayList<>();
		if (guild == null)
			return roster;
		
		if (guild.isErrant())
			return roster;
		
		if (DbManager.getList(GameObjectType.PlayerCharacter) == null)
			return roster;
		for (AbstractGameObject ago : DbManager.getList(GameObjectType.PlayerCharacter)){
			PlayerCharacter toAdd = (PlayerCharacter)ago;
			
			if (!toAdd.getGuild().equals(guild))
			continue;
			
			if (toAdd.isDeleted())
				continue;
			
			roster.add(toAdd);
			
		}
		return roster;
	}



}
