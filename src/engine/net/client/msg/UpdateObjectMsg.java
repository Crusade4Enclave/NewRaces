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
import engine.objects.*;

public class UpdateObjectMsg extends ClientNetMsg {
	private int msgType;
	private AbstractWorldObject ago;
	

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UpdateObjectMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.UPDATEOBJECT, origin, reader);
	}
	
	public UpdateObjectMsg() {
		super(Protocol.UPDATEOBJECT);
	}

	public UpdateObjectMsg(AbstractWorldObject ago,int type) {
		super(Protocol.UPDATEOBJECT);
		if (ago == null)
			return;
		this.msgType = type;
		this.ago = ago;

		
	}
	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {
		writer.putInt(this.msgType);
		switch (this.msgType){
		case 2:
			updateName(writer);
			break;
		case 3:
			updateRank(writer);
			break;
		case 4:
			derank(writer);
			break;
		case 5:
			updateGuild(writer);
			break;
			
		default:
			break;
				
		}
	}
	
	private void updateName(ByteBufferWriter writer){
            
                writer.putInt(ago.getObjectType().ordinal());
                writer.putInt(ago.getObjectUUID());
		writer.putString(ago.getName());
		writer.putInt(0);
	}
	
	private void updateRank(ByteBufferWriter writer){
                writer.putInt(ago.getObjectType().ordinal());
                writer.putInt(ago.getObjectUUID());
		writer.putFloat(ago.getHealthMax());
		writer.putFloat(ago.getCurrentHitpoints());
		writer.put((byte)1);
		writer.putInt(0);
	
	}
	
	private void updateGuild(ByteBufferWriter writer){
            
                writer.putInt(ago.getObjectType().ordinal());
                writer.putInt(ago.getObjectUUID());

		switch (ago.getObjectType()){
		case Building:
			Guild guild = ((Building)ago).getGuild();
			GuildTag._serializeForDisplay(guild.getGuildTag(),writer);
			GuildTag._serializeForDisplay(guild.getNation().getGuildTag(),writer);
			writer.putInt(0);
			
			break;
		default:
			break;
		}
		
	
	}
	
	private void derank(ByteBufferWriter writer){
		writer.putInt(0);
		writer.putInt(0);
		writer.put((byte)0);
                writer.putInt(ago.getObjectType().ordinal());
                writer.putInt(ago.getObjectUUID());
		writer.putInt(0);
	}


	
	public AbstractGameObject getAgo() {
		return ago;
	}


}
