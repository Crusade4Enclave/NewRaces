// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;

public class GuildTag {
	public final int backgroundColor01;
	public final int backgroundColor02;
	public final int symbolColor;
	public final int symbol;
	public final int backgroundDesign;
	public static final GuildTag ERRANT = new GuildTag(16,16,16,0,0);

	public GuildTag(int backgroundColor01, int backgroundColor02,
			int symbolColor, int symbol, int backgroundDesign) {
		super();
		this.backgroundColor01 = backgroundColor01;
		this.backgroundColor02 = backgroundColor02;
		this.symbolColor = symbolColor;
		this.symbol = symbol;
		this.backgroundDesign = backgroundDesign;
	}

	public GuildTag(ByteBufferReader reader, boolean forCreation) {
		this.backgroundColor01 = reader.getInt();
		this.backgroundColor02 = reader.getInt();
		this.symbolColor = reader.getInt();
		if(forCreation) {
			this.symbol = reader.getInt();
			this.backgroundDesign = reader.getInt();
		} else {
			this.backgroundDesign = reader.getInt();
			this.symbol = reader.getInt();
		}
	}

	public GuildTag(ByteBufferReader reader) {
		this(reader, false);
	}

	public boolean isValid() {
		if(this.backgroundColor01 < 0 || this.backgroundColor01 > 18)
			return false;
		if(this.backgroundColor02 < 0 || this.backgroundColor02 > 18)
			return false;
		if(this.symbolColor < 0 || this.symbolColor > 18)
			return false;
		if(this.symbol < 0 || this.symbol > 183)
			return false;
        return this.backgroundDesign >= 0 && this.backgroundDesign <= 14;
    }

	
	public static void _serializeForGuildCreation(GuildTag guildTag, ByteBufferWriter writer) {
		writer.putInt(guildTag.backgroundColor01);
		writer.putInt(guildTag.backgroundColor02);
		writer.putInt(guildTag.symbolColor);
		writer.putInt(guildTag.symbol);
		writer.putInt(guildTag.backgroundDesign);
	}

	public static void _serializeForDisplay(GuildTag guildTag, ByteBufferWriter writer) {
		writer.putInt(guildTag.backgroundColor01);
		writer.putInt(guildTag.backgroundColor02);
		writer.putInt(guildTag.symbolColor);
		writer.putInt(guildTag.backgroundDesign);
		writer.putInt(guildTag.symbol);
	}

	public void serializeObject(ByteBufferWriter writer) {
		writer.put((byte)this.backgroundColor01);
		writer.put((byte)this.backgroundColor02);
		writer.put((byte)this.symbolColor);
		writer.put((byte)this.backgroundDesign);
		writer.put((byte)this.symbol);
	}

	
	
	public String summarySentence() {
		return "Bkgrnd: " + this.backgroundDesign + '(' + this.backgroundColor01 + '-' + this.backgroundColor02 + ')' +
				"; Symbol: " + this.symbol + '(' + this.symbolColor + ')';
	}
}
