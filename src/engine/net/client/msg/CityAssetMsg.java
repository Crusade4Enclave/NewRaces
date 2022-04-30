// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.Enum.ProtectionState;
import engine.exception.SerializationException;
import engine.gameManager.BuildingManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Building;
import engine.objects.City;
import engine.objects.Zone;
import org.pmw.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;

public class CityAssetMsg extends ClientNetMsg {

	Set<Building> allCityAssets;
	Set<Building> canProtectAssets;
	private int type;
	private int buildingID;
	private int size;
	private int pad = 0;

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public CityAssetMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CITYASSET, origin, reader);
	}

	public CityAssetMsg() {
		super(Protocol.CITYASSET);
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.type = reader.getInt();
		reader.getInt();
		this.buildingID = reader.getInt();
		reader.getInt();

	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		// Larger size for historically larger opcodes
		return (12);
	}


	// Precache and configure this message before we serialize it

	public void configure() {

		Building tol;
		Zone zone;
		City city;

		canProtectAssets = new HashSet<>();

		tol = BuildingManager.getBuildingFromCache(this.buildingID);

		if (tol == null) {
			Logger.debug("TOL is null");
			return;
		}

		zone = tol.getParentZone();

		if (zone == null) {
			Logger.debug("Zone is null");
			return;
		}

		city = City.getCity(zone.getPlayerCityUUID());

		if (city == null) {
			Logger.debug( "Failed to load city data for Tree of life.");
			return;
		}

		allCityAssets = zone.zoneBuildingSet;

		for (Building building : allCityAssets) {

			if (building.getBlueprint() == null)
				continue;

			// Protected assets do not show up on list

			if (building.assetIsProtected() == true)
				continue;

			if (building.getProtectionState() == ProtectionState.PENDING)
				continue;

			// Shouldn't need this but just in case

			if (building.getBlueprint().isWallPiece())
				continue;

			if (building.getBlueprint().getBuildingGroup().equals(Enum.BuildingGroup.TOL))
				continue;

			if (building.getBlueprint().getBuildingGroup().equals(Enum.BuildingGroup.BANESTONE))
				continue;

			if (building.getBlueprint().getBuildingGroup().equals(Enum.BuildingGroup.SHRINE))
				continue;

			if (!city.isLocationOnCityGrid(building.getLoc()))
				continue;

			canProtectAssets.add(building);

		}

	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		String buildingName;

		writer.putInt(0);
		writer.putInt(0);

		writer.putInt(GameObjectType.Building.ordinal());
		writer.putInt(this.buildingID);

		writer.putInt(canProtectAssets.size());

		for (Building cityBuilding : canProtectAssets) {

			buildingName = cityBuilding.getName();

			if (buildingName.isEmpty() && cityBuilding.getBlueprint() != null) {
				buildingName = cityBuilding.getBlueprint().getName();
			}

			writer.putInt(cityBuilding.getObjectType().ordinal());
			writer.putInt(cityBuilding.getObjectUUID());

			writer.putString(buildingName);
			writer.putString(cityBuilding.getGuild().getName());
			writer.putInt(20);//  \/ Temp \/
			writer.putInt(cityBuilding.getRank());

			if (cityBuilding.getBlueprint() != null) {
				writer.putInt(cityBuilding.getBlueprint().getIcon());
			}
			else {
				writer.putInt(0);
			}
			writer.putInt(7);  //TODO identify these Guild tags??
			writer.putInt(17);
			writer.putInt(14);
			writer.putInt(14);
			writer.putInt(98);// /\ Temp /\

		}
	}

	public int getPad() {
		return pad;
	}

	public void setPad(int value) {
		this.pad = value;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getBuildingID() {
		return buildingID;
	}

	public void setBuildingID(int buildingID) {
		this.buildingID = buildingID;
	}

}
