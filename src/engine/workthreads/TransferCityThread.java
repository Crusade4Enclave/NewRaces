// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.workthreads;

/*
 * This thread is spawned to process transfer
 * ownership of a player owned city, including
 * subguild and database cleanup.
 *
 */

import engine.Enum;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.gameManager.GuildManager;
import engine.net.DispatchMessage;
import engine.net.client.msg.CityZoneMsg;
import engine.objects.AbstractCharacter;
import engine.objects.City;
import engine.objects.Guild;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;

public class TransferCityThread implements Runnable {

    City city;
    AbstractCharacter newOwner;

    public TransferCityThread(City city, AbstractCharacter newOwner) {

        this.city = city;
        this.newOwner = newOwner;
    }

    public void run(){

        Guild formerGuild;
        ArrayList<Guild> subGuildList;

        formerGuild = this.city.getTOL().getGuild();

        // Former guild loses it's tree!

        if (formerGuild != null)
            if (DbManager.GuildQueries.SET_GUILD_OWNED_CITY(formerGuild.getObjectUUID(), 0)) {
                formerGuild.setGuildState(Enum.GuildState.Errant);
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

        //Reset TOL to rank 1

        city.getTOL().setRank(1);

        // Transfer all assets to new owner

        city.claim(newOwner);

        //Set name of City to attacker's guild name
        BuildingManager.setUpgradeDateTime(city.getTOL(),null, 0);
        city.getTOL().setName(newOwner.getGuild().getName());

        // Send updated cityZone to players
        CityZoneMsg czm = new CityZoneMsg(2, city.getTOL().getLoc().x, city.getTOL().getLoc().y, city.getTOL().getLoc().z, city.getTOL().getName(), city.getTOL().getParentZone(), 0f, 0f);

        DispatchMessage.dispatchMsgToAll(czm);

        // Reset city timer for map update

        City.lastCityUpdate = System.currentTimeMillis();

        Logger.info("uuid:" + city.getObjectUUID() + "transferred from " + formerGuild.getName() +
                       " to " + newOwner.getGuild().getName());
    }
}
