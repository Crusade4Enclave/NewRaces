// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.gameManager.DbManager;
import engine.objects.AbstractGameObject;
import engine.objects.MaxSkills;
import engine.objects.SkillsBase;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class dbSkillBaseHandler extends dbHandlerBase {

	public dbSkillBaseHandler() {
		this.localClass = SkillsBase.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public SkillsBase GET_BASE(final int objectUUID) {

		SkillsBase skillsBase = (SkillsBase) DbManager.getFromCache(GameObjectType.SkillsBase, objectUUID);
		if (skillsBase != null)
			return skillsBase;
		prepareCallable("SELECT * FROM static_skill_skillsbase WHERE ID = ?");
		setInt(1, objectUUID);
		SkillsBase sb;
		sb = (SkillsBase) getObjectSingle(objectUUID);
		SkillsBase.putInCache(sb);
		return sb;
	}

	public SkillsBase GET_BASE_BY_NAME(String name) {
		SkillsBase sb = SkillsBase.getFromCache(name);
		if (sb != null) {
			return sb;
		}
		prepareCallable("SELECT * FROM static_skill_skillsbase WHERE name = ?");
		setString(1, name);
		ArrayList<AbstractGameObject> result = getObjectList();
		if (result.size() > 0) {
			sb = (SkillsBase) result.get(0);
			SkillsBase.putInCache(sb);
			return sb;
		} else {
			return null;
		}
	}

	public SkillsBase GET_BASE_BY_TOKEN(final int token) {
		SkillsBase sb = SkillsBase.getFromCache(token);
		if (sb != null) {
			return sb;
		}

		prepareCallable("SELECT * FROM static_skill_skillsbase WHERE token = ?");
		setInt(1, token);
		ArrayList<AbstractGameObject> result = getObjectList();
		if (result.size() > 0) {
			sb = (SkillsBase) result.get(0);
			SkillsBase.putInCache(sb);
			return sb;
		} else {
			return null;
		}
	}

	public void LOAD_ALL_MAX_SKILLS_FOR_CONTRACT() {

		prepareCallable("SELECT * FROM `static_rune_maxskills`");


		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {

				MaxSkills maxSKills = new MaxSkills(rs);
				if (MaxSkills.MaxSkillsSet.get(maxSKills.getRuneID()) == null){
					ArrayList<MaxSkills> newMaxSkillsList = new ArrayList<>();
					newMaxSkillsList.add(maxSKills);
					MaxSkills.MaxSkillsSet.put(maxSKills.getRuneID(), newMaxSkillsList);
				}else
					MaxSkills.MaxSkillsSet.get(maxSKills.getRuneID()).add(maxSKills);

			}



		} catch (SQLException e) {
			Logger.error( e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}

	}
	
	public void LOAD_ALL_RUNE_SKILLS() {

		prepareCallable("SELECT * FROM `static_skill_skillsgranted`");


		try {
			ResultSet rs = executeQuery();

			//shrines cached in rs for easy cache on creation.
			while (rs.next()) {

				int runeID = rs.getInt("runeID");
				int token = rs.getInt("token");
				int amount = rs.getInt("amount");
				
				if (SkillsBase.runeSkillsCache.get(runeID) == null)
					SkillsBase.runeSkillsCache.put(runeID, new HashMap<>());
				
				SkillsBase.runeSkillsCache.get(runeID).put(token, amount);
			}



		} catch (SQLException e) {
			Logger.error( e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}

	}


}
