// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





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
import engine.gameManager.DbManager;
import engine.objects.AbstractGameObject;
import engine.objects.Mob;
import engine.objects.PlayerCharacter;
import engine.objects.RuneBase;

/**
 * 
 * @author Eighty
 * 
 */
public class AddMobRuneCmd extends AbstractDevCmd {

	public AddMobRuneCmd() {
        super("addmobrune");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		
		
		if(args.length != 1){
			this.sendUsage(pcSender);
			return;
		}
		
	if (target.getObjectType() != GameObjectType.Mob){
		this.throwbackError(pcSender, "Target is not a valid Mob.");
		return;
	}
	Mob mobTarget = (Mob)target;

		
		int runeID = 0;
		try{
			runeID = Integer.valueOf(args[0]);
		}catch(Exception e){
			this.throwbackInfo(pcSender, "Failed to Parse an Integer.");
			return;
		}
		
		RuneBase rune = RuneBase.getRuneBase(runeID);
		if (rune == null){
			this.throwbackError(pcSender, "Invalid Rune ID");
			return;
		}
		
		
		if (!DbManager.MobBaseQueries.ADD_MOBBASE_RUNE(mobTarget.mobBase.getObjectUUID(), runeID)){
			this.throwbackError(pcSender, "Failed to update Database");
			return;
		}
		
		mobTarget.getMobBase().updateRunes();
		
		this.throwbackInfo(pcSender, "Successfuly added rune  " + rune.getName() + " to Mobbase with UID " + mobTarget.mobBase.getObjectUUID());
		
		
		
	}

	@Override
	protected String _getUsageString() {
        return "' /visualeffect visualeffectID";
	}

	@Override
	protected String _getHelpString() {
        return "Temporarily add visual effects to Character";
	}

}
