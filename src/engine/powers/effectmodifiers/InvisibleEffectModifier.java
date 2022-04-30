// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.effectmodifiers;

import engine.Enum;
import engine.gameManager.SessionManager;
import engine.jobs.AbstractEffectJob;
import engine.net.client.ClientConnection;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InvisibleEffectModifier extends AbstractEffectModifier {

	public InvisibleEffectModifier(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {

		if (awo.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) {
			PlayerCharacter pc = (PlayerCharacter) awo;

			if (effect == null)
				return;

			PowersBase pb = effect.getPower();
			if (pb == null)
				return;

			ActionsBase ab = effect.getAction();

			if (ab == null)
				return;

			//send invis message to everyone around.
			ClientConnection origin = SessionManager.getClientConnection(pc);
			if (origin == null)
				return;

			ab.getDurationInSeconds(trains);

			pc.setHidden(trains);

			pc.setTimeStampNow("Invis");

		}
		else {
			Logger.error( "Cannot go invis on a non player.");
		}
	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {
		if (ac == null)
			return;
		PlayerBonuses bonus = ac.getBonuses();
		if (bonus != null)
			bonus.updateIfHigher(this, (float)trains);

		//remove pets
		if (ac.getObjectType().equals(Enum.GameObjectType.PlayerCharacter))
			((PlayerCharacter)ac).dismissPet();
	}

	@Override
	public void applyBonus(Item item, int trains) {}
	@Override
	public void applyBonus(Building building, int trains) {}
}
