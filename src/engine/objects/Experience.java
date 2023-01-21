// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.TargetColor;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.server.MBServerStatics;

import java.util.ArrayList;
import java.util.TreeMap;

public class Experience  {

	private static final TreeMap<Integer, Integer> ExpToLevel;
	private static final int[] LevelToExp = { Integer.MIN_VALUE, // Pad
			// everything
			// over 1

			// R0
			0, // Level 1
			150, // Level 2
			1200, // Level 3
			4050, // Level 4
			9600, // Level 5
			18750, // Level 6
			32400, // Level 7
			51450, // Level 8
			76800, // Level 9

			// R1
			109350, // Level 10
			150000, // Level 11
			199650, // Level 12
			259200, // Level 13
			329550, // Level 14
			411600, // Level 15
			506250, // Level 16
			614400, // Level 17
			736950, // Level 18
			874800, // Level 19

			// R2
			1028850, // Level 20
			1200000, // Level 21
			1389150, // Level 22
			1597200, // Level 23
			1825050, // Level 24
			2073600, // Level 25
			2343750, // Level 26
			2636400, // Level 27
			2952450, // Level 28
			3292800, // Level 29

			// R3
			3658350, // Level 30
			4050000, // Level 31
			4468650, // Level 32
			4915200, // Level 33
			5390550, // Level 34
			5895600, // Level 35
			6431250, // Level 36
			6998400, // Level 37
			7597950, // Level 38
			8230800, // Level 39

			// R4
			8897850, // Level 40
			10091520, // Level 41
			11396777, // Level 42
			12820187, // Level 43
			14368505, // Level 44
			16048666, // Level 45
			17867790, // Level 46
			19833183, // Level 47
			21952335, // Level 48
			24232919, // Level 49

			// R5
			26682793, // Level 50
			29310000, // Level 51
			32122766, // Level 52
			35129502, // Level 53
			38338805, // Level 54
			41759452, // Level 55
			45400409, // Level 56
			49270824, // Level 57
			53380030, // Level 58
			57737542, // Level 59

			// R6
			62353064, // Level 60
			67236479, // Level 61
			72397859, // Level 62
			77847457, // Level 63
			83595712, // Level 64
			89653247, // Level 65
			96030869, // Level 66
			102739569, // Level 67
			109790524, // Level 68
			117195093, // Level 69

			// R7
			124964822, // Level 70
			133111438, // Level 71
			141646855, // Level 72
			150583171, // Level 73
			159932666, // Level 74
			169707808, // Level 75
			179921247, // Level 76

	};

	private static final float[] MaxExpPerLevel = { Float.MIN_VALUE, // Pad
			// everything
			// over
			// 1

			// R0
			15, // Level 1
			105, // Level 2
			285, // Level 3
			555, // Level 4
			610, // Level 5
			682.5f, // Level 6
			730, // Level 7
			975, // Level 8
			1251.92f, // Level 9

			// R1
			1563.46f, // Level 10
			1909.62f, // Level 11
			2290.38f, // Level 12
			2705.77f, // Level 13
			3155.77f, // Level 14
			3640.38f, // Level 15
			4159.62f, // Level 16
			4713.46f, // Level 17
			5301.92f, // Level 18
			5925, // Level 19

			// R2
			6582.69f, // Level 20
			7275, // Level 21
			8001.92f, // Level 22
			8763.46f, // Level 23
			9559.62f, // Level 24
			10390.38f, // Level 25
			11255.77f, // Level 26
			12155.77f, // Level 27
			13090.38f, // Level 28
			14059.62f, // Level 29

			// R3
			15063.46f, // Level 30
			16101.92f, // Level 31
			17175, // Level 32
			18282.69f, // Level 33
			19425, // Level 34
			20601.92f, // Level 35
			21813.46f, // Level 36
			23059.62f, // Level 37
			24340.38f, // Level 38
			25655.77f, // Level 39

			// R4
			45910.38f, // Level 40
			34348.87f, // Level 41
			37458.16f, // Level 42
			40745.21f, // Level 43
			44214.76f, // Level 44
			47871.68f, // Level 45
			51720.87f, // Level 46
			55767.16f, // Level 47
			60015.37f, // Level 48
			64470.37f, // Level 49

			// R5
			69137.03f, // Level 50
			74020.16f, // Level 51
			79124.63f, // Level 52
			84455.34f, // Level 53
			90017.03f, // Level 54
			95814.66f, // Level 55
			101853.03f, // Level 56
			108137, // Level 57
			114671.37f, // Level 58
			121461.11f, // Level 59

			// R6
			128510.92f, // Level 60
			135825.79f, // Level 61
			143410.47f, // Level 62
			151269.87f, // Level 63
			159408.82f, // Level 64
			167832.16f, // Level 65
			176544.74f, // Level 66
			185551.45f, // Level 67
			194857.08f, // Level 68
			204466.55f, // Level 69

			// R7
			214384.63f, // Level 70
			224616.24f, // Level 71
			235166.21f, // Level 72
			246039.34f, // Level 73
			257240.58f, // Level 74
			1 // 268774.71 //Level 75

	};

	static {
		ExpToLevel = new TreeMap<>();

		// flip keys and values for other Map
		for (int i = 1; i < LevelToExp.length; i++) {
			ExpToLevel.put(LevelToExp[i], i);
		}
	} // end Static block

	// Used to calcuate the amount of experience a monster grants in the
	// following formula
	// expGranted = a(moblevel)^2 + b(moblevel) + c
	private static final float EXPQUADA = 10.0f;
	private static final float EXPQUADB = 6.0f;
	private static final float EXPQUADC = -10.0f;

	// Adds addtional exp per addtional member of a group using the following
	// (expGranted / group.size()) * (groupBonus * (group.size()-1) +1)
	private static final float GROUP_BONUS = 0.5f; // 0.2 grants (20%) addtional
	// exp per group member

	// called to determine current level based on xp
	public static int getLevel(int experience) {
		int expKey = ExpToLevel.floorKey(experience);
		int level = ExpToLevel.get(expKey);
		if (level > MBServerStatics.LEVELCAP) {
			level = MBServerStatics.LEVELCAP;
		}
		return level;
	}

	// Get the base xp for a level
	public static int getBaseExperience(int level) {
		if (level < LevelToExp.length) {
			return LevelToExp[level];
		}

		int fLevel = level - 1;
		int baseXP = fLevel * fLevel * fLevel;
		return (int) ((fLevel < 40) ? (baseXP * 150)
				: (baseXP * (150 + (7.6799998 * (level - 40)))));
	}

	// Get XP needed for the next level
	public static int getExpForNextLevel(int experience, int currentLevel) {
		return (getBaseExperience(currentLevel + 1) - experience);
	}

	// Max XP granted for killing a blue, yellow or orange mob
	public static float maxXPPerKill(int level) {
		if (level < 1)
			level = 1;
		if (level > 75)
			level = 75;
		return MaxExpPerLevel[level];
		// return (LevelToExp[level + 1] - LevelToExp[level])/(11 + level/2);
		// return ((((level * level)-level)*50)+16);
	}

	// Returns a penalty modifier depending on mob color
	public static double getConMod(AbstractCharacter pc, AbstractCharacter mob) {
		switch (TargetColor.getCon(pc, mob)) {
		case Red:
			return 1.25;
		case Orange:
			return 1.15;
		case Yellow:
			return 1.05;
		case Blue:
			return 1;
		case Cyan:
			return 0.8;
		case Green:
			return 0.5;
		default:
			return 0;
		}
	}

	public static double getGroupMemberPenalty(double leadership,
			PlayerCharacter currPlayer, ArrayList<PlayerCharacter> players,
			int highestLevel) {

		double penalty = 0.0;
		int adjustedGroupSize = 0;
		int totalLevels = 0;
		int level = currPlayer.getLevel();

		// Group Size Penalty
		if (players.size() > 2)
			penalty = (players.size() - 2) * 1.5;

		// Calculate Penalty For Highest level -> Current Player difference, !=
		// check to prevent divide by zero error
		if (highestLevel != level)
			penalty += ((highestLevel - level) * .5);

		// double avgLevels = totalLevels / adjustedGroupSize;
		// if (adjustedGroupSize >= 1)
		// if (level < avgLevels)
		// penalty += ((avgLevels - level) * .5);

		// Extra noob penalty
		if ((highestLevel - level) > 25)
			penalty += (highestLevel - level - 25);

		return penalty;
	}

	public static void doExperience(PlayerCharacter killer, AbstractCharacter mob, Group g) {
		// Check for some failure conditions
		if (killer == null || mob == null)
			return;

		double xp = 0.0;

		//Get the xp modifier for the world
		float xpMod = MBServerStatics.EXP_RATE_MOD;



		if (g != null) { // Do group EXP stuff

			int leadership = 0;
			int highestLevel = 0;
			double penalty = 0.0;

			ArrayList<PlayerCharacter> giveEXPTo = new ArrayList<>();

			// Check if leader is within range of kill and then get leadership
			// skill
			Vector3fImmutable killLoc = mob.getLoc();
			if (killLoc.distanceSquared2D(g.getGroupLead().getLoc()) < (MBServerStatics.EXP_RANGE * MBServerStatics.EXP_RANGE)) {
				CharacterSkill leaderskill = g.getGroupLead().skills
						.get("Leadership");
				if (leaderskill != null)
					leadership = leaderskill.getNumTrains();
				if (leadership > 90)
					leadership = 90; // leadership caps at 90%
			}

			// Check every group member for distance to see if they get xp
			for (PlayerCharacter pc : g.getMembers()) {
				if (pc.isAlive()) { // Skip if the player is dead.

					// Check within range
					if (killLoc.distanceSquared2D(pc.getLoc()) < (MBServerStatics.EXP_RANGE * MBServerStatics.EXP_RANGE)) {
						giveEXPTo.add(pc);
						// Track highest level character
						if (pc.getLevel() > highestLevel)
							highestLevel = pc.getLevel();
					}
				}
			}

			// Process every player in the group getting XP
			for (PlayerCharacter pc : giveEXPTo) {
				if (pc.getLevel() >= MBServerStatics.LEVELCAP)
					continue;

				// Sets Max XP with server exp mod taken into account.
				xp = (double) xpMod * maxXPPerKill(pc.getLevel());

				// Adjust XP for Mob Level
				xp *= getConMod(pc, mob);

				// Process XP for this member
				penalty = getGroupMemberPenalty(leadership, pc, giveEXPTo,
						highestLevel);

				// Leadership Penalty Reduction
				if (leadership > 0)
					penalty -= ((leadership) * 0.01) * penalty;

				// Modify for hotzone
				if (xp != 0)
					if (ZoneManager.inHotZone(mob.getLoc()))
						xp *= MBServerStatics.HOT_EXP_RATE_MOD;

				// Check for 0 XP due to white mob, otherwise subtract penalty
				// xp
				if (xp == 0) {
					xp = 1;
				} else {
					xp -= (penalty * 0.01) * xp;

					// Errant Penalty Calculation
					if (pc.getGuild().isEmptyGuild())
						xp *= 0.6;
				}

				if (xp == 0)
					xp = 1;

				// Grant the player the EXP
				pc.grantXP((int) Math.floor(xp));
			}

		} else { // Give EXP to a single character
			if (!killer.isAlive()) // Skip if the player is dead.
				return;

			if (killer.getLevel() >= MBServerStatics.LEVELCAP)
				return;

			// Get XP and adjust for Mob Level with world xp modifier taken into account
			xp = (double) xpMod * maxXPPerKill(killer.getLevel());
			xp *= getConMod(killer, mob);

			// Modify for hotzone
			if (ZoneManager.inHotZone(mob.getLoc()))
				xp *= MBServerStatics.HOT_EXP_RATE_MOD;

			// Errant penalty
			if (xp != 1)
				if (killer.getGuild().isEmptyGuild())
					xp *= .6;

			// Grant XP
			killer.grantXP((int) Math.floor(xp));
		}
	}
}
