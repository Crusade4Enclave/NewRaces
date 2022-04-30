// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum;
import engine.Enum.RunegateType;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.msg.PromptRecallMsg;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;

import static engine.math.FastMath.sqr;
import static engine.math.FastMath.sqrt;

public class RunegateTeleportPowerAction extends AbstractPowerAction {

	/**
	 * ResultSet Constructor
	 */
	public RunegateTeleportPowerAction(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {

		if (source == null || awo == null || !(awo .getObjectType().equals(Enum.GameObjectType.PlayerCharacter)))
			return;

		PlayerCharacter pc = (PlayerCharacter) awo;
		float dist = 9999999999f;
		Building rg = null;
		Vector3fImmutable rgLoc;

		for (Runegate runegate: Runegate.getRunegates()) {

			if ((runegate.getGateType() == RunegateType.OBLIV) ||
					(runegate.getGateType() == RunegateType.CHAOS))
				continue;

			for (Runegate thisGate : Runegate.getRunegates()) {

				rgLoc = thisGate.getGateType().getGateBuilding().getLoc();

				float distanceToRunegateSquared = source.getLoc().distanceSquared2D(rgLoc);

				if (distanceToRunegateSquared < sqr(dist)) {
					dist = sqrt(distanceToRunegateSquared);
					rg = thisGate.getGateType().getGateBuilding();
				}
			}
		}

		if(source.getObjectUUID() != pc.getObjectUUID()) {
			pc.setTimeStampNow("PromptRecall");
			pc.setTimeStamp("LastRecallType",0); //recall to rg

			if (rg != null) {
				PromptRecallMsg promptRecallMsgmsg = new PromptRecallMsg();
				Dispatch dispatch = Dispatch.borrow(pc, promptRecallMsgmsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
			}

		} else {
			if (rg != null) {
				pc.teleport(rg.getLoc());
				pc.setSafeMode();
			}
		}
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
