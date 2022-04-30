// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.Enum.ShrineType;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Shrine extends AbstractWorldObject implements Comparable<Shrine> {

	private final ShrineType shrineType;
	private Integer favors;
	private final int buildingID;

	public static ConcurrentHashMap<Integer, Shrine> shrinesByBuildingUUID = new ConcurrentHashMap<>();

	/**
	 * ResultSet Constructor
	 */
	public Shrine(ResultSet rs) throws SQLException {
		super(rs);
		this.shrineType = ShrineType.valueOf(rs.getString("shrine_type"));
		this.favors = rs.getInt("shrine_favors");
		this.buildingID = rs.getInt("parent");
		shrinesByBuildingUUID.put(this.buildingID, this);
	}

	// Decays this shrine's favor by 10%

	public void decay() {

		if (this.getFavors() == 0)
			return;

		int decayAmount = (int) (this.getFavors() - (this.getFavors() *.10f));

		if (decayAmount < 0)
			decayAmount = 0;

		if (!DbManager.ShrineQueries.updateFavors(this, decayAmount, this.getFavors())) {
			Logger.error("Shrine Decay", "Error writing to DB. UUID: " + this.getObjectUUID());
			return;
		}
		this.favors = decayAmount;

		Logger.info( shrineType.name() + " uuid:" + this.getObjectUUID() + " Amount: " + this.getFavors() *.10f );

	}

	public synchronized boolean addFavor(PlayerCharacter boonOwner, Item boonItem) {

		if (boonOwner == null)
			return false;

		if (boonItem == null)
			return false;

		ItemBase ib = boonItem.getItemBase();

		if (ib == null)
			return false;

		if (!boonOwner.getCharItemManager().doesCharOwnThisItem(boonItem.getObjectUUID()))
			return false;

		ArrayList<Boon> boonList = Boon.GetBoonsForItemBase.get(ib.getUUID());

		if (boonList == null)
			return false;

		for (Boon boon : boonList) {

			ShrineType boonShrineType = boon.getShrineType();

            if (boonShrineType != shrineType)
				continue;

			//Same Shrine Type, add favors and stop loop.
			int amount = boon.getAmount() * boonItem.getNumOfItems();
			int oldAmount = this.favors;

			if (!DbManager.ShrineQueries.updateFavors(this, this.favors + amount, oldAmount)) {
				ChatManager.chatSystemError(boonOwner, "Failed to add boon to shrine.");
				return false;
			}

			this.favors += amount;
			boonOwner.getCharItemManager().delete(boonItem);
			boonOwner.getCharItemManager().updateInventory();
			return true;
		}
		return false;
	}

	public synchronized boolean takeFavor(PlayerCharacter boonOwner) {

		if (boonOwner == null)
			return false;

		int oldAmount = this.favors;
		int newAmount = this.favors - 1;

		if (!DbManager.ShrineQueries.updateFavors(this, newAmount, oldAmount)) {
			ChatManager.chatSystemError(boonOwner, "Failed to add boon to shrine.");
			return false;
		}
		this.favors = newAmount;
		return true;
	}

	public static boolean canTakeFavor(PlayerCharacter grantee, Shrine shrine) {

        if (shrine.shrineType.isRace())
			switch (grantee.getRaceID()) {
			case 2000:
			case 2001:
                if (shrine.shrineType == ShrineType.Aelfborn)
					return true;
				break;
			case 2002:
			case 2003:
                if (shrine.shrineType == ShrineType.Aracoix)
					return true;
				break;
			case 2004:
			case 2005:
                if (shrine.shrineType == ShrineType.Centaur)
					return true;
				break;
			case 2006:
                if (shrine.shrineType == ShrineType.Dwarf)
					return true;
				break;
			case 2008:
			case 2009:
                if (shrine.shrineType == ShrineType.Elf)
					return true;
				break;
			case 2010:
			case 2027:
                if (shrine.shrineType == ShrineType.HalfGiant)
					return true;
				break;
			case 2011:
			case 2012:
                if (shrine.shrineType == ShrineType.Human)
					return true;
				break;
			case 2013:
			case 2014:
                if (shrine.shrineType == ShrineType.Irekei)
					return true;
				break;
			case 2015:
			case 2016:
                if (shrine.shrineType == ShrineType.Shade)
					return true;
				break;
			case 2017:
                if (shrine.shrineType == ShrineType.Minotaur)
					return true;
				break;

			case 2025:
			case 2026:
                if (shrine.shrineType == ShrineType.Nephilim)
					return true;
				break;
			case 2028:
			case 2029:
                if (shrine.shrineType == ShrineType.Vampire)
					return true;
				break;

			}
		else
			switch (grantee.getPromotionClassID()) {
			case 2504:
                if (shrine.shrineType == ShrineType.Assassin)
					return true;
				break;
			case 2505:
                if (shrine.shrineType == ShrineType.Barbarian)
					return true;
				break;
			case 2506:
                if (shrine.shrineType == ShrineType.Bard)
					return true;
				break;
			case 2507:
                if (shrine.shrineType == ShrineType.Channeler)
					return true;
				break;
			case 2508:
                if (shrine.shrineType == ShrineType.Confessor)
					return true;
				break;
			case 2509:
                if (shrine.shrineType == ShrineType.Crusader)
					return true;
				break;
			case 2510:
                if (shrine.shrineType == ShrineType.Druid)
					return true;
				break;
			case 2511:
                if (shrine.shrineType == ShrineType.Fury)
					return true;
				break;
			case 2512:
                if (shrine.shrineType == ShrineType.Huntress)
					return true;
				break;
			case 2513:
                if (shrine.shrineType == ShrineType.Prelate)
					return true;
				break;
			case 2514:
                if (shrine.shrineType == ShrineType.Ranger)
					return true;
				break;
			case 2515:
                if (shrine.shrineType == ShrineType.Scout)
					return true;
				break;
			case 2516:
                if (shrine.shrineType == ShrineType.Templar)
					return true;
				break;
			case 2517:
                if (shrine.shrineType == ShrineType.Warlock)
					return true;
				break;
			case 2518:
                if (shrine.shrineType == ShrineType.Warrior)
					return true;
				break;
			case 2519:
                if (shrine.shrineType == ShrineType.Priest)
					return true;
				break;
			case 2520:
                if (shrine.shrineType == ShrineType.Thief)
					return true;
				break;
			case 2521:
                if (shrine.shrineType == ShrineType.Wizard)
					return true;
				break;
			case 2523:
                if (shrine.shrineType == ShrineType.Doomsayer)
					return true;
				break;
			case 2524:
                if (shrine.shrineType == ShrineType.Sentinel)
					return true;
				break;
			case 2525:
                if (shrine.shrineType == ShrineType.Necromancer)
					return true;
				break;
			case 2526:
                if (shrine.shrineType == ShrineType.Nightstalker)
					return true;
				break;
			}

		return false;
	}

	public static ShrineType getShrineTypeByBlueprintUUID(int blueprintUUID) {

		for (ShrineType shrineType : ShrineType.values()) {

			if (shrineType.getBlueprintUUID() == blueprintUUID)
				return shrineType;
		}
		return null;
	}

	@Override
	public int compareTo(Shrine other) {
		return other.favors.compareTo(this.favors);
	}

	public int getRank() {
        return shrineType.getShrinesCopy().indexOf(this);
	}

	public ShrineType getShrineType() {
		return shrineType;
	}

	public static void RemoveShrineFromCacheByBuilding(Building building) {

		if (building.getBlueprint() != null && building.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE) {
			Shrine shrine = Shrine.shrinesByBuildingUUID.get(building.getObjectUUID());

			if (shrine != null) {
                shrine.shrineType.RemoveShrineFromServerList(shrine);
				Shrine.shrinesByBuildingUUID.remove(building.getObjectUUID());
				DbManager.removeFromCache(Enum.GameObjectType.Shrine,
						shrine.getObjectUUID());
			}
		}

	}

	@Override
	public void updateDatabase() {
		// TODO Auto-generated method stub

	}

	public int getFavors() {
		return favors;
	}

	public int getBuildingID() {
		return buildingID;
	}

	@Override
	public void runAfterLoad() {
		// TODO Auto-generated method stub

	}


	@Override
	public void removeFromCache() {
		// TODO Auto-generated method stub

	}

	public void setFavors(Integer favors) {
		this.favors = favors;
	}

}
