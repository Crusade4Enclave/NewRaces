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
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Building;
import engine.objects.PlayerCharacter;
import engine.objects.Regions;
import engine.session.Session;
import org.pmw.tinylog.Logger;

public class SendOwnPlayerMsg extends ClientNetMsg {

	private PlayerCharacter ch;

	/**
	 * This is the general purpose constructor.
	 *
	 * @param s
	 *            Session from which the PlayerCharacter is obtained
	 */
	public SendOwnPlayerMsg(Session s) {
		super(Protocol.PLAYERDATA);

		// Get all the character records for this account
		ch = s.getPlayerCharacter();
	}

	/**
	 * This is the general purpose constructor.
	 *
	 * @param pc
	 *            Playercharacter
	 */
	public SendOwnPlayerMsg(PlayerCharacter pc) {
		super(Protocol.PLAYERDATA);
		this.ch = pc;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public SendOwnPlayerMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.PLAYERDATA, origin, reader);
	}

	/**
	 * @see AbstractNetMsg#getPowerOfTwoBufferSize()
	 */
	@Override
	protected int getPowerOfTwoBufferSize() {
		//Larger size for historically larger opcodes
		return (17); // 2^17 == 131,072
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		
		Regions region = this.ch.getRegion();
		//region loading seralization. serialzes building level and floor. -1 = not in building.
		if (region == null){
			writer.putInt(-1);
			writer.putInt(-1);
		}else{
			Building regionBuilding = Regions.GetBuildingForRegion(region);
			if (regionBuilding == null){
				writer.putInt(-1);
				writer.putInt(-1);
			}else{
				writer.putInt(region.getLevel());
				writer.putInt(region.getRoom());
			}
		}
		writer.putVector3f(ch.getLoc());
		try {
			PlayerCharacter.serializeForClientMsgFull(this.ch,writer);
		} catch (SerializationException e) {
			Logger.error(e);
		}
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {

		int unknown1 = reader.getInt();
		int unknown2 = reader.getInt();

		int unknown3 = reader.getInt();
		int unknown4 = reader.getInt();

		int unknown5 = reader.getInt();
		int unknown6 = reader.getInt();
		int unknown7 = reader.getInt();

		// TODO finish deserialization implementation.
	}

	public PlayerCharacter getChar() {
		return this.ch;
	}
}
