// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.DbManager;
import engine.net.ByteBufferWriter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class BaseClass extends AbstractGameObject {

	private final String name;
	private final String description;

	private final byte strMod;
	private final byte dexMod;
	private final byte conMod;
	private final byte intMod;
	private final byte spiMod;

	private final float healthMod;
	private final float manaMod;
	private final float staminaMod;

	private int token = 0;

	private final ArrayList<SkillReq> skillsGranted;
	private final ArrayList<PowerReq> powersGranted;
	private ArrayList<MobBaseEffects> effectsList = new ArrayList<>();


	/**
	 * No Table ID Constructor
	 */
	public BaseClass(String name, String description, byte strMod, byte dexMod, byte conMod, byte intMod, byte spiMod,
			ArrayList<RuneBase> allowedRunes, ArrayList<SkillReq> skillsGranted, ArrayList<PowerReq> powersGranted) {
		super();
		this.name = name;
		this.description = description;
		this.strMod = strMod;
		this.dexMod = dexMod;
		this.conMod = conMod;
		this.intMod = intMod;
		this.spiMod = spiMod;
		this.healthMod = 1;
		this.manaMod = 1;
		this.staminaMod = 1;

		this.skillsGranted = skillsGranted;
		this.powersGranted = powersGranted;

	}

	/**
	 * Normal Constructor
	 */
	public BaseClass(String name, String description, byte strMod, byte dexMod, byte conMod, byte intMod, byte spiMod,
			ArrayList<RuneBase> allowedRunes, ArrayList<SkillReq> skillsGranted, ArrayList<PowerReq> powersGranted, int newUUID) {
		super(newUUID);
		this.name = name;
		this.description = description;
		this.strMod = strMod;
		this.dexMod = dexMod;
		this.conMod = conMod;
		this.intMod = intMod;
		this.spiMod = spiMod;
		this.healthMod = 1;
		this.manaMod = 1;
		this.staminaMod = 1;
		this.skillsGranted = skillsGranted;
		this.powersGranted = powersGranted;

	}


	/**
	 * ResultSet Constructor
	 */
	public BaseClass(ResultSet rs) throws SQLException {
		super(rs);

		this.name = rs.getString("name");
		this.description = rs.getString("description");
		this.strMod = rs.getByte("strMod");
		this.dexMod = rs.getByte("dexMod");
		this.conMod = rs.getByte("conMod");
		this.intMod = rs.getByte("intMod");
		this.spiMod = rs.getByte("spiMod");
		this.token = rs.getInt("token");
		this.healthMod = rs.getInt("healthMod");
		this.manaMod = rs.getInt("manaMod");
		this.staminaMod = rs.getInt("staminaMod");
		this.skillsGranted = DbManager.SkillReqQueries.GET_REQS_FOR_RUNE(this.getObjectUUID());
		this.powersGranted = PowerReq.getPowerReqsForRune(this.getObjectUUID());
		this.effectsList = (DbManager.MobBaseQueries.GET_RUNEBASE_EFFECTS(this.getObjectUUID()));

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

	public byte getStrMod() {
		return strMod;
	}

	public byte getDexMod() {
		return dexMod;
	}

	public byte getConMod() {
		return conMod;
	}

	public byte getIntMod() {
		return intMod;
	}

	public byte getSpiMod() {
		return spiMod;
	}

	public int getToken() {
		return this.token;
	}

	public float getHealthMod() {
		return this.healthMod;
	}

	public float getManaMod() {
		return this.manaMod;
	}

	public float getStaminaMod() {
		return this.staminaMod;
	}

	public ArrayList<Integer> getRuneList() {
		return RuneBase.AllowedBaseClassRunesMap.get(this.getObjectUUID());
	}

	public ArrayList<SkillReq> getSkillsGranted() {
		return this.skillsGranted;
	}

	public ArrayList<PowerReq> getPowersGranted() {
		return this.powersGranted;
	}

	public ArrayList<RuneBaseEffect> getEffectsGranted() {
		return  RuneBaseEffect.RuneIDBaseEffectMap.get(this.getObjectUUID());
	}

	/*
	 * Utils
	 */
	public boolean isAllowedRune(RuneBase rb) {

		if (this.getRuneList().contains(rb.getObjectUUID()))
			return true;

		if (RuneBase.AllowedBaseClassRunesMap.containsKey(111111)){
			if (RuneBase.AllowedBaseClassRunesMap.get(111111).contains(rb.getObjectUUID()))
				return true;
		}
		return false;
	}

	public static void LoadAllBaseClasses(){
		DbManager.BaseClassQueries.GET_ALL_BASE_CLASSES();
	}

	/*
	 * Serializing
	 */
	
	public static void serializeForClientMsg(BaseClass baseClass, ByteBufferWriter writer) {
		serializeForClientMsg(baseClass,writer, 3);
	}

	public static void serializeForClientMsg(BaseClass baseClass,ByteBufferWriter writer, int type) {
		writer.putInt(type); // For BaseClass
		writer.putInt(0); // Pad
		writer.putInt(baseClass.getObjectUUID());
		writer.putInt(baseClass.getObjectType().ordinal()); // Is this correct?
		writer.putInt(baseClass.getObjectUUID());
	}

	public static BaseClass getBaseClass(final int UUID) {
		return DbManager.BaseClassQueries.GET_BASE_CLASS(UUID);
	}

	@Override
	public void updateDatabase() {
		; //Never update..
	}

	public ArrayList<MobBaseEffects> getEffectsList() {
		return effectsList;
	}

}
