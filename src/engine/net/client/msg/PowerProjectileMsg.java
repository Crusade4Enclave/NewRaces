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
import engine.objects.AbstractWorldObject;


public class PowerProjectileMsg extends ClientNetMsg {
	private AbstractWorldObject source;
	private AbstractWorldObject target;
	private float range = 10;

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */

	public PowerProjectileMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ARCPOWERPROJECTILE, origin, reader);
	}

	public PowerProjectileMsg() {
		super(Protocol.ARCPOWERPROJECTILE);
	}

	public PowerProjectileMsg(AbstractWorldObject source,AbstractWorldObject target) {
		super(Protocol.ARCPOWERPROJECTILE);
		this.source = source;
		this.target = target;
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {

	}

	// Pre-cache and configure values so they are available when we serialize

	public void configure() {

		if (this.source == null)
			return;

		if (this.target == null)
			return;

	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		engine.math.Vector3fImmutable faceDir = this.source.getLoc().subtract2D(target.getLoc()).normalize();
		engine.math.Vector3fImmutable newLoc =faceDir.scaleAdd(range, target.getLoc());

		newLoc = newLoc.setY(newLoc.getY() + range);

		writer.putInt(this.source.getObjectType().ordinal());
		writer.putInt(this.source.getObjectUUID());
		writer.putVector3f(newLoc);

	}

	public float getRange() {
		return range;
	}

	public void setRange(float range) {
		this.range = range;
	}

}
