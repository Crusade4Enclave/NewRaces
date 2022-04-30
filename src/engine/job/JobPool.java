// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.job;


import engine.jobs.AttackJob;
import engine.jobs.UsePowerJob;
import engine.net.CheckNetMsgFactoryJob;
import engine.net.ConnectionMonitorJob;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class JobPool {

	int jobPoolID;
	int maxWorkers;
	int nextWorkerID;
	private final LinkedBlockingQueue<AbstractJob> jobWaitQueue = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<JobWorker> jobWorkerQueue = new LinkedBlockingQueue<>();
	private final ArrayList<JobWorker> jobWorkerList = new ArrayList<>();
	private final LinkedBlockingQueue<AbstractJob> jobRunList = new LinkedBlockingQueue<>();
	private boolean blockNewSubmissions = false;
	
	public JobPool(int id, int workers) {
		this.jobPoolID = id;
			
		// default to 1 unless workers parameter is higher
		int actualWorkers = 1;
		if (workers > 1)
			actualWorkers = workers;
		
		this.maxWorkers = actualWorkers;	
		for (int i=0;i<actualWorkers; i++) {
			this.startWorker(i);
		}
		
		this.nextWorkerID = this.maxWorkers;
		
			
			
	}
	
	private int getNextWorkerID () {
		return this.nextWorkerID++;
		
		
	}
	public int getJobPoolID () {
		return this.jobPoolID;
	}
	
	public void setBlockNewSubmissions(boolean blocked) {
		this.blockNewSubmissions = blocked;
	}
	
	public boolean submitJob(AbstractJob aj) {
		
		if (blockNewSubmissions) {
			Logger.warn("A '" + aj.getClass().getSimpleName() + "' job was submitted, but submissions are currently blocked.");
			return false;
		}
	
		aj.markSubmitTime();
		jobWaitQueue.add(aj);
		
		// keep notifying workers if the wait queue has items
		// commented out as the price of polling the wait queue
		// size while it is being updated out-weighs the gain 
		// for not just blindly waking all workers
		// unless we have a stupidly large pool vs CPU threads
		
		JobWorker jw = jobWorkerQueue.poll();
		if(jw != null) {
			synchronized (jw) {
				jw.notify();
			}
		}
		
		return true;
	}
	
	private void startWorker(int workerID) {
		
		// check we dont already have a jobWorker with that ID
		synchronized(this.jobWorkerList) {
			for (JobWorker jwi : this.jobWorkerList) {
				if (jwi.getWorkerId() == workerID) {
					Logger.error("Attempt to create worker with ID " + workerID + " failed in JobPool " + jobPoolID + " as worker ID already exists");
					return;
				}
			}
		}
		
		// ID is unique, create worker		
		JobWorker jw;
		jw = new JobWorker(workerID, this.jobPoolID, this.jobWaitQueue, this.jobWorkerQueue);
		
		synchronized(this.jobWorkerList) {
			//Adds to the overall list..
			jobWorkerList.add(jw);
		}
		
		//Returns to the free worker queue..
		jw.startup();
	}

	private String getQueueByClassAsString(Queue<AbstractJob> q) {
		HashMap<String, Integer> ch = new HashMap<>();
		int cnt = 0;
		
		
		// iterate through the linked queue and get every item
		// putting classname and incrementing the value each time in the hashmap
		Iterator<AbstractJob> wi = q.iterator();
		
		while (cnt < q.size() && wi.hasNext()) {
			AbstractJob aj = wi.next();
			if (ch.containsKey(aj.getClass().getSimpleName())) {
				int newValue = ch.get(aj.getClass().getSimpleName()) + 1;
				ch.put(aj.getClass().getSimpleName(), newValue);
			} else {
				ch.put(aj.getClass().getSimpleName(), 1);
			}
			cnt++;
		}
		
		// iterate through the hashmap outputting the classname and number of jobs
		Iterator<String> i = ch.keySet().iterator();
		String out = "";
		while(i.hasNext()) {
			Object key = i.next();
			out += "JobPoolID_" + this.jobPoolID + ' ' + key.toString() + "=>" + ch.get(key) + '\n';
		}
		if (out.isEmpty())
			return "No Jobs on queue\n";
		else
			return out;
	}
	
	public void auditWorkers() {
		
		if(!MBServerStatics.ENABLE_AUDIT_JOB_WORKERS) {
			return;
		}
		ArrayList<AbstractJob> problemJobs = new ArrayList<>();

		// Checked for stalled Workers
		Iterator<JobWorker> it = jobWorkerList.iterator();

		while (it.hasNext()) {
			JobWorker jw = it.next();
			AbstractJob curJob = jw.getCurrentJob();

			if (curJob != null) { // Has a job

				if (JobPool.isExemptJobFromAudit(curJob)) {
					continue;
				}

				// Determine whether the job is being executed or waiting to
				// start;

				if (curJob.getStartTime() <= 0) {
					// Waiting to start
					long diff = System.currentTimeMillis() - curJob.getSubmitTime();

					if (diff >= MBServerStatics.JOB_STALL_THRESHOLD_MS) {
						Logger.warn("Job did not start within threshold.  Stopping worker#" + jw.getWorkerId() + " JobData:"
								+ curJob.toString());
						jw.EmergencyStop();
						problemJobs.add(jw.getCurrentJob());
						it.remove();
					} // end if (diff >=

				} else if (curJob.getStopTime() <= 0L) {
					// is executing it
					long diff = System.currentTimeMillis() - curJob.getStartTime();

					if (diff >= MBServerStatics.JOB_STALL_THRESHOLD_MS) {
						Logger.warn("Job execution time exceeded threshold(" + diff + "). Stopping worker#" + jw.getWorkerId() + " JobData:"
								+ curJob.toString());
						jw.EmergencyStop();
						problemJobs.add(jw.getCurrentJob());
						it.remove();
					} // end if (diff >=
				} // end if(curJob.getStopTime()
			} // end if(curJob != null)
		} // end While

		// Check Worker Count and add workers as necessary;
		int workerCount = jobWorkerList.size();

		int maxThreads = this.maxWorkers;
		
		
		// no pool can go below a single thread
		if (maxThreads < 1)
			maxThreads = 1;
				
		while (workerCount != maxThreads) {
			Logger.info("Resizing JobPool " + this.jobPoolID + " from " + workerCount + " to " + maxThreads);
			
			if (workerCount < maxThreads) {
				this.startWorker(this.getNextWorkerID());

				
				if (jobWorkerList.size() <= workerCount) {
					// Something didnt work correctly
					Logger.warn("auditWorkers() failed to add a new JobWorker to JobPool " + this.jobPoolID + ". Worker count " + workerCount + " Worker pool size " + jobWorkerList.size() + " Aborting Audit.");
					return;
				}

			} else if (workerCount > maxThreads) {
				synchronized(this.jobWorkerList) {
					Logger.warn("Reducing workers in JobPool " + this.jobPoolID + " Worker Count: " + workerCount + " to Max threads: " + maxThreads);
					// pick a worker off the list and shut it down
					
					JobWorker toRemove = null;
					int loopTries = 5;
					do {
						//Infinite loop could be bad..
						toRemove = jobWorkerQueue.poll();
					} while (toRemove == null && --loopTries >= 0);
					
					//remove it from the list
					toRemove.shutdown();
					jobWorkerList.remove(toRemove);
				}
			}

			// update value for next loop pass
			workerCount = jobWorkerList.size();
		}
	
	}

	private static boolean isExemptJobFromAudit(AbstractJob aj) {
		// If the job is any of the following classes, exempt it from auditWorkers
                if (aj instanceof ConnectionMonitorJob) {
			return true;
		} else
                    return aj instanceof CheckNetMsgFactoryJob  || aj instanceof AttackJob || aj instanceof UsePowerJob;

    }
	
	public void shutdown() {
		synchronized(this.jobWorkerList) {
			for (JobWorker jw : this.jobWorkerList)
				jw.shutdown();
		}
	}
	
	public void emergencyStop() {
		synchronized(this.jobWorkerList) {
			for (JobWorker jw : this.jobWorkerList)
				jw.EmergencyStop();
		}
	}

	public String getRunningQueueByClassAsString() {
		return this.getQueueByClassAsString(this.jobRunList);
	}
	
	public String getWaitQueueByClassAsString () {
		return this.getQueueByClassAsString(this.jobWaitQueue);
	}
	
	
	// used by devcmds
	public String setMaxWorkers (int maxWorkers) {
		
		if (maxWorkers > 0 && maxWorkers < 101) {
			this.maxWorkers = maxWorkers;
			// audit workers reduces the cap
			this.auditWorkers();
			return "Max workers set to " + maxWorkers + " for JobPool_" + this.jobPoolID;
		} else {
			return "Max workers not set, value must be from 1-100";
		}
		
	}
}
