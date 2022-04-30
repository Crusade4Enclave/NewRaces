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
 * Transfer gold from vault to inventory
 *
 * @author Eighty
 */
public class TransferGoldFromVaultToInventoryMsg extends ClientNetMsg {

	private long unknown01;
	private long playerCompID;
	private long accountCompID;
	private int amount;

	/**
	 * This is the general purpose constructor
	 */
	public TransferGoldFromVaultToInventoryMsg(long unknown01, long playerCompID, long accountCompID, int amount) {
		super(Protocol.TRANSFERGOLDFROMVAULTTOINVENTORY);
		this.unknown01 = unknown01;
		this.playerCompID = playerCompID;
		this.accountCompID = accountCompID;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TransferGoldFromVaultToInventoryMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.TRANSFERGOLDFROMVAULTTOINVENTORY, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		unknown01 = reader.getLong();
		playerCompID = reader.getLong();
		accountCompID = reader.getLong();
		amount = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putLong(unknown01);
		writer.putLong(playerCompID);
		writer.putLong(accountCompID);
		writer.putInt(amount);
	}

	/**
	 * @return the unknown01
	 */
	public long getUnknown01() {
		return unknown01;
	}

	/**
	 * @param unknown01 the unknown01 to set
	 */
	public void setUnknown01(long unknown01) {
		this.unknown01 = unknown01;
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
	 * @return the unknown02
	 */
	public long getAccountCompID() {
		return accountCompID;
	}

	/**
	 * @param unknown02 the unknown02 to set
	 */
	public void setAccountCompID(long accountCompID) {
		this.accountCompID = accountCompID;
	}

	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

}
