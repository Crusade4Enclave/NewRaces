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
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.City;
import engine.objects.Zone;
import org.pmw.tinylog.Logger;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class dbCityHandler extends dbHandlerBase {

	public dbCityHandler() {
		this.localClass = City.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public ArrayList<AbstractGameObject> CREATE_CITY(int ownerID, int parentZoneID, int realmID, float xCoord, float yCoord, float zCoord, float rotation, float W, String name, LocalDateTime established) {
		prepareCallable("CALL `city_CREATE`(?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)");
		LocalDateTime upgradeTime = LocalDateTime.now().plusHours(2);
		setLong(1, (long) ownerID); //objectUUID of owning player
		setLong(2, (long) parentZoneID); //objectUUID of parent (continent) zone
		setLong(3, (long) realmID); //objectUUID of realm city belongs in
		setFloat(4, xCoord); //xOffset from parentZone center
		setFloat(5, yCoord); //yOffset from parentZone center
		setFloat(6, zCoord); //zOffset from parentZone center
		setString(7, name); //city name
		setLocalDateTime(8, established);
		setFloat(9, rotation);
		setFloat(10, W);
		setLocalDateTime(11, upgradeTime);
		ArrayList<AbstractGameObject> list = new ArrayList<>();

		try {
			boolean work = execute();
			if (work) {
				ResultSet rs = this.cs.get().getResultSet();
				while (rs.next()) {
					addObject(list, rs);
				}
				rs.close();
			} else {
				Logger.info("City Placement Failed: " + this.cs.get().toString());
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
			Logger.info("City Placement Failed, SQLException: " + this.cs.get().toString() + e.toString());
			return list; //city creation failure
		} catch (UnknownHostException e) {
			Logger.info("City Placement Failed, UnknownHostException: " + this.cs.get().toString());
			return list; //city creation failure
		} finally {
			closeCallable();
		}
		return list;
	}

	public static void addObject(ArrayList<AbstractGameObject> list, ResultSet rs) throws SQLException, UnknownHostException {
		String type = rs.getString("type");
		switch (type) {
		case "zone":
			Zone zone = new Zone(rs);
			DbManager.addToCache(zone);
			list.add(zone);
			break;
		case "building":
			Building building = new Building(rs);
			DbManager.addToCache(building);
			list.add(building);
			break;
		case "city":
			City city = new City(rs);
			DbManager.addToCache(city);
			list.add(city);
			break;
		}
	}

	public ArrayList<City> GET_CITIES_BY_ZONE(final int objectUUID) {
		prepareCallable("SELECT `obj_city`.*, `object`.`parent` FROM `obj_city` INNER JOIN `object` ON `object`.`UID` = `obj_city`.`UID` WHERE `object`.`parent`=?;");
		setLong(1, (long) objectUUID);

        return getObjectList();
	}

	public City GET_CITY(final int cityId) {
		City city = (City) DbManager.getFromCache(Enum.GameObjectType.City, cityId);
		if (city != null)
			return city;
		prepareCallable("SELECT `obj_city`.*, `object`.`parent` FROM `obj_city` INNER JOIN `object` ON `object`.`UID` = `obj_city`.`UID` WHERE `object`.`UID`=?;");
		setLong(1, (long) cityId);
		city = (City) getObjectSingle(cityId);
		return city;
	}

	public String SET_PROPERTY(final City c, String name, Object new_value) {
		prepareCallable("CALL city_SETPROP(?,?,?)");
		setLong(1, (long) c.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		return getResult();
	}

	public String SET_PROPERTY(final City c, String name, Object new_value, Object old_value) {
		prepareCallable("CALL city_GETSETPROP(?,?,?,?)");
		setLong(1, (long) c.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		setString(4, String.valueOf(old_value));
		return getResult();
	}

	public boolean updateforceRename(City city, boolean value) {

		prepareCallable("UPDATE `obj_city` SET `forceRename`=?"
				+ " WHERE `UID` = ?");
		setByte(1, (value == true) ? (byte) 1 : (byte) 0);
		setInt(2, city.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean updateOpenCity(City city, boolean value) {

		prepareCallable("UPDATE `obj_city` SET `open`=?"
				+ " WHERE `UID` = ?");
		setByte(1, (value == true) ? (byte) 1 : (byte) 0);
		setInt(2, city.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean updateTOL(City city, int tolID) {

		prepareCallable("UPDATE `obj_city` SET `treeOfLifeUUID`=?"
				+ " WHERE `UID` = ?");
		setInt(1,tolID);
		setInt(2, city.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean renameCity(City city, String name) {

		prepareCallable("UPDATE `obj_city` SET `name`=?"
				+ " WHERE `UID` = ?");
		setString(1, name);
		setInt(2, city.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean updateSiegesWithstood(City city, int value) {

		prepareCallable("UPDATE `obj_city` SET `siegesWithstood`=?"
				+ " WHERE `UID` = ?");
		setInt(1, value);
		setInt(2, city.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean updateRealmTaxDate(City city, LocalDateTime localDateTime) {

		prepareCallable("UPDATE `obj_city` SET `realmTaxDate` =?"
				+ " WHERE `UID` = ?");
		setLocalDateTime(1, localDateTime);
		setInt(2,city.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean DELETE_CITY(final City city) {

		prepareCallable("DELETE FROM `object` WHERE `UID` = ? AND `type` = 'city'");
		setInt(1, city.getObjectUUID());
		return (executeUpdate() != 0);
	}

}
