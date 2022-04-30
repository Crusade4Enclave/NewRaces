// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.DbManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class StaticColliders  {

	private int meshID;
	private float startX;
	private float startY;
	private float endX;
	private float endY;
	private int doorID;
	public static HashMap<Integer,ArrayList<StaticColliders>> _staticColliders = new HashMap<>();
	private boolean link = false;




	/**
	 * ResultSet Constructor
	 */

	public StaticColliders(ResultSet rs) throws SQLException {
		this.meshID = rs.getInt("meshID");
		this.startX = rs.getInt("startX");
		this.startY = rs.getInt("startY");
		this.endX = rs.getInt("endX");
		this.endY = rs.getInt("endY");
		this.doorID = rs.getInt("doorID");
		this.link = rs.getBoolean("link");
	}

	public StaticColliders(int meshID, float startX, float startY, float endX,
			float endY, int doorID,boolean link) {
		super();
		this.meshID = meshID;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.doorID = doorID;
		this.link = link;
	}

	public static void loadAllStaticColliders(){
		_staticColliders = DbManager.BuildingQueries.LOAD_ALL_STATIC_COLLIDERS();
	}

	public static ArrayList<StaticColliders> GetStaticCollidersForMeshID(int meshID) {
		return _staticColliders.get(meshID);
	}




	public int getMeshID() {
		return meshID;
	}

	public float getStartX() {
		return startX;
	}

	public float getStartY() {
		return startY;
	}

	public float getEndX() {
		return endX;
	}

	public float getEndY() {
		return endY;
	}

	public int getDoorID() {
		return doorID;
	}

	public boolean isLink() {
		return link;
	}

	public void setLink(boolean link) {
		this.link = link;
	}
}
