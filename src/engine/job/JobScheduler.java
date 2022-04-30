// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.job;

import engine.server.MBServerStatics;

import java.util.PriorityQueue;


public class JobScheduler {

	private static final JobScheduler INSTANCE = new JobScheduler();

	private final PriorityQueue<JobContainer> jobs;
	private volatile boolean alive;
	private long timeOfKill = -1;

	public static JobScheduler getInstance() {
		return INSTANCE;
	}

	private JobScheduler() {
		jobs = new PriorityQueue<>(MBServerStatics.SCHEDULER_INITIAL_CAPACITY);
		Runnable worker = new Runnable() {
			@Override
			public void run() {
				execution();
			}
		};

		alive = true;

		Thread t = new Thread(worker, "JobScheduler");
		t.start();
	}

	/**
	 * This function schedules a job to execute in <i>timeToExecution</i>
	 * milliseconds from now.
	 *
	 * @param job
	 * @param timeToExecution
	 * @return
	 */
	public JobContainer scheduleJob(AbstractJob job, int timeToExecution) {
		long timeOfExecution = System.currentTimeMillis() + timeToExecution;
		JobContainer container = new JobContainer(job, timeOfExecution);

		synchronized (jobs) {
			jobs.offer(container);
			jobs.notify();
		}

		return container;
	}

	/**
	 * This function schedules a job to execute at the absolute time of
	 * <i>timeOfExecution</i> (milliseconds).
	 *
	 * @param job
	 * @param timeOfExecution
	 * @return
	 */
	public JobContainer scheduleJob(AbstractJob job, long timeOfExecution) {
		JobContainer container = new JobContainer(job, timeOfExecution);

		synchronized (jobs) {
			jobs.offer(container);
			jobs.notify();
		}

		return container;
	}

	public boolean cancelScheduledJob(JobContainer container) {
		return cancelScheduledJob(container.getJob());
	}

	public boolean cancelScheduledJob(AbstractJob job) {
		JobContainer container = new JobContainer(job, -1);

		boolean success = false;
		synchronized (jobs) {
			success = jobs.remove(container);
			jobs.notify();
		}

		return success;
	}

	/**
	 * Stops the jobScheduler
	 */
	public void shutdown() {
		if (alive) {
			alive = false;
			timeOfKill = System.currentTimeMillis();
			synchronized (jobs) {
				jobs.notify();
			}
		}
	}

	public JobContainer pollNextJobContainer() {
		if (alive) {
			throw new IllegalStateException("Can't poll jobs from a live scheduler.");
		}

		synchronized (jobs) {
			return jobs.poll();
		}
	}

	public long getTimeOfKill() {
		return this.timeOfKill;
	}

	public boolean isAlive() {
		return this.alive;
	}

	private void execution() {
		long duration;
		JobContainer container;
		int compensation = MBServerStatics.SCHEDULER_EXECUTION_TIME_COMPENSATION;

		while (alive) {
			synchronized (jobs) {
				container = jobs.peek();
				if (container == null) {
					// queue is empty, wait until notified (which happens after
					// a new job is offered)
					try {
						jobs.wait(0);
					} catch (InterruptedException ie) {
						// do nothing
					}
				} else {
					duration = container.timeOfExecution - System.currentTimeMillis();
					if (duration < compensation) {
						jobs.poll();
					} else {
						// enforce new loop
						container = null;

						// sleep until the head job execution time
						try {
							jobs.wait(duration);
						} catch (InterruptedException ie) {
							// do nothing
						}
					}
				}
			}

			if (container != null) {
				JobManager.getInstance().submitJob(container.job);
			}
		}
	}
}
