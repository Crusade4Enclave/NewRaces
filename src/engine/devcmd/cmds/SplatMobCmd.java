package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.Mob;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author
 * Summary: Game designer utility command to create multiple
 *        mobiles of a given UUID within a supplied range
 */

public class SplatMobCmd extends AbstractDevCmd {

	// Instance variables

	private int _mobileUUID;
	private int _mobileCount;
	private float _targetRange;
	private Vector3fImmutable _currentLocation;

	// Concurrency support

	private ReadWriteLock lock = new ReentrantReadWriteLock();

	// Constructor

	public SplatMobCmd() {
        super("splatmob");
    }


	// AbstractDevCmd Overridden methods

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] args,
			AbstractGameObject target) {

		// Member variables

		Vector3fImmutable mobileLocation;
		Mob mobile;
		Zone serverZone;

		// Concurrency write lock due to instance variable usage

		lock.writeLock().lock();

		try {

			// Validate user input

			if(validateUserInput(args) == false) {
				this.sendUsage(pc);
				return;
			}

			// Parse user input

			parseUserInput(args);

			// Arguments have been validated and parsed at this point
			// Begin creating mobiles

			_currentLocation = pc.getLoc();
			serverZone = ZoneManager.findSmallestZone(_currentLocation);

			for(int i=0;i<_mobileCount;i++) {

				mobile = Mob.createMob(_mobileUUID,
						Vector3fImmutable.getRandomPointInCircle(_currentLocation, _targetRange),
						null, true, serverZone,null,0);

				if (mobile != null) {
					mobile.updateDatabase();
				}
			}

		} // End Try Block

		// Release Reentrant lock

		finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	protected String _getHelpString() {
        return "Spawns multiple mobiles with a given range";
	}

	@Override
	protected String _getUsageString() {
        return "/splatmob UUID [Count <= 100] [range <= 1200]";
	}

	// Class methods

	private static boolean validateUserInput(String[] userInput) {

		// incorrect number of arguments test

		if (userInput.length != 3)
			return false;

		// Test for UUID conversion to int

		try {
			Integer.parseInt(userInput[0]); }
		catch (NumberFormatException | NullPointerException e) {
			return false;
		}


		// Test for Number of Mobs conversion to int

		try {
			Integer.parseInt(userInput[1]); }
		catch (NumberFormatException | NullPointerException e) {
			return false;
		}

		// Test if range argument can convert to a float

		try {
			Float.parseFloat(userInput[2]); }
		catch (NumberFormatException | NullPointerException e) {
			return false;
		}

		return true;
	}

	private void parseUserInput(String[] userInput) {

		// Clear previous values

		_mobileUUID = 0;
		_mobileCount = 0;
		_targetRange = 0f;

		// Parse first argument into mobile UID.

		_mobileUUID = Integer.parseInt(userInput[0]);

		// Parse second argument into mobile count. Cap at 100 mobs.

		_mobileCount = Integer.parseInt(userInput[1]);
		_mobileCount = Math.min(_mobileCount, 100);

		// Parse third argument into range. Cap at 200 units.

		_targetRange = Float.parseFloat(userInput[2]);
		_targetRange = Math.min(_targetRange, 1200f);

	}

}