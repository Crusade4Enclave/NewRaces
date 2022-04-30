// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.*;

public class LoadCharacterMsg extends ClientNetMsg {

	private AbstractCharacter absChar;
	private Corpse corpse;
	private boolean hideNonAscii;

	/**
	 * This is the general purpose constructor.
	 */
	public LoadCharacterMsg(AbstractCharacter ch, boolean laln) {
		super(Protocol.LOADCHARACTER);
		this.absChar = ch;
		this.corpse = null;
		this.hideNonAscii = laln;
	}

	/**
	 * This is the general purpose constructor.
	 */
	public LoadCharacterMsg(Corpse corpse, boolean laln) {
		super(Protocol.LOADCHARACTER);
		this.corpse = corpse;
		this.absChar = null;
		this.hideNonAscii = laln;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public LoadCharacterMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.LOADCHARACTER, origin, reader);
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
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		if (absChar != null && absChar.getObjectType() == GameObjectType.NPC) {
			NPC npc = (NPC)absChar;



			if (npc.getBuilding() != null){
				writer.putInt(npc.getBuildingLevel());
				writer.putInt(npc.getBuildingFloor());
			}else{
				writer.putInt(-1);
				writer.putInt(-1);
			}


		} else if (absChar != null) {
		
		if (absChar.getObjectType().equals(GameObjectType.PlayerCharacter)){
			Regions region = absChar.getRegion();
			
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
			//TODO below is Mob Region Serialization, not implemented. default to -1, which is ground.
		}else{
			writer.putInt(-1);
			writer.putInt(-1);
		}
			
			
			

		} else if (corpse != null){
			writer.putInt(-1);
			writer.putInt(-1);
		}
		if (corpse != null) {
			writer.putInt(Float.floatToIntBits(corpse.getLoc().getX()));
			writer.putInt(Float.floatToIntBits(corpse.getLoc().getY()));
			writer.putInt(Float.floatToIntBits(corpse.getLoc().getZ()));
			writer.put((byte) 0);
		} else if (absChar != null) {

			writer.putFloat(absChar.getLoc().getX());
			writer.putFloat(absChar.getLoc().getY());
			writer.putFloat(absChar.getLoc().getZ());

			if (absChar.isMoving()) {
				writer.put((byte) 1);
				writer.putFloat(absChar.getEndLoc().x);
				writer.putFloat(absChar.getEndLoc().y);
				writer.putFloat(absChar.getEndLoc().z);
			} else
				writer.put((byte) 0);
		} else {
			writer.put((byte) 0);
		}

		if (corpse != null)
			Corpse._serializeForClientMsg(corpse, writer, this.hideNonAscii);
		else if (absChar != null)
			AbstractCharacter.serializeForClientMsgOtherPlayer(this.absChar,writer, this.hideNonAscii);
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
		// TODO finish deserialization impl
	}

	public AbstractCharacter getChar() {
		return this.absChar;
	}

	public void setChar(AbstractCharacter value) {
		this.absChar = value;
	}

	public void setCorpse(Corpse value) {
		this.corpse = value;
	}
}
