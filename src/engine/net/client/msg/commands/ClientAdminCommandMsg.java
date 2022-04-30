// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.commands;

import engine.Enum.GameObjectType;
import engine.gameManager.DbManager;
import engine.math.Vector3f;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.AbstractGameObject;

public class ClientAdminCommandMsg extends ClientNetMsg {

	private int msgType;
	private String msgCommand;
	private long senderCompID;
        private int targetType;
	private int targetUUID;
	private Vector3f location;
	private AbstractGameObject target;

	/**
	 * This is the general purpose constructor.
	 */
	public ClientAdminCommandMsg() {
		super(Protocol.CLIENTADMINCOMMAND);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ClientAdminCommandMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CLIENTADMINCOMMAND, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		// TODO implement Serialize()
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.msgType = reader.getInt();
		this.msgCommand = reader.getString();
                this.targetType = reader.getInt();
		this.targetUUID = reader.getInt();
		this.senderCompID = reader.getLong(); // always null??
		this.location = new Vector3f(reader.getFloat(), reader.getFloat(), reader.getFloat());

		if (targetUUID != 0) 
                    target = DbManager.getObject(GameObjectType.values()[this.targetType], this.targetUUID);
	}

	/**
	 * @return the msgCommand
	 */
	public String getMsgCommand() {
		return msgCommand;
	}

	/**
	 * @return the targetUUID
	 */
	public int getTargetUUID() {
		return targetUUID;
	}

	/**
	 * @return the location
	 */
	public Vector3f getLocation() {
		return location;
	}

	/**
	 * @return the target
	 */
	public AbstractGameObject getTarget() {
		return target;
	}

}
