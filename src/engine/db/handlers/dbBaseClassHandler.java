// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.gameManager.DbManager;
import engine.objects.BaseClass;

import java.util.ArrayList;

public class dbBaseClassHandler extends dbHandlerBase {

	public dbBaseClassHandler() {
		this.localClass = BaseClass.class;
		this.localObjectType = Enum.GameObjectType.BaseClass;
	}

	public BaseClass GET_BASE_CLASS(final int id) {

		if (id == 0)
			return null;
		BaseClass baseClass = (BaseClass) DbManager.getFromCache(GameObjectType.BaseClass, id);
		if (baseClass != null)
			return baseClass;


		prepareCallable("SELECT * FROM `static_rune_baseclass` WHERE `ID` = ?;");
		setInt(1, id);
		return (BaseClass) getObjectSingle(id);
	}

	public ArrayList<BaseClass> GET_BASECLASS_FOR_RACE(final int id) {
		prepareCallable("SELECT b.* FROM `static_rune_baseclass` b, `static_rune_racebaseclass` r WHERE b.`ID` = r.`BaseClassID` && r.`RaceID` = ?");
		setInt(1, id);
		return getObjectList();
	}

	public ArrayList<BaseClass> GET_ALL_BASE_CLASSES(){
		prepareCallable("SELECT * FROM `static_rune_baseclass`;");
		return  getObjectList();
	}
}
