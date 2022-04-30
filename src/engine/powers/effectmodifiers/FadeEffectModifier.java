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

public class FadeEffectModifier extends AbstractEffectModifier {

	public FadeEffectModifier(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {

	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {

	}

	@Override
	public void applyBonus(Item item, int trains) {}
	@Override
	public void applyBonus(Building building, int trains) {}
}
