// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.MobBase;
import engine.objects.NPC;
import engine.objects.PlayerCharacter;

public class SetNpcMobbaseCmd extends AbstractDevCmd {

	public SetNpcMobbaseCmd() {
        super("setmobbase");
        this.addCmdString("npcmobbase");
    }

	@Override
	protected void _doCmd(PlayerCharacter player, String[] words,
			AbstractGameObject target) {

		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(player);
			return;
		}

		if (target.getObjectType() != GameObjectType.NPC){
			this.sendUsage(player);
			return;
		}
		
		NPC npc = (NPC)target;
		
		int mobBaseID = Integer.parseInt(words[0]);
		
		if (MobBase.getMobBase(mobBaseID) == null){
			this.throwbackError(player, "Cannot find Mobbase for ID " + mobBaseID);
			return;
		}
		NPC.UpdateRaceID(npc, mobBaseID);
		
		WorldGrid.updateObject(npc);

	}

	@Override
	protected String _getHelpString() {
		return "Sets mobbase override for an NPC";
	}

	@Override
	protected String _getUsageString() {
		return "' /setmobbase mobBaseID'";
	}

}
