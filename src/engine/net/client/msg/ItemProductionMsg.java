// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.gameManager.BuildingManager;
import engine.gameManager.PowersManager;
import engine.net.AbstractConnection;
import engine.net.AbstractNetMsg;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.Building;
import engine.objects.Item;
import engine.objects.MobLoot;
import engine.objects.NPC;
import engine.powers.EffectsBase;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemProductionMsg extends ClientNetMsg {

	private static final int ACTION_PRODUCE = 1;
	private static final int ACTION_JUNK = 2;
	private static final int ACTION_RECYCLE = 3;
	private static final int ACTION_COMPLETE = 4;
	private static final int ACTION_DEPOSIT = 6;
	private static final int ACTION_SETPRICE = 5;
	private static final int ACTION_TAKE = 7;
	private static final int ACTION_CONFIRM_SETPRICE = 9;
	private static final int ACTION_CONFIRM_DEPOSIT = 10;
	private static final int ACTION_CONFIRM_TAKE = 11;     // Unsure. Sent by client
	private static final int ACTION_CONFIRM_PRODUCE = 8;

	private ArrayList<Long> ItemList;
	private int size;
	private int buildingUUID;
	private int unknown01;
	private int itemUUID;
	private int itemType;
	public void setItemUUID(int itemUUID) {
		this.itemUUID = itemUUID;
	}

	private int totalProduction;
	private int unknown03;
	private int pToken;
	private int sToken;
	private String name;
	private int actionType;
	private int npcUUID;
	private boolean add;
	private int itemPrice;
	private boolean isMultiple;

	private HashMap<Integer,Integer> itemIDtoTypeMap;;

	/**
	 * This is the general purpose constructor.
	 */

	public ItemProductionMsg() {
		super(Protocol.ITEMPRODUCTION);
        this.actionType = 0;
        this.size = 0;
		this.buildingUUID = 0;
		this.unknown01 = 0;
		this.itemUUID = 0;
		this.totalProduction = 0;
		this.unknown03 = 0;
		this.pToken = 0;
		this.sToken = 0;
		this.name = "";
		this.itemPrice = 0;
		this.itemType = 0;

	}

	public ItemProductionMsg(Building building, NPC vendor, Item item, int actionType, boolean add) {
		super(Protocol.ITEMPRODUCTION);
		this.actionType = actionType;
		this.size = 0;
		this.buildingUUID = building.getObjectUUID();
		this.npcUUID = vendor.getObjectUUID();
		this.itemType = item.getObjectType().ordinal();
		this.itemUUID = item.getObjectUUID();
		this.unknown01 = 0;
		this.totalProduction = 0;
		this.unknown03 = 0;
		this.pToken = 0;
		this.sToken = 0;
		this.name = "";
		this.add = add;
		this.itemPrice = item.getValue();

	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ItemProductionMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ITEMPRODUCTION, origin, reader);

	}

	/**
	 * @see AbstractNetMsg#getPowerOfTwoBufferSize()
	 */
	@Override
	protected int getPowerOfTwoBufferSize() {
		//Larger size for historically larger opcodes
		return (16); // 2^16 == 64k
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		Building building = BuildingManager.getBuildingFromCache(this.buildingUUID);
		if (building == null)
			return;
		// Common Header

		writer.putInt(this.actionType);
		writer.putInt(GameObjectType.Building.ordinal());
		writer.putInt(this.buildingUUID);
		writer.putInt(GameObjectType.NPC.ordinal());
		writer.putInt(this.npcUUID);

		switch (this.actionType) {

		case ACTION_CONFIRM_DEPOSIT:
			writer.putInt(0); // Not item ordinal?
			writer.putInt(0); // Not item uuid?
			writer.putInt(1);
			writer.putInt(0);

			if (!add) {
				writer.put((byte) 1);
				Item item = Item.getFromCache(this.itemUUID);
				if (item != null)
					Item.serializeForClientMsgWithoutSlot(item,writer);
				writer.putInt(building.getStrongboxValue());
				writer.putInt(0);
				writer.putInt(0);
				writer.put((byte) 0);
				break;
			}
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte) 1);
			Item item;
			if (this.itemType == GameObjectType.Item.ordinal())
				item = Item.getFromCache(this.itemUUID);
			else
				item = MobLoot.getFromCache(this.itemUUID);
			if (item != null)
				Item.serializeForClientMsgWithoutSlot(item,writer);
			writer.putInt(building.getStrongboxValue());
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte) 0);
			break;
		case ACTION_CONFIRM_TAKE:
			writer.putInt(this.itemType);
			writer.putInt(this.itemUUID);
			writer.putInt(1);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte) 0);
			break;
		case ACTION_SETPRICE:
			writer.putInt(this.itemType);
			writer.putInt(this.itemUUID);
			writer.putInt(1);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(this.itemPrice); // new price
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte)0);
			break;
		case ACTION_CONFIRM_SETPRICE:
			writer.putInt(this.itemType);
			writer.putInt(this.itemUUID);
			writer.putInt(1);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte)1);
			writer.putInt(building.getStrongboxValue()); // new price
			writer.putInt(0);
			writer.putInt(0);
			//writer.put((byte) 0);
			break;
		case ACTION_DEPOSIT:
			writer.putInt(this.itemType);
			writer.putInt(this.itemUUID);
			writer.putInt(1);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte) 0);
			break;
		case ACTION_TAKE:
		case ACTION_RECYCLE:

			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(1);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte) 1);
			writer.putInt(building.getStrongboxValue());

			if (this.itemIDtoTypeMap != null){
				writer.putInt(this.itemIDtoTypeMap.size());

				for (int itemID : this.itemIDtoTypeMap.keySet()) {
					writer.putInt(this.itemIDtoTypeMap.get(itemID));
					writer.putInt(itemID);
				}

			}else
				writer.putInt(0);

			writer.putInt(0);

			break;
		case ACTION_CONFIRM_PRODUCE:
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(1);
			MobLoot toRoll = MobLoot.getFromCache(this.itemUUID);
			writer.putInt(-1497023830);
			if (toRoll != null && toRoll.getPrefix() != null && !toRoll.getPrefix().isEmpty()){
				EffectsBase eb = PowersManager.getEffectByIDString(toRoll.getPrefix());
				if (eb == null)
					this.pToken = 0;
				else
					this.pToken = eb.getToken();
			}

			if (toRoll != null && toRoll.getSuffix() != null && !toRoll.getSuffix().isEmpty()){
				EffectsBase eb = PowersManager.getEffectByIDString(toRoll.getSuffix());
				if (eb == null)
					this.sToken = 0;
				else
					this.sToken = eb.getToken();
			}
			if (toRoll.isRandom() == false || (toRoll != null && toRoll.isComplete())){
				writer.putInt(this.pToken);
				writer.putInt(this.sToken);
			}else{
				writer.putInt(0);
				writer.putInt(0);
			}

			writer.putString(toRoll.getCustomName());;
			writer.putInt(GameObjectType.MobLoot.ordinal());
			writer.putInt(this.itemUUID);
			writer.putInt(0); //items left to produce?
			if (toRoll != null){
				writer.putInt(toRoll.getItemBaseID());
				writer.putInt(toRoll.getValue());
			}
			else{
				writer.putInt(0);
				writer.putInt(0);
			}

			NPC vendor = NPC.getFromCache(this.npcUUID);
			if (vendor != null){
				if (toRoll.isComplete()){
					writer.putInt(0);
					writer.putInt(0);
				}else{
					long timeLeft = toRoll.getDateToUpgrade() - System.currentTimeMillis();

					timeLeft /=1000;
					writer.putInt((int)timeLeft);
					writer.putInt(vendor.getRollingTimeInSeconds(toRoll.getItemBaseID()));
				}

			}else{
				writer.putInt(0);
				writer.putInt(0);
			}
			if (toRoll.isComplete())
				writer.putInt(0);
			else
				writer.putInt(1);
			writer.put((byte)0);

			if (toRoll != null && toRoll.isComplete())
				writer.put((byte)1);
			else
				writer.put((byte)0);
			writer.put((byte)0);
			writer.put((byte)1);
			writer.putInt(vendor.getBuilding().getStrongboxValue());

			writer.putInt(0);
			writer.putInt(0);
			//writer.putInt(0); //error popup

			break;
		case ACTION_COMPLETE:
			writer.putInt(this.itemType);
			writer.putInt(this.itemUUID);
			writer.putInt(this.totalProduction);
			writer.putInt(this.unknown03);
			writer.putInt(this.pToken);
			writer.putInt(this.sToken);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.put((byte)0);
			break;
		case ACTION_JUNK:
			writer.putInt(this.itemType);
			writer.putInt(this.itemUUID);
			writer.putInt(this.totalProduction);
			writer.putInt(this.unknown03);
			writer.putInt(this.pToken);
			writer.putInt(this.sToken);
			writer.putString(this.name);
			writer.putInt(0);
			writer.putInt(0);
			writer.putShort((short)0);
			writer.put((byte)0);
			break;
		default:
			writer.putInt(0);
			writer.putInt(1);
			writer.putInt(0);
			writer.putInt(0);
			break;
		}

	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {

		// Common header

		this.actionType = reader.getInt();
		reader.getInt(); // Building type padding
		this.buildingUUID = reader.getInt();
		reader.getInt(); // NPC type padding
		this.npcUUID = reader.getInt();

		switch (this.actionType) {
		case ACTION_SETPRICE:
			this.itemType = reader.getInt();
			this.itemUUID = reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			this.itemPrice = reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.get();
			break;
		case ACTION_RECYCLE:
		case ACTION_TAKE:
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.get();
			this.size = reader.getInt();
			HashMap<Integer,Integer> tempIDs = new HashMap<>();
			for (int i = 0; i < this.size; i++) {
				int type = reader.getInt(); // Item type padding
				this.itemUUID = reader.getInt();
				tempIDs.put(this.itemUUID, type);
			}
			reader.getInt();
			this.itemIDtoTypeMap = tempIDs;
			break;
		case ACTION_DEPOSIT:
			this.itemType = reader.getInt();
			this.itemUUID = reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.get();
			break;
		case ACTION_JUNK:
			this.itemType = reader.getInt();
			this.itemUUID = reader.getInt();
			this.totalProduction =reader.getInt();
			this.unknown03 = reader.getInt();
			this.pToken = reader.getInt();
			this.sToken = reader.getInt();
			this.name = reader.getString();
			reader.getInt();
			reader.getInt();
			reader.get();
			break;
		default:
			this.itemType = reader.getInt();
			this.itemUUID = reader.getInt();
			this.totalProduction =reader.getInt();
			this.unknown03 = reader.getInt();
			this.pToken = reader.getInt();
			this.sToken = reader.getInt();
			this.name = reader.getString();
			this.isMultiple = reader.getInt() != 0 ? true:false;
			reader.getInt();

			if (this.actionType == ACTION_COMPLETE || this.actionType == ACTION_JUNK)
				reader.get();
			else
				reader.getShort();
			break;

		}
	}

	public int getItemType() {
		return itemType;
	}

	// TODO fix ArrayList Accessability.
	public ArrayList<Long> getItemList() {
		return this.ItemList;
	}

	public void addItem(Long item) {
		this.ItemList.add(item);
	}

	public int getUnknown01() {
		return unknown01;
	}

	public int getTotalProduction() {
		return totalProduction;
	}

	public int getUnknown03() {
		return unknown03;
	}

	public void setUnknown01(int unknown01) {
		this.unknown01 = unknown01;
	}

	public void setTotalProduction(int unknown02) {
		this.totalProduction = unknown02;
	}

	public void setUnknown03(int unknown03) {
		this.unknown03 = unknown03;
	}

	public void setItemList(ArrayList<Long> value) {
		this.ItemList = value;
	}

	public int getItemUUID() {
		return itemUUID;
	}

	public void setpToken(int pToken) {
		this.pToken = pToken;
	}

	public int getpToken() {
		return pToken;
	}

	public void setsToken(int sToken) {
		this.sToken = sToken;
	}

	public int getsToken() {
		return sToken;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getActionType() {
		return actionType;
	}

	public final void setActionType(int actionType) {
		this.actionType = actionType;
	}

	public int getNpcUUID() {
		return npcUUID;
	}

	public HashMap<Integer,Integer> getItemIDtoTypeMap() {
		return itemIDtoTypeMap;
	}

	/**
	 * @return the itemPrice
	 */
	public int getItemPrice() {
		return itemPrice;
	}

	public boolean isMultiple() {
		return isMultiple;
	}

	public void setMultiple(boolean isMultiple) {
		this.isMultiple = isMultiple;
	}
}
