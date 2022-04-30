// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.objects;

import engine.Enum;
import engine.InterestManagement.RealmMap;
import engine.db.archive.DataWarehouse;
import engine.db.archive.RealmRecord;
import engine.gameManager.DbManager;
import engine.gameManager.PowersManager;
import engine.net.ByteBufferWriter;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import static engine.Enum.CharterType;


public class Realm {

	// Internal class cache

	private static ConcurrentHashMap<Integer, Realm> _realms = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

	private final float mapR; //Red color
	private final float mapG; //Green color
	private final float mapB; //Blue color
	private final float mapA; //Alpha color
	private final boolean canBeClaimed;
	private final boolean canPlaceCities;
	private final int numCities;
	private final String realmName;
	private int rulingCityUUID;
	private int rulingCharacterUUID;
	private int rulingCharacterOrdinal;
	private String rulingCharacterName;
	private int rulingNationUUID;
	private GuildTag rulingNationTags;
	private String rulingNationName;
	private int charterType;
	public LocalDateTime ruledSince;
	private final float mapY1;
	private final float mapX1;
	private final float mapY2;
	private final float mapX2;
	private final int stretchX;
	private final int stretchY;
	private final int locX;
	private final int locY;
	private final int realmID;
	private final HashSet<Integer> cities = new HashSet<>();
	private String hash;

	/**
	 * ResultSet Constructor
	 */
	public Realm(ResultSet rs) throws SQLException, UnknownHostException {

		this.mapR = rs.getFloat("mapR");
		this.mapG = rs.getFloat("mapG");
		this.mapB = rs.getFloat("mapB");
		this.mapA = rs.getFloat("mapA");
		this.canBeClaimed = rs.getBoolean("canBeClaimed");
		this.canPlaceCities = rs.getBoolean("canPlaceCities");
		this.numCities = rs.getInt("numCities");
		this.realmName = rs.getString("realmName");
		this.rulingCityUUID = rs.getInt("rulingCityUID");
		this.charterType = rs.getInt("charterType");

		java.sql.Timestamp ruledTimeStamp = rs.getTimestamp("ruledSince");

		if (ruledTimeStamp != null)
			this.ruledSince = LocalDateTime.ofInstant(ruledTimeStamp.toInstant(), ZoneId.systemDefault());

		this.mapY1 = rs.getFloat("mapY1");
		this.mapX1 = rs.getFloat("mapX1");
		this.mapY2 = rs.getFloat("mapY2");
		this.mapX2 = rs.getFloat("mapX2");
		this.stretchX = rs.getInt("stretchX");
		this.stretchY = rs.getInt("stretchY");
		this.locX = rs.getInt("locX");
		this.locY = rs.getInt("locY");
		this.realmID = rs.getInt("realmID");
		this.hash = rs.getString("hash");
	}

	/*
	 * Getters
	 */
	public boolean isRuled() {
		return (this.rulingCityUUID != 0);
	}

	public float getMapR() {
		return this.mapR;
	}

	public float getMapG() {
		return this.mapG;
	}

	public float getMapB() {
		return this.mapB;
	}

	public float getMapA() {
		return this.mapA;
	}

	public boolean getCanBeClaimed() {
		return this.canBeClaimed;
	}

	public boolean getCanPlaceCities() {
		return this.canPlaceCities;
	}

	public int getNumCities() {
		return this.numCities;
	}

	public String getRealmName() {
		return this.realmName;
	}

	public City getRulingCity() {
		return City.getCity(this.rulingCityUUID);
	}

	public float getMapY1() {
		return this.mapY1;
	}

	public float getMapX1() {
		return this.mapX1;
	}

	public float getMapY2() {
		return this.mapY2;
	}

	public float getMapX2() {
		return this.mapX2;
	}

	public int getStretchX() {
		return this.stretchX;
	}

	public int getStretchY() {
		return this.stretchY;
	}

	public int getlocX() {
		return this.locX;
	}

	public int getlocY() {
		return this.locY;
	}

	public int getRealmID() {
		return this.realmID;
	}

	public void addCity(int cityUUID) {
		if (!this.cities.add(cityUUID))
			this.cities.add(cityUUID);
	}

	public void removeCity(int cityUUID) {
		this.cities.remove(cityUUID);
	}

	public boolean isRealmFull() {

		return this.cities.size() >= this.numCities;
	}

	public boolean isRealmFullAfterBane() {

		return this.cities.size() > this.numCities;
	}

	public static void configureAllRealms() {

		Realm serverRealm;
		int realmID;

		for (Enum.RealmType realmType : Enum.RealmType.values()) {

			realmID = realmType.getRealmID();
			// Don't serialize seafloor

			if (realmID == 0)
				continue;

			serverRealm = Realm.getRealm(realmID);
			serverRealm.configure();

		}
	}

	// Call this after changing ownership before you serialize a realm

	public void configure() {

		PlayerCharacter rulingCharacter;

		// Configure what exactly?  We won't send any of it.

		if (this.rulingCityUUID == 0)
			return;
		if (this.getRulingCity() == null)
			return;
		if (this.getRulingCity().getTOL() == null)
			return;

		rulingCharacter = PlayerCharacter.getPlayerCharacter(this.getRulingCity().getTOL().getOwnerUUID());
		if (rulingCharacter == null){
			Logger.info( this.realmName + " failed to load " + this.getRulingCity().getCityName() + " ID : " + this.rulingCityUUID);
			return;
		}

		this.rulingCharacterUUID = rulingCharacter.getObjectUUID();
		this.rulingCharacterOrdinal = rulingCharacter.getObjectType().ordinal();
		this.rulingCharacterName = rulingCharacter.getFirstName() + ' ' + rulingCharacter.getLastName();
		this.rulingNationUUID = rulingCharacter.getGuild().getNation().getObjectUUID();
		this.rulingNationName = rulingCharacter.getGuild().getNation().getName();
		this.rulingNationTags = rulingCharacter.getGuild().getNation().getGuildTag();
	}

	public void serializeForClientMsg(ByteBufferWriter writer) {

		writer.putFloat(this.mapR);
		writer.putFloat(this.mapG);
		writer.putFloat(this.mapB);
		writer.putFloat(this.mapA);
		writer.put((byte) (this.canBeClaimed ? 0x1 : 0x0));
		writer.put((byte) (this.canPlaceCities ? 0x1 : 0x0));
		writer.putInt(this.numCities);
		writer.putFloat(this.mapR);
		writer.putFloat(this.mapG);
		writer.putFloat(this.mapB);
		writer.putFloat(this.mapA);
		writer.putString(this.realmName);

		if (isRuled() == true) {
			writer.putInt(Enum.GameObjectType.Guild.ordinal());
			writer.putInt(rulingNationUUID);

			writer.putInt(rulingCharacterOrdinal);
			writer.putInt(rulingCharacterUUID);

			writer.putInt(Enum.GameObjectType.City.ordinal());
			writer.putInt(rulingCityUUID);

			writer.putLocalDateTime(this.ruledSince);

			writer.putString(rulingNationName);
			GuildTag._serializeForDisplay(rulingNationTags,writer);
			writer.putString(rulingCharacterName);
			writer.putInt(0xB); // Display Title: enum index starts at 10.
		} else {
			if (this.rulingCityUUID != 0)
				Logger.error( "Failed to Load realm info for city" + this.rulingCityUUID);
			writer.putLong(0);
			writer.putLong(0);
			writer.putLong(0);
			writer.put((byte) 1);
			writer.put((byte) 0);
			writer.putInt(0x64);
			writer.put((byte) 0);
			writer.put((byte) 0);
			writer.put((byte) 0);
			writer.putInt(0);
			writer.putInt(0x10);
			writer.putInt(0x10);
			writer.putInt(0x10);
			writer.putInt(0x0);
			writer.putInt(0x0);
			writer.putInt(0);
			writer.putInt(0);
		}
		writer.putInt(0); // Male/Female
		writer.putInt(this.charterType); // Charter Type
		writer.putFloat(this.mapY1);
		writer.putFloat(this.mapX1);
		writer.putFloat(this.mapY2);
		writer.putFloat(this.mapX2);
		writer.putInt(this.stretchX);
		writer.putInt(this.stretchY);
		writer.putInt(this.locX);
		writer.putInt(this.locY);
	}

	public void updateDatabase() {
		DbManager.RealmQueries.REALM_UPDATE(this);
	}

	public static Realm getRealm(int realmID) {
		return _realms.get(realmID);
	}

	/**
	 * @return the charterType
	 */
	public int getCharterType() {
		return charterType;
	}

	/**
	 * @param charterType the charterType to set
	 */
	public void setCharterType(int charterType) {
		this.charterType = charterType;
	}

	public void abandonRealm() {

		// Push event to warehouse

		RealmRecord realmRecord = RealmRecord.borrow(this, Enum.RecordEventType.LOST);
		DataWarehouse.pushToWarehouse(realmRecord);

		// No longer own a realm
		this.getRulingCity().getGuild().setRealmsOwned(this.getRulingCity().getGuild().getRealmsOwned() - 1);
		if (!this.getRulingCity().getGuild().getNation().equals(this.getRulingCity().getGuild()))
			this.getRulingCity().getGuild().getNation().setRealmsOwned(this.getRulingCity().getGuild().getNation().getRealmsOwned() - 1);

		// Configure realm
		this.charterType = 0;
		this.rulingCityUUID = 0;
		this.ruledSince = null;

		this.updateDatabase();
	}

	public void claimRealmForCity(City city, int charterType) {

		// Configure realm
		this.charterType = charterType;
		this.rulingCityUUID = city.getObjectUUID();
		this.ruledSince = LocalDateTime.now();
		this.configure();
		this.updateDatabase();

		// Push event to warehouse

		RealmRecord realmRecord = RealmRecord.borrow(this, Enum.RecordEventType.CAPTURE);
		DataWarehouse.pushToWarehouse(realmRecord);

	}

	public static boolean HasAllBlessings(PlayerCharacter claimer) {

		if (claimer == null)
			return false;

		PowersBase powerBlessing = PowersManager.getPowerByIDString("BLS-POWER");
		if (!claimer.effects.containsKey(Integer.toString(powerBlessing.getActions().get(0).getUUID())))
			return false;
		PowersBase wisdomBlessing = PowersManager.getPowerByIDString("BLS-POWER");
		if (!claimer.effects.containsKey(Integer.toString(wisdomBlessing.getActions().get(0).getUUID())))
			return false;
		PowersBase fortuneBlessing = PowersManager.getPowerByIDString("BLS-FORTUNE");
		return claimer.effects.containsKey(Integer.toString(fortuneBlessing.getActions().get(0).getUUID()));
	}

	public static float getRealmHealthMod(City city) {
		Realm serverRealm;
		int charterType;
		float returnBonus = 0.0f;

		serverRealm = RealmMap.getRealmForCity(city);
		charterType = serverRealm.charterType;

		switch (charterType) {

		case 762228431:
			returnBonus = 0.0f;
			break;
		case -15978914:
			returnBonus = -.15f;
			break;
		case -600065291:
			returnBonus = .15f;
			break;
		default:
			break;
		}

		return returnBonus;
	}

	public static int getRealmMesh(City city) {
		Realm serverRealm;
		CharterType charterType;

		serverRealm = city.getRealm();
		charterType = CharterType.getCharterTypeByID(serverRealm.charterType);

		return charterType.getMeshID();
	}

	public static void loadAllRealms() {

		_realms = DbManager.RealmQueries.LOAD_ALL_REALMS();

	}

	public String getHash() {
		return hash;
	}

	public void setHash() {

		this.hash = DataWarehouse.hasher.encrypt(this.realmID);

		// Write hash to player character table

		DataWarehouse.writeHash(Enum.DataRecordType.REALM, this.realmID);
	}

	/* *** Keeping around in case needed for server wipe or some other emergency

    public static void backfillRealms() {

        // Backfill realm records

        for (Realm realm : _realms.values()) {

            realm.setHash();

            if ( (realm.isRuled() == true) &&
                    (DataWarehouse.recordExists(Enum.DataRecordType.REALM, realm.getRealmID()) == false)) {
                RealmRecord realmRecord = RealmRecord.borrow(realm, Enum.RecordEventType.CAPTURE);
                DataWarehouse.pushToWarehouse(realmRecord);
            }

        }
    }
	 */
}
