// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.GameObjectType;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.objects.*;
import engine.powers.effectmodifiers.AbstractEffectModifier;

public class PrintBonusesCmd extends AbstractDevCmd {

	public PrintBonusesCmd() {
		super("printbonuses");
		//		super("printbonuses", MBServerStatics.ACCESS_LEVEL_ADMIN);
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		AbstractWorldObject tar;
		String name;
		String type = "PlayerCharacter";
		if (target != null)
			if (target instanceof Item) {
				type = "Item";
				tar = (Item) target;
				name = ((Item) tar).getItemBase().getName();
			} else if (target instanceof AbstractCharacter) {
				tar = (AbstractCharacter) target;
				name = ((AbstractCharacter) tar).getFirstName();
			} else {
				tar = pc;
				name = ((AbstractCharacter) tar).getFirstName();
			}
		else {
			tar = pc;
			name = ((AbstractCharacter) tar).getFirstName();
		}

		//Get name and type
		if (tar instanceof Mob) {
			Mob mob = (Mob) tar;
			MobBase mb = mob.getMobBase();
			if (mb != null)
				name = mb.getFirstName();
			type = "Mob";
		} else if (tar instanceof NPC) {
			NPC npc = (NPC) tar;
			Contract contract = npc.getContract();
			if (contract != null)
				if (contract.isTrainer())
					name = tar.getName() + ", " + contract.getName();
				else
					name = tar.getName() + " the " + contract.getName();
			type = "NPC";
		}

		if (tar.getObjectType() == GameObjectType.Item) {
			Item targetItem = (Item) tar;

			if (targetItem.getBonuses() != null)
				for (AbstractEffectModifier targetName : targetItem.getBonuses().keySet()) {
					ChatManager.chatSystemInfo(pc, "  " + targetName.modType.name() + "-" + targetName.sourceType.name() + ": " + targetItem.getBonuses().get(name));
				}
		}	 else if (((AbstractCharacter)tar).getBonuses() != null) {
			((AbstractCharacter)tar).getBonuses().printBonusesToClient(pc);
		}
		else
			throwbackInfo(pc, "Bonuses for " + type + ' ' + name + " not found");
	}

	@Override
	protected String _getHelpString() {
		return "Returns the player's current bonuses";
	}

	@Override
	protected String _getUsageString() {
		return "' /printbonuses'";
	}

}
