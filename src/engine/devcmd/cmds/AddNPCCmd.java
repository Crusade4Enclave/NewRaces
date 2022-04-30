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
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.objects.*;
import org.pmw.tinylog.Logger;

/**
 * @author Eighty
 *
 */
public class AddNPCCmd extends AbstractDevCmd {

	public AddNPCCmd() {
        super("npc");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		int contractID;
		String name = "";
		int level = 0;

		if (words.length < 2) {
			this.sendUsage(pc);
			return;
		}
		try {
			contractID = Integer.parseInt(words[0]);
			level = Integer.parseInt(words[1]);

			for (int i = 2; i < words.length; i++) {
				name += words[i];
				if (i + 1 < words.length)
					name += "";
			}

		} catch (NumberFormatException e) {
			throwbackError(pc,
					"Failed to parse supplied contractID or level to an Integer.");
			return; // NaN
		}

		Contract contract = DbManager.ContractQueries.GET_CONTRACT(contractID);

		if (contract == null || level < 1 || level > 75) {
			throwbackError(pc,
					"Invalid addNPC Command. Need contract ID, and level");
			return; // NaN
		}

		// Pick a random name
		if (name.isEmpty())
			name = NPC.getPirateName(contract.getMobbaseID());

		Zone zone = ZoneManager.findSmallestZone(pc.getLoc());

		if (zone == null) {
			throwbackError(pc, "Failed to find zone to place npc in.");
			return;
		}

		if (target != null)
			if (target.getObjectType() == GameObjectType.Building){
				Building parentBuilding = (Building)target;
				BuildingManager.addHirelingForWorld(parentBuilding, pc, parentBuilding.getLoc(), parentBuilding.getParentZone(), contract, level);
				return;
			}

		NPC npc = NPC.createNPC(name, contractID,
				pc.getLoc(), null, true, zone, (short)level, true, null);

		if (npc != null) {
			WorldGrid.addObject(npc, pc);
			ChatManager.chatSayInfo(pc,
					"NPC with ID " + npc.getDBID() + " added");
			this.setResult(String.valueOf(npc.getDBID()));
		} else {
			throwbackError(pc, "Failed to create npc of contract type "
					+ contractID);
			Logger.error(
					"Failed to create npc of contract type " + contractID);
		}
	}

	@Override
	protected String _getHelpString() {
		return "Creates an NPC of type 'npcID' at the location your character is standing";
	}

	@Override
	protected String _getUsageString() {
		return "' /npc npcID level name'";
	}

}
