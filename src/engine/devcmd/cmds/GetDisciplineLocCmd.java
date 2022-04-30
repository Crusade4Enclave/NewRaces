// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.objects.*;

import java.util.ArrayList;

/**
 * @author
 *
 */
public class GetDisciplineLocCmd extends AbstractDevCmd {

	public GetDisciplineLocCmd() {
        super("getdiscloc");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		System.out.println("MOB UUID , MOB NAME  , MACRO ZONE NAME   , MOB LOCATION, DROPPED ITEM, DROP CHANCE");

		for (Zone zone: ZoneManager.getAllZones()){
			for (Mob mob: zone.zoneMobSet){

				if (mob.getLevel() >= 80)
					continue;

				ArrayList<SpecialLoot> specialLootList = SpecialLoot.LootMap.get(mob.getLootSet());


				if (specialLootList != null)
					for (SpecialLoot specialLoot: specialLootList){


						ItemBase itemBase = ItemBase.getItemBase(specialLoot.getItemID());
						System.out.println(mob.getObjectUUID() + " : " + mob.getName() + " :  " + (mob.getParentZone().isMacroZone() ? mob.getParentZone().getName() : mob.getParentZone().getParent().getName()) + " , "   + mob.getLoc().toString2D() + " , " + itemBase.getName() + " , " + specialLoot.getDropChance() + '%');
					}
			}
		}
	}

	@Override
	protected String _getHelpString() {
		return "Enchants an item with a prefix and suffix";
	}

	@Override
	protected String _getUsageString() {
		return "' /enchant clear/Enchant1 Enchant2 Enchant3 ...'";
	}

}
