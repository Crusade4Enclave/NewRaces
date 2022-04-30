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

import engine.objects.ProducedItem;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static engine.net.MessageDispatcher.itemPoolSize;

/**
 * Data class holds a message and a distribution list
 */

public class ItemQueue implements Delayed {

    private static final ConcurrentLinkedQueue<ItemQueue> itemPool = new ConcurrentLinkedQueue<>();
            
    public ProducedItem item;
    public long delayTime;
    
    
        public ItemQueue(ProducedItem item, long delayTime) {
        	this.item = item;
        	this.delayTime = System.currentTimeMillis() + delayTime;
        
}
        
        public void reset() {
            this.item = null;
            this.delayTime = 0;
        }
        
        public static ItemQueue borrow(ProducedItem item, long delayTime) {

            ItemQueue itemQueue;
            
            itemQueue = itemPool.poll();

            if (itemQueue == null) {
            itemQueue = new ItemQueue(item, delayTime);
            }      else {
            	itemQueue.item = item;
            	itemQueue.delayTime = System.currentTimeMillis() + delayTime;
                itemPoolSize.decrement();
            }
            
            return itemQueue;
        }
        
        public void release() {
            this.reset();
            itemPool.add(this);
            itemPoolSize.increment();
        }

		@Override
		public int compareTo(Delayed another) {
			   ItemQueue anotherTask = (ItemQueue) another;
			   
		        if (this.delayTime < anotherTask.delayTime) {
		            return -1;
		        }
		 
		        if (this.delayTime > anotherTask.delayTime) {
		            return 1;
		        }
		 
		        return 0;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			 long difference = delayTime - System.currentTimeMillis();
		        return unit.convert(difference, TimeUnit.MILLISECONDS);
		}
}
