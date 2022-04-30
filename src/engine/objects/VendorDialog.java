// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.DbManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class VendorDialog extends AbstractGameObject {

	private final String dialogType;
	private final String intro;
	private ArrayList<MenuOption> options = new ArrayList<>();

	public VendorDialog(String dialogType, String intro, int UUID) {
		super(UUID);
		this.dialogType = dialogType;
		this.intro = intro;
	}

	/**
	 * ResultSet Constructor
	 */
	public VendorDialog(ResultSet rs) throws SQLException {
		super(rs);
		this.dialogType = rs.getString("dialogType");
		this.intro = rs.getString("intro");
		this.options = DbManager.MenuQueries.GET_MENU_OPTIONS(this.getObjectUUID());
	}

	/*
	 * Getters
	 */
	public String getDialogType() {
		return this.dialogType;
	}

	public String getIntro() {
		return this.intro;
	}

	public ArrayList<MenuOption> getOptions() {
		return this.options;
	}

	private static VendorDialog vd;
	public static VendorDialog getHostileVendorDialog() {
		if (VendorDialog.vd == null)
			VendorDialog.vd = new VendorDialog("TrainerDialog", "HostileIntro", 0);
		return VendorDialog.vd;
	}


	/*
	 * Database
	 */
	@Override
	public void updateDatabase() {}

	public static VendorDialog getVendorDialog(int id) {
		
		return DbManager.VendorDialogQueries.GET_VENDORDIALOG(id);
	}
}
