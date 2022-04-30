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
import engine.objects.AbstractCharacter;

public class TransferItemFromInventoryToEquipMsg extends ClientNetMsg {

	private int sourceType;
	private int sourceID;
	private int pad1;
	private int itemBase;
	private int type;
	private int objectUUID;
	private int slotNumber;
	private int pad2;
	private float unknown1, unknown2;

	/**
	 * This is the general purpose constructor.
	 */
	public TransferItemFromInventoryToEquipMsg() {
		super(Protocol.EQUIP);
	}

	public TransferItemFromInventoryToEquipMsg(AbstractCharacter source, int slot, int itemBaseID) {
		super(Protocol.EQUIP);
		this.sourceType = source.getObjectType().ordinal();
		this.sourceID = source.getObjectUUID();
		this.slotNumber = slot;
		this.itemBase = itemBaseID;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TransferItemFromInventoryToEquipMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.EQUIP, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
		pad1 = reader.getInt();
		itemBase = reader.getInt();
		type = reader.getInt();
		objectUUID = reader.getInt();
		slotNumber = reader.getInt();
		pad2 = reader.getInt();
		unknown1 = reader.getFloat();
		unknown2 = reader.getFloat();
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);
		writer.putInt(pad1);
		writer.putInt(itemBase);
		writer.putInt(type);
		writer.putInt(objectUUID);
		writer.putInt(slotNumber);
		writer.putInt(pad2);
		writer.putFloat(unknown1);
		writer.putFloat(unknown1);
	}



	/**
	 * @return the pad1
	 */
	public int getPad1() {
		return pad1;
	}

	/**
	 * @param pad1
	 *            the pad1 to set
	 */
	public void setPad1(int pad1) {
		this.pad1 = pad1;
	}

	/**
	 * @return the itemBase
	 */
	public int getItemBase() {
		return itemBase;
	}

	/**
	 * @param itemBase
	 *            the itemBase to set
	 */
	public void setItemBase(int itemBase) {
		this.itemBase = itemBase;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the objectUUID
	 */
	public int getUUID() {
		return objectUUID;
	}

	/**
	 * @return the slotNumber
	 */
	public int getSlotNumber() {
		return slotNumber;
	}

	/**
	 * @param slotNumber
	 *            the slotNumber to set
	 */
	public void setSlotNumber(int slotNumber) {
		this.slotNumber = slotNumber;
	}

	/**
	 * @return the pad2
	 */
	public int getPad2() {
		return pad2;
	}

	/**
	 * @param pad2
	 *            the pad2 to set
	 */
	public void setPad2(int pad2) {
		this.pad2 = pad2;
	}

	/**
	 * @return the unknown1
	 */
	public float getUnknown1() {
		return unknown1;
	}

	/**
	 * @param unknown1
	 *            the unknown1 to set
	 */
	public void setUnknown1(float unknown1) {
		this.unknown1 = unknown1;
	}

	/**
	 * @return the unknown2
	 */
	public float getUnknown2() {
		return unknown2;
	}

	/**
	 * @param unknown2
	 *            the unknown2 to set
	 */
	public void setUnknown2(float unknown2) {
		this.unknown2 = unknown2;
	}

	public int getSourceType() {
		return sourceType;
	}

	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	public int getSourceID() {
		return sourceID;
	}

	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}

}
