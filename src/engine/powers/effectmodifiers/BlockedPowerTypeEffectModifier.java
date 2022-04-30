// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.effectmodifiers;

import engine.Enum.ModType;
import engine.jobs.AbstractEffectJob;
import engine.objects.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;


public class BlockedPowerTypeEffectModifier extends AbstractEffectModifier {

	public BlockedPowerTypeEffectModifier(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {

	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {
		PlayerBonuses bonus = ac.getBonuses();
		bonus.setBool(this.modType,this.sourceType, true);
		
		
		for (String effect : ac.getEffects().keySet()){
			Effect eff = ac.getEffects().get(effect);
			ModType toBlock = ModType.None;
			
			switch (this.sourceType){
			case Invisible:
				toBlock = ModType.Invisible;
				break;
			}
			
			HashSet<AbstractEffectModifier> aemList = eff.getEffectModifiers();
			for (AbstractEffectModifier aem : aemList ){
				if (aem.modType.equals(toBlock)){
					ac.endEffect(effect);
				}
			}
			
		}
	}

	@Override
	public void applyBonus(Item item, int trains) {}
	@Override
	public void applyBonus(Building building, int trains) {}
}
