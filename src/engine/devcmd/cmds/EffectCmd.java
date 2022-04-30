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
import engine.gameManager.PowersManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.powers.EffectsBase;

/**
 * 
 * @author Eighty
 * 
 */
public class EffectCmd extends AbstractDevCmd {

	public EffectCmd() {
        super("effect");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		int ID = 0;
		int token = 0;

		if (args.length != 2) {
			this.sendUsage(pcSender);
			return;
		}
		ID = Integer.parseInt(args[0]);
		token = Integer.parseInt(args[1]);

		EffectsBase eb = PowersManager.setEffectToken(ID, token);
		if (eb == null) {
			throwbackError(pcSender, "Unable to find EffectsBase " + ID
					+ " to modify.");
			return;
		}
		ChatManager.chatSayInfo(pcSender,
				"EffectsBase with ID " + ID + " changed token to " + token);
	}

	@Override
	protected String _getUsageString() {
        return "' /effect EffectsBaseID Token'";
	}

	@Override
	protected String _getHelpString() {
        return "Temporarily places the effect token with the corresponding EffectsBase on the server";
	}

}
