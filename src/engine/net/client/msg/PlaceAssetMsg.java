// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum;
import engine.math.Vector3fImmutable;
import engine.net.*;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.objects.Zone;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class 	PlaceAssetMsg extends ClientNetMsg {

	/*
	Client -> Server
	//Type 1
	//940962DF 00000001 00000000 0A400000 03903DC1 00000000 00000000 00000000 3F800000 00000000 00000000 00000000 00
	//00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00
	//00000000
	//00000000
	//00000000
	//Type3
	//940962DF 00000003 00000000 00000000 00000000 00000000 00000000 00000000 3F800000 00000000 00000000 00000000 00
	//00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 01
	//00000000
	//00000001
	//	00000000 0013FF24 475765FD 417BD060 C794FF75 B33BBD2E 00000000 3F800000 00000000 <-placement info
	//00000000


	Server -> Client
	//Type 2 (Asset list / walls)
	//940962DF 00000002 00000000 00000000 00000000 00000000 00000000 00000000 3F800000 00000000 00000000 00000000 01
	//475725EC 412BD080 C794DF86 44600000 44600000 44600000 43000000 43000000 43000000 445AC000 445AC000 01
	//00000000
	//00000000
	//00000006 <-building list for walls
	//	00000000 0013FA74 00061A80 <-wall ID and cost
	//	00000000 0013FBA0 000249F0
	//	00000000 0013FCCC 000186A0
	//	00000000 0013FDF8 0007A120
	//	00000000 0013FF24 0007A120
	//	00000000 004D5FD0 0007A120
	//Type 2 (Single asset)
	//940962DF 00000002 00000000 00000000 00000000 00000000 00000000 00000000 3F800000 00000000 00000000 00000000 01
	//475725EC 412BD080 C794DF86 44600000 44600000 44600000 43000000 43000000 43000000 445AC000 445AC000 00
	//00000001
	//	00000000 00063060 00000001 <-blueprintUUID, 1 Building
	//00000000
	//00000000
	//Type 0 (Response Error)
	//940962DF 00000000 00000006 0000001b
	//430061006e006e006f007400200070006c00610063006500200061007300730065007400200069006e00200077006100740065007200
	//00000000 00000000 00000000 00000000 00000000 3F800000 00000000 00000000 00000000 00
	//00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00
	//00000000
	//00000000
	//00000000
	//Type 4 (Response Success (place asset))
	//940962DF 00000000 00000002 0000000D 00460065007500640061006C0020004300680075007200630068
	//00000000 00000000 00000000 00000000 00000000 3F800000 00000000 00000000 00000000 00
	//00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00
	//00000000
	//00000000
	//00000000
	 */

	//UseItem(C->S)->Type 2(S->C)->Type 1(C->S)  <-close placement window, no response
	//UseItem(C->S)->Type 2(S->C)->Type 3(C->S)->Type 0(S->C)  <-Attempt place asset, with error response
	//UseItem(C->S)->Type 2(S->C)->Type 3(C->S)->Type 4(S->C)  <-Attempt place asset, with success response

	/* Error msg codes
	//1		"msg append here" <- what's in msg string.
	//2		Conflict with "msg append here"
	//3		Conflict between proposed assets
	//4		Asset "msg append here" Cannot be isolated.
	//5		Asset "msg append here" is outside the fortress zone.
	//6		Cannot place the asset in water.
	//7		Cannot place asset on land.
	//8		NULL template for asset
	//9		You must be a guild member to place this asset
	//10	You must be a guild leader to place this asset
	//11	No city asset template
	//12	Only leaders of your guild can place fortresses
	//13	Your guild cannot place fortress placements here
	//14	This city architechture cannot be placed in this zone
	//15	Cannot place assets in peace zone
	//16	You do not belong to a guild
	//17	Yours is not an errant or swarn guild
	//18	There are no guild trees to be found
	//19	NULL tree city asset
	//20	You cannot place a bane circle on a tree your affiliated with
	//21	This tree cannot be affected by bane circles
	//22	Bane circles cannot effect dead trees
	//23	A bane circle is already attached to nearest tree
	//24	Banecircle is too far from guild
	//25	Banecircle is too close to guild tree
	//26	Cannot find deed in inventory
	//27	Target object is not a deed
	//28	You do not have enough gold to complete this request
	//29	You apparently do not have access to some of these assets
	//30	Insufficient deeds
	//31	Unable to locate deed to this asset
	//32	Unknown error occurred: 32
	//33	Unknown error occurred: 33
	//34	Unknown error occurred: 34
	//35	Unknown error occurred: 35
	//36	Unknown error occurred: 36
	//37	Unknown error occurred: 37
	//38	Unknown error occurred: 38
	//39	Too close to another tree
	//40	Cannot place into occupied guild zone
	//41	Cannot place outisde a guild zone
	//42	Tree cannot support anymore shrines
	//43	The city already has a shrine of that type
	//44	You must be in a player guild to place a bane circle
	//45	Tree cannot support anymore spires
	//46	A spire of that type already exists
	//47	Tree cannot support anymore barracks
	//48	Assets (except walls) must be placed one at a time
	//49	The city cannot support a warehouse at its current rank
	//50	You can only have one warehouse
	//51	This asset cannot be placed on city grid
	//52	No city to associate asset with
	//53	Buildings of war cannot be placed around a city grid unless there is an active bane
	//54	You must belong to the nation of the Bane Circle or the Tree to place buildings of war.
	//55	You must belong to a nation to place a bane circle
	//56	The building of war must be placed closer to the city
	//57	No building may be placed within this territory
	//58	This territory is full, it can support no more then "msg append here" trees.
	//59	No city to siege at this location.
	//60	This scroll's rank is too low to bane this city
	//61	The bane circle cannot support any more buildings of war
	//62	The tree cannot support any more buildings of war
	//63	Failure in guild tree claiming phase
	//64	Your nation is already at war and your limit has been reached
	//65	Unable to find a tree to target
	//66	There is no bane circle to support this building of war
	//67	There is no tree to support this building of war
	//68	There is not tree or bane circle to support this building of war
	//69	Trees must be placed within a territory
	//70	Unknown error occurred: 38
	//71	This building of war may not be placed on a city grid by attackers
	//72	You cannot place a bane circle while you are in a non player nation
	//73	Only the guild leader or inner council may place a bane circle
	//74	Only buildings of war may be placed during a bane
	//75	This current vigor of the tree withstands your attempt to place a bane circle. Minutes remaining: "msg appended here"
	//76	This tree cannot support towers or gatehouses
	//77	This tree cannot support more towers or gatehouses
	//78	This tree cannot support walls.
	//79	This tree cannot support more walls.
	 */

	private static final Map<Integer,Integer> wallToCost;
	static {
		Map<Integer,Integer> map = new HashMap<>();
		map.put(454700, 100000);   //Straight Outer Wall
		map.put(1309900, 100000);  //Irekei Outer Straight Wall
		map.put(1348900, 100000);  //Invorri Outer Straight Wall
		map.put(454650, 150000);   //Outer Wall with Stairs
		map.put(455000, 150000);   //Outer Wall with Tower
		map.put(454550, 150000);   //Outer Wall Gate
		map.put(455700, 150000);   //Small Gate House
		map.put(1309600, 150000);  //Irekei Outer Wall with Stairs
		map.put(1309300, 150000);  //Irekei Outer Wall Gate
		map.put(1331200, 150000);  //Elven Straight Outer Wall
		map.put(1330900, 150000);  //Elven Outer Wall with Stairs
		map.put(1332100, 150000);  //Elven Outer Wall with Tower
		map.put(1330300, 150000);  //Elven Outer Wall Gate
		map.put(1348600, 150000);  //Invorri Outer Wall with Stairs
		map.put(1348300, 150000);  //Invorri Outer Wall Gate
		map.put(454750, 300000);   //Concave Tower
		map.put(458100, 300000);   //Artillery Tower
		map.put(455300, 300000);   //Tower Junction
		map.put(454800, 300000);   //Convex Tower (inside corner)
		map.put(1310200, 300000);  //Irekei Concave Tower
		map.put(5070800, 300000);  //Irekei Artillery Tower
		map.put(1310500, 300000);  //Irekei Convex Tower
		map.put(1330600, 300000);  //Elven Gate House
		map.put(1331500, 300000);  //Elven Concave Tower
		map.put(5070200, 300000);  //Elven Artillery Tower
		map.put(1332400, 300000);  //Elven Tower Junction
		map.put(1331800, 300000);  //Elven Convex Tower
		map.put(1349200, 300000);  //Invorri Concave Tower
		map.put(5071400, 300000);  //Invorri Artillery Tower
		map.put(1349500, 300000);  //Invorri Convex Tower
		wallToCost = Collections.unmodifiableMap(map);
	}
	private static final Map<Integer,Integer> wallToUseId;
	static {
		Map<Integer,Integer> map = new HashMap<>();
		///Feudal Outer Walls
		map.put(454700, 1);   //Straight Outer Wall
		map.put(454650, 1);   //Outer Wall with Stairs
		map.put(455000, 1);   //Outer Wall with Tower
		map.put(454550, 1);   //Outer Wall Gate
		map.put(455700, 1);   //Small Gate House
		map.put(454750, 1);   //Concave Tower
		map.put(458100, 1);   //Artillery Tower
		map.put(455300, 1);   //Tower Junction
		map.put(454800, 1);   //Convex Tower (inside corner)
		//map.put(1, 454000, 1); //Gate House (giant gatehouse) NOT USE IN GAME
		//Feudal Inner Walls
		/*
		map.put(454100, 2);   //Inner Archway
		map.put(454200, 2);   //Inner Wall Corner
		map.put(454250, 2);   //Inner Wall Gate
		map.put(454300, 2);   //Inner Straight Wall
		map.put(454350, 2);   //Inner Wall T-Junction
		map.put(454400, 2);   //Inner Wall Cross Junction
		map.put(454850, 2);   //Tower-Inner Wall Junction (T-East)	Stuck inside left
		map.put(454900, 2);   //Tower-Inner Wall Junction (T-South) stuck inside right
		map.put(454950, 2);   //Tower-Inner Wall Junction (4-way) stuck inside
		 */
		//Irekei Outer Walls
		map.put(1309900, 3);  //Irekei Outer Straight Wall
		map.put(1309600, 3);  //Irekei Outer Wall with Stairs
		map.put(1309300, 3);  //Irekei Outer Wall Gate
		map.put(1310200, 3);  //Irekei Concave Tower
		map.put(5070800, 3);  //Irekei Artillery Tower
		map.put(1310500, 3);  //Irekei Convex Tower
		//Elven Outer Walls
		map.put(1331200, 4);  //Elven Straight Outer Wall
		map.put(1330900, 4);  //Elven Outer Wall with Stairs
		map.put(1332100, 4);  //Elven Outer Wall with Tower
		map.put(1330300, 4);  //Elven Outer Wall Gate
		map.put(1330600, 4);  //Elven Gate House
		map.put(1331500, 4);  //Elven Concave Tower
		map.put(5070200, 4);  //Elven Artillery Tower
		map.put(1332400, 4);  //Elven Tower Junction
		map.put(1331800, 4);  //Elven Convex Tower
		//Invorri Outer Walls
		map.put(1348900, 5);  //Invorri Outer Straight Wall
		map.put(1348600, 5);  //Invorri Outer Wall with Stairs
		map.put(1348300, 5);  //Invorri Outer Wall Gate
		map.put(1349200, 5);  //Invorri Concave Tower
		map.put(5071400, 5);  //Invorri Artillery Tower
		map.put(1349500, 5);  //Invorri Convex Tower
		wallToUseId = Collections.unmodifiableMap(map);
	}
	private static final Map<Integer, Map<Integer,Integer>> useIdToWallCostMaps;
	static {
		//autoloaded based on wallToUseId and wallToCost
		Map<Integer, Map<Integer,Integer>> map = new HashMap<>();
		for (Map.Entry<Integer,Integer> entry : wallToUseId.entrySet()) {
			int wallId = entry.getKey();
			int useId = entry.getValue();
			int cost = 0;
			Integer costCheck = wallToCost.get(wallId);
			if (costCheck != null) {
				cost = costCheck;
			} else {
				throw new Error("PlaceAssetMsg: WallId '" + wallId + "' has no cost in 'wallToCost' but exists in 'useIdToWall'.");
			}
			if (!map.containsKey(useId)) {
				map.put(useId, new HashMap<>());
			}
			map.get(useId).put(wallId, cost);
		}
		for (Map.Entry<Integer,Map<Integer,Integer>> entry : map.entrySet()) {
			map.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
		}
		useIdToWallCostMaps = Collections.unmodifiableMap(map);
	}

	private int actionType; //1,3 (recv), 0,2 (send)
	private int msgID; //used on type 0, 0 on all other types
	private String msg; //used on type 0
	private int contractType; //used on type 1
	private int contractID; //used on type 1
	private byte unknown01; //0x01 on type 2 (send city data). 0x00 otherwise
	private float x;
	private float y;
	private float z;
	private byte unknown02; //0x01 if data follow, 0x00 otherwise. Best guess
	private ArrayList<PlacementInfo> placementInfo;

	private static final int NONE = 0;
	private static final int CLIENTREQ_UNKNOWN = 1;
	private static final int SERVER_OPENWINDOW = 2;
	private static final int CLIENTREQ_NEWBUILDING = 3;  // Request to place asset
	private static final int SERVER_CLOSEWINDOW = 4;

	/**
	 * This is the general purpose constructor.
	 */
	public PlaceAssetMsg() {
		super(Protocol.PLACEASSET);
		this.placementInfo = new ArrayList<>();
		this.actionType = SERVER_OPENWINDOW;
		this.msgID = 0;
		this.msg = "";
		this.contractType = 0;
		this.contractID = 0;
		this.unknown01 = (byte)0x00;
		this.x = 0f;
		this.y = 0f;
		this.z = 0f;
		this.unknown02 = (byte)0x01;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public PlaceAssetMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.PLACEASSET, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {

		writer.putInt(this.actionType);

		if (this.actionType == NONE) {
			writer.putInt(this.msgID);
			if (this.msgID != 0)
			writer.putString(this.msg);
		} else if (this.actionType == SERVER_CLOSEWINDOW) {
			//writer.putInt(1);  //Qty of assets placed?? A 0 will crash the client. Any value >0 seems to do the same thing.
			writer.putInt(0);
		} else {
			writer.putInt(0);
		}

		writer.putInt(this.contractType);
		writer.putInt(this.contractID);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putFloat(1f);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);

		if (this.actionType == SERVER_OPENWINDOW || (this.actionType == NONE && this.msgID == 0) ) {
			//send Place Asset Msg
			writer.put((byte)0x01);
			writer.putFloat(x);
			writer.putFloat(y);
			writer.putFloat(z);
			for (int i=0;i<3;i++)
				writer.putFloat(896); // city Bounds full extent.
			for (int i=0;i<3;i++)
				writer.putFloat(128); //grid dimensions
			PlacementInfo pi = (this.placementInfo.size() > 0) ? this.placementInfo.get(0) : null;
			int buildingID = (pi != null) ? pi.getBlueprintUUID() : 0;
			if (buildingID == 24200){
				writer.putFloat(875);
				writer.putFloat(875);
			}else{
				writer.putFloat(576);
				writer.putFloat(576);
			}



			if (buildingID < 6) {
				//place wall lists
				writer.put((byte)0x01);
				writer.putInt(0);
				writer.putInt(0);
				Map<Integer, Integer> buildings = getBuildingList(buildingID);
				writer.putInt(buildings.size());
				for (int bID : buildings.keySet()) {
					writer.putInt(0);
					writer.putInt(bID);
					writer.putInt(buildings.get(bID));
				}
			} else {
				//send individual building
				writer.put((byte)0x00);
				writer.putInt(1);
				writer.putInt(0);
				writer.putInt(buildingID);
				writer.putInt(1);
				writer.putInt(0);
				writer.putInt(0);
			}
		} else {
			//Send Server response to client placing asset
			writer.put((byte)0x00);
			for (int i=0;i<11;i++)
				writer.putFloat(0f);
			writer.put((byte)0x00);
			writer.putInt(0);
			if (this.placementInfo == null)
				writer.putInt(0);
			else{
				writer.putInt(this.placementInfo.size());
				for (PlacementInfo placementInfo : this.placementInfo){
					writer.putInt(0);
					writer.putInt(placementInfo.blueprintUUID);
					writer.putVector3f(placementInfo.loc);
					writer.putFloat(placementInfo.w);
					writer.putVector3f(placementInfo.rot);
				}
			}
			
			writer.putInt(0);
		}
	}

	private static Map<Integer, Integer> getBuildingList(int buildingID) {
		if (useIdToWallCostMaps.containsKey(buildingID)) {
			return useIdToWallCostMaps.get(buildingID);
		}
		return new HashMap<>(0);
	}

	public static int getWallCost(int blueprintUUID) {
		if (wallToCost.containsKey(blueprintUUID)) {
			return wallToCost.get(blueprintUUID);
		}
		Logger.warn("Cost of Wall '" + blueprintUUID + "' was requested but no cost is configured for that wallId.");
		return 0;
	}

	public static void sendPlaceAssetError(ClientConnection origin, int errorID, String stringData) {

		PlaceAssetMsg outMsg;

		outMsg = new PlaceAssetMsg();
        outMsg.actionType = 0;
        outMsg.msgID = errorID;
        outMsg.msg = stringData;

        Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), outMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	}
	
	public static void sendPlaceAssetConfirmWall(ClientConnection origin, Zone zone) {
		
		PlaceAssetMsg outMsg = new PlaceAssetMsg();
        outMsg.actionType = 0;
        outMsg.msgID = 0;
        outMsg.msg = "";
        outMsg.x = zone.getLoc().x + 64;
        outMsg.y = zone.getLoc().y;
        outMsg.z = zone.getLoc().z + 64;
        

        Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), outMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.placementInfo = new ArrayList<>();
		this.actionType = reader.getInt();
		reader.getInt();
		this.contractType = reader.getInt();
		this.contractID = reader.getInt();
		for (int i=0; i<7; i++)
			reader.getInt();
		reader.get();
		for (int i=0; i<11; i++)
			reader.getInt();
		reader.get();
		reader.getInt();
		int placementInfo = reader.getInt();
		for (int i=0;i<placementInfo;i++) {
			reader.getInt();
			PlacementInfo pi = new PlacementInfo(reader.getInt(), reader.getFloat(), reader.getFloat(),
					reader.getFloat(), reader.getFloat(), reader.getFloat(), reader.getFloat(), reader.getFloat());
			this.placementInfo.add(pi);
		}
		reader.getInt();
	}

	public int getActionType() {
		return this.actionType;
	}
	public int getID() {
		return this.msgID;
	}
	public String getMsg() {
		return msg;
	}
	public int getContractType() {
		return this.contractType;
	}
	public int getContractID() {
		return this.contractID;
	}
	public byte getUnknown01() {
		return this.unknown01;
	}
	public float getX() {
		return this.x;
	}
	public float getY() {
		return this.y;
	}
	public float getZ() {
		return this.z;
	}
	public byte getUnknown02() {
		return this.unknown02;
	}
	public ArrayList<PlacementInfo> getPlacementInfo() {
		return this.placementInfo;
	}
	public PlacementInfo getFirstPlacementInfo() {
		if (this.placementInfo.size() > 0)
			return this.placementInfo.get(0);
		return null;
	}

	public void setActionType(int value) {
		this.actionType = value;
	}
	public void setMsgID(int value) {
		this.msgID = value;
	}
	public void setMsg(String value) {
		this.msg = value;
	}
	public void setContractType(int value) {
		this.contractType = value;
	}
	public void setContractID(int value) {
		this.contractID = value;
	}
	public void setUnknown01(byte value) {
		this.unknown01 = value;
	}
	public void setX(float value) {
		this.x = value;
	}
	public void setY(float value) {
		this.y = value;
	}
	public void setZ(float value) {
		this.z = value;
	}
	public void setUnknown02(byte value) {
		this.unknown02 = value;
	}
	public void addPlacementInfo(int ID) {
		PlacementInfo pi = new PlacementInfo(ID, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
		this.placementInfo.add(pi);
	}

	public static class PlacementInfo {
		int blueprintUUID;
		Vector3fImmutable loc;
		float w;
		Vector3fImmutable rot;
		public PlacementInfo(int blueprintUUID, float locX, float locY, float locZ, float w, float rotX, float rotY, float rotZ) {
			this.blueprintUUID = blueprintUUID;
			this.loc = new Vector3fImmutable(locX, locY, locZ);
			this.w = w;
			this.rot = new Vector3fImmutable(rotX, rotY, rotZ);
		}
		public int getBlueprintUUID() {
			return this.blueprintUUID;
		}
		public Vector3fImmutable getLoc() {
			return this.loc;
		}
		public float getW() {
			return this.w;
		}
		public Vector3fImmutable getRot() {
			return this.rot;
		}
		public void setLoc(Vector3fImmutable loc) {
			this.loc = loc;
		}
	}
}
