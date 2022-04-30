// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.objects.Account;
import engine.objects.PlayerCharacter;


public class IgnoreMsg extends ClientNetMsg {

    private int unknown1;
    private int unknown2;
    private String nameToIgnore;

    /**
     * This is the general purpose constructor.
     */
    public IgnoreMsg() {
        super(Protocol.IGNORE);
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public IgnoreMsg(AbstractConnection origin, ByteBufferReader reader)  {
        super(Protocol.IGNORE, origin, reader);
    }

    /**
     * Serializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
        writer.putInt(unknown1);
        writer.putInt(unknown2);
        writer.putUnicodeString(nameToIgnore);
    }

    /**
     * Deserializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
        unknown1 = reader.getInt();
        unknown2 = reader.getInt();
        nameToIgnore = reader.getUnicodeString();
    }

    public void handleRequest(ClientConnection origin) {

        PlayerCharacter pcSource = SessionManager.getPlayerCharacter(origin);

        if (nameToIgnore.isEmpty()) { // list ignored players
            String[] ignoredPlayers = pcSource.getIgnoredPlayerNames();
            String crlf = "\r\n";
            String out = "Ignored players (" + ignoredPlayers.length + "):";
            for (String name : ignoredPlayers) {
                out += crlf + name;
            }
            ChatManager.chatSystemInfo(pcSource, out);
            return;
        }

        //FIX THIS, USE OUR CACHE!
        PlayerCharacter pcToIgnore = PlayerCharacter.getByFirstName(nameToIgnore);

        if (pcSource == null) {
            return;
        }
        if (pcToIgnore == null || pcToIgnore.getAccount() == null) {
            ChatManager.chatSystemError(pcSource, "Character name " + nameToIgnore + " does not exist and cannot be ignored.");
            return;
        }
        if (pcToIgnore.getObjectUUID() == pcSource.getObjectUUID()) {
            ChatManager.chatSystemError(pcSource, "Try as you might, you are unable to ignore yourself!");
            return;
        }
        String fn = pcToIgnore.getFirstName();

        Account ac = pcSource.getAccount();

        if (ac == null)
            return;

        if (pcSource.isIgnoringPlayer(pcToIgnore)) {

            if (ac != null) {
                if (!DbManager.PlayerCharacterQueries.SET_IGNORE_LIST(ac.getObjectUUID(), pcToIgnore.getObjectUUID(), false, pcToIgnore.getFirstName())) {
                    ChatManager.chatSystemError(pcSource, "Unable to update database ignore list.");
                }
            } else {
                ChatManager.chatSystemError(pcSource, "Unable to update database ignore list.");
            }

            pcSource.removeIgnoredPlayer(pcToIgnore.getAccount());
            ChatManager.chatSystemInfo(pcSource, "Character " + fn + " is no longer ignored.");
        } else {
            if (!PlayerCharacter.isIgnorable()) {
                ChatManager.chatSystemError(pcSource, "This character cannot be ignored.");
                return;
            }
            if (PlayerCharacter.isIgnoreListFull()) {
                ChatManager.chatSystemError(pcSource, "Your ignore list is already full.");
                return;
            }

            if (ac != null) {
                if (!DbManager.PlayerCharacterQueries.SET_IGNORE_LIST(ac.getObjectUUID(), pcToIgnore.getObjectUUID(), true, pcToIgnore.getFirstName())) {
                    ChatManager.chatSystemError(pcSource, "Unable to update database ignore list. This ignore will not persist past server down.");
                }
            } else {
                ChatManager.chatSystemError(pcSource, "Unable to update database ignore list.");
            }

            pcSource.addIgnoredPlayer(pcToIgnore.getAccount(), pcToIgnore.getFirstName());
            ChatManager.chatSystemInfo(pcSource, "Character " + fn + " is now being ignored.");
        }
    }

    /**
     * @return the unknown1
     */
    public int getUnknown1() {
        return unknown1;
    }

    /**
     * @param unknown1 the unknown1 to set
     */
    public void setUnknown1(int unknown1) {
        this.unknown1 = unknown1;
    }

    /**
     * @return the unknown2
     */
    public int getUnknown2() {
        return unknown2;
    }

    /**
     * @param unknown2 the unknown2 to set
     */
    public void setUnknown2(int unknown2) {
        this.unknown2 = unknown2;
    }

    /**
     * @return the nameToIgnore
     */
    public String getNameToIgnore() {
        return nameToIgnore;
    }

    /**
     * @param nameToIgnore the nameToIgnore to set
     */
    public void setNameToIgnore(String nameToIgnore) {
        this.nameToIgnore = nameToIgnore;
    }

}
