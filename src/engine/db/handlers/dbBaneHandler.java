// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.Bane;
import engine.objects.Building;
import engine.objects.City;
import engine.objects.PlayerCharacter;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class dbBaneHandler extends dbHandlerBase {

    public dbBaneHandler() {

    }

    public boolean CREATE_BANE(City city, PlayerCharacter owner, Building stone) {

        prepareCallable("INSERT INTO `dyn_banes` (`cityUUID`, `ownerUUID`, `stoneUUID`, `placementDate`) VALUES(?,?,?,?)");
        setLong(1, (long) city.getObjectUUID());
        setLong(2, (long) owner.getObjectUUID());
        setLong(3, (long) stone.getObjectUUID());
        setTimeStamp(4, System.currentTimeMillis());

        return (executeUpdate() > 0);

    }

    public Bane LOAD_BANE(int cityUUID) {

        Bane newBane = null;

        try {

            prepareCallable("SELECT * from dyn_banes WHERE `dyn_banes`.`cityUUID` = ?");

            setLong(1, (long) cityUUID);
            ResultSet rs = executeQuery();

            if (rs.next()) {
                newBane = new Bane(rs);
                Bane.addBane(newBane);
            }

        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(dbBaneHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeCallable();
        }
        return newBane;

    }

    public ConcurrentHashMap<Integer, Bane> LOAD_ALL_BANES() {

        ConcurrentHashMap<Integer, Bane> baneList;
        Bane thisBane;

        baneList = new ConcurrentHashMap<>();

        int recordsRead = 0;

        prepareCallable("SELECT * FROM dyn_banes");

        try {
            ResultSet rs = executeQuery();

            while (rs.next()) {

                recordsRead++;
                thisBane = new Bane(rs);
                baneList.put(thisBane.getCityUUID(), thisBane);

            }

            Logger.info("read: " + recordsRead + " cached: " + baneList.size());

        } catch (SQLException e) {
            Logger.error( e.toString());
        } finally {
            closeCallable();
        }
        return baneList;
    }

    public boolean SET_BANE_TIME(DateTime toSet, int cityUUID) {
        prepareCallable("UPDATE `dyn_banes` SET `liveDate`=? WHERE `cityUUID`=?");
        setTimeStamp(1, toSet.getMillis());
        setLong(2, cityUUID);
        return (executeUpdate() > 0);
    }

    public boolean REMOVE_BANE(Bane bane) {

        if (bane == null)
            return false;

        prepareCallable("DELETE FROM `dyn_banes` WHERE `cityUUID` = ?");
        setLong(1, (long) bane.getCity().getObjectUUID());
        return (executeUpdate() > 0);
    }
}
