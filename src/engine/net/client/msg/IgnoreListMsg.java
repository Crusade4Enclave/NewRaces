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

import java.util.ArrayList;

public class IgnoreListMsg extends ClientNetMsg {

    private final ArrayList<String> names = new ArrayList<>();
    private int playerType;
    private int playerID;
    private String playerName;

    /**
     * This is the general purpose constructor.
     */
    public IgnoreListMsg() {
        super(Protocol.ARCIGNORELISTUPDATE);
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public IgnoreListMsg(AbstractConnection origin, ByteBufferReader reader)  {
        super(Protocol.ARCIGNORELISTUPDATE, origin, reader);
    }

    /**
     * Serializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
        writer.putInt(this.names.size());
        for (String name : this.names) {
            writer.putString(name);
        }
        writer.putInt(this.playerType);
        writer.putInt(this.playerID);
        writer.putString(this.playerName);
        writer.put((byte) 0);
    }

    /**
     * Deserializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
        int size = reader.getInt();
        for (int i = 0; i < size; i++) {
            this.names.add(reader.getString());
        }
        this.playerType = reader.getInt();
        this.playerID = reader.getInt();
        this.playerName = reader.getString();
        reader.get();
    }

    public ArrayList<String> getNames() {
        return this.names;
    }

    public void addName(String value) {
        this.names.add(value);
    }

    public void removeName(String value) {
        this.names.remove(value);
    }

    public int getPlayerType() {
        return this.playerType;
    }

    public void setPlayerType(int value) {
        this.playerType = value;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    public void setPlayerID(int value) {
        this.playerID = value;
    }

    public String getName() {
        return this.playerName;
    }

    public void setName(String value) {
        this.playerName = value;
    }

}
