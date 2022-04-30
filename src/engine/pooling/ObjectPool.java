// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.pooling;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;

public abstract class ObjectPool<T> {
	protected final static int DEFAULT_SIZE = 1000;

	// Simple + quick list
	private final ArrayList<T> pool;

	/**
	 * Once the ArrayList fills to <b>threshold</b>, subsequent .put() calls
	 * result in the object being thrown away. Default is 75% of supplied
	 * <b>size</b>.
	 */
	private int threshold;

	/**
	 * Constructor that allows the caller to specify initial array size and
	 * percent of prefill.
	 * 
	 * @param size
	 *            - initial size for the containing array (pool)
	 */
	public ObjectPool(int size) {
		if (size == 0) {
			size = DEFAULT_SIZE;
		}

		threshold = (int) (0.75 * size);

		this.pool = new ArrayList<>(size);

	}

	/**
	 * Default Constructor that uses default initial Array size and percent
	 * prefill values
	 */
	public ObjectPool() {
		this(DEFAULT_SIZE);
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
		synchronized (pool) {
			//Logger.debug("Objectpool.get().  Pool size before get: " + pool.size());
			if (pool.size() > 0) {
				return pool.remove(0);
			}
		}
		
		this.handlePoolExhaustion();

		T obj = this.makeNewObject();

		if (obj == null) {
			Logger.error("Pooling failure: Object creation failed.");
		}
		return obj;
	}

	protected void handlePoolExhaustion(){
		Logger.debug("Pool exhausted, making more objects.");
		
		// If none exist, make (and pool) a few
		// Dont sync the loop, let the makeNewObject()
		// call return before locking the pool object
		for (int i = 0; i < 5; ++i)
			this.makeAndAdd();

	}
	
	/**
	 * Generic put routine. If the current pool size is below threshold, this
	 * object will be pooled, otherwise it will be NULL'ed and scheduled for
	 * Garbage Collection
	 * 
	 * @param obj
	 */
	public void put(T obj) {
		synchronized (pool) {
			if (pool.size() >= this.threshold) {
				//Logger.debug("Objectpool.put() rejected.  Pool size: " + pool.size());
				return;
			}
			//Logger.debug("Objectpool.put().  Pool size: " + pool.size());
			this.resetObject(obj);
			this.pool.add(obj);
		}
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
		synchronized (this.pool) {
			return this.pool.size();
		}
	}

	/**
	 * Culls the pool and removes half of the stored objects. Removed objects
	 * are NULL'ed and scheduled form Garbage Collection.
	 * 
	 * @return the number of Objects removed from the pool.
	 */
	public int cullHalf() {
		int full, half;
		synchronized (this.pool) {
			full = this.pool.size();
			if (full < 1) {
				return full;
			}

			half = (full / 2);

			for (int i = 0; i < (full / 2); ++i) {
				T obj = this.get();
				obj = null; // Null out for GC
			}
		}
		return half;
	}

	/**
	 * @return the threshold
	 */
	public final int getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            the threshold to set
	 */
	public void setThreshold(int threshold) {
		if (threshold < 0)
			return;

		this.threshold = threshold;
	}

}
