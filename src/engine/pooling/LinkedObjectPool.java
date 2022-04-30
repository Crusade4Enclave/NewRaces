// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.pooling;


import org.pmw.tinylog.Logger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class LinkedObjectPool<T> {
	private final LinkedBlockingQueue<T> pool;
	private final AtomicInteger poolSize;

	/**
	 * Constructor that allows the caller to specify initial array size and
	 * percent of prefill.
	 * 
	 * @param size
	 *            - initial size for the containing array (pool)
	 */
	public LinkedObjectPool(int size) {
		this.pool = new LinkedBlockingQueue<>();
		this.poolSize = new AtomicInteger();
	}

	/**
	 * Default Constructor that uses default initial Array size and percent
	 * prefill values
	 */
	public LinkedObjectPool() {
		this(0);
	}

	/**
	 * Forces pool to add <i>numberOfObjects</i> to the pool. This may cause
	 * internal ArrayList to resize.
	 * 
	 * @param numberOfObjects
	 */
	public void fill(int numberOfObjects) {
		for (int i = 0; i < numberOfObjects; ++i)
			this.makeAndAdd();
	}

	/**
	 * Forces subclasses to implement factory routine for object creation.
	 */
	protected abstract T makeNewObject();

	/**
	 * Forces subclasses to implement a way to reset object to a reusable state.
	 */
	protected abstract void resetObject(T obj);

	/**
	 * Generic Get routine. If the pool is empty, then one (or more) of T type
	 * objects will be created. If more than one T is created, then they will be
	 * put into the pool. One T will always be created and returned.
	 */
	public T get() {
		T obj = pool.poll();
		
		if(obj == null) {
			//Oops pool is empty.. make a new obj
			obj = this.makeNewObject();
		} else {
			poolSize.decrementAndGet();
		}

		return obj;
	}
	
	/**
	 * Generic put routine. If the current pool size is below threshold, this
	 * object will be pooled, otherwise it will be NULL'ed and scheduled for
	 * Garbage Collection
	 * 
	 * @param obj
	 */
	public void put(T obj) {
		//Logger.debug("Objectpool.put().  Pool size: " + pool.size());
		this.resetObject(obj);
		this.poolSize.incrementAndGet();
		this.pool.add(obj);
	}

	/**
	 * Helper method. Attempts to create and add a new <i>T</i> object. If this
	 * fails, an error is logged.
	 */
	protected final void makeAndAdd() {
		T obj = this.makeNewObject();
		if (obj == null) {
			Logger.error("Pooling failure: Object creation failed.");
		} else {
			this.put(obj);
		}
	}

	/**
	 * 
	 * @return the current size of the pool, not the maximum capacity of the
	 *         Array holding the pool.
	 */
	public final int getPoolSize() {
		return this.poolSize.get();
	}

	/**
	 * Culls the pool and removes half of the stored objects. Removed objects
	 * are NULL'ed and scheduled form Garbage Collection.
	 * 
	 * @return the number of Objects removed from the pool.
	 */
	public int cullHalf() {
		int full, half;
		full = this.getPoolSize();
		if (full < 1) {
			return full;
		}

		half = (full / 2);

		for (int i = 0; i < (full / 2); ++i) {
			T obj = this.pool.poll();
			obj = null; // Null out for GC
		}
		return half;
	}

	protected void handlePoolExhaustion() {
		//Not needed in this implementation
	}
}
