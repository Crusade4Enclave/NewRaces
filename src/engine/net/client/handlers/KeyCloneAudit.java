package engine.net.client.handlers;

import engine.gameManager.DbManager;
import engine.objects.Group;
import engine.objects.PlayerCharacter;
import org.pmw.tinylog.Logger;

public enum KeyCloneAudit {
    KEYCLONEAUDIT;

    void audit(PlayerCharacter player, Group group) {

        int machineCount = 0;
        String machineID;

        machineID = player.getClientConnection().machineID;

        for (PlayerCharacter member : group.getMembers())
            if (machineID.equals(member.getClientConnection().machineID))
                machineCount = machineCount + 1;

            // (int) ConfigManager.WORLDSERVER.config.get("keyclone")
            if (machineCount > 4) {
                Logger.error("Keyclone detected from: " + player.getAccount().getUname() +
                        " with machine count of: " + machineCount);
                DbManager.AccountQueries.SET_TRASH(machineID);
            }
        // Refactor to separate file to log keyclones
       // DbManager.AccountQueries.EMPTY_TRASH(machineID);

    }
}
