// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.effectmodifiers;

import engine.Enum.DamageType;
import engine.Enum.GameObjectType;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.gameManager.ChatManager;
import engine.jobs.AbstractEffectJob;
import engine.jobs.DamageOverTimeJob;
import engine.net.AbstractNetMsg;
import engine.net.DispatchMessage;
import engine.net.client.msg.ModifyHealthKillMsg;
import engine.net.client.msg.ModifyHealthMsg;
import engine.objects.*;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class HealthEffectModifier extends AbstractEffectModifier {

	private DamageType damageType;

	public HealthEffectModifier(ResultSet rs) throws SQLException {
		super(rs);
		String damageTypeDB = rs.getString("type");
		try {
			this.damageType = DamageType.valueOf(damageTypeDB);
		} catch (IllegalArgumentException e) {
			Logger.error("DamageType could not be loaded from database. " + "UUID = " + this.UUID
					+ " value received = '" + damageTypeDB + '\'', e);
		}
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {
		if (awo == null) {
			Logger.error("_applyEffectModifier(): NULL AWO passed in.");
			return;
		}

		if (effect == null) {
			Logger.error( "_applyEffectModifier(): NULL AbstractEffectJob passed in.");
			return;
		}

		float modAmount = 0f;

		// Modify health by percent
		if (this.percentMod != 0f) {

			//high level mobs/players should not be %damaged/healed.
			if (awo.getHealthMax() > 25000f && (this.percentMod < 0f || this.percentMod > 5f))
				return;

			float mod = 1f;
			if (this.useRampAdd)
				mod = (this.percentMod + (this.ramp * trains)) / 100;
			else
				mod = (this.percentMod * (1 + (this.ramp * trains))) / 100;
			modAmount = mod * awo.getHealthMax();
			if (AbstractWorldObject.IsAbstractCharacter(awo)) {
				if (((AbstractCharacter)awo).isSit())
					modAmount *= 2.5f;
			}

			//debug for spell damage and atr
			if (source.getDebug(16) && source.getObjectType().equals(GameObjectType.PlayerCharacter)) {
				PlayerCharacter pc = (PlayerCharacter) source;
				String smsg = "Percent Damage: " + mod * 100 + '%';
				ChatManager.chatSystemInfo(pc, smsg);
			}
		}

		// Modify health by min/max amount
		else if (this.minMod != 0f || this.maxMod != 0f) {
			float min = this.minMod;
			float max = this.maxMod;
			if (this.ramp > 0f) {
				float mod = this.ramp * trains;
				if (this.useRampAdd) {
					min += mod;
					max += mod;
				} else {
					min *= (1 + mod);
					max *= (1 + mod);
				}
			}
			if (source.getObjectType().equals(GameObjectType.PlayerCharacter)) {
				PlayerCharacter pc = (PlayerCharacter) source;

				float focus;
				CharacterSkill skill = pc.getSkills().get(effect.getPower().getSkillName());
				if (skill == null)
					focus = CharacterSkill.getQuickMastery(pc, effect.getPower().getSkillName());
				else
					focus = skill.getModifiedAmount();
				//TODO clean up old formulas once new one is verified
				//				min *= (0.5 + 0.0075 * pc.getStatIntCurrent() + 0.011 * pc.getStatSpiCurrent() + 0.0196 * focus);
				//				max *= (0.62 + 0.0192 * pc.getStatIntCurrent() + 0.00415 * pc.getStatSpiCurrent() + 0.015 * focus);
				float intt = (pc.getStatIntCurrent() >= 1) ? (float)pc.getStatIntCurrent() : 1f;
				float spi = (pc.getStatSpiCurrent() >= 1) ? (float)pc.getStatSpiCurrent() : 1f;
				//				min *= (intt * 0.0045 + 0.055 * (float)Math.sqrt(intt - 0.5) + spi * 0.006 + 0.07 * (float)Math.sqrt(spi - 0.5) + 0.02 * (int)focus);
				//				max *= (intt * 0.0117 + 0.13 * (float)Math.sqrt(intt - 0.5) + spi * 0.0024 + (float)Math.sqrt(spi - 0.5) * 0.021 + 0.015 * (int)focus);
				min = HealthEffectModifier.getMinDamage(min, intt, spi, focus);
				max = HealthEffectModifier.getMaxDamage(max, intt, spi, focus);

				//debug for spell damage and atr
				if (pc.getDebug(16)) {
					String smsg = "Damage: " + (int)Math.abs(min) + " - " + (int)Math.abs(max);
					ChatManager.chatSystemInfo(pc, smsg);
				}
			}else if (source.getObjectType() == GameObjectType.Mob){
				Mob pc = (Mob) source;

				float focus;
				CharacterSkill skill = pc.getSkills().get(effect.getPower().getSkillName());
				if (skill == null)
					focus = CharacterSkill.getQuickMastery(pc, effect.getPower().getSkillName());
				else
					focus = skill.getModifiedAmount();
				//TODO clean up old formulas once new one is verified
				//				min *= (0.5 + 0.0075 * pc.getStatIntCurrent() + 0.011 * pc.getStatSpiCurrent() + 0.0196 * focus);
				//				max *= (0.62 + 0.0192 * pc.getStatIntCurrent() + 0.00415 * pc.getStatSpiCurrent() + 0.015 * focus);
				float intt = (pc.getStatIntCurrent() >= 1) ? (float)pc.getStatIntCurrent() : 1f;

				if (pc.isPlayerGuard())
					intt = 200;
				float spi = (pc.getStatSpiCurrent() >= 1) ? (float)pc.getStatSpiCurrent() : 1f;

				if (pc.isPlayerGuard())
					spi = 200;
				//				min *= (intt * 0.0045 + 0.055 * (float)Math.sqrt(intt - 0.5) + spi * 0.006 + 0.07 * (float)Math.sqrt(spi - 0.5) + 0.02 * (int)focus);
				//				max *= (intt * 0.0117 + 0.13 * (float)Math.sqrt(intt - 0.5) + spi * 0.0024 + (float)Math.sqrt(spi - 0.5) * 0.021 + 0.015 * (int)focus);
				min = HealthEffectModifier.getMinDamage(min, intt, spi, focus);
				max = HealthEffectModifier.getMaxDamage(max, intt, spi, focus);

				//debug for spell damage and atr
				//				if (pc.getDebug(16)) {
				//					String smsg = "Damage: " + (int)Math.abs(min) + " - " + (int)Math.abs(max);
				//					ChatManager.chatSystemInfo(pc, smsg);
				//				}
			}
			modAmount = calculateDamage(source, min, max, awo, trains);
			PlayerBonuses bonus = source.getBonuses();

			// Apply any power effect modifiers (such as stances)
			if (bonus != null)
				modAmount *= (1 + (bonus.getFloatPercentAll(ModType.PowerDamageModifier, SourceType.None)));
		}
		if (modAmount == 0f)
			return;
		if (AbstractWorldObject.IsAbstractCharacter(awo)) {
			AbstractCharacter ac = (AbstractCharacter) awo;

			if (!ac.isAlive())
				return;

			int powerID = 0, effectID = 0;
			String powerName = "";
			if (effect.getPower() != null) {
				powerID = effect.getPower().getToken();
				powerName = effect.getPower().getName();
			} else {
				Logger.error("Power has returned null! Damage will fail to register! (" + (ac.getCurrentHitpoints()>0?"Alive)":"Dead)"));
			}

			if (effect.getEffect() != null) {
				effectID = effect.getEffect().getToken();
			} else {
				Logger.error("Effect has returned null! Damage will fail to register! (" + (ac.getCurrentHitpoints()>0?"Alive)":"Dead)"));
			}

			//see if target is immune to heals
			if (modAmount > 0f) {
				boolean skipImmune = false;
				// first tick of HoT going thru SM was removed in a later patch
				/*if (effect.getAction().getPowerAction() instanceof DirectDamagePowerAction) {
					ArrayList<ActionsBase> actions = effect.getPower().getActions();
					for (ActionsBase ab : actions) {
						AbstractPowerAction apa = ab.getPowerAction();
						if (apa instanceof DamageOverTimePowerAction)
							skipImmune = true;
					}
				}*/

				PlayerBonuses bonus = ac.getBonuses();
				if (!skipImmune && bonus.getFloat(ModType.BlackMantle, SourceType.Heal) >= trains) {
					ModifyHealthMsg mhm = new ModifyHealthMsg(source, ac, 0f, 0f, 0f, powerID, powerName, trains, effectID);
					mhm.setUnknown03(5); //set target is immune
					DispatchMessage.sendToAllInRange(ac, mhm);
					return;
				}
			}
			float mod = 0;

			//Modify health

			mod = ac.modifyHealth(modAmount, source, false);

			float cur = awo.getCurrentHitpoints();
			float maxAmount = awo.getHealthMax() - cur;

			AbstractNetMsg mhm = null;
			if (modAmount < 0 && cur < 0 && mod != 0)
				mhm = new ModifyHealthKillMsg(source, ac, modAmount, 0f, 0f, powerID, powerName, trains, effectID);
			else
				mhm = new ModifyHealthMsg(source, ac, modAmount, 0f, 0f, powerID, powerName, trains, effectID);

			if (effect instanceof DamageOverTimeJob) {
				if (mhm instanceof ModifyHealthMsg)
					((ModifyHealthMsg)mhm).setOmitFromChat(1);
				else if (mhm instanceof ModifyHealthKillMsg)
					((ModifyHealthKillMsg)mhm).setUnknown02(1);
			}

			//send the damage

			DispatchMessage.sendToAllInRange(ac, mhm);

			//			//send corpse if this kills a mob
			//			//TODO fix the someone misses blurb.
			//			if(awo instanceof Mob && awo.getHealth() <= 0) {
			//				CombatMessageMsg cmm = new CombatMessageMsg(null, 0, awo, 15);
			//				try {
			//					DispatchMessage.sendToAllInRange(ac, cmm);
			//				} catch (MsgSendException e) {
			//					Logger.error("MobCorpseSendError", e);
			//				}
			//			}
		} else if (awo.getObjectType().equals(GameObjectType.Building)) {

			Building b = (Building) awo;

			if (modAmount < 0 && (!b.isVulnerable()))
				return; //can't damage invul building

			int powerID = 0, effectID = 0;
			String powerName = "";
			if (effect.getPower() != null) {
				powerID = effect.getPower().getToken();
				powerName = effect.getPower().getName();
			} else
				Logger.error("Power has returned null! Damage will fail to register! (" + (b.getRank() == -1 ? "Standing)" : "Destroyed)"));

			if (effect.getEffect() != null) {
				effectID = effect.getEffect().getToken();
			} else
				Logger.error("Effect has returned null! Damage will fail to register! (" + (b.getRank() == -1 ? "Standing)" : "Destroyed)"));

			float mod = b.modifyHealth(modAmount, source);
			ModifyHealthMsg mhm = new ModifyHealthMsg(source, b, modAmount, 0f, 0f, powerID, powerName, trains, effectID);

			if (effect instanceof DamageOverTimeJob)
				mhm.setOmitFromChat(1);

			//send the damage

			DispatchMessage.sendToAllInRange(b, mhm);

		}
	}

	private float calculateDamage(AbstractCharacter source, float minDamage, float maxDamage, AbstractWorldObject awo, int trains) {

		// get range between min and max
		float range = maxDamage - minDamage;

		// Damage is calculated twice to average a more central point
		float damage = ThreadLocalRandom.current().nextFloat() * range;
		damage = (damage + (ThreadLocalRandom.current().nextFloat() * range)) / 2;

		// put it back between min and max
		damage += minDamage;

		Resists resists = null;
		// get resists
		if (AbstractWorldObject.IsAbstractCharacter(awo)) {
			AbstractCharacter ac = (AbstractCharacter) awo;
			resists = ac.getResists();
		} else if (awo.getObjectType().equals(GameObjectType.Building))
			resists = ((Building) awo).getResists();

		// calculate resists in if any
		if (resists != null) {
			if (AbstractWorldObject.IsAbstractCharacter(awo))
				damage = resists.getResistedDamage(source, (AbstractCharacter) awo, damageType, damage * -1, trains) * -1;
			else
				damage = resists.getResistedDamage(source, null, damageType, damage * -1, trains) * -1;
		}

		if (AbstractWorldObject.IsAbstractCharacter(awo)) {
			AbstractCharacter ac = (AbstractCharacter) awo;
			if (ac.isSit())
				damage *= 2.5f; // increase damage if sitting
		}

		return damage;
	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {

	}

	@Override
	public void applyBonus(Item item, int trains) {}
	@Override
	public void applyBonus(Building building, int trains) {}

	public static float getMinDamage(float baseMin, float intelligence, float spirit, float focus) {
		float min = baseMin * (((float)Math.pow(intelligence, 0.75f) * 0.0311f) + (0.02f * (int)focus) + ((float)Math.pow(spirit, 0.75f) * 0.0416f));
		return (float)((int)(min + 0.5f)); //round to nearest whole number
	}

	public static float getMaxDamage(float baseMax, float intelligence, float spirit, float focus) {
		float max = baseMax * (((float)Math.pow(intelligence, 0.75f) * 0.0785f) + (0.015f * (int)focus) + ((float)Math.pow(spirit, 0.75f) * 0.0157f));
		return (float)((int)(max + 0.5f)); //round to nearest whole number
	}

}
