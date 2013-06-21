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

import java.util.Queue;

import org.magnos.resource.Resource;
import org.magnos.service.AbstractService;
import org.magnos.util.BlockableQueue;



/**
 * A service which can process any type of task.
 * 
 * @author Philip Diffenderfer
 *
 */
public class TaskService extends AbstractService<Task<?>> implements TaskEventHandler, Resource
{
	
	/**
	 * Instantiates a new TaskService.
	 */
	public TaskService() 
	{
		super();
	}

	/**
	 * Instantiates a new TaskService.
	 * 
	 * @param eventQueue
	 * 		The queue of events to poll from.
	 */
	public TaskService(BlockableQueue<Task<?>> eventQueue) 
	{
		super(eventQueue);
	}
	
	/**
	 * Instantiates a new TaskService.
	 * 
	 * @param blocking
	 * 		Whether the event queue for this service blocks when it polls for
	 * 		events or whether it returns immediately when empty.
	 */
	public TaskService(boolean blocking) 
	{
		super(blocking);
	}

	/**
	 * Instantiates a new TaskService.
	 * 
	 * @param sourceQueue
	 * 		The queue implementation to use internally as an event queue.
	 */
	public TaskService(Queue<Task<?>> sourceQueue) 
	{
		super(sourceQueue);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void onEvent(Task<?> event) 
	{
		event.run();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void onExecute() {
		
	}

	/**
	 * {@inheritDoc}
	 */
	protected void onPause() 
	{
		
	}

	/**
	 * {@inheritDoc}
	 */
	protected void onResume() 
	{
		
	}

	/**
	 * {@inheritDoc}
	 */
	protected void onStart() 
	{
		
	}

	/**
	 * {@inheritDoc}
	 */
	protected void onStop() 
	{
		
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isReusable() 
	{
		// can queue multiple tasks
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isUnused() 
	{
		// Only unused if there are no more tasks at this instant.
		return getEventQueue().isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public void free() 
	{
		// Stop (non-blocking) to free service from use.
		stop(false);
	}

}
