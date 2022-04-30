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
import engine.objects.Item;
import engine.objects.PlayerCharacter;

import java.util.ArrayList;

/**
 * Update trade window message.  Send item info to other player.
 * @author Eighty
 */
public class UpdateTradeWindowMsg extends ClientNetMsg {

	PlayerCharacter pc1;
	PlayerCharacter pc2;

	/**
	 * This is the general purpose constructor.
	 */
	public UpdateTradeWindowMsg(PlayerCharacter pc1, PlayerCharacter pc2) {
		super(Protocol.UPDATETRADEWINDOW);
		this.pc1 = pc1;
		this.pc2 = pc2;
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
            
                writer.putInt(pc1.getObjectType().ordinal());
                writer.putInt(pc1.getObjectUUID());
                
                writer.putInt(pc2.getObjectType().ordinal());
                writer.putInt(pc2.getObjectUUID());
                
         

		ArrayList<Item> trading1 = new ArrayList<>();
		
		for (int itemID : pc1.getCharItemManager().getTrading()){
			Item item = Item.getFromCache(itemID);
			if (item == null)
				continue;
			trading1.add(item);
		}
		
		ArrayList<Item> trading2 = new ArrayList<>();
		for (int itemID : pc2.getCharItemManager().getTrading()){
			Item item = Item.getFromCache(itemID);
			if (item == null)
				continue;
			trading2.add(item);
		}
		Item.putTradingList(pc1,writer, trading1, false, pc1.getObjectUUID(),false,null);
		Item.putTradingList(pc2,writer, trading2, false, pc2.getObjectUUID(),false,null);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public UpdateTradeWindowMsg(AbstractConnection origin,
			ByteBufferReader reader)  {
		super(Protocol.UPDATETRADEWINDOW, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		return 16;
	}
}
