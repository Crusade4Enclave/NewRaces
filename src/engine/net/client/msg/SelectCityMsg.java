// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.PlayerCharacter;


public class SelectCityMsg extends ClientNetMsg {

	private PlayerCharacter pc;
	private boolean isTeleport;

	/**
	 * This is the general purpose constructor.
	 */
	public SelectCityMsg(PlayerCharacter pc, boolean isTeleport) {
		super(Protocol.SELECTCITY);
		this.pc = pc;
		this.isTeleport = isTeleport;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public SelectCityMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.SELECTCITY, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public SelectCityMsg(SelectCityMsg msg) {
		super(Protocol.SELECTCITY);
		this.pc = msg.pc;
		this.isTeleport = msg.isTeleport;
	}

	/**
	 * @see AbstractNetMsg#getPowerOfTwoBufferSize()
	 */
	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return (12);
	}
	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		//Do we even want to try this?
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
	writer.put((byte)0);
	}

	public PlayerCharacter pc() {
		return this.pc;
	}

	public boolean isTeleport() {
		return this.isTeleport;
	}

	public void setPC(PlayerCharacter pc) {
		this.pc = pc;
	}

	public void setIsTeleport(boolean value) {
		this.isTeleport = value;
	}
}
