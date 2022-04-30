// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;

public abstract class ClientNetMsg extends AbstractNetMsg {

	/**
	 * This is the general purpose constructor.
	 */
	protected ClientNetMsg(Protocol protocolMsg) {
		super(protocolMsg);
	}

	protected ClientNetMsg(Protocol protocolMsg, ClientNetMsg msg) {
		super(protocolMsg, msg);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	protected ClientNetMsg(Protocol protocolMsg, AbstractConnection origin,
                           ByteBufferReader reader)  {
		super(protocolMsg, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected abstract void _deserialize(ByteBufferReader reader)
			;

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected abstract void _serialize(ByteBufferWriter writer)
			throws SerializationException;

	/*
	 * return the header size of 4 for ClientMsgs
	 */
	@Override
	protected int getHeaderSize() {
		return 4;
	}

	/*
	 * Write in the header for ClientMsgs
	 */
	@Override
	protected void writeHeaderAt(int startPos, ByteBufferWriter writer) {
		writer.putIntAt(this.getProtocolMsg().opcode, startPos);
	}
}
