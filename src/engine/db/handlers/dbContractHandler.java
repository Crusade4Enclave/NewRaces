// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum;
import engine.gameManager.DbManager;
import engine.objects.Contract;
import engine.objects.ItemBase;
import engine.objects.MobEquipment;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class dbContractHandler extends dbHandlerBase {

	public dbContractHandler() {
		this.localClass = Contract.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public Contract GET_CONTRACT(final int objectUUID) {
		Contract contract = (Contract) DbManager.getFromCache(Enum.GameObjectType.Contract, objectUUID);
		if (contract != null)
			return contract;
		if (objectUUID == 0)
			return null;
		prepareCallable("SELECT * FROM `static_npc_contract` WHERE `ID` = ?");
		setInt(1, objectUUID);
		return (Contract) getObjectSingle(objectUUID);
	}

	public ArrayList<Contract> GET_CONTRACT_BY_RACE(final int objectUUID) {

		ArrayList<Contract> contracts = new ArrayList<>();

		prepareCallable("SELECT * FROM static_npc_contract WHERE `mobbaseID` =?;");
		setLong(1, objectUUID);

		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {
				Contract contract = new Contract(rs);
				if (contract != null)
					contracts.add(contract);
			}

		} catch (SQLException e) {
			Logger.error( e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		return contracts;
	}

	public void GET_GENERIC_INVENTORY(final Contract contract) {

		prepareCallable("SELECT * FROM `static_npc_inventoryset` WHERE `inventorySet` = ?;");
		setInt(1, contract.inventorySet);

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				//handle item base
				int itemBaseID = rs.getInt("itembaseID");

				ItemBase ib = ItemBase.getItemBase(itemBaseID);

				if (ib != null) {

					MobEquipment me = new MobEquipment(ib, 0, 0);
					contract.getSellInventory().add(me);

					//handle magic effects
					String prefix = rs.getString("prefix");
					int pRank = rs.getInt("pRank");
					String suffix = rs.getString("suffix");
					int sRank = rs.getInt("sRank");

					if (prefix != null) {
						me.setPrefix(prefix, pRank);
						me.setIsID(true);
					}

					if (suffix != null) {
						me.setSuffix(suffix, sRank);
						me.setIsID(true);
					}

				}
			}
		} catch (SQLException e) {
			Logger.error("SQL Error number: " + e.getErrorCode() + ' ' + e.getMessage());
		} finally {
			closeCallable();
		}
	}

	public void GET_SELL_LISTS(final Contract con) {
		prepareCallable("SELECT * FROM `static_npc_contract_selltype` WHERE `contractID` = ?;");
		setInt(1, con.getObjectUUID());
		try {
			ResultSet rs = executeQuery();
			ArrayList<Integer> item = con.getBuyItemType();
			ArrayList<Integer> skill = con.getBuySkillToken();
			ArrayList<Integer> unknown = con.getBuyUnknownToken();
			while (rs.next()) {
				int type = rs.getInt("type");
				int value = rs.getInt("value");
				if (type == 1) {
					item.add(value);
				} else if (type == 2) {
					skill.add(value);
				} else if (type == 3) {
					unknown.add(value);
				}
			}
		} catch (SQLException e) {
			Logger.error("SQL Error number: " + e.getErrorCode() + ' ' + e.getMessage());
		} finally {
			closeCallable();
		}
	}

	public boolean updateAllowedBuildings(final Contract con, final long slotbitvalue) {
		prepareCallable("UPDATE `static_npc_contract` SET `allowedBuildingTypeID`=? WHERE `contractID`=?");
		setLong(1, slotbitvalue);
		setInt(2, con.getContractID());
		return (executeUpdate() > 0);
	}

	public boolean updateDatabase(final Contract con) {
		prepareCallable("UPDATE `static_npc_contract` SET `contractID`=?, `name`=?, "
				+ "`mobbaseID`=?, `classID`=?, vendorDialog=?, iconID=?, allowedBuildingTypeID=? WHERE `ID`=?");
		setInt(1, con.getContractID());
		setString(2, con.getName());
		setInt(3, con.getMobbaseID());
		setInt(4, con.getClassID());
		setInt(5, (con.getVendorDialog() != null) ? con.getVendorDialog().getObjectUUID() : 0);
		setInt(6, con.getIconID());
		setInt(8, con.getObjectUUID());
		setLong(7, con.getAllowedBuildings().toLong());
		return (executeUpdate() > 0);
	}
}
