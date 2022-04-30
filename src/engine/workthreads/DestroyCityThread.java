// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.workthreads;

/*
 * This thread is spawned to process destruction
 * of a player owned city, including subguild
 * and database cleanup.
 *
 * The 'destroyed' city zone persists until the
 * next reboot.
 */

import engine.Enum;
import engine.gameManager.DbManager;
import engine.gameManager.GuildManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.objects.Building;
import engine.objects.City;
import engine.objects.Guild;
import engine.objects.Zone;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;

public class DestroyCityThread implements Runnable {

	City city;

	public DestroyCityThread(City city) {

		this.city = city;
	}

	public void run(){

		// Member variable declaration

		Zone cityZone;
		Zone newParent;
		Guild formerGuild;
		Vector3fImmutable localCoords;
		ArrayList<Guild> subGuildList;

		// Member variable assignment

		cityZone = city.getParent();
		newParent = cityZone.getParent();
		formerGuild = city.getTOL().getGuild();

		// Former guild loses it's tree!

		if (DbManager.GuildQueries.SET_GUILD_OWNED_CITY(formerGuild.getObjectUUID(), 0)) {

			//Successful Update of guild

			formerGuild.setGuildState(engine.Enum.GuildState.Errant);
			formerGuild.setNation(null);
			formerGuild.setCityUUID(0);
			GuildManager.updateAllGuildTags(formerGuild);
			GuildManager.updateAllGuildBinds(formerGuild, null);
		}

		// By losing the tree, the former owners lose all of their subguilds.

		if (formerGuild.getSubGuildList().isEmpty() == false) {

			subGuildList = new ArrayList<>();

			for (Guild subGuild : formerGuild.getSubGuildList()) {
				subGuildList.add(subGuild);
			}

			for (Guild subGuild : subGuildList) {
				formerGuild.removeSubGuild(subGuild);
			}
		}

		// Build list of buildings within this parent zone

		for (Building cityBuilding : cityZone.zoneBuildingSet) {

			// Sanity Check in case player deletes the building
			// before this thread can get to it

			if (cityBuilding == null)
				continue;

			// Do nothing with the banestone.  It will be removed elsewhere

			if (cityBuilding.getBlueprint().getBuildingGroup().equals(Enum.BuildingGroup.BANESTONE))
				continue;
			
			// All buildings are moved to a location relative
			// to their new parent zone

			localCoords = ZoneManager.worldToLocal(cityBuilding.getLoc(), newParent);

			DbManager.BuildingQueries.MOVE_BUILDING(cityBuilding.getObjectUUID(), newParent.getObjectUUID(), localCoords.x, localCoords.y, localCoords.z);

			// All buildings are re-parented to a zone one node
			// higher in the tree (continent) as we will be
			// deleting the city zone very shortly.

			if (cityBuilding.getParentZoneID() != newParent.getParentZoneID())
				cityBuilding.setParentZone(newParent);

			// No longer a tree, no longer any protection contract!

			cityBuilding.setProtectionState(Enum.ProtectionState.NONE);

			// Destroy all remaining city assets

			if ((cityBuilding.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.BARRACK)
					|| (cityBuilding.getBlueprint().isWallPiece())
					|| (cityBuilding.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.SHRINE)
					|| (cityBuilding.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.TOL)
					|| (cityBuilding.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.SPIRE)
					|| (cityBuilding.getBlueprint().getBuildingGroup() == Enum.BuildingGroup.WAREHOUSE)) {

				if (cityBuilding.getRank() != -1)
					cityBuilding.setRank(-1);
			}
		}

		if (city.getRealm() != null)
			city.getRealm().removeCity(city.getObjectUUID());

		// It's now safe to delete the city zone from the database
		// which will cause a cascade delete of everything else


		if (DbManager.ZoneQueries.DELETE_ZONE(cityZone) == false) {
			Logger.error("DestroyCityThread", "Database error when deleting city zone: " + cityZone.getObjectUUID());
			return;
		}

		// Refresh the city for map requests

		City.lastCityUpdate = System.currentTimeMillis();

		// Zone and city should vanish upon next reboot
		// if the codebase reaches here.

		Logger.info(city.getParent().getName() + " uuid:" + city.getObjectUUID() + "has been destroyed!");
	}
}
