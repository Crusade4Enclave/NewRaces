// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Contract;
import engine.objects.NPC;

import java.util.ArrayList;

/**
 * Sell to NPC window msg
 *
 * @author 
 */
public class RepairMsg extends ClientNetMsg {

//Item Types:
//1: weapon
//2: armor/cloth/shield
//5: scrolls
//8: potions
//10: charters
//13: Jewelry

	private int msgType;
	//static 0
	private int unknown01; //0 or 10
	private int npcType;
	private int npcID;
	private NPC npc = null;
	private int itemType;
	private int itemID;
	private int amountRepaired;
	//static 0
	//static 0 //end repair req/ack here
	//01 inc, 00 out
	//item list
	//skill list
	//unk list
	//static 0 //out
	//static 0 //out
	//static 10000 out?
	//static 0

	/**
	 * This is the general purpose constructor
	 */
	public RepairMsg() {
		super(Protocol.REPAIROBJECT);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public RepairMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.REPAIROBJECT, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {

		this.msgType = reader.getInt();
		reader.getInt();
		this.unknown01 = reader.getInt();
		this.npcType = reader.getInt();
		this.npcID = reader.getInt();
		this.itemType = reader.getInt();
		this.itemID = reader.getInt();
		this.amountRepaired = reader.getInt();
		reader.getInt();
		reader.getInt();

		if (this.msgType == 1) {
			reader.get(); //0x00 inc
			int size = reader.getInt();
			for (int i=0;i<size;i++)
				reader.getInt();
			size = reader.getInt();
			for (int i=0;i<size;i++)
				reader.getInt();
			size = reader.getInt();
			for (int i=0;i<size;i++)
				reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
		}
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(this.msgType);
		writer.putInt(0);
		writer.putInt(this.unknown01);
		writer.putInt(this.npcType);
		writer.putInt(this.npcID);
		writer.putInt(this.itemType);
		writer.putInt(this.itemID);
		writer.putInt(this.amountRepaired);
		writer.putInt(0);
		writer.putInt(0);
		if (this.msgType == 1) {
			writer.put((byte)0x00);
			Contract c = (npc != null) ? npc.getContract() : null;
			if (c != null) {
				ArrayList<Integer> list = c.getBuyItemType();
				writer.putInt(list.size());
				for (int l : list)
					writer.putInt(l);
				list = c.getBuySkillToken();
				writer.putInt(list.size());
				for (int l : list)
					writer.putInt(l);
				list = c.getBuyUnknownToken();
				writer.putInt(list.size());
				for (int l : list)
					writer.putInt(l);
			} else {
				writer.putInt(0);
				writer.putInt(0);
				writer.putInt(0);
			}
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(10000);
			writer.putInt(0);
		}
	}

	public int getMsgType() {
		return this.msgType;
	}

	public int getNPCType() {
		return this.npcType;
	}

	public int getNPCID() {
		return this.npcID;
	}

	public int getItemType() {
		return this.itemType;
	}

	public int getItemID() {
		return this.itemID;
	}

	public int getAmountRepaired() {
		return this.amountRepaired;
	}

	public NPC getNPC() {
		return this.npc;
	}

	public void setMsgType(int value) {
		this.msgType = value;
	}

	public void setNPCType(int value) {
		this.npcType = value;
	}

	public void setNPCID(int value) {
		this.npcID = value;
	}

	public void setItemType(int value) {
		this.itemType = value;
	}

	public void setItemID(int value) {
		this.itemID = value;
	}

	public void setAmountRrepaired(int value) {
		this.amountRepaired = value;
	}

	public void setNPC(NPC value) {
		this.npc = value;
	}

	public void setupRepairAck(int amountRepaired) {
		this.unknown01 = 10;
		this.amountRepaired = amountRepaired;
	}

	public void setRepairWindowAck(NPC npc) {
		this.unknown01 = 10;
		this.npc = npc;
	}
}
