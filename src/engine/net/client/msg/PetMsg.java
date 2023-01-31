// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.ai.StaticMobActions;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Mob;

public class PetMsg extends ClientNetMsg {

	private int type; //5 or 6
	private Mob pet;

	/**
	 * This is the general purpose constructor.
	 */
	public PetMsg(int type, Mob pet) {
		super(Protocol.PET);
		if (this.type != 6)
			this.type = 5;
		this.pet = pet;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public PetMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.PET, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.type);

		if (this.pet != null) {
			writer.putInt(pet.getObjectType().ordinal());
			writer.putInt(pet.getObjectUUID());
		} else {
			writer.putInt(0);
			writer.putInt(0);
		}

		if (type == 6) {
			writer.putInt(0);
		} else if (type == 5) {
			if (pet != null){
				writer.putInt((int)(pet.getCurrentHitpoints() / pet.getHealthMax())); //suspect %health left
				writer.putInt((int)(pet.getMana() / pet.getManaMax())); //suspect %mana left
				writer.putInt((int)(pet.getStamina() / pet.getStaminaMax())); //suspect %stamina left
				writer.putString(pet.getName());
				writer.putInt(0);
				writer.put((byte)0);
			}else{
				writer.putInt(0); //suspect %health left
				writer.putInt(0); //suspect %mana left
				writer.putInt(0); //suspect %stamina left
				writer.putString("No Pet");
				writer.putInt(0);
				writer.put((byte)0);
			}
			
		}
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.type = reader.getInt();
		reader.getInt();
		int petID = reader.getInt();
		this.pet = StaticMobActions.getFromCache(petID);
		if (this.type == 5) {
			reader.getInt();
		} else if (this.type == 6) {
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getString();
			reader.getInt();
			reader.get();
		}
	}

	public int getType() {
		return this.type;
	}

	public Mob getPet() {
		return this.pet;
	}

	public void setType(int value) {
		this.type = value;
	}

	public void setPet(Mob value) {
		this.pet = value;
	}
}
