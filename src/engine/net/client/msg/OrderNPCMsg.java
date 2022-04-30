// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.math.Vector3fImmutable;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;

import java.util.ArrayList;

public class OrderNPCMsg extends ClientNetMsg {

	// 2 = manage this asset.  20 = manage entire city
	private int objectType;
	private int npcUUID;
	private int buildingUUID;
	private int unknown02;
	private ArrayList<Vector3fImmutable> patrolPoints;
	private ArrayList<Vector3fImmutable> sentryPoints;
	private int patrolSize;
	private int sentrySize;

	private int actionType;
	private float buySellPercent;

	/**
	 * This is the general purpose constructor
	 */
	public OrderNPCMsg() {
		super(Protocol.ORDERNPC);
		this.actionType = 0;
		this.unknown02 = 0;
		this.npcUUID = 0;
		this.buildingUUID = 0;

	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public OrderNPCMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.ORDERNPC, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		actionType = reader.getInt();
		if (this.actionType == 28){
			this.handleCityCommand(reader);
			return;
		}
		unknown02 = reader.getInt();
		this.objectType = reader.getInt(); // Object Type Padding
		npcUUID = reader.getInt();
		reader.getInt(); // Object Type Padding
		buildingUUID = reader.getInt();
		this.buySellPercent = reader.getFloat();
                 if (actionType > 6 && actionType < 13)
			reader.getInt();



	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(actionType);
		writer.putInt(unknown02);
		writer.putInt(GameObjectType.NPC.ordinal());
		writer.putInt(npcUUID);
		writer.putInt(GameObjectType.Building.ordinal());
		writer.putInt(buildingUUID);
		writer.putFloat(this.buySellPercent);
		writer.putInt(0);


	}

	private void handleCityCommand(ByteBufferReader reader){
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		this.buildingUUID = reader.getInt();
		reader.get();
		reader.get();
		reader.getInt();
		patrolSize = reader.getInt();
		if (patrolSize > 0){
			this.patrolPoints = new ArrayList<>();
			for (int i = 0;i<patrolSize;i++){
				float x = reader.getFloat();
				float y = reader.getFloat();
				float z = reader.getFloat();
				if (this.patrolPoints.size() < 4)
					this.patrolPoints.add(new Vector3fImmutable(x,y,z));
			}
		}
		sentrySize = reader.getInt();
		if (sentrySize > 0){
			this.sentryPoints = new ArrayList<>();
			for (int i = 0;i<sentrySize;i++){
				float x = reader.getFloat();
				float y = reader.getFloat();
				float z = reader.getFloat();
				if (this.sentryPoints.size() < 4)
					this.sentryPoints.add(new Vector3fImmutable(x,y,z));
			}
		}
		reader.getInt();
		reader.getInt();
	}

	/**
	 * @return the npcUUID
	 */
	public int getNpcUUID() {
		return npcUUID;
	}



	public int getActionType() {
		return actionType;
	}


	public int getBuildingUUID() {
		return buildingUUID;
	}

	/**
	 * @param buildingUUID the buildingUUID to set
	 */
	public void setBuildingUUID(int buildingUUID) {
		this.buildingUUID = buildingUUID;
	}

	public float getBuySellPercent() {
		return buySellPercent;
	}

	public void setBuySellPercent(float buySellPercent) {
		this.buySellPercent = buySellPercent;
	}

	public int getObjectType() {
		return objectType;
	}

	public void setObjectType(int objectType) {
		this.objectType = objectType;
	}

	public ArrayList<Vector3fImmutable> getPatrolPoints() {
		return patrolPoints;
	}

	public ArrayList<Vector3fImmutable> getSentryPoints() {
		return sentryPoints;
	}

	public int getPatrolSize() {
		return patrolSize;
	}

	public void setPatrolSize(int patrolSize) {
		this.patrolSize = patrolSize;
	}

	public int getSentrySize() {
		return sentrySize;
	}

	public void setSentrySize(int sentrySize) {
		this.sentrySize = sentrySize;
	}

}

//Debug Info
//Run: Failed to make object TEMPLATE:135700 INSTANCE:1717987027141... (t=50.46) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:108760 INSTANCE:1717987027161... (t=50.46) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:108760 INSTANCE:1717987027177... (t=50.67) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:60040 INSTANCE:1717987027344... (t=50.87) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:3 INSTANCE:1717987027164... (t=50.88) (r=7/4/2011 11:56:39)

