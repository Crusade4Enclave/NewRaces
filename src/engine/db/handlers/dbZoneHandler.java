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
import engine.objects.Zone;

import java.sql.ResultSet;
import java.util.ArrayList;

public class dbZoneHandler extends dbHandlerBase {

	public dbZoneHandler() {
		this.localClass = Zone.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public ArrayList<Zone> GET_ALL_NODES(Zone zone) {
		ArrayList<Zone> wsmList = new ArrayList<>();
		wsmList.addAll(zone.getNodes());
		if (zone.absX == 0.0f) {
			zone.absX = zone.getXCoord();
		}
		if (zone.absY == 0.0f) {
			zone.absY = zone.getYCoord();
		}
		if (zone.absZ == 0.0f) {
			zone.absZ = zone.getZCoord();
		}
		for (Zone child : zone.getNodes()) {
			child.absX = child.getXCoord() + zone.absX;
			child.absY = child.getYCoord() + zone.absY;
			child.absZ = child.getZCoord() + zone.absZ;
			wsmList.addAll(this.GET_ALL_NODES(child));
		}
		return wsmList;
	}

	public Zone GET_BY_UID(long ID) {

		Zone zone = (Zone) DbManager.getFromCache(Enum.GameObjectType.Zone, (int)ID);
		if (zone != null)
			return zone;
		prepareCallable("SELECT `obj_zone`.*, `object`.`parent` FROM `object` INNER JOIN `obj_zone` ON `obj_zone`.`UID` = `object`.`UID` WHERE `object`.`UID` = ?;");
		setLong(1, ID);
		return (Zone) getObjectSingle((int) ID);
	}

	public ArrayList<Zone> GET_MAP_NODES(final int objectUUID) {
		prepareCallable("SELECT `obj_zone`.*, `object`.`parent` FROM `object` INNER JOIN `obj_zone` ON `obj_zone`.`UID` = `object`.`UID` WHERE `object`.`parent` = ?;");
		setLong(1, (long) objectUUID);
		return getObjectList();
	}

	public ResultSet GET_ZONE_EXTENTS(final int loadNum) {
		prepareCallable("SELECT * FROM `static_zone_size` WHERE `loadNum`=?;");
		setInt(1, loadNum);
		return executeQuery();
	}

	public String SET_PROPERTY(final Zone z, String name, Object new_value) {
		prepareCallable("CALL zone_SETPROP(?,?,?)");
		setLong(1, (long) z.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		return getResult();
	}

	public String SET_PROPERTY(final Zone z, String name, Object new_value, Object old_value) {
		prepareCallable("CALL zone_GETSETPROP(?,?,?,?)");
		setLong(1, (long) z.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		setString(4, String.valueOf(old_value));
		return getResult();
	}

	public boolean DELETE_ZONE(final Zone zone) {

		prepareCallable("DELETE FROM `object` WHERE `UID` = ? AND `type` = 'zone'");
		setInt(1, zone.getObjectUUID());
		return (executeUpdate() != 0);
	}

}
