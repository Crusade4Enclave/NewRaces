// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.DbManager;
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.NPC;
import engine.objects.PlayerCharacter;

/**
 *
 * @author Eighty
 *
 */
public class RenameCmd extends AbstractDevCmd {

	public RenameCmd() {
        super("rename");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		if (args.length < 1) {
			this.sendUsage(pcSender);
			return;
		}
		if (args[0].isEmpty()) {
			throwbackError(pcSender, "Invalid rename Command. must specify a name.");
			return;
		}
		NPC npc = null;
		Building building = null;
		
		if (target != null) {
			if (target instanceof NPC)
				npc = (NPC) target;
			else if (target instanceof Building)
				building = (Building)target;
		} else
			npc = getTargetAsNPC(pcSender);
		if (npc != null) {
			DbManager.NPCQueries.SET_PROPERTY(npc, "npc_name", args[0]);
			String name = args[0];
			name = name.replaceAll("_", " ");
			
			npc.setName(name);

			this.setResult(String.valueOf(npc.getDBID()));

//			npc.updateDatabase();
			WorldGrid.updateObject(npc, pcSender);
		} else if (building != null){
			String name = args[0];
			name = name.replaceAll("_", " ");
			building.setName(name);
		}
			throwbackError(pcSender, "Invalid rename Command. must target an npc.");
	}

	@Override
	protected String _getUsageString() {
        return "' /rename npcName'";
	}

	@Override
	protected String _getHelpString() {
        return "Renames an NPC.";
	}

}
