// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.chat;


import engine.gameManager.SessionManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.objects.AbstractWorldObject;
import engine.server.MBServerStatics;
import engine.session.Session;

public class ChatCSRMsg extends AbstractChatMsg {

	/**
	 * This is the general purpose constructor.
	 */
	public ChatCSRMsg(AbstractWorldObject source, String message) {
		super(Protocol.CHATCSR, source, message);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ChatCSRMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CHATCSR, origin, reader);
		Session s = SessionManager.getSession((ClientConnection) origin);
		if (s == null)
			return;
		this.source = s.getPlayerCharacter();
	}

	/**
	 * Copy constructor
	 */
	public ChatCSRMsg(ChatCSRMsg msg) {
		super(msg);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.getInt();
		reader.getLong();
		this.message = reader.getString();
		reader.getInt(); // pad
		this.unknown02 = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.source.getObjectType().ordinal());
		writer.putInt(this.source.getObjectUUID());
		writer.putInt(this.unknown01);
		writer.putString(this.message);
		writer.putString(this.source.getName());
		writer.putInt(MBServerStatics.worldMapID);
	}

}
