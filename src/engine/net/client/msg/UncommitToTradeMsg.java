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


/**
 * Uncommit to trade
 *
 * @author Eighty
 */
public class UncommitToTradeMsg extends ClientNetMsg {

	private int unknown01;
	private long playerCompID;

	/**
	 * This is the general purpose constructor
	 */
	public UncommitToTradeMsg(int unknown01, long playerCompID) {
		super(Protocol.TRADEUNCONFIRM);
		this.unknown01 = unknown01;
		this.playerCompID = playerCompID;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public UncommitToTradeMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.TRADEUNCONFIRM, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		unknown01 = reader.getInt();
		playerCompID = reader.getLong();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(unknown01);
		writer.putLong(playerCompID);
	}

	/**
	 * @return the unknown01
	 */
	public int getUnknown01() {
		return unknown01;
	}

	/**
	 * @param unknown01 the unknown01 to set
	 */
	public void setUnknown01(int unknown01) {
		this.unknown01 = unknown01;
	}

	/**
	 * @return the playerCompID
	 */
	public long getPlayerCompID() {
		return playerCompID;
	}

	/**
	 * @param playerCompID the playerCompID to set
	 */
	public void setPlayerCompID(long playerCompID) {
		this.playerCompID = playerCompID;
	}

}
