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

/**
 * A listener notified on the events of task.
 * 
 * @author Philip Diffenderfer
 *
 * @param <R>
 * 		The result type.
 */
public interface TaskListener<R> 
{
	
	/**
	 * The method invoked when a task is completely finished. The task either
	 * succeeded, errored, was canceled, or timed out.
	 * 
	 * @param source
	 * 		The task that finished.
	 */
	public void onTaskFinish(Task<R> source);
	
	/**
	 * The method invoked when a task completes successfully (was not canceled,
	 * timed out, and did not produce an error).
	 * 
	 * @param source
	 * 		The task that finished successfully.
	 * @param result
	 * 		The result of the task.
	 */
	public void onTaskSuccess(Task<R> source, R result);
	
	/**
	 * The method invoked when a task executes and throws an error.
	 * 
	 * @param source
	 * 		The task that finished after throwing an exception.
	 * @param error
	 * 		The exception thrown.
	 */
	public void onTaskError(Task<R> source, Throwable error);
	
	/**
	 * The method invoked when a task is cancelled before it begins running.
	 * 
	 * @param source
	 * 		The task that was cancelled.
	 */
	public void onTaskCancel(Task<R> source);
	
	/**
	 * The method invoked when a task times out. A task can time out while its
	 * running or while its waiting.
	 * 
	 * @param source
	 * 		The task that timed out.
	 */
	public void onTaskTimeout(Task<R> source);
	
}