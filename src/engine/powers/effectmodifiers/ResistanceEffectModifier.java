// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.effectmodifiers;

import engine.jobs.AbstractEffectJob;
import engine.objects.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResistanceEffectModifier extends AbstractEffectModifier {

	public ResistanceEffectModifier(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {

	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {
		Float amount = 0f;
		PlayerBonuses bonus = ac.getBonuses();
		if (this.percentMod != 0f) { //Stat Percent Modifiers
			if (this.useRampAdd)
				amount = this.percentMod + (this.ramp * trains);
			else
				amount = this.percentMod * (1 + (this.ramp * trains));
			amount = amount/100;
			bonus.addFloat(this, amount);
		} else { //Stat Modifiers
			if (this.useRampAdd)
				amount = this.minMod + (this.ramp * trains);
			else
				amount = this.minMod * (1 + (this.ramp * trains));
			bonus.addFloat(this, amount);
		}
	}

	@Override
	public void applyBonus(Item item, int trains) {}
	@Override
	public void applyBonus(Building building, int trains) {}
}
