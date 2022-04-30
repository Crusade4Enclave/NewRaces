// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum;
import engine.gameManager.DbManager;
import engine.objects.AbstractWorldObject;
import engine.objects.Heraldry;
import engine.objects.PlayerCharacter;
import engine.objects.PlayerFriends;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class dbPlayerCharacterHandler extends dbHandlerBase {

	public dbPlayerCharacterHandler() {
		this.localClass = PlayerCharacter.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public PlayerCharacter ADD_PLAYER_CHARACTER(final PlayerCharacter toAdd) {
		if (toAdd.getAccount() == null) {
			return null;
		}
		prepareCallable("CALL `character_CREATE`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		setLong(1, toAdd.getAccount().getObjectUUID());
		setString(2, toAdd.getFirstName());
		setString(3, toAdd.getLastName());
		setInt(4, toAdd.getRace().getRaceRuneID());
		setInt(5, toAdd.getBaseClass().getObjectUUID());
		setInt(6, toAdd.getStrMod());
		setInt(7, toAdd.getDexMod());
		setInt(8, toAdd.getConMod());
		setInt(9, toAdd.getIntMod());
		setInt(10, toAdd.getSpiMod());
		setInt(11, toAdd.getExp());
		setInt(12, toAdd.getSkinColor());
		setInt(13, toAdd.getHairColor());
		setByte(14, toAdd.getHairStyle());
		setInt(15, toAdd.getBeardColor());
		setByte(16, toAdd.getBeardStyle());

		int objectUUID = (int) getUUID();
		if (objectUUID > 0) {
			return GET_PLAYER_CHARACTER(objectUUID);
		}
		return null;
	}

	public boolean SET_IGNORE_LIST(int sourceID, int targetID, boolean toIgnore, String charName) {
		if (toIgnore) {
			//Add to ignore list
			prepareCallable("INSERT INTO `dyn_character_ignore` (`accountUID`, `ignoringUID`, `characterName`) VALUES (?, ?, ?)");
			setLong(1, (long) sourceID);
			setLong(2, (long) targetID);
			setString(3, charName);
			return (executeUpdate() > 0);
		} else {
			//delete from ignore list
			prepareCallable("DELETE FROM `dyn_character_ignore` WHERE `accountUID` = ? && `ignoringUID` = ?");
			setLong(1, (long) sourceID);
			setLong(2, (long) targetID);
			return (executeUpdate() > 0);
		}
	}

	public static boolean DELETE_CHARACTER_IGNORE(final PlayerCharacter pc, final ArrayList<Integer> toDelete) {

		return false;
	}

	public ArrayList<PlayerCharacter> GET_ALL_PLAYERCHARACTERS() {
		prepareCallable("SELECT * FROM `obj_character`");
		return getObjectList();
	}

	public ArrayList<PlayerCharacter> GET_CHARACTERS_FOR_ACCOUNT(final int id, boolean forceFromDB) {
		prepareCallable("SELECT `obj_character`.*, `object`.`parent` FROM `object` INNER JOIN `obj_character` ON `obj_character`.`UID` = `object`.`UID` WHERE `object`.`parent`=? && `obj_character`.`char_isActive`='1';");
		setLong(1, (long) id);
		return getObjectList(10, forceFromDB);
	}

	public ArrayList<PlayerCharacter> GET_CHARACTERS_FOR_ACCOUNT(final int id) {
		prepareCallable("SELECT `obj_character`.*, `object`.`parent` FROM `object` INNER JOIN `obj_character` ON `obj_character`.`UID` = `object`.`UID` WHERE `object`.`parent`=? && `obj_character`.`char_isActive`='1';");
		setLong(1, (long) id);
		return getObjectList();
	}

	public ArrayList<PlayerCharacter> GET_ALL_CHARACTERS() {
		prepareCallable("SELECT `obj_character`.*, `object`.`parent` FROM `object` INNER JOIN `obj_character` ON `obj_character`.`UID` = `object`.`UID` WHERE `obj_character`.`char_isActive`='1';");
		return getObjectList();
	}

	/**
	 *
	 * <code>getFirstName</code> looks up the first name of a PlayerCharacter by
	 * first checking the GOM cache and then querying the database.
	 * PlayerCharacter objects that are not already cached won't be instantiated
	 * and cached.
	 *
	 */
	public String GET_FIRST_NAME(final int objectUUID) {
		prepareCallable("SELECT `char_firstname` from `obj_character` WHERE `UID` = ? LIMIT 1");
		setLong(1, (long) objectUUID);
		String firstName = "";
		try {
			ResultSet rs = executeQuery();
			if (rs.next()) {
				firstName = rs.getString("char_firstname");
			}
		} catch (SQLException e) {
			Logger.error( e);
		} finally {
			closeCallable();
		}
		return firstName;
	}

	public ConcurrentHashMap<Integer, String> GET_IGNORE_LIST(final int objectUUID, final boolean skipActiveCheck) {
		ConcurrentHashMap<Integer, String> out = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		prepareCallable("SELECT * FROM `dyn_character_ignore` WHERE `accountUID` = ?;");
		setLong(1, (long) objectUUID);
		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {
				int ignoreCharacterID = rs.getInt("ignoringUID");
				if (ignoreCharacterID == 0) {
					continue;
				}
				String name = rs.getString("characterName");
				out.put(ignoreCharacterID, name);
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error("SQL Error number: " + e.getErrorCode());
			return out; // null to explicitly indicate a problem and prevent data loss
		} finally {
			closeCallable();
		}
		return out;
	}

	public PlayerCharacter GET_PLAYER_CHARACTER(final int objectUUID) {

		if (objectUUID == 0)
			return null;

		PlayerCharacter pc = (PlayerCharacter) DbManager.getFromCache(Enum.GameObjectType.PlayerCharacter, objectUUID);
		if (pc != null)
			return pc;
		prepareCallable("SELECT `obj_character`.*, `object`.`parent` FROM `object` INNER JOIN `obj_character` ON `obj_character`.`UID` = `object`.`UID` WHERE `object`.`UID` = ?");
		setLong(1, (long) objectUUID);
		return (PlayerCharacter) getObjectSingle(objectUUID);
	}

	public boolean INSERT_CHARACTER_IGNORE(final PlayerCharacter pc, final ArrayList<Integer> toAdd) {
		boolean allWorked = true;
		prepareCallable("INSERT INTO `dyn_character_ignore` (`characterUID`, `ignoringUID`) VALUES (?, ?)");
		setLong(1, (long) pc.getObjectUUID());
		for (int id : toAdd) {
			setLong(2, (long) id);
			if (executeUpdate(false) == 0) {
				allWorked = false;
			}
		}
		closeCallable();
		return allWorked;
	}

	public boolean IS_CHARACTER_NAME_UNIQUE(final String firstName) {
		boolean unique = true;
		prepareCallable("SELECT `char_firstname` FROM `obj_character` WHERE `char_isActive`=1 && `char_firstname`=?");
		setString(1, firstName);
		try {
			ResultSet rs = executeQuery();
			if (rs.next()) {
				unique = false;
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error("SQL Error number: " + e.getMessage());
			unique = false;
		} finally {
			closeCallable();
		}
		return unique;
	}

	public boolean UPDATE_NAME(String oldFirstName, String newFirstName, String newLastName) {
		prepareCallable("UPDATE `obj_character` SET `char_firstname`=?, `char_lastname`=? WHERE `char_firstname`=? AND `char_isActive`='1'");
		setString(1, newFirstName);
		setString(2, newLastName);
		setString(3, oldFirstName);
		return (executeUpdate() != 0);
	}

	public boolean SET_DELETED(final PlayerCharacter pc) {
		prepareCallable("UPDATE `obj_character` SET `char_isActive`=? WHERE `UID` = ?");
		setBoolean(1, !pc.isDeleted());
		setLong(2, (long) pc.getObjectUUID());
		return (executeUpdate() != 0);
	}
	public boolean SET_ACTIVE(final PlayerCharacter pc, boolean status) {
		prepareCallable("UPDATE `obj_character` SET `char_isActive`=? WHERE `UID` = ?");
		setBoolean(1, status);
		setLong(2, (long) pc.getObjectUUID());
		return (executeUpdate() != 0);
	}
	public boolean SET_BIND_BUILDING(final PlayerCharacter pc, int bindBuildingID) {
		prepareCallable("UPDATE `obj_character` SET `char_bindBuilding`=? WHERE `UID` = ?");
		setInt(1, bindBuildingID);
		setLong(2, (long) pc.getObjectUUID());
		return (executeUpdate() != 0);
	}

	public boolean SET_ANNIVERSERY(final PlayerCharacter pc, boolean flag) {
		prepareCallable("UPDATE `obj_character` SET `anniversery`=? WHERE `UID` = ?");
		setBoolean(1, flag);
		setLong(2, (long) pc.getObjectUUID());
		return (executeUpdate() != 0);
	}


	public boolean UPDATE_CHARACTER_EXPERIENCE(final PlayerCharacter pc) {
		prepareCallable("UPDATE `obj_character` SET `char_experience`=? WHERE `UID` = ?");
		setInt(1, pc.getExp());
		setLong(2, (long) pc.getObjectUUID());
		return (executeUpdate() != 0);
	}
	
	public boolean UPDATE_GUILD(final PlayerCharacter pc, int guildUUID) {
		prepareCallable("UPDATE `obj_character` SET `guildUID`=? WHERE `UID` = ?");
		setInt(1, guildUUID);
		setLong(2, (long) pc.getObjectUUID());
		return (executeUpdate() != 0);
	}

	public boolean UPDATE_CHARACTER_STAT(final PlayerCharacter pc, String stat, short amount) {
		prepareCallable("UPDATE `obj_character` SET `" + stat + "`=? WHERE `UID`=?");
		setInt(1, pc.getExp());
		setLong(2, (long) pc.getObjectUUID());
		return (executeUpdate() != 0);
	}

	public boolean UPDATE_CHARACTER_STATS(final PlayerCharacter pc) {
		prepareCallable("UPDATE `obj_character` SET `char_strMod`=?, `char_dexMod`=?, `char_conMod`=?, `char_intMod`=?, `char_spiMod`=? WHERE `UID`=?");
		setInt(1, pc.getStrMod());
		setInt(2, pc.getDexMod());
		setInt(3, pc.getConMod());
		setInt(4, pc.getIntMod());
		setInt(5, pc.getSpiMod());
		setLong(6, (long) pc.getObjectUUID());
		return (executeUpdate() != 0);
	}

	public String SET_PROPERTY(final PlayerCharacter c, String name, Object new_value) {
		prepareCallable("CALL character_SETPROP(?,?,?)");
		setLong(1, (long) c.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		return getResult();
	}

	public String SET_PROPERTY(final PlayerCharacter c, String name, Object new_value, Object old_value) {
		prepareCallable("CALL character_GETSETPROP(?,?,?,?)");
		setLong(1, (long) c.getObjectUUID());
		setString(2, name);
		setString(3, String.valueOf(new_value));
		setString(4, String.valueOf(old_value));
		return getResult();
	}
	
	public boolean SET_PROMOTION_CLASS(PlayerCharacter player, int promotionClassID) {
		prepareCallable("UPDATE `obj_character` SET `char_promotionClassID`=?  WHERE `UID`=?;");
		setInt(1,promotionClassID);
		setInt(2, player.getObjectUUID());
		return (executeUpdate() != 0);
	}
	
	public boolean SET_INNERCOUNCIL(PlayerCharacter player, boolean isInnerCouncil) {
		prepareCallable("UPDATE `obj_character` SET `guild_isInnerCouncil`=?  WHERE `UID`=?;");
		setBoolean(1,isInnerCouncil);
		setInt(2, player.getObjectUUID());
		return (executeUpdate() != 0);
	}
	
	public boolean SET_FULL_MEMBER(PlayerCharacter player, boolean isFullMember) {
		prepareCallable("UPDATE `obj_character` SET `guild_isFullMember`=?  WHERE `UID`=?;");
		setBoolean(1,isFullMember);
		setInt(2, player.getObjectUUID());
		return (executeUpdate() != 0);
	}
	
	public boolean SET_TAX_COLLECTOR(PlayerCharacter player, boolean isTaxCollector) {
		prepareCallable("UPDATE `obj_character` SET `guild_isTaxCollector`=?  WHERE `UID`=?;");
		setBoolean(1,isTaxCollector);
		setInt(2, player.getObjectUUID());
		return (executeUpdate() != 0);
	}
	
	public boolean SET_RECRUITER(PlayerCharacter player, boolean isRecruiter) {
		prepareCallable("UPDATE `obj_character` SET `guild_isRecruiter`=?  WHERE `UID`=?;");
		setBoolean(1,isRecruiter);
		setInt(2, player.getObjectUUID());
		return (executeUpdate() != 0);
	}
	
	public boolean SET_GUILD_TITLE(PlayerCharacter player, int title) {
		prepareCallable("UPDATE `obj_character` SET `guild_title`=?  WHERE `UID`=?;");
		setInt(1,title);
		setInt(2, player.getObjectUUID());
		return (executeUpdate() != 0);
	}
	
	
	
	public boolean ADD_FRIEND(int source, long friend){
		prepareCallable("INSERT INTO `dyn_character_friends` (`playerUID`, `friendUID`) VALUES (?, ?)");
		setLong(1, (long) source);
		setLong(2, (long)friend);
		return (executeUpdate() != 0);
	}
	
	public boolean REMOVE_FRIEND(int source, int friend){
		prepareCallable("DELETE FROM `dyn_character_friends` WHERE (`playerUID`=?) AND (`friendUID`=?)");
		setLong(1, (long) source);
		setLong(2, (long)friend);
		return (executeUpdate() != 0);
	}
	
	public void LOAD_PLAYER_FRIENDS() {

		PlayerFriends playerFriend;


		prepareCallable("SELECT * FROM dyn_character_friends");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {
				playerFriend = new PlayerFriends(rs);
			}


		} catch (SQLException e) {
			Logger.error("LoadMeshBounds: " + e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
	}
	
	public boolean ADD_HERALDY(int source, AbstractWorldObject character){
		prepareCallable("INSERT INTO `dyn_character_heraldy` (`playerUID`, `characterUID`,`characterType`) VALUES (?, ?,?)");
		setLong(1, (long) source);
		setLong(2, (long)character.getObjectUUID());
		setInt(3, character.getObjectType().ordinal());
		return (executeUpdate() != 0);
	}
	
	public boolean REMOVE_HERALDY(int source, int characterUID){
		prepareCallable("DELETE FROM `dyn_character_heraldy` WHERE (`playerUID`=?) AND (`characterUID`=?)");
		setLong(1, (long) source);
		setLong(2, (long)characterUID);
		return (executeUpdate() != 0);
	}
	
	public void LOAD_HERALDY() {

		Heraldry heraldy;


		prepareCallable("SELECT * FROM dyn_character_heraldy");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {
				heraldy = new Heraldry(rs);
			}


		} catch (SQLException e) {
			Logger.error("LoadHeraldy: " + e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
	}
	
}
