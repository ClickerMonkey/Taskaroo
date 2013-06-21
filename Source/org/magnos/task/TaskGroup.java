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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

/**
 * A collection of tasks that must start at the exact same time (execute
 * concurrently).
 * 
 * @author Philip Diffenderfer
 *
 */
public class TaskGroup extends TaskCollection 
{

	/**
	 * A runner which waits for a barrier then executes another runner.
	 * 
	 * @author Philip Diffenderfer
	 *
	 */
	private class SyncRunner implements Runnable 
	{
		
		// The barrier to wait for.
		private final CyclicBarrier barrier;
		
		// The runner to execute.
		private final Runnable runner;
		
		/**
		 * Instantiates a new SyncRunner.
		 * 
		 * @param barrier
		 * 		The barrier to wait for.
		 * @param runner
		 * 		The runner to execute.
		 */
		public SyncRunner(CyclicBarrier barrier, Runnable runner) 
		{
			this.barrier = barrier;
			this.runner = runner;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public void run() 
		{
			try {
				barrier.await();	
				runner.run();
			}
			catch (Exception e) {
			}
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<?> onExecute() 
	{
		// A barrier to make all tasks wait for each other to start.
		CyclicBarrier barrier = new CyclicBarrier(list.size());
		
		// Create new threads and start them. They will block until they're all
		// started and blocking on the barrier.
		for (Task<?> task : list) 
		{
			new Thread(new SyncRunner(barrier, task)).start();
		}
		
		// Finally wait for each task to finish, and if the list should be 
		// cleaned then remove the task from the list.
		List<Object> results = new ArrayList<Object>();
		for (int i = list.size() - 1; i >= 0; i--) 
		{
			results.add(list.get(i).sync());
			// Remove this task once completed?
			if (cleanList) {
				list.remove(i);
			}
		}
		return results;
	}

}
