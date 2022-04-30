// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.group;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;

public class GroupInviteResponseMsg extends ClientNetMsg {

	private int groupType;
	private int groupID;
	private int unknown01;
	private int unknown02;

	/**
	 * This is the general purpose constructor.
	 */
	public GroupInviteResponseMsg() {
		super(Protocol.JOINGROUP);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public GroupInviteResponseMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.JOINGROUP, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.groupType);
		writer.putInt(this.groupID);
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.groupType = reader.getInt();
		this.groupID = reader.getInt();
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
	}

	/**
	 * @return the groupType
	 */
	public int getGroupType() {
		return groupType;
	}

	/**
	 * @param groupType
	 *            the groupType to set
	 */
	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	/**
	 * @return the groupID
	 */
	public int getGroupID() {
		return groupID;
	}

	/**
	 * @param groupID
	 *            the groupID to set
	 */
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	/**
	 * @return the unknown01
	 */
	public int getUnknown01() {
		return unknown01;
	}

	/**
	 * @param unknown01
	 *            the unknown01 to set
	 */
	public void setUnknown01(int unknown01) {
		this.unknown01 = unknown01;
	}

	/**
	 * @return the unknown02
	 */
	public int getUnknown02() {
		return unknown02;
	}

	/**
	 * @param unknown02
	 *            the unknown02 to set
	 */
	public void setUnknown02(int unknown02) {
		this.unknown02 = unknown02;
	}

}
