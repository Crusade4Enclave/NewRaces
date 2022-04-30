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


public class UpdateClientAlliancesMsg extends ClientNetMsg {


	private int guildID;


	public UpdateClientAlliancesMsg(Guild guild) {
		super(Protocol.UPDATECLIENTALLIANCES);
		this.guildID = guild.getObjectUUID();

	}

	public UpdateClientAlliancesMsg() {
		super(Protocol.UPDATECLIENTALLIANCES);
	}


	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UpdateClientAlliancesMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.UPDATECLIENTALLIANCES, origin, reader);
	}
	//CALL THIS AFTER SANITY CHECKS AND BEFORE UPDATING HEALTH/GOLD.


	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.get();
	}


	// Precache and configure this message before we serialize it


	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		Guild guild = Guild.getGuild(this.guildID);

		writer.putInt(guild.getAllyList().size());

		for (Guild allies: guild.getAllyList()){
			writer.putInt(GameObjectType.Guild.ordinal());
			writer.putInt(allies.getObjectUUID());
		}

		writer.putInt(guild.getEnemyList().size());
		for (Guild enemies: guild.getEnemyList()){
			writer.putInt(GameObjectType.Guild.ordinal());
			writer.putInt(enemies.getObjectUUID());
		}
		writer.putInt(0);
		writer.putInt(0);
		writer.put((byte)1);
	}

	public int getGuildID() {
		return guildID;
	}

}
