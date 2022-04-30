// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import java.sql.ResultSet;
import java.sql.SQLException;


public class MenuOption extends AbstractGameObject {

	private final int menuID;
	private final String message;
	private final int optionID;
	private final int prereq;

	/**
	 * ResultSet Constructor
	 */
	public MenuOption(ResultSet rs) throws SQLException {
		super(rs);
		this.menuID = rs.getInt("menuID");
		this.message = rs.getString("message");
		this.optionID = rs.getInt("optionID");
		this.prereq = rs.getInt("prereq");
	}

	/*
	 * Getters
	 */
	public int getMenuID() {
		return this.menuID;
	}

	public String getMessage() {
		return this.message;
	}

	public int getOptionID() {
		return this.optionID;
	}

	public int getPrereq() {
		return this.prereq;
	}

	/*
	 * Database
	 */
	@Override
	public void updateDatabase() {}
}
