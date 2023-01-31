// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.ai.StaticMobActions;
import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.*;

import java.util.HashSet;


public class TrackWindowMsg extends ClientNetMsg {

	private int powerToken;
	private PlayerCharacter source = null;
	private HashSet<AbstractCharacter> characters = new HashSet<>();

	/**
	 * This is the general purpose constructor.
	 */
	public TrackWindowMsg(int powerToken, HashSet<AbstractCharacter> characters) {
		super(Protocol.ARCTRACKINGLIST);
		this.powerToken = powerToken;
		this.characters = characters;
	}

	public TrackWindowMsg(TrackWindowMsg trackWindowMsg) {
		super(Protocol.ARCTRACKINGLIST);
		this.powerToken = trackWindowMsg.powerToken;
		this.source = trackWindowMsg.source;
		this.characters = trackWindowMsg.characters;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TrackWindowMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ARCTRACKINGLIST, origin, reader);
	}

	/**
	 * @see AbstractNetMsg#getPowerOfTwoBufferSize()
	 */
	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return (13);
	}
	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.powerToken);
		writer.putInt(characters.size());
		for (AbstractCharacter ac : characters) {
			boolean isGroup = false;
			if (this.source != null && ac.getObjectType().equals(GameObjectType.PlayerCharacter)) {
				if (Group.sameGroup((PlayerCharacter)ac, this.source))
					isGroup = true;
			}
			AbstractCharacter.serializeForTrack(ac,writer, isGroup);
		}
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.powerToken = reader.getInt();

		int size = reader.getInt();
		for (int i=0;i<size;i++) {
			int objectType = reader.getInt();
			int objectID = reader.getInt();
			this.source = PlayerCharacter.getFromCache(objectID);
			reader.getString(); //name
			reader.get(); //always 00?
			reader.getInt(); //guildObjectType
			reader.getInt(); //guildID
			reader.get(); //always 01?
			for (int j=0;j<5;j++)
				reader.getInt(); //guild tags
			reader.getInt(); //nation ObjectType
			reader.getInt(); //nation ID
			reader.get(); //always 01?
			for (int j=0;j<5;j++)
				reader.getInt(); //nation tags

			//Get the Character from it's Object Type and ID
			AbstractCharacter ac = null;
			if (objectType == GameObjectType.PlayerCharacter.ordinal())
				ac = PlayerCharacter.getFromCache(objectID);
			else if (objectType == GameObjectType.NPC.ordinal())
				ac = NPC.getFromCache(objectID);
			else if (objectType == GameObjectType.Mob.ordinal())
				ac = StaticMobActions.getFromCache(objectID);

			//If found, add to message list
			if (ac != null)
				characters.add(ac);
		}
	}

	public int getPowerToken() {
		return this.powerToken;
	}

	public HashSet<AbstractCharacter> getCharacters() {
		return this.characters;
	}

	public void setPowerToken(int value) {
		this.powerToken = value;
	}

	public void setCharacters(HashSet<AbstractCharacter> value) {
		this.characters = value;
	}

	public void addCharacter(PlayerCharacter value) {
		if (value != null)
			this.characters.add(value);
	}

	public void clearChracters() {
		this.characters.clear();
	}

	public void setSource(PlayerCharacter value) {
		this.source = value;
	}
}
