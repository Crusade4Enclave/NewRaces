package engine.devcmd.cmds;

import engine.InterestManagement.WorldGrid;
import engine.ai.StaticMobActions;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;

/**
 *
 * @author
 * Dev command to move mobile and it's spawn location
 * to the player's current location
 */
public class PullCmd extends AbstractDevCmd {

	public PullCmd() {
        super("pull");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {

		Mob targetMobile;
		Vector3fImmutable targetLoc;
		Zone serverZone;

		if (validateUserInput(pcSender, target, args) == false) {
			this.sendUsage(pcSender);
			return;
		}

		targetLoc = pcSender.getLoc();
		serverZone = ZoneManager.findSmallestZone(targetLoc);
		switch (target.getObjectType()) {
		case Mob:
			MoveMobile((Mob) target, pcSender, targetLoc, serverZone);
			break;
		case Building:
			MoveBuilding((Building) target, pcSender, targetLoc, serverZone);
			break;
		case NPC:
			MoveNPC((NPC) target, pcSender, targetLoc, serverZone);
		}
	}

	@Override
	protected String _getUsageString() {
        return "/pull";
	}

	@Override
	protected String _getHelpString() {
        return "Moves mobile (and spawn) to player's location";
	}

	private boolean validateUserInput(PlayerCharacter pcSender, AbstractGameObject currTarget, String[] userInput) {

		// No target
		if (currTarget == null) {
			throwbackError(pcSender, "Requires a Mobile be targeted");
			return false;
		}
		return true;
	}

	private static void MoveMobile(Mob targetMobile, PlayerCharacter pcSender, Vector3fImmutable newLoc, Zone serverZone) {

		Vector3fImmutable localCoords;

		localCoords = ZoneManager.worldToLocal(newLoc, serverZone);

		DbManager.MobQueries.MOVE_MOB(targetMobile.getObjectUUID(), serverZone.getObjectUUID(), localCoords.x, localCoords.y, localCoords.z);
		targetMobile.setBindLoc(newLoc);
		targetMobile.setLoc(newLoc);
		StaticMobActions.refresh(targetMobile);
	}

	private static void MoveBuilding(Building targetBuilding, PlayerCharacter pcSender, Vector3fImmutable newLoc, Zone serverZone) {

		Vector3fImmutable localCoords;

		localCoords = ZoneManager.worldToLocal(newLoc, serverZone);

		DbManager.BuildingQueries.MOVE_BUILDING(targetBuilding.getObjectUUID(), serverZone.getObjectUUID(), localCoords.x, localCoords.y, localCoords.z);
		targetBuilding.setLoc(newLoc);
		targetBuilding.getBounds().setBounds(targetBuilding);
		targetBuilding.refresh(true);
	}

	private static void MoveNPC(NPC targetNPC, PlayerCharacter pcSender, Vector3fImmutable newLoc, Zone serverZone) {

		Vector3fImmutable localCoords;

		localCoords = ZoneManager.worldToLocal(newLoc, serverZone);

		DbManager.NPCQueries.MOVE_NPC(targetNPC.getObjectUUID(), serverZone.getObjectUUID(), localCoords.x, localCoords.y, localCoords.z);
		targetNPC.setBindLoc(newLoc);
		targetNPC.setLoc(newLoc);
		WorldGrid.updateObject(targetNPC, pcSender);
	}
}
