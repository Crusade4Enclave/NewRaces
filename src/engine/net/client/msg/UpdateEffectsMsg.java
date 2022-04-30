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
import engine.objects.AbstractWorldObject;
import engine.objects.Effect;

import java.util.ArrayList;

public class UpdateEffectsMsg extends ClientNetMsg {

	AbstractWorldObject awo;

	/**
	 * This is the general purpose constructor.
	 */
	public UpdateEffectsMsg() {
		super(Protocol.UPDATEEFFECTS);
		this.awo = null;
	}

	/**
	 * This is the general purpose constructor.
	 */
	public UpdateEffectsMsg(AbstractWorldObject awo) {
		super(Protocol.UPDATEEFFECTS);
		this.awo = awo;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UpdateEffectsMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.UPDATEEFFECTS, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		if (awo == null) {
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
		} else {
			writer.putInt(awo.getObjectType().ordinal());
			writer.putInt(awo.getObjectUUID());

			ArrayList<Effect> effects = new ArrayList<>(awo.getEffects().values());
			writer.putInt(effects.size());
			for (Effect effect : effects)
				effect.serializeForClientMsg(writer);
		}
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
	}

	public AbstractWorldObject getAwo() {
		return this.awo;
	}

	public void setAwo(AbstractWorldObject awo) {
		this.awo = awo;
	}
}
