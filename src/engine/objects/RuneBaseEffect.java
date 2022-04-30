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


public class RuneBaseEffect extends AbstractGameObject {

	private byte type;
	private String name;
	private short amount;
	private int runeBaseID;

	public static HashMap<Integer,ArrayList<RuneBaseEffect>> RuneIDBaseEffectMap = new HashMap<>();
	/**
	 * ResultSet Constructor
	 */
	public RuneBaseEffect(ResultSet rs) throws SQLException {
		super(rs);
		this.type = rs.getByte("type");
		this.name = rs.getString("name");
		this.amount = rs.getShort("amount");
		this.runeBaseID = rs.getInt("runeID");
	}

	/*
	 * Getters
	 */

	public int getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public short getAmount() {
		return this.amount;
	}


	@Override
	public void updateDatabase() {

	}

	public int getRuneBaseID() {
		return runeBaseID;
	}

	public static void LoadRuneBaseEffects(){
		//cache runebase effects.
		DbManager.RuneBaseEffectQueries.GET_ALL_RUNEBASE_EFFECTS();
		//store runebase effects in new hashmap.
		RuneBaseEffect.RuneIDBaseEffectMap = DbManager.RuneBaseEffectQueries.LOAD_BASEEFFECTS_FOR_RUNEBASE();
	}

}