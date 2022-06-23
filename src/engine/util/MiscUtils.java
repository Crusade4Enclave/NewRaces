// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.util;

import engine.gameManager.ConfigManager;
import engine.server.MBServerStatics;

import java.util.regex.Pattern;


public class MiscUtils {

	public static boolean checkIfFirstNameInvalid(String firstName) {
		if ((firstName == null) || (firstName.length() == 0)
				|| (firstName.length() > MBServerStatics.MAX_NAME_LENGTH)
				|| (firstName.length() < MBServerStatics.MIN_NAME_LENGTH)) {
			return true;
		}
		    return (!ConfigManager.regex.get(ConfigManager.MB_LOGIN_FNAME_REGEX).matcher(firstName).matches());
	}

	public static boolean checkIfLastNameInvalid(String lastName) {
		if ((lastName != null) && (lastName.length() != 0)) {
			// make sure it's less than max length
            return lastName.length() > MBServerStatics.MAX_NAME_LENGTH;
			// first character: A-Z, a-z
			// remaining chars (optional): hyphen, apostrophe, A-Z, a-z, space
//			return (!lastNameRegex.matcher(lastName).matches());
		}
		// empty last names are fine, return false
		return false;
	}
}
