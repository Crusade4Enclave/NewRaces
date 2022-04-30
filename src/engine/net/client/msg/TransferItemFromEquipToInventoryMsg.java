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
import engine.objects.AbstractGameObject;

public class TransferItemFromEquipToInventoryMsg extends ClientNetMsg {

	private int playerType;
	private int playerUUID;
	private int slotNumber;

    /**
	 * This is the general purpose constructor.
	 */
	public TransferItemFromEquipToInventoryMsg(AbstractGameObject ago, int slotNumber) {
		super(Protocol.UNEQUIP);
		this.playerType = ago.getObjectType().ordinal();
		this.playerUUID = ago.getObjectUUID();
		this.slotNumber = slotNumber;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TransferItemFromEquipToInventoryMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.UNEQUIP, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.playerType = reader.getInt();
		this.playerUUID = reader.getInt();
		this.slotNumber = reader.getInt();
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {
		writer.putInt(this.playerType);
		writer.putInt(this.playerUUID);
		writer.putInt(this.slotNumber);
	}

	

	/**
	 * @return the slotNumber
	 */
	public int getSlotNumber() {
		return slotNumber;
	}

	public int getPlayerType() {
		return playerType;
	}

	public void setPlayerType(int playerType) {
		this.playerType = playerType;
	}

	public int getPlayerUUID() {
		return playerUUID;
	}

	public void setPlayerUUID(int playerUUID) {
		this.playerUUID = playerUUID;
	}

}
