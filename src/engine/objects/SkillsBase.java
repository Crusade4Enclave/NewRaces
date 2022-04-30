// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.objects;

import engine.Enum.SourceType;
import engine.gameManager.DbManager;
import engine.server.MBServerStatics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class SkillsBase extends AbstractGameObject {

	private final String name;
	private final String nameNoSpace;
	private final String description;
	private final int token;
	private final short strMod;
	private final short dexMod;
	private final short conMod;
	private final short intMod;
	private final short spiMod;
	public static ConcurrentHashMap<String, SkillsBase> skillsCache = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	public static ConcurrentHashMap<Integer, SkillsBase> tokenCache = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	public static HashMap<Integer, HashMap<Integer, Integer>> runeSkillsCache = new HashMap<>();
	public SourceType sourceType;
	/**
	 * No Table ID Constructor
	 */
	public SkillsBase(String name, String description, int token, short strMod,
					  short dexMod, short conMod, short intMod, short spiMod) {
		super();
		this.name = name;
		this.nameNoSpace = name.replace(" ", "");
		this.sourceType = SourceType.GetSourceType(this.nameNoSpace.replace(",", ""));
		this.description = description;
		this.token = token;
		this.strMod = strMod;
		this.dexMod = dexMod;
		this.conMod = conMod;
		this.intMod = intMod;
		this.spiMod = spiMod;
	}

	/**
	 * Normal Constructor
	 */
	public SkillsBase(String name, String description, int token, short strMod,
			short dexMod, short conMod, short intMod, short spiMod, int newUUID) {
		super(newUUID);
		this.name = name;
		this.nameNoSpace = name.replace(" ", "");
		this.description = description;
		this.token = token;
		this.strMod = strMod;
		this.dexMod = dexMod;
		this.conMod = conMod;
		this.intMod = intMod;
		this.spiMod = spiMod;
	}

	/**
	 * ResultSet Constructor
	 */
	public SkillsBase(ResultSet rs) throws SQLException {
		super(rs);

		this.name = rs.getString("name");
		this.nameNoSpace = name.replace(" ", "");
		this.description = rs.getString("description");
		this.sourceType = SourceType.GetSourceType(this.nameNoSpace.replace("-", "").replace("\"", "").replace(",", ""));
		this.token = rs.getInt("token");
		this.strMod = rs.getShort("strMod");
		this.dexMod = rs.getShort("dexMod");
		this.conMod = rs.getShort("conMod");
		this.intMod = rs.getShort("intMod");
		this.spiMod = rs.getShort("spiMod");
	}

	/*
	 * Getters
	 */
	public String getName() {
		return name;
	}

	public String getNameNoSpace() {
		return nameNoSpace;
	}
	

	public String getDescription() {
		return description;
	}

	public int getToken() {
		return this.token;
	}

	public short getStrMod() {
		return this.strMod;
	}

	public short getDexMod() {
		return this.dexMod;
	}

	public short getConMod() {
		return this.conMod;
	}

	public short getIntMod() {
		return this.intMod;
	}

	public short getSpiMod() {
		return this.spiMod;
	}

	public static SkillsBase getFromCache(String name) {
		if (skillsCache.containsKey(name))
			return skillsCache.get(name);
		else
			return null;
	}

	public static SkillsBase getFromCache(int token) {
		if (tokenCache.containsKey(token))
			return tokenCache.get(token);
		else
			return null;
	}

	public static void putInCache(SkillsBase sb) {

		if(sb == null)
			return;

		DbManager.addToCache(sb);
        skillsCache.putIfAbsent(sb.name, sb);
        tokenCache.putIfAbsent(sb.token, sb);
	}



	@Override
	public void updateDatabase() {
		// TODO Auto-generated method stub
	}
}
