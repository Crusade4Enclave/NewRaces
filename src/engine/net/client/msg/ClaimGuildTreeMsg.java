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


/**
 * Open manage city asset window
 *
 * @author Eighty
 */
public class ClaimGuildTreeMsg extends ClientNetMsg {

	// 2 = manage this asset.  20 = manage entire city

	private int messageType;

	private int targetType;
	private int targetID;

	private int charter;
	private int bgc1;
	private int bgc2;
	private int symbolColor;
	private int bgDesign;
	private int symbol;
	private int unknown07;
	private int unknown08;
	private int unknown09;
	private int unknown10;
	private int unknown11;

	private String CityName;
	private String OwnerName;
	private String GuildName;
	private int unknown12;
	private byte UnkByte01;
	private int unknown13;
	private int unknown14;
	private int unknown15;
	private int unknown16;
	private int unknown17;
	private int unknown18;

	private byte UnkByte02;
	private byte UnkByte03;
	private byte UnkByte04;
	private byte UnkByte05;

	private int unknown19; //Arraylist motto length?
	private String motto; //motto Length 60 max?
	public static final int RENAME_TREE = 2;
	public static final int OPEN_CITY = 4;
	public static final int CLOSE_CITY = 5;
	private String treeName;

//	private int unknown01;









	/**
	 * This is the general purpose constructor
	 */
	public ClaimGuildTreeMsg() {
		super(Protocol.CLAIMGUILDTREE);
	 this.messageType = 0;
		this.targetType=0;
		this.targetID = 0;

	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public ClaimGuildTreeMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.CLAIMGUILDTREE, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		this.messageType = reader.getInt();
		switch (this.messageType){
		case OPEN_CITY:
		case CLOSE_CITY:
			targetType = reader.getInt();
			targetID = reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			break;
		case RENAME_TREE:
			targetType = reader.getInt();
			targetID = reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			this.treeName = reader.getString();
			break;
		default:
			targetType = reader.getInt();
			targetID = reader.getInt();
			reader.getInt();
			reader.getInt();
			this.treeName = reader.getString();
			break;
		}
		
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer){
		writer.putInt(this.messageType);
		writer.putInt(this.targetType);
		writer.putInt(this.targetID);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		if (this.messageType == RENAME_TREE)
		writer.putString(this.treeName);
		}

	/**
	 * @return the charter
	 */
	public int getcharter() {
		return charter;
	}

	
	

	public int getbgc1() {
		return bgc1;
	}
	public int getbgc2() {
		return bgc2;
	}
	public int getsymbolColor() {
		return symbolColor;
	}
	public int getbgDesign() {
		return bgDesign;
	}
	public int getsymbol() {
		return symbol;
	}
	public int getUnknown07() {
		return unknown07;
	}
	public int getUnknown08() {
		return unknown08;
	}
	public int getUnknown09() {
		return unknown09;
	}
	public int getUnknown10() {
		return unknown10;
	}
	public int getUnknown11() {
		return unknown11;
	}
	public int getUnknown12() {
		return unknown12;
	}
	public int getUnknown13() {
		return unknown13;
	}
	public int getUnknown14() {
		return unknown14;
	}
	public int getUnknown15() {
		return unknown15;
	}
	public int getUnknown16() {
		return unknown16;
	}
	public int getUnknown17() {
		return unknown17;
	}
	public int getUnknown18() {
		return unknown18;
	}
	public int getUnknown19() {
		return unknown19;
	}









	public String getOwnerName() {
		return OwnerName;
	}

	public String getCityName() {
		return CityName;
	}

	public String getGuildName() {
		return GuildName;
	}
	public void setcharter(int charter) {
		this.charter = charter;
	}
	public void setbgc1 (int bgc1) {
		this.bgc1 = bgc1;
	}
	public void setbgc2 (int bgc2) {
		this.bgc2 = bgc2;
	}
	public void setsymbolColor (int symbolColor) {
		this.symbolColor = symbolColor;
	}
	public void setbgDesign (int bgDesign) {
		this.bgDesign = bgDesign;
	}
	public void setsymbol (int symbol) {
		this.symbol = symbol;
	}
	public void setUnknown07 (int unknown07) {
		this.unknown07 = unknown07;
	}
	public void setUnknown08 (int unknown08) {
		this.unknown08 = unknown08;
	}
	public void setUnknown09 (int unknown09) {
		this.unknown09 = unknown09;
	}
	public void setUnknown10 (int unknown10) {
		this.unknown10 = unknown10;
	}
	public void setUnknown11 (int unknown11) {
		this.unknown11 = unknown11;
	}
	public void setUnknown12 (int unknown12) {
		this.unknown12 = unknown12;
	}
	public void setUnknown13 (int unknown13) {
		this.unknown13 = unknown13;
	}
	public void setUnknown14 (int unknown14) {
		this.unknown14 = unknown14;
	}
	public void setUnknown15 (int unknown15) {
		this.unknown15 = unknown15;
	}
	public void setUnknown16 (int unknown16) {
		this.unknown16 = unknown16;
	}
	public void setUnknown17 (int unknown17) {
		this.unknown17 = unknown17;
	}
	public void setUnknown18 (int unknown18) {
		this.unknown18 = unknown18;
	}
	public void setUnknown19 (int unknown19) {
		this.unknown19 = unknown19;
	}




	public void setUnkByte01 (byte UnkByte01) {
		this.UnkByte01 = UnkByte01;
	}
	public void setUnkByte02 (byte UnkByte02) {
		this.UnkByte02 = UnkByte02;
	}
	public void setUnkByte03 (byte UnkByte03) {
		this.UnkByte03 = UnkByte03;
	}
	public void setUnkByte04 (byte UnkByte04) {
		this.UnkByte04 = UnkByte04;
	}


	public void setOwnerName(String OwnerName) {
		this.OwnerName = OwnerName;
	}

	public void setCityName(String CityName) {
		this.CityName = CityName;
	}

	public void setGuildName(String GuildName) {
		this.GuildName = GuildName;
	}



	public void setMotto(String motto) {
		this.motto = motto;
	}

	public String getMotto() {
		return motto;
	}



	public void setUnkByte05(byte unkByte05) {
		UnkByte05 = unkByte05;
	}

	public byte getUnkByte05() {
		return UnkByte05;
	}

	public void setMessageType(int value) {
		this.messageType = value;
	}

	public int getMessageType() {
		return messageType;
	}

	public int getTargetID() {
		return targetID;
	}

	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

	public int getTargetType() {
		return targetType;
	}

	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}
	

	public String getTreeName() {
		return treeName;
	}

}

//Debug Info
//Run: Failed to make object TEMPLATE:135700 INSTANCE:1717987027141... (t=50.46) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:108760 INSTANCE:1717987027161... (t=50.46) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:108760 INSTANCE:1717987027177... (t=50.67) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:60040 INSTANCE:1717987027344... (t=50.87) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:3 INSTANCE:1717987027164... (t=50.88) (r=7/4/2011 11:56:39)

