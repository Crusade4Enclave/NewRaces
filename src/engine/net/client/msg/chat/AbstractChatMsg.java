// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.chat;


import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.AbstractWorldObject;

public abstract class AbstractChatMsg extends ClientNetMsg {

	protected int sourceType;
	protected int sourceID;
	protected String sourceName;
	protected AbstractWorldObject source;

	protected int unknown01;
	protected String message;
	protected int unknown02;

	/**
	 * This is the general purpose constructor.
	 */
	protected AbstractChatMsg(Protocol protocolMsg, AbstractWorldObject source, String message) {
		super(protocolMsg);

		this.unknown01 = 0;
		this.message = message;
		this.source = source;
		this.unknown02 = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	protected AbstractChatMsg(Protocol protocolMsg, AbstractConnection origin, ByteBufferReader reader) {
		super(protocolMsg, origin, reader);

		//THIS may cause initialization error, but messing up guild chat
		//this.unknown01 = 0;
		//this.unknown02 = 0;
	}

	/**
	 * Copy constructor
	 */
	protected AbstractChatMsg(AbstractChatMsg msg) {
		super(msg.getProtocolMsg());
        this.sourceType = msg.sourceType;
        this.sourceID = msg.sourceID;
        this.sourceName = msg.sourceName;
        this.source = msg.source;
        this.unknown01 = msg.unknown01;
		this.message = msg.getMessage();
		this.unknown02 = msg.getUnknown02();
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected abstract void _deserialize(ByteBufferReader reader) ;

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected abstract void _serialize(ByteBufferWriter writer) throws SerializationException;

	/**
	 * @return the unknown01
	 */
	public int getUnknown01() {
		return unknown01;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the unknown02
	 */
	public int getUnknown02() {
		return unknown02;
	}

	/**
	 * @param unknown01
	 *            the unknown01 to set
	 */
	public void setUnknown01(int unknown01) {
		this.unknown01 = unknown01;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @param unknown02
	 *            the unknown02 to set
	 */
	public void setUnknown02(int unknown02) {
		this.unknown02 = unknown02;
	}

	/**
	 * @return the sourceObjName
	 */
	public AbstractWorldObject getSource() {
		return source;
	}

	/**
	 * @param sourceObjName
	 *            the sourceObjName to set
	 */
	public void setSource(AbstractWorldObject source) {
		this.source = source;
	}

	/**
	 * @return the sourceType
	 */
	public int getSourceType() {
		return sourceType;
	}

	/**
	 * @return the sourceID
	 */
	public int getSourceID() {
		return sourceID;
	}

	
	/**
	 * @return the sourceName
	 */
	public String getSourceName() {
		return sourceName;
	}

	/**
	 * @param sourceType
	 *            the sourceType to set
	 */
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * @param sourceID
	 *            the sourceID to set
	 */
	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}

	/**
	 * @param sourceName
	 *            the sourceName to set
	 */
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

}
