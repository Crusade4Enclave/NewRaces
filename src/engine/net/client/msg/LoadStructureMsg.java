// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Building;

import java.util.ArrayList;


public class LoadStructureMsg extends ClientNetMsg {

	private ArrayList<Building> structureList;

	/**
	 * This is the general purpose constructor.
	 */
	public LoadStructureMsg() {
		this(new ArrayList<>());
	}

	/**
	 * This is the general purpose constructor.
	 *
	 * @param structures
	 */
	public LoadStructureMsg(ArrayList<Building> structures) {
		super(Protocol.LOADSTRUCTURE);
		this.structureList = structures;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public LoadStructureMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.LOADSTRUCTURE, origin, reader);
	}

	/**
	 * @see AbstractNetMsg#getPowerOfTwoBufferSize()
	 */
	@Override
	protected int getPowerOfTwoBufferSize() {
		//Larger size for historically larger opcodes
		return (18); // 2^16 == 64k
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		for (int i = 0; i < 4; i++)
			writer.putInt(0);

		writer.putInt(this.structureList.size());
		
		for (Building building:this.structureList){
			Building._serializeForClientMsg(building, writer);
		}
		writer.putInt(0);
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
		int size = reader.getInt();
		// TODO finish Deserialize impl
	}

	// TODO fix ArrayList Accessability.
	public ArrayList<Building> getStructureList() {
		return this.structureList;
	}

	public void addObject(Building obj) {
		this.structureList.add(obj);
	}

	public int size() {
		return this.structureList.size();
	}
}
