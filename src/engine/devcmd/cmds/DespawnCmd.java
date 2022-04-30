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
import engine.objects.PlayerCharacter;

public class DespawnCmd extends AbstractDevCmd {

	public DespawnCmd() {
        super("debugmob");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		
		
		
		

		if (pc == null) {
			return;
		}

		
		
		
		
		Mob mob = null;
		
		if (target != null && target.getObjectType().equals(GameObjectType.Mob))
			mob = (Mob)target;
		
		if (mob == null)
			mob = Mob.getFromCache(Integer.parseInt(words[1]));
		
		if (mob == null)
			return;
		
		if (words[0].equalsIgnoreCase("respawn")){
			mob.respawn();
			this.throwbackInfo(pc, "Mob with ID " + mob.getObjectUUID() + " Respawned"); 
		}else if (words[0].equalsIgnoreCase("despawn")){
			mob.despawn();
			this.throwbackInfo(pc, "Mob with ID " + mob.getObjectUUID() + " Despawned");
		}
	}

	@Override
	protected String _getHelpString() {
		return "Gets distance from a target.";

	}

	@Override
	protected String _getUsageString() {
		return  "' /distance'";

	}

}
