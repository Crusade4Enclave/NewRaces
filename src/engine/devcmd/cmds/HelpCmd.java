// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.DevCmdManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

public class HelpCmd extends AbstractDevCmd {

	public HelpCmd() {
		super("help");
		this.addCmdString("list");
	}

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
        if (pcSender == null)
            return;
        if (pcSender.getAccount() == null)
            return;
        this.throwbackInfo(
                pcSender,
                "Type ' /command ?' for info about a command.  A space is necessary before the slash.");
        String commands = DevCmdManager.getCmdsForAccessLevel();
        this.throwbackInfo(pcSender, "Commands your account is eligible to use: ");

        int first = 0;
        int last = 500;
        int charLimit = 500;
        while (commands.length() > charLimit) {
            this.throwbackInfo(pcSender, commands.substring(first, last));
            first = charLimit;
            charLimit += 500;
            last = charLimit;
		}
		this.throwbackInfo(pcSender, commands.substring(first));
	}

	@Override
	protected String _getUsageString() {
        return "' /help' || ' /list'";
	}

	@Override
	protected String _getHelpString() {
        return "Displays help info and lists all commands accessible for the player's access level.";
	}

}
