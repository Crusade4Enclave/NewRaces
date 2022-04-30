// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;

/**
 * Transfer gold from inventory to vault
 *
 * @author Eighty
 */

public class TransferGoldFromInventoryToVaultMsg extends ClientNetMsg {

	private int playerID;
	private int accountID;
	private int npcID;

	private int amount;

	/**
	 * This is the general purpose constructor
	 */
	public TransferGoldFromInventoryToVaultMsg() {
		super(Protocol.GOLDTOVAULT);

	}
	public TransferGoldFromInventoryToVaultMsg(int playerID, int npcID, int accountID, int amount) {
		super(Protocol.GOLDTOVAULT);
		this.playerID = playerID;
		this.npcID = npcID;
		this.accountID = accountID;
		this.amount = amount;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TransferGoldFromInventoryToVaultMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.GOLDTOVAULT, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		reader.getInt();
		this.playerID = reader.getInt();
		reader.getInt();
		this.accountID = reader.getInt();
		reader.getInt();
		this.npcID = reader.getInt();
		this.amount = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(GameObjectType.PlayerCharacter.ordinal());
		writer.putInt(this.playerID);
		writer.putInt(GameObjectType.Account.ordinal());
		writer.putInt(this.accountID);
		writer.putInt(GameObjectType.NPC.ordinal());
		writer.putInt(this.npcID);

		writer.putInt(this.amount);
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
