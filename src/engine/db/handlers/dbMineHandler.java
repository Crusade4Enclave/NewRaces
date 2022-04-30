// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





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
import engine.objects.Mine;
import engine.objects.MineProduction;
import engine.objects.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class dbMineHandler extends dbHandlerBase {

	public dbMineHandler() {
		this.localClass = Mine.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public Mine GET_MINE(int id) {

		if (id == 0)
			return null;

		Mine mine = (Mine) DbManager.getFromCache(Enum.GameObjectType.Mine, id);
		if (mine != null)
			return mine;

		prepareCallable("SELECT `obj_building`.*, `object`.`parent` FROM `object` INNER JOIN `obj_building` ON `obj_building`.`UID` = `object`.`UID` WHERE `object`.`UID` = ?;");

		setLong(1, (long) id);
		return (Mine) getObjectSingle(id);

	}

	public ArrayList<Mine> GET_ALL_MINES_FOR_SERVER() {
		prepareCallable("SELECT `obj_mine`.*, `object`.`parent` FROM `object` INNER JOIN `obj_mine` ON `obj_mine`.`UID` = `object`.`UID`");
		return getObjectList();
	}

	public boolean CHANGE_OWNER(Mine mine, int playerUID) {
		prepareCallable("UPDATE `obj_mine` SET `mine_ownerUID`=? WHERE `UID`=?");
		setInt(1, playerUID);
		setLong(2, (long) mine.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean CHANGE_RESOURCE(Mine mine, Resource resource) {
		prepareCallable("UPDATE `obj_mine` SET `mine_resource`=? WHERE `UID`=?");
		setString(1, resource.name());
		setLong(2, (long) mine.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean CHANGE_TYPE(Mine mine, MineProduction productionType) {
		prepareCallable("UPDATE `obj_mine` SET `mine_type`=? WHERE `UID`=?");
		setString(1, productionType.name());
		setLong(2, (long) mine.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean CHANGE_MINE_TIME(Mine mine, LocalDateTime mineOpenTime) {
		prepareCallable("UPDATE `obj_mine` SET `mine_openDate`=? WHERE `UID`=?");
		setLocalDateTime(1, mineOpenTime);
		setLong(2, (long) mine.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public boolean SET_FLAGS(Mine mine, int newFlags) {
		prepareCallable("UPDATE `obj_mine` SET `flags`=? WHERE `UID`=?");
		setInt(1, newFlags);
		setLong(2, (long) mine.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public String SET_PROPERTY(final Mine m, String name, Object new_value) {
		prepareCallable("CALL mine_SETPROP(?,?,?)");
		setLong(1, (long) m.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		return getResult();
	}

	// Advance all the mine windows respective to the current day
	// at boot time.  This ensures that mines always go live
	// no matter what date in the database

	public String SET_PROPERTY(final Mine m, String name, Object new_value, Object old_value) {
		prepareCallable("CALL mine_GETSETPROP(?,?,?,?)");
		setLong(1, (long) m.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		setString(4, String.valueOf(old_value));
		return getResult();
	}

}
