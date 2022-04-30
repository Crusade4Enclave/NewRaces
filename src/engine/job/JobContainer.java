// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.job;


public class JobContainer implements Comparable<JobContainer> {

	final AbstractJob job;
	final long timeOfExecution;
	final boolean noTimer;

	JobContainer(AbstractJob job, long timeOfExecution) {
		if (job == null) {
			throw new IllegalArgumentException("No 'null' jobs allowed.");
		}
		this.job = job;
		this.timeOfExecution = timeOfExecution;
		this.noTimer = false;
	}

	public JobContainer(AbstractJob job) {
		if (job == null) {
			throw new IllegalArgumentException("No 'null' jobs allowed.");
		}
		this.job = job;
		this.timeOfExecution = Long.MAX_VALUE;
		this.noTimer = true;
	}

	public AbstractJob getJob() {
		return job;
	}

	public boolean noTimer() {
		return noTimer;
	}

	public long timeOfExection() {
		return this.timeOfExecution;
	}

	public int timeToExecutionLeft() {
		if (JobScheduler.getInstance().isAlive()) {
			int timeLeft = (int) (timeOfExecution - System.currentTimeMillis());
			if (timeLeft < 0)
				timeLeft = 0;
			return timeLeft;
		} else
			return (int) (timeOfExecution - JobScheduler.getInstance().getTimeOfKill());
	}

	@Override
	public int compareTo(JobContainer compared) {
		if (timeOfExecution < compared.timeOfExecution) {
			return -1;
		}
		if (timeOfExecution > compared.timeOfExecution) {
			return 1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return job.equals(((JobContainer) obj).job);
	}

	@Override
	public int hashCode() {
		return job.hashCode();
	}

	public void cancelJob() {
		if (job != null && job instanceof AbstractScheduleJob)
			((AbstractScheduleJob)job).cancelJob();
	}
}
