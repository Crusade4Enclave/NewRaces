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

public class RemoveFromGroupMsg extends ClientNetMsg {

	private int targetType;
	private int targetID;
	private int response;
	private String message;

	/**
	 * This is the general purpose constructor.
	 */
	public RemoveFromGroupMsg() {
		super(Protocol.GROUPREMOVE);

		this.message = "Quit if you want to remove yourself";
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public RemoveFromGroupMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.GROUPREMOVE, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.targetType);
		writer.putInt(this.targetID);
		writer.putInt(this.response);
		if (this.response == 1)
			writer.putString(this.message);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.targetType = reader.getInt();
		this.targetID = reader.getInt();
		this.response = reader.getInt();
		if (this.response == 1)
			this.message = reader.getString();
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
	 * @return the response
	 */
	public int getResponse() {
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(int response) {
		this.response = response;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
