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
import engine.gameManager.PowersManager;
import engine.objects.AbstractGameObject;
import engine.objects.Mob;
import engine.objects.PlayerCharacter;
import engine.powers.PowersBase;

/**
 * 
 * @author Eighty
 * 
 */
public class AddMobPowerCmd extends AbstractDevCmd {

	public AddMobPowerCmd() {
        super("addmobpower");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		
	
		if(args.length != 2){
			this.sendUsage(pcSender);
			return;
		}
		
	if (target.getObjectType() != GameObjectType.Mob){
		this.throwbackError(pcSender, "Target is not a valid Mob.");
		return;
	}
	Mob mobTarget = (Mob)target;

		
		int rank = 0;
		String idString = args[0];
		
		try{
			rank = Integer.valueOf(args[1]);
		}catch(Exception e){
			this.throwbackInfo(pcSender, "Failed to Parse an Integer.");
			return;
		}
		
		PowersBase pb = PowersManager.getPowerByIDString(idString);
		if (pb == null){
			this.throwbackError(pcSender, "not a valid Effect. IDString is Case Sensitive.");
			return;
		}
		
		if (!DbManager.MobBaseQueries.ADD_MOBBASE_POWER(mobTarget.getMobBaseID(), pb.getToken(), rank)){
			this.throwbackError(pcSender, "Failed to update Database");
		}
		
		mobTarget.getMobBase().updatePowers();
		
		this.throwbackInfo(pcSender, "Successfuly added Power " + pb.getIDString() + " to Mobbase with UID " + mobTarget.getMobBaseID());
		
		
	}

	@Override
	protected String _getUsageString() {
        return "' /addmobpower poweridstring rank";
	}

	@Override
	protected String _getHelpString() {
        return "Temporarily add visual effects to Character";
	}

}
