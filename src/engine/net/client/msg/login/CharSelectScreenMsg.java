// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.login;


import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.Account;
import engine.objects.PlayerCharacter;
import engine.session.Session;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;


public class CharSelectScreenMsg extends ClientNetMsg {

	private int numChars;
	private int selectedIndex;
	private int static01;
	private byte static02;
	private ArrayList<PlayerCharacter> chars;
	private boolean fromCommit;
	private Account account;

	/**
	 * Special Constructor
	 *
	 * @param s
	 */
	public CharSelectScreenMsg(Session s) {
		this(s, false);
	}

	/**
	 * Special Constructor
	 *
	 * @param s
	 * @param fromCommit
	 */
	public CharSelectScreenMsg(Session s, boolean fromCommit) {
		super(Protocol.CHARSELECTSCREEN);
		this.fromCommit = fromCommit;
		this.chars = new ArrayList<>();
		// get this account
		this.account = s.getAccount();

		// Get all the character records for this account
		chars = new ArrayList<>(this.account.characterMap.values());

		if (chars == null) {
			this.chars = new ArrayList<>();
		}

		// idiot check the quantity of the ArrayList/numChars
		this.numChars = chars.size();
		if (this.numChars > 7) {
			Logger.error("Account '" + this.account.getUname() + "' has more than 7 characters.");

			this.numChars = 7;
		}

		// Get the last character used (As a composite ID).
		int lastChar = s.getAccount().getLastCharIDUsed();

		// Look it up for the index #
		this.selectedIndex = 0;

		for (PlayerCharacter pc : chars)
			if (pc.getObjectUUID() == lastChar)
				break;
			else
				selectedIndex++;

		// idiot check the index #
		if (this.selectedIndex < 0) {
			this.selectedIndex = 0;
		}
		if (this.selectedIndex > 6) {
			this.selectedIndex = 6;
		}

		this.static01 = 7;
		this.static02 = (byte) 1;
	}

	/**
	 * This is the general purpose constructor.
	 */
	public CharSelectScreenMsg() {
		super(Protocol.CHARSELECTSCREEN);
		this.chars = new ArrayList<>();
		this.selectedIndex = 0;
		this.static01 = 7;
		this.static02 = (byte) 1;
		this.fromCommit = false;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public CharSelectScreenMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CHARSELECTSCREEN, origin, reader);

		this.chars = new ArrayList<>();
	}

	/**
	 * @see AbstractNetMsg#getPowerOfTwoBufferSize()
	 */
	@Override
	protected int getPowerOfTwoBufferSize() {
		//Larger size for historically larger opcodes
		return (17); // 2^17 == 131,072
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {

		if (this.account == null)
			Logger.error( "failed to find account for message");

		// Double check char belongs to this account
		for (int i = 0; i < this.numChars; ++i) {
			if (this.chars.get(i) == null)
				Logger.error("failed to find character");
			if (this.chars.get(i).getAccount() == null)
				Logger.error("failed to find account for character "
						+ this.chars.get(i).getObjectUUID());
			if (this.chars.get(i).getAccount().getObjectUUID() != this.account.getObjectUUID()) {
				this.chars.remove(i);
				this.numChars--;

				Logger.error( "This character doesn't belong to this account.");

			}
		}

		writer.putInt(this.numChars); // 4bytes
		writer.putInt(this.selectedIndex); // 4bytes
		writer.putInt(this.static01); // 4bytes
		writer.put(this.static02); // 1 byte

		for (int i = 0; i < this.numChars; ++i) {
			try {
				if (!fromCommit)
					PlayerCharacter.serializeForClientMsgLogin(this.chars.get(i),writer);
				else
					PlayerCharacter.serializeForClientMsgCommit(this.chars.get(i),writer);
			} catch (SerializationException e) {
				Logger.error( "failed to serialize character " + this.chars.get(i).getObjectUUID());
				// Handled already.
			}
		}
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.numChars = reader.getInt();
		this.selectedIndex = reader.getInt();

		this.static01 = reader.monitorInt(0, "CharSelectScreenMsg-01");
		this.static02 = reader.monitorByte((byte) 0, "CharSelectScreenMsg-02");

		// TODO is this correct?!?!?
	}

	/**
	 * @return the numChars
	 */
	public int getNumChars() {
		return numChars;
	}

	/**
	 * @param numChars
	 *            the numChars to set
	 */
	public void setNumChars(int numChars) {
		this.numChars = numChars;
	}

	/**
	 * @return the selectedIndex
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * @param selectedIndex
	 *            the selectedIndex to set
	 */
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	/**
	 * @return the static01
	 */
	public int getStatic01() {
		return static01;
	}

	/**
	 * @param static01
	 *            the static01 to set
	 */
	public void setStatic01(int static01) {
		this.static01 = static01;
	}

	/**
	 * @return the static02
	 */
	public byte getStatic02() {
		return static02;
	}

	/**
	 * @param static02
	 *            the static02 to set
	 */
	public void setStatic02(byte static02) {
		this.static02 = static02;
	}

	/**
	 * @return the chars
	 */
	public ArrayList<PlayerCharacter> getChars() {
		return chars;
	}

	/**
	 * @param chars
	 *            the chars to set
	 */
	public void setChars(ArrayList<PlayerCharacter> chars) {
		this.chars = chars;
	}

	/**
	 * @return the fromCommit
	 */
	public boolean isFromCommit() {
		return fromCommit;
	}

	/**
	 * @param fromCommit
	 *            the fromCommit to set
	 */
	public void setFromCommit(boolean fromCommit) {
		this.fromCommit = fromCommit;
	}

	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * @param account
	 *            the account to set
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

}
