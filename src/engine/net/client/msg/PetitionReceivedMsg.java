// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;


public class PetitionReceivedMsg extends ClientNetMsg {

	// TODO pull these statics out into SBEmuStatics.java
	private static final int PETITION_NEW = 1;
	private static final int PETITION_CANCEL = 2;

	private static final int TYPE_GENERAL_HELP = 1;
	private static final int TYPE_FEEDBACK = 2;
	private static final int TYPE_STUCK = 3;
	private static final int TYPE_HARASSMENT = 4;
	private static final int TYPE_EXPLOIT = 5;
	private static final int TYPE_BUG = 6;
	private static final int TYPE_GAME_STOPPER = 7;
	private static final int TYPE_TECH_SUPPORT = 8;

	private static final int SUBTYPE_EXPLOIT_DUPE = 1;
	private static final int SUBTYPE_EXPLOIT_LEVELING = 2;
	private static final int SUBTYPE_EXPLOIT_SKILL_GAIN = 3;
	private static final int SUBTYPE_EXPLOIT_KILLING = 4;
	private static final int SUBTYPE_EXPLOIT_POLICY = 5;
	private static final int SUBTYPE_EXPLOIT_OTHER = 6;
	private static final int SUBTYPE_TECH_VIDEO = 7;
	private static final int SUBTYPE_TECH_SOUND = 8;
	private static final int SUBTYPE_TECH_NETWORK = 9;
	private static final int SUBTYPE_TECH_OTHER = 10;

	private int petition;
	private int unknown01;
	private int unknown02;
	private byte unknownByte01;
	private int unknown03;
	private int unknown04;
	private int unknown05;
	private int unknown06;
	private int type;
	private int subType;
	private String compType;
	private String language;
	private int unknown07;
	private String message;

	/**
	 * This is the general purpose constructor.
	 */
	public PetitionReceivedMsg() {
		super(Protocol.CUSTOMERPETITION);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public PetitionReceivedMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CUSTOMERPETITION, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.petition);
		if (this.petition == PETITION_NEW) {
			writer.putInt(this.unknown01);
			writer.putInt(this.unknown02);
			writer.put(this.unknownByte01);
			writer.putInt(this.unknown03);
			writer.putInt(this.unknown04);
			writer.putInt(this.unknown05);
			writer.putInt(this.unknown06);
			writer.putInt(this.type);
			writer.putInt(this.subType);
			writer.putString(this.compType);
			writer.putString(this.language);
			writer.putInt(this.unknown07);
			writer.putUnicodeString(message);
		} else if (this.petition == PETITION_CANCEL) {
			writer.putInt(this.unknown01);
			writer.putInt(this.unknown02);
			writer.put(this.unknownByte01);
			writer.putInt(this.unknown03);
			writer.putInt(this.unknown04);
		}
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		petition = reader.getInt();
		if (petition == PETITION_NEW) {
			this.unknown01 = reader.getInt();
			this.unknown02 = reader.getInt();
			this.unknownByte01 = reader.get();
			this.unknown03 = reader.getInt();
			this.unknown04 = reader.getInt();
			this.unknown05 = reader.getInt();
			this.unknown06 = reader.getInt();
			this.type = reader.getInt();
			this.subType = reader.getInt();
			this.compType = reader.getString();
			this.language = reader.getString();
			this.unknown07 = reader.getInt();
			this.message = reader.getUnicodeString();
		} else if (petition == PETITION_CANCEL) {
			this.unknown01 = reader.getInt();
			this.unknown02 = reader.getInt();
			this.unknownByte01 = reader.get();
			this.unknown03 = reader.getInt();
			this.unknown04 = reader.getInt();
		}
	}

	/**
	 * @return the petition
	 */
	public int getPetition() {
		return petition;
	}

	/**
	 * @param petition
	 *            the petition to set
	 */
	public void setPetition(int petition) {
		this.petition = petition;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the subType
	 */
	public int getSubType() {
		return subType;
	}

	/**
	 * @param subType
	 *            the subType to set
	 */
	public void setSubType(int subType) {
		this.subType = subType;
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

	public int getUnknown01() {
		return this.unknown01;
	}

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}

	public int getUnknown02() {
		return this.unknown02;
	}

	public void setUnknown02(int value) {
		this.unknown02 = value;
	}

	public int getUnknown03() {
		return this.unknown03;
	}

	public void setUnknown03(int value) {
		this.unknown03 = value;
	}

	public int getUnknown04() {
		return this.unknown04;
	}

	public void setUnknown04(int value) {
		this.unknown04 = value;
	}

	public int getUnknown05() {
		return this.unknown05;
	}

	public void setUnknown05(int value) {
		this.unknown05 = value;
	}

	public int getUnknown06() {
		return this.unknown06;
	}

	public void setUnknown06(int value) {
		this.unknown06 = value;
	}

	public int getUnknown07() {
		return this.unknown07;
	}

	public void setUnknown07(int value) {
		this.unknown07 = value;
	}

	public byte getUnknownByte01() {
		return this.unknownByte01;
	}

	public void setUnknownByte01(byte value) {
		this.unknownByte01 = value;
	}

	public String getCompType() {
		return this.compType;
	}

	public void setCompType(String value) {
		this.compType = value;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String value) {
		this.language = value;
	}
}
