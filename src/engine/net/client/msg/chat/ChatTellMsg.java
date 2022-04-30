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
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.server.MBServerStatics;

public class ChatTellMsg extends AbstractChatMsg {

	protected AbstractWorldObject target;
	protected int targetType;
	protected int targetID;
	protected String targetName;
	protected int unknown03;

	/**
	 * This is the general purpose constructor.
	 */
	public ChatTellMsg(AbstractWorldObject source, AbstractWorldObject target, String message) {
		super(Protocol.CHATTELL, source, message);
		this.target = target;

		// TODO could this be simplified? Check serializer;
		if (this.target != null) {
			this.targetType = target.getObjectType().ordinal();
			this.targetID = target.getObjectUUID();
			this.targetName = target.getName();
		}
		this.unknown03 = 0;
	}

	/**
	 * Copy constructor
	 */
	public ChatTellMsg(ChatTellMsg msg) {
		super(msg);
		this.target = msg.target;
		this.targetType = msg.targetType;
		this.targetID = msg.targetID;
		this.targetName = msg.targetName;
		this.unknown03 = msg.unknown03;
	}

	public int getTargetType() {
		return targetType;
	}

	public int getTargetID() {
		return targetID;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ChatTellMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CHATTELL, origin, reader);
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
		this.sourceName = reader.getString(); // sourceName
		this.targetType = reader.getInt();
		this.targetID = reader.getInt();
		this.targetName = reader.getString();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.source.getObjectType().ordinal());
		writer.putInt(source.getObjectUUID());
		writer.putInt(this.unknown01);
		writer.putString(this.message);
		if (AbstractWorldObject.IsAbstractCharacter(source)) {
			writer.putString(((AbstractCharacter) this.source).getFirstName());
		} else {
			writer.putString(this.source.getName());
		}
		if (this.target != null) {
			writer.putInt(this.target.getObjectType().ordinal());
			writer.putInt(this.target.getObjectUUID());
			if (AbstractWorldObject.IsAbstractCharacter(target)) {
				writer.putString(((AbstractCharacter) this.target).getFirstName());
			} else {
				writer.putString(this.target.getName());
			}
		} else {
			writer.putInt(this.targetType);
			writer.putInt(this.targetID);
			writer.putString(this.targetName);
		}
		writer.putInt(MBServerStatics.worldMapID);
		writer.putInt(MBServerStatics.worldMapID);
	}

	/**
	 * @return the target
	 */
	public AbstractWorldObject getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(AbstractWorldObject target) {
		this.target = target;
	}




	/**
	 * @return the targetName
	 */
	public String getTargetName() {
		return targetName;
	}

	/**
	 * @param targetName
	 *            the targetName to set
	 */
	public void setTargetName(String targetName) {
		this.targetName = targetName;
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
