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


public class UpgradeAssetMessage extends ClientNetMsg {

	private int unknown01;
	private int unknown02;
	private int buildingUUID;


    /**
	 * This is the general purpose constructor.
	 */
	public UpgradeAssetMessage() {
		super(Protocol.UPGRADEASSET);
	
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UpgradeAssetMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.UPGRADEASSET, origin, reader);
	}

	/**
	 * @see AbstractNetMsg#getPowerOfTwoBufferSize()
	 */
	@Override
	protected int getPowerOfTwoBufferSize() {
		//Larger size for historically larger opcodes
		return (16); // 2^16 == 64k
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
	
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
                reader.getInt(); // Object Type Padding
		this.buildingUUID = reader.getInt();
	}

	public void setUnknown01(int unknown01) {
		this.unknown01 = unknown01;
	}

	public int getUnknown01() {
		return unknown01;
	}

	public void setUnknown02(int unknown02) {
		this.unknown02 = unknown02;
	}

	public int getUnknown02() {
		return unknown02;
	}

	public int getBuildingUUID() {
		return buildingUUID;
	}
}
