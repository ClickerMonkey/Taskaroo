/* 
 * NOTICE OF LICENSE
 * 
 * This source file is subject to the Open Software License (OSL 3.0) that is 
 * bundled with this package in the file LICENSE.txt. It is also available 
 * through the world-wide-web at http://opensource.org/licenses/osl-3.0.php
 * If you did not receive a copy of the license and are unable to obtain it 
 * through the world-wide-web, please send an email to pdiffenderfer@gmail.com 
 * so we can send you a copy immediately. If you use any of this software please
 * notify me via my website or email, your feedback is much appreciated. 
 * 
 * @copyright   Copyright (c) 2011 Magnos Software (http://www.magnos.org)
 * @license     http://opensource.org/licenses/osl-3.0.php
 * 				Open Software License (OSL 3.0)
 */

package org.magnos.task;

import org.magnos.util.State;

/**
 * A single action that can be executed synchronously or asynchronously. A
 * task can finish successfully, because an exception was thrown, before it 
 * started by being canceled, or by not completing by a specific time. A task
 * uses a state machine which can hold Initialized, Waiting, Running, and 
 * Finished states. Sub-states of Finished exist which are Success, TimedOut, 
 * Error, and Canceled.
 * 
 * @author Philip Diffenderfer
 *
 * @param <R>
 * 		The result type.
 */
public abstract class Task<R> implements TaskEventHandler, TaskListener<R>, Runnable 
{
	
	/*
	 * Task States
	 */
	
	/**
	 * The state of the task when it has not been executed yet.
	 */
	public static final int Initialized = State.create(0);
	
	/**
	 * The state of the task when it has been handled and is waiting to be executed.
	 */
	public static final int Waiting 	= State.create(1);
	
	/**
	 * The state of the task when it is currently executing and has not finished.
	 */
	public static final int Running 	= State.create(2);
	
	/**
	 * 
	 * The state of the task when it has completely finished.
	 */
	public static final int Finished	= State.create(3);
	
	/*
	 * Tasks Finished Sub-states
	 */
	
	/**
	 * The state of a finished task that has completed successfully.
	 */
	public static final int Success 	= State.create(4);
	
	/**
	 * The state of a finished task that has timed out.
	 */
	public static final int TimedOut 	= State.create(5);
	
	/**
	 * The state of a finished task that has error.
	 */
	public static final int Error 	= State.create(6);
	
	/**
	 * The state of a finished task that has been canceled.
	 */
	public static final int Canceled 	= State.create(7);
	

	
	// The state control object.
	private final State state = new State();
	
	// The handler which executes this task.
	private TaskEventHandler handler;
	
	// The listener of this task.
	private TaskListener<R> listener;
	
	// The result of this tasks execution.
	private R result;
	
	// The error (if any) that occurred when the task was executed.
	private Throwable error;

	// The maximum time the sync method will wait for a result.
	private long timeout = Long.MAX_VALUE;
	
	
	/**
	 * Instantiates a new Task which executes in the invoking thread.
	 */
	public Task() 
	{
		this(null);
	}

	/**
	 * Instantiates a new Task which is executed by the given handler. 
	 * 
	 * @param handler
	 * 		The handle which runs this task.
	 */
	public Task(TaskEventHandler handler) 
	{
		this.handler = (handler != null ? handler : this);
		this.state.set(Initialized);
	}

	/**
	 * Performs the Task's job.
	 * 
	 * @return
	 * 		The result of the Task.
	 */
	protected abstract R execute();

	/**
	 * Executes this task, waits for it to finish, and returns the result. If
	 * the task is already currently running this will wait for it to finish. If
	 * the task has already finished the result will be returned immediately.
	 * 
	 * @return
	 * 		The result of the Task.
	 */
	public R sync()
	{
		synchronized (state) 
		{
			// If the task hasn't started, start it.
			if (state.equals(Initialized)) {
				// State is waiting until execute method is called.
				state.set(Waiting);
				listener = this;
				handler.addEvent(this);
			}
			
			// If the task is currently running, wait for it to finish
			if (state.has(Waiting | Running)) 
			{
				// Wait for the state to finish or timeout.
				if (!state.waitFor(Finished, timeout)) {
					// It has timed out, notify listener.
					setResult(null);
					state.set(Finished | TimedOut);
					listener.onTaskTimeout(this);
					listener.onTaskFinish(this);
				}
			}
			// If the task has executed already return the result
		}
		return result;
	}

	/**
	 * Executes this task and notifies the given listener when it has finished.
	 * The task can only be executed asynchronously if it has not been executed
	 * yet or it has been reset (i.e. its not waiting, running, or finished).
	 * 
	 * @param async
	 * 		The listener to the events of the task.
	 * @return
	 * 		True if the task will execute asynchronously, otherwise false.
	 */
	public boolean async(TaskListener<R> async) 
	{
		synchronized (state) 
		{
			// If the task hasn't started, start it.
			boolean runnable = state.equals(Initialized); 
			if (runnable) {
				// State is waiting until execute method is called.
				state.set(Waiting);
				listener = async;
				handler.addEvent(this);
			}
			return runnable;
		}
	}
	
	/**
	 * Executes this task and notifies itself when it has finished. The task
	 * can only be executed asynchronously if it has not been executed yet or
	 * it has been reset (i.e. its not waiting, running, or finished).
	 * 
	 * @return
	 * 		True if the task will execute asynchronously, otherwise false.
	 */
	public boolean async() 
	{
		return async(this);
	}

	/**
	 * Performs the actual execution of the task. This is called by the handler.
	 */
	@Override
	public void run() 
	{
		// Acquire the state, if it has been timed out or has been cancelled
		// then exit this method. Else set the state to Running.
		synchronized (state) {
			if (state.has(TimedOut | Canceled)) {
				return;
			}
			state.set(Running);
		}
		
		// Try execution
		try 
		{
			setResult(execute());

			// If this task has not timed out...
			if (!state.has(TimedOut)) {
				// If executing was error free update the state and notify the listener.
				state.set(Finished | Success);
				listener.onTaskSuccess(this, result);
			}
		} 
		catch (Throwable error) 
		{
			// If this task has not timed out...
			if (!state.has(TimedOut)) {
				// An error has occurred, update the state and notify the listener.
				setError(error);
				state.set(Finished | Error);
				listener.onTaskError(this, error);
			}
		}
		finally 
		{
			// If this task has not timed out...
			if (!state.has(TimedOut)) {
				// Finally invoke the finished event.
				listener.onTaskFinish(this);
			}
		}
	}
	
	/**
	 * Cancels this Task if it is currently in the waiting state. This will also
	 * immediately notify the listener if the task can be canceled.
	 * 
	 * @return
	 * 		True if the task has been canceled, otherwise false.
	 */
	public boolean cancel() 
	{
		boolean cancellable = state.cas(Waiting, Finished | Canceled);
		if (cancellable) {
			listener.onTaskCancel(this);
			listener.onTaskFinish(this);
		}
		return cancellable;
	}
	
	/**
	 * Resets this task so it can be executed again only if it is in the 
	 * Finished state.
	 * 
	 * @return
	 * 		True if the task has been reset, otherwise false.
	 */
	public boolean reset() 
	{
		boolean resetable = state.cas(Finished, Initialized);
		if (resetable) {
			setError(null);
			setResult(null);
		}
		return resetable;
	}

	/**
	 * Waits for this task to finish. If this task has already finished this 
	 * will return immediately. This may fail and return false if the current
	 * thread is interrupted while its waiting.
	 * 
	 * @return
	 * 		True if the task has finished, otherwise false. 
	 */
	public boolean join() 
	{
		return state.waitFor(Finished);
	}
	
	/**
	 * Waits for this task to finish. If this task has already finished this
	 * will return immediately. This may fail if the current thread is 
	 * interrupted or the wait times out, which will return false.
	 * 
	 * @param timeout
	 * 		The maximum amount of time in milliseconds to wait to finish.
	 * @return
	 * 		True if the task has finished, otherwise false.
	 */
	public boolean join(long timeout)
	{
		return state.waitFor(Finished, timeout);
	}
	
	/**
	 * Forks this task by creating a duplicate task which invokes this tasks
	 * execute method. Therefore any variables accessed in this task's
	 * execute method will be modified by the returned Task once ints executed.
	 * 
	 * @return
	 * 		A new task which calls this task's execute method.
	 */
	public TaskFork<R> fork() 
	{
		return new TaskFork<R>(this);
	}
	
	/**
	 * Pauses the current thread until the given state of the task is reached.
	 * If the current thread is interrupted this will return before the state
	 * is reached and false will be returned.
	 * 
	 * @param desiredState
	 * 		The set of acceptable states to wait to reach.
	 * @return
	 * 		True if any of the states have been reached, otherwise false.
	 */
	public boolean waitForState(int desiredState) 
	{
		return state.waitFor(desiredState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTaskError(Task<R> source, Throwable error) 
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTaskSuccess(Task<R> source, R result) 
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTaskTimeout(Task<R> source) 
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTaskFinish(Task<R> source) 
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTaskCancel(Task<R> source) 
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addEvent(Task<?> task) 
	{
		task.run();
		return true;
	}
	
	/**
	 * Sets the result of this Task.
	 * 
	 * @param result
	 * 		The result of this task.
	 */
	protected void setResult(R result) 
	{
		this.result = result;
	}
	
	/**
	 * Sets the error that occured in this Task.
	 * 
	 * @param error
	 * 		The error that occured in this task.
	 */
	protected void setError(Throwable error) 
	{
		this.error = error;
	}
	
	/**
	 * Returns the result of the task if one has been determined.
	 * 
	 * @return
	 * 		The last result determined if any.
	 */
	public R getResult() 
	{
		return result;
	}

	/**
	 * Returns the error of the task if one was thrown during last execution.
	 * 
	 * @return
	 * 		The last thrown error if any.
	 */
	public Throwable getError() 
	{
		return error;
	}

	/**
	 * Returns the state machine of this Task. Use of this should be avoided,
	 * especially acquiring the lock of the state. This may be used to wait
	 * for a possible set of states but should only be done carefully.
	 * 
	 * @return
	 * 		The state machine of this task.
	 */
	public State getState() 
	{
		return state;
	}
	
	/**
	 * Returns the TaskHandler this tasks is using to perform execution.
	 *  
	 * @return
	 * 		The TaskHandler of this task.
	 */
	public TaskEventHandler getHandler() 
	{
		return handler;
	}
	
	/**
	 * Sets the TaskHandler of this Task. This is not a thread safe method.
	 *  
	 * @param handler
	 * 		The new TaskHandler for this task.
	 */
	public void setHandler(TaskEventHandler handler) 
	{
		this.handler = handler;
	}
	
	/**
	 * Sets the timeout of this task in milliseconds. By default the timeout
	 * is 2^63-1 milliseconds (292 million years). The timeout is the maximum
	 * amount of time a sync method will wait for the finished status before
	 * it tries to cancel and ignore the results of the task.
	 * 
	 * @param timeout
	 * 		The timeout of the Task in milliseconds.
	 */
	public void setTimeout(long timeout) 
	{
		this.timeout = timeout;
	}
	
	/**
	 * Returns the timeout of this task.
	 * 
	 * @return
	 * 		The timeout of this task in milliseconds.
	 */
	public long getTimeout()
	{
		return timeout;
	}
	
	/**
	 * Returns whether this Task is currently waiting to be executed.
	 * 
	 * @return
	 * 		True if the state of this task is waiting, otherwise false.
	 */
	public boolean isWaiting() 
	{
		return state.equals(Waiting);
	}
	
	/**
	 * Returns whether this Task is currently running (performing execution).
	 *  
	 * @return
	 * 		True if the state of this task is running, otherwise false.
	 */
	public boolean isRunning() 
	{
		return state.equals(Running);
	}
	
	/**
	 * Returns whether this Task has completely finished.
	 * 
	 * @return
	 * 		True if the state of this task is finished, otherwise false.
	 */
	public boolean isFinished() 
	{
		return state.has(Finished);
	}
	
	/**
	 * Returns whether this Task has finished successfully.
	 * 
	 * @return
	 * 		True if the state of this task is success, otherwise false.
	 */
	public boolean isSuccess() 
	{
		return state.has(Success);
	}
	
	/**
	 * Returns whether this Task was cancelled.
	 * 
	 * @return
	 * 		True if the state of this task is cancelled, otherwise false.
	 */
	public boolean isCanceled() 
	{
		return state.has(Canceled);
	}
	
	/**
	 * Returns whether this Task has timed out.
	 * 
	 * @return
	 * 		True if the state of this task is timedout, otherwise false.
	 */
	public boolean isTimedOut() 
	{
		return state.has(TimedOut);
	}
	
	/**
	 * Returns whether this Task has errored.
	 * 
	 * @return
	 * 		True if the state of this task is errored, otherwise false.
	 */
	public boolean isError() 
	{
		return state.has(Error);
	}

}