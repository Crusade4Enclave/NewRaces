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

public class EnchantCmd extends AbstractDevCmd {

	public EnchantCmd() {
        super("enchant");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		int rank = 0;
		if (words.length < 1) {
			this.sendUsage(pc);
			return;
		}


		try{
			rank = Integer.parseInt(words[0]);
		}catch(Exception e){

		}



		Item item;
		if (target == null || target instanceof Item)
			item = (Item) target;
		else {
			throwbackError(pc, "Must have an item targeted");
			return;
		}

		CharacterItemManager cim = pc.getCharItemManager();
		if (cim == null) {
			throwbackError(pc, "Unable to find the character item manager for player " + pc.getFirstName() + '.');
			return;
		}

		if (words[0].equals("clear")) {
			item.clearEnchantments();
			cim.updateInventory();
			this.setResult(String.valueOf(item.getObjectUUID()));
		} else {
			int cnt = words.length;
			for (int i=1;i<cnt;i++) {
				String enchant = words[i];
				boolean valid = true;
				for (Effect eff: item.getEffects().values()){
					if (eff.getEffectsBase().getIDString().equals(enchant)){
						throwbackError(pc,"This item already has that enchantment");
						return;
					}
				}
				if (valid) {
					item.addPermanentEnchantmentForDev(enchant, rank);
					this.setResult(String.valueOf(item.getObjectUUID()));
				} else
					throwbackError(pc, "Invalid Enchantment. Enchantment must consist of SUF-001 to SUF-328 or PRE-001 to PRE-334. Sent " + enchant + '.');
			}
			cim.updateInventory();
		}
	}

	@Override
	protected String _getHelpString() {
		return  "Enchants an item with a prefix and suffix";
	}

	@Override
	protected String _getUsageString() {
		return "' /enchant clear/Enchant1 Enchant2 Enchant3 ...'";
	}

}
