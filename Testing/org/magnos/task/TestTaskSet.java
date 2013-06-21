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

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.magnos.task.Task;
import org.magnos.task.TaskListenerAdapter;
import org.magnos.task.TaskService;
import org.magnos.task.TaskSet;
import org.magnos.task.TestTask.PowerTask;
import org.magnos.test.BaseTest;


public class TestTaskSet extends BaseTest 
{

	private TaskService executor;
	
	@Before
	public void testBefore() {
		executor = new TaskService();
		executor.start();
	}
	
	@After
	public void testAfter() {
		executor.stop();
	}
	
	@Test
	public void testSet()
	{
		TaskSet set = new TaskSet();
		set.setClean(false);
		
		assertEquals( 0, set.size() );
		
		set.add(new PowerTask(2, 20));
		set.add(new PowerTask(5, 3));
		set.add(new PowerTask(4, 3));
		
		assertEquals( 3, set.size() );
		
		List<?> results = set.sync();
		assertEquals( 3, results.size() );
		assertTrue( results.contains(new BigInteger("1048576")) );
		assertTrue( results.contains(new BigInteger("125")) );
		assertTrue( results.contains(new BigInteger("64")) );
		
		assertEquals( 3, set.size() );
		
		set.reset();
		set.setClean(true);
		
		set.async(new TaskListenerAdapter<List<?>>() {
			public void onTaskFinish(Task<List<?>> task) {
				List<?> results = task.getResult();
				assertTrue( results.contains(new BigInteger("1048576")) );
				assertTrue( results.contains(new BigInteger("125")) );
				assertTrue( results.contains(new BigInteger("64")) );
			}
		});
		
		set.join();

		assertEquals( 0, set.size() );
	}
	
}
