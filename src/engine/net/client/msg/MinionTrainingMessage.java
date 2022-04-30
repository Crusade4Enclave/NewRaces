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

public class MinionTrainingMessage extends ClientNetMsg {
	private int npcID;
	private int npcType;
	private int buildingID;
	private int buildingType;
	private int type;
	private int pad = 0;
	private int objectType;
	private int objectUUID;
	private boolean isTreb = false;
	private boolean isMangonal = false;
	private boolean isBallista = false;
	private int minion;
	private int mobType;
	private int mobID;
	
	
	
	
	

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public MinionTrainingMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.MINIONTRAINING, origin, reader);
	}
	
	public MinionTrainingMessage() {
		super(Protocol.MINIONTRAINING);
	}
	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.type = reader.getInt();
		if (this.type == 2){
			this.buildingType = reader.getInt();
			this.buildingID = reader.getInt();
			this.npcType = reader.getInt();
			this.npcID = reader.getInt();
			this.objectType = reader.getInt();
			this.objectUUID = reader.getInt();
			reader.getInt();
			reader.getInt();
			
		}else{
			this.buildingType = reader.getInt();
			this.buildingID = reader.getInt();
			this.npcType = reader.getInt();
			this.npcID = reader.getInt();
		reader.getInt();
		this.minion = reader.getInt();
		if (this.minion == 1)
			this.isTreb = true;
		else if(this.minion == 2)
			this.isBallista = true;
		else if (this.minion == 3)
			this.isMangonal = true;
		reader.getInt();
		reader.getInt();
		}
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {
		
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

	public int getUUID() {
		return objectUUID;

	}

	public int getPad() {
		return pad;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public boolean isTreb() {
		return isTreb;
	}
	public void setTreb(boolean isTreb) {
		this.isTreb = isTreb;
	}
	public boolean isMangonal() {
		return isMangonal;
	}
	public void setMangonal(boolean isMangonal) {
		this.isMangonal = isMangonal;
	}
	public boolean isBallista() {
		return isBallista;
	}
	public void setBallista(boolean isBallista) {
		this.isBallista = isBallista;
	}
	public int getMinion() {
		return minion;
	}
	public void setMinion(int minion) {
		this.minion = minion;
	}

	public int getNpcID() {
		return npcID;
	}

	public void setNpcID(int npcID) {
		this.npcID = npcID;
	}

	public int getNpcType() {
		return npcType;
	}

	public void setNpcType(int npcType) {
		this.npcType = npcType;
	}

	public int getBuildingID() {
		return buildingID;
	}

	public void setBuildingID(int buildingID) {
		this.buildingID = buildingID;
	}

	public int getBuildingType() {
		return buildingType;
	}

	public void setBuildingType(int buildingType) {
		this.buildingType = buildingType;
	}

	public int getMobType() {
		return mobType;
	}

	public void setMobType(int mobType) {
		this.mobType = mobType;
	}

	public int getMobID() {
		return mobID;
	}

	public void setMobID(int mobID) {
		this.mobID = mobID;
	}

}
