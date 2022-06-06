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

	// no need to recompile these each call, put them in object scope and
	// compile just once.
	private static final Pattern lastNameRegex = Pattern
			.compile("^[A-Za-z][-'A-Za-z\\x20]*$");
	private static final Pattern firstNameRegex = Pattern
			.compile("^[A-Za-z]+$");

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

	public static String getCallingMethodName() {
		StackTraceElement e[] = Thread.currentThread().getStackTrace();
		int numElements = e.length;

		if (numElements < 1) {
			return "NoStack";
		}

		if (numElements == 1) {
			return e[0].getMethodName();
		} else if (numElements == 2) {
			return e[1].getMethodName();
		} else if (numElements == 3) {
			return e[2].getMethodName();
		} else {
			return e[3].getMethodName();
		}
	}

	public static String getCallStackAsString() {
		String out = "";

		StackTraceElement e[] = Thread.currentThread().getStackTrace();
		int numElements = e.length;

		for (int i = (numElements - 1); i > 1; --i) {

			String[] classStack = e[i].getClassName().split("\\.");
			String methName = e[i].getMethodName();

			String className = classStack[classStack.length - 1];

			if (methName.equals("<init>")) {
				methName = className;
			}

			out += className + '.' + methName + "()";

			if (i > 2) {
				out += " -> ";
			}

		}

		return out;
	}

}
