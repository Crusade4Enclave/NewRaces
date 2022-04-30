// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ByteAnalyzer {

	public static void analyze4Bytes(byte[] data, String Label,
			boolean switchEndian) throws IOException {

		ByteArrayInputStream bias = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bias);
		dis.mark(4);

		System.out.println("Analysis 4 bytes: (" + Label + ')');
		System.out.println("\t Hex: " + ByteUtils.byteArrayToStringHex(data));
		System.out.println(ByteAnalyzer.buildAll4(dis, switchEndian));
		System.out.println(ByteAnalyzer.buildUTF8(dis));
		System.out.println(ByteAnalyzer.buildUTF16(dis));
		System.out.println(ByteAnalyzer.buildRawNumericalBytes(dis));
		System.out.println();
	}

	public static void analyze8Bytes(byte[] data, String Label,
			boolean switchEndian) throws IOException {

		ByteArrayInputStream bias = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bias);
		dis.mark(8);

		System.out.println("Analysis for 8 bytes: (" + Label + ')');
		System.out.println("\tHex: " + ByteUtils.byteArrayToStringHex(data));
		System.out.println(ByteAnalyzer.buildAll8(dis, switchEndian));
		dis.reset();
		System.out.println(ByteAnalyzer.buildUTF8(dis));
		System.out.println(ByteAnalyzer.buildUTF16(dis));
		System.out.println(ByteAnalyzer.buildRawNumericalBytes(dis));
		System.out.println("\n");
	}

	public static String buildAll8(DataInputStream indis, boolean se)
			throws IOException {
		byte[] ba = new byte[8];
		indis.read(ba);
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ba));
		dis.mark(8);

		String out = "";

		out += buildFromTemplate("8", dis, se);
		out += buildFromTemplate("4.4", dis, se);

		out += buildFromTemplate("4.2.2", dis, se);
		out += buildFromTemplate("4.2.1.1", dis, se);
		out += buildFromTemplate("4.1.2.1", dis, se);
		out += buildFromTemplate("4.1.1.2", dis, se);
		out += buildFromTemplate("4.1.1.1.1", dis, se);

		out += buildFromTemplate("2.2.4", dis, se);
		out += buildFromTemplate("2.1.1.4", dis, se);
		out += buildFromTemplate("1.2.1.4", dis, se);
		out += buildFromTemplate("1.1.2.4", dis, se);
		out += buildFromTemplate("1.1.1.1.4", dis, se);

		out += buildFromTemplate("2.4.2", dis, se);
		out += buildFromTemplate("2.4.1.1", dis, se);
		out += buildFromTemplate("1.1.4.2", dis, se);
		out += buildFromTemplate("1.1.4.1.1", dis, se);

		out += buildFromTemplate("2.1.2.2.1", dis, se);
		out += buildFromTemplate("2.1.2.1.2", dis, se);
		out += buildFromTemplate("1.2.2.2.1", dis, se);
		out += buildFromTemplate("1.2.2.1.2", dis, se);

		out += buildFromTemplate("1.1.1.2.2.1", dis, se);
		out += buildFromTemplate("2.1.2.1.1.1", dis, se);
		out += buildFromTemplate("1.1.1.2.1.1.1", dis, se);
		out += buildFromTemplate("1.1.1.1.1.1.1.1", dis, se);

		out += buildFromTemplate("2.1.1.1.1.1.1", dis, se);
		out += buildFromTemplate("1.2.1.1.1.1.1", dis, se);
		out += buildFromTemplate("1.1.2.1.1.1.1", dis, se);

		out += buildFromTemplate("1.1.1.1.2.1.1", dis, se);
		out += buildFromTemplate("1.1.1.1.1.2.1", dis, se);
		out += buildFromTemplate("1.1.1.1.1.1.2", dis, se);

		return out;
	}

	public static String buildAll4(DataInputStream indis, boolean se)
			throws IOException {
		byte[] ba = new byte[4];
		indis.read(ba);
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ba));
		dis.mark(4);

		String out = "";
		out += buildFromTemplate("4", dis, se);
		out += buildFromTemplate("2.2", dis, se);
		out += buildFromTemplate("2.1.1", dis, se);
		out += buildFromTemplate("1.2.1", dis, se);
		out += buildFromTemplate("1.1.2", dis, se);
		out += buildFromTemplate("1.1.1.1", dis, se);

		return out;
	}

	public static String buildFromTemplate(String template,
			DataInputStream dis, boolean se) throws IOException {
		String out = '\t' + template + ": ";

		for (int i = template.length(); i < 16; ++i) {
			out += " ";
		}

		template = template.replace(".", "");
		String[] items = template.split("");
		dis.mark(dis.available());

		for (String s : items) {
			if (s.equals("1")) {
				out += " (B:" + dis.readByte();
				dis.reset();
				out += "/uB:" + dis.readUnsignedByte() + ')';
			} else if (s.equals("2")) {
				byte[] read = new byte[2];
				dis.read(read);
				byte[] use = new byte[2];
				if (se) {
					use = ByteUtils.switchByteArrayEndianness(read);
				} else {
					use = read;
				}
				out += " (S:"
						+ new DataInputStream(new ByteArrayInputStream(use))
								.readShort();
				out += "/uS:"
						+ new DataInputStream(new ByteArrayInputStream(use))
								.readUnsignedShort() + ')';

			} else if (s.equals("4")) {
				byte[] read = new byte[4];
				dis.read(read);
				byte[] use = new byte[4];
				if (se) {
					use = ByteUtils.switchByteArrayEndianness(read);
				} else {
					use = read;
				}
				out += "  (I:";
				out += new DataInputStream(new ByteArrayInputStream(use))
						.readInt();

				out += " / F:";
				out += new DataInputStream(new ByteArrayInputStream(use))
						.readFloat()
						+ ")";
			} else if (s.equals("8")) {
				byte[] read = new byte[8];
				dis.read(read);

				byte[] use = new byte[8];
				if (se) {
					use = ByteUtils.switchByteArrayEndianness(read);
				} else {
					use = read;
				}
				out += "  (L:";
				out += new DataInputStream(new ByteArrayInputStream(use))
						.readLong();

				out += " / D:";
				out += new DataInputStream(new ByteArrayInputStream(use))
						.readDouble()
						+ ")";
			}
		}
		dis.reset();
		return out + '\n';
	}

	public static String buildUTF8(DataInputStream dis) throws IOException {
		dis.mark(dis.available());
		String out = "\tUTF-8: ";
		while (dis.available() > 1) {
			out += " '" + (char) dis.read() + '\'';
		}
		dis.reset();
		return out;
	}

	public static String buildUTF16(DataInputStream dis) throws IOException {
		dis.mark(dis.available());
		String out = "\tUTF-16:";
		while (dis.available() > 1) {
			out += " '" + dis.readChar() + '\'';
		}
		dis.reset();
		return out;
	}

	public static String buildRawNumericalBytes(DataInputStream dis)
			throws IOException {
		dis.mark(dis.available());
		String out = "\tRaw Bytes (int vals): ";
		while (dis.available() > 1) {
			out += " '" + dis.read() + '\'';
		}
		dis.reset();
		return out;
	}

}
