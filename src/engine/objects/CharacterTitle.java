// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.net.ByteBufferWriter;

import java.nio.ByteBuffer;

public enum CharacterTitle {

	NONE(0,0,0,""),
	CSR_1(255,0,0,"CCR"),
	CSR_2(255,0,0,"CCR"),
	CSR_3(255,0,0,"CCR"),
	CSR_4(251,181,13,"CCR"),
	DEVELOPER(166,153,114,"Programmer"),
	QA(88,250,244,"GIRLFRIEND");

	CharacterTitle(int _r, int _g, int _b, String _prefix) {
		char[] str_header = ("^\\c" +
								(((_r < 100)?((_r < 10)?"00":"0"):"") + ((byte) _r & 0xFF)) +
								(((_g < 100)?((_g < 10)?"00":"0"):"") + ((byte) _g & 0xFF)) +
								(((_b < 100)?((_b < 10)?"00":"0"):"") + ((byte) _b & 0xFF)) +
                '<' + _prefix + "> ").toCharArray();

		char[] str_footer = ("^\\c255255255").toCharArray();

		this.headerLength = str_header.length;
		this.footerLength = str_footer.length;

		this.header = ByteBuffer.allocateDirect(headerLength << 1);
		this.footer = ByteBuffer.allocateDirect(footerLength << 1);

		ByteBufferWriter headWriter= new ByteBufferWriter(header);

		for(char c : str_header) {
			headWriter.putChar(c);
		}

		ByteBufferWriter footWriter = new ByteBufferWriter(footer);

		for(char c : str_footer) {
			footWriter.putChar(c);
		}
	}

	int headerLength, footerLength;
	private ByteBuffer header;
	private ByteBuffer footer;

	public void _serializeFirstName(ByteBufferWriter writer, String firstName) {
		_serializeFirstName(writer, firstName, false);
	}

	public void _serializeFirstName(ByteBufferWriter writer, String firstName, boolean smallString) {
		if(this.ordinal() == 0) {
			if (smallString)
				writer.putSmallString(firstName);
			else
				writer.putString(firstName);
			return;
		}

		char[] chars = firstName.toCharArray();

		if (smallString)
			writer.put((byte)(chars.length + this.headerLength));
		else
			writer.putInt(chars.length + this.headerLength);
		writer.putBB(header);

		for(char c : chars) {
			writer.putChar(c);
		}
	}

	public void _serializeLastName(ByteBufferWriter writer, String lastName, boolean haln, boolean asciiLastName) {
		_serializeLastName(writer, lastName, haln, asciiLastName, false);
	}

	public void _serializeLastName(ByteBufferWriter writer, String lastName, boolean haln, boolean asciiLastName, boolean smallString) {
		if (!haln || asciiLastName) {
			if(this.ordinal() == 0) {
				if (smallString)
					writer.putSmallString(lastName);
				else
					writer.putString(lastName);
				return;
			}
		}

		if (!haln || asciiLastName) {
			char[] chars = lastName.toCharArray();

			if (smallString)
				writer.put((byte)(chars.length + this.footerLength));
			else
				writer.putInt(chars.length + this.footerLength);

			for(char c : chars) {
				writer.putChar(c);
			}

			writer.putBB(footer);
		} else {
			if (smallString)
				writer.put((byte)this.footerLength);
			else
				writer.putInt(this.footerLength);
			writer.putBB(footer);
		}

	}
}
