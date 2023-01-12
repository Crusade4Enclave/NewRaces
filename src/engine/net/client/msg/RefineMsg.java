// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.gameManager.PowersManager;
import engine.gameManager.SessionManager;
import engine.net.*;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.objects.*;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RefineMsg extends ClientNetMsg {

	private int npcType;
	private int npcID;
	private int unknown01;
	private int type;
	private int token;
	private int unknown02;

	/**
	 * This is the general purpose constructor.
	 */
	public RefineMsg() {
		super(Protocol.ARCUNTRAINABILITY);
	}

	public RefineMsg(int npcType, int npcID, int type, int token) {
		super(Protocol.ARCUNTRAINABILITY);
		this.npcType = npcType;
		this.npcID = npcID;
		this.unknown01 = 1;
		this.type = type;
		this.token = token;
		this.unknown02 = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public RefineMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ARCUNTRAINABILITY, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.npcType);
		writer.putInt(this.npcID);
		writer.putInt(this.unknown01);
		writer.putInt(this.type);
		writer.putInt(this.token);
		writer.putInt(this.unknown02);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.npcType = reader.getInt();
		this.npcID = reader.getInt();
		this.unknown01 = reader.getInt();
		this.type = reader.getInt();
		this.token = reader.getInt();
		this.unknown02 = reader.getInt();
	}

	public int getNpcType() {
		return this.npcType;
	}

	public int getNpcID() {
		return this.npcID;
	}

	public int getUnknown01() {
		return this.unknown01;
	}

	public int getType() {
		return this.type;
	}

	public int getToken() {
		return this.token;
	}

	public int getUnknown02() {
		return this.unknown02;
	}

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}

	public void setType(int value) {
		this.type = value;
	}

	public void setToken(int value) {
		this.token = value;
	}

	public void setUnknown02(int value) {
		this.unknown02 = value;
	}

	public static void refine(RefineMsg msg, ClientConnection origin) {
		if (origin == null)
			return;
		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);
		if (pc == null)
			return;
        NPC npc = NPC.getFromCache(msg.npcID);
		if (npc == null)
			return;
        int type = msg.type;
        int token = msg.token;
		boolean worked = false;
		boolean skillPower = true;
		if (type == 0) { //refine skill
			worked = refineSkill(origin, pc, token, msg);
		} else if (type == 1) { //refine power
			worked = refinePower(origin, pc, token, msg);
		} else if (type == 2) { //refine stat
			worked = refineStat(origin, pc, token, msg);
			skillPower = false;
		}

		if (worked) {

			//update player
			pc.applyBonuses();
			pc.getCharItemManager().RemoveEquipmentFromLackOfSkill(pc, true);

			//echo refine message back

			Dispatch dispatch = Dispatch.borrow(pc, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);

			//			if (type == 0 && token == 1488335491){
			//				dispatch = Dispatch.borrow(pc, msg);
			//				DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
			//			}

			//resend refine screen

			RefinerScreenMsg refinerScreenMsg = new RefinerScreenMsg(skillPower, npc.getSellPercent(pc)); //TODO set npc cost
			dispatch = Dispatch.borrow(pc, refinerScreenMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
		}
	}

	private static boolean refineSkill(ClientConnection origin, PlayerCharacter pc, int token, RefineMsg msg) {
		CharacterSkill skill = null;
		ConcurrentHashMap<String, CharacterSkill> skills = pc.getSkills();
		for (CharacterSkill sk : skills.values()) {
			if (sk == null)
				continue;
			SkillsBase sb = sk.getSkillsBase();
			if (sb == null)
				continue;
			if (sb.getToken() == token)
				skill = sk;
		}
		//check if player has skill to refine
		if (skill == null)
			return false;
		//check there's a train to refine
		if (skill.getNumTrains() < 1)
			return false;

		//TODO verify if any skills have this as prereq

		//TODO verify if any powers have this as a prereq
		//get all players powers
		for(CharacterPower power : pc.getPowers().values()){
			ArrayList<PowerReq> reqs = PowerReq.getPowerReqsForRune(power.getPowerID());
			for (PowerReq req : reqs) {
				ConcurrentHashMap<String,CharacterSkill> playerSkills =  pc.getSkills();
				CharacterSkill playerSkill = playerSkills.get(token);
				int currentSkillLevel = playerSkill.getTotalSkillPercet();
				if (token == req.getToken() && req.getLevel() == currentSkillLevel) {
					return false;
				}
			}
		}

		//refine skill
		return skill.refine(pc);
	}

	private static boolean refinePower(ClientConnection origin, PlayerCharacter pc, int token, RefineMsg msg) {
		CharacterPower power = null;
		ConcurrentHashMap<Integer, CharacterPower> powers = pc.getPowers();
		if (!powers.containsKey(token))
			return false;
		power = powers.get(token);
		if (power == null)
			return false;
		if (power.getTrains() < 1)
			return false;

		//TODO verify if any powers have this as a prereq

		return power.refine(pc);
	}

	private static boolean refineStat(ClientConnection origin, PlayerCharacter pc, int token, RefineMsg msg) {
		if (token == MBServerStatics.STAT_STR_ID)
			return pc.refineStr();
		if (token == MBServerStatics.STAT_DEX_ID)
			return pc.refineDex();
		if (token == MBServerStatics.STAT_CON_ID)
			return pc.refineCon();
		if (token == MBServerStatics.STAT_INT_ID)
			return pc.refineInt(msg);
		if (token == MBServerStatics.STAT_SPI_ID)
			return pc.refineSpi();
		return false;
	}
}
