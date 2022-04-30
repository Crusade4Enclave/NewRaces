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

public class GroupInviteMsg extends ClientNetMsg {

	private int sourceType;
	private int sourceID;
	private int targetType;
	private int targetID;
	private int groupType;
	private int groupID;
	private int unknown01;
	private int invited;
	private String name;

	/**
	 * This is the general purpose constructor.
	 */
	public GroupInviteMsg() {
		super(Protocol.INVITEGROUP);
		this.name = "";
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public GroupInviteMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.INVITEGROUP, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);
		writer.putInt(this.targetType);
		writer.putInt(this.targetID);
		writer.putInt(this.groupType);
		writer.putInt(this.groupID);
		writer.putInt(this.unknown01);
		writer.putInt(this.invited);
		if (this.invited == 1)
			writer.putString(this.name);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {

		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
		this.targetType = reader.getInt();
		this.targetID = reader.getInt();
		this.groupType = reader.getInt();
		this.groupID = reader.getInt();
		this.unknown01 = reader.getInt();
		this.invited = reader.getInt();
		if (this.invited == 1)
			this.name = reader.getString();

	}

	/**
	 * @return the sourceType
	 */
	public int getSourceType() {
		return sourceType;
	}

	/**
	 * @param sourceType
	 *            the sourceType to set
	 */
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * @return the sourceID
	 */
	public int getSourceID() {
		return sourceID;
	}

	/**
	 * @param sourceID
	 *            the sourceID to set
	 */
	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}

	/**
	 * @return the targetType
	 */
	public int getTargetType() {
		return targetType;
	}

	/**
	 * @param targetType
	 *            the targetType to set
	 */
	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	/**
	 * @return the targetID
	 */
	public int getTargetID() {
		return targetID;
	}

	/**
	 * @param targetID
	 *            the targetID to set
	 */
	public void setTargetID(int targetID) {
		this.targetID = targetID;
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
	 * @return the invited
	 */
	public int getInvited() {
		return invited;
	}

	/**
	 * @param invited
	 *            the invited to set
	 */
	public void setInvited(int invited) {
		this.invited = invited;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
