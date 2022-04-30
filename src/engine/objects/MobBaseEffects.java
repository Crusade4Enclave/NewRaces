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


public class MobBaseEffects  {

	private int mobBaseID;
	private int token;
	private int rank;
	private int reqLvl;
	


	/**
	 * ResultSet Constructor
	 */
	public MobBaseEffects(ResultSet rs) throws SQLException {
		this.token = rs.getInt("token");
		this.rank = rs.getInt("rank");
		this.reqLvl = rs.getInt("reqLvl");
	}


	/**
	 * @return the mobBaseID
	 */
	public int getMobBaseID() {
		return mobBaseID;
	}



	public void setMobBaseID(int mobBaseID) {
		this.mobBaseID = mobBaseID;
	}


	public int getToken() {
		return token;
	}


	public int getRank() {
		return rank;
	}



	public int getReqLvl() {
		return reqLvl;
	}



}
