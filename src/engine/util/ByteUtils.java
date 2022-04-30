// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class ByteUtils {

	private ByteUtils() {
	}

	public static byte[] switchByteArrayEndianness(byte[] in) {
		int size = in.length;

		byte[] out = new byte[size];

		for (int i = 0; i < size; ++i) {
			out[size - i - 1] = in[i];
		}
		return out;
	}

	/*
	 * Converts a single byte to a hex StringBuffer
	 */
	public static void byteToStringHex(byte b, StringBuffer buf) {
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		int high = ((b & 0xf0) >> 4);
		int low = (b & 0x0f);
		buf.append(hexChars[high]);
		buf.append(hexChars[low]);
	}

	/*
	 * Converts a single byte to a hex String
	 */
	public static String byteToStringHex(byte b) {
		StringBuffer sb = new StringBuffer();
		byteToStringHex(b, sb);
		return sb.toString();
	}

	/*
	 * Converts a byte array to hex String
	 */
	public static String byteArrayToStringHex(byte[] block) {
		StringBuffer buf = new StringBuffer();
		int len = block.length;
		
		for (int i = 0; i < len; i++) {
			ByteUtils.byteToStringHex(block[i], buf);
			if (i < len - 1) {
				buf.append(':');
			}
		}
		return buf.toString();
	}

	/*
	 * Converts a single byte to a hex StringBuffer
	 */
	public static void byteToSafeStringHex(byte b, StringBuffer buf) {
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		int high = ((b & 0xf0) >> 4);
		int low = (b & 0x0f);
		buf.append(hexChars[high]);
		buf.append(hexChars[low]);
	}

	/*
	 * Converts a single byte to a hex String
	 */
	public static String byteToSafeStringHex(byte b) {
		StringBuffer sb = new StringBuffer();
		byteToSafeStringHex(b, sb);
		return sb.toString();
	}

	/*
	 * Converts a byte array to hex String
	 */
	public static String byteArrayToSafeStringHex(byte[] block) {
		StringBuffer buf = new StringBuffer();

		int len = block.length;

		for (int i = 0; i < len; i++) {
			ByteUtils.byteToSafeStringHex(block[i], buf);
		}
		return buf.toString();
	}

	/*
	 * Converts a hex string to Byte Array
	 */
	public static byte[] stringHexToByteArray(String hex) {
		int length = hex.length();
		char[] hexchar = hex.toCharArray();
		byte[] ret = new byte[length / 2];
		int i1 = 0;

		for (int i = 0; i < length - 1; i += 2) {
			ret[i1] = (byte) (Character.digit(hexchar[i], 16) * 16 + Character
					.digit(hexchar[i + 1], 16));
			i1++;
		}

		return ret;
	}
	
	/*
	 * Converts a hex string formatted by our byteToStringHex to a byte array
	 * returns null if passed a null string
	 */
	public static byte[] formattedStringHexToByteArray(String hex) {
		if(hex == null){
			return null;
		}
			
		String tmpString = hex.replaceAll(":","");
		int length = tmpString.length();
		char[] hexchar = tmpString.toCharArray();
		byte[] ret = new byte[length / 2];
		int i1 = 0;

		for (int i = 0; i < length - 1; i += 2) {
			ret[i1] = (byte) (Character.digit(hexchar[i], 16) * 16 + Character
					.digit(hexchar[i + 1], 16));
			i1++;
		}

		return ret;
	}
	
	public static byte[] compress(final byte[] in) throws IOException{
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final GZIPOutputStream gzOs = new GZIPOutputStream(out);
        gzOs.write(in);
        gzOs.close();
        return out.toByteArray();
	}
	
    public static byte[] decompress(final byte[] in) throws IOException{
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final GZIPInputStream gzIs = new GZIPInputStream(new ByteArrayInputStream(in));
        final byte[] buffer = new byte[512];
        int lastRead = 0;
        
        lastRead = gzIs.read(buffer);
        while (lastRead > 0) {
        	out.write(buffer,0,lastRead);
            lastRead = gzIs.read(buffer);
        }
        gzIs.close();
        return out.toByteArray();
    }
	
}
