// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.login;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;

public class CommitNewCharacterMsg extends ClientNetMsg {

	private String firstName;
	private String lastName;
	private int serverID;
	private int hairStyle;
	private int beardStyle;
	private int skinColor;
	private int hairColor;
	private int beardColor;
	private int kit;
	private int numRunes;
	private int[] runes;
	private int numStats;
	private int strengthMod;
	private int dexterityMod;
	private int constitutionMod;
	private int intelligenceMod;
	private int spiritMod;

	/**
	 * This is the general purpose constructor.
	 */
	public CommitNewCharacterMsg() {
		super(Protocol.CREATECHAR);
		runes = new int[23];
		strengthMod = 0;
		dexterityMod = 0;
		constitutionMod = 0;
		intelligenceMod = 0;
		spiritMod = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public CommitNewCharacterMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CREATECHAR, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {

		writer.putString(this.firstName);
		writer.putString(this.lastName);

		writer.putInt(this.serverID);
		writer.putInt(0);
		writer.putInt(this.hairStyle);
		writer.putInt(0);
		writer.putInt(this.beardStyle);
		writer.putInt(this.skinColor);
		writer.putInt(this.hairColor);
		writer.putInt(this.beardColor);
		writer.putInt(this.kit);
		for (int i = 0; i < 23; i++) {
			writer.putInt(0);
			writer.putInt(this.runes[i]);
			writer.putInt(0);
			writer.putInt(0);
		}
		writer.putInt(this.numStats);
		if (this.strengthMod != 0) {
			writer.putInt(0x8AC3C0E6);
			writer.putInt(this.strengthMod);
		}
		if (this.dexterityMod != 0) {
			writer.putInt(0xE07B3336);
			writer.putInt(this.dexterityMod);
		}
		if (this.constitutionMod != 0) {
			writer.putInt(0xB15DC77E);
			writer.putInt(this.constitutionMod);
		}
		if (this.intelligenceMod != 0) {
			writer.putInt(0xFF665EC3);
			writer.putInt(this.intelligenceMod);
		}
		if (this.spiritMod != 0) {
			writer.putInt(0xACB82E33);
			writer.putInt(this.spiritMod);
		}

	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		runes = new int[23];
		runes = new int[23];
		strengthMod = 0;
		dexterityMod = 0;
		constitutionMod = 0;
		intelligenceMod = 0;
		spiritMod = 0;

		this.firstName = reader.getString();
		this.lastName = reader.getString();
		this.serverID = reader.getInt();

		reader.monitorInt(0, "CommitNewCharacter 01");

		this.hairStyle = reader.getInt();

		reader.monitorInt(0, "CommitNewCharacter 02");

		this.beardStyle = reader.getInt();
		this.skinColor = reader.getInt();
		this.hairColor = reader.getInt();
		this.beardColor = reader.getInt();
		this.kit = reader.getInt();
		this.clearRunes();
		int runeCount = 0;
		for (int i = 0; i < 23; i++) {

			reader.monitorInt(0, "CommitNewCharacter 03-" + i);

			this.runes[i] = reader.getInt();

			reader.monitorInt(0, "CommitNewCharacter 04-" + i);
			reader.monitorInt(0, "CommitNewCharacter 05-" + i);

			if (this.runes[i] != 0)
				runeCount++;
		}
		this.numRunes = runeCount;
		this.numStats = reader.getInt();
		int stattype;
		for (int i = 0; i < this.numStats; i++) {
			stattype = reader.getInt();
			if (stattype == 0x8AC3C0E6)
				this.strengthMod = reader.getInt();
			else if (stattype == 0xE07B3336)
				this.dexterityMod = reader.getInt();
			else if (stattype == 0xB15DC77E)
				this.constitutionMod = reader.getInt();
			else if (stattype == 0xFF665EC3)
				this.intelligenceMod = reader.getInt();
			else if (stattype == 0xACB82E33)
				this.spiritMod = reader.getInt();
		}
	}


	public void clearRunes() {
		for (int i = 0; i < 23; i++)
			this.runes[i] = 0;
	}

	public int getRace() {
		for (int i = 0; i < 23; i++)
			if (this.runes[i] > 1999 && this.runes[i] < 2030)
				return this.runes[i];
		return 0;
	}

	public int getBaseClass() {
		for (int i = 0; i < 23; i++)
			if (this.runes[i] > 2499 && this.runes[i] < 2504)
				return this.runes[i];
		return 0;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the serverID
	 */
	public int getServerID() {
		return serverID;
	}

	/**
	 * @param serverID
	 *            the serverID to set
	 */
	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

	/**
	 * @return the hairStyle
	 */
	public int getHairStyle() {
		return hairStyle;
	}

	/**
	 * @param hairStyle
	 *            the hairStyle to set
	 */
	public void setHairStyle(int hairStyle) {
		this.hairStyle = hairStyle;
	}

	/**
	 * @return the beardStyle
	 */
	public int getBeardStyle() {
		return beardStyle;
	}

	/**
	 * @param beardStyle
	 *            the beardStyle to set
	 */
	public void setBeardStyle(int beardStyle) {
		this.beardStyle = beardStyle;
	}

	/**
	 * @return the skinColor
	 */
	public int getSkinColor() {
		return skinColor;
	}

	/**
	 * @param skinColor
	 *            the skinColor to set
	 */
	public void setSkinColor(int skinColor) {
		this.skinColor = skinColor;
	}

	/**
	 * @return the hairColor
	 */
	public int getHairColor() {
		return hairColor;
	}

	/**
	 * @param hairColor
	 *            the hairColor to set
	 */
	public void setHairColor(int hairColor) {
		this.hairColor = hairColor;
	}

	/**
	 * @return the beardColor
	 */
	public int getBeardColor() {
		return beardColor;
	}

	/**
	 * @param beardColor
	 *            the beardColor to set
	 */
	public void setBeardColor(int beardColor) {
		this.beardColor = beardColor;
	}

	/**
	 * @return the kit
	 */
	public int getKit() {
		return kit;
	}

	/**
	 * @param kit
	 *            the kit to set
	 */
	public void setKit(int kit) {
		this.kit = kit;
	}

	/**
	 * @return the runeCount
	 */
	public int getNumRunes() {
		return numRunes;
	}

	/**
	 * @param numRunes
	 *            the runeCount to set
	 */
	public void setNumRunes(int numRunes) {
		this.numRunes = numRunes;
	}

	/**
	 * @return the runes
	 */
	public int[] getRunes() {
		return runes;
	}

	/**
	 * @param runes
	 *            the runes to set
	 */
	public void setRunes(int[] runes) {
		this.runes = runes;
	}

	/**
	 * @return the numStats
	 */
	public int getNumStats() {
		return numStats;
	}

	/**
	 * @param numStats
	 *            the numStats to set
	 */
	public void setNumStats(int numStats) {
		this.numStats = numStats;
	}

	/**
	 * @return the strengthMod
	 */
	public int getStrengthMod() {
		return strengthMod;
	}

	/**
	 * @param strengthMod
	 *            the strengthMod to set
	 */
	public void setStrengthMod(int strengthMod) {
		this.strengthMod = strengthMod;
	}

	/**
	 * @return the dexterityMod
	 */
	public int getDexterityMod() {
		return dexterityMod;
	}

	/**
	 * @param dexterityMod
	 *            the dexterityMod to set
	 */
	public void setDexterityMod(int dexterityMod) {
		this.dexterityMod = dexterityMod;
	}

	/**
	 * @return the constitutionMod
	 */
	public int getConstitutionMod() {
		return constitutionMod;
	}

	/**
	 * @param constitutionMod
	 *            the constitutionMod to set
	 */
	public void setConstitutionMod(int constitutionMod) {
		this.constitutionMod = constitutionMod;
	}

	/**
	 * @return the intelligenceMod
	 */
	public int getIntelligenceMod() {
		return intelligenceMod;
	}

	/**
	 * @param intelligenceMod
	 *            the intelligenceMod to set
	 */
	public void setIntelligenceMod(int intelligenceMod) {
		this.intelligenceMod = intelligenceMod;
	}

	/**
	 * @return the spiritMod
	 */
	public int getSpiritMod() {
		return spiritMod;
	}

	/**
	 * @param spiritMod
	 *            the spiritMod to set
	 */
	public void setSpiritMod(int spiritMod) {
		this.spiritMod = spiritMod;
	}

}
