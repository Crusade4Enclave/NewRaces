package engine.db.handlers;

import engine.objects.Blueprint;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class dbBlueprintHandler extends dbHandlerBase {

    public dbBlueprintHandler() {

    }

    public HashMap<Integer, Integer> LOAD_ALL_DOOR_NUMBERS() {

        HashMap<Integer, Integer> doorInfo;
        doorInfo = new HashMap<>();

        int doorUUID;
        int doorNum;
        int recordsRead = 0;

        prepareCallable("SELECT * FROM static_building_doors ORDER BY doorMeshUUID ASC");

        try {
            ResultSet rs = executeQuery();

            while (rs.next()) {

                recordsRead++;
                doorUUID = rs.getInt("doorMeshUUID");
                doorNum = rs.getInt("doorNumber");
                doorInfo.put(doorUUID, doorNum);
            }

            Logger.info( "read: " + recordsRead + " cached: " + doorInfo.size());

        } catch (SQLException e) {
            Logger.error("LoadAllDoorNumbers: " + e.getErrorCode() + ' ' + e.getMessage(), e);
        } finally {
            closeCallable();
        }
        return doorInfo;
    }

    public HashMap<Integer, Blueprint> LOAD_ALL_BLUEPRINTS() {

        HashMap<Integer, Blueprint> blueprints;
        Blueprint thisBlueprint;

        blueprints = new HashMap<>();
        int recordsRead = 0;

        prepareCallable("SELECT * FROM static_building_blueprint");

        try {
            ResultSet rs = executeQuery();

            while (rs.next()) {

                recordsRead++;
                thisBlueprint = new Blueprint(rs);

                blueprints.put(thisBlueprint.getBlueprintUUID(), thisBlueprint);

                // load mesh cache
                Blueprint._meshLookup.putIfAbsent(thisBlueprint.getMeshForRank(-1), thisBlueprint);
                Blueprint._meshLookup.putIfAbsent(thisBlueprint.getMeshForRank(0), thisBlueprint);
                Blueprint._meshLookup.putIfAbsent(thisBlueprint.getMeshForRank(1), thisBlueprint);
                Blueprint._meshLookup.putIfAbsent(thisBlueprint.getMeshForRank(3), thisBlueprint);
                Blueprint._meshLookup.putIfAbsent(thisBlueprint.getMeshForRank(7), thisBlueprint);

            }

            Logger.info( "read: " + recordsRead + " cached: " + blueprints.size());

        } catch (SQLException e) {
            Logger.error("LoadAllBlueprints: " + e.getErrorCode() + ' ' + e.getMessage(), e);
        } finally {
            closeCallable();
        }
        return blueprints;
    }
}
