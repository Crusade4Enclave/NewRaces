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
import engine.objects.AbstractCharacter;
import engine.objects.Item;
import engine.objects.PlayerCharacter;

import java.util.ArrayList;


/**
 * Bank inventory contents
 *
 * @author Burfo
 */
public class ShowBankInventoryMsg extends ClientNetMsg {

	PlayerCharacter pc;
	long unknown01;

	/**
	 * This is the general purpose constructor.
	 */
	public ShowBankInventoryMsg(PlayerCharacter pc, long unknown01) {
		super(Protocol.BANKINVENTORY);
		this.pc = pc;
		this.unknown01 = unknown01;
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		ArrayList<Item> bank = pc.getCharItemManager().getBank();

		writer.put((byte) 1); // static value
		Item.putList(writer, bank, false, pc.getObjectUUID());
		writer.putInt(AbstractCharacter.getBankCapacity());

		// TODO: Gold is sent last and has a slightly different structure.
		// Everything is static except the 3 labeled lines
		//TODO: f/Eighty: I don't think gold is sent separately.
		//		will need to check once transfer to bank is working.
		/*
		00:00:00:00:
		07:00:00:00:
		00:00:20:0A:5C:46:29:14: comp id
		00:00:00:00:00:00:00:00:00:00:00:00:
		00:00:80:3F:00:00:80:3F:00:00:80:3F:00:00:80:3F:
		00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:
		FF:FF:FF:FF:FF:FF:FF:FF:
		00:00:00:00:
		01:
		00:00:00:00:
		01:
		00:00:00:00:00:00:00:00:
		01:
		00:00:00:00:00:00:00:00:
		9C:1B:04:00 quantity of gold?
		00:00:00:00:
		00:00:00:00:
		00:00:00:00:
		04:00:00:00:
		00:00:00:00:
		00:00:00:00:
		01:00:00:00:
		00:00:
		00:
		58:02:00:00: unknown?
		*/

                writer.putInt(pc.getObjectType().ordinal());
                writer.putInt(pc.getObjectUUID());
		writer.putLong(unknown01);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ShowBankInventoryMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.BANKINVENTORY, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return 17; // 2^15 == 32,768
	}
}
