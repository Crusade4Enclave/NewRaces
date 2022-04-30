// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum.GameObjectType;
import engine.Enum.ProtectionState;
import engine.Enum.TransactionType;
import engine.gameManager.DbManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class dbWarehouseHandler extends dbHandlerBase {

	private static final ConcurrentHashMap<Integer, String> columns = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);

	public dbWarehouseHandler() {
		this.localClass = Warehouse.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());

		if (columns.isEmpty()) {
			createColumns();
		}
	}

	public Warehouse CREATE_WAREHOUSE(Warehouse wh) {
		try {
			wh = this.addWarehouse(wh);
		} catch (Exception e) {
			Logger.error(e);
			wh = null;
			
		}
		return wh;
	}

	public ArrayList<AbstractGameObject> CREATE_WAREHOUSE( int parentZoneID, int OwnerUUID, String name, int meshUUID,
			Vector3fImmutable location, float meshScale, int currentHP,
			ProtectionState protectionState, int currentGold, int rank,
			DateTime upgradeDate, int blueprintUUID, float w, float rotY) {

		prepareCallable("CALL `WAREHOUSE_CREATE`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? ,? ,?, ?);");

		setInt(1, parentZoneID);
		setInt(2, OwnerUUID);
		setString(3, name);
		setInt(4, meshUUID);
		setFloat(5, location.x);
		setFloat(6, location.y);
		setFloat(7, location.z);
		setFloat(8, meshScale);
		setInt(9, currentHP);
		setString(10, protectionState.name());
		setInt(11, currentGold);
		setInt(12, rank);

		if (upgradeDate != null) {
			setTimeStamp(13, upgradeDate.getMillis());
		} else {
			setNULL(13, java.sql.Types.DATE);
		}

		setInt(14, blueprintUUID);
		setFloat(15, w);
		setFloat(16, rotY);

		ArrayList<AbstractGameObject> list = new ArrayList<>();
		//System.out.println(this.cs.get().toString());
		try {
			boolean work = execute();
			if (work) {
				ResultSet rs = this.cs.get().getResultSet();
				while (rs.next()) {
					addObject(list, rs);
				}
				rs.close();
			} else {
				Logger.info("Warehouse Creation Failed: " + this.cs.get().toString());
				return list; //city creation failure
			}
			while (this.cs.get().getMoreResults()) {
				ResultSet rs = this.cs.get().getResultSet();
				while (rs.next()) {
					addObject(list, rs);
				}
				rs.close();
			}
		} catch (SQLException e) {
			Logger.info("Warehouse Creation Failed, SQLException: " + this.cs.get().toString() + e.toString());
			return list; //city creation failure
		} catch (UnknownHostException e) {
			Logger.info("Warehouse Creation Failed, UnknownHostException: " + this.cs.get().toString());
			return list; //city creation failure
		} finally {
			closeCallable();
		}
		return list;

	}

	//Don't call yet, not ready in DB. -
	public boolean WAREHOUSE_ADD(Item item, Warehouse warehouse, ItemBase ib, int amount) {
		if (item == null || warehouse == null || ib == null || !(dbWarehouseHandler.columns.containsKey(ib.getUUID()))) {
			return false;
		}
		if ((item.getNumOfItems() - amount) < 0) {
			return false;
		}
		if (!warehouse.getResources().containsKey(ib)) {
			return false;
		}

		prepareCallable("CALL `warehouse_ADD`(?,?,?,?,?,?,?);");
		setLong(1, (long) warehouse.getObjectUUID());
		setInt(2, warehouse.getResources().get(ib));
		setLong(3, (long) item.getObjectUUID());
		setInt(4, item.getNumOfItems());
		setInt(5, amount);
		setString(6, dbWarehouseHandler.columns.get(ib.getUUID()));
		setInt(7, ib.getUUID());
		String result = getResult();

		return (result != null && result.equals("success"));
	}

	private Warehouse addWarehouse(Warehouse toAdd) {
		prepareCallable("CALL `warehouse_CREATE`(?);");
		setInt(1, toAdd.getUID());
		int objectUUID = (int) getUUID();
		if (objectUUID > 0) {
			return GET_WAREHOUSE(objectUUID);
		}
		return null;
	}

	public Warehouse GET_WAREHOUSE(int objectUUID) {
		Warehouse warehouse = (Warehouse) DbManager.getFromCache(GameObjectType.Warehouse, objectUUID);
		if (warehouse != null)
			return warehouse;
		prepareCallable("SELECT * FROM `obj_warehouse` WHERE `UID` = ?");
		setInt(1, objectUUID);
		return (Warehouse) getObjectSingle(objectUUID);
	}

	public boolean updateLocks(final Warehouse wh, long locks) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_locks`=? WHERE `UID` = ?");
		setLong(1, locks);
		setInt(2, wh.getUID());
		return (executeUpdate() != 0);
	}

	public boolean updateGold(final Warehouse wh, int amount ) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_gold`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());
		return (executeUpdate() != 0);
	}

	public boolean updateStone(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_stone`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateTruesteel(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_truesteel`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateIron(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_iron`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateAdamant(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_adamant`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateLumber(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_lumber`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateOak(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_oak`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateBronzewood(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_bronzewood`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateMandrake(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_mandrake`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateCoal(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_coal`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateAgate(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_agate`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateDiamond(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_diamond`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateOnyx(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_onyx`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateAzoth(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_azoth`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateOrichalk(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_orichalk`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateAntimony(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_antimony`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateSulfur(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_sulfur`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateQuicksilver(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_quicksilver`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateGalvor(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_galvor`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateWormwood(final Warehouse wh, int amount ) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_wormwood`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateObsidian(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_obsidian`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateBloodstone(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_bloodstone`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	public boolean updateMithril(final Warehouse wh, int amount) {
		prepareCallable("UPDATE `obj_warehouse` SET `warehouse_mithril`=? WHERE `UID` = ?");
		setInt(1, amount);
		setInt(2, wh.getUID());

		return (executeUpdate() != 0);
	}

	private static void createColumns() {
		columns.put(1580000, "warehouse_stone");
		columns.put(1580001, "warehouse_truesteel");
		columns.put(1580002, "warehouse_iron");
		columns.put(1580003, "warehouse_adamant");
		columns.put(1580004, "warehouse_lumber");
		columns.put(1580005, "warehouse_oak");
		columns.put(1580006, "warehouse_bronzewood");
		columns.put(1580007, "warehouse_mandrake");
		columns.put(1580008, "warehouse_coal");
		columns.put(1580009, "warehouse_agate");
		columns.put(1580010, "warehouse_diamond");
		columns.put(1580011, "warehouse_onyx");
		columns.put(1580012, "warehouse_azoth");
		columns.put(1580013, "warehouse_orichalk");
		columns.put(1580014, "warehouse_antimony");
		columns.put(1580015, "warehouse_sulfur");
		columns.put(1580016, "warehouse_quicksilver");
		columns.put(1580017, "warehouse_galvor");
		columns.put(1580018, "warehouse_wormwood");
		columns.put(1580019, "warehouse_obsidian");
		columns.put(1580020, "warehouse_bloodstone");
		columns.put(1580021, "warehouse_mithril");
		columns.put(7, "warehouse_gold");
	}

	public boolean CREATE_TRANSACTION(int warehouseBuildingID, GameObjectType targetType, int targetUUID, TransactionType transactionType,Resource resource, int amount,DateTime date){
		Transaction transactions = null;
		prepareCallable("INSERT INTO `dyn_warehouse_transactions` (`warehouseUID`, `targetType`,`targetUID`, `type`,`resource`,`amount`,`date` ) VALUES (?,?,?,?,?,?,?)");
		setLong(1, warehouseBuildingID);
		setString(2, targetType.name());
		setLong(3, targetUUID);
		setString(4, transactionType.name());
		setString(5, resource.name());
		setInt(6,amount);
		setTimeStamp(7,date.getMillis());
		return (executeUpdate() != 0);
	}



	public static void addObject(ArrayList<AbstractGameObject> list, ResultSet rs) throws SQLException, UnknownHostException {
		String type = rs.getString("type");
		switch (type) {
		case "building":
			Building building = new Building(rs);
			DbManager.addToCache(building);
			list.add(building);
			break;
		case "warehouse":
			Warehouse warehouse = new Warehouse(rs);
			DbManager.addToCache(warehouse);
			list.add(warehouse);
			break;
		}
	}

	public ArrayList<Transaction> GET_TRANSACTIONS_FOR_WAREHOUSE(final int warehouseUUID) {
		ArrayList<Transaction> transactionsList = new ArrayList<>();
		prepareCallable("SELECT * FROM dyn_warehouse_transactions WHERE `warehouseUID` = ?;");
		setInt(1, warehouseUUID);
		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {
				Transaction transactions = new Transaction(rs);
				transactionsList.add(transactions);
			}

		} catch (SQLException e) {
			Logger.error( e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		return transactionsList;
	}

	public void LOAD_ALL_WAREHOUSES() {

		Warehouse thisWarehouse;

		prepareCallable("SELECT `obj_warehouse`.*, `object`.`parent`, `object`.`type` FROM `object` LEFT JOIN `obj_warehouse` ON `object`.`UID` = `obj_warehouse`.`UID` WHERE `object`.`type` = 'warehouse';");

		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {
				thisWarehouse = new Warehouse(rs);
				thisWarehouse.runAfterLoad();
				thisWarehouse.loadAllTransactions();
			}

		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}

	}
}
