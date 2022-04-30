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
import engine.math.Vector3fImmutable;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;

public class FurnitureMsg extends ClientNetMsg {


	private int type;
	private int buildingID;
	private int size;
	private int pad = 0;
	private int itemID;
	private Vector3fImmutable furnitureLoc;
	private int floor;
	private float rot;
	private float w;




	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public FurnitureMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.FURNITURE, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.type = reader.getInt();
		reader.getInt();
		this.buildingID = reader.getInt();
		if (this.type == 3){
			reader.getInt();
			reader.getInt();
			reader.getInt();
            this.itemID = reader.getInt();
            this.furnitureLoc = reader.getVector3fImmutable();
            reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			return;


		}
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();


	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return (15);
	}


	// Precache and configure this message before we serialize it

	public void configure() {



	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {
		if (this.type == 3 || this.type == 4){
			writer.putInt(this.type);
			writer.putInt(GameObjectType.Building.ordinal());
			writer.putInt(this.buildingID);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(GameObjectType.Item.ordinal());
			writer.putInt(62280);
			writer.putVector3f(this.furnitureLoc);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			return;
		}
		
		
		writer.putInt(2); //Type
		writer.putInt(GameObjectType.Building.ordinal());
		writer.putInt(this.buildingID);
		writer.putInt(GameObjectType.Building.ordinal());
		writer.putInt(this.buildingID);
		writer.put((byte)1);
		writer.putInt(1);
		writer.putInt(GameObjectType.Item.ordinal());
		writer.putInt(62280);
		writer.putInt(0);
	
		writer.putInt(1);
	
		writer.putInt(0);
		writer.putInt(362003);
		writer.putInt(GameObjectType.Item.ordinal());
		writer.putInt(62280);
		
		writer.putInt(GameObjectType.Building.ordinal());
		writer.putInt(this.buildingID);
		
		writer.putInt(0);
		writer.putInt(0);
				
				writer.putInt(0);
				writer.putInt(0);
				writer.putInt(0);
				writer.putInt(0);
				
				writer.put((byte)0);
				
			//	writer.putInt(0);
		//writer.putInt(0);
		
//		writer.putInt(1);
//		writer.putInt(GameObjectType.Building.ordinal());
//				writer.putInt(62281);
//				writer.putInt(0);
//				writer.putInt(362003);
//				writer.putInt(0);
//				writer.putInt(0);
//				writer.putInt(0);
//				writer.putInt(0);
//				writer.putInt(0);
//				writer.putInt(0);
//				writer.putInt(0);
//				writer.putInt(0);
//				writer.put((byte)1);
//				writer.putInt(1);
//				writer.putInt(GameObjectType.Building.ordinal());
//						writer.putInt(62281);
//						writer.putInt(0);
//						writer.putInt(362003);
//						writer.putInt(0);
//						writer.putInt(0);
//						writer.putInt(0);
//						writer.putInt(0);
//						writer.putInt(0);
//						writer.putInt(0);
//						writer.putInt(0);
//						writer.putInt(0);
//						writer.put((byte)0);

		

	
		//		else{
		//			writer.putInt(building.getFurnitureList().size());
		//			for (int furnitureID: building.getFurnitureList()){
		//				Building furniture = Building.getBuildingFromCache(furnitureID);
		//				writer.putInt(GameObjectType.Building.ordinal());
		//				writer.putInt(furniture.getObjectUUID());
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.put((byte)0);
		//			}

		//}

		//		else{
		//			writer.putInt(building.getFurnitureList().size());
		//			for (int furnitureID: building.getFurnitureList()){
		//				Building furniture = Building.getBuildingFromCache(furnitureID);
		//				writer.putInt(GameObjectType.Building.ordinal());
		//				writer.putInt(furniture.getObjectUUID());
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.putInt(0);
		//				writer.put((byte)0);
		//			}
		//
		//		}
	}

	public int getPad() {
		return pad;
	}

	public void setPad(int value) {
		this.pad = value;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getBuildingID() {
		return buildingID;
	}

	public void setBuildingID(int buildingID) {
		this.buildingID = buildingID;
	}

	public int getItemID() {
		return itemID;
	}

	public void setItemID(int itemID) {
		this.itemID = itemID;
	}

	public Vector3fImmutable getFurnitureLoc() {
		return furnitureLoc;
	}

	public void setFurnitureLoc(Vector3fImmutable furnitureLoc) {
		this.furnitureLoc = furnitureLoc;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	public float getRot() {
		return rot;
	}

	public void setRot(float rot) {
		this.rot = rot;
	}

	public float getw() {
		return w;
	}

	public void setw(float w) {
		this.w = w;
	}

}
