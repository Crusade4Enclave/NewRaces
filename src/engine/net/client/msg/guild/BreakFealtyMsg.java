// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.net.client.msg.guild;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;

public class BreakFealtyMsg extends ClientNetMsg {

	private int guildUUID;

	/**
	 * This is the general purpose constructor.
	 */
	public BreakFealtyMsg() {
		super(Protocol.BREAKFEALTY);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public BreakFealtyMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.BREAKFEALTY, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {

	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
                        reader.getInt(); // Object Type Padding
			this.guildUUID = reader.getInt();
			reader.getInt();
	}

	public int getGuildUUID() {
		return guildUUID;
	}

}
