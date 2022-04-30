// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.objects.AbstractGameObject;
import engine.objects.City;
import engine.objects.PlayerCharacter;
import engine.objects.Runegate;
import org.pmw.tinylog.Logger;

import java.util.Collection;

/*
 * This class contains all methods necessary to drive periodic
 * updates of the game simulation from the main _exec loop.
 */
public enum SimulationManager {

	SERVERHEARTBEAT;

	private static SimulationManager instance = null;

	private static final long CITY_PULSE = 2000;
	private static final long RUNEGATE_PULSE = 3000;
	private static final long UPDATE_PULSE = 1000;
	private static final long FlIGHT_PULSE = 100;

	private long _cityPulseTime = System.currentTimeMillis() + CITY_PULSE;
	private long _runegatePulseTime = System.currentTimeMillis()
			+ RUNEGATE_PULSE;
	private long _updatePulseTime = System.currentTimeMillis() + UPDATE_PULSE;
	private long _flightPulseTime = System.currentTimeMillis() + FlIGHT_PULSE;
	
	public static long HeartbeatDelta = 0;
	public static long currentHeartBeatDelta = 0;

	private SimulationManager() {

		// don't allow instantiation.
	}

    public static String getPopulationString() {
        String outString;
        String newLine = System.getProperty("line.separator");
        outString = "[LUA_POPULATION()]" + newLine;
        outString += DbManager.CSSessionQueries.GET_POPULATION_STRING();
        return outString;
    }

    /*
	 * Update the simulation. *** Important: Whatever you do in here, do it damn
	 * quick!
	 */
	public void tick() {

		/*
		 * As we're on the main thread we must be sure to catch any possible
		 * errors.
		 *
		 * IF something does occur, disable that particular heartbeat. Better
		 * runegates stop working than the game itself!
		 */
		
		long start = System.currentTimeMillis();

		try {
			if ((_flightPulseTime != 0)
					&& (System.currentTimeMillis() > _flightPulseTime))
				pulseFlight();
		} catch (Exception e) {
			Logger.error(
					"Fatal error in City Pulse: DISABLED. Error Message : "
							+ e.getMessage());
		}
		try {

			if ((_updatePulseTime != 0)
					&& (System.currentTimeMillis() > _updatePulseTime))
				pulseUpdate();
		} catch (Exception e) {
			Logger.error(
					"Fatal error in Update Pulse: DISABLED");
			//  _runegatePulseTime = 0;
		}

		try {
			if ((_runegatePulseTime != 0)
					&& (System.currentTimeMillis() > _runegatePulseTime))
				pulseRunegates();
		} catch (Exception e) {
			Logger.error(
					"Fatal error in Runegate Pulse: DISABLED");
			_runegatePulseTime = 0;
		}

		try {
			if ((_cityPulseTime != 0)
					&& (System.currentTimeMillis() > _cityPulseTime))
				pulseCities();
		} catch (Exception e) {
			Logger.error(
					"Fatal error in City Pulse: DISABLED. Error Message : "
							+ e.getMessage());
			e.printStackTrace();
	
		}
		
		long end = System.currentTimeMillis();
		
		long delta = end - start;
		
		if (delta > SimulationManager.HeartbeatDelta)
			SimulationManager.HeartbeatDelta = delta;
		
		SimulationManager.currentHeartBeatDelta = delta;
		


	}

	/*
	 * Mainline simulation update method: handles movement and regen for all
	 * player characters
	 */

	private void pulseUpdate() {

		Collection<AbstractGameObject> playerList;

		playerList = DbManager.getList(GameObjectType.PlayerCharacter);

		// Call update() on each player in game

		if (playerList == null)
			return;

		for (AbstractGameObject ago : playerList) {
			PlayerCharacter player = (PlayerCharacter)ago;

			if (player == null)
				continue;
			player.update();
		}

        _updatePulseTime = System.currentTimeMillis() + 500;
	}
	
	private void pulseFlight() {

		Collection<AbstractGameObject> playerList;

		playerList = DbManager.getList(GameObjectType.PlayerCharacter);

		// Call update() on each player in game

		if (playerList == null)
			return;

		for (AbstractGameObject ago : playerList) {
			PlayerCharacter player = (PlayerCharacter)ago;

			if (player == null)
				continue;
			
			
			player.updateFlight();
		}

        _flightPulseTime = System.currentTimeMillis() + FlIGHT_PULSE;
	}

	private void pulseCities() {

		City city;

		// *** Refactor: Need a list cached somewhere as it doesn't change very
		// often at all.  Have a cityListIsDirty boolean that gets set if it
		// needs an update.  Will speed up this method a great deal.

		Collection<AbstractGameObject> cityList = DbManager.getList(Enum.GameObjectType.City);

		if (cityList == null) {
			Logger.info( "City List null");
			return;
		}

		for (AbstractGameObject cityObject : cityList) {
			city = (City) cityObject;
				city.onEnter();
		}

		_cityPulseTime = System.currentTimeMillis() + CITY_PULSE;
	}

	/*
	 * Method runs proximity collision detection for all active portals on the
	 * game's Runegates
	 */
	private void pulseRunegates() {

		for (Runegate runegate : Runegate.getRunegates()) {
			runegate.collidePortals();
		}

		_runegatePulseTime = System.currentTimeMillis() + RUNEGATE_PULSE;

	}
}
