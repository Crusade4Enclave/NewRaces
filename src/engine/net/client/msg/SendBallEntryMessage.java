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


public class SendBallEntryMessage extends ClientNetMsg {

	public int playerID;
	public String description;
	public int msgType;
	public int ballColor;
	public static final int ADDBALL = 4;
	public static final int WHITEBALL = 2;
	public static final int BLACKBALL = 1;

	/**
	 * This is the general purpose constructor.
	 */
	public SendBallEntryMessage(PlayerCharacter pc) {
		super(Protocol.SENDBALLENTRY);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public SendBallEntryMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.SENDBALLENTRY, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public SendBallEntryMessage(SendBallEntryMessage msg) {
		super(Protocol.SENDBALLENTRY);
	}

	
	
	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.msgType = reader.getInt();
		
		switch (this.msgType){
		case ADDBALL:
			this.readAddBall(reader);
			break;
			default:
				break;
		}
		
	}
	
	public void readAddBall(ByteBufferReader reader){
		reader.getInt();
		this.playerID = reader.getInt();
		
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		
		reader.getInt(); // 1 ?
		
		reader.getInt();
		reader.getInt();
		
		reader.getInt(); // source player type
		reader.getInt(); // source player ID
		
		reader.getInt(); // targetType
		reader.getInt(); // targetID (same as this.playerID)
		this.description = reader.getString();
		reader.get();
		reader.get();
		reader.getInt(); // 100, max ball mark
		reader.get();
		reader.get();
		reader.get();
		this.ballColor = reader.getInt();
		reader.get();
		
		
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
	}
}
