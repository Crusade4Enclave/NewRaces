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
import engine.objects.AbstractGameObject;

/**
 * Accept trade request
 *
 * @author Eighty
 */
public class AcceptTradeRequestMsg extends ClientNetMsg {

    private int unknown01; //pad?
    private int playerType;
    private int playerID;
    private int targetType;
    private int targetID;
  

    /**
     * This is the general purpose constructor
     */
    public AcceptTradeRequestMsg(int unknown01, AbstractGameObject player, AbstractGameObject target) {
        super(Protocol.REQUESTTRADEOK);
        this.unknown01 = unknown01;
        this.playerType = player.getObjectType().ordinal();
        this.playerID =player.getObjectUUID();
        this.targetType = target.getObjectType().ordinal();
        this.targetID =target.getObjectUUID();
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public AcceptTradeRequestMsg(AbstractConnection origin, ByteBufferReader reader)
             {
        super(Protocol.REQUESTTRADEOK, origin, reader);
    }

    /**
     * Deserializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)
             {
        unknown01 = reader.getInt();
        playerType = reader.getInt();
        playerID = reader.getInt();
        targetType = reader.getInt();
        targetID = reader.getInt();
      
    }

    /**
     * Serializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer)
            throws SerializationException {
        writer.putInt(unknown01);
        writer.putInt(playerType);
        writer.putInt(playerID);
        writer.putInt(targetType);
        writer.putInt(targetID);
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

  
	

	public int getPlayerType() {
		return playerType;
	}

	public void setPlayerType(int playerType) {
		this.playerType = playerType;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public int getTargetType() {
		return targetType;
	}

	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	public int getTargetID() {
		return targetID;
	}

	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

}
