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

public class ByteBufferPool extends LinkedObjectPool<ByteBuffer> {

	private final int defaultBufferSize;

	public ByteBufferPool(int defaultBufferSize) {
		super(ObjectPool.DEFAULT_SIZE);
		this.defaultBufferSize = defaultBufferSize;
	}

	public ByteBufferPool(int size, int defaultBufferSize) {
		super(size);
		this.defaultBufferSize = defaultBufferSize;
	}

	@Override
	protected ByteBuffer makeNewObject() {
		return ByteBuffer.allocate(defaultBufferSize);
	}

	@Override
	protected void resetObject(ByteBuffer obj) {
		obj.clear();
	}

	@Override
	public ByteBuffer get() {
        // Logger.debug("ByteBufferPool.get() BB.capacity(): " + bb.capacity()
		// + ", bb.pos(): " + bb.position() + ". Pool.size() is now: "
		// + this.getPoolSize());
		return super.get();
	}

	@Override
	public void put(ByteBuffer bb) {
		if(bb.isDirect())
			super.put(bb);
		// Logger.debug("ByteBufferPool.put() BB.capacity(): " + bb.capacity()
		// + ", bb.pos(): " + bb.position() + ". Pool.size() is now: "
		// + this.getPoolSize());
	}

	@Override
	protected void handlePoolExhaustion() {
		Logger.debug("ByteBufferPool(" + defaultBufferSize
				+ ") exhausted, making more objects.");

		// If none exist, make (and pool) a few
		// Dont sync the loop, let the makeNewObject()
		// call return before locking the pool object
		for (int i = 0; i < 5; ++i)
			this.makeAndAdd();
	}

}
