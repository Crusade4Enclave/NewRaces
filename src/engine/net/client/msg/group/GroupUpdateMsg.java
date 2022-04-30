// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg.group;

import engine.Enum.GameObjectType;
import engine.gameManager.GroupManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.net.client.msg.ClientNetMsg;
import engine.objects.Group;
import engine.objects.PlayerCharacter;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//See GroupUpdateMsgBreakdown.txt is SBData directory.
public class GroupUpdateMsg extends ClientNetMsg {

    private int messageType;
    private int unknown02;
    private int playerUUID;
    private Set<PlayerCharacter> players;
    private Group group;

    /**
     * This is the general purpose constructor.
     */
    public GroupUpdateMsg() {
        super(Protocol.UPDATEGROUP);
        this.messageType = 1;
        this.unknown02 = 1;
        this.playerUUID = 0;
        this.players = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public GroupUpdateMsg(int messageType, int unknown02, Set<PlayerCharacter> players, Group group) {
        super(Protocol.UPDATEGROUP);
        this.messageType = messageType;
        this.unknown02 = unknown02;
        this.playerUUID = 0;
        this.players = players;
        this.group = group;
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public GroupUpdateMsg(AbstractConnection origin, ByteBufferReader reader)  {
        super(Protocol.UPDATEGROUP, origin, reader);
    }

    /**
     * Serializes the subclass specific items to the supplied ByteBufferWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
        writer.putInt(GameObjectType.Group.ordinal());
        writer.putInt(this.group.getObjectUUID());
        writer.putInt(this.messageType);

	// 5 breaks everything including movement etc
        // 4 sends a party dissolved message
        // 3 closes the group window and leaves the group
        // 2 seems to update the location but not the stats correctly upon coming back into range
        // 1 seems to add you to the group but if called by a job tops up your stats on the client and desyncs it
        
        switch (messageType) {
            case 4:
                GroupUpdateMsg._serializeFour(writer);
                break;
            case 5:
                this._serializeFive(writer);
                break;
            case 6:
                this._serializeSix(writer);
                break;
            case 7:
                this._serializeSeven(writer);
                break;
            case 8:
                this._serializeEight(writer);
                break;
            default:
                writer.putInt(this.unknown02);
                // Send player data
                writer.putInt(this.players.size());
                int i = 0;
                for (PlayerCharacter pc : this.players) {
                    this.serializePlayer(writer, pc, this.messageType, i++);
                }
                writer.putInt(0); // end data
                break;
        }
    }

    // *** Refactor: This method is an abortion.  Needs to be re-written from scratch.
    
    private void serializePlayer(ByteBufferWriter writer, PlayerCharacter player, int messageType, int count) {
        
        if (messageType == 1) {
            writer.putString((player != null) ? player.getFirstName() : "nullError");
            writer.putString((player != null) ? player.getLastName() : "");
        } else if (messageType == 2) {
            if (count == 0) {
                writer.putString((player != null) ? player.getFirstName() : "nullError");
                writer.putString((player != null) ? player.getLastName() : "");
            } else {
                writer.putInt(0);
            }
        } else if (messageType == 3) {
            writer.putString(" ");
            writer.putString(" ");
        } else {
            writer.putInt(0);
            writer.putInt(0);
        }

        if (messageType == 3) {
            for (int i = 0; i < 6; i++) {
                writer.putInt(0);
            }
        } else {
            // mana health stam %
            writer.putInt((int) (player.getHealth() / player.getHealthMax() * 100)); // should be health but does nothing
            writer.putInt((int) (player.getStamina() / player.getStaminaMax() * 100)); // stam %
            writer.putInt((int) (player.getMana() / player.getManaMax() * 100)); // mana %
            writer.putInt((player != null) ? Float.floatToIntBits(player.getLoc().getX()) : 0);
            writer.putInt((player != null) ? Float.floatToIntBits(player.getLoc().getY()) : 0);
            writer.putInt((player != null) ? Float.floatToIntBits(player.getLoc().getZ()) : 0);
        }
        
        if (player == null)
            writer.putLong(0);
        else {
            writer.putInt(GameObjectType.PlayerCharacter.ordinal());
            writer.putInt(player.getObjectUUID());
        }

        if (messageType == 3) {
            writer.putInt(0);
            writer.putInt(-1);
            writer.putInt(0);
            return;
        } else if (messageType == 5) {
            writer.putInt(0);
            writer.putInt(0);
            return;
        }
        if (group != null && player != null) {
            writer.putInt((this.group.getGroupLead() == player) ? 2 : 1);
        } else {
            writer.putInt(1);
        }
        if (messageType == 2) {
            writer.putInt(-1);
            writer.putInt(0);
            return;
        }
        writer.putInt(1);
        writer.putInt(1);
        writer.put((byte) 1);

	// if sending message type 1 this seems to make the group window flicker the button
        // i think getfollow and split gold might be the wrong way around
        if (group != null) {
            writer.put(this.group.getSplitGold() ? (byte) 1 : (byte) 0);
        } else {
            writer.put((byte) 0);
        }

        // always gets reset on a message type 1
        if (player != null) {
            writer.put(player.getFollow() ? (byte) 1 : (byte) 0);
        } else {
            writer.put((byte) 0);
        }
    }

    private static void _serializeFour(ByteBufferWriter writer) {

        // 4 sends a party dissolved window
        for (int i = 0; i < 3; i++) {
            writer.putInt(0);
        }
    }

    //sync player's stats and position
    private void _serializeFive(ByteBufferWriter writer) {
        writer.putInt(1);
        writer.putInt(players.size() - 1);
        for (PlayerCharacter player : players) {
            if (player.getObjectUUID() == this.playerUUID) {
                continue; //skip self
            }
            writer.putInt((int) (player.getHealth() / player.getHealthMax() * 100));
            writer.putInt((int) (player.getStamina() / player.getStaminaMax() * 100));
            writer.putInt((int) (player.getMana() / player.getManaMax() * 100));
            writer.putFloat(player.getLoc().x);
            writer.putFloat(player.getLoc().y);
            writer.putFloat(player.getLoc().z);
            writer.putInt(GameObjectType.PlayerCharacter.ordinal());
            writer.putInt(player.getObjectUUID());
           
        }
        writer.putInt(0);
        writer.putInt(0);
    }

    private void _serializeSix(ByteBufferWriter writer) {
        writer.putInt(0);
        if (this.group != null) {
            writer.put(this.group.getSplitGold() ? (byte) 1 : (byte) 0);
        } else {
            writer.put((byte) 0);
        }
        writer.putInt(0);
        writer.putInt(0);
    }

    private void _serializeSeven(ByteBufferWriter writer) {
        PlayerCharacter player = this.players.iterator().next();
        writer.putInt(0);
        if (player != null) {
            writer.put(player.getFollow() ? (byte) 1 : (byte) 0);
        } else {
            writer.put((byte) 0);
        }
        writer.putInt(0);
        writer.putInt(0);
    }

    private void _serializeEight(ByteBufferWriter writer) {
        PlayerCharacter player = this.players.iterator().next();;
        writer.putInt(0);
        if (player != null) {
            writer.put(player.getFollow() ? (byte) 1 : (byte) 0);
            writer.putInt(GameObjectType.PlayerCharacter.ordinal());
            writer.putInt(player.getObjectUUID());
        } else {
            writer.put((byte) 0);
            writer.putLong(0L);
        }
        writer.putInt(0);
        writer.putInt(0);
    }

    /**
     * Deserializes the subclass specific items from the supplied
     * ByteBufferReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
        this.players = Collections.newSetFromMap(new ConcurrentHashMap<>());

        reader.getInt();
        this.group = GroupManager.getGroup(reader.getInt());
        // TODO figure this mess out
    }

    public void addPlayer(PlayerCharacter value) {
        this.players.add(value);
    }

    public void setPlayer(PlayerCharacter value) {
        this.players.clear();
        this.players.add(value);
    }

    /**
     * @return the messageType
     */
    public int getMessageType() {
        return messageType;
    }

    /**
     * @param messageType the messageType to set
     */
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    /**
     * @return the unknown02
     */
    public int getUnknown02() {
        return unknown02;
    }

    /**
     * @param unknown02 the unknown02 to set
     */
    public void setUnknown02(int unknown02) {
        this.unknown02 = unknown02;
    }

    /**
     * @return the playerUUID
     */
    public long getPlayerID() {
        return playerUUID;
    }

    /**
     * @param playerUUID the playerUUID to set
     */
    public void setPlayerUUID(int playerUUID) {
        this.playerUUID = playerUUID;
    }

    /**
     * @return the players
     */
    public Set<PlayerCharacter> getPlayers() {
        return players;
    }

    /**
     * @param players the players to set
     */
    public void setPlayers(Set<PlayerCharacter> players) {
        this.players = players;
    }

    /**
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(Group group) {
        this.group = group;
    }

}
