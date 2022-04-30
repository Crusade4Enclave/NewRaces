// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import java.awt.geom.Line2D;


public class Colliders  {


	private Line2D collider;
	private int doorID;
	private boolean link = false;
	public float startX;
	public float startY;
	public float endX;
	public float endY;

	public Colliders(Line2D collider, int doorID, boolean link) {
		super();
		this.collider = collider;
		this.doorID = doorID;
		this.link = link;
	}
	
	public Colliders(float startX, float startY, float endX ,float endY, int doorID, boolean link) {
		super();
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.doorID = doorID;
		this.link = link;
	}


	public int getDoorID() {
		return doorID;
	}

	public Line2D getCollider() {
		return collider;
	}


	public boolean isLink() {
		return link;
	}


	public void setLink(boolean link) {
		this.link = link;
	}




}
