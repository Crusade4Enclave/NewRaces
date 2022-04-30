// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.net.client.msg.login;

import engine.Enum.GameObjectType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;

public class DeleteCharacterMsg extends ClientNetMsg {

    private int characterUUID;
    private String firstName;
    private String serverName;

    /**
     * This is the general purpose constructor.
     */
    public DeleteCharacterMsg() {
        super(Protocol.REMOVECHAR);
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public DeleteCharacterMsg(AbstractConnection origin, ByteBufferReader reader)
             {
        super(Protocol.REMOVECHAR, origin, reader);
    }

    /**
     * Serializes the subclass specific items to the supplied ByteBufferWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
        writer.putInt(GameObjectType.PlayerCharacter.ordinal());
        writer.putInt(this.characterUUID);
        writer.putString(this.firstName);
        writer.putString(this.serverName);
    }

    /**
     * Deserializes the subclass specific items from the supplied
     * ByteBufferReader.
     */
    
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
        reader.getInt(); // Object Type Padding
        this.characterUUID = reader.getInt();
        this.firstName = reader.getString();
        this.serverName = reader.getString();
    }

    /**
     * @return the characterUUID
     */
    public int getCharacterUUID() {
        return characterUUID;
    }

}
