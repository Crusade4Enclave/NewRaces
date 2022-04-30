// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.AbstractWorldObject;
import engine.objects.CharacterItemManager;
import engine.objects.Item;
import engine.objects.PlayerCharacter;

/**
 * Update gold in inventory and/or bank
 *
 * @author Eighty
 */

public class UpdateGoldMsg extends ClientNetMsg {

	private AbstractWorldObject looter;
    CharacterItemManager itemManager;
	private Item goldInventory;
	private Item goldBank;
	private int tradeGold = 0;


    /**
     * This is the general purpose constructor
     */
    public UpdateGoldMsg(AbstractWorldObject player) {
        super(Protocol.UPDATEGOLDVALUE);
        this.looter = player;
    }


	/**
	 * This is the general purpose constructor
	 */
	public UpdateGoldMsg() {
		super(Protocol.UPDATEGOLDVALUE);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UpdateGoldMsg(AbstractConnection origin,
			ByteBufferReader reader)  {
		super(Protocol.UPDATEGOLDVALUE, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {

	}

    // Pre-cache and set values so they are available when we
    // serialize the data.

    public void configure() {

    	if (this.looter != null && this.looter.getObjectType() == GameObjectType.PlayerCharacter){
    		itemManager = ((PlayerCharacter)looter).getCharItemManager();
            goldInventory = itemManager.getGoldInventory();
            this.tradeGold = itemManager.getGoldTrading();
            goldBank = itemManager.getGoldBank();
    	}else{
    		itemManager = null;
    		goldInventory = null;
    		goldBank = null;
    	}
    }

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {

    	if (looter == null){
            writer.putInt(0);
            writer.putInt(0);
    	}else{
            writer.putInt(looter.getObjectType().ordinal());
            writer.putInt(looter.getObjectUUID());
    	}

        if (goldInventory != null && goldInventory.getNumOfItems() - this.tradeGold > 0) {
            writer.put((byte) 1);
            Item.serializeForClientMsgWithoutSlot(goldInventory,writer);
        } else
            writer.put((byte) 0);

        if (goldBank != null && goldBank.getNumOfItems() != 0) {
            writer.put((byte) 1);
            Item.serializeForClientMsgWithoutSlot(goldBank,writer);
        } else
            writer.put((byte) 0);
    }

}
