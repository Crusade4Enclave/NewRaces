// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.SessionManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.net.client.msg.RecvSummonsRequestMsg;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;

public class SummonCmd extends AbstractDevCmd {

	public SummonCmd() {
        super("summon");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] args,
			AbstractGameObject target) {
		// Arg Count Check
		if (args.length != 1) {
			this.sendUsage(pc);
			return;
		}
		PlayerCharacter pcToSummon = null;


		if (args[0].equalsIgnoreCase("all")){
			for (PlayerCharacter toSummon: SessionManager.getAllActivePlayerCharacters()){
				Zone zone = ZoneManager.findSmallestZone(pc.getLoc());
				String location = "Somewhere";
				if (zone != null)
					location = zone.getName();
				RecvSummonsRequestMsg rsrm = new RecvSummonsRequestMsg(pc.getObjectType().ordinal(), pc.getObjectUUID(), pc.getFirstName(),
						location, false);
				toSummon.getClientConnection().sendMsg(rsrm);

			}
			return;
		}
		// 1-9 numeric digits, must be playerID
		if (args[0].matches("\\d{1,9}?")) {
			try {
				int playerID = Integer.parseInt(args[0]);
				pcToSummon = SessionManager
						.getPlayerCharacterByID(playerID);

				if (pcToSummon == null) {
					this.throwbackError(pc, "Character not found by ID: "
							+ playerID);
					return;
				}
			} catch (NumberFormatException e) {
				this.throwbackError(pc, "Supplied ID: '" + args[0]
						+ "' failed to parse to an INT");
				return;

			} catch (Exception e) {
				this.throwbackError(pc,
						"An unknown exception occurred while attempting to summon '"
								+ args[0] + "'by ID");
				return;
			}

		} else { // player name
			try {
				pcToSummon = SessionManager
						.getPlayerCharacterByLowerCaseName(args[0]);
				if (pcToSummon == null) {
					this.throwbackError(pc, "Character not found by name: "
							+ args[0]);
					return;
				}
			} catch (Exception e) {
				this.throwbackError(pc,
						"An unknown exception occurred while attempting to summon '"
								+ args[0] + "'by name");
				return;
			}
		}
		this.setTarget(pcToSummon); //for logging

		Vector3fImmutable loc = pc.getLoc();
		pcToSummon.teleport(loc);

		this.throwbackInfo(pc, "Player " + pcToSummon.getCombinedName()
		+ " has been summoned to your location.");
		this.throwbackInfo(pcToSummon,
				"You have been transported to another location.");
	}

	@Override
	protected String _getHelpString() {
		return "Summons 'character' TO your current position.  Can summon by character's first name or by the character's characterID.";

	}

	@Override
	protected String _getUsageString() {
		return "' /summon characterName' || ' /summon characterID'";
	}

}
