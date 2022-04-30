// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum;
import engine.gameManager.SessionManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.RecvSummonsRequestMsg;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SummonPowerAction extends AbstractPowerAction {

	/**
	 * ResultSet Constructor
	 */
	public SummonPowerAction(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab,
			PowersBase pb) {

		if (source == null || awo == null || !(awo.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) || !(source.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)))
			return;

		PlayerCharacter target = (PlayerCharacter) awo;

		ClientConnection conn = SessionManager.getClientConnection(target);

		if (conn == null)
			return;

		// TODO get location of summoning player
		Zone zone = ZoneManager.findSmallestZone(source.getLoc());
		String location = "Somewhere";

		if (zone != null)
			location = zone.getName();

		RecvSummonsRequestMsg rsrm = new RecvSummonsRequestMsg(source.getObjectType().ordinal(), source.getObjectUUID(), source.getFirstName(),
				location, false);

		Dispatch dispatch = Dispatch.borrow(target, rsrm);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

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
