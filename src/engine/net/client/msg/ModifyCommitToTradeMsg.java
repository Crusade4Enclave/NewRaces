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
 * Commit to trade
 *
 * @author Eighty
 */
public class ModifyCommitToTradeMsg extends ClientNetMsg {

	private int playerType;
	private int playerID;
	private int targetType;
	private int targetID;
	private byte commit1;
	private byte commit2;

	/**
	 * This is the general purpose constructor
	 */
	public ModifyCommitToTradeMsg(AbstractGameObject player, AbstractGameObject target, byte commit1, byte commit2) {
		super(Protocol.TRADECONFIRMSTATUS);
        this.playerType = player.getObjectType().ordinal();
        this.playerID = player.getObjectUUID();
        this.targetType = target.getObjectType().ordinal();
        this.targetID = target.getObjectUUID();
        this.commit1 = commit1;
		this.commit2 = commit2;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public ModifyCommitToTradeMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.TRADECONFIRMSTATUS, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		this.playerType = reader.getInt();
		this.playerID = reader.getInt();
		this.targetType = reader.getInt();
		this.targetID = reader.getInt();
		commit1 = reader.get();
		commit2 = reader.get();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(playerType);
		writer.putInt(playerID);
		writer.putInt(targetType);
		writer.putInt(targetID);
		writer.put(commit1);
		writer.put(commit2);
	}

	

	/**
	 * @return the commit1
	 */
	public byte getCommit1() {
		return commit1;
	}

	/**
	 * @param commit1 the commit1 to set
	 */
	public void setCommit1(byte commit1) {
		this.commit1 = commit1;
	}

	/**
	 * @return the commit2
	 */
	public byte getCommit2() {
		return commit2;
	}

	/**
	 * @param commit2 the commit2 to set
	 */
	public void setCommit2(byte commit2) {
		this.commit2 = commit2;
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

	public int getTargetType() {
		return targetType;
	}

	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	public int getTargetID() {
		return targetID;
	}

	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

}
