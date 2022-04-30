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


public class ApplyBuildingEffectMsg extends ClientNetMsg {

	protected int unknown01;
	protected int unknown02;
	protected int buildingType;
	protected int buildingID;
	protected int unknown03;

	/**
	 * This is the general purpose constructor.
	 */
	public ApplyBuildingEffectMsg() {
		super(Protocol.VISUALUPDATE);
	}

	public ApplyBuildingEffectMsg(int unknown01, int unknown02, int buildingType, int buildingID, int unknown03) {
		super(Protocol.VISUALUPDATE);
		this.unknown01 = unknown01;
		this.unknown02 = unknown02;
		this.buildingType = buildingType;
		this.buildingID = buildingID;
		this.unknown03 = unknown03;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ApplyBuildingEffectMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.VISUALUPDATE, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		if (this.unknown02 == 0){
			writer.putInt(this.unknown03);
			writer.putInt(this.buildingType);
			writer.putInt(this.buildingID);
			writer.putInt(0);
			return;
		}
		writer.putInt(this.buildingType);
		writer.putInt(this.buildingID);
		writer.putInt(this.unknown03);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
	}

	public void setUnknown01(int unknown01) {
		this.unknown01 = unknown01;
	}

	public void setUnknown02(int unknown02) {
		this.unknown02 = unknown02;
	}

	public void setBuildingID(int buildingID) {
		this.buildingID = buildingID;
	}

	public void setUnknown03(int unknown03) {
		this.unknown03 = unknown03;
	}

	public int getUnknown01() {
		return unknown01;
	}

	public int getUnknown02() {
		return unknown02;
	}

	public int getBuildingID() {
		return buildingID;
	}

	public int getUnknown03() {
		return unknown03;
	}
}
