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

public class NoModEffectModifier extends AbstractEffectModifier {

	public NoModEffectModifier(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {
		//TODO check if anything needs removed.
	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {
		PlayerBonuses bonus = ac.getBonuses();
		bonus.setBool(this.modType,this.sourceType,true);
		
		switch (this.sourceType){
		case Fly:
			if (!ac.getObjectType().equals(GameObjectType.PlayerCharacter))
				return;
			PlayerCharacter flyer = (PlayerCharacter)ac;
			
			if (flyer.getAltitude() > 0)
				flyer.update();
			PlayerCharacter.GroundPlayer(flyer);
			break;
			
		}
	}

	@Override
	public void applyBonus(Item item, int trains) {}
	@Override
	public void applyBonus(Building building, int trains) {}
}
