// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.job;

public abstract class AbstractJobStatistics {

	private String objectName;
	private long totalServiceTime = 0L;
	private long totalQueueTime = 0L;
	private long executions = 0L;
	private long maxServiceTime = 0L;
	private long minServiceTime = 0L;
	private long minQueueTime = 0L;
	private long maxQueueTime = 0L;
	
	
	public AbstractJobStatistics() {
		this.objectName = "Unknown";
	}
	
	public AbstractJobStatistics(String objectName) {
		this.objectName = objectName;
	}
	
	public void setObjectName (String objectName) {
		this.objectName = objectName;
	}
	
		public String getObjectName () {
		return this.objectName;
	}
	
	public long getExecutions() {
		return this.executions;
	}
	
	public long getTotalServiceTime() { 
		return this.totalServiceTime;
	}
	
	public long getTotalQueueTime() { 
		return this.totalQueueTime;
	}
	
	public long getAvgQueueTime() {
		if (this.executions > 0L && this.totalQueueTime > 0L)
			return this.totalQueueTime / this.executions;
		else
			return 0L;
	}
	
	public long getAvgServiceTime() {
		if (this.executions > 0L && this.totalServiceTime > 0L)
			return this.totalServiceTime / this.executions;
		else
			return 0L;
	}
	
	public long getMinServiceTime() {
		return this.minServiceTime;
	}
	
	public long getMinQueueTime() {
		return this.minQueueTime;
	}
	
	public long getMaxServiceTime() {
		return this.maxServiceTime;
	}
	
	public long getMaxQueueTime() {
		return this.maxQueueTime;
	}
	
	public void incrExecutions() {
		this.executions++;
	}
	
	public void addServiceTime(long svcTime) {
		this.totalServiceTime += svcTime;
		this.incrExecutions();
		if (svcTime > this.maxServiceTime)
			this.maxServiceTime = svcTime;
		if (svcTime < this.minServiceTime || this.minServiceTime == 0L)
			this.minServiceTime = svcTime;
		
	}
	
	public void addQueueTime(long queueTime) {
		this.totalQueueTime += queueTime;
		this.incrExecutions();
		if (queueTime > this.maxQueueTime)
			this.maxQueueTime = queueTime;
		if (queueTime < this.minQueueTime || this.minQueueTime == 0L)
			this.minQueueTime = queueTime;
		
	}
	
	public String asString() {
        return this.objectName + " execs=" + this.executions + " avg_svc_ms=" + this.getAvgServiceTime() +
			" min_svc_ms=" + this.minServiceTime + " max_svc_ms=" + this.maxServiceTime +
			" avg_q_ms=" + this.getAvgQueueTime() + " min_q_ms=" + this.minQueueTime +
			" max_q_ms=" + this.maxQueueTime;
	}
	
	public String asChatMsg() {
        return this.objectName + "=>" + this.executions + ',' + this.getAvgServiceTime() + '/' + this.minServiceTime +
                '/' + this.maxServiceTime + "  "+ this.getAvgQueueTime() + '/' +
                this.minQueueTime + '/' + this.maxQueueTime + '\n';
	}
}
