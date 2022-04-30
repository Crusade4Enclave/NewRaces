/*
HashSet<Integer> playerFriendSet = PlayerFriendsMap.get(playerUID);
			playerFriendSet.add(friendUID); * Copyright 2013 MagicBane Emulator Project
 * All Rights Reserved
 */
package engine.net.client.msg;


import engine.Enum.GameObjectType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.PlayerCharacter;


public class AddFriendMessage extends ClientNetMsg {

	public PlayerCharacter friend;

	/**
	 * This is the general purpose constructor.
	 */
	public AddFriendMessage(PlayerCharacter pc) {
		super(Protocol.ADDFRIEND);
		friend = pc;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public AddFriendMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ADDFRIEND, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public AddFriendMessage(AddFriendMessage msg) {
		super(Protocol.ADDFRIEND);
	}

	
	
	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	//message is serialize only, no need for deserialize.
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
	writer.putInt(GameObjectType.PlayerCharacter.ordinal());
	writer.putInt(friend.getObjectUUID());
	writer.putString(friend.getName());
	writer.putInt(0); //possibly available, busy, away?
	writer.putInt(0);
	}
}
