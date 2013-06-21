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
 * An adapter which implements all TaskListener methods. This can be used to
 * only listen to specific task events.
 * 
 * <h1>Example Usage</h1>
 * <pre>
 * Task t = new ...
 * t.async(new TastListenerAdapter() {
 *	public void onTaskFinish(Task t) {
 * 		// task has finished (success, error, timed out, cancelled)
 *	}
 * });
 * </pre>
 * 
 * @author Philip Diffenderfer
 *
 * @param <R>
 * 		The result type.
 */
public class TaskListenerAdapter<R> implements TaskListener<R> 
{

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
	@Override
	public void onTaskError(Task<R> source, Throwable error) 
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

}
