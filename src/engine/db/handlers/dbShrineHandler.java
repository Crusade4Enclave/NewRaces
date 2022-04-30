// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum.ProtectionState;
import engine.gameManager.DbManager;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.Shrine;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class dbShrineHandler extends dbHandlerBase {

    public dbShrineHandler() {
        this.localClass = Shrine.class;
        this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
    }

    public ArrayList<AbstractGameObject> CREATE_SHRINE( int parentZoneID, int OwnerUUID, String name, int meshUUID,
            Vector3fImmutable location, float meshScale, int currentHP,
            ProtectionState protectionState, int currentGold, int rank,
            DateTime upgradeDate, int blueprintUUID, float w, float rotY, String shrineType) {

        prepareCallable("CALL `shrine_CREATE`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? ,? ,?, ?,?);");

      
        setInt(1, parentZoneID);
        setInt(2, OwnerUUID);
        setString(3, name);
        setInt(4, meshUUID);
        setFloat(5, location.x);
        setFloat(6, location.y);
        setFloat(7, location.z);
        setFloat(8, meshScale);
        setInt(9, currentHP);
        setString(10, protectionState.name());
        setInt(11, currentGold);
        setInt(12, rank);

        if (upgradeDate != null) {
            setTimeStamp(13, upgradeDate.getMillis());
        } else {
            setNULL(13, java.sql.Types.DATE);
        }

        setInt(14, blueprintUUID);
        setFloat(15, w);
        setFloat(16, rotY);
        setString(17, shrineType);

        ArrayList<AbstractGameObject> list = new ArrayList<>();
        //System.out.println(this.cs.get().toString());
        try {
            boolean work = execute();
            if (work) {
                ResultSet rs = this.cs.get().getResultSet();
                while (rs.next()) {
                    addObject(list, rs);
                }
                rs.close();
            } else {
                Logger.info("Shrine Creation Failed: " + this.cs.get().toString());
                return list; //city creation failure
            }
            while (this.cs.get().getMoreResults()) {
                ResultSet rs = this.cs.get().getResultSet();
                while (rs.next()) {
                    addObject(list, rs);
                }
                rs.close();
            }
        } catch (SQLException e) {
            Logger.info("Shrine Creation Failed, SQLException: " + this.cs.get().toString() + e.toString());
            return list; //city creation failure
        } catch (UnknownHostException e) {
            Logger.info("Shrine Creation Failed, UnknownHostException: " + this.cs.get().toString());
            return list; //city creation failure
        } finally {
            closeCallable();
        }
        return list;

    }

    public boolean updateFavors(Shrine shrine, int amount, int oldAmount) {

        prepareCallable("UPDATE `obj_shrine` SET `shrine_favors`=? WHERE `UID` = ? AND `shrine_favors` = ?");
        setInt(1, amount);
        setLong(2, (long) shrine.getObjectUUID());
        setInt(3, oldAmount);
        return (executeUpdate() != 0);
    }

    public static void addObject(ArrayList<AbstractGameObject> list, ResultSet rs) throws SQLException, UnknownHostException {
        String type = rs.getString("type");
        switch (type) {
            case "building":
                Building building = new Building(rs);
                DbManager.addToCache(building);
                list.add(building);
                break;
            case "shrine":
                Shrine shrine = new Shrine(rs);
                DbManager.addToCache(shrine);
                list.add(shrine);
                break;
        }
    }

    public void LOAD_ALL_SHRINES() {

        Shrine thisShrine;

        prepareCallable("SELECT `obj_shrine`.*, `object`.`parent`, `object`.`type` FROM `object` LEFT JOIN `obj_shrine` ON `object`.`UID` = `obj_shrine`.`UID` WHERE `object`.`type` = 'shrine';");

        try {
            ResultSet rs = executeQuery();

            //shrines cached in rs for easy cache on creation.
            while (rs.next()) {
                thisShrine = new Shrine(rs);
                thisShrine.getShrineType().addShrineToServerList(thisShrine);
            }

        } catch (SQLException e) {
            Logger.error( e.getErrorCode() + ' ' + e.getMessage(), e);
        } finally {
            closeCallable();
        }

    }

}
