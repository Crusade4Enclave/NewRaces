// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.GameObjectType;
import engine.gameManager.DbManager;
import engine.net.ByteBufferWriter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class PromotionClass extends AbstractGameObject {

	private final String name;
	private final String description;
	private int token = 0;

	private final float healthMod;
	private final float manaMod;
	private final float staminaMod;

	private final ArrayList<Integer> allowedRunes;
	private final ArrayList<SkillReq> skillsGranted;
	private final ArrayList<PowerReq> powersGranted;
	private final ArrayList<RuneBaseEffect> effectsGranted;
	private final ArrayList<RuneBaseEffect> effectsGrantedFighter;
	private final ArrayList<RuneBaseEffect> effectsGrantedHealer;
	private final ArrayList<RuneBaseEffect> effectsGrantedRogue;
	private final ArrayList<RuneBaseEffect> effectsGrantedMage;
	private ArrayList<MobBaseEffects> effectsList = new ArrayList<>();

	/**
	 * No Table ID Constructor
	 */
	public PromotionClass(String name, String description,
			ArrayList<Integer> allowedRunes, ArrayList<SkillReq> skillsGranted, ArrayList<PowerReq> powersGranted) {
		super();
		this.name = name;
		this.description = description;
		this.allowedRunes = allowedRunes;
		this.skillsGranted = skillsGranted;
		this.powersGranted = powersGranted;
		this.healthMod = 0f;
		this.manaMod = 0f;
		this.staminaMod = 0f;
		this.effectsGranted = new ArrayList<>();
		this.effectsGrantedFighter = new ArrayList<>();
		this.effectsGrantedHealer = new ArrayList<>();
		this.effectsGrantedRogue = new ArrayList<>();
		this.effectsGrantedMage = new ArrayList<>();
	}

	/**
	 * Normal Constructor
	 */
	public PromotionClass(String name, String description,
			ArrayList<Integer> allowedRunes, ArrayList<SkillReq> skillsGranted, ArrayList<PowerReq> powersGranted, int newUUID) {
		super(newUUID);
		this.name = name;
		this.description = description;
		this.allowedRunes = allowedRunes;
		this.skillsGranted = skillsGranted;
		this.powersGranted = powersGranted;
		this.healthMod = 0f;
		this.manaMod = 0f;
		this.staminaMod = 0f;
		this.effectsGranted = new ArrayList<>();
		this.effectsGrantedFighter = new ArrayList<>();
		this.effectsGrantedHealer = new ArrayList<>();
		this.effectsGrantedRogue = new ArrayList<>();
		this.effectsGrantedMage = new ArrayList<>();
	}

	/**
	 * ResultSet Constructor
	 */
	public PromotionClass(ResultSet rs) throws SQLException {
		super(rs);

		this.name = rs.getString("name");
		this.description = rs.getString("description");
		this.token = rs.getInt("token");
		this.healthMod = rs.getFloat("healthMod");
		this.manaMod = rs.getFloat("manaMod");
		this.staminaMod = rs.getFloat("staminaMod");
		this.allowedRunes = DbManager.PromotionQueries.GET_ALLOWED_RUNES(this);
		this.skillsGranted = DbManager.SkillReqQueries.GET_REQS_FOR_RUNE(this.getObjectUUID());
		this.powersGranted = PowerReq.getPowerReqsForRune(this.getObjectUUID());
		this.effectsGranted = DbManager.RuneBaseEffectQueries.GET_EFFECTS_FOR_RUNEBASE(this.getObjectUUID());
		this.effectsGrantedFighter = DbManager.RuneBaseEffectQueries.GET_EFFECTS_FOR_RUNEBASE((this.getObjectUUID() * 10) + 2500);
		this.effectsGrantedHealer = DbManager.RuneBaseEffectQueries.GET_EFFECTS_FOR_RUNEBASE((this.getObjectUUID() * 10) + 2501);
		this.effectsGrantedRogue = DbManager.RuneBaseEffectQueries.GET_EFFECTS_FOR_RUNEBASE((this.getObjectUUID() * 10) + 2502);
		this.effectsGrantedMage = DbManager.RuneBaseEffectQueries.GET_EFFECTS_FOR_RUNEBASE((this.getObjectUUID() * 10) + 2503);
		this.effectsList = DbManager.MobBaseQueries.GET_RUNEBASE_EFFECTS(this.getObjectUUID());

		
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

	public boolean isAllowedRune(int token) {
		for (int b : this.allowedRunes) {
			if (token == b) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<Integer> getRuneList() {
		return this.allowedRunes;
	}

	public ArrayList<SkillReq> getSkillsGranted() {
		return this.skillsGranted;
	}

	public ArrayList<PowerReq> getPowersGranted() {
		return this.powersGranted;
	}

	public ArrayList<RuneBaseEffect> getEffectsGranted() {
		return this.effectsGranted;
	}

	public ArrayList<RuneBaseEffect> getEffectsGranted(int baseClassID) {
		if (baseClassID == 2500)
			return this.effectsGrantedFighter;
		else if (baseClassID == 2501)
			return this.effectsGrantedHealer;
		else if (baseClassID == 2502)
			return this.effectsGrantedRogue;
		else if (baseClassID == 2503)
			return this.effectsGrantedMage;
		else
			return new ArrayList<>();
	}

	/*
	 * Serializing
	 */
	
	public static void serializeForClientMsg(PromotionClass promotionClass, ByteBufferWriter writer) {
		writer.putInt(3); // For BaseClass
		writer.putInt(0); // Pad
		writer.putInt(promotionClass.getObjectUUID());
                writer.putInt(promotionClass.getObjectType().ordinal());
                writer.putInt(promotionClass.getObjectUUID());
	}

	@Override
	public void updateDatabase() {
		// TODO Create update logic.
	}
	
	public static PromotionClass GetPromtionClassFromCache(int runeID){
		if (runeID == 0)
			return null;
		return (PromotionClass) DbManager.getFromCache(GameObjectType.PromotionClass, runeID);
	}

	public ArrayList<MobBaseEffects> getEffectsList() {
		return effectsList;
	}

	public void setEffectsList(ArrayList<MobBaseEffects> effectsList) {
		this.effectsList = effectsList;
	}
}
