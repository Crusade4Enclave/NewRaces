// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.server.MBServerStatics;

import java.util.concurrent.ConcurrentHashMap;

public enum Resource {

	ADAMANT("DefaultAdamant", 1557001525, 10, 1580003),
	AGATE("DefaultAgate", -1096157543, 20, 1580009),
	ANTIMONY("DefaultAntimony", 1256147265, 10, 1580014),
	AZOTH("DefaultAzoth", -1205326951, 20, 1580012),
	BLOODSTONE("DefaultBloodstone", -1912381716, 5, 1580020),
	BRONZEWOOD("DefaultBronzewood", -519681813, 30, 1580006),
	COAL("DefaultCoal", -1672872311, 30, 1580008),
	DIAMOND("DefaultDiamond", 1540225085, 20, 1580010),
	GALVOR("DefaultGalvor", -1683992404, 5, 1580017),
	IRON("DefaultIron", -1673518119, 20, 1580002),
	LUMBER("DefaultLumber", -1628412684, 100, 1580004),
	MANDRAKE("DefaultMandrake", -1519910613, 10, 1580007),
	MITHRIL("DefaultMithril", 626743397, 5, 1580021),
	OAK("DefaultOak", -1653034775, 30, 1580005),
	OBSIDIAN("DefaultObsidian", 778019055, 5, 1580019),
	ONYX("DefaultOnyx", -1675952151, 10, 1580011),
	ORICHALK("DefaultOrichalk", -1468730955, 30, 1580013),
	QUICKSILVER("DefaultQuicksilver", -2081208434, 10, 1580016),
	STONE("DefaultStone", -1094703863, 100, 1580000),
	SULFUR("DefaultSulfur", -1763687412, 10, 1580015),
	TRUESTEEL("DefaultTruesteel", -169012482, 20, 1580001),
	WORMWOOD("DefaultWormwood", 1204785075, 5, 1580018),
	GOLD("DefaultGold", -1670881623, 50000, 7);

	public final String name;
	public final int hash;
	public final int baseProduction;
	public final int UUID;
	public static ConcurrentHashMap<Integer, Resource> resourceByHash;

	Resource(String name, int hash, int baseProduction, int uuid) {
		this.name = name;
		this.hash = hash;
		this.baseProduction = baseProduction;
		this.UUID = uuid;
	}
	
	public static Resource GetResourceByHash(int hash){
		for (Resource resource: Resource.values()){
			if (hash == resource.hash)
				return resource;
		}
		return Resource.MITHRIL;
	}

	//load lookups via hashes
	static {
        resourceByHash = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
        for (Resource r : Resource.values())
            resourceByHash.put(r.hash, r);
    }
}
