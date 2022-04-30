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
import engine.objects.Item;

public class BuyFromNPCMsg extends ClientNetMsg {

	private int npcType;
	private int npcID;
	private int itemType;
	private int itemID;
	private byte unknown01;
	private Item item;

	/**
	 * This is the general purpose constructor
	 */
	public BuyFromNPCMsg() {
		super(Protocol.BUYFROMNPC);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public BuyFromNPCMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.BUYFROMNPC, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		this.npcType = reader.getInt();
		this.npcID = reader.getInt();
		this.itemType = reader.getInt();
		this.itemID = reader.getInt();
		this.unknown01 = reader.get();
		reader.get();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {

		writer.putInt(this.npcType);
		writer.putInt(this.npcID);

		if (this.item != null){
			writer.putInt(this.item.getObjectType().ordinal());
			writer.putInt(this.item.getObjectUUID());
		}else{
			writer.putInt(this.itemType);
			writer.putInt(this.itemID);
		}

		writer.put(this.unknown01);
		if (item != null) {
			writer.put((byte) 1);
			Item.serializeForClientMsgWithoutSlot(item,writer);
		} else {
			writer.put((byte) 0);
		}
	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		return (16); // 2^14 == 16384
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

	public byte getUnknown01() {
		return this.unknown01;
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

	public void setUnknown01(byte value) {
		this.unknown01 = value;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
}
