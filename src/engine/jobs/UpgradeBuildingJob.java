package engine.jobs;

import engine.job.AbstractScheduleJob;
import engine.objects.Building;
import org.pmw.tinylog.Logger;

/*
 * This class handles upgrading of buildings, swapping the
 * appropriate mesh according to the building's blueprint.
 * @Author
 */
public class UpgradeBuildingJob extends AbstractScheduleJob {

	private final Building rankingBuilding;

	public UpgradeBuildingJob(Building building) {
		super();
		this.rankingBuilding = building;

	}

	@Override
	protected void doJob() {



		// Must have a building to rank!

		if (rankingBuilding == null) {
			Logger.error("Attempting to rank null building");
			return;
		}

		// Make sure the building is currently set to upgrade
		// (Duplicate job sanity check)

		if (rankingBuilding.isRanking() == false)
			return;

		// SetCurrentRank also changes the mesh and maxhp
		// accordingly for buildings with blueprints

		rankingBuilding.setRank(rankingBuilding.getRank() + 1);

		// Reload the object



	}

	@Override
	protected void _cancelJob() {
	}

}
