// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.guild;

import engine.Enum.GameObjectType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;

public class AcceptInviteToGuildMsg extends ClientNetMsg {

    private int guildUUID;
    private int unknown01;
    private int unknown02;

    /**
     * This is the general purpose constructor.
     */
    public AcceptInviteToGuildMsg() {
        super(Protocol.JOINGUILD);
    }

    public AcceptInviteToGuildMsg(int guildUUID, int unknown01, int unknown02) {
        super(Protocol.JOINGUILD);
        this.guildUUID = guildUUID;
        this.unknown01 = unknown01;
        this.unknown02 = unknown02;
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public AcceptInviteToGuildMsg(AbstractConnection origin, ByteBufferReader reader)
             {
        super(Protocol.JOINGUILD, origin, reader);
    }

    /**
     * Serializes the subclass specific items to the supplied ByteBufferWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
        writer.putInt(GameObjectType.Guild.ordinal());
        writer.putInt(this.guildUUID);
        writer.putInt(this.unknown01);
        writer.putInt(this.unknown02);
    }

    /**
     * Deserializes the subclass specific items from the supplied
     * ByteBufferReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
        reader.getInt(); // Object Type padding
        this.guildUUID = reader.getInt();
        this.unknown01 = reader.getInt();
        this.unknown02 = reader.getInt();
    }

    /**
     * @return the guildUUID
     */
    public int getGuildUUID() {
        return guildUUID;
    }

    /**
     * @return the unknown01
     */
    public int getUnknown01() {
        return unknown01;
    }

    /**
     * @param unknown01
     * the unknown01 to set
     */
    public void setUnknown01(int unknown01) {
        this.unknown01 = unknown01;
    }

    /**
     * @return the unknown02
     */
    public int getUnknown02() {
        return unknown02;
    }

    /**
     * @param unknown02
     * the unknown02 to set
     */
    public void setUnknown02(int unknown02) {
        this.unknown02 = unknown02;
    }

}
