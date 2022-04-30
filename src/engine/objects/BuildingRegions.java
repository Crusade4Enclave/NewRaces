// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.DbManager;
import engine.math.Vector3f;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class BuildingRegions  {


	private int buildingID;
	private int level;
	private int numVertex	;
	private float vertex1X	;
	private float vertex1Y	;
	private float vertex1Z	;
	private float vertex2X	;
	private float vertex2Y	;
	private float vertex2Z	;
	private float vertex3X	;
	private float vertex3Y	;
	private float vertex3Z	;
	private float vertex4X	;
	private float vertex4Y	;
	private float vertex4Z	;
	private byte ground1;
	private byte ground2;
	private byte ground3;
	private byte ground4;

	private short contentBehavior;
	private boolean outside;
	private float centerX;
	private float centerZ;
	private int room = 0;
	public static HashMap<Integer,ArrayList<BuildingRegions>> _staticRegions = new HashMap<>();
	private final boolean exitRegion;
	private final boolean stairs;
	
	private ArrayList<Vector3f> regionPoints = new ArrayList<>();
	public Vector3f center;

	/**
	 * ResultSet Constructor
	 */

	public BuildingRegions(ResultSet rs) throws SQLException {

		buildingID = rs.getInt("buildingID");
		level = rs.getInt("level");
		room = rs.getInt("room");
		numVertex = rs.getInt("numVertex");
		vertex1X = rs.getFloat("vertex1X");
		vertex1Y = rs.getFloat("vertex1Y");
		vertex1Z = rs.getFloat("vertex1Z");
		vertex2X = rs.getFloat("vertex2X");
		vertex2Y = rs.getFloat("vertex2Y");
		vertex2Z = rs.getFloat("vertex2Z");
		vertex3X = rs.getFloat("vertex3X");
		vertex3Y = rs.getFloat("vertex3Y");
		vertex3Z = rs.getFloat("vertex3Z");
		vertex4X = rs.getFloat("vertex4X");
		vertex4Y = rs.getFloat("vertex4Y");
		vertex4Z = rs.getFloat("vertex4Z");
		
		regionPoints.add(new Vector3f(vertex1X,vertex1Y,vertex1Z));
		regionPoints.add(new Vector3f(vertex2X,vertex2Y,vertex2Z));
		regionPoints.add(new Vector3f(vertex3X,vertex3Y,vertex3Z));
		
		
		if(numVertex ==4)
		regionPoints.add(new Vector3f(vertex4X,vertex4Y,vertex4Z));
	
		
		this.contentBehavior = (rs.getShort("unknown_Order1"));
		short state = rs.getShort("unknown_Order2");
		
		if (state == 2)
			this.outside = (true);
		else
			this.outside = (false);
		
		this.exitRegion = rs.getBoolean("colOrder1");
		this.stairs = rs.getBoolean("colOrder2");

		
		
		ground1 = rs.getByte("colOrder1");
		ground2 = rs.getByte("colOrder2");
		ground3 = rs.getByte("colOrder3");
		ground4 = rs.getByte("colOrder4");
		
		float centerY = rs.getFloat("unknown_VectorY");
		centerX = rs.getFloat("unknown_VectorX");
		centerZ = rs.getFloat("unknown_VectorZ");
		
		this.center = new Vector3f(centerX,centerY,centerZ);
		

	}



	public int getBuildingID() {
		return buildingID;
	}



	public void setBuildingID(int buildingID) {
		this.buildingID = buildingID;
	}



	public int getLevel() {
		return level;
	}



	public void setLevel(int level) {
		this.level = level;
	}



	public int getNumVertex() {
		return numVertex;
	}



	public void setNumVertex(int numVertex) {
		this.numVertex = numVertex;
	}



	public float getVertex1X() {
		return vertex1X;
	}



	public void setVertex1X(float vertex1x) {
		vertex1X = vertex1x;
	}



	public float getVertex1Y() {
		return vertex1Y;
	}



	public void setVertex1Y(float vertex1y) {
		vertex1Y = vertex1y;
	}



	public float getVertex1Z() {
		return vertex1Z;
	}



	public void setVertex1Z(float vertex1z) {
		vertex1Z = vertex1z;
	}



	public float getVertex2X() {
		return vertex2X;
	}



	public void setVertex2X(float vertex2x) {
		vertex2X = vertex2x;
	}



	public float getVertex2Y() {
		return vertex2Y;
	}



	public void setVertex2Y(float vertex2y) {
		vertex2Y = vertex2y;
	}



	public float getVertex2Z() {
		return vertex2Z;
	}



	public void setVertex2Z(float vertex2z) {
		vertex2Z = vertex2z;
	}



	public float getVertex3X() {
		return vertex3X;
	}



	public void setVertex3X(float vertex3x) {
		vertex3X = vertex3x;
	}



	public float getVertex3Y() {
		return vertex3Y;
	}



	public void setVertex3Y(float vertex3y) {
		vertex3Y = vertex3y;
	}



	public float getVertex3Z() {
		return vertex3Z;
	}



	public void setVertex3Z(float vertex3z) {
		vertex3Z = vertex3z;
	}



	public float getVertex4X() {
		return vertex4X;
	}



	public void setVertex4X(float vertex4x) {
		vertex4X = vertex4x;
	}



	public float getVertex4Y() {
		return vertex4Y;
	}



	public void setVertex4Y(float vertex4y) {
		vertex4Y = vertex4y;
	}



	public float getVertex4Z() {
		return vertex4Z;
	}



	public void setVertex4Z(float vertex4z) {
		vertex4Z = vertex4z;
	}



	public static HashMap<Integer, ArrayList<BuildingRegions>> get_staticRegions() {
		return _staticRegions;
	}



	public static void set_staticRegions(HashMap<Integer, ArrayList<BuildingRegions>> _staticRegions) {
		BuildingRegions._staticRegions = _staticRegions;
	}



	public static void loadAllStaticColliders(){
		_staticRegions = DbManager.BuildingQueries.LOAD_BUILDING_REGIONS();
	}

	public static ArrayList<BuildingRegions> GetStaticCollidersForMeshID(int meshID) {
		return _staticRegions.get(meshID);
	}

	public boolean isGroundLevel(){
		if (this.level > 0)
			return false;

		if (this.ground1 == 0)
			return true;
		if (this.ground2 == 0)
			return true;
		if (this.ground3 == 0)
			return true;
        return this.ground4 == 0;

    }



	public float getCenterX() {
		return centerX;
	}



	public void setCenterX(float centerX) {
		this.centerX = centerX;
	}



	public float getCenterY() {
		return centerZ;
	}



	public void setCenterY(float centerY) {
		this.centerZ = centerY;
	}



	public boolean isOutside() {
		return outside;
	}

	public short getContentBehavior() {
		return contentBehavior;
	}



	public int getRoom() {
		return room;
	}



	public ArrayList<Vector3f> getRegionPoints() {
		return regionPoints;
	}



	public boolean isExitRegion() {
		return exitRegion;
	}



	public boolean isStairs() {
		return stairs;
	}

}
