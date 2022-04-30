// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.City;
import engine.objects.PlayerCharacter;

import java.util.ArrayList;


public class TeleportRepledgeListMsg extends ClientNetMsg {

	private PlayerCharacter player;
	private boolean isTeleport;
    ArrayList<City> cities;

	/**
	 * This is the general purpose constructor.
	 */
	public TeleportRepledgeListMsg(PlayerCharacter player, boolean isTeleport) {
		super(Protocol.SENDCITYENTRY);
		this.player = player;
		this.isTeleport = isTeleport;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TeleportRepledgeListMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.SENDCITYENTRY, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public TeleportRepledgeListMsg(TeleportRepledgeListMsg msg) {
		super(Protocol.SENDCITYENTRY);
        this.player = msg.player;
        this.isTeleport = msg.isTeleport;
	}

	/**
	 * @see AbstractNetMsg#getPowerOfTwoBufferSize()
	 */
	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return (16);
	}
	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		//Do we even want to try this?
	}

    // Pre-caches and configures message so data is avaiable
    // when we serialize.

    public void configure() {

        if (isTeleport)
            cities = City.getCitiesToTeleportTo(player);
        else
            cities = City.getCitiesToRepledgeTo(player);
    }

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		if (isTeleport)
			writer.putInt(2); //teleport?
		else
			writer.putInt(1); //repledge?

		for (int i=0;i<3;i++)
			writer.putInt(0);

		writer.putInt(cities.size());

		for (City city : cities)
			City.serializeForClientMsg(city,writer);
	}

	public PlayerCharacter getPlayer() {
		return this.player;
	}

	public boolean isTeleport() {
		return this.isTeleport;
	}

}
