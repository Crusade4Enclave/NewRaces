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
import java.util.ArrayList;
import java.util.HashMap;




public class GuildCondemn  {

	private int ID;
	private int playerUID;
	private int parentGuildUID;
	private int guildUID;
	private int friendType;
	public static HashMap<Integer,ArrayList<GuildCondemn>> GetCondemnedFromGuildID = new HashMap<>();


	/**
	 * ResultSet Constructor
	 */
	public GuildCondemn(ResultSet rs) throws SQLException {
		this.playerUID = rs.getInt("playerUID");
		this.parentGuildUID = rs.getInt("buildingUID");
		this.guildUID = rs.getInt("guildUID");
		this.friendType = rs.getInt("friendType");
	}
	
	


	public GuildCondemn(int playerUID, int parentGuildUID, int guildUID, int friendType) {
		super();
		this.playerUID = playerUID;
		this.parentGuildUID = parentGuildUID;
		this.guildUID = guildUID;
		this.friendType = friendType;
	}




	public int getPlayerUID() {
		return playerUID;
	}


	public int getParentGuildUID() {
		return parentGuildUID;
	}


	public int getGuildUID() {
		return guildUID;
	}


	public int getFriendType() {
		return friendType;
	}



	
	
}
