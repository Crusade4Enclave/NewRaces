// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.*;

import java.time.LocalDateTime;

public class GuildTreeStatusMsg extends ClientNetMsg {

    // 2 = manage this asset.  20 = manage entire city
    private int targetType;
    private int targetID;
    private String CityName;
    private String OwnerName;
    private String GuildName;

    private String motto; //motto Length 60 max?
    private Building treeOfLife;
    private PlayerCharacter player;
    private City city;
    private Zone cityZone;
    private GuildTag cityGuildTag;
    private GuildTag cityNationTag;
    private java.time.LocalDateTime cityDate;
    private boolean canAccess = false;
    private byte canBind = 0;
    private int accessType = 0;

    /**
     * This is the general purpose constructor
     */
    public GuildTreeStatusMsg() {
        super(Protocol.GUILDTREESTATUS);

        this.targetType = 0;
        this.targetID = 0;
        this.OwnerName = "";
        this.CityName = "";
        this.GuildName = "";
        this.cityGuildTag = null;
        this.cityNationTag = null;
    }

    public GuildTreeStatusMsg(Building treeOfLife, PlayerCharacter sourcePlayer) {
        super(Protocol.GUILDTREESTATUS);
        this.treeOfLife = treeOfLife;
        this.player = sourcePlayer;
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public GuildTreeStatusMsg(AbstractConnection origin, ByteBufferReader reader)
             {
        super(Protocol.GUILDTREESTATUS, origin, reader);
    }

    /**
     * Deserializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)
             {

        targetType = reader.getInt();
        targetID = reader.getInt();

        for (int i = 0; i < 3; i++) {
            reader.monitorInt(0, "GuildTreeStatusMSG");
        }
    }

    public void configure() {

        this.targetType = treeOfLife.getObjectType().ordinal();
        this.targetID = treeOfLife.getObjectUUID();
        this.OwnerName = treeOfLife.getOwner() != null ? treeOfLife.getOwnerName() : "Abandoned";
        this.CityName = treeOfLife.getCityName();
        this.GuildName = treeOfLife.getGuildName();

        this.cityGuildTag = treeOfLife.getGuild().getGuildTag();
        this.cityNationTag = this.treeOfLife.getGuild().getNation().getGuildTag();

        canAccess = this.canModify();
        canBind = 0;
        
        if (player.getGuild() != null && this.treeOfLife.getGuild() != null && !this.treeOfLife.getGuild().isEmptyGuild()
                && player.getGuild().getNation() == this.treeOfLife.getGuild().getNation())
            canBind = 1;


        if (this.treeOfLife.getGuild() != null && this.treeOfLife.getGuild().getOwnedCity() == null)
            accessType = 9;

        //accessType not 9 not null city
        if (accessType != 9)
            if (this.treeOfLife.getGuild().getOwnedCity().isForceRename() && canAccess )
                accessType = 10;
            else accessType = 8;

        cityZone = this.treeOfLife.getParentZone();
        city = null;

        if (cityZone != null)
        	if (cityZone.isPlayerCity())
            city = City.GetCityFromCache(cityZone.getPlayerCityUUID());
        	else
        		if (this.treeOfLife != null && this.treeOfLife.getGuild() != null)
        			city = this.treeOfLife.getGuild().getOwnedCity();
        

        if (city == null)
            CityName = "None";
        else
            CityName = city.getCityName();

        if (city == null)
            cityDate = LocalDateTime.now();
        else
            cityDate = city.established;

    }

    private boolean canModify() {

        return this.player.getGuild() == this.treeOfLife.getGuild() && GuildStatusController.isInnerCouncil(player.getGuildStatus());
    }

    /**
     * Serializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {

        writer.putInt(targetType);
        writer.putInt(targetID);

        if (canAccess)
            writer.putInt(accessType);
      
        else
            writer.putInt(9);

        GuildTag._serializeForDisplay(cityGuildTag,writer);
        GuildTag._serializeForDisplay(cityNationTag,writer);

        writer.putString(CityName);
        writer.putString(GuildName);
        writer.putString(OwnerName);

        writer.putLocalDateTime(cityDate);
        writer.putInt(0);
        writer.putInt(0);
        if (city == null)
        	writer.putInt(0);
        else
        writer.putInt(city.isOpen() ? 1 : 0); //check mark for open city
        writer.putInt(0);
        writer.putInt(0);
        writer.put(canBind);
        writer.put((byte) 0);

        if (canAccess)
            writer.put((byte) 1);
        else
            writer.put((byte) 0);

        writer.put((byte) 0);

        writer.putInt(1);
        writer.putString(city != null ? city.getDescription() : "None");
        writer.putInt(0);
    }

    public int getTargetID() {
        return targetID;
    }

}