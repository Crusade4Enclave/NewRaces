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
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.AbstractGameObject;

/**
 * Bank inventory requested
 *
 * @author Burfo
 */
public class ReqBankInventoryMsg extends ClientNetMsg {
	
	private int playerType;
	private int playerID;
	private long unknown01; // possibly NPC ID?

	/**
	 * This is the general purpose constructor.
	 */
	public ReqBankInventoryMsg(AbstractGameObject ago, long unknown01) {
		super(Protocol.OKCOSTTOOPENBANK);
		this.playerType = ago.getObjectType().ordinal();
		this.playerID = ago.getObjectUUID();
		this.unknown01 = unknown01;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ReqBankInventoryMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.OKCOSTTOOPENBANK, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		playerType = reader.getInt();
		playerID = reader.getInt();
		unknown01 = reader.getLong();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {
		writer.putInt(playerType);
		writer.putInt(playerID);
		writer.putLong(unknown01);
	}

	public int getPlayerType() {
		return playerType;
	}

	public void setPlayerType(int playerType) {
		this.playerType = playerType;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}
        
}
