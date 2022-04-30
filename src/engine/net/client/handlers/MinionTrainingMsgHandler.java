package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.InterestManagement.WorldGrid;
import engine.ai.MobileFSM;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * @Author:
 * @Summary: Processes application protocol message which
 * processes training of minions in guard barracks
 */

public class MinionTrainingMsgHandler extends AbstractClientMsgHandler {

	public static HashMap<Integer, ArrayList<Integer>> _minionsByCaptain = null;

	public MinionTrainingMsgHandler() {
		super(MinionTrainingMessage.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		MinionTrainingMessage minionMsg = (MinionTrainingMessage) baseMsg;

		PlayerCharacter player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return true;

		if (minionMsg.getNpcType() == Enum.GameObjectType.NPC.ordinal()){

			NPC npc = NPC.getFromCache(minionMsg.getNpcID());

			if (npc == null)
				return true;

			Building b = BuildingManager.getBuildingFromCache(minionMsg.getBuildingID());

			if (b == null)
				return true;

			//clear minion

			if (npc.minionLock.writeLock().tryLock()) {
				try {
					if (minionMsg.getType() == 2) {

						Mob toRemove = Mob.getFromCache(minionMsg.getUUID());

						if (!npc.getSiegeMinionMap().containsKey(toRemove))
							return true;

						toRemove.setState(MobileFSM.STATE.Disabled);
						npc.getSiegeMinionMap().remove(toRemove);

						//toRemove.disableIntelligence();
						WorldGrid.RemoveWorldObject(toRemove);

						if (toRemove.getParentZone() != null)
							toRemove.getParentZone().zoneMobSet.remove(toRemove);

						DbManager.removeFromCache(toRemove);
						PlayerCharacter petOwner = toRemove.getOwner();

						if (petOwner != null) {
							petOwner.setPet(null);
							toRemove.setOwner(null);
							PetMsg petMsg = new PetMsg(5, null);
							Dispatch dispatch = Dispatch.borrow(petOwner, petMsg);
							DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
						}

						// we Found the move to remove, lets break the for loop so it doesnt look for more.

						ManageCityAssetsMsg mca1 = new ManageCityAssetsMsg(player, b);
						mca1.actionType = 3;
						mca1.setTargetType(b.getObjectType().ordinal());
						mca1.setTargetID(b.getObjectUUID());

						mca1.setTargetType3(npc.getObjectType().ordinal());
						mca1.setTargetID3(npc.getObjectUUID());
						mca1.setAssetName1(b.getName());
						mca1.setUnknown54(1);

						Dispatch dispatch = Dispatch.borrow(player, mca1);
						DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

						ManageNPCMsg mnm = new ManageNPCMsg(npc);
						dispatch = Dispatch.borrow(player, mnm);
						DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

						//Add Minion
					}
					else {
						Zone zone = npc.getParentZone();

						if (zone == null)
							return true;

						int maxSlots = 3;

						if (npc.getContractID() == 842)
							maxSlots = 1;

						if (npc.getSiegeMinionMap().size() == maxSlots)
							return true;

						int mobBase;

						switch (minionMsg.getMinion()){
							case 9:
								mobBase = 13171;
								break;
							case 2:
								mobBase = 13758;
								break;
							case 3:
								mobBase = 13757;
								break;
							case 4:
								mobBase = 2111;
								break;
							case 5:
								mobBase = 12402;
								break;
							case 6:
								mobBase = 2113;
								break;
							default:
								mobBase = minionMsg.getMinion();
						}

						if (mobBase == 0)
							return true;

						Mob toCreate = npc.createSiegeMob(mobBase, npc.getGuild(), zone, b.getLoc(), (short) 1);

						if (toCreate == null)
							return true;

						//   toCreate.despawn();
						if (toCreate != null) {
							toCreate.setSpawnTime(60 * 15);
							toCreate.setTimeToSpawnSiege(System.currentTimeMillis() + (60 * 15 * 1000));
							toCreate.setDeathTime(System.currentTimeMillis());
							toCreate.setState(MobileFSM.STATE.Respawn);
						}
					}

					ManageNPCMsg mnm = new ManageNPCMsg(npc);
					mnm.setMessageType(1);
					Dispatch dispatch = Dispatch.borrow(player, mnm);
					DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

				} finally {
					npc.minionLock.writeLock().unlock();
				}
			}

		}else if (minionMsg.getNpcType() == Enum.GameObjectType.Mob.ordinal()){

			Mob npc = Mob.getFromCache(minionMsg.getNpcID());

			if (npc == null)
				return true;

			Building b = BuildingManager.getBuildingFromCache(minionMsg.getBuildingID());

			if (b == null)
				return true;

			//clear minion

			if (npc.minionLock.writeLock().tryLock()) {
				try {
					if (minionMsg.getType() == 2) {

						Mob toRemove = Mob.getFromCache(minionMsg.getUUID());
						if (!npc.getSiegeMinionMap().containsKey(toRemove))
							return true;

						if (!DbManager.MobQueries.REMOVE_FROM_GUARDS(npc.getObjectUUID(), toRemove.getMobBaseID(), npc.getSiegeMinionMap().get(toRemove)))
							return true;

						toRemove.setState(MobileFSM.STATE.Disabled);
						npc.getSiegeMinionMap().remove(toRemove);

						//toRemove.disableIntelligence();
						WorldGrid.RemoveWorldObject(toRemove);

						if (toRemove.getParentZone() != null)
							toRemove.getParentZone().zoneMobSet.remove(toRemove);

						DbManager.removeFromCache(toRemove);
						PlayerCharacter petOwner = toRemove.getOwner();

						if (petOwner != null) {
							petOwner.setPet(null);
							toRemove.setOwner(null);
							PetMsg petMsg = new PetMsg(5, null);
							Dispatch dispatch = Dispatch.borrow(petOwner, petMsg);
							DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
						}

						// we Found the move to remove, lets break the for loop so it doesnt look for more.

						ManageCityAssetsMsg mca1 = new ManageCityAssetsMsg(player, b);
						mca1.actionType = 3;
						mca1.setTargetType(b.getObjectType().ordinal());
						mca1.setTargetID(b.getObjectUUID());

						mca1.setTargetType3(npc.getObjectType().ordinal());
						mca1.setTargetID3(npc.getObjectUUID());
						mca1.setAssetName1(b.getName());
						mca1.setUnknown54(1);

						Dispatch dispatch = Dispatch.borrow(player, mca1);
						DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);;

						ManageNPCMsg mnm = new ManageNPCMsg(npc);
						dispatch = Dispatch.borrow(player, mnm);
						DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

						//Add Minion
					}
					else {
						Zone zone = npc.getParentZone();

						if (zone == null)
							return true;

						int maxSlots = 5;

						if (npc.getContract().getContractID() == 842)
							maxSlots = 1;

						switch (npc.getRank()){
							case 1:
							case 2:
								maxSlots = 1;
								break;
							case 3:
								maxSlots = 2;
								break;
							case 4:
							case 5:
								maxSlots = 3;
								break;
							case 6:
								maxSlots = 4;
								break;
							case 7:
								maxSlots = 5;
								break;
						}

						if (npc.getSiegeMinionMap().size() == maxSlots)
							return true;

						int mobBase = npc.getMobBaseID();
						
						if (mobBase == 0)
							return true;

						String pirateName = NPC.getPirateName(mobBase);

						if (!DbManager.MobQueries.ADD_TO_GUARDS(npc.getObjectUUID(), mobBase, pirateName, npc.getSiegeMinionMap().size() + 1))
							return true;

						Mob toCreate = npc.createGuardMob(mobBase, npc.getGuild(), zone, b.getLoc(), npc.getLevel(),pirateName);

						if (toCreate == null)
							return true;

						//   toCreate.despawn();
						if (toCreate != null) {
							toCreate.setTimeToSpawnSiege(System.currentTimeMillis() + MBServerStatics.FIFTEEN_MINUTES);
							toCreate.setDeathTime(System.currentTimeMillis());
							toCreate.setState(MobileFSM.STATE.Respawn);
						}
					}

					ManageNPCMsg mnm = new ManageNPCMsg(npc);
					mnm.setMessageType(1);
					Dispatch dispatch = Dispatch.borrow(player, mnm);
					DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

				}catch (Exception e){
					Logger.error(e);
				}finally {
				
					npc.minionLock.writeLock().unlock();
				}
			}

		}
		return true;
	}

}