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
import engine.objects.*;

import java.util.concurrent.ConcurrentHashMap;

public class PrintEquipCmd extends AbstractDevCmd {

	public PrintEquipCmd() {
		super("printequip");
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
			if (contract != null) {
				if (contract.isTrainer())
					name = tar.getName() + ", " + contract.getName();
				else
					name = tar.getName() + " the " + contract.getName();
			}
			type = "NPC";
		}

		if (tar.getObjectType() == GameObjectType.Mob){
			Mob tarMob = (Mob)tar;
			throwbackInfo(pc, "Equip for " + type + ' ' + name + " (" + tar.getObjectUUID() + ')');
			for (int slot:tarMob.getEquip().keySet()){
				MobEquipment equip = tarMob.getEquip().get(slot);
				throwbackInfo(pc, equip.getItemBase().getUUID() +  "  :  " + equip.getItemBase().getName() + ", slot: " + slot);
			}
			return;
		}

		if (tar.getObjectType() == GameObjectType.NPC){
			NPC tarMob = (NPC)tar;
			throwbackInfo(pc, "Equip for " + type + ' ' + name + " (" + tar.getObjectUUID() + ')');
			for (int slot:tarMob.getEquip().keySet()){
				MobEquipment equip = tarMob.getEquip().get(slot);
				throwbackInfo(pc,equip.getItemBase().getUUID() +  "  :  " + equip.getItemBase().getName() + ", slot: " + slot);
			}
			return;
		}

		CharacterItemManager cim = ((AbstractCharacter)tar).getCharItemManager();
		ConcurrentHashMap<Integer, Item> list = cim.getEquipped();
		throwbackInfo(pc, "Equip for " + type + ' ' + name + " (" + tar.getObjectUUID() + ')');
		for (Integer slot : list.keySet()) {
			Item item = list.get(slot);
			throwbackInfo(pc, "    " + item.getItemBase().getName() + ", slot: " + slot);
		}
	}

	@Override
	protected String _getHelpString() {
		return "Returns the player's current equipment";
	}

	@Override
	protected String _getUsageString() {
		return "' /printequip'";
	}

}
