// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.gameManager.ChatManager;
import engine.gameManager.ConfigManager;
import engine.gameManager.PowersManager;
import engine.powers.DamageShield;
import engine.powers.EffectsBase;
import engine.powers.effectmodifiers.AbstractEffectModifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;


public class PlayerBonuses {

	//First bonus set
	private ConcurrentHashMap<AbstractEffectModifier, Float> bonusFloats = new ConcurrentHashMap<>();
	private ConcurrentHashMap<AbstractEffectModifier, DamageShield> bonusDamageShields = new ConcurrentHashMap<>();
	private ConcurrentHashMap<AbstractEffectModifier, String> bonusStrings = new ConcurrentHashMap<>();
	private ConcurrentHashMap<ModType, HashSet<SourceType>> bonusLists = new ConcurrentHashMap<>();
	private ConcurrentHashMap<ModType, HashMap<SourceType, Boolean>> bonusBools = new ConcurrentHashMap<>();
	private ConcurrentHashMap<SourceType, Float> skillBonuses = new ConcurrentHashMap<>();
	private ConcurrentHashMap<ModType, Float> regens = new ConcurrentHashMap<>();

	//If active == 0 then all gets come from the A list and all puts go to the B list
	//If active == 1 then all gets come from the B list and all puts go to the A list
	//They alternate each time bonuses are calculated so the one being updated isn't read from.
	

	/**
	 * Generic Constructor
	 */
	public PlayerBonuses(PlayerCharacter pc) {
	}
	
	public static void InitializeBonuses(PlayerCharacter player){
		if (player.bonuses == null)
			return;
		if (ConfigManager.serverType.equals(Enum.ServerType.LOGINSERVER))
			return;
		
		player.bonuses.calculateRuneBaseEffects(player);
	}

	public PlayerBonuses(Mob mob) {
		clearRuneBaseEffects();
	}

	public static PlayerBonuses grantBonuses(AbstractCharacter ac) {

		if (ac.getObjectType().equals(Enum.GameObjectType.PlayerCharacter))
			return new PlayerBonuses((PlayerCharacter)ac);
		else if (ac.getObjectType().equals(Enum.GameObjectType.Mob))
			return new PlayerBonuses((Mob)ac);
		else
			return null;
	}

	public void clearRuneBaseEffects() {
		
			this.bonusBools.clear();
			this.bonusFloats.clear();
			this.bonusStrings.clear();
			this.bonusDamageShields.clear();
			this.bonusLists.clear();
			this.skillBonuses.clear();
			this.regens.put(ModType.HealthRecoverRate, (float) 1);
			this.regens.put(ModType.ManaRecoverRate, (float) 1);
			this.regens.put(ModType.StaminaRecoverRate, (float) 1);
	}
	
	

	public void calculateRuneBaseEffects(PlayerCharacter pc) {
		//Clear everything
		clearRuneBaseEffects();

		//recalculate race
		
		
		if (pc.getRace() != null){
			
			
			if (pc.getRace().getEffectsList() != null)
			for (MobBaseEffects raceEffect: pc.getRace().getEffectsList()){
				EffectsBase eb = PowersManager.getEffectByToken(raceEffect.getToken());
				
				if (eb == null)
					continue;
				if (pc.getLevel() < raceEffect.getReqLvl())
					continue;
				for (AbstractEffectModifier modifier: eb.getModifiers()){
					modifier.applyBonus(pc, raceEffect.getRank());
				}
				
			}
			
			if (SkillsBase.runeSkillsCache.containsKey(pc.getRaceID())){
				for (int skillToken : SkillsBase.runeSkillsCache.get(pc.getRaceID()).keySet()){
					float amount = SkillsBase.runeSkillsCache.get(pc.getRaceID()).get(skillToken);
					
					SkillsBase sb = SkillsBase.tokenCache.get(skillToken);
					
					if (sb == null)
						continue;
					if (this.skillBonuses.containsKey(sb.sourceType) == false)
						this.skillBonuses.put(sb.sourceType, amount);
					else
						this.skillBonuses.put(sb.sourceType, this.skillBonuses.get(sb.sourceType) + amount);

				}
			}
		}
		
		//calculate baseclass effects
		if (pc.getBaseClass() != null){
			
			if (pc.getBaseClass().getEffectsList() != null)
				for (MobBaseEffects classEffect: pc.getBaseClass().getEffectsList()){
					EffectsBase eb = PowersManager.getEffectByToken(classEffect.getToken());
					
					if (eb == null)
						continue;
					if (pc.getLevel() < classEffect.getReqLvl())
						continue;
					for (AbstractEffectModifier modifier: eb.getModifiers()){
						modifier.applyBonus(pc, classEffect.getRank());
					}
					
				}
			
			if (SkillsBase.runeSkillsCache.containsKey(pc.getBaseClassID())){
				for (int skillToken : SkillsBase.runeSkillsCache.get(pc.getBaseClassID()).keySet()){
					float amount = SkillsBase.runeSkillsCache.get(pc.getBaseClassID()).get(skillToken);
					
					SkillsBase sb = SkillsBase.tokenCache.get(skillToken);
					
					if (sb == null)
						continue;
					if (this.skillBonuses.containsKey(sb.sourceType) == false)
						this.skillBonuses.put(sb.sourceType, amount);
					else
						this.skillBonuses.put(sb.sourceType, this.skillBonuses.get(sb.sourceType) + amount);
				}
			}
			
		}
		
		//calculate promotionClass Effects
		if (pc.getPromotionClass() != null){
			if (pc.getPromotionClass().getEffectsList() != null)
				for (MobBaseEffects promoEffect: pc.getPromotionClass().getEffectsList()){
					EffectsBase eb = PowersManager.getEffectByToken(promoEffect.getToken());
					
					if (eb == null)
						continue;
					if (pc.getLevel() < promoEffect.getReqLvl())
						continue;
					for (AbstractEffectModifier modifier: eb.getModifiers()){
						modifier.applyBonus(pc, promoEffect.getRank());
					}
					
				}
			
			if (SkillsBase.runeSkillsCache.containsKey(pc.getPromotionClassID())){
				for (int skillToken : SkillsBase.runeSkillsCache.get(pc.getPromotionClassID()).keySet()){
					float amount = SkillsBase.runeSkillsCache.get(pc.getPromotionClassID()).get(skillToken);
					
					SkillsBase sb = SkillsBase.tokenCache.get(skillToken);
					
					if (sb == null)
						continue;
					if (this.skillBonuses.containsKey(sb.sourceType) == false)
						this.skillBonuses.put(sb.sourceType, amount);
					else
						this.skillBonuses.put(sb.sourceType, this.skillBonuses.get(sb.sourceType) + amount);

				}
			}
			
		}
		
		for(CharacterRune runes : pc.getRunes()){
			RuneBase characterRune = RuneBase.getRuneBase(runes.getRuneBaseID());
			
			if (characterRune.getEffectsList() != null)
				for (MobBaseEffects runeEffect: characterRune.getEffectsList()){
					EffectsBase eb = PowersManager.getEffectByToken(runeEffect.getToken());
					
					if (eb == null)
						continue;
					if (pc.getLevel() < runeEffect.getReqLvl())
						continue;
					for (AbstractEffectModifier modifier: eb.getModifiers()){
						modifier.applyBonus(pc, runeEffect.getRank());
					}
					
				}
			
			if (SkillsBase.runeSkillsCache.containsKey(runes.getRuneBaseID())){
				for (int skillToken : SkillsBase.runeSkillsCache.get(runes.getRuneBaseID()).keySet()){
					float amount = SkillsBase.runeSkillsCache.get(runes.getRuneBaseID()).get(skillToken);
					
					SkillsBase sb = SkillsBase.tokenCache.get(skillToken);
					
					if (sb == null)
						continue;
					if (this.skillBonuses.containsKey(sb.sourceType) == false)
						this.skillBonuses.put(sb.sourceType, amount);
					else
						this.skillBonuses.put(sb.sourceType, this.skillBonuses.get(sb.sourceType) + amount);

				}
			}
			
		}

		//Update seeInvis if needed
		
			float seeInvis = this.getFloat(ModType.SeeInvisible, SourceType.None);
			if (pc.getSeeInvis() < seeInvis)
				pc.setSeeInvis((short)seeInvis);
		
	}
	

	public void grantEffect(RuneBaseEffect rbe) {
	}
	
	
	public void setFloat(AbstractEffectModifier mod, float val) {
		if (val != 0)
			this.bonusFloats.put(mod, val);
		else 
			this.bonusFloats.remove(mod);
	}

	public void setString(AbstractEffectModifier mod, String val) {
		if (!val.isEmpty())
			this.bonusStrings.put(mod, val);
		else 
			this.bonusStrings.remove(mod);
	}

	public void setList(ModType mod, HashSet<SourceType> val) {
		if (!val.equals(null))
			this.bonusLists.put(mod, val);
		else 
			this.bonusLists.remove(mod);
	}


	

	public void addFloat(AbstractEffectModifier mod, Float val) {
		if (this.bonusFloats.containsKey(mod) == false)
			this.bonusFloats.put(mod, val);
		else
			this.bonusFloats.put(mod, this.bonusFloats.get(mod) + val);
	}
	
	public void multFloat(AbstractEffectModifier mod, Float val) {
		if (this.bonusFloats.containsKey(mod) == false)
			this.bonusFloats.put(mod, val);
		else
			this.bonusFloats.put(mod,this.bonusFloats.get(mod) + (val * ( this.bonusFloats.get(mod) + val)));
	}
	
	public void multRegen(ModType mod, Float val) {
			this.regens.put(mod,this.regens.get(mod) + (this.regens.get(mod) * val));
	}

	
	public boolean getBool(ModType modType, SourceType sourceType) {
		
		if (this.bonusBools.containsKey(modType) == false)
			return false;
		
		if (this.bonusBools.get(modType).containsKey(sourceType) == false)
			return false;
		
		return this.bonusBools.get(modType).get(sourceType);
	
	}
	
	public float getSkillBonus(SourceType sourceType) {
		
		if (this.skillBonuses.containsKey(sourceType) == false)
			return 0;
	return this.skillBonuses.get(sourceType);
	}

	
	public float getFloat(ModType modType, SourceType sourceType) {
		float amount = 0;
		for (AbstractEffectModifier mod : this.bonusFloats.keySet()){
				if (mod.getPercentMod() != 0)
					continue;
			if (mod.modType.equals(modType) == false || mod.sourceType.equals(sourceType) == false)
				continue;
			
			if (this.bonusFloats.get(mod) == null)
				continue;
			
			amount += this.bonusFloats.get(mod);
		}
		return amount;
	}
	
	public float getFloatPercentPositive(ModType modType, SourceType sourceType) {
		float amount = 0;
		for (AbstractEffectModifier mod : this.bonusFloats.keySet()){
		
				if (mod.getPercentMod() == 0 && !modType.equals(ModType.AdjustAboveDmgCap))
					continue;
				
			
			if (mod.modType.equals(modType) == false || mod.sourceType.equals(sourceType) == false)
				continue;
			
			
			if (this.bonusFloats.get(mod) == null)
				continue;
			
			if (this.bonusFloats.get(mod) < 0)
				continue;
			amount += this.bonusFloats.get(mod);
		}
		
		return amount;
	}
	
	public float getFloatPercentAll(ModType modType, SourceType sourceType) {
		float amount = 0;
		for (AbstractEffectModifier mod : this.bonusFloats.keySet()){
		
				if (mod.getPercentMod() == 0 && !modType.equals(ModType.AdjustAboveDmgCap))
					continue;
				
			
			if (mod.modType.equals(modType) == false || mod.sourceType.equals(sourceType) == false)
				continue;
			
			if (this.bonusFloats.get(mod) == null)
				continue;
			
			amount += this.bonusFloats.get(mod);
		}
		
		return amount;
	}
	
	public float getRegen(ModType modType) {
		return this.regens.get(modType);
	}
	
	
	
	public float getFloatPercentNullZero(ModType modType, SourceType sourceType) {
		float amount = 0;
		for (AbstractEffectModifier mod : this.bonusFloats.keySet()){
		
				if (mod.getPercentMod() == 0)
					continue;
			if (mod.modType.equals(modType) == false || mod.sourceType.equals(sourceType) == false)
				continue;
			
			if (this.bonusFloats.get(mod) == null)
				continue;
			
			amount += this.bonusFloats.get(mod);
		}
		return amount;
	}
	
	public float getFloatPercentNegative(ModType modType, SourceType sourceType) {
		float amount = 0;
		for (AbstractEffectModifier mod : this.bonusFloats.keySet()){
		
				if (mod.getPercentMod() == 0)
					continue;
			if (mod.modType.equals(modType) == false || mod.sourceType.equals(sourceType) == false)
				continue;
			
			if (this.bonusFloats.get(mod) == null)
				continue;
			
			if (this.bonusFloats.get(mod) > 0)
				continue;
			
			
			amount += this.bonusFloats.get(mod);
		}
		return amount;
	}


	
	

	public HashSet<SourceType> getList(ModType modType) {
		if (this.bonusLists.containsKey(modType))
			return this.bonusLists.get(modType);
		else
			return null;
	}
	
	public ConcurrentHashMap<AbstractEffectModifier, DamageShield> getDamageShields() {
		return this.bonusDamageShields;
	}

	public void addDamageShield(AbstractEffectModifier mod , DamageShield ds) {
		this.bonusDamageShields.put(mod, ds);
	}
	
	

	public void updateIfHigher(AbstractEffectModifier mod, Float val) {
		
		if (this.bonusFloats.containsKey(mod) == false){
			this.bonusFloats.put(mod, val);
			return;
		}
		float oldVal = this.getFloat(mod.modType, mod.sourceType);
		
		if (oldVal > val)
			return;
		
		this.bonusFloats.put(mod,val);
		
	}


	//Read maps

	public void printBonusesToClient(PlayerCharacter pc) {
		
		
		
		for (ModType modType: this.bonusBools.keySet()){
			for (SourceType sourceType: this.bonusBools.get(modType).keySet()){
				ChatManager.chatSystemInfo(pc, modType.name() + "-" + sourceType.name() + " = " + this.bonusBools.get(modType).get(sourceType));
			}	
		}
		
		for (ModType modType : ModType.values()){
			
			if (modType.equals(ModType.StaminaRecoverRate) || modType.equals(ModType.HealthRecoverRate) || modType.equals(ModType.ManaRecoverRate))
				ChatManager.chatSystemInfo(pc, modType.name()  + " = " + this.getRegen(modType));
			else
			for (SourceType sourceType : SourceType.values()){
				float amount = this.getFloat(modType, sourceType);
				float percentAmount = this.getFloatPercentPositive(modType, sourceType);
				float percentAmountNegative = this.getFloatPercentNegative(modType, sourceType);
				
				if (amount != 0)
					ChatManager.chatSystemInfo(pc, modType.name() + "-" + (sourceType.equals(SourceType.None) == false ? sourceType.name() : "") + " = " + amount);	
				
				if (percentAmount != 0)
					ChatManager.chatSystemInfo(pc, "Percent : " + modType.name() + "-" + (sourceType.equals(SourceType.None) == false ? sourceType.name() : "") + " = " + percentAmount);	

				if (percentAmountNegative != 0)
					ChatManager.chatSystemInfo(pc, "Negative Percent : " + modType.name() + "-" + (sourceType.equals(SourceType.None) == false ? sourceType.name() : "") + " = " + percentAmountNegative);	

			}
		}

	}
	
	
	public void setBool(ModType modType, SourceType sourceType , boolean val) {
		if (val == true){
			
			if (this.bonusBools.get(modType) == null){
				HashMap<SourceType, Boolean> sourceMap = new HashMap<>();
				this.bonusBools.put(modType, sourceMap);
			}
				
				this.bonusBools.get(modType).put(sourceType, val);
				return;
		}
		
		if (this.bonusBools.containsKey(modType))
		this.bonusBools.get(modType).remove(sourceType);
	}
	
}
