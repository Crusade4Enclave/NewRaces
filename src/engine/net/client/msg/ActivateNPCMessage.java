// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Item;

import java.util.ArrayList;

public class ActivateNPCMessage extends ClientNetMsg {

    private ArrayList<Item> ItemList;
    private int unknown01;
    private int unknown02;
    private int buildingUUID;
    private int unknown03;
    private int unknown04;
    private int unknown05;
    private int size;

    /**
     * This is the general purpose constructor.
     */
    public ActivateNPCMessage() {
        this(new ArrayList<>());
    }

    /**
     * This is the general purpose constructor.
     *
     */
    public ActivateNPCMessage(ArrayList<Item> items) {
        super(Protocol.ACTIVATENPC);
        this.unknown01 = 0;
        this.size = 0;
        this.ItemList = items;
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public ActivateNPCMessage(AbstractConnection origin, ByteBufferReader reader)  {
        super(Protocol.ACTIVATENPC, origin, reader);
    }

    /**
     * @see AbstractNetMsg#getPowerOfTwoBufferSize()
     */
    @Override
    protected int getPowerOfTwoBufferSize() {
        //Larger size for historically larger opcodes
        return (16); // 2^16 == 64k
    }

    /**
     * Serializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
        for (int i = 0; i < 6; i++) {
            writer.putInt(0);
        }
        writer.putInt(this.size);
        for (Item item : this.ItemList) {
        	writer.putInt(item.getObjectType().ordinal());
        	writer.putInt(item.getObjectUUID());
           
        }
    }

    /**
     * Deserializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
        unknown01 = reader.getInt();
        unknown02 = reader.getInt();
        reader.getInt(); // Object Type Padding
        buildingUUID = reader.getInt();
        unknown03 = reader.getInt();
        unknown04 = reader.getInt();
        unknown05 = reader.getInt();

    }

    // TODO fix ArrayList Accessability.

    public int size() {
        return this.ItemList.size();
    }

    public int getUnknown01() {
        return unknown01;
    }

    public int getUnknown02() {
        return unknown02;
    }

    public int getUnknown03() {
        return unknown03;
    }

    public int getUnknown04() {
        return unknown04;
    }

    public int getUnknown05() {
        return unknown05;
    }

    public int buildingUUID() {
        return buildingUUID;
    }

    public void setUnknown01(int unknown01) {
        this.unknown01 = unknown01;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setUnknown02(int unknown02) {
        this.unknown02 = unknown02;
    }

    public void setUnknown03(int unknown03) {
        this.unknown03 = unknown03;
    }

    public void setUnknown04(int unknown04) {
        this.unknown04 = unknown04;
    }

    public void setUnknown05(int unknown05) {
        this.unknown05 = unknown05;
    }

    public void setItemList(ArrayList<Item> value) {
        this.ItemList = value;
    }

}
