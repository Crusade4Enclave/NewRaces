// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.ItemType;
import engine.devcmd.AbstractDevCmd;
import engine.objects.*;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PrintInventoryCmd extends AbstractDevCmd {

	public PrintInventoryCmd() {
		super("printinventory");
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		if (target == null || (!(target instanceof AbstractCharacter) && !(target instanceof Corpse))) {
			target = pc;
		}


		String type = target.getClass().getSimpleName();
		String name = "";
		ArrayList<Item> inventory = null;
		Item gold = null;
		DecimalFormat df = new DecimalFormat("###,###,###,##0");

		if (target instanceof AbstractCharacter) {
			AbstractCharacter tar = (AbstractCharacter)target;

			name = tar.getFirstName();

			if (tar instanceof Mob) {
				Mob mob = (Mob) tar;
				MobBase mb = mob.getMobBase();
				if (mb != null) {
					name = mb.getFirstName();
				}
			} else if (tar instanceof NPC) {
				NPC npc = (NPC) tar;
				Contract contract = npc.getContract();
				if (contract != null) {
					if (contract.isTrainer()) {
						name = tar.getName() + ", " + contract.getName();
					} else {
						name = tar.getName() + " the " + contract.getName();
					}
				}
			}

			CharacterItemManager cim = tar.getCharItemManager();
			inventory = cim.getInventory();  //this list can contain gold when tar is a PC that is dead
			gold = cim.getGoldInventory();
			throwbackInfo(pc,  " Weight : " + (cim.getInventoryWeight() + cim.getEquipWeight()));
		} else if (target instanceof Corpse) {
			Corpse corpse = (Corpse) target;
			name = "of " + corpse.getName();
			inventory = corpse.getInventory();
		}

		throwbackInfo(pc, "Inventory for " + type + ' ' + name + " (" + target.getObjectUUID() + ')');

		int goldCount = 0;

		for (Item item : inventory) {
			if (item.getItemBase().getType().equals(ItemType.GOLD) == false) {
				String chargeInfo = "";
				byte chargeMax = item.getChargesMax();
				if (chargeMax > 0) {
					byte charges = item.getChargesRemaining();
					chargeInfo = " charges: " + charges + '/' + chargeMax;
				}
				throwbackInfo(pc, "    " + item.getItemBase().getName() + ", count: " + item.getNumOfItems() + chargeInfo);
			} else goldCount += item.getNumOfItems();
		}
		if (gold != null) {
			goldCount += gold.getNumOfItems();
		}

		if (goldCount > 0 || gold != null) {
			throwbackInfo(pc, "    Gold, count: " + df.format(goldCount));
		} else {
			throwbackInfo(pc, "    NULL Gold");
		}
	}

	@Override
	protected String _getHelpString() {
		return "Returns the player's current inventory";
	}

	@Override
	protected String _getUsageString() {
		return "' /printinventory'";
	}

}
