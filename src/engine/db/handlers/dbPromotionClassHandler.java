// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.PromotionClass;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class dbPromotionClassHandler extends dbHandlerBase {

    public dbPromotionClassHandler() {
        this.localClass = PromotionClass.class;
        this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
    }

    public ArrayList<Integer> GET_ALLOWED_RUNES(final PromotionClass pc) {
        ArrayList<Integer> runes = new ArrayList<>();
        prepareCallable("SELECT * FROM `static_rune_promotionrunereq` WHERE `promoID`=?");
        setInt(1, pc.getObjectUUID());
        try {
            ResultSet rs = executeQuery();
            while (rs.next()) {
                runes.add(rs.getInt("runereqID"));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve Allowed Runes for PromotionClass " + pc.getObjectUUID() + ". Error number: " + e.getErrorCode(), e);
            return null;
        } finally {
            closeCallable();
        }
        return runes;
    }

    public PromotionClass GET_PROMOTION_CLASS(final int objectUUID) {
        prepareCallable("SELECT * FROM `static_rune_promotion` WHERE `ID` = ?");
        setInt(1, objectUUID);
        return (PromotionClass) getObjectSingle(objectUUID);
    }
    
    public ArrayList<PromotionClass> GET_ALL_PROMOTIONS() {
		prepareCallable("SELECT * FROM `static_rune_promotion`");
		return getObjectList();
	}
}
