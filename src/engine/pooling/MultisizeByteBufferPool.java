// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.pooling;

import org.pmw.tinylog.Logger;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class MultisizeByteBufferPool {

	/**
	 * Maps a power of two (0-30) to a BB Pool
	 */
	private final HashMap<Integer, ByteBufferPool> powerToPoolMap = new HashMap<>();

	public MultisizeByteBufferPool() {
		super();
	}

	/**
	 * Gets a ByteBuffer that is of the size 2^<i>powerOfTwo</i> from the
	 * appropriate pool.
	 *
	 * @param powerOfTwo
	 *            int range of 0-30
	 * @return
	 */
	public ByteBuffer getBuffer(int powerOfTwo) {
		// Validate input
		if (powerOfTwo > 30 || powerOfTwo < 0) {
			Logger.error("powerOfTwo out of range (0-30) in getBuffer().  Got: " + powerOfTwo);
			return null;
		}

		// Check to see if there is a pool for this size
		ByteBufferPool bbp = this.getByteBufferPool(powerOfTwo);
		return bbp.get();
	}

	/**
	 * Internal getter to provide synchronization. Adds ByteBufferPool if not mapped.
	 *
	 * @param powerOfTwo
	 * @return
	 */
	private ByteBufferPool getByteBufferPool(Integer powerOfTwo) {
		synchronized (this.powerToPoolMap) {
			// Check to see if there is a pool for this size
			ByteBufferPool bbp = powerToPoolMap.get(powerOfTwo);

			if (bbp == null) {
				bbp = MultisizeByteBufferPool.makeByteBufferPool(powersOfTwo[powerOfTwo]);
				this.putByteBufferPool(powerOfTwo, bbp);
			}
			return bbp;
		}
	}

	/**
	 * Internal setter to provide synchronization
	 *
	 * @param powerOfTwo
	 * @param bbp
	 * @return
	 */
	private ByteBufferPool putByteBufferPool(Integer powerOfTwo,
                                             ByteBufferPool bbp) {
		synchronized (this.powerToPoolMap) {
			return powerToPoolMap.put(powerOfTwo, bbp);
		}
	}

	public ByteBuffer getBufferToFit(int numOfBytes) {
		int pow = MultisizeByteBufferPool.getPowerThatWillFit(numOfBytes);
		return this.getBuffer(pow);
	}

	/**
	 * Puts a ByteBuffer that is of the size 2^<i>powerOfTwo</i> back into the
	 * appropriate pool.
	 *
	 * @param bb
	 *            - Bytebuffer to put into a pool
	 *
	 */
	public void putBuffer(ByteBuffer bb) {

		if (bb == null)
			return;

		// determine size:
		int pow = MultisizeByteBufferPool.getPowerThatWillFit(bb.capacity());

		// if we get here and pow == -1 then we have a bytebuffer > 2^30 !!!!
		// so just file it under power of 30;
		if (pow == -1) {
			pow = 30;
		}

		// get pool
		ByteBufferPool bbp = this.getByteBufferPool(pow);

		// put buffer (back) into pool
		bbp.put(bb);
	}

	/**
	 * Returns the next power of two that is larger than or equal too the input
	 * <i>value</i>
	 *
	 * @param value
	 * @return the power of two that is larger than or equal too the input
	 *         <i>value</i>. A return of -1 indicates out of range.
	 */
	public static int getPowerThatWillFit(final int value) {
		return (value == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(value - 1));
	}

	private static ByteBufferPool makeByteBufferPool(int bbInitialSize) {
		return new ByteBufferPool(bbInitialSize);
	}

	/**
	 * Returns the size of the ByteBufferPool mapped to the given powerOfTwo.
	 *
	 * @param powerOfTwo
	 *            int range of 0-30
	 * @return size of pool mapped to provided <i>powerOfTwo</i>. Returns -1 on
	 *         error and lastError will be set.
	 */
	public int getSizeOfPool(int powerOfTwo) {
		if (powerOfTwo > 30 || powerOfTwo < 0) {
			Logger.error("powerOfTwo out of range (0-30) in getSizeOfPool().  Got: "
							+ powerOfTwo);
			return -1;
		}
		ByteBufferPool bbp = this.getByteBufferPool(powerOfTwo);

		return bbp.getPoolSize();
	}


	/**
	 * List of the powers of two from 2^0 to 2^30. The index of the array
	 * corresponds to the power of two. Example: If you'd like to quickly lookup
	 * 2^19, then reference powersOfTwo[19]
	 */
	public static final int[] powersOfTwo = {
			1, // 2^0
			2, // 2^1
			4, // 2^2
			8, // 2^3
			16, // 2^4
			32, // 2^5
			64, // 2^6
			128, // 2^7
			256, // 2^8
			512, // 2^9
			1024, // 2^10
			2048, // 2^11
			4096, // 2^12
			8192, // 2^13
			16384, // 2^14
			32768, // 2^15
			65536, // 2^16
			131072, // 2^17
			262144, // 2^18
			524288, // 2^19
			1048576, // 2^20
			2097152, // 2^21
			4194304, // 2^22
			8388608, // 2^23
			16777216, // 2^24
			33554432, // 2^25
			67108864, // 2^26
			134217728, // 2^27
			268435456, // 2^28
			536870912, // 2^29
			1073741824, // 2^30
	};

}
