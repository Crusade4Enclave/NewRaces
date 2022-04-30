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
import engine.objects.Item;
import engine.objects.PlayerCharacter;

/**
 * @author Eighty
 *
 */
public class AddGoldCmd extends AbstractDevCmd {

	public AddGoldCmd() {
        super("addgold");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		Item gold = pc.getCharItemManager().getGoldInventory();
		int curAmt;
		if (gold == null)
			curAmt = 0;
		else
			curAmt = gold.getNumOfItems();

		int amt;
		try {
			amt = Integer.parseInt(words[0]);
		} catch (NumberFormatException e) {
			throwbackError(pc, "Quantity must be a number, " + words[0] + " is invalid");
			return;
		}
		if (amt < 1 || amt > 10000000) {
			throwbackError(pc, "Quantity must be between 1 and 10000000 (10 million)");
			return;
		} else if ((curAmt + amt) > 10000000) {
			throwbackError(pc, "This would place your inventory over 10,000,000 gold.");
			return;
		}

		if (!pc.getCharItemManager().addGoldToInventory(amt, true)) {
			throwbackError(pc, "Failed to add gold to inventory");
			return;
		}

		ChatManager.chatSayInfo(pc, amt + " gold added to inventory");
		pc.getCharItemManager().updateInventory();
	}

	@Override
	protected String _getHelpString() {
        return "adds gold to inventory";
	}

	@Override
	protected String _getUsageString() {
        return "' /addGold quantity'";
	}

}
