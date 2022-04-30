// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.chat;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.AbstractWorldObject;

public class ChatICMsg extends AbstractChatMsg {

	protected int unknown03;
	protected int unknown04;
	protected int unknown05;

	/**
	 * This is the general purpose constructor.
	 */
	public ChatICMsg(AbstractWorldObject source, String message) {
		super(Protocol.CHATIC, source, message);
		this.unknown03 = 0;
		this.unknown04 = 0;
		this.unknown05 = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ChatICMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CHATIC, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public ChatICMsg(ChatICMsg msg) {
		super(msg);
        this.unknown03 = msg.unknown03;
        this.unknown04 = msg.unknown04;
        this.unknown05 = msg.unknown05;
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
		this.unknown01 = reader.getInt();
		this.message = reader.getString();
		this.sourceName = reader.getString();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
		this.unknown04 = reader.getInt();
		this.unknown05 = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);
		writer.putInt(this.unknown01);
		writer.putString(this.message);
		writer.putString(this.sourceName);
		writer.putInt(this.unknown02);
		writer.putInt(this.unknown03);
		writer.putInt(this.unknown04);
		writer.putInt(this.unknown05);
	}

	/**
	 * @return the unknown03
	 */
	public int getUnknown03() {
		return unknown03;
	}

	/**
	 * @param unknown03
	 *            the unknown03 to set
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
	 * @param unknown04
	 *            the unknown04 to set
	 */
	public void setUnknown04(int unknown04) {
		this.unknown04 = unknown04;
	}

	/**
	 * @return the unknown05
	 */
	public int getUnknown05() {
		return unknown05;
	}

	/**
	 * @param unknown05
	 *            the unknown05 to set
	 */
	public void setUnknown05(int unknown05) {
		this.unknown05 = unknown05;
	}

}