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

public class ChatSystemChannelMsg extends AbstractChatMsg {

	// TODO enum this?

	// messageType
	// 1 = Error
	// 2 = Info
	// 3 = Message of the Day
	protected int messageType;

	protected int unknown03;
	protected int unknown04;

	protected int channel;

	/**
	 * This is the general purpose constructor.
	 */
	public ChatSystemChannelMsg(AbstractWorldObject source, String message, int messageType) {
		super(Protocol.SYSTEMCHANNEL, source, message);
		this.channel = 0;
		this.messageType = messageType;
		this.unknown03 = 0;
		this.unknown04 = 0;
	}

	/**
	 * Copy constructor
	 */
	public ChatSystemChannelMsg(ChatSystemChannelMsg msg) {
		super(msg);
        this.messageType = msg.messageType;
        this.unknown03 = msg.unknown03;
        this.unknown04 = msg.unknown04;
        this.channel = msg.channel;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ChatSystemChannelMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.SYSTEMCHANNEL, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.channel = reader.getInt();
		this.messageType = reader.getInt();
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
		this.message = reader.getString();
		this.unknown03 = reader.getInt();
		this.unknown04 = reader.getInt(); // seems to alternate beween 0x0 and
		// 0x40C30000
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.channel);
		writer.putInt(this.messageType);
		if (this.source != null)
			;// writer.putLong(this.source.getCompositeID());
		else
			// writer.putLong(0L);
			writer.putString(this.message);
		writer.putInt(this.unknown03);
		writer.putInt(this.unknown04);

		/*
		 * for (String key : this.vars.keySet()) { writer.putString(key);
		 * writer.putString(this.vars.get(key)); }
		 */
	}

	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            the channel to set
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

	/**
	 * @return the messageType
	 */
	public int getMessageType() {
		return messageType;
	}

	/**
	 * @param messageType
	 *            the messageType to set
	 */
	public void setMessageType(int messageType) {
		this.messageType = messageType;
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

}
