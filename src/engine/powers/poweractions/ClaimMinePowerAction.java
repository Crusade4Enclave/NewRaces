// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum;
import engine.gameManager.ChatManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ClaimMinePowerAction extends AbstractPowerAction {

	public ClaimMinePowerAction(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {

		if (source == null || awo == null)
			return;

		if (!(source.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)))
			return;

		PlayerCharacter playerCharacter = (PlayerCharacter) source;

		if (!(awo.getObjectType().equals(Enum.GameObjectType.Building)))
			return;

		Building mineBuilding = (Building)awo;

		if (mineBuilding.getRank() > 0)
			return;

		Mine mine = Mine.getMineFromTower(mineBuilding.getObjectUUID());

		if (mine == null)
			return;

		if (mine.claimMine(playerCharacter) == true)
			ChatManager.sendSystemMessage( (PlayerCharacter) source, "You successfully claimed this mine..");
		else
			ChatManager.sendSystemMessage( (PlayerCharacter) source, "Your attempt for to claim this mine failed.");
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
