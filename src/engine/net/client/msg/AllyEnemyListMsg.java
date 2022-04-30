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
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Guild;
import engine.objects.GuildAlliances;
import engine.objects.GuildTag;
import engine.objects.PlayerCharacter;



public class AllyEnemyListMsg extends ClientNetMsg {


	private int guildID;


	public AllyEnemyListMsg(PlayerCharacter player) {
		super(Protocol.ALLYENEMYLIST);
		this.guildID = player.getGuildUUID();

	}

	public AllyEnemyListMsg() {
		super(Protocol.ALLYENEMYLIST);
	}


	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public AllyEnemyListMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ALLYENEMYLIST, origin, reader);
	}
	//CALL THIS AFTER SANITY CHECKS AND BEFORE UPDATING HEALTH/GOLD.


	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.getInt();
		this.guildID = reader.getInt();
	}


	// Precache and configure this message before we serialize it


	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		writer.putInt(GameObjectType.Guild.ordinal());
		writer.putInt(this.guildID);

		Guild guild = Guild.getGuild(this.guildID);

		writer.putInt(guild.getAllyList().size());

		for (Guild ally: guild.getAllyList()){
			writer.putInt(GameObjectType.Guild.ordinal());//guildType
			writer.putInt(ally.getObjectUUID());//GuildID
			writer.putString(ally.getName());
			GuildTag._serializeForDisplay(ally.getGuildTag(),writer);
			writer.put((byte)0);
		}


		writer.putInt(guild.getEnemyList().size());

		for (Guild enemy: guild.getEnemyList()){
			writer.putInt(GameObjectType.Guild.ordinal());//guildType
			writer.putInt(enemy.getObjectUUID());//GuildID
			writer.putString(enemy.getName());
			GuildTag._serializeForDisplay(enemy.getGuildTag(),writer);
			writer.put((byte)1);
		}


		writer.putInt(guild.getRecommendList().size());
		for (Guild recommended: guild.getRecommendList()){

			GuildAlliances guildAlliance = guild.guildAlliances.get(recommended.getObjectUUID());
			writer.putInt(GameObjectType.Guild.ordinal());//guildType
			writer.putInt(recommended.getObjectUUID());//GuildID
			writer.putString(recommended.getName());
			GuildTag._serializeForDisplay(recommended.getGuildTag(),writer);
			writer.put((byte)1); // ?
			writer.putString(guildAlliance.getRecommender()); // recommender name.
			writer.put((byte) (guildAlliance.isAlly()?1:0)); //ally 1 enemy 0

		}

		writer.put((byte)1);

	}

	public int getGuildID() {
		return guildID;
	}

}
