// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.Enum;
import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import org.pmw.tinylog.Logger;


public class AssetSupportMsg extends ClientNetMsg {

    private int npcType;
    private int npcID;
    private int buildingType;
    private int buildingID;
    private int messageType;
    private int pad = 0;
    private int objectType;
    private int objectUUID;
    private int protectedBuildingType;
    private int protectedBuildingID;
    private int profitTax;
    private int weeklyTax;
    private byte enforceKOS;
    private Enum.SupportMsgType supportMsgType;
    public static int confirmProtect;


    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public AssetSupportMsg(AbstractConnection origin, ByteBufferReader reader) {
        super(Protocol.ASSETSUPPORT, origin, reader);
    }

    /**
     * Deserializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader) {

        this.messageType = reader.getInt();
        this.supportMsgType = Enum.SupportMsgType.typeLookup.get(this.messageType);

        if (this.supportMsgType == null) {
            this.supportMsgType = Enum.SupportMsgType.NONE;
            Logger.error("No enumeration for support type" + this.messageType);
        }

        switch (supportMsgType) {

            case PROTECT:
                this.buildingType = reader.getInt();
                this.buildingID = reader.getInt();
                this.npcType = reader.getInt();
                this.npcID = reader.getInt();
                reader.getInt();
                reader.getInt();
                this.protectedBuildingType = reader.getInt();
                this.protectedBuildingID = reader.getInt();
                reader.getInt();
                this.weeklyTax = reader.getInt();
                this.profitTax = reader.getInt();
                this.enforceKOS = reader.get();
                reader.get();
                reader.getInt();
                reader.getInt();
                break;

            case UNPROTECT:
                this.buildingType = reader.getInt();
                this.buildingID = reader.getInt();
                this.npcType = reader.getInt();
                this.npcID = reader.getInt();
                this.protectedBuildingType = reader.getInt();
                this.protectedBuildingID = reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                break;
            case VIEWUNPROTECTED:
                this.buildingType = reader.getInt();
                this.buildingID = reader.getInt();
                this.npcType = reader.getInt();
                this.npcID = reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                break;
            case REMOVETAX:
                reader.getInt();
                this.buildingID = reader.getInt();

                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.get();
                reader.get();
                reader.get();
                reader.getInt();
                reader.getInt();
                break;

            case ACCEPTTAX:
                reader.getInt();
                this.buildingID = reader.getInt();

                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();
                reader.getInt();

                reader.get();
                reader.get();
                reader.get();
                break;
        }
    }

    /**
     * Serializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) throws SerializationException {

        writer.putInt(this.messageType);
        this.supportMsgType = Enum.SupportMsgType.typeLookup.get(this.messageType);

        if (this.supportMsgType == null) {
            this.supportMsgType = Enum.SupportMsgType.NONE;
            Logger.error("No enumeration for support type" + this.messageType);
        }

        switch (this.supportMsgType) {

            case PROTECT:
                writer.putInt(this.buildingType);
                writer.putInt(this.buildingID);
                writer.putInt(npcType);
                writer.putInt(npcID);
                writer.putInt(0);
                writer.putInt(0);
                writer.putInt(this.protectedBuildingType);
                writer.putInt(this.protectedBuildingID);
                writer.putInt(0);
                writer.putInt(this.weeklyTax);
                writer.putInt(this.profitTax);
                writer.put(this.enforceKOS);
                writer.put((byte) 0);
                writer.putInt(0);
                writer.putInt(0);
                break;
            case CONFIRMPROTECT:
            	  writer.putInt(this.buildingType);
                  writer.putInt(this.buildingID);
                  writer.putInt(this.npcType);
                  writer.putInt(this.npcID);
                  writer.putInt(0);
                  writer.putInt(0);
                  writer.putInt(0);
                  writer.putInt(0);
                  writer.putInt(0);
                  writer.putInt(0);
                  writer.putInt(0);
                  writer.putInt(0);
                  writer.putInt(0);
                  writer.putInt(0);
                  writer.putInt(0);
                  writer.put((byte)0);
                  writer.put((byte)0);
                  writer.put((byte)0);
              
                  
                  break;
            case UNPROTECT:
                writer.putInt(this.buildingType);
                writer.putInt(this.buildingID);
                writer.putInt(npcType);
                writer.putInt(npcID);
                writer.putInt(this.protectedBuildingType);
                writer.putInt(this.protectedBuildingID);
                writer.putInt(0);
                writer.putInt(0);
                writer.putInt(0);
                writer.putInt(0);
                break;
            case VIEWUNPROTECTED:
                writer.putInt(this.buildingType);
                writer.putInt(this.buildingID);
                writer.putInt(npcType);
                writer.putInt(npcID);
                writer.putInt(0);
                writer.putInt(0);
                writer.putInt(0);
                writer.putInt(0);
                writer.putInt(0);
                writer.putInt(0);
                break;
        }
    }

    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(int value) {
        this.objectType = value;
    }

    public void setPad(int value) {
        this.pad = value;
    }

    public int getUUID() {
        return objectUUID;
    }

    public int getPad() {
        return pad;
    }


    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }


    public int getNpcType() {
        return npcType;
    }

    public void setNpcType(int npcType) {
        this.npcType = npcType;
    }

    public int getNpcID() {
        return npcID;
    }

    public void setNpcID(int npcID) {
        this.npcID = npcID;
    }

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

    public int getBuildingID() {
        return buildingID;
    }

    public void setBuildingID(int buildingID) {
        this.buildingID = buildingID;
    }

    public int getProtectedBuildingType() {
        return protectedBuildingType;
    }

    public void setProtectedBuildingType(int protectedBuildingType) {
        this.protectedBuildingType = protectedBuildingType;
    }

    public int getProtectedBuildingID() {
        return protectedBuildingID;
    }

    public void setProtectedBuildingID(int protectedBuildingID) {
        this.protectedBuildingID = protectedBuildingID;
    }

    public int getWeeklyTax() {
        return weeklyTax;
    }

    public void setWeeklyTax(int weeklyTax) {
        this.weeklyTax = weeklyTax;
    }

    public int getProfitTax() {
        return profitTax;
    }

    public void setProfitTax(int profitTax) {
        this.profitTax = profitTax;
    }

    public byte getEnforceKOS() {
        return enforceKOS;
    }

    public void setEnforceKOS(byte enforceKOS) {
        this.enforceKOS = enforceKOS;
    }
}
