/*
HashSet<Integer> playerFriendSet = PlayerFriendsMap.get(playerUID);
			playerFriendSet.add(friendUID); * Copyright 2013 MagicBane Emulator Project
 * All Rights Reserved
 */
package engine.net.client.msg;


import engine.Enum.GameObjectType;
import engine.gameManager.SessionManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.PlayerCharacter;


public class UpdateFriendStatusMessage extends ClientNetMsg {
	
	public int statusType;
	public PlayerCharacter player;
	public boolean online = true;



	/**
	 * This is the general purpose constructor.
	 */
	public UpdateFriendStatusMessage(PlayerCharacter player) {
		super(Protocol.UPDATEFRIENDSTATUS);
		this.player = player;
		this.online = SessionManager.getPlayerCharacterByID(player.getObjectUUID()) != null ? true : false;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UpdateFriendStatusMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.UPDATEFRIENDSTATUS, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public UpdateFriendStatusMessage(UpdateFriendStatusMessage msg) {
		super(Protocol.UPDATEFRIENDSTATUS);
	}

	
	
	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	//message is serialize only, no need for deserialize.
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		this.statusType = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
	writer.putInt(GameObjectType.PlayerCharacter.ordinal());
	writer.putInt(player.getObjectUUID());
	writer.putString(player.getCombinedName());
	writer.putInt(online ? 0 : 1);
	writer.putInt(player.friendStatus.ordinal());
	}
}
