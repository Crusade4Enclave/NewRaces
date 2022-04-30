// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum;
import engine.Enum.ProtectionState;
import engine.exception.MsgSendException;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.PowersManager;
import engine.gameManager.SessionManager;
import engine.net.*;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.objects.*;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.concurrent.ConcurrentHashMap;


public class TrainMsg extends ClientNetMsg {

	private int npcType;
	private int npcID;
	private int unknown01 = 1;
	private int trainCost01; //why two trainer costs?
	private int trainCost02; //why two trainer costs?
	private boolean isSkill; //true: skill; false: power
	private int token;
	private boolean unknown02 = true;
	private String ok = "";
	private int unknown03 = 0;

	/**
	 * This is the general purpose constructor.
	 */
	public TrainMsg() {
		super(Protocol.TRAINSKILL);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TrainMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.TRAINSKILL, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.npcType);
		writer.putInt(this.npcID);
		writer.putInt(this.unknown01);
		writer.putInt(trainCost01);
		writer.putInt(trainCost02);
		writer.put((this.isSkill == true) ? (byte)0x01 : (byte)0x00);
		writer.putInt(this.token);
		writer.put((this.unknown02 == true) ? (byte)0x01 : (byte)0x00);
		writer.putString(this.ok);
		writer.putInt(this.unknown03);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.npcType = reader.getInt();
		this.npcID = reader.getInt();
		this.unknown01 = reader.getInt();
		this.trainCost01 = reader.getInt();
		this.trainCost02 = reader.getInt();
		this.isSkill = (reader.get() == (byte)0x01) ? true : false;
		this.token = reader.getInt();
		this.unknown02 = (reader.get() == (byte)0x01) ? true : false;
		this.ok = reader.getString();
		this.unknown03 = reader.getInt();
	}

	public int getToken() {
		return this.token;
	}

	public boolean isSkill() {
		return this.isSkill;
	}

	public int getNpcType() {
		return this.npcType;
	}

	public int getNpcID() {
		return this.npcID;
	}

	public static void train(TrainMsg msg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter playerCharacter = SessionManager.getPlayerCharacter(origin);
		Dispatch dispatch;

		if (playerCharacter == null)
			return;

        NPC npc = NPC.getFromCache(msg.npcID);

		if (npc == null)
			return;

		if (origin.trainLock.tryLock()){
			try{
				Item gold = playerCharacter.getCharItemManager().getGoldInventory();

				if (gold == null)
					return;

				if (!gold.validForInventory(origin, playerCharacter, playerCharacter.getCharItemManager()))
					return;

				boolean canTrain = false;
                if (msg.isSkill) {

					//Get skill
                    SkillsBase sb = DbManager.SkillsBaseQueries.GET_BASE_BY_TOKEN(msg.token);
					ConcurrentHashMap<String, CharacterSkill> skills = playerCharacter.getSkills();

					if (sb == null || skills == null)
						return;

					CharacterSkill sk = skills.get(sb.getName());

					if (sk == null)
						return;

					if (sk.getSkillsBase().getToken() == 40661438){
						int maxValue = 15;


						if (MaxSkills.MaxSkillsSet.get(252647) != null)
							for (MaxSkills maxSkills: MaxSkills.MaxSkillsSet.get(252647)){
								if (maxSkills.getSkillToken() != sk.getToken())
									continue;

								if (maxSkills.getSkillLevel() > npc.getLevel())
									continue;
								maxValue += maxSkills.getMaxSkillPercent();
							}
						if (maxValue> sk.getModifiedAmountBeforeMods()){
							canTrain = true;
						}

					}
							if (canTrain == false)
						if  (npc.getContract() != null && npc.getContract().getExtraRune() != 0){
							int maxValue = 15;


							if (MaxSkills.MaxSkillsSet.get(npc.getContract().getExtraRune()) != null)
								for (MaxSkills maxSkills: MaxSkills.MaxSkillsSet.get(npc.getContract().getExtraRune())){
									if (maxSkills.getSkillToken() != sk.getToken())
										continue;

									if (maxSkills.getSkillLevel() > npc.getLevel())
										continue;
									maxValue += maxSkills.getMaxSkillPercent();
								}
							if (maxValue > sk.getModifiedAmountBeforeMods()){
								canTrain = true;
							}


						}
							if (canTrain == false){
							int maxValue = 15;
							if (MaxSkills.MaxSkillsSet.get(npc.getContractID()) != null)
								for (MaxSkills maxSkills: MaxSkills.MaxSkillsSet.get(npc.getContractID())){
									if (maxSkills.getSkillToken() != sk.getToken())
										continue;

									if (maxSkills.getSkillLevel() > npc.getLevel())
										continue;
									maxValue += maxSkills.getMaxSkillPercent();
								}
							if (maxValue > sk.getModifiedAmountBeforeMods()){
								canTrain = true;
							}
						}
							
							if (canTrain == false){
								int maxValue = 15;
								if (MaxSkills.MaxSkillsSet.get(npc.getContract().getClassID()) != null)
									for (MaxSkills maxSkills: MaxSkills.MaxSkillsSet.get(npc.getContract().getClassID())){
										if (maxSkills.getSkillToken() != sk.getToken())
											continue;

										if (maxSkills.getSkillLevel() > npc.getLevel())
											continue;
										maxValue += maxSkills.getMaxSkillPercent();
									}
								if (maxValue > sk.getModifiedAmountBeforeMods()){
									canTrain = true;
								}
							}
							
							if (canTrain == false){
								int maxValue = 15;
								if (MaxSkills.MaxSkillsSet.get(npc.extraRune2) != null)
									for (MaxSkills maxSkills: MaxSkills.MaxSkillsSet.get(npc.getContract().getClassID())){
										if (maxSkills.getSkillToken() != sk.getToken())
											continue;

										if (maxSkills.getSkillLevel() > npc.getLevel())
											continue;
										maxValue += maxSkills.getMaxSkillPercent();
									}
								if (maxValue > sk.getModifiedAmountBeforeMods()){
									canTrain = true;
								}
							}
							
							if (canTrain == false){
								ChatManager.chatSystemError(playerCharacter, "NPC cannot train that skill any higher");
								return;
							}

					float cost =  sk.getTrainingCost(playerCharacter, npc);
					float profitCost =  cost * npc.getSellPercent(playerCharacter);

					profitCost += .5f;
					if (profitCost > playerCharacter.getCharItemManager().getGoldInventory().getNumOfItems())
						return;
					Building b = npc.getBuilding();
					if (b != null && b.getProtectionState().equals(ProtectionState.NPC))
						b = null;

					if (b != null && b.getStrongboxValue() + (profitCost - cost) > b.getMaxGold()){
						ErrorPopupMsg.sendErrorPopup(playerCharacter, 206);
						return;
					}

					if (playerCharacter.getCharItemManager().getGoldInventory().getNumOfItems() - profitCost < 0)
						return;

					if (playerCharacter.getCharItemManager().getGoldInventory().getNumOfItems() - profitCost > MBServerStatics.PLAYER_GOLD_LIMIT)
						return;


					//attempt to train
					if (sk.train(playerCharacter)) {
						playerCharacter.getCharItemManager().buyFromNPC(b, (int)profitCost, (int)(profitCost - cost));

						dispatch = Dispatch.borrow(playerCharacter, msg);
						DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

						//update trainer window

						if (npc != null) {
                            TrainerInfoMsg tim = new TrainerInfoMsg(msg.npcType, msg.npcID, npc.getSellPercent(playerCharacter));
							tim.setTrainPercent(npc.getSellPercent(playerCharacter));
							dispatch = Dispatch.borrow(playerCharacter, tim);
							DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
						}
					}

				} else {
					//Get Power
                    int token = msg.token;

					if (MBServerStatics.POWERS_DEBUG) {
                        ChatManager.chatSayInfo(playerCharacter, "Training Power: " +
								Integer.toHexString(msg.token) + " (" + msg.token + ')');
                        System.out.println("Training Power: " +
								Integer.toHexString(msg.token) + " (" + msg.token + ')');
					}

					PowersBase pb = PowersManager.getPowerByToken(token);
					ConcurrentHashMap<Integer, CharacterPower> powers = playerCharacter.getPowers();
					if (pb == null || powers == null)
						return;
					
					if (pb.isWeaponPower)
						return;
					CharacterPower cp = null;
					if (powers.containsKey(token))
						cp = powers.get(token);
					if (cp == null)
						return;

					//attempt to train
					float cost = (int) cp.getTrainingCost(playerCharacter, npc);
					float profitCost = cost * npc.getSellPercent(playerCharacter);
					profitCost += .5f;
					if (profitCost > playerCharacter.getCharItemManager().getGoldInventory().getNumOfItems()){
						//	ChatManager.chatSystemError(pc, "You do not have enough gold to train this skill.");
						return;
					}
					
					Building b = npc.getBuilding();
					
					if (b != null && b.getProtectionState().equals(ProtectionState.NPC))
						b = null;

					if (b != null && b.getStrongboxValue() + (profitCost - cost) > b.getMaxGold()){
						ErrorPopupMsg.sendErrorPopup(playerCharacter, 206);
						return;
					}
					if (cp.train(playerCharacter)) {

						if (!playerCharacter.getCharItemManager().buyFromNPC(b, (int)profitCost, (int)(profitCost - cost)))
							ChatManager.chatSystemError(playerCharacter, "Failed to Withdrawl gold from inventory. Contact CCR");

						//train succeeded

						dispatch = Dispatch.borrow(playerCharacter, msg);
						DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

						//update trainer window

						if (npc != null) {
                            TrainerInfoMsg tim = new TrainerInfoMsg(msg.npcType, msg.npcID, npc.getSellPercent(playerCharacter));
							tim.setTrainPercent(npc.getSellPercent(playerCharacter));
							dispatch = Dispatch.borrow(playerCharacter, tim);
							DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
						}
					}
				}
			}catch(Exception e){
				Logger.error(e);
			}finally{
			origin.trainLock.unlock();
			}


		}








	}

	public static float getTrainPercent(NPC npc) {
		return 0f;
	}
}
