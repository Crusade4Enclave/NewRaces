// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.RuneBase;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class dbRuneBaseHandler extends dbHandlerBase {

	public dbRuneBaseHandler() {
		this.localClass = RuneBase.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public void GET_RUNE_REQS(final RuneBase rb) {
		prepareCallable("SELECT * FROM `static_rune_runereq` WHERE `runeID` = ?");
		setInt(1, rb.getObjectUUID());
		try {

			ResultSet rs = executeQuery();

			while (rs.next()) {
				int type = rs.getInt("type");

				switch (type) {
				case 1:
					rb.getRace().put(rs.getInt("requiredRuneID"), rs.getBoolean("isAllowed"));
					break;
				case 2:
					rb.getBaseClass().put(rs.getInt("requiredRuneID"), rs.getBoolean("isAllowed"));
					break;
				case 3:
					rb.getPromotionClass().put(rs.getInt("requiredRuneID"), rs.getBoolean("isAllowed"));
					break;
				case 4:
					rb.getDiscipline().put(rs.getInt("requiredRuneID"), rs.getBoolean("isAllowed"));
					break;
				case 5:
					rb.getOverwrite().add(rs.getInt("requiredRuneID"));
					break;
				case 6:
					rb.setLevelRequired(rs.getInt("requiredRuneID"));
					break;
				}
			}
			rs.close();
		} catch (SQLException e) {
			Logger.error("SQL Error number: " + e.getErrorCode());
		} finally {
			closeCallable();
		}
	}

	public RuneBase GET_RUNEBASE(final int id) {
		prepareCallable("SELECT * FROM `static_rune_runebase` WHERE `ID` = ?");
		setInt(1, id);
		return (RuneBase) getObjectSingle(id);
	}

	public ArrayList<RuneBase> LOAD_ALL_RUNEBASES() {
		prepareCallable("SELECT * FROM `static_rune_runebase`;");
		return  getObjectList();
	}

	public HashMap<Integer, ArrayList<Integer>> LOAD_ALLOWED_STARTING_RUNES_FOR_BASECLASS() {

		HashMap<Integer, ArrayList<Integer>> runeSets;

		runeSets = new HashMap<>();
		int recordsRead = 0;

		prepareCallable("SELECT * FROM static_rune_baseclassrune");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;

				int baseClassID = rs.getInt("BaseClassesID");
				int runeBaseID = rs.getInt("RuneBaseID");

				if (runeSets.get(baseClassID) == null){
					ArrayList<Integer> runeList = new ArrayList<>();
					runeList.add(runeBaseID);
					runeSets.put(baseClassID, runeList);
				}
				else{
					ArrayList<Integer>runeList = runeSets.get(baseClassID);
					runeList.add(runeBaseID);
					runeSets.put(baseClassID, runeList);
				}
			}

			Logger.info("read: " + recordsRead + " cached: " + runeSets.size());

		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		return runeSets;
	}

	public HashMap<Integer, ArrayList<Integer>> LOAD_ALLOWED_STARTING_RUNES_FOR_RACE() {

		HashMap<Integer, ArrayList<Integer>> runeSets;

		runeSets = new HashMap<>();
		int recordsRead = 0;

		prepareCallable("SELECT * FROM static_rune_racerune");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;

				int raceID = rs.getInt("RaceID");
				int runeBaseID = rs.getInt("RuneBaseID");

				if (runeSets.get(raceID) == null){
					ArrayList<Integer> runeList = new ArrayList<>();
					runeList.add(runeBaseID);
					runeSets.put(raceID, runeList);
				}
				else{
					ArrayList<Integer>runeList = runeSets.get(raceID);
					runeList.add(runeBaseID);
					runeSets.put(raceID, runeList);
				}
			}

			Logger.info( "read: " + recordsRead + " cached: " + runeSets.size());

		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		return runeSets;
	}

	public ArrayList<RuneBase> GET_RUNEBASE_FOR_BASECLASS(final int id) {
		prepareCallable("SELECT rb.* FROM static_rune_baseclassrune bcr, static_rune_runebase rb WHERE bcr.RuneBaseID = rb.ID "
				+ "&& ( bcr.BaseClassesID = 111111 || bcr.BaseClassesID = ? )");
		setInt(1, id);
		return getObjectList();
	}

	public HashSet<RuneBase> GET_RUNEBASE_FOR_RACE(final int id) {
		prepareCallable("SELECT rb.* FROM static_rune_racerune rr, static_rune_runebase rb"
				+ " WHERE rr.RuneBaseID = rb.ID && ( rr.RaceID = 111111 || rr.RaceID = ?)");
		setInt(1, id);
		return new HashSet<>(getObjectList());
	}
}
