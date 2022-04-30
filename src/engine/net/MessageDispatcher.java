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
import org.pmw.tinylog.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;

/**
 * Thread blocks until MagicBane dispatch messages are
 * enqueued then processes them in FIFO order. The collection
 * is thread safe.
 * 
 * Any large messages not time sensitive such as load object
 * sent to more than a single individual should be spawned
 * individually on a DispatchMessageThread.
 */

public class MessageDispatcher implements Runnable {

    // Instance variables
    
    private Dispatch messageDispatch;
    private final Pattern filterPattern; // Unused, but just in case
    
    // Class variables

    @SuppressWarnings("unchecked") // Cannot have arrays of generics in java.
    private static final ConcurrentLinkedQueue<Dispatch>[] _messageQueue = new ConcurrentLinkedQueue[DispatchChannel.values().length];

    private static final LinkedBlockingQueue<Boolean> _blockingQueue = new LinkedBlockingQueue<>();
    
    // Performance metrics
    
    public static  volatile long[] messageCount = new long[DispatchChannel.values().length];
    public static  LongAdder[] dispatchCount = new LongAdder[DispatchChannel.values().length];
    public static  volatile long[] maxRecipients = new long[DispatchChannel.values().length];
    public static  LongAdder itemPoolSize = new LongAdder();
    
    // Thread constructor
    
    public MessageDispatcher() {

        // Create new FIFO queues for this network thread
        
        for (DispatchChannel dispatchChannel : DispatchChannel.values()) {
            _messageQueue[dispatchChannel.getChannelID()] = new ConcurrentLinkedQueue<>();
            dispatchCount[dispatchChannel.getChannelID()] = new LongAdder();
        }
            
        filterPattern = Pattern.compile("[^\\p{ASCII}]");
        Logger.info( " Dispatcher thread has started!");
    
    }

    public static void send(Dispatch messageDispatch, DispatchChannel dispatchChannel) {
    
        // Don't queue up empty dispatches!
        
        if (messageDispatch.player == null)
            return;
        
        _messageQueue[dispatchChannel.getChannelID()].add(messageDispatch);
        _blockingQueue.add(true);
        
        // Update performance metrics
        
        messageCount[dispatchChannel.getChannelID()]++;
        
    }
        
    @Override
    public void run() {
        
        boolean shouldBlock;
        
        while (true) {
            try {
                
                shouldBlock = true;
                
                for (DispatchChannel dispatchChannel : DispatchChannel.values()) {
                    
                    this.messageDispatch = _messageQueue[dispatchChannel.getChannelID()].poll();
                    
                    if (this.messageDispatch != null) {
                    DispatchMessage.serializeDispatch(this.messageDispatch);
                    shouldBlock = false;
                }
                
                }
                
                if (shouldBlock == true)
                    shouldBlock = _blockingQueue.take();

            } catch (Exception e) {
                Logger.error(e);
            }

        }
    }

    public static String getNetstatString() {

        String outString = null;
        String newLine = System.getProperty("line.separator");
        outString = "[LUA_NETSTA()]" + newLine;
        outString += "poolSize: " + itemPoolSize.longValue() + '\n';
        
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
