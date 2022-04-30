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

public class ChatPvPMsg extends AbstractChatMsg {

	protected int unknown03;

	/**
	 * This is the general purpose constructor.
	 */
	public ChatPvPMsg(AbstractWorldObject source, String message) {
		super(Protocol.CHATPVP, source, message);
		this.unknown03 = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ChatPvPMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CHATPVP, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public ChatPvPMsg(ChatPvPMsg msg) {
		super(msg);
        this.unknown03 = msg.unknown03;
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
		this.message = reader.getString();
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(0); // Probably Type
		writer.putInt(0); // Probably objectUUID
		writer.putInt(0);

		writer.putString(this.message);
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

}
