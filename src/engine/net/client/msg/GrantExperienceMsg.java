// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.PlayerCharacter;

public class GrantExperienceMsg extends ClientNetMsg {

    private int xpGranted;
    private int objectType;
    private int objectID;

    /**
     * This is the general purpose constructor.
     */
    public GrantExperienceMsg(PlayerCharacter pc, int xpGranted) {
        super(Protocol.EXPERIENCE);
        this.xpGranted = xpGranted;
        this.objectType = pc.getObjectType().ordinal();
        this.objectID = pc.getObjectUUID();
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public GrantExperienceMsg(AbstractConnection origin, ByteBufferReader reader)  {
        super(Protocol.EXPERIENCE, origin, reader);
    }

    /**
     * Copy constructor
     */
    public GrantExperienceMsg(GrantExperienceMsg msg) {
        super(Protocol.EXPERIENCE);
        this.xpGranted = msg.xpGranted;
        this.objectType = msg.objectType;
        this.objectID = msg.objectID;
    }

    /**
     * Deserializes the subclass specific items from the supplied
     * ByteBufferReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
        reader.get();
        this.xpGranted = reader.getInt();
        this.objectType = reader.getInt();
        this.objectID = reader.getInt();
    }

    /**
     * Serializes the subclass specific items to the supplied ByteBufferWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
        writer.putInt(this.xpGranted);
        writer.putInt(this.objectType);
        writer.putInt(this.objectID);
    }

    public int getXPGranted() {
        return this.xpGranted;
    }

    public int getObjectType() {
        return this.objectType;
    }

    public int getObjectID() {
        return this.objectID;
    }

    public void setXPGranted(int value) {
        this.xpGranted = value;
    }

    public void setObjectType(int value) {
        this.objectType = value;
    }

    public void setObjectID(int value) {
        this.objectID = value;
    }
}
