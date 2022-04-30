// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;


import engine.Enum.GameObjectType;
import engine.Enum.GuildHistoryType;
import engine.net.ByteBufferWriter;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;


public class GuildHistory {


	private int guildID;
	private String guildName;
	private DateTime time;
	private GuildHistoryType historyType;




	public GuildHistory( int guildID, String guildName,
			DateTime dateTime, GuildHistoryType historyType ) {
		super();
		this.guildID = guildID;
		this.guildName = guildName;
		this.time = dateTime;
		this.historyType = historyType;

	}

	public GuildHistoryType getHistoryType() {
		return historyType;
	}

	public GuildHistory(ResultSet rs) throws SQLException {
		java.util.Date sqlDateTime;
		this.guildID = rs.getInt("guildID");
		Guild guild = Guild.getGuild(this.guildID);
		if (guild != null)
			this.guildName = guild.getName();
		else
			this.guildName = "Guild Not Found";

		sqlDateTime = rs.getTimestamp("historyDate");
		if (sqlDateTime != null)
			this.time = new DateTime(sqlDateTime);
		else
			this.time = DateTime.now().minusYears(1);
		this.historyType = GuildHistoryType.valueOf(rs.getString("historyType"));
	}


	public long getGuildID() {
		return guildID;
	}

	public String getGuildName() {
		return guildName;
	}



	public void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.historyType.getType());
		writer.putInt(GameObjectType.Guild.ordinal());
		writer.putInt(this.guildID);
		writer.putString(guildName);
		writer.putInt(0);	//Pad
		writer.putDateTime(this.time);
	}

	public DateTime getTime() {
		return time;
	}

	public void setTime(DateTime time) {
		this.time = time;
	}



}
