// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.ItemType;
import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.objects.*;

import java.util.ArrayList;

public class BuyFromNPCWindowMsg extends ClientNetMsg {

	private int unknown01;
	private int npcType;
	private int npcID;
	private float unknown02;
	private byte unknown03;
	private int unknown04;

	/**
	 * This is the general purpose constructor
	 */
	public BuyFromNPCWindowMsg(int unknown01, int npcType, int npcID,
			float unknown02, byte unknown03, int unknown04) {
		super(Protocol.SHOPLIST);
		this.unknown01 = unknown01;
		this.npcType = npcType;
		this.npcID = npcID;
		this.unknown02 = unknown02;
		this.unknown03 = unknown03;
		this.unknown04 = unknown04;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public BuyFromNPCWindowMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.SHOPLIST, origin, reader);
	}

	/**
	 * @see AbstractNetMsg#getPowerOfTwoBufferSize()
	 */
	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return (16);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		unknown01 = reader.getInt();
		npcType = reader.getInt();
		npcID = reader.getInt();
		unknown02 = reader.getFloat();
		unknown03 = reader.get();
		unknown04 = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		
		ClientConnection clientConnection = (ClientConnection) this.getOrigin();
		PlayerCharacter player = null;
		
		if (clientConnection != null)
			player = clientConnection.getPlayerCharacter();
		
		float sellPercent = 1;

		NPC npc = NPC.getFromCache(npcID);
		CharacterItemManager man = null;
		ArrayList<Item> inventory = null;
		ArrayList<MobEquipment> sellInventory = null;

		if (npc != null) {
			man = npc.getCharItemManager();
			Contract contract = npc.getContract();
			if (player != null){
				float barget = player.getBargain();
				float profit = npc.getSellPercent(player) - barget;
				
				if (profit < 1)
					profit = 1;
				
				sellPercent = 1 * profit;
			}
				
			else sellPercent = 1 * npc.getSellPercent();

			if (contract != null)
				sellInventory = contract.getSellInventory();
		}

		if (man != null)
			inventory = man.getInventory();

		writer.putInt(unknown01);
		writer.putInt(npcType);
		writer.putInt(npcID);
		
		writer.putFloat(sellPercent); //npc sell markup

		int size = 0;

		if (inventory != null)
			size += inventory.size();

		if (sellInventory != null)
			size += sellInventory.size();

		if (size == 0) {
			writer.put((byte) 0);
			writer.putInt(0);
			return;
		}

		writer.put((byte) 1);

		int ownerID = npc.getObjectUUID();
		int	indexPosition = writer.position();
		writer.putInt(0); //placeholder for item cnt
		int total = 0;

		//add generic sell inventory from contract
		if (sellInventory != null) {

			for (MobEquipment mobEquipment : sellInventory) {
				try {
					MobEquipment.serializeForVendor(mobEquipment,writer, sellPercent);
				} catch (SerializationException se) {
					continue;
				}
				++total;
			}
		}

		//add npc inventory
		if (inventory != null) {
			for (Item item : inventory) {
				if (item.getOwnerID() != ownerID)
					continue;
				if (item.getItemBase().getType().equals(ItemType.GOLD)) {
					if (item.getNumOfItems() == 0)
						continue;
				}
				Item.serializeForClientMsgForVendorWithoutSlot(item,writer, sellPercent);
				++total;
			}
		}
		writer.putIntAt(total, indexPosition);
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
	 * @return the unknown02
	 */
	public float getUnknown02() {
		return unknown02;
	}

	/**
	 * @param unknown02
	 *            the unknown02 to set
	 */
	public void setUnknown02(float unknown02) {
		this.unknown02 = unknown02;
	}

	/**
	 * @return the unknown03
	 */
	public byte getUnknown03() {
		return unknown03;
	}

	/**
	 * @param unknown03
	 *            the unknown03 to set
	 */
	public void setUnknown03(byte unknown03) {
		this.unknown03 = unknown03;
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
	 * @return the npcType
	 */
	public int getNpcType() {
		return npcType;
	}

	/**
	 * @param npcType
	 *            the npcType to set
	 */
	public void setNpcType(int npcType) {
		this.npcType = npcType;
	}

	/**
	 * @return the npcID
	 */
	public int getNpcID() {
		return npcID;
	}

	/**
	 * @param npcID
	 *            the npcID to set
	 */
	public void setNpcID(int npcID) {
		this.npcID = npcID;
	}

}
