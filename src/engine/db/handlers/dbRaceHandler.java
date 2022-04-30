// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.Race;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class dbRaceHandler extends dbHandlerBase {

    public dbRaceHandler() {
    }

    public HashSet<Integer> BEARD_COLORS_FOR_RACE(final int id) {
        prepareCallable("SELECT `color` FROM `static_rune_racebeardcolor` WHERE `RaceID` = ?");
        setInt(1, id);
        return getIntegerList(1);
    }

    public HashSet<Integer> BEARD_STYLES_FOR_RACE(final int id) {
        prepareCallable("SELECT `beardStyle` FROM `static_rune_racebeardstyle` WHERE `RaceID` = ?");
        setInt(1, id);
        return getIntegerList(1);
    }

    public HashSet<Integer> HAIR_COLORS_FOR_RACE(final int id) {
        prepareCallable("SELECT `color` FROM `static_rune_racehaircolor` WHERE `RaceID` = ?");
        setInt(1, id);
        return getIntegerList(1);
    }

    public HashSet<Integer> HAIR_STYLES_FOR_RACE(final int id) {
        prepareCallable("SELECT `hairStyle` FROM `static_rune_racehairstyle` WHERE `RaceID` = ?");
        setInt(1, id);
        return getIntegerList(1);
    }

    public HashSet<Integer> SKIN_COLOR_FOR_RACE(final int id) {
        prepareCallable("SELECT `color` FROM `static_rune_raceskincolor` WHERE `RaceID` = ?");
        setInt(1, id);
        return getIntegerList(1);
    }

    public ConcurrentHashMap<Integer, Race> LOAD_ALL_RACES() {

        ConcurrentHashMap<Integer, Race> races;
        Race thisRace;

        races = new ConcurrentHashMap<>();
        int recordsRead = 0;

        prepareCallable("SELECT * FROM static_rune_race");

        try {
            ResultSet rs = executeQuery();

            while (rs.next()) {

                recordsRead++;
                thisRace = new Race(rs);

                races.put(thisRace.getRaceRuneID(), thisRace);
            }

            Logger.info("read: " + recordsRead + " cached: " + races.size());

        } catch (SQLException e) {
            Logger.error( e.toString());
        } finally {
            closeCallable();
        }
        return races;
    }
}
