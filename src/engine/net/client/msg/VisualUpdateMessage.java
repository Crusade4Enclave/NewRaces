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
import engine.objects.AbstractGameObject;
import engine.objects.Building;

public class VisualUpdateMessage extends ClientNetMsg {
	private int effectType;
	private AbstractGameObject ago;
	private Building building;

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public VisualUpdateMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.VISUALUPDATE, origin, reader);
	}
	
	public VisualUpdateMessage() {
		super(Protocol.VISUALUPDATE);
	}

	public VisualUpdateMessage(AbstractGameObject ago, int visualID) {
		super(Protocol.VISUALUPDATE);

		if (ago == null)
			return;

		this.effectType = visualID;
		this.ago = ago;
		
	}
	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		
	}

    public void configure() {

        if (this.ago.getObjectType() == GameObjectType.Building)
            this.building = (Building)this.ago;
        else
            this.building = null;

    }
	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

        if (this.building == null) {
            writer.putInt(4);
            writer.putInt(0);
            writer.putInt(this.effectType);
            writer.putInt(ago.getObjectType().ordinal());
            writer.putInt(ago.getObjectUUID());
            writer.putInt(0);
            return;
        }

        writer.putShort((short)100);
        writer.putShort((short)120);
        writer.putInt(1);
        writer.putInt(this.building.getObjectType().ordinal());
        writer.putInt(this.building.getObjectUUID());
        writer.putInt(this.effectType);

	}
}
