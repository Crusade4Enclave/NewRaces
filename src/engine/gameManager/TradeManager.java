// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.Enum;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.CharacterItemManager;
import engine.objects.PlayerCharacter;
import org.pmw.tinylog.Logger;

public enum TradeManager {

    TRADEMANAGER;

    public static void tradeRequest(TradeRequestMsg msg, ClientConnection origin) {

        PlayerCharacter source = origin.getPlayerCharacter();
      
        if (source == null)
        	return;
        
        source.getCharItemManager().tradeRequest(msg);

    }


    public static void acceptTradeRequest(AcceptTradeRequestMsg msg, ClientConnection origin) {

        PlayerCharacter source = origin.getPlayerCharacter();
     
        if (source == null)
        	return;
        
        try {
			source.getCharItemManager().acceptTradeRequest(msg);
		} catch (Exception e) {
			Logger.error(e);
			// TODO Auto-generated catch block
			}
        
    }

    public static void rejectTradeRequest(RejectTradeRequestMsg msg, ClientConnection origin) {
        // TODO Do nothing? If so, delete this method & case above
    }

    public static void addItemToTradeWindow(AddItemToTradeWindowMsg msg, ClientConnection origin) {
    	
    	
        PlayerCharacter source = origin.getPlayerCharacter();
        if (source == null || !source.isAlive())
            return;
        try{
            source.getCharItemManager().addItemToTradeWindow(msg);

        }catch(Exception e){
        	Logger.error(e);
        }

    }

    public static void addGoldToTradeWindow(AddGoldToTradeWindowMsg msg, ClientConnection origin) {

        PlayerCharacter source = origin.getPlayerCharacter();

        if (source == null || !source.isAlive())
            return;
        
        

        CharacterItemManager sourceItemMan = source.getCharItemManager();

        if (sourceItemMan == null)
            return;
        
        try{
            sourceItemMan.addGoldToTradeWindow(msg);
        }catch(Exception e){
        	Logger.error(e);
        }
    }

    public static void commitToTrade(CommitToTradeMsg msg, ClientConnection origin) {

        PlayerCharacter source = origin.getPlayerCharacter();

        if (source == null || !source.isAlive())
            return;

        CharacterItemManager sourceItemMan = source.getCharItemManager();

        if (sourceItemMan == null)
            return;

        try {
			sourceItemMan.commitToTrade(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		Logger.error(e);
		}
    }

    public static void uncommitToTrade(UncommitToTradeMsg msg, ClientConnection origin) {

        PlayerCharacter source = origin.getPlayerCharacter();

        if (source == null || !source.isAlive())
            return;

        CharacterItemManager sourceItemMan = source.getCharItemManager();

        if (sourceItemMan == null)
            return;
        
        try {
			sourceItemMan.uncommitToTrade(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.error(e);
		}

    }

    

    public static void closeTradeWindow(CloseTradeWindowMsg msg, ClientConnection origin) {

        PlayerCharacter source = origin.getPlayerCharacter();

        if (source == null)
            return;

        CharacterItemManager sourceItemMan = source.getCharItemManager();

        if (sourceItemMan == null)
            return;

        try {
        	sourceItemMan.closeTradeWindow(msg, true);
        } catch (Exception e) {
        	// TODO Auto-generated catch block
        	Logger.error(e);
        }
    }

    public static void invalidTradeRequest(InvalidTradeRequestMsg msg) {
        PlayerCharacter requester = PlayerCharacter.getFromCache(msg.getRequesterID());
        Dispatch dispatch;

        dispatch = Dispatch.borrow(requester, msg);
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

    }
}