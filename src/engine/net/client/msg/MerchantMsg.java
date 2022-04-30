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


public class MerchantMsg extends ClientNetMsg {

	private int type;
	private int unknown01;
	private int unknown02;
	private int unknown03;
	private int npcType;
	private int npcID;
	private int cityType;
	private int cityID;
	private int teleportTime;
	private int unknown04;
	private int itemType;
	private int itemID;
	private int amount;
	private int hashID;

	/**
	 * This is the general purpose constructor.
	 */
	public MerchantMsg() {
		super(Protocol.MERCHANT);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public MerchantMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.MERCHANT, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public MerchantMsg(MerchantMsg msg) {
		super(Protocol.MERCHANT);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.type = reader.getInt();
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
		this.npcType = reader.getInt();
		this.npcID = reader.getInt();
		if (this.type == 11 || type == 13) {
			this.cityType = reader.getInt();
			this.cityID = reader.getInt();
			this.teleportTime = reader.getInt();
		}else if(this.type == 18){
			this.itemType = reader.getInt();
			this.itemID = reader.getInt();
			this.amount = reader.getInt();
		}else if (this.type == 17){
			this.hashID = reader.getInt();
			this.amount = reader.getInt();
		}else if (this.type == 19){
			this.hashID = reader.getInt();
		}else {

			this.cityType = 0;
			this.cityID = 0;
			this.teleportTime = 0;
		}
		this.unknown04 = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.type);
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		writer.putInt(this.unknown03);
		writer.putInt(this.npcType);
		writer.putInt(this.npcID);
		if (this.type == 11 || type == 13) {
			writer.putInt(this.cityType);
			writer.putInt(this.cityID);
			writer.putInt(this.teleportTime);
		}
		writer.putInt(this.unknown04);
		if (this.type == 5){
			writer.putInt(2097253);
			writer.putInt(0);
		}

	}

	public int getType() {
		return this.type;
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

	public int getNPCType() {
		return this.npcType;
	}

	public int getNPCID() {
		return this.npcID;
	}

	public int getCityType() {
		return this.cityType;
	}

	public int getCityID() {
		return this.cityID;
	}

	public int getTeleportTime() {
		return this.teleportTime;
	}

	public int getUnknown04() {
		return this.unknown04;
	}

	public void setType(int value) {
		this.type = value;
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

	public void setNPCType(int value) {
		this.npcType = value;
	}

	public void setNPCID(int value) {
		this.npcID = value;
	}

	public void setCityType(int value) {
		this.cityType = value;
	}

	public void setCityID(int value) {
		this.cityID = value;
	}

	public void setTeleportTime(int value) {
		this.teleportTime = value;
	}

	public void setUnknown04(int value) {
		this.unknown04 = value;
	}

	public int getItemType() {
		return itemType;
	}

	public void setItemType(int itemType) {
		this.itemType = itemType;
	}

	public int getItemID() {
		return itemID;
	}

	public void setItemID(int itemID) {
		this.itemID = itemID;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getHashID() {
		return hashID;
	}

	public void setHashID(int hashID) {
		this.hashID = hashID;
	}
}
