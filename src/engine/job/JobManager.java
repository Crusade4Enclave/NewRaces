// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.job;

import engine.core.ControlledRunnable;
import engine.server.MBServerStatics;
import engine.util.ThreadUtils;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Note to DEVs. When attempting to log something in this class, use logDIRECT
 * only.
 */
public class JobManager extends ControlledRunnable {

	/*
	 * Singleton implementation.
	 */
	private static volatile JobManager INSTANCE;
	public static JobManager getInstance() {
		if (JobManager.INSTANCE == null) {
			synchronized (JobManager.class) {
				if (JobManager.INSTANCE == null) {
					JobManager.INSTANCE = new JobManager();
					JobManager.INSTANCE.startup();
				}
			}
		}
		return JobManager.INSTANCE;
	}

	/*
	 * Class implementation
	 */

	private final ArrayList<JobPool> jobPoolList = new ArrayList<>();
	private final ConcurrentHashMap<String, JobPool> jobQueueMapping= new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_HIGH);
	
	private boolean shutdownNowFlag = false;

	private JobManager() {
		super("JobManager");
		
		// create the initial job pools with the correct sizes 
		// based on the number of array elements in the initial_jo_workers
		// definition in Statisc

		if (MBServerStatics.INITIAL_JOBPOOL_WORKERS != null && MBServerStatics.INITIAL_JOBPOOL_WORKERS.length >0 ) {
			for (int i=0; i<MBServerStatics.INITIAL_JOBPOOL_WORKERS.length; i++) {
				JobPool jp = new JobPool(i,MBServerStatics.INITIAL_JOBPOOL_WORKERS[i]);
				this.jobPoolList.add(jp);
				Logger.info( "Adding JobPool_" + jp.getJobPoolID() + " with " + MBServerStatics.INITIAL_JOBPOOL_WORKERS[i] + " workers");
			}
		} else {
		// not defined or empty in statics so just create 1 JobPool with 25 workers
		System.out.println("Creating 1 pool called 0");
		JobPool jp = new JobPool(0,25);
		this.jobPoolList.add(jp);
			Logger.info( "Adding JobPool_" + jp.getJobPoolID() + " with 25 workers");
		}

		JobScheduler.getInstance();
		
		// if you want any jobs to default onto a given queue put it here
		// otherwise everything goes on the P1 queue by default

	}
	
		/**
	 * Submit a job to be processed by the JobManager. There is no guarantee
	 * that the job will be executed immediately.
	 *
	 * @param aj
	 *            AbstractJob to be submitted.
	 * @return boolean value indicating whether the job was submitted or not.
	 */
	public boolean submitJob(AbstractJob aj) {

		if (jobQueueMapping.containsKey(aj.getClass().getSimpleName())) {
			return this.jobQueueMapping.get(aj.getClass().getSimpleName()).submitJob(aj);
		} else {
			return this.jobPoolList.get(0).submitJob(aj);
		}
	}

		
	private void auditAllWorkers() {
		
		for (JobPool jp : this.jobPoolList) {
			jp.auditWorkers();
		}
	}

	@Override
	protected boolean _Run() {
		// This thread is to be used to JM internal monitoring.

		// Startup workers:
		this.auditAllWorkers();
		ThreadUtils.sleep(MBServerStatics.JOBMANAGER_INTERNAL_MONITORING_INTERVAL_MS * 2);

		this.runStatus = true;

		// Monitoring loop
		while (this.runCmd) {
			this.auditAllWorkers();
			ThreadUtils.sleep(MBServerStatics.JOBMANAGER_INTERNAL_MONITORING_INTERVAL_MS);
		}

		// No new submissions
		for (JobPool jp : this.jobPoolList){
			jp.setBlockNewSubmissions(true);
		}

		if (this.shutdownNowFlag == false) {
			// shutdown each pool
			for (JobPool jp : this.jobPoolList) {
				jp.shutdown();
			}
		} else {
			// Emergency Stop each pool
			for (JobPool jp : this.jobPoolList) {
				jp.emergencyStop();
			}
		}
		this.runStatus = false;
		Logger.info("JM Shutdown");
		return true;
	}

	@Override
	protected boolean _postRun() {
		return true;
	}

	@Override
	protected boolean _preRun() {
		return true;
	}

	@Override
	protected void _shutdown() {
		Logger.info("JM Shutdown Requested");
		JobScheduler.getInstance().shutdown();
	}

	@Override
	protected void _startup() {
		Logger.info("JM Starting up... ");
	}

	public void shutdownNow() {
		this.shutdownNowFlag = true;
		this.shutdown();
	}
	
	public String moveJob(String simpleClassName, String jobPoolID) {
		// moves a job to a different queue
		
		try {
			// parse string into an int
			Integer i = Integer.parseInt(jobPoolID);
			for (JobPool jp : this.jobPoolList) {
				if (jp.getJobPoolID() == i) {
					// move the job to the new queue
					this.jobQueueMapping.put(simpleClassName, jp);
					return simpleClassName + " moved to JobPool ID " + jp.getJobPoolID(); 
				}
			}

		} catch (NumberFormatException e) {
			return "jobPoolID is not a valid number";
		}
		
		//Verify jobpool ID
		return "Invalid parameters <simpleClassName - case sensitive> <jobPoolID> required";
	}

	public String resetJobs() {
		// moves all jobs to the P1 queue
		this.jobQueueMapping.clear();
		return "All Jobs reset onto P1 queue";
	}

	public String showJobs() {
		String out = "";
		Iterator<String> jmi = this.jobQueueMapping.keySet().iterator();
		
		while (jmi.hasNext()) {
			String jmiKey = jmi.next();
			out += jmiKey + ' ' + this.jobQueueMapping.get(jmiKey).getJobPoolID() + '\n';
		}
		return out;
	}

	public ArrayList<JobPool> getJobPoolList() {
		
		return this.jobPoolList;
	}

	public String modifyJobPoolWorkers(String jobPoolID, String maxWorkers) {
		
		try {
			// parse string into an int
			Integer jid = Integer.parseInt(jobPoolID);
			Integer mw = Integer.parseInt(maxWorkers);
			for (JobPool jp : this.jobPoolList) {
				if (jp.getJobPoolID() == jid) {
					// change the number of workers
					return jp.setMaxWorkers(mw);
				}
			}

		} catch (NumberFormatException e) {
			return "Invalid parameters <jobPoolID> <maxWorkers> required";
		}
		
		return "Invalid parameters <jobPoolID> <maxWorkers> required";
	}
}
