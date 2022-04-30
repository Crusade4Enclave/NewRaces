// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.effectmodifiers;

import engine.Enum.DamageType;
import engine.jobs.AbstractEffectJob;
import engine.objects.*;
import engine.powers.DamageShield;

import java.sql.ResultSet;
import java.sql.SQLException;


public class DamageShieldEffectModifier extends AbstractEffectModifier {

	public DamageShieldEffectModifier(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {

	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {
		float amount; boolean usePercent;
		if (this.percentMod != 0) {
			amount = this.percentMod;
			usePercent = true;
		} else {
			amount = this.minMod;
			usePercent = false;
		}

		if (this.ramp > 0f) {
			float mod = this.ramp * trains;
			if (this.useRampAdd)
				amount += mod;
			else
				amount *= (1 + mod);
		}

		DamageType dt = DamageType.valueOf(this.type);
		if (dt != null) {
			DamageShield ds = new DamageShield(dt, amount, usePercent);
			PlayerBonuses bonus = ac.getBonuses();
			if (bonus != null)
				bonus.addDamageShield(this, ds);
		}
	}

	@Override
	public void applyBonus(Item item, int trains) {}
	@Override
	public void applyBonus(Building building, int trains) {}
}
