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
 * A clone of a task used for forking. This task merely calls the parent tasks
 * execute method.
 * 
 * @author Philip Diffenderfer
 *
 * @param <R>
 * 		The result type.
 */
public class TaskFork<R> extends Task<R> 
{

	// The parent task to invoke.
	private final Task<R> parent;
	
	/**
	 * Instantiates a new TaskClone given the parent task.
	 * 
	 * @param parent
	 * 		The parent of this task.
	 */
	public TaskFork(Task<R> parent) 
	{
		this.parent = parent;
		this.setHandler(parent.getHandler());
		this.setTimeout(parent.getTimeout());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public R execute() 
	{
		return parent.execute();
	}
	
	/**
	 * Returns the parent of this forked task.
	 * 
	 * @return
	 * 		The reference to the parent task.
	 */
	public Task<R> getParent()
	{
		return parent;
	}

}
