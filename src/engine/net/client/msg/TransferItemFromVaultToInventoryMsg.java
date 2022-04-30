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

/**
 * Transfer item from Vault to inventory
 *
 * @author Eighty
 */

public class TransferItemFromVaultToInventoryMsg extends ClientNetMsg {

	private int unknown01;
	private int unknown02;
	private long playerCompID;
	private int type;
	private int objectUUID;
	private int unknown03;
	private int unknown04;

	/**
	 * This is the general purpose constructor
	 */
	public TransferItemFromVaultToInventoryMsg(int unknown01, int unknown02,
			long playerCompID, int type, int objectUUID, int unknown03, int unknown04) {
		super(Protocol.TRANSFERITEMFROMVAULTTOINVENTORY);
		this.unknown01 = unknown01;
		this.unknown02 = unknown02;
		this.playerCompID = playerCompID;
		this.type = type;
		this.objectUUID = objectUUID;
		this.unknown03 = unknown03;
		this.unknown04 = unknown04;
	}

	public TransferItemFromVaultToInventoryMsg(TransferItemFromInventoryToVaultMsg msg) {
		super(Protocol.TRANSFERITEMFROMVAULTTOINVENTORY);
		this.playerCompID = msg.getPlayerCompID();
		this.unknown01 = msg.getUnknown01();
		this.unknown02 = msg.getUnknown02();
		this.type = msg.getType();
		this.objectUUID = msg.getUUID();
		this.unknown03 = msg.getUnknown03();
		this.unknown04 = msg.getUnknown04();
	}

	/**
	 * This is the general purpose constructor
	 */
	public TransferItemFromVaultToInventoryMsg() {
		super(Protocol.TRANSFERITEMFROMVAULTTOINVENTORY);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TransferItemFromVaultToInventoryMsg(AbstractConnection origin,
			ByteBufferReader reader)  {
		super(Protocol.TRANSFERITEMFROMVAULTTOINVENTORY, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		unknown01 = reader.getInt();
		unknown02 = reader.getInt();
		playerCompID = reader.getLong();
		type = reader.getInt();
		objectUUID = reader.getInt();
		unknown03 = reader.getInt();
		unknown04 = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(unknown01);
		writer.putInt(unknown02);
		writer.putLong(playerCompID);
		writer.putInt(type);
		writer.putInt(objectUUID);
		writer.putInt(unknown03);
		writer.putInt(unknown04);
	}

	/**
	 * @return the unknown01
	 */
	public int getUnknown01() {
		return unknown01;
	}

	/**
	 * @param unknown01 the unknown01 to set
	 */
	public void setUnknown01(int unknown01) {
		this.unknown01 = unknown01;
	}

	/**
	 * @return the unknown02
	 */
	public int getUnknown02() {
		return unknown02;
	}

	/**
	 * @param unknown02 the unknown02 to set
	 */
	public void setUnknown02(int unknown02) {
		this.unknown02 = unknown02;
	}

	/**
	 * @return the playerCompID
	 */
	public long getPlayerCompID() {
		return playerCompID;
	}

	/**
	 * @param playerCompID the playerCompID to set
	 */
	public void setPlayerCompID(long playerCompID) {
		this.playerCompID = playerCompID;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
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
	 * @return the unknown03
	 */
	public int getUnknown03() {
		return unknown03;
	}

	/**
	 * @param unknown03 the unknown03 to set
	 */
	public void setUnknown03(int unknown03) {
		this.unknown03 = unknown03;
	}

	/**
	 * @return the unknown04
	 */
	public int getUnknown04() {
		return unknown04;
	}

	/**
	 * @param unknown04 the unknown04 to set
	 */
	public void setUnknown04(int unknown04) {
		this.unknown04 = unknown04;
	}

}
