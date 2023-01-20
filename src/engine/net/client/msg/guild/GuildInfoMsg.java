// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.guild;


import engine.Enum.GameObjectType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.*;
import org.joda.time.DateTime;


public class GuildInfoMsg extends ClientNetMsg {

	private int msgType;
	private int objectUUID;
        private int objectType;
	private Guild guild;
	private AbstractGameObject ago;

	/**
	 * This is the general purpose constructor.
	 */
	public GuildInfoMsg() {
		super(Protocol.UPDATEGUILD);
		this.msgType = 4;
        this.objectType = 0;
        this.objectUUID = 0;

	}

	public GuildInfoMsg(AbstractGameObject ago, Guild guild, int unknown01) {
		super(Protocol.UPDATEGUILD);
		this.msgType = unknown01;
        this.objectType = ago.getObjectType().ordinal();
        this.objectUUID = ago.getObjectUUID();
		this.ago = ago;

		this.guild = guild;
	}


	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public GuildInfoMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.UPDATEGUILD, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.msgType);

		if(this.msgType == 2) {
			new GuildInfoMessageType2(this.ago, guild)._serialize(writer);
		} else if(this.msgType == 5) {
			new GuildInfoMessageType5(this.ago, guild)._serialize(writer);
		}else if(this.msgType == 4){
			new GuildInfoMessageType4(this.ago, guild)._serialize(writer);
		} else {
			writer.putLong(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte) 0);
		}
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.msgType = reader.getInt();
        this.objectType = reader.getInt();
        this.objectUUID = reader.getInt();
        
        if (this.msgType == 1)
        	reader.getInt();
		reader.getInt(); // PAdding
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.get();
		
		//default guild tag deserializations.
		if (this.msgType == 5){
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
		
		
	}

	/**
	 * @return the unknown01
	 */
	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	/**
	 * @return the objectUUID
	 */
	public int getObjectID() {
		return objectUUID;
	}

	public int getObjectType() {
		return objectType;
	}

	public void setObjectType(int objectType) {
		this.objectType = objectType;
	}
}

abstract class GuildInfoMessageType {
	protected int objectType;
	protected int objectID;
	protected Guild g;
	protected AbstractGameObject ago;

	public GuildInfoMessageType(AbstractGameObject ago, Guild g) {
		this.objectType = ago.getObjectType().ordinal();
		this.objectID = ago.getObjectUUID();
		this.ago = ago;
		this.g = g;
	}

	abstract void _serialize(ByteBufferWriter writer);
}

class GuildInfoMessageType2 extends GuildInfoMessageType {

	public GuildInfoMessageType2(AbstractGameObject ago, Guild g) {
		super(ago, g);
	}

	@Override
	void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.objectType);
		writer.putInt(this.objectID);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(1);

		Guild nation = null;
		if (this.g != null) {
                        writer.putInt(GameObjectType.Guild.ordinal());
			writer.putInt(g.getObjectUUID());
			writer.putString(g.getName());

			if(this.objectType == GameObjectType.PlayerCharacter.ordinal()) {
				PlayerCharacter pc = PlayerCharacter.getFromCache(this.objectID);

				if(pc != null) {
					writer.putInt(GuildStatusController.getRank(pc.getGuildStatus()));
				}
			} else {
				writer.putInt(5);
			}
			GuildTag._serializeForDisplay(g.getGuildTag(),writer);
			nation = this.g.getNation();
		} else {
			writer.putLong(0L);
			writer.putString("");
			GuildTag._serializeForDisplay(GuildTag.ERRANT,writer);
		}

		writer.putInt(1);
		if (nation != null) {
                        writer.putInt(GameObjectType.Guild.ordinal());
			writer.putInt(nation.getObjectUUID());

			City city = g.getOwnedCity();
			if (city != null) {
				writer.putString(city.getCityName());
				writer.putInt(city.getObjectType().ordinal());
				writer.putInt(city.getObjectUUID());
			} else {
				writer.putString(""); //city name
				writer.putLong(0L); //should be city ID
			}

			GuildTag._serializeForDisplay(nation.getGuildTag(),writer);

		} else {
			writer.putLong(0L);
			writer.putString("");
			writer.putLong(0L);
			GuildTag._serializeForDisplay(GuildTag.ERRANT,writer);
		}
		writer.putInt(0);
	 	
		writer.putInt(0);
		writer.put((byte)0);
	}
}

class GuildInfoMessageType4 extends GuildInfoMessageType {

	public GuildInfoMessageType4(AbstractGameObject ago, Guild g) {
		super(ago, g);
	}

	@Override
	void _serialize(ByteBufferWriter writer) {
		String cityName = "";
		writer.putInt(this.objectType);
		writer.putInt(this.objectID);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		PlayerCharacter pc = PlayerCharacter.getFromCache(this.objectID);
		if (this.g == null || pc == null){
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte)0);
			return;
		}

		writer.putInt(1);
		Guild nation = this.g.getNation();
		writer.putInt(0);
		writer.putInt(0);
		writer.putString(g.getName());
		writer.putInt(0);
		GuildTag._serializeForDisplay(g.getGuildTag(),writer);
		writer.putInt(1);
		writer.putInt(0);
		writer.putInt(0);
		City city = g.getOwnedCity();
		if (city != null)
			cityName = city.getCityName();
		writer.putString(nation.getName());
		writer.putInt(0);
		writer.putInt(0);

		GuildTag._serializeForDisplay(nation.getGuildTag(),writer);
		writer.putInt(1);

		writer.putString(g.getName());
		writer.putString(g.getMotto());
		writer.putString(nation.getName());
		writer.putInt(GuildStatusController.getRank(pc.getGuildStatus()));
		writer.putInt(GuildStatusController.getTitle(pc.getGuildStatus()));
		writer.putInt(g.getCharter());
		writer.putString(cityName); //Shows City Name FUCK
		AbstractCharacter guildLeader;
		String guildLeaderName = "";
		if (g.isNPCGuild()){
			guildLeader = NPC.getFromCache(g.getGuildLeaderUUID());
			if (guildLeader != null)
				guildLeaderName = guildLeader.getName();
		}
			
		else{
			 guildLeader = PlayerCharacter.getFromCache(g.getGuildLeaderUUID());
			 if (guildLeader != null)
				 guildLeaderName = ((PlayerCharacter)guildLeader).getCombinedName();
		}
		
			writer.putString(guildLeaderName);
		writer.putString(pc.getName());
	
		writer.putDateTime(DateTime.now());
		writer.put((byte)1);
		writer.put((byte)1);
		writer.put((byte)1);
		writer.put((byte)0);
		writer.putInt(0);






		//	writer.put((byte)0);


		//		writer.putString(cityName);
		//		writer.putInt(10);
		//		writer.putInt(6);
		//		writer.putInt(10);
		//		writer.putString(cityName);
		//		writer.putString(pc.getFirstName());
		//		writer.putString("Nation Leader");
		//		writer.putDateTime(DateTime.now());
		//		writer.put((byte)1);
		//		writer.put((byte)1);
		//		writer.putInt(1);
		//		writer.putInt(this.objectType);
		//		writer.putInt(this.objectID);
		//		writer.putInt(0);
		//		writer.putInt(0);


	}
}

class GuildInfoMessageType5 extends GuildInfoMessageType {

	public GuildInfoMessageType5(AbstractGameObject ago, Guild g) {
		super(ago, g);
	}

	@Override
	void _serialize(ByteBufferWriter writer) {

		PlayerCharacter pc = null;

		if(ago.getObjectType().equals(GameObjectType.PlayerCharacter)) {
			pc = (PlayerCharacter) ago;
		}

		if(pc != null && g != null && g.getObjectUUID() != 0) {
			Guild n = g.getNation();
			if(n == null) {
				n = Guild.getErrantGuild();
			}
			
			writer.putInt(ago.getObjectType().ordinal());
			writer.putInt(ago.getObjectUUID());

			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);

			writer.putInt(1);
			writer.putInt(0);
			writer.putInt(0);

			writer.putString(g.getName());	//No Effect?
			writer.putInt(0);	//Pad

			GuildTag._serializeForDisplay(g.getGuildTag(),writer);	//Also a waste of space...

			writer.putInt(1);
			writer.putInt(0);
			writer.putInt(0);
			writer.putString(n.getName());	//No Effect?
			writer.putInt(0);
			writer.putInt(0);

			GuildTag._serializeForDisplay(n.getGuildTag(),writer);

			writer.putInt(1);

			writer.putString(g.getName());	//Guild Name
			writer.putString(g.getMotto());	//TODO Motto
			writer.putString(n.getName());	//Nation Name

			writer.putInt(GuildStatusController.getRank(pc.getGuildStatus()));	//Rank
			writer.putInt(GuildStatusController.getTitle(pc.getGuildStatus()));	//Title
			writer.putInt(g.getCharter());

			if(g.getNation().equals(Guild.getErrantGuild()))
				writer.putString("Errant");
			else
				writer.putString("City");
			writer.putString("Guild Leader");
			writer.putString("");	//Nation Leader - I believe

			DateTime now = DateTime.now();
			writer.putDateTime(now);

			writer.put((byte) 1);
			writer.put((byte) 1);
			writer.put((byte) 0);
			writer.put((byte) 0);

			GuildTag._serializeForDisplay(g.getGuildTag(),writer);
			GuildTag._serializeForDisplay(g.getNation().getGuildTag(),writer);
			writer.putInt(0);
		} else {
			writer.putLong(0);

			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);

			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);

			writer.put((byte) 0);

			GuildTag._serializeForDisplay(GuildTag.ERRANT,writer);
			GuildTag._serializeForDisplay(GuildTag.ERRANT,writer);

			writer.putInt(0);
		}
	}
}
