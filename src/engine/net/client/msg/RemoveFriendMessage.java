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


public class RemoveFriendMessage extends ClientNetMsg {

	public int friendID;

	/**
	 * This is the general purpose constructor.
	 */
	public RemoveFriendMessage(int friendID) {
		super(Protocol.REMOVEFRIEND);
		this.friendID = friendID;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public RemoveFriendMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.REMOVEFRIEND, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public RemoveFriendMessage(RemoveFriendMessage msg) {
		super(Protocol.REMOVEFRIEND);
	}
	
	

	
	
	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		//Do we even want to try this?
		reader.getInt();
		this.friendID = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
	writer.putInt(GameObjectType.PlayerCharacter.ordinal());
	writer.putInt(this.friendID);
	}
}
