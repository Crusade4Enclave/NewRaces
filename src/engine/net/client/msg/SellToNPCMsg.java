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
 * Buy from NPC msg
 *
 * @author Eighty
 */
public class SellToNPCMsg extends ClientNetMsg {

	int npcType;
	int npcID;
	int itemType;
	int itemID;
	int unknown01;

	/**
	 * This is the general purpose constructor
	 */
	public SellToNPCMsg() {
		super(Protocol.SELLOBJECT);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public SellToNPCMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.SELLOBJECT, origin, reader);
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
		this.unknown01 = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(this.npcType);
		writer.putInt(this.npcID);
		writer.putInt(this.itemType);
		writer.putInt(this.itemID);
		writer.putInt(this.unknown01);
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

	public int getUnknown01() {
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

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}
}
