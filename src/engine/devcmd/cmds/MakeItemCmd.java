// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.ItemContainerType;
import engine.Enum.ItemType;
import engine.Enum.OwnerType;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.DbManager;
import engine.objects.*;
import engine.powers.EffectsBase;

import java.util.ArrayList;

/**
 * @author Eighty
 *
 */
public class MakeItemCmd extends AbstractDevCmd {

	public MakeItemCmd() {
        super("makeitem");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		
		if (words[0].equals("resources")){
			for (int ibID : Warehouse.getMaxResources().keySet()){
				if (ibID == 7)
					continue;
				
				ItemBase ib = ItemBase.getItemBase(ibID);
				
				short weight = ib.getWeight();
				if (!pc.getCharItemManager().hasRoomInventory(weight)) {
					throwbackError(pc, "Not enough room in inventory for any more of this item");
					
						pc.getCharItemManager().updateInventory();
					return;
				}

				boolean worked = false;
				Item item = new Item(ib, pc.getObjectUUID(),
						OwnerType.PlayerCharacter, (byte)0, (byte)0, (short)ib.getDurability(), (short)ib.getDurability(),
						true, false, ItemContainerType.INVENTORY, (byte) 0,
	                    new ArrayList<>(),"");
				
					item.setNumOfItems(Warehouse.getMaxResources().get(ibID));

				try {
					item = DbManager.ItemQueries.ADD_ITEM(item);
					worked = true;
				} catch (Exception e) {
					throwbackError(pc, "DB error 1: Unable to create item. " + e.getMessage());
					return;
				}

				if (item == null || !worked) {
					throwbackError(pc, "DB error 2: Unable to create item.");
					return;
				}

				

				//add item to inventory
				pc.getCharItemManager().addItemToInventory(item);
			}
			return;
		}
		if (words.length < 3 || words.length > 5) {
			this.sendUsage(pc);
			return;
		}

		int quantity = 1;
		if (words.length > 3) {
			try {
				quantity = Integer.parseInt(words[3]);
			} catch (NumberFormatException e) {
				throwbackError(pc, "Quantity must be a number, " + words[3] + " is invalid");
				return;
			}
			if (quantity < 1 || quantity > 100)
				quantity = 1;
		}

		int numItems = 1;
		if (words.length > 4) {
			try {
				numItems = Integer.parseInt(words[4]);
			} catch (NumberFormatException e) {
				throwbackError(pc, "numResources must be a number, " + words[4] + " is invalid");
				return;
			}
			numItems = (numItems < 1) ? 1 : numItems;
			numItems = (numItems > 5000) ? 5000 : numItems;
		}

		int itembaseID;
		try {
			itembaseID = Integer.parseInt(words[0]);
		} catch (NumberFormatException e) {
			itembaseID = ItemBase.getIDByName(words[0].toLowerCase());
			if (itembaseID == 0) {
				throwbackError(pc, "Supplied type " + words[0]
						+ " failed to parse to an Integer");
				return;
			}
		} catch (Exception e) {
			throwbackError(pc,
					"An unknown exception occurred when trying to use createitem command for type "
							+ words[0]);
			return; // NaN
		}

		if (itembaseID == 7) {
			this.throwbackInfo(pc, "use /addgold to add gold.");
			return;
		}

		String prefix = "";
		String suffix = "";

		if (!(words[1].equals("0"))) {
			prefix = words[1];
			if (!(prefix.substring(0, 4).equals("PRE-")))
				prefix = EffectsBase.getItemEffectsByName(prefix.toLowerCase());
			if (!(prefix.substring(0, 4).equals("PRE-"))) {
				throwbackError(pc, "Invalid Prefix. Prefix must consist of PRE-001 to PRE-334 or 0 for no Prefix.");
				return;
			}

			boolean validInt = false;
			try {
				int num = Integer.parseInt(prefix.substring(4, 7));
				if (num > 0 && num < 335)
					validInt = true;
			} catch (Exception e) {
				throwbackError(pc, "error parsing number " + prefix);
			}
			if (!validInt) {
				throwbackError(pc, "Invalid Prefix. Prefix must consist of PRE-001 to PRE-334 or 0 for no Prefix.");
				return;
			}
		}

		if (!(words[2].equals("0"))) {
			suffix = words[2];

			if (!(suffix.substring(0, 4).equals("SUF-")))
				suffix = EffectsBase.getItemEffectsByName(suffix.toLowerCase());
			if (!(suffix.substring(0, 4).equals("SUF-"))) {
				throwbackError(pc, "Invalid Suffix. Suffix must consist of SUF-001 to SUF-328 or 0 for no Suffix.");
				return;
			}

			boolean validInt = false;
			try {
				int num = Integer.parseInt(suffix.substring(4, 7));
				if (num > 0 && num < 329)
					validInt = true;
			} catch (Exception e) {
				throwbackError(pc, "error parsing number " + suffix);
			}
			if (!validInt) {
				throwbackError(pc, "Invalid Suffix. Suffix must consist of SUF-001 to SUF-328 or 0 for no Suffix.");
				return;
			}
		}
		ItemBase ib = ItemBase.getItemBase(itembaseID);
		if (ib == null) {
			throwbackError(pc, "Unable to find itembase of ID " + itembaseID);
			return;
		}

		if ((numItems > 1)
				&& (ib.getType().equals(ItemType.RESOURCE) == false)
				&& (ib.getType().equals(ItemType.OFFERING)) == false)
			numItems = 1;

		CharacterItemManager cim = pc.getCharItemManager();
		if (cim == null) {
			throwbackError(pc, "Unable to find the character item manager for player " + pc.getFirstName() + '.');
			return;
		}

		byte charges = (byte) ib.getNumCharges();
		short dur = (short) ib.getDurability();

		String result = "";
		for (int i = 0; i < quantity; i++) {
			short weight = ib.getWeight();
			if (!cim.hasRoomInventory(weight)) {
				throwbackError(pc, "Not enough room in inventory for any more of this item. " + i + " produced.");
				if (i > 0)
					cim.updateInventory();
				return;
			}

			boolean worked = false;
			Item item = new Item(ib, pc.getObjectUUID(),
					OwnerType.PlayerCharacter, charges, charges, dur, dur,
					true, false, ItemContainerType.INVENTORY, (byte) 0,
                    new ArrayList<>(),"");
			if (numItems > 1)
				item.setNumOfItems(numItems);

			try {
				item = DbManager.ItemQueries.ADD_ITEM(item);
				worked = true;
			} catch (Exception e) {
				throwbackError(pc, "DB error 1: Unable to create item. " + e.getMessage());
				return;
			}

			if (item == null || !worked) {
				throwbackError(pc, "DB error 2: Unable to create item.");
				return;
			}

			//create prefix
			if (!prefix.isEmpty())
				item.addPermanentEnchantmentForDev(prefix, 0);

			//create suffix
			if (!suffix.isEmpty())
				item.addPermanentEnchantmentForDev(suffix, 0);

			//add item to inventory
			cim.addItemToInventory(item);
			result += " " + item.getObjectUUID();
		}
		this.setResult(result);
		cim.updateInventory();
	}

	@Override
	protected String _getHelpString() {
        return "Creates an item of type 'itembaseID' with a prefix and suffix";
	}

	@Override
	protected String _getUsageString() {
        return "'./makeitem itembaseID PrefixID SuffixID [quantity] [numResources]'";
	}

}
