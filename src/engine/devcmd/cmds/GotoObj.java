// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.Enum;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.DbManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;

public class GotoObj extends AbstractDevCmd {

	public GotoObj() {
        super("gotoobj");
    }

	@Override
	protected void _doCmd(PlayerCharacter player, String[] words,
			AbstractGameObject target) {

		int uuid;
		Vector3fImmutable targetLoc =  Vector3fImmutable.ZERO;
		Enum.DbObjectType objectType;

		try {
			uuid = Integer.parseInt(words[0]);
		} catch (NumberFormatException e) {
			this.throwbackError(player, "Failed to parse UUID" + e.toString());
			return;
		}

		objectType = DbManager.BuildingQueries.GET_UID_ENUM(uuid);

		switch (objectType) {

			case NPC:
				NPC npc = (NPC) DbManager.getFromCache(Enum.GameObjectType.NPC, uuid);

				if (npc != null)
					targetLoc = npc.getLoc();
					break;
			case MOB:
				Mob mob = (Mob) DbManager.getFromCache(Enum.GameObjectType.Mob, uuid);

				if (mob != null)
					targetLoc = mob.getLoc();
				break;
			case CHARACTER:
				PlayerCharacter playerCharacter = (PlayerCharacter) DbManager.getFromCache(Enum.GameObjectType.PlayerCharacter, uuid);

				if (playerCharacter != null)
					targetLoc = playerCharacter.getLoc();
				break;
			case BUILDING:
				Building building = (Building) DbManager.getFromCache(Enum.GameObjectType.Building, uuid);

				if (building != null)
					targetLoc = building.getLoc();
				break;
			case ZONE:
				Zone zone = (Zone) DbManager.getFromCache(Enum.GameObjectType.Zone, uuid);

				if (zone != null)
					targetLoc = zone.getLoc();
				break;
			case CITY:
				City city = (City) DbManager.getFromCache(Enum.GameObjectType.City, uuid);

				if (city != null)
					targetLoc = city.getLoc();
				break;
		}
		// Teleport player

		if (targetLoc == Vector3fImmutable.ZERO) {
			this.throwbackError(player, "Failed to locate UUID");
			return;
		}

		player.teleport(targetLoc);

	}

	@Override
	protected String _getHelpString() {
        return "Teleports player to a UUID";
	}

	@Override
	protected String _getUsageString() {
		return "' /gotoobj <UID>'";

	}

}
