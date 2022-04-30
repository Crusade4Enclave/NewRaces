// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.objects.*;

import java.util.ArrayList;

public class PrintBankCmd extends AbstractDevCmd {

	public PrintBankCmd() {
		super("printbank");
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		AbstractWorldObject tar;
		String name;
		String type = "PlayerCharacter";

		if (target != null) {
			if (target instanceof AbstractCharacter) {
				tar = (AbstractCharacter) target;
				name = ((AbstractCharacter) tar).getFirstName();
			} else {
				tar = pc;
				name = ((AbstractCharacter) tar).getFirstName();
			}
		} else {
			tar = pc;
			name = ((AbstractCharacter) tar).getFirstName();
		}

		if (!(tar instanceof PlayerCharacter)) {
			throwbackError(pc, "Must target player");
			return;
		}


		CharacterItemManager cim = ((AbstractCharacter)tar).getCharItemManager();
		ArrayList<Item> list = cim.getBank();
		throwbackInfo(pc, "Bank for " + type + ' ' + name + " (" + tar.getObjectUUID() + ')');
		for (Item item : list) {
			throwbackInfo(pc, "    " + item.getItemBase().getName() + ", count: " + item.getNumOfItems());
		}
		Item gold = cim.getGoldBank();
		if (gold != null)
			throwbackInfo(pc, "    Gold, count: " + gold.getNumOfItems());
		else
			throwbackInfo(pc, "    NULL Gold");
	}

	@Override
	protected String _getHelpString() {
		return "Returns the player's current bank";
	}

	@Override
	protected String _getUsageString() {
		return "' /printbank'";
	}

}
