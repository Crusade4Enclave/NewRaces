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

public class TrainerInfoMsg extends ClientNetMsg {

	private int objectType;
	private int objectID;
	private float trainPercent;

	/**
	 * This is the general purpose constructor.
	 */
	public TrainerInfoMsg(int objectType, int objectID, float trainPercent) {
		super(Protocol.TRAINERLIST);
		this.objectType = objectType;
		this.objectID = objectID;
		this.trainPercent = trainPercent;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TrainerInfoMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.TRAINERLIST, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public TrainerInfoMsg(TrainerInfoMsg msg) {
		super(Protocol.TRAINERLIST);
		this.objectType = msg.objectType;
		this.objectID = msg.objectID;
		this.trainPercent = msg.trainPercent;
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.get();
		this.objectType = reader.getInt();
		this.objectID = reader.getInt();
		this.trainPercent = reader.getFloat();
		reader.getInt();
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.put((byte)0);
		writer.putInt(this.objectType);
		writer.putInt(this.objectID);
		writer.putFloat(this.trainPercent);
		writer.putInt(0);
	}

	public int getObjectType() {
		return this.objectType;
	}

	public int getObjectID() {
		return this.objectID;
	}

	public float getTrainPercent() {
		return this.trainPercent;
	}

	public void setObjectType(int value) {
		this.objectType = value;
	}

	public void setObjectID(int value) {
		this.objectID = value;
	}

	public void setTrainPercent(float value) {
		this.trainPercent = value;
	}
}
