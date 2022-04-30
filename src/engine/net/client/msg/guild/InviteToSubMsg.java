// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.guild;

import engine.Enum.GameObjectType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.GuildTag;

public class InviteToSubMsg extends ClientNetMsg {

	//12d67402 0000f062 56253600 0010b062 d7821800 00000000
	//10000000 4e006f0020006500720072006f00720020006d00650073007300610067006500
	//0a000000 03000000 07000000 a0000000 04000000
	//17000000 4800650072006f006500730020006f0066002000530065006100200044006f006700730020005200650073007400
	//0000200a 33000000 00000000 01000000

	//12d67402 0000d062 11fe1700 00009063 3ee91300 00000000
	//10000000 4e006f0020006500720072006f00720020006d00650073007300610067006500
	//08000000 08000000 07000000 96000000 0d000000
	//11000000 42006c006f006f00640020004d006f006f006e00200052006900730069006e006700
	//0000c006 64540000 01000000 00000000

	//12d67402 0000e062 db2d0000 00005063 116e1100 00000000
	//10000000 4e006f0020006500720072006f00720020006d00650073007300610067006500
	//05000000 11000000 0e000000 95000000 05000000
	//09000000 530074006f0072006d0068006f006c006400
	//0000c006 56300000 01000000 00000000

	private int sourceUUID;
	private int targetUUID;
	private int unknown01;
	private String message;
	private GuildTag gt;
	private String guildName;
	private int guildUUID;
	private int unknown02;
	private int unknown03;

	/**
	 * This is the general purpose constructor.
	 */
	public InviteToSubMsg() {
		super(Protocol.INVITEGUILDFEALTY);
		this.message = "No error message";
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public InviteToSubMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.INVITEGUILDFEALTY, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
                writer.putInt(GameObjectType.PlayerCharacter.ordinal());
		writer.putInt(this.sourceUUID);
                writer.putInt(GameObjectType.PlayerCharacter.ordinal());
		writer.putInt(this.targetUUID);
		writer.putInt(this.unknown01);
		writer.putString(this.message);
		writer.putInt(this.gt.backgroundColor01);
		writer.putInt(this.gt.backgroundColor02);
		writer.putInt(this.gt.symbolColor);
		writer.putInt(this.gt.symbol);
		writer.putInt(this.gt.backgroundDesign);
		writer.putString(this.guildName);
		writer.putInt(GameObjectType.Guild.ordinal());
		writer.putInt(this.guildUUID);
		writer.putInt(this.unknown02);
		writer.putInt(this.unknown03);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.getInt();
                this.sourceUUID = reader.getInt();
                reader.getInt();
		this.targetUUID = reader.getInt();
		this.unknown01 = reader.getInt();
		this.message = reader.getString();
		this.gt = new GuildTag(reader);
		this.guildName = reader.getString();
		reader.getInt(); // Padding for Object Type
		this.guildUUID = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
	}

	public GuildTag getGuildTag() {
		return this.gt;
	}

	public void setGuildTag(GuildTag gt) {
		this.gt = gt;
	}

	/**
	 * @return the sourceUUID
	 */
	public int getSourceUUID() {
		return sourceUUID;
	}

	/**
	 * @return the targetUUID
	 */
	public int getTargetUUID() {
		return targetUUID;
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

	/**
	 * @return the guildName
	 */
	public String getGuildName() {
		return guildName;
	}

	/**
	 * @param guildName
	 *            the guildName to set
	 */
	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}


	public int getUnknown02() {
		return unknown02;
	}

	public void setUnknown02(int unknown02) {
		this.unknown02 = unknown02;
	}

	public int getUnknown03() {
		return unknown03;
	}

	public void setUnknown03(int unknown03) {
		this.unknown03 = unknown03;
	}

	public int getGuildUUID() {
		return guildUUID;
	}

	public void setGuildUUID(int guildUUID) {
		this.guildUUID = guildUUID;
	}
}
