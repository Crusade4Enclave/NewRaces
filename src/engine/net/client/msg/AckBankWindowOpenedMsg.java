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
import engine.objects.PlayerCharacter;

/**
 * Bank window opened
 *
 * @author Burfo
 */
public class AckBankWindowOpenedMsg extends ClientNetMsg {

	private int playerType;
	private int playerID;
	private long unknown01; // possibly NPC ID?
    private long unknown02;

    /**
     * This is the general purpose constructor.
     */
    public AckBankWindowOpenedMsg(PlayerCharacter pc, long unknown01, long unknown02) {
        super(Protocol.COSTTOOPENBANK);
        this.playerType = pc.getObjectType().ordinal();
        this.playerID = pc.getObjectUUID();
        this.unknown01 = unknown01;
        this.unknown02 = unknown02;
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public AckBankWindowOpenedMsg(AbstractConnection origin, ByteBufferReader reader)  {
        super(Protocol.COSTTOOPENBANK, origin, reader);
    }

    /**
     * Deserializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
    	playerType = reader.getInt();
    	playerID = reader.getInt();
        unknown01 = reader.getLong();
        unknown02 = reader.getLong();
    }

    /**
     * Serializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) throws SerializationException {
    	writer.putInt(playerType);
    	writer.putInt(playerID);
        writer.putLong(unknown01);
        writer.putLong(unknown02);
    }
    public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public int getPlayerType() {
		return playerType;
	}

	public void setPlayerType(int playerType) {
		this.playerType = playerType;
	}

}
