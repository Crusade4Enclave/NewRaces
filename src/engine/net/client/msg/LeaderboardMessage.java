// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.ShrineType;
import engine.exception.SerializationException;
import engine.gameManager.BuildingManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Building;
import engine.objects.Guild;
import engine.objects.GuildTag;
import engine.objects.Shrine;

public class LeaderboardMessage extends ClientNetMsg {

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public LeaderboardMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.LEADERBOARD, origin, reader);
	}

	public LeaderboardMessage() {
		super(Protocol.LEADERBOARD);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {

	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		writer.putInt(ShrineType.values().length);//??

		for (ShrineType shrineType : ShrineType.values()) {
			writer.putInt(shrineType.ordinal());
			writer.putInt(shrineType.getShrinesCopy().size());
			int i = 0;
			for (Shrine shrine : shrineType.getShrinesCopy()) {
				i++;
				writer.putInt(shrine.getFavors());
				Building shrineBuilding = BuildingManager.getBuilding(shrine.getBuildingID());
				if (shrineBuilding != null) {
					Guild shrineGuild = shrineBuilding.getGuild();
					if (shrineGuild != null) {
						writer.putInt(shrineGuild.getObjectType().ordinal());
						writer.putInt(shrineGuild.getObjectUUID());

						GuildTag._serializeForDisplay(shrineGuild.getGuildTag(),writer);
						writer.putString(shrineGuild.getName());
					} else {
						writer.putLong(0);
						writer.putInt(16);
						writer.putInt(16);
						writer.putInt(16);
						writer.putInt(0);
						writer.putInt(0);
						writer.putString("");
					}
				}else{
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
			}
			writer.putString(shrineType.name());
		}

	}
	@Override
	protected int getPowerOfTwoBufferSize() {
		return (14); // 2^14 == 16384
	}

}
