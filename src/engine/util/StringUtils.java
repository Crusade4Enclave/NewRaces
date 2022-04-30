// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.util;

import java.net.InetAddress;
import java.util.StringTokenizer;

public class StringUtils {

	public static String addWS(String s, int totalLen) {
		if (s.length() >= totalLen) {
			return s;
		}

		int diff = totalLen - s.length();

		String out = s;

		for (int i = 0; i < diff; ++i) {
			out += " ";
		}
		return out;
	}

	public static String bannerize(String s, int totalLen) {
		if (s.length() >= totalLen) {
			return s;
		}

		int diff = totalLen - s.length();
		int halfDiff = diff / 2;

		String side = "";
		for (int i = 0; i < halfDiff; ++i) {
			side += "*";
		}

		return side + ' ' + s + ' ' + side;
	}

	public static String InetAddressToClientString(InetAddress address) {
		return address.toString().replaceAll("/", "");
	}

	public static String toHexString(int i) {
		return Integer.toHexString(i).toUpperCase();
	}

	public static String toHexString(long l) {
		return Long.toHexString(l).toUpperCase();
	}

	// Well done IDA Pro.

	public static int hashString(String toHash) {
		byte[] hashArray = toHash.getBytes();
		int hash = 0;
		int shift = 0;
		if (hashArray.length == 8 ||hashArray.length == 7){
			int ecx = 0;
			if (hashArray.length == 8){
				ecx = hashArray[7];
			}
			int eax = hashArray[4];
			int esi = ecx * 0x8;
            eax ^= esi;
            ecx ^= 0x5A0;
			esi = hashArray[5];
            eax <<= 4;
            eax ^= esi;
			esi = hashArray[6];
            eax <<= 4;
            eax ^= esi;
			esi = hashArray[2];
            eax <<= 5;
            eax ^= esi;
			esi = hashArray[1];
			int edx = hashArray[0];
            eax <<= 5;
            eax ^= esi;
            ecx /= 2;
            ecx /= 2;
            eax <<= 5;
            ecx ^= edx;
            eax ^= ecx;
			return eax;
		}else{

			for (int i = 0; i<hashArray.length;i++){
				if (i == 0)
					shift = 0;
				else
                    shift += 5;
				int toShift = hashArray[i] - 0x20;
				int shifted = (toShift<<shift);
                hash ^= shifted;
				if (shift > 24){
					int newShift = 0x20 - shift;
					int newShifted = toShift >> newShift;
                    hash ^= newShifted;
					if (shift > 27){
                        shift -= 0x20;
					}
				}
			}
			return hash;
		}
	}

         public static String wordWrap(String text,int LineWidth)
	{
		StringTokenizer st=new StringTokenizer(text);
		int SpaceLeft=LineWidth;
		int SpaceWidth=80;
                String outString = "";
                
		while(st.hasMoreTokens())
		{
			String word=st.nextToken();
			if((word.length()+SpaceWidth)>SpaceLeft)
			{
				outString+= '\n' +word+ ' ';
				SpaceLeft=LineWidth-word.length();
			}
			else
			{
				outString+=word+ ' ';
				SpaceLeft-=(word.length()+SpaceWidth);
			}
                
		}
                
                return outString;

}
         
public static String truncate(String input, int length) {
  if (input != null && input.length() > length)
    input = input.substring(0, length);
  return input;
}
   
}
