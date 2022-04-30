package engine.db.handlers;

import engine.InterestManagement.HeightMap;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class dbHeightMapHandler extends dbHandlerBase {

	public dbHeightMapHandler() {


	}

	public void LOAD_ALL_HEIGHTMAPS() {

		HeightMap thisHeightmap;

		int recordsRead = 0;
		int worthlessDupes = 0;

		HeightMap.heightMapsCreated = 0;

		prepareCallable("SELECT * FROM static_zone_heightmap INNER JOIN static_zone_size ON static_zone_size.loadNum = static_zone_heightmap.zoneLoadID");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;
				thisHeightmap = new HeightMap(rs);

				if (thisHeightmap.getHeightmapImage() == null) {
					Logger.info( "Imagemap for " + thisHeightmap.getHeightMapID() + " was null");
					continue;
				}
			}
		} catch (SQLException e) {
			Logger.error("LoadAllHeightMaps: " + e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
	}

}
