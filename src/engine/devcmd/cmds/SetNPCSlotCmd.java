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

public class SetNPCSlotCmd extends AbstractDevCmd {

	public SetNPCSlotCmd() {
        super("updateNPCSlot");
        this.addCmdString("changeslot");
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


		int slot = 0;
		try {
			slot = Integer.parseInt(words[0]);

			if (!NPC.UpdateSlot(npc, slot)){
				this.throwbackError(pc, "Failed to Update Slot");
				return;
			}

			npc.setParentZone(npc.getParentZone());
			WorldGrid.updateObject(npc);

			this.setTarget(pc); //for logging

			// Update all surrounding clients.

		} catch (NumberFormatException e) {
			this.throwbackError(pc, "Supplied data: " + words[0]
					+ " failed to parse to an Integer.");
		} catch (Exception e) {
			this.throwbackError(pc,
					"An unknown exception occurred while attempting to setSlot to "
							+ words[0]);
		}
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
