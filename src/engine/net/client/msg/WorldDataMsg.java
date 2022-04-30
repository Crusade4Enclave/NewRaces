// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.exception.SerializationException;
import engine.gameManager.ConfigManager;
import engine.gameManager.ZoneManager;
import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Zone;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

public class WorldDataMsg extends ClientNetMsg {

	public static final long wdComp = 0xFF00FF0000000001L;
	private static byte ver;

	/**
	 * This is the general purpose constructor.
	 */
	public WorldDataMsg() {
		super(Protocol.NEWWORLD);
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public WorldDataMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.NEWWORLD, origin, reader);
	}

	/**
	 * @see AbstractNetMsg#getPowerOfTwoBufferSize()
	 */
	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return (18); // 2^17 == 131,072
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {


		// TODO replace this return with SerializationException

		Zone root = ZoneManager.getSeaFloor();
		if (root == null){
			Logger.error("Failed to find Sea Floor!");
			return;
		}

		writer.putString(ConfigManager.MB_WORLD_NAME.getValue());
		writer.putInt(512);
		writer.putInt(384);

		writer.putInt(MBServerStatics.worldMapID);
		writer.putInt(0x00000000);

		writer.putInt(getTotalMapSize(root) + 1);
		Zone.serializeForClientMsg(root,writer);

		Zone hotzone = ZoneManager.getHotZone();

		if (hotzone == null)
			writer.putLong(0L);
		else {
			writer.putInt(hotzone.getObjectType().ordinal());
			writer.putInt(hotzone.getObjectUUID());
		}


		

		writer.putFloat(0);
		writer.putFloat(1);
		writer.putFloat(0);
		writer.putFloat(0.69999999f);
		writer.putFloat(.5f);
		writer.putFloat(1);
		writer.putFloat(.5f);
		writer.putFloat(0.69999999f);
		writer.putFloat(1);
		writer.putFloat(0);
		writer.putFloat(0);
		writer.putFloat(0.69999999f);
		writer.putFloat(1);
		writer.putFloat(.5f);
		writer.putFloat(.5f);
		writer.putFloat(0.69999999f);
		writer.putFloat(1);
		


	}


	@Override
	protected void _deserialize(ByteBufferReader reader) {

	}

	private static int getTotalMapSize(Zone root) {
		if (root.getNodes().isEmpty())
			return 0;

		int size = root.getNodes().size();
		for (Zone child : root.getNodes())
			size += getTotalMapSize(child);
		return size;
	}


}
