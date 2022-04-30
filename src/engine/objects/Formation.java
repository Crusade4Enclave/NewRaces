// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.math.Vector3f;

public class Formation {

	// Offsets are as follows.
	// X determines left/right offset
	// Y not used
	// Z determines front/back offset

	private static final Vector3f[] COLUMN = { new Vector3f(0, 0, 0), // Group
																		// Lead
			new Vector3f(6, 0, 0), // Player 1 offset
			new Vector3f(0, 0, -6), // Player 2 offset
			new Vector3f(6, 0, -6), // Player 3 offset
			new Vector3f(0, 0, -12), // Player 4 offset
			new Vector3f(6, 0, -12), // Player 5 offset
			new Vector3f(0, 0, -18), // Player 6 offset
			new Vector3f(6, 0, -18), // Player 7 offset
			new Vector3f(0, 0, -24), // Player 8 offset
			new Vector3f(6, 0, -24) }; // Player 9 offset

	private static final Vector3f[] LINE = { new Vector3f(0, 0, 0),
			new Vector3f(0, 0, -6), new Vector3f(0, 0, -12),
			new Vector3f(0, 0, -18), new Vector3f(0, 0, -24),
			new Vector3f(0, 0, -30), new Vector3f(0, 0, -36),
			new Vector3f(0, 0, -42), new Vector3f(0, 0, -48),
			new Vector3f(0, 0, -54) };

	private static final Vector3f[] BOX = { new Vector3f(0, 0, 0),
			new Vector3f(-6, 0, 0), new Vector3f(6, 0, 0),
			new Vector3f(-6, 0, -6), new Vector3f(0, 0, -6),
			new Vector3f(6, 0, -6), new Vector3f(-6, 0, -12),
			new Vector3f(0, 0, -12), new Vector3f(5, 0, -12),
			new Vector3f(0, 0, -18) };

	private static final Vector3f[] TRIANGLE = { new Vector3f(0, 0, 0),
			new Vector3f(-6, 0, -6), new Vector3f(6, 0, -6),
			new Vector3f(-12, 0, -12), new Vector3f(0, 0, -12),
			new Vector3f(12, 0, -12), new Vector3f(-18, 0, -18),
			new Vector3f(-6, 0, -18), new Vector3f(6, 0, -18),
			new Vector3f(18, 0, -18) };

	private static final Vector3f[] CIRCLE = { new Vector3f(0, 0, 0),
			new Vector3f(-12, 0, -3), new Vector3f(12, 0, -3),
			new Vector3f(-18, 0, -12), new Vector3f(18, 0, -12),
			new Vector3f(-18, 0, -21), new Vector3f(18, 0, -21),
			new Vector3f(-12, 0, -30), new Vector3f(12, 0, -30),
			new Vector3f(0, 0, -33) };

	private static final Vector3f[] RANKS = { new Vector3f(0, 0, 0),
			new Vector3f(0, 0, -6), new Vector3f(-6, 0, 0),
			new Vector3f(-6, 0, -6), new Vector3f(6, 0, 0),
			new Vector3f(6, 0, -6), new Vector3f(-12, 0, 0),
			new Vector3f(-12, 0, -6), new Vector3f(12, 0, 0),
			new Vector3f(12, 0, -6) };

	private static final Vector3f[] WEDGE = { new Vector3f(0, 0, 0),
			new Vector3f(6, 0, 0), new Vector3f(-6, 0, -6),
			new Vector3f(12, 0, -6), new Vector3f(-12, 0, -12),
			new Vector3f(18, 0, -12), new Vector3f(-18, 0, -18),
			new Vector3f(24, 0, -18), new Vector3f(-24, 0, -24),
			new Vector3f(30, 0, -24) };

	private static final Vector3f[] INVERSEWEDGE = { new Vector3f(0, 0, 0),
			new Vector3f(6, 0, 0), new Vector3f(-6, 0, 6),
			new Vector3f(12, 0, 6), new Vector3f(-12, 0, 12),
			new Vector3f(18, 0, 12), new Vector3f(-18, 0, 18),
			new Vector3f(24, 0, 18), new Vector3f(-24, 0, 24),
			new Vector3f(30, 0, 24) };

	private static final Vector3f[] T = { new Vector3f(0, 0, 0),
			new Vector3f(-6, 0, 0), new Vector3f(6, 0, 0),
			new Vector3f(0, 0, -6), new Vector3f(-12, 0, 0),
			new Vector3f(12, 0, 0), new Vector3f(0, 0, -12),
			new Vector3f(-18, 0, 0), new Vector3f(18, 0, 0),
			new Vector3f(0, 0, -18) };

	public static Vector3f getOffset(int formation, int position) {
		if (position > 9 || position < 0) {
			// TODO log error here
			position = 0;
		}

		switch (formation) {
		case 0:
			return Formation.COLUMN[position];
		case 1:
			return Formation.LINE[position];
		case 2:
			return Formation.BOX[position];
		case 3:
			return Formation.TRIANGLE[position];
		case 4:
			return Formation.CIRCLE[position];
		case 5:
			return Formation.RANKS[position];
		case 6:
			return Formation.WEDGE[position];
		case 7:
			return Formation.INVERSEWEDGE[position];
		case 9:
			return Formation.T[position];
		default: // default to box
			return Formation.BOX[position];
		}
	}
}
