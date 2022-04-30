// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.util;

import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ByteBufferUtils {
	public static String getString(ByteBuffer bb)
			throws BufferUnderflowException {
		return getString(bb, false, false);
	}

	public static String getString(ByteBuffer bb, boolean switchEndian, boolean small)
			throws BufferUnderflowException {
		String out = "";
		synchronized (bb) {

			//This version works with non-latin characters
			int stringLen;
			if (small)
				stringLen = (int)bb.get();
			else
				stringLen = bb.getInt();
			if (switchEndian)
				stringLen = ((Integer.reverseBytes(stringLen)) * 2);
			else
				stringLen *= 2;
			byte[] b = new byte[stringLen];
			for (int i=0;i<stringLen;i+=2) {
				if (switchEndian) {
					b[i+1] = bb.get();
					b[i] = bb.get();
				} else {
					b[i] = bb.get();
					b[i+1] = bb.get();
				}
			}
			try {
				out = new String(b, "UTF-16BE");
			} catch (UnsupportedEncodingException e) {}
		}
		return out;
	}

	public static void putString(ByteBuffer bb, String data, boolean small)
			throws BufferOverflowException {
		putString(bb, data, false, small);
	}

	public static void putString(ByteBuffer bb, String data,
			boolean switchEndian, boolean small) throws BufferOverflowException {
		if (data == null) {
			data = "";
		}
		char[] chars = data.toCharArray();

		int length = chars.length;
		if (small && length > 255)
			length = 255; //limit for smallString

		synchronized (bb) {
			// Write length
			if (small)
				bb.put((byte)length);
			else {
				if (switchEndian) {
					bb.putInt(Integer.reverseBytes(length));
				} else {
					bb.putInt(length);
				}
			}
			// Write chars
			for (int i=0;i<length;i++) {
				char c = chars[i];
				if (switchEndian) {
					bb.putChar(Character.reverseBytes(c));
				} else {
					bb.putChar(c);
				}
			}
		}
	}

	public static String getHexString(ByteBuffer bb)
			throws BufferUnderflowException {
		return getHexString(bb, false);
	}

	public static String getHexString(ByteBuffer bb, boolean switchEndian)
			throws BufferUnderflowException {
		String out = "";
		synchronized (bb) {
			// Read len
			int stringLen = bb.getInt();

			if (switchEndian) {
				stringLen = Integer.reverseBytes(stringLen);
			}

			// Read len worth of chars
			for (int i = 0; i < stringLen; ++i) {
				out += Integer.toString((bb.get() & 0xff) + 0x100, 16)
						.substring(1);
			}
		}
		return out;
	}

	public static void putHexString(ByteBuffer bb, String data)
			throws BufferOverflowException {
		putHexString(bb, data, false);
	}

	public static void putHexString(ByteBuffer bb, String data,
			boolean switchEndian) throws BufferOverflowException {

		if (data == null) {
			data = "";
		}

		byte[] bts = new byte[data.length() / 2];

		for (int i = 0; i < bts.length; i++) {
			bts[i] = (byte) Integer.parseInt(data.substring(2 * i, 2 * i + 1),
					16);
		}

		synchronized (bb) {
			if (switchEndian) {
				bb.putInt(Integer.reverseBytes(data.length() / 2));
			} else {
				bb.putInt(data.length() / 2);
			}
			bb.put(bts);
		}
	}

	public static String getUnicodeString(ByteBuffer bb)
			throws BufferUnderflowException {
		return getUnicodeString(bb, false);
	}

	public static String getUnicodeString(ByteBuffer bb, boolean switchEndian)
			throws BufferUnderflowException {
		byte[] out;
		short thisChar;
		synchronized (bb) {
			// Read len
			int stringLen = bb.getInt();
			if (switchEndian) {
				stringLen = Integer.reverseBytes(stringLen);
			}
			out = new byte[stringLen];
			// Read len worth of chars
			for (int i = 0; i < stringLen; ++i) {
				thisChar = bb.getShort();
				if (switchEndian)
					Short.reverseBytes(thisChar);
				out[i] = (byte) (thisChar & 0xff); // ignore first byte
			}
		}
		return new String(out);
	}

	public static void putUnicodeString(ByteBuffer bb, String data)
			throws BufferOverflowException {
		putUnicodeString(bb, data, false);
	}

	public static void putUnicodeString(ByteBuffer bb, String data,
			boolean switchEndian) throws BufferOverflowException {
		byte[] out;
		short thisChar;
		if (data == null)
			return;
		out = new byte[data.length()];
		out = data.getBytes();

		for (byte b : out) {
			thisChar = b;
			if (switchEndian)
				thisChar = Short.reverseBytes(thisChar);
			bb.putShort(thisChar);
		}
	}

	public static boolean checkByteBufferNearFull(ByteBuffer bb) {
        return bb.position() >= (bb.capacity() * 0.9);
    }

	//FIXME: Replace these!!!
//	public static ByteBuffer resizeByteBuffer(ByteBuffer bb, int multiplyer) {
//
//		ByteBuffer out = ByteBuffer.allocate(bb.capacity() * multiplyer);
//
//		// Copy the data to a temp buf
//		bb.flip();
//		out.put(bb);
//
//		return out;
//	}
//
//	public static ByteBuffer shrinkByteBuffer(ByteBuffer bb) {
//
//		bb.flip();
//		ByteBuffer out = ByteBuffer.allocate(bb.remaining());
//		out.put(bb);
//		return out;
//	}

}
