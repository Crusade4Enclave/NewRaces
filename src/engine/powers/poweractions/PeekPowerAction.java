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
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.msg.LootWindowResponseMsg;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;


public class PeekPowerAction extends AbstractPowerAction {

	public PeekPowerAction(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {

		if (source == null || awo == null || !(source.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)))
			return;

		PlayerCharacter pc = null;
		if (source.getObjectType().equals(Enum.GameObjectType.PlayerCharacter))
			pc = (PlayerCharacter) source;

		AbstractCharacter target = null;
		if (AbstractWorldObject.IsAbstractCharacter(awo))
			target = (AbstractCharacter) awo;

		//test probability of successful peek
		boolean peekSuccess = peekSuccess(source, awo);
		if (peekSuccess) {
			ChatManager.chatPeekSteal(pc, target, null, true, peekDetect(source, awo), -1);
		} else {
			ChatManager.chatPeekSteal(pc, target, null, false, false, -1);
			return;
		}

		LootWindowResponseMsg lwrm = null;

		if (awo.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) {

			PlayerCharacter tar = (PlayerCharacter)awo;

			if (!tar.isAlive())
				return;

			lwrm = new LootWindowResponseMsg(tar.getObjectType().ordinal(), tar.getObjectUUID(), tar.getInventory(true));
		} else if (awo.getObjectType().equals(Enum.GameObjectType.Mob)) {

			Mob tar = (Mob) awo;

			if (!tar.isAlive())
				return;

			lwrm = new LootWindowResponseMsg(tar.getObjectType().ordinal(), tar.getObjectUUID(), tar.getInventory(true));
		}
		if (lwrm == null)
			return;

		Dispatch dispatch = Dispatch.borrow(pc, lwrm);
		DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
	}

	@Override
	protected void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
	}

	protected static boolean peekSuccess(AbstractCharacter pc, AbstractWorldObject awo) {
		if (pc == null || awo == null || !AbstractWorldObject.IsAbstractCharacter(awo) || pc.getPowers() == null)
			return false;

		int levelDif = pc.getLevel() - ((AbstractCharacter)awo).getLevel();

		if (!pc.getPowers().containsKey(429494332))
			return false;

		CharacterPower cp = pc.getPowers().get(429494332);
		int trains = cp.getTotalTrains();

		float chance = 30 + (trains * 1.5f) + levelDif;
		chance = (chance < 5f) ? 5f : chance;
		chance = (chance > 95f) ? 95f : chance;

		float roll = ThreadLocalRandom.current().nextFloat() * 100f;

		return roll < chance;

	}


	protected static boolean peekDetect(AbstractCharacter pc, AbstractWorldObject awo) {
		if (pc == null || awo == null || !AbstractWorldObject.IsAbstractCharacter(awo) || pc.getPowers() == null)
			return false;

		int levelDif = pc.getLevel() - ((AbstractCharacter)awo).getLevel();

		if (!pc.getPowers().containsKey(429494332))
			return false;

		CharacterPower cp = pc.getPowers().get(429494332);
		int trains = cp.getTotalTrains();

		// check if peek is detected
		float chance = 30 + (40-trains)*1.5f - levelDif;
		chance = (chance < 5f) ? 5f : chance;
		chance = (chance > 95f) ? 95f : chance;

		float roll = ThreadLocalRandom.current().nextFloat() * 100f;
		return roll < chance;

	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc,
			int numTrains, ActionsBase ab, PowersBase pb, int duration) {
		// TODO Auto-generated method stub

	}
}
