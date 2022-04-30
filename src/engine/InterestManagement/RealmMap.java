// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.InterestManagement;

/* This class is the main interface for Magicbane's
*  Interest management facilities.
*/

import engine.Enum;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.msg.TerritoryChangeMessage;
import engine.objects.City;
import engine.objects.PlayerCharacter;
import engine.objects.Realm;
import engine.server.MBServerStatics;
import engine.util.MapLoader;
import org.pmw.tinylog.Logger;

import static engine.objects.Realm.getRealm;

public class RealmMap {

    // Spatial hashmap.  Used for detecting which Realm
    // a player is currently in..
    
    public static int[][] _realmImageMap;


    public static int getRealmIDAtLocation(Vector3fImmutable pos) {

        int xBuckets = (int) ((pos.getX() / MBServerStatics.MAX_WORLD_WIDTH) * MBServerStatics.SPATIAL_HASH_BUCKETSX);
        int yBuckets = (int) ((pos.getZ() / MBServerStatics.MAX_WORLD_HEIGHT) * MBServerStatics.SPATIAL_HASH_BUCKETSY);

        if (yBuckets < 0 || yBuckets >= MBServerStatics.SPATIAL_HASH_BUCKETSY
                || xBuckets < 0 || xBuckets >= MBServerStatics.SPATIAL_HASH_BUCKETSX) {
            Logger.error("WorldServerRealm.getRealmFromPosition",
                    "Invalid range; Z: " + yBuckets + ", X: " + xBuckets);
            return 255;
        }

        return RealmMap._realmImageMap[xBuckets][yBuckets];
    }

    public static Realm getRealmForCity(City city) {
        Realm outRealm = null;
        outRealm = city.getRealm();
        return outRealm;
    }

    public static Realm getRealmAtLocation(Vector3fImmutable worldVector) {

        return getRealm(RealmMap.getRealmIDAtLocation(worldVector));

    }

    public static void updateRealm(PlayerCharacter player){

        int realmID = RealmMap.getRealmIDAtLocation(player.getLoc());

        if (realmID != player.getLastRealmID()){
            player.setLastRealmID(realmID);
            Realm realm = Realm.getRealm(realmID);
            if (realm != null){
                if (realm.isRuled()){
                    City city = realm.getRulingCity();
                    if (city != null){
                        TerritoryChangeMessage tcm = new TerritoryChangeMessage((PlayerCharacter)realm.getRulingCity().getOwner(),realm);
                        Dispatch dispatch = Dispatch.borrow(player, tcm);
                        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
                    }else{
                        TerritoryChangeMessage tcm = new TerritoryChangeMessage(null,realm);
                        Dispatch dispatch = Dispatch.borrow(player, tcm);
                        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
                    }

                }else{
                    TerritoryChangeMessage tcm = new TerritoryChangeMessage(null,realm);
                    Dispatch dispatch = Dispatch.borrow(player, tcm);
                    DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
                }
            }

        }


    }

    public static void loadRealmImageMap() {

        RealmMap._realmImageMap = MapLoader.loadMap();

    }

}
