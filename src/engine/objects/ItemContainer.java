// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.objects;

import engine.Enum.ContainerType;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;


public class ItemContainer extends AbstractGameObject {

	private AbstractWorldObject owner;
	private ConcurrentHashMap<Long, Item>itemMap = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

	private ContainerType containerType;

	/**
	 * No Table ID Constructor
	 */
	public ItemContainer(AbstractWorldObject owner, ContainerType containerType) {
		super();
		this.owner = owner;
		this.containerType = containerType;
	}

	/**
	 * Normal Constructor
	 */
	public ItemContainer(AbstractWorldObject owner, ContainerType containerType, int newUUID) {
		super(newUUID);
		this.owner = owner;
		this.containerType = containerType;
	}

	/**
	 * ResultSet Constructor
	 */
	public ItemContainer(ResultSet rs) throws SQLException {
		super(rs);

		//get owner
		long ownerID = rs.getLong("parent");
		this.owner = (AbstractWorldObject)AbstractGameObject.getFromTypeAndID(ownerID);

		//get ContainerType
		String ct = rs.getString("container_type");
		try {
			this.containerType = ContainerType.valueOf(ct.toUpperCase());
		} catch (Exception e) {
			this.containerType = ContainerType.INVENTORY;
			Logger.error( "invalid containerType");
		}
	}

	/*
	 * Getters
	 */
	public AbstractWorldObject getOwner() {
		return this.owner;
	}

	public ConcurrentHashMap<Long, Item> getItemMap() {
		return this.itemMap;
	}

	public ContainerType getContainerType() {
		return this.containerType;
	}

	public boolean isBank() {
		return (this.containerType == ContainerType.BANK);
	}

	public boolean isInventory() {
		return (this.containerType == ContainerType.INVENTORY);
	}

	public boolean isVault() {
		return (this.containerType == ContainerType.VAULT);
	}

	public boolean containsItem(long itemID) {
		return this.itemMap.containsKey(itemID);
	}

	@Override
	public void updateDatabase() {
		// TODO Create update logic.
	}

}
