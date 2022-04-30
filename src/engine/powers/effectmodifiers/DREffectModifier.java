// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.effectmodifiers;

import engine.jobs.AbstractEffectJob;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Building;
import engine.objects.Item;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DREffectModifier extends AbstractEffectModifier {

	public DREffectModifier(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {

	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {
		//Defense Rating (defense bonus for armor)
	}

	@Override
	public void applyBonus(Item item, int trains) {
		if (item == null)
			return;
		String key; float amount = 0f;
		if (this.percentMod != 0f) {
			if (this.useRampAdd)
				amount = (this.percentMod + (this.ramp * trains)) / 100f;
			else
				amount = (this.percentMod * (1 + (this.ramp * trains))) / 100f;
			amount = amount/100;
			key = "DR.percent";
		} else {
			if (this.useRampAdd)
				amount = this.minMod + (this.ramp * trains);
			else
				amount = this.minMod * (1 + (this.ramp * trains));
			key = "DR";
		}
		item.addBonus(this, amount);
	}

	@Override
	public void applyBonus(Building building, int trains) {}
}
