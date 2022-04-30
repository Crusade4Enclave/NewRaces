// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.core;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;

/**
 * 
 */
public abstract class ControlledRunnable implements Runnable {
	protected boolean runCmd = false;
	protected boolean runStatus = false;
	private Thread thisThread;
	private final String threadName;

	public ControlledRunnable(String threadName) {
		super();
		this.threadName = threadName;
		ControlledRunnable.runnables.add(this);
	}

	/*
	 * Main loop
	 */

	/**
	 * This is the method called when ControlledRunnable.thisThread.start() is
	 * called.
	 */
	@Override
	public void run() {
		if (this._preRun() == false) {
			return;
		}

		this.runStatus = true;

		if (this._Run() == false) {
			return;
		}

		if (this._postRun() == false) {
			return;
		}

		this.runStatus = false;
	}

	/**
	 * _preRun() is called prior to the call to _Run(), but after _startup()
	 * 
	 * @return
	 */
	protected abstract boolean _preRun();

	/**
	 * _Run() is called after _startup() and contains should contain the main
	 * loop.
	 * 
	 * @return
	 */
	protected abstract boolean _Run();

	/**
	 * _postRun() is called after _Run() exits, not necessarily before
	 * _shutdown()
	 * 
	 * @return
	 */
	protected abstract boolean _postRun();

	/*
	 * Control
	 */

	/**
	 * startup() initializes the internal thread, sets the runCMD to true, and
	 * calls _startup() prior to starting of the internal Thread.
	 */
	public void startup() {

		this.thisThread = new Thread(this, this.threadName);
		this.runCmd = true;
		this._startup();
		this.thisThread.start();
	}

	/**
	 * This method is called just before ControlledRunnable.thisThread.start()
	 * is called.
	 */
	protected abstract void _startup();

	/**
	 * This method is called to request a shutdown of the runnable.
	 */
	public void shutdown() {
		this.runCmd = false;
		this._shutdown();
	}

	/**
	 * This method is called just after ControlledRunnable.runCmd is set to
	 * False.
	 */
	protected abstract void _shutdown();

	/*
	 * Getters n setters
	 */
	public boolean getRunCmd() {
		return runCmd;
	}

	public boolean getRunStatus() {
		return runStatus;
	}

	/*
	 * Blockers
	 */
	public void blockTillRunStatus(boolean status) {
		while (this.runStatus != status) {
			try {
				System.out.println("BLOCKING");
				Thread.sleep(25L);
			} catch (InterruptedException e) {
				Logger.debug( e.getMessage());

				break;
			}
		}
	}

	/**
	 * @return the thisThread
	 */
	protected Thread getThisThread() {
		return thisThread;
	}

	/**
	 * @return the threadName
	 */
	public String getThreadName() {
		return threadName;
	}

	/*
	 * Instance monitoring and tools
	 */

	// Runnable tracking
	private static final ArrayList<ControlledRunnable> runnables = new ArrayList<>();

	public static void shutdownAllRunnables() {
		for (ControlledRunnable cr : ControlledRunnable.runnables) {
			//Use Direct logging since JobManager is a runnable.
            Logger.info("ControlledRunnable",
					"Sending Shutdown cmd to: " + cr.threadName);
			cr.shutdown();
		}
	}

}
