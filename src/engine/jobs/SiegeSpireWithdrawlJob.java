// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.jobs;

import engine.job.AbstractScheduleJob;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.objects.Building;
import engine.objects.City;
import org.pmw.tinylog.Logger;

public class SiegeSpireWithdrawlJob extends AbstractScheduleJob {

	private Building spire = null;

	public SiegeSpireWithdrawlJob(Building spire) {
		super();
		this.spire = spire;
	}

	@Override
	protected void doJob() {

		if (spire == null)
			return;

		// Early exit if someone disabled the spire

		if (!spire.isSpireIsActive())
			return;
		
	City buildingCity = spire.getCity();
		
	if (buildingCity == null)
		return;
	
	
		
			buildingCity.transactionLock.writeLock().lock();
			try{
			
		// If the spire runs out of money, disable it.
		//*** Refactor: 5000 every 30 seconds?  Wtf?

		if (!spire.hasFunds(5000)){
			spire.disableSpire(true);
			return;
		}
		if (spire.getStrongboxValue() < 5000) {
			spire.disableSpire(true);
			return;
		}

		// Deduct the activation cost from the strongbox and resubmit the job

		if (!spire.transferGold(-5000,false))
			return;
		JobContainer jc = JobScheduler.getInstance().scheduleJob(new SiegeSpireWithdrawlJob(spire), 300000);
		spire.getTimers().put("SpireWithdrawl", jc);
		
			}catch(Exception e){
				Logger.error(e);
			}finally{
				buildingCity.transactionLock.writeLock().unlock();
			}

	}

	@Override
	protected void _cancelJob() {
	}
}
