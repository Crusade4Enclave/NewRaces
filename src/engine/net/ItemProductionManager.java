// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net;

import engine.Enum.DispatchChannel;
import engine.objects.ProducedItem;
import org.pmw.tinylog.Logger;

import java.util.HashSet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.LongAdder;

/**
 * Thread blocks until MagicBane dispatch messages are
 * enqueued then processes them in FIFO order. The collection
 * is thread safe.
 * 
 * Any large messages not time sensitive such as load object
 * sent to more than a single individual should be spawned
 * individually on a DispatchMessageThread.
 */



public enum ItemProductionManager implements Runnable {

	ITEMPRODUCTIONMANAGER;
	
	
    // Instance variables
    
    private ItemQueue itemQueue;

    private long nextFailedItemAudit;
    
    // Class variables

    @SuppressWarnings("unchecked") // Cannot have arrays of generics in java.
    private static final DelayQueue<ItemQueue> producedQueue = new DelayQueue<>();

    
    // Performance metrics
    
    public static  volatile long[] messageCount = new long[DispatchChannel.values().length];
    public static  LongAdder[] dispatchCount = new LongAdder[DispatchChannel.values().length];
    public static  volatile long[] maxRecipients = new long[DispatchChannel.values().length];
    public static  LongAdder dispatchPoolSize = new LongAdder();
    
    public static HashSet<ProducedItem> FailedItems = new HashSet<>();
    
    public Thread itemProductionThread = null;
    
    // Thread constructor
    
    
    public void startMessagePump() {

		itemProductionThread = new Thread(this);
		itemProductionThread.setName("ItemProductionManager");
		
	}
    
    public void initialize(){
    	itemProductionThread.start();
    }
    

    public static void send(ItemQueue item) {
    
        // Don't queue up empty dispatches!
        
        if (item == null)
        	return;
        
        producedQueue.add(item);
        
    }
        
    @Override
    public void run() {
        
        
        while (true) {
            try {												
                
                    this.itemQueue = producedQueue.take();
                    
                    if (this.itemQueue == null){	
                    	return;
                    }
                    
                    if (this.itemQueue != null) {
                    if (this.itemQueue.item == null){
                    	this.itemQueue.release();
                   	return;
                   	}
                    
                    
                   boolean created = this.itemQueue.item.finishProduction();
                   
                   if (!created)
                	   FailedItems.add(this.itemQueue.item);
                   
                   this.itemQueue.release();
                   
                
                }
                
              
            } catch (Exception e) {
                Logger.error(e);
            }

        }
    }

    public static String getNetstatString() {

        String outString = null;
        String newLine = System.getProperty("line.separator");
        outString = "[LUA_NETSTA()]" + newLine;
        outString += "poolSize: " + dispatchPoolSize.longValue() + '\n';
        
        for (DispatchChannel dispatchChannel : DispatchChannel.values()) {
            
            outString += "Channel: " + dispatchChannel.name() + '\n';
            outString += "Dispatches: " + dispatchCount[dispatchChannel.getChannelID()].longValue()+ '\n';
            outString += "Messages: " + messageCount[dispatchChannel.getChannelID()] + '\n';
            outString += "maxRecipients: " + maxRecipients[dispatchChannel.getChannelID()] + '\n';
        }
        return outString;
    }
            
        // For Debugging:
        //Logger.error("MessageDispatcher", messageDispatch.msg.getOpcodeAsString() + " sent to " + messageDispatch.playerList.size() + " players");
    }
