/*
HashSet<Integer> playerFriendSet = PlayerFriendsMap.get(playerUID);
			playerFriendSet.add(friendUID); * Copyright 2013 MagicBane Emulator Project
 * All Rights Reserved
 */
package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.PlayerCharacter;


public class AcceptFriendMsg extends ClientNetMsg {

	public String sourceName;
	public String friendName;

	/**
	 * This is the general purpose constructor.
	 */
	public AcceptFriendMsg(PlayerCharacter pc) {
		super(Protocol.FRIENDACCEPT);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public AcceptFriendMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.FRIENDACCEPT, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public AcceptFriendMsg(AcceptFriendMsg msg) {
		super(Protocol.FRIENDACCEPT);
	}

	
	
	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		//Do we even want to try this?
		this.sourceName = reader.getString(); //This is source name.. this friends list must never been updated since launch, not using IDS.
		this.friendName = reader.getString();
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
	writer.putString(this.sourceName);
	writer.putString(this.friendName);
	}
}
