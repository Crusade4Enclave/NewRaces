// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd;

import engine.Enum.GameObjectType;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.objects.*;

import java.util.ArrayList;

public abstract class AbstractDevCmd {

    protected final ArrayList<String> cmdStrings;
    private AbstractGameObject tr;
    private String rsult;

    public AbstractDevCmd(String cmdString) {
        super();
        this.cmdStrings = new ArrayList<>();
        this.addCmdString(cmdString);
        this.rsult = "";
    }

    /**
     * This function is called by the DevCmdManager. Method splits argString
     * into a String array and then calls the subclass specific _doCmd method.
     */

    public void doCmd(PlayerCharacter pcSender, String argString,
            AbstractGameObject target) {
        String[] args = argString.split(" ");

        if (pcSender == null) {
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("?")) {
            this.sendHelp(pcSender);
            this.sendUsage(pcSender);
        } else {
            this.tr = target;
            this._doCmd(pcSender, args, target);
        }
    }

    protected abstract void _doCmd(PlayerCharacter pcSender, String[] args,
            AbstractGameObject target);

    /**
     * Returns the string sent to the client that displays how to use this
     * command.
     */

    public final String getUsageString() {
        return "Usage: " + this._getUsageString();
    }

    protected abstract String _getUsageString();

    /**
     * Returns the string sent to the client that displays what this command
     * does.
     */

    public final String getHelpString() {
        return this.getMainCmdString() + ": " + this._getHelpString();
    }

    protected abstract String _getHelpString();

    public final ArrayList<String> getCmdStrings() {
        return cmdStrings;
    }

    public final String getMainCmdString() {
        return this.cmdStrings.get(0);
    }

    protected void addCmdString(String cmdString) {
        String lowercase = cmdString.toLowerCase();
        this.cmdStrings.add(lowercase);
    }

    public void setTarget(AbstractGameObject ago) {
        this.tr = ago;
    }

    public AbstractGameObject getTarget() {
        return this.tr;
    }

    public void setResult(String result) {
        this.rsult = result;
    }

    public String getResult() {
        return this.rsult;
    }

    /*
     * Helper functions
     */
    protected void sendUsage(PlayerCharacter pc) {
        this.throwbackError(pc, this.getUsageString());
    }

    protected void sendHelp(PlayerCharacter pc) {
        this.throwbackError(pc, this.getHelpString());
    }

    protected void throwbackError(PlayerCharacter pc, String msgText) {
        ChatManager.chatSystemError(pc, msgText);
    }

    protected void throwbackInfo(PlayerCharacter pc, String msgText) {
        ChatManager.chatSystemInfo(pc, msgText);
    }

    /*
     * Misc tools/helpers
     */
    protected static Building getTargetAsBuilding(PlayerCharacter pc) {
        int targetType = pc.getLastTargetType().ordinal();
        int targetID = pc.getLastTargetID();
        if (targetType == GameObjectType.Building.ordinal()) {
            Building b = (Building) DbManager
                    .getFromCache(GameObjectType.Building, targetID);
            if (b == null) {
                ChatManager.chatSystemError(
                        pc,
                        "Command Failed. Could not find building of ID "
                        + targetID);
                return null;
            }
            return b;
        } else {
            return null;
        }
    }

    protected static Mob getTargetAsMob(PlayerCharacter pc) {
        int targetType = pc.getLastTargetType().ordinal();
        int targetID = pc.getLastTargetID();
        if (targetType == GameObjectType.Mob.ordinal()) {
            Mob b = Mob.getMob(targetID);
            if (b == null) {
                ChatManager.chatSystemError(pc,
                        "Command Failed. Could not find Mob of ID " + targetID);
                return null;
            }
            return b;
        } else {
            return null;
        }
    }

    protected static NPC getTargetAsNPC(PlayerCharacter pc) {
        int targetType = pc.getLastTargetType().ordinal();
        int targetID = pc.getLastTargetID();
        if (targetType == GameObjectType.NPC.ordinal()) {
            NPC b = NPC.getFromCache(targetID);
            if (b == null) {
                ChatManager.chatSystemError(pc,
                        "Command Failed. Could not find NPC of ID " + targetID);
                return null;
            }
            return b;
        } else {
            return null;
        }
    }

}
