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

public class ArcMineChangeProductionMsg extends ClientNetMsg {

    private int mineID;
    private int resourceHash;

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public ArcMineChangeProductionMsg(AbstractConnection origin, ByteBufferReader reader)  {
        super(Protocol.ARCMINECHANGEPRODUCTION, origin, reader);
    }

    /**
     * Deserializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
        this.mineID = reader.getInt();
        this.resourceHash = reader.getInt();
    }

    /**
     * Serializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) throws SerializationException {
        writer.putInt(this.mineID);
        writer.putInt(this.resourceHash);
    }

    public int getMineID() {
        return this.mineID;
    }

    public int getResourceHash() {
        return this.resourceHash;
    }

}
