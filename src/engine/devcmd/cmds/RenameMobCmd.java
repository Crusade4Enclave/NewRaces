// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.objects.AbstractGameObject;
import engine.objects.MobBase;
import engine.objects.NPC;
import engine.objects.PlayerCharacter;

/**
 *
 * @author Eighty
 *
 */
public class RenameMobCmd extends AbstractDevCmd {

	public RenameMobCmd() {
        super("renamemob");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		if (args.length < 1) {
			this.sendUsage(pcSender);
			return;
		}
		int loadID = 0;
		String name = "";
		NPC npc;
		if (target != null && target instanceof NPC)
			npc = (NPC) target;
		else
			npc = getTargetAsNPC(pcSender);
		if (npc != null) {
			for (int i = 0; i < args.length; i++) {
				name += args[i];
				if (i + 1 < args.length)
					name += " ";
			}
			npc.setName(name);
			npc.updateDatabase();
			ChatManager.chatSayInfo(
					pcSender,
					"NPC with ID " + npc.getObjectUUID() + " renamed to "
							+ npc.getFirstName());
		} else {
			try {
				loadID = Integer.parseInt(args[0]);
				if (args.length > 1) {
					for (int i = 1; i < args.length; i++) {
						name += args[i];
						if (i + 1 < args.length)
							name += " ";
					}
				}
			} catch (Exception e) {
				throwbackError(pcSender,
						"Invalid renameMob Command. Need mob ID specified.");
				return; // NaN
			}
			MobBase mob = MobBase.getMobBase(loadID);
			if (mob == null) {
				throwbackError(pcSender,
						"Invalid renameMob Command. Mob ID specified is not valid.");
				return;
			}
			if (!MobBase.renameMobBase(mob.getObjectUUID(), name)) {
				throwbackError(pcSender,
						"renameMob SQL Error. Failed to rename mob.");
				return;
			}
			mob = MobBase.getMobBase(mob.getObjectUUID(), true); // force refresh
																// from db
			ChatManager.chatSayInfo(
					pcSender,
					"MobBase with ID " + mob.getObjectUUID() + " renamed to "
							+ mob.getFirstName());
		}
	}

	@Override
	protected String _getUsageString() {
        return "' /renamemob [ID] newName'";
	}

	@Override
	protected String _getHelpString() {
        return "Changes a mobs old name to a new name";
	}

}
