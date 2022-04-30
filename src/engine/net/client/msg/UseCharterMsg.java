// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.ItemType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Item;
import engine.objects.PlayerCharacter;


public class UseCharterMsg extends ClientNetMsg {

	private int unknown01;
	private int unknown02;
	private int unknown03;
	private String type;
	private int unknown04;
	private int unknown05;
	private int unknown06;
	private boolean close = false;
	private PlayerCharacter player;
    private int charterUUID;

	/**
	 * This is the general purpose constructor.
	 */
	public UseCharterMsg() {
		super(Protocol.ACTIVATECHARTER);
	}
	
	public UseCharterMsg(PlayerCharacter player, boolean close) {
		super(Protocol.ACTIVATECHARTER);
		this.close = close;
		this.player = player;
		
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UseCharterMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ACTIVATECHARTER, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */

    public void configure() {

        if (close) {
            for (Item i : player.getInventory()) {
                if (i.getItemBase().getType().equals(ItemType.GUILDCHARTER)) {
                    charterUUID = i.getObjectUUID();
                    break;
                }
            }
        }
    }

	@Override
	protected void _serialize(ByteBufferWriter writer) {       
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		writer.putInt(this.unknown03);
		writer.putString(this.type);
		writer.putInt(this.unknown04);
		writer.putInt(this.unknown05);
		writer.putInt(this.unknown06);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
		this.type = reader.getString();
		this.unknown04 = reader.getInt();
		this.unknown05 = reader.getInt();
		this.unknown06 = reader.getInt();
	}

	/**
	 * @return the unknown01
	 */
	public void setUnknown02(int unknown02) {
		this.unknown02 = unknown02;
	}
}
