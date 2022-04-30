// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.job;

import engine.core.ControlledRunnable;
import org.pmw.tinylog.Logger;

import java.util.Queue;


public class JobWorker extends ControlledRunnable {
	private final int workerId;

	private final Queue<AbstractJob> jobWaitQueue;
	private final Queue<JobWorker> jobWorkerList;
		
	private AbstractJob currentJob;

	public JobWorker(final int workerID, int priorityQueue,
			Queue<AbstractJob> jobWaitQueue,
			Queue<JobWorker> jobWorkerList) {
		super("JobWorker_" +  priorityQueue + '_' +  workerID);
		
		workerId = workerID;
		this.jobWaitQueue = jobWaitQueue;
		this.jobWorkerList = jobWorkerList;
	}

	@Override
	protected boolean _Run() {

		while (this.runCmd) {
			// Access to Queue is synchronized internal to JobManager
			this.currentJob = this.jobWaitQueue.poll();

			if (this.currentJob == null) {
				try {
					// use self as MUTEX
					synchronized (this) {
						this.jobWorkerList.add(this);
						this.wait();
					}

				} catch (InterruptedException e) {
					Logger.error(this.getThreadName(), e.getClass()
							.getSimpleName()
							+ ": " + e.getMessage());
					break;

				} 
			} else {

				// execute the new job..
				this.currentJob.executeJob(this.getThreadName());
				this.currentJob = null;
			}

		}
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
	}

	@Override
	protected void _startup() {
		//this.logDirectINFO(this.getThreadName(), "Starting up...");
	}

	public final int getWorkerId() {
		return workerId;
	}

	public final AbstractJob getCurrentJob() {
		return currentJob;
	}

	public final boolean hasCurrentJob() {
        return (currentJob != null);
	}

	protected void EmergencyStop() {
		this.runCmd = false;
		String out = "Stack Trace";
		for(StackTraceElement e : this.getThisThread().getStackTrace()) {
			out += " -> " + e.toString();
		}
		Logger.info(out);
		this.getThisThread().interrupt();
	}
}
