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
 * Transfer gold to/from building strongbox/reserve
 *
 * @author Eighty
 */

public class TransferGoldToFromBuildingMsg extends ClientNetMsg {

	private int direction; //Maybe? 1 and 2
	private int failReason;
	private int objectType;
	private int objectID;
	private int amount;
	private int unknown01;
	private int unknown02;

	/**
	 * This is the general purpose constructor
	 */
	public TransferGoldToFromBuildingMsg() {
		super(Protocol.TRANSFERGOLDTOFROMBUILDING);
		this.direction = 0;
		this.failReason = 0;
		this.objectType = 0;
		this.objectID = 0;
		this.amount = 0;
		this.unknown01 = 0;
		this.unknown02 = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TransferGoldToFromBuildingMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.TRANSFERGOLDTOFROMBUILDING, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		this.direction = reader.getInt();
		this.failReason = reader.getInt();
		this.objectType = reader.getInt();
		this.objectID = reader.getInt();
		this.amount = reader.getInt();
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(this.direction);
		writer.putInt(this.failReason);
		writer.putInt(this.objectType);
		writer.putInt(this.objectID);
		writer.putInt(this.amount);
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);

	}

	public int getDirection() {
		return this.direction;
	}

	public int getAmount() {
		return this.amount;
	}

	public int getUnknown01() {
		return this.unknown01;
	}

	public int getUnknown02() {
		return this.unknown02;
	}

	public void setDirection(int value) {
		this.direction = value;
	}


	public void setAmount(int value) {
		this.amount = value;
	}

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}

	public void setUnknown02(int value) {
		this.unknown02 = value;
	}

	public void setFailReason(int failReason) {
		this.failReason = failReason;
	}

	public int getFailReason() {
		return failReason;
	}


	public int getObjectType() {
		return objectType;
	}

	public void setObjectType(int objectType) {
		this.objectType = objectType;
	}

	public int getObjectID() {
		return objectID;
	}

	public void setObjectID(int objectID) {
		this.objectID = objectID;
	}
}
