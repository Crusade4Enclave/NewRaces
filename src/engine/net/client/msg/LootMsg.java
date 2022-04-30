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
import engine.objects.MobEquipment;

public class LootMsg extends ClientNetMsg {

	private Item item;
	private int sourceType1;
	private int sourceID1;
	private int targetType;
	private int targetID;
	private int sourceType2;
	private int sourceID2;

	private int unknown01;
	private int unknown02;
	private int unknown03;
	private int unknown04;
	private int unknown05;
	private byte unknown06 = (byte) 0;
	private int unknown07;
	private int unknown08;

	private MobEquipment mobEquipment = null;

	/**
	 * This is the general purpose constructor.
	 */
	public LootMsg(int sourceType, int sourceID, int targetType, int targetID, Item item) {
		super(Protocol.MOVEOBJECTTOCONTAINER);
		this.sourceType1 = sourceType;
		this.sourceID1 = sourceID;
		this.targetType = targetType;
		this.targetID = targetID;
		this.sourceType2 = sourceType;
		this.sourceID2 = sourceID;
		this.item = item;
		this.unknown01 = 0;
		this.unknown02 = 0;
		this.unknown03 = 0;
		this.unknown04 = 0;
		this.unknown05 = 0;
		this.unknown07 = 0;
		this.unknown08 = 0;
	}

	//for MobEquipment

	public LootMsg(int sourceType, int sourceID, int targetType, int targetID, MobEquipment mobEquipment) {
		super(Protocol.MOVEOBJECTTOCONTAINER);
		this.sourceType1 = sourceType;
		this.sourceID1 = sourceID;
		this.targetType = targetType;
		this.targetID = targetID;
		this.sourceType2 = sourceType;
		this.sourceID2 = sourceID;
		this.item = null;
		this.mobEquipment = mobEquipment;

		this.unknown01 = 0;
		this.unknown02 = 0;
		this.unknown03 = 0;
		this.unknown04 = 0;
		this.unknown05 = 0;
		this.unknown07 = 0;
		this.unknown08 = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public LootMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.MOVEOBJECTTOCONTAINER, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {

		if (this.item != null)
			Item.serializeForClientMsgWithoutSlot(this.item,writer);
		else if (this.mobEquipment != null)
			try {
				MobEquipment._serializeForClientMsg(this.mobEquipment,writer, false);
			} catch (SerializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		writer.putInt(this.sourceType1);
		writer.putInt(this.sourceID1);
		writer.putInt(this.targetType);
		writer.putInt(this.targetID);
		writer.putInt(this.sourceType2);
		writer.putInt(this.sourceID2);
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		writer.putInt(this.unknown03);
		writer.putInt(this.unknown04);
		writer.putInt(this.unknown05);
		writer.put(this.unknown06);
		writer.putInt(this.unknown07);
		writer.putInt(this.unknown08);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.item = Item.deserializeFromClientMsg(reader, false);
		this.sourceType1 = reader.getInt();
		this.sourceID1 = reader.getInt();
		this.targetType = reader.getInt();
		this.targetID = reader.getInt();
		this.sourceType2 = reader.getInt();
		this.sourceID2 = reader.getInt();
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
		this.unknown04 = reader.getInt();
		this.unknown05 = reader.getInt();
		this.unknown06 = reader.get();
		this.unknown07 = reader.getInt();
		this.unknown08 = reader.getInt();
	}

	public int getSourceType1() {
		return this.sourceType1;
	}

	public int getSourceID1() {
		return this.sourceID1;
	}

	public int getTargetType() {
		return this.targetType;
	}

	public int getTargetID() {
		return this.targetID;
	}

	public int getSourceType2() {
		return this.sourceType2;
	}

	public int getSourceID2() {
		return this.sourceID2;
	}

	public int getUnknown01() {
		return this.unknown01;
	}

	public int getUnknown02() {
		return this.unknown02;
	}

	public int getUnknown03() {
		return this.unknown03;
	}

	public int getUnknown04() {
		return this.unknown04;
	}

	public int getUnknown05() {
		return this.unknown05;
	}

	public byte getUnknown06() {
		return this.unknown06;
	}

	public int getUnknown07() {
		return this.unknown07;
	}

	public int getUnknown08() {
		return this.unknown08;
	}

	public Item getItem() {
		return this.item;
	}

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}

	public void setUnknown02(int value) {
		this.unknown02 = value;
	}

	public void setUnknown03(int value) {
		this.unknown03 = value;
	}

	public void setUnknown04(int value) {
		this.unknown04 = value;
	}

	public void setUnknown05(int value) {
		this.unknown05 = value;
	}

	public void setUnknown06(byte value) {
		this.unknown06 = value;
	}

	public void setUnknown07(int value) {
		this.unknown07 = value;
	}

	public void setUnknown08(int value) {
		this.unknown08 = value;
	}

	public void setSourceType1(int value) {
		this.sourceType1 = value;
	}

	public void setSourceID1(int value) {
		this.sourceID1 = value;
	}

	public void setTargetType(int value) {
		this.targetType = value;
	}

	public void setTargetID(int value) {
		this.targetID = value;
	}

	public void setSourceType2(int value) {
		this.sourceType2 = value;
	}

	public void setSourceID2(int value) {
		this.sourceID2 = value;
	}

	public void setItem(Item value) {
		this.item = value;
	}
}
