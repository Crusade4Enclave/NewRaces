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

public class RefinerScreenMsg extends ClientNetMsg {

	private int npcType;
	private int npcID;
	private int unknown01; //might be - 0: skills/powers, 2: stats
	private float unknown02; //cost to refine
	private int unknown03;
	private int unknown04;

	/**
	 * This is the general purpose constructor.
	 */
	public RefinerScreenMsg(boolean skillPower, float cost) {
		super(Protocol.ARCUNTRAINLIST);
		if (skillPower)
			this.unknown01 = 0; //skill/power screen
		else
			this.unknown01 = 2; //stat screen
		this.unknown02 = cost;
		this.unknown03 = 0;
		this.unknown04 = 0;
	}


	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public RefinerScreenMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ARCUNTRAINLIST, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.npcType);
		writer.putInt(this.npcID);
		writer.putInt(this.unknown01);
		writer.putFloat(this.unknown02);
		writer.putInt(this.unknown03);
		writer.putInt(this.unknown04);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.npcType = reader.getInt();
		this.npcID = reader.getInt();
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
		this.unknown04 = reader.getInt();
	}

	public int getNpcType() {
		return this.npcType;
	}

	public int getNpcID() {
		return this.npcID;
	}

	public int getUnknown01() {
		return this.unknown01;
	}

	public float getUnknown02() {
		return this.unknown02;
	}

	public int getUnknown03() {
		return this.unknown03;
	}

	public int getUnknown04() {
		return this.unknown04;
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
}
