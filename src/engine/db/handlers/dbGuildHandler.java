// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum;
import engine.Enum.GuildHistoryType;
import engine.gameManager.DbManager;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class dbGuildHandler extends dbHandlerBase {

	public dbGuildHandler() {
		this.localClass = Guild.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public int BANISH_FROM_GUILD_OFFLINE(final int target, boolean sourceIsGuildLeader) {
		if (!sourceIsGuildLeader)  //one IC cannot banish another IC
			prepareCallable("UPDATE `obj_character` SET `guildUID`=NULL, `guild_isInnerCouncil`=0, `guild_isTaxCollector`=0,"
					+ " `guild_isRecruiter`=0, `guild_isFullMember`=0, `guild_title`=0 WHERE `UID`=? && `guild_isInnerCouncil`=0");
		else
			prepareCallable("UPDATE `obj_character` SET `guildUID`=NULL, `guild_isInnerCouncil`=0, `guild_isTaxCollector`=0,"
					+ " `guild_isRecruiter`=0, `guild_isFullMember`=0, `guild_title`=0 WHERE `UID`=?");
		setLong(1, (long) target);
		return executeUpdate();
	}



	public boolean ADD_TO_BANISHED_FROM_GUILDLIST(int target, long characterID) {
		prepareCallable("INSERT INTO  `dyn_guild_banishlist` (`GuildID`, `CharacterID`) VALUES (?,?)");
		setLong(1, (long) target);
		setLong(2, characterID);
		return (executeUpdate() > 0);
	}

	public boolean REMOVE_FROM_BANISH_LIST(int target, long characterID) {
		prepareCallable("DELETE FROM `dyn_guild_banishlist` (`GuildID`, `CharacterID`) VALUES (?,?)");
		setLong(1, (long) target);
		setLong(2, characterID);
		return (executeUpdate() > 0);
	}

	public boolean ADD_TO_GUILDHISTORY(int target, PlayerCharacter pc, DateTime historyDate, GuildHistoryType historyType) {
		prepareCallable("INSERT INTO  `dyn_character_guildhistory` (`GuildID`, `CharacterID`, `historyDate`, `historyType`) VALUES (?,?,?,?)");
		setLong(1, (long) target);
		setLong(2, pc.getObjectUUID());

		if (historyDate == null)
			setNULL(3, java.sql.Types.DATE);
		else
			setTimeStamp(3, historyDate.getMillis());
		setString(4,historyType.name());
		return (executeUpdate() > 0);
	}

	//TODO Need to get this working.
	public ArrayList<Guild> GET_GUILD_HISTORY_OF_PLAYER(final int id) {
		prepareCallable("SELECT g.* FROM `obj_guild` g, `dyn_character_guildhistory` l WHERE  g.`UID` = l.`GuildID` && l.`CharacterID` = ?");
		setLong(1, (long) id);
		return getObjectList();
	}

	public String GET_GUILD_LIST(int guildType) {

		String newLine = System.getProperty("line.separator");
		String outputStr = null;
		ResultSet resultSet;

		// Setup and execute stored procedure

		prepareCallable("CALL `guild_GETLIST`(?)");
		setInt(1, guildType);
		resultSet = executeQuery();

		// Build formatted string with data from query

		outputStr += newLine;
		outputStr += String.format("%-10s %-30s %-10s %-10s", "UUID", "Name", "GL UUID", "TOL_UUID");
		outputStr += newLine;

		try {

			while (resultSet.next()) {

				outputStr += String.format("%-10d %-30s %-10d %-10d", resultSet.getInt(1),
						resultSet.getString(2), resultSet.getInt(3), resultSet.getInt(4));
				outputStr += newLine;

			}

			// Exception handling

		} catch (SQLException e) {
			Logger.error( e.getMessage());
		} finally {
			closeCallable();
		}

		return outputStr;
	}

	public boolean SET_LAST_WOO_UPDATE(Guild guild, LocalDateTime lastEditTime) {
		prepareCallable("UPDATE `obj_guild` SET `lastWooEditTime`=? WHERE `UID`=?");
		setLocalDateTime(1, lastEditTime);
		setLong(2, (long) guild.getObjectUUID());
		return (executeUpdate() > 0);
	}

	public ArrayList<Guild> GET_GUILD_ALLIES(final int id) {
		prepareCallable("SELECT g.* FROM `obj_guild` g, `dyn_guild_allianceenemylist` l "
				+ "WHERE l.isAlliance = 1 && l.OtherGuildID = g.UID && l.GuildID=?");
		setLong(1, (long) id);
		return getObjectList();

	}

	public static ArrayList<PlayerCharacter> GET_GUILD_BANISHED(final int id) {

		return new ArrayList<>();

		// Bugfix
		// prepareCallable("SELECT * FROM `obj_character`, `dyn_guild_banishlist` WHERE `obj_character.char_isActive` = 1 AND `dyn_guild_banishlist.CharacterID` = `obj_character.UID` AND `obj_character.GuildID`=?");

		//prepareCallable("SELECT * FROM `obj_character` `,` `dyn_guild_banishlist` WHERE obj_character.char_isActive = 1 AND dyn_guild_banishlist.CharacterID = obj_character.UID AND dyn_guild_banishlist.GuildID = ?");
		//setLong(1, (long) id);

		//return getObjectList();
	}

	public ArrayList<Guild> GET_GUILD_ENEMIES(final int id) {
		prepareCallable("SELECT g.* FROM `obj_guild` g, `dyn_guild_allianceenemylist` l "
				+ "WHERE l.isAlliance = 0 && l.OtherGuildID = g.UID && l.GuildID=?");
		setLong(1, (long) id);
		return getObjectList();
	}

	public ArrayList<PlayerCharacter> GET_GUILD_KOS_CHARACTER(final int id) {
		prepareCallable("SELECT c.* FROM `obj_character` c, `dyn_guild_characterkoslist` l WHERE c.`char_isActive` = 1 && l.`KOSCharacterID` = c.`UID` && l.`GuildID`=?");
		setLong(1, (long) id);
		return getObjectList();
	}

	public ArrayList<Guild> GET_GUILD_KOS_GUILD(final int id) {
		prepareCallable("SELECT g.* FROM `obj_guild` g, `dyn_guild_guildkoslist` l "
				+ "WHERE l.KOSGuildID = g.UID && l.GuildID = ?");
		setLong(1, (long) id);
		return getObjectList();
	}

	public ArrayList<Guild> GET_SUB_GUILDS(final int guildID) {
		prepareCallable("SELECT `obj_guild`.*, `object`.`parent` FROM `object` INNER JOIN `obj_guild` ON `obj_guild`.`UID` = `object`.`UID` WHERE `object`.`parent` = ?;");
		setInt(1, guildID);
		return getObjectList();
	}


	public Guild GET_GUILD(int id) {
		Guild guild = (Guild) DbManager.getFromCache(Enum.GameObjectType.Guild, id);
		if (guild != null)
			return guild;
		if (id == 0)
			return Guild.getErrantGuild();
		prepareCallable("SELECT `obj_guild`.*, `object`.`parent` FROM `obj_guild` INNER JOIN `object` ON `object`.`UID` = `obj_guild`.`UID` WHERE `object`.`UID`=?");
		setLong(1, (long) id);
		return (Guild) getObjectSingle(id);
	}
	
	public ArrayList<Guild> GET_ALL_GUILDS() {
		
		prepareCallable("SELECT `obj_guild`.*, `object`.`parent` FROM `obj_guild` INNER JOIN `object` ON `object`.`UID` = `obj_guild`.`UID`");
		
		return getObjectList();
	}

	public boolean IS_CREST_UNIQUE(final GuildTag gt) {
		boolean valid = false;
		if (gt.backgroundColor01 == gt.backgroundColor02) {
			//both background colors the same, ignore backgroundDesign
			prepareCallable("SELECT `name` FROM `obj_guild` WHERE `backgroundColor01`=? && `backgroundColor02`=? && `symbolColor`=? && `symbol`=?;");
			setInt(1, gt.backgroundColor01);
			setInt(2, gt.backgroundColor02);
			setInt(3, gt.symbolColor);
			setInt(4, gt.symbol);
			
		} else {
			prepareCallable("SELECT `name` FROM `obj_guild` WHERE `backgroundColor01`=? && `backgroundColor02`=? && `symbolColor`=? && `backgroundDesign`=? && `symbol`=?;");
			setInt(1, gt.backgroundColor01);
			setInt(2, gt.backgroundColor02);
			setInt(3, gt.symbolColor);
			setInt(4, gt.backgroundDesign);
			setInt(5, gt.symbol);
		}
		try {
			ResultSet rs = executeQuery();
			if (!rs.next())
				valid = true;
			rs.close();
		} catch (SQLException e) {
			Logger.error(e.getMessage());
		}
		return valid;
	}

	public String SET_PROPERTY(final Guild g, String name, Object new_value) {
		prepareCallable("CALL guild_SETPROP(?,?,?)");
		setLong(1, (long) g.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		return getResult();
	}

	public String SET_PROPERTY(final Guild g, String name, Object new_value, Object old_value) {
		prepareCallable("CALL guild_GETSETPROP(?,?,?,?)");
		setLong(1, (long) g.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		setString(4, String.valueOf(old_value));
		return getResult();
	}

	public boolean SET_GUILD_OWNED_CITY(int guildID, int cityID) {
		prepareCallable("UPDATE `obj_guild` SET `ownedCity`=? WHERE `UID`=?");
		setLong(1, (long) cityID);
		setLong(2, (long) guildID);
		return (executeUpdate() > 0);
	}

	public boolean SET_GUILD_LEADER(int objectUUID,int guildID) {
		prepareCallable("UPDATE `obj_guild` SET `leaderUID`=? WHERE `UID`=?");
		setLong(1, (long) objectUUID);
		setLong(2, (long) guildID);
		return (executeUpdate() > 0);
	}


	public boolean IS_NAME_UNIQUE(final String name) {
		boolean valid = false;
		prepareCallable("SELECT `name` FROM `obj_guild` WHERE `name`=?;");
		setString(1, name);
		try {
			ResultSet rs = executeQuery();
			if (!rs.next())
				valid = true;
			rs.close();
		} catch (SQLException e) {
			Logger.warn(e.getMessage());
		}
		return valid;

	}

	public Guild SAVE_TO_DATABASE(Guild g) {
		prepareCallable("CALL `guild_CREATE`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
	
		GuildTag gt = g.getGuildTag();
		if ( gt == null)
			return null;
		setLong(1, MBServerStatics.worldUUID);
		setLong(2, g.getGuildLeaderUUID());
		setString(3, g.getName());
		setInt(4, gt.backgroundColor01);
		setInt(5, gt.backgroundColor02);
		setInt(6, gt.symbolColor);
		setInt(7, gt.backgroundDesign);
		setInt(8 , gt.symbol);
		setInt(9, g.getCharter());
		setString(10, g.getLeadershipType());
		setString(11, g.getMotto());

		int objectUUID = (int) getUUID();
		if (objectUUID > 0)
			return GET_GUILD(objectUUID);
		return null;
	}

	public boolean UPDATE_GUILD_RANK_OFFLINE(int target, int newRank, int guildId) {
		prepareCallable("UPDATE `obj_character` SET `guild_title`=? WHERE `UID`=? && `guildUID`=?");
		setInt(1, newRank);
		setInt(2, target);
		setInt(3, guildId);
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_PARENT(int guildUID, int parentUID) {
		prepareCallable("UPDATE `object` SET `parent`=? WHERE `UID`=?");
		setInt(1, parentUID);
		setInt(2, guildUID);
		return (executeUpdate() > 0);
	}

	public int DELETE_GUILD(final Guild guild) {
		prepareCallable("DELETE FROM `object` WHERE `UID` = ?");
		setLong(1, (long) guild.getObjectUUID());
		return executeUpdate();
	}

	public boolean UPDATE_MINETIME(int guildUID, int mineTime) {
		prepareCallable("UPDATE `obj_guild` SET `mineTime`=? WHERE `UID`=?");
		setInt(1, mineTime);
		setInt(2, guildUID);
		return (executeUpdate() > 0);
	}

	public int UPDATE_GUILD_STATUS_OFFLINE(int target, boolean isInnerCouncil, boolean isRecruiter, boolean isTaxCollector, int guildId) {
		int updateMask = 0;
		prepareCallable("SELECT `guild_isInnerCouncil`, `guild_isTaxCollector`, `guild_isRecruiter` FROM `obj_character` WHERE `UID`=? && `guildUID`=?");
		setLong(1, (long) target);
		setLong(2, (long) guildId);
		try {
			ResultSet rs = executeQuery();

			//If the first query had no results, neither will the second
			if (rs.first()) {
				//Determine what is different
				if (rs.getBoolean("guild_isInnerCouncil") != isInnerCouncil)
					updateMask |= 4;
				if (rs.getBoolean("guild_isRecruiter") != isRecruiter)
					updateMask |= 2;
				if (rs.getBoolean("guild_isTaxCollector") != isTaxCollector)
					updateMask |= 1;
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error( e.toString());
		}
		prepareCallable("UPDATE `obj_character` SET `guild_isInnerCouncil`=?, `guild_isTaxCollector`=?, `guild_isRecruiter`=?, `guild_isFullMember`=? WHERE `UID`=? && `guildUID`=?");
		setBoolean(1, isInnerCouncil);
		setBoolean(2, isRecruiter);
		setBoolean(3, isTaxCollector);
		setBoolean(4, ((updateMask > 0))); //If you are becoming an officer, or where an officer, your a full member...
		setLong(5, (long) target);
		setLong(6, (long) guildId);
		return executeUpdate();

	}


	// *** Refactor: Why are we saving tags/charter in update?
	//               It's not like this shit ever changes.

	public boolean updateDatabase(final Guild g) {
		prepareCallable("UPDATE `obj_guild` SET `name`=?, `backgroundColor01`=?, `backgroundColor02`=?, `symbolColor`=?, `backgroundDesign`=?, `symbol`=?, `charter`=?, `motd`=?, `icMotd`=?, `nationMotd`=?, `leaderUID`=? WHERE `UID`=?");
		setString(1, g.getName());
		setInt(2, g.getGuildTag().backgroundColor01);
		setInt(3, g.getGuildTag().backgroundColor02);
		setInt(4, g.getGuildTag().symbolColor);
		setInt(5, g.getGuildTag().backgroundDesign);
		setInt(6, g.getGuildTag().symbol);
		setInt(7, g.getCharter());
		setString(8, g.getMOTD());
		setString(9, g.getICMOTD());
		setString(10, "");
		setInt(11, g.getGuildLeaderUUID());
		setLong(12, (long) g.getObjectUUID());
		return (executeUpdate() != 0);
	}
	public boolean ADD_TO_ALLIANCE_LIST(final long sourceGuildID, final long targetGuildID, boolean isRecommended, boolean isAlly, String recommender) {
		prepareCallable("INSERT INTO `dyn_guild_allianceenemylist` (`GuildID`, `OtherGuildID`,`isRecommended`, `isAlliance`, `recommender`) VALUES (?,?,?,?,?)");
		setLong(1, sourceGuildID);
		setLong(2, targetGuildID);
		setBoolean(3, isRecommended);
		setBoolean(4, isAlly);
		setString(5, recommender);
		return (executeUpdate() > 0);
	}

	public boolean REMOVE_FROM_ALLIANCE_LIST(final long sourceGuildID, long targetGuildID) {
		prepareCallable("DELETE FROM `dyn_guild_allianceenemylist` WHERE `GuildID`=? AND `OtherGuildID`=?");
		setLong(1, sourceGuildID);
		setLong(2, targetGuildID);
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_RECOMMENDED(final long sourceGuildID, long targetGuildID) {
		prepareCallable("UPDATE `dyn_guild_allianceenemylist` SET `isRecommended` = ? WHERE `GuildID`=? AND `OtherGuildID`=?");
		setByte(1,(byte)0);
		setLong(2, sourceGuildID);
		setLong(3, targetGuildID);
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_ALLIANCE(final long sourceGuildID, long targetGuildID, boolean isAlly) {
		prepareCallable("UPDATE `dyn_guild_allianceenemylist` SET `isAlliance` = ? WHERE `GuildID`=? AND `OtherGuildID`=?");
		setBoolean(1,isAlly);
		setLong(2, sourceGuildID);
		setLong(3, targetGuildID);
		return (executeUpdate() > 0);
	}

	public boolean UPDATE_ALLIANCE_AND_RECOMMENDED(final long sourceGuildID, long targetGuildID, boolean isAlly) {
		prepareCallable("UPDATE `dyn_guild_allianceenemylist` SET `isRecommended` = ?, `isAlliance` = ? WHERE `GuildID`=? AND `OtherGuildID`=?");
		setByte(1,(byte)0);
		setBoolean(2,isAlly);
		setLong(3, sourceGuildID);
		setLong(4, targetGuildID);

		return (executeUpdate() > 0);
	}

	public void LOAD_ALL_ALLIANCES_FOR_GUILD(Guild guild) {

		if (guild == null)
			return;

		prepareCallable("SELECT * FROM `dyn_guild_allianceenemylist` WHERE `GuildID` = ?");
		setInt(1,guild.getObjectUUID());

		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {
				GuildAlliances guildAlliance = new GuildAlliances(rs);
				guild.guildAlliances.put(guildAlliance.getAllianceGuild(), guildAlliance);
			}


		} catch (SQLException e) {
			Logger.error( e.getMessage());
		} finally {
			closeCallable();
		}

	}

	public void LOAD_GUILD_HISTORY_FOR_PLAYER(PlayerCharacter pc) {

		if (pc == null)
			return;

		prepareCallable("SELECT * FROM `dyn_character_guildhistory` WHERE `CharacterID` = ?");
		setInt(1,pc.getObjectUUID());

		try {
			ArrayList<GuildHistory> tempList = new ArrayList<>();
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {
				GuildHistory guildHistory = new GuildHistory(rs);
				tempList.add(guildHistory);
			}

			pc.setGuildHistory(tempList);


		} catch (SQLException e) {
			Logger.error(e.getMessage());
		} finally {
			closeCallable();
		}

	}
	
	//TODO uncomment this when finished with guild history warehouse integration
//	public HashMap<Integer, GuildRecord> GET_WAREHOUSE_GUILD_HISTORY(){
//		
//		HashMap<Integer, GuildRecord> tempMap = new HashMap<>();
//		prepareCallable("SELECT * FROM `warehouse_guildhistory` WHERE `eventType` = 'CREATE'");
//		try {
//			ResultSet rs = executeQuery();
//			
//			while (rs.next()) {
//				GuildRecord guildRecord = new GuildRecord(rs);
//				tempMap.put(guildRecord.guildID, guildRecord);
//			}
//		}catch (Exception e){
//			Logger.error(e);
//		}
//		return tempMap;
//		
//	}


}
