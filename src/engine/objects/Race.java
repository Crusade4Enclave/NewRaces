// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum;
import engine.Enum.RaceType;
import engine.gameManager.DbManager;
import engine.net.ByteBufferWriter;
import engine.server.MBServerStatics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Race {

	// Local class cache

	private static ConcurrentHashMap<Integer, Race> _raceByID = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

	private final String name;
	private final String description;
	private final int raceRuneID;
	private Enum.RaceType raceType;
	private final short strStart;
	private final short strMin;
	private final short strMax;

	private final short dexStart;
	private final short dexMin;
	private final short dexMax;

	private final short conStart;
	private final short conMin;
	private final short conMax;

	private final short intStart;
	private final short intMin;
	private final short intMax;

	private final short spiStart;
	private final short spiMin;
	private final short spiMax;

	private final short healthBonus;
	private final short manaBonus;
	private final short staminaBonus;

	private final byte startingPoints;

	private final float minHeight;
	private final float strHeightMod;

	private int token = 0;

	private HashSet<Integer> hairStyles;
	private HashSet<Integer> beardStyles;

	private final HashSet<Integer> skinColors;
	private final HashSet<Integer> beardColors;
	private final HashSet<Integer> hairColors;

	private final ArrayList<BaseClass> baseClasses;
	private ArrayList<MobBaseEffects> effectsList = new ArrayList<>();


	private final ArrayList<SkillReq> skillsGranted;
	private final ArrayList<PowerReq> powersGranted;

	public static void loadAllRaces() {
		Race._raceByID = DbManager.RaceQueries.LOAD_ALL_RACES();
	}


	@Override
	public boolean equals(Object object) {

		if ((object instanceof Race) == false)
			return false;

		Race race = (Race) object;

		return this.raceRuneID == race.raceRuneID;
	}

	@Override
	public int hashCode() {
		return this.raceRuneID;
	}

	/**
	 * ResultSet Constructor
	 */
	public Race(ResultSet rs) throws SQLException {

		this.raceRuneID = rs.getInt("ID");
		this.raceType = Enum.RaceType.getRaceTypebyRuneID(raceRuneID);
		this.name = rs.getString("name");
		this.description = rs.getString("description");
		this.strStart = rs.getShort("strStart");
		this.strMin = rs.getShort("strMin");
		this.strMax = rs.getShort("strMax");
		this.dexStart = rs.getShort("dexStart");
		this.dexMin = rs.getShort("dexMin");
		this.dexMax = rs.getShort("dexMax");
		this.conStart = rs.getShort("conStart");
		this.conMin = rs.getShort("conMin");
		this.conMax = rs.getShort("conMax");
		this.intStart = rs.getShort("intStart");
		this.intMin = rs.getShort("intMin");
		this.intMax = rs.getShort("intMax");
		this.spiStart = rs.getShort("spiStart");
		this.spiMin = rs.getShort("spiMin");
		this.spiMax = rs.getShort("spiMax");
		this.token = rs.getInt("token");
		this.healthBonus = rs.getShort("healthBonus");
		this.manaBonus = rs.getShort("manaBonus");
		this.staminaBonus = rs.getShort("staminaBonus");
		this.startingPoints = rs.getByte("startingPoints");
		this.raceType = RaceType.getRaceTypebyRuneID(this.raceRuneID);
		this.minHeight = rs.getFloat("minHeight");
		this.strHeightMod = rs.getFloat("strHeightMod");
		this.hairStyles = DbManager.RaceQueries.HAIR_STYLES_FOR_RACE(raceRuneID);
		this.beardStyles = DbManager.RaceQueries.BEARD_STYLES_FOR_RACE(raceRuneID);
		this.skinColors = DbManager.RaceQueries.SKIN_COLOR_FOR_RACE(raceRuneID);
		this.beardColors = DbManager.RaceQueries.BEARD_COLORS_FOR_RACE(raceRuneID);
		this.hairColors = DbManager.RaceQueries.HAIR_COLORS_FOR_RACE(raceRuneID);
		this.baseClasses = DbManager.BaseClassQueries.GET_BASECLASS_FOR_RACE(raceRuneID);
		this.skillsGranted = DbManager.SkillReqQueries.GET_REQS_FOR_RUNE(raceRuneID);
		this.powersGranted = PowerReq.getPowerReqsForRune(raceRuneID);
		this.effectsList = DbManager.MobBaseQueries.GET_RUNEBASE_EFFECTS(this.raceRuneID);

	}

	/*
	 * Getters
	 */
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public short getStrStart() {
		return strStart;
	}

	public short getStrMin() {
		return strMin;
	}

	public short getStrMax() {
		return strMax;
	}

	public short getDexStart() {
		return dexStart;
	}

	public short getDexMin() {
		return dexMin;
	}

	public short getDexMax() {
		return dexMax;
	}

	public short getConStart() {
		return conStart;
	}

	public short getConMin() {
		return conMin;
	}

	public short getConMax() {
		return conMax;
	}

	public short getIntStart() {
		return intStart;
	}

	public short getIntMin() {
		return intMin;
	}

	public short getIntMax() {
		return intMax;
	}

	public short getSpiStart() {
		return spiStart;
	}

	public short getSpiMin() {
		return spiMin;
	}

	public short getSpiMax() {
		return spiMax;
	}

	public byte getStartingPoints() {
		return startingPoints;
	}

	public int getToken() {
		return token;
	}

	public final HashSet<Integer> getHairStyles() {
		return hairStyles;
	}

	public final HashSet<Integer> getBeardStyles() {
		return beardStyles;
	}

	public final HashSet<Integer> getBeardColors() {
		return beardColors;
	}

	public HashSet<Integer> getHairColors() {
		return hairColors;
	}

	public HashSet<Integer> getSkinColors() {
		return skinColors;
	}

	public int getNumSkinColors() {
		return this.skinColors.size();
	}

	public int getNumHairColors() {
		return this.hairColors.size();
	}

	public int getNumBeardColors() {
		return this.beardColors.size();
	}

	public final ArrayList<BaseClass> getValidBaseClasses() {
		return baseClasses;
	}

	public ArrayList<Integer> getAllowedRunes() {
        return RuneBase.AllowedRaceRunesMap.get(raceRuneID);
	}

	public ArrayList<SkillReq> getSkillsGranted() {
		return this.skillsGranted;
	}

	public ArrayList<PowerReq> getPowersGranted() {
		return this.powersGranted;
	}

	public ArrayList<RuneBaseEffect> getEffectsGranted() {
		return RuneBaseEffect.RuneIDBaseEffectMap.get(this.raceRuneID);
	}

	/*
	 * Validators
	 */
	public boolean isValidBeardStyle(int id) {
		return this.beardStyles.contains(id);
	}

	public boolean isValidBeardColor(int id) {
		return this.beardColors.contains(id);
	}

	public boolean isValidHairStyle(int id) {
		return this.hairStyles.contains(id);
	}

	public boolean isValidHairColor(int id) {
		return this.hairColors.contains(id);
	}

	public boolean isValidSkinColor(int id) {
		return this.skinColors.contains(id);
	}

	public boolean isAllowedRune(RuneBase rb) {

		if (this.getAllowedRunes() != null)
			if (this.getAllowedRunes().contains(rb.getObjectUUID()))
				return true;
		if (RuneBase.AllowedBaseClassRunesMap.containsKey(111111)){
			if (RuneBase.AllowedRaceRunesMap.get(111111).contains(rb.getObjectUUID()))
				return true;
		}
		return false;
	}

	public float getHealthBonus() {
		return this.healthBonus;
	}

	public float getManaBonus() {
		return this.manaBonus;
	}

	public float getStaminaBonus() {
		return this.staminaBonus;
	}

	public float getMinHeight() {
		return this.minHeight;
	}

	public float getStrHeightMod() {
		return this.strHeightMod;
	}

	public float getHeight(short str) {
		return this.minHeight + (this.strHeightMod * str);
	}

	public float getCenterHeight(short str) {
		return getHeight(str) / 2f;
	}


	/*
	 * Serializing
	 */

	public void serializeForClientMsg(ByteBufferWriter writer) {
		writer.putInt(1); // For Race
		writer.putInt(0); // Pad
		writer.putInt(this.raceRuneID);

		writer.putInt(Enum.GameObjectType.Race.ordinal());
		writer.putInt(raceRuneID);
	}

	public static Race getRace(int id) {
		return _raceByID.get(id);
	}

	public int getRaceRuneID() {
		return raceRuneID;
	}

	public Enum.RaceType getRaceType() {
		return raceType;
	}


	public ArrayList<MobBaseEffects> getEffectsList() {
		return effectsList;
	}
}
