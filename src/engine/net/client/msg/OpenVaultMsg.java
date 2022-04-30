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
 * Vault window opened
 * @author Eighty
 */
public class OpenVaultMsg extends ClientNetMsg {

	private int playerType;
	private int playerID;
	private int npcType;
	private int npcID;
	

	/**
	 * This is the general purpose constructor.
	 */
	public OpenVaultMsg(AbstractGameObject ago, AbstractGameObject target) {
		super(Protocol.OPENVAULT);
		this.playerType = ago.getObjectType().ordinal();
		this.playerID = ago.getObjectUUID();
		this.npcType = target.getObjectType().ordinal();
		this.npcID = target.getObjectUUID();
		
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public OpenVaultMsg(AbstractConnection origin,
			ByteBufferReader reader)  {
		super(Protocol.OPENVAULT, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		playerType = reader.getInt();
		playerID = reader.getInt();
		npcType = reader.getInt();
		npcID = reader.getInt();
		
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(playerType);
		writer.putInt(playerID);
		writer.putInt(npcType);
		writer.putInt(npcID);
	}

}
