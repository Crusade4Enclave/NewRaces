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
import engine.objects.*;

import java.util.ArrayList;

/**
 * Vault inventory contents
 * @author Eighty
 */
public class ShowVaultInventoryMsg extends ClientNetMsg {

	PlayerCharacter pc;
	int accountType;
	int accountID;

	int npcType;
	int npcID;


	/**
	 * This is the general purpose constructor.
	 */
	public ShowVaultInventoryMsg(PlayerCharacter pc, Account account, NPC npc) {
		super(Protocol.SHOWVAULTINVENTORY);
		this.pc = pc;
		this.accountType = account.getObjectType().ordinal();
		this.accountID = account.getObjectUUID();
		this.npcType = npc.getObjectType().ordinal();
		this.npcID = npc.getObjectUUID();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {

		writer.putInt(accountType);
		writer.putInt(accountID);
		writer.putInt(npcType);
		writer.putInt(npcID);
		writer.putString(pc.getFirstName());

		ArrayList<Item> vault = pc.getAccount().getVault();

		Item.putList(writer, vault, false, pc.getObjectUUID());
		writer.putInt(AbstractCharacter.getVaultCapacity());
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public ShowVaultInventoryMsg(AbstractConnection origin,
			ByteBufferReader reader)  {
		super(Protocol.SHOWVAULTINVENTORY, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		return 17;
	}
}
