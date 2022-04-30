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
 * Reject trade request
 *
 * @author Eighty
 */
public class RejectTradeRequestMsg extends ClientNetMsg {

	private int unknown01; //pad?
	private long playerCompID;
	private long targetCompID;

	/**
	 * This is the general purpose constructor
	 */
	public RejectTradeRequestMsg(int unknown01, long playerCompID, long targetCompID) {
		super(Protocol.REQUESTTRADECANCEL);
		this.unknown01 = unknown01;
		this.playerCompID = playerCompID;
		this.targetCompID = targetCompID;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public RejectTradeRequestMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.REQUESTTRADECANCEL, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		unknown01 = reader.getInt();
		playerCompID = reader.getLong();
		targetCompID = reader.getLong();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(unknown01);
		writer.putLong(playerCompID);
		writer.putLong(targetCompID);
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

	/**
	 * @return the targetCompID
	 */
	public long getTargetCompID() {
		return targetCompID;
	}

	/**
	 * @param targetCompID the targetCompID to set
	 */
	public void setTargetCompID(long targetCompID) {
		this.targetCompID = targetCompID;
	}

}
