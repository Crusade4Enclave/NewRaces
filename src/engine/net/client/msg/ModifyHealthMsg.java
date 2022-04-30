// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.AbstractCharacter;
import engine.objects.Building;

public class ModifyHealthMsg extends ClientNetMsg {

	private int trains;
	private int unknownID; //effectID
	private int sourceType;
	private int sourceID;
	private int targetType;
	private int targetID;
	private int omitFromChat = 0; //1 heal, 0 hurt?
	private int unknown03 = 0; //0=normalCast, 1to4=powerFailed, 5=targetIsImmune, 6=targetResisted
	private int unknown04 = -1;
	private int unknown05 = 0;
	private byte unknownByte = (byte) 0; //0
	private int powerID;
	private String powerName;
	private float health;
	private float healthMod;
	private float mana;
	private float manaMod;
	private float stamina;
	private float staminaMod;

	/**
	 * This is the general purpose constructor.
	 */
	public ModifyHealthMsg(AbstractCharacter source, Building target, float healthMod, float manaMod, float staminaMod, int powerID, String powerName, int trains, int effectID) {
		super(Protocol.POWERACTIONDD);
		if (source != null) {
			this.sourceType = source.getObjectType().ordinal();
			this.sourceID = source.getObjectUUID();
		} else {
			this.sourceType = 0;
			this.sourceID = 0;
		}
		if (target != null) {
			this.targetType = target.getObjectType().ordinal();
			this.targetID = target.getObjectUUID();
			this.health = target.getCurrentHitpoints();
			this.healthMod = healthMod;
			this.mana = 0;
			this.manaMod = 0;
			this.stamina = 0;
			this.staminaMod = 0;
		} else {
			this.targetType = 0;
			this.targetID = 0;
			this.health = 0;
			this.healthMod = 0;
			this.mana = 0;
			this.manaMod = 0;
			this.stamina = 0;
			this.staminaMod = 0;
		}
		this.unknownID = effectID;
		this.trains = trains;
		this.powerID = powerID;
		this.powerName = powerName;

		this.omitFromChat = 0;
	}

	public ModifyHealthMsg(AbstractCharacter source, AbstractCharacter target, float healthMod, float manaMod, float staminaMod, int powerID, String powerName, int trains, int effectID) {
		super(Protocol.POWERACTIONDD);
		if (source != null) {
			this.sourceType = source.getObjectType().ordinal();
			this.sourceID = source.getObjectUUID();
		} else {
			this.sourceType = 0;
			this.sourceID = 0;
		}
		if (target != null) {
			this.targetType = target.getObjectType().ordinal();
			this.targetID = target.getObjectUUID();
			this.health = target.getCurrentHitpoints();
			this.healthMod = healthMod;
			this.mana = target.getMana();
			this.manaMod = manaMod;
			this.stamina = target.getStamina();
			this.staminaMod = staminaMod;
		} else {
			this.targetType = 0;
			this.targetID = 0;
			this.health = 0;
			this.healthMod = 0;
			this.mana = 0;
			this.manaMod = 0;
			this.stamina = 0;
			this.staminaMod = 0;
		}
		this.unknownID = effectID;
		this.trains = trains;
		this.powerID = powerID;
		this.powerName = powerName;

		this.omitFromChat = 0;
	}

	//called for kills
	public ModifyHealthMsg(AbstractCharacter source, AbstractCharacter target, int powerID, String powerName, int trains, int effectID) {
		super(Protocol.POWERACTIONDD);
		if (source != null) {
			this.sourceType = source.getObjectType().ordinal();
			this.sourceID = source.getObjectUUID();
		} else {
			this.sourceType = 0;
			this.sourceID = 0;
		}
		if (target != null) {
			this.targetType = target.getObjectType().ordinal();
			this.targetID = target.getObjectUUID();
			this.mana = target.getMana();
			this.stamina = target.getStamina();
		} else {
			this.targetType = 0;
			this.targetID = 0;
			this.mana = 0f;
			this.stamina = 0f;
		}
		this.health = -50f;
		this.healthMod = 0f;
		this.manaMod = 0f;
		this.staminaMod = 0f;
		this.omitFromChat = 0;
		this.unknownID = effectID;
		this.trains = trains;
		this.powerID = powerID;
		this.powerName = powerName;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public ModifyHealthMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.POWERACTIONDD, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.trains);
		writer.putInt(this.unknownID);
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);
		writer.putInt(this.targetType);
		writer.putInt(this.targetID);
		writer.putInt(this.omitFromChat);
		writer.putInt(this.unknown03);
		writer.putInt(this.unknown04);
		writer.putInt(this.unknown05);
		writer.put(this.unknownByte);
		writer.putInt(this.powerID);
		writer.putString(this.powerName);
		writer.putFloat(this.health);
		writer.putFloat(this.healthMod);
		writer.putFloat(this.mana);
		writer.putFloat(this.manaMod);
		writer.putFloat(this.stamina);
		writer.putFloat(this.staminaMod);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.trains = reader.getInt();
		this.unknownID = reader.getInt();
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
		this.targetType = reader.getInt();
		this.targetID = reader.getInt();
		this.omitFromChat = reader.getInt();
		this.unknown03 = reader.getInt();
		this.unknown04 = reader.getInt();
		this.unknown05 = reader.getInt();
		this.unknownByte = reader.get();
		this.powerID = reader.getInt();
		this.powerName = reader.getString();
		this.health = reader.getFloat();
		this.healthMod = reader.getFloat();
		this.mana = reader.getFloat();
		this.manaMod = reader.getFloat();
		this.stamina = reader.getFloat();
		this.staminaMod = reader.getFloat();
	}

	/**
	 * @return the sourceType
	 */
	public int getSourceType() {
		return sourceType;
	}

	/**
	 * @return the sourceID
	 */
	public int getSourceID() {
		return sourceID;
	}

	/**
	 * @return the targetType
	 */
	public int getTargetType() {
		return targetType;
	}

	public float getHealthMod() {
		return healthMod;
	}

	public float getManaMod() {
		return manaMod;
	}

	public float getStaminaMod() {
		return manaMod;
	}

	/**
	 * @return the targetID
	 */
	public int getTargetID() {
		return targetID;
	}

	public void setSourceType(int value) {
		this.sourceType = value;
	}

	public void setSourceID(int value) {
		this.sourceID = value;
	}

	public void setTargetType(int value) {
		this.targetType = value;
	}

	public void setTargetID(int value) {
		this.targetID = value;
	}

	public void setOmitFromChat(int value) {
		this.omitFromChat = value;
	}

	public void setUnknown03(int value) {
		this.unknown03 = value;
	}
}
