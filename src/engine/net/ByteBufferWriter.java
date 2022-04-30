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
import org.joda.time.DateTime;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.UUID;

//TODO possibly pool this class? Maybe make them static?
//TODO need to extract the SB specific stuff from here into subclass.

public class ByteBufferWriter {

	private final ByteBuffer bb;

	public ByteBufferWriter(ByteBuffer bb) {
		super();
		this.bb = bb;
	}

	/*
	 * Putters
	 */

	public synchronized void put(byte x) {

		this.bb.put(x);
	}

	public synchronized void putChar(char x) {

		this.bb.putChar(x);
	}

	public synchronized void putShort(short x) {

		this.bb.putShort(x);
	}

	public synchronized void putInt(int x) {
		this.bb.putInt(x);
	}

	public synchronized void putLong(long x) {
		this.bb.putLong(x);
	}

	public synchronized void putDateTime(DateTime dateTime){
		this.put((byte)dateTime.getDayOfMonth());
		this.put((byte) ((byte)dateTime.getMonthOfYear() -1));
		this.putInt(dateTime.getYear()-1900);
		this.put((byte)dateTime.getHourOfDay());
		this.put((byte)dateTime.getMinuteOfHour());
		this.put((byte)dateTime.getSecondOfDay());
	}

	public synchronized void putLocalDateTime(LocalDateTime dateTime){
		this.put((byte)dateTime.getDayOfMonth());
		this.put((byte) ((byte)dateTime.getMonth().getValue() -1));
		this.putInt(dateTime.getYear() -1900);
		this.put((byte)dateTime.getHour());
		this.put((byte)dateTime.getMinute());
		this.put((byte)dateTime.getSecond());
	}

	public synchronized void putFloat(float x) {
			this.bb.putFloat(x);
	}

	public synchronized void putDouble(double x) {
			this.bb.putDouble(x);
	}

	public synchronized void putString(String x) {
			ByteBufferUtils.putString(this.bb, x, false, false);
	}

	public synchronized void putSmallString(String x) {
			ByteBufferUtils.putString(this.bb, x, false, true);
	}

	public synchronized void putHexString(String x) {
			ByteBufferUtils.putHexString(this.bb, x, false);
	}

	public synchronized void putHexStringWithoutSize(String data) {
		int length = data.length() / 2;

		for (int i = 0; i < length; i++) {
			bb.put((byte) Integer.parseInt(data.substring(2 * i, 2 * i + 2), 16));
		}
	}

	public synchronized void putUnicodeString(String x) {
			ByteBufferUtils.putUnicodeString(this.bb, x, false);
	}

	public synchronized void putWriter(ByteBufferWriter writer) {
		synchronized (writer) {
            ByteBuffer bbin = writer.bb;
			bbin.flip();
			this.bb.put(bbin);
		}
	}

	public synchronized void putBB(ByteBuffer bb) {
		synchronized (bb) {
			bb.flip();
			this.bb.put(bb);
		}
	}

	public synchronized void putIntAt(int value, int position) {
		// mark end position
		int endPosition = this.position();

		// go back to the desired position
		this.bb.position(position);

		// Write in the value:
		this.putInt(value);

		// now go back to end:
		this.bb.position(endPosition);
	}

	public synchronized void putVector3f(Vector3f x) {
			this.bb.putFloat(x.x);
			this.bb.putFloat(x.y);
			this.bb.putFloat(x.z);
	}

	public synchronized void putVector3f(Vector3fImmutable x) {
			this.bb.putFloat(x.x);
			this.bb.putFloat(x.y);
			this.bb.putFloat(x.z);
	}

	public synchronized void putUUID(final UUID uuid) {
		final long msb = uuid.getMostSignificantBits();
		final long lsb = uuid.getLeastSignificantBits();
		final byte[] buffer = new byte[16];

		for (int i = 0; i < 8; i++) {
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
		}

		for (int i = 8; i < 16; i++) {
			buffer[i] = (byte) (lsb >>> 8 * (7 - i));
		}

		this.bb.put(buffer);
	}

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

	/**
	 * @return
	 * @see java.nio.Buffer#remaining()
	 */
	public final int remaining() {
		return bb.remaining();
	}

	/*
	 * Getters
	 */

	public synchronized ByteBuffer getBb() {
		return this.bb;
	}
}
