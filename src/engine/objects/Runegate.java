package engine.objects;

import engine.Enum.RunegateType;
import engine.gameManager.BuildingManager;
import engine.net.ByteBufferWriter;

import java.util.ArrayList;

/* Runegates are tied to particular buildings at
 * bootstrap.  They aren't tighly coupled, with
 * the Runegate merely toggling effect bits on it's
 * parent building.
 */

public class Runegate {

	// Runegate class Instance variables
	private static final Runegate[] _runegates = new Runegate[9];

	private final Portal[] _portals;
	private final RunegateType gateType;

	private Runegate(RunegateType gateType) {

		this._portals = new Portal[8];
		this.gateType = gateType;

		// Each Runegate has a different destination
		// for each portal opened.
		configureGatePortals();

		// Chaos, Khar and Oblivion are on by default

		_portals[RunegateType.CHAOS.ordinal()].activate(false);
		_portals[RunegateType.OBLIV.ordinal()].activate(false);
		_portals[RunegateType.MERCHANT.ordinal()].activate(false);

	}

	public void activatePortal(RunegateType gateType) {

		this._portals[gateType.ordinal()].activate(true);

	}

	public void deactivatePortal(RunegateType gateType) {

		this._portals[gateType.ordinal()].deactivate();

	}

	public RunegateType getGateType() {
		return this.gateType;
	}

	public static Runegate[] getRunegates() {
		return Runegate._runegates;
	}

	public Portal[] getPortals() {

		return this._portals;

	}

	public void collidePortals() {

		for (Portal portal : this.getPortals()) {

			if (portal.isActive())
				portal.collide();
		}
	}

	public static void loadAllRunegates() {

		for (RunegateType runegateType : RunegateType.values()) {
			_runegates[runegateType.ordinal()] = new Runegate(runegateType);
		}

	}

	public void _serializeForEnterWorld(ByteBufferWriter writer) {

		Building gateBuilding;

		gateBuilding = BuildingManager.getBuilding(this.gateType.getGateUUID());

		writer.putInt(gateBuilding.getObjectType().ordinal());
		writer.putInt(gateBuilding.getObjectUUID());
		writer.putString(gateBuilding.getParentZone().getName());
		writer.putFloat(gateBuilding.getLoc().getLat());
		writer.putFloat(gateBuilding.getLoc().getAlt());
		writer.putFloat(gateBuilding.getLoc().getLong());
	}

	private void configureGatePortals() {

		// Source gate type, portal type and destination gate type;
		switch (this.gateType) {

		case EARTH:
			_portals[RunegateType.EARTH.ordinal()] = new Portal(RunegateType.EARTH, RunegateType.EARTH, RunegateType.EARTH);
			_portals[RunegateType.AIR.ordinal()] = new Portal(RunegateType.EARTH, RunegateType.AIR, RunegateType.AIR);
			_portals[RunegateType.FIRE.ordinal()] = new Portal(RunegateType.EARTH, RunegateType.FIRE, RunegateType.FORBID);
			_portals[RunegateType.WATER.ordinal()] = new Portal(RunegateType.EARTH, RunegateType.WATER, RunegateType.WATER);
			_portals[RunegateType.SPIRIT.ordinal()] = new Portal(RunegateType.EARTH, RunegateType.SPIRIT, RunegateType.SPIRIT);
			_portals[RunegateType.CHAOS.ordinal()] = new Portal(RunegateType.EARTH, RunegateType.CHAOS, RunegateType.CHAOS);
			_portals[RunegateType.OBLIV.ordinal()] = new Portal(RunegateType.EARTH, RunegateType.OBLIV, RunegateType.OBLIV);
			_portals[RunegateType.MERCHANT.ordinal()] = new Portal(RunegateType.EARTH, RunegateType.MERCHANT, RunegateType.MERCHANT);
			break;

		case AIR:
			_portals[RunegateType.EARTH.ordinal()] = new Portal(RunegateType.AIR, RunegateType.EARTH, RunegateType.EARTH);
			_portals[RunegateType.AIR.ordinal()] = new Portal(RunegateType.AIR, RunegateType.AIR, RunegateType.FORBID);
			_portals[RunegateType.FIRE.ordinal()] = new Portal(RunegateType.AIR, RunegateType.FIRE, RunegateType.FIRE);
			_portals[RunegateType.WATER.ordinal()] = new Portal(RunegateType.AIR, RunegateType.WATER, RunegateType.WATER);
			_portals[RunegateType.SPIRIT.ordinal()] = new Portal(RunegateType.AIR, RunegateType.SPIRIT, RunegateType.SPIRIT);
			_portals[RunegateType.CHAOS.ordinal()] = new Portal(RunegateType.AIR, RunegateType.CHAOS, RunegateType.CHAOS);
			_portals[RunegateType.OBLIV.ordinal()] = new Portal(RunegateType.AIR, RunegateType.OBLIV, RunegateType.OBLIV);
			_portals[RunegateType.MERCHANT.ordinal()] = new Portal(RunegateType.AIR, RunegateType.MERCHANT, RunegateType.MERCHANT);

			break;

		case FIRE:
			_portals[RunegateType.EARTH.ordinal()] = new Portal(RunegateType.FIRE, RunegateType.EARTH, RunegateType.EARTH);
			_portals[RunegateType.AIR.ordinal()] = new Portal(RunegateType.FIRE, RunegateType.AIR, RunegateType.AIR);
			_portals[RunegateType.FIRE.ordinal()] = new Portal(RunegateType.FIRE, RunegateType.FIRE, RunegateType.FORBID);
			_portals[RunegateType.WATER.ordinal()] = new Portal(RunegateType.FIRE, RunegateType.WATER, RunegateType.WATER);
			_portals[RunegateType.SPIRIT.ordinal()] = new Portal(RunegateType.FIRE, RunegateType.SPIRIT, RunegateType.SPIRIT);
			_portals[RunegateType.CHAOS.ordinal()] = new Portal(RunegateType.FIRE, RunegateType.CHAOS, RunegateType.CHAOS);
			_portals[RunegateType.OBLIV.ordinal()] = new Portal(RunegateType.FIRE, RunegateType.OBLIV, RunegateType.OBLIV);
			_portals[RunegateType.MERCHANT.ordinal()] = new Portal(RunegateType.FIRE, RunegateType.MERCHANT, RunegateType.MERCHANT);
			break;

		case WATER:
			_portals[RunegateType.EARTH.ordinal()] = new Portal(RunegateType.WATER, RunegateType.EARTH, RunegateType.EARTH);
			_portals[RunegateType.AIR.ordinal()] = new Portal(RunegateType.WATER, RunegateType.AIR, RunegateType.AIR);
			_portals[RunegateType.FIRE.ordinal()] = new Portal(RunegateType.WATER, RunegateType.FIRE, RunegateType.FIRE);
			_portals[RunegateType.WATER.ordinal()] = new Portal(RunegateType.WATER, RunegateType.WATER, RunegateType.FORBID);
			_portals[RunegateType.SPIRIT.ordinal()] = new Portal(RunegateType.WATER, RunegateType.SPIRIT, RunegateType.SPIRIT);
			_portals[RunegateType.CHAOS.ordinal()] = new Portal(RunegateType.WATER, RunegateType.CHAOS, RunegateType.CHAOS);
			_portals[RunegateType.OBLIV.ordinal()] = new Portal(RunegateType.WATER, RunegateType.OBLIV, RunegateType.OBLIV);
			_portals[RunegateType.MERCHANT.ordinal()] = new Portal(RunegateType.WATER, RunegateType.MERCHANT, RunegateType.MERCHANT);
			break;

		case SPIRIT:
			_portals[RunegateType.EARTH.ordinal()] = new Portal(RunegateType.SPIRIT, RunegateType.EARTH, RunegateType.EARTH);
			_portals[RunegateType.AIR.ordinal()] = new Portal(RunegateType.SPIRIT, RunegateType.AIR, RunegateType.AIR);
			_portals[RunegateType.FIRE.ordinal()] = new Portal(RunegateType.SPIRIT, RunegateType.FIRE, RunegateType.FIRE);
			_portals[RunegateType.WATER.ordinal()] = new Portal(RunegateType.SPIRIT, RunegateType.WATER, RunegateType.WATER);
			_portals[RunegateType.SPIRIT.ordinal()] = new Portal(RunegateType.SPIRIT, RunegateType.SPIRIT, RunegateType.FORBID);
			_portals[RunegateType.CHAOS.ordinal()] = new Portal(RunegateType.SPIRIT, RunegateType.CHAOS, RunegateType.CHAOS);
			_portals[RunegateType.OBLIV.ordinal()] = new Portal(RunegateType.SPIRIT, RunegateType.OBLIV, RunegateType.OBLIV);
			_portals[RunegateType.MERCHANT.ordinal()] = new Portal(RunegateType.SPIRIT, RunegateType.MERCHANT, RunegateType.MERCHANT);
			break;

		case CHAOS:
			_portals[RunegateType.EARTH.ordinal()] = new Portal(RunegateType.CHAOS, RunegateType.EARTH, RunegateType.EARTH);
			_portals[RunegateType.AIR.ordinal()] = new Portal(RunegateType.CHAOS, RunegateType.AIR, RunegateType.AIR);
			_portals[RunegateType.FIRE.ordinal()] = new Portal(RunegateType.CHAOS, RunegateType.FIRE, RunegateType.FIRE);
			_portals[RunegateType.WATER.ordinal()] = new Portal(RunegateType.CHAOS, RunegateType.WATER, RunegateType.WATER);
			_portals[RunegateType.SPIRIT.ordinal()] = new Portal(RunegateType.CHAOS, RunegateType.SPIRIT, RunegateType.SPIRIT);
			_portals[RunegateType.CHAOS.ordinal()] = new Portal(RunegateType.CHAOS, RunegateType.CHAOS, RunegateType.MERCHANT);
			_portals[RunegateType.OBLIV.ordinal()] = new Portal(RunegateType.CHAOS, RunegateType.OBLIV, RunegateType.OBLIV);
			_portals[RunegateType.MERCHANT.ordinal()] = new Portal(RunegateType.CHAOS, RunegateType.MERCHANT, RunegateType.MERCHANT);
			break;

		case OBLIV:
			_portals[RunegateType.EARTH.ordinal()] = new Portal(RunegateType.OBLIV, RunegateType.EARTH, RunegateType.EARTH);
			_portals[RunegateType.AIR.ordinal()] = new Portal(RunegateType.OBLIV, RunegateType.AIR, RunegateType.AIR);
			_portals[RunegateType.FIRE.ordinal()] = new Portal(RunegateType.OBLIV, RunegateType.FIRE, RunegateType.FIRE);
			_portals[RunegateType.WATER.ordinal()] = new Portal(RunegateType.OBLIV, RunegateType.WATER, RunegateType.WATER);
			_portals[RunegateType.SPIRIT.ordinal()] = new Portal(RunegateType.OBLIV, RunegateType.SPIRIT, RunegateType.SPIRIT);
			_portals[RunegateType.CHAOS.ordinal()] = new Portal(RunegateType.OBLIV, RunegateType.CHAOS, RunegateType.CHAOS);
			_portals[RunegateType.OBLIV.ordinal()] = new Portal(RunegateType.OBLIV, RunegateType.OBLIV, RunegateType.MERCHANT);
			_portals[RunegateType.MERCHANT.ordinal()] = new Portal(RunegateType.OBLIV, RunegateType.MERCHANT, RunegateType.MERCHANT);
			break;

		case MERCHANT:
			_portals[RunegateType.EARTH.ordinal()] = new Portal(RunegateType.MERCHANT, RunegateType.EARTH, RunegateType.EARTH);
			_portals[RunegateType.AIR.ordinal()] = new Portal(RunegateType.MERCHANT, RunegateType.AIR, RunegateType.AIR);
			_portals[RunegateType.FIRE.ordinal()] = new Portal(RunegateType.MERCHANT, RunegateType.FIRE, RunegateType.FIRE);
			_portals[RunegateType.WATER.ordinal()] = new Portal(RunegateType.MERCHANT, RunegateType.WATER, RunegateType.WATER);
			_portals[RunegateType.SPIRIT.ordinal()] = new Portal(RunegateType.MERCHANT, RunegateType.SPIRIT, RunegateType.SPIRIT);
			_portals[RunegateType.CHAOS.ordinal()] = new Portal(RunegateType.MERCHANT, RunegateType.CHAOS, RunegateType.CHAOS);
			_portals[RunegateType.OBLIV.ordinal()] = new Portal(RunegateType.MERCHANT, RunegateType.OBLIV, RunegateType.OBLIV);
			_portals[RunegateType.MERCHANT.ordinal()] = new Portal(RunegateType.MERCHANT, RunegateType.MERCHANT, RunegateType.FORBID);
			break;

		case FORBID:
			_portals[RunegateType.EARTH.ordinal()] = new Portal(RunegateType.FORBID, RunegateType.EARTH, RunegateType.EARTH);
			_portals[RunegateType.AIR.ordinal()] = new Portal(RunegateType.FORBID, RunegateType.AIR, RunegateType.AIR);
			_portals[RunegateType.FIRE.ordinal()] = new Portal(RunegateType.FORBID, RunegateType.FIRE, RunegateType.FIRE);
			_portals[RunegateType.WATER.ordinal()] = new Portal(RunegateType.FORBID, RunegateType.WATER, RunegateType.WATER);
			_portals[RunegateType.SPIRIT.ordinal()] = new Portal(RunegateType.FORBID, RunegateType.SPIRIT, RunegateType.SPIRIT);
			_portals[RunegateType.CHAOS.ordinal()] = new Portal(RunegateType.FORBID, RunegateType.CHAOS, RunegateType.CHAOS);
			_portals[RunegateType.OBLIV.ordinal()] = new Portal(RunegateType.FORBID, RunegateType.OBLIV, RunegateType.OBLIV);
			_portals[RunegateType.MERCHANT.ordinal()] = new Portal(RunegateType.FORBID, RunegateType.MERCHANT, RunegateType.MERCHANT);
			break;

		}

	}
	
	public static ArrayList<String> GetAllOpenGateIDStrings(){
		ArrayList<String> openGateIDStrings = new ArrayList<>();
		
		openGateIDStrings.add("TRA-003");
		openGateIDStrings.add("TRA-004");
		openGateIDStrings.add("TRA-005");
		openGateIDStrings.add("TRA-006");
		openGateIDStrings.add("TRA-007");
		openGateIDStrings.add("TRA-008");
		openGateIDStrings.add("TRA-009");
		openGateIDStrings.add("TRA-010");
		return openGateIDStrings;
	}

}
