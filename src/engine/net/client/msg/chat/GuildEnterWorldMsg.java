// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.chat;

import engine.Enum.GameObjectType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.PlayerCharacter;

public class GuildEnterWorldMsg extends AbstractChatMsg {

	protected String name;
	protected int guildTitle;
	protected int charterType;
	protected int unknown04;
	protected int guildUUID;
	protected int unknown05;
	protected int unknown06;

	/**
	 * This is the general purpose constructor.
	 */
	public GuildEnterWorldMsg(PlayerCharacter source) {
		super(Protocol.GUILDMEMBERONLINE, source, "[!PLAYERRANK!] !PLAYERNAME! is online");

		this.name = "";
		this.guildTitle = 0;
		this.charterType = 9;
		this.unknown02 = 12;
		this.unknown04 = 2;
		this.guildUUID = 0;
		this.unknown05 = 0x2E46D00C;
		this.unknown06 = 0;
	}

	/**
	 * Copy constructor
	 */
	public GuildEnterWorldMsg(GuildEnterWorldMsg msg) {
		super(msg);
        this.name = msg.name;
        this.guildTitle = msg.guildTitle;
        this.charterType = msg.charterType;
        this.unknown04 = msg.unknown04;
        this.guildUUID = msg.guildUUID;
        this.unknown05 = msg.unknown05;
        this.unknown06 = msg.unknown06;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public GuildEnterWorldMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.GUILDMEMBERONLINE, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putString(this.name);

		writer.putInt(guildTitle);
		writer.put((byte) 1); // forgot this
		writer.putInt(this.charterType);
		writer.putInt(this.unknown02);
		writer.putInt(this.unknown04);
                writer.putInt(GameObjectType.Guild.ordinal());
		writer.putInt(this.guildUUID);

		writer.putString(this.message);
		writer.putInt(this.unknown05);
		writer.putInt(this.unknown06);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.name = reader.getString();
		this.guildTitle = reader.getInt();
		reader.get(); // forgot this
		this.unknown02 = reader.getInt();
		this.charterType = reader.getInt();
		this.unknown04 = reader.getInt();
                reader.getInt(); // Object Type Padding
		this.guildUUID = reader.getInt();
		this.message = reader.getString();
		this.unknown05 = reader.getInt();
		this.unknown06 = reader.getInt();
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

	/**
	 * @return the guildTitle
	 */
	public int getGuildTitle() {
		return guildTitle;
	}

	/**
	 * @param guildTitle
	 *            the guildTitle to set
	 */
	public void setGuildTitle(int guildTitle) {
		this.guildTitle = guildTitle;
	}

	/**
	 * @return the unknown02
	 */
	@Override
	public int getUnknown02() {
		return unknown02;
	}

	/**
	 * @param unknown02
	 *            the unknown02 to set
	 */
	@Override
	public void setUnknown02(int unknown02) {
		this.unknown02 = unknown02;
	}

	/**
	 * @return the unknown03
	 */
	public int getCharter() {
		return charterType;
	}

	/**
	 * @param unknown03
	 *            the unknown03 to set
	 */
	public void setCharter(int charter) {
		this.charterType = charter;
	}

	/**
	 * @return the unknown04
	 */
	public int getUnknown04() {
		return unknown04;
	}

	/**
	 * @param unknown04
	 *            the unknown04 to set
	 */
	public void setUnknown04(int unknown04) {
		this.unknown04 = unknown04;
	}

	/**
	 * @return the guildUUID
	 */
	public int getGuildUUID() {
		return guildUUID;
	}

	/**
	 * @param guildUUID
	 *            the guildUUID to set
	 */
	public void setGuildUUID(int guildUUID) {
		this.guildUUID = guildUUID;
	}

	/**
	 * @return the message
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the unknown05
	 */
	public int getUnknown05() {
		return unknown05;
	}

	/**
	 * @param unknown05
	 *            the unknown05 to set
	 */
	public void setUnknown05(int unknown05) {
		this.unknown05 = unknown05;
	}

	/**
	 * @return the unknown06
	 */
	public int getUnknown06() {
		return unknown06;
	}

	/**
	 * @param unknown06
	 *            the unknown06 to set
	 */
	public void setUnknown06(int unknown06) {
		this.unknown06 = unknown06;
	}

}
