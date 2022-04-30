// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net;

import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.util.ByteBufferUtils;
import engine.util.ByteUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class ByteBufferReader {

	private final ByteBuffer bb;
	private final boolean endianFlip;

	/**
	 * Helper class for getting structured information out of a ByteBuffer
	 * easily. ByteBuffer passed in is copied to an internal ByteBuffer.
	 * <B>bbin</B> must have the position attribute at the <i>end</i> of the
	 * data to be copied over, since <b>bbin.flip()</b> is called in this
	 * constructor.
	 *
	 * @param bbin
	 * @param endianFlip
	 */
	public ByteBufferReader(ByteBuffer bbin, boolean endianFlip) {
		super();

		// Copy supplied BB.
		this.bb = ByteBuffer.allocate(bbin.position()); //FIXME Do we want to get this from pool?
		bbin.flip();
		this.bb.put(bbin);

		// prepare bb for reading
		this.bb.flip();

		this.endianFlip = endianFlip;
	}

	/*
	 * Getters
	 */

	/**
	 * @return
	 * @see java.nio.ByteBuffer#get()
	 */
	public byte get() {
		return bb.get();
	}

	public void get(byte[] ba) {
		this.bb.get(ba);
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getChar()
	 */
	public char getChar() {
		char x = bb.getChar();
		if (this.endianFlip) {
			x = Character.reverseBytes(x);
		}
		return x;
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getDouble()
	 */
	public double getDouble() {
		double x = 0;
		if (this.endianFlip) {
			x = bb.order(ByteOrder.LITTLE_ENDIAN).getDouble();
			bb.order(ByteOrder.BIG_ENDIAN);
		} else {
			x = bb.getDouble();
		}
		return x;
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getFloat()
	 */
	public float getFloat() {
		float x = 0;
		if (this.endianFlip) {
			x = bb.order(ByteOrder.LITTLE_ENDIAN).getFloat();
			bb.order(ByteOrder.BIG_ENDIAN);
		} else {
			x = bb.getFloat();
		}
		return x;
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getInt()
	 */
	public int getInt() {
		int x = bb.getInt();
		if (this.endianFlip) {
			x = Integer.reverseBytes(x);
		}
		return x;
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getLong()
	 */
	public long getLong() {
		long x = bb.getLong();
		if (this.endianFlip) {
			x = Long.reverseBytes(x);
		}
		return x;
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getShort()
	 */
	public short getShort() {
		short x = bb.getShort();
		if (this.endianFlip) {
			x = Short.reverseBytes(x);
		}
		return x;
	}

	public final String getString() {
		if (this.endianFlip) {
			return ByteBufferUtils.getString(this.bb, true, false);
		} else {
			return ByteBufferUtils.getString(this.bb, false, false);
		}
	}

	public final String getSmallString() {
		if (this.endianFlip) {
			return ByteBufferUtils.getString(this.bb, true, true);
		} else {
			return ByteBufferUtils.getString(this.bb, false, true);
		}
	}

	public final String getHexString() {
		if (this.endianFlip) {
			return ByteBufferUtils.getHexString(this.bb, true);
		} else {
			return ByteBufferUtils.getHexString(this.bb);
		}
	}

	public final String getUnicodeString() {
		if (this.endianFlip) {
			return ByteBufferUtils.getUnicodeString(this.bb, true);
		} else {
			return ByteBufferUtils.getUnicodeString(this.bb);
		}
	}

	public String get1ByteAsHexString() {
		return getBytesAsHexStringCommon(new byte[1]);
	}

	public String get2BytesAsHexString() {
		return getBytesAsHexStringCommon(new byte[2]);
	}

	public String get4BytesAsHexString() {
		return getBytesAsHexStringCommon(new byte[4]);
	}

	public String get8BytesAsHexString() {
		return getBytesAsHexStringCommon(new byte[8]);
	}

	private String getBytesAsHexStringCommon(byte[] ba) {
		this.bb.get(ba);
		if (this.endianFlip) {
			return ByteUtils.byteArrayToStringHex(ByteUtils
					.switchByteArrayEndianness(ba));
		} else {
			return ByteUtils.byteArrayToStringHex(ba);
		}
	}

	public Vector3f getVector3f() {
		Vector3f out = new Vector3f();
		if (this.endianFlip) {
			out.x = Float
					.intBitsToFloat(Integer.reverseBytes(this.bb.getInt()));
			out.y = Float
					.intBitsToFloat(Integer.reverseBytes(this.bb.getInt()));
			out.z = Float
					.intBitsToFloat(Integer.reverseBytes(this.bb.getInt()));
		} else {
			out.x = this.bb.getFloat();
			out.y = this.bb.getFloat();
			out.z = this.bb.getFloat();
		}
		return out;
	}

	public Vector3fImmutable getVector3fImmutable() {
		Vector3fImmutable out;
		if (this.endianFlip) {
			out = new Vector3fImmutable(Float.intBitsToFloat(Integer
					.reverseBytes(this.bb.getInt())), Float
					.intBitsToFloat(Integer.reverseBytes(this.bb.getInt())),
					Float
							.intBitsToFloat(Integer.reverseBytes(this.bb
									.getInt())));
		} else {
			out = new Vector3fImmutable(this.bb.getFloat(), this.bb.getFloat(),
					this.bb.getFloat());
		}
		return out;
	}

	public final UUID getUUID() {
		final byte[] buffer = new byte[16];
		this.bb.get(buffer);
		
	    long msb = 0;
	    long lsb = 0;
            
	    for (int i = 0; i < 8; i++) {
                msb = (msb << 8) | (buffer[i] & 0xff);
                }
            
	    for (int i = 8; i < 16; i++) {
                lsb = (lsb << 8) | (buffer[i] & 0xff);
                }

        return new UUID(msb, lsb);
	}

	
	/*
	 * Monitors
	 */

	public byte monitorByte(byte expectedValue, String label) {
		return this.monitorByte(expectedValue, label, false);
	}

	public byte monitorByte(byte expectedValue, String label, boolean peek) {
        //		if (x != expectedValue) {
//			Logger.info("MonitorTrip: " + label + ". Expected: "
//					+ expectedValue + " Got: " + x);
//		}
		return this.get();
	}

	public short monitorShort(short expectedValue, String label) {
		return this.monitorShort(expectedValue, label, false);
	}

	public short monitorShort(short expectedValue, String label, boolean peek) {
        //		if (x != expectedValue) {
//			Logger.info("MonitorTrip: " + label + ". Expected: "
//					+ expectedValue + " Got: " + x);
//		}
		return this.getShort();
	}

	public int monitorInt(int expectedValue, String label) {
		return this.monitorInt(expectedValue, label, false);
	}

	public int monitorInt(int expectedValue, String label, boolean peek) {
        //		if (x != expectedValue) {
//			Logger.info("MonitorTrip: " + label + ". Expected: "
//					+ expectedValue + " Got: " + x);
//		}
		return this.getInt();
	}

	public long monitorLong(long expectedValue, String label) {
		return this.monitorLong(expectedValue, label, false);
	}

	public long monitorLong(long expectedValue, String label, boolean peek) {
        //		if (x != expectedValue) {
//			Logger.info("MonitorTrip: " + label + ". Expected: "
//					+ expectedValue + " Got: " + x);
//		}
		return this.getLong();
	}

	/*
	 * ByteBuffer delegates
	 */

	/**
	 * @return
	 * @see java.nio.Buffer#hasRemaining()
	 */
	public final boolean hasRemaining() {
		return bb.hasRemaining();
	}

	/**
	 * @return
	 * @see java.nio.Buffer#limit()
	 */
	public final int limit() {
		return bb.limit();
	}

	/**
	 * @return
	 * @see java.nio.Buffer#position()
	 */
	public final int position() {
		return bb.position();
	}
	public final Buffer position(int newPosition){
		return bb.position(newPosition);
	}
	/**
	 * @return
	 * @see java.nio.Buffer#remaining()
	 */
	public final int remaining() {
		return bb.remaining();
	}

	/*
	 * Status getters
	 */

	protected ByteBuffer getBb() {
		return bb;
	}

	protected boolean isEndianFlip() {
		return endianFlip;
	}

	public String getByteArray() {
		String ret = "";
		if (this.bb == null)
			return ret;
		byte[] bbyte = bb.array();
		if (bbyte == null)
			return ret;
		for (int i=0;i<bbyte.length;i++) {
                    ret += Integer.toString((bbyte[i] & 0xff) + 0x100, 16).substring(1).toUpperCase();
                }
		return ret;
	}

}
