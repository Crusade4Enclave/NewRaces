// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.GameObjectType;
import engine.gameManager.DbManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


public class RuneBaseAttribute extends AbstractGameObject {

	private short attributeID;
	private short modValue;

	private int runeBaseID;

	public static HashMap<Integer,ArrayList<RuneBaseAttribute>> runeBaseAttributeMap = new HashMap<>();


	/**
	 * No Table ID Constructor
	 */
	public RuneBaseAttribute(short attributeID, short modValue) {
		super();

		this.attributeID = attributeID;
		this.modValue = modValue;
	}

	/**
	 * Normal
	 */
	public RuneBaseAttribute(short attributeID, short modValue, int newUUID) {
		super(newUUID);

		this.attributeID = attributeID;
		this.modValue = modValue;
	}
	/**
	 * ResultSet Constructor
	 */
	public RuneBaseAttribute(ResultSet rs) throws SQLException {
		super(rs);

		this.attributeID = rs.getShort("attributeID");
		this.modValue = rs.getShort("modValue");
		this.runeBaseID = rs.getInt("RuneBaseID");
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

	public static void LoadAllAttributes(){
		DbManager.RuneBaseAttributeQueries.GET_ATTRIBUTES_FOR_RUNEBASE();


		//cache attributeLists for rune.
		for (AbstractGameObject ago : DbManager.getList(GameObjectType.RuneBaseAttribute)){

			RuneBaseAttribute runeBaseAttribute = (RuneBaseAttribute)ago;

			int runeBaseID = ((RuneBaseAttribute)runeBaseAttribute).runeBaseID;
			if (runeBaseAttributeMap.get(runeBaseID) == null){
				ArrayList<RuneBaseAttribute> attributeList = new ArrayList<>();
				attributeList.add(runeBaseAttribute);
				runeBaseAttributeMap.put(runeBaseID, attributeList);
			}
			else{
				ArrayList<RuneBaseAttribute>attributeList = runeBaseAttributeMap.get(runeBaseID);
				attributeList.add(runeBaseAttribute);
				runeBaseAttributeMap.put(runeBaseID, attributeList);
			}

		}

	}

	/*
	 * Utils
	 */



	@Override
	public void updateDatabase() {
		// TODO Auto-generated method stub
	}
}
