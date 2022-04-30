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

import engine.Enum.GameObjectType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.AbstractCharacter;
import engine.objects.PlayerCharacter;

public class UpdateStateMsg extends ClientNetMsg {

	private int charType;
	private int charUUID;
	private int activity; //1 dead,2 unconscious, 3 Sleeping,  4 resting,5 casting,6 IDLE,7 casting? , 8 nothing 9 unknown, 
	private int speed; // 1 low, 2 high (walk,run)
	private int aware; // 1 low, 2 high (combat off,combat on)

	private int mode; // 0 unknown, 1 water, 2 ground, 3 flight.
	private int fighting; // 1 disengaged, 2 engaged.
	private int headlights; // LFGroup/LFGuild/Recruiting Icons

	/**
	 * This is the general purpose constructor.
	 */
	public UpdateStateMsg() {
		super(Protocol.UPDATESTATE);
		this.fighting = 1;
		this.headlights = 0;
	}

	public UpdateStateMsg(AbstractCharacter ac) {
		super(Protocol.UPDATESTATE);
		this.fighting = 1;
		this.headlights = 0;
		setPlayer(ac);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public UpdateStateMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.UPDATESTATE, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.charType);
		writer.putInt(this.charUUID);
		writer.putInt(this.activity);
		writer.putInt(this.speed);
		writer.putInt(this.aware);
		writer.putInt(this.mode);
		writer.putInt(this.fighting);
		writer.putInt(this.headlights);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.charType = reader.getInt();
		this.charUUID = reader.getInt();
		this.activity = reader.getInt();
		this.speed = reader.getInt();
		this.aware = reader.getInt();

		this.mode = reader.getInt();
		this.fighting = reader.getInt();
		this.headlights = reader.getInt();
	}

	/**
	 * Sets this Msg's charUUID, sitStand, walkRun and combatToggle parameters
	 * based on supplied AbstractCharacter
	 *
	 * @param ac
	 */
	public final void setPlayer(AbstractCharacter ac) {

		PlayerCharacter player;

		this.charType = ac.getObjectType().ordinal();
		this.charUUID = ac.getObjectUUID();
		this.activity = ac.getIsSittingAsInt();
		this.speed = ac.getIsWalkingAsInt();
		this.aware = ac.getIsCombatAsInt();

		if (ac.getObjectType() == GameObjectType.PlayerCharacter) {
			player = (PlayerCharacter)ac;
			this.headlights = player.getHeadlightsAsInt();
		} else this.headlights = 0;

		this.mode = ac.getIsFlightAsInt();
	}

	/**
	 * @return the charUUID
	 */
	public int getPlayerUUID() {
		return charUUID;
	}

	/**
	 * @return the sitStand
	 */
	public int getActivity() {
		return activity;
	}

	/**
	 * @param activity
	 *            the sitStand to set
	 */
	public void setActivity(int activity) {
		this.activity = activity;
	}

	/**
	 * @return the walkRun
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * @param speed
	 *            the walkRun to set
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	/**
	 * @return the combatToggle
	 */
	public int getAware() {
		return aware;
	}

	/**
	 * @param aware
	 *            the combatToggle to set
	 */
	public void setAware(int aware) {
		this.aware = aware;
	}

	/**
	 * @return the unknown01
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the unknown01 to set
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	/**
	 * @return the unknown02
	 */
	public int getFighting() {
		return fighting;
	}

	/**
	 * @param fighting
	 *            the unknown02 to set
	 */
	public void setFighting(int fighting) {
		this.fighting = fighting;
	}

	/**
	 * @return the unknown03
	 */
	public int getHeadlights() {
		return headlights;
	}

	/**
	 * @param headlights
	 *            the headlights to set
	 */
	public void setHeadlights(int headlights) {
		this.headlights = headlights;
	}

}
