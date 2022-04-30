// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.ItemType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Item;

import java.util.ArrayList;

public class UpdateInventoryMsg extends ClientNetMsg {

	private ArrayList<Item> toAdd;
	private ArrayList<Item> bank;

	/**
	 * This is the general purpose constructor.
	 */
	public UpdateInventoryMsg(ArrayList<Item> inventory,ArrayList<Item> bank, Item gold, boolean add) {
		super(Protocol.UPDATECLIENTINVENTORIES);
		toAdd = inventory;
		if (gold != null)
			toAdd.add(gold);

		this.bank = bank;

	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UpdateInventoryMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.UPDATECLIENTINVENTORIES, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		putList(writer, this.toAdd);
		if (bank != null)
			putList(writer, this.bank);
		else
			writer.putInt(0);
	}

	public static void putList(ByteBufferWriter writer, ArrayList<Item> list) {
		int indexPosition = writer.position();
		writer.putInt(0);

		int serialized = 0;


		for (Item item : list) {
			if (item.getItemBase().getType().equals(ItemType.GOLD)) {
				if (item.getNumOfItems() == 0)
					continue;
			}
			Item.serializeForClientMsgWithoutSlot(item,writer);
			++serialized;
		}

		writer.putIntAt(serialized, indexPosition);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
	}

	public ArrayList<Item> getToAdd() {
		return this.toAdd;
	}


	public void setToAdd(ArrayList<Item> value) {
		this.toAdd = value;
	}


	public void addToInventory(Item value) {
		this.toAdd.add(value);
	}


	@Override
	protected int getPowerOfTwoBufferSize() {
		return 20;
	}
}
