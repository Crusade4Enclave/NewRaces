// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.effectmodifiers;

import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.jobs.AbstractEffectJob;
import engine.objects.*;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;


public class BlackMantleEffectModifier extends AbstractEffectModifier {

	public BlackMantleEffectModifier(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {

	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {
		PlayerBonuses bonus = ac.getBonuses();
		SourceType sourceType = SourceType.valueOf(this.type);
		
		if (sourceType == null){
			Logger.error("Bad Source Type for " + this.type);
			return;
		}

		if (this.type.equals("Heal"))
			bonus.setFloat(this, trains);
		else
			bonus.setBool(ModType.ImmuneTo, this.sourceType, true);
	}

	@Override
	public void applyBonus(Item item, int trains) {}
	@Override
	public void applyBonus(Building building, int trains) {}
}
