// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.guild;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.gameManager.SessionManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.Guild;
import engine.objects.GuildHistory;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

import java.util.ArrayList;


public class GuildListMsg extends ClientNetMsg {

	private GuildListMessageType glm;

	/**
	 * Type 1 Constructor	- Guild Roster
	 */
	public GuildListMsg(Guild g) {
		super(Protocol.SENDMEMBERENTRY);
		this.glm = new GuildListMessageType1(g);
	}

	/**
	 * Type 4 Constructor  - Guild History
	 */
	public GuildListMsg(PlayerCharacter pc) {
		super(Protocol.SENDMEMBERENTRY);
		this.glm = new GuildListMessageType2(pc);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public GuildListMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.SENDMEMBERENTRY, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {

		//TODO Find Default and null check this
		this.glm._serialize(writer);
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		/*
		 *
		 * The Server should never receive this message directly.
		 * Instances in recordings will need to be decoded by hand,
		 * converting from our format to the standard format will
		 * cause more problems than its worth to fix.
		 *
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.get();
		this.unknown04 = reader.getInt();
		this.unknown05 = reader.get();
		this.unknown06 = reader.getInt();
		int size = reader.getInt();
		for (int i = 0; i < size; i++) {
			GuildTableList gt = new GuildTableList();
			reader.getInt(); // Player Character ID Type
			gt.setUUID(reader.getInt());
			gt.setName(reader.getString());
			gt.setActionType(reader.get());
			gt.setGuildTitle(reader.getInt());
			gt.setGuildRank(reader.getInt());
			gt.setUnknown02(reader.getInt());
			gt.setUnknown03(reader.getInt());
			gt.setUnknown04(reader.getInt());
			gt.setUnknown05(reader.getInt());
			gt.setUnknown06(reader.getInt());
			gt.setUnknown07(reader.getInt());
		}
		 */
	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return 15;
	}
}

abstract class GuildListMessageType {
	public ArrayList<Guild> history = new ArrayList<>();

	abstract void _serialize(ByteBufferWriter writer);
}

class GuildListMessageType1 extends GuildListMessageType {

	private Guild g;

	public GuildListMessageType1(Guild g) {
		this.g = g;
	}

	@Override
	void _serialize(ByteBufferWriter writer) {
		Enum.GuildType gt = Enum.GuildType.getGuildTypeFromInt(g.getCharter());

		writer.putInt(1);
		writer.putInt(gt.ordinal());	//Charter Type
		writer.putInt(1);	//Always 1?
		writer.put((byte) 1);
		writer.put((byte) 0);
		writer.putInt(gt.getNumberOfRanks());	//Number of Ranks
		
		
		ArrayList<PlayerCharacter> guildRoster = Guild.GuildRoster(g);
		writer.putInt(guildRoster.size() + g.getBanishList().size());

		// Send guild list of each player
		for (PlayerCharacter player : guildRoster) {
			
			byte isActive = SessionManager.getPlayerCharacterByID(player.getObjectUUID()) != null ? (byte)1 : (byte)0;
			writer.putInt(Enum.GameObjectType.PlayerCharacter.ordinal());
			writer.putInt(player.getObjectUUID());
			writer.putString(player.getCombinedName());
			writer.put(isActive);	//Active?
			writer.putInt(GuildStatusController.getTitle(player.getGuildStatus()));
			writer.putInt(GuildStatusController.getRank(player.getGuildStatus()));
			writer.putInt(0); // 1/2 has no effect
			writer.putInt(0); // 1 has no effect
			writer.putInt(0); // 1 has no effect
			writer.putInt(0); // 1 has no effect
			writer.putInt(GuildStatusController.getRank(player.getGuildStatus()));
			writer.putInt(0); // window failed to open when set to 1
		}
		
		for (PlayerCharacter banished : g.getBanishList()){
			byte isActive = SessionManager.getPlayerCharacterByID(banished.getObjectUUID()) != null ? (byte)1 : (byte)0;
			writer.putInt(Enum.GameObjectType.PlayerCharacter.ordinal());
			writer.putInt(banished.getObjectUUID());
			writer.putString(banished.getCombinedName());
			writer.put(isActive);	//Active?
			writer.putInt(GuildStatusController.getTitle(banished.getGuildStatus()));
			writer.putInt(3);
			writer.putInt(0); // 1/2 has no effect
			writer.putInt(0); // 1 has no effect
			writer.putInt(0); // 1 has no effect
			writer.putInt(0); // 1 has no effect
			writer.putInt(3);
			writer.putInt(0); // window failed to open when set to 1
		}
	}
}

class GuildListMessageType2 extends GuildListMessageType {

	private PlayerCharacter pc;

	public GuildListMessageType2(PlayerCharacter pc) {
		this.pc = pc;
	}

	@Override
	void _serialize(ByteBufferWriter writer) {

		Guild g = pc.getGuild();

		writer.putInt(4);

		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);

		writer.put((byte) 0);
		writer.put((byte) 0);
		writer.putInt(1);
		writer.putInt(pc.getObjectType().ordinal());
		writer.putInt(pc.getObjectUUID());
		writer.putString(pc.getCombinedName());

		writer.put((byte) 1);
		writer.putInt(GuildStatusController.getTitle(pc.getGuildStatus()));	//Title Maybe?
		writer.putInt(GuildStatusController.getRank(pc.getGuildStatus()));	//Rank?

		writer.putInt(pc.getRaceToken());	//race token
		writer.putInt(pc.getBaseClassToken());	//class token

		writer.putInt(2);	//PAD
		writer.putInt(pc.getLevel());
		writer.putInt(g.getCharter());

		//TODO Get Guild History from the DB
		//ArrayList<GuildHistory> history = DbManager.GuildQueries.GET_GUILD_HISTORY_OF_PLAYER((int)pc.getPlayerUUID());
		writer.putInt(pc.getGuildHistory().size());	//Number of Entries

		for(GuildHistory guildHistory : pc.getGuildHistory()) {
			writer.putInt(guildHistory.getHistoryType().getType());
			writer.putInt(GameObjectType.Guild.ordinal());
			writer.putInt((int) guildHistory.getGuildID());
			writer.putString(guildHistory.getGuildName());
			writer.putInt(0);
			writer.putDateTime(guildHistory.getTime());
		}
	}

}
