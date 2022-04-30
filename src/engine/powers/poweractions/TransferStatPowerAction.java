// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum;
import engine.Enum.DamageType;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.gameManager.ChatManager;
import engine.math.Vector3fImmutable;
import engine.net.AbstractNetMsg;
import engine.net.DispatchMessage;
import engine.net.client.msg.ModifyHealthKillMsg;
import engine.net.client.msg.ModifyHealthMsg;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import engine.powers.effectmodifiers.HealthEffectModifier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class TransferStatPowerAction extends AbstractPowerAction {

	protected String effectID;
	protected boolean transferFromHealth = false;
	protected boolean transferFromMana = false;
	protected boolean transferFromStamina = false;
	protected boolean transferToHealth = false;
	protected boolean transferToMana = false;
	protected boolean transferToStamina = false;
	protected float transferAmount;
	protected float transferRamp;
	protected boolean transferRampAdd;
	protected float transferEfficiency;
	protected float transferEfficiencyRamp;
	protected boolean transferEfficiencyRampAdd;
	protected boolean targetToCaster;
	protected DamageType damageType;
	protected EffectsBase effect;

	public TransferStatPowerAction(ResultSet rs, HashMap<String, EffectsBase> effects) throws SQLException {
		super(rs);
		this.effectID = rs.getString("effectID");
		String st = rs.getString("transferFromType");
		if (st.equals("HEALTH"))
			this.transferFromHealth = true;
		else if (st.equals("MANA"))
			this.transferFromMana = true;
		else
			this.transferFromStamina = true;
		st = rs.getString("transferToType");
		if (st.equals("HEALTH"))
			this.transferToHealth = true;
		else if (st.equals("MANA"))
			this.transferToMana = true;
		else
			this.transferToStamina = true;
		this.transferAmount = rs.getFloat("transferAmount");
		this.transferRamp = rs.getFloat("transferRamp");
		this.transferEfficiency = rs.getFloat("transferEfficiency");
		this.transferEfficiencyRamp = rs.getFloat("transferEfficiencyRamp");
		int flags = rs.getInt("flags");
		this.transferRampAdd = ((flags & 4096) != 0) ? true : false;
		this.transferEfficiencyRampAdd = ((flags & 8192) != 0) ? true : false;
		this.targetToCaster = ((flags & 16384) != 0) ? true : false;
		this.effect = effects.get(this.effectID);
		try {
			String damageString = rs.getString("damageType");
			// Damage type can sometimes be null in the DB.

			if (damageString.isEmpty() == false)
				this.damageType = DamageType.valueOf(damageString);
		} catch (Exception e) {
			this.damageType = null;
		}
	}

	public String getEffectID() {
		return this.effectID;
	}

	public boolean transferFromHealth() {
		return this.transferFromHealth;
	}

	public boolean transferFromMana() {
		return this.transferFromMana;
	}

	public boolean transferFromStamina() {
		return this.transferFromStamina;
	}

	public boolean transferToHealth() {
		return this.transferToHealth;
	}

	public boolean transferToMana() {
		return this.transferToMana;
	}

	public boolean transferToStamina() {
		return this.transferToStamina;
	}

	public EffectsBase getEffect() {
		return this.effect;
	}

	public float getTransferAmount(float trains) {
		//		if (this.transferRampAdd)
		return this.transferAmount + (this.transferRamp * trains);
		//		else
		//			return this.transferAmount * (1 + (this.transferRamp * trains));
	}

	public float getTransferEfficiency(float trains) {
		return this.transferEfficiency + (this.transferEfficiencyRamp * trains);
	}

	public boolean targetToCaster() {
		return this.targetToCaster;
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
		this.__startAction(source, awo, trains, ab, pb);
	}

	//Added for dependancy check on TransferStatOTPowerAction
	protected void __startAction(AbstractCharacter source, AbstractWorldObject awo, int trains, ActionsBase ab, PowersBase pb) {
		this.runAction(source, awo, trains, ab, pb);
	}

	public void runAction(AbstractCharacter source, AbstractWorldObject awo, int trains, ActionsBase ab, PowersBase pb) {
		if (source == null || awo == null || ab == null || pb == null)
			return;

		if (!source.isAlive() || !awo.isAlive())
			return;

		AbstractWorldObject fromAwo;
		AbstractWorldObject toAwo;
		if (this.targetToCaster) {
			fromAwo = awo;
			toAwo = source;
		} else {
			fromAwo = source;
			toAwo = awo;
		}

	

		if (AbstractWorldObject.IsAbstractCharacter(fromAwo) && AbstractWorldObject.IsAbstractCharacter(toAwo)) {
			AbstractCharacter from = (AbstractCharacter) fromAwo;
			AbstractCharacter to = (AbstractCharacter) toAwo;

			//get amount to drain
			float fromAmount = getTransferAmount(trains);

			//modify for resists if needed
			if (this.damageType != null) {
				Resists resists = from.getResists();
				if (resists != null)
					fromAmount = resists.getResistedDamage(to, from, this.damageType, fromAmount * -1, trains) * -1;
			}

			float min = fromAmount;// * (getTransferEfficiency(trains) / 100);
			float max = min;
			float damage = 0f;

			if (source.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) {
				PlayerCharacter pc = (PlayerCharacter) source;
				float focus;
				CharacterSkill skill = pc.getSkills().get(pb.getSkillName());
				if (skill == null)
					focus = CharacterSkill.getQuickMastery(pc, pb.getSkillName());
				else
					focus = skill.getModifiedAmount();

				//TODO fix this formula later
				float intt = (pc.getStatIntCurrent() >= 1) ? (float)pc.getStatIntCurrent() : 1f;
				float spi = (pc.getStatSpiCurrent() >= 1) ? (float)pc.getStatSpiCurrent() : 1f;
				//				min *= (intt * 0.0045 + 0.055 * (float)Math.sqrt(intt - 0.5) + spi * 0.006 + 0.07 * (float)Math.sqrt(spi - 0.5) + 0.02 * (int)focus);
				//				max *= (intt * 0.0117 + 0.13 * (float)Math.sqrt(intt - 0.5) + spi * 0.0024 + (float)Math.sqrt(spi - 0.5) * 0.021 + 0.015 * (int)focus);
				//				min *= (0.62 + 0.0192 * pc.getStatSpiCurrent() + 0.00415 * pc.getStatIntCurrent() + 0.015 * focus) / 2;
				//				max *= (0.62 + 0.0192 * pc.getStatIntCurrent() + 0.00415 * pc.getStatSpiCurrent() + 0.015 * focus) / 2;
				min = HealthEffectModifier.getMinDamage(min, intt, spi, focus);
				max = HealthEffectModifier.getMaxDamage(max, intt, spi, focus);

				// get range between min and max
				float range = max - min;

				//debug for spell damage and atr
				if (pc.getDebug(16)) {
					String smsg = "Damage: " + (int)Math.abs(min) + " - " + (int)Math.abs(max);
					ChatManager.chatSystemInfo(pc, smsg);
				}

				// Damage is calculated twice to average a more central point
				damage = ThreadLocalRandom.current().nextFloat() * range;
				damage = (damage + (ThreadLocalRandom.current().nextFloat() * range)) / 2;

				// put it back between min and max
				damage += min;
			}

			// Apply any power effect modifiers (such as stances)
			PlayerBonuses bonus = source.getBonuses();
			if (bonus != null)
				damage *= (1 + bonus.getFloatPercentAll(ModType.PowerDamageModifier, SourceType.None));

			//get amount to transfer
			fromAmount = damage;
			float toAmount = fromAmount * (getTransferEfficiency(trains) / 100);

			//get max amount to transfer, don't give more then the target has
			float maxDrain;
			if (this.transferFromHealth)
				maxDrain = from.getCurrentHitpoints();
			else if (this.transferFromMana)
				maxDrain = from.getMana();
			else
				maxDrain = from.getStamina();
			if (toAmount > maxDrain)
				toAmount = maxDrain;

			//prep messages for transfer
			int powerID = pb.getToken();
			int effectID = 496519310;
			String powerName = pb.getName();
			ModifyHealthMsg mhmTo;
			//			ModifyHealthMsg mhmFrom;
			AbstractNetMsg mhmFrom = null;
			
			//stop if target is immune to drains
			if ( from.getBonuses().getBool(ModType.ImmuneTo, SourceType.Drain)) {
				ModifyHealthMsg mhm = new ModifyHealthMsg(source, to, 0f, 0f, 0f, powerID, powerName, trains, effectID);
				mhm.setUnknown03(5); //set target is immune
				DispatchMessage.sendToAllInRange(from, mhm);
				return;
			}

			//apply transfer bonus
			if (this.transferToHealth) {
				to.modifyHealth(toAmount, source, false);
				mhmTo = new ModifyHealthMsg(source, to, toAmount, 0f, 0f, powerID, powerName, trains, effectID);
			} else if (this.transferToMana) {
				to.modifyMana(toAmount, source);
				mhmTo = new ModifyHealthMsg(source, to, 0f, toAmount, 0f, powerID, powerName, trains, effectID);
			} else {
				to.modifyStamina(toAmount, source);
				mhmTo = new ModifyHealthMsg(source, to, 0f, 0f, toAmount, powerID, powerName, trains, effectID);
			}

			//subtract transfer amount
			if (this.transferFromHealth) {
				float modFrom = from.modifyHealth(-fromAmount, source, false);
				float cur = from.getHealth();
				if (cur < 0 && modFrom != 0)
					mhmFrom = new ModifyHealthKillMsg(source, from, -fromAmount, 0f, 0f, powerID, powerName, trains, effectID);
				else
					mhmFrom = new ModifyHealthMsg(source, from, -fromAmount, 0f, 0f, powerID, powerName, trains, effectID);
			} else if (this.transferFromMana) {
				from.modifyMana(-fromAmount, source);
				mhmFrom = new ModifyHealthMsg(source, from, 0f, -fromAmount, 0f, powerID, powerName, trains, effectID);
			} else {
				from.modifyStamina(-fromAmount, source);
				mhmFrom = new ModifyHealthMsg(source, from, 0f, 0f, -fromAmount, powerID, powerName, trains, effectID);
			}

			DispatchMessage.sendToAllInRange(to, mhmTo);
			DispatchMessage.sendToAllInRange(from, mhmFrom);

		}
	}

	@Override
	protected void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc,
			int numTrains, ActionsBase ab, PowersBase pb, int duration) {
		// TODO Auto-generated method stub

	}
}
