// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.Realm;
import org.pmw.tinylog.Logger;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class dbRealmHandler extends dbHandlerBase {

    public dbRealmHandler() {

    }

        public ConcurrentHashMap<Integer, Realm> LOAD_ALL_REALMS() {
        
            ConcurrentHashMap<Integer, Realm> realmList;
            Realm thisRealm;
                    
            realmList = new ConcurrentHashMap<>();
            int recordsRead = 0;
            
		prepareCallable("SELECT * FROM obj_realm");

		try {
			ResultSet rs = executeQuery();
                        
			while (rs.next()) {
                            
                          recordsRead++;
                          thisRealm = new Realm(rs);
                          realmList.put(thisRealm.getRealmID(), thisRealm);
			}
                        
                        Logger.info( "read: " + recordsRead + " cached: " + realmList.size());
                                
		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(dbRealmHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
			closeCallable();
		}
		return realmList;
	}
        
    public void REALM_UPDATE(Realm realm) {

            prepareCallable("CALL realm_UPDATE(?,?,?,?)");
            
            setInt(1, realm.getRealmID());
            setInt(2, (realm.getRulingCity() == null) ? 0 : realm.getRulingCity().getObjectUUID());
            setInt(3, realm.getCharterType());
        if (realm.ruledSince != null)
            setLocalDateTime(4, realm.ruledSince);
            else
                setNULL(4, java.sql.Types.DATE);
            
            executeUpdate();
    }
}
