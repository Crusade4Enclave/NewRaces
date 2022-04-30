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
import engine.objects.VendorDialog;

public class dbVendorDialogHandler extends dbHandlerBase {

	public dbVendorDialogHandler() {
		this.localClass = VendorDialog.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public VendorDialog GET_VENDORDIALOG(final int objectUUID) {
		VendorDialog vd = (VendorDialog) DbManager.getFromCache(Enum.GameObjectType.VendorDialog, objectUUID);
		if (vd != null)
			return vd;
		prepareCallable("SELECT * FROM `static_npc_vendordialog` WHERE `ID`=?");
		setInt(1, objectUUID);
		return (VendorDialog) getObjectSingle(objectUUID);
	}
}
