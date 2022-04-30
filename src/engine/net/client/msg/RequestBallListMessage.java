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


public class RequestBallListMessage extends ClientNetMsg {

	public int playerID;
	public String errorMessage;
	public int msgType;
	
	public static int REQUEST = 0;

	/**
	 * This is the general purpose constructor.
	 */
	public RequestBallListMessage(PlayerCharacter pc) {
		super(Protocol.REQUESTBALLLIST);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public RequestBallListMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.REQUESTBALLLIST, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public RequestBallListMessage(RequestBallListMessage msg) {
		super(Protocol.REQUESTBALLLIST);
	}

	
	
	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.getInt();
		this.playerID = reader.getInt();
		this.msgType = reader.getInt();
		this.errorMessage = reader.getString();
		
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(GameObjectType.PlayerCharacter.ordinal());
		writer.putInt(this.playerID);
		writer.putInt(this.msgType);
		writer.putString(this.errorMessage);
	}
}
