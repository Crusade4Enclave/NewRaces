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
import engine.objects.Building;
import engine.objects.Guild;
import engine.server.MBServerStatics;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

public class ArcMineWindowAvailableTimeMsg extends ClientNetMsg {

	private int buildingUUID;
	private Building treeOfLife;

	private int currentMineHour;
	private Seconds secondsLeft;
	private DateTime lateTime;
	private int late;

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ArcMineWindowAvailableTimeMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ARCMINEWINDOWAVAILABLETIME, origin, reader);
	}

	public ArcMineWindowAvailableTimeMsg(Building treeOfLife, int timeLeft) {
		super(Protocol.ARCMINEWINDOWAVAILABLETIME);
		this.treeOfLife = treeOfLife;
		this.buildingUUID = treeOfLife.getObjectUUID();
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt(); // Object type padding (We know it's a building)
		this.buildingUUID = reader.getInt();
		reader.getInt();

	}

	// Configures and pre-caches values for this message
	// so everything is already available during serialisation.

	public void configure() {

		 Guild guild;
	        guild = this.treeOfLife.getGuild();

	        if (guild != null)
	            currentMineHour = guild.getMineTime();

	        late = MBServerStatics.MINE_LATE_WINDOW;
	        lateTime = DateTime.now();

	        if (late == 0)
	            lateTime = lateTime.plusDays(1);

	        lateTime = lateTime.hourOfDay().setCopy(late);

	        late = ((late > 23) ? (late - 24) : late);
	        secondsLeft = Seconds.secondsBetween(DateTime.now(), lateTime);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		writer.putInt(MBServerStatics.MINE_EARLY_WINDOW); //15);
		writer.putInt(late);
		writer.putInt(currentMineHour);

		writer.putInt(this.treeOfLife.getObjectType().ordinal());
		writer.putInt(this.treeOfLife.getObjectUUID());

		writer.putInt((int)secondsLeft.getSeconds());
	}

	public int getBuildingUUID() {
		return buildingUUID;
	}

	public void setBuildingUUID(int buildingUUID) {
		this.buildingUUID = buildingUUID;
	}
}
