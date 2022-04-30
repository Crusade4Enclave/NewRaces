// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.chat;

import engine.Enum.GameObjectType;
import engine.gameManager.SessionManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractGameObject;
import engine.objects.AbstractWorldObject;
import engine.server.MBServerStatics;

public class ChatCityMsg extends AbstractChatMsg {

	/**
	 * This is the general purpose constructor.
	 */
	public ChatCityMsg(AbstractWorldObject source, String message) {
		super(Protocol.CHATCITY, source, message);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ChatCityMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CHATCITY, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public ChatCityMsg(ChatCityMsg msg) {
		super(msg);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		long sourceID = reader.getLong();
		int objectUUID = AbstractGameObject.extractUUID(GameObjectType.PlayerCharacter, sourceID);
		this.source = SessionManager.getPlayerCharacterByID(objectUUID);
		this.unknown01 = reader.getInt();
		this.message = reader.getString();
		this.sourceName = reader.getString();
		this.unknown02 = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		// TODO Implement Serialize
		if (this.source != null){
			writer.putInt(source.getObjectType().ordinal());
			writer.putInt(source.getObjectUUID());
		}
			
		else
			writer.putLong(0L);
		writer.putInt(0);
		writer.putString(this.message);
		if (this.source == null) {
			// TODO log error here
			writer.putString("");
			writer.putInt(0);
		} else {
			writer.putString(((AbstractCharacter) this.source).getFirstName());
			writer.putInt(MBServerStatics.worldMapID);
		}
	}

}
