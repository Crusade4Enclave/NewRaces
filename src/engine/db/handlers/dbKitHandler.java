// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.Kit;

import java.util.ArrayList;

public class dbKitHandler extends dbHandlerBase {

	public dbKitHandler() {
		this.localClass = Kit.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public ArrayList<Kit> GET_KITS_FOR_RACE_AND_BASECLASS(int raceID, int baseClassID) {
		prepareCallable("SELECT vk.* FROM `static_rune_validkit` vk, `static_rune_racebaseclass` rbc WHERE rbc.`RaceID` = ? "
				+ "&& rbc.`BaseClassID` = ? && rbc.`ID` = vk.`RaceBaseClassesID`");
		setInt(1, raceID);
		setInt(2, baseClassID);
		return getObjectList();
	}

	public ArrayList<Kit> GET_ALL_KITS() {
		prepareCallable("SELECT * FROM `static_rune_validkit`");

		return getObjectList();
	}
}
