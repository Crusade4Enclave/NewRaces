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
import engine.objects.AbstractCharacter;
import engine.objects.CharacterSkill;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class dbCharacterSkillHandler extends dbHandlerBase {

	public dbCharacterSkillHandler() {
		this.localClass = CharacterSkill.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public CharacterSkill ADD_SKILL(CharacterSkill toAdd) {
		if (CharacterSkill.GetOwner(toAdd) == null || toAdd.getSkillsBase() == null) {
			Logger.error("dbCharacterSkillHandler.ADD_SKILL", toAdd.getObjectUUID() + " missing owner or skillsBase");
			return null;
		}

		prepareCallable("INSERT INTO `dyn_character_skill` (`CharacterID`, `skillsBaseID`, `trains`) VALUES (?, ?, ?);");
		setLong(1, (long)CharacterSkill.GetOwner(toAdd).getObjectUUID());
		setInt(2, toAdd.getSkillsBase().getObjectUUID());
		setInt(3, toAdd.getNumTrains());
		int skillID = insertGetUUID();
		return GET_SKILL(skillID);
	}

	public boolean DELETE_SKILL(final int objectUUID) {
		prepareCallable("DELETE FROM `dyn_character_skill` WHERE `UID` = ?");
		setLong(1, (long)objectUUID);
		return (executeUpdate() != 0);
	}

	public CharacterSkill GET_SKILL(final int objectUUID) {
		CharacterSkill skill = (CharacterSkill) DbManager.getFromCache(Enum.GameObjectType.CharacterSkill, objectUUID);
		if (skill != null)
			return skill;
		prepareCallable("SELECT * FROM `dyn_character_skill` WHERE `UID` = ?");
		setInt(1, objectUUID);
		return (CharacterSkill) getObjectSingle(objectUUID);
	}

	public ConcurrentHashMap<String, CharacterSkill> GET_SKILLS_FOR_CHARACTER(final AbstractCharacter ac) {
		ConcurrentHashMap<String, CharacterSkill> skills = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		if (ac == null || (!(ac.getObjectType().equals(Enum.GameObjectType.PlayerCharacter))))
			return skills;
		PlayerCharacter pc = (PlayerCharacter) ac;
		int objectUUID = pc.getObjectUUID();

		prepareCallable("SELECT * FROM `dyn_character_skill` WHERE `CharacterID` = ?");
		setInt(1, objectUUID);
		try {
			ResultSet rs = executeQuery();
			while (rs.next()) {
				CharacterSkill cs = new CharacterSkill(rs, pc);
				if (cs.getSkillsBase() != null)
					skills.put(cs.getSkillsBase().getName(), cs);
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error("CharacterSkill.getCharacterSkillForCharacter", e);
		} finally {
			closeCallable();
		}
		return skills;
	}


	public void UPDATE_TRAINS(final CharacterSkill cs) {
		if (!cs.isTrained())
			return;

		prepareCallable("UPDATE `dyn_character_skill` SET `trains`=? WHERE `UID` = ?");
		setShort(1, (short)cs.getNumTrains());
		setLong(2, (long)cs.getObjectUUID());
		if (executeUpdate() != 0)
			cs.syncTrains();
	}

	public void updateDatabase(final CharacterSkill cs) {
		if (cs.getSkillsBase() == null) {
			Logger.error("Failed to find skillsBase for Skill " + cs.getObjectUUID());
			return;
		}
		if (CharacterSkill.GetOwner(cs) == null) {
			Logger.error("Failed to find owner for Skill " + cs.getObjectUUID());
			return;
		}

		prepareCallable("UPDATE `dyn_character_skill` SET `skillsBaseID`=?, `CharacterID`=?, `trains`=? WHERE `UID`=?");
		setInt(1, cs.getSkillsBase().getObjectUUID());
		setInt(2, CharacterSkill.GetOwner(cs).getObjectUUID());
		setShort(3, (short)cs.getNumTrains());
		setLong(4, (long)cs.getObjectUUID());
		if (executeUpdate() != 0)
			cs.syncTrains();
	}

}
