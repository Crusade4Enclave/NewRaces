// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.job.AbstractScheduleJob;
import engine.objects.AbstractCharacter;
import engine.objects.Mob;
import engine.objects.NPC;

public class UpgradeNPCJob extends AbstractScheduleJob {

	private final AbstractCharacter rankingAC;
	boolean success;

	public UpgradeNPCJob(AbstractCharacter ac) {
		super();
		this.rankingAC = ac;
	}

	@Override
	protected void doJob() {

		int newRank;

		if (this.rankingAC.getObjectType() == GameObjectType.NPC){


			if (this.rankingAC == null)  //NPC could've been removed...
				return;
			newRank = (this.rankingAC.getRank() * 10) + 10;
			
			

			((NPC)this.rankingAC).setRank(newRank);
			((NPC)this.rankingAC).setUpgradeDateTime(null);
		}else if (this.rankingAC.getObjectType() == GameObjectType.Mob){
			if (this.rankingAC == null)  //NPC could've been removed...
				return;
			newRank = (this.rankingAC.getRank() * 10) + 10;



			((Mob)this.rankingAC).setRank(newRank);
			Mob.setUpgradeDateTime((Mob)this.rankingAC, null);
			WorldGrid.updateObject(this.rankingAC);

		}

	}

	@Override
	protected void _cancelJob() {
	}

}
