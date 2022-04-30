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
import engine.objects.Guild;
import engine.objects.GuildTag;
import engine.objects.PlayerCharacter;

import java.util.ArrayList;

public class SendGuildEntryMsg extends ClientNetMsg {

	private PlayerCharacter pc;

	/**
	 * This is the general purpose constructor.
	 */
	public SendGuildEntryMsg(PlayerCharacter pc) {
		super(Protocol.SENDGUILDENTRY);
		this.pc = pc;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public SendGuildEntryMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.SENDGUILDENTRY, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		
		ArrayList<Guild>subsAndSovs = new ArrayList<>();
		
		Guild nation = pc.getGuild().getNation();
		if (pc.getGuild() != nation)
			subsAndSovs.add(nation);
		subsAndSovs.addAll(pc.getGuild().getSubGuildList());
		
		
		writer.putInt(1);
		//GuildState
		//Petitioner = 2, Sworn = 4 , Nation = 5, protectorate = 6,  city-State = 7(nation), province = 8,
	
		writer.putInt(subsAndSovs.size());
		writer.putInt(1);
		if (subsAndSovs.size() > 0){
		
			for (Guild guild : subsAndSovs){
				int state = guild.getGuildState().getStateID();
				
					writer.putInt(guild.getObjectType().ordinal());
					writer.putInt(guild.getObjectUUID());
				
					writer.putString(guild.getName());
					
					//TODO set Alliance date
					writer.putShort((short)1);
					writer.putInt(0);
					writer.putShort((short)0);
					writer.put((byte)0);
					
					writer.putInt(state);
					GuildTag._serializeForDisplay(guild.getGuildTag(),writer);
					if (guild == nation)
						writer.putInt(2); // Break Fealty
					else
						writer.putInt(1); // Dismiss, Swear In.
			}
		}
	}

	/**
	 * Deserializes the subclass specific items from the supplied ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		
	}

	public PlayerCharacter getPc() {
		return pc;
	}

	public void setPc(PlayerCharacter pc) {
		this.pc = pc;
	}

}
