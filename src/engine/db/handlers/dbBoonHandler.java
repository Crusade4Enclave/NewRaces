// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.Boon;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class dbBoonHandler extends dbHandlerBase {

	public dbBoonHandler() {
	}
	

	public ArrayList<Boon>  GET_BOON_AMOUNTS_FOR_ITEMBASEUUID(int itemBaseUUID){
        
        ArrayList<Boon>boons = new ArrayList<>();
        Boon thisBoon;
        prepareCallable("SELECT * FROM `static_item_boons`  WHERE `itemBaseID` = ?");
        setInt(1, itemBaseUUID);

	try {
		ResultSet rs = executeQuery();
                    
		while (rs.next()) {
                        
                     
                      thisBoon = new Boon(rs);
                      boons.add(thisBoon);
		}
                    
  
                            
	} catch (SQLException e) {
		Logger.error("GetBoonAmountsForItembaseUUID: " + e.getErrorCode() + ' ' + e.getMessage(), e);
	} finally {
		closeCallable();
	}
	return boons;
}
}
