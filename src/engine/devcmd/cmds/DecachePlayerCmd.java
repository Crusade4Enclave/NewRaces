// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.DbManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

/**
 *
 */
public class DecachePlayerCmd extends AbstractDevCmd {

	public DecachePlayerCmd() {
        super("decacheplayer");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		if(words.length < 1) {
			this.sendUsage(pc);
		}

		int objectUUID = Integer.parseInt(words[0]);

		if(DbManager.inCache(Enum.GameObjectType.PlayerCharacter, objectUUID)) {
			this.setTarget(PlayerCharacter.getFromCache(objectUUID)); //for logging
			PlayerCharacter.getFromCache(objectUUID).removeFromCache();
		} else {
			this.sendHelp(pc);
		}
	}

	@Override
	protected String _getHelpString() {
        return "No player found. Please make sure the table ID is correct.";
	}

	@Override
	protected String _getUsageString() {
        return "' /decacheplayer <UUID>'";
	}

}
