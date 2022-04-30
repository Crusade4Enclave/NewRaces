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




public class Condemned  {

	private int ID;
	private int playerUID;
	private int parent;
	private int guildUID;
	private int friendType;
	private boolean active;
	public static final int INDIVIDUAL = 2;
	public static final int GUILD = 4;
	public static final int NATION = 5;

	



	/**
	 * ResultSet Constructor
	 */
	public Condemned(ResultSet rs) throws SQLException {
		this.playerUID = rs.getInt("playerUID");
		this.parent = rs.getInt("buildingUID");
		this.guildUID = rs.getInt("guildUID");
		this.friendType = rs.getInt("friendType");
		this.active = rs.getBoolean("active");
	}
	
	


	public Condemned(int playerUID, int parent, int guildUID, int friendType) {
		super();
		this.playerUID = playerUID;
		this.parent = parent;
		this.guildUID = guildUID;
		this.friendType = friendType;
		this.active = false;
	}




	public int getPlayerUID() {
		return playerUID;
	}


	public int getParent() {
		return parent;
	}


	public int getGuildUID() {
		return guildUID;
	}


	public int getFriendType() {
		return friendType;
	}

	public boolean isActive() {
		return active;
	}




	public boolean setActive(boolean active) {
		if (!DbManager.BuildingQueries.updateActiveCondemn(this, active))
			return false;
		this.active = active;
		return true;
	}


	
	
}
