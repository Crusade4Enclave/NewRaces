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
import engine.objects.NPC;
import engine.objects.PlayerCharacter;

public class SetNpcNameCmd extends AbstractDevCmd {

	public static int lastEquipSetID = 0;
	public SetNpcNameCmd() {
        super("setNPCName");
        this.addCmdString("npcname");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		if (target.getObjectType() != GameObjectType.NPC){
			this.sendUsage(pc);
			return;
		}
		
		NPC npc = (NPC)target;
		
		String name = words[0];
		
		NPC.UpdateName(npc, name);
		
		WorldGrid.updateObject(npc);

	}

	@Override
	protected String _getHelpString() {
		return "Sets slot position for an NPC to 'slot'";
	}

	@Override
	protected String _getUsageString() {
		return "' /changeslot slot'";
	}

}
