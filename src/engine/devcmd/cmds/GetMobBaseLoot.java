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
import engine.objects.AbstractGameObject;
import engine.objects.Mob;
import engine.objects.MobLootBase;
import engine.objects.PlayerCharacter;

/**
 * @author Eighty
 *
 */
public class GetMobBaseLoot extends AbstractDevCmd {

	public GetMobBaseLoot() {
        super("mobbaseloot");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		if (target.getObjectType() != GameObjectType.Mob){
			this.throwbackError(pc, "Must be targeting a Valid Mob For this Command.");
			return;
		}


		Mob mob = (Mob)target;
		for (MobLootBase mlb : MobLootBase.MobLootSet.get(mob.getMobBase().getLoadID())){

			this.throwbackInfo(pc, "LootTable11 = " + mlb.getLootTableID() + "\rn ");
			this.throwbackInfo(pc, "Chance = " + mlb.getChance() + "\rn ");

		}


	}

	@Override
	protected String _getHelpString() {
        return "Copies a Mob of type 'mobID' with optional new name";
	}

	@Override
	protected String _getUsageString() {
        return "' /mob mobID [name]'";
	}

}
