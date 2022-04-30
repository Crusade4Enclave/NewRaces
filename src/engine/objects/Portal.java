package engine.objects;

import engine.Enum.RunegateType;
import engine.InterestManagement.WorldGrid;
import engine.gameManager.ConfigManager;
import engine.job.JobScheduler;
import engine.jobs.CloseGateJob;
import engine.math.Vector3fImmutable;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.HashSet;

/* A Runegate object holds an array of these
 * portals.  This class controls their triggers
 * and visual effects.
 */

public class Portal {

	private boolean active;
	private RunegateType sourceGateType;
	private RunegateType portalType;
	private RunegateType destinationGateType;
	private final Vector3fImmutable portalLocation;
	private long lastActive = 0;

	public Portal(RunegateType gateType, RunegateType portalType, RunegateType destinationGate) {

		Building gateBuilding;

		this.active = false;
		this.sourceGateType = gateType;
		this.destinationGateType = destinationGate;
		this.portalType = portalType;

		gateBuilding = this.sourceGateType.getGateBuilding();

		if (gateBuilding == null) {
			Logger.error("Gate building " + this.sourceGateType.getGateUUID() + " for " + this.sourceGateType.name() + " missing");
		}

		this.portalLocation = gateBuilding.getLoc().add(new Vector3fImmutable(portalType.getOffset().x, 6, portalType.getOffset().y));
	}

	public boolean isActive() {

		return this.active;

	}

	public void deactivate() {

		Building sourceBuilding;

		// Remove effect bit from the runegate building, which turns off this
		// portal type's particle effect

		sourceBuilding = this.sourceGateType.getGateBuilding();
        sourceBuilding.removeEffectBit(portalType.getEffectFlag());
		this.active = false;
		sourceBuilding.updateEffects();
	}

	public void activate(boolean autoClose) {

		Building sourceBuilding;


		// Apply  effect bit to the runegate building, which turns on this
		// portal type's particle effect

		sourceBuilding = this.sourceGateType.getGateBuilding();
        sourceBuilding.addEffectBit(portalType.getEffectFlag());
		this.lastActive = System.currentTimeMillis();
		this.active = true;

		// Do not update effects at bootstrap as it
		// tries to send a dispatch.

		if (ConfigManager.worldServer.isRunning == true)
			sourceBuilding.updateEffects();

		if (autoClose == true) {
            CloseGateJob cgj = new CloseGateJob(sourceBuilding, portalType);
			JobScheduler.getInstance().scheduleJob(cgj, MBServerStatics.RUNEGATE_CLOSE_TIME);
		}
	}

	public void collide() {

		HashSet<AbstractWorldObject> playerList;

		playerList = WorldGrid.getObjectsInRangePartial(this.portalLocation, 2, MBServerStatics.MASK_PLAYER);

		if (playerList.isEmpty())
			return;

		for (AbstractWorldObject player : playerList) {

			onEnter((PlayerCharacter) player);

		}
	}

	public void onEnter(PlayerCharacter player) {

		if (player.getTimeStamp("lastMoveGate") < this.lastActive)
			return;
		Building gateBuilding;

        gateBuilding = destinationGateType.getGateBuilding();

        if (gateBuilding != null){
        	player.teleport(gateBuilding.getLoc());
    		player.setSafeMode();
        }
		
	}

	/**
	 * @return the sourceGateType
	 */
	public RunegateType getSourceGateType() {
		return sourceGateType;
	}

	/**
	 * @param sourceGateType the sourceGateType to set
	 */
	public void setSourceGateType(RunegateType sourceGateType) {
		this.sourceGateType = sourceGateType;
	}

	/**
	 * @return the portalType
	 */
	public RunegateType getPortalType() {
		return portalType;
	}

	/**
	 * @param portalType the portalType to set
	 */
	public void setPortalType(RunegateType portalType) {
		this.portalType = portalType;
	}

	/**
	 * @return the destinationGateType
	 */
	public RunegateType getDestinationGateType() {
		return destinationGateType;
	}

	/**
	 * @param destinationGateType the destinationGateType to set
	 */
	public void setDestinationGateType(RunegateType destinationGateType) {
		this.destinationGateType = destinationGateType;
	}

	/**
	 * @return the portalLocation
	 */
	public Vector3fImmutable getPortalLocation() {
		return portalLocation;
	}
}
