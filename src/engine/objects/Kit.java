// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.ItemContainerType;
import engine.Enum.OwnerType;
import engine.gameManager.DbManager;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


public class Kit extends AbstractGameObject {

	private final int raceBaseClassID;
	private final byte kitNumber;
	private final int legs;
	private final int chest;
	private final int feet;
	private final int offhand;
	private final int weapon;
	public static HashMap<Integer,Boolean> NoobGearIDS = new HashMap<>();
	public static HashMap<Integer, ArrayList<Kit>> RaceClassIDMap = new HashMap<>();


	/**
	 * No Table ID Constructor
	 */
	public Kit(int raceBaseClassID, byte kitNumber, int legs, int chest,
			int feet, int offhand, int weapon) {
		super();
		this.raceBaseClassID = raceBaseClassID;
		this.kitNumber = kitNumber;
		this.legs = legs;
		this.chest = chest;
		this.feet = feet;
		this.offhand = offhand;
		this.weapon = weapon;
	}

	/**
	 * Normal Constructor
	 */
	public Kit(int raceBaseClassID, byte kitNumber, int legs, int chest,
			int feet, int offhand, int weapon, int newUUID) {
		super(newUUID);
		this.raceBaseClassID = raceBaseClassID;
		this.kitNumber = kitNumber;
		this.legs = legs;
		this.chest = chest;
		this.feet = feet;
		this.offhand = offhand;
		this.weapon = weapon;
	}

	/**
	 * RecordSet Constructor
	 */
	public Kit(ResultSet rs) throws SQLException, UnknownHostException {
		super(rs);

		this.raceBaseClassID = rs.getInt("RaceBaseClassesID");
		this.kitNumber = rs.getByte("kitNumber");

		this.legs = rs.getInt("legs");
		this.chest = rs.getInt("chest");
		this.feet = rs.getInt("feet");
		this.offhand = rs.getInt("offhand");
		this.weapon = rs.getInt("weapon");
		if (Kit.RaceClassIDMap.containsKey(this.raceBaseClassID)){
			Kit.RaceClassIDMap.get(this.raceBaseClassID).add(this);
		}else{
			ArrayList<Kit> tempList = new ArrayList<>();
			tempList.add(this);
			Kit.RaceClassIDMap.put(this.raceBaseClassID, tempList);

		}

		if (this.legs != 0)
			Kit.NoobGearIDS.put(this.legs, true);
		if (this.chest != 0)
			Kit.NoobGearIDS.put(this.chest, true);
		if (this.feet != 0)
			Kit.NoobGearIDS.put(this.feet, true);
		if (this.offhand != 0)
			Kit.NoobGearIDS.put(this.offhand, true);
		if (this.weapon != 0)
			Kit.NoobGearIDS.put(this.weapon, true);

	}

	public static boolean IsNoobGear(int itemID){

        return Kit.NoobGearIDS.containsKey(itemID);

    }

	/*
	 * Getters
	 */

	public int getRaceBaseClassID() {
		return raceBaseClassID;
	}

	public byte getKitNumber() {
		return kitNumber;
	}

	public int getLegs() {
		return legs;
	}

	public int getChest() {
		return chest;
	}

	public int getFeet() {
		return feet;
	}

	public int getOffhand() {
		return offhand;
	}

	public int getWeapon() {
		return weapon;
	}

	public void equipPCwithKit(PlayerCharacter pc) {
		if (weapon != 0)
			kitItemCreator(pc, weapon, MBServerStatics.SLOT_MAINHAND);
		if (offhand != 0)
			kitItemCreator(pc, offhand, MBServerStatics.SLOT_OFFHAND);
		if (chest != 0)
			kitItemCreator(pc, chest, MBServerStatics.SLOT_CHEST);
		if (legs != 0)
			kitItemCreator(pc, legs, MBServerStatics.SLOT_LEGGINGS);
		if (feet != 0)
			kitItemCreator(pc, feet, MBServerStatics.SLOT_FEET);
	}

	private static boolean kitItemCreator(PlayerCharacter pc, int itemBase, int slot)
			 {
		ItemBase i = ItemBase.getItemBase(itemBase);

		Item temp = new Item( i, pc.getObjectUUID(),
				OwnerType.PlayerCharacter, (byte) 0, (byte) 0, (short) 0, (short) 0,
				false, false,ItemContainerType.EQUIPPED, (byte) slot,
                new ArrayList<>(),"");

		try {
			temp = DbManager.ItemQueries.ADD_ITEM(temp);
		} catch (Exception e) {
		Logger.error(e);
		}

		if (temp == null) {
			Logger.info("Ungoof this goof, something is wrong with our kit.");
		}
		return true;
	}

	public static int GetKitIDByRaceClass(final int raceID, final int classID){
		switch (raceID){
		case 2000:
			switch(classID){
			case 2500:
				return 2;
			case 2501:
				return 3;
			case 2502:
				return 4;
			case 2503:
				return 5;
			}
		case 2001:

			switch(classID){
			case 2500:
				return 6;
			case 2501:
				return 7;
			case 2502:
				return 8;
			case 2503:
				return 9;
			}
		case 2002:
			switch(classID){
			case 2500:
				return 10;
			case 2501:
				return 11;
			case 2502:
				return 12;

			}
		case 2003:
			switch(classID){
			case 2500:
				return 13;
			case 2501:
				return 14;
			case 2502:
				return 15;
			}
		case 2004:

			switch(classID){
			case 2500:
				return 16;
			case 2501:
				return 17;
			}
		case 2005:
			switch(classID){
			case 2500:
				return 18;
			case 2501:
				return 19;
			}
		case 2006:

			switch(classID){
			case 2500:
				return 20;
			case 2501:
				return 21;

			}
		case 2008:

			switch(classID){
			case 2500:
				return 22;
			case 2501:
				return 23;
			case 2502:
				return 24;
			case 2503:
				return 25;
			}

		case 2009:

			switch(classID){
			case 2500:
				return 26;
			case 2501:
				return 27;
			case 2502:
				return 28;
			case 2503:
				return 29;
			}
		case 2010:

			switch(classID){
			case 2500:
				return 30;
			}

		case 2011:

			switch(classID){
			case 2500:
				return 31;
			case 2501:
				return 32;
			case 2502:
				return 33;
			case 2503:
				return 34;
			}

		case 2012:

			switch(classID){
			case 2500:
				return 35;
			case 2501:
				return 36;
			case 2502:
				return 37;
			case 2503:
				return 38;
			}

		case 2013:

			switch(classID){
			case 2500:
				return 39;
			case 2501:
				return 40;
			case 2502:
				return 41;
			case 2503:
				return 42;
			}

		case 2014:

			switch(classID){
			case 2500:
				return 43;
			case 2501:
				return 44;
			case 2502:
				return 45;
			case 2503:
				return 46;
			}

		case 2015:

			switch(classID){
			case 2500:
				return 47;
			case 2502:
				return 48;
			case 2503:
				return 49;

			}

		case 2016:

			switch(classID){
			case 2500:
				return 50;
			case 2502:
				return 51;
			case 2503:
				return 52;

			}

		case 2017:

			switch(classID){
			case 2500:
				return 53;
			case 2501:
				return 54;

			}

		case 2025:

			switch(classID){
			case 2500:
				return 55;
			case 2501:
				return 56;
			case 2502:
				return 57;
			case 2503:
				return 58;
			}

		case 2026:

			switch(classID){
			case 2500:
				return 59;
			case 2501:
				return 60;
			case 2502:
				return 61;
			case 2503:
				return 62;
			}

		case 2027:

			switch(classID){
			case 2500:
				return 63;
			}

		case 2028:

			switch(classID){
			case 2500:
				return 64;

			case 2502:
				return 65;
			case 2503:
				return 66;
			}

		case 2029:

			switch(classID){
			case 2500:
				return 67;

			case 2502:
				return 68;
			case 2503:
				return 69;
			}



		}
		return -1;
	}

	@Override
	public void updateDatabase() {
		// TODO Create update logic.
	}
}
