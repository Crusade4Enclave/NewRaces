// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.DbManager;
import engine.math.Bounds;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MeshBounds {

	public int meshID;
	public final float minX;
	public final float minY;
	public final  float minZ;
	public final float maxX;
	public final float maxY;
	public final float maxZ;
	public final float radius;

	public MeshBounds(ResultSet rs) throws SQLException {

		meshID = rs.getInt("meshID");
		minX = rs.getFloat("minX"); 
		minY = rs.getFloat("minY");
		minZ = rs.getFloat("minZ");
		maxX = rs.getFloat("maxX");
		maxY = rs.getFloat("maxY");
		maxZ = rs.getFloat("maxZ");
		float radiusX = (int) maxX;
		float radiusZ = (int) maxZ;
		
		radius = Math.max(radiusX,radiusZ);
	}

	public static void InitializeBuildingBounds(){
		Bounds.meshBoundsCache = DbManager.BuildingQueries.LOAD_MESH_BOUNDS();
	}
	

}
