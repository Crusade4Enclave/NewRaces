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
import engine.server.MBServerStatics;

import java.util.concurrent.ConcurrentHashMap;


public class ChatSystemMsg extends AbstractChatMsg {

	// TODO enum this?

	// channel
	// 1 = System
	// 2 = General Announcement (Flashing at top of screen!)
	// 3 = Commander
	// 5 = Nation
	// 6 = Leader
	// 7 = Shout
	// 8 = Siege
	// 9 = Territory
	// 10 = Info
	// 12 = Guild
	// 13 = Inner Council
	// 14 = Group
	// 15 = City
	// 16 = say
	// 17 = Emote
	// 19 = tell
	protected int channel;

	// messageType
	// 1 = Error
	// 2 = Info
	// 3 = Message of the Day
	protected int messageType;

	protected int unknown03;
	protected int numVariables;
	// TODO this doesn't need to be a global value,
	// its inherit to the HashMap
	protected ConcurrentHashMap<String, String> vars;

	// TODO make a list of variables
	/**
	 * This is the general purpose constructor.
	 */
	public ChatSystemMsg(AbstractWorldObject source, String message) {
		super(Protocol.SYSTEMBROADCASTCHANNEL, source, message);
		this.messageType = 2;
		this.unknown03 = 0;
		this.numVariables = 0;
		vars = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ChatSystemMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.SYSTEMBROADCASTCHANNEL, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public ChatSystemMsg(ChatSystemMsg msg) {
		super(msg);
        this.channel = msg.channel;
        this.messageType = msg.messageType;
        this.unknown03 = msg.unknown03;
        this.numVariables = msg.numVariables;
        this.vars = msg.vars;
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.vars = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

		this.channel = reader.getInt();
		this.messageType = reader.getInt();
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();

		this.message = reader.getString();
		this.unknown03 = reader.getInt();
		this.numVariables = reader.getInt();

		for (int i = 0; i < this.numVariables; ++i) {
			String key = reader.getString();
			String value = reader.getString();
			reader.get();
			this.vars.put(key, value);
		}

	}


	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.channel);
		writer.putInt(this.messageType);
		if (this.source != null){
			writer.putInt(source.getObjectType().ordinal());
			writer.putInt(source.getObjectUUID());
		}
		else
			writer.putLong(0L);
		writer.putString(this.message);
		writer.putInt(this.unknown03);
		writer.putInt(this.numVariables);

		for (String key : this.vars.keySet()) {
			writer.putString(key);
			writer.putString(this.vars.get(key));
		}
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
	 * @return the numVariables
	 */
	public int getNumVariables() {
		return numVariables;
	}



	/**
	 * @return the vars
	 */
	public ConcurrentHashMap<String, String> getVars() {
		return vars;
	}


}
