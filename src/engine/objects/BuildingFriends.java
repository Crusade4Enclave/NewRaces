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

public class BuildingFriends  {

	private int playerUID;
	private int buildingUID;
	private int guildUID;
	private int friendType;

	/**
	 * ResultSet Constructor
	 */

	public BuildingFriends(ResultSet rs) throws SQLException {
		this.playerUID = rs.getInt("playerUID");
		this.buildingUID = rs.getInt("buildingUID");
		this.guildUID = rs.getInt("guildUID");
		this.friendType = rs.getInt("friendType");
	}

	public BuildingFriends(int playerUID, int buildingUID, int guildUID, int friendType) {
		super();
		this.playerUID = playerUID;
		this.buildingUID = buildingUID;
		this.guildUID = guildUID;
		this.friendType = friendType;
	}

	public int getPlayerUID() {
		return playerUID;
	}
	public int getGuildUID() {
		return guildUID;
	}
	public int getFriendType() {
		return friendType;
	}

}
