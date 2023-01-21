// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.ProtectionState;
import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import org.pmw.tinylog.Logger;

/**
 * @author Eighty
 *
 */
public class MakeBaneCmd extends AbstractDevCmd {

	public MakeBaneCmd() {
        super("makebane");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		if (words.length < 1 || words.length > 2) {
			this.sendUsage(pc);
			return;
		}

		int attackerID = 0;
		int rank = 8;

		if (words.length == 2) {
			try {
				attackerID = Integer.parseInt(words[0]);
				rank = Integer.parseInt(words[1]);
			} catch (NumberFormatException e) {
				throwbackError(pc, "AttackerGuildID must be a number, " + words[0] + " is invalid");
				return;
			}
		} else if (words.length == 1) {
			if (target == null) {
				throwbackError(pc, "No target specified");
				return;
			}

			if (!(target instanceof PlayerCharacter)) {
				throwbackError(pc, "Target is not a player");
				return;
			}
			attackerID = target.getObjectUUID();

			try {
				rank = Integer.parseInt(words[0]);
			} catch (NumberFormatException e) {
				throwbackError(pc, "Rank must be specified, 1 through 8");
				return;
			}
		}

		if (rank < 1 || rank > 8) {
			throwbackError(pc, "Rank must be 1 through 8");
			return;
		}

		PlayerCharacter player = PlayerCharacter.getPlayerCharacter(attackerID);


		

		if (player.getGuild().isEmptyGuild()) {
			throwbackError(pc, "Errant's can not place banes");
			return;
		}

		AbstractCharacter attackerAGL = Guild.GetGL(player.getGuild());

		if (attackerAGL == null) {
			throwbackError(pc, "No guild leader found for attacking guild.");
			return;
		}

		if (!(attackerAGL instanceof PlayerCharacter)) {
			throwbackError(pc, "Attacking guild leader is an NPC.");
			return;
		}

		if (player.getGuild().isNPCGuild()) {
			throwbackError(pc, "The guild used is an npc guild. They can not bane.");
			return;
		}

		//		if (player.getGuild().getOwnedCity() != null) {
		//			throwbackError(pc, "The attacking guild already has a city.");
		//			return;
		//		}

		Zone zone = ZoneManager.findSmallestZone(pc.getLoc());

		if (zone == null) {
			throwbackError(pc, "Unable to find the zone you're in.");
			return;
		}

		if (!zone.isPlayerCity()) {
			throwbackError(pc, "This is not a player city.");
			return;
		}

		City city = City.getCity(zone.getPlayerCityUUID());
		if (city == null) {
			throwbackError(pc, "Unable to find the city associated with this zone.");
			return;
		}

		if (city.getTOL() == null) {
			throwbackError(pc, "Unable to find the tree of life for this city.");
			return;
		}

		if (city.getBane() != null) {
			throwbackError(pc, "This city is already baned.");
			return;
		}

		if (Bane.getBaneByAttackerGuild(player.getGuild()) != null) {
			throwbackError(pc, "This guild is already baning someone.");
			return;
		}

		Blueprint blueprint = Blueprint.getBlueprint(24300);

		if (blueprint == null) {
			throwbackError(pc, "Unable to find building set for banestone.");
			return;
		}

		Vector3f rot = new Vector3f(0, 0, 0);

		// *** Refactor : Overlap test goes here

		//Let's drop a banestone!
		Vector3fImmutable localLocation = ZoneManager.worldToLocal(pc.getLoc(), zone);

		if (localLocation == null){
			ChatManager.chatSystemError(pc, "Failed to convert world location to zone location. Contact a CCR.");
			Logger.info("Failed to Convert World coordinates to local zone coordinates");
			return;
		}

		Building stone = DbManager.BuildingQueries.CREATE_BUILDING(
				zone.getObjectUUID(), pc.getObjectUUID(), blueprint.getName(), blueprint.getBlueprintUUID(),
				localLocation, 1.0f, blueprint.getMaxHealth(rank), ProtectionState.PROTECTED, 0, rank,
				null, blueprint.getBlueprintUUID(), 1, 0.0f);

		if (stone == null) {
			ChatManager.chatSystemError(pc, "Failed to create banestone.");
			return;
		}
		stone.addEffectBit((1 << 19));
		stone.setRank((byte) rank);
		stone.setMaxHitPoints( blueprint.getMaxHealth(stone.getRank()));
		stone.setCurrentHitPoints(stone.getMaxHitPoints());
		BuildingManager.setUpgradeDateTime(stone, null, 0);

		//Make the bane

		Bane bane = Bane.makeBane(player, city, stone);

		if (bane == null) {

			//delete bane stone, failed to make bane object
			DbManager.BuildingQueries.DELETE_FROM_DATABASE(stone);

			throwbackError(pc, "Failed to create bane.");
			return;
		}

		WorldGrid.addObject(stone, pc);

		//Add baned effect to TOL
		city.getTOL().addEffectBit((1 << 16));
		city.getTOL().updateEffects();

		Vector3fImmutable movePlayerOutsideStone = player.getLoc();
		movePlayerOutsideStone = movePlayerOutsideStone.setX(movePlayerOutsideStone.x + 10);
		movePlayerOutsideStone = movePlayerOutsideStone.setZ(movePlayerOutsideStone.z + 10);
		player.teleport(movePlayerOutsideStone);

		throwbackInfo(pc, "The city has been succesfully baned.");
	}

	@Override
	protected String _getHelpString() {
        return "Creates an bane.";
	}

	@Override
	protected String _getUsageString() {
        return "'./makebane playerUUID baneRank'";
	}

}
