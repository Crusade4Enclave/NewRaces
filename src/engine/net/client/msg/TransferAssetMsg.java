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

public class TransferAssetMsg extends ClientNetMsg {
	
	private int objectType;
	private int objectID;
	private int targetType;
	private int targetID;
	private int pad = 0;
	
/**
 * This constructor is used by NetMsgFactory. It attempts to deserialize the
 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
 * past the limit) then this constructor Throws that Exception to the
 * caller.
 */
public TransferAssetMsg(AbstractConnection origin, ByteBufferReader reader)  {
	super(Protocol.TRANSFERASSET, origin, reader);
}
/**
 * Deserializes the subclass specific items from the supplied NetMsgReader.
 */
@Override
protected void _deserialize(ByteBufferReader reader)  {
	this.pad = reader.getInt();
	this.objectType = reader.getInt();
	this.objectID = reader.getInt();
	this.targetType = reader.getInt();
	this.targetID = reader.getInt();
	reader.getShort();
}

/**
 * Serializes the subclass specific items to the supplied NetMsgWriter.
 */
@Override
protected void _serialize(ByteBufferWriter writer) throws SerializationException {
	writer.putInt(this.pad);
	writer.putInt(this.objectType);
	writer.putInt(this.objectID);
	writer.putInt(this.targetType);
	writer.putInt(this.targetID);
	writer.putShort((short)0);
	
}

public int getObjectType() {
	return objectType;
}

public void setObjectType(int value) {
this.objectType = value;
}

public void setPad(int value) {
this.pad = value;
}

public int getPad() {
return pad;
}

public int getTargetType() {
	return targetType;
}
public void setTargetObject(int targetObject) {
	this.targetType = targetObject;
}
public int getTargetID() {
	return targetID;
}
public void setTargetID(int targetID) {
	this.targetID = targetID;
}
public int getObjectID() {
	return objectID;
}
public void setObjectID(int objectID) {
	this.objectID = objectID;
}


}