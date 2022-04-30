// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.ItemBase;
import engine.objects.ItemFactory;
import engine.objects.PlayerCharacter;

/**
 * @author Eighty
 *
 */
public class CreateItemCmd extends AbstractDevCmd {

	public CreateItemCmd() {
        super("createitem");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		if (words.length < 2) {
			this.sendUsage(pc);
			return;
		}
		int id;
		id = ItemBase.getIDByName(words[0]);

		if (id == 0)
			id = Integer.parseInt(words[0]);
		if (id == 7){
			this.throwbackInfo(pc, "use /addgold to add gold.....");
			return;
		}

		int size = 1;

		if(words.length < 3) {
			size = Integer.parseInt(words[1]);
		}

		ItemFactory.fillInventory(pc, id, size);

	}

	@Override
	protected String _getHelpString() {
        return "Fill your inventory with items";
	}

	@Override
	protected String _getUsageString() {
        return "' /createitem <ItembaseID> <quantity>'";
	}

}
