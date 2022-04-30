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

public class FormationFollowMsg extends ClientNetMsg {

	private boolean follow;
	private int formation;
	private int unknown01;

	/**
	 * This is the general purpose constructor.
	 */
	public FormationFollowMsg() {
		super(Protocol.GROUPFOLLOW);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public FormationFollowMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.GROUPFOLLOW, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		if (this.follow) {
			writer.putInt(1);
			writer.putInt(0);
		} else {
			writer.putInt(0);
			writer.putInt(1);
		}
		writer.putInt(this.formation);
		writer.putInt(this.unknown01);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
        this.follow = reader.getInt() == 1;
		reader.getInt();
		this.formation = reader.getInt();
		this.unknown01 = reader.getInt();
	}

	/**
	 * @return the follow
	 */
	public boolean isFollow() {
		return follow;
	}

	/**
	 * @param follow
	 *            the follow to set
	 */
	public void setFollow(boolean follow) {
		this.follow = follow;
	}

	/**
	 * @return the formation
	 */
	public int getFormation() {
		return formation;
	}

	/**
	 * @param formation
	 *            the formation to set
	 */
	public void setFormation(int formation) {
		this.formation = formation;
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

}
