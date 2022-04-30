// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

/**
 * 
 * @author Eighty
 * 
 */
public class FlashMsgCmd extends AbstractDevCmd {

	public FlashMsgCmd() {
        super("flash");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		if (args.length != 1 || args[0].isEmpty()) {
			this.sendUsage(pcSender);
			return;
		}
		ChatManager.chatSystemFlash(args[0]);
	}

	/**
	 * This function is called by the DevCmdManager. Override to avoid splitting
	 * argString into String array, since flash displays full String as message,
	 * then calls the subclass specific _doCmd method.
	 *
	 */

	@Override
	public void doCmd(PlayerCharacter pcSender, String argString,
			AbstractGameObject target) {
		String[] args = new String[1];
		args[0] = argString;

		if (pcSender == null) {
			return;
		}

		this._doCmd(pcSender, args, target);
	}

	@Override
	protected String _getUsageString() {
        return "' /flash message'";
	}

	@Override
	protected String _getHelpString() {
        return "Flashes system message to all players";
	}

}
