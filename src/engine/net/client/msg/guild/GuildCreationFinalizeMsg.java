// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.guild;

import engine.Enum.GameObjectType;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.GuildTag;
import engine.objects.Item;
import org.pmw.tinylog.Logger;

public class GuildCreationFinalizeMsg extends ClientNetMsg {

    private GuildTag guildTag;

    private String guildName;
    private String guildMotto;

    private int unknown01; //Appears to be a pad
    private int unknown02; //Appears to be a pad
    private int unknown03; //Appears to be a pad

    private int icVote;
    private int membershipVote;

    private int unknown04; //Always -1
    private int unknown05; //Appears to be a pad
    private int unknown06; //Appears to be a pad
    private int subType; //Appears to be a pad
    private int unknown08; //3 = close window, 4 = Failure to create Guild.
    private boolean close = false;
    private int charterUUID;

    /**
     * This is the general purpose constructor.
     */
    public GuildCreationFinalizeMsg() {
        super(Protocol.CREATEPETITION);
    }

    public GuildCreationFinalizeMsg(boolean close) {
        super(Protocol.CREATEPETITION);
        this.close = close;
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public GuildCreationFinalizeMsg(AbstractConnection origin, ByteBufferReader reader)  {
        super(Protocol.CREATEPETITION, origin, reader);
    }

    /**
     * Serializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
        
        GuildTag._serializeForDisplay(guildTag,writer);

        writer.putString(this.guildName);
        writer.putString(this.guildMotto);

        writer.putInt(this.unknown01);
        writer.putInt(this.unknown02);
        writer.putInt(this.unknown03);

        writer.putInt(this.membershipVote);
        writer.putInt(this.icVote);

        writer.putInt(this.unknown04);
        writer.putInt(this.unknown05);

        writer.putInt(this.unknown06);
        writer.putInt(3);
        writer.putInt(this.unknown08);
        writer.putInt(GameObjectType.Item.ordinal());
        writer.putInt(this.charterUUID);
    }

    /**
     * Deserializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
        guildTag = new GuildTag(reader, true);

        this.guildName = reader.getString();
        this.guildMotto = reader.getString();

        this.unknown01 = reader.getInt();
        this.unknown02 = reader.getInt();
        this.unknown03 = reader.getInt();

        this.membershipVote = reader.getInt();
        this.icVote = reader.getInt();

        this.unknown04 = reader.getInt();
        this.unknown05 = reader.getInt();

        this.unknown06 = reader.getInt();
        this.subType = reader.getInt();
        this.unknown08 = reader.getInt();
        reader.getInt(); // Object Type padding
        this.charterUUID = reader.getInt();
    }

    public String getName() {
        return this.guildName;
    }

    public String getMotto() {
        return this.guildMotto;
    }

    public Item getCharter() {

        Item charterObject;

        charterObject = Item.getFromCache(this.charterUUID);

        if (charterObject == null)
            Logger.error( "Invalid charter object UUID: " + this.charterUUID);

        return charterObject;
    }

    public GuildTag getGuildTag() {
        return this.guildTag;
    }

    public int getMemberVoteFlag() {
        
        if (this.membershipVote != 0)
            return 1;
        
        return 0;
    }

    public int getICVoteFlag() {
        
        if (this.icVote != 0)
            return 1;
        
        return 0;
    }
}
