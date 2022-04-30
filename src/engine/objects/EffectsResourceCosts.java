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


public class EffectsResourceCosts extends AbstractGameObject {

	private String IDString;
	private int resourceID;
	private int amount;
	private int UID;

	/**
	 * No Table ID Constructor
	 */
	public EffectsResourceCosts() {

	}

	/**
	 * ResultSet Constructor
	 */
	public EffectsResourceCosts(ResultSet rs) throws SQLException {

		this.UID = rs.getInt("UID");
		this.IDString = rs.getString("IDString");
		this.resourceID = rs.getInt("resource");
		this.amount = rs.getInt("amount");
	}

	

	public String getIDString() {
		return this.IDString;
	}


	
	public int getAmount() {
		return this.amount;
	}

	

	public int getResourceID() {
		return resourceID;
	}

	@Override
	public void removeFromCache() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateDatabase() {
		// TODO Auto-generated method stub
		
	}

	public int getUID() {
		return UID;
	}

	
}
