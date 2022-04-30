// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.objects;

import java.sql.ResultSet;
import java.sql.SQLException;


public class PowersBaseAttribute extends AbstractGameObject {

	private final short attributeID;
	private final short modValue;
	private final short castTime;
	private final short duration;
	private final short recycleTime;

	/**
	 * ResultSet Constructor
	 */
	public PowersBaseAttribute(ResultSet rs) throws SQLException {
		super(rs);

		this.attributeID = rs.getShort("attributeID");
		this.modValue = rs.getShort("modValue");
		this.castTime = rs.getShort("castTime");
		this.duration = rs.getShort("duration");
		this.recycleTime = rs.getShort("recycleTime");
	}

	/*
	 * Getters
	 */
	public short getAttributeID() {
		return attributeID;
	}

	public short getModValue() {
		return modValue;
	}

	public short getCastTime() {
		return castTime;
	}

	public short getDuration() {
		return duration;
	}

	public short getRecycleTime() {
		return recycleTime;
	}


	@Override
	public void updateDatabase() {
		// TODO Create update logic.
	}
}
