package engine.util;

import engine.gameManager.ConfigManager;
import engine.gameManager.DbManager;
import engine.objects.Group;
import engine.objects.PlayerCharacter;
import org.pmw.tinylog.Logger;

public enum KeyCloneAudit {
    KEYCLONEAUDIT;

    public void audit(PlayerCharacter player, Group group) {

        int machineCount = 0;
        String machineID;

        machineID = player.getClientConnection().machineID;

        for (PlayerCharacter member : group.getMembers())
            if (machineID.equals(member.getClientConnection().machineID))
                machineCount = machineCount + 1;

            if (machineCount > Integer.parseInt(ConfigManager.MB_WORLD_KEYCLONE_MAX.getValue())) {
                Logger.error("Keyclone detected from: " + player.getAccount().getUname() +
                        " with machine count of: " + machineCount);
                DbManager.AccountQueries.SET_TRASH(machineID);
            }

    }
}
