// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.job;

import org.pmw.tinylog.Logger;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Provides mandatory base implementation for all 'Job's'.
 * 
 * @author 
 */
public abstract class AbstractJob implements Runnable {

	public enum JobRunStatus {
		CREATED, RUNNING, FINISHED;
	};

	public enum JobCompletionStatus {
		NOTCOMPLETEDYET, SUCCESS, SUCCESSWITHERRORS, FAIL;
	};

	/**
	 * Keep fields private. All access through getters n setters for Thread
	 * Safety.
	 */
	private JobRunStatus runStatus;
	private JobCompletionStatus completeStatus;
	private UUID uuid = null;

	private final AtomicReference<String> workerID;
	private static final String DEFAULT_WORKERID = "UNPROCESSED_JOB";

	private long submitTime = 0L;
	private long startTime = 0L;
	private long stopTime = 0L;

	public AbstractJob() {
			//Tests against DEFAULT_WORKERID to ensure single execution
			this.workerID = new AtomicReference<>(DEFAULT_WORKERID);
			this.runStatus = JobRunStatus.RUNNING;
			this.completeStatus = JobCompletionStatus.NOTCOMPLETEDYET;
			this.uuid = UUID.randomUUID();
	}

	@Override
	public void run() {

		if(workerID.get().equals(DEFAULT_WORKERID)) {
			Logger.warn("FIX ME! Job ran through 'run()' directly. Use executeJob(String) instead.");
		}
		
		this.markStartRunTime();

		this.setRunStatus(JobRunStatus.RUNNING);
		try {
			this.doJob();
		} catch (Exception e) {
			Logger.error(e);
		}
		
		this.setRunStatus(JobRunStatus.FINISHED);

		this.markStopRunTime();
	}

	protected abstract void doJob();

	public void executeJob(String threadName) {
		this.workerID.set(threadName);
		this.run();
	}
		
	protected void setRunStatus(JobRunStatus status) {
		synchronized (this.runStatus) {
			this.runStatus = status;
		}
	}

	public JobRunStatus getRunStatus() {
		synchronized (this.runStatus) {
			return runStatus;
		}
	}

	protected void setCompletionStatus(JobCompletionStatus status) {
		synchronized (this.completeStatus) {
			this.completeStatus = status;
		}
	}

	public JobCompletionStatus getCompletionStatus() {
		synchronized (this.completeStatus) {
			return completeStatus;
		}
	}

	protected void setJobId(UUID id) {
		synchronized (this.uuid) {
			this.uuid = id;
		}
	}

	public UUID getJobId() {
		synchronized (this.uuid) {
			return uuid;
		}
	}
	
	public String getWorkerID() {
		return workerID.get();
	}

	/*
	 * Time markers
	 */

	protected void markSubmitTime() {
		this.submitTime = System.currentTimeMillis()-2;
	}

	protected void markStartRunTime() {
		this.startTime = System.currentTimeMillis()-1;
	}

	protected void markStopRunTime() {
		this.stopTime = System.currentTimeMillis();
	}

	public final long getSubmitTime() {
		return submitTime;
	}

	public final long getStartTime() {
		return startTime;
	}

	public final long getStopTime() {
		return stopTime;
	}
}
