// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.Enum.GameObjectType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;


public class HirelingServiceMsg extends ClientNetMsg {


	public int npcID;
	public int buildingID;
	public int messageType;
	public int repairCost;
	
	public static final int SETREPAIRCOST = 2;

    /**
     * This is the general purpose constructor.
     *
     * @param channel
     */
    public HirelingServiceMsg(int channel) {
        super(Protocol.HIRELINGSERVICE);
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public HirelingServiceMsg(AbstractConnection origin, ByteBufferReader reader)  {
        super(Protocol.HIRELINGSERVICE, origin, reader);
    }

    /**
     * Serializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
    	writer.putInt(this.messageType);
    	switch (this.messageType){
    	case SETREPAIRCOST:
    		this.writeSetRepairCost(writer);
    		break;
    	}
    }

    /**
     * Deserializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
    	this.messageType = reader.getInt();
    	
    	switch (this.messageType){
    	case SETREPAIRCOST:
    		this.readSetRepairCost(reader);
    		break;
    		
    	}
    }
    
    private void readSetRepairCost(ByteBufferReader reader){
    	reader.getInt(); //building type;
    	this.buildingID = reader.getInt();
    	reader.getInt();
    	this.npcID = reader.getInt();
    	reader.getInt();
    	reader.getInt(); //3
    	this.repairCost = reader.getInt();
    	reader.getInt();
    	reader.getInt();
    	
    }
    
    private void writeSetRepairCost(ByteBufferWriter writer){
    	writer.putInt(GameObjectType.Building.ordinal());
    	writer.putInt(this.buildingID);
    	writer.putInt(GameObjectType.NPC.ordinal());
    	writer.putInt(this.npcID);
    	writer.putInt(0);
    	writer.putInt(3);
    	writer.putInt(this.repairCost);
    	writer.putInt(0);
    	writer.putInt(0);
    }
    
    private void writeTest(ByteBufferWriter writer){
    	writer.putInt(GameObjectType.Building.ordinal());
    	writer.putInt(this.buildingID);
    	writer.putInt(GameObjectType.NPC.ordinal());
    	writer.putInt(this.npcID);
    	writer.putInt(3);
    	writer.putInt(1);
    	writer.putInt(1);
    	writer.putInt(GameObjectType.Building.ordinal());
    	writer.putInt(this.buildingID);
    	writer.putInt(GameObjectType.NPC.ordinal());
    	writer.putInt(this.npcID);
    	writer.putInt(0);
    	writer.putInt(0);
    	writer.putInt(3);
    	writer.putInt(1);
    	
    	writer.putInt(0);
    
    }
}
