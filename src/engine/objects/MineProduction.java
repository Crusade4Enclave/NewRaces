// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import java.util.HashMap;

public enum MineProduction {

	LUMBER("Lumber Camp", new HashMap<>(), Resource.WORMWOOD, 1618637196, 1663491950),
	ORE("Ore Mine", new HashMap<>(), Resource.OBSIDIAN, 518103023, -788976428),
	GOLD("Gold Mine", new HashMap<>(), Resource.GALVOR, -662193002, -1227205358),
	MAGIC("Magic Mine", new HashMap<>(), Resource.BLOODSTONE, 504746863, -1753567069);

	public final String name;
	public final HashMap<Integer, Resource> resources;
	public final Resource xpac;
	public final int hash;
	public final int xpacHash;

	MineProduction(String name, HashMap<Integer, Resource>resources, Resource xpac, int hash, int xpacHash) {
		this.name = name;
		this.resources = resources;
		this.xpac = xpac;
		this.hash = hash;
		this.xpacHash = xpacHash;
	}

	public static void addResources() {
		if (MineProduction.LUMBER.resources.size() == 0) {
			MineProduction.LUMBER.resources.put(7, Resource.GOLD);
			MineProduction.LUMBER.resources.put(1580004, Resource.LUMBER);
			MineProduction.LUMBER.resources.put(1580005, Resource.OAK);
			MineProduction.LUMBER.resources.put(1580006, Resource.BRONZEWOOD);
			MineProduction.LUMBER.resources.put(1580007, Resource.MANDRAKE);
		}
		if (MineProduction.ORE.resources.size() == 0) {
			MineProduction.ORE.resources.put(7, Resource.GOLD);
			MineProduction.ORE.resources.put(1580000, Resource.STONE);
			MineProduction.ORE.resources.put(1580001, Resource.TRUESTEEL);
			MineProduction.ORE.resources.put(1580002, Resource.IRON);
			MineProduction.ORE.resources.put(1580003, Resource.ADAMANT);
		}
		if (MineProduction.GOLD.resources.size() == 0) {
			MineProduction.GOLD.resources.put(7, Resource.GOLD);
			MineProduction.GOLD.resources.put(1580000, Resource.STONE);
			MineProduction.GOLD.resources.put(1580008, Resource.COAL);
			MineProduction.GOLD.resources.put(1580009, Resource.AGATE);
			MineProduction.GOLD.resources.put(1580010, Resource.DIAMOND);
			MineProduction.GOLD.resources.put(1580011, Resource.ONYX);
		}
		if (MineProduction.MAGIC.resources.size() == 0) {
			MineProduction.MAGIC.resources.put(7, Resource.GOLD);
			MineProduction.MAGIC.resources.put(1580012, Resource.AZOTH);
			MineProduction.MAGIC.resources.put(1580013, Resource.ORICHALK);
			MineProduction.MAGIC.resources.put(1580014, Resource.ANTIMONY);
			MineProduction.MAGIC.resources.put(1580015, Resource.SULFUR);
			MineProduction.MAGIC.resources.put(1580016, Resource.QUICKSILVER);
		}
	}

	public static MineProduction getByName(String name) {
		if (name.toLowerCase().equals("lumber"))
			return MineProduction.LUMBER;
		else if (name.toLowerCase().equals("ore"))
			return MineProduction.ORE;
		else if (name.toLowerCase().equals("gold"))
			return MineProduction.GOLD;
		else
			return MineProduction.MAGIC;
	}

	public boolean validForMine(Resource r, boolean isXpac) {
		if (r == null)
			return false;
		if (this.resources.containsKey(r.UUID))
			return true;
		else return isXpac && r.UUID == this.xpac.UUID;
    }
	
	

//Name			Xpac		Resources
//Lumber Camp	Wormwood	Gold, Lumber, Oak, Bronzewood, Mandrake
//Ore Mine		Obsidian	Gold, Stone, Truesteal, Iron, Adamant
//Gold Mine		Galvor		Gold, Coal, Agate, Diamond, Onyx
//Magic Mine	Bloodstone	Gold, Orichalk, Azoth, Antimony, Quicksilver, Sulfer
}
