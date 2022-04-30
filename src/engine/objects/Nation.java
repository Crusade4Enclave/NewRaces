// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.objects;

import engine.net.ByteBufferWriter;

import java.sql.ResultSet;
import java.sql.SQLException;


public class Nation extends AbstractWorldObject {

	private final String name;
	private GuildTag gt;
	private String motd = "";
	private int primaryGuildID = 0;

	/**
	 * No Id Constructor
	 */
	public Nation( String name, GuildTag gt) {
		super();
		this.name = name;
		this.gt = gt;
	}

	/**
	 * Normal Constructor
	 */
	public Nation(String name, GuildTag gt, int newUUID) {
		super(newUUID);
		this.name = name;
		this.gt = gt;
	}

	/**
	 * ResultSet Constructor
	 */
	public Nation(ResultSet rs) throws SQLException {
		super(rs);

		this.name = rs.getString("name");

		this.gt = new GuildTag(	rs.getInt("backgroundColor01"),
								rs.getInt("backgroundColor02"),
								rs.getInt("symbolColor"),
								rs.getInt("symbol"),
								rs.getInt("backgroundDesign"));
		this.motd = rs.getString("motd");
		this.primaryGuildID = rs.getInt("primaryGuild");
	}

	/*
	 * Getters
	 */
	@Override
	public String getName() {
		return this.name;
	}

	public GuildTag getGuildTag() {
		return this.gt;
	}

	public String getMOTD() {
		return this.motd;
	}

	public void setMOTD(String value) {
		this.motd = value;
	}

	public int getPrimaryGuildID() {
		return this.primaryGuildID;
	}

	public void setPrimaryGuildID(int value) {
		this.primaryGuildID = value;
	}

	/*
	 * Utils
	 */
	private static Nation n;

	public static Nation getErrantNation() {
		if (n == null) {
			n = new Nation("None", GuildTag.ERRANT, 0);
		}
		return n;
	}



	/*
	 * Serialization
	 */

	public static void serializeForTrack(Nation nation,ByteBufferWriter writer) {
		writer.putInt(nation.getObjectType().ordinal());
		writer.putInt(nation.getObjectUUID());
		writer.put((byte)1);
		GuildTag._serializeForDisplay(nation.gt,writer);
	}


	@Override
	public void updateDatabase() {

	}

	@Override
	public void runAfterLoad() {}
}
