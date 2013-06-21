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
 * A collection of tasks that must be executed in order, one after another.
 * 
 * @author Philip Diffenderfer
 *
 */
public class TaskList extends TaskCollection
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<?> onExecute() 
	{
		List<Object> results = new ArrayList<Object>();
		for (int i = list.size() - 1; i >= 0; i--) {
			results.add(list.get(i).sync());
			// Remove this task once completed?
			if (cleanList) {
				list.remove(i);
			}
		}
		return results;
	}

}
