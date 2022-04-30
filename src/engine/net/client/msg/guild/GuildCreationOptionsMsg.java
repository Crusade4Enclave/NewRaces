// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.guild;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.GuildTag;

public class GuildCreationOptionsMsg extends ClientNetMsg {

	private int screenType;
	private ScreenType messageScreen;
	private boolean close = false;

	/**
	 * This is the general purpose constructor.
	 */
	public GuildCreationOptionsMsg() {
		super(Protocol.CHECKUNIQUEGUILD);
	}
	public GuildCreationOptionsMsg(boolean close) {
		super(Protocol.CHECKUNIQUEGUILD);
		this.close = close;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public GuildCreationOptionsMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.CHECKUNIQUEGUILD, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.screenType);
		if (this.close){
			writer.putInt(2);
			writer.putInt(0);
//			writer.putInt(0);
			return;
		}
		if(this.messageScreen != null) {
			this.messageScreen._serialize(writer);
		} else {
			writer.putInt(0);
			writer.putInt(5);
		}
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.screenType = reader.getInt();
		if(this.screenType == 1) {
			this.messageScreen = new Screen1();
		} else if(this.screenType == 2){
			this.messageScreen = new Screen2();
		} else {
			System.out.println("Attempting to Deserilaize: Message Type" + screenType);
			int counter = 0;
			do {
			int i = reader.getInt();
			System.out.println(counter++ + "->" + i);
			} while (true);
		}
		this.messageScreen._deserialize(reader);
	}

	/**
	 * @return the rulership
	 */
	public int getScreenType() {
		return this.screenType;
	}

	/**
	 * @param rulership
	 *            the rulership to set
	 */
	public void setScreenType(int type) {
		this.screenType = type;
	}
}

abstract class ScreenType {
	public abstract void _serialize(ByteBufferWriter writer);
	public abstract void _deserialize(ByteBufferReader reader) ;
}

class Screen1 extends ScreenType{
	private int unknown01;
	private int unknown02;
	private String name;
	private String motto;

	@Override
	public void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		writer.putString(this.name);
	}
	@Override
	public void _deserialize(ByteBufferReader reader)
			 {
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.name = reader.getString();
		this.motto = reader.getString();
	}
}

class Screen2 extends ScreenType{
	private int unknown01;
	private int unknown02;
	private GuildTag gt;

	@Override
	public void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		GuildTag._serializeForGuildCreation(this.gt,writer);
	}
	@Override
	public void _deserialize(ByteBufferReader reader)
			 {
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.gt = new GuildTag(reader, true);
	}
}


