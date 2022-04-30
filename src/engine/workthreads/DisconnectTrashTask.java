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
import engine.gameManager.SessionManager;
import engine.objects.PlayerCharacter;
import engine.session.Session;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.TimerTask;

public class DisconnectTrashTask extends TimerTask {

    private final ArrayList<PlayerCharacter> trashList;

    // Pass it a list of characters and it will disconnect them
    // 5 seconds in the future.

    public DisconnectTrashTask(ArrayList<PlayerCharacter> trashList)
    {
        this.trashList = new ArrayList<>(trashList);
    }

    public void run() {

       Logger.info("Disconnecting actives from pool of: " + trashList.size());

        Session trashSession;
        int accountUID;

                for (PlayerCharacter trashPlayer:trashList) {
                    trashSession = SessionManager.getSession(trashPlayer);
                    accountUID = trashPlayer.getAccount().getObjectUUID();

                    if (trashSession != null)
                        trashSession.getConn().disconnect();

                    // Remove account from cache

                    DbManager.removeFromCache(Enum.GameObjectType.Account, accountUID);
                }
            };
}
