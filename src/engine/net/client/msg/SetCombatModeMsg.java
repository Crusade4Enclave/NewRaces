// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;


/**
 * Attack from outside of combat mode.
 *
 * @author Eighty
 */
public class SetCombatModeMsg extends ClientNetMsg {

	private long playerCompID;
	private boolean toggle;

	/**
	 * This is the general purpose constructor.
	 */
	public SetCombatModeMsg(long playerCompID, boolean toggle) {
		super(Protocol.ARCCOMBATMODEATTACKING);
		this.playerCompID = playerCompID;
		this.toggle = toggle;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public SetCombatModeMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.ARCCOMBATMODEATTACKING, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putLong(playerCompID);
		writer.put(toggle ? (byte) 0x01 : (byte) 0x00);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		this.playerCompID = reader.getLong();
		this.toggle = (reader.get() == 0x01) ? true : false;
	}

	/**
	 * @return the playerCompID
	 */
	public long getPlayerCompID() {
		return playerCompID;
	}

	/**
	 * @return the toggle
	 */
	public boolean getToggle() {
		return toggle;
	}

	/**
	 * @param playerCompID
	 *            the playerCompID to set
	 */
	public void setPlayerCompID(long playerCompID) {
		this.playerCompID = playerCompID;
	}

	/**
	 * @param toggle
	 *            the toggle to set
	 */
	public void setToggle(boolean toggle) {
		this.toggle = toggle;
	}
}
