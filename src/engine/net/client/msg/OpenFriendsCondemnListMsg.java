// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.gameManager.DbManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.objects.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class OpenFriendsCondemnListMsg extends ClientNetMsg {

	private int messageType;
    private ArrayList<Integer> characterList;
	private int buildingType;
	private int buildingID;
	
	private int playerType;
	private int playerID;
	private int guildID;
	private int inviteType;
	private ConcurrentHashMap<Integer,BuildingFriends>friends;
	private int removeFriendType;
	private int removeFriendID;
	private boolean reverseKOS; //TODO Rename this for to fit ReverseKOS/Activate/deactive Condemned.
	private ConcurrentHashMap<Integer,Condemned> guildCondemned;
	private int nationID;

	public OpenFriendsCondemnListMsg(int messageType,ConcurrentHashMap<Integer,BuildingFriends>friends) {
		super(Protocol.OPENFRIENDSCONDEMNLIST);
		this.messageType = messageType;
		this.friends = friends;
		
	}
	
	public OpenFriendsCondemnListMsg(int messageType,ConcurrentHashMap<Integer,Condemned> guildCondemned ,boolean reverse) {
		super(Protocol.OPENFRIENDSCONDEMNLIST);
		this.messageType = messageType;
		this.guildCondemned = guildCondemned;
		this.reverseKOS = reverse;
	}

    // clone

    public OpenFriendsCondemnListMsg(OpenFriendsCondemnListMsg openFriendsCondemnListMsg) {
        super(Protocol.OPENFRIENDSCONDEMNLIST);
        this.messageType = openFriendsCondemnListMsg.messageType;
        this.guildCondemned = openFriendsCondemnListMsg.guildCondemned;
        this.reverseKOS = openFriendsCondemnListMsg.reverseKOS;
        this.playerType = openFriendsCondemnListMsg.playerType;
        this.playerID = openFriendsCondemnListMsg.playerID;
        this.inviteType = openFriendsCondemnListMsg.inviteType;
        this.removeFriendID = openFriendsCondemnListMsg.removeFriendID;
        this.removeFriendType = openFriendsCondemnListMsg.removeFriendType;
        this.reverseKOS = openFriendsCondemnListMsg.reverseKOS;
        this.nationID = openFriendsCondemnListMsg.nationID;
        this.buildingType = openFriendsCondemnListMsg.buildingType;
        this.buildingID = openFriendsCondemnListMsg.buildingID;
        this.friends = openFriendsCondemnListMsg.friends;
        this.characterList = openFriendsCondemnListMsg.characterList;
    }

    public void configure() {

        // Pre-Cache all players and guild targets

        if (characterList == null)
            return;

        for (Integer uuid : characterList) {

            PlayerCharacter player = (PlayerCharacter) DbManager.getObject(GameObjectType.PlayerCharacter, uuid);

            if (player == null)
                continue;

            Guild guild = player.getGuild();

            if (guild == null)
                continue;
        }

    }
    
    public void configureHeraldry(PlayerCharacter player){
    	
    }

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public OpenFriendsCondemnListMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.OPENFRIENDSCONDEMNLIST, origin, reader); // openFriendsCondemnList =1239809615
        characterList = new ArrayList<>();
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.messageType);
		
		if (this.messageType == 2){
			this.showHeraldy(writer);
			return;
		}
		
		if (this.messageType == 4){
			this.writeAddHealrdy(writer);
			return;
		}
		if (this.messageType == 26){
			showBuildingFriends(writer);
			return;
		}

		if (this.messageType == 12){
			this.showCondemnList(writer);
			return;
		}
		
		if (this.messageType == 15){
			this.removeCondemned(writer);
			return;
		}
		if (this.messageType == 17){
			this.handleActivateCondemned(writer);
			return;
		}
		
	
		
		writer.putInt(0);
		writer.putInt(this.characterList.size());
		writer.putInt(this.characterList.size());

		for (Integer uuid : characterList) {

            PlayerCharacter player = (PlayerCharacter) DbManager.getObject(GameObjectType.PlayerCharacter, uuid);

            if (player == null)
                continue;

			writer.put((byte) 1);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(1);
			writer.putInt(GameObjectType.PlayerCharacter.ordinal());
			writer.putInt(player.getObjectUUID());
			Guild guild = player.getGuild();
			Guild nation = null;

			if (guild != null) {
				writer.putInt(guild.getObjectType().ordinal());
				writer.putInt(guild.getObjectUUID());
				nation = guild.getNation();
				if (nation != null) {
					writer.putInt(nation.getObjectType().ordinal());
					writer.putInt(nation.getObjectUUID());
				} else {
					writer.putInt(0);
					writer.putInt(0);
				}
			} else {
				for (int i=0;i<4;i++)
					writer.putInt(0);
			}
			writer.putShort((short)0);
			writer.put((byte)0);
			writer.putString(player.getFirstName());

			if (guild != null) {
				GuildTag._serializeForDisplay(guild.getGuildTag(),writer);
			} else {
				writer.putInt(16);
				writer.putInt(16);
				writer.putInt(16);
				writer.putInt(0);
				writer.putInt(0);
			}
			if (nation != null) {
				GuildTag._serializeForDisplay(nation.getGuildTag(),writer);
			} else {
				writer.putInt(16);
				writer.putInt(16);
				writer.putInt(16);
				writer.putInt(0);
				writer.putInt(0);
			}
			if (guild != null)
				writer.putString(guild.getName());
			else
				writer.putString("[No Guild]");
			if (nation != null)
				writer.putString(nation.getName());
			else
				writer.putString("[No Nation]");
			writer.putInt(0);
		}
	}
	
	private void readHandleToItem(ByteBufferReader reader){
		reader.getInt();
		reader.getInt();
		reader.getInt();
		
		reader.getInt(); //object Type;
		reader.getInt(); //objectID;
		
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
	}
	
	private void writeHandleToItem(ByteBufferWriter writer){
	}
	
	private void removeCondemned(ByteBufferWriter writer){
		writer.putInt(0);
		writer.putInt(playerType);
		writer.putInt(playerID);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(removeFriendType);
		writer.putInt(removeFriendID);
		writer.putInt(buildingType);
		writer.putInt(buildingID);
		writer.putInt(0);
		writer.putInt(0);
		writer.putShort((short) 0);

	}


	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.messageType = reader.getInt(); //25 on open of friends list // 28 friends list // 11 open condemned list // 14 Open condemned selection of (indivual, guild, nation)

		if (this.messageType == 1){
			this.viewHealrdy(reader);
			return;
		}
		
		if (this.messageType == 4){
			this.readAddHealrdy(reader);
			return;
		}
		
		if (this.messageType == 6){
			this.readRemoveHeraldry(reader);
			return;
		}
		if (this.messageType == 11){
			this.ackBuildingFriends(reader);
			reader.get();
			return;
		}
		
		if (this.messageType == 14){
			this.addGuildCondemn(reader);
			return;
		}
		if (this.messageType == 15){
			this.removeFriendList(reader);
			reader.get();
			return;
		}
		
		if (this.messageType == 17){
			this.handleActivateCondemned(reader);
			return;
		}
		
		if (this.messageType == 18){
			this.handleCondemnErrant(reader);
			return;
		}
		
		
		if (this.messageType == 19){
			this.handleKOS(reader);
			return;
		}
		
		if (this.messageType == 23){
			this.readHandleToItem(reader);
			return;
		}
		if (this.messageType == 28){
			addFriendsList(reader);
			return;
		}
		if (this.messageType == 30){
			removeFriendList(reader);
			return;
		}
		if (this.messageType == 25){
			ackBuildingFriends(reader);
			return;
		}

		reader.getInt();
		int size = reader.getInt();
		reader.getInt(); //size again
		for (int i=0;i<size;i++) {
			reader.get();
			reader.getInt(); //0
			reader.getInt(); //0
			reader.getInt(); //1
			reader.getInt(); //Player Type
			int ID = reader.getInt(); //Player ID
			reader.getLong(); //Guild ID
			reader.getLong(); //Nation ID
			reader.getShort();
			reader.get();
			reader.getString(); //name
			for (int j=0;j<10;j++)////////The problem is in here its stops here!!!!
				reader.getInt(); //Guild and Nation tags
			reader.getString(); //guild name
			reader.getString(); //nation name
			reader.getInt();
			this.characterList.add(ID);
		}
	}
	
	private void readRemoveHeraldry(ByteBufferReader reader){
		reader.getInt();
		reader.getInt();
		reader.getInt();
		this.playerType = reader.getInt();
		this.playerID = reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
	}
	
	private void handleActivateCondemned(ByteBufferReader reader){
		reader.getInt();
		this.removeFriendType=reader.getInt();
		this.removeFriendID = reader.getInt();
		reader.getInt();
		this.buildingID = reader.getInt();
		this.reverseKOS = reader.get() == 1? true:false;
		reader.getInt();
		reader.getInt();
		reader.get();
	}
	
	private void handleCondemnErrant(ByteBufferReader reader){
		reader.getInt();
		this.removeFriendType=reader.getInt();
		this.removeFriendID = reader.getInt();
		reader.getInt();
		this.buildingID = reader.getInt();
		this.reverseKOS = reader.get() == 1? true:false;
		reader.getInt();
		reader.getInt();
		reader.get();
	}
	private void handleActivateCondemned(ByteBufferWriter writer){
		writer.putInt(0);
		writer.putInt(removeFriendType);
		writer.putInt(removeFriendID);
		writer.putInt(GameObjectType.Building.ordinal());
		writer.putInt(buildingID);
		writer.put(reverseKOS ? (byte)1 : (byte)0);
		writer.putInt(0);
		writer.putInt(0);
		writer.put((byte)0);
	}
	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return 12;
	}
	
	private void addGuildCondemn(ByteBufferReader reader){
		reader.getInt();
		this.inviteType = reader.getInt();
		reader.getInt();
		this.playerID = reader.getInt();
		reader.getInt();
		this.guildID = reader.getInt();
		reader.getInt();
		this.nationID = reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		this.buildingID = reader.getInt();
		reader.getLong();
		reader.getShort();
		
		
	}

	private void addFriendsList(ByteBufferReader reader){
		reader.getInt();
		this.inviteType = reader.getInt(); //7 individual, 8 guild, 9 guild IC
		this.playerType = reader.getInt();
		this.playerID = reader.getInt();
		reader.getInt();
		this.guildID = reader.getInt();
		
		reader.getLong(); //Nation, do we need this?
		reader.getLong();
		reader.getLong();
		reader.getLong();
		reader.getInt();
		this.buildingType = reader.getInt();
		this.buildingID = reader.getInt();
		
		reader.getInt();
		reader.getInt();
		reader.get();

	}
	
	private void handleKOS(ByteBufferReader reader){
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		this.buildingID = reader.getInt();
		reader.get();
		byte reverse = reader.get();
		this.reverseKOS = reverse == 1 ? true : false;
		reader.getInt();
		reader.getInt();

	}
	
	private void removeFriendList(ByteBufferReader reader){
		reader.getInt();
		this.playerType = reader.getInt();
		this.playerID = reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		this.removeFriendType = reader.getInt();
		this.removeFriendID = reader.getInt();
		this.buildingType = reader.getInt();
		this.buildingID = reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.get();

	}


    public int getRemoveFriendID() {
		return removeFriendID;
	}
	
	private void showCondemnList(ByteBufferWriter writer){
		String name = "";
		PlayerCharacter pc = null;
		Guild guild = null;
		
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.put((byte)0);
		writer.put(reverseKOS ? (byte)1 : 0); //Reverse?
		
		int listSize = this.guildCondemned.size();
		writer.putInt(listSize);
		writer.putInt(listSize);

		for (Condemned condemned:this.guildCondemned.values()){
			
			
			writer.put((byte)1);
			
			switch (condemned.getFriendType()){
			case 2:
                PlayerCharacter playerCharacter = (PlayerCharacter) DbManager.getObject(engine.Enum.GameObjectType.PlayerCharacter, condemned.getPlayerUID());


				guild = playerCharacter.getGuild();
				writer.putInt(GameObjectType.PlayerCharacter.ordinal());
				writer.putInt(condemned.getPlayerUID());
				writer.putInt(condemned.getFriendType());
				writer.putInt(GameObjectType.PlayerCharacter.ordinal());
				writer.putInt(condemned.getPlayerUID());
				writer.putInt(0);
				writer.putInt(0);
				writer.putInt(GameObjectType.Guild.ordinal());
				if (guild != null)
				writer.putInt(guild.getObjectUUID());
				else
					writer.putInt(0);
				writer.put(condemned.isActive() ?(byte)1:(byte)0);
				writer.put((byte)0);
				writer.put(condemned.isActive() ?(byte)1:(byte)0);

				if (playerCharacter != null)
				writer.putString(playerCharacter.getFirstName());
				else
					writer.putInt(0);
				GuildTag._serializeForDisplay(GuildTag.ERRANT,writer);
				if (guild != null)
					GuildTag._serializeForDisplay(guild.getGuildTag(),writer);
				else{
					GuildTag._serializeForDisplay(GuildTag.ERRANT,writer);
				}
				break;
			case 4:
				guild = Guild.getGuild(condemned.getGuildUID());
				writer.putInt(GameObjectType.Guild.ordinal());
				writer.putInt(condemned.getGuildUID());
				writer.putInt(condemned.getFriendType());
				writer.putLong(0);
				writer.putInt(GameObjectType.Guild.ordinal());
				writer.putInt(condemned.getGuildUID());
				writer.putLong(0);
				writer.put((byte)0);
				writer.put(condemned.isActive() ?(byte)1:(byte)0);
				writer.put((byte)0);
				if (guild != null)
				writer.putString(guild.getName());
				else
					writer.putInt(0);
				
				if (guild != null)
					GuildTag._serializeForDisplay(guild.getGuildTag(),writer);
				else
					GuildTag._serializeForDisplay(GuildTag.ERRANT,writer);
				GuildTag._serializeForDisplay(GuildTag.ERRANT,writer);
				break;
			case 5:
				guild = Guild.getGuild(condemned.getGuildUID());
				writer.putInt(GameObjectType.Guild.ordinal());
				writer.putInt(condemned.getGuildUID());
				writer.putInt(condemned.getFriendType());
				writer.putLong(0);
				writer.putLong(0);
				writer.putInt(GameObjectType.Guild.ordinal());
				writer.putInt(condemned.getGuildUID());
				writer.put((byte)0);
				writer.put((byte)0);
				writer.put(condemned.isActive() ?(byte)1:(byte)0);
				if (guild != null)
				writer.putString(guild.getName());
				else
					writer.putInt(0);
				GuildTag._serializeForDisplay(GuildTag.ERRANT,writer);
				if (guild != null)
					GuildTag._serializeForDisplay(guild.getGuildTag(),writer);
				else{
					GuildTag._serializeForDisplay(GuildTag.ERRANT,writer);
				}
				break;
			}

			
			
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
		}
	}

	private void showBuildingFriends(ByteBufferWriter writer){
		
		
		String name = "";
		PlayerCharacter pc = null;
		Guild guild = null;
		
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.put((byte)0);
		int listSize = this.friends.size();
		writer.putInt(listSize);
		writer.putInt(listSize);

		for (BuildingFriends friend:this.friends.values()){
			pc = PlayerCharacter.getFromCache(friend.getPlayerUID());
				guild = Guild.getGuild(friend.getGuildUID());
			if (friend.getFriendType() == 7){
				if (pc != null)
				name = pc.getCombinedName();
			}
			
			else if (guild != null)
				name = guild.getName();
			writer.put((byte)1);
			if (friend.getFriendType() == 7){
				writer.putInt(GameObjectType.PlayerCharacter.ordinal());
				writer.putInt(friend.getPlayerUID());
			}else{
				writer.putInt(GameObjectType.Guild.ordinal());
				writer.putInt(friend.getGuildUID());
			}
			writer.putInt(friend.getFriendType());
			writer.putInt(0);
			writer.putInt(0);
                        
			if (guild != null) {
				writer.putInt(guild.getObjectType().ordinal());
				writer.putInt(guild.getObjectUUID());

				if (!guild.getNation().isEmptyGuild()) {
					writer.putInt(guild.getNation().getObjectType().ordinal());
					writer.putInt(guild.getNation().getObjectUUID());
				}else{
					writer.putInt(0);
					writer.putInt(0);
				}
			}else{
				writer.putLong(0);
				writer.putLong(0);
			}
			writer.putShort((short)0);
			writer.put((byte)0);
			writer.putString(name);
			if (guild != null)
				GuildTag._serializeForDisplay(guild.getGuildTag(),writer);
			else{
				writer.putInt(16);
				writer.putInt(16);
				writer.putInt(16);
				writer.putInt(0);
				writer.putInt(0);
			}
			writer.putInt(16);
			writer.putInt(16);
			writer.putInt(16);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
		}

	}

	private void ackBuildingFriends(ByteBufferReader reader){
		reader.getInt();
		reader.getInt();
		reader.getInt();
		this.buildingType = reader.getInt();
		this.buildingID = reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.get();
		
	}
	
	private void viewHealrdy(ByteBufferReader reader){
		reader.getInt();
		reader.getInt();
		reader.getInt();
	}
	
	private void showHeraldy(ByteBufferWriter writer){
		
		PlayerCharacter player = ((ClientConnection)this.getOrigin()).getPlayerCharacter();
		writer.putInt(0); //error pop up msg
		writer.putInt(0);
		
//		writer.putInt(0);
		
		HashMap<Integer,Integer> heraldryMap = Heraldry.HeraldyMap.get(player.getObjectUUID());
		
		//send empty list if no heraldry
		if (heraldryMap == null || heraldryMap.isEmpty()){
			writer.putInt(0);
			return;
		}
		
		
		writer.putInt(heraldryMap.size());
		
		for (int characterID : heraldryMap.keySet()){
			AbstractCharacter heraldryCharacter = null;
			int characterType = heraldryMap.get(characterID);
			if (characterType == GameObjectType.PlayerCharacter.ordinal())
				heraldryCharacter = PlayerCharacter.getFromCache(characterID);
			else if (characterType == GameObjectType.NPC.ordinal())
				heraldryCharacter = NPC.getFromCache(characterID);
			else if (characterType == GameObjectType.Mob.ordinal())
				heraldryCharacter = Mob.getFromCache(characterID);
			
			if (heraldryCharacter == null)
				this.showNullHeraldryCharacter(writer);
			else{
				writer.put((byte)1);
				writer.putInt(heraldryCharacter.getObjectType().ordinal());
				writer.putInt(heraldryCharacter.getObjectUUID());
			
			writer.putInt(9);
			writer.putInt(heraldryCharacter.getObjectType().ordinal());
			writer.putInt(heraldryCharacter.getObjectUUID());
	                    
			if (heraldryCharacter.getGuild() != null) {
				writer.putInt(heraldryCharacter.getGuild().getObjectType().ordinal());
				writer.putInt(heraldryCharacter.getGuild().getObjectUUID());

				if (!heraldryCharacter.getGuild().getNation().isEmptyGuild()) {
					writer.putInt(heraldryCharacter.getGuild().getNation().getObjectType().ordinal());
					writer.putInt(heraldryCharacter.getGuild().getNation().getObjectUUID());
				}else{
					writer.putInt(0);
					writer.putInt(0);
				}
			}else{
				writer.putLong(0);
				writer.putLong(0);
			}
			writer.putShort((short)0);
			writer.put((byte)0);
			writer.putString(heraldryCharacter.getName());
			if (heraldryCharacter.getGuild() != null)
				GuildTag._serializeForDisplay(heraldryCharacter.getGuild().getGuildTag(),writer);
			else{
				writer.putInt(16);
				writer.putInt(16);
				writer.putInt(16);
				writer.putInt(0);
				writer.putInt(0);
			}
			writer.putInt(16);
			writer.putInt(16);
			writer.putInt(16);
			writer.putInt(0);
			writer.putInt(0);
			if (heraldryCharacter.getGuild() == null){
				writer.putString("Errant");
				writer.putString("Errant");
			}	
			else{
				writer.putString(heraldryCharacter.getGuild().getName());
				if (heraldryCharacter.getGuild().getNation() == null)
					writer.putString("Errant");
				else
					writer.putString(heraldryCharacter.getGuild().getNation().getName());
			}
			writer.putInt(0);
			}
			
		}
	
	}
	
	private void readAddHealrdy(ByteBufferReader reader){
		reader.getInt();
		reader.getInt();
		this.playerType = reader.getInt(); //player object type;
		this.playerID = reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		
		reader.getInt();
		reader.getInt();
		reader.getInt();
	}
	
	private void writeAddHealrdy(ByteBufferWriter writer){
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(this.playerType); //player object type;
		writer.putInt(this.playerID);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
	}

	public int getMessageType() {
		return this.messageType;
	}

	public ArrayList<Integer> getList() {
		return this.characterList;
	}

	public void setMessageType(int value) {
		this.messageType = value;
	}

	public void setList(ArrayList<Integer> value) {
		this.characterList = value;
	}

	public void updateMsg(int messageType, ArrayList<Integer> list) {
		this.messageType = messageType;
		this.characterList = list;
        this.configure();
	}

	public int getInviteType() {
		return inviteType;
	}

    public int getPlayerType() {
		return playerType;
	}

	public void setPlayerType(int playerType) {
		this.playerType = playerType;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public int getGuildID() {
		return guildID;
	}
	
	public int getNationID() {
		return nationID;
	}

	public void setGuildID(int guildID) {
		this.guildID = guildID;
	}

    public int getBuildingID() {
		return buildingID;
	}

	public void setBuildingID(int buildingID) {
		this.buildingID = buildingID;
	}

	public boolean isReverseKOS() {
		return reverseKOS;
	}

	public void setReverseKOS(boolean reverseKOS) {
		this.reverseKOS = reverseKOS;
	}
	
	
	private void showNullHeraldryCharacter(ByteBufferWriter writer){
		writer.put((byte)1);
		writer.putInt(0);
		writer.putInt(0);
	
	writer.putInt(6);
	writer.putInt(0);
	writer.putInt(0);
                
	
		writer.putLong(0);
		writer.putLong(0);
	writer.putShort((short)0);
	writer.put((byte)0);
	writer.putInt(0);
	
		writer.putInt(16);
		writer.putInt(16);
		writer.putInt(16);
		writer.putInt(0);
		writer.putInt(0);
	writer.putInt(16);
	writer.putInt(16);
	writer.putInt(16);
	writer.putInt(0);
	writer.putInt(0);
	
	writer.putInt(0);
	writer.putInt(0);
	writer.putInt(0);
	
	}
}
