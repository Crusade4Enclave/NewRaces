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


package engine.powers.effectmodifiers;

import engine.Enum;
import engine.Enum.DamageType;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.gameManager.ChatManager;
import engine.jobs.AbstractEffectJob;
import engine.jobs.DamageOverTimeJob;
import engine.net.DispatchMessage;
import engine.net.client.msg.ModifyHealthMsg;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.poweractions.AbstractPowerAction;
import engine.powers.poweractions.DamageOverTimePowerAction;
import engine.powers.poweractions.DirectDamagePowerAction;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class StaminaEffectModifier extends AbstractEffectModifier {

	private DamageType damageType;

	public StaminaEffectModifier(ResultSet rs) throws SQLException {
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
			Logger.error( "_applyEffectModifier(): NULL AWO passed in.");
			return;
		}

		if (effect == null) {
			Logger.error( "_applyEffectModifier(): NULL AbstractEffectJob passed in.");
			return;
		}

		if (!AbstractWorldObject.IsAbstractCharacter(awo))
			return;
		AbstractCharacter awoac = (AbstractCharacter) awo;

		float modAmount = 0f;

		// Modify Stamina by percent
		if (this.percentMod != 0f) {
			float mod = 1f;
			if (this.useRampAdd)
				mod = (this.percentMod + (this.ramp * trains)) / 100;
			else
				mod = (this.percentMod * (1 + (this.ramp * trains))) / 100;
			modAmount = mod * awoac.getStaminaMax();
			if (awoac.isSit())
				modAmount *= 2.5f;

			//debug for spell damage and atr
			if (source.getDebug(16) && source.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) {
				PlayerCharacter pc = (PlayerCharacter) source;
				String smsg = "Percent Damage: " + mod * 100 + '%';
				ChatManager.chatSystemInfo(pc, smsg);
			}
		}

		// Modify Stamina by min/max amount
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
			if (source.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) {
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
			}
			modAmount = calculateDamage(source, awoac, min, max, awo, trains);
			PlayerBonuses bonus = source.getBonuses();

			// Apply any power effect modifiers (such as stances)
			if (bonus != null)
				modAmount *= (1 + (bonus.getFloatPercentAll(ModType.PowerDamageModifier, SourceType.None)));
		}
		if (modAmount == 0f)
			return;
		if (AbstractWorldObject.IsAbstractCharacter(awo)) {
			AbstractCharacter ac = (AbstractCharacter) awo;
			int powerID = 0, effectID = 0;
			String powerName = "";
			if (effect.getPower() != null) {
				powerID = effect.getPower().getToken();
				powerName = effect.getPower().getName();
			}
			if (effect.getEffect() != null) {
				effectID = effect.getEffect().getToken();
			}

			//see if target is immune to heals
			if (modAmount > 0f) {
				boolean skipImmune = false;
				if (effect.getAction().getPowerAction() instanceof DirectDamagePowerAction) {
					ArrayList<ActionsBase> actions = effect.getPower().getActions();
					for (ActionsBase ab : actions) {
						AbstractPowerAction apa = ab.getPowerAction();
						if (apa instanceof DamageOverTimePowerAction)
							skipImmune = true;
					}
				}
				PlayerBonuses bonus = ac.getBonuses();
				if (!skipImmune && bonus.getFloat(ModType.BlackMantle, SourceType.Heal) >= trains) {
					ModifyHealthMsg mhm = new ModifyHealthMsg(source, ac, 0f, 0f, 0f, powerID, powerName, trains, effectID);
					mhm.setUnknown03(5); //set target is immune
					DispatchMessage.sendToAllInRange(ac, mhm);

					return;
				}
			}

			ac.modifyStamina(modAmount, source);

			ModifyHealthMsg mhm = new ModifyHealthMsg(source, ac, 0f, 0f, modAmount, powerID, powerName, trains,
					effectID);
			if (effect instanceof DamageOverTimeJob)
				mhm.setOmitFromChat(1);
			DispatchMessage.sendToAllInRange(ac, mhm);
		}
	}

	private float calculateDamage(AbstractCharacter source, AbstractCharacter target, float minDamage, float maxDamage, AbstractWorldObject awo, int trains) {

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
		} else if (awo.getObjectType().equals(Enum.GameObjectType.Building))
			resists = ((Building) awo).getResists();

		// calculate resists in if any
		if (resists != null)
			damage = resists.getResistedDamage(source, target, damageType, damage * -1, trains) * -1;

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
}
