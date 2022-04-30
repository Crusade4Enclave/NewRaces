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


public class EnchantmentBase extends AbstractGameObject {

	private final String name;
	private final String prefix;
	private final String suffix;

	private final byte attributeID;
	private final int modValue;

	/**
	 * No Table ID Constructor
	 */
	public EnchantmentBase(String name, String prefix, String suffix,
			byte attributeID, int modValue) {
		super();
		this.name = name;
		this.prefix = prefix;
		this.suffix = suffix;
		this.attributeID = attributeID;
		this.modValue = modValue;
	}

	/**
	 * Normal Constructor
	 */
	public EnchantmentBase(String name, String prefix, String suffix,
			byte attributeID, int modValue, int newUUID) {
		super(newUUID);
		this.name = name;
		this.prefix = prefix;
		this.suffix = suffix;
		this.attributeID = attributeID;
		this.modValue = modValue;
	}

	/**
	 * ResultSet Constructor
	 */
	public EnchantmentBase(ResultSet rs) throws SQLException {
		super(rs);

		this.name = rs.getString("name");
		this.prefix = rs.getString("prefix");
		this.suffix = rs.getString("suffix");
		this.attributeID = rs.getByte("attributeID");
		this.modValue = rs.getInt("modValue");

	}

	/*
	 * Getters
	 */
	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public byte getAttributeID() {
		return attributeID;
	}

	public int getModValue() {
		return modValue;
	}

	@Override
	public void updateDatabase() {
		// TODO Create update logic.
	}
}
