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
import engine.objects.LootTable;
import engine.objects.PlayerCharacter;

/**
 *
 * @author Eighty
 *
 */
public class MBDropCmd extends AbstractDevCmd {

	public MBDropCmd() {
        super("mbdrop");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		String newline = "\r\n ";
		if (args.length != 1){
			this.sendUsage(pcSender);
			this.sendHelp(pcSender);
			return;
		}

		String output = "";
		switch (args[0].toLowerCase()){
		case "clear":

			LootTable.contractCount = 0;
			LootTable.dropCount = 0;
			LootTable.glassCount = 0;
			LootTable.runeCount = 0;
			LootTable.rollCount = 0;
			LootTable.resourceCount = 0;

			LootTable.contractDroppedMap.clear();
			LootTable.glassDroppedMap.clear();
			LootTable.itemsDroppedMap.clear();
			LootTable.resourceDroppedMap.clear();
			LootTable.runeDroppedMap.clear();
			break;
		case "all":
			output = LootTable.dropCount + " items - ITEM NAME : DROP COUNT" + newline;
			for (ItemBase ib: LootTable.itemsDroppedMap.keySet()){

				int dropCount = LootTable.itemsDroppedMap.get(ib);
				output += ib.getName() + " : " + dropCount + newline;

			}
			break;
		case "resource":
			output = LootTable.resourceCount + " Resources - ITEM NAME : DROP COUNT" + newline;
			for (ItemBase ib: LootTable.resourceDroppedMap.keySet()){

				int dropCount = LootTable.resourceDroppedMap.get(ib);
				output += ib.getName() + " : " + dropCount + newline;

			}

			break;
		case "rune":

			output =  LootTable.runeCount + " Runes - ITEM NAME : DROP COUNT" + newline;
			for (ItemBase ib: LootTable.runeDroppedMap.keySet()){

				int dropCount = LootTable.runeDroppedMap.get(ib);
				output += ib.getName() + " : " + dropCount + newline;

			}
			break;
		case "contract":

			output =  LootTable.contractCount + " Contracts - ITEM NAME : DROP COUNT" + newline;
			for (ItemBase ib: LootTable.contractDroppedMap.keySet()){

				int dropCount = LootTable.contractDroppedMap.get(ib);
				output += ib.getName() + " : " + dropCount + newline;


			}
			break;

		case "glass":

			output =  LootTable.glassCount + " Glass - ITEM NAME : DROP COUNT" + newline;
			for (ItemBase ib: LootTable.glassDroppedMap.keySet()){

				int dropCount = LootTable.glassDroppedMap.get(ib);
				output += ib.getName() + " : " + dropCount + newline;
			}
			break;

		case "chance":
			float chance = (float)LootTable.dropCount/(float)LootTable.rollCount * 100;
			output = LootTable.dropCount + " out of " + LootTable.rollCount + " items Dropped. chance = " + chance + '%';

			break;

		default:
			this.sendUsage(pcSender);
			this.sendHelp(pcSender);
			return;
		}

		this.throwbackInfo(pcSender, output);


	}

	@Override
	protected String _getUsageString() {
        return "' /mbdrop all/resource/rune/contract/glass/chance/clear";
	}

	@Override
	protected String _getHelpString() {
        return "Lists drops for server since a reboot. All lists all items and drops. chance is the overall chance items drop from mobs on server. (not including Equipment)";
	}

}
