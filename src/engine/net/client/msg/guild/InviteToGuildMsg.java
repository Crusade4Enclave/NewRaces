// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.guild;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.GuildTag;

public class InviteToGuildMsg extends ClientNetMsg {

	private int response;
	private int sourceType;
	private int sourceUUID;
	private int targetType;
	private int targetUUID;
	private int unknown01;
	private String message;
	private GuildTag gt;
	private String guildName;
	private int guildType;
	private int guildID;
	private String targetName;

	/**
	 * This is the general purpose constructor.
	 */
	public InviteToGuildMsg() {
		super(Protocol.INVITETOGUILD);
		this.response = 0;
		this.message = "No error message";
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public InviteToGuildMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.INVITETOGUILD, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.response);
		writer.putLong(this.sourceUUID);
		writer.putLong(this.targetUUID);
		writer.putInt(this.unknown01);
		writer.putString(this.message);
		writer.putInt(this.gt.backgroundColor01);
		writer.putInt(this.gt.backgroundColor02);
		writer.putInt(this.gt.symbolColor);
		writer.putInt(this.gt.symbol);
		writer.putInt(this.gt.backgroundDesign);
		writer.putString(this.guildName);
		writer.putInt(this.guildType);
		writer.putInt(this.guildID);
		writer.putString(this.targetName);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.response = reader.getInt();
		this.sourceType = reader.getInt();
		this.sourceUUID = reader.getInt();
		this.targetType = reader.getInt();
		this.targetUUID = reader.getInt();
		this.unknown01 = reader.getInt();
		this.message = reader.getString();
		this.gt = new GuildTag(reader);
		this.guildName = reader.getString();
		this.guildType = reader.getInt();
		this.guildID = reader.getInt();
		this.targetName = reader.getString();
	}

	public GuildTag getGuildTag() {
		return this.gt;
	}

	public void setGuildTag(GuildTag gt) {
		this.gt = gt;
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
	 * @return the sourceUUID
	 */
	public int getSourceUUID() {
		return sourceUUID;
	}

	/**
	 * @param sourceUUID
	 *            the sourceUUID to set
	 */
	public void setSourceUUID(int sourceUUID) {
		this.sourceUUID = sourceUUID;
	}

	/**
	 * @return the targetUUID
	 */
	public int getTargetUUID() {
		return targetUUID;
	}

	/**
	 * @param targetUUID
	 *            the targetUUID to set
	 */
	public void setTargetUUID(int targetUUID) {
		this.targetUUID = targetUUID;
	}

	/**
	 * @param guildName
	 *            the guildName to set
	 */
	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}

	/**
	 * @param guildID
	 *            the guildID to set
	 */
	public void setGuildUUID(int guildID) {
		this.guildID = guildID;
	}

	/**
	 * @return the targetName
	 */
	public String getTargetName() {
		return targetName;
	}

	/**
	 * @param targetName
	 *            the targetName to set
	 */
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	public void setGuildType(int guildType) {
		this.guildType = guildType;
	}

    /**
     * @return the targetType
     */
    public int getTargetType() {
        return targetType;
    }
}
