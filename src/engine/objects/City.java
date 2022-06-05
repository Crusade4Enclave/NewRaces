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
import engine.InterestManagement.HeightMap;
import engine.InterestManagement.RealmMap;
import engine.InterestManagement.WorldGrid;
import engine.db.archive.CityRecord;
import engine.db.archive.DataWarehouse;
import engine.gameManager.*;
import engine.math.Bounds;
import engine.math.FastMath;
import engine.math.Vector2f;
import engine.math.Vector3fImmutable;
import engine.net.ByteBufferWriter;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.TaxResourcesMsg;
import engine.net.client.msg.ViewResourcesMessage;
import engine.powers.EffectsBase;
import engine.server.MBServerStatics;
import engine.workthreads.DestroyCityThread;
import engine.workthreads.TransferCityThread;
import org.pmw.tinylog.Logger;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class City extends AbstractWorldObject {

	private String cityName;
	private String motto;
	private  String description;
	public java.time.LocalDateTime established;
	private  int isNoobIsle; //1: noob, 0: not noob: -1: not noob, no teleport
	private int population = 0;
	private int siegesWithstood = 0;
	private  int realmID;
	private  int radiusType;
	private  float bindRadius;
	private  float statLat;
	private  float statAlt;
	private  float statLon;
	private  float bindX;
	private  float bindZ;
	private  byte isNpc;  //aka Safehold
	private byte isCapital = 0;
	private  byte isSafeHold;
	private boolean forceRename = false;
	public boolean hasBeenTransfered = false;

	private boolean noTeleport = false; //used by npc cities
	private boolean noRepledge = false; //used by npc cities
	private boolean isOpen = false;

	private int treeOfLifeID;
	private Vector3fImmutable location = Vector3fImmutable.ZERO;
	private Vector3fImmutable bindLoc;
	protected Zone parentZone;
	private int warehouseBuildingID = 0;
	private boolean open = false;
	private boolean reverseKOS = false;
	public static long lastCityUpdate = 0;
	public LocalDateTime realmTaxDate;
	
	public ReentrantReadWriteLock transactionLock = new ReentrantReadWriteLock();

	// Players who have entered the city (used for adding and removing affects)

	private final HashSet<Integer> _playerMemory = new HashSet<>();

	public volatile boolean protectionEnforced = true;
	private String hash;

	/**
	 * ResultSet Constructor
	 */

	public City(ResultSet rs) throws SQLException, UnknownHostException {
		super(rs);
		try{
			this.cityName = rs.getString("name");
			this.motto = rs.getString("motto");
			this.isNpc = rs.getByte("isNpc");
			this.isSafeHold = (byte) ((this.isNpc == 1) ? 1 : 0);
			this.description = ""; // TODO Implement this!
			this.isNoobIsle = rs.getByte("isNoobIsle"); // Noob
			this.gridObjectType = GridObjectType.STATIC;
			// Island
			// City(00000001),
			// Otherwise(FFFFFFFF)
			this.population = rs.getInt("population");
			this.siegesWithstood = rs.getInt("siegesWithstood");

			java.sql.Timestamp establishedTimeStamp = rs.getTimestamp("established");

			if (establishedTimeStamp != null)
				this.established = java.time.LocalDateTime.ofInstant(establishedTimeStamp.toInstant(), ZoneId.systemDefault());

			this.location = new Vector3fImmutable(rs.getFloat("xCoord"), rs.getFloat("yCoord"), rs.getFloat("zCoord"));
			this.statLat = rs.getFloat("xCoord");
			this.statAlt = rs.getFloat("yCoord");
			this.statLon = rs.getFloat("zCoord");

			java.sql.Timestamp realmTaxTimeStamp = rs.getTimestamp("realmTaxDate");

			if (realmTaxTimeStamp != null)
				this.realmTaxDate = realmTaxTimeStamp.toLocalDateTime();

			if (this.realmTaxDate == null)
				this.realmTaxDate = LocalDateTime.now();

			this.treeOfLifeID = rs.getInt("treeOfLifeUUID");
			this.bindX = rs.getFloat("bindX");
			this.bindZ = rs.getFloat("bindZ");
			this.bindLoc = new Vector3fImmutable(this.location.getX() + this.bindX,
					this.location.getY(),
					this.location.getZ() + this.bindZ);
			this.radiusType = rs.getInt("radiusType");
			float bindradiustemp = rs.getFloat("bindRadius");
			if (bindradiustemp > 2)
				bindradiustemp -=2;

			this.bindRadius = bindradiustemp;

			this.forceRename = rs.getInt("forceRename") == 1;
			this.open = rs.getInt("open") == 1;

			if (this.cityName.equals("Perdition") || this.cityName.equals("Bastion")) {
				this.noTeleport = true;
				this.noRepledge = true;
			} else {
				this.noTeleport = false;
				this.noRepledge = false;
			}

			this.hash = rs.getString("hash");
			
			if (this.motto.isEmpty()){
				Guild guild = this.getGuild();
				
				if (guild != null && guild.isErrant() == false)
					this.motto = guild.getMotto();
			}
				

			//Disabled till i finish.
			// this.reverseKOS  = rs.getInt("kos") == 1;


			Zone zone = ZoneManager.getZoneByUUID(rs.getInt("parent"));

			if (zone != null)
				setParent(zone);
			
			//npc cities without heightmaps except swampstone are specials.
			
				

			this.realmID = rs.getInt("realmID");

		}catch(Exception e){
			Logger.error(e);
		}

		// *** Refactor: Is this working?  Intended to supress
		//                login server errors from attempting to
		//                 load cities/realms along with players



	}

	/*
	 * Utils
	 */

	public boolean renameCity(String cityName){
		if (!DbManager.CityQueries.renameCity(this, cityName))
			return false;
		if (!DbManager.CityQueries.updateforceRename(this, false))
			return false;

		this.cityName = cityName;
		this.forceRename = false;
		return true;
	}

	public boolean updateTOL(Building tol){
		if (tol == null)
			return false;
		if (!DbManager.CityQueries.updateTOL(this, tol.getObjectUUID()))
			return false;
		this.treeOfLifeID = tol.getObjectUUID();
		return true;
	}

	public boolean renameCityForNewPlant(String cityName){
		if (!DbManager.CityQueries.renameCity(this, cityName))
			return false;
		if (!DbManager.CityQueries.updateforceRename(this, true))
			return false;

		this.cityName = cityName;
		this.forceRename = true;
		return true;
	}

	public void setForceRename(boolean forceRename) {
		if (!DbManager.CityQueries.updateforceRename(this, forceRename))
			return;
		this.forceRename = forceRename;
	}
	public String getCityName() {

		return cityName;
	}

	public String getMotto() {
		return motto;
	}

	public String getDescription() {
		return description;
	}

	public Building getTOL() {
		if (this.treeOfLifeID == 0)
			return null;

		return BuildingManager.getBuildingFromCache(this.treeOfLifeID);

	}

	public int getIsNoobIsle() {
		return isNoobIsle;
	}

	public int getPopulation() {
		return population;
	}

	public int getSiegesWithstood() {
		return siegesWithstood;
	}

	public float getLatitude() {
		return this.location.x;
	}

	public float getLongitude() {
		return this.location.z;
	}

	public float getAltitude() {
		return this.location.y;
	}

	@Override
	public Vector3fImmutable getLoc() {
		return this.location;
	}

	public byte getIsNpcOwned() {
		return isNpc;
	}

	public byte getIsSafeHold() {
		return this.isSafeHold;
	}

	public boolean isSafeHold() {
		return (this.isSafeHold == (byte) 1);
	}

	public byte getIsCapital() {
		return isCapital;
	}

	public void setIsCapital(boolean state) {
		this.isCapital = (state) ? (byte) 1 : (byte) 0;
	}

	public int getRadiusType() {
		return this.radiusType;
	}

	public float getBindRadius() {
		return this.bindRadius;
	}

	public int getRank() {
		return (this.getTOL() == null) ? 0 : this.getTOL().getRank();
	}

	public Bane getBane() {
		return Bane.getBane(this.getObjectUUID());
	}

	public void setParent(Zone zone) {

		try {
			
		
		this.parentZone = zone;
		this.location = new Vector3fImmutable(zone.absX + statLat, zone.absY + statAlt, zone.absZ + statLon);
		this.bindLoc = new Vector3fImmutable(this.location.x + this.bindX,
				this.location.y,
				this.location.z + this.bindZ);

		// set city bounds

		Bounds cityBounds = Bounds.borrow();
		cityBounds.setBounds(new Vector2f(this.location.x + 64, this.location.z + 64), // location x and z are offset by 64 from the center of the city.
				new Vector2f(Enum.CityBoundsType.GRID.extents, Enum.CityBoundsType.GRID.extents),
				0.0f);
		this.setBounds(cityBounds);
		
		if (zone.getHeightMap() == null && this.isNpc == 1 && this.getObjectUUID() != 1213 ){
			HeightMap.GenerateCustomHeightMap(zone);
			Logger.info(zone.getName() + " created custom heightmap");
		}
		}catch(Exception e){
			Logger.error(e);
		}
	}

	public Zone getParent() {
		return this.parentZone;
	}

	public boolean isCityZone(Zone zone) {

		if (zone == null || this.parentZone == null)
			return false;

		return zone.getObjectUUID() == this.parentZone.getObjectUUID();

	}

	public AbstractCharacter getOwner() {

		if (this.getTOL() == null)
			return null;

		int ownerID = this.getTOL().getOwnerUUID();

		if (ownerID == 0)
			return null;

		if (this.isNpc == 1)
			return NPC.getNPC(ownerID);
		else
			return PlayerCharacter.getPlayerCharacter(ownerID);
	}

	public Guild getGuild() {

		if (this.getTOL() == null)
			return null;



		if (this.isNpc == 1) {

			if (this.getTOL().getOwner() == null)
				return null;
			return this.getTOL().getOwner().getGuild();
		} else {
			if (this.getTOL().getOwner() == null)
				return null;
			return this.getTOL().getOwner().getGuild();
		}
	}

	public boolean openCity(boolean open){
		if (!DbManager.CityQueries.updateOpenCity(this, open))
			return false;
		this.open = open;
		return true;
	}

	
	public static void _serializeForClientMsg(City city, ByteBufferWriter writer) {
		City.serializeForClientMsg(city,writer);
	}

	/*
	 * Serializing
	 */

	
	public static void serializeForClientMsg(City city, ByteBufferWriter writer) {
		AbstractCharacter guildRuler;
		Guild rulingGuild;
		Guild rulingNation;
		java.time.LocalDateTime dateTime1900;

		// Cities aren't a city without a TOL. Time to early exit.
		// No need to spam the log here as non-existant TOL's are indicated
		// during bootstrap routines.

		if (city.getTOL() == null){

			Logger.error( "NULL TOL FOR " + city.cityName);
		}


		// Assign city owner

		if (city.getTOL() != null)
			guildRuler = city.getTOL().getOwner();
		else guildRuler = null;

		// If is an errant tree, use errant guild for serialization.
		// otherwise we serialize the soverign guild

		if (guildRuler == null)
			rulingGuild = Guild.getErrantGuild();
		else
			rulingGuild = guildRuler.getGuild();

		rulingNation = rulingGuild.getNation();

		// Begin Serialzing soverign guild data
		writer.putInt(city.getObjectType().ordinal());
		writer.putInt(city.getObjectUUID());
		writer.putString(city.cityName);
		writer.putInt(rulingGuild.getObjectType().ordinal());
		writer.putInt(rulingGuild.getObjectUUID());

		writer.putString(rulingGuild.getName());
		writer.putString(city.motto);
		writer.putString(rulingGuild.getLeadershipType());

		// Serialize guild ruler's name
		// If tree is abandoned blank out the name
		// to allow them a rename.

		if (guildRuler == null)
			writer.putString("");
		else
			writer.putString(guildRuler.getFirstName() + ' ' + guildRuler.getLastName());

		writer.putInt(rulingGuild.getCharter());
		writer.putInt(0); // always 00000000

		writer.put(city.isSafeHold);

		writer.put((byte) 1);
		writer.put((byte) 1);  // *** Refactor: What are these flags?
		writer.put((byte) 1);
		writer.put((byte) 1);
		writer.put((byte) 1);

		GuildTag._serializeForDisplay(rulingGuild.getGuildTag(),writer);
		GuildTag._serializeForDisplay(rulingNation.getGuildTag(),writer);

		writer.putInt(0);// TODO Implement description text

		writer.put((byte) 1);

		if (city.isCapital > 0)
			writer.put((byte) 1);
		else
			writer.put((byte) 0);

		writer.put((byte) 1);

		// Begin serializing nation guild info

		if (rulingNation.isErrant()){
			writer.putInt(rulingGuild.getObjectType().ordinal());
			writer.putInt(rulingGuild.getObjectUUID());
		}

		else{
			writer.putInt(rulingNation.getObjectType().ordinal());
			writer.putInt(rulingNation.getObjectUUID());
		}


		// Serialize nation name

		if (rulingNation.isErrant())
			writer.putString("None");
		else
			writer.putString(rulingNation.getName());

		writer.putInt(city.getTOL().getRank());

		if (city.isNoobIsle > 0)
			writer.putInt(1);
		else
			writer.putInt(0xFFFFFFFF);

		writer.putInt(city.population);

		if (rulingNation.isErrant())
			writer.putString(" ");
		else
			writer.putString(Guild.GetGL(rulingNation).getFirstName() + ' ' + Guild.GetGL(rulingNation).getLastName());


		writer.putLocalDateTime(city.established);

//		writer.put((byte) city.established.getDayOfMonth());
//		writer.put((byte) city.established.minusMonths(1).getMonth().getValue());
//		writer.putInt((int) years);
//		writer.put((byte) hours);
//		writer.put((byte) minutes);
//		writer.put((byte) seconds);

		writer.putFloat(city.location.x);
		writer.putFloat(city.location.y);
		writer.putFloat(city.location.z);

		writer.putInt(city.siegesWithstood);

		writer.put((byte) 1);
		writer.put((byte) 0);
		writer.putInt(0x64);
		writer.put((byte) 0);
		writer.put((byte) 0);
		writer.put((byte) 0);
	}

	public static Vector3fImmutable getBindLoc(int cityID) {

		City city;

		city = City.getCity(cityID);

		if (city == null)
			return Enum.Ruins.getRandomRuin().getLocation();

		return city.getBindLoc();
	}

	public Vector3fImmutable getBindLoc() {
		Vector3fImmutable treeLoc = null;

		if (this.getTOL() != null && this.getTOL().getRank() == 8)
			treeLoc = this.getTOL().getStuckLocation();

		if (treeLoc != null)
			return treeLoc;

		if (this.radiusType == 1 && this.bindRadius > 0f) {
			//square radius
			float x = this.bindLoc.getX();
			float z = this.bindLoc.getZ();
			float offset = ((ThreadLocalRandom.current().nextFloat() * 2) - 1) * this.bindRadius;
			int direction = ThreadLocalRandom.current().nextInt(4);

			switch (direction) {
			case 0:
				x += this.bindRadius;
				z += offset;
				break;
			case 1:
				x += offset;
				z -= this.bindRadius;
				break;
			case 2:
				x -= this.bindRadius;
				z += offset;
				break;
			case 3:
				x += offset;
				z += this.bindRadius;
				break;
			}
			return new Vector3fImmutable(x, this.bindLoc.getY(), z);
		} else if (this.radiusType == 2 && this.bindRadius > 0f) {
			//circle radius
			Vector3fImmutable dir = FastMath.randomVector2D();
			return this.bindLoc.scaleAdd(this.bindRadius, dir);
		} else if (this.radiusType == 3 && this.bindRadius > 0f) {
			//random inside square
			float x = this.bindLoc.getX();
			x += ((ThreadLocalRandom.current().nextFloat() * 2) - 1) * this.bindRadius;
			float z = this.bindLoc.getZ();
			z += ((ThreadLocalRandom.current().nextFloat() * 2) - 1) * this.bindRadius;
			return new Vector3fImmutable(x, this.bindLoc.getY(), z);
		} else if (this.radiusType == 4 && this.bindRadius > 0f) {
			//random inside circle
			Vector3fImmutable dir = FastMath.randomVector2D();
			return this.bindLoc.scaleAdd(ThreadLocalRandom.current().nextFloat() * this.bindRadius, dir);
		} else
			//spawn at bindLoc
			//System.out.println("x: " + this.bindLoc.x + ", z: " + this.bindLoc.z);
			return this.bindLoc;
	}

	public static ArrayList<City> getCitiesToTeleportTo(PlayerCharacter pc) {

		ArrayList<City> cities = new ArrayList<>();

		if (pc == null)
			return cities;

		Guild pcG = pc.getGuild();

		ConcurrentHashMap<Integer, AbstractGameObject> worldCities = DbManager.getMap(Enum.GameObjectType.City);

		//add npc cities
		for (AbstractGameObject ago : worldCities.values()) {

			if (ago.getObjectType().equals(GameObjectType.City)) {
				City city = (City) ago;

				if (city.noTeleport)
					continue;

				if (city.parentZone != null && city.parentZone.isPlayerCity()) {

                    if (pc.getAccount().status.equals(AccountStatus.ADMIN)) {
                        cities.add(city);
                    } else
                        //list Player cities

                        //open city, just list
                        if (city.open && city.getTOL() != null && city.getTOL().getRank() > 4) {

                            if (!BuildingManager.IsPlayerHostile(city.getTOL(), pc))
                                cities.add(city); //verify nation or guild is same
                        }

						else if (Guild.sameNationExcludeErrant(city.getGuild(), pcG))
							cities.add(city);


				} else if (city.isNpc == 1) {
					//list NPC cities
					Guild g = city.getGuild();
					if (g == null) {
						if (city.isNpc == 1)
							if (city.isNoobIsle == 1) {
								if (pc.getLevel() < 21)
									cities.add(city);
							} else if (pc.getLevel() > 9)
								cities.add(city);

					} else if (pc.getLevel() >= g.getTeleportMin() && pc.getLevel() <= g.getTeleportMax()){


						cities.add(city);
					}
				}


			}
		}

		return cities;
	}

	public NPC getRuneMaster() {
		NPC outNPC = null;

		if (this.getTOL() == null)
			return outNPC;

		for (AbstractCharacter npc : getTOL().getHirelings().keySet()) {
			if (npc.getObjectType() == GameObjectType.NPC)
				if (((NPC)npc).getContract().isRuneMaster() == true)
					outNPC = (NPC)npc;
		}

		return outNPC;
	}

	public static ArrayList<City> getCitiesToRepledgeTo(PlayerCharacter pc) {
		ArrayList<City> cities = new ArrayList<>();
		if (pc == null)
			return cities;
		Guild pcG = pc.getGuild();

		ConcurrentHashMap<Integer, AbstractGameObject> worldCities = DbManager.getMap(Enum.GameObjectType.City);

		//add npc cities
		for (AbstractGameObject ago : worldCities.values()) {
			if (ago.getObjectType().equals(GameObjectType.City)) {
				City city = (City) ago;
				if (city.noRepledge)
					continue;

				if (city.parentZone != null && city.parentZone.isPlayerCity()) {

                    //list Player cities
                    //open city, just list
                    if (pc.getAccount().status.equals(AccountStatus.ADMIN)) {
                        cities.add(city);
                    } else if (city.open && city.getTOL() != null && city.getTOL().getRank() > 4) {

                        if (!BuildingManager.IsPlayerHostile(city.getTOL(), pc))
                            cities.add(city); //verify nation or guild is same
                    } else if (Guild.sameNationExcludeErrant(city.getGuild(), pcG))
                        cities.add(city);

				} else if (city.isNpc == 1) {
					//list NPC cities

					Guild g = city.getGuild();
					if (g == null) {
						if (city.isNpc == 1)
							if (city.isNoobIsle == 1) {
								if (pc.getLevel() < 21)
									cities.add(city);
							} else if (pc.getLevel() > 9)
								cities.add(city);
					} else if (pc.getLevel() >= g.getRepledgeMin() && pc.getLevel() <= g.getRepledgeMax()){

						cities.add(city);
					}
				}
			}
		}
		return cities;
	}

	public boolean isOpen() {
		return open;
	}

	public static void loadCities(Zone zone) {

		ArrayList<City> cities = DbManager.CityQueries.GET_CITIES_BY_ZONE(zone.getObjectUUID());

		for (City city : cities) {

			city.setParent(zone);
			city.setObjectTypeMask(MBServerStatics.MASK_CITY);
            city.setLoc(city.location);
            
            //not player city, must be npc city..
            if (!zone.isPlayerCity())
            	zone.setNPCCity(true);
            
			if ((ConfigManager.serverType.equals(ServerType.WORLDSERVER)) && (city.hash == null)) {

				city.setHash();

				if (DataWarehouse.recordExists(Enum.DataRecordType.CITY, city.getObjectUUID()) == false) {
					CityRecord cityRecord = CityRecord.borrow(city, Enum.RecordEventType.CREATE);
					DataWarehouse.pushToWarehouse(cityRecord);
				}
			}
		}
	}



	@Override
	public void updateDatabase() {
		// TODO Create update logic.
	}

	public static City getCity(int cityId) {

		if (cityId == 0)
			return null;

		City city = (City) DbManager.getFromCache(Enum.GameObjectType.City, cityId);
		if (city != null)
			return city;

		return DbManager.CityQueries.GET_CITY(cityId);

	}
	public static City GetCityFromCache(int cityId) {

		if (cityId == 0)
			return null;

		return (City) DbManager.getFromCache(Enum.GameObjectType.City, cityId);
	}

	@Override
	public void runAfterLoad() {

		// Set city bounds
		// *** Note: Moved to SetParent()
		//     for some undocumented reason

		// Set city motto to current guild motto

		if (BuildingManager.getBuilding(this.treeOfLifeID) == null)
			Logger.info( "City UID " + this.getObjectUUID() + " Failed to Load Tree of Life with ID " + this.treeOfLifeID);

		if ((ConfigManager.serverType.equals(ServerType.WORLDSERVER))
				&& (this.isNpc == (byte) 0)) {

			Realm wsr = Realm.getRealm(this.realmID);

			if (wsr != null)
				wsr.addCity(this.getObjectUUID());
			else
				Logger.error("Unable to find realm of ID " + realmID + " for city " + this.getObjectUUID());
		}

		if (this.getGuild() != null) {
			this.motto = this.getGuild().getMotto();

			// Determine if this city is a nation capitol

			if (this.getGuild().getGuildState() == GuildState.Nation)
				for (Guild sub : this.getGuild().getSubGuildList()) {

					if ( (sub.getGuildState() == GuildState.Protectorate) ||
							(sub.getGuildState() == GuildState.Province))
						this.isCapital = 1;
				}

			ArrayList<PlayerCharacter> guildList = Guild.GuildRoster(this.getGuild());

			this.population = guildList.size();
		}

		// Banes are loaded for this city from the database at this point

		if (this.getBane() == null)
			return;

		// if this city is baned, add the siege effect

		try {
			this.getTOL().addEffectBit((1 << 16));
			this.getBane().getStone().addEffectBit((1 << 19));;
		}catch(Exception e){
			Logger.info("Failed ao add bane effects on city." + e.getMessage());
		}
	}

	public void addCityEffect(EffectsBase effectBase, int rank) {

		HashSet<AbstractWorldObject> currentPlayers;
		PlayerCharacter player;

		// Add this new effect to the current city effect collection.
		// so any new player to the grid will have all effects applied

		this.addEffectNoTimer(Integer.toString(effectBase.getUUID()), effectBase, rank, false);

		// Any players currently in the zone will not be processed by the heartbeat
		// if it's not the first effect toggled so we do it here manually

		currentPlayers = WorldGrid.getObjectsInRangePartial(this.location, this.parentZone.getBounds().getHalfExtents().x * 1.2f, MBServerStatics.MASK_PLAYER);

		for (AbstractWorldObject playerObject : currentPlayers) {

			if (playerObject == null)
				continue;
			if (!this.isLocationOnCityZone(playerObject.getLoc()))
				continue;

			player = (PlayerCharacter) playerObject;
			player.addCityEffect(Integer.toString(effectBase.getUUID()), effectBase, rank, MBServerStatics.FOURTYFIVE_SECONDS, true,this);
		}

	}

	public void removeCityEffect(EffectsBase effectBase, int rank, boolean refreshEffect) {


		PlayerCharacter player;

		// Remove the city effect from the ago's internal collection

		if (this.getEffects().containsKey(Integer.toString(effectBase.getUUID())))
			this.getEffects().remove(Integer.toString(effectBase.getUUID()));

		// Any players currently in the zone will not be processed by the heartbeat
		// so we do it here manually


		for (Integer playerID : this._playerMemory) {

			player = PlayerCharacter.getFromCache(playerID);
			if (player == null)
				continue;

			player.endEffectNoPower(Integer.toString(effectBase.getUUID()));

			// Reapply effect with timeout?

			if (refreshEffect == true)
				player.addCityEffect(Integer.toString(effectBase.getUUID()), effectBase, rank, MBServerStatics.FOURTYFIVE_SECONDS, false,this);

		}

	}

	public Warehouse getWarehouse() {
		if (this.warehouseBuildingID == 0)
			return null;
		return Warehouse.warehouseByBuildingUUID.get(this.warehouseBuildingID);
	}

	public Realm getRealm() {

		return Realm.getRealm(this.realmID);

	}

	public boolean isLocationOnCityGrid(Vector3fImmutable insideLoc) {

		Bounds newBounds = Bounds.borrow();
		newBounds.setBounds(insideLoc);
		boolean collided = Bounds.collide(this.getBounds(), newBounds,0);
		newBounds.release();
		return collided;
	}
	
	public boolean isLocationOnCityGrid(Bounds newBounds) {

		boolean collided = Bounds.collide(this.getBounds(), newBounds,0);
		return collided;
	}

	public boolean isLocationWithinSiegeBounds(Vector3fImmutable insideLoc) {

		return insideLoc.isInsideCircle(this.getLoc(), CityBoundsType.SIEGE.extents);

	}

	public boolean isLocationOnCityZone(Vector3fImmutable insideLoc) {
		return Bounds.collide(insideLoc, this.parentZone.getBounds());
	}

	private void applyAllCityEffects(PlayerCharacter player) {

		Effect effect;
		EffectsBase effectBase;

		try {
			for (String cityEffect : this.getEffects().keySet()) {

				effect = this.getEffects().get(cityEffect);
				effectBase = effect.getEffectsBase();

				if (effectBase == null)
					continue;

				player.addCityEffect(Integer.toString(effectBase.getUUID()), effectBase, effect.getTrains(), MBServerStatics.FOURTYFIVE_SECONDS, true,this);
			}
		} catch (Exception e) {
			Logger.error( e.getMessage());
		}

	}

	private void removeAllCityEffects(PlayerCharacter player,boolean force) {

		Effect effect;
		EffectsBase effectBase;

		try {
			for (String cityEffect : this.getEffects().keySet()) {

				effect = this.getEffects().get(cityEffect);
				effectBase = effect.getEffectsBase();

				if (player.getEffects().get(cityEffect) == null)
					return;

				//                player.endEffectNoPower(cityEffect);
				player.addCityEffect(Integer.toString(effectBase.getUUID()), effectBase, effect.getTrains(), MBServerStatics.FOURTYFIVE_SECONDS, false,this);
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
	}

	public void onEnter() {

		HashSet<AbstractWorldObject> currentPlayers;
		HashSet<Integer> currentMemory;
		PlayerCharacter player;

		// Gather current list of players within a distance defined by the seige bounds

		currentPlayers = WorldGrid.getObjectsInRangePartial(this.location, CityBoundsType.SIEGE.extents, MBServerStatics.MASK_PLAYER);
		currentMemory = new HashSet<>();

		for (AbstractWorldObject playerObject : currentPlayers) {

			if (playerObject == null)
				continue;

			player = (PlayerCharacter) playerObject;
			currentMemory.add(player.getObjectUUID());

			// Player is already in our memory

			if (_playerMemory.contains(player.getObjectUUID()))
				continue;

			if (!this.isLocationOnCityZone(player.getLoc()))
				continue;
			// Apply safehold affect to player if needed

			if ((this.isSafeHold == 1))
				player.setSafeZone(true);

			//add spire effects.
			if (this.getEffects().size() > 0)
				this.applyAllCityEffects(player);

			// Add player to our city's memory

			_playerMemory.add(player.getObjectUUID());

			// ***For debugging
			// Logger.info("PlayerMemory for ", this.getCityName() + ": " + _playerMemory.size());
		}
		try {
			onExit(currentMemory);
		} catch (Exception e) {
			Logger.error( e.getMessage());
		}

	}

	/* All characters in city player memory but
	 * not the current memory have obviously
	 * left the city.  Remove their affects.
	 */

	private void onExit(HashSet<Integer> currentMemory) {

		PlayerCharacter player;
		int playerUUID = 0;
		HashSet<Integer> toRemove = new HashSet<>();
		Iterator<Integer> iter = _playerMemory.iterator();

		while (iter.hasNext()) {

			playerUUID = iter.next();



			player = PlayerCharacter.getFromCache(playerUUID);
			if (this.isLocationOnCityZone(player.getLoc()))
				continue;

			// Remove players safezone status if warranted
			// they can assumed to be not on the citygrid at
			// this point.


			player.setSafeZone(false);

			this.removeAllCityEffects(player,false);

			// We will remove this player after iteration is complete
			// so store it in a temporary collection

			toRemove.add(playerUUID);

			// ***For debugging
			// Logger.info("PlayerMemory for ", this.getCityName() + ": " + _playerMemory.size());
		}

		// Remove players from city memory

		_playerMemory.removeAll(toRemove);
	}

	public int getWarehouseBuildingID() {
		return warehouseBuildingID;
	}

	public void setWarehouseBuildingID(int warehouseBuildingID) {
		this.warehouseBuildingID = warehouseBuildingID;
	}

	public final void destroy() {

		Thread destroyCityThread = new Thread(new DestroyCityThread(this));

		destroyCityThread.setName("deestroyCity:" + this.getName());
		destroyCityThread.start();
	}

	public final void transfer(AbstractCharacter newOwner) {

		Thread transferCityThread = new Thread(new TransferCityThread(this, newOwner));

		transferCityThread.setName("TransferCity:" + this.getName());
		transferCityThread.start();
	}

	public final void claim(AbstractCharacter sourcePlayer) {

		Guild sourceNation;
		Guild sourceGuild;
		Zone cityZone;

		sourceGuild = sourcePlayer.getGuild();

		if (sourceGuild == null)
			return;

		sourceNation = sourcePlayer.getGuild().getNation();

		if (sourceGuild.isErrant())
			return;

		//cant claim tree with owned tree.

		if (sourceGuild.getOwnedCity() != null)
			return;

		cityZone = this.parentZone;

		// Can't claim a tree not in a player city zone

		// Reset sieges withstood

		this.setSiegesWithstood(0);

		this.hasBeenTransfered = true;

		// If currently a sub of another guild, desub when
		// claiming your new tree and set as Landed

		if (!sourceNation.isErrant() && sourceNation != sourceGuild) {
			if (!DbManager.GuildQueries.UPDATE_PARENT(sourceGuild.getObjectUUID(), MBServerStatics.worldUUID)) {
				ChatManager.chatGuildError((PlayerCharacter) sourcePlayer, "A Serious error has occurred. Please post details for to ensure transaction integrity");
				return;
			}

			sourceNation.getSubGuildList().remove(sourceGuild);

			if (sourceNation.getSubGuildList().isEmpty())
				sourceNation.downgradeGuildState();
		}

		// Link the mew guild with the tree

		if (!DbManager.GuildQueries.SET_GUILD_OWNED_CITY(sourceGuild.getObjectUUID(), this.getObjectUUID())) {
			ChatManager.chatGuildError((PlayerCharacter) sourcePlayer, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			return;
		}

		sourceGuild.setCityUUID(this.getObjectUUID());

		sourceGuild.setNation(sourceGuild);
		sourceGuild.setGuildState(GuildState.Sovereign);
		GuildManager.updateAllGuildTags(sourceGuild);
		GuildManager.updateAllGuildBinds(sourceGuild, this);

		// Build list of buildings within this parent zone

		for (Building cityBuilding : cityZone.zoneBuildingSet) {

			// Buildings without blueprints are unclaimable

			if (cityBuilding.getBlueprintUUID() == 0)
				continue;

			// All protection contracts are void upon transfer of a city

			// All protection contracts are void upon transfer of a city
			//Dont forget to not Flip protection on Banestones and siege Equipment... Noob.
			if (cityBuilding.getBlueprint() != null && !cityBuilding.getBlueprint().isSiegeEquip()
					&& cityBuilding.getBlueprint().getBuildingGroup() != BuildingGroup.BANESTONE)
				cityBuilding.setProtectionState(ProtectionState.NONE);

			// Transfer ownership of valid city assets
			// these assets are autoprotected.

			if ((cityBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.TOL)
					|| (cityBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.SPIRE)
					|| (cityBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.BARRACK)
					|| (cityBuilding.getBlueprint().isWallPiece())
					|| (cityBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE)) {

				cityBuilding.claim(sourcePlayer);
				cityBuilding.setProtectionState(ProtectionState.PROTECTED);
			}
		}

		this.setForceRename(true);

		// Reset city timer for map update

		City.lastCityUpdate = System.currentTimeMillis();
	}

	public final boolean transferGuildLeader(PlayerCharacter sourcePlayer) {

		Guild sourceGuild;
		Zone cityZone;
		sourceGuild = sourcePlayer.getGuild();


		if (sourceGuild == null)
			return false;

		if (sourceGuild.isErrant())
			return false;

		cityZone = this.parentZone;

		for (Building cityBuilding : cityZone.zoneBuildingSet) {

			// Buildings without blueprints are unclaimable

			if (cityBuilding.getBlueprintUUID() == 0)
				continue;

			// All protection contracts are void upon transfer of a city
			//Dont forget to not Flip protection on Banestones and siege Equipment... Noob.

			// Transfer ownership of valid city assets
			// these assets are autoprotected.

			if ((cityBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.TOL)
					|| (cityBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.SPIRE)
					|| (cityBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.BARRACK)
					|| (cityBuilding.getBlueprint().isWallPiece())
					|| (cityBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE)
					) {

				cityBuilding.claim(sourcePlayer);
				cityBuilding.setProtectionState(ProtectionState.PROTECTED);
			} else if(cityBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.WAREHOUSE)
				cityBuilding.claim(sourcePlayer);


		}
		this.setForceRename(true);
		CityRecord cityRecord = CityRecord.borrow(this, Enum.RecordEventType.TRANSFER);
		DataWarehouse.pushToWarehouse(cityRecord);
		return true;

	}

	/**
	 * @return the forceRename
	 */
	public boolean isForceRename() {
		return forceRename;
	}

	/**
	 * @param siegesWithstood the siegesWithstood to set
	 */
	public void setSiegesWithstood(int siegesWithstood) {

		// early exit if setting to current value

		if (this.siegesWithstood == siegesWithstood)
			return;

		if (DbManager.CityQueries.updateSiegesWithstood(this, siegesWithstood) == true)
			this.siegesWithstood = siegesWithstood;
		else
			Logger.error("Error when writing to database for cityUUID: " + this.getObjectUUID());
	}

	/**
	 * @param population the population to set
	 */
	public void setPopulation(int population) {
		this.population = population;
	}

	public boolean isReverseKOS() {
		return reverseKOS;
	}

	public void setReverseKOS(boolean reverseKOS) {
		this.reverseKOS = reverseKOS;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setHash() {

		this.hash = DataWarehouse.hasher.encrypt(this.getObjectUUID());

		// Write hash to player character table

		DataWarehouse.writeHash(Enum.DataRecordType.CITY, this.getObjectUUID());
	}

	public boolean setRealmTaxDate(LocalDateTime realmTaxDate) {

		if (!DbManager.CityQueries.updateRealmTaxDate(this, realmTaxDate))
			return false;

		this.realmTaxDate = realmTaxDate;
		return true;

	}

	//TODO use this for taxing later.
//	public boolean isAfterTaxPeriod(LocalDateTime dateTime,PlayerCharacter player){
//		if (dateTime.isBefore(realmTaxDate)){
//			String wait = "";
//			float hours = 1000*60*60;
//			float seconds = 1000;
//			float hoursUntil = realmTaxDate.minus(dateTime.get).getMillis() /hours;
//			int secondsUntil = (int) (realmTaxDate.minus(dateTime.getMillis()).getMillis() /seconds);
//			if (hoursUntil < 1)
//				wait = "You must wait " + secondsUntil + " seconds before taxing this city again!";
//			else
//				wait = "You must wait " + hoursUntil + " hours before taxing this city again!";
//			ErrorPopupMsg.sendErrorMsg(player, wait);
//			return false;
//		}
//
//		return true;
//	}

	

	public synchronized boolean TaxWarehouse(TaxResourcesMsg msg,PlayerCharacter player) {

		// Member variable declaration
		Building building = BuildingManager.getBuildingFromCache(msg.getBuildingID());
		Guild playerGuild = player.getGuild();

		if (building == null){
			ErrorPopupMsg.sendErrorMsg(player, "Not a valid Building!");
			return true;
		}

		City city = building.getCity();
		if (city == null){
			ErrorPopupMsg.sendErrorMsg(player, "This building does not belong to a city.");
			return true;
		}


		if (playerGuild == null || playerGuild.isErrant()){
			ErrorPopupMsg.sendErrorMsg(player, "You must belong to a guild to do that!");
			return true;
		}

		if (playerGuild.getOwnedCity() == null){
			ErrorPopupMsg.sendErrorMsg(player, "Your Guild needs to own a city!");
			return true;
		}

		if (playerGuild.getOwnedCity().getTOL() == null){
			ErrorPopupMsg.sendErrorMsg(player, "Cannot find Tree of Life for your city!");
			return true;
		}

		if (playerGuild.getOwnedCity().getTOL().getRank() != 8){
			ErrorPopupMsg.sendErrorMsg(player, "Your City needs to Own a realm!");
			return true;
		}

		if (playerGuild.getOwnedCity().getRealm() == null){
			ErrorPopupMsg.sendErrorMsg(player, "Cannot find realm for your city!");
			return true;
		}
		Realm targetRealm = RealmMap.getRealmForCity(city);

		if (targetRealm == null){
			ErrorPopupMsg.sendErrorMsg(player, "Cannot find realm for city you are attempting to tax!");
			return true;
		}

		if (targetRealm.getRulingCity() == null){
			ErrorPopupMsg.sendErrorMsg(player, "Realm Does not have a ruling city!");
			return true;
		}

		if (targetRealm.getRulingCity().getObjectUUID() != playerGuild.getOwnedCity().getObjectUUID()){
			ErrorPopupMsg.sendErrorMsg(player, "Your guild does not rule this realm!");
			return true;
		}

		if (playerGuild.getOwnedCity().getObjectUUID() == city.getObjectUUID()){
			ErrorPopupMsg.sendErrorMsg(player, "You cannot tax your own city!");
			return true;
		}




		if (!GuildStatusController.isTaxCollector(player.getGuildStatus())){
			ErrorPopupMsg.sendErrorMsg(player, "You Must be a tax Collector!");
			return true;
		}


		
		if (this.realmTaxDate.isAfter(LocalDateTime.now()))
			return true;
		if (msg.getResources().size() == 0)
			return true;

		if (city.getWarehouse() == null)
			return true;
		Warehouse ruledWarehouse = playerGuild.getOwnedCity().getWarehouse();
		if (ruledWarehouse == null)
			return true;



		ItemBase.getItemHashIDMap();

		ArrayList<Integer>resources = new ArrayList<>();

		float taxPercent = msg.getTaxPercent();
		if (taxPercent > 20)
			taxPercent = .20f;

		for (int resourceHash:msg.getResources().keySet()){
			if (ItemBase.getItemHashIDMap().get(resourceHash) != null)
				resources.add(ItemBase.getItemHashIDMap().get(resourceHash));

		}

		for (Integer itemBaseID:resources){
			ItemBase ib = ItemBase.getItemBase(itemBaseID);
			if (ib == null)
				continue;
			if (ruledWarehouse.isAboveCap(ib, (int) (city.getWarehouse().getResources().get(ib) * taxPercent))){
				ErrorPopupMsg.sendErrorMsg(player, "You're warehouse has enough " + ib.getName() + " already!");
				return true;
			}

		}

		if(!city.setRealmTaxDate(LocalDateTime.now().plusDays(7))){
			ErrorPopupMsg.sendErrorMsg(player, "Failed to Update next Tax Date due to internal Error. City was not charged taxes this time.");
			return false;
		}
		try{
			city.getWarehouse().transferResources(player,msg,resources, taxPercent,ruledWarehouse);
		}catch(Exception e){
			Logger.info( e.getMessage());
		}

		// Member variable assignment

		ViewResourcesMessage vrm = new ViewResourcesMessage(player);
		vrm.setGuild(building.getGuild());
		vrm.setWarehouseBuilding(BuildingManager.getBuildingFromCache(building.getCity().getWarehouse().getBuildingUID()));
		vrm.configure();
		Dispatch dispatch = Dispatch.borrow(player, vrm);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		dispatch = Dispatch.borrow(player, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		return true;

	}
}
