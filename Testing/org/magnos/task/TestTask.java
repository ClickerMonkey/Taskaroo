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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.magnos.task.Task;
import org.magnos.task.TaskFork;
import org.magnos.task.TaskService;
import org.magnos.test.BaseTest;


public class TestTask extends BaseTest 
{

	public static class PowerTask extends Task<BigInteger> {
		public long base;
		public long exponent;
		public PowerTask(long base, long exponent) {
			this.base = base;
			this.exponent = exponent;
		}
		protected BigInteger execute() {
			BigInteger multiplier = new BigInteger(Long.toString(base));
			BigInteger result = new BigInteger(Long.toString(base));
			for (long x = 1; x < exponent; x++) {
				result = result.multiply(multiplier);
			}
			return result;
		}
	}
	
	public static class WaitTask extends Task<Boolean> {
		public long wait;
		public WaitTask(long wait) {
			this.wait = wait;
		}
		protected Boolean execute() {
			sleep(wait);
			return true;
		}
	}
	
	public static class ErrorTask extends Task<Boolean> {
		protected Boolean execute() {
			throw new RuntimeException();
		}
	}
	
	
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
	public void testAccessors()
	{
		final PowerTask pt = new PowerTask(5, 2);
		pt.setHandler(executor);
		
		assertEquals( pt.getHandler(), executor );
		
		assertFalse( pt.isRunning() );
		assertFalse( pt.isWaiting() );
		
		pt.async();
		
		assertTrue( pt.isWaiting() || pt.isRunning() || pt.isFinished() );
		
		pt.join();
		
		assertTrue( pt.isFinished() );
		assertTrue( pt.isSuccess() );
		
		System.out.format("%d^%d = %s\n", pt.base, pt.exponent, pt.getResult());
	}
	
	@Test
	public void testSync()
	{
		final PowerTask pt = new PowerTask(5, 5);
		pt.setHandler(executor);
		
		assertTrue( pt.getState().equals(Task.Initialized) );
		BigInteger result = pt.sync();
		assertTrue( pt.getState().has(Task.Finished) );
		
		System.out.format("%d^%d = %s\n", pt.base, pt.exponent, result);
	}
	
	@Test
	public void testTimeoutWhileRunning()
	{
		final WaitTask pt = new WaitTask(500);
		pt.setHandler(executor);
		pt.setTimeout(200);
		
		assertNull( pt.sync() );
		assertTrue( pt.isTimedOut() );
		
		sleep(500);
	}
	
	@Test
	public void testTimeoutWithCancel()
	{
		final WaitTask pt1 = new WaitTask(500);
		pt1.setHandler(executor);
		
		final WaitTask pt2 = new WaitTask(500);
		pt2.setHandler(executor);
		
		GroupTask.initialize(1);
		GroupTask.add(new Runnable() {
			public void run() {
				pt1.async();
				pt2.sync();
			}
		});
		GroupTask.begin();
		
		sleep(100);
		
		pt2.cancel();
		
		GroupTask.finish();
		
		pt1.join();
		pt2.join();

		assertTrue( pt1.isSuccess() );
		assertTrue( pt2.isCanceled() );
	}
	
	@Test
	public void testError()
	{
		ErrorTask et = new ErrorTask();
		et.setHandler(executor);
		
		assertNull( et.sync() );
		
		assertNotNull( et.getError() );
		assertEquals( et.getError().getClass(), RuntimeException.class );

		assertTrue( et.isFinished() );
		assertTrue( et.isError() );
	}

	@Test
	public void testReset()
	{
		PowerTask pt = new PowerTask(4, 3);
		pt.setHandler(executor);
		
		assertEquals( pt.sync(), new BigInteger("64") );
		assertTrue( pt.isFinished() );
		
		assertTrue( pt.reset() );
		assertTrue( pt.getState().equals(Task.Initialized) );
		
		pt.exponent = 2;
		
		assertEquals( pt.sync(), new BigInteger("16") );
		assertTrue( pt.isFinished() );
	}
	
	@Test
	public void testJoinTimeout()
	{
		WaitTask wt = new WaitTask(500);
		wt.setHandler(executor);
		
		assertTrue( wt.async() );
		
		assertFalse( wt.join(100) );
		assertTrue( wt.isRunning() );
		
		assertTrue( wt.join() );
		assertTrue( wt.isFinished() );
	}
	
	@Test
	public void testFork()
	{
		PowerTask pt = new PowerTask(3, 4);
		pt.setHandler(executor);
		
		TaskFork<BigInteger> forked = pt.fork();
		
		assertSame( forked.getParent(), pt );
		
		pt.async();
		forked.async();
		
		pt.join();
		forked.join();
		
		assertEquals( pt.getResult(), forked.getResult() );
	}
	
	@Test
	public void testWaitForState()
	{
		PowerTask pt = new PowerTask(3, 87);
		pt.setHandler(executor);
		
		assertTrue( pt.async() );
		
		assertTrue( pt.waitForState(Task.Finished) );	// same as join
		assertTrue( pt.isFinished() );
	}
	
	@Test
	public void testSelfHandler()
	{
		PowerTask pt = new PowerTask(2, 4);
		
		assertTrue( pt.async() );	// this will run synchronously
		
		assertTrue( pt.isFinished() );
		assertEquals( pt.getResult(), new BigInteger("16") );
	}
	
/*	
	private class PrimeTask extends Task<Boolean> 
	{
		private long number;
		public PrimeTask(long number) {
			this.number = number;
		}
		protected Boolean execute() {
			// 1, 2, and 3 are prime
			if ((number & ~3) == 0) {
				return true;
			}
			// Even numbers are not prime
			if ((number & 1) == 0) {
				return false;
			}
			// A prime number must be in the form 6x + 1 and 6x + 5 when > 3
			long mod = number % 6;
			if (!(mod == 1 || mod == 5)) {
				return false;
			}
			
			int delta = 4;
			long sqrt = (long)Math.sqrt(number);
			// 
			for (long x = 5; x <= sqrt; x += delta) {
				// If number has a factor of x, it is not prime.
				if (number % x == 0) {
					return false;
				}
				// Toggle delta between 2 and 4.
				delta = ~delta & 0x6;
			}
			return true;
		}
		
	}
*/
	
}
