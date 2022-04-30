// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum;
import engine.Enum.ItemType;
import engine.gameManager.ChatManager;
import engine.gameManager.CombatManager;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.LootMsg;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import static engine.math.FastMath.sqr;


public class StealPowerAction extends AbstractPowerAction {

	/**
	 * ResultSet Constructor
	 */
	public StealPowerAction(ResultSet rs) throws SQLException {
		super(rs);
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {

		if (source == null || awo == null || !(source.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) || !(awo.getObjectType().equals(Enum.GameObjectType.Item)))
			return;

		PlayerCharacter sourcePlayer = (PlayerCharacter) source;

		if (sourcePlayer.isSafeMode())
			return;

		if (!sourcePlayer.isAlive())
			return;

		//prevent stealing no steal mob loot
		if (awo instanceof MobLoot && ((MobLoot)awo).noSteal())
			return;

		Item tar = (Item) awo;
		AbstractWorldObject owner = (AbstractWorldObject) tar.getOwner();

		if (owner == null)
			return;


		AbstractCharacter ownerAC = null;
		if (AbstractWorldObject.IsAbstractCharacter(owner))
			ownerAC = (AbstractCharacter) owner;

			if (ownerAC != null)
				if (ownerAC.getLoc().distanceSquared(sourcePlayer.getLoc()) > sqr(MBServerStatics.LOOT_RANGE))
					return;

		//only steal from players or mobs

		if (owner.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) {

			PlayerCharacter ownerPC = (PlayerCharacter)owner;

			if (ownerPC.isSafeMode() || sourcePlayer.inSafeZone() || ownerPC.inSafeZone())
				return;

			if (ownerPC.getLoc().distanceSquared(sourcePlayer.getLoc()) > sqr(MBServerStatics.LOOT_RANGE))
				return;

			//dupe check, validate player has item
			if (!tar.validForInventory(ownerPC.getClientConnection(), ownerPC, ownerPC.getCharItemManager()))//pc.getCharItemManager()))
				return;

			//mark thief and target as player aggressive
			sourcePlayer.setLastPlayerAttackTime();
			ownerPC.setLastPlayerAttackTime();

			//Handle target attacking back if in combat and has no other target
			CombatManager.handleRetaliate(ownerAC, sourcePlayer);

		} else if (owner.getObjectType().equals(Enum.GameObjectType.Mob)) {
			sourcePlayer.setLastMobAttackTime(); //mark thief as mob aggressive
		} else
			return;

		ClientConnection origin = sourcePlayer.getClientConnection();

		if (origin == null)
			return;

		int amount = getAmountToSteal(tar);

		//test probability of steal success
		if (!stealSuccess(sourcePlayer, owner)) {
			ChatManager.chatPeekSteal(sourcePlayer, ownerAC, tar, false, false, -1);
			return;
		} else {
			ChatManager.chatPeekSteal(sourcePlayer, ownerAC, tar, true, false, amount);
			//TODO send steal failure success
		}

		//attempt transfer item
		CharacterItemManager myCIM = sourcePlayer.getCharItemManager();
		CharacterItemManager ownerCIM = ((AbstractCharacter)owner).getCharItemManager();
		if (myCIM == null || ownerCIM == null)
			return;

		if (tar.getItemBase().getType().equals(ItemType.GOLD)) {
			//stealing gold
			if (!myCIM.transferGoldToMyInventory((AbstractCharacter)owner, amount))
				return;
		} else {
			//stealing items
			if (ownerCIM.lootItemFromMe(tar, sourcePlayer, origin, true, amount) == null)
				return;
		}

		//send loot message to person stealing.
		LootMsg lm = new LootMsg(source.getObjectType().ordinal(), source.getObjectUUID(), owner.getObjectType().ordinal(), owner.getObjectUUID(), tar);
		Dispatch dispatch = Dispatch.borrow(sourcePlayer, lm);
		DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

		//update thief's inventory
		if (sourcePlayer.getCharItemManager() != null)
			sourcePlayer.getCharItemManager().updateInventory();

		//update victims inventory
		if (owner.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) {
			PlayerCharacter ownerPC = (PlayerCharacter) owner;

			if (ownerPC.getCharItemManager() != null)
				ownerPC.getCharItemManager().updateInventory();
		}

		//TODO if victim is trading, cancel trade window for both people involved in trade
	}

	@Override
	protected void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
	}

	protected static boolean stealSuccess(PlayerCharacter pc, AbstractWorldObject awo) {
		if (pc == null || awo == null || !AbstractWorldObject.IsAbstractCharacter(awo) || pc.getPowers() == null)
			return false;

		int levelDif = pc.getLevel() - ((AbstractCharacter)awo).getLevel();

		if (!pc.getPowers().containsKey(429396028))
			return false;

		CharacterPower cp = pc.getPowers().get(429396028);
		int trains = cp.getTotalTrains();

		float chance = 20 + (trains * 1.5f) + levelDif;
		chance = (chance < 5f) ? 5f : chance;
		chance = (chance > 85f) ? 85f : chance;

		float roll = ThreadLocalRandom.current().nextFloat() * 100f;

		return roll < chance;

	}

	//called to get amount of gold to steal between 0 and max gold
	protected static int getAmountToSteal(Item i) {
		if (i.getItemBase() != null && i.getItemBase().getUUID() == 7) {
			int amount = i.getNumOfItems();
			if (amount < 1)
				return -1;
			int a = ThreadLocalRandom.current().nextInt(amount + 1);
			int b = ThreadLocalRandom.current().nextInt(amount + 1);
			int c = ThreadLocalRandom.current().nextInt(amount + 1);
			return (a + b + c) / 3;
		} else
			return 0;
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc,
			int numTrains, ActionsBase ab, PowersBase pb, int duration) {
		// TODO Auto-generated method stub

	}
}
