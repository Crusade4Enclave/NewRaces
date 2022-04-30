// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.util;

import org.pmw.tinylog.Logger;


public abstract class ThreadUtils {

	private ThreadUtils() {
	}

	/**
	 * Force the current thread to sleep for <i>sec</i> seconds and <i>ms</i>
	 * milliseconds.
	 * 
	 *
	 */
	public static void sleep(int sec, long ms) {
		try {
			Thread.sleep((1000L * sec) + ms);
		} catch (InterruptedException e) {
			Logger.error( e.toString());
		}
	}

	/**
	 * Force the current thread to sleep for <i>ms</i> milliseconds.
	 * 
	 *
	 */
	public static void sleep(long ms) {
		ThreadUtils.sleep(0, ms);
	}

}
