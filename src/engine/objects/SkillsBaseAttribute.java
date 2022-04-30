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


public class SkillsBaseAttribute extends AbstractGameObject {

	private short attributeID;
	private short modValue;

	/**
	 * No Table ID Constructor
	 */
	public SkillsBaseAttribute(short attributeID, short modValue) {
		super();

		this.attributeID = attributeID;
		this.modValue = modValue;
	}

	/**
	 * Normal Constructor
	 */
	public SkillsBaseAttribute(short attributeID, short modValue, int newUUID) {
		super(newUUID);

		this.attributeID = attributeID;
		this.modValue = modValue;
	}

	/**
	 * ResultSet Constructor
	 */
	public SkillsBaseAttribute(ResultSet rs) throws SQLException {
		super(rs);

		this.attributeID = rs.getShort("attributeID");
		this.modValue = rs.getShort("modValue");
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


	/*
	 * Database
	 */
	@Override
	public void updateDatabase() {
		// TODO Auto-generated method stub
	}
}
