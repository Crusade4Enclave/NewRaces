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
import engine.objects.CharacterPower;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class dbCharacterPowerHandler extends dbHandlerBase {

	public dbCharacterPowerHandler() {
		this.localClass = CharacterPower.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public CharacterPower ADD_CHARACTER_POWER(CharacterPower toAdd) {
		if (CharacterPower.getOwner(toAdd) == null || toAdd.getPower() == null) {
			Logger.error("dbCharacterSkillHandler.ADD_Power", toAdd.getObjectUUID() + " missing owner or powersBase");
			return null;
		}

		prepareCallable("INSERT INTO `dyn_character_power` (`CharacterID`, `powersBaseToken`, `trains`) VALUES (?, ?, ?);");
		setLong(1, (long)CharacterPower.getOwner(toAdd).getObjectUUID());
		setInt(2, toAdd.getPower().getToken());
		setInt(3, toAdd.getTrains());
		int powerID = insertGetUUID();
		return GET_CHARACTER_POWER(powerID);

	}

	public int DELETE_CHARACTER_POWER(final int objectUUID) {
		prepareCallable("DELETE FROM `dyn_character_power` WHERE `UID` = ?");
		setLong(1, (long)objectUUID);
		return executeUpdate();
	}

	public CharacterPower GET_CHARACTER_POWER(int objectUUID) {

		CharacterPower cp = (CharacterPower) DbManager.getFromCache(Enum.GameObjectType.CharacterPower, objectUUID);
		if (cp != null)
			return cp;
		prepareCallable("SELECT * FROM `dyn_character_power` WHERE `UID` = ?");
		setLong(1, (long)objectUUID);
		return (CharacterPower) getObjectSingle(objectUUID);
	}

	public ConcurrentHashMap<Integer, CharacterPower> GET_POWERS_FOR_CHARACTER(PlayerCharacter pc) {
		ConcurrentHashMap<Integer, CharacterPower> powers = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		int objectUUID = pc.getObjectUUID();
		prepareCallable("SELECT * FROM `dyn_character_power` WHERE CharacterID = ?");
		setLong(1, (long)objectUUID);
		ResultSet rs = executeQuery();
		try {
			while (rs.next()) {
				CharacterPower cp = new CharacterPower(rs, pc);
				if (cp.getPower() != null)
					powers.put(cp.getPower().getToken(), cp);
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error("CharacterPower.getCharacterPowerForCharacter", "Exception:" + e.getMessage());
		} finally {
			closeCallable();
		}
		return powers;
	}

	public void UPDATE_TRAINS(final CharacterPower pow) {
		//skip update if nothing changed
		if (!pow.isTrained())
			return;

		prepareCallable("UPDATE `dyn_character_power` SET `trains`=? WHERE `UID`=?");
		setShort(1, (short)pow.getTrains());
		setInt(2, pow.getObjectUUID());
		executeUpdate();
		pow.setTrained(false);
	}

	public void updateDatabase(final CharacterPower pow) {
		if (pow.getPower() == null) {
			Logger.error( "Failed to find powersBase for Power " + pow.getObjectUUID());
			return;
		}
		if (CharacterPower.getOwner(pow) == null) {
			Logger.error( "Failed to find owner for Power " + pow.getObjectUUID());
			return;
		}


		prepareCallable("UPDATE `dyn_character_power` SET `PowersBaseToken`=?, `CharacterID`=?, `trains`=? WHERE `UID`=?");
		setInt(1, pow.getPower().getToken());
		setInt(2, CharacterPower.getOwner(pow).getObjectUUID());
		setShort(3, (short)pow.getTrains());
		setInt(4, pow.getObjectUUID());
		executeUpdate();
		pow.setTrained(false);
	}
}
