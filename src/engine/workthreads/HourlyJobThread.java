// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.workthreads;

import engine.Enum;
import engine.gameManager.DbManager;
import engine.gameManager.SimulationManager;
import engine.gameManager.ZoneManager;
import engine.net.MessageDispatcher;
import engine.objects.*;
import engine.server.world.WorldServer;
import org.pmw.tinylog.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class HourlyJobThread implements Runnable {

	private static int hotzoneCount = 0;

	public HourlyJobThread() {

	}

	public void run() {

		// *** REFACTOR: TRY TRY TRY TRY {{{{{{{{{{{ OMG

		Logger.info("Hourly job is now running.");

			try {

					ZoneManager.generateAndSetRandomHotzone();
					Zone hotzone = ZoneManager.getHotZone();

					if (hotzone == null) {
						Logger.error( "Null hotzone returned from mapmanager");
					} else {
						Logger.info( "new hotzone: " + hotzone.getName());
						WorldServer.setLastHZChange(System.currentTimeMillis());
					}

			} catch (Exception e) {
				Logger.error( e.toString());
			}

			//updateMines.
			try {

				// Update mine effective date if this is a midnight window

				if (LocalDateTime.now().getHour() == 0 || LocalDateTime.now().getHour() == 24)
					Mine.effectiveMineDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

				ArrayList<Mine> mines = Mine.getMines();
				LocalDateTime now = LocalDateTime.now();

				for (Mine mine : mines) {
					try {

						if (mine.getOwningGuild() == null) {
							mine.handleStartMineWindow();
							Mine.setLastChange(System.currentTimeMillis());
							continue;
						}

						//handle claimed mines
                        LocalDateTime mineWindow = mine.openDate.withMinute(0).withSecond(0).withNano(0);

						if (mineWindow != null && now.plusMinutes(1).isAfter(mineWindow))
							if (!mine.getIsActive()) {
								mine.handleStartMineWindow();
								Mine.setLastChange(System.currentTimeMillis());

							}
							else if (mine.handleEndMineWindow())
								Mine.setLastChange(System.currentTimeMillis());
					} catch (Exception e) {
						Logger.error ("mineID: " + mine.getObjectUUID(), e.toString());
					}
				}
			} catch (Exception e) {
				Logger.error( e.toString());
			}

			for (Mine mine : Mine.getMines()) {

				try {
					mine.depositMineResources();
				} catch (Exception e) {
					Logger.info(e.getMessage() + " for Mine " + mine.getObjectUUID());
				}
			}


			// Update city population values

			ConcurrentHashMap<Integer, AbstractGameObject> map = DbManager.getMap(Enum.GameObjectType.City);

			if (map != null) {

				for (AbstractGameObject ago : map.values()){

					City city = (City)ago;

					if (city != null)
						if (city.getGuild() != null) {
							ArrayList<PlayerCharacter> guildList = Guild.GuildRoster(city.getGuild());
							city.setPopulation(guildList.size());
						}
				}
				City.lastCityUpdate = System.currentTimeMillis();
			} else {
				Logger.error("missing city map");
			}

			// Log metrics to console
			Logger.info( WorldServer.getUptimeString());
			Logger.info( SimulationManager.getPopulationString());
			Logger.info( MessageDispatcher.getNetstatString());
			Logger.info(PurgeOprhans.recordsDeleted.toString() + "orphaned items deleted");
	}
}
