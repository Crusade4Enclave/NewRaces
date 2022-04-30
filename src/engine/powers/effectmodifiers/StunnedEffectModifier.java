// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.effectmodifiers;

import engine.Enum.GameObjectType;
import engine.jobs.AbstractEffectJob;
import engine.objects.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StunnedEffectModifier extends AbstractEffectModifier {

	public StunnedEffectModifier(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {

	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {
		if (ac.getObjectType() == GameObjectType.Mob) {
			Mob mob = (Mob) ac;
		}

		PlayerBonuses bonus = ac.getBonuses();
		bonus.setBool(this.modType,this.sourceType, true);
		ac.cancelOnStun();	
		ac.setIsCasting(false);
		ac.stopMovement(ac.getLoc());
	}

	@Override
	public void applyBonus(Item item, int trains) {}
	@Override
	public void applyBonus(Building building, int trains) {}
}
