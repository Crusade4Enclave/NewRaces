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
import engine.objects.PlayerCharacter;

public class ChangeAltitudeMsg extends ClientNetMsg {

	private int sourceType;
	private int sourceID;
	private boolean up;
	private float startAlt;
	private float targetAlt;
	private float amountToMove;
	private byte unknown01 = (byte) 0;

	/**
	 * This is the general purpose constructor.
	 */
	public ChangeAltitudeMsg() {
		super(Protocol.CHANGEALTITUDE);
	}

	public ChangeAltitudeMsg(int sourceType, int sourceID, boolean up, float startAlt, float targetAlt, float amountToMove) {
		super(Protocol.CHANGEALTITUDE);
		this.sourceType = sourceType;
		this.sourceID = sourceID;
		this.startAlt = startAlt;
		this.targetAlt = targetAlt;
		this.amountToMove = amountToMove;
		this.up = up;
	}
	
	public static ChangeAltitudeMsg GroundPlayerMsg(PlayerCharacter pc){
		
		ChangeAltitudeMsg msg = new ChangeAltitudeMsg(pc.getObjectType().ordinal(),pc.getObjectUUID(),false,pc.getAltitude(),0,pc.getAltitude());
		return msg;
		
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ChangeAltitudeMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CHANGEALTITUDE, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);
		writer.put((this.up ? (byte) 1 : (byte) 0));
		writer.putFloat(this.startAlt);
		writer.putFloat(this.targetAlt);
		writer.putFloat(this.amountToMove);
		writer.put((byte)0);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
		this.up = (reader.get() == (byte) 1);
		this.startAlt = reader.getFloat();
		this.targetAlt = reader.getFloat();
		this.amountToMove = reader.getFloat();
		this.unknown01 = reader.get();
	}

	public int getSourceType() {
		return this.sourceType;
	}

	public int getSourceID() {
		return this.sourceID;
	}

	public boolean up() {
		return this.up;
	}

	public float getStartAlt() {
		return this.startAlt;
	}

	public float getTargetAlt() {
		return this.targetAlt;
	}

	public float getAmountToMove() {
		return this.amountToMove;
	}

	public byte getUnknown01() {
		return this.unknown01;
	}

	public void setSourceType(int value) {
		this.sourceType = value;
	}

	public void setSourceID(int value) {
		this.sourceID = value;
	}

	public void setUp(boolean value) {
		this.up = value;
	}

	public void setStartAlt(float value) {
		this.startAlt = value;
	}

	public void setTargetAlt(float value) {
		this.targetAlt = value;
	}

	public void setAmountToMove(float value) {
		this.amountToMove = value;
	}

	public void setUnknown01(byte value) {
		this.unknown01 = value;
	}
}
