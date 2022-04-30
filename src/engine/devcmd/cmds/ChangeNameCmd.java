// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.util.MiscUtils;

public class ChangeNameCmd extends AbstractDevCmd {

	public ChangeNameCmd() {
		super("changename");
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		Vector3fImmutable loc = null;

		// Arg Count Check
		if (words.length < 2) {
			this.sendUsage(pc);
			return;
		}

		String oldFirst = words[0];
		String newFirst = words[1];
		String newLast = "";
		if (words.length > 2) {
			newLast = words[2];
			for (int i=3; i<words.length; i++)
				newLast += ' ' + words[i];
		}

		//verify new name length
		if (newFirst.length() < 3) {
			this.throwbackError(pc, "Error: First name is incorrect length. Must be between 3 and 15 characters");
			return;
		}

		//verify old name length
		if (newLast.length() > 50) {
			this.throwbackError(pc, "Error: Last name is incorrect length. Must be no more than 50 characters");
			return;
		}

		// Check if firstname is valid
		if (MiscUtils.checkIfFirstNameInvalid(newFirst)) {
			this.throwbackError(pc, "Error: First name is not allowed");
			return;
		}


		//get the world ID we're modifying for

		//test if first name is unique, unless new and old first name are equal.
		if (!(oldFirst.equals(newFirst))) {
			if (!DbManager.PlayerCharacterQueries.IS_CHARACTER_NAME_UNIQUE(newFirst)) {
				this.throwbackError(pc, "Error: First name is not unique.");
				return;
			}
		}

		//tests passed, update name in database
		if (!DbManager.PlayerCharacterQueries.UPDATE_NAME(oldFirst, newFirst, newLast)) {
			this.throwbackError(pc, "Error: Database failed to update the name.");
			return;
		}

		//Finally update player ingame
		PlayerCharacter pcTar = null;
		try {
			pcTar = SessionManager
					.getPlayerCharacterByLowerCaseName(words[0]);
			pcTar.setFirstName(newFirst);
			pcTar.setLastName(newLast);
			this.setTarget(pcTar); //for logging

			//specify if last name is ascii characters only
			String lastAscii =  newLast.replaceAll("[^\\p{ASCII}]", "");
			pcTar.setAsciiLastName(lastAscii.equals(newLast));
		} catch (Exception e) {
			this.throwbackError(pc, "Database was updated but ingame character failed to update.");
			return;
		}

		String out = oldFirst + " was changed to " + newFirst + (newLast.isEmpty() ? "." : (' ' + newLast + '.'));
		this.throwbackInfo(pc, out);


	}

	@Override
	protected String _getHelpString() {
		return "Changes the name of a player";
	}

	@Override
	protected String _getUsageString() {
		return "'./changename oldFirstName newFirstName newLastName'";
	}

}
