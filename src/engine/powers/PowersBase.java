// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers;

import engine.Enum.PowerCategoryType;
import engine.Enum.PowerTargetType;
import engine.objects.PreparedStatementShared;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class PowersBase {

	public int UUID;
	public String name;
	public int token;
	public String IDString;
	public String category;
	public int skillID;
	public float range;
	public float cost;
	public float costRamp;
	public float castTime;
	public float castTimeRamp;
	public float cooldown;
	public float recycleTime;
	public float recycleRamp;
	public int maxTrains;
	public float hateValue;
	public float hateRamp;
	public String monsterTypePrereq; // target limited to these types
	public String skillName;
	public float weaponRange = 15f;

	// aoe related
	public boolean isAOE = true;
	public boolean useCone = false;
	public boolean usePointBlank = false;
	public boolean useSphere = false;
	public float radius;
	public byte groupReq; // who the spell won't hit
	public int maxNumMobTargets;
	public int maxNumPlayerTargets;

	// chant related
	public float chantDuration;
	public int chantIterations;

	// valid target types from targetType field
	public boolean targetPlayer = false;
	public boolean targetMob = false;
	public boolean targetPet = false;
	public boolean targetNecroPet = false;
	public boolean targetSelf = false;
	public boolean targetWeapon = false;
	public boolean targetCorpse = false;
	public boolean targetBuilding = false;
	public boolean targetGroup = false;
	public boolean targetGuildLeader = false;
	public boolean targetJewelry = false;
	public boolean targetArmor = false;
	public boolean targetItem = false;


	// flags
	public boolean isCasterFriendly = false; // from groupReq
	public boolean isGroupFriendly = false; // from groupReq
	public boolean isGroupOnly = false; // from groupReq
	public boolean mustHitPets = false; // from groupReq
	public boolean isNationFriendly = false; // from groupReq
	public boolean targetFromLastTarget = false; // from unknown06
	public boolean targetFromSelf = false; // from unknown06
	public boolean targetFromName = false; // from unknown06
	public boolean targetFromNearbyMobs = false; // from unknown06
	public boolean useHealth = false; // from costType
	public boolean useMana = false; // from costType
	public boolean useStamina = false; // from costType
	public boolean isSpell = true; // from skillOrSpell field
	public boolean allowedInCombat = false; // from combat field
	public boolean allowedOutOfCombat = false; // from combat field
	public boolean regularPlayerCanCast = false; // from grantOverrideVar
	public boolean hateRampAdd = true; // 1 bit flag
	public boolean costRampAdd = true; // 2 bit flag
	public boolean recycleRampAdd = true; // 4 bit flag
	public boolean initRampAdd = true; // 8 bit flag
	public boolean canCastWhileMoving = false; // 16 bit flag
	public boolean canCastWhileFlying = false; // 32 bit flag
	public boolean isChant = false; // 64 bit flag
	public boolean losCheck = false; // 128 bit flag
	public boolean sticky = false; // 256 bit flag
	public boolean isAdminPower = false; // 512 bit flag
	public boolean requiresHitRoll = false; // 1024 bit flag
	public boolean isWeaponPower = false; // from category
	public boolean isHeal = false; //from category
	public boolean isTrack = false; //from category
	public boolean isHarmful = true;
	public boolean vampDrain = false;

	public boolean cancelOnCastSpell = false;
	public boolean cancelOnTakeDamage = false;

	public final ArrayList<String> monsterTypeRestrictions = new ArrayList<>();
	public final ArrayList<ActionsBase> actions = new ArrayList<>();
	public final ArrayList<PowerPrereq> effectPrereqs = new ArrayList<>();
	public final ArrayList<PowerPrereq> targetEffectPrereqs = new ArrayList<>();
	public final ArrayList<PowerPrereq> equipPrereqs = new ArrayList<>();
	
	public PowerTargetType targetType;
	public PowerCategoryType powerCategory;
	public String description;

	/**
	 * No Table ID Constructor
	 */
	public PowersBase() {

	}

	/**
	 * ResultSet Constructor
	 */
	public PowersBase(ResultSet rs) throws SQLException {

		this.UUID = rs.getInt("ID");
		this.name = rs.getString("name").trim();
		this.token = rs.getInt("token");
		this.skillName = rs.getString("skillName").trim();
		this.IDString = rs.getString("IDString").trim();
		this.isSpell = (rs.getString("skillOrSpell").equals("SPELL")) ? true : false;
		this.skillID = rs.getInt("skillID");
		this.range = rs.getFloat("range");
		this.description = (rs.getString("description")).trim().replace("\r\n ", "");
		String ct = rs.getString("costType").trim();
		if (ct.equals("HEALTH"))
			this.useHealth = true;
		else if (ct.equals("MANA"))
			this.useMana = true;
		else if (ct.equals("STAMINA"))
			this.useStamina = true;
		ct = rs.getString("targetType").trim();
		if (ct.equals("BUILDING"))
			this.targetBuilding = true;
		else if (ct.equals("CORPSE"))
			this.targetCorpse = true;
		else if (ct.equals("GROUP"))
			this.targetGroup = true;
		else if (ct.equals("GUILDLEADER"))
			this.targetGuildLeader = true;
		else if (ct.equals("MOBILE")) {
			this.targetMob = true;
			this.targetPet = true; // sure on this one?
		} else if (ct.equals("PC"))
			this.targetPlayer = true;
		else if (ct.equals("SELF"))
			this.targetSelf = true;
		else if (ct.equals("PET"))
			this.targetPet = true;
		else if (ct.equals("NECROPET"))
			this.targetNecroPet = true;
		else if (ct.equals("ARMOR"))
			this.targetArmor = true;
		else if (ct.equals("WEAPON"))
			this.targetWeapon = true;
		else if (ct.equals("JEWELRY"))
			this.targetJewelry = true;
		else if (ct.equals("ITEM")) {
			this.targetItem = true;
			this.targetJewelry = true;
			this.targetArmor = true;
			this.targetWeapon = true;
		} else if (ct.equals("ARMORWEAPONJEWELRY")) {
			this.targetArmor = true;
			this.targetWeapon = true;
			this.targetJewelry = true;
		} else if (ct.equals("PCMOBILE")) {
			this.targetPlayer = true;
			this.targetMob = true;
			this.targetPet = true; // sure on this one?
		} else if (ct.equals("WEAPONARMOR")) {
			this.targetWeapon = true;
			this.targetArmor = true;
		} else {
			Logger.info("Missed " + ct + " targetType");
		}
		String cat = rs.getString("category").trim();
		this.category = cat;
		
		
		if (cat.isEmpty())
			this.powerCategory = PowerCategoryType.NONE;
		else
		this.powerCategory = PowerCategoryType.valueOf(cat.replace("-", ""));
		
		
		
		
		if (cat.equals("WEAPON")) {
			this.isWeaponPower = true;
			this.isHarmful = false;
			if (this.skillName.equals("Bow") || this.skillName.equals("Crossbow") || this.skillName.equals("Archery"))
				this.weaponRange = 1000f;
			else if (this.skillName.equals("Throwing"))
				this.weaponRange = 60f;
			else
				this.weaponRange = 15f;
		} else if (cat.equals("HEAL") || cat.equals("GROUPHEAL")) {
			this.isHeal = true;
			this.isHarmful = false;
		} else if (cat.equals("TRACK")) {
			this.isTrack = true;
			this.isHarmful = false;
		} else if (cat.equals("AE") || cat.equals("AEDAMAGE") ||
				cat.equals("BREAKFLY") ||
				cat.equals("DAMAGE") || cat.equals("DEBUFF") ||
				cat.equals("MOVE") || cat.equals("SPECIAL") ||
				cat.equals("SPIREDISABLE"))
			this.isHarmful = true;
		else if (cat.equals("CHANT")) {
			this.isHarmful = ct.equals("MOBILE") || ct.equals("PC") || ct.equals("PCMOBILE");
		} else if (cat.equals("DISPEL")) {
			//TODO this needs broken down better later
			this.isHarmful = false;
		} else if (cat.isEmpty()) {
			if (ct.equals("MOBILE") || ct.equals("PCMOBILE"))
				this.isHarmful = true;
			else if (ct.equals("PC")) {
				this.isHarmful = this.token != 429607195 && this.token != 429425915;
			} else
				this.isHarmful = false;
		} else
			this.isHarmful = false;

		if (cat.equals("VAMPDRAIN")) {
			this.vampDrain = true;
			this.isHarmful = true;
		}

		this.cost = rs.getFloat("cost");
		this.costRamp = rs.getFloat("costRamp");
		this.castTime = rs.getFloat("castTime");
		this.castTimeRamp = rs.getFloat("initRamp");
		this.cooldown = rs.getFloat("cooldown");
		this.recycleTime = rs.getFloat("recycleTime");
		this.recycleRamp = rs.getFloat("recycleRamp");
		this.maxTrains = rs.getInt("maxTrains");
		this.hateValue = rs.getFloat("hateValue");
		this.hateRamp = rs.getFloat("hateRamp");
		ct = rs.getString("unknown06").trim();
		if (this.targetSelf) {
		} else if (ct.equals("CLICK"))
			if (!this.targetGroup)
			this.targetFromLastTarget = true;
		else if (ct.equals("NAME"))
			this.targetFromName = true;
		else if (ct.equals("NEARBYMOBS"))
			this.targetFromNearbyMobs = true;
		this.monsterTypePrereq = rs.getString("monsterTypePrereqs").trim();
		ct = rs.getString("radiusType").trim();
		if (ct.equals("CONE"))
			this.useCone = true;
		else if (ct.equals("POINTBLANK"))
			this.usePointBlank = true;
		else if (ct.equals("SPHERE"))
			this.useSphere = true;
		else
			this.isAOE = false;
		this.radius = rs.getFloat("radius");
		ct = rs.getString("groupReq").trim();
		if (ct.equals("CASTER"))
			this.isCasterFriendly = true;
		else if (ct.equals("GROUP")) {
			this.isGroupFriendly = true;
			this.isCasterFriendly = true;
		}
		else if (ct.equals("ALLBUTGROUP"))
			this.isGroupOnly = true;
		else if (ct.equals("ALLBUTPETS"))
			this.mustHitPets = true;
		else if (ct.equals("NATION")) {
			this.isNationFriendly = true;
			this.isCasterFriendly = true;
		}
		this.maxNumMobTargets = rs.getInt("maxNumMobTargets");
		this.maxNumPlayerTargets = rs.getInt("maxNumPlayerTargets");
		this.chantDuration = rs.getFloat("chantDuration");
		this.chantIterations = rs.getInt("chantIterations");
		ct = rs.getString("combat").trim();
		if (ct.equals("COMBAT"))
			this.allowedInCombat = true;
		else if (ct.equals("NONCOMBAT"))
			this.allowedOutOfCombat = true;
		else if (ct.equals("BOTH")) {
			this.allowedInCombat = true;
			this.allowedOutOfCombat = true;
		}
		ct = rs.getString("grantOverideVar").trim();
		if (ct.equals("PGOV_PLAYER"))
			this.regularPlayerCanCast = true;
		int flags = rs.getInt("flags");
		if ((flags & 1) == 0)
			this.hateRampAdd = false;
		if ((flags & 2) == 0)
			this.costRampAdd = false;
		if ((flags & 4) == 0)
			this.recycleRampAdd = false;
		if ((flags & 8) == 0)
			this.initRampAdd = false;
		if ((flags & 16) != 0)
			this.canCastWhileMoving = true;
		if ((flags & 32) != 0)
			this.canCastWhileFlying = true;
		if ((flags & 64) != 0)
			this.isChant = true;
		if ((flags & 128) != 0)
			this.losCheck = true;
		if ((flags & 256) != 0)
			this.sticky = true;
		if ((flags & 512) != 0)
			this.isAdminPower = true;
		if ((flags & 1024) != 0)
			this.requiresHitRoll = true;
		ct = rs.getString("monsterTypeRestrict1").trim();
		if (!ct.isEmpty())
			this.monsterTypeRestrictions.add(ct);
		ct = rs.getString("monsterTypeRestrict2").trim();
		if (!ct.isEmpty())
			this.monsterTypeRestrictions.add(ct);
		ct = rs.getString("monsterTypeRestrict3").trim();
		if (!ct.isEmpty())
			this.monsterTypeRestrictions.add(ct);
	}

	public static ArrayList<PowersBase> getAllPowersBase() {
		PreparedStatementShared ps = null;
		ArrayList<PowersBase> out = new ArrayList<>();
		try {
			ps = new PreparedStatementShared("SELECT * FROM static_power_powerbase");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				PowersBase toAdd = new PowersBase(rs);
				out.add(toAdd);
			}
			rs.close();
		} catch (Exception e) {
			Logger.error( e.toString());
		} finally {
			ps.release();
		}
		return out;
	}
	

	public static void getFailConditions(HashMap<String, PowersBase> powers) {
		PreparedStatementShared ps = null;
		try {
			ps = new PreparedStatementShared("SELECT IDString, type FROM static_power_failcondition where powerOrEffect = 'Power'");
			ResultSet rs = ps.executeQuery();
			String type, IDString; PowersBase pb;
			while (rs.next()) {
				type = rs.getString("type");
				IDString = rs.getString("IDString");
				pb = powers.get(IDString);
				if (pb != null) {
					switch (type) {
					case "CastSpell":
						pb.cancelOnCastSpell = true;
						break;
					case "TakeDamage":
						pb.cancelOnTakeDamage = true;
						break;
					}
				}else{
					Logger.error("null power for Grief " + IDString);
				}
			}
			rs.close();
		} catch (Exception e) {
			Logger.error( e.toString());
		} finally {
			ps.release();
		}
	}
	

	public String getName() {
		return this.name;
	}

	public int getMaxTrains() {
		return this.maxTrains;
	}

	public int getUUID() {
		return this.UUID;
	}

	public String getIDString() {
		return this.IDString;
	}

	public int getToken() {
		if (this.IDString.equals("BLEED-DOT-10.5-RANGE"))
			return -369682965;
		return this.token;
	}

	public int getCastTime(int trains) { // returns cast time in ms
		if (this.initRampAdd)
			return (int) ((this.castTime + (this.castTimeRamp * trains)) * 1000);
		else
			return (int) ((this.castTime * (1 + (this.castTimeRamp * trains))) * 1000);
	}

	public int getRecycleTime(int trains) { // returns cast time in ms
		if (this.recycleRampAdd)
			return (int) (((this.recycleTime + (this.recycleRamp * trains)) * 1000) + getCastTime(trains));
		else
			return (int) (((this.recycleTime * (1 + (this.recycleRamp * trains))) * 1000) + getCastTime(trains));
	}

	// public ArrayList<FailCondition> getConditions() {
	// return this.conditions;
	// }

	public ArrayList<PowerPrereq> getEffectPrereqs() {
		return this.effectPrereqs;
	}

	public ArrayList<PowerPrereq> getTargetEffectPrereqs() {
		return this.targetEffectPrereqs;
	}

	public ArrayList<PowerPrereq> getEquipPrereqs() {
		return this.equipPrereqs;
	}

	public ArrayList<ActionsBase> getActions() {
		return this.actions;
	}

	public boolean usePointBlank() {
		return this.usePointBlank;
	}

	public float getRadius() {
		return this.radius;
	}

	public int getMaxNumMobTargets() {
		return this.maxNumMobTargets;
	}

	public int getMaxNumPlayerTargets() {
		return this.maxNumPlayerTargets;
	}

	public boolean cancelOnCastSpell() {
		return this.cancelOnCastSpell;
	}

	public boolean cancelOnTakeDamage() {
		return this.cancelOnTakeDamage;
	}

	public boolean allowedInCombat() {
		return this.allowedInCombat;
	}

	public boolean allowedOutOfCombat() {
		return this.allowedOutOfCombat;
	}

	public boolean isCasterFriendly() {
		return this.isCasterFriendly;
	}

	public boolean isGroupFriendly() {
		return this.isGroupFriendly;
	}

	public boolean isNationFriendly() {
		return this.isNationFriendly;
	}

	public boolean isGroupOnly() {
		return this.isGroupOnly;
	}

	public boolean mustHitPets() {
		return this.mustHitPets;
	}

	public boolean targetFromLastTarget() {
		return this.targetFromLastTarget;
	}

	public boolean targetFromSelf() {
		return this.targetFromSelf;
	}

	public boolean targetFromName() {
		return this.targetFromName;
	}

	public boolean targetFromNearbyMobs() {
		return this.targetFromNearbyMobs;
	}

	public float getRange() {
		return this.range;
	}

	public boolean requiresHitRoll() {
		return this.requiresHitRoll;
	}

	public boolean regularPlayerCanCast() {
		return this.regularPlayerCanCast;
	}

	public boolean isSpell() {
		return this.isSpell;
	}

	public boolean isHarmful() {
		return this.isHarmful;
	}

	public boolean targetPlayer() {
		return this.targetPlayer;
	}

	public boolean targetMob() {
		return this.targetMob;
	}

	public boolean targetPet() {
		return this.targetPet;
	}

	public boolean targetNecroPet() {
		return this.targetNecroPet;
	}

	public boolean targetSelf() {
		return this.targetSelf;
	}

	public boolean targetCorpse() {
		return this.targetCorpse;
	}

	public boolean targetBuilding() {
		return this.targetBuilding;
	}

	public boolean targetGroup() {
		return this.targetGroup;
	}

	public boolean targetGuildLeader() {
		return this.targetGuildLeader;
	}

	public boolean targetJewelry() {
		return this.targetJewelry;
	}

	public boolean targetArmor() {
		return this.targetArmor;
	}

	public boolean targetWeapon() {
		return this.targetWeapon;
	}

	public boolean targetItem() {
		return this.targetItem;
	}

	public long getCooldown() {
		return  (long) (this.cooldown * 1000); // return
		// in ms
	}

	public boolean useHealth() {
		return this.useHealth;
	}

	public boolean useMana() {
		return this.useMana;
	}

	public boolean useStamina() {
		return this.useStamina;
	}

	public float getCost(int trains) {
		if (this.costRampAdd)
			return this.cost + (this.costRamp * trains);
		else
			return this.cost * (1 + (this.costRamp * trains));

	}

	public float getHateValue() {
		return this.hateValue;
	}

	public float getHateRamp() {
		return this.hateRamp;
	}

	public float getHateValue(int trains) {
		return this.hateValue + (this.hateRamp * trains);
	}

	public boolean canCastWhileMoving() {
		return this.canCastWhileMoving;
	}

	public boolean canCastWhileFlying() {
		return this.canCastWhileFlying;
	}

	public boolean isAOE() {
		return isAOE;
	}

	public boolean isChant() {
		return isChant;
	}

	public int getChantIterations() {
		return chantIterations;
	}

	public float getChantDuration() {
		return chantDuration;
	}

	public boolean isWeaponPower() {
		return isWeaponPower;
	}

	public boolean isHeal() {
		return isHeal;
	}

	public boolean isTrack() {
		return isTrack;
	}

	public boolean vampDrain() {
		return vampDrain;
	}

	public void setCancelOnCastSpell(boolean value) {
		this.cancelOnCastSpell = value;
	}

	public void setCancelOnTakeDamage(boolean value) {
		this.cancelOnTakeDamage = value;
	}

	public String getSkillName() {
		return this.skillName;
	}

	public String getMonsterTypePrereq() {
		return this.monsterTypePrereq;
	}

	public String getCategory() {
		return this.category;
	}

	public float getWeaponRange() {
		return this.weaponRange;
	}
	
	public PowerCategoryType getPowerCategoryType(){
		return this.powerCategory;
	}

	public String getDescription() {
		return description;
	}

}
