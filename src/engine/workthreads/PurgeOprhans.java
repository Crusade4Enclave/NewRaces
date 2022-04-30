// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.workthreads;

import engine.db.archive.DataWarehouse;
import org.pmw.tinylog.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.LongAdder;

/*
 * This thread runs at bootstrap to ensure cleanup of
 * orphaned items (deleted items).  God does this mess
 * ever need to be refactored and re-use of item uuid's
 * implemented.
 */
public class PurgeOprhans implements Runnable {

    public static LongAdder recordsDeleted = new LongAdder();

    public PurgeOprhans() {

        recordsDeleted.reset();

    }

    public static void startPurgeThread() {

        Thread purgeOrphans;
        purgeOrphans = new Thread(new PurgeOprhans());

        purgeOrphans.setName("purgeOrphans");
        purgeOrphans.start();
    }

    public void run() {

        // Member variable declaration

        try (
                Connection connection = DataWarehouse.connectionPool.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * from `object` where `type` = 'item' AND `parent` IS NULL", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                rs.deleteRow();
                recordsDeleted.increment();
            }

        } catch (Exception e) {
            Logger.error( e.toString());
        }

        Logger.info("Thread is exiting with " + recordsDeleted.toString() + " items deleted");
    }

}
