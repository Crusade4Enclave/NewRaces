// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum.BuildingGroup;
import engine.Enum.GameObjectType;
import engine.gameManager.ChatManager;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SpireDisablePowerAction extends AbstractPowerAction {

	/**
	 * ResultSet Constructor
	 */
	public SpireDisablePowerAction(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
		if (awo == null)
			return;

		if (source == null)
			return;

		PlayerCharacter pc = null;

		if (source.getObjectType() == GameObjectType.PlayerCharacter)
			pc = (PlayerCharacter)source;
		else
			return;

		if (awo.getObjectType() != GameObjectType.Building)
			return;


		//Check if Building is Spire.

		Building spire = (Building)awo;

		if ((spire.getBlueprintUUID() == 0) ||
				(spire.getBlueprint() != null && spire.getBlueprint().getBuildingGroup() != BuildingGroup.SPIRE)) {
			ChatManager.chatSystemError((PlayerCharacter)source, "This Building is not a spire.");
			return;
		}

		if (!spire.isSpireIsActive())
			return;

		spire.disableSpire(false);

		if (trains > 20)
			trains = 20;

		long duration = trains * 4500 + 30000;
		spire.setTimeStamp("DISABLED", System.currentTimeMillis() + duration);



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
