// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.Enum;
import engine.Enum.ChatChannelType;
import engine.gameManager.DbManager;
import engine.job.AbstractScheduleJob;
import engine.net.DispatchMessage;
import engine.net.client.msg.chat.ChatSystemMsg;
import engine.objects.City;
import org.pmw.tinylog.Logger;

public class ActivateBaneJob extends AbstractScheduleJob {

    private final int cityUUID;

    public ActivateBaneJob(int cityUUID) {
        super();
        this.cityUUID = cityUUID;

    }

    @Override
    protected void doJob() {

        City city;

        city = (City) DbManager.getObject(Enum.GameObjectType.City, cityUUID);

        if (city == null)
            return;


        if (city.getBane() == null) {
            Logger.info( "No bane found for " + city.getCityName());
            return;
        }

        if (city.getBane().isErrant()) {
            Logger.info("Removed errant bane on " + city.getCityName());
            city.getBane().remove();
            return;
        }

        if (city.getBane() == null)
            return;

        if (city.protectionEnforced == true)
            city.protectionEnforced = false;
        else {
            Logger.info("Bane on " + city.getCityName() + " activated for unprotected city?");
            return;
        }

        Logger.info("ActivateBaneJob", "Bane on " + city.getCityName() + " is now active");

        ChatSystemMsg msg = new ChatSystemMsg(null, "[Bane Channel]  The Banecircle placed by " + city.getBane().getOwner().getGuild().getName() + " is now active! Buildings are now vulnerable to damage!");
        msg.setMessageType(4); // Error message
        msg.setChannel(ChatChannelType.SYSTEM.getChannelID());
        
        DispatchMessage.dispatchMsgToAll(msg);
    }

    @Override
    protected void _cancelJob() {
    }

}
