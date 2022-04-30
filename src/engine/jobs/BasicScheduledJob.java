// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.job.AbstractJob;
import engine.job.JobScheduler;
import org.pmw.tinylog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * A generic execution job which is an extension of {@link AbstractJob}.
 * <p>
 * Intended to be used with the {@link JobScheduler}, a BasicScheduledJob will
 * hold a reference to an execution method.
 * 
 * @author Burfo
 **/

public class BasicScheduledJob extends AbstractJob {

	private Method execution;
	private Object referenceObject;

	/**
	 * Generates a new BasicScheduledJob that executes a static method that has
	 * no parameters.
	 * 
	 * @param methodName
	 *            Name of the static method to execute, such as "myMethod"
	 * 
	 * @param methodClass
	 *            The class in which {@code methodName} exists
	 */
	public BasicScheduledJob(String methodName, Class methodClass) {
		this(methodName, methodClass, null);
	}

	/**
	 * Generates a new BasicScheduledJob that executes an instance method that
	 * has no parameters.
	 * 
	 * @param methodName
	 *            Name of the instance method to execute, such as "myMethod"
	 * 
	 * @param methodClass
	 *            The class in which {@code methodName} exists
	 * 
	 * @param referenceObject
	 *            Instance of {@code methodClass} against which {@code
	 *            methodName} should be executed
	 */
	@SuppressWarnings("unchecked")
	public BasicScheduledJob(String methodName, Class methodClass, Object referenceObject) {
		super();
		Method method = null;
		try {
			method = methodClass.getMethod(methodName);
		} catch (SecurityException e) {
			Logger.error( e);
		} catch (NoSuchMethodException e) {
			Logger.error(  e);
		}
		setData(method, null);
	}

	/**
	 * Generates a new BasicScheduledJob that executes a static method that has
	 * no parameters.
	 * 
	 * @param executionMethod
	 *            Reference to the static method to execute
	 */
	public BasicScheduledJob(Method executionMethod) {
		this(executionMethod, null);
	}

	/**
	 * Generates a new BasicScheduledJob that executes an instance method that
	 * has no parameters.
	 * 
	 * @param executionMethod
	 *            Reference to the static method to execute
	 * 
	 * @param referenceObject
	 *            Instanciated object against which {@code executionMethod}
	 *            should be executed
	 */
	public BasicScheduledJob(Method executionMethod, Object referenceObject) {
		super();
		setData(executionMethod, referenceObject);
	}

	private void setData(Method executionMethod, Object referenceObject) {
		this.execution = executionMethod;
		this.referenceObject = referenceObject;
		if (execution == null) {
			Logger.error("BasicScheduledJob instanciated with no execution method.");
		}
	}

	@Override
	protected void doJob() {
		if (execution == null) {
			Logger.error( "BasicScheduledJob executed with nothing to execute.");
			return;
		}

		try {
			execution.invoke(referenceObject);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			Logger.error( "BasicScheduledJob execution failed. Method: " + execution.toString(), e);
		} catch (InvocationTargetException e) {
			Logger.error( "BasicScheduledJob execution failed. " + "Method: " + execution.toString(), e);
		}
	}
}
