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

import static engine.server.MBServerStatics.MINE_LATE_WINDOW;

public class HourlyJobThread implements Runnable {

    private static final int hotzoneCount = 0;

    public HourlyJobThread() {

    }

    public void run() {

        // *** REFACTOR: TRY TRY TRY TRY {{{{{{{{{{{ OMG

        Logger.info("Hourly job is now running.");

        try {

            ZoneManager.generateAndSetRandomHotzone();
            Zone hotzone = ZoneManager.getHotZone();

            if (hotzone == null) {
                Logger.error("Null hotzone returned from mapmanager");
            } else {
                Logger.info("new hotzone: " + hotzone.getName());
                WorldServer.setLastHZChange(System.currentTimeMillis());
            }

        } catch (Exception e) {
            Logger.error(e.toString());
        }

        // Open or Close mines for the current mine window.

        processMineWindow();

        // Deposit mine resources to Guilds

        for (Mine mine : Mine.getMines()) {

            try {
                mine.depositMineResources();
            } catch (Exception e) {
                Logger.info(e.getMessage() + " for Mine " + mine.getObjectUUID());
            }
        }

        // Reset time-gated access to WOO slider.
        // *** Do this after the mines open/close!

        if (LocalDateTime.now().getHour() == MINE_LATE_WINDOW) {
            Guild guild;

            for (AbstractGameObject dbObject : DbManager.getList(Enum.GameObjectType.Guild)) {
                guild = (Guild) dbObject;

                if (guild != null)
                    guild.wooWasModified = false;
            }
        }


        // Mines can only be claimed once per cycle.
        // This will reset at 1am after the last mine
        // window closes.

        if (LocalDateTime.now().getHour() == MINE_LATE_WINDOW + 1) {

            for (Mine mine : Mine.getMines()) {

                if (mine.wasClaimed == true)
                    mine.wasClaimed = false;
            }
        }

        // Decay Shrines at midnight every day

        if (LocalDateTime.now().getHour() == MINE_LATE_WINDOW)
            decayShrines();

        // Update city population values

        ConcurrentHashMap<Integer, AbstractGameObject> map = DbManager.getMap(Enum.GameObjectType.City);

        if (map != null) {

            for (AbstractGameObject ago : map.values()) {

                City city = (City) ago;

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
        Logger.info(WorldServer.getUptimeString());
        Logger.info(SimulationManager.getPopulationString());
        Logger.info(MessageDispatcher.getNetstatString());
        Logger.info(PurgeOprhans.recordsDeleted.toString() + "orphaned items deleted");
    }


    public static void decayShrines() {
        ArrayList<Shrine> shrineList = new ArrayList<>();

        for (Shrine shrine : Shrine.shrinesByBuildingUUID.values()) {
            try {
                Building shrineBuilding = (Building) DbManager.getObject(Enum.GameObjectType.Building, shrine.getBuildingID());

                if (shrineBuilding == null)
                    continue;


                if (shrineBuilding.getOwner().equals(shrineBuilding.getCity().getOwner()) == false)
                    shrineBuilding.claim(shrineBuilding.getCity().getOwner());
            } catch (Exception e) {
                Logger.info("Shrine " + shrine.getBuildingID() + " Error " + e);
            }
        }

        // Grab list of top two shrines of each type

        for (Shrine shrine : Shrine.shrinesByBuildingUUID.values()) {

            if (shrine.getRank() == 0 || shrine.getRank() == 1)
                shrineList.add(shrine);
        }

        Logger.info("Decaying " + shrineList.size() + " shrines...");

        // Top 2 shrines decay by 10% a day

        for (Shrine shrine : shrineList) {

            try {
                shrine.decay();
            } catch (Exception e) {
                Logger.info("Shrine " + shrine.getBuildingID() + " Error " + e);
            }
        }
    }

    public static void processMineWindow() {

        try {

            ArrayList<Mine> mines = Mine.getMines();

            for (Mine mine : mines) {

                try {

                    // Open Errant Mines

                    if (mine.getOwningGuild().isErrant()) {
                        mine.handleStartMineWindow();
                        Mine.setLastChange(System.currentTimeMillis());
                        continue;
                    }

                    // Open Mines with a current guild hour

                    if (mine.getOwningGuild().getNation().getMineTime() ==
                            LocalDateTime.now().getHour()) {
                        mine.handleStartMineWindow();
                        Mine.setLastChange(System.currentTimeMillis());
                        continue;
                    }

                    // Close all remaining mines

                    if (mine.handleEndMineWindow())
                        Mine.setLastChange(System.currentTimeMillis());

                } catch (Exception e) {
                    Logger.error("mineID: " + mine.getObjectUUID(), e.toString());
                }
            }
        } catch (Exception e) {
            Logger.error(e.toString());
        }
    }
}
