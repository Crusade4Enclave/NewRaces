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
 * Transfer item from inventory to bank
 *
 * @author Eighty
 */

public class TransferItemFromInventoryToBankMsg extends ClientNetMsg {

	private long playerCompID1;
	private long playerCompID2;
	private int type;
	private int objectUUID;
	private int unknown1;
	private int unknown2;
	private int numItems;
	private byte unknown4;

	/**
	 * This is the general purpose constructor
	 */
	public TransferItemFromInventoryToBankMsg(long playerCompID1,
			long playerCompID2, int type, int objectUUID, int unknown1,
			int unknown2, int numItems, byte unknown4) {
		super(Protocol.TRANSFERITEMTOBANK);
		this.playerCompID1 = playerCompID1;
		this.playerCompID2 = playerCompID2;
		this.type = type;
		this.objectUUID = objectUUID;
		this.unknown1 = unknown1;
		this.unknown2 = unknown2;
		this.numItems = numItems;
		this.unknown4 = unknown4;
	}

	public TransferItemFromInventoryToBankMsg(TransferItemFromBankToInventoryMsg msg) {
		super(Protocol.TRANSFERITEMTOBANK);
		this.playerCompID1 = msg.getPlayerCompID1();
		this.playerCompID2 = msg.getPlayerCompID2();
		this.type = msg.getType();
		this.objectUUID = msg.getUUID();
		this.unknown1 = msg.getUnknown1();
		this.unknown2 = msg.getUnknown2();
		this.numItems = msg.getNumItems();
		this.unknown4 = msg.getUnknown4();
	}

	/**
	 * This is the general purpose constructor
	 */
	public TransferItemFromInventoryToBankMsg() {
		super(Protocol.TRANSFERITEMTOBANK);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TransferItemFromInventoryToBankMsg(AbstractConnection origin,
			ByteBufferReader reader)  {
		super(Protocol.TRANSFERITEMTOBANK, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		playerCompID1 = reader.getLong();
		playerCompID2 = reader.getLong();
		type = reader.getInt();
		objectUUID = reader.getInt();
		unknown1 = reader.getInt();
		unknown2 = reader.getInt();
		numItems = reader.getInt();
		unknown4 = reader.get();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putLong(playerCompID1);
		writer.putLong(playerCompID2);
		writer.putInt(type);
		writer.putInt(objectUUID);
		writer.putInt(unknown1);
		writer.putInt(unknown2);
		writer.putInt(numItems);
		writer.put(unknown4);
	}

	/**
	 * @return the playerCompID1
	 */
	public long getPlayerCompID1() {
		return playerCompID1;
	}

	/**
	 * @param playerCompID1 the playerCompID1 to set
	 */
	public void setPlayerCompID1(long playerCompID1) {
		this.playerCompID1 = playerCompID1;
	}

	/**
	 * @return the playerCompID2
	 */
	public long getPlayerCompID2() {
		return playerCompID2;
	}

	/**
	 * @param playerCompID2 the playerCompID2 to set
	 */
	public void setPlayerCompID2(long playerCompID2) {
		this.playerCompID2 = playerCompID2;
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
	 * @return the unknown1
	 */
	public int getUnknown1() {
		return unknown1;
	}

	/**
	 * @param unknown1 the unknown1 to set
	 */
	public void setUnknown1(int unknown1) {
		this.unknown1 = unknown1;
	}

	/**
	 * @return the unknown2
	 */
	public int getUnknown2() {
		return unknown2;
	}

	/**
	 * @param unknown2 the unknown2 to set
	 */
	public void setUnknown2(int unknown2) {
		this.unknown2 = unknown2;
	}

	/**
	 * @return the numItems
	 */
	public int getNumItems() {
		return numItems;
	}

	/**
	 * @param numItems the numItems to set
	 */
	public void setNumItems(int numItems) {
		this.numItems = numItems;
	}

	/**
	 * @return the unknown4
	 */
	public byte getUnknown4() {
		return unknown4;
	}

	/**
	 * @param unknown4 the unknown4 to set
	 */
	public void setUnknown4(byte unknown4) {
		this.unknown4 = unknown4;
	}

}
