// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.archive;

import engine.Enum;
import engine.Enum.RecordEventType;
import engine.objects.Guild;
import engine.workthreads.WarehousePushThread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class GuildRecord extends DataRecord {

	private static final LinkedBlockingQueue<GuildRecord> recordPool = new LinkedBlockingQueue<>();
	private Enum.RecordEventType eventType;
	private Guild guild;
	public String guildHash;
	private String guildName;
	private String charterName;
	private String GLHash;
	private String guildMotto;
	private int bgIcon;
	private int bgColour1;
	private int bgColour2;
	private int fgIcon;
	private int fgColour;
	public int guildID;

	private java.time.LocalDateTime  eventDatetime;
	
	public static HashMap<Integer, GuildRecord> GuildRecordCache = null;

	private GuildRecord(Guild guild) {
		this.recordType = Enum.DataRecordType.GUILD;
		this.guild = guild;
		this.eventType = Enum.RecordEventType.CREATE;
	}
	
	

	public GuildRecord(ResultSet rs) throws SQLException {
		super();
		this.eventType = RecordEventType.valueOf(rs.getString("eventType"));
		this.guildHash = rs.getString("guild_id");
		this.guildName = rs.getString("guild_name");
		this.charterName = rs.getString("charter");
		GLHash = rs.getString("guild_founder");
		this.guildMotto = rs.getString("guild_motto");
		this.bgIcon = rs.getInt("bgicon");
		this.bgColour1 = rs.getInt("bgcoloura");
		this.bgColour2 = rs.getInt("bgcolourb");
		this.fgIcon = rs.getInt("fgicon");
		this.fgColour = rs.getInt("fgcolour");

		java.sql.Timestamp eventTimeStamp = rs.getTimestamp("upgradeDate");

		if (eventTimeStamp != null)
			this.eventDatetime = LocalDateTime.ofInstant(eventTimeStamp.toInstant(), ZoneId.systemDefault());
	}



	public static GuildRecord borrow(Guild guild, Enum.RecordEventType eventType) {
		GuildRecord guildRecord;
		//add
		guildRecord = recordPool.poll();

		if (guildRecord == null) {
			guildRecord = new GuildRecord(guild);
			guildRecord.eventType = eventType;
		}
		else {
			guildRecord.guild = guild;
			guildRecord.recordType = Enum.DataRecordType.GUILD;
			guildRecord.eventType = eventType;

		}

		guildRecord.guildHash = guildRecord.guild.getHash();
		guildRecord.guildID = guildRecord.guild.getObjectUUID();
		guildRecord.guildName = guildRecord.guild.getName();
		guildRecord.charterName = Enum.GuildType.getGuildTypeFromInt(guildRecord.guild.getCharter()).getCharterName();

		guildRecord.GLHash = DataWarehouse.hasher.encrypt(guildRecord.guild.getGuildLeaderUUID());

		guildRecord.guildMotto = guildRecord.guild.getMotto();
		guildRecord.bgIcon = guildRecord.guild.getBgDesign();
		guildRecord.bgColour1 = guildRecord.guild.getBgc1();
		guildRecord.bgColour2 = guildRecord.guild.getBgc2();
		guildRecord.fgIcon = guildRecord.guild.getSymbol();
		guildRecord.fgColour = guildRecord.guild.getSc();

		if (guild.getOwnedCity() != null)
            guildRecord.eventDatetime =  guild.getOwnedCity().established;
		else
			guildRecord.eventDatetime = LocalDateTime.now();

		return guildRecord;
	}

	public static PreparedStatement buildGuildPushStatement(Connection connection, ResultSet rs) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "INSERT INTO `warehouse_guildhistory` (`event_number`, `guild_id`, `guild_name`, `guild_motto`, `guild_founder`, `charter`, `bgicon`, `bgcoloura`, `bgcolourb`, `fgicon`, `fgcolour`, `eventtype`, `datetime`) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

		outStatement = connection.prepareStatement(queryString);

		// Bind record data

		outStatement.setInt(1, rs.getInt("event_number"));
		outStatement.setString(2, rs.getString("guild_id"));
		outStatement.setString(3, rs.getString("guild_name"));
		outStatement.setString(4, rs.getString("guild_motto"));
		outStatement.setString(5, rs.getString("guild_founder"));
		outStatement.setString(6, rs.getString("charter"));
		outStatement.setInt(7, rs.getInt("bgicon"));
		outStatement.setInt(8, rs.getInt("bgcoloura"));
		outStatement.setInt(9, rs.getInt("bgcolourb"));
		outStatement.setInt(10, rs.getInt("fgicon"));
		outStatement.setInt(11, rs.getInt("fgcolour"));
		outStatement.setString(12, rs.getString("eventtype"));
		outStatement.setTimestamp(13, rs.getTimestamp("datetime"));

		return outStatement;
	}

	public static PreparedStatement buildGuildQueryStatement(Connection connection) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "SELECT * FROM `warehouse_guildhistory` WHERE `event_number` > ?";
		outStatement = connection.prepareStatement(queryString);
		outStatement.setInt(1, WarehousePushThread.guildIndex);
		return outStatement;
	}

	void reset() {

		this.guild = null;
		this.guildHash = null;
		this.GLHash = null;
		this.guildMotto = null;
		this.charterName = null;
		this.eventDatetime = null;
	}

	public void release() {
		this.reset();
		recordPool.add(this);
	}

	public void write() {

		try (Connection connection = DataWarehouse.connectionPool.getConnection();
				PreparedStatement statement = this.buildGuildInsertStatement(connection)) {

			statement.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private PreparedStatement buildGuildInsertStatement(Connection connection) throws SQLException {

		PreparedStatement outStatement = null;
		String queryString = "INSERT INTO `warehouse_guildhistory` (`guild_id`, `guild_name`, `guild_motto`, `guild_founder`, `charter`, `bgicon`, `bgcoloura`, `bgcolourb`, `fgicon`, `fgcolour`, `eventtype`, `datetime`) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

		outStatement = connection.prepareStatement(queryString);

		// Bind character data

		outStatement.setString(1, this.guildHash);
		outStatement.setString(2, this.guildName);
		outStatement.setString(3, this.guildMotto);
		outStatement.setString(4, this.GLHash);
		outStatement.setString(5, this.charterName);

		outStatement.setInt(6, this.bgIcon);
		outStatement.setInt(7, this.bgColour1);
		outStatement.setInt(8, this.bgColour2);
		outStatement.setInt(9, this.fgIcon);
		outStatement.setInt(10, this.fgColour);
		outStatement.setString(11, this.eventType.name());
		outStatement.setTimestamp(12, new java.sql.Timestamp(	this.eventDatetime.atZone(ZoneId.systemDefault())
				.toInstant().toEpochMilli()));

		return outStatement;
	}
	
//	public static void InitializeGuildRecords(){
//		GuildRecord.GuildRecordCache = DbManager.GuildQueries.GET_WAREHOUSE_GUILD_HISTORY();
//	}
}







