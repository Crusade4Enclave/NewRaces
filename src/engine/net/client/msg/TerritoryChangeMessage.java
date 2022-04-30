// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.PlayerCharacter;
import engine.objects.Realm;

public class TerritoryChangeMessage extends ClientNetMsg {



	private Realm realm;
	private PlayerCharacter realmOwner;



	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TerritoryChangeMessage(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.TERRITORYCHANGE, origin, reader);
	}
	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {


	}

	public TerritoryChangeMessage() {
		super(Protocol.TERRITORYCHANGE);
	}

	public TerritoryChangeMessage(PlayerCharacter guildLeader, Realm realm) {
		super(Protocol.TERRITORYCHANGE);

		this.realm = realm;
		this.realmOwner = guildLeader;
	}
	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		writer.putString(realm.getRealmName());
		if(this.realmOwner != null){
			writer.putString(this.realmOwner.getCombinedName());
			writer.putInt(PlayerCharacter.GetPlayerRealmTitle(this.realmOwner));
			writer.putInt(1);
			writer.put((byte)1);
			writer.put((byte)1);
			writer.putInt(realm.getCharterType());
			if (this.realmOwner != null && this.realmOwner.getGuild() != null)
				writer.putString(this.realmOwner.getGuild().getName());
			else
				writer.putString("None");
			writer.put((byte)0);
		}else{
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte)1);
			writer.put((byte)1);
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte)0);
		}



	}




}
