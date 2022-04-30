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
import engine.objects.AbstractGameObject;
import org.pmw.tinylog.Logger;

import java.util.HashMap;


public class UnloadObjectsMsg extends ClientNetMsg {

	private HashMap<Integer,Integer> objectList = new HashMap<>();

	/**
	 * This is the general purpose constructor.
	 */
	public UnloadObjectsMsg() {
		super(Protocol.FORGETOBJECTS);
		init();
	}

	private void init() {
		objectList = new HashMap<>();
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UnloadObjectsMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.FORGETOBJECTS, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		
		if (this.objectList == null){
			writer.putInt(0);
			return;
		}
			
		writer.putInt(this.objectList.size());
		for (int objectUUID: this.objectList.keySet()){
			writer.putInt(this.objectList.get(objectUUID));
			writer.putInt(objectUUID);
		}
			
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		init();
		int size = reader.getInt();
//		for (int i = 0; i < size; i++)
//			this.objectList.add(reader.getLong());
		Logger.info( "Client telling server to unload objects.. ??");
	}

	public HashMap<Integer,Integer> getObjectList() {
		return this.objectList;
	}

	public void addObject(AbstractGameObject value) {
		this.objectList.put(value.getObjectUUID(), value.getObjectType().ordinal());
	}

	public int size() {
		return this.objectList.size();
	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		return 13;
	}
}
