// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;


public class ChangeGuildLeaderMsg extends ClientNetMsg {

	private int targetID;
    
 
    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public ChangeGuildLeaderMsg(AbstractConnection origin, ByteBufferReader reader)  {
        super(Protocol.CHANGEGUILDLEADER, origin, reader);
    }

    /**
     * Deserializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
       reader.getInt();
       reader.getInt();
       reader.getInt();
       reader.getInt();
       targetID = reader.getInt();
       reader.getInt();
       reader.get();
    }

    /**
     * Serializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) throws SerializationException {
    	writer.putInt(0);
    	writer.putInt(0);
    	writer.putInt(0);
    	writer.putInt(GameObjectType.PlayerCharacter.ordinal());
    	writer.putInt(targetID);
    	writer.put((byte)100);
    	writer.putInt(0);
    	
    	
    }

	public int getTargetID() {
		return targetID;
	}

	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}
}
