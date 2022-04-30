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


public class AddGoldToTradeWindowMsg extends ClientNetMsg {

    private int unknown01;
    private long playerCompID;
    private int amount;

    /**
     * This is the general purpose constructor
     */
    public AddGoldToTradeWindowMsg(int unknown01, long playerCompID, int amount) {
        super(Protocol.TRADEADDGOLD);
        this.unknown01 = unknown01;
        this.playerCompID = playerCompID;
        this.amount = amount;
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public AddGoldToTradeWindowMsg(AbstractConnection origin, ByteBufferReader reader)
             {
        super(Protocol.TRADEADDGOLD, origin, reader);
    }

    /**
     * Deserializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)
             {
        unknown01 = reader.getInt();
        playerCompID = reader.getLong();
        amount = reader.getInt();
    }

    /**
     * Serializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer)
            throws SerializationException {
        writer.putInt(unknown01);
        writer.putLong(playerCompID);
        writer.putInt(amount);
    }

    /**
     * @return the unknown01
     */
    public int getUnknown01() {
        return unknown01;
    }

    /**
     * @param unknown01 the unknown01 to set
     */
    public void setUnknown01(int unknown01) {
        this.unknown01 = unknown01;
    }

    /**
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

}
