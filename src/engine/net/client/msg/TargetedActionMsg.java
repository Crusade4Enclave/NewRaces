// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





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
import engine.objects.AbstractWorldObject;
import engine.objects.PlayerCharacter;

public class TargetedActionMsg extends ClientNetMsg {

	public static int un2cnt = 65;

	//attack animations
	//64: overhead RH swing											1h RH axe?
	//65: overhead LH swing
	//66: underhand RH uppercut
	//67: shoulder high RH swing
	//68: underhand RH swing
	//69: sweeping LH swing
	//70: overhead circle RH swing
	//71: RH across body and back swing
	//72: RH 1h overhead to 2h swing
	//73: RH overhead to cross body swing (bm?)
	//74: 2h low stab to cross body slash
	//75: unarmed punch												unarmed RH
	//76: unarmed RH punch LH punch
	//77: unarmed LH jab
	//78: unarmed LH jab, RH uppercut
	//79: kick
	//80: roundhouse kick
	//81: dagger RH stab											dagger RH
	//82: dagger LH stab
	//83: dagger slash
	//84: dagger hard stab
	//85: Polearm/staff overhead swing								Polearm, Staff
	//86: Polearm/staff side swing
	//87: Polearm/staff step into overhead swing
	//88: swinging RH stab
	//89: swinging LF stab
	//90: swinging RH stab (faster)
	//91: 1H slash across body and back (sword?)
	//92: spear stab												spear
	//93: spear low stab step into
	//94: spear swing leg stab
	//95: unarmed overhead swing RH, underhand swing LH
	//96: inverted weapon across body followed by roundhouse LH swing
	//97: step back followed by overhead roundhouse swing 2H
	//98: underhand slash (1h sword)								1H RH Sword
	//99: fast LH swing (dagger or sword?)
	//100: 1h swing RH (sword)										1h axe?
	//101: 1h overhead swing (club or sword)
	//102: fast 1h underhand swing (club or sword)
	//103: step into RH slash 1h
	//104: 1h overhead to cross body slash RH
	//105: 2h overhead swing (axe, hammer, sword)					2H Axe, Hammer, Sword
	//106: step into 2h swing
	//107: step int 2h overhead swing
	//108: step into 2h swing
	//109: bow draw and fire										bow
	//110: crossbow draw and fire									crossbow
	//115: throwing axe/hammer?
	//116: overhand throwing dagger?
	//117: throwing dagger

	private int sourceType;
	private int sourceID;
	private int targetType;
	private int targetID;
	private float locX;
	private float locZ;
	private int unknown01 = 14;
	private int unknown02 = 100; //source animation
	private float unknown03 = 1f;
	private float sourceStamina = 1f; // attackers stamina after attack
	private int unknown05 = 6;	//signify passive defense
	private int unknown06 = 10; //target animation
	private float newHealth = 10f; // health after damage
	private float damage = 0f; // damage, 0 for miss

	/**
	 * This is the general purpose constructor.
	 */
	public TargetedActionMsg(int sourceType, int sourceID, int targetType, int targetID, float locX, float locZ, float sourceStamina,
			float newHealth, float damage) {
		super(Protocol.TARGETEDACTION);
		this.sourceType = sourceType;
		this.sourceID = sourceID;
		this.targetType = targetType;
		this.targetID = targetID;
		this.sourceStamina = sourceStamina;
		this.locX = locX;
		this.locZ = locZ;
		this.newHealth = newHealth;
		this.damage = damage;
		//this.unknown02 = TargetedActionMsg.un2cnt;
	}

	/**
	 * This is a helper constructor. Designed to send an UPDATE only. Damage for
	 * this constructor is hard coded to ZERO.
	 */
	public TargetedActionMsg(PlayerCharacter pc) {
		super(Protocol.TARGETEDACTION);
		this.sourceType = pc.getObjectType().ordinal();
		this.sourceID = pc.getObjectUUID();
		this.targetType = pc.getObjectType().ordinal();
		this.targetID = pc.getObjectUUID();
		this.sourceStamina = pc.getStamina();
		this.locX = pc.getLoc().x;
		this.locZ = pc.getLoc().z;
		this.newHealth = pc.getCurrentHitpoints();
		this.damage = 0.0f;
	}
	
	public TargetedActionMsg(PlayerCharacter pc,int unknown06) {
		super(Protocol.TARGETEDACTION);
		this.sourceType = pc.getObjectType().ordinal();
		this.sourceID = pc.getObjectUUID();
		this.targetType = pc.getObjectType().ordinal();
		this.targetID = pc.getObjectUUID();
		this.sourceStamina = pc.getStamina();
		this.locX = pc.getLoc().x;
		this.locZ = pc.getLoc().z;
		this.newHealth = pc.getCurrentHitpoints();
		this.damage = 0.0f;
	}

	public TargetedActionMsg(AbstractCharacter source, AbstractWorldObject target, Float damage, int swingAnimation) {
		super(Protocol.TARGETEDACTION);
		if (source != null) {
			this.sourceType = source.getObjectType().ordinal();
			this.sourceID = source.getObjectUUID();
			this.sourceStamina = source.getStamina();
		} else {
			this.sourceType = 0;
			this.sourceID = 0;
			this.sourceStamina = 0;
		}
		if (target != null) {
			this.targetType = target.getObjectType().ordinal();
			this.targetID = target.getObjectUUID();
			this.locX = target.getLoc().x;
			this.locZ = target.getLoc().z;
			this.newHealth = target.getHealth();
			this.damage = damage;
			
		} else {
			this.targetType = 0;
			this.targetID = 0;
			this.locX = 50000f;
			this.locZ = -50000f;
			this.newHealth = 1f;
			this.damage = damage;
		}
		this.unknown02 = swingAnimation;
		//this.unknown02 = TargetedActionMsg.un2cnt;
	}
	
	public TargetedActionMsg(AbstractCharacter source, AbstractWorldObject target, Float damage, int swingAnimation, int dead) {
		super(Protocol.TARGETEDACTION);
		if (source != null) {
			this.sourceType = source.getObjectType().ordinal();
			this.sourceID = source.getObjectUUID();
			this.sourceStamina = source.getStamina();
		} else {
			this.sourceType = 0;
			this.sourceID = 0;
			this.sourceStamina = 0;
		}
		if (target != null) {
			this.targetType = target.getObjectType().ordinal();
			this.targetID = target.getObjectUUID();
			this.locX = target.getLoc().x;
			this.locZ = target.getLoc().z;
			this.newHealth = target.getCurrentHitpoints();
			this.damage = damage;
			this.unknown06 = dead;
			
		} else {
			this.targetType = 0;
			this.targetID = 0;
			this.locX = 50000f;
			this.locZ = -50000f;
			this.newHealth = 1f;
			this.damage = damage;
		}
		this.unknown02 = swingAnimation;
		//this.unknown02 = TargetedActionMsg.un2cnt;
	}

	/**
	 * Added in an attempt to have mobs fall over after death.
	 */
	
	public TargetedActionMsg(AbstractWorldObject target, boolean kill) {
			this(null, target, 0f, 75);
			if (kill){ 
				this.newHealth = -1f;
				this.sourceType = 0x101;
				this.sourceID = 0x101;
			}
	}
	
	/**
	 * This constructor can be used to create CombatMessages that indicate a block or parry has occurred.<br>
	 * <br>
	 * Set passiveAnimation to 21 for block.<br>
	 * Set passiveAnimation to 22 for parry.<br>
	 */
	public TargetedActionMsg(AbstractCharacter source, int swingAnimation, AbstractWorldObject target,  int passiveAnimation) {
		this(source, target, 0.0f, swingAnimation);
		this.unknown05 = passiveAnimation;
		this.unknown06 = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public TargetedActionMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.TARGETEDACTION, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.sourceType);
		writer.putInt(this.sourceID);
		writer.putInt(this.targetType);
		writer.putInt(this.targetID);
		writer.putFloat(this.locX);
		writer.putFloat(this.locZ);
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		writer.putFloat(this.unknown03);
		writer.putFloat(this.sourceStamina);
		writer.putInt(this.unknown05);
		// writer.putInt(this.unknown06);

		if (this.newHealth < 0)
			writer.putInt(55);
		else if(damage != 0 && this.unknown05 < 20)
			writer.putInt(60);
		else
			writer.putInt(this.unknown06);
		writer.putFloat(this.newHealth);
		writer.putFloat(this.damage);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.sourceType = reader.getInt();
		this.sourceID = reader.getInt();
		this.targetType = reader.getInt();
		this.targetID = reader.getInt();
		this.locX = reader.getFloat();
		this.locZ = reader.getFloat();
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getFloat();
		this.sourceStamina = reader.getFloat();
		this.unknown05 = reader.getInt();
		this.unknown06 = reader.getInt();
		this.newHealth = reader.getFloat();
		this.damage = reader.getFloat();
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

	public float getDamage() {
		return damage;
	}
}
