// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.Enum.TransactionType;
import engine.exception.SerializationException;
import engine.gameManager.BuildingManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.*;

import java.util.ArrayList;

public class ArcViewAssetTransactionsMsg extends ClientNetMsg {

	private int warehouseID;
	private Warehouse warehouse;
	private int transactionID;
	private ArrayList<Transaction> transactions;
	Building warehouseBuilding;

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ArcViewAssetTransactionsMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.ARCVIEWASSETTRANSACTIONS, origin, reader);
	}

	public ArcViewAssetTransactionsMsg(Warehouse warehouse, ArcViewAssetTransactionsMsg msg) {
		super(Protocol.ARCVIEWASSETTRANSACTIONS);
        this.warehouseID = msg.warehouseID;
        this.transactionID = msg.transactionID;
		this.warehouse = warehouse;
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.transactionID = reader.getInt(); //some odd type?
		this.warehouseID = reader.getInt();
		reader.getInt();
	}

	// Method pre-caches and configures values so they are
	// available before we attempt serialization

	public void configure() {

		warehouseBuilding = BuildingManager.getBuilding(this.warehouse.getBuildingUID());
		transactions = new ArrayList<>(50);

		if (this.warehouse.getTransactions().size() > 150){
			transactions.addAll(this.warehouse.getTransactions().subList(this.warehouse.getTransactions().size() - 150, this.warehouse.getTransactions().size()));
		}else
			transactions = this.warehouse.getTransactions();

	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) throws SerializationException {

		writer.putInt(this.transactionID);
		writer.putInt(this.warehouse.getBuildingUID());
		writer.putInt(transactions.size()); //list Size

		for (Transaction transaction:transactions){
			String name = "No Name";
			switch (transaction.getTargetType()){
			case Building:
				Building building = BuildingManager.getBuildingFromCache(transaction.getTargetUUID());
				if (building != null)
					name = building.getName();
				Mine mine = Mine.getMineFromTower(transaction.getTargetUUID());
				//
				if (mine != null)
					name = mine.getZoneName();
				
				if (transaction.getTransactionType().equals(TransactionType.TAXRESOURCE) || transaction.getTransactionType().equals(TransactionType.TAXRESOURCEDEPOSIT)){
					City city = building.getCity();
					
					if (city != null)
						name = city.getCityName();
				}
				
				break;
			case PlayerCharacter:
				PlayerCharacter pc = PlayerCharacter.getPlayerCharacter(transaction.getTargetUUID());
				if (pc != null)
					name = pc.getCombinedName();
				break;
			case NPC:
				NPC npc = NPC.getFromCache(transaction.getTargetUUID());
				if (npc != null){
					
					if (npc.getBuilding() != null)
						name = npc.getBuilding().getName();
					else
					name = npc.getName();
				}
					
				
			default:
				break;
			}
			writer.putInt(transaction.getTargetType().ordinal()); //Type
			writer.putInt(transaction.getTargetUUID()); //ID
			writer.putString(name); //Name of depositer/withdrawler or mine name
			writer.putInt(GameObjectType.Building.ordinal()); //Type
			writer.putInt(warehouse.getBuildingUID()); //ID
			writer.putString(warehouseBuilding.getName()); //warehouse
			writer.putInt(transaction.getTransactionType().getID()); //79,80 withdrew, 81 mine produced, 82 deposit
			writer.putInt(transaction.getAmount()); //amount
			writer.putString(transaction.getResource().name().toLowerCase()); //item type
			writer.putDateTime(transaction.getDate());
		}


		//writer.putString("balls");
	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		return (16); // 2^14 == 16384
	}

	public int getWarehouseID() {
		return warehouseID;
	}
	public int getTransactionID() {
		return transactionID;
	}
}
