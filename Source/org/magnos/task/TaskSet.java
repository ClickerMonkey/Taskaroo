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

/**
 * A collection of tasks that do not have a particular execution order.
 * 
 * @author Philip Diffenderfer
 *
 */
public class TaskSet extends TaskCollection 
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<?> onExecute() 
	{
		// Call all tasks asynchronously
		for (Task<?> task : list) {
			task.async(); 
		}
		
		List<Object> results = new ArrayList<Object>();
		// Now wait for each one to finish and add the results to a list.
		for (int i = list.size() - 1; i >= 0; i--) {
			list.get(i).join();
			results.add(list.get(i).getResult());
			// Remove this task once completed?
			if (cleanList) {
				list.remove(i);
			}
		}
		return results;
	}

}
