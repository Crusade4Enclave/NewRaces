// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.RuneBaseAttribute;

import java.util.ArrayList;

public class dbRuneBaseAttributeHandler extends dbHandlerBase {

	public dbRuneBaseAttributeHandler() {
		this.localClass = RuneBaseAttribute.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public ArrayList<RuneBaseAttribute> GET_ATTRIBUTES_FOR_RUNEBASE(int id) {
		prepareCallable("SELECT * FROM `static_rune_runebaseattribute` WHERE `RuneBaseID`=?");
		setInt(1, id);
		return getObjectList();
	}

	public ArrayList<RuneBaseAttribute> GET_ATTRIBUTES_FOR_RUNEBASE() {
		prepareCallable("SELECT * FROM `static_rune_runebaseattribute`");
		return getObjectList();
	}


}
