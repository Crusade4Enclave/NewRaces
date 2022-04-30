// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Item;

import java.util.ArrayList;


public class LootWindowResponseMsg extends ClientNetMsg {

	private int targetType;
	private int targetID;
	private ArrayList<Item> inventory;
	private int unknown01 = 45;

	/**
	 * This is the general purpose constructor.
	 */
	public LootWindowResponseMsg(int targetType, int targetID, ArrayList<Item> inventory) {
		super(Protocol.WEIGHTINVENTORY);
		this.targetType = targetType;
		this.targetID = targetID;
		this.inventory = inventory;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public LootWindowResponseMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.WEIGHTINVENTORY, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.put((byte) 1);
		Item.putList(writer, this.inventory, false, this.targetID);
		writer.putInt(this.unknown01);
		writer.putInt(this.targetType);
		writer.putInt(this.targetID);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
	}

	public int getUnknown01() {
		return this.unknown01;
	}

	public int getTargetType() {
		return targetType;
	}

	public int getTargetID() {
		return targetID;
	}

	public ArrayList<Item> getInventory() {
		return this.inventory;
	}

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}

	public void setTargetType(int value) {
		this.targetType = value;
	}

	public void setTargetID(int value) {
		this.targetID = value;
	}

	public void setInventory(ArrayList<Item> value) {
		this.inventory = value;
	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return 17; // 2^15 == 32,768
	}
}
