// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.ai.MobileFSM;
import engine.gameManager.MovementManager;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.PromptRecallMsg;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Mob;
import engine.objects.PlayerCharacter;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;

import java.sql.ResultSet;
import java.sql.SQLException;


public class RecallPowerAction extends AbstractPowerAction {

	public RecallPowerAction(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
		if (!AbstractWorldObject.IsAbstractCharacter(awo) || source == null)
			return;
		AbstractCharacter awoac = (AbstractCharacter)awo;

		if (awo.getObjectType().equals(GameObjectType.PlayerCharacter)) {

			PlayerCharacter pc = (PlayerCharacter) awo;

			if (pc.hasBoon())
				return;

			ClientConnection cc = pc.getClientConnection();

			if(source.getObjectUUID() != pc.getObjectUUID()) {
				pc.setTimeStampNow("PromptRecall");
				pc.setTimeStamp("LastRecallType",1); //recall to bind
				PromptRecallMsg promptRecallMsgmsg = new PromptRecallMsg();
				Dispatch dispatch = Dispatch.borrow(pc, promptRecallMsgmsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

			} else {
				MovementManager.translocate(awoac, awoac.getBindLoc(), null);
			}
		} else {
			Vector3fImmutable bindloc = awoac.getBindLoc();
			if (bindloc.x == 0.0f || bindloc.y == 0.0f)
				awoac.setBindLoc(MBServerStatics.startX, MBServerStatics.startY, MBServerStatics.startZ);
			awoac.teleport(awoac.getBindLoc());
			if (awoac.getObjectType() == GameObjectType.Mob){
				MobileFSM.setAwake((Mob)awoac,true);
				if (awoac.isAlive())
					WorldGrid.updateObject(awoac);
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
