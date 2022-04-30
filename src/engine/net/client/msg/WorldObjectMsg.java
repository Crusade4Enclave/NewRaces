// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum;
import engine.Enum.RunegateType;
import engine.gameManager.DbManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.Network;
import engine.net.client.Protocol;
import engine.objects.AbstractGameObject;
import engine.objects.City;
import engine.objects.Mine;
import engine.objects.Runegate;
import engine.session.Session;
import org.pmw.tinylog.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class WorldObjectMsg extends ClientNetMsg {

	private Session s;
	private boolean forEnterWorld;
	private static ByteBuffer cachedEnterWorld;
	private static long cachedExpireTime;

	public static final long wdComp = 0xFF00FF0000000003L;
	private static byte ver = 1;

	private boolean updateCities = false;
	private boolean updateRunegates = false;
	private boolean updateMines = false;

	/**
	 * This is the general purpose constructor.
	 *
	 * @param s
	 *            Session
	 * @param forEnterWorld
	 *            boolean flag
	 */
	public WorldObjectMsg(Session s, boolean forEnterWorld) {
		super(Protocol.CITYDATA);
		this.s = s;
		this.forEnterWorld = forEnterWorld;
	}

	public WorldObjectMsg(boolean updateCities, boolean updateRunegates, boolean updateMines) {
		super(Protocol.CITYDATA);
		this.s = null;
		this.forEnterWorld = false;
		this.updateCities = updateCities;
		this.updateRunegates = updateRunegates;
		this.updateMines = updateMines;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public WorldObjectMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.CITYDATA, origin, reader);
		this.forEnterWorld = false;
	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		return (18); // 2^14 == 16384
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		if (this.forEnterWorld)
			serializeForEnterWorld(writer);
		else
			serializeForMapUpdate(writer);
	}

	/**
	 * Specific use serializer
	 *
	 * @param writer
	 */
	private void serializeForMapUpdate(ByteBufferWriter writer) {

		//Handle City updates

		if (this.updateCities) {
			writer.put((byte) 0);
			ArrayList<City> cityList = new ArrayList<>();
			ConcurrentHashMap<Integer, AbstractGameObject> map = DbManager.getMap(Enum.GameObjectType.City);
			if (map != null) {
				for (AbstractGameObject ago : map.values())
					if (ago.getObjectType().equals(Enum.GameObjectType.City))
						cityList.add((City)ago);
				
				writer.putInt(cityList.size());
				for (City city: cityList){
					City.serializeForClientMsg(city, writer);
				}
				
			} else {
				Logger.error("missing city map");
				writer.putInt(0);
			}
		} else
			writer.put((byte) 1);


		//Handle Runegate updates
		if (this.updateRunegates) {

			writer.put((byte) 0);
			writer.putInt(RunegateType.values().length);

			for(RunegateType gateType : engine.Enum.RunegateType.values()) {

				Runegate.getRunegates()[gateType.ordinal()]._serializeForEnterWorld(writer);
			}
		} else
			writer.put((byte) 1);


		//Handle Mine updates
		try{
			if (this.updateMines) {
				ArrayList<Mine> mineList = new ArrayList<>();
				for (Mine toAdd: Mine.mineMap.keySet()){
					mineList.add(toAdd);
				}
				
				writer.putInt(mineList.size());
				for (Mine mine: mineList)
					Mine.serializeForClientMsg(mine, writer);
			} else
				writer.putInt(0);
		}catch(Exception e){
			Logger.error(e);
		}
		


		writer.put((byte) 0); // PAD
	}

	/**
	 * Specific use serializer
	 *
	 * @param writer
	 */
	private void serializeForEnterWorld(ByteBufferWriter writer) {
		if (s == null || s.getPlayerCharacter() == null)
			return;

		long startT = System.currentTimeMillis();

		if (cachedEnterWorld == null) {
			// Never before been cached, so init stuff
			cachedEnterWorld = Network.byteBufferPool.getBuffer(19);
			cachedExpireTime = 0L;
		}

		//Check to see if its time to renew cache.
		if (cachedExpireTime < System.currentTimeMillis()) {
			synchronized (cachedEnterWorld) {
				WorldObjectMsg.attemptSerializeForEnterWorld(cachedEnterWorld);
			}
			cachedExpireTime = startT + 60000;
		}

		writer.putBB(cachedEnterWorld);

	}

	private static void attemptSerializeForEnterWorld(ByteBuffer bb) {
		bb.clear();
		ByteBufferWriter temp = new ByteBufferWriter(bb);
		temp.put((byte) 0); // PAD


		ArrayList<City> cityList = new ArrayList<>();
		ConcurrentHashMap<Integer, AbstractGameObject> map = DbManager.getMap(Enum.GameObjectType.City);
		for (AbstractGameObject ago : map.values())

			if (ago.getObjectType().equals(Enum.GameObjectType.City))
				cityList.add((City)ago);

		temp.putInt(cityList.size());
		
		for (City city: cityList)
			City.serializeForClientMsg(city, temp);
		temp.put((byte) 0); // PAD

		// Serialize runegates

		temp.putInt(RunegateType.values().length);

		for(RunegateType gateType : engine.Enum.RunegateType.values()) {

			Runegate.getRunegates()[gateType.ordinal()]._serializeForEnterWorld(temp);
		}

		ArrayList<Mine> mineList = new ArrayList<>();
		for (Mine toAdd : Mine.mineMap.keySet()){
			mineList.add(toAdd);
		}
		
		temp.putInt(mineList.size());
		for (Mine mine: mineList)
			Mine.serializeForClientMsg(mine, temp);
		temp.put((byte) 0); // PAD
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		// Client only sends 11 bytes.
		
		byte type = reader.get();
		
		if (type == 1){
			reader.get();
			reader.get();
			reader.getInt();
			
		}else{
			reader.get();
			reader.getInt();
			reader.get();
			reader.getInt();
		}
		   
	}

	/**
	 * @return the s
	 */
	public Session getS() {
		return s;
	}

	/**
	 * @return the forEnterWorld
	 */
	public boolean isForEnterWorld() {
		return forEnterWorld;
	}

	public void updateCities(boolean value) {
		this.updateCities = value;
	}

	public void updateRunegates(boolean value) {
		this.updateRunegates = value;
	}

	public void updateMines(boolean value) {
		this.updateMines = value;
	}


}
